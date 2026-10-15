package com.example.ui.meter

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AppDatabase
import com.example.data.meter.CompensationEntry
import com.example.data.meter.CompensationSla
import com.example.data.meter.CompensationStatus
import com.example.data.meter.CompensationTokens
import com.example.data.meter.StsToken
import com.example.ui.solutions.common.SolutionIntents
import kotlinx.coroutines.flow.map

/** Explanation line shown under every "Compensation tokens" heading. */
@Composable
fun CompensationTokensExplanation() {
    Text(
        CompensationSla.EXPLANATION,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * "Compensation tokens": the user's complaint tickets that ran past the NERC repair limit, with
 * what each one owes and where the claim stands. The only token entry in Bright is the 20-digit
 * compensation token the DisCo issued for one of these tickets.
 */
@Composable
fun CompensationTokensSection(
    meterNumber: String,
    customerName: String,
    discoCode: String,
    modifier: Modifier = Modifier,
    showHeading: Boolean = true
) {
    val context = LocalContext.current
    val complaintsFlow = remember(meterNumber) {
        AppDatabase.getDatabase(context).complaintDao()
            .getComplaintsForMeter(meterNumber)
            .map { list -> list.map { it.toDomain() } }
    }
    val complaints by complaintsFlow.collectAsState(initial = emptyList())
    var revision by remember { mutableIntStateOf(0) }
    val entries = remember(complaints, revision) { CompensationTokens.entries(context, complaints) }

    Column(modifier = modifier.testTag("compensation_tokens_section"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (showHeading) {
            Text(
                "Compensation tokens",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        CompensationTokensExplanation()
        if (entries.isEmpty()) {
            Text(
                "No overdue repairs right now. When one of your phase-fault or transformer complaints runs past its limit, it shows here.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            entries.forEach { e ->
                CompensationEntryCard(
                    entry = e,
                    meterNumber = meterNumber,
                    customerName = customerName,
                    discoCode = discoCode,
                    onChanged = { revision++ }
                )
            }
        }
    }
}

@Composable
private fun CompensationEntryCard(
    entry: CompensationEntry,
    meterNumber: String,
    customerName: String,
    discoCode: String,
    onChanged: () -> Unit
) {
    val context = LocalContext.current
    var showEntry by remember(entry.complaintId) { mutableStateOf(false) }
    var token by remember(entry.complaintId) { mutableStateOf("") }
    var kwh by remember(entry.complaintId) { mutableStateOf("") }
    var error by remember(entry.complaintId) { mutableStateOf<String?>(null) }
    var showSteps by remember(entry.complaintId) { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), shape)
            .padding(14.dp)
            .testTag("compensation_entry_${entry.complaintId}"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Fault ref ${entry.complaintId}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            CompensationStatusChip(entry.status)
        }
        Text(entry.faultTitle, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        CompRow("Type", CompensationSla.slaLabel(entry.faultType))
        CompRow("Hours late", "${entry.hoursLate}h" + if (entry.stillOpen) " (still open)" else "")
        CompRow("kWh owed", entry.kwhOwedText)

        when (entry.status) {
            CompensationStatus.PENDING -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        val text = claimText(entry, meterNumber, customerName, discoCode)
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Compensation claim: fault ref ${entry.complaintId}")
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(send, "Send compensation claim"))
                    }) { Text("Send claim", fontSize = 16.sp) }
                    OutlinedButton(onClick = {
                        CompensationTokens.markClaimed(context, entry.complaintId)
                        onChanged()
                    }) { Text("Mark claimed", fontSize = 16.sp) }
                }
            }
            CompensationStatus.CLAIMED -> {
                if (!showEntry) {
                    OutlinedButton(onClick = { showEntry = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Enter the token your DisCo issued", fontSize = 16.sp)
                    }
                }
            }
            CompensationStatus.ISSUED -> {
                entry.token?.let { t ->
                    Text(
                        StsToken.pretty(t),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { SolutionIntents.copy(context, "Compensation token", StsToken.pretty(t)) }) {
                            Text("Copy", fontSize = 16.sp)
                        }
                        OutlinedButton(onClick = { showSteps = !showSteps }) {
                            Text(if (showSteps) "Hide keypad steps" else "Keypad steps", fontSize = 16.sp)
                        }
                    }
                    if (showSteps) {
                        StsToken.keypadSteps(t, null).forEachIndexed { i, s ->
                            Text("${i + 1}. $s", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        if (showEntry && entry.status != CompensationStatus.ISSUED) {
            Text(
                "Only the 20-digit compensation token your DisCo issued for this fault.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = StsToken.pretty(token),
                onValueChange = { token = StsToken.digits(it).take(20); error = null },
                label = { Text("Compensation token (20 digits)", fontSize = 16.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("compensation_token_input")
            )
            OutlinedTextField(
                value = kwh,
                onValueChange = { kwh = it.filter { c -> c.isDigit() || c == '.' }.take(8) },
                label = { Text("kWh on the token (optional)", fontSize = 16.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            if (token.isNotEmpty() && token.length < 20) {
                Text("${token.length} of 20 digits.", fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
            }
            error?.let { Text(it, fontSize = 16.sp, color = MaterialTheme.colorScheme.error) }
            Button(
                enabled = token.length == 20,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    error = CompensationTokens.recordIssuedToken(context, entry.complaintId, token, kwh.toDoubleOrNull())
                    if (error == null) {
                        showEntry = false
                        onChanged()
                    }
                }
            ) { Text("Save compensation token", fontSize = 16.sp) }
        }
    }
}

@Composable
private fun CompRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun CompensationStatusChip(status: CompensationStatus) {
    val c = when (status) {
        CompensationStatus.ISSUED -> MaterialTheme.colorScheme.primary
        CompensationStatus.CLAIMED -> MaterialTheme.colorScheme.tertiary
        CompensationStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(c.copy(alpha = 0.15f))
            .border(1.dp, c, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(status.label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = c)
    }
}

private fun claimText(e: CompensationEntry, meterNumber: String, customerName: String, discoCode: String): String = """
    Compensation claim under the NERC repair service level

    To: $discoCode customer care
    From: ${customerName.ifBlank { "Customer" }}, meter $meterNumber

    Fault ref: ${e.complaintId} (${e.faultTitle})
    Type: ${CompensationSla.slaLabel(e.faultType)}
    Repair time so far: ${e.hoursTaken} hours, ${e.hoursLate} hours past the ${e.slaHours}-hour limit${if (e.stillOpen) " (still not fixed)" else ""}

    Please issue the compensation token owed for this fault to meter $meterNumber.
""".trimIndent()
