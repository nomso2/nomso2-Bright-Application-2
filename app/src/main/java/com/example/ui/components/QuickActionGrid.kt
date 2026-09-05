package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.Slate400Text

data class QuickActionItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun QuickActionGrid(
    onNavigateMap: () -> Unit,
    onNavigateVandalism: () -> Unit,
    onNavigateHazard: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateBilling: () -> Unit,
    onNavigateLoadShed: () -> Unit,
    onNavigateEscalate: () -> Unit,
    onNavigateOthers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        QuickActionItem("Outage Map", Icons.Default.LocationOn, onNavigateMap),
        QuickActionItem("Theft Rpt", Icons.Default.Security, onNavigateVandalism),
        QuickActionItem("Hazard", Icons.Default.Warning, onNavigateHazard),
        QuickActionItem("History", Icons.Default.History, onNavigateHistory),
        QuickActionItem("Billing", Icons.Default.Payment, onNavigateBilling),
        QuickActionItem("Load Shed", Icons.Default.Bolt, onNavigateLoadShed),
        QuickActionItem("Escalate", Icons.Default.TrendingUp, onNavigateEscalate),
        QuickActionItem("Others", Icons.Default.Add, onNavigateOthers)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1 (first 4 items)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.take(4).forEach { item ->
                QuickActionCell(item = item, modifier = Modifier.weight(1f))
            }
        }

        // Row 2 (next 4 items)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.drop(4).take(4).forEach { item ->
                QuickActionCell(item = item, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun QuickActionCell(
    item: QuickActionItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ElegantDarkSurface)
            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = item.onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp)
            .testTag("quick_action_${item.title.lowercase().replace(" ", "_")}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x14FFFFFF))
                    .border(1.dp, Color(0x1AFFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = ElegantGoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = Slate400Text,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
