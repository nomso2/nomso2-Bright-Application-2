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
import com.example.ui.components.ResolutionRatingDialog
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
            BrightTheme {
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
                        onReportFaultClicked = { currentDestination = BrightNavDestination.REPORT },
                        onEmergencyHazardTriggered = { hazardName ->
                            viewModel.reportQuickEmergencyHazard(hazardName)
                        },
                        onEscalateComplaint = { id -> viewModel.escalateComplaint(id) },
                        onUpvoteComplaint = { id -> viewModel.upvoteComplaint(id) },
                        onConfirmResolution = { id -> resolvingTicketId = id },
                        onEditProfileClicked = { showEditProfileDialog = true },
                        onNavigateMap = { currentDestination = BrightNavDestination.MAP },
                        onNavigateVandalism = { currentDestination = BrightNavDestination.VANDALISM },
                        onNavigateHistory = { currentDestination = BrightNavDestination.HISTORY },
                        onNavigateHub = { currentDestination = BrightNavDestination.GRID_HUB }
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
                        }
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
}
