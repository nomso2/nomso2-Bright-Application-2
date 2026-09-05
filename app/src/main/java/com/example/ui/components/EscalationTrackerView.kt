package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Complaint
import com.example.model.EscalationTier
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantGoldDark
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.Slate100Text
import com.example.ui.theme.Slate400Text
import com.example.ui.theme.Slate500Text

@Composable
fun EscalationTrackerView(
    complaint: Complaint,
    onEscalateClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLevel = complaint.escalationTier.level

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("escalation_tracker_view"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x0DFFFFFF)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
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
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Escalation Protection",
                        tint = ElegantGoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "NERC SLA ESCALATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        ),
                        color = Slate100Text
                    )
                }

                Text(
                    text = "ESCALATED LEVEL $currentLevel",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        color = ElegantGoldPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step line & nodes
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                EscalationTier.entries.forEachIndexed { index, tier ->
                    val isPast = tier.level < currentLevel
                    val isCurrent = tier.level == currentLevel

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isPast -> ElegantGoldDark
                                        isCurrent -> ElegantGoldPrimary
                                        else -> Color(0xFF1E2430)
                                    }
                                )
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) Color(0x66FACC15) else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPast) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Step Completed",
                                    tint = Color(0xFF0A0C10),
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Text(
                                    text = "${tier.level}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (isCurrent) Color(0xFF0A0C10) else Slate400Text
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = when (tier) {
                                EscalationTier.LEVEL_1 -> "Crew"
                                EscalationTier.LEVEL_2 -> "District"
                                EscalationTier.LEVEL_3 -> "DisCo HQ"
                                EscalationTier.LEVEL_4 -> "NERC"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isCurrent) Slate100Text else Slate500Text,
                            maxLines = 1
                        )
                    }

                    if (index < EscalationTier.entries.size - 1) {
                        Box(
                            modifier = Modifier
                                .weight(0.6f)
                                .height(2.dp)
                                .background(
                                    if (tier.level < currentLevel) ElegantGoldPrimary
                                    else Color(0xFF1E2430)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current Active Authority Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x0DFFFFFF))
                    .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "Current Authority Handling Ticket:",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Slate500Text
                    )
                    Text(
                        text = complaint.escalationTier.authority,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate100Text
                        )
                    )
                    Text(
                        text = "Standard SLA window: ${complaint.escalationTier.maxSlaHours} hours before auto-escalation",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = ElegantGoldPrimary
                        )
                    )
                }
            }

            if (currentLevel < 4) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onEscalateClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("escalate_ticket_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Trigger Escalation",
                        tint = ElegantGoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Trigger Manual Escalation (${complaint.escalationTier.nextTier()?.title ?: "Next"})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Slate100Text
                        )
                    )
                }
            }
        }
    }
}
