package com.example.ui.solutions.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.solutions.SolutionsPrefs
import com.example.model.FeederBand
import com.example.model.UserProfile
import com.example.ui.solutions.*
import com.example.ui.solutions.common.ActionButton
import com.example.ui.solutions.common.ChoiceRow
import com.example.ui.solutions.common.InfoNote
import com.example.ui.solutions.common.SirenPlayer
import com.example.ui.solutions.common.SolutionsAutomation
import com.example.ui.solutions.common.rememberPermissionAction
import com.example.ui.solutions.refund.RefundTrackerFeature

@Composable
internal fun SolutionSettingsDetail(
    info: SolutionInfo,
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    userProfile: UserProfile,
    isBatSignalMode: Boolean,
    onToggleBatSignalMode: (Boolean) -> Unit,
    onOpenForum: () -> Unit,
    onOpenHazardForm: () -> Unit,
    resumeTick: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val missing = resumeTick >= 0 && isOn && needsPermission(context, info)
    var granted by remember { mutableStateOf(0) }
    val askPermission = rememberPermissionAction(
        permissions = info.permissions,
        title = "Allow Bright to help",
        rationale = when (info.permissions) {
            com.example.ui.solutions.common.BrightPermissions.LOCATION -> "Bright uses your location only to find where you are when this tool runs."
            else -> "Bright needs to show notifications so this tool can tell you things, even when the app is closed."
        }
    ) { granted++ ; SolutionsAutomation.sync(context) }

    SettingsPage(info.title, onBack) {
        SettingsRow("On", info.summary, isOn, needsPermission = false, onToggle = onToggle, onClick = null)
        if (missing && granted == 0) {
            InfoNote("This tool needs your permission to work fully.")
            ActionButton("Turn on", null, askPermission)
        }
        if (isOn) {
            ExtraSettings(info.number, userProfile)
            HorizontalDivider()
            val profile = if (info.number == 12 || info.number == 11) bandAdjusted(context, userProfile) else userProfile
            SolutionFeatureContent(info.number, profile, isBatSignalMode, onToggleBatSignalMode, onOpenHazardForm, onOpenForum)
        } else {
            Text("This tool is off. Turn it on above to use it.", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun bandAdjusted(context: android.content.Context, profile: UserProfile): UserProfile {
    val code = SolutionsPrefs(context).refundBand
    val band = FeederBand.entries.firstOrNull { it.code == code } ?: return profile
    return profile.copy(feederBand = band)
}

/** Small extra options shown above the tool, in plain words. */
@Composable
private fun ExtraSettings(number: Int, userProfile: UserProfile) {
    val context = LocalContext.current
    val prefs = remember { SolutionsPrefs(context) }
    fun changed() = SolutionsAutomation.sync(context)

    when (number) {
        12 -> {
            var auto by remember { mutableStateOf(prefs.autoTrackOutages) }
            var alerts by remember { mutableStateOf(prefs.shortfallAlerts) }
            var band by remember { mutableStateOf(prefs.refundBand.ifBlank { userProfile.feederBand.code }) }
            SettingsRow("Track light by itself", "Keep your phone on its charger at home. When the charger loses power, Bright notes the light went off.", auto, false,
                onToggle = { auto = it; prefs.autoTrackOutages = it; changed() }, onClick = null)
            SettingsRow("Tell me when I'm owed money", "A message when you get fewer hours than you pay for.", alerts, false,
                onToggle = { alerts = it; prefs.shortfallAlerts = it; changed() }, onClick = null)
            Text("Your band (on your bill or token receipt)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            ChoiceRow(FeederBand.entries.map { it.code }, band) {
                band = it
                prefs.refundBand = it
                prefs.promisedHours = FeederBand.entries.first { b -> b.code == it }.minimumHours
                changed()
            }
            Text("If the automatic log is wrong, use the Light ON / Light OFF buttons below to correct it.", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        11 -> {
            var weekly by remember { mutableStateOf(prefs.weeklyAuditSummary) }
            SettingsRow("Weekly summary", "Once a week, a message with your average hours of light.", weekly, false,
                onToggle = { weekly = it; prefs.weeklyAuditSummary = it; changed() }, onClick = null)
        }
        26 -> {
            var onReturn by remember { mutableStateOf(prefs.surgeOnReturn) }
            SettingsRow("Remind me when power returns", "Wait 2 minutes before plugging things back in.", onReturn, false,
                onToggle = { onReturn = it; prefs.surgeOnReturn = it }, onClick = null)
        }
        27 -> {
            val labels = mapOf("NOTIFICATION" to "Gentle", "RINGTONE" to "Ringtone", "ALARM" to "Loud")
            var sound by remember { mutableStateOf(prefs.sirenSound) }
            var volume by remember { mutableFloatStateOf(prefs.sirenVolume.toFloat()) }
            Text("Sound", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            ChoiceRow(labels.values.toList(), labels[sound] ?: "Gentle") { chosen ->
                sound = labels.entries.first { it.value == chosen }.key
                prefs.sirenSound = sound
            }
            Text("Volume", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Slider(
                value = volume, onValueChange = { volume = it }, valueRange = 10f..100f,
                onValueChangeFinished = { prefs.sirenVolume = volume.toInt() },
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Alert volume" }
            )
            ActionButton("Play the sound", Icons.Default.VolumeUp, { prefs.sirenVolume = volume.toInt(); SirenPlayer.play(context, 4) }, outlined = true)
        }
        30 -> {
            var news by remember { mutableStateOf(prefs.newsNotifications) }
            SettingsRow("Tell me about price news", "A message when there's a new price change.", news, false,
                onToggle = { news = it; prefs.newsNotifications = it; changed() }, onClick = null)
        }
        22 -> {
            var stage by remember { mutableStateOf(prefs.stageNotifications) }
            SettingsRow("Tell me when my report moves", "A message at each step of the repair.", stage, false,
                onToggle = { stage = it; prefs.stageNotifications = it }, onClick = null)
        }
        8 -> {
            val opts = listOf("Never", "10%", "15%", "20%")
            var t by remember { mutableStateOf(prefs.batSignalThreshold) }
            Text("Switch on by itself when battery is below", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            ChoiceRow(opts, if (t == 0) "Never" else "$t%") {
                t = if (it == "Never") 0 else it.removeSuffix("%").toInt()
                prefs.batSignalThreshold = t
            }
        }
        else -> Unit
    }
}

/** Routes a solution number to its working screen (used by Settings pages and the 5 quick actions). */
@Composable
fun SolutionFeatureContent(
    number: Int,
    userProfile: UserProfile,
    isBatSignalMode: Boolean,
    onToggleBatSignalMode: (Boolean) -> Unit,
    onOpenHazardForm: () -> Unit,
    onOpenForum: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (number) {
            1 -> TieredUrgencyCategoriserFeature(userProfile)
            2 -> GpsFaultGeofencingFeature(userProfile)
            3 -> AutomatedDispatchRouterFeature(userProfile)
            4 -> CriticalDangerRedButtonFeature(userProfile, onOpenHazardForm = onOpenHazardForm)
            5 -> DiagnosticStatusTrackerFeature(userProfile)
            6 -> OfflineUssdBridgeFeature(userProfile)
            7 -> TamperCrowdsourcingFeature(userProfile)
            8 -> LowPowerBatSignalFeature(isBatSignalMode, onToggleBatSignalMode, userProfile)
            9 -> UniversalMeterSyncFeature(userProfile)
            10 -> NationalGridPulseMonitorFeature()
            11 -> AutomatedBandAuditorFeature(userProfile)
            12 -> RefundTrackerFeature(userProfile)
            13 -> CommunityConsumptionCalculatorFeature(userProfile)
            14 -> MeterWaitlistTrackerFeature(userProfile)
            15 -> OfflineTokenVendingFeature(userProfile)
            16 -> VisualProofOverrideFeature(userProfile)
            17 -> WakeUpStreetAlertsFeature(userProfile)
            18 -> UserTrustScoreFeature()
            19 -> NeighborhoodGridForumFeature(userProfile, onOpenFullForum = onOpenForum)
            20 -> MultiLingualVoiceReportingFeature(userProfile)
            21 -> AnonymousWhistleblowerFeature(userProfile)
            22 -> PizzaStyleDeliveryTrackerFeature(userProfile)
            23 -> ConsumerClosureVerificationFeature(userProfile)
            24 -> FaultHistoryLogFeature(userProfile)
            25 -> InventoryRequestMonitorFeature(userProfile)
            26 -> SurgeReturnWarningFeature()
            27 -> GridIsBackAudioSirenFeature()
            28 -> ApplianceLoadBudgeterFeature()
            29 -> HybridEnergyOptimizerFeature()
            30 -> TariffFlashNewsFeature()
            else -> Unit
        }
    }
}
