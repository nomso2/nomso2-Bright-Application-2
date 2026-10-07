package com.example.ui.solutions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.solutions.FaultReportEntity
import com.example.data.solutions.InventoryRequestEntity
import com.example.data.solutions.Refs
import com.example.data.solutions.SupplyMath
import com.example.data.solutions.TransformerFaultEntity
import com.example.data.solutions.WhistleblowEntity
import com.example.model.UserProfile
import com.example.ui.solutions.common.ActionButton
import com.example.ui.solutions.common.ChoiceRow
import com.example.ui.solutions.common.DemoDataBadge
import com.example.ui.solutions.common.DiscoContactButtons
import com.example.ui.solutions.common.FeatureHeader
import com.example.ui.solutions.common.Fmt
import com.example.ui.solutions.common.InfoNote
import com.example.ui.solutions.common.NumberField
import com.example.ui.solutions.common.PhotoThumb
import com.example.ui.solutions.common.SectionCard
import com.example.ui.solutions.common.SolutionIntents
import com.example.ui.solutions.common.StatRow
import com.example.ui.solutions.common.StatusStepper
import com.example.ui.solutions.common.SupplyLogger
import com.example.ui.solutions.common.TextInput
import com.example.ui.solutions.common.customerBlock
import com.example.ui.solutions.common.rememberSolutionsDao
import com.example.ui.solutions.common.rememberTakePhotoAction
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

val TICKET_STAGES = listOf("Ticket created", "DisCo acknowledged", "Crew dispatched", "Repair in progress", "Restored - awaiting your check", "Verified closed")

// SOLUTION 21: ANONYMOUS WHISTLEBLOWER TOOL
@Composable
fun AnonymousWhistleblowerFeature(userProfile: UserProfile) {
    val context = LocalContext.current
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val saved by dao.whistleblowReports().collectAsState(initial = emptyList())
    var techId by remember { mutableStateOf("") }
    var demand by remember { mutableStateOf("Cash for fuel") }
    var amount by remember { mutableStateOf("") }
    var where by remember { mutableStateOf(userProfile.lga) }
    var details by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf<String?>(null) }
    var includeContact by remember { mutableStateOf(false) }
    val take = rememberTakePhotoAction { f -> if (f != null) photo = f.absolutePath }

    fun body(r: WhistleblowEntity) = buildString {
        appendLine("Extortion report ${r.reference}")
        appendLine("Demand: ${r.demandType}${r.amountNaira?.let { " - " + Fmt.naira(it) } ?: ""}")
        appendLine("Technician name / ID / vehicle: ${r.technicianId.ifBlank { "unknown" }}")
        appendLine("Where: ${r.location}")
        appendLine("When: ${Fmt.dateTime(r.createdAt)}")
        appendLine("What happened: ${r.description}")
        if (includeContact) { appendLine(); append(customerBlock(userProfile)) } else append("Reporter: anonymous")
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(21, "Anonymous Whistleblower Tool", "Report a technician who demands money for fuel, cables or reconnection. Saved privately on this phone; you choose when to send it to NERC.")
        SectionCard {
            ChoiceRow(listOf("Cash for fuel", "Cash for cables / parts", "Bribe to reconnect", "Bribe for meter", "Other"), demand) { demand = it }
            NumberField("Amount demanded", amount, { amount = it }, suffix = "₦")
            TextInput("Technician name, ID card or vehicle no.", techId, { techId = it })
            TextInput("Where it happened", where, { where = it })
            TextInput("What happened", details, { details = it }, singleLine = false)
            ActionButton(if (photo == null) "Add photo (optional)" else "Retake photo", Icons.Default.AddAPhoto, take, outlined = true)
            photo?.let { PhotoThumb(it, "Whistleblower evidence", Modifier.fillMaxWidth().height(150.dp)) }
            CheckRow("Include my name and meter (off = anonymous)", includeContact) { includeContact = it }
        }
        ActionButton("Save report privately", Icons.Default.Save, {
            scope.launch {
                dao.insertWhistleblow(WhistleblowEntity(
                    reference = Refs.make("WB"), technicianId = techId.trim(), demandType = demand,
                    amountNaira = amount.toDoubleOrNull(), description = details.trim(), location = where.trim(), photoPath = photo
                ))
                techId = ""; amount = ""; details = ""; photo = null
            }
        }, enabled = details.isNotBlank())
        InfoNote("Email shows your email address to NERC. To stay anonymous, send from an email account that doesn't use your name.")
        saved.forEach { r ->
            SectionCard {
                StatRow(r.reference, if (r.status == "SENT") "Sent to NERC" else "Saved")
                Text("${r.demandType} - ${Fmt.dateTime(r.createdAt)}", fontSize = 16.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        if (SolutionIntents.email(context, SolutionIntents.NERC_EMAIL, "Extortion report ${r.reference}", body(r))) {
                            scope.launch { dao.updateWhistleblow(r.copy(status = "SENT")) }
                        }
                    }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Email NERC") }
                    OutlinedButton(onClick = { scope.launch { dao.deleteWhistleblow(r) } }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Delete") }
                }
            }
        }
    }
}

