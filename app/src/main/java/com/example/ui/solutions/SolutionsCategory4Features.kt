package com.example.ui.solutions

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.solutions.FaultReportEntity
import com.example.data.solutions.ForumPostEntity
import com.example.data.solutions.Refs
import com.example.data.solutions.VoiceNoteEntity
import com.example.model.UserProfile
import com.example.ui.solutions.common.ActionButton
import com.example.ui.solutions.common.ChoiceRow
import com.example.ui.solutions.common.DemoDataBadge
import com.example.ui.solutions.common.FeatureHeader
import com.example.ui.solutions.common.Fmt
import com.example.ui.solutions.common.HoldToRecordButton
import com.example.ui.solutions.common.InfoNote
import com.example.ui.solutions.common.LocationPinner
import com.example.ui.solutions.common.PhotoThumb
import com.example.ui.solutions.common.PinnedLocation
import com.example.ui.solutions.common.SectionCard
import com.example.ui.solutions.common.SolutionIntents
import com.example.ui.solutions.common.StatRow
import com.example.ui.solutions.common.TextInput
import com.example.ui.solutions.common.VoicePlayer
import com.example.ui.solutions.common.rememberSolutionsDao
import com.example.ui.solutions.common.rememberSolutionsPrefs
import com.example.ui.solutions.common.rememberTakePhotoAction
import kotlinx.coroutines.launch
import java.io.File

// SOLUTION 16: VISUAL PROOF OVERRIDE
@Composable
fun VisualProofOverrideFeature(userProfile: UserProfile) {
    val context = LocalContext.current
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val reports by dao.reports().collectAsState(initial = emptyList())
    var kind by remember { mutableStateOf("Blown fuse") }
    var photo by remember { mutableStateOf<String?>(null) }
    var takenAt by remember { mutableStateOf(0L) }
    var pin by remember { mutableStateOf<PinnedLocation?>(null) }
    var saved by remember { mutableStateOf<String?>(null) }
    val take = rememberTakePhotoAction { f -> if (f != null) { photo = f.absolutePath; takenAt = System.currentTimeMillis() } }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(16, "Visual Proof Override", "One clear photo is enough to report a fault - no need to wait for neighbours.")
        ChoiceRow(listOf("Blown fuse", "Snapped cable", "Burnt pillar", "Leaning pole", "Burnt transformer", "Other"), kind) { kind = it }
        ActionButton(if (photo == null) "Take proof photo" else "Retake photo", Icons.Default.AddAPhoto, take, outlined = photo != null)
        photo?.let {
            PhotoThumb(it, "Proof photo of $kind", Modifier.fillMaxWidth().height(180.dp))
            Text("Taken ${Fmt.dateTime(takenAt)}", fontSize = 14.sp)
        }
        LocationPinner(pin) { pin = it }
        val ready = photo != null && pin != null
        ActionButton("File photo-verified report", Icons.Default.VerifiedUser, {
            val ref = Refs.make("PV")
            scope.launch {
                dao.insertReport(FaultReportEntity(
                    reference = ref, source = "PHOTO", tier = 2, title = "Photo proof: $kind",
                    details = "Photo ${Fmt.dateTime(takenAt)}; location ${pin?.describe()}",
                    transformerId = userProfile.transformerId, latitude = pin?.latitude, longitude = pin?.longitude,
                    photoPath = photo
                ))
                saved = ref
            }
        }, enabled = ready)
        if (!ready) Text("Take a photo and add the place first.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        saved?.let { ref ->
            InfoNote("Report sent ($ref).")
            InviteNeighboursPrompt(userProfile, ref)
            ActionButton("Send photo to DisCo", Icons.Default.Share, {
                photo?.let { p -> SolutionIntents.shareFile(context, File(p), "image/jpeg", "Fault $ref: $kind at ${pin?.mapsLink() ?: pin?.label}") }
            }, outlined = true)
        }
        val past = reports.filter { it.source == "PHOTO" }
        if (past.isNotEmpty()) StatRow("Photo-verified reports filed", "${past.size}")
    }
}

