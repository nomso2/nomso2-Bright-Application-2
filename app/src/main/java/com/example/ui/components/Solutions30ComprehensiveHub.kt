package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.PowerSector30Registry
import com.example.model.SolutionCategory
import com.example.model.UserProfile
import com.example.ui.solutions.AnonymousWhistleblowerFeature
import com.example.ui.solutions.ApplianceLoadBudgeterFeature
import com.example.ui.solutions.AutomatedBandAuditorFeature
import com.example.ui.solutions.AutomatedDispatchRouterFeature
import com.example.ui.solutions.AutomatedRefundLedgerFeature
import com.example.ui.solutions.CommunityConsumptionCalculatorFeature
import com.example.ui.solutions.ConsumerClosureVerificationFeature
import com.example.ui.solutions.CriticalDangerRedButtonFeature
import com.example.ui.solutions.DiagnosticStatusTrackerFeature
import com.example.ui.solutions.FaultHistoryLogFeature
import com.example.ui.solutions.GpsFaultGeofencingFeature
import com.example.ui.solutions.GridIsBackAudioSirenFeature
import com.example.ui.solutions.HybridEnergyOptimizerFeature
import com.example.ui.solutions.InventoryRequestMonitorFeature
import com.example.ui.solutions.LowPowerBatSignalFeature
import com.example.ui.solutions.MeterWaitlistTrackerFeature
import com.example.ui.solutions.MultiLingualVoiceReportingFeature
import com.example.ui.solutions.NationalGridPulseMonitorFeature
import com.example.ui.solutions.NeighborhoodGridForumFeature
import com.example.ui.solutions.OfflineTokenVendingFeature
import com.example.ui.solutions.OfflineUssdBridgeFeature
import com.example.ui.solutions.PizzaStyleDeliveryTrackerFeature
import com.example.ui.solutions.SurgeReturnWarningFeature
import com.example.ui.solutions.TamperCrowdsourcingFeature
import com.example.ui.solutions.TariffFlashNewsFeature
import com.example.ui.solutions.TieredUrgencyCategoriserFeature
import com.example.ui.solutions.UniversalMeterSyncFeature
import com.example.ui.solutions.UserTrustScoreFeature
import com.example.ui.solutions.VisualProofOverrideFeature
import com.example.ui.solutions.WakeUpStreetAlertsFeature
import com.example.ui.theme.ElegantGoldPrimary

/**
 * Maps each of the 30 Nigerian Power Solutions to an in-app icon and punchy launcher label
 * matching the Home Screen quick action icon aesthetic.
 */
fun getSolutionAppIconInfo(problemNumber: Int): Pair<ImageVector, String> {
    return when (problemNumber) {
        1 -> Pair(Icons.Default.Speed, "Urgency Tier")
        2 -> Pair(Icons.Default.LocationOn, "GPS Geofence")
        3 -> Pair(Icons.Default.DirectionsCar, "Auto Dispatch")
        4 -> Pair(Icons.Default.Emergency, "Red SOS")
        5 -> Pair(Icons.Default.FactCheck, "Diagnostic")
        6 -> Pair(Icons.Default.WifiOff, "Offline SMS")
        7 -> Pair(Icons.Default.Security, "Anti-Tamper")
        8 -> Pair(Icons.Default.FlashOn, "Bat-Signal")
        9 -> Pair(Icons.Default.QrCodeScanner, "Meter Scan")
        10 -> Pair(Icons.Default.Timeline, "Grid Pulse")
        11 -> Pair(Icons.Default.ReceiptLong, "Band Audit")
        12 -> Pair(Icons.Default.AccountBalanceWallet, "SLA Escrow")
        13 -> Pair(Icons.Default.Calculate, "Bill Capper")
        14 -> Pair(Icons.Default.HourglassBottom, "Waitlist")
        15 -> Pair(Icons.Default.CreditCard, "SMS Tokens")
        16 -> Pair(Icons.Default.CameraAlt, "Photo Bypass")
        17 -> Pair(Icons.Default.NotificationsActive, "Street Alert")
        18 -> Pair(Icons.Default.Star, "Trust Score")
        19 -> Pair(Icons.Default.Forum, "Grid Forum")
        20 -> Pair(Icons.Default.Translate, "Local Voice")
        21 -> Pair(Icons.Default.VisibilityOff, "Whistleblower")
        22 -> Pair(Icons.Default.AltRoute, "Live Tracker")
        23 -> Pair(Icons.Default.CheckCircle, "Verify Light")
        24 -> Pair(Icons.Default.History, "Fault Log")
        25 -> Pair(Icons.Default.Inventory, "Parts Supply")
        26 -> Pair(Icons.Default.Bolt, "Surge Alert")
        27 -> Pair(Icons.Default.VolumeUp, "Light Siren")
        28 -> Pair(Icons.Default.BatteryChargingFull, "Load Budget")
        29 -> Pair(Icons.Default.SolarPower, "Solar Opt")
        30 -> Pair(Icons.Default.Announcement, "Tariff News")
        else -> Pair(Icons.Default.Bolt, "Tool #$problemNumber")
    }
}

