package com.example.ui.solutions.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.solutions.SolutionsPrefs
import com.example.model.SolutionCategory
import com.example.model.UserProfile
import com.example.ui.solutions.common.BrightPermissions
import com.example.ui.solutions.common.SolutionsAutomation
import com.example.ui.theme.extendedColors

/** True when the tool is on but a permission it needs for background work is missing. */
fun needsPermission(context: android.content.Context, info: SolutionInfo): Boolean =
    info.permissions.isNotEmpty() && !BrightPermissions.hasAny(context, info.permissions)

/**
 * "Solutions settings": the 25 automatic tools, grouped in six plain-language sections. Each row
 * is a short title, one line of explanation and an on/off switch; tapping it opens its settings page.
 */
@Composable
fun SolutionsSettingsScreen(
    userProfile: UserProfile,
    initialSolution: Int,
    isBatSignalMode: Boolean,
    onToggleBatSignalMode: (Boolean) -> Unit,
    onOpenForum: () -> Unit,
    onOpenHazardForm: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { SolutionsPrefs(context) }
    var selected by remember { mutableStateOf(initialSolution.takeIf { n -> SolutionCatalog.settingsBased.any { it.number == n } }) }
    val enabled = remember { mutableStateMapOf<Int, Boolean>().apply { SolutionCatalog.settingsBased.forEach { put(it.number, prefs.isEnabled(it.number)) } } }

    // Re-check permissions when the user comes back from the system Settings app.
    var resumeTick by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) resumeTick++ }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    fun setEnabled(n: Int, on: Boolean) {
        prefs.setEnabled(n, on)
        enabled[n] = on
        SolutionsAutomation.sync(context)
    }

    BackHandler(enabled = selected != null) { selected = null }

    val current = selected
    if (current != null) {
        SolutionSettingsDetail(
            info = SolutionCatalog.info(current),
            isOn = enabled[current] ?: true,
            onToggle = { setEnabled(current, it) },
            userProfile = userProfile,
            isBatSignalMode = isBatSignalMode,
            onToggleBatSignalMode = onToggleBatSignalMode,
            onOpenForum = onOpenForum,
            onOpenHazardForm = onOpenHazardForm,
            resumeTick = resumeTick,
            onBack = { selected = null }
        )
        return
    }

    Column(Modifier.fillMaxSize()) {
        SettingsTopBar("Bright tools", onBack)
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                Text(
                    "These tools work on their own. Turn any of them off, or tap one to change how it works.",
                    fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            SolutionCategory.entries.forEach { cat ->
                val rows = SolutionCatalog.settingsBased.filter { it.category == cat }
                if (rows.isNotEmpty()) {
                    item(key = "cat_${cat.id}") {
                        Text(
                            SolutionCatalog.categoryTitle(cat), fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp)
                        )
                    }
                    rows.forEach { info ->
                        item(key = "row_${info.number}") {
                            val on = enabled[info.number] ?: true
                            // resumeTick is read so the hint refreshes after visiting system Settings.
                            val missing = resumeTick >= 0 && on && needsPermission(context, info)
                            SettingsRow(info.title, info.summary, on, missing, onToggle = { setEnabled(info.number, it) }, onClick = { selected = info.number })
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = 16.dp))
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
internal fun SettingsTopBar(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
internal fun SettingsRow(
    title: String,
    summary: String,
    on: Boolean,
    needsPermission: Boolean,
    onToggle: (Boolean) -> Unit,
    onClick: (() -> Unit)?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(summary, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (needsPermission) {
                Text("Needs permission - tap to turn on", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.extendedColors.warning)
            }
        }
        Switch(
            checked = on,
            onCheckedChange = onToggle,
            modifier = Modifier.padding(start = 8.dp).semantics { contentDescription = title }
        )
        if (onClick != null) Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** A simple settings page: title, the main switch, any extra options, then the tool itself. */
@Composable
internal fun SettingsPage(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        SettingsTopBar(title, onBack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            content()
            Spacer(Modifier.height(40.dp))
        }
    }
}
