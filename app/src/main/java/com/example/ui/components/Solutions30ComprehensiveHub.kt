package com.example.ui.components

import android.content.Context
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.solutions.SolutionsPrefs
import com.example.model.UserProfile
import com.example.ui.solutions.LowPowerBatSignalFeature
import com.example.ui.solutions.settings.SolutionCatalog
import com.example.ui.solutions.settings.SolutionFeatureContent
import com.example.ui.solutions.settings.needsPermission
import com.example.ui.theme.extendedColors

/**
 * The 30 solutions: five quick-action buttons that do the thing right away (SOS, speak a report,
 * photo, location, SMS) and a status list of the 25 tools that run on their own from Settings.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun Solutions30ComprehensiveHub(
    userProfile: UserProfile,
    isBatSignalMode: Boolean,
    onToggleBatSignalMode: (Boolean) -> Unit,
    onOpenForum: () -> Unit,
    onOpenRedDangerSOS: () -> Unit,
    onPlaySirenAlarm: () -> Unit,
    onOpenSolutionSettings: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { SolutionsPrefs(context) }
    var openAction by remember { mutableStateOf<Int?>(null) }

    // Low battery mode (#8) switches itself on below the chosen battery level.
    LaunchedEffect(Unit) {
        val threshold = prefs.batSignalThreshold
        val level = batteryLevel(context)
        if (prefs.isEnabled(8) && threshold > 0 && level != null && level < threshold && !isBatSignalMode) {
            onToggleBatSignalMode(true)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isBatSignalMode) {
            LowPowerBatSignalFeature(isBatSignalMode, onToggleBatSignalMode, userProfile)
            HorizontalDivider()
        }

        Text("Report a problem", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        QuickActionTile(4, Icons.Default.Warning, danger = true) { openAction = 4 }
        QuickActionTile(20, Icons.Default.Mic) { openAction = 20 }
        QuickActionTile(16, Icons.Default.CameraAlt) { openAction = 16 }
        QuickActionTile(2, Icons.Default.MyLocation) { openAction = 2 }
        QuickActionTile(6, Icons.Default.Sms) { openAction = 6 }

        Spacer(Modifier.height(8.dp))
        Text("Working for you", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text("These tools run on their own. Tap one to change it.", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column {
                SolutionCatalog.settingsBased.forEachIndexed { i, info ->
                    val on = prefs.isEnabled(info.number)
                    val missing = on && needsPermission(context, info)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .clickable { onOpenSolutionSettings(info.number) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("solution_status_${info.number}")
                    ) {
                        Text(info.title, fontSize = 16.sp, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(
                            when { missing -> "Needs permission"; on -> "On"; else -> "Off" },
                            fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                            color = when {
                                missing -> MaterialTheme.extendedColors.warning
                                on -> MaterialTheme.extendedColors.success
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (i < SolutionCatalog.settingsBased.size - 1) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
        OutlinedButton(onClick = { onOpenSolutionSettings(0) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Text("  All tool settings", fontSize = 16.sp)
        }
    }

    openAction?.let { n ->
        val info = SolutionCatalog.info(n)
        Dialog(onDismissRequest = { openAction = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxSize()) {
                    Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { openAction = null }) { Icon(Icons.Default.Close, contentDescription = "Close") }
                        Text(info.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    HorizontalDivider()
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                        SolutionFeatureContent(
                            number = n,
                            userProfile = userProfile,
                            isBatSignalMode = isBatSignalMode,
                            onToggleBatSignalMode = onToggleBatSignalMode,
                            onOpenHazardForm = { openAction = null; onOpenRedDangerSOS() },
                            onOpenForum = { openAction = null; onOpenForum() }
                        )
                        Spacer(Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(number: Int, icon: ImageVector, danger: Boolean = false, onClick: () -> Unit) {
    val info = SolutionCatalog.info(number)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (danger) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).semantics { contentDescription = "${info.title}. ${info.summary}" }
            .testTag("quick_action_$number")
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).background(
                    if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null,
                    tint = if (danger) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary)
            }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(info.title, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = if (danger) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer)
                Text(info.summary, fontSize = 15.sp,
                    color = if (danger) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

private fun batteryLevel(context: Context): Int? {
    val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager ?: return null
    val v = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    return if (v in 0..100) v else null
}
