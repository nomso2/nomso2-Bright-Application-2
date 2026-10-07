package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.FlashOn
import com.example.data.service.CitizenMeterStatus
import com.example.data.service.NigeriaSmartMeterDiscoveryService
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
import androidx.compose.material3.TextButton
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
    onNavigateMore: () -> Unit = {},
    onOpenRedDangerSOS: () -> Unit = {},
    diagnosticStatus: String = "LOAD_SHEDDING",
    onToggleDiagnosticStatus: () -> Unit = {},
    userTrustScore: Int = 98,
    onOpenEstateExcoDossier: () -> Unit = {},
    onOpenSmartMeterGateway: () -> Unit = {},
    citizenMeterStatus: CitizenMeterStatus? = null,
    onAutoDetectSmartMeter: () -> Unit = {},
    onLockApp: () -> Unit = {},
    onLogOut: () -> Unit = {},
    surgeWarningActive: Boolean = false,
    surgeCountdownSeconds: Int = 180,
    onTriggerSurgeSiren: () -> Unit = {},
    onDismissSurgeWarning: () -> Unit = {},
    pendingSyncCount: Int = 0,
    onSyncNow: () -> Unit = {},
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
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out? Your session will end and you will be returned to the sign-up and meter setup screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Branded Header with User Display Name and Clean Action Controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "THE BRIGHT PROJECT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        ),
                        color = ElegantGoldPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${userProfile.discoCode} • ${userProfile.feederBand.code}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }

                // Header actions: at most two icons (theme toggle + overflow menu).
                // Report lives in the big gold button and the bottom bar; other tools live in More.
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleThemeMode,
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) "Switch to light theme" else "Switch to dark theme",
                            tint = ElegantGoldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showToolsDropdown = true },
                            modifier = Modifier.testTag("header_tools_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showToolsDropdown,
                            onDismissRequest = { showToolsDropdown = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Lock App", color = MaterialTheme.colorScheme.onSurface) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = ElegantGoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showToolsDropdown = false
                                    onLockApp()
                                },
                                modifier = Modifier.testTag("header_menu_lock")
                            )
                            DropdownMenuItem(
                                text = { Text("Profile & Security", color = MaterialTheme.colorScheme.onSurface) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showToolsDropdown = false
                                    onOpenProfileAdmin()
                                },
                                modifier = Modifier.testTag("header_security_protocols_button")
                            )
                            DropdownMenuItem(
                                text = { Text("Estate & Dossier", color = MaterialTheme.colorScheme.onSurface) },
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
                                },
                                modifier = Modifier.testTag("header_estate_exco_button")
                            )
                            DropdownMenuItem(
                                text = { Text("Smart Meter", color = MaterialTheme.colorScheme.onSurface) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Router,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showToolsDropdown = false
                                    onOpenSmartMeterGateway()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sign In / Switch Meter", color = MaterialTheme.colorScheme.onSurface) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
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
                                text = { Text("Log Out", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showToolsDropdown = false
                                    showLogoutConfirmDialog = true
                                },
                                modifier = Modifier.testTag("header_menu_logout")
                            )
                        }
                    }
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
                // 1. High-Pitch Grid Surge Warning Banner (Audio & 3-Min Countdown)
                if (surgeWarningActive) {
                    item {
                        GridSurgeWarningBanner(
                            countdownSeconds = surgeCountdownSeconds,
                            onDismiss = onDismissSurgeWarning
                        )
                    }
                }

                // 2. Personal Meter Profile & Connection Identity
                item {
                    MeterProfileHeader(
                        profile = userProfile,
                        onEditProfileClicked = onEditProfileClicked
                    )
                }

                // 3. Direct Action: Report Power Outage / Fault
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

                // Offline queue status: shown high up only when reports are waiting to send
                if (pendingSyncCount > 0) {
                    item {
                        OfflineSyncStatusCard(pendingSyncCount = pendingSyncCount, onSyncNow = onSyncNow)
                    }
                }

                // 4. Section Title: "MY ACTIVE COMPLAINTS"
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
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Tracked directly with Meter #${userProfile.meterNumber}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
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

                // 4b. Personal complaints list or empty state (the Report button above is the single entry point)
                if (personalComplaints.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("empty_complaints_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
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
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No active faults logged for Meter #${userProfile.meterNumber} on ${userProfile.transformerId}. If your power goes off, tap Report Outage above.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
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

                // 5. Compact Tools grid
                item {
                    Text(
                        text = "TOOLS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
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
                        onNavigateOthers = onNavigateMore
                    )
                }

                // 6. Emergency Hazard Fast-Track (1-Tap SOS), kept on Home because it is safety-critical
                item {
                    HazardFastTrackCard(
                        onQuickHazardSelected = onEmergencyHazardTriggered
                    )
                }

                // 7. Citizen Smart Meter Gateway Card (Auto-Connected or Standard STS)
                item {
                    val meterStatus = citizenMeterStatus ?: remember(userProfile) {
                        NigeriaSmartMeterDiscoveryService.checkSmartMeterAccess(userProfile)
                    }
                    val isSmart = meterStatus.hasSmartAccess
                    val themeColor = if (isSmart) Color(0xFF38BDF8) else Color(0xFFD97706)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenSmartMeterGateway)
                            .testTag("smart_meter_server_gateway_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, themeColor.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(themeColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSmart) Icons.Default.Router else Icons.Default.ElectricMeter,
                                        contentDescription = null,
                                        tint = themeColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (isSmart) "SMART METER AUTO-CONNECTED" else "STANDARD PREPAID METER",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = themeColor
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isSmart) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFFD97706).copy(alpha = 0.15f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = if (isSmart) "AUTO-LINKED" else "STS KEYPAD",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSmart) Color(0xFF22C55E) else Color(0xFFD97706)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (isSmart)
                                            "✓ Auto-connected to ${meterStatus.manufacturerName} • 228V"
                                        else
                                            "Non-Smart Area • Standard 20-digit token keypad meter",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = onOpenSmartMeterGateway,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = themeColor,
                                    contentColor = if (isSmart) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("open_smart_meter_gateway_btn")
                            ) {
                                Text(if (isSmart) "View" else "Status", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 8. More Tools (opens the More tab) & Comprehensive Utilities
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateMore)
                            .testTag("more_tools_section_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
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
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Tariffs, diagnostics, escrow rebates, forums & policies",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "View More",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // 9. Offline status when nothing is waiting (low priority)
                if (pendingSyncCount == 0) {
                    item {
                        OfflineSyncStatusCard(pendingSyncCount = 0, onSyncNow = onSyncNow)
                    }
                }

                // 10. Real-Time National Grid Telemetry Bar
                item {
                    RealTimeTicker(telemetry = telemetry)
                }

                // 11. Power Restoration Alert Chime (Feature 9)
                item {
                    PowerRestorationAlertCard(
                        isAlarmEnabled = isRestorationAlarmEnabled,
                        transformerId = userProfile.transformerId,
                        onToggleAlarm = onToggleRestorationAlarm,
                        onTestChime = onPlayRestorationChime
                    )
                }
            }
        }
    }
}

/**
 * Plain-language status of reports saved on the phone that haven't reached the server yet.
 */
@Composable
private fun OfflineSyncStatusCard(
    pendingSyncCount: Int,
    onSyncNow: () -> Unit
) {
    val hasPending = pendingSyncCount > 0
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("offline_cache_status_card"),
        colors = CardDefaults.cardColors(
            containerColor = if (hasPending) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (hasPending) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (hasPending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            shape = CircleShape
                        )
                )
                Column {
                    Text(
                        text = when {
                            pendingSyncCount == 1 -> "1 report waiting to send"
                            hasPending -> "$pendingSyncCount reports waiting to send"
                            else -> "All caught up"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Your reports are saved on this phone and send when you're back online.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (hasPending) {
                TextButton(
                    onClick = onSyncNow,
                    modifier = Modifier.testTag("offline_sync_button")
                ) {
                    Text(
                        text = "Send now",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
