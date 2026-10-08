package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DisCoContacts
import com.example.model.SupportContacts
import com.example.model.UserProfile
import com.example.ui.solutions.common.ActionButton
import com.example.ui.solutions.common.InfoNote
import com.example.ui.solutions.common.SolutionIntents
import com.example.ui.solutions.common.customerBlock

/** One short question and its plain-words answer. */
private data class HelpQuestion(val key: String, val question: String, val answer: String)

private val helpQuestions = listOf(
    HelpQuestion(
        key = "report_fault",
        question = "How do I report a fault?",
        answer = "Tap the big \"Report Outage / Fault\" button on Home, or \"Report\" at the bottom of " +
            "the screen. Choose what is wrong, write a short note (a photo helps), then tap " +
            "\"Submit fault ticket\". You can follow it under \"History\"."
    ),
    HelpQuestion(
        key = "refund_tracker",
        question = "What does the refund tracker do?",
        answer = "It keeps a record of when your light goes off and comes back. Bright adds up your " +
            "hours of light and compares them with what your band promises. If you got fewer hours, " +
            "it works out what your DisCo owes you and helps you claim it. Find it in More, then " +
            "\"Settings: Bright tools\", then \"Track when light goes and comes back\"."
    ),
    HelpQuestion(
        key = "change_pin",
        question = "How do I change my PIN?",
        answer = "Open More, then \"Profile & Security\", then tap \"Change my PIN\". Type your " +
            "current PIN, then your new PIN of 4 to 6 numbers, then the new PIN again. " +
            "Forgot your PIN? Tap \"Forgot your PIN?\" on the sign-in or unlock screen. You only " +
            "need your meter number and full name, and nothing on your phone is deleted."
    )
)

/**
 * Help: big buttons to reach the user's DisCo (and Bright support, once its numbers are set in
 * [SupportContacts]), plus a few short answers. Reached from More > Help and the Home header.
 */
@Composable
fun HelpScreen(
    userProfile: UserProfile,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val disco = SolutionIntents.discoFor(userProfile)
    val helloDisco = "Hello, I need help with my electricity.\n\n" + customerBlock(userProfile)
    val helloBright = "Hello Bright, I need help with the app.\n\n" +
        "Name: ${userProfile.customerName}\nMeter: ${userProfile.meterNumber}"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("help_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("help_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Help",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = "We're here to help. Tap a button to call or send a WhatsApp message.",
            fontSize = 17.sp,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Your DisCo
        HelpSectionTitle("Your electricity company")
        if (disco == null) {
            InfoNote("Add your state or DisCo in your profile, and their numbers will show here.", isWarning = true)
        } else {
            Text(
                text = disco.name,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (disco.primaryPhone.isNotBlank()) {
                ActionButton(
                    text = "Call your DisCo",
                    icon = Icons.Default.Call,
                    onClick = { SolutionIntents.dial(context, disco.primaryPhone) },
                    modifier = Modifier.testTag("help_call_disco_button")
                )
            }
            disco.whatsapp.firstOrNull()?.let { wa ->
                ActionButton(
                    text = "WhatsApp your DisCo",
                    icon = Icons.Default.Chat,
                    onClick = { SolutionIntents.whatsApp(context, wa, helloDisco) },
                    outlined = true,
                    modifier = Modifier.testTag("help_whatsapp_disco_button")
                )
            }
            Text(
                text = DisCoContacts.SOURCE_FOOTER,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Bright support: only shown once real numbers are set in SupportContacts.
        if (SupportContacts.hasPhone || SupportContacts.hasWhatsApp) {
            HelpSectionTitle("Bright support")
            if (SupportContacts.hasPhone) {
                ActionButton(
                    text = "Call Bright support",
                    icon = Icons.Default.Call,
                    onClick = { SolutionIntents.dial(context, SupportContacts.SUPPORT_PHONE) },
                    modifier = Modifier.testTag("help_call_bright_button")
                )
            }
            if (SupportContacts.hasWhatsApp) {
                ActionButton(
                    text = "WhatsApp Bright support",
                    icon = Icons.Default.Chat,
                    onClick = { SolutionIntents.whatsApp(context, SupportContacts.SUPPORT_WHATSAPP, helloBright) },
                    outlined = true,
                    modifier = Modifier.testTag("help_whatsapp_bright_button")
                )
            }
        }

        // Short answers, all shown (nothing hidden behind taps).
        HelpSectionTitle("Common questions")
        helpQuestions.forEach { item -> HelpAnswerCard(item) }
    }
}

@Composable
private fun HelpSectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun HelpAnswerCard(item: HelpQuestion) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("help_faq_${item.key}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = item.question,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = item.answer,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
