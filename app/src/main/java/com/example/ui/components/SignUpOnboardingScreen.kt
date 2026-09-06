package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DisCo
import com.example.model.FeederBand
import com.example.model.UserProfile
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpOnboardingScreen(
    currentProfile: UserProfile = UserProfile(),
    onCompleteSignUp: (UserProfile) -> Unit,
    onSkipForNow: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var meterNumber by remember { mutableStateOf(currentProfile.meterNumber) }
    var customerName by remember { mutableStateOf(currentProfile.customerName) }
    var phoneNumber by remember { mutableStateOf(currentProfile.phoneNumber) }
    var streetAddress by remember { mutableStateOf(currentProfile.streetAddress) }
    var lga by remember { mutableStateOf(currentProfile.lga) }
    var selectedState by remember { mutableStateOf(currentProfile.state) }
    var selectedDisCo by remember { mutableStateOf(DisCo.fromCode(currentProfile.discoCode)) }
    var selectedBand by remember { mutableStateOf(currentProfile.feederBand) }
    var isPrepaid by remember { mutableStateOf(currentProfile.isPrepaid) }
    var transformerId by remember { mutableStateOf(currentProfile.transformerId) }

    var isDisCoDropdownExpanded by remember { mutableStateOf(false) }
    var isBandDropdownExpanded by remember { mutableStateOf(false) }

    // Simulation states
    var showQrScannerModal by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("4892") }
    var isOtpSent by remember { mutableStateOf(false) }

    val statesOfNigeria = listOf(
        "Lagos State", "Abuja FCT", "Rivers State", "Oyo State", "Enugu State",
        "Kano State", "Kaduna State", "Edo State", "Plateau State", "Delta State", "Ogun State"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Brand Logo & Welcome
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(GoldPrimary.copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, GoldPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Bright Power Logo",
                    tint = GoldPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "WELCOME TO BRIGHT",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Nigeria's Automated Consumer Power & Grid Rights Intelligence",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // First-Time Setup Badge
            Surface(
                color = EmeraldAccent.copy(alpha = 0.12f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = EmeraldAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "NEW CONSUMER ONBOARDING WIZARD",
                        color = EmeraldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick 1-Tap Demo Fill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sign up with your meter:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = {
                        meterNumber = "01429583192"
                        customerName = "Chuka Obunma"
                        phoneNumber = "+234 803 892 4110"
                        streetAddress = "14 Adeola Odeku Street, Victoria Island"
                        lga = "Eti-Osa"
                        selectedState = "Lagos State"
                        selectedDisCo = DisCo.EKEDC
                        selectedBand = FeederBand.BAND_A
                        transformerId = "TR-LOS-VI-04B"
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                    modifier = Modifier.testTag("quick_fill_demo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Auto-Fill Demo Profile", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SECTION 1: Meter Details & Scanner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricMeter,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "1. Prepaid / Postpaid Smart Meter",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = { showQrScannerModal = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("scan_meter_barcode_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan Meter QR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = meterNumber,
                        onValueChange = { meterNumber = it },
                        label = { Text("11-Digit Meter Number") },
                        placeholder = { Text("e.g. 01429583192") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_meter_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // DisCo Selector
                        ExposedDropdownMenuBox(
                            expanded = isDisCoDropdownExpanded,
                            onExpandedChange = { isDisCoDropdownExpanded = !isDisCoDropdownExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedDisCo.code,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("DisCo") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDisCoDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("disco_dropdown"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = isDisCoDropdownExpanded,
                                onDismissRequest = { isDisCoDropdownExpanded = false }
                            ) {
                                DisCo.entries.forEach { disco ->
                                    DropdownMenuItem(
                                        text = { Text("${disco.code} - ${disco.fullName}") },
                                        onClick = {
                                            selectedDisCo = disco
                                            isDisCoDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Feeder Band Selector
                        ExposedDropdownMenuBox(
                            expanded = isBandDropdownExpanded,
                            onExpandedChange = { isBandDropdownExpanded = !isBandDropdownExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedBand.code,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Feeder Band") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBandDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("band_dropdown"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = isBandDropdownExpanded,
                                onDismissRequest = { isBandDropdownExpanded = false }
                            ) {
                                FeederBand.entries.forEach { band ->
                                    DropdownMenuItem(
                                        text = { Text("${band.code} (${band.minimumHours}h+ SLA)") },
                                        onClick = {
                                            selectedBand = band
                                            isBandDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION 2: Location & Address
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "2. Geofenced Residence Location",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedTextField(
                        value = streetAddress,
                        onValueChange = { streetAddress = it },
                        label = { Text("House & Street Address") },
                        placeholder = { Text("e.g. 14 Adeola Odeku Street, Victoria Island") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_address_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = lga,
                            onValueChange = { lga = it },
                            label = { Text("LGA / District") },
                            placeholder = { Text("e.g. Eti-Osa") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("onboarding_lga_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary
                            )
                        )

                        OutlinedTextField(
                            value = selectedState,
                            onValueChange = { selectedState = it },
                            label = { Text("State") },
                            placeholder = { Text("e.g. Lagos State") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("onboarding_state_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary
                            )
                        )
                    }

                    OutlinedTextField(
                        value = transformerId,
                        onValueChange = { transformerId = it },
                        label = { Text("Neighborhood Transformer ID") },
                        placeholder = { Text("e.g. TR-LOS-VI-04B") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_transformer_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION 3: Customer Identity & SIM Authentication
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "3. Customer Identity & SIM Handshake",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Full Legal Name") },
                        placeholder = { Text("e.g. Chuka Obunma") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary
                        )
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Mobile Phone Number (SIM-Linked)") },
                        placeholder = { Text("e.g. +234 803 892 4110") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_phone_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary
                        )
                    )

                    // Cellular OTP Simulation Banner
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SIM-Authenticated OTP",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isOtpSent) "OTP code sent via SMS: $otpCode" else "Cellular handshake ready",
                                    fontSize = 11.sp,
                                    color = EmeraldAccent
                                )
                            }

                            Button(
                                onClick = {
                                    isOtpSent = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                                modifier = Modifier.testTag("send_otp_button")
                            ) {
                                Text(if (isOtpSent) "Resend" else "Send OTP", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Complete Sign Up Action Button
            Button(
                onClick = {
                    val updated = UserProfile(
                        meterNumber = meterNumber.ifBlank { "01429583192" },
                        customerName = customerName.ifBlank { "Chuka Obunma" },
                        phoneNumber = phoneNumber.ifBlank { "+234 803 892 4110" },
                        streetAddress = streetAddress.ifBlank { "14 Adeola Odeku Street, Victoria Island" },
                        lga = lga.ifBlank { "Eti-Osa" },
                        state = selectedState.ifBlank { "Lagos State" },
                        discoCode = selectedDisCo.code,
                        feederName = "${selectedDisCo.code} 33kV Injection Feeder",
                        feederBand = selectedBand,
                        transformerId = transformerId.ifBlank { "TR-LOS-VI-04B" },
                        isPrepaid = isPrepaid,
                        connectedHouseholdsCount = 184
                    )
                    onCompleteSignUp(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("complete_signup_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ACTIVATE METER & ENTER BRIGHT",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // SIMULATED QR / BARCODE CAMERA SCANNER DIALOG
        if (showQrScannerModal) {
            Dialog(onDismissRequest = { showQrScannerModal = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Universal Meter Scanner",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            IconButton(onClick = { showQrScannerModal = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        // Camera Viewfinder Box
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .border(2.dp, GoldPrimary, RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Point camera at prepaid meter barcode or STS QR code",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                meterNumber = "04192837461"
                                selectedDisCo = DisCo.EKEDC
                                selectedBand = FeederBand.BAND_A
                                showQrScannerModal = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("simulate_successful_scan_button")
                        ) {
                            Text("Simulate Successful Meter Scan", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
