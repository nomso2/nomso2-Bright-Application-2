package com.example.ui.solutions.common

import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.R

/**
 * Bright's calm alert: a short, soft two-note chime (res/raw/bright_chime.wav, ~0.6 s with a fade)
 * and one light buzz. Used for notifications and in-app alerts so nothing startles the user.
 */
object GentleAlert {
    /** Soft buzz length and strength (1-255). */
    private const val BUZZ_MS = 80L
    private const val BUZZ_AMPLITUDE = 60

    /** Pattern for notification channels (no amplitude control there): wait 0, buzz 80 ms. */
    val CHANNEL_VIBRATION: LongArray = longArrayOf(0, BUZZ_MS)

    private var player: MediaPlayer? = null

    fun chimeUri(context: Context): Uri =
        Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.bright_chime}")

    val audioAttributes: AudioAttributes
        get() = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

    /** Plays the chime once at [volume] (0-1) and gives one soft buzz. Safe to call from any screen. */
    fun play(context: Context, volume: Float = 0.6f, buzz: Boolean = true) {
        stop()
        try {
            val p = MediaPlayer.create(context.applicationContext, R.raw.bright_chime) ?: return
            val v = volume.coerceIn(0.05f, 1f)
            p.setVolume(v, v)
            p.setOnCompletionListener { done ->
                done.release()
                if (player === done) player = null
            }
            p.start()
            player = p
        } catch (_: Exception) {
            // No sound available: the soft buzz below still tells the user.
        }
        if (buzz) buzz(context)
    }

    fun stop() {
        try {
            player?.stop()
        } catch (_: Exception) {
        }
        player?.release()
        player = null
    }

    /** One short, soft vibration: 80 ms at about a quarter strength, falling back to a plain short buzz. */
    fun buzz(context: Context) {
        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= 31) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                val amplitude = if (vibrator.hasAmplitudeControl()) BUZZ_AMPLITUDE else VibrationEffect.DEFAULT_AMPLITUDE
                vibrator.vibrate(VibrationEffect.createOneShot(BUZZ_MS, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(BUZZ_MS)
            }
        } catch (_: Exception) {
        }
    }
}
