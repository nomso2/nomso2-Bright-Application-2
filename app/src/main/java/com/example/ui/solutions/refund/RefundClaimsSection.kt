package com.example.ui.solutions.refund

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.data.solutions.RefundClaimEntity
import com.example.data.solutions.Refs
import com.example.model.UserProfile
import com.example.ui.solutions.common.ActionButton
import com.example.ui.solutions.common.DiscoContactButtons
import com.example.ui.solutions.common.EvidencePdf
import com.example.ui.solutions.common.Fmt
import com.example.ui.solutions.common.InfoNote
import com.example.ui.solutions.common.SectionCard
import com.example.ui.solutions.common.SolutionIntents
import com.example.ui.solutions.common.StatRow
import com.example.ui.solutions.common.StatusStepper
import com.example.ui.solutions.common.customerBlock
import com.example.ui.solutions.common.rememberSolutionsDao
import kotlinx.coroutines.launch

private val CLAIM_STEPS = listOf("Filed with DisCo", "Acknowledged", "Credited")
private fun stepOf(status: String) = when (status) {
    "ACKNOWLEDGED" -> 1
    "CREDITED" -> 2
    else -> 0
}

internal fun claimText(profile: UserProfile, s: RefundSummary, ref: String): String = buildString {
    appendLine("Compensation claim $ref - supply below ${profile.feederBand.code} service level")
    appendLine()
    appendLine(customerBlock(profile))
    appendLine()
    appendLine("Period: ${Fmt.date(s.periodStart)} to ${Fmt.date(s.periodEnd)} (${s.daysCounted} days logged)")
    appendLine("Promised: ${s.promisedHours} h/day (${profile.feederBand.code}). Average received: ${Fmt.hours(s.avgHours)}/day.")
    appendLine("Supplied ${Fmt.hours(s.suppliedTotal)} of ${Fmt.hours(s.promisedTotal)} promised; ${s.shortDays} short days; ${s.consecutiveShort} consecutive short days.")
    appendLine("Service actually delivered matches ${s.deliveredBand}.")
    appendLine("Estimated compensation: ${Fmt.naira(s.owedNaira)} (my estimate).")
    appendLine()
    appendLine("Under NERC Order NERC/334/2022 and Addendum NERC/2024/003, I request compensation for the gap between the tariff I paid and the tariff for the service delivered (as kWh token credit for prepaid), and a review of my feeder's band classification.")
    appendLine()
    appendLine("Daily log:")
    s.dailyLines.takeLast(31).forEach { appendLine(it) }
}

internal fun nercEscalationText(profile: UserProfile, s: RefundSummary, claim: RefundClaimEntity): String = buildString {
    appendLine("To: NERC Consumer Forum / NERC Consumer Affairs")
    appendLine()
    appendLine("I filed compensation claim ${claim.reference} with ${claim.discoCode} on ${Fmt.date(claim.createdAt)} by ${claim.channel.lowercase()} and it has not been resolved.")
    appendLine()
    appendLine(customerBlock(profile))
    appendLine()
    appendLine("Claim period: ${Fmt.date(claim.periodStart)} to ${Fmt.date(claim.periodEnd)}")
    appendLine("Promised ${Fmt.hours(claim.promisedHours)} in total, supplied ${Fmt.hours(claim.suppliedHours)}.")
    appendLine("Estimated compensation: ${Fmt.naira(claim.estimatedOwedNaira)}.")
    appendLine()
    appendLine("I ask the Forum to direct ${claim.discoCode} to compensate me under NERC Order NERC/334/2022 (Addendum NERC/2024/003) and to review the feeder's band. My daily supply log is attached/below.")
    appendLine()
    s.dailyLines.takeLast(31).forEach { appendLine(it) }
}