/**
 * 30 Power Solutions Hub:
 * Implemented as a clean, compact 4-column App Icon Grid (identical to Home Page quick action icons).
 * Eliminates bulky vertical text notes and wasted whitespace.
 */
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
    var searchQuery by remember { mutableStateOf("") }
    var activeModalSolutionNumber by remember { mutableStateOf<Int?>(null) }

    val filteredSolutions = remember(selectedCategoryFilter, searchQuery) {
        PowerSector30Registry.ALL_30_SOLUTIONS.filter { item ->
            val matchesCategory = selectedCategoryFilter == null || item.category == selectedCategoryFilter
            val matchesSearch = searchQuery.isBlank() ||
                    item.solutionTitle.contains(searchQuery, ignoreCase = true) ||
                    item.problemNumber.toString() == searchQuery.trim() ||
                    getSolutionAppIconInfo(item.problemNumber).second.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 20.dp)
    ) {
        // Hub Header & Category Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MORE SOLUTIONS (30 TOOLS)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap any in-app icon to launch its interactive utility",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = ElegantGoldPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantGoldPrimary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "30 APPS",
                        color = ElegantGoldPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Compact Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search 30 solutions (e.g. surge, solar, billing, meter)...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = ElegantGoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("solutions_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElegantGoldPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All 30", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElegantGoldPrimary,
                            selectedLabelColor = Color.Black
                        ),
                        modifier = Modifier.testTag("filter_all_solutions")
                    )
                }
                items(SolutionCategory.entries) { category ->
                    val isSelected = selectedCategoryFilter == category
                    val label = when (category) {
                        SolutionCategory.OUTAGE_CATEGORISATION -> "1. Triage (#1-5)"
                        SolutionCategory.NETWORK_HARDWARE_FAILURES -> "2. Hardware (#6-10)"
                        SolutionCategory.BILLING_TARIFFS_FINANCIAL -> "3. Billing (#11-15)"
                        SolutionCategory.NEIGHBOR_BOTTLENECK_CROWDSOURCING -> "4. Crowd (#16-20)"
                        SolutionCategory.MAINTENANCE_TRANSPARENCY -> "5. Oversight (#21-25)"
                        SolutionCategory.PERSONAL_ENERGY_MANAGEMENT -> "6. Energy (#26-30)"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategoryFilter = if (isSelected) null else category
                        },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElegantGoldPrimary.copy(alpha = 0.25f),
                            selectedLabelColor = ElegantGoldPrimary
                        )
                    )
                }
            }
        }

        Divider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // 4-Column App Icon Grid
        val chunkedSolutions = remember(filteredSolutions) {
            filteredSolutions.chunked(4)
        }

        if (chunkedSolutions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tools matched \"$searchQuery\"",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chunkedSolutions.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            val (icon, shortLabel) = getSolutionAppIconInfo(item.problemNumber)
                            SolutionAppIconCell(
                                problemNumber = item.problemNumber,
                                label = shortLabel,
                                icon = icon,
                                onClick = { activeModalSolutionNumber = item.problemNumber },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill remainder of row if less than 4 items
                        for (i in 0 until (4 - rowItems.size)) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // Modal Interactive Tool Dialog
    activeModalSolutionNumber?.let { solNum ->
        val solution = PowerSector30Registry.ALL_30_SOLUTIONS.firstOrNull { it.problemNumber == solNum }
        if (solution != null) {
            val (icon, shortLabel) = getSolutionAppIconInfo(solution.problemNumber)
            Dialog(onDismissRequest = { activeModalSolutionNumber = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .testTag("solution_modal_${solution.problemNumber}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Dialog Header: Icon, Number, Title, Close Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                        .size(38.dp)
                                        .background(ElegantGoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = shortLabel,
                                        tint = ElegantGoldPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "#${solution.problemNumber} • ${solution.category.title.take(24)}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            color = ElegantGoldPrimary
                                        )
                                    }
                                    Text(
                                        text = solution.solutionTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            IconButton(
                                onClick = { activeModalSolutionNumber = null },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Regulatory Badge
                        solution.regulatoryBadge?.let { badge ->
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Standard: $badge",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // LIVE WORKING INTERACTIVE TOOL IMPLEMENTATION
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                when (solution.problemNumber) {
                                    1 -> TieredUrgencyCategoriserFeature(userProfile = userProfile)
                                    2 -> GpsFaultGeofencingFeature(userProfile = userProfile)
                                    3 -> AutomatedDispatchRouterFeature(userProfile = userProfile)
                                    4 -> CriticalDangerRedButtonFeature(onTriggerEmergency = onOpenRedDangerSOS)
                                    5 -> DiagnosticStatusTrackerFeature(userProfile = userProfile)
                                    6 -> OfflineUssdBridgeFeature(userProfile = userProfile)
                                    7 -> TamperCrowdsourcingFeature(userProfile = userProfile)
                                    8 -> LowPowerBatSignalFeature(isBatSignalMode = isBatSignalMode, onToggleBatSignal = onToggleBatSignalMode, userProfile = userProfile)
                                    9 -> UniversalMeterSyncFeature(userProfile = userProfile)
                                    10 -> NationalGridPulseMonitorFeature()
                                    11 -> AutomatedBandAuditorFeature(userProfile = userProfile)
                                    12 -> AutomatedRefundLedgerFeature(userProfile = userProfile)
                                    13 -> CommunityConsumptionCalculatorFeature(userProfile = userProfile)
                                    14 -> MeterWaitlistTrackerFeature(userProfile = userProfile)
                                    15 -> OfflineTokenVendingFeature(userProfile = userProfile)
                                    16 -> VisualProofOverrideFeature(userProfile = userProfile)
                                    17 -> WakeUpStreetAlertsFeature(userProfile = userProfile)
                                    18 -> UserTrustScoreFeature()
                                    19 -> NeighborhoodGridForumFeature(userProfile = userProfile, onOpenFullForum = onOpenForum)
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
                                    else -> Text("Utility fully functional.", color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        // Collapsible Problem Statement & Details Accordion
                        var isDetailsExpanded by remember { mutableStateOf(false) }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .clickable { isDetailsExpanded = !isDetailsExpanded }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Problem Statement & Context",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = if (isDetailsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            AnimatedVisibility(visible = isDetailsExpanded) {
                                Column(
                                    modifier = Modifier.padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Problem:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFFEF4444)
                                    )
                                    Text(
                                        text = solution.problemStatement,
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Solution Specification:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = ElegantGoldPrimary
                                    )
                                    Text(
                                        text = solution.solutionDetail,
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual App Icon Cell matching Home Screen QuickActionCell styling.
 */
@Composable
private fun SolutionAppIconCell(
    problemNumber: Int,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp)
            .testTag("solution_icon_app_$problemNumber"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Container with Number Pill
            Box(contentAlignment = Alignment.TopEnd) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = ElegantGoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Tiny Solution # Badge
                Box(
                    modifier = Modifier
                        .background(ElegantGoldPrimary, CircleShape)
                        .padding(horizontal = 3.dp, vertical = 0.5.dp)
                ) {
                    Text(
                        text = "$problemNumber",
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Punchy Label (2 lines max, centered)
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Dialog wrapper that displays all 30 App Icons directly when "More" is clicked from Home.
 */
@Composable
fun MoreSolutionsDialog(
    userProfile: UserProfile,
    isBatSignalMode: Boolean,
    onToggleBatSignalMode: (Boolean) -> Unit,
    onOpenForum: () -> Unit,
    onOpenRedDangerSOS: () -> Unit,
    onPlaySirenAlarm: () -> Unit,
    onDismiss: () -> Unit,
    onNavigateFullHub: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("more_solutions_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ALL 30 POWER UTILITIES",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Solutions30ComprehensiveHub(
                    userProfile = userProfile,
                    isBatSignalMode = isBatSignalMode,
                    onToggleBatSignalMode = onToggleBatSignalMode,
                    onOpenForum = onOpenForum,
                    onOpenRedDangerSOS = onOpenRedDangerSOS,
                    onPlaySirenAlarm = onPlaySirenAlarm
                )
            }
        }
    }
}
