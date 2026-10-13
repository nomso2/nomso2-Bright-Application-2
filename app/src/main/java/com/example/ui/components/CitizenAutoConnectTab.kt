package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.service.CitizenMeterStatus
import com.example.model.SmartMeterDevice
import com.example.model.UserProfile
import com.example.ui.theme.extendedColors

/**
 * Citizen-First Auto-Connect & Status Tab
 * Automatically displays whether the citizen's meter has access to smart metering infrastructure
 * (Mojec, Momas, Conlog) and provides 1-tap auto-connection with zero technical configuration.
 */
@Composable
fun CitizenAutoConnectTab(
    userProfile: UserProfile,
    citizenMeterStatus: CitizenMeterStatus,
    activeSmartMeter: SmartMeterDevice?,
    onAutoDetectSmartMeter: () -> Unit,
    onSendOtaToken: (meterNumber: String, token: String) -> Unit,
    onPingMeter: (meterNumber: String) -> Unit,
    onToggleRelay: (meterNumber: String) -> Unit
) {
    var otaTokenInput by remember { mutableStateOf("") }
    var isSubmittingOta by remember { mutableStateOf(false) }
    var showHowItWorks by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("citizen_auto_connect_tab"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Hero Banner
        item {
            if (citizenMeterStatus.hasSmartAccess) {
                SmartMeterConnectedHeroCard(
                    userProfile = userProfile,
                    status = citizenMeterStatus,
                    activeMeter = activeSmartMeter,
                    onAutoDetect = onAutoDetectSmartMeter
                )
            } else {
                StandardMeterNonSmartHeroCard(
                    userProfile = userProfile,
                    status = citizenMeterStatus,
                    onAutoDetect = onAutoDetectSmartMeter
                )
            }
        }

        // Dedicated smart meter connection: nameplate scan, optical probe, HES (or labelled DEMO),
        // STS token, SMS/USSD backup, CIU RF / PLC status, manual readings.
        item { com.example.ui.meter.MeterConnectHub(userProfile) }
    }
}

@Composable
private fun SmartMeterConnectedHeroCard(
    userProfile: UserProfile,
    status: CitizenMeterStatus,
    activeMeter: SmartMeterDevice?,
    onAutoDetect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.extendedColors.info.copy(alpha = 0.08f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.extendedColors.info)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                    Text(
                        text = "AUTO-CONNECTED ONCE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                OutlinedButton(
                    onClick = onAutoDetect,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.extendedColors.info
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Re-scan", fontSize = 12.sp, color = MaterialTheme.extendedColors.info)
                }
            }

            Column {
                Text(
                    text = "Smart Meter Auto-Connected",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${status.manufacturerName} AMI • ${status.modelName}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.extendedColors.info
                )
            }

            Text(
                text = status.detailedExplanation,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Feeder & Meter Spec Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text("Meter Number", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(userProfile.meterNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text("Feeder Band", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(userProfile.feederBand.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text("DisCo", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(userProfile.discoCode, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun StandardMeterNonSmartHeroCard(
    userProfile: UserProfile,
    status: CitizenMeterStatus,
    onAutoDetect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.extendedColors.warning.copy(alpha = 0.08f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.extendedColors.warning)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.extendedColors.warning)
                    )
                    Text(
                        text = "STANDARD STS AREA (NO GATEWAY NEEDED)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.extendedColors.warning
                    )
                }

                Button(
                    onClick = onAutoDetect,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.extendedColors.warning, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("citizen_recheck_smart_upgrade_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Check Upgrade", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column {
                Text(
                    text = "Standard Prepaid Meter (Non-Smart)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Traditional STS Keypad Meter • Manual Token Entry",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.extendedColors.warning
                )
            }

            Text(
                text = status.detailedExplanation,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.extendedColors.warning,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "You do NOT need a smart meter server to use Bright. All fault reporting, tariff auditing, and outage alerts work normally for your meter #${userProfile.meterNumber}.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveCitizenTelemetryGrid(
    activeMeter: SmartMeterDevice?,
    onPing: () -> Unit
) {
    val voltage = activeMeter?.voltageV ?: 228.4
    val current = activeMeter?.currentA ?: 12.1
    val frequency = activeMeter?.frequencyHz ?: 50.02
    val power = activeMeter?.activePowerKw ?: 2.76

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Voltage Tile
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Line Voltage", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "%.1f V".format(voltage),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Nominal (220V - 240V)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Active Power Tile
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Load", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "%.2f kW".format(power),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "%.1f A Draw".format(current),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Frequency Tile
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Grid Frequency", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "%.2f Hz".format(frequency),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "National 50Hz Standard",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Signal APN Tile
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cellular APN", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Icon(
                            imageVector = Icons.Default.SignalCellularAlt,
                            contentDescription = null,
                            tint = MaterialTheme.extendedColors.info,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "-68 dBm",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.extendedColors.info
                    )
                    Text(
                        text = "Strong DisCo SIM Link",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Instant Ping Button
        OutlinedButton(
            onClick = onPing,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("citizen_ping_meter_btn"),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Ping Meter Line (Instant Telemetry Refresh)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
