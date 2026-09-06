package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BrightViewModel
import com.example.ui.components.EditMeterDialog
import com.example.ui.components.EnergyOptimizationDialog
import com.example.ui.components.PhaseOnboardingDialog
import com.example.ui.components.ProfileAdminDialog
import com.example.ui.components.ResolutionRatingDialog
import com.example.ui.components.SignUpOnboardingScreen
import com.example.ui.components.TokenEscrowClearinghouseDialog
import com.example.ui.components.TransformerForumDialog
import com.example.ui.screens.GridHubScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveMapScreen
import com.example.ui.screens.ReportFaultScreen
import com.example.ui.screens.VandalismScreen
import com.example.ui.theme.BrightTheme
import com.example.ui.theme.ElegantDarkBar
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.Slate500Text

enum class BrightNavDestination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Home", Icons.Default.Home),
    MAP("Outage Map", Icons.Default.Map),
    REPORT("Report", Icons.Default.Add),
    VANDALISM("Anti-Theft", Icons.Default.Security),
    HISTORY("History", Icons.Default.History),
    GRID_HUB("Grid Hub", Icons.Default.Bolt)
}

class MainActivity : ComponentActivity() {

    private val viewModel: BrightViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            BrightTheme(darkTheme = isDarkMode) {
                BrightApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BrightApp(viewModel: BrightViewModel) {
    var currentDestination by remember { mutableStateOf(BrightNavDestination.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog states
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var resolvingTicketId by remember { mutableStateOf<String?>(null) }
    var showOnboardingDialog by remember { mutableStateOf(false) }
    var showClearinghouseDialog by remember { mutableStateOf(false) }
    var showTransformerForumDialog by remember { mutableStateOf(false) }
    var showEnergyOptimizationDialog by remember { mutableStateOf(false) }
    var showProfileAdminDialog by remember { mutableStateOf(false) }

    // State collections
    val userProfile by viewModel.userProfile.collectAsState()
    val personalComplaints by viewModel.activePersonalComplaints.collectAsState()
    val historyComplaints by viewModel.historicalComplaints.collectAsState()
    val outageNodes by viewModel.outageNodes.collectAsState()
    val vandalismReports by viewModel.vandalismReports.collectAsState()
    val billingDisputes by viewModel.billingDisputes.collectAsState()
    val gridTelemetry by viewModel.gridTelemetry.collectAsState()
    val maintenanceAlerts by viewModel.maintenanceAlerts.collectAsState()
    val currentLanguage by viewModel.selectedLanguage.collectAsState()
    val isLowDataMode by viewModel.isLowDataMode.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    // Phase 1 - 7 States
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val auditingRecords by viewModel.auditingRecords.collectAsState()
    val escrowTokens by viewModel.escrowRebateTokens.collectAsState()
    val escrowVaultBalanceNgn by viewModel.escrowLiquidityVaultBalanceNgn.collectAsState()
    val communityForumPosts by viewModel.communityForumPosts.collectAsState()
    val applianceBudgetList by viewModel.applianceBudgetList.collectAsState()
    val linkedMeterAssets by viewModel.linkedMeterAssets.collectAsState()
    val whistleblowerReports by viewModel.whistleblowerReports.collectAsState()
    val transformerTelemetry by viewModel.transformerTelemetry.collectAsState()
    val isRestorationAlarmEnabled by viewModel.isRestorationAlarmEnabled.collectAsState()

    // 30 Power Solutions State
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
    val isBatSignalMode by viewModel.isBatSignalMode.collectAsState()
    val diagnosticStatus by viewModel.diagnosticStatus.collectAsState()
    val userTrustScore by viewModel.userTrustScore.collectAsState()

    // If new user (not onboarded yet) or opened from menu, show the interactive sign-up flow
    if ((!isOnboardingCompleted && !userProfile.isOnboarded) || showOnboardingDialog) {
        SignUpOnboardingScreen(
            onCompleteSignUp = { newProfile ->
                viewModel.completeOnboarding(newProfile)
                showOnboardingDialog = false
            },
            onSkipForNow = {
                showOnboardingDialog = false
                viewModel.completeOnboarding(userProfile)
            }
        )
        return
    }

    // Show Snackbars when user messages are triggered
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = com.example.ui.theme.ElegantDarkCanvas,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .testTag("bright_bottom_nav_bar")
                    .border(width = 1.dp, color = ElegantDarkBorder),
                containerColor = ElegantDarkBar,
                tonalElevation = 0.dp
            ) {
                BrightNavDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination == destination,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantGoldPrimary,
                            selectedTextColor = ElegantGoldPrimary,
                            indicatorColor = Color(0x26FACC15),
                            unselectedIconColor = Slate500Text,
                            unselectedTextColor = Slate500Text
                        ),
                        modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                BrightNavDestination.HOME -> {
                    HomeScreen(
                        userProfile = userProfile,
                        personalComplaints = personalComplaints,
                        telemetry = gridTelemetry,
                        isDarkMode = isDarkMode,
                        auditingRecords = auditingRecords,
                        transformerTelemetry = transformerTelemetry,
                        isRestorationAlarmEnabled = isRestorationAlarmEnabled,
                        onToggleThemeMode = { viewModel.toggleThemeMode() },
                        onReportFaultClicked = { currentDestination = BrightNavDestination.REPORT },
                        onEmergencyHazardTriggered = { hazardName ->
                            viewModel.reportQuickEmergencyHazard(hazardName)
                        },
                        onEscalateComplaint = { id -> viewModel.escalateComplaint(id) },
                        onUpvoteComplaint = { id -> viewModel.upvoteComplaint(id) },
                        onConfirmResolution = { id -> resolvingTicketId = id },
                        onEditProfileClicked = { showEditProfileDialog = true },
                        onOpenOnboarding = { showOnboardingDialog = true },
                        onOpenClearinghouse = { showClearinghouseDialog = true },
                        onOpenTransformerForum = { showTransformerForumDialog = true },
                        onOpenEnergyOptimization = { showEnergyOptimizationDialog = true },
                        onOpenProfileAdmin = { showProfileAdminDialog = true },
                        onReportTransformerHumSpark = { viewModel.reportTransformerHumSpark() },
                        onToggleRestorationAlarm = { viewModel.toggleRestorationAlarm() },
                        onPlayRestorationChime = { viewModel.playRestorationChime() },
                        onNavigateMap = { currentDestination = BrightNavDestination.MAP },
                        onNavigateVandalism = { currentDestination = BrightNavDestination.VANDALISM },
                        onNavigateHistory = { currentDestination = BrightNavDestination.HISTORY },
                        onNavigateHub = { currentDestination = BrightNavDestination.GRID_HUB },
                        onOpenRedDangerSOS = { viewModel.triggerRedDangerEmergency() },
                        diagnosticStatus = diagnosticStatus,
                        onToggleDiagnosticStatus = { viewModel.toggleDiagnosticStatus() },
                        userTrustScore = userTrustScore
                    )
                }

                BrightNavDestination.MAP -> {
                    LiveMapScreen(
                        userProfile = userProfile,
                        outageNodes = outageNodes,
                        onRefreshMap = {
                            viewModel.showNotification("Refreshing SCADA transmission lines and transformer statuses...")
                        }
                    )
                }

                BrightNavDestination.REPORT -> {
                    ReportFaultScreen(
                        userProfile = userProfile,
                        onBack = { currentDestination = BrightNavDestination.HOME },
                        onSubmit = { title, desc, faultType, isHazard, mediaUri, isVideo ->
                            viewModel.reportFault(title, desc, faultType, isHazard, mediaUri, isVideo)
                            currentDestination = BrightNavDestination.HISTORY
                        }
                    )
                }

                BrightNavDestination.VANDALISM -> {
                    VandalismScreen(
                        userProfile = userProfile,
                        reports = vandalismReports,
                        onSubmitReport = { incident, loc, land, isAnon, desc, suspects ->
                            viewModel.reportVandalism(incident, loc, land, isAnon, desc, suspects)
                        }
                    )
                }

                BrightNavDestination.HISTORY -> {
                    HistoryScreen(
                        userProfile = userProfile,
                        historicalComplaints = historyComplaints,
                        billingDisputes = billingDisputes
                    )
                }

                BrightNavDestination.GRID_HUB -> {
                    GridHubScreen(
                        userProfile = userProfile,
                        maintenanceAlerts = maintenanceAlerts,
                        currentLanguage = currentLanguage,
                        isLowDataMode = isLowDataMode,
                        onLanguageSelected = { viewModel.setLanguage(it) },
                        onToggleLowData = { viewModel.toggleLowDataMode() },
                        onSubmitBillingDispute = { type, amount, month, desc ->
                            viewModel.submitBillingDispute(type, amount, month, desc)
                        },
                        isBatSignalMode = isBatSignalMode,
                        onToggleBatSignalMode = { viewModel.toggleBatSignalMode(it) },
                        onOpenRedDangerSOS = { viewModel.triggerRedDangerEmergency() },
                        onOpenForum = { showTransformerForumDialog = true },
                        onPlaySirenAlarm = { viewModel.playRestorationChime() }
                    )
                }
            }
        }
    }

