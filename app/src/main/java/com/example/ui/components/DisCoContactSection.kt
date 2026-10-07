package com.example.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DisCoContact
import com.example.model.DisCoContacts
import com.example.model.FaultType
import com.example.model.PhoneNumbers
import com.example.model.StateDisCo
import com.example.model.StateDisCoMap
import com.example.model.UserProfile
import com.example.ui.theme.extendedColors

/**
 * "Contact your DisCo" block for the Report flow: pick a state (defaults to the user's
 * profile state, then their DisCo's first state), see every distributor serving it and
 * reach them by Call (ACTION_DIAL, no CALL_PHONE permission), WhatsApp (wa.me), Email
 * (ACTION_SENDTO with a prefilled subject) or copy the number.
 */
@Composable
fun DisCoContactSection(
    userProfile: UserProfile,
    faultType: FaultType,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val defaultState = remember(userProfile.state, userProfile.discoCode) {
        StateDisCoMap.forState(userProfile.state)
            ?: StateDisCoMap.firstStateServedBy(userProfile.discoCode)
    }
    var selectedState by remember(defaultState) { mutableStateOf(defaultState) }
    var query by remember { mutableStateOf("") }
    var pickerOpen by remember { mutableStateOf(false) }

    val emailSubject = buildString {
        append("Fault report: ")
        append(faultType.displayName)
        if (userProfile.meterNumber.isNotBlank()) {
            append(" | Meter ")
            append(userProfile.meterNumber)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("disco_contact_section"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Contact your DisCo",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Call, WhatsApp or email the distributor that serves your state.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // State picker: a search field that filters an inline list of all 36 states + FCT.
            OutlinedTextField(
                value = if (pickerOpen) query else selectedState?.state.orEmpty(),
                onValueChange = {
                    query = it
                    pickerOpen = true
                },
                label = { Text("State") },
                placeholder = { Text("Search your state") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            pickerOpen = !pickerOpen
                            if (pickerOpen) query = ""
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (pickerOpen) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = if (pickerOpen) "Close state list" else "Open state list"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("disco_state_search")
            )

            if (pickerOpen) {
                val matches = StateDisCoMap.search(query)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (matches.isEmpty()) {
                            Text(
                                text = "No state matches \"$query\"",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        matches.forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clickable {
                                        selectedState = row
                                        pickerOpen = false
                                        query = ""
                                    }
                                    .padding(horizontal = 12.dp)
                                    .semantics { contentDescription = "Select ${row.state}" },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = row.state,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = row.discoCodes.joinToString(" / "),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            val row: StateDisCo? = selectedState
            if (row == null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Pick your state to see your DisCo's numbers.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                if (row.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (row.isSplit) "${row.state} is served by more than one DisCo. ${row.note}" else row.note,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                row.contacts.forEach { contact ->
                    Spacer(modifier = Modifier.height(12.dp))
                    DisCoContactCard(
                        contact = contact,
                        emailSubject = emailSubject,
                        onCopy = { label, value ->
                            clipboard.setText(AnnotatedString(value))
                            Toast.makeText(context, "$label copied: $value", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = DisCoContacts.SOURCE_FOOTER,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("disco_contacts_footer")
            )
        }
    }
}

@Composable
private fun DisCoContactCard(
    contact: DisCoContact,
    emailSubject: String,
    onCopy: (label: String, value: String) -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("disco_contact_${contact.code.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (!contact.verifiedOfficial) {
                    Spacer(modifier = Modifier.width(8.dp))
                    VerifyBadge(text = "Verify number")
                }
            }
            if (contact.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = contact.note,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            contact.phones.forEach { phone ->
                Spacer(modifier = Modifier.height(8.dp))
                ChannelRow(
                    value = phone,
                    actionLabel = "Call",
                    actionIcon = { Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    actionDescription = "Call ${contact.code} on $phone",
                    onAction = {
                        launchSafely(
                            context,
                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${PhoneNumbers.toDialable(phone)}")),
                            "No phone app found. The number is $phone."
                        )
                    },
                    onCopy = { onCopy("Number", phone) },
                    copyDescription = "Copy ${contact.code} number $phone"
                )
            }

            contact.whatsapp.forEach { wa ->
                val digits = PhoneNumbers.toWhatsAppDigits(wa) ?: return@forEach
                Spacer(modifier = Modifier.height(8.dp))
                ChannelRow(
                    value = wa,
                    actionLabel = "WhatsApp",
                    actionIcon = { Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    actionDescription = "Message ${contact.code} on WhatsApp at $wa",
                    onAction = {
                        launchSafely(
                            context,
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits")),
                            "Couldn't open WhatsApp. The number is $wa."
                        )
                    },
                    onCopy = { onCopy("WhatsApp number", wa) },
                    copyDescription = "Copy ${contact.code} WhatsApp number $wa"
                )
            }

            if (contact.email.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                ChannelRow(
                    value = contact.email,
                    actionLabel = "Email",
                    actionIcon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    actionDescription = "Email ${contact.code} at ${contact.email}",
                    onAction = {
                        val uri = Uri.parse("mailto:${contact.email}?subject=${Uri.encode(emailSubject)}")
                        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(contact.email))
                            putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                        }
                        launchSafely(context, intent, "No email app found. The address is ${contact.email}.")
                    },
                    onCopy = { onCopy("Email", contact.email) },
                    copyDescription = "Copy ${contact.code} email ${contact.email}",
                    badge = if (!contact.emailVerified) "Verify email" else null
                )
            }

            if (contact.website.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        launchSafely(
                            context,
                            Intent(Intent.ACTION_VIEW, Uri.parse(contact.website)),
                            "No browser found. The site is ${contact.website}."
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .semantics { contentDescription = "Open ${contact.code} website" }
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = contact.website.removePrefix("https://").removePrefix("www."),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelRow(
    value: String,
    actionLabel: String,
    actionIcon: @Composable () -> Unit,
    actionDescription: String,
    onAction: () -> Unit,
    onCopy: () -> Unit,
    copyDescription: String,
    badge: String? = null
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (badge != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    VerifyBadge(text = badge)
                }
            }
            OutlinedButton(
                onClick = onAction,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .semantics { contentDescription = actionDescription }
            ) {
                actionIcon()
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
            }
            IconButton(onClick = onCopy, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = copyDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun VerifyBadge(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
        color = MaterialTheme.extendedColors.onWarning,
        modifier = Modifier
            .background(MaterialTheme.extendedColors.warning, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .semantics { contentDescription = "$text: not confirmed on the official website" }
    )
}

private fun launchSafely(context: Context, intent: Intent, fallbackMessage: String) {
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, fallbackMessage, Toast.LENGTH_LONG).show()
    }
}
