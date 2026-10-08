package com.example.data.security

import android.content.SharedPreferences
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Salted SHA-256 hashing for the security PIN. Pure Kotlin, so it can be unit-tested on the JVM.
 *
 * Note: a 4 to 6 digit PIN has at most a million values, so a hash alone can't stop someone who
 * has copied the phone's private storage. The hash keeps the PIN from being readable at a glance
 * (backups, logs, rooted-phone file browsers); the real protection is the phone's own app sandbox.
 */
object PinHasher {
    private const val SALT_BYTES = 16
    private val random = SecureRandom()

    fun newSalt(): ByteArray = ByteArray(SALT_BYTES).also { random.nextBytes(it) }

    fun hash(pin: String, salt: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        digest.update(pin.trim().toByteArray(Charsets.UTF_8))
        return digest.digest()
    }

    /** Constant-time comparison, so timing doesn't leak how much of the PIN was right. */
    fun matches(pin: String, salt: ByteArray, expectedHash: ByteArray): Boolean =
        MessageDigest.isEqual(hash(pin, salt), expectedHash)

    fun toHex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it.toInt() and 0xff) }

    fun fromHex(hex: String): ByteArray? {
        if (hex.isEmpty() || hex.length % 2 != 0) return null
        return try {
            ByteArray(hex.length / 2) { i -> hex.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
        } catch (e: NumberFormatException) {
            null
        }
    }
}

/** Result of a "Forgot PIN" identity check (registered meter number + full name). */
sealed class PinResetCheck {
    /** Details matched: the user may now choose a new PIN. */
    object Verified : PinResetCheck()

    /** Details didn't match. [triesLeft] more tries before a short break. */
    data class Wrong(val triesLeft: Int) : PinResetCheck()

    /** Too many wrong tries: wait [minutesLeft] minutes. */
    data class LockedOut(val minutesLeft: Int) : PinResetCheck()

    /** There is no account (no PIN) on this phone, so there is nothing to reset. */
    object NoAccount : PinResetCheck()
}

/**
 * Keeps the security PIN as a salted SHA-256 hash in SharedPreferences, never as plain text.
 *
 * Older versions of Bright stored the PIN in plain text under [KEY_LEGACY_PLAINTEXT]. The first
 * time a PinStore is created (i.e. the first read after updating) that value is hashed, saved,
 * and the plain-text copy removed.
 *
 * Also keeps the "Forgot PIN" try counter: [MAX_RESET_TRIES] wrong tries start a
 * [RESET_LOCKOUT_MINUTES]-minute break. The counter is stored, so closing the app doesn't reset it.
 *
 * With [prefs] = null (previews, tests without Android) everything is kept in memory.
 */
