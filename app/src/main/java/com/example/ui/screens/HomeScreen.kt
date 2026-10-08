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
import androidx.compose.material.icons.filled.Check
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
import com.example.ui.theme.ThemeMode
import com.example.model.Complaint
import com.example.model.GridTelemetry
import com.example.model.TransformerOverloadTelemetry
import com.example.model.UserProfile
import com.example.ui.components.AuditingMatrixCard
import com.example.ui.components.ComplaintCard
import com.example.ui.components.HazardFastTrackCard
import com.example.ui.components.MeterProfileHeader
import com.example.ui.components.PowerRestorationAlertCard
import com.example.ui.components.QuickActionScreen
import com.example.ui.components.ReportProblemButton
import com.example.ui.components.ReportProblemSheet
import com.example.ui.components.RealTimeTicker
import com.example.ui.components.TransformerOverloadCard
import com.example.ui.components.GridSurgeWarningBanner
import com.example.ui.theme.extendedColors

@Composable
fun HomeScreen(
    state: HomeUiState,
    actions: HomeActions,
    modifier: Modifier = Modifier
) {
    // Local aliases keep the body below unchanged after collapsing ~45 parameters into
    // HomeUiState + HomeActions.
    val userProfile = state.userProfile
    val personalComplaints = state.personalComplaints
    val telemetry = state.telemetry
    val isDarkMode = state.isDarkMode
    val auditingRecords = state.auditingRecords
    val transformerTelemetry = state.transformerTelemetry
    val isRestorationAlarmEnabled = state.isRestorationAlarmEnabled
    val diagnosticStatus = state.diagnosticStatus
    val userTrustScore = state.userTrustScore
    val citizenMeterStatus = state.citizenMeterStatus
    val surgeWarningActive = state.surgeWarningActive
    val surgeCountdownSeconds = state.surgeCountdownSeconds
    val pendingSyncCount = state.pendingSyncCount
    val onSetThemeMode = actions.onSetThemeMode
    val themeMode = state.themeMode
    val onReportFaultClicked = actions.onReportFaultClicked
    val onEmergencyHazardTriggered = actions.onEmergencyHazardTriggered
    val onEscalateComplaint = actions.onEscalateComplaint
    val onUpvoteComplaint = actions.onUpvoteComplaint
    val onConfirmResolution = actions.onConfirmResolution
    val onEditProfileClicked = actions.onEditProfileClicked
    val onOpenOnboarding = actions.onOpenOnboarding
    val onOpenClearinghouse = actions.onOpenClearinghouse
    val onOpenTransformerForum = actions.onOpenTransformerForum
    val onOpenEnergyOptimization = actions.onOpenEnergyOptimization
    val onOpenProfileAdmin = actions.onOpenProfileAdmin
    val onReportTransformerHumSpark = actions.onReportTransformerHumSpark
    val onToggleRestorationAlarm = actions.onToggleRestorationAlarm
    val onPlayRestorationChime = actions.onPlayRestorationChime
    val onNavigateMap = actions.onNavigateMap
    val onNavigateVandalism = actions.onNavigateVandalism
    val onNavigateHistory = actions.onNavigateHistory
    val onNavigateHub = actions.onNavigateHub
    val onNavigateMore = actions.onNavigateMore
    val onOpenRedDangerSOS = actions.onOpenRedDangerSOS
    val onToggleDiagnosticStatus = actions.onToggleDiagnosticStatus
    val onOpenEstateExcoDossier = actions.onOpenEstateExcoDossier
    val onOpenSmartMeterGateway = actions.onOpenSmartMeterGateway
    val onAutoDetectSmartMeter = actions.onAutoDetectSmartMeter
    val onLockApp = actions.onLockApp
    val onLogOut = actions.onLogOut
    val onTriggerSurgeSiren = actions.onTriggerSurgeSiren
    val onDismissSurgeWarning = actions.onDismissSurgeWarning
    val onSyncNow = actions.onSyncNow
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showToolsDropdown by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }
    var showReportSheet by remember { mutableStateOf(false) }
    var openChoice by remember { mutableStateOf<Int?>(null) }

    if (showReportSheet) {
        ReportProblemSheet(
            onDismiss = { showReportSheet = false },
            onChoose = { number ->
                showReportSheet = false
                openChoice = number
            }
        )
    }
    openChoice?.let { number ->
        QuickActionScreen(
            number = number,
            userProfile = userProfile,
            isBatSignalMode = state.isBatSignalMode,
            onToggleBatSignalMode = actions.onToggleBatSignalMode,
            onOpenHazardForm = onOpenRedDangerSOS,
            onOpenForum = onOpenTransformerForum,
            onClose = { openChoice = null }
        )
    }

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
                        containerColor = MaterialTheme.colorScheme.error,
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
                        color = MaterialTheme.colorScheme.primary,
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
                // Reporting lives in the one big "Report a problem" button and the Report tab.
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Colours: Same as my phone / Light / Dark
                    Box {
                        IconButton(
                            onClick = { showThemeMenu = true },
                            modifier = Modifier.size(56.dp).testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Choose light or dark colours",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            ThemeMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = mode.label,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 17.sp,
                                            fontWeight = if (mode == themeMode) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = {
                                        if (mode == themeMode) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.size(24.dp))
                                        }
                                    },
                                    onClick = {
                                        showThemeMenu = false
                                        onSetThemeMode(mode)
                                    },
                                    modifier = Modifier.height(56.dp).testTag("theme_mode_${mode.name.lowercase()}")
                                )
                            }
                        }
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
                                        tint = MaterialTheme.colorScheme.primary,
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
                                text = { Text("Sign In / Switch Meter", color = MaterialTheme.colorScheme.onSurface) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
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

                // 3. The one big button: opens the five ways to report.
                item {
                    ReportProblemButton(onClick = { showReportSheet = true })
                }

                // 3b. A gentle hint only when a background helper is missing a permission.
                item {
                    GentlePermissionHints()
                }

                // Save status lives in the one small chip at the top of the app (no duplicate banner here).

                // 4. Section Title: "My reports"
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "My reports",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "For meter ${userProfile.meterNumber}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 16.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (personalComplaints.isNotEmpty()) MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${personalComplaints.size} Active",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (personalComplaints.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
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
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null, // decorative: the heading says it
                                        tint = MaterialTheme.colorScheme.primary,
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
                                    text = "No active faults logged for Meter #${userProfile.meterNumber} on ${userProfile.transformerId}. If your power goes off, tap Report a problem above.",
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
            }
        }
    }
}


