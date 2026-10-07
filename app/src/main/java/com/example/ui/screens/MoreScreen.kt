package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** One row on the More screen. */
private data class MoreItem(
    val key: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val isDestructive: Boolean = false
)

/**
 * "More" tab: holds the tools that used to crowd the bottom bar (Anti-Theft, Grid Hub) and the
 * Home header/tools menu (gateway, dossier, rebates, forum, energy, account actions).
 */
@Composable
fun MoreScreen(
    onOpenAntiTheft: () -> Unit,
    onOpenGridHub: () -> Unit,
    onOpenSmartMeterGateway: () -> Unit,
    onOpenEstateExcoDossier: () -> Unit,
    onOpenClearinghouse: () -> Unit,
    onOpenTransformerForum: () -> Unit,
    onOpenEnergyOptimization: () -> Unit,
    onOpenProfileAdmin: () -> Unit,
    onOpenOnboarding: () -> Unit,
    onLockApp: () -> Unit,
    onLogOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    val toolItems = listOf(
        MoreItem("anti_theft", "Anti-Theft", "Report cable theft and vandalism, anonymously if you like", Icons.Default.Security, onOpenAntiTheft),
        MoreItem("grid_hub", "Grid Hub", "Tariffs, billing disputes, language and data-saver settings", Icons.Default.Bolt, onOpenGridHub),
        MoreItem("smart_meter_gateway", "Smart Meter", "Check your meter's connection and readings", Icons.Default.Router, onOpenSmartMeterGateway),
        MoreItem("estate_exco", "Estate & Dossier", "Estate dues, compensation claims and NERC dossier", Icons.Default.Gavel, onOpenEstateExcoDossier),
        MoreItem("clearinghouse", "Rebates & Refunds", "Claim rebate tokens for long outages", Icons.Default.AccountBalanceWallet, onOpenClearinghouse),
        MoreItem("transformer_forum", "Neighbour Forum", "Talk with people on your transformer", Icons.Default.Forum, onOpenTransformerForum),
        MoreItem("energy_optimization", "Energy & Surge Guard", "Appliance budget, restore alerts and surge warnings", Icons.Default.Tune, onOpenEnergyOptimization)
    )
    val accountItems = listOf(
        MoreItem("profile_admin", "Profile & Security", "Linked meters, biometrics and privacy", Icons.Default.AccountCircle, onOpenProfileAdmin),
        MoreItem("switch_meter", "Sign In / Switch Meter", "Use a different meter account", Icons.Default.SwapHoriz, onOpenOnboarding),
        MoreItem("lock_app", "Lock App", "Require your PIN or fingerprint to reopen", Icons.Default.Lock, onLockApp),
        MoreItem("log_out", "Log Out", "Sign out of Bright on this phone", Icons.AutoMirrored.Filled.ExitToApp, { showLogoutConfirmDialog = true }, isDestructive = true)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("more_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { MoreSectionTitle("Tools") }
        toolItems.forEach { entry -> item(key = entry.key) { MoreRow(entry) } }
        item { MoreSectionTitle("Account") }
        accountItems.forEach { entry -> item(key = entry.key) { MoreRow(entry) } }
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("Log out of Bright?", fontWeight = FontWeight.Bold) },
            text = { Text("You'll need to sign in again to see your reports.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        onLogOut()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("more_confirm_logout_button")
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MoreSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun MoreRow(entry: MoreItem) {
    val accent: Color = if (entry.isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = entry.onClick)
            .testTag("more_item_${entry.key}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = entry.icon,
                    contentDescription = null, // decorative: the title text names the row
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (entry.isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
