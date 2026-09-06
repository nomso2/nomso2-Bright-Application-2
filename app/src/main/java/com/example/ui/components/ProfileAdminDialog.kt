package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.LinkedMeterAsset
import com.example.model.UserProfile
import com.example.model.WhistleblowerReport
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.Slate100Text
import com.example.ui.theme.Slate400Text
import com.example.ui.theme.Slate500Text

/**
 * Phase 7: Profile Management & Administrative Protocols
 */
@Composable
fun ProfileAdminDialog(
    userProfile: UserProfile,
    linkedMeters: List<LinkedMeterAsset>,
    whistleblowerReports: List<WhistleblowerReport>,
    onSwitchMeter: (String) -> Unit,
    onSubmitWhistleblower: (target: String, extortionType: String, amount: Double, desc: String) -> Unit,
    onPurgeDataDeindexing: () -> Unit,
    onSessionTokenClearance: () -> Unit,
    onExportLedger: () -> Unit,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Multi-Asset Switcher, 1: NERC Whistleblower, 2: Security & Privacy

    // Whistleblower form states
    var officerOrUnit by remember { mutableStateOf("") }
    var extortionType by remember { mutableStateOf("Jumper / Fuse Reconnection Bribe") }
    var bribeAmount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("phase7_profile_admin_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = ElegantGoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PHASE 7: PROFILE & PROTOCOLS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = ElegantGoldPrimary
                            )
                        }
                        Text(
                            text = "Multi-Asset & NERC Compliance",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate100Text
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate400Text)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x14FFFFFF))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Meters", "Whistleblower", "Privacy & Logout").forEachIndexed { index, title ->
                        val isSelected = activeTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ElegantGoldPrimary else Color.Transparent)
                                .clickable { activeTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color(0xFF0A0C10) else Slate400Text
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (activeTab) {
                    0 -> {
                        // Multi-Asset Meter Switcher
                        Text(
                            text = "Multi-Asset Meter Switcher",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Slate100Text
                        )
                        Text(
                            text = "Switch active monitoring between primary residence, workspace, or family properties:",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Slate400Text
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        linkedMeters.forEach { asset ->
                            val isCurrent = asset.isSelected || asset.meterNumber == userProfile.meterNumber
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isCurrent) Color(0x26FACC15) else Color(0x0DFFFFFF))
                                    .border(
                                        1.dp,
                                        if (isCurrent) ElegantGoldPrimary else ElegantDarkBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSwitchMeter(asset.id) }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = asset.label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isCurrent) ElegantGoldPrimary else Slate100Text
                                            )
                                            if (isCurrent) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "ACTIVE",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF4ADE80)
                                                    )
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Meter #${asset.meterNumber} • ${asset.discoCode} (${asset.feederBand.code})",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = Slate400Text
                                        )
                                        Text(
                                            text = asset.address,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            color = Slate500Text
                                        )
                                    }

                                    if (isCurrent) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = ElegantGoldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Transactional Accounting Ledger Export
                        Button(
                            onClick = onExportLedger,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("export_accounting_ledger_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x14FFFFFF),
                                contentColor = Slate100Text
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
                        ) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Export Transactional Accounting Ledger (CSV)", fontWeight = FontWeight.Bold)
                        }
                    }

                    1 -> {
                        // Encrypted Regulatory Whistleblower Portal
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x1AEF4444))
                                    .border(1.dp, Color(0x33EF4444), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Color(0xFFEF4444))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "NERC Anti-Extortion Whistleblower Portal",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFFFCA5A5)
                                        )
                                        Text(
                                            text = "End-to-end encrypted evidence pipeline directly to NERC Compliance Directorate. Bypasses DisCo channels.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = Slate400Text
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = officerOrUnit,
                                onValueChange = { officerOrUnit = it },
                                label = { Text("DisCo Undertaking / Linesman Identifier") },
                                placeholder = { Text("e.g., Victoria Island Feeder 4 Crew / Truck #LAG-482") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFEF4444),
                                    unfocusedBorderColor = ElegantDarkBorder,
                                    focusedTextColor = Slate100Text,
                                    unfocusedTextColor = Slate100Text
                                )
                            )

                            OutlinedTextField(
                                value = bribeAmount,
                                onValueChange = { bribeAmount = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Amount Demanded (₦ Naira)") },
                                placeholder = { Text("e.g. 15000") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFEF4444),
                                    unfocusedBorderColor = ElegantDarkBorder,
                                    focusedTextColor = Slate100Text,
                                    unfocusedTextColor = Slate100Text
                                )
                            )

                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Extortion & Evidence Details") },
                                placeholder = { Text("Describe what happened, audio/photo proof available...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFEF4444),
                                    unfocusedBorderColor = ElegantDarkBorder,
                                    focusedTextColor = Slate100Text,
                                    unfocusedTextColor = Slate100Text
                                )
                            )

                            Button(
                                onClick = {
                                    val amount = bribeAmount.toDoubleOrNull() ?: 0.0
                                    onSubmitWhistleblower(
                                        officerOrUnit.ifBlank { "Unidentified DisCo Crew" },
                                        extortionType,
                                        amount,
                                        description.ifBlank { "Illegal reconnection extortion fee demanded." }
                                    )
                                    officerOrUnit = ""
                                    bribeAmount = ""
                                    description = ""
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_whistleblower_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEF4444),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Seal & Submit to NERC Officers", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    2 -> {
                        // Privacy & Compliance Data De-indexing Switch + Session Clearance
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x14FFFFFF))
                                    .border(1.dp, ElegantDarkBorder, RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFEF4444))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Compliance Data De-indexing Switch",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Slate100Text
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Purge your phone records, registered addresses, and historical telemetry metadata from this active client node.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = Slate400Text
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            onPurgeDataDeindexing()
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0x26EF4444),
                                            contentColor = Color(0xFFEF4444)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(text = "Purge & De-index Node", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x14FFFFFF))
                                    .border(1.dp, ElegantDarkBorder, RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = Slate400Text)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Session Token Clearance Protocol",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Slate100Text
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Terminates temporary cryptographic session tokens from device memory and returns to unauthenticated gateway state.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = Slate400Text
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            onSessionTokenClearance()
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0x14FFFFFF),
                                            contentColor = Slate100Text
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
                                    ) {
                                        Text(text = "Clear Session & Logout", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
