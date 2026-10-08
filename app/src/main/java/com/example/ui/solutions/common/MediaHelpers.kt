package com.example.ui.solutions.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.io.File

/** Records AAC voice notes into app-private storage (filesDir/voice_notes). */
class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var file: File? = null
    private var startedAt = 0L

    val isRecording: Boolean get() = recorder != null

    fun start(): Boolean {
        if (recorder != null) return true
        val dir = File(context.filesDir, "voice_notes").apply { mkdirs() }
        val out = File(dir, "voice_${System.currentTimeMillis()}.m4a")
        val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
        return try {
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioEncodingBitRate(64_000)
            r.setAudioSamplingRate(44_100)
            r.setOutputFile(out.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            file = out
            startedAt = System.currentTimeMillis()
            true
        } catch (e: Exception) {
            r.release()
            out.delete()
            false
        }
    }

    /** Stops and returns the saved file and its length, or null if it was too short or failed. */
    fun stop(): Pair<File, Long>? {
        val r = recorder ?: return null
        val out = file
        val duration = System.currentTimeMillis() - startedAt
        recorder = null
        file = null
        return try {
            r.stop()
            r.release()
            if (out == null || duration < 800) {
                out?.delete()
                null
            } else out to duration
        } catch (e: RuntimeException) {
            r.release()
            out?.delete()
            null
        }
    }

    fun cancel() {
        val r = recorder ?: return
        try { r.stop() } catch (_: RuntimeException) { }
        r.release()
        file?.delete()
        recorder = null
        file = null
    }
}

/** Plays one voice note at a time. */
class VoicePlayer {
    private var player: MediaPlayer? = null
    var playingPath: String? = null
        private set

    fun play(path: String, onDone: () -> Unit) {
        stop()
        try {
            val p = MediaPlayer()
            p.setDataSource(path)
            p.setOnCompletionListener {
                stop()
                onDone()
            }
            p.prepare()
            p.start()
            player = p
            playingPath = path
        } catch (e: Exception) {
            stop()
            onDone()
        }
    }

    fun stop() {
        player?.let {
            try { it.stop() } catch (_: IllegalStateException) { }
            it.release()
        }
        player = null
        playingPath = null
    }
}

/** Loud alarm used by the "Grid is back" siren and SOS confirmation. */
object SirenPlayer {
    private var ringtone: Ringtone? = null
    private val handler = Handler(Looper.getMainLooper())

    fun isPlaying(): Boolean = ringtone?.isPlaying == true

    /** Plays the chosen alert sound (gentle notification tone by default) at the chosen volume, with vibration. */
    fun play(context: Context, seconds: Int = 8) {
        stop()
        val prefs = com.example.data.solutions.SolutionsPrefs(context)
        val type = when (prefs.sirenSound) {
            "RINGTONE" -> RingtoneManager.TYPE_RINGTONE
            "NOTIFICATION" -> RingtoneManager.TYPE_NOTIFICATION
            else -> RingtoneManager.TYPE_ALARM
        }
        val uri = RingtoneManager.getDefaultUri(type)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val tone = RingtoneManager.getRingtone(context.applicationContext, uri)
        if (tone == null) {
            Toast.makeText(context, "No alarm sound on this phone", Toast.LENGTH_SHORT).show()
            return
        }
        if (Build.VERSION.SDK_INT >= 28) {
            tone.isLooping = true
            tone.volume = prefs.sirenVolume / 100f
        }
        tone.play()
        ringtone = tone
        vibrate(context, longArrayOf(0, 400, 300, 400))
        handler.postDelayed({ stop() }, seconds * 1000L)
    }

    fun stop() {
        handler.removeCallbacksAndMessages(null)
        ringtone?.stop()
        ringtone = null
    }

    fun vibrate(context: Context, pattern: LongArray) {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
}

object EvidenceFiles {
    fun newPhoto(context: Context): File {
        val dir = File(context.filesDir, "evidence").apply { mkdirs() }
        return File(dir, "photo_${System.currentTimeMillis()}.jpg")
    }

    /** Downsampled bitmap for thumbnails, so a 12 MP photo doesn't blow the heap. */
    fun thumbnail(path: String, maxPx: Int = 480): Bitmap? = try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (bounds.outWidth / sample > maxPx || bounds.outHeight / sample > maxPx) sample *= 2
        BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    } catch (e: Exception) {
        null
    }
}

/**
 * Returns an action that asks for the camera (the app declares CAMERA, so the system camera
 * intent needs it granted), takes a full-size photo into filesDir/evidence and hands back the file.
 */
@Composable
fun rememberTakePhotoAction(onPhoto: (File?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val callback by rememberUpdatedState(onPhoto)
    var pending by remember { mutableStateOf<File?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val f = pending
        pending = null
        if (ok && f != null && f.length() > 0) callback(f) else {
            f?.delete()
            callback(null)
        }
    }
    return rememberPermissionAction(
        permissions = BrightPermissions.CAMERA,
        title = "Camera for evidence",
        rationale = "Bright uses the camera only when you tap to take an evidence photo. Photos stay on this phone unless you share them."
    ) {
        val f = EvidenceFiles.newPhoto(context)
        pending = f
        try {
            launcher.launch(SolutionIntents.fileUri(context, f))
        } catch (e: Exception) {
            pending = null
            Toast.makeText(context, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }
}