// SOLUTION 17: WAKE UP THE STREET
@Composable
fun WakeUpStreetAlertsFeature(userProfile: UserProfile) {
    val context = LocalContext.current
    val prefs = rememberSolutionsPrefs()
    val dao = rememberSolutionsDao()
    val reports by dao.reports().collectAsState(initial = emptyList())
    var sent by remember { mutableIntStateOf(prefs.invitesSent) }
    val open = reports.firstOrNull { it.stage < 4 }
    val invite = "Light is out on our transformer (${userProfile.transformerId}, ${userProfile.streetAddress}). " +
        (open?.let { "I've reported it (ref ${it.reference}). " } ?: "") +
        "Please report it too so the DisCo sends a crew faster - get the Bright app and co-sign."

    fun count() { sent += 1; prefs.invitesSent = sent }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(17, "'Wake Up the Street' Alerts", "Invite neighbours on your transformer to co-sign the report - by SMS from your own phone or any chat app.")
        SectionCard { Text(invite, fontSize = 16.sp) }
        ActionButton("Send SMS invite", Icons.Default.Sms, {
            if (SolutionIntents.sms(context, emptyList(), invite)) count()
        })
        ActionButton("Share to WhatsApp / estate group", Icons.Default.Share, {
            if (SolutionIntents.shareText(context, "Light is out - please co-sign", invite)) count()
        }, outlined = true)
        StatRow("Invites you've sent", "$sent")
        Text("SMS is sent from your phone at your network's normal rate; Bright doesn't send messages for you.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** #17 in action: after a report, offers to invite neighbours (only when that setting is on). */
@Composable
fun InviteNeighboursPrompt(userProfile: UserProfile, reference: String) {
    val context = LocalContext.current
    val prefs = rememberSolutionsPrefs()
    if (!prefs.isEnabled(17)) return
    val text = "Light is out on our transformer (${userProfile.transformerId}). I've reported it (ref $reference). Please report it too so the repair team comes faster."
    ActionButton("Ask neighbours to report too", Icons.Default.Share, {
        if (SolutionIntents.shareText(context, "Please report the outage too", text)) prefs.invitesSent = prefs.invitesSent + 1
    }, outlined = true)
}

// SOLUTION 18: USER TRUST SCORE
object TrustScore {
    const val START = 50
    fun of(reports: List<FaultReportEntity>): Int =
        (START + reports.count { it.verified == true } * 5 - reports.count { it.verified == false } * 15).coerceIn(0, 100)
}

@Composable
fun UserTrustScoreFeature() {
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val reports by dao.reports().collectAsState(initial = emptyList())
    val score = TrustScore.of(reports)
    val pending = reports.filter { it.verified == null }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(18, "User Trust Score", "Starts at 50. Each report confirmed fixed (in #23) adds 5; a false alarm takes away 15.")
        SectionCard {
            Text("$score / 100", fontWeight = FontWeight.Black, fontSize = 28.sp,
                color = if (score >= 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            LinearProgressIndicator(progress = { score / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp))
            StatRow("Verified reports", "${reports.count { it.verified == true }}")
            StatRow("False alarms", "${reports.count { it.verified == false }}")
            StatRow("Awaiting verification", "${pending.size}")
        }
        if (pending.isNotEmpty()) {
            Text("Reported by mistake? Withdraw it honestly:", fontSize = 16.sp)
            pending.take(5).forEach { r ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${r.reference} - ${r.title}", fontSize = 16.sp, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { scope.launch { dao.updateReport(r.copy(verified = false, stage = 5)) } },
                        modifier = Modifier.heightIn(min = 48.dp)) { Text("False alarm") }
                }
            }
        }
    }
}

// SOLUTION 19: NEIGHBOURHOOD GRID FORUM
@Composable
fun NeighborhoodGridForumFeature(userProfile: UserProfile, onOpenFullForum: () -> Unit) {
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val zone = userProfile.transformerId
    val posts by dao.forumPosts(zone).collectAsState(initial = emptyList())
    var text by remember { mutableStateOf("") }
    var alert by remember { mutableStateOf(false) }

    LaunchedEffect(zone) {
        if (dao.forumPostCount(zone) == 0) {
            val now = System.currentTimeMillis()
            dao.insertForumPost(ForumPostEntity(transformerId = zone, author = "Neighbour (demo)", body = "Transformer was humming loudly last night. Anyone else?", isMine = false, isDemo = true, createdAt = now - 5 * 3_600_000L))
            dao.insertForumPost(ForumPostEntity(transformerId = zone, author = "Estate chairman (demo)", body = "Reminder: never pay a technician cash for 'fuel' or cables. Report it in #21.", isExtortionAlert = true, isMine = false, isDemo = true, createdAt = now - 26 * 3_600_000L))
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(19, "Neighbourhood Grid Forum", "A board for everyone on transformer $zone: local faults, who the electricity rep is, and anti-extortion alerts.")
        InfoNote("Posts are saved on this phone until the shared forum server is live. Example posts are marked demo.")
        TextInput("Write to your transformer zone", text, { text = it.take(500) }, singleLine = false)
        CheckRow("Mark as anti-extortion alert", alert) { alert = it }
        ActionButton("Post", Icons.Default.Send, {
            val body = text.trim()
            scope.launch {
                dao.insertForumPost(ForumPostEntity(transformerId = zone, author = "You", body = body, isExtortionAlert = alert))
                text = ""; alert = false
            }
        }, enabled = text.isNotBlank())
        posts.forEach { p ->
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(p.author, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    if (p.isDemo) DemoDataBadge("Demo")
                    if (p.isMine) IconButton(onClick = { scope.launch { dao.deleteForumPost(p) } }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete your post")
                    }
                }
                if (p.isExtortionAlert) Text("ANTI-EXTORTION ALERT", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text(p.body, fontSize = 16.sp)
                Text(Fmt.dateTime(p.createdAt), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        ActionButton("Open community forum", Icons.Default.Forum, onOpenFullForum, outlined = true)
    }
}

// SOLUTION 20: MULTI-LINGUAL VOICE REPORTING
private val LANGS = listOf("Pidgin" to "en-NG", "English" to "en-NG", "Yorùbá" to "yo-NG", "Hausa" to "ha-NG", "Igbo" to "ig-NG")

@Composable
fun MultiLingualVoiceReportingFeature(userProfile: UserProfile) {
    val context = LocalContext.current
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val notes by dao.voiceNotes().collectAsState(initial = emptyList())
    var lang by remember { mutableStateOf("Pidgin") }
    val player = remember { VoicePlayer() }
    var playing by remember { mutableStateOf<String?>(null) }
    var dictated by remember { mutableStateOf("") }
    DisposableEffect(Unit) { onDispose { player.stop() } }

    val speech = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            dictated = res.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(20, "Multi-Lingual Voice Reporting", "Say it in your own language - 'Light don quench for our street'. Hold the mic, release to save, then send the voice note to your DisCo.")
        ChoiceRow(LANGS.map { it.first }, lang) { lang = it }
        HoldToRecordButton(onRecorded = { file, ms ->
            scope.launch { dao.insertVoiceNote(VoiceNoteEntity(filePath = file.absolutePath, durationMs = ms, language = lang)) }
        })
        notes.forEach { n ->
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (playing == n.filePath) { player.stop(); playing = null }
                        else { playing = n.filePath; player.play(n.filePath) { playing = null } }
                    }) {
                        Icon(if (playing == n.filePath) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (playing == n.filePath) "Stop voice note" else "Play voice note")
                    }
                    Column(Modifier.weight(1f)) {
                        Text("${n.language} - ${Fmt.duration(n.durationMs)}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(Fmt.dateTime(n.createdAt), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = {
                        SolutionIntents.shareFile(context, File(n.filePath), "audio/mp4", "Voice fault report - meter ${userProfile.meterNumber}")
                    }) { Icon(Icons.Default.Share, contentDescription = "Send voice note") }
                    IconButton(onClick = {
                        if (playing == n.filePath) { player.stop(); playing = null }
                        File(n.filePath).delete()
                        scope.launch { dao.deleteVoiceNote(n) }
                    }) { Icon(Icons.Default.Delete, contentDescription = "Delete voice note") }
                }
                OutlinedButton(onClick = {
                    scope.launch {
                        dao.insertReport(FaultReportEntity(reference = Refs.make("VN"), source = "VOICE", tier = 1,
                            title = "Voice report (${n.language})", transformerId = userProfile.transformerId, voicePath = n.filePath))
                        Toast.makeText(context, "Filed as a report - track it in #22", Toast.LENGTH_SHORT).show()
                    }
                }, modifier = Modifier.heightIn(min = 48.dp)) { Text("File as fault report") }
            }
        }
        SectionCard {
            Text("Prefer text? Dictate it", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Uses your phone's speech-to-text. Yorùbá, Hausa and Igbo depend on the languages your phone supports.", fontSize = 14.sp)
            ActionButton("Speak in $lang", Icons.Default.RecordVoiceOver, {
                val tag = LANGS.first { it.first == lang }.second
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                    .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    .putExtra(RecognizerIntent.EXTRA_LANGUAGE, tag)
                    .putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe the fault")
                try { speech.launch(intent) } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "Speech-to-text isn't available on this phone", Toast.LENGTH_LONG).show()
                }
            }, outlined = true)
            if (dictated.isNotBlank()) {
                TextInput("What you said (edit if needed)", dictated, { dictated = it }, singleLine = false)
                ActionButton("File text report", Icons.Default.Send, {
                    val body = dictated
                    scope.launch {
                        dao.insertReport(FaultReportEntity(reference = Refs.make("TX"), source = "VOICE", tier = 1, title = "Dictated report ($lang)", details = body, transformerId = userProfile.transformerId))
                        dictated = ""
                        Toast.makeText(context, "Report filed", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}
