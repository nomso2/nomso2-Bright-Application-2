package com.example.ui.solutions

import com.example.data.solutions.SupplyMath
import com.example.ui.solutions.common.TextInput
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.solutions.DemoOutageStatusSource
import com.example.data.solutions.FaultReportEntity
import com.example.data.solutions.Geo
import com.example.data.solutions.OutageDiagnosis
import com.example.data.solutions.Refs
import com.example.model.UserProfile
import com.example.ui.solutions.common.ActionButton
import com.example.ui.solutions.common.BrightPermissions
import com.example.ui.solutions.common.ChoiceRow
import com.example.ui.solutions.common.DemoDataBadge
import com.example.ui.solutions.common.DiscoContactButtons
import com.example.ui.solutions.common.FeatureHeader
import com.example.ui.solutions.common.Fmt
import com.example.ui.solutions.common.InfoNote
import com.example.ui.solutions.common.LocationFetcher
import com.example.ui.solutions.common.LocationPinner
import com.example.ui.solutions.common.PhotoThumb
import com.example.ui.solutions.common.PinnedLocation
import com.example.ui.solutions.common.SectionCard
import com.example.ui.solutions.common.SirenPlayer
import com.example.ui.solutions.common.SolutionIntents
import com.example.ui.solutions.common.StatRow
import com.example.ui.solutions.common.customerBlock
import com.example.ui.solutions.common.rememberSolutionsDao
import com.example.ui.solutions.common.rememberTakePhotoAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun CheckRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onChange)
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(label, fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
    }
}

/** Tier rules shared by the categoriser and the GPS cluster view. */
internal object TierRules {
    fun tier(scope: String, neighboursOn: Boolean, bangOrSmoke: Boolean, manyStreets: Boolean, nearbyReports: Int): Int = when {
        manyStreets || scope == "Whole area" -> 3
        bangOrSmoke || scope == "My street" || nearbyReports >= 3 -> 2
        neighboursOn -> 1
        else -> 1
    }

    fun label(tier: Int): String = when (tier) {
        3 -> "Whole area is off (level 3)"
        2 -> "Your street is off (level 2)"
        else -> "Only your house (level 1)"
    }

    fun advice(tier: Int): String = when (tier) {
        3 -> "Likely a feeder or 33kV injection substation problem. The DisCo control room handles these first."
        2 -> "Likely a blown transformer fuse or cable fault serving your street. A field crew is needed."
        else -> "Check your meter credit and the breaker / cut-out in your house first. If they're fine, report it."
    }
}

