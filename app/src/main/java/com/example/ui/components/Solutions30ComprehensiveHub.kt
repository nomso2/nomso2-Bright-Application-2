package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DisCo
import com.example.model.PowerProblemSolution
import com.example.model.PowerSector30Registry
import com.example.model.SolutionCategory
import com.example.model.UserProfile
import com.example.ui.solutions.*
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldPrimary

@Composable
fun Solutions30ComprehensiveHub(
    userProfile: UserProfile,
    isBatSignalMode: Boolean,
    onToggleBatSignalMode: (Boolean) -> Unit,
    onOpenForum: () -> Unit,
    onOpenRedDangerSOS: () -> Unit,
    onPlaySirenAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf<SolutionCategory?>(null) }
    var activeModalSolutionNumber by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    // Interactive state across solutions
    var selectedUrgencyTier by remember { mutableStateOf(1) } // Solution 1
    var simulatedDiagnosticMode by remember { mutableStateOf("LOAD_SHEDDING") } // Solution 5
    var estimatedBillInput by remember { mutableStateOf("45000") } // Solution 13
    var userTrustScore by remember { mutableStateOf(98) } // Solution 18
    var wakeUpCount by remember { mutableStateOf(184) } // Solution 17
    var hasConsumerVerifiedLight by remember { mutableStateOf(false) } // Solution 23
    var selectedDialect by remember { mutableStateOf("Pidgin") } // Solution 20

    val filteredSolutions = remember(selectedCategoryFilter) {
        if (selectedCategoryFilter == null) {
            PowerSector30Registry.ALL_30_SOLUTIONS
        } else {
            PowerSector30Registry.ALL_30_SOLUTIONS.filter { it.category == selectedCategoryFilter }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hub Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MORE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Comprehensive Nigerian Power Sector Solutions & Utilities",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = GoldPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary)
                ) {
                    Text(
                        text = "30/30 READY",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All 30 (${PowerSector30Registry.ALL_30_SOLUTIONS.size})", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_all_solutions")
                    )
                }
                items(SolutionCategory.entries) { category ->
                    val isSelected = selectedCategoryFilter == category
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategoryFilter = if (isSelected) null else category
                        },
                        label = {
                            Text(
                                text = "${category.id}. ${category.title.take(22)}...",
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = GoldPrimary
                        )
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // Solutions List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            filteredSolutions.forEach { item ->
                key(item.problemNumber) {
                    SolutionCardItem(
                        item = item,
                        isBatSignalMode = isBatSignalMode,
                        onToggleBatSignal = onToggleBatSignalMode,
                        onLaunchInteractiveAction = {
                            activeModalSolutionNumber = item.problemNumber
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // INTERACTIVE SOLUTION INSPECTION MODAL
    activeModalSolutionNumber?.let { solNum ->
        val solution = PowerSector30Registry.ALL_30_SOLUTIONS.firstOrNull { it.problemNumber == solNum }
        if (solution != null) {
            Dialog(onDismissRequest = { activeModalSolutionNumber = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                        .size(32.dp)
                                        .background(GoldPrimary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#${solution.problemNumber}",
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    text = solution.solutionTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { activeModalSolutionNumber = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        // Problem Statement
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "GRID PROBLEM:",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = solution.problemStatement,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // App Solution
                        Surface(
                            color = EmeraldAccent.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "BRIGHT SOFTWARE SOLUTION:",
                                    color = EmeraldAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = solution.solutionDetail,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Solution-Specific Interactive Feature Implementation (All 30 Features Fully Accessible)
                        when (solution.problemNumber) {
                            1 -> TieredUrgencyCategoriserFeature(userProfile = userProfile)
                            2 -> GpsFaultGeofencingFeature(userProfile = userProfile)
                            3 -> AutomatedDispatchRouterFeature(userProfile = userProfile)
                            4 -> CriticalDangerRedButtonFeature(onTriggerEmergency = {
                                activeModalSolutionNumber = null
                                onOpenRedDangerSOS()
                            })
                            5 -> DiagnosticStatusTrackerFeature(userProfile = userProfile)
                            6 -> OfflineUssdBridgeFeature(userProfile = userProfile)
                            7 -> TamperCrowdsourcingFeature(userProfile = userProfile)
                            8 -> LowPowerBatSignalFeature(
                                isBatSignalMode = isBatSignalMode,
                                onToggleBatSignal = onToggleBatSignalMode,
                                userProfile = userProfile
                            )
                            9 -> UniversalMeterSyncFeature(userProfile = userProfile)
                            10 -> NationalGridPulseMonitorFeature()
                            11 -> AutomatedBandAuditorFeature(userProfile = userProfile)
                            12 -> AutomatedRefundLedgerFeature(userProfile = userProfile)
                            13 -> CommunityConsumptionCalculatorFeature(userProfile = userProfile)
                            14 -> MeterWaitlistTrackerFeature(userProfile = userProfile)
                            15 -> OfflineTokenVendingFeature(userProfile = userProfile)
                            16 -> VisualProofOverrideFeature(userProfile = userProfile)
                            17 -> WakeUpStreetAlertsFeature(userProfile = userProfile)
                            18 -> UserTrustScoreFeature(userTrustScore = userTrustScore)
                            19 -> NeighborhoodGridForumFeature(userProfile = userProfile, onOpenFullForum = {
                                activeModalSolutionNumber = null
                                onOpenForum()
                            })
                            20 -> MultiLingualVoiceReportingFeature(userProfile = userProfile)
                            21 -> AnonymousWhistleblowerFeature(userProfile = userProfile)
                            22 -> PizzaStyleDeliveryTrackerFeature(userProfile = userProfile)
                            23 -> ConsumerClosureVerificationFeature(userProfile = userProfile)
                            24 -> FaultHistoryLogFeature(userProfile = userProfile)
                            25 -> InventoryRequestMonitorFeature(userProfile = userProfile)
                            26 -> SurgeReturnWarningFeature(onPlaySiren = onPlaySirenAlarm)
                            27 -> GridIsBackAudioSirenFeature(onPlaySiren = onPlaySirenAlarm)
                            28 -> ApplianceLoadBudgeterFeature()
                            29 -> HybridEnergyOptimizerFeature()
                            30 -> TariffFlashNewsFeature()
                            else -> {
                                Text("Feature fully active and ready.")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SolutionCardItem(
    item: PowerProblemSolution,
    isBatSignalMode: Boolean,
    onToggleBatSignal: (Boolean) -> Unit,
    onLaunchInteractiveAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onLaunchInteractiveAction() }
            .testTag("solution_card_${item.problemNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Number & Title Row
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
                            .size(28.dp)
                            .background(GoldPrimary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${item.problemNumber}",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = item.solutionTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item.regulatoryBadge?.let { badge ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Problem statement in subtle red tint
            Surface(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = item.problemStatement,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Solution Detail
            Text(
                text = item.solutionDetail,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp
            )

            // Card Action Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.category.title,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                Button(
                    onClick = onLaunchInteractiveAction,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Launch Feature #${item.problemNumber}", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
