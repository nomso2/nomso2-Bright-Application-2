package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.theme.ElegantDarkBar
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkCardStart
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.ElegantGreenLive
import com.example.ui.theme.Slate100Text
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
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Branded Header from Elegant Dark Design HTML
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
                Column {
                    Text(
                        text = "THE BRIGHT PROJECT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = ElegantGoldPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${userProfile.discoCode} • ${userProfile.feederBand.code}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Slate100Text
                    )
                }

                // Action Bar: Light/Dark Mode Switcher & Multi-Asset Profile Icon & Signup Wizard Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Sign-Up Wizard / Switch Meter Button
                    IconButton(
                        onClick = onOpenOnboarding,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x14FFFFFF))
                            .border(1.dp, ElegantDarkBorder, CircleShape)
                            .testTag("onboarding_signup_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New User Sign-Up / Meter Setup",
                            tint = ElegantGoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Theme Switcher Button (Dark / Light)
                    IconButton(
                        onClick = onToggleThemeMode,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x14FFFFFF))
                            .border(1.dp, ElegantDarkBorder, CircleShape)
                            .testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Light/Dark Theme",
                            tint = ElegantGoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Multi-Asset & Protocols Button
                    IconButton(
                        onClick = onOpenProfileAdmin,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x14FFFFFF))
                            .border(1.dp, ElegantDarkBorder, CircleShape)
                            .testTag("multi_asset_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Profile Protocols",
                            tint = ElegantGoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Live status pulsing ring
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkCardStart)
                            .border(1.dp, Color(0x3364748B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ElegantGreenLive)
                        )
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
                // 0A. 30 Critical Grid Solutions Hub Banner
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("solutions_30_banner_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD97706))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
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
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x26F59E0B)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = ElegantGoldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "30 GRID FIXES DEPLOYED",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = ElegantGoldPrimary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x2610B981))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Trust: $userTrustScore%",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF34D399)
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Intelligent software layer resolving Nigeria's 30 power sector crises: tiered dispatch, transformer fire alerts, USSD offline sync, load-shedding diagnosis & extortion shield.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Slate400Text
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onNavigateHub,
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("open_30_solutions_btn"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElegantGoldPrimary,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "View 30 Solutions",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Button(
                                    onClick = onOpenRedDangerSOS,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("danger_sos_btn"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFDC2626),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "🚨 Red SOS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 0B. Diagnostic Classifier: Load Shedding vs Unplanned Fault (Solution #5)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onToggleDiagnosticStatus)
                            .testTag("diagnostic_classifier_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (diagnosticStatus == "LOAD_SHEDDING") Color(0xFFF59E0B) else Color(0xFFEF4444))
                                )
                                Column {
                                    Text(
                                        text = if (diagnosticStatus == "LOAD_SHEDDING") "AI DIAGNOSIS: PLANNED LOAD SHEDDING" else "AI DIAGNOSIS: UNPLANNED NETWORK FAULT",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (diagnosticStatus == "LOAD_SHEDDING") Color(0xFFFBBF24) else Color(0xFFF87171)
                                    )
                                    Text(
                                        text = if (diagnosticStatus == "LOAD_SHEDDING") "TCN System Operator Quota reduction on ${userProfile.feederName}" else "Feeder tripped on overcurrent or blown drop-out fuse",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = Slate400Text
                                    )
                                }
                            }
                            Text(
                                text = "Tap Switch",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Slate500Text
                            )
                        }
                    }
                }

                // 1. Meter Identity Card
                item {
                    MeterProfileHeader(
                        profile = userProfile,
                        onEditProfileClicked = onEditProfileClicked
                    )
                }

                // 2. Phase 2: Contractual Hour Auditing Matrix Card
                item {
                    AuditingMatrixCard(
                        userProfile = userProfile,
                        auditingRecords = auditingRecords,
                        telemetry = telemetry,
                        onOpenClearinghouse = onOpenClearinghouse
                    )
                }

                // 3. Phase Roadmap Quick Access Grid (Phases 1, 4, 5, 6, 7)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("roadmap_quick_hub_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "PMI REGULATORY CLEARINGHOUSE PROTOCOLS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = ElegantGoldPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Grid of 4 key action items
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Phase 1: Onboarding & SIM OTP
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x14FFFFFF))
                                        .border(1.dp, ElegantDarkBorder, RoundedCornerShape(12.dp))
                                        .clickable(onClick = onOpenOnboarding)
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ElegantGoldPrimary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Phase 1: Setup", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Slate100Text)
                                        Text(text = "SIM OTP & ₦100", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = Slate400Text)
                                    }
                                }

                                // Phase 4: Token Escrow
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x1422C55E))
                                        .border(1.dp, Color(0x3322C55E), RoundedCornerShape(12.dp))
                                        .clickable(onClick = onOpenClearinghouse)
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, tint = ElegantGreenLive, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Phase 4: Escrow", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF86EFAC))
                                        Text(text = "₦14.8M Vault & Rebates", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = Slate400Text)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Phase 5: Cluster Forum & Voice AI
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x14FFFFFF))
                                        .border(1.dp, ElegantDarkBorder, RoundedCornerShape(12.dp))
                                        .clickable(onClick = onOpenTransformerForum)
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Icon(imageVector = Icons.Default.Forum, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Phase 5: Forum", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Slate100Text)
                                        Text(text = "Line Feed & Voice AI", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = Slate400Text)
                                    }
                                }

                                // Phase 6: Energy & Surge Guard
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x14FFFFFF))
                                        .border(1.dp, ElegantDarkBorder, RoundedCornerShape(12.dp))
                                        .clickable(onClick = onOpenEnergyOptimization)
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = ElegantGoldPrimary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Phase 6: Surge", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Slate100Text)
                                        Text(text = "T-5 Min Alert & Load", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = Slate400Text)
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Real-Time National Grid Telemetry Bar
                item {
                    RealTimeTicker(telemetry = telemetry)
                }

                // 5. Transformer Overload & Phase Telemetry (Feature 7)
                item {
                    TransformerOverloadCard(
                        telemetry = transformerTelemetry,
                        onReportHumSpark = onReportTransformerHumSpark
                    )
                }

                // 6. Power Restoration Alert Chime (Feature 9)
                item {
                    PowerRestorationAlertCard(
                        isAlarmEnabled = isRestorationAlarmEnabled,
                        transformerId = userProfile.transformerId,
                        onToggleAlarm = onToggleRestorationAlarm,
                        onTestChime = onPlayRestorationChime
                    )
                }

                // 7. Quick Action Grid (8 items from design)
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

                // 8. Section Title: "MY ACTIVE COMPLAINTS"
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

                // 5. Personal Complaints List
                if (personalComplaints.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("empty_complaints_card"),
                            shape = RoundedCornerShape(24.dp),
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
                                        .height(48.dp)
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
                            onEscalateClicked = onEscalateComplaint,
                            onUpvoteClicked = onUpvoteComplaint,
                            onConfirmResolutionClicked = onConfirmResolution,
                            onAdvanceStatusDemo = { _, _ -> }
                        )
                    }
                }

                // 6. Emergency Hazard Fast-Track (1-Tap SOS)
                item {
                    HazardFastTrackCard(
                        onQuickHazardSelected = onEmergencyHazardTriggered
                    )
                }
            }

            // Floating Action Button to Report Outage
            ExtendedFloatingActionButton(
                onClick = onReportFaultClicked,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Report New Fault",
                        tint = Color(0xFF0A0C10)
                    )
                },
                text = {
                    Text(
                        text = "Report Fault",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A0C10)
                    )
                },
                containerColor = ElegantGoldPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 20.dp, end = 20.dp)
                    .testTag("report_fault_fab")
            )
        }
    }
}
