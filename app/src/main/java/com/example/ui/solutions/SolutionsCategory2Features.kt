package com.example.ui.solutions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldPrimary

// ==========================================
// SOLUTION 6: OFFLINE USSD / SMS DATA BRIDGE
// ==========================================
@Composable
fun OfflineUssdBridgeFeature(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedFaultCode by remember { mutableStateOf("01") } // 01: Complete Outage, 02: Low Voltage, 03: Sparking Wire

    val ussdString = "*384*55*${userProfile.meterNumber}*${selectedFaultCode}#"
    val smsString = "OUTAGE ${userProfile.meterNumber} CODE$selectedFaultCode"

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "6. Offline USSD / SMS Data Bridge (Zero Internet)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "When mobile data or fiber cuts out during a severe rainstorm, transmit emergency outage pings directly through GSM telecom signaling.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Pair("01", "Total Blackout"),
                Pair("02", "Low Voltage"),
                Pair("03", "Sparking Pole")
            ).forEach { (code, label) ->
                val isSelected = selectedFaultCode == code
                Button(
                    onClick = { selectedFaultCode = code },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }

        // USSD Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Generated Offline USSD String:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = ussdString,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val encoded = Uri.encode(ussdString)
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$encoded"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dial USSD Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(ussdString))
                            Toast.makeText(context, "Copied USSD string to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy String", fontSize = 11.sp)
                    }
                }

                Text(
                    text = "SMS Fallback Gateway: Text '$smsString' to shortcode 38455.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==========================================
// SOLUTION 7: TAMPER & CABLE THEFT CROWDSOURCING
// ==========================================
@Composable
fun TamperCrowdsourcingFeature(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isSubstationAlarmActive by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "7. Substation Cable Theft & Tamper Telemetry",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Correlates simultaneous smart-meter telemetry drops with active telecom base stations to detect ongoing copper armoring cable vandalism in real time.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(18.dp))
                        Text("Transformer Perimeter:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text(userProfile.transformerId, fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 12.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Cell Tower GSM Signal:", fontSize = 11.sp)
                    Text("100% (Normal)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Meter Telemetry Heartbeat:", fontSize = 11.sp)
                    Text("Dropped to 0% (Instant)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }

                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Text("THEFT SIGNATURE: Power severed while telecom is normal indicates physical cable cutting.", fontSize = 11.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        isSubstationAlarmActive = true
                        Toast.makeText(context, "NSCDC Infrastructure Protection Command alerted with GPS of ${userProfile.transformerId}!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSubstationAlarmActive) EmeraldAccent else MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isSubstationAlarmActive) "NSCDC & Police Patrol Alerted ✓" else "Alert NSCDC Anti-Vandalism Unit", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// SOLUTION 8: LOW-POWER "BAT-SIGNAL" MODE
// ==========================================
@Composable
fun LowPowerBatSignalFeature(
    isBatSignalMode: Boolean,
    onToggleBatSignal: (Boolean) -> Unit,
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var lastGaspTransmitted by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "8. Low-Power 'Bat-Signal' Mode",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Converts BRIGHT into an ultra-dark AMOLED pure monochrome interface that consumes <1% battery during extended blackouts, with a 140-byte emergency distress transmitter.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = if (isBatSignalMode) Color(0xFF111111) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("AMOLED Bat-Signal Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isBatSignalMode) GoldPrimary else MaterialTheme.colorScheme.onSurface)
                        Text(if (isBatSignalMode) "Active: 92% screen energy conserved" else "Inactive", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isBatSignalMode,
                        onCheckedChange = onToggleBatSignal,
                        colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary, checkedTrackColor = GoldPrimary.copy(alpha = 0.5f))
                    )
                }

                Surface(
                    color = Color.Black,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isBatSignalMode) GoldPrimary else Color.DarkGray)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("140-Byte Last Gasp Ping Payload:", fontSize = 10.sp, color = Color.Gray)
                        Text(
                            text = "PING:MTR=${userProfile.meterNumber}&TR=${userProfile.transformerId}&BATT=4%&SIG=SOS",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = if (isBatSignalMode) GoldPrimary else EmeraldAccent
                        )
                    }
                }

                Button(
                    onClick = {
                        lastGaspTransmitted = true
                        Toast.makeText(context, "Last Gasp Outage Ping transmitted to DisCo Gateway before shutdown!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (lastGaspTransmitted) "Last Gasp Ping Logged ✓" else "Send 1-Tap 'Last Gasp' Ping", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// SOLUTION 9: UNIVERSAL METER BARCODE & QR SYNC
// ==========================================
@Composable
fun UniversalMeterSyncFeature(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var simulatedBarcode by remember { mutableStateOf("62140092184") }
    var selectedManufacturer by remember { mutableStateOf("Mojec International") }
    var isSynced by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "9. Universal Meter Barcode & QR Code Sync",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Instantly reads optical barcodes and QR stamps on MAP meters (Mojec, Conlog, Memmcol) to bind feeder codes and eliminate typing mistakes.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Mojec", "Conlog", "Memmcol").forEach { brand ->
                val isSelected = selectedManufacturer.startsWith(brand)
                Button(
                    onClick = {
                        selectedManufacturer = when (brand) {
                            "Mojec" -> "Mojec International (STS-2)"
                            "Conlog" -> "Conlog Wireless (STS-1)"
                            else -> "Memmcol Nigeria Ltd"
                        }
                        simulatedBarcode = when (brand) {
                            "Mojec" -> "62140092184"
                            "Conlog" -> "04128930129"
                            else -> "45019284102"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(brand, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Optical Barcode Readout:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Icon(Icons.Default.QrCode, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(simulatedBarcode, fontFamily = FontFamily.Monospace, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                        Text(selectedManufacturer, fontSize = 10.sp, color = Color.LightGray)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Feeder Classification:", fontSize = 11.sp)
                    Text("${userProfile.feederBand.code} (${userProfile.discoCode})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent)
                }

                Button(
                    onClick = {
                        isSynced = true
                        Toast.makeText(context, "Meter $simulatedBarcode bound to ${userProfile.discoCode} cloud registry!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSynced) "Meter Synchronized ✓" else "Sync Meter with DisCo Registry", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// SOLUTION 10: NATIONAL GRID PULSE MONITOR
// ==========================================
@Composable
fun NationalGridPulseMonitorFeature(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var frequencyHz by remember { mutableStateOf(49.88f) }
    var generationMw by remember { mutableStateOf(4310) }
    var spinningReserveMw by remember { mutableStateOf(290) }

    val isFrequencyHealthy = frequencyHz in 49.80f..50.20f
    val isFrequencyCritical = frequencyHz < 49.50f || frequencyHz > 50.50f

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "10. National Grid Pulse Monitor (TCN NCC Osogbo)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Live telemetry from National Control Centre Osogbo monitoring 50.00Hz frequency stability and total generation across all 24 Nigerian power stations.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        Text("System Frequency Dial:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text(
                        text = if (isFrequencyHealthy) "GRID STABLE" else if (isFrequencyCritical) "COLLAPSE THREAT" else "UNDER-FREQUENCY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFrequencyHealthy) EmeraldAccent else if (isFrequencyCritical) MaterialTheme.colorScheme.error else Color(0xFFF59E0B)
                    )
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${String.format("%.2f", frequencyHz)} Hz",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isFrequencyHealthy) EmeraldAccent else if (isFrequencyCritical) MaterialTheme.colorScheme.error else Color(0xFFF59E0B)
                        )
                        Text("Target: 50.00 Hz • Statutory Band: 49.75 - 50.25 Hz", fontSize = 10.sp, color = Color.Gray)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Active Generation:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$generationMw MW", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Spinning Reserve:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$spinningReserveMw MW", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            frequencyHz = 49.42f
                            generationMw = 3120
                            spinningReserveMw = 60
                            Toast.makeText(context, "Simulating TCN System Under-Frequency Drop (<49.50 Hz)!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Simulate Collapse Risk", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                    }

                    Button(
                        onClick = {
                            frequencyHz = 49.96f
                            generationMw = 4350
                            spinningReserveMw = 310
                            Toast.makeText(context, "Grid telemetry refreshed from Osogbo NCC!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Live NCC Sync", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
