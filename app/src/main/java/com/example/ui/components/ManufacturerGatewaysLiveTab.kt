package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MeterGatewayTelemetry
import com.example.model.MeterManufacturer
import com.example.model.MeterRelayState
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.MutedSlateText
import com.example.ui.theme.Slate100Text

@Composable
fun ManufacturerGatewaysLiveTab(
    telemetryMap: Map<MeterManufacturer, MeterGatewayTelemetry?>,
    isPollingMap: Map<MeterManufacturer, Boolean>,
    onPollGateway: (MeterManufacturer) -> Unit,
    activeMeterNumber: String = "04192837461"
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CellTower,
                            contentDescription = null,
                            tint = ElegantGoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Nigerian AMI Gateway API Layer",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Real-time bidirectional telemetry simulation across Mojec International, Momas (MEMCOL), and Conlog Nigeria head-end AMI gateways. Polling retrieves live voltage, active load, power factor, and relay status directly from meter firmware.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlateText,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        items(MeterManufacturer.values()) { manufacturer ->
            val telemetry = telemetryMap[manufacturer]
            val isPolling = isPollingMap[manufacturer] == true

            ManufacturerGatewayCard(
                manufacturer = manufacturer,
                telemetry = telemetry,
                isPolling = isPolling,
                onPoll = { onPollGateway(manufacturer) },
                meterNumber = activeMeterNumber
            )
        }
    }
}

@Composable
private fun ManufacturerGatewayCard(
    manufacturer: MeterManufacturer,
    telemetry: MeterGatewayTelemetry?,
    isPolling: Boolean,
    onPoll: () -> Unit,
    meterNumber: String
) {
    val accentColor = when (manufacturer) {
        MeterManufacturer.MOJEC -> Color(0xFFF59E0B) // Amber
        MeterManufacturer.MOMAS -> Color(0xFF10B981) // Emerald Green
        MeterManufacturer.CONLOG -> Color(0xFF38BDF8) // Sky Blue
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Manufacturer & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f))
                            .border(1.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricMeter,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = manufacturer.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Protocol: ${manufacturer.defaultProtocol}",
                            fontSize = 11.sp,
                            color = MutedSlateText
                        )
                    }
                }

                Button(
                    onClick = onPoll,
                    enabled = !isPolling,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("poll_gateway_${manufacturer.name.lowercase()}")
                ) {
                    if (isPolling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Polling...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Poll Gateway", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = ElegantDarkBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(14.dp))

            // Endpoint & Gateway Target
            Text(
                text = "AMI Gateway Endpoint: ${manufacturer.apiEndpointPrefix}",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = MutedSlateText
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (telemetry != null) {
                // Real-time Power & Status Metrics Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        title = "VOLTAGE",
                        value = "${telemetry.voltageV} V",
                        subtitle = if (telemetry.voltageV >= 210.0) "Nominal" else "Low Voltage",
                        accentColor = if (telemetry.voltageV >= 210.0) Color(0xFF22C55E) else Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "ACTIVE LOAD",
                        value = "${telemetry.activePowerKw} kW",
                        subtitle = "${telemetry.currentA} A",
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "FREQUENCY",
                        value = "${telemetry.frequencyHz} Hz",
                        subtitle = "PF: ${telemetry.powerFactor}",
                        accentColor = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        title = "ENERGY UNITS",
                        value = "${telemetry.remainingCreditUnitsKwh} kWh",
                        subtitle = "Total: ${telemetry.accumulatedKwh} kWh",
                        accentColor = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1.5f)
                    )
                    MetricBox(
                        title = "RELAY STATUS",
                        value = when (telemetry.relayState) {
                            MeterRelayState.CONNECTED -> "CONNECTED"
                            MeterRelayState.DISCONNECTED -> "DISCONNECTED"
                            MeterRelayState.TRIPPED_OVERLOAD -> "TRIPPED"
                            MeterRelayState.TAMPER_SUSPENDED -> "TAMPER LOCK"
                        },
                        subtitle = "Cell RSSI: ${telemetry.signalStrengthDbm} dBm",
                        accentColor = if (telemetry.relayState == MeterRelayState.CONNECTED) Color(0xFF22C55E) else Color(0xFFEF4444),
                        modifier = Modifier.weight(1.5f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hardware Model: ${telemetry.model}",
                        fontSize = 11.sp,
                        color = Slate100Text,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Gateway Latency: ${telemetry.latencyMs} ms",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF22C55E)
                    )
                }
            } else {
                // Not polled yet placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tap 'Poll Gateway' to query ${manufacturer.displayName} cloud API for Meter #$meterNumber",
                        fontSize = 12.sp,
                        color = MutedSlateText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, ElegantDarkBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MutedSlateText,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Slate100Text.copy(alpha = 0.7f)
            )
        }
    }
}