    // Modal dialog for editing user's linked meter profile
    if (showEditProfileDialog) {
        EditMeterDialog(
            currentProfile = userProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { updated ->
                viewModel.saveUserProfile(updated)
            }
        )
    }

    // Modal dialog for verifying light has been restored and rating the field crew
    resolvingTicketId?.let { ticketId ->
        ResolutionRatingDialog(
            ticketId = ticketId,
            onDismiss = { resolvingTicketId = null },
            onConfirmResolution = { rating, notes ->
                viewModel.resolveComplaint(ticketId, rating, notes)
                resolvingTicketId = null
            }
        )
    }

    // Phase 1: Onboarding & SIM-Authenticated Verification Dialog
    if (showOnboardingDialog) {
        PhaseOnboardingDialog(
            currentProfile = userProfile,
            onDismiss = { showOnboardingDialog = false },
            onCompleteOnboarding = { meterNum, disco, band, address, paymentGateway ->
                viewModel.verifyAndOnboardMeter(meterNum, disco, band, address, paymentGateway)
            }
        )
    }

    // Phase 4: Token Escrow & AI Settlement Clearinghouse
    if (showClearinghouseDialog) {
        TokenEscrowClearinghouseDialog(
            userProfile = userProfile,
            escrowTokens = escrowTokens,
            escrowVaultBalanceNgn = escrowVaultBalanceNgn,
            onClaimToken = { tokenId -> viewModel.claimRebateToken(tokenId) },
            onGenerateManualRebate = { viewModel.requestEmergencyRebateGeneration() },
            onCompileNercReport = { viewModel.compileNercEnforcementReport() },
            onDismiss = { showClearinghouseDialog = false }
        )
    }