@Composable
fun RefundClaimsSection(profile: UserProfile, summary: RefundSummary) {
    val context = LocalContext.current
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val claims by dao.refundClaims().collectAsState(initial = emptyList())
    val draftRef = "RF-" + Fmt.date(summary.periodEnd).replace(" ", "")
    val disco = SolutionIntents.discoFor(profile)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard {
            Text("Claim from your DisCo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (!summary.isShort) {
                Text(
                    if (summary.daysCounted == 0) "Log at least one full day first." else "No shortfall so far - nothing to claim yet.",
                    fontSize = 16.sp
                )
            } else {
                Text("One tap sends a claim with your meter, feeder, dates and daily log.", fontSize = 16.sp)
                DiscoContactButtons(
                    profile,
                    subject = "Compensation claim $draftRef - meter ${profile.meterNumber}",
                    message = claimText(profile, summary, draftRef),
                    onUsed = { channel ->
                        scope.launch {
                            dao.insertRefundClaim(RefundClaimEntity(
                                reference = Refs.make("RF"),
                                discoCode = disco?.code ?: profile.discoCode,
                                periodStart = summary.periodStart,
                                periodEnd = summary.periodEnd,
                                promisedHours = summary.promisedTotal,
                                suppliedHours = summary.suppliedTotal,
                                estimatedOwedNaira = summary.owedNaira,
                                channel = channel.name
                            ))
                        }
                    }
                )
            }
            ActionButton("Save supply log as PDF", Icons.Default.PictureAsPdf, {
                val file = EvidencePdf.write(
                    context, "bright_supply_log.pdf", "Bright supply log - meter ${profile.meterNumber}",
                    claimText(profile, summary, draftRef).lines()
                )
                if (file != null) SolutionIntents.shareFile(context, file, "application/pdf", "Supply log")
                else Toast.makeText(context, "Couldn't create the PDF", Toast.LENGTH_SHORT).show()
            }, outlined = true)
        }

        if (claims.isNotEmpty()) {
            Text("Your claims", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            claims.forEach { claim ->
                SectionCard {
                    StatRow(claim.reference, claim.discoCode)
                    StatRow("Filed", "${Fmt.date(claim.createdAt)} by ${claim.channel.lowercase()}")
                    StatRow("Estimated owed", Fmt.naira(claim.estimatedOwedNaira))
                    if (claim.status == "ESCALATED") {
                        InfoNote("Escalated to NERC on ${Fmt.date(claim.updatedAt)}.", isWarning = true)
                    } else {
                        StatusStepper(CLAIM_STEPS, stepOf(claim.status))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (claim.status == "FILED") {
                            OutlinedButton(onClick = {
                                scope.launch { dao.updateRefundClaim(claim.copy(status = "ACKNOWLEDGED", updatedAt = System.currentTimeMillis())) }
                            }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Mark acknowledged") }
                        }
                        if (claim.status == "FILED" || claim.status == "ACKNOWLEDGED") {
                            OutlinedButton(onClick = {
                                scope.launch { dao.updateRefundClaim(claim.copy(status = "CREDITED", updatedAt = System.currentTimeMillis())) }
                            }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Mark credited") }
                        }
                    }
                    if (claim.status != "CREDITED") {
                        val ageDays = (System.currentTimeMillis() - claim.createdAt) / 86_400_000L
                        Text(
                            if (ageDays < 15) "Give the DisCo's complaints unit time to respond (filed $ageDays day(s) ago). If it doesn't resolve it, escalate to the NERC Forum."
                            else "Filed $ageDays days ago with no credit - escalate to the NERC Forum.",
                            fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ActionButton("Email NERC escalation", Icons.Default.Gavel, {
                            val ok = SolutionIntents.email(
                                context, SolutionIntents.NERC_EMAIL,
                                "Unresolved compensation claim ${claim.reference} - ${claim.discoCode} - meter ${profile.meterNumber}",
                                nercEscalationText(profile, summary, claim)
                            )
                            if (ok) scope.launch {
                                dao.updateRefundClaim(claim.copy(status = "ESCALATED", updatedAt = System.currentTimeMillis()))
                            }
                        }, outlined = true)
                        ActionButton("Find your NERC Forum office", Icons.Default.Public, {
                            SolutionIntents.openUrl(context, SolutionIntents.NERC_FORUM_OFFICES_URL)
                        }, outlined = true)
                    }
                }
            }
        }
    }
}
