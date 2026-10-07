package com.example.ui.solutions.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DisCoContact
import com.example.model.DisCoContacts
import com.example.model.UserProfile

/** Which channel the user picked, so claims can record it. */
enum class ContactChannel { CALL, WHATSAPP, EMAIL, SMS }

/**
 * Call / WhatsApp / Email / SMS buttons for the user's own DisCo, using the verified
 * numbers in [DisCoContacts]. [onUsed] fires after the hand-off opens.
 */
@Composable
fun DiscoContactButtons(
    profile: UserProfile,
    subject: String,
    message: String,
    onUsed: (ContactChannel) -> Unit = {},
    showCall: Boolean = true
) {
    val context = LocalContext.current
    val disco: DisCoContact? = SolutionIntents.discoFor(profile)
    if (disco == null) {
        InfoNote("Set your state or DisCo in your profile to see its contacts.", isWarning = true)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Send to ${disco.name}", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (showCall && disco.primaryPhone.isNotBlank()) {
            ActionButton("Call ${disco.primaryPhone}", Icons.Default.Call, {
                if (SolutionIntents.dial(context, disco.primaryPhone)) onUsed(ContactChannel.CALL)
            }, outlined = true)
        }
        disco.whatsapp.firstOrNull()?.let { wa ->
            ActionButton("WhatsApp $wa", Icons.Default.Chat, {
                if (SolutionIntents.whatsApp(context, wa, message)) onUsed(ContactChannel.WHATSAPP)
            }, outlined = true)
        }
        if (disco.email.isNotBlank()) {
            ActionButton("Email ${disco.email}", Icons.Default.Email, {
                if (SolutionIntents.email(context, disco.email, subject, message)) onUsed(ContactChannel.EMAIL)
            }, outlined = true)
        }
        val smsTarget = (disco.whatsapp + disco.phones).firstOrNull { it.filter(Char::isDigit).let { d -> d.length == 11 && d.startsWith("0") } }
        if (smsTarget != null) {
            ActionButton("SMS $smsTarget", Icons.Default.Sms, {
                if (SolutionIntents.sms(context, listOf(smsTarget), message)) onUsed(ContactChannel.SMS)
            }, outlined = true)
        }
        Text(DisCoContacts.SOURCE_FOOTER, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Standard header block of every message we draft for a DisCo / NERC. */
fun customerBlock(profile: UserProfile): String = buildString {
    appendLine("Name: ${profile.customerName}")
    appendLine("Meter: ${profile.meterNumber}")
    appendLine("Phone: ${profile.phoneNumber}")
    appendLine("Address: ${profile.streetAddress}, ${profile.lga}, ${profile.state}")
    appendLine("Feeder: ${profile.feederName} (${profile.feederBand.code})")
    append("Transformer: ${profile.transformerId}")
}
