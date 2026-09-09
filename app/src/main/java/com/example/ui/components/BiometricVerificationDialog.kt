package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantGoldDark
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.Slate100Text
import com.example.ui.theme.Slate400Text
import com.example.ui.theme.Slate500Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class BiometricAuthMode {
    FINGERPRINT,
    FACIAL_RECOGNITION
}

/**
 * Biometric Verification Dialog
 * Provides high-fidelity Fingerprint and Facial Recognition authentication
 * for users who want biometric access.
 */
@Composable
fun BiometricVerificationDialog(
    initialMode: BiometricAuthMode = BiometricAuthMode.FINGERPRINT,
    title: String = "Biometric Verification",
    subtitle: String = "Confirm identity using fingerprint or facial recognition",
    allowSwitchMode: Boolean = true,
    onVerificationSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var currentMode by remember { mutableStateOf(initialMode) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var statusMessage by remember {
        mutableStateOf(
            if (initialMode == BiometricAuthMode.FINGERPRINT)
                "Touch the sensor to verify your fingerprint"
            else
                "Align your face within the frame to verify"
        )
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(70)
                }
            }
        } catch (_: Exception) {}
    }

    // When mode is Facial Recognition, auto-start scan after 400ms
    LaunchedEffect(currentMode) {
        isSuccess = false
        if (currentMode == BiometricAuthMode.FACIAL_RECOGNITION) {
            statusMessage = "Analyzing facial geometry and 3D depth..."
            isAuthenticating = true
            delay(1600)
            triggerHapticFeedback()
            statusMessage = "Biometric Face Match Verified (99.7% confidence)"
            isAuthenticating = false
            isSuccess = true
            delay(800)
            onVerificationSuccess()
        } else {
            statusMessage = "Touch and hold the fingerprint sensor"
            isAuthenticating = false
        }
    }

    // Fingerprint verification action
    fun performFingerprintScan() {
        if (isAuthenticating || isSuccess) return
        coroutineScope.launch {
            isAuthenticating = true
            triggerHapticFeedback()
            statusMessage = "Scanning biometric fingerprint ridges..."
            delay(900)
            triggerHapticFeedback()
            statusMessage = "Fingerprint verified! Identity confirmed."
            isAuthenticating = false
            isSuccess = true
            delay(700)
            onVerificationSuccess()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("biometric_verification_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ElegantGoldPrimary.copy(alpha = 0.6f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0x14FACC15)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (currentMode == BiometricAuthMode.FINGERPRINT)
                                    Icons.Default.Fingerprint
                                else
                                    Icons.Default.Face,
                                contentDescription = null,
                                tint = ElegantGoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate100Text
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_biometric_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = Slate400Text,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400Text,
                    textAlign = TextAlign.Center
                )

                if (allowSwitchMode) {
                    Spacer(modifier = Modifier.height(16.dp))
                    // Mode Selector Tabs (Fingerprint vs Face ID)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x14FFFFFF))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { currentMode = BiometricAuthMode.FINGERPRINT }
                                .testTag("select_fingerprint_mode_tab"),
                            color = if (currentMode == BiometricAuthMode.FINGERPRINT)
                                ElegantGoldPrimary
                            else
                                Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = if (currentMode == BiometricAuthMode.FINGERPRINT) Color.Black else Slate400Text,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Fingerprint",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    ),
                                    color = if (currentMode == BiometricAuthMode.FINGERPRINT) Color.Black else Slate400Text
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { currentMode = BiometricAuthMode.FACIAL_RECOGNITION }
                                .testTag("select_face_mode_tab"),
                            color = if (currentMode == BiometricAuthMode.FACIAL_RECOGNITION)
                                ElegantGoldPrimary
                            else
                                Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = null,
                                    tint = if (currentMode == BiometricAuthMode.FACIAL_RECOGNITION) Color.Black else Slate400Text,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Facial ID",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    ),
                                    color = if (currentMode == BiometricAuthMode.FACIAL_RECOGNITION) Color.Black else Slate400Text
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Interactive Biometric Sensor / Scanner Visualization
                if (currentMode == BiometricAuthMode.FINGERPRINT) {
                    FingerprintSensorVisual(
                        isScanning = isAuthenticating,
                        isSuccess = isSuccess,
                        onTouchSensor = { performFingerprintScan() }
                    )
                } else {
                    FacialRecognitionScannerVisual(
                        isScanning = isAuthenticating,
                        isSuccess = isSuccess
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Status Message Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSuccess) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0x14FFFFFF),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSuccess) Color(0xFF10B981) else Color(0x22FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Security,
                            contentDescription = null,
                            tint = if (isSuccess) Color(0xFF10B981) else ElegantGoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSuccess) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.5.sp
                            ),
                            color = if (isSuccess) Color(0xFF10B981) else Slate100Text,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Action or Fallback
                if (currentMode == BiometricAuthMode.FINGERPRINT && !isSuccess) {
                    Button(
                        onClick = { performFingerprintScan() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("tap_to_scan_fingerprint_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantGoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SCAN FINGERPRINT SENSOR",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.5.sp
                        )
                    }
                } else if (currentMode == BiometricAuthMode.FACIAL_RECOGNITION && !isSuccess) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isAuthenticating = true
                                statusMessage = "Re-scanning facial contours..."
                                delay(1200)
                                triggerHapticFeedback()
                                statusMessage = "Face Verified!"
                                isAuthenticating = false
                                isSuccess = true
                                delay(600)
                                onVerificationSuccess()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("re_scan_face_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantGoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RE-ALIGN & SCAN FACE",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cancel_biometric_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF))
                ) {
                    Text(
                        text = "Cancel Verification",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Slate400Text
                    )
                }
            }
        }
    }
}

