package com.example

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.BrightViewModel
import com.example.ui.components.EditMeterDialog
import com.example.ui.components.EnergyOptimizationDialog
import com.example.ui.components.EstateExcoAndSlaDossierDialog
import com.example.ui.components.ProfileAdminDialog
import com.example.ui.components.DeleteAccountDialog
import com.example.ui.components.RoomDatabaseSyncDialog
import com.example.ui.components.RoomSyncStatusBar
import com.example.ui.components.ResolutionRatingDialog
import com.example.ui.components.SessionLockScreen
import com.example.ui.components.SignUpOnboardingScreen
import com.example.ui.components.SmartMeterServerGatewayDialog
import com.example.ui.components.TokenEscrowClearinghouseDialog
import com.example.ui.components.TransformerForumDialog
import com.example.ui.screens.GridHubScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeActions
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HomeUiState
import com.example.ui.screens.LiveMapScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.ReportFaultScreen
import com.example.ui.screens.VandalismScreen
import com.example.ui.solutions.settings.SolutionsFirstRun
import com.example.ui.solutions.settings.WelcomeTour
import com.example.ui.solutions.settings.SolutionsSettingsScreen
import com.example.ui.theme.BrightTheme

/** The five bottom-bar tabs (Material 3 recommends at most five). */
enum class BrightNavDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    HOME("home", "Home", Icons.Default.Home),
    MAP("map", "Map", Icons.Default.Map),
    REPORT("report", "Report", Icons.Default.Add),
    HISTORY("history", "History", Icons.Default.History),
    MORE("more", "More", Icons.Default.Menu)
}

/** Screens reached from the More tab; they keep the More tab highlighted. */
object BrightSubRoutes {
    const val ANTI_THEFT = "more/anti_theft"
    const val GRID_HUB = "more/grid_hub"
    const val HELP = "more/help"
    /** Bright tools settings; "?solution=n" opens one tool's page directly (0 = the list). */
    const val SOLUTIONS_SETTINGS = "more/solutions_settings?solution={solution}"
    fun solutionsSettings(solution: Int) = "more/solutions_settings?solution=$solution"
}

// FragmentActivity (a ComponentActivity subclass) is required by androidx.biometric's BiometricPrompt.
// setContent from activity-compose still works because it is an extension on ComponentActivity.
class MainActivity : FragmentActivity() {

