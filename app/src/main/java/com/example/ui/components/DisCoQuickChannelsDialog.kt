package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DisCo
import com.example.model.UserProfile
import com.example.ui.theme.ElegantDarkBar
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.Slate100Text
import com.example.ui.theme.Slate400Text
import com.example.ui.theme.Slate500Text

data class UssdQuickCode(
    val title: String,
    val code: String,
    val description: String
)

/**
 * Feature 8: DisCo WhatsApp Chatbot & USSD Dialers
 * 1-tap launcher for official DisCo WhatsApp bots and USSD quick dial codes.
 */
@Composable
fun DisCoQuickChannelsDialog(
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentDisCo = DisCo.fromCode(userProfile.discoCode)

    // DisCo specific WhatsApp contact numbers (clean international format)
    val whatsappNumber = when (currentDisCo) {
        DisCo.EKEDC -> "2347080655555"
        DisCo.IE -> "2349088999900"
        DisCo.AEDC -> "2348039070070"
        DisCo.IBEDC -> "2347001239999"
        DisCo.PHED -> "2348139834000"
        DisCo.EEDC -> "23484700100"
        DisCo.BEDC -> "2348035888888"
        DisCo.KAEDC -> "2348031230000"
        DisCo.KEDCO -> "2347005555555"
        DisCo.JED -> "2347000533267"
        DisCo.YEDC -> "2348031234567"
    }

    val ussdCodes = listOf(
        UssdQuickCode("Prepaid Token Retrieval", "*389*300#", "Retrieve lost 20-digit token or check vend history"),
        UssdQuickCode("DisCo Outage Status", "*5455#", "Check feeder status and scheduled load-shedding"),
        UssdQuickCode("Meter Account Verification", "*746#", "Verify tariff band, NIN link, and meter status"),
        UssdQuickCode("Quick Energy Vend Pay", "*737*50#", "Direct USSD mobile payment for electricity token")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("disco_channels_dialog"),
        containerColor = ElegantDarkBar,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF25D366).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "${userProfile.discoCode} Direct Channels",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "WhatsApp Bot, Voice Care & USSD Codes",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElegantGoldPrimary
                    )
                }
            }
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // WhatsApp Bot Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF25D366).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable {
                            val msg = "Hello ${userProfile.discoCode}, I am contacting you regarding Meter ${userProfile.meterNumber} on ${userProfile.feederName}."
                            val encodedMsg = Uri.encode(msg)
                            val url = "https://wa.me/$whatsappNumber?text=$encodedMsg"
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot launch WhatsApp: $url", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .testTag("launch_whatsapp_bot_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF132219))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF25D366)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Official WhatsApp Bot",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Chat with ${userProfile.discoCode} Virtual Assistant",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF86EFAC)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Phone Voice Dispatch Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, ElegantDarkBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${currentDisCo.customerCarePhone}"))
                            context.startActivity(intent)
                        }
                        .testTag("dial_customer_care_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B24))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ElegantGoldPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = ElegantGoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Customer Care Hotline",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Slate100Text
                                )
                                Text(
                                    text = currentDisCo.customerCarePhone,
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElegantGoldPrimary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = ElegantGoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // USSD Quick Dial Section
                Text(
                    text = "ONE-TAP USSD SHORTCODES (NO DATA REQUIRED):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = ElegantGoldPrimary
                )

                ussdCodes.forEach { ussd ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF161B24))
                            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(ussd.code)))
                                    context.startActivity(dialIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open dialer for ${ussd.code}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ussd.title,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Slate100Text
                            )
                            Text(
                                text = ussd.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400Text
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ElegantGoldPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = ussd.code,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = ElegantGoldPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElegantGoldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}