// SOLUTION 22: PIZZA-STYLE DELIVERY TRACKER
@Composable
fun PizzaStyleDeliveryTrackerFeature(userProfile: UserProfile) {
    val context = LocalContext.current
    val prefs = com.example.ui.solutions.common.rememberSolutionsPrefs()
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val reports by dao.reports().collectAsState(initial = emptyList())
    val active = reports.filter { it.stage < 5 }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(22, "Pizza-Style Delivery Tracker", "Every report you file anywhere in Bright shows here with its live stage, from ticket to verified fix.")
        Row { DemoDataBadge("DisCo updates simulated") }
        if (active.isEmpty()) InfoNote("No open reports. File one in #1, #2, #4, #16 or #20.")
        active.forEach { r ->
            SectionCard {
                StatRow(r.reference, "Tier ${r.tier}")
                Text(r.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Filed ${Fmt.dateTime(r.createdAt)}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                StatusStepper(TICKET_STAGES.dropLast(1), r.stage.coerceAtMost(4), mapOf(r.stage.coerceAtMost(4) to "Updated ${Fmt.dateTime(r.stageUpdatedAt)}"))
                if (r.stage < 4) {
                    OutlinedButton(onClick = {
                        scope.launch { dao.updateReport(r.copy(stage = r.stage + 1, stageUpdatedAt = System.currentTimeMillis())) }
                        if (prefs.isEnabled(22) && prefs.stageNotifications) {
                            com.example.ui.solutions.common.SolutionsNotifier.postUpdate(context, 4200 + (r.id % 100).toInt(),
                                "Report ${r.reference}", "Now: ${TICKET_STAGES[(r.stage + 1).coerceAtMost(4)]}")
                        }
                    }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Simulate next DisCo update") }
                } else {
                    Text("Confirm in #23 whether your light is really back.", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }
                if (System.currentTimeMillis() - r.stageUpdatedAt > 24 * SupplyMath.HOUR_MS && r.stage < 4) {
                    InfoNote("No update for over 24 hours - chase it.", isWarning = true)
                    DiscoContactButtons(userProfile, "Update on report ${r.reference}",
                        "Please update me on fault report ${r.reference} (${r.title}), filed ${Fmt.dateTime(r.createdAt)}.\n${customerBlock(userProfile)}", showCall = false)
                }
            }
        }
        Text("When the DisCo ticket API is connected, these stages update automatically.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// SOLUTION 23: CONSUMER CLOSURE VERIFICATION
@Composable
fun ConsumerClosureVerificationFeature(userProfile: UserProfile) {
    val context = LocalContext.current
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val reports by dao.reports().collectAsState(initial = emptyList())
    val awaiting = reports.filter { it.stage == 4 }
    val closed = reports.filter { it.stage == 5 && it.verified == true }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(23, "Consumer Closure Verification", "A fix doesn't count until you say so. When the DisCo marks your fault restored, confirm it here.")
        if (awaiting.isEmpty()) InfoNote("Nothing waiting for your check. Reports reach this step when marked 'Restored' (see #22).")
        awaiting.forEach { r ->
            SectionCard {
                Text("${r.reference}: ${r.title}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("DisCo says this is fixed. Is your light back?", fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Neighbour checks: 2 of 3 said yes", fontSize = 14.sp, modifier = Modifier.weight(1f))
                    DemoDataBadge()
                }
                ActionButton("Yes, my light is back", Icons.Default.CheckCircle, {
                    scope.launch {
                        dao.updateReport(r.copy(stage = 5, verified = true, stageUpdatedAt = System.currentTimeMillis()))
                        SupplyLogger.log(context, true, "APP")
                    }
                })
                ActionButton("No, still dark - reopen", Icons.Default.Report, {
                    scope.launch { dao.updateReport(r.copy(stage = 3, stageUpdatedAt = System.currentTimeMillis())) }
                    SolutionIntents.shareText(context, "Fault ${r.reference} not fixed",
                        "Fault ${r.reference} was marked fixed but my light is still off. Please send the crew back.\n${customerBlock(userProfile)}")
                }, danger = true)
            }
        }
        StatRow("Fixes you've verified", "${closed.size}")
    }
}

// SOLUTION 24: FAULT HISTORY LOG
private data class HistoryItem(val at: Long, val text: String)

@Composable
fun FaultHistoryLogFeature(userProfile: UserProfile) {
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val reports by dao.reports().collectAsState(initial = emptyList())
    val manual by dao.transformerFaults().collectAsState(initial = emptyList())
    var desc by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    val fmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.UK).apply { isLenient = false } }
    val parsed = runCatching { fmt.parse(date)?.time }.getOrNull()?.takeIf { it <= System.currentTimeMillis() }
    val tr = userProfile.transformerId

    val items = (reports.filter { it.transformerId == tr && it.tier >= 2 }.map { HistoryItem(it.createdAt, "${it.title} (${it.reference})") } +
        manual.filter { it.transformerId == tr }.map { HistoryItem(it.occurredOn, it.description) }).sortedByDescending { it.at }
    val lastMonth = items.count { System.currentTimeMillis() - it.at <= 30 * SupplyMath.DAY_MS }
    val recurrent = lastMonth > 2

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(24, "Fault History Log", "Every street-level fault on transformer $tr. More than two in 30 days flags a Recurrent Infrastructure Failure.")
        SectionCard {
            StatRow("Faults in last 30 days", "$lastMonth", if (recurrent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            StatRow("All recorded", "${items.size}")
        }
        if (recurrent) {
            InfoNote("Recurrent Infrastructure Failure: $lastMonth faults in 30 days. Ask for a full overhaul, not another patch.", isWarning = true)
            DiscoContactButtons(userProfile, "Recurrent failure - transformer $tr",
                "Transformer $tr has failed $lastMonth times in 30 days:\n" + items.take(10).joinToString("\n") { "- ${Fmt.date(it.at)}: ${it.text}" } +
                    "\nPlease schedule a full engineering overhaul with standard parts.\n${customerBlock(userProfile)}", showCall = false)
        }
        SectionCard {
            Text("Add a past fault", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            TextInput("What failed", desc, { desc = it })
            TextInput("Date (DD/MM/YYYY)", date, { date = it.take(10) }, keyboardType = KeyboardType.Number)
            ActionButton("Add to history", Icons.Default.Save, {
                scope.launch {
                    dao.insertTransformerFault(TransformerFaultEntity(transformerId = tr, description = desc.trim(), occurredOn = parsed!!))
                    desc = ""; date = ""
                }
            }, enabled = desc.isNotBlank() && parsed != null)
        }
        items.take(20).forEach { StatRow(Fmt.date(it.at), it.text) }
    }
}

// SOLUTION 25: INVENTORY REQUEST MONITOR
@Composable
fun InventoryRequestMonitorFeature(userProfile: UserProfile) {
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val requests by dao.inventoryRequests().collectAsState(initial = emptyList())
    val reports by dao.reports().collectAsState(initial = emptyList())
    var ticket by remember { mutableStateOf("") }
    var part by remember { mutableStateOf("Fuse") }
    var who by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("3") }
    LaunchedEffect(reports) {
        if (ticket.isBlank()) reports.firstOrNull { it.stage < 5 }?.let { ticket = it.reference }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(25, "Inventory Request Monitor", "When the DisCo says a part is 'not in stock', record exactly what and by when. Bright shows if the delay is real or stalling.")
        SectionCard {
            TextInput("Ticket / report reference", ticket, { ticket = it })
            ChoiceRow(listOf("Fuse", "Cable", "Transformer oil", "Insulator", "Cut-out", "Other"), part) { part = it }
            TextInput("Who said it (name / role)", who, { who = it })
            NumberField("They promised it within", days, { days = it }, suffix = "days")
            ActionButton("Start monitoring", Icons.Default.Save, {
                val now = System.currentTimeMillis()
                val d = days.toDoubleOrNull() ?: 0.0
                scope.launch {
                    dao.insertInventoryRequest(InventoryRequestEntity(ticketReference = ticket.trim(), partName = part, statedBy = who.trim(),
                        statedOn = now, promisedBy = now + (d * SupplyMath.DAY_MS).toLong()))
                    who = ""
                }
            }, enabled = ticket.isNotBlank())
        }
        requests.forEach { r ->
            val now = System.currentTimeMillis()
            val overdueDays = ((now - r.promisedBy) / SupplyMath.DAY_MS).toInt()
            SectionCard {
                StatRow("${r.partName} for ${r.ticketReference}", when {
                    r.deliveredOn != null -> "Delivered"
                    overdueDays > 0 -> "$overdueDays days late"
                    else -> "Due ${Fmt.date(r.promisedBy)}"
                }, if (r.deliveredOn == null && overdueDays > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                Text("Stated ${Fmt.date(r.statedOn)}" + if (r.statedBy.isNotBlank()) " by ${r.statedBy}" else "", fontSize = 14.sp)
                if (r.deliveredOn == null && overdueDays >= 3) InfoNote("Late by $overdueDays days - this may be stalling. Ask for the requisition number.", isWarning = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (r.deliveredOn == null) OutlinedButton(onClick = {
                        scope.launch { dao.updateInventoryRequest(r.copy(deliveredOn = System.currentTimeMillis())) }
                    }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Part arrived") }
                    OutlinedButton(onClick = { scope.launch { dao.deleteInventoryRequest(r) } }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Remove") }
                }
                if (r.deliveredOn == null && overdueDays > 0) {
                    DiscoContactButtons(userProfile, "Missing part for ${r.ticketReference}",
                        "On ${Fmt.date(r.statedOn)} we were told the ${r.partName.lowercase()} for fault ${r.ticketReference} would arrive by ${Fmt.date(r.promisedBy)}. It is $overdueDays days late. Please give the requisition number and a firm date.\n${customerBlock(userProfile)}",
                        showCall = false)
                }
            }
        }
    }
}
