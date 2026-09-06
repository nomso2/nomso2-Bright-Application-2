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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuditingHourRecord
import com.example.model.GridTelemetry
import com.example.model.UserProfile
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkCardStart
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.ElegantGreenLive
import com.example.ui.theme.Slate100Text
import com.example.ui.theme.Slate400Text
import com.example.ui.theme.Slate500Text

/**
 * Phase 2: Geofenced Grid Telemetry & Contractual Hour Auditing Matrix
 */
@Composable
fun AuditingMatrixCard(
    userProfile: UserProfile,
    auditingRecords: List<AuditingHourRecord>,
    telemetry: GridTelemetry,
    onOpenClearinghouse: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val todayRecord = auditingRecords.firstOrNull() ?: AuditingHourRecord("Today", "Sunday", 20.0, 15.2)
    val weeklyDelivered = auditingRecords.sumOf { it.actualDeliveredHours }
    val weeklyPromised = auditingRecords.sumOf { it.promisedBandHours }
    val totalShortfall = auditingRecords.sumOf { it.shortfallHours }
    val totalRebateDue = auditingRecords.sumOf { it.compensationDueNgn }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("contractual_auditing_matrix_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0x26FACC15)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = ElegantGoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "CONTRACTUAL HOUR AUDITING MATRIX",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = ElegantGoldPrimary
                        )
                        Text(
                            text = "${userProfile.feederBand.code} • 20h Target SLA Ledger",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Slate100Text
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (todayRecord.isSlaBreached) Color(0x26EF4444) else Color(0x1A22C55E))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (todayRecord.isSlaBreached) "SLA SHORTFALL" else "ON TARGET",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (todayRecord.isSlaBreached) Color(0xFFEF4444) else Color(0xFF4ADE80)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Today's Live Power Delivery Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Delivered Today: ${todayRecord.actualDeliveredHours} hrs / 20.0 hrs",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = Slate100Text
                )
                Text(
                    text = "${((todayRecord.actualDeliveredHours / 20.0) * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = ElegantGoldPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (todayRecord.actualDeliveredHours / 20.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (todayRecord.actualDeliveredHours >= 16.0) ElegantGoldPrimary else Color(0xFFEF4444),
                trackColor = Color(0x1AFFFFFF)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Geofenced Core Isolation & Feeder Isolation Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x14FFFFFF))
                    .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Geofenced Isolation: ${userProfile.transformerId}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Slate100Text
                        )
                    }

                    Text(
                        text = "Radius 850m LV Feeder",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = Slate400Text)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7-Day Cumulative Summary Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Shortfall hours
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x0DEF4444))
                        .border(1.dp, Color(0x26EF4444), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "7-DAY SHORTFALL",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFFFCA5A5)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${Math.round(totalShortfall * 10.0) / 10.0} hrs",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFEF4444)
                        )
                        Text(
                            text = "Verified DisCo Default",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = Slate500Text
                        )
                    }
                }

                // Rebate Entitlement Due
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1422C55E))
                        .border(1.dp, Color(0x2622C55E), RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenClearinghouse)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "ESCROW REBATE DUE",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFF86EFAC)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₦${totalRebateDue.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF4ADE80)
                        )
                        Text(
                            text = "Tap to Claim Escrow Token ›",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = ElegantGoldPrimary
                        )
                    }
                }
            }

            // Expandable 7-day Breakdown
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Hide Daily Ledger" else "View 7-Day Immutable SLA Ledger",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = ElegantGoldPrimary
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ElegantGoldPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    auditingRecords.forEach { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x0DFFFFFF))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${record.dateText} (${record.dayName})",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Slate100Text
                                )
                                Text(
                                    text = "Target: 20.0h • Delivered: ${record.actualDeliveredHours}h",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    color = Slate400Text
                                )
                            }

                            if (record.isSlaBreached) {
                                Text(
                                    text = "-${record.shortfallHours}h (₦${record.compensationDueNgn.toInt()})",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFEF4444)
                                )
                            } else {
                                Text(
                                    text = "MET (100%)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF4ADE80)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
