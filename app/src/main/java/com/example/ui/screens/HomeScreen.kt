package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import com.example.model.Complaint
import com.example.model.GridTelemetry
import com.example.model.UserProfile
import com.example.ui.components.ComplaintCard
import com.example.ui.components.HazardFastTrackCard
import com.example.ui.components.MeterProfileHeader
import com.example.ui.components.QuickActionGrid
import com.example.ui.components.RealTimeTicker
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
    onReportFaultClicked: () -> Unit,
    onEmergencyHazardTriggered: (String) -> Unit,
    onEscalateComplaint: (String) -> Unit,
    onUpvoteComplaint: (String) -> Unit,
    onConfirmResolution: (String) -> Unit,
    onEditProfileClicked: () -> Unit,
    onNavigateMap: () -> Unit = {},
    onNavigateVandalism: () -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    onNavigateHub: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Branded Header from Elegant Dark Design HTML
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElegantDarkBar)
                .border(1.dp, ElegantDarkBorder)
                .padding(horizontal = 16.dp, vertical = 14.dp)
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
                        text = "${userProfile.discoCode} • Lagos District",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Slate100Text
                    )
                }

                // Live status pulsing ring
                Box(
                    modifier = Modifier
                        .size(40.dp)
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

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Meter Identity Card
                item {
                    MeterProfileHeader(
                        profile = userProfile,
                        onEditProfileClicked = onEditProfileClicked
                    )
                }

                // 2. Real-Time National Grid Telemetry Bar
                item {
                    RealTimeTicker(telemetry = telemetry)
                }

                // 3. Quick Action Grid (8 items directly from Design HTML)
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

                // 4. Section Title: "MY ACTIVE COMPLAINTS" (Strictly personal complaints)
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
