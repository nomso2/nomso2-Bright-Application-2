package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuditingHourRecord
import com.example.model.Complaint
import com.example.model.GridTelemetry
import com.example.model.TransformerOverloadTelemetry
import com.example.model.UserProfile
import com.example.ui.components.AuditingMatrixCard
import com.example.ui.components.ComplaintCard
import com.example.ui.components.HazardFastTrackCard
import com.example.ui.components.MeterProfileHeader
import com.example.ui.components.PowerRestorationAlertCard
import com.example.ui.components.QuickActionGrid
import com.example.ui.components.RealTimeTicker
import com.example.ui.components.TransformerOverloadCard
import com.example.ui.components.GridSurgeWarningBanner
import com.example.ui.theme.ElegantDarkBar
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkCardStart
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.ElegantGreenLive
import com.example.ui.theme.Slate100Text
import com.example.ui.theme.Slate300Text
import com.example.ui.theme.Slate400Text
import com.example.ui.theme.Slate500Text

@Composable
fun HomeScreen(
    userProfile: UserProfile,
    personalComplaints: List<Complaint>,
    telemetry: GridTelemetry,
    isDarkMode: Boolean = true,
    auditingRecords: List<AuditingHourRecord> = emptyList(),
    transformerTelemetry: TransformerOverloadTelemetry = TransformerOverloadTelemetry(),
    isRestorationAlarmEnabled: Boolean = true,
    onToggleThemeMode: () -> Unit = {},
    onReportFaultClicked: () -> Unit,
    onEmergencyHazardTriggered: (String) -> Unit,
    onEscalateComplaint: (String) -> Unit,
    onUpvoteComplaint: (String) -> Unit,
    onConfirmResolution: (String) -> Unit,
    onEditProfileClicked: () -> Unit,
    onOpenOnboarding: () -> Unit = {},
    onOpenClearinghouse: () -> Unit = {},
    onOpenTransformerForum: () -> Unit = {},
    onOpenEnergyOptimization: () -> Unit = {},
    onOpenProfileAdmin: () -> Unit = {},
    onReportTransformerHumSpark: () -> Unit = {},
    onToggleRestorationAlarm: () -> Unit = {},
    onPlayRestorationChime: () -> Unit = {},
    onNavigateMap: () -> Unit = {},
    onNavigateVandalism: () -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    onNavigateHub: () -> Unit = {},
    onOpenRedDangerSOS: () -> Unit = {},
    diagnosticStatus: String = "LOAD_SHEDDING",
    onToggleDiagnosticStatus: () -> Unit = {},
    userTrustScore: Int = 98,
    onOpenEstateExcoDossier: () -> Unit = {},
    onOpenSmartMeterGateway: () -> Unit = {},
    onLogOut: () -> Unit = {},
    surgeWarningActive: Boolean = false,
    surgeCountdownSeconds: Int = 180,
    onTriggerSurgeSiren: () -> Unit = {},
    onDismissSurgeWarning: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showToolsDropdown by remember { mutableStateOf(false) }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text(
                    text = "Log Out of Bright?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Slate100Text
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out? Your session will end and you will be returned to the sign-up and meter setup screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate400Text
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        onLogOut()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutConfirmDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = Slate100Text)
                }
            },
            containerColor = ElegantDarkBar,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Branded Header with User Display Name and Clean Action Controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElegantDarkBar)
                .border(1.dp, ElegantDarkBorder)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = userProfile.customerName.ifBlank { "Resident User" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp,
                            fontSize = 17.sp
                        ),
                        color = Slate100Text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${userProfile.discoCode} • ${userProfile.feederBand.code}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = ElegantGoldPrimary,
                        maxLines = 1
                    )
                }

                // Header Action Bar: Clean Theme Switcher & Menu for all quick tools
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Theme Switcher Button (Dark / Light)
                    IconButton(
                        onClick = onToggleThemeMode,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x1AFFFFFF))
                            .border(1.dp, ElegantDarkBorder, CircleShape)
                            .testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Light/Dark Theme",
                            tint = ElegantGoldPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Tools & Settings Menu Button
                    Box {
                        IconButton(
                            onClick = { showToolsDropdown = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x1AFFFFFF))
                                .border(1.dp, ElegantDarkBorder, CircleShape)
                                .testTag("header_tools_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Tools and Options",
                                tint = Slate100Text,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showToolsDropdown,
                            onDismissRequest = { showToolsDropdown = false },
                            modifier = Modifier.background(ElegantDarkBar)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Smart Meter Gateway", color = Slate100Text) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Router,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showToolsDropdown = false
                                    onOpenSmartMeterGateway()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Estate Exco & Dossier", color = Slate100Text) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Gavel,
                                        contentDescription = null,
                                        tint = ElegantGoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showToolsDropdown = false
                                    onOpenEstateExcoDossier()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Switch / Add Meter", color = Slate100Text) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = ElegantGoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showToolsDropdown = false
                                    onOpenOnboarding()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Profile & Protocols", color = Slate100Text) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = Slate300Text,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showToolsDropdown = false
                                    onOpenProfileAdmin()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Log Out", color = Color(0xFFEF4444)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showToolsDropdown = false
                                    showLogoutConfirmDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Ribbon: Clean, readable arrangement for Light Mode, Gateway, Exco, etc.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D1117))
                .border(1.dp, Color(0x1AFFFFFF))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Light / Dark Mode Action Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0x14FFFFFF))
                    .border(1.dp, ElegantDarkBorder, RoundedCornerShape(100.dp))
                    .clickable { onToggleThemeMode() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("ribbon_theme_toggle")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = null,
                        tint = ElegantGoldPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (isDarkMode) "Light Mode" else "Dark Mode",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Slate100Text,
                        softWrap = false,
                        maxLines = 1
                    )
                }
            }

            // Smart Meter Gateway Action Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0x1438BDF8))
                    .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(100.dp))
                    .clickable { onOpenSmartMeterGateway() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("ribbon_smart_gateway")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Smart Gateway",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF38BDF8),
                        softWrap = false,
                        maxLines = 1
                    )
                }
            }

            // Estate Exco Action Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0x14E5B869))
                    .border(1.dp, Color(0x33E5B869), RoundedCornerShape(100.dp))
                    .clickable { onOpenEstateExcoDossier() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("ribbon_estate_exco")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = ElegantGoldPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Estate Exco",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = ElegantGoldPrimary,
                        softWrap = false,
                        maxLines = 1
                    )
                }
            }

            // Switch / Add Meter Action Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0x14FFFFFF))
                    .border(1.dp, ElegantDarkBorder, RoundedCornerShape(100.dp))
                    .clickable { onOpenOnboarding() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("ribbon_switch_meter")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Slate100Text,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Switch Meter",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Slate100Text,
                        softWrap = false,
                        maxLines = 1
                    )
                }
            }

            // Security Protocols Action Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0x14FFFFFF))
                    .border(1.dp, ElegantDarkBorder, RoundedCornerShape(100.dp))
                    .clickable { onOpenProfileAdmin() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("ribbon_security_protocols")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Slate300Text,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Security",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Slate300Text,
                        softWrap = false,
                        maxLines = 1
                    )
                }
            }

            // Log Out Action Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0x14EF4444))
                    .border(1.dp, Color(0x33EF4444), RoundedCornerShape(100.dp))
                    .clickable { showLogoutConfirmDialog = true }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("ribbon_logout")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Log Out",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFFEF4444),
                        softWrap = false,
                        maxLines = 1
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // High-Pitch Grid Surge Warning Banner (Audio & 3-Min Countdown)
                if (surgeWarningActive) {
                    item {
                        GridSurgeWarningBanner(
                            countdownSeconds = surgeCountdownSeconds,
                            onDismiss = onDismissSurgeWarning
                        )
                    }
                }

                // 1. Personal Meter Profile & Connection Identity
                item {
                    MeterProfileHeader(
                        profile = userProfile,
                        onEditProfileClicked = onEditProfileClicked
                    )
                }

                // 2. Direct Action: Report Power Outage / Fault
                item {
                    Button(
                        onClick = onReportFaultClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("report_fault_banner_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantGoldPrimary,
                            contentColor = Color(0xFF0A0C10)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Report Outage / Fault",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // 3. Section Title: "MY ACTIVE COMPLAINTS"
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MY ACTIVE COMPLAINTS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Slate100Text
                            )
                            Text(
                                text = "Tracked directly with Meter #${userProfile.meterNumber}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = ElegantGoldPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (personalComplaints.isNotEmpty()) Color(0x26EF4444)
                                    else Color(0x1A22C55E)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${personalComplaints.size} Active",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (personalComplaints.isNotEmpty()) Color(0xFFEF4444) else Color(0xFF4ADE80)
                                )
                            )
                        }
                    }
                }

                // 4. Personal Complaints List / Clean WhatsApp-like empty card
                if (personalComplaints.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("empty_complaints_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = ElegantDarkSurface
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x26FACC15)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = "Light is Bright",
                                        tint = ElegantGoldPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Your Lights are Bright!",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Slate100Text
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No active faults logged for Meter #${userProfile.meterNumber} on ${userProfile.transformerId}. If power drops, lodge complaint instantly below.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400Text,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onReportFaultClicked,
                                    modifier = Modifier
                                        .height(44.dp)
                                        .testTag("report_fault_empty_state_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElegantGoldPrimary,
                                        contentColor = Color(0xFF0A0C10)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PowerOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Report Outage / Fault",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(personalComplaints, key = { it.id }) { complaint ->
                        ComplaintCard(
                            complaint = complaint,
                            userProfile = userProfile,
                            onEscalateClicked = onEscalateComplaint,
                            onUpvoteClicked = onUpvoteComplaint,
                            onConfirmResolutionClicked = onConfirmResolution,
                            onAdvanceStatusDemo = { _, _ -> }
                        )
                    }
                }

                // 5. Quick Action Grid
                item {
                    Text(
                        text = "GRID & UTILITY SERVICES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Slate100Text
                    )
                }

                item {
                    QuickActionGrid(
                        onNavigateMap = onNavigateMap,
                        onNavigateVandalism = onNavigateVandalism,
                        onNavigateHazard = { onEmergencyHazardTriggered("Immediate Transformer Fire Hazard") },
                        onNavigateHistory = onNavigateHistory,
                        onNavigateBilling = onNavigateHub,
                        onNavigateLoadShed = onNavigateHub,
                        onNavigateEscalate = {
                            if (personalComplaints.isNotEmpty()) {
                                onEscalateComplaint(personalComplaints.first().id)
                            } else {
                                onReportFaultClicked()
                            }
                        },
                        onNavigateOthers = onNavigateHub
                    )
                }

                // 6. Smart Meter Server Gateway & Nigerian AMI Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenSmartMeterGateway)
                            .testTag("smart_meter_server_gateway_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
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
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Router,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "SMART METER SERVER GATEWAY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = Color(0xFF38BDF8)
                                    )
                                    Text(
                                        text = "Connect Mojec, Momas & Conlog across Nigeria",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = Slate100Text
                                    )
                                }
                            }

                            Button(
                                onClick = onOpenSmartMeterGateway,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF38BDF8),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("open_smart_meter_gateway_btn")
                            ) {
                                Text("Manage", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 7. Real-Time National Grid Telemetry Bar
                item {
                    RealTimeTicker(telemetry = telemetry)
                }

                // 8. Power Restoration Alert Chime (Feature 9)
                item {
                    PowerRestorationAlertCard(
                        isAlarmEnabled = isRestorationAlarmEnabled,
                        transformerId = userProfile.transformerId,
                        onToggleAlarm = onToggleRestorationAlarm,
                        onTestChime = onPlayRestorationChime
                    )
                }

                // 9. Emergency Hazard Fast-Track (1-Tap SOS)
                item {
                    HazardFastTrackCard(
                        onQuickHazardSelected = onEmergencyHazardTriggered
                    )
                }

                // 10. More Tools & Comprehensive Utilities
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateHub)
                            .testTag("more_tools_section_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131722)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2937))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x1AFACC15)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = ElegantGoldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "MORE TOOLS & PROTOCOLS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = Slate100Text
                                    )
                                    Text(
                                        text = "Tariffs, diagnostics, escrow rebates, forums & policies",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = Slate400Text
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "View More",
                                tint = Slate400Text,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
