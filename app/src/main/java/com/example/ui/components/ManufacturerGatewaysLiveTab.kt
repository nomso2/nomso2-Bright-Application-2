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
import com.example.ui.theme.extendedColors

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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CellTower,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Nigerian AMI Gateway API Layer",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Real-time bidirectional telemetry simulation across Mojec International, Momas (MEMCOL), and Conlog Nigeria head-end AMI gateways. Polling retrieves live voltage, active load, power factor, and relay status directly from meter firmware.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        MeterManufacturer.MOJEC -> MaterialTheme.extendedColors.warning // Amber
        MeterManufacturer.MOMAS -> MaterialTheme.extendedColors.success // Emerald Green
        MeterManufacturer.CONLOG -> MaterialTheme.extendedColors.info // Sky Blue
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Protocol: ${manufacturer.defaultProtocol}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(14.dp))

            // Endpoint & Gateway Target
            Text(
                text = "AMI Gateway Endpoint: ${manufacturer.apiEndpointPrefix}",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        accentColor = if (telemetry.voltageV >= 210.0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
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
                        accentColor = MaterialTheme.extendedColors.info,
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
                        accentColor = MaterialTheme.extendedColors.warning,
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
                        accentColor = if (telemetry.relayState == MeterRelayState.CONNECTED) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
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
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Gateway Latency: ${telemetry.latencyMs} ms",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                // Not polled yet placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tap 'Poll Gateway' to query ${manufacturer.displayName} cloud API for Meter #$meterNumber",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
