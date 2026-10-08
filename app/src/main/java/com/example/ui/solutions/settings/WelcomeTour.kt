package com.example.ui.solutions.settings

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

private data class TourSlide(
    val title: String,
    val body: String,
    val icon: ImageVector?,
    val isSos: Boolean = false
)

private val TourSlides = listOf(
    TourSlide(
        "Report a problem in one tap",
        "On the Home screen, tap the big \"Report a problem\" button. Then choose what happened.",
        Icons.Filled.Campaign
    ),
    TourSlide(
        "Danger? Hold the red circle",
        "For fallen or sparking wires, choose Danger. Hold the red SOS circle for 2 seconds. Let go to cancel.",
        null,
        isSos = true
    ),
    TourSlide(
        "Speak your report",
        "Say what happened in your own words. No typing needed.",
        Icons.Filled.Mic
    ),
    TourSlide(
        "Send a photo or pin the place",
        "Show the problem with a photo, and pin where the fault is so the repair team finds it.",
        Icons.Filled.CameraAlt
    ),
    TourSlide(
        "No internet? Send a text",
        "Report by text message. It works with no data. Bright will play a soft chime when your light is back.",
        Icons.Filled.Sms
    )
)

/** About 30 seconds in all: each slide moves on by itself after this long. */
private const val SLIDE_MILLIS = 6_000L
private const val TICK_MILLIS = 50L

/**
 * Full-screen welcome tour: five calm slides that move on every 6 seconds, with big Next and Skip
 * buttons. Touching and holding the slide pauses it. [onDone] runs on Skip or after the last slide.
 */
@Composable
fun WelcomeTour(onDone: () -> Unit) {
    val done by rememberUpdatedState(onDone)
    var index by remember { mutableIntStateOf(0) }
    var elapsed by remember { mutableLongStateOf(0L) }
    var paused by remember { mutableStateOf(false) }
    val last = TourSlides.lastIndex

    fun next() {
        if (index < last) index++ else done()
    }

    LaunchedEffect(index) {
        elapsed = 0L
        while (elapsed < SLIDE_MILLIS) {
            delay(TICK_MILLIS)
            if (!paused) elapsed += TICK_MILLIS
        }
        next()
    }

    Dialog(
        onDismissRequest = { done() },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .testTag("welcome_tour")
        ) {
            // One bar per slide: done ones full, the current one filling, later ones empty.
            Row(
                Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Step ${index + 1} of ${TourSlides.size}" },
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TourSlides.indices.forEach { i ->
                    val p = when {
                        i < index -> 1f
                        i == index -> (elapsed.toFloat() / SLIDE_MILLIS).coerceIn(0f, 1f)
                        else -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { p },
                        modifier = Modifier.weight(1f).height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(onPress = {
                            paused = true
                            tryAwaitRelease()
                            paused = false
                        })
                    },
                contentAlignment = Alignment.Center
            ) {
                Crossfade(targetState = index, label = "tour_slide") { i ->
                    SlideContent(TourSlides[i])
                }
            }

            Text(
                text = if (paused) "Paused. Let go to carry on." else "Step ${index + 1} of ${TourSlides.size}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { done() },
                    modifier = Modifier.weight(1f).heightIn(min = 64.dp).testTag("tour_skip")
                ) { Text("Skip", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                Button(
                    onClick = { next() },
                    modifier = Modifier.weight(1f).heightIn(min = 64.dp).testTag("tour_next")
                ) { Text(if (index < last) "Next" else "Start", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun SlideContent(slide: TourSlide) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (slide.isSos) {
            Box(
                Modifier
                    .size(160.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("SOS", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Black, fontSize = 40.sp)
            }
        } else if (slide.icon != null) {
            Box(
                Modifier
                    .size(140.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(slide.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(72.dp))
            }
        }
        Text(
            slide.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            slide.body,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