    private val viewModel: BrightViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val isDarkMode = when (themeMode) {
                com.example.ui.theme.ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                com.example.ui.theme.ThemeMode.LIGHT -> false
                com.example.ui.theme.ThemeMode.DARK -> true
            }
            BrightTheme(darkTheme = isDarkMode) {
                BrightApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BrightApp(viewModel: BrightViewModel) {
    // Navigation Compose back stack: system back pops sub-screens, then returns to Home.
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog states
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var resolvingTicketId by remember { mutableStateOf<String?>(null) }
    var showOnboardingDialog by remember { mutableStateOf(false) }
    var showClearinghouseDialog by remember { mutableStateOf(false) }
    var showTransformerForumDialog by remember { mutableStateOf(false) }
    var showEnergyOptimizationDialog by remember { mutableStateOf(false) }
    var showProfileAdminDialog by remember { mutableStateOf(false) }
    var showEstateExcoDialog by remember { mutableStateOf(false) }
    var showSmartMeterGatewayDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

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

    // Phase 1 - 7 States & New Features
    val isDarkMode = com.example.ui.theme.LocalIsDarkTheme.current
    val themeMode by viewModel.themeMode.collectAsState()
    val auditingRecords by viewModel.auditingRecords.collectAsState()
    val escrowTokens by viewModel.escrowRebateTokens.collectAsState()
    val escrowVaultBalanceNgn by viewModel.escrowLiquidityVaultBalanceNgn.collectAsState()
    val communityForumPosts by viewModel.communityForumPosts.collectAsState()
    val applianceBudgetList by viewModel.applianceBudgetList.collectAsState()
    val linkedMeterAssets by viewModel.linkedMeterAssets.collectAsState()
    val whistleblowerReports by viewModel.whistleblowerReports.collectAsState()
    val transformerTelemetry by viewModel.transformerTelemetry.collectAsState()
    val isRestorationAlarmEnabled by viewModel.isRestorationAlarmEnabled.collectAsState()
    val transformerDuesEntries by viewModel.transformerDuesEntries.collectAsState()
    val surgeWarningActive by viewModel.surgeWarningActive.collectAsState()
    val surgeCountdownSeconds by viewModel.surgeCountdownSeconds.collectAsState()

    // 30 Power Solutions State
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
    val isBatSignalMode by viewModel.isBatSignalMode.collectAsState()
    val diagnosticStatus by viewModel.diagnosticStatus.collectAsState()
    val userTrustScore by viewModel.userTrustScore.collectAsState()

    // Nigeria Smart Meter Server Gateway State
    val smartMeterServerConfig by viewModel.smartMeterServerConfig.collectAsState()
    val smartMetersList by viewModel.smartMetersList.collectAsState()
    val smartMeterCommands by viewModel.smartMeterCommands.collectAsState()
    val gatewayTelemetryMap by viewModel.gatewayTelemetryMap.collectAsState()
    val isPollingGateway by viewModel.isPollingGateway.collectAsState()
    val paidMeterNumbers by viewModel.paidMeterNumbers.collectAsState()
    val activationPaymentState by viewModel.activationPaymentState.collectAsState()
    val citizenMeterStatus by viewModel.citizenMeterStatus.collectAsState()

    // Session Lock & Re-Login State (Auto-Lock on leaving app)
    val isAppLocked by viewModel.isAppLocked.collectAsState()
    val requireLoginOnLeave by viewModel.requireLoginOnLeave.collectAsState()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val lastSyncTimeText by viewModel.lastSyncTimeText.collectAsState()
    val pendingSyncActions by viewModel.pendingSyncActions.collectAsState()
    var showRoomSyncDialog by remember { mutableStateOf(false) }
    val isPinSet by viewModel.pinSet.collectAsState()

    // Auto-lock when user leaves the app (presses Home, switches apps, locks screen)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, requireLoginOnLeave, isOnboardingCompleted, userProfile.isOnboarded) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                if (requireLoginOnLeave && (isOnboardingCompleted || userProfile.isOnboarded)) {
                    viewModel.lockAppSession()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // If new user (not onboarded yet) or opened from menu, show the interactive sign-in / sign-up flow
    if ((!isOnboardingCompleted && !userProfile.isOnboarded) || showOnboardingDialog) {
        val canDismissOnboarding = showOnboardingDialog && (isOnboardingCompleted || userProfile.isOnboarded)
        // System back closes the sign-in / switch-meter screen when it was opened from the app.
        BackHandler(enabled = canDismissOnboarding) { showOnboardingDialog = false }
        SignUpOnboardingScreen(
            currentProfile = userProfile,
            // A phone with an account on it opens on Sign in; otherwise on Register.
            initialSignInMode = isPinSet,
            isDismissible = canDismissOnboarding,
            paidMeters = paidMeterNumbers,
            paymentState = activationPaymentState,
            onStartPayment = { meterNum -> viewModel.startActivationPayment(meterNum) },
            onResetPayment = { viewModel.resetActivationPayment() },
            onDismiss = { showOnboardingDialog = false },
            onCompleteSignUp = { newProfile, newPin ->
                viewModel.completeOnboarding(newProfile, newPin)
                showOnboardingDialog = false
            },
            onSignIn = { signedInProfile ->
                viewModel.signIn(signedInProfile)
                showOnboardingDialog = false
            },
            isPinSet = isPinSet,
            verifyPin = { pin -> viewModel.verifyPin(pin) },
            onDeleteAccount = { viewModel.deleteAccount() },
            onCheckPinResetDetails = { meter, name -> viewModel.checkPinResetDetails(meter, name) },
            onResetPin = { newPin -> viewModel.resetPinAfterCheck(newPin) }
        )
        return
    }

    // If session is locked (e.g. after leaving the app or pressing Lock button), require re-login
    if (isAppLocked && (isOnboardingCompleted || userProfile.isOnboarded)) {
        SessionLockScreen(
            userProfile = userProfile,
            requireLoginOnLeave = requireLoginOnLeave,
            onToggleRequireLoginOnLeave = { enabled ->
                viewModel.setRequireLoginOnLeave(enabled)
            },
            onUnlockWithPin = { pin ->
                viewModel.unlockAppSessionWithPin(pin)
            },
            onUnlockBiometric = {
                // Invoked only from BiometricPrompt's onAuthenticationSucceeded callback.
                viewModel.unlockAppSessionBiometric()
            },
            isPinSet = isPinSet,
            onCreatePin = { newPin -> viewModel.createPinAndUnlock(newPin) },
            onSwitchAccount = {
                showOnboardingDialog = true
            },
            onLogOut = {
                viewModel.logOut()
            },
            onCheckPinResetDetails = { meter, name -> viewModel.checkPinResetDetails(meter, name) },
            onResetPin = { newPin -> viewModel.resetPinAfterCheck(newPin) },
            onDeleteAccount = { viewModel.deleteAccount() }
        )
        return
    }

    // Bright tools: keep background checks in step with Settings; calm one-time welcome + permissions.
    SolutionsFirstRun(userProfile)

    // "Show the tour again" from More.
    var replayTour by remember { mutableStateOf(false) }
    if (replayTour) {
        WelcomeTour(onDone = { replayTour = false })
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
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            val selectedTab = when {
                currentRoute == null -> BrightNavDestination.HOME
                currentRoute.startsWith("more") -> BrightNavDestination.MORE
                else -> BrightNavDestination.entries.firstOrNull { it.route == currentRoute } ?: BrightNavDestination.HOME
            }
            NavigationBar(
                modifier = Modifier
                    .testTag("bright_bottom_nav_bar")
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                BrightNavDestination.entries.forEach { destination ->
                    val isSelected = selectedTab == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { navController.navigateToTab(destination) },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = null // the visible label already names the tab
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
        // Room database sync & offline mode status bar
        RoomSyncStatusBar(
            isSyncing = isSyncing,
            isOfflineMode = isOfflineMode,
            pendingSyncCount = pendingSyncCount,
            lastSyncTime = lastSyncTimeText,
            onSyncNow = { viewModel.syncOfflineQueue() },
            onToggleOfflineMode = { viewModel.toggleOfflineMode() },
            onOpenDetails = { showRoomSyncDialog = true }
        )
        NavHost(
            navController = navController,
            startDestination = BrightNavDestination.HOME.route,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            composable(BrightNavDestination.HOME.route) {
                HomeScreen(
                    state = HomeUiState(
                        userProfile = userProfile,
                        personalComplaints = personalComplaints,
                        telemetry = gridTelemetry,
                        isDarkMode = isDarkMode,
                        themeMode = themeMode,
                        auditingRecords = auditingRecords,
                        transformerTelemetry = transformerTelemetry,
                        isRestorationAlarmEnabled = isRestorationAlarmEnabled,
                        diagnosticStatus = diagnosticStatus,
                        userTrustScore = userTrustScore,
                        citizenMeterStatus = citizenMeterStatus,
                        surgeWarningActive = surgeWarningActive,
                        surgeCountdownSeconds = surgeCountdownSeconds,
                        pendingSyncCount = pendingSyncCount,
                        isBatSignalMode = isBatSignalMode
                    ),
                    actions = HomeActions(
                        onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
                        onReportFaultClicked = { navController.navigateToTab(BrightNavDestination.REPORT) },
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
                        onNavigateMap = { navController.navigateToTab(BrightNavDestination.MAP) },
                        onNavigateVandalism = { navController.navigateToMoreSubScreen(BrightSubRoutes.ANTI_THEFT) },
                        onNavigateHistory = { navController.navigateToTab(BrightNavDestination.HISTORY) },
                        onNavigateHub = { navController.navigateToMoreSubScreen(BrightSubRoutes.GRID_HUB) },
                        onNavigateMore = { navController.navigateToTab(BrightNavDestination.MORE) },
                        onOpenHelp = { navController.navigateToMoreSubScreen(BrightSubRoutes.HELP) },
                        onOpenRedDangerSOS = { viewModel.triggerRedDangerEmergency() },
                        onToggleDiagnosticStatus = { viewModel.toggleDiagnosticStatus() },
                        onOpenEstateExcoDossier = { showEstateExcoDialog = true },
                        onOpenSmartMeterGateway = { showSmartMeterGatewayDialog = true },
                        onAutoDetectSmartMeter = { viewModel.autoDetectCitizenMeter() },
                        onLockApp = { viewModel.lockAppSession() },
                        onLogOut = { viewModel.logOut() },
                        onTriggerSurgeSiren = { viewModel.triggerSurgeSafetySiren() },
                        onDismissSurgeWarning = { viewModel.dismissSurgeWarning() },
                        onSyncNow = { viewModel.syncOfflineQueue() },
                        onToggleBatSignalMode = { viewModel.toggleBatSignalMode(it) }
                    )
                )
            }

            composable(BrightNavDestination.MAP.route) {
                LiveMapScreen(
                    userProfile = userProfile,
                    outageNodes = outageNodes,
                    onRefreshMap = {
                        viewModel.showNotification("Refreshing outage map...")
                    }
                )
            }

            composable(BrightNavDestination.REPORT.route) {
                ReportFaultScreen(
                    userProfile = userProfile,
                    onBack = {
                        if (!navController.popBackStack()) {
                            navController.navigateToTab(BrightNavDestination.HOME)
                        }
                    },
                    onSubmit = { title, desc, faultType, isHazard, mediaUri, isVideo ->
                        viewModel.reportFault(title, desc, faultType, isHazard, mediaUri, isVideo)
                        navController.navigateToTab(BrightNavDestination.HISTORY)
                    }
                )
            }

            composable(BrightNavDestination.HISTORY.route) {
                HistoryScreen(
                    userProfile = userProfile,
                    historicalComplaints = historyComplaints,
                    billingDisputes = billingDisputes,
                    onEscalateClicked = { id -> viewModel.escalateComplaint(id) },
                    onAdvanceLifecycle = { id, nextStatus -> viewModel.advanceComplaintLifecycle(id, nextStatus) }
                )
            }

            composable(BrightNavDestination.MORE.route) {
                MoreScreen(
                    onOpenAntiTheft = { navController.navigateToMoreSubScreen(BrightSubRoutes.ANTI_THEFT) },
                    onOpenGridHub = { navController.navigateToMoreSubScreen(BrightSubRoutes.GRID_HUB) },
                    onOpenSmartMeterGateway = { showSmartMeterGatewayDialog = true },
                    onOpenEstateExcoDossier = { showEstateExcoDialog = true },
                    onOpenClearinghouse = { showClearinghouseDialog = true },
                    onOpenTransformerForum = { showTransformerForumDialog = true },
                    onOpenEnergyOptimization = { showEnergyOptimizationDialog = true },
                    onOpenProfileAdmin = { showProfileAdminDialog = true },
                    onOpenOnboarding = { showOnboardingDialog = true },
                    onLockApp = { viewModel.lockAppSession() },
                    onLogOut = { viewModel.logOut() },
                    onDeleteAccount = { showDeleteAccountDialog = true },
                    onOpenSolutionsSettings = { navController.navigateToMoreSubScreen(BrightSubRoutes.solutionsSettings(0)) },
                    onOpenHelp = { navController.navigateToMoreSubScreen(BrightSubRoutes.HELP) },
                    onShowTour = { replayTour = true }
                )
            }

            composable(BrightSubRoutes.HELP) {
                com.example.ui.screens.HelpScreen(
                    userProfile = userProfile,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                BrightSubRoutes.SOLUTIONS_SETTINGS,
                arguments = listOf(navArgument("solution") { type = NavType.IntType; defaultValue = 0 })
            ) { entry ->
                SolutionsSettingsScreen(
                    userProfile = userProfile,
                    initialSolution = entry.arguments?.getInt("solution") ?: 0,
                    isBatSignalMode = isBatSignalMode,
                    onToggleBatSignalMode = { viewModel.toggleBatSignalMode(it) },
                    onOpenForum = { showTransformerForumDialog = true },
                    onOpenHazardForm = { viewModel.triggerRedDangerEmergency() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(BrightSubRoutes.ANTI_THEFT) {
                VandalismScreen(
                    userProfile = userProfile,
                    reports = vandalismReports,
                    onSubmitReport = { incident, loc, land, isAnon, desc, suspects ->
                        viewModel.reportVandalism(incident, loc, land, isAnon, desc, suspects)
                    }
                )
            }

            composable(BrightSubRoutes.GRID_HUB) {
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
                    onPlaySirenAlarm = { viewModel.playRestorationChime() },
                    onOpenSolutionSettings = { n ->
                        navController.navigate(BrightSubRoutes.solutionsSettings(n)) { launchSingleTop = true }
                    },
                    onOpenEstateExco = { showEstateExcoDialog = true },
                    onOpenSmartMeterGateway = { showSmartMeterGatewayDialog = true },
                    citizenMeterStatus = citizenMeterStatus,
                    onAutoDetectSmartMeter = { viewModel.autoDetectCitizenMeter() }
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
                viewModel.showNotification("🎙️ Your $lang voice note was turned into fault report #TR-VOC-${(1000..9999).random()}")
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
            onRequestDeleteAccount = {
                showProfileAdminDialog = false
                showDeleteAccountDialog = true
            },
            onSessionTokenClearance = { viewModel.sessionTokenClearance() },
            onExportLedger = {
                viewModel.showNotification("📄 Transactional Accounting Ledger exported: BRIGHT_LEDGER_${userProfile.meterNumber}.csv downloaded")
            },
            onUpdateBiometrics = { fp, face -> viewModel.updateBiometricSettings(fp, face) },
            requireLoginOnLeave = requireLoginOnLeave,
            onToggleRequireLoginOnLeave = { enabled -> viewModel.setRequireLoginOnLeave(enabled) },
            onLockSession = { viewModel.lockAppSession() },
            isPinSet = isPinSet,
            verifyPin = { pin -> viewModel.verifyPin(pin) },
            onChangePin = { current, new -> viewModel.changePin(current, new) },
            onDismiss = { showProfileAdminDialog = false }
        )
    }

    // Delete account (from More > Account or Profile & Security)
    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            meterNumber = userProfile.meterNumber,
            isPinSet = isPinSet,
            verifyPin = { pin -> viewModel.verifyPin(pin) },
            onConfirmDelete = {
                showDeleteAccountDialog = false
                viewModel.deleteAccount()
                // Back to Home's place in the back stack; the sign-up screen takes over once the
                // profile is gone.
                navController.navigateToTab(BrightNavDestination.HOME)
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }

    // Estate Exco Portal & NERC Dossier / Dues Ledger / SLA Refund Calculator
    if (showEstateExcoDialog) {
        EstateExcoAndSlaDossierDialog(
            userProfile = userProfile,
            activeComplaints = personalComplaints,
            duesEntries = transformerDuesEntries,
            onAddDuesEntry = { name, addr, meter, purpose, amount, method ->
                viewModel.addTransformerDuesContribution(name, addr, meter, purpose, amount, method)
            },
            onGenerateSlaAssessment = { ticketId, title, delayHours ->
                viewModel.generateSlaCompensationAssessment(ticketId, title, delayHours)
            },
            onDismiss = { showEstateExcoDialog = false }
        )
    }

    // Nigeria Smart Meter Server Gateway & AMI Telemetry Portal
    if (showSmartMeterGatewayDialog) {
        SmartMeterServerGatewayDialog(
            serverConfig = smartMeterServerConfig,
            metersList = smartMetersList,
            commandsHistory = smartMeterCommands,
            userProfile = userProfile,
            citizenMeterStatus = citizenMeterStatus,
            onAutoDetectSmartMeter = { viewModel.autoDetectCitizenMeter() },
            gatewayTelemetryMap = gatewayTelemetryMap,
            isPollingGateway = isPollingGateway,
            onPollGateway = { mfg -> viewModel.pollManufacturerGateway(userProfile.meterNumber, mfg, userProfile.discoCode) },
            onUpdateServerConfig = { url, proto, mqtt, key, interval, tls ->
                viewModel.updateSmartMeterServerConfig(url, proto, mqtt, key, interval, tls)
            },
            onTestServerConnection = { viewModel.testAppServerConnection() },
            onAddMeterDevice = { num, mfg, model, disco, state, feeder, ip, proto ->
                viewModel.addSmartMeterDevice(num, mfg, model, disco, state, feeder, ip, proto)
            },
            onToggleRelay = { meterNum -> viewModel.toggleSmartMeterRelay(meterNum) },
            onSendOtaToken = { meterNum, token -> viewModel.sendOtaTokenToSmartMeter(meterNum, token) },
            onPingMeter = { meterNum -> viewModel.pingSmartMeterInstantRead(meterNum) },
            onDismiss = { showSmartMeterGatewayDialog = false }
        )
    }

    // Room database sync & offline mode details
    if (showRoomSyncDialog) {
        RoomDatabaseSyncDialog(
            isSyncing = isSyncing,
            isOfflineMode = isOfflineMode,
            pendingSyncCount = pendingSyncCount,
            lastSyncTime = lastSyncTimeText,
            pendingActions = pendingSyncActions,
            onToggleOfflineMode = { viewModel.toggleOfflineMode() },
            onSyncNow = { viewModel.syncOfflineQueue() },
            onAddTestOfflineAction = { viewModel.addTestOfflineFaultReport() },
            onDismiss = { showRoomSyncDialog = false }
        )
    }
}

/**
 * Switches bottom-bar tabs the standard Material way: one copy of each tab, Home kept at the root
 * so system back from any tab returns to Home, and tab state saved/restored.
 */
private fun NavHostController.navigateToTab(destination: BrightNavDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/** Opens a screen that lives under the More tab, so back returns to More. */
private fun NavHostController.navigateToMoreSubScreen(route: String) {
    navigate(BrightNavDestination.MORE.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
    navigate(route) {
        launchSingleTop = true
    }
}