// SOLUTION 1: TIERED URGENCY CATEGORISER
@Composable
fun TieredUrgencyCategoriserFeature(userProfile: UserProfile) {
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val reports by dao.reports().collectAsState(initial = emptyList())
    var area by remember { mutableStateOf("Only my house") }
    var neighboursOn by remember { mutableStateOf(false) }
    var bang by remember { mutableStateOf(false) }
    var manyStreets by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf<String?>(null) }

    val recentNearby = reports.count {
        it.transformerId == userProfile.transformerId && it.stage < 4 &&
            System.currentTimeMillis() - it.createdAt < 2 * 3_600_000L
    }
    val tier = TierRules.tier(area, neighboursOn, bang, manyStreets, recentNearby)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(1, "Tiered Urgency Categoriser", "Answer three questions. Bright works out how serious it is.")
        SectionCard {
            Text("What is out?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            ChoiceRow(listOf("Only my house", "My street", "Whole area"), area) { area = it }
            CheckRow("My neighbours still have light", neighboursOn) { neighboursOn = it }
            CheckRow("I heard a bang or saw smoke at the transformer", bang) { bang = it }
            CheckRow("Several streets or the whole estate are dark", manyStreets) { manyStreets = it }
        }
        SectionCard {
            Text(TierRules.label(tier), fontWeight = FontWeight.Black, fontSize = 15.sp,
                color = if (tier == 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            Text(TierRules.advice(tier), fontSize = 16.sp)
            if (recentNearby >= 3) InfoNote("$recentNearby neighbours reported in the last 2 hours, so this is treated as a street problem.")
        }
        ActionButton("Send report", Icons.Default.Send, {
            val ref = Refs.make("BR")
            scope.launch {
                dao.insertReport(FaultReportEntity(
                    reference = ref, source = "TIER", tier = tier,
                    title = TierRules.label(tier),
                    details = "Area: $area; neighbours on: $neighboursOn; bang/smoke: $bang; many streets: $manyStreets",
                    transformerId = userProfile.transformerId, isHazard = bang
                ))
                submitted = ref
            }
        })
        submitted?.let { InfoNote("Report $it saved. Follow it in #22 Delivery Tracker; send it to your DisCo below.") }
        if (submitted != null) {
            DiscoContactButtons(
                userProfile, "Outage report ${submitted}",
                "Outage report ${submitted}: ${TierRules.label(tier)}.\n${customerBlock(userProfile)}"
            )
        }
    }
}

// SOLUTION 2: GPS GEOFENCING FOR FAULTS
@Composable
fun GpsFaultGeofencingFeature(userProfile: UserProfile) {
    val context = LocalContext.current
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val reports by dao.reports().collectAsState(initial = emptyList())
    var pin by remember { mutableStateOf<PinnedLocation?>(null) }
    var saved by remember { mutableStateOf<String?>(null) }

    val p = pin
    val nearby = if (p?.hasCoordinates == true) reports.filter {
        it.latitude != null && it.longitude != null &&
            Geo.distanceKm(p.latitude!!, p.longitude!!, it.latitude, it.longitude) <= 0.5
    } else emptyList()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(2, "GPS Geofencing for Faults", "Mark where the fault is so the repair team can find it.")
        LocationPinner(pin) { pin = it }
        if (p != null) {
            SectionCard {
                StatRow("Reports close by", "${nearby.size}")
                Text(
                    if (nearby.size >= 2) "Other reports nearby - this looks like the same fault."
                    else "You are the first to report here.",
                    fontSize = 16.sp
                )
                Text("Uses reports saved on this phone for now.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            ActionButton("Send report with this place", Icons.Default.Send, {
                val ref = Refs.make("GPS")
                scope.launch {
                    dao.insertReport(FaultReportEntity(
                        reference = ref, source = "GPS", tier = if (nearby.size >= 2) 2 else 1,
                        title = "Fault at a marked place", details = p.describe(),
                        transformerId = userProfile.transformerId,
                        latitude = p.latitude, longitude = p.longitude
                    ))
                    saved = ref
                }
            })
            saved?.let {
                InfoNote("Saved as $it.")
                InviteNeighboursPrompt(userProfile, it)
            }
            ActionButton("Share pin with DisCo / neighbours", Icons.Default.Share, {
                SolutionIntents.shareText(context, "Fault location",
                    "Electricity fault here: ${p.mapsLink() ?: p.label}\nMeter ${userProfile.meterNumber}, transformer ${userProfile.transformerId}")
            }, outlined = true)
        }
    }
}