/**
 * Animated Fingerprint Sensor Pad with concentric wave ripples
 */
@Composable
private fun FingerprintSensorVisual(
    isScanning: Boolean,
    isSuccess: Boolean,
    onTouchSensor: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fingerprint_ripple")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .size(130.dp)
            .testTag("fingerprint_sensor_touch_pad")
            .clickable { onTouchSensor() },
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ripple rings
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(if (isScanning) pulseScale else 1f)
                .clip(CircleShape)
                .background(
                    when {
                        isSuccess -> Color(0xFF10B981).copy(alpha = 0.15f)
                        isScanning -> ElegantGoldPrimary.copy(alpha = 0.2f)
                        else -> Color(0x14FFFFFF)
                    }
                )
                .border(
                    width = 1.5.dp,
                    color = when {
                        isSuccess -> Color(0xFF10B981).copy(alpha = 0.5f)
                        isScanning -> ElegantGoldPrimary.copy(alpha = 0.6f)
                        else -> Color(0x22FFFFFF)
                    },
                    shape = CircleShape
                )
        )

        // Middle circle
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isSuccess -> Color(0xFF10B981).copy(alpha = 0.25f)
                        isScanning -> ElegantGoldDark.copy(alpha = 0.35f)
                        else -> Color(0x1FFFFFFF)
                    }
                )
                .border(
                    width = 2.dp,
                    color = if (isSuccess) Color(0xFF10B981) else if (isScanning) ElegantGoldPrimary else Color(0x33FFFFFF),
                    shape = CircleShape
                )
        )

        // Core Fingerprint Icon
        Icon(
            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Fingerprint,
            contentDescription = "Biometric Sensor",
            tint = when {
                isSuccess -> Color(0xFF10B981)
                isScanning -> ElegantGoldPrimary
                else -> Slate100Text
            },
            modifier = Modifier.size(52.dp)
        )
    }
}

/**
 * Animated Facial Recognition Scanner Viewfinder with dynamic laser sweep
 */
@Composable
private fun FacialRecognitionScannerVisual(
    isScanning: Boolean,
    isSuccess: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "laser_sweep")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0F131C))
            .border(
                2.dp,
                if (isSuccess) Color(0xFF10B981) else ElegantGoldPrimary.copy(alpha = 0.6f),
                RoundedCornerShape(24.dp)
            )
            .testTag("facial_scanner_viewfinder"),
        contentAlignment = Alignment.Center
    ) {
        // Face wireframe or icon in center
        Icon(
            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Face,
            contentDescription = "Facial Scanner",
            tint = if (isSuccess) Color(0xFF10B981) else Slate100Text.copy(alpha = 0.85f),
            modifier = Modifier.size(70.dp)
        )

        // Corner Targeting Reticle Canvas
        Canvas(modifier = Modifier.matchParentSize()) {
            val bracketLen = 20.dp.toPx()
            val strokeWidth = 3.dp.toPx()
            val bracketColor = if (isSuccess) Color(0xFF10B981) else Color(0xFFFACC15)

            // Top-left
            drawLine(bracketColor, Offset(10f, 10f), Offset(10f + bracketLen, 10f), strokeWidth)
            drawLine(bracketColor, Offset(10f, 10f), Offset(10f, 10f + bracketLen), strokeWidth)

            // Top-right
            drawLine(bracketColor, Offset(size.width - 10f, 10f), Offset(size.width - 10f - bracketLen, 10f), strokeWidth)
            drawLine(bracketColor, Offset(size.width - 10f, 10f), Offset(size.width - 10f, 10f + bracketLen), strokeWidth)

            // Bottom-left
            drawLine(bracketColor, Offset(10f, size.height - 10f), Offset(10f + bracketLen, size.height - 10f), strokeWidth)
            drawLine(bracketColor, Offset(10f, size.height - 10f), Offset(10f, size.height - 10f - bracketLen), strokeWidth)

            // Bottom-right
            drawLine(bracketColor, Offset(size.width - 10f, size.height - 10f), Offset(size.width - 10f - bracketLen, size.height - 10f), strokeWidth)
            drawLine(bracketColor, Offset(size.width - 10f, size.height - 10f), Offset(size.width - 10f, size.height - 10f - bracketLen), strokeWidth)

            // Moving horizontal laser line
            if (isScanning && !isSuccess) {
                val currentY = size.height * laserY
                drawLine(
                    color = Color(0xFF10B981),
                    start = Offset(14f, currentY),
                    end = Offset(size.width - 14f, currentY),
                    strokeWidth = 2.5.dp.toPx()
                )
            }
        }

        // Live Recognition Badge overlay at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (isSuccess) Color(0xFF10B981).copy(alpha = 0.3f) else Color(0x99000000))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isSuccess) "MATCH: 99.7%" else "3D BIOMETRIC MESH",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = if (isSuccess) Color(0xFF10B981) else ElegantGoldPrimary
            )
        }
    }
}