class PinStore(
    private val prefs: SharedPreferences?,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    // In-memory fallback, used only when there are no SharedPreferences.
    private var memHash: String = ""
    private var memSalt: String = ""
    private var memResetFails: Int = 0
    private var memLockedUntil: Long = 0L

    init {
        migrateLegacyPlaintext()
    }

    private fun migrateLegacyPlaintext() {
        val p = prefs ?: return
        val legacy = p.getString(KEY_LEGACY_PLAINTEXT, null) ?: return
        val editor = p.edit().remove(KEY_LEGACY_PLAINTEXT)
        if (legacy.isNotBlank() && p.getString(KEY_HASH, null).isNullOrEmpty()) {
            val salt = PinHasher.newSalt()
            editor.putString(KEY_SALT, PinHasher.toHex(salt))
            editor.putString(KEY_HASH, PinHasher.toHex(PinHasher.hash(legacy, salt)))
        }
        // commit (not apply) so the plain-text copy is gone before anything else reads prefs.
        editor.commit()
    }

    private fun storedHash(): String = prefs?.getString(KEY_HASH, "") ?: memHash
    private fun storedSalt(): String = prefs?.getString(KEY_SALT, "") ?: memSalt

    /** True once a PIN has been created on this phone. */
    val isPinSet: Boolean
        get() = storedHash().isNotEmpty() && storedSalt().isNotEmpty()

    /** Checks [pin] against the stored hash. Always false when no PIN is set. */
    fun verify(pin: String): Boolean {
        if (pin.isBlank()) return false
        val salt = PinHasher.fromHex(storedSalt()) ?: return false
        val hash = PinHasher.fromHex(storedHash()) ?: return false
        return PinHasher.matches(pin, salt, hash)
    }

    /** Saves a new PIN with a fresh salt. Format checks are the caller's job. */
    fun setPin(pin: String) {
        val salt = PinHasher.newSalt()
        val saltHex = PinHasher.toHex(salt)
        val hashHex = PinHasher.toHex(PinHasher.hash(pin, salt))
        val p = prefs
        if (p == null) {
            memSalt = saltHex
            memHash = hashHex
        } else {
            p.edit()
                .putString(KEY_SALT, saltHex)
                .putString(KEY_HASH, hashHex)
                .remove(KEY_LEGACY_PLAINTEXT)
                .apply()
        }
    }

    /** Removes the PIN and the forgot-PIN counter (used by Delete account). */
    fun clear() {
        memHash = ""
        memSalt = ""
        memResetFails = 0
        memLockedUntil = 0L
        prefs?.edit()
            ?.remove(KEY_HASH)
            ?.remove(KEY_SALT)
            ?.remove(KEY_LEGACY_PLAINTEXT)
            ?.remove(KEY_RESET_FAILS)
            ?.remove(KEY_RESET_LOCKED_UNTIL)
            ?.apply()
    }

    // ---- Forgot-PIN tries --------------------------------------------------------------------

    private fun resetFails(): Int = prefs?.getInt(KEY_RESET_FAILS, 0) ?: memResetFails
    private fun lockedUntil(): Long = prefs?.getLong(KEY_RESET_LOCKED_UNTIL, 0L) ?: memLockedUntil

    private fun saveResetState(fails: Int, lockedUntil: Long) {
        memResetFails = fails
        memLockedUntil = lockedUntil
        prefs?.edit()
            ?.putInt(KEY_RESET_FAILS, fails)
            ?.putLong(KEY_RESET_LOCKED_UNTIL, lockedUntil)
            ?.apply()
    }

    /** Minutes left in a forgot-PIN break, or 0 when the user may try. */
    fun resetLockoutMinutesLeft(): Int {
        val remainingMs = lockedUntil() - clock()
        if (remainingMs <= 0L) return 0
        return ((remainingMs + 59_999L) / 60_000L).toInt().coerceAtLeast(1)
    }

    /** Records one wrong forgot-PIN try and says what happens next. */
    fun recordResetFailure(): PinResetCheck {
        val fails = resetFails() + 1
        return if (fails >= MAX_RESET_TRIES) {
            saveResetState(0, clock() + RESET_LOCKOUT_MINUTES * 60_000L)
            PinResetCheck.LockedOut(RESET_LOCKOUT_MINUTES)
        } else {
            saveResetState(fails, 0L)
            PinResetCheck.Wrong(MAX_RESET_TRIES - fails)
        }
    }

    /** Clears the forgot-PIN counter after a successful check. */
    fun clearResetFailures() = saveResetState(0, 0L)

    companion object {
        /** Where older versions kept the PIN in plain text. Read once for migration, then removed. */
        const val KEY_LEGACY_PLAINTEXT = "user_pin"
        const val KEY_HASH = "user_pin_sha256"
        const val KEY_SALT = "user_pin_salt"
        const val KEY_RESET_FAILS = "pin_reset_failed_tries"
        const val KEY_RESET_LOCKED_UNTIL = "pin_reset_locked_until"

        const val MAX_RESET_TRIES = 5
        const val RESET_LOCKOUT_MINUTES = 15
    }
}