/**
 * Background helpers run on their own; the only thing the user ever sees about them is a calm
 * hint when one needs a permission (notifications first, then location). Hidden when all is fine.
 */
@Composable
private fun GentlePermissionHints() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var tick by remember { mutableStateOf(0) }
    val notif = com.example.ui.solutions.common.BrightPermissions.NOTIFICATIONS
    val location = com.example.ui.solutions.common.BrightPermissions.LOCATION
    val needsNotif = tick >= 0 && notif.isNotEmpty() && !com.example.ui.solutions.common.BrightPermissions.hasAny(context, notif)
    val needsLocation = tick >= 0 && !com.example.ui.solutions.common.BrightPermissions.hasAny(context, location)
    val onGranted = {
        tick++
        com.example.ui.solutions.common.SolutionsAutomation.sync(context)
    }
    val askNotif = com.example.ui.solutions.common.rememberPermissionAction(
        permissions = notif,
        title = "Let Bright tell you things",
        rationale = "Bright plays a soft chime when your light comes back and when there is news about your report."
    ) { onGranted() }
    val askLocation = com.example.ui.solutions.common.rememberPermissionAction(
        permissions = location,
        title = "Let Bright find your street",
        rationale = "Bright uses your location only to show the repair team where the fault is."
    ) { onGranted() }

    when {
        needsNotif -> PermissionHintCard("Turn on alerts so Bright can tell you when your light is back.", askNotif)
        needsLocation -> PermissionHintCard("Allow location so the repair team can find your street.", askLocation)
        else -> Unit
    }
}

@Composable
private fun PermissionHintCard(text: String, onAllow: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("permission_hint_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onAllow,
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Allow", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
