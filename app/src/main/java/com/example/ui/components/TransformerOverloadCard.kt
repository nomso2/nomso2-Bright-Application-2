package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransformerOverloadTelemetry
import com.example.ui.theme.ElegantDarkBar
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkCardStart
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.Slate100Text
import com.example.ui.theme.Slate400Text
import com.example.ui.theme.Slate500Text

/**
 * Feature 7: Transformer Overload Alert Indicator
 * Real-time monitoring of local neighborhood transformer load density,
 * 3-phase voltage balance (identifying low-phase drop risks), and overload warnings.
 */
@Composable
fun TransformerOverloadCard(
    telemetry: TransformerOverloadTelemetry,
    onReportHumSpark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = telemetry.currentLoadPercent / 100f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "transformer_load_anim"
    )

    val loadColor = when {
        telemetry.currentLoadPercent >= 85 -> Color(0xFFEF4444) // Red alert
        telemetry.currentLoadPercent >= 70 -> Color(0xFFF59E0B) // Amber
        else -> Color(0xFF10B981) // Green
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transformer_overload_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkBar),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
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
                            .background(loadColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricMeter,
                            contentDescription = null,
                            tint = loadColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "LOCAL TRANSFORMER HEALTH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = ElegantGoldPrimary
                        )
                        Text(
                            text = "${telemetry.transformerId} (${telemetry.transformerCapacityKva} kVA)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Slate100Text
                        )
                    }
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(loadColor.copy(alpha = 0.15f))
                        .border(1.dp, loadColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (telemetry.isOverloaded) "OVERLOAD RISK" else "NOMINAL LOAD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = loadColor
                    )
                }
            }

            // Load Percentage Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Current Load: ${telemetry.currentLoadPercent}% of Rated Capacity",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Slate100Text
                    )
                    Text(
                        text = "Peak: ${telemetry.peakWindowText}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400Text
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = loadColor,
                    trackColor = Color(0xFF1E2430)
                )
            }

            // 3-Phase Balance Gauges
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "3-PHASE VOLTAGE BALANCE (STATUTORY: 230V ±6%):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Slate400Text
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhaseVoltageBadge(
                        phaseLabel = "Phase A (Red)",
                        volts = telemetry.phaseAVolts,
                        isWarning = telemetry.phaseAVolts < 200 || telemetry.phaseAVolts > 250,
                        modifier = Modifier.weight(1f)
                    )
                    PhaseVoltageBadge(
                        phaseLabel = "Phase B (Yellow)",
                        volts = telemetry.phaseBVolts,
                        isWarning = telemetry.phaseBVolts < 200 || telemetry.phaseBVolts > 250,
                        modifier = Modifier.weight(1f)
                    )
                    PhaseVoltageBadge(
                        phaseLabel = "Phase C (Blue)",
                        volts = telemetry.phaseCVolts,
                        isWarning = telemetry.phaseCVolts < 200 || telemetry.phaseCVolts > 250,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Household Density & Temperature Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F131A))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Connected Households",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500Text
                    )
                    Text(
                        text = "${telemetry.connectedHouseholds} / ${telemetry.designHouseholdCapacity} max threshold",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = if (telemetry.connectedHouseholds > telemetry.designHouseholdCapacity) Color(0xFFEF4444) else Slate100Text
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = if (telemetry.oilTemperatureCelsius > 70) Color(0xFFEF4444) else ElegantGoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${telemetry.oilTemperatureCelsius}°C Coil Temp",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (telemetry.oilTemperatureCelsius > 70) Color(0xFFEF4444) else Slate100Text
                    )
                }
            }

            // One-Tap Report Hum / Spark Button
            Button(
                onClick = onReportHumSpark,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x33EF4444),
                    contentColor = Color(0xFFEF4444)
                ),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("report_transformer_spark_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "REPORT ABNORMAL HUM / SPARKING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun PhaseVoltageBadge(
    phaseLabel: String,
    volts: Int,
    isWarning: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isWarning) Color(0xFFEF4444) else ElegantDarkBorder
    val bgColor = if (isWarning) Color(0x22EF4444) else Color(0xFF161B24)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = phaseLabel,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = Slate400Text,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${volts}V",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isWarning) Color(0xFFEF4444) else Color(0xFF10B981)
            )
            if (isWarning) {
                Text(
                    text = "Low Phase!",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            }
        }
    }
}
