package com.example.ui.solutions

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldPrimary

// ==========================================
// SOLUTION 16: VISUAL PROOF OVERRIDE
// ==========================================
@Composable
fun VisualProofOverrideFeature(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedProofType by remember { mutableStateOf("Blown Transformer Jumper Fuse") }
    var proofAttached by remember { mutableStateOf(false) }

    val proofTypes = listOf(
        "Blown Transformer Jumper Fuse",
        "Snapped Aluminum Overhead Cable",
        "Melted Feeder Pillar Cut-Out",
        "Cracked Concrete Pole Leaning on Building"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "16. Visual Proof Override (Bypass 3-Neighbor Rule)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Bypasses the restrictive 3-neighbor co-signature rule immediately when you upload geotagged photographic proof of physical equipment damage.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Visible Physical Damage:", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                proofTypes.forEach { type ->
                    val isSelected = selectedProofType == type
                    Surface(
                        color = if (isSelected) GoldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GoldPrimary else Color.Transparent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = type,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (proofAttached) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(24.dp))
                                Text("Cryptographic EXIF Validated • GPS: 6.5244°N, 3.3792°E", fontSize = 10.sp, color = Color.White)
                                Text("3-Neighbor Co-Signature Requirement: BYPASSED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent)
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(22.dp))
                                Text("Tap Below to Attach Real-Time Camera Proof", fontSize = 11.sp, color = Color.LightGray)
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        proofAttached = true
                        Toast.makeText(context, "Visual evidence attached & 3-neighbor rule bypassed!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (proofAttached) "Fast-Track Evidence Verified ✓" else "Capture & Verify Damage Proof", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// SOLUTION 17: "WAKE UP THE STREET" ALERT BROADCAST
// ==========================================
@Composable
fun WakeUpStreetAlertsFeature(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var coSignersCount by remember { mutableStateOf(6) }
    var broadcastSent by remember { mutableStateOf(false) }
    val threshold = 5

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "17. 'Wake Up the Street' Community Alert Broadcast",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Broadcasts a free localized alert ping to all registered households on your transformer to co-sign the outage and hit the priority escalation threshold.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Transformer ID:", fontSize = 11.sp)
                    Text(userProfile.transformerId, fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 12.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Co-Signers on Transformer:", fontSize = 11.sp)
                    Text("$coSignersCount / $threshold Required", fontWeight = FontWeight.Bold, color = EmeraldAccent, fontSize = 12.sp)
                }

                LinearProgressIndicator(
                    progress = { (coSignersCount.toFloat() / threshold).coerceAtMost(1f) },
                    color = EmeraldAccent,
                    modifier = Modifier.fillMaxWidth().height(6.dp)
                )

                Surface(
                    color = EmeraldAccent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(16.dp))
                        Text("AUTOMATIC NERC ESCALATION: Threshold reached! Outage officially flagged as critical feeder priority.", fontSize = 10.sp, color = EmeraldAccent, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        coSignersCount += 2
                        broadcastSent = true
                        Toast.makeText(context, "Broadcast ping sent to 142 households on ${userProfile.transformerId}!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.NotificationAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (broadcastSent) "Street Ping Sent (+2 Co-Signers) ✓" else "Send 'Wake Up The Street' Alert", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// SOLUTION 18: USER TRUST SCORE & REPUTATION
// ==========================================
@Composable
fun UserTrustScoreFeature(
    userTrustScore: Int = 98,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentScore by remember { mutableStateOf(userTrustScore) }
    var isNinVerified by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "18. Citizen Trust Score & VIP Dispatch Reputation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Earns reputation points for verified outage logs and hazard reports. High-trust citizens bypass customer care queues directly to Area Managers.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Citizen Reputation Score", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$currentScore / 100", fontSize = 24.sp, fontWeight = FontWeight.Black, color = GoldPrimary)
                    }
                    Surface(
                        color = EmeraldAccent.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("VIP LEVEL 3 GUARDIAN", color = EmeraldAccent, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }

                Text("Recent Points Ledger:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("• +20 Pts: Reported fallen conductor hazard (Confirmed by DisCo)", fontSize = 10.sp, color = EmeraldAccent)
                    Text("• +10 Pts: Provided verified photo proof of blown jumper", fontSize = 10.sp, color = EmeraldAccent)
                    Text("• +5 Pts: Co-signed street outage ticket", fontSize = 10.sp, color = EmeraldAccent)
                }

                Button(
                    onClick = {
                        if (!isNinVerified) {
                            currentScore = (currentScore + 2).coerceAtMost(100)
                            isNinVerified = true
                            Toast.makeText(context, "NIN & Utility Bill verified! +2 Trust points awarded.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isNinVerified) "ID Fully Verified ✓" else "Verify NIN & Utility Bill (+2 Pts)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// SOLUTION 19: NEIGHBORHOOD GRID FORUM
// ==========================================
@Composable
fun NeighborhoodGridForumFeature(
    userProfile: UserProfile,
    onOpenFullForum: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var newPostText by remember { mutableStateOf("") }
    var postsList by remember {
        mutableStateOf(
            listOf(
                Pair("Adebayo (House 12)", "Linesmen just entered our street. They are inspecting the 33kV jumper."),
                Pair("Engr. Kenneth (House 4)", "Please no resident should give them money for fuel. Maintenance is legally free!"),
                Pair("Mama Chidinma (House 19)", "Light is back on Phase 1, but Phase 2 is still dim.")
            )
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "19. Local Transformer Grid Forum (${userProfile.transformerId})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Private community board for residents connected strictly to your transformer to organize, share restoration news, and counter extortion.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                postsList.take(3).forEach { (author, text) ->
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(author, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GoldPrimary)
                            Text(text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newPostText,
                        onValueChange = { newPostText = it },
                        placeholder = { Text("Post update to transformer neighbors...", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Button(
                        onClick = {
                            if (newPostText.isNotBlank()) {
                                postsList = listOf(Pair("You (${userProfile.meterNumber.takeLast(4)})", newPostText)) + postsList
                                newPostText = ""
                                Toast.makeText(context, "Posted update to transformer thread!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ==========================================
// SOLUTION 20: MULTI-LINGUAL VOICE REPORTING
// ==========================================
@Composable
fun MultiLingualVoiceReportingFeature(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf("Pidgin") }
    var isRecording by remember { mutableStateOf(false) }
    var ticketConverted by remember { mutableStateOf(false) }

    val samplePhrases = mapOf(
        "Pidgin" to "Light don quench for our street since morning. The transformer spark gboaa and smoke full everywhere.",
        "Yoruba" to "Iná ti kú láti ọ̀sán. Wáyà tọ́pọ̀ lórí pọ́ọ̀lù ti já lulẹ̀, iná sì ń tàn lórí ilẹ̀.",
        "Hausa" to "Wutar lantarki ta dauke tun safe. Waya ta fadi a kasa kuma tana fitar da wuta.",
        "Igbo" to "Ọkụ agwala n'ogbe anyị kemgbe ụtụtụ. Waya daa n'ala na-enwu ọkụ."
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "20. Multi-Lingual AI Voice Reporting Engine",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Speak in Nigerian languages (Pidgin, Yoruba, Hausa, Igbo). Converts conversational voice notes into formal technical NERC fault tickets.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Pidgin", "Yoruba", "Hausa", "Igbo").forEach { lang ->
                val isSelected = selectedLanguage == lang
                Button(
                    onClick = { selectedLanguage = lang },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(lang, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Spoken Voice Note ($selectedLanguage):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"${samplePhrases[selectedLanguage]}\"",
                        fontSize = 12.sp,
                        color = GoldPrimary,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Surface(
                    color = EmeraldAccent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("AI Formal NERC Technical Translation:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent)
                        Text("• Fault Category: Unplanned Distribution Feeder Outage", fontSize = 10.sp)
                        Text("• Hazard Code: Active Overhead Conductor Arcing on Ground", fontSize = 10.sp)
                        Text("• Substation: Transformer ${userProfile.transformerId}", fontSize = 10.sp)
                    }
                }

                Button(
                    onClick = {
                        ticketConverted = true
                        Toast.makeText(context, "Voice note transcribed & mapped to NERC complaint!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (ticketConverted) "Mapped to Official NERC Ticket ✓" else "Record & Transcribe Voice Ticket", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
