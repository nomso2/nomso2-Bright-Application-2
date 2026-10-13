package com.example.data.meter

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Small encrypted key-value store for HES API keys: values are AES-256-GCM encrypted with a key
 * that lives in the Android Keystore (never leaves the device), then saved in SharedPreferences.
 */
class SecureStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("bright_secure_store", Context.MODE_PRIVATE)

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return gen.generateKey()
    }

    fun put(name: String, value: String) {
        if (value.isEmpty()) {
            prefs.edit().remove(name).apply()
            return
        }
        try {
            val cipher = Cipher.getInstance(TRANSFORM)
            cipher.init(Cipher.ENCRYPT_MODE, key())
            val enc = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            val packed = Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(enc, Base64.NO_WRAP)
            prefs.edit().putString(name, packed).apply()
        } catch (e: Exception) {
            // Never fall back to storing the secret in plain text.
            prefs.edit().remove(name).apply()
        }
    }

    fun get(name: String): String? {
        val packed = prefs.getString(name, null) ?: return null
        return try {
            val (ivB64, encB64) = packed.split(":", limit = 2)
            val cipher = Cipher.getInstance(TRANSFORM)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, Base64.decode(ivB64, Base64.NO_WRAP)))
            String(cipher.doFinal(Base64.decode(encB64, Base64.NO_WRAP)), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    fun remove(name: String) = prefs.edit().remove(name).apply()

    fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val ALIAS = "bright_hes_keys"
        const val TRANSFORM = "AES/GCM/NoPadding"
    }
}
