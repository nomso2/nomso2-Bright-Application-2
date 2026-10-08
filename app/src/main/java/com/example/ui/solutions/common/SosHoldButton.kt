package com.example.ui.solutions.common

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The big red SOS circle. Press and hold for [holdMillis] (2 s): a ring fills around the circle and
 * the alert fires when it is full. Letting go early cancels and the ring empties.
 * Screen readers get a single "Raise danger alert" action instead of the hold.
 */
@Composable
fun SosHoldButton(
    sent: Boolean,
    onTrigger: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    holdMillis: Long = 2000L
) {
    val scope = rememberCoroutineScope()
    val trigger by rememberUpdatedState(onTrigger)
    val isSent by rememberUpdatedState(sent)
    var progress by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .testTag("sos_hold_button")
                .semantics {
                    role = Role.Button
                    contentDescription = if (isSent) "SOS sent" else "SOS. Press and hold for two seconds to call for help"
                    onClick(label = "Raise danger alert") { trigger(); true }
                }
                .pointerInput(Unit) {
                    detectTapGestures(onPress = {
                        var fired = isSent
                        val steps = 40
                        val job = if (fired) null else scope.launch {
                            for (i in 1..steps) {
                                delay(holdMillis / steps)
                                progress = i / steps.toFloat()
                            }
                            fired = true
                            trigger()
                        }
                        tryAwaitRelease()
                        if (!fired) job?.cancel()
                        progress = 0f
                    })
                }
        ) {
            CircularProgressIndicator(
                progress = { if (isSent) 1f else progress },
                modifier = Modifier.size(size),
                strokeWidth = 10.dp,
                color = MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.errorContainer
            )
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.error, modifier = Modifier.size(size - 28.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            if (isSent) "SENT" else "SOS",
                            color = MaterialTheme.colorScheme.onError,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        )
                    }
                }
            }
        }
        Text(
            text = if (sent) "Help has been asked for" else "Hold for 2 seconds",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
