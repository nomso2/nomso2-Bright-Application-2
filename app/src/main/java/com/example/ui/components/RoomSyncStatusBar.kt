package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElegantGoldPrimary

/**
 * Visual indicator status bar showing whether the app is currently syncing data
 * or operating in offline mode using the Room database.
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
    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sync_rotation"
    )

    // Dynamic background and border based on state
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSyncing -> Color(0xFF1E293B)
            isOfflineMode -> Color(0xFF291B07)
            else -> Color(0xFF0D1B16)
        },
        label = "bg_color"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isSyncing -> ElegantGoldPrimary.copy(alpha = 0.7f)
            isOfflineMode -> Color(0xFFF59E0B).copy(alpha = 0.8f)
            else -> Color(0xFF22C55E).copy(alpha = 0.4f)
        },
        label = "border_color"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenDetails() }
            .testTag("room_sync_status_bar"),
        color = backgroundColor,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Icon & State Label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = when {
                                    isSyncing -> ElegantGoldPrimary.copy(alpha = 0.2f)
                                    isOfflineMode -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                    else -> Color(0xFF22C55E).copy(alpha = 0.2f)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isSyncing -> {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Syncing in progress",
                                    tint = ElegantGoldPrimary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .rotate(rotationAngle)
                                        .testTag("icon_syncing_spinning")
                                )
                            }
                            isOfflineMode -> {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Offline Mode Active",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .testTag("icon_offline_mode")
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Room Database Synced",
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .testTag("icon_online_synced")
                                )
                            }
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when {
                                    isSyncing -> "SYNCING DATA"
                                    isOfflineMode -> "OFFLINE MODE"
                                    else -> "ONLINE • ROOM SYNCED"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = when {
                                    isSyncing -> ElegantGoldPrimary
                                    isOfflineMode -> Color(0xFFFBBF24)
                                    else -> Color(0xFF4ADE80)
                                }
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // Room DB SQLite Chip
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "Room SQLite",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = Color(0xFFCBD5E1)
                                    )
                                }
                            }
                        }

                        Text(
                            text = when {
                                isSyncing -> "Flushing offline queue & syncing grid telemetry..."
                                isOfflineMode -> {
                                    if (pendingSyncCount > 0) "$pendingSyncCount action(s) cached locally in Room database"
                                    else "Local Room DB active (zero data loss on outage)"
                                }
                                else -> "Room cache up to date • Synced $lastSyncTime"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right Quick Action Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isOfflineMode) {
                        Button(
                            onClick = onSyncNow,
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF59E0B),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(26.dp)
                                .testTag("status_bar_sync_button")
                        ) {
                            Text(
                                text = "Sync",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (!isSyncing) {
                        IconButton(
                            onClick = onSyncNow,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("status_bar_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Trigger Manual Sync",
                                tint = Color(0xFF4ADE80),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Info Button to open detailed Room Database sheet
                    IconButton(
                        onClick = onOpenDetails,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("status_bar_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Room Database Details",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Syncing Progress Indicator Line
            AnimatedVisibility(
                visible = isSyncing,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .testTag("syncing_linear_progress"),
                    color = ElegantGoldPrimary,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        }
    }
}

/**
 * Compact pill indicator designed for top headers or compact bars
 */
@Composable
fun RoomSyncHeaderPill(
    isSyncing: Boolean,
    isOfflineMode: Boolean,
    pendingCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pill_spin")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pill_rotation"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = when {
            isSyncing -> Color(0xFF1E293B)
            isOfflineMode -> Color(0xFF332008)
            else -> Color(0xFF0F2618)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                isSyncing -> ElegantGoldPrimary.copy(alpha = 0.6f)
                isOfflineMode -> Color(0xFFF59E0B).copy(alpha = 0.6f)
                else -> Color(0xFF22C55E).copy(alpha = 0.4f)
            }
        ),
        modifier = modifier
            .clickable { onClick() }
            .testTag("room_sync_header_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            when {
                isSyncing -> {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Syncing",
                        tint = ElegantGoldPrimary,
                        modifier = Modifier
                            .size(12.dp)
                            .rotate(rotationAngle)
                    )
                    Text(
                        text = "Syncing...",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElegantGoldPrimary
                    )
                }
                isOfflineMode -> {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Offline",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (pendingCount > 0) "Offline ($pendingCount)" else "Room DB",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFBBF24)
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF22C55E), CircleShape)
                    )
                    Text(
                        text = "Room Synced",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4ADE80)
                    )
                }
            }
        }
    }
}
