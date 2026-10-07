package com.example.ui.solutions.common

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.solutions.SolutionsDao
import com.example.data.solutions.SolutionsDatabase
import com.example.data.solutions.SolutionsPrefs
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun rememberSolutionsDao(): SolutionsDao {
    val context = LocalContext.current
    return remember { SolutionsDatabase.get(context).dao() }
}

@Composable
fun rememberSolutionsPrefs(): SolutionsPrefs {
    val context = LocalContext.current
    return remember { SolutionsPrefs(context) }
}

/**
 * "Pin my location" with GPS (runtime permission), falling back to a typed landmark or
 * "lat, lng" when permission is refused or there is no fix.
 */
@Composable
fun LocationPinner(pinned: PinnedLocation?, onPinned: (PinnedLocation?) -> Unit) {
    val context = LocalContext.current
    var locating by remember { mutableStateOf(false) }
    var manual by remember { mutableStateOf("") }
    var showManual by remember { mutableStateOf(false) }
    val callback by rememberUpdatedState(onPinned)

    val pinWithGps = rememberPermissionAction(
        permissions = BrightPermissions.LOCATION,
        title = "Use your location",
        rationale = "Bright pins your report to where you are so the crew can drive straight to the fault. Location is read once, only when you tap.",
        onDenied = { showManual = true }
    ) {
        locating = true
        LocationFetcher.current(context) { loc ->
            locating = false
            if (loc != null) {
                callback(PinnedLocation(loc.latitude, loc.longitude, "GPS", if (loc.hasAccuracy()) loc.accuracy else null))
            } else {
                showManual = true
                Toast.makeText(context, "Couldn't find your location. You can type the place instead.", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (locating) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Text("Finding your location...", fontSize = 16.sp)
            }
        } else {
            ActionButton(
                text = if (pinned?.hasCoordinates == true) "Use my location again" else "Use my location",
                icon = Icons.Default.MyLocation,
                onClick = pinWithGps,
                outlined = pinned != null
            )
        }
        pinned?.let { p ->
            InfoNote("Place: " + p.describe())
            if (p.hasCoordinates) {
                ActionButton("See it on a map", Icons.Default.Map, {
                    SolutionIntents.openMap(context, p.latitude!!, p.longitude!!, "Fault report")
                }, outlined = true)
            }
        }
        if (showManual || pinned == null) {
            TextButton(onClick = { showManual = !showManual }) {
                Text(if (showManual) "Hide manual entry" else "Type the place instead")
            }
        }
        if (showManual) {
            TextInput("Street or landmark", manual, { manual = it })
            ActionButton("Use this location", null, {
                if (manual.isNotBlank()) callback(PinnedLocation.fromManual(manual))
            }, enabled = manual.isNotBlank(), outlined = true)
        }
    }
}

/**
 * WhatsApp-style voice note button: press and hold to record, release to save.
 * TalkBack users (who can't easily hold) get a tap-to-start / tap-to-stop action.
 */
@Composable
fun HoldToRecordButton(onRecorded: (File, Long) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val recorder = remember { VoiceRecorder(context) }
    var recording by remember { mutableStateOf(false) }
    var startedAt by remember { mutableLongStateOf(0L) }
    var elapsed by remember { mutableLongStateOf(0L) }
    val callback by rememberUpdatedState(onRecorded)

    DisposableEffect(Unit) { onDispose { recorder.cancel() } }
    LaunchedEffect(recording) {
        while (recording) {
            elapsed = System.currentTimeMillis() - startedAt
            delay(200)
        }
    }

    fun begin() {
        if (recorder.start()) {
            recording = true
            startedAt = System.currentTimeMillis()
            elapsed = 0
            SirenPlayer.vibrate(context, longArrayOf(0, 40))
        } else {
            Toast.makeText(context, "Couldn't start the microphone", Toast.LENGTH_SHORT).show()
        }
    }

    fun finish() {
        if (!recording) return
        recording = false
        val result = recorder.stop()
        if (result == null) {
            Toast.makeText(context, "Hold a little longer to record", Toast.LENGTH_SHORT).show()
        } else {
            callback(result.first, result.second)
        }
    }

    var micGranted by remember { mutableStateOf(BrightPermissions.hasAny(context, BrightPermissions.MICROPHONE)) }
    val requestMic = rememberPermissionAction(
        permissions = BrightPermissions.MICROPHONE,
        title = "Microphone for voice reports",
        rationale = "Bright records only while you hold the button. Voice notes are saved on this phone and shared only when you choose."
    ) { micGranted = true }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(88.dp)
                .background(
                    if (recording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    CircleShape
                )
                .semantics {
                    role = Role.Button
                    contentDescription = if (recording) "Recording. Double tap to stop and save" else "Hold to record a voice report"
                    onClick(label = if (recording) "Stop and save" else "Start recording") {
                        if (!micGranted) requestMic() else if (recording) finish() else begin()
                        true
                    }
                }
                .pointerInput(micGranted) {
                    detectTapGestures(onPress = {
                        if (!micGranted) {
                            requestMic()
                        } else {
                            begin()
                            tryAwaitRelease()
                            finish()
                        }
                    })
                }
        ) {
            Icon(
                if (recording) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = null,
                tint = if (recording) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(40.dp)
            )
        }
        Text(
            if (recording) "Recording ${Fmt.duration(elapsed)} - release to save" else "Hold to record",
            fontSize = 16.sp,
            color = if (recording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
