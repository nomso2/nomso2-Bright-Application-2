package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.theme.ElegantDarkBar
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.Slate100Text
import com.example.ui.theme.Slate400Text
import com.example.ui.theme.Slate500Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Feature 4: Appliance Surge Damage Claim Assistant
 * Generates and serves a formal NERC statutory compensation demand letter
 * under Customer Protection Regulations (CPR) 2023 [NERC/REG/2023/CPR-01].
 */
@Composable
fun ApplianceDamageClaimDialog(
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onSubmitClaim: (
        applianceName: String,
        brandModel: String,
        estimatedLossNgn: Double,
        surgeTime: String,
        surgeDescription: String,
        statutoryNotice: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val applianceCategories = listOf(
        "Solar Inverter / Power Board",
        "Air Conditioner Compressor",
        "Smart OLED / LED Television",
        "Refrigerator / Deep Freezer",
        "Borehole Pumping Machine",
        "Washing Machine / Microwave",
        "Other Electrical Appliance"
    )

    var selectedAppliance by remember { mutableStateOf(applianceCategories[0]) }
    var brandModel by remember { mutableStateOf("Luminous 5kVA Pure Sine Wave Inverter") }
    var lossAmountText by remember { mutableStateOf("185000") }
    var surgeTime by remember { mutableStateOf("Yesterday, 11:30 PM (Post-blackout line re-energization)") }
    var surgeDescription by remember {
        mutableStateOf("Destructive high-voltage spike exceeding 315V upon 33kV line restoration blasted charging board capacitor and smoked internal transformer coil.")
    }

    val statutoryNotice = remember(
        userProfile, selectedAppliance, brandModel, lossAmountText, surgeTime, surgeDescription
    ) {
        """
        FORMAL STATUTORY DEMAND FOR APPLIANCE SURGE COMPENSATION
        PURSUANT TO NERC CUSTOMER PROTECTION REGULATIONS (CPR) 2023 [NERC/REG/2023/CPR-01]

        TO:
        The Managing Director & Directorate of Legal Enforcement
        ${userProfile.discoCode} Head Office

        COPIED TO:
        Consumer Protection & Forum Directorate
        Nigerian Electricity Regulatory Commission (NERC) HQ, Abuja

        1. AFFECTED CONSUMER PARTICULARS:
        Account Name: ${userProfile.customerName}
        Meter Number: ${userProfile.meterNumber}
        Supply Band: ${userProfile.feederBand.code} (${userProfile.feederBand.description})
        Injection Feeder: ${userProfile.feederName}
        Distribution Transformer: ${userProfile.transformerId}
        Service Address: ${userProfile.streetAddress}, ${userProfile.lga}, ${userProfile.state}
        Contact Phone: ${userProfile.phoneNumber}

        2. INCIDENT & DAMAGE PARTICULARS:
        Date & Time of Surge: $surgeTime
        Damaged Equipment: $selectedAppliance ($brandModel)
        Nature of Voltage Abnormality: $surgeDescription
        Estimated Damage Loss / Replacement: NGN ${lossAmountText.toDoubleOrNull()?.let { "%,.2f".format(it) } ?: lossAmountText}

        3. STATUTORY NOTICE & DEMAND:
        Under CPR 2023 Regulation 18(2) and Section 68 of the Electricity Act 2023, Distribution Licensees bear strict civil and statutory liability for damages sustained by customer end-user appliances resulting from power quality deviations, voltage spikes, or phase surges exceeding standard statutory ±6% nominal tolerance.

        Demand is hereby formally entered for:
        (a) A joint on-site physical engineering inspection of the damaged appliance within 7 business days; AND
        (b) Immediate settlement or compensatory energy token billing credit of NGN ${lossAmountText.toDoubleOrNull()?.let { "%,.2f".format(it) } ?: lossAmountText}.

        Failure to acknowledge or settle within the statutory SLA shall trigger formal arbitration before the NERC Consumer Forum.
        """.trimIndent()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("appliance_surge_claim_dialog"),
        containerColor = ElegantDarkBar,
        titleContentColor = Color.White,
        textContentColor = Slate400Text,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FACC15)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = ElegantGoldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Appliance Surge Claim",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "NERC Statutory Compensation Assistant",
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
                Text(
                    text = "Did a power surge or voltage spike burn your inverter, fridge, AC, or TV? DisCos are legally required by NERC CPR 2023 to inspect and compensate verified claims.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400Text
                )

                // Select Appliance
                Text(
                    text = "DAMAGED APPLIANCE TYPE:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = ElegantGoldPrimary
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(applianceCategories) { category ->
                        val isSelected = selectedAppliance == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAppliance = category },
                            label = { Text(category, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElegantGoldPrimary,
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF1E2430),
                                labelColor = Slate400Text
                            ),
                            modifier = Modifier.testTag("appliance_chip_${category.take(8)}")
                        )
                    }
                }

                // Brand & Model
                OutlinedTextField(
                    value = brandModel,
                    onValueChange = { brandModel = it },
                    label = { Text("Brand & Model (e.g. Luminous 5kVA, Hisense 65\" 4K)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("claim_brand_model_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantGoldPrimary,
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedTextColor = Slate100Text,
                        unfocusedTextColor = Slate100Text
                    )
                )

                // Estimated Loss / Replacement NGN
                OutlinedTextField(
                    value = lossAmountText,
                    onValueChange = { lossAmountText = it },
                    label = { Text("Estimated Repair / Replacement Cost (₦)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("claim_loss_amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantGoldPrimary,
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedTextColor = Slate100Text,
                        unfocusedTextColor = Slate100Text
                    )
                )

                // Surge Date / Time
                OutlinedTextField(
                    value = surgeTime,
                    onValueChange = { surgeTime = it },
                    label = { Text("Date & Time of Voltage Spike") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("claim_surge_time_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantGoldPrimary,
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedTextColor = Slate100Text,
                        unfocusedTextColor = Slate100Text
                    )
                )

                // Surge description
                OutlinedTextField(
                    value = surgeDescription,
                    onValueChange = { surgeDescription = it },
                    label = { Text("Circumstances / Voltage Surge Details") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("claim_surge_desc_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantGoldPrimary,
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedTextColor = Slate100Text,
                        unfocusedTextColor = Slate100Text
                    )
                )

                // Preview Box of Statutory Notice
                Text(
                    text = "GENERATED STATUTORY DEMAND LETTER:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0B0D12))
                        .border(1.dp, ElegantDarkBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = statutoryNotice,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }

                // Copy & Share buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboardManager =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("NERC Surge Claim Notice", statutoryNotice)
                            clipboardManager.setPrimaryClip(clip)
                            Toast.makeText(context, "Formal Statutory Demand copied!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantGoldPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_statutory_claim_btn")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Letter", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "NERC CPR 2023 Statutory Surge Damage Demand - Meter ${userProfile.meterNumber}")
                                putExtra(Intent.EXTRA_TEXT, statutoryNotice)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Statutory Claim Notice"))
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_statutory_claim_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share Letter", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = lossAmountText.toDoubleOrNull() ?: 100000.0
                    onSubmitClaim(
                        selectedAppliance,
                        brandModel,
                        amount,
                        surgeTime,
                        surgeDescription,
                        statutoryNotice
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElegantGoldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("submit_surge_claim_dialog_btn")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("FILE CLAIM WITH DISCO", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Close", color = Slate400Text)
            }
        }
    )
}
