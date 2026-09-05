package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OutageGridNode
import com.example.model.OutageStatus

@Composable
fun LiveOutageCanvasMap(
    nodes: List<OutageGridNode>,
    selectedNode: OutageGridNode?,
    onNodeSelected: (OutageGridNode) -> Unit,
    modifier: Modifier = Modifier
) {
    var filterStatus by remember { mutableStateOf<OutageStatus?>(null) }

    val filteredNodes = remember(nodes, filterStatus) {
        if (filterStatus == null) nodes else nodes.filter { it.status == filterStatus }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = filterStatus == null,
                onClick = { filterStatus = null },
                label = { Text("All Nodes (${nodes.size})", fontSize = 11.sp) },
                modifier = Modifier.testTag("filter_chip_all")
            )
            FilterChip(
                selected = filterStatus == OutageStatus.FAULT_DOWN,
                onClick = { filterStatus = if (filterStatus == OutageStatus.FAULT_DOWN) null else OutageStatus.FAULT_DOWN },
                label = { Text("Blackouts", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                    selectedLabelColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.testTag("filter_chip_faults")
            )
            FilterChip(
                selected = filterStatus == OutageStatus.MAINTENANCE,
                onClick = { filterStatus = if (filterStatus == OutageStatus.MAINTENANCE) null else OutageStatus.MAINTENANCE },
                label = { Text("Maintenance", fontSize = 11.sp) },
                modifier = Modifier.testTag("filter_chip_maintenance")
            )
            FilterChip(
                selected = filterStatus == OutageStatus.ACTIVE,
                onClick = { filterStatus = if (filterStatus == OutageStatus.ACTIVE) null else OutageStatus.ACTIVE },
                label = { Text("Power ON", fontSize = 11.sp) },
                modifier = Modifier.testTag("filter_chip_active")
            )
        }

        // The Interactive Canvas Map
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .testTag("live_outage_interactive_map_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF11141B) // Elegant Dark Bar Canvas
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2430)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(filteredNodes) {
                            detectTapGestures { tapOffset ->
                                val canvasW = size.width
                                val canvasH = size.height

                                // Find closest node to tap point within 40dp threshold
                                val hit = filteredNodes.minByOrNull { node ->
                                    val nodeX = node.xPosRatio * canvasW
                                    val nodeY = node.yPosRatio * canvasH
                                    val dx = nodeX - tapOffset.x
                                    val dy = nodeY - tapOffset.y
                                    dx * dx + dy * dy
                                }

                                if (hit != null) {
                                    val hitX = hit.xPosRatio * canvasW
                                    val hitY = hit.yPosRatio * canvasH
                                    val distSq = (hitX - tapOffset.x) * (hitX - tapOffset.x) + (hitY - tapOffset.y) * (hitY - tapOffset.y)
                                    if (distSq <= (48.dp.toPx() * 48.dp.toPx())) {
                                        onNodeSelected(hit)
                                    }
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // 1. Draw national 330kV transmission grid backbone lines (connecting key hubs)
                    val lineStroke = 2.dp.toPx()
                    val gridLineColor = Color(0xFF1E293B)
                    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)

                    // Connect nodes with simulated high-voltage transmission interconnects
                    for (i in filteredNodes.indices) {
                        for (j in i + 1 until filteredNodes.size) {
                            val n1 = filteredNodes[i]
                            val n2 = filteredNodes[j]
                            val p1 = Offset(n1.xPosRatio * w, n1.yPosRatio * h)
                            val p2 = Offset(n2.xPosRatio * w, n2.yPosRatio * h)
                            val distance = (p1 - p2).getDistance()

                            // Draw lines only between geographically adjacent nodes
                            if (distance < w * 0.45f) {
                                val isBothActive = n1.status == OutageStatus.ACTIVE && n2.status == OutageStatus.ACTIVE
                                val lineCol = if (isBothActive) Color(0xFF10B981).copy(alpha = 0.35f) else gridLineColor

                                drawLine(
                                    color = lineCol,
                                    start = p1,
                                    end = p2,
                                    strokeWidth = lineStroke,
                                    cap = StrokeCap.Round,
                                    pathEffect = if (!isBothActive) dashedEffect else null
                                )
                            }
                        }
                    }

                    // 2. Draw Nodes (Transformers / Injection Stations)
                    filteredNodes.forEach { node ->
                        val center = Offset(node.xPosRatio * w, node.yPosRatio * h)
                        val isSelected = selectedNode?.id == node.id
                        val nodeColor = Color(node.status.colorHex)

                        // Outer glowing pulse ring
                        drawCircle(
                            color = nodeColor.copy(alpha = if (isSelected) 0.45f else 0.2f),
                            radius = if (isSelected) 22.dp.toPx() else 14.dp.toPx(),
                            center = center
                        )

                        // Main node body
                        drawCircle(
                            color = nodeColor,
                            radius = if (isSelected) 9.dp.toPx() else 6.dp.toPx(),
                            center = center
                        )

                        // Selected inner highlight
                        if (isSelected) {
                            drawCircle(
                                color = Color.White,
                                radius = 3.5.dp.toPx(),
                                center = center
                            )
                        }
                    }
                }

                // Overlay Map Title & Compass
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF161920).copy(alpha = 0.92f))
                        .border(1.dp, Color(0xFF1E2430), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "NIGERIA SCADA GRID INTERCONNECT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFFACC15),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                // Legend at Bottom Right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF161920).copy(alpha = 0.92f))
                        .border(1.dp, Color(0xFF1E2430), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendDot(Color(0xFF10B981), "ON")
                        LegendDot(Color(0xFFEF4444), "FAULT")
                        LegendDot(Color(0xFFF59E0B), "MAINT")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected Node Details Card
        if (selectedNode != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("selected_outage_node_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedNode.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${selectedNode.city}, ${selectedNode.state} (${selectedNode.discoCode})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(selectedNode.status.colorHex).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = selectedNode.status.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = Color(selectedNode.status.colorHex)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Reported Faults",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${selectedNode.reportedFaults} tickets",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column {
                            Text(
                                text = "Affected Consumers",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (selectedNode.affectedConsumers > 0) "${selectedNode.affectedConsumers} homes" else "None",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column {
                            Text(
                                text = "Restoration ETA",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedNode.estimatedRestoration,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendDot(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
