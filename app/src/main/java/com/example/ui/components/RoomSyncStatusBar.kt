package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.extendedColors

/** Plain words for the save state, shared by the status line and the header chip. */
private data class SaveState(val label: String, val icon: ImageVector, val color: Color, val container: Color)

@Composable
private fun saveState(isSyncing: Boolean, isOfflineMode: Boolean, pendingCount: Int): SaveState {
    val ext = MaterialTheme.extendedColors
    return when {
        isSyncing -> SaveState("Saving…", Icons.Default.Sync, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
        isOfflineMode || pendingCount > 0 -> SaveState(
            if (pendingCount > 0) "Offline – $pendingCount will send when online" else "Offline – will send when online",
            Icons.Default.CloudOff, ext.warning, ext.warningContainer
        )
        else -> SaveState("Saved", Icons.Default.CheckCircle, ext.success, ext.successContainer)
    }
}

/**
 * One small, calm line at the top of the app: "✓ Saved" / "Saving…" / "Offline – will send when online".
 * Tap it to see your saved reports.
 */
@Composable
fun RoomSyncStatusBar(
    isSyncing: Boolean,
    isOfflineMode: Boolean,
    pendingSyncCount: Int,
    lastSyncTime: String,
    onSyncNow: () -> Unit,
    onToggleOfflineMode: () -> Unit,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("room_sync_status_bar"),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoomSyncHeaderPill(
            isSyncing = isSyncing,
            isOfflineMode = isOfflineMode,
            pendingCount = pendingSyncCount,
            onClick = onOpenDetails
        )
    }
}

/** Small one-line chip. Never wraps letter by letter: one line, ellipsis if the screen is narrow. */
@Composable
fun RoomSyncHeaderPill(
    isSyncing: Boolean,
    isOfflineMode: Boolean,
    pendingCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = saveState(isSyncing, isOfflineMode, pendingCount)
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = state.container,
        modifier = modifier
            .heightIn(min = 36.dp)
            .clickable(onClickLabel = "See your saved reports") { onClick() }
            .testTag("room_sync_header_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = state.icon,
                contentDescription = null,
                tint = state.color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = state.label,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
