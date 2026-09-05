package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Complaint
import com.example.model.ComplaintStatus
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.ElegantRedHazard
import com.example.ui.theme.Slate100Text
import com.example.ui.theme.Slate300Text
import com.example.ui.theme.Slate400Text
import com.example.ui.theme.Slate500Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ComplaintCard(
    complaint: Complaint,
    onEscalateClicked: (String) -> Unit,
    onUpvoteClicked: (String) -> Unit,
    onConfirmResolutionClicked: (String) -> Unit,
    onAdvanceStatusDemo: (String, ComplaintStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("complaint_card_${complaint.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = ElegantDarkSurface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Ticket ID & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Active Complaint: #${complaint.id}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Slate100Text
                        )
                        if (complaint.isHazardEmergency) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ElegantRedHazard)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Emergency Hazard",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "SOS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "Reported: ${dateFormat.format(Date(complaint.reportedAt))}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Slate500Text
                    )
                }

                // Status Badge
                StatusBadge(status = complaint.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Description
            Text(
                text = complaint.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Slate100Text
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = complaint.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate400Text
            )

            if (complaint.imageUri != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x1A38BDF8))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (complaint.isVideo) Icons.Default.Videocam else Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (complaint.isVideo) "Video Evidence Attached" else "Photo Evidence Attached",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Auto-Clustering Indicator (Showing how many neighbors share this fault)
            if (complaint.autoClusteredCount > 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x0DFFFFFF))
                        .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Clustered Households",
                            tint = ElegantGoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${complaint.autoClusteredCount} verified neighbors on ${complaint.transformerId} auto-clustered into this ticket.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Slate300Text
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Technician Pill (matches Design HTML bg-white/5 pattern)
            if (complaint.assignedCrewName != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x0DFFFFFF))
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
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
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElegantGoldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ID",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    ),
                                    color = Color(0xFF0A0C10)
                                )
                            }
                            Column {
                                Text(
                                    text = "Technician: ${complaint.assignedCrewName}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Slate100Text
                                )
                                Text(
                                    text = "ETA: ~${complaint.etaMinutes ?: 30} mins • Contact: ${complaint.assignedCrewPhone ?: "Direct Hotline"}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = Slate500Text
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 4-Tier Automatic Escalation Pathway
            EscalationTrackerView(
                complaint = complaint,
                onEscalateClicked = { onEscalateClicked(complaint.id) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: Upvote & Confirm Resolved
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Community Upvoting / Priority booster
                OutlinedButton(
                    onClick = { onUpvoteClicked(complaint.id) },
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("upvote_button_${complaint.id}"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Upvote Fault Priority",
                        tint = ElegantGoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Prioritize (${complaint.upvotesCount})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate100Text
                        )
                    )
                }

                // Customer Sign-off Button (Rich gold CTA)
                Button(
                    onClick = { onConfirmResolutionClicked(complaint.id) },
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("confirm_resolution_button_${complaint.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantGoldPrimary,
                        contentColor = Color(0xFF0A0C10)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Confirm Light Restored",
                        tint = Color(0xFF0A0C10),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Light Restored",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0A0C10)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: ComplaintStatus) {
    val (bgColor, textColor, borderColor) = when (status) {
        ComplaintStatus.LOGGED -> Triple(Color(0x1A94A3B8), Slate300Text, Color(0x3394A3B8))
        ComplaintStatus.ASSIGNED -> Triple(Color(0x1A60A5FA), Color(0xFF60A5FA), Color(0x3360A5FA))
        ComplaintStatus.DISPATCHED -> Triple(Color(0x1AFACC15), ElegantGoldPrimary, Color(0x33FACC15))
        ComplaintStatus.WORK_IN_PROGRESS -> Triple(Color(0x26FACC15), ElegantGoldPrimary, Color(0x4DFACC15))
        ComplaintStatus.TESTING -> Triple(Color(0x1A22C55E), Color(0xFF4ADE80), Color(0x3322C55E))
        ComplaintStatus.RESOLVED -> Triple(Color(0x1A22C55E), Color(0xFF4ADE80), Color(0x3322C55E))
        ComplaintStatus.ESCALATED -> Triple(Color(0x26EF4444), ElegantRedHazard, Color(0x4DEF4444))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.displayName.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            ),
            color = textColor
        )
    }
}
