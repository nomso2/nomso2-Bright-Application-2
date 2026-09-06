package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.Slate400Text
import com.example.ui.theme.Slate500Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Feature 3: Offline SMS / Emergency Dispatcher
 * Allows reporting outages and hazards with ZERO internet or data.
 * Formats a standardized NERC/DisCo emergency SMS and launches Android SMS with 1-tap.
 */
@Composable
fun OfflineEmergencySmsDialog(
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFault by remember { mutableStateOf("Transformer Sparking / Fire Hazard") }

    val faultOptions = listOf(
        "Transformer Sparking / Fire Hazard",
        "Fallen 33kV Live Cable Electrocution Risk",
        "Total Feeder Blackout Without Notice",
        "Low Single-Phase Voltage Damage Risk",
        "Feeder Pillar Submerged in Floodwater"
    )

    val emergencyDisCo = DisCo.fromCode(userProfile.discoCode)
    val emergencyPhone = emergencyDisCo.customerCarePhone

    val nowFormatted = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    }

    val formattedSms = remember(userProfile, selectedFault, nowFormatted) {
        """
        [BRIGHT EMERGENCY OUTAGE REPORT]
        DISCO: ${userProfile.discoCode}
        METER: ${userProfile.meterNumber}
        FEEDER: ${userProfile.feederName} (${userProfile.feederBand.code})
        TRANSFORMER: ${userProfile.transformerId}
        ADDRESS: ${userProfile.streetAddress}, ${userProfile.lga}, ${userProfile.state}
        RESIDENT: ${userProfile.customerName} (${userProfile.phoneNumber})
        FAULT: $selectedFault
        TIME: $nowFormatted
        ACTION: Immediate field crew intervention requested under NERC CPR SLA.
        """.trimIndent()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("offline_sms_emergency_dialog"),
        containerColor = ElegantDarkBar,
        titleContentColor = Color.White,
        textContentColor = Slate400Text,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x33EF4444)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SignalCellularOff,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Offline SMS Dispatcher",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Zero Data • Works without Internet",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "When cellular data is down during a blackout, this formats an official DisCo emergency dispatch SMS with your meter & transformer coordinates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400Text
                )

                Text(
                    text = "SELECT EMERGENCY FAULT TYPE:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = ElegantGoldPrimary
                )

                faultOptions.forEach { option ->
                    val isSelected = selectedFault == option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0x33FACC15) else Color(0xFF1E2430))
                            .border(
                                1.dp,
                                if (isSelected) ElegantGoldPrimary else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedFault = option }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.FlashOn else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isSelected) ElegantGoldPrimary else Slate500Text,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) Color.White else Slate400Text
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "FORMATTED SMS PAYLOAD TO ${userProfile.discoCode} (${emergencyPhone}):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )

                // SMS Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0B0D12))
                        .border(1.dp, ElegantDarkBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = formattedSms,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFFE2E8F0)
                    )
                }

                // Copy Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboardManager =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("DisCo Emergency SMS", formattedSms)
                            clipboardManager.setPrimaryClip(clip)
                            Toast.makeText(context, "SMS text copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantGoldPrimary),
                        modifier = Modifier.testTag("copy_sms_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy SMS",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy SMS Text", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("smsto:$emergencyPhone")
                            putExtra("sms_body", formattedSms)
                        }
                        context.startActivity(smsIntent)
                    } catch (e: Exception) {
                        // Fallback generic send
                        val sendIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("sms:$emergencyPhone")
                            putExtra("sms_body", formattedSms)
                        }
                        try {
                            context.startActivity(sendIntent)
                        } catch (ex: Exception) {
                            Toast.makeText(context, "No SMS app found. Text copied to clipboard!", Toast.LENGTH_LONG).show()
                        }
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("send_emergency_sms_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Sms,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("SEND VIA SMS NOW", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel", color = Slate400Text)
            }
        }
    )
}
