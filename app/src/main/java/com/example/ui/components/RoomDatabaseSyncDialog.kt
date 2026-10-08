package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.OfflineSyncQueueEntity

/** "Your saved reports": plain status, an offline switch, and what is waiting to send. */
@Composable
fun RoomDatabaseSyncDialog(
    isSyncing: Boolean,
    isOfflineMode: Boolean,
    pendingSyncCount: Int,
    lastSyncTime: String,
    pendingActions: List<OfflineSyncQueueEntity>,
    onToggleOfflineMode: () -> Unit,
    onSyncNow: () -> Unit,
    onAddTestOfflineAction: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("room_database_sync_dialog"),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Your saved reports",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                RoomSyncHeaderPill(
                    isSyncing = isSyncing,
                    isOfflineMode = isOfflineMode,
                    pendingCount = pendingSyncCount,
                    onClick = {}
                )
                Text(
                    text = when {
                        isSyncing -> "Sending your reports now."
                        pendingSyncCount > 0 -> "$pendingSyncCount report(s) are kept safe on this phone. They will send when you are online."
                        else -> "Everything is sent. Last checked: $lastSyncTime"
                    },
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Work offline",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Keep reports on this phone until you turn this off.",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = isOfflineMode,
                        onCheckedChange = { onToggleOfflineMode() },
                        modifier = Modifier.testTag("dialog_offline_mode_switch")
                    )
                }

                if (pendingActions.isNotEmpty()) {
                    Text(
                        text = "Waiting to send",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    pendingActions.take(3).forEach { action ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = plainActionName(action.actionType),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Number: ${action.referenceId}",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    if (pendingActions.size > 3) {
                        Text(
                            text = "and ${pendingActions.size - 3} more",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSyncNow,
                enabled = !isSyncing,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.heightIn(min = 56.dp).testTag("dialog_sync_now_button")
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSyncing) "Sending…" else "Send now", fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 56.dp).testTag("dialog_close_button")
            ) {
                Text("Close", fontSize = 17.sp, maxLines = 1)
            }
        }
    )
}

private fun plainActionName(actionType: String): String = when (actionType) {
    "REPORT_FAULT_OFFLINE" -> "Fault report"
    else -> actionType.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}
