package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Complaint
import com.example.model.ComplaintStatus
import com.example.model.EscalationTier
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantGoldDark
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.Slate100Text
import com.example.ui.theme.Slate400Text
import com.example.ui.theme.Slate500Text

/**
 * Real-Time Visual Escalation Status Bar for Submitted Fault Tickets
 * Displays statutory NERC escalation tiers, continuous progress fill,
 * SLA countdown, active authority, and live escalation controls.
 */
@Composable
fun FaultEscalationStatusBar(
    complaints: List<Complaint>,
    selectedComplaintId: String? = null,
    onSelectComplaint: (String) -> Unit = {},
    onEscalateClicked: (String) -> Unit = {},
    onAdvanceLifecycle: ((String, ComplaintStatus) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (complaints.isEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("fault_escalation_status_bar_empty"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x14FACC15)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = ElegantGoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "REAL-TIME ESCALATION STATUS BAR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = ElegantGoldPrimary
                    )
                    Text(
                        text = "No active submitted tickets currently in escalation queue.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400Text
                    )
                }
            }
        }
        return
    }

    // Determine target complaint
    val activeComplaint = complaints.find { it.id == selectedComplaintId }
        ?: complaints.firstOrNull { it.status != ComplaintStatus.RESOLVED }
        ?: complaints.first()

    var isSelectorDropdownExpanded by remember { mutableStateOf(false) }

    // Pulsing transition for real-time live radar indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val currentLevel = activeComplaint.escalationTier.level
    // Progress calculation: Level 1 -> 25%, Level 2 -> 50%, Level 3 -> 75%, Level 4 -> 100%
    val targetProgressFraction = currentLevel / 4.0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgressFraction,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "escalation_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("fault_escalation_status_bar"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, ElegantGoldPrimary.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Top Status Bar Banner & Live Radar Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pulsing radar live indicator
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = pulseAlpha))
                    )
                    Text(
                        text = "REAL-TIME ESCALATION STATUS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp
                        ),
                        color = ElegantGoldPrimary
                    )
                }

                // DisCo Code & Feeder Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0x14FFFFFF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x24FFFFFF))
                ) {
                    Text(
                        text = "${activeComplaint.discoCode} • ${activeComplaint.status.displayName.uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        ),
                        color = if (activeComplaint.status == ComplaintStatus.RESOLVED) Color(0xFF10B981) else ElegantGoldPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ticket Details & Ticket Switcher (if multiple tickets)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Ticket #${activeComplaint.id}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = Slate100Text
                        )
                        if (complaints.size > 1) {
                            Box {
                                IconButton(
                                    onClick = { isSelectorDropdownExpanded = true },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSelectorDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Switch Ticket",
                                        tint = ElegantGoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = isSelectorDropdownExpanded,
                                    onDismissRequest = { isSelectorDropdownExpanded = false }
                                ) {
                                    complaints.forEach { c ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text("#${c.id} - ${c.title}", fontWeight = FontWeight.Bold)
                                                    Text("Tier ${c.escalationTier.level}: ${c.escalationTier.title}", fontSize = 11.sp, color = Color.Gray)
                                                }
                                            },
                                            onClick = {
                                                onSelectComplaint(c.id)
                                                isSelectorDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Text(
                        text = activeComplaint.title,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Slate400Text,
                        maxLines = 1
                    )
                }

                // Escalation Level Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x26FACC15),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantGoldPrimary)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = ElegantGoldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "TIER $currentLevel / 4",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            ),
                            color = ElegantGoldPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // THE VISUAL ESCALATION STATUS BAR
            // Continuous track + filled progress
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF1E2430))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFEAB308), // Gold
                                        Color(0xFFF59E0B), // Amber
                                        Color(0xFF10B981)  // Emerald
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Milestone Nodes for all 4 NERC statutory escalation stages
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    EscalationTier.entries.forEach { tier ->
                        val isPast = tier.level < currentLevel
                        val isCurrent = tier.level == currentLevel
                        val isUpcoming = tier.level > currentLevel

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Node circle
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isPast -> Color(0xFF10B981)
                                            isCurrent -> ElegantGoldPrimary
                                            else -> Color(0xFF1E2430)
                                        }
                                    )
                                    .border(
                                        width = if (isCurrent) 2.dp else 1.dp,
                                        color = if (isCurrent) Color.White else Color(0x33FFFFFF),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isPast) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = "Active",
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${tier.level}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Slate500Text
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Stage title
                            Text(
                                text = when (tier) {
                                    EscalationTier.LEVEL_1 -> "Crew"
                                    EscalationTier.LEVEL_2 -> "District"
                                    EscalationTier.LEVEL_3 -> "DisCo HQ"
                                    EscalationTier.LEVEL_4 -> "NERC"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium
                                ),
                                color = if (isCurrent) ElegantGoldPrimary else if (isPast) Slate100Text else Slate500Text
                            )

                            // SLA label
                            Text(
                                text = "${tier.maxSlaHours}h SLA",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                color = Slate500Text
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current Handler and Escalation Info Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x14FFFFFF))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CURRENT HANDLING AUTHORITY:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Slate400Text
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Live SCADA Sync",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    Text(
                        text = activeComplaint.escalationTier.authority,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate100Text
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Standard Window: ${activeComplaint.escalationTier.maxSlaHours} hrs",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Slate400Text
                        )
                        Text(
                            text = if (currentLevel < 4) "Auto-escalates upon SLA breach" else "Highest Statutory Level",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = ElegantGoldPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row: Manual Fast-Track Escalation & Advance Lifecycle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentLevel < 4) {
                    Button(
                        onClick = { onEscalateClicked(activeComplaint.id) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("status_bar_fast_track_escalate_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantGoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Fast-Track (${activeComplaint.escalationTier.nextTier()?.title ?: "Next"})",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }

                if (onAdvanceLifecycle != null && activeComplaint.status != ComplaintStatus.RESOLVED) {
                    OutlinedButton(
                        onClick = {
                            val next = when (activeComplaint.status) {
                                ComplaintStatus.LOGGED -> ComplaintStatus.ASSIGNED
                                ComplaintStatus.ASSIGNED -> ComplaintStatus.DISPATCHED
                                ComplaintStatus.DISPATCHED -> ComplaintStatus.WORK_IN_PROGRESS
                                ComplaintStatus.WORK_IN_PROGRESS -> ComplaintStatus.TESTING
                                ComplaintStatus.TESTING -> ComplaintStatus.RESOLVED
                                else -> ComplaintStatus.RESOLVED
                            }
                            onAdvanceLifecycle(activeComplaint.id, next)
                        },
                        modifier = Modifier
                            .weight(if (currentLevel < 4) 1f else 2f)
                            .height(44.dp)
                            .testTag("status_bar_advance_lifecycle_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Advance Lifecycle",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Slate100Text
                        )
                    }
                }
            }
        }
    }
}