    // Phase 5: Transformer Cluster Forum & Voice AI Parser
    if (showTransformerForumDialog) {
        TransformerForumDialog(
            userProfile = userProfile,
            posts = communityForumPosts,
            onPostMessage = { content, isExtortion -> viewModel.postCommunityMessage(content, isExtortion) },
            onUpvotePost = { postId -> viewModel.upvoteCommunityPost(postId) },
            onTriggerPeerBroadcast = {
                viewModel.showNotification("📢 Geofenced outage broadcast dispatched to ${userProfile.connectedHouseholdsCount} neighbor meters on ${userProfile.transformerId}!")
            },
            onSimulateVoiceReport = { lang ->
                viewModel.showNotification("🎙️ $lang speech audio converted to SCADA fault ticket #TR-VOC-${(1000..9999).random()}")
            },
            onDismiss = { showTransformerForumDialog = false }
        )
    }

    // Phase 6: Energy Load Management & Surge Guard
    if (showEnergyOptimizationDialog) {
        EnergyOptimizationDialog(
            userProfile = userProfile,
            appliances = applianceBudgetList,
            isAlarmEnabled = isRestorationAlarmEnabled,
            onToggleAlarm = { viewModel.toggleRestorationAlarm() },
            onPlaySirenTest = { viewModel.playRestorationChime() },
            onToggleEco = { appId -> viewModel.toggleApplianceEco(appId) },
            onTriggerSurgeWarning = {
                viewModel.showNotification("⚠️ T-5 MIN SURGE WARNING: Feeder line energization in 5 minutes! Unplug high-draw appliances immediately.")
            },
            onDismiss = { showEnergyOptimizationDialog = false }
        )
    }

    // Phase 7: Profile Management & Administrative Protocols
    if (showProfileAdminDialog) {
        ProfileAdminDialog(
            userProfile = userProfile,
            linkedMeters = linkedMeterAssets,
            whistleblowerReports = whistleblowerReports,
            onSwitchMeter = { assetId -> viewModel.switchActiveMeter(assetId) },
            onSubmitWhistleblower = { target, extType, amt, desc ->
                viewModel.submitWhistleblowerReport(target, extType, amt, desc)
            },
            onPurgeDataDeindexing = { viewModel.purgeUserDataDeindexing() },
            onSessionTokenClearance = { viewModel.sessionTokenClearance() },
            onExportLedger = {
                viewModel.showNotification("📄 Transactional Accounting Ledger exported: BRIGHT_LEDGER_${userProfile.meterNumber}.csv downloaded")
            },
            onDismiss = { showProfileAdminDialog = false }
        )
    }
}
