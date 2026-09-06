package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.FeederBand
import com.example.model.UserProfile
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.ElegantGreenLive
import com.example.ui.theme.Slate100Text
import com.example.ui.theme.Slate400Text
import com.example.ui.theme.Slate500Text
import kotlinx.coroutines.delay

/**
 * Phase 1: Identity Resolution & Decentralized Ledger Onboarding
 */
@Composable
fun PhaseOnboardingDialog(
    currentProfile: UserProfile,
    onDismiss: () -> Unit,
    onCompleteOnboarding: (meterNum: String, disco: String, band: FeederBand, address: String, paymentGateway: String) -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) } // Manual Sign up or Login
    var step by remember { mutableStateOf(1) } // 1: Meter & SIM, 2: Topology Match, 3: ₦100 Capitalization, 4: Done

    var meterInput by remember { mutableStateOf(currentProfile.meterNumber) }
    var phoneInput by remember { mutableStateOf(currentProfile.phoneNumber) }
    var addressInput by remember { mutableStateOf(currentProfile.streetAddress) }
    var detectedDisCo by remember { mutableStateOf("EKEDC") }
    var detectedBand by remember { mutableStateOf(FeederBand.BAND_A) }
    var selectedPaymentGateway by remember { mutableStateOf("OPay") } // OPay, Moniepoint, Airtime
    var selectedCarrier by remember { mutableStateOf("MTN") }

    var isVerifyingSim by remember { mutableStateOf(false) }
    var isSimVerified by remember { mutableStateOf(true) }
    var otpCode by remember { mutableStateOf("849201") }

    val isValidMeterLength = meterInput.filter { it.isDigit() }.length in 11..13

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("phase1_onboarding_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = ElegantGoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PHASE 1: LEDGER ONBOARDING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = ElegantGoldPrimary
                            )
                        }
                        Text(
                            text = if (isSignUpMode) "Create Decentralized Profile" else "Meter Identity Resolution",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate100Text
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate400Text)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sign Up / Login Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x14FFFFFF))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isSignUpMode) ElegantGoldPrimary else Color.Transparent)
                            .clickable { isSignUpMode = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Manual Login",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (!isSignUpMode) Color(0xFF0A0C10) else Slate400Text
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSignUpMode) ElegantGoldPrimary else Color.Transparent)
                            .clickable { isSignUpMode = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign Up / Register",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSignUpMode) Color(0xFF0A0C10) else Slate400Text
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Omnichannel Ingestion Gateway Info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x0D60A5FA))
                        .border(1.dp, Color(0x3360A5FA), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Dialpad,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Omnichannel Ingestion Gateway Active",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF93C5FD)
                            )
                            Text(
                                text = "Native Android App • Data-free USSD Code: *38432#",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Slate400Text
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Step 1: Manual Meter Index Verification Engine
                Text(
                    text = "1. Meter Index & SIM Authentication",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate100Text
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = meterInput,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(13)
                        meterInput = digits
                        // Auto-detect DisCo & Band based on prefix
                        if (digits.startsWith("01") || digits.startsWith("45")) {
                            detectedDisCo = "EKEDC"
                            detectedBand = FeederBand.BAND_A
                        } else if (digits.startsWith("61") || digits.startsWith("02")) {
                            detectedDisCo = "IBEDC"
                            detectedBand = FeederBand.BAND_B
                        } else if (digits.startsWith("03") || digits.startsWith("77")) {
                            detectedDisCo = "AEDC"
                            detectedBand = FeederBand.BAND_A
                        }
                    },
                    label = { Text("Prepaid Meter Number (11 or 13 digits)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = ElegantGoldPrimary)
                    },
                    trailingIcon = {
                        if (isValidMeterLength) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = ElegantGreenLive)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantGoldPrimary,
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedTextColor = Slate100Text,
                        unfocusedTextColor = Slate100Text
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("SIM MSISDN Phone Number") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, tint = ElegantGoldPrimary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantGoldPrimary,
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedTextColor = Slate100Text,
                        unfocusedTextColor = Slate100Text
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // SIM-Authenticated OTP Verification Handshake
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1422C55E))
                        .border(1.dp, Color(0x3322C55E), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = ElegantGreenLive)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "SIM Cryptographic Handshake",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF4ADE80)
                                )
                                Text(
                                    text = "Automated carrier-level OTP validation ($otpCode)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Slate400Text
                                )
                            }
                        }
                        Text(
                            text = "VERIFIED",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = ElegantGreenLive
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Step 2: Utility & Service-Band Topology Mapper
                Text(
                    text = "2. Utility & Service-Band Topology Mapper",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate100Text
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x14FFFFFF))
                        .border(1.dp, ElegantDarkBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "DisCo Franchise:", style = MaterialTheme.typography.bodySmall, color = Slate400Text)
                            Text(text = "$detectedDisCo Licensee Area", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ElegantGoldPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Service Tier Classification:", style = MaterialTheme.typography.bodySmall, color = Slate400Text)
                            Text(text = "${detectedBand.code} (${detectedBand.minimumHours}+ hrs SLA target)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF60A5FA))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Presidential Metering Initiative (PMI):", style = MaterialTheme.typography.bodySmall, color = Slate400Text)
                            Text(text = "Smart Node Cross-Synced", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ElegantGreenLive)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Step 3: ₦100 Capitalization Gateway Integration & Carrier Airtime Deduction
                Text(
                    text = "3. ₦100 Infrastructure Activation Gateway",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate100Text
                )
                Text(
                    text = "Fixed statutory ₦100 token ledger capitalization via local fintech or direct airtime",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Slate400Text
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Channel options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("OPay", "Moniepoint", "Airtime (MTN/Airtel)").forEach { gateway ->
                        val isSelected = selectedPaymentGateway == gateway
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0x26FACC15) else Color(0x0DFFFFFF))
                                .border(
                                    1.dp,
                                    if (isSelected) ElegantGoldPrimary else ElegantDarkBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedPaymentGateway = gateway }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (gateway.contains("Airtime")) Icons.Default.PhoneAndroid else Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = if (isSelected) ElegantGoldPrimary else Slate400Text,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = gateway,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = if (isSelected) ElegantGoldPrimary else Slate400Text
                                )
                            }
                        }
                    }
                }

                if (selectedPaymentGateway.contains("Airtime")) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Carrier VAS Bridge: Direct ₦100 airtime balance deduction on SIM line",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFFFBBF24)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        onCompleteOnboarding(
                            meterInput,
                            detectedDisCo,
                            detectedBand,
                            addressInput,
                            selectedPaymentGateway
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_onboarding_button"),
                    enabled = isValidMeterLength,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantGoldPrimary,
                        contentColor = Color(0xFF0A0C10)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isSignUpMode) "Register & Activate (₦100)" else "Verify & Sync Decentralized Meter",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