// SOLUTION 3: AUTOMATED DISPATCH ROUTER -> crowdsourced crew tracker
// No DisCo publishes crew positions, so this shows real "crew seen / working / left" posts from
// residents on the same transformer (Firestore crew_sightings), cached on the phone for offline.
@Composable
fun AutomatedDispatchRouterFeature(userProfile: UserProfile) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val transformer = userProfile.transformerId
    val sightingsFlow = remember(transformer) {
        com.example.data.database.AppDatabase.getDatabase(context).crewSightingDao().observe(transformer)
    }
    val sightings by sightingsFlow.collectAsState(initial = emptyList())
    var pin by remember { mutableStateOf<PinnedLocation?>(null) }
    var note by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val cloudReady = com.example.data.cloud.BrightFirebase.isConfigured

    LaunchedEffect(transformer) {
        if (cloudReady && transformer.isNotBlank()) {
            com.example.data.cloud.CommunityFeed.refreshSightings(context, transformer)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(3, "Automated Dispatch Router", "See when neighbours on your transformer last saw a DisCo crew, and tell them when you see one.")
        if (transformer.isBlank()) InfoNote("Add your transformer in your profile to see crew sightings for it.")
        if (!cloudReady) InfoNote("Crew sightings need Bright's online service, which isn't switched on in this version yet. You can still call your DisCo below.", isWarning = true)
        SectionCard {
            Text("Crew sightings for ${transformer.ifBlank { "your transformer" }} (last 48 hours)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (sightings.isEmpty()) {
                Text("No neighbours have reported a crew yet.", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val recent = sightings.filter { System.currentTimeMillis() - it.createdAt < 6 * SupplyMath.HOUR_MS }
                com.example.data.cloud.CommunityFeed.CrewKind.entries.forEach { k ->
                    val people = recent.filter { it.kind == k.name }.map { it.reporterKey }.toSet().size
                    if (people > 0) StatRow(k.label, "$people neighbour${if (people == 1) "" else "s"} (last 6 h)")
                }
                sightings.take(15).forEach { s ->
                    val label = com.example.data.cloud.CommunityFeed.CrewKind.entries.firstOrNull { it.name == s.kind }?.label ?: s.kind
                    StatRow(label + if (s.isMine) " (you)" else "", Fmt.dateTime(s.createdAt))
                    if (s.note.isNotBlank()) Text(s.note, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        SectionCard {
            Text("I can see a crew", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            TextInput("Short note (optional, no names or phone numbers)", note, { note = it.take(140) })
            com.example.data.cloud.CommunityFeed.CrewKind.entries.forEach { k ->
                ActionButton(k.label, Icons.Default.LocalShipping, {
                    busy = true
                    scope.launch {
                        val ok = com.example.data.cloud.CommunityFeed.postSighting(context, transformer, userProfile.discoCode, k, note.trim())
                        status = if (ok) "Thanks. Your neighbours can see it now." else "Couldn't post. Check your internet and try again."
                        if (ok) note = ""
                        busy = false
                    }
                }, enabled = cloudReady && transformer.isNotBlank() && !busy, outlined = k != com.example.data.cloud.CommunityFeed.CrewKind.SEEN)
            }
            status?.let { Text(it, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary) }
            ActionButton("Refresh sightings", Icons.Default.Refresh, {
                scope.launch { com.example.data.cloud.CommunityFeed.refreshSightings(context, transformer) }
            }, enabled = cloudReady && transformer.isNotBlank(), outlined = true)
        }
        Text("Pin the fault so you can share its exact location with the DisCo:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LocationPinner(pin) { pin = it }
        pin?.let { p ->
            ActionButton("Share fault location", Icons.Default.Send, {
                SolutionIntents.shareText(context, "Fault location",
                    "Electricity fault here: ${p.mapsLink() ?: p.label}\nMeter ${userProfile.meterNumber}, transformer ${userProfile.transformerId}")
            }, outlined = true)
        }
        SolutionIntents.discoFor(userProfile)?.let { d ->
            if (d.primaryPhone.isNotBlank()) ActionButton("Call ${d.code} dispatch (${d.primaryPhone})", Icons.Default.Call, {
                SolutionIntents.dial(context, d.primaryPhone)
            }, outlined = true)
        }
    }
}

// SOLUTION 4: CRITICAL DANGER RED BUTTON
@Composable
fun CriticalDangerRedButtonFeature(userProfile: UserProfile, onOpenHazardForm: () -> Unit) {
    val context = LocalContext.current
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    var progress by remember { mutableFloatStateOf(0f) }
    var triggeredRef by remember { mutableStateOf<String?>(null) }
    var reportId by remember { mutableStateOf<Long?>(null) }
    var location by remember { mutableStateOf<PinnedLocation?>(null) }
    var photoPath by remember { mutableStateOf<String?>(null) }

    fun trigger() {
        if (triggeredRef != null) return
        val ref = Refs.make("SOS")
        triggeredRef = ref
        SirenPlayer.vibrate(context, longArrayOf(0, 300, 120, 300))
        scope.launch {
            reportId = dao.insertReport(FaultReportEntity(
                reference = ref, source = "SOS", tier = 3, title = "DANGER: live hazard",
                details = "SOS raised from the red button", transformerId = userProfile.transformerId, isHazard = true
            ))
        }
        if (BrightPermissions.hasAny(context, BrightPermissions.LOCATION)) {
            LocationFetcher.current(context) { loc ->
                if (loc != null) location = PinnedLocation(loc.latitude, loc.longitude, "GPS", loc.accuracy)
            }
        }
    }

    val takePhoto = rememberTakePhotoAction { file ->
        if (file != null) {
            photoPath = file.absolutePath
            val id = reportId
            if (id != null) scope.launch {
                dao.reportById(id)?.let { dao.updateReport(it.copy(photoPath = file.absolutePath)) }
            }
        }
    }

    val sosText = buildString {
        appendLine("EMERGENCY ${triggeredRef ?: ""}: live electrical hazard (fallen / sparking line).")
        location?.let { appendLine("Location: " + (it.mapsLink() ?: it.label)) }
        append(customerBlock(userProfile))
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(4, "Critical Danger Red Button", "For fallen or sparking wires only. Press and hold the red button for 2 seconds.")
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(150.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "SOS. Hold for two seconds to raise a danger alert"
                        onClick(label = "Raise danger alert") { trigger(); true }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(onPress = {
                            val job = scope.launch {
                                val steps = 20
                                for (i in 1..steps) {
                                    delay(100)
                                    progress = i / steps.toFloat()
                                }
                                trigger()
                            }
                            tryAwaitRelease()
                            if (triggeredRef == null) job.cancel()
                            progress = 0f
                        })
                    }
            ) {
                CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(150.dp), strokeWidth = 8.dp,
                    color = MaterialTheme.colorScheme.onErrorContainer)
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.error, modifier = Modifier.size(126.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onError, modifier = Modifier.size(36.dp))
                            Text(if (triggeredRef == null) "HOLD SOS" else "SENT", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
        val ref = triggeredRef
        if (ref == null) {
            InfoNote("Stay at least 10 metres away from any fallen line. Never touch it or anything it touches.", isWarning = true)
        } else {
            InfoNote("Saved ($ref). Now please call so they can switch the line off.", isWarning = true)
            InviteNeighboursPrompt(userProfile, ref)
            ActionButton("Call 112 (emergency)", Icons.Default.Call, { SolutionIntents.dial(context, SolutionIntents.EMERGENCY_NUMBER) }, danger = true)
            DiscoContactButtons(userProfile, "EMERGENCY hazard $ref", sosText)
            ActionButton(if (photoPath == null) "Add a photo for the control room" else "Retake photo", Icons.Default.AddAPhoto, takePhoto, outlined = true)
            photoPath?.let { path ->
                PhotoThumb(path, "Hazard photo", Modifier.fillMaxWidth().heightIn(max = 180.dp))
                ActionButton("Share photo", Icons.Default.Share, {
                    SolutionIntents.shareFile(context, java.io.File(path), "image/jpeg", "Hazard $ref")
                }, outlined = true)
            }
            ActionButton("Open full hazard form", null, onOpenHazardForm, outlined = true)
        }
    }
}

// SOLUTION 5: DIAGNOSTIC STATUS TRACKER
@Composable
fun DiagnosticStatusTrackerFeature(userProfile: UserProfile) {
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val source = remember { DemoOutageStatusSource() }
    val events by dao.supplyEvents().collectAsState(initial = emptyList())
    val reports by dao.reports().collectAsState(initial = emptyList())
    var result by remember { mutableStateOf<OutageDiagnosis?>(null) }
    val lightOn = events.lastOrNull()?.isOn

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(5, "Diagnostic Status Tracker", "Is it load shedding or a fault? Check, and see when supply is expected back.")
        Row { DemoDataBadge("Demo schedule") }
        StatRow("Your supply log says", when (lightOn) { true -> "Light ON"; false -> "Light OFF"; null -> "Not logged yet" })
        ActionButton("Check why my light is out", Icons.Default.ManageSearch, {
            scope.launch {
                result = source.diagnose(userProfile.feederName, lightOn, reports.any { it.stage < 4 })
            }
        })
        result?.let { r ->
            SectionCard {
                Text(r.kind, fontWeight = FontWeight.Black, fontSize = 16.sp,
                    color = if (r.kind == "Unplanned fault") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                Text(r.explanation, fontSize = 16.sp)
                r.expectedBackAt?.let { StatRow("Expected back", Fmt.time(it)) }
                if (r.isDemo) Text("Rules-based demo until your DisCo publishes its feeder schedule.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DiscoContactButtons(userProfile, "Outage status query",
                "Please confirm whether my outage is load shedding or a fault, and when supply returns.\n${customerBlock(userProfile)}")
        }
    }
}
