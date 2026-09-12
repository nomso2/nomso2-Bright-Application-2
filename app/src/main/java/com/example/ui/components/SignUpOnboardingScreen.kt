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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DisCo
import com.example.model.FeederBand
import com.example.model.UserProfile
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldPrimary

data class DemoAccount(
    val profile: UserProfile,
    val subtitle: String,
    val badgeColor: Color
)

val PRESET_ACCOUNTS = listOf(
    DemoAccount(
        profile = UserProfile(
            meterNumber = "01429583192",
            customerName = "Chuka Obunma",
            phoneNumber = "+234 803 892 4110",
            streetAddress = "14 Adeola Odeku Street, Victoria Island",
            lga = "Eti-Osa",
            state = "Lagos State",
            discoCode = "EKEDC",
            feederName = "Victoria Island 33kV Injection Feeder 4",
            feederBand = FeederBand.BAND_A,
            transformerId = "TR-VI-ADEOLA-04B",
            isPrepaid = true,
            connectedHouseholdsCount = 184,
            isOnboarded = true
        ),
        subtitle = "Prepaid Residential • Band A (20h+ SLA) • Victoria Island",
        badgeColor = Color(0xFFE5B869)
    ),
    DemoAccount(
        profile = UserProfile(
            meterNumber = "04821094821",
            customerName = "Amina Bello",
            phoneNumber = "+234 802 445 9921",
            streetAddress = "22 Gana Street, Maitama",
            lga = "Abuja Municipal",
            state = "Abuja FCT",
            discoCode = "AEDC",
            feederName = "Maitama Central 33kV Feeder",
            feederBand = FeederBand.BAND_B,
            transformerId = "TR-ABJ-MAIT-12A",
            isPrepaid = true,
            connectedHouseholdsCount = 96,
            isOnboarded = true
        ),
        subtitle = "Prepaid Residential • Band B (16h+ SLA) • Abuja FCT",
        badgeColor = Color(0xFF38BDF8)
    ),
    DemoAccount(
        profile = UserProfile(
            meterNumber = "02839104852",
            customerName = "Babatunde Adeleke",
            phoneNumber = "+234 805 123 7890",
            streetAddress = "8 Isaac John Street, GRA Ikeja",
            lga = "Ikeja",
            state = "Lagos State",
            discoCode = "IKEDC",
            feederName = "Ikeja GRA 11kV Feeder 2",
            feederBand = FeederBand.BAND_A,
            transformerId = "TR-LOS-IKJ-08",
            isPrepaid = false,
            connectedHouseholdsCount = 210,
            isOnboarded = true
        ),
        subtitle = "Postpaid Commercial • Band A (20h+ SLA) • Ikeja GRA",
        badgeColor = Color(0xFF4ADE80)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpOnboardingScreen(
    currentProfile: UserProfile = UserProfile(),
    initialSignInMode: Boolean = true,
    isDismissible: Boolean = false,
    onDismiss: () -> Unit = {},
    onCompleteSignUp: (UserProfile) -> Unit,
    onSignIn: (UserProfile) -> Unit = onCompleteSignUp,
    onSkipForNow: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Mode switcher: true for Sign In, false for Register New Meter / Sign Up
    var isSignInMode by remember { mutableStateOf(initialSignInMode) }

    // Sign In form fields
    var signInIdentifier by remember {
        mutableStateOf(if (currentProfile.meterNumber.isNotBlank()) currentProfile.meterNumber else "01429583192")
    }
    var signInPin by remember { mutableStateOf("1234") }
    var isPinVisible by remember { mutableStateOf(false) }
    var signInErrorMessage by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }

    // Forgot PIN dialog state
    var showForgotPinDialog by remember { mutableStateOf(false) }
    var forgotPinOtpInput by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    // Register New Meter form fields
    var meterNumber by remember { mutableStateOf(currentProfile.meterNumber.ifBlank { "01429583192" }) }
    var customerName by remember { mutableStateOf(currentProfile.customerName.ifBlank { "Chuka Obunma" }) }
    var phoneNumber by remember { mutableStateOf(currentProfile.phoneNumber.ifBlank { "+234 803 892 4110" }) }
    var streetAddress by remember { mutableStateOf(currentProfile.streetAddress.ifBlank { "14 Adeola Odeku Street, Victoria Island" }) }
    var lga by remember { mutableStateOf(currentProfile.lga.ifBlank { "Eti-Osa" }) }
    var selectedState by remember { mutableStateOf(currentProfile.state.ifBlank { "Lagos State" }) }
    var selectedDisCo by remember { mutableStateOf(DisCo.fromCode(currentProfile.discoCode)) }
    var selectedBand by remember { mutableStateOf(currentProfile.feederBand) }
    var isPrepaid by remember { mutableStateOf(currentProfile.isPrepaid) }
    var transformerId by remember { mutableStateOf(currentProfile.transformerId.ifBlank { "TR-LOS-VI-04B" }) }
    var newAccountPin by remember { mutableStateOf("1234") }

    var isDisCoDropdownExpanded by remember { mutableStateOf(false) }
    var isBandDropdownExpanded by remember { mutableStateOf(false) }
    var isRegistrationSubmitted by remember { mutableStateOf(false) }

    // Optional STS Token simulator state
    var showStsSimulator by remember { mutableStateOf(false) }
    var generatedStsToken by remember { mutableStateOf("4819 2041 8932 1094 8201") }
    val clipboardManager = LocalClipboardManager.current

    val currentConstructedProfile = UserProfile(
        meterNumber = meterNumber.ifBlank { "01429583192" },
        customerName = customerName.ifBlank { "Resident User" },
        phoneNumber = phoneNumber.ifBlank { "+234 800 000 0000" },
        streetAddress = streetAddress.ifBlank { "Residential Address" },
        lga = lga.ifBlank { "LGA" },
        state = selectedState.ifBlank { "Lagos State" },
        discoCode = selectedDisCo.code,
        feederName = "${selectedDisCo.code} 33kV Injection Feeder",
        feederBand = selectedBand,
        transformerId = transformerId.ifBlank { "TR-AUTO-01" },
        isPrepaid = isPrepaid,
        connectedHouseholdsCount = 184,
        isOnboarded = true
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
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top App Bar with optional Dismiss (X) button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(GoldPrimary.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Bright Power Logo",
                            tint = GoldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "THE BRIGHT PROJECT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                fontSize = 10.sp
                            ),
                            color = GoldPrimary
                        )
                        Text(
                            text = if (isSignInMode) "Resident Sign In" else "Meter Registration",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                if (isDismissible) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .testTag("dismiss_auth_screen_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close and return to dashboard",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Mode Selector Tabs: [ Sign In ] vs [ Register New Meter ]
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_mode_selector"),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Sign In Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSignInMode) GoldPrimary else Color.Transparent)
                            .clickable {
                                isSignInMode = true
                                signInErrorMessage = null
                            }
                            .padding(vertical = 10.dp)
                            .testTag("tab_sign_in"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isSignInMode) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Sign In",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = if (isSignInMode) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Register Meter Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isSignInMode) GoldPrimary else Color.Transparent)
                            .clickable {
                                isSignInMode = false
                                signInErrorMessage = null
                            }
                            .padding(vertical = 10.dp)
                            .testTag("tab_register_meter"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricMeter,
                                contentDescription = null,
                                tint = if (!isSignInMode) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Register Meter",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = if (!isSignInMode) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // MODE 1: SIGN IN (Fast, direct, frictionless)
            // ==========================================
            if (isSignInMode) {
                // Sign In Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sign_in_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Sign In to Your Account",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Access live feeder monitoring, tariff SLAs, and automated billing disputes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Meter Number or Phone Input
                        OutlinedTextField(
                            value = signInIdentifier,
                            onValueChange = {
                                signInIdentifier = it
                                signInErrorMessage = null
                            },
                            label = { Text("Meter Number or Registered Phone") },
                            placeholder = { Text("e.g. 01429583192 or +234...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ElectricMeter,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (signInIdentifier.isNotBlank()) {
                                    IconButton(onClick = { signInIdentifier = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear identifier",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sign_in_identifier_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        // 4-Digit Security PIN
                        OutlinedTextField(
                            value = signInPin,
                            onValueChange = {
                                if (it.length <= 8) {
                                    signInPin = it
                                    signInErrorMessage = null
                                }
                            },
                            label = { Text("4-Digit Security PIN") },
                            placeholder = { Text("1234") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                    Icon(
                                        imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isPinVisible) "Hide PIN" else "Show PIN",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sign_in_pin_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        // Error message banner if any
                        signInErrorMessage?.let { err ->
                            Text(
                                text = err,
                                color = Color(0xFFEF4444),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        // Forgot PIN link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Forgot PIN? Reset via SMS OTP",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = GoldPrimary,
                                modifier = Modifier
                                    .clickable { showForgotPinDialog = true }
                                    .testTag("forgot_pin_button")
                            )
                        }

                        // Main Sign In Action Button
                        Button(
                            onClick = {
                                if (signInIdentifier.isBlank()) {
                                    signInErrorMessage = "Please enter your meter number or phone number"
                                    return@Button
                                }
                                isAuthenticating = true
                                val matched = PRESET_ACCOUNTS.find {
                                    it.profile.meterNumber == signInIdentifier.trim() ||
                                            it.profile.phoneNumber == signInIdentifier.trim()
                                }
                                val profileToSignIn = matched?.profile ?: currentProfile.copy(
                                    meterNumber = signInIdentifier.trim(),
                                    isOnboarded = true
                                )
                                onSignIn(profileToSignIn)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_sign_in_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isAuthenticating) {
                                CircularProgressIndicator(
                                    color = Color.Black,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Signing In...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SIGN IN TO DASHBOARD",
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Biometric Quick Unlock Option
                        OutlinedButton(
                            onClick = {
                                onSignIn(currentProfile.copy(isOnboarded = true))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("biometric_sign_in_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = EmeraldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instant Biometric / Fingerprint Unlock",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldAccent
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // One-Tap Verified Resident Accounts Switcher
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ONE-TAP VERIFIED ACCOUNTS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Switch Instantly",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = GoldPrimary
                        )
                    }

                    PRESET_ACCOUNTS.forEach { acc ->
                        val isCurrent = acc.profile.meterNumber == currentProfile.meterNumber
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    signInIdentifier = acc.profile.meterNumber
                                    signInPin = "1234"
                                    onSignIn(acc.profile)
                                }
                                .testTag("preset_account_${acc.profile.meterNumber}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCurrent) GoldPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(acc.badgeColor.copy(alpha = 0.15f))
                                            .border(1.dp, acc.badgeColor.copy(alpha = 0.5f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = acc.profile.discoCode.take(2),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = acc.badgeColor
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = acc.profile.customerName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isCurrent) {
                                                Surface(
                                                    color = GoldPrimary.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(100.dp)
                                                ) {
                                                    Text(
                                                        text = "ACTIVE",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = GoldPrimary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Meter #${acc.profile.meterNumber} • ${acc.profile.discoCode}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            ),
                                            color = GoldPrimary
                                        )
                                        Text(
                                            text = acc.subtitle,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        onSignIn(acc.profile)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCurrent) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isCurrent) Color.Black else MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("login_account_${acc.profile.meterNumber}")
                                ) {
                                    Text(
                                        text = if (isCurrent) "Resume" else "Sign In",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // MODE 2: REGISTER NEW METER (Not floppy!)
            // ==========================================
            if (!isSignInMode) {
                // Section 1: Meter & DisCo Setup
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_meter_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                text = "1. Smart Meter & Utility Feeder",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Meter Number Field
                        OutlinedTextField(
                            value = meterNumber,
                            onValueChange = { meterNumber = it },
                            label = { Text("11-Digit Meter Number") },
                            placeholder = { Text("e.g. 01429583192") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_meter_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary
                            )
                        )

                        // DisCo and Feeder Band Selectors
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // DisCo Dropdown
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
                                        .testTag("disco_dropdown_select"),
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

                            // Feeder Band Dropdown
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
                                        .testTag("band_dropdown_select"),
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

                        // Tariff Type (Prepaid vs Postpaid)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isPrepaid) GoldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .border(1.dp, if (isPrepaid) GoldPrimary else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { isPrepaid = true }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Prepaid Residential",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPrepaid) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (!isPrepaid) GoldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .border(1.dp, if (!isPrepaid) GoldPrimary else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { isPrepaid = false }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Postpaid Account",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isPrepaid) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Resident Identity & Location
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_identity_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "2. Resident Profile & Security PIN",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Full Name
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Full Legal / Resident Name") },
                            placeholder = { Text("e.g. Chuka Obunma") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_customer_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                        )

                        // Phone Number
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Mobile Phone (+234 SIM)") },
                            placeholder = { Text("+234 803 892 4110") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_phone_number_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                        )

                        // Street Address & LGA
                        OutlinedTextField(
                            value = streetAddress,
                            onValueChange = { streetAddress = it },
                            label = { Text("Street Address") },
                            placeholder = { Text("e.g. 14 Adeola Odeku Street, Victoria Island") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_street_address_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = lga,
                                onValueChange = { lga = it },
                                label = { Text("LGA") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                            )
                            OutlinedTextField(
                                value = selectedState,
                                onValueChange = { selectedState = it },
                                label = { Text("State") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                            )
                        }

                        // Security PIN for future sign-ins
                        OutlinedTextField(
                            value = newAccountPin,
                            onValueChange = { if (it.length <= 6) newAccountPin = it },
                            label = { Text("Create 4-Digit PIN for Sign In") },
                            placeholder = { Text("1234") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_account_pin_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Auto-Fill sample button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
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
                            newAccountPin = "1234"
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("autofill_sample_resident")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Auto-Fill Sample Resident", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Direct Clean Registration & Entry (No floppy payment blocks!)
                Button(
                    onClick = {
                        isRegistrationSubmitted = true
                        onCompleteSignUp(currentConstructedProfile)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("register_and_enter_dashboard_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REGISTER METER & ENTER BRIGHT",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Optional STS Token Simulator accordion
                OutlinedButton(
                    onClick = { showStsSimulator = !showStsSimulator },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("toggle_sts_simulator_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (showStsSimulator) "Hide ₦500 STS Vending Simulator" else "Optional: Test ₦500 STS Vending Simulator",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                AnimatedVisibility(visible = showStsSimulator) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .testTag("sts_simulator_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "₦500 TEST STS TOKEN VENDING",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = GoldPrimary
                            )
                            Text(
                                text = "Simulated STS IEC 62055-41 20-digit prepaid activation token:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = generatedStsToken,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp
                                        ),
                                        color = EmeraldAccent
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(generatedStsToken))
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy token",
                                            tint = EmeraldAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    onCompleteSignUp(currentConstructedProfile)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Use Token & Launch Dashboard", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Forgot PIN Dialog
    if (showForgotPinDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showForgotPinDialog = false },
            title = {
                Text(
                    text = "Reset PIN via SMS OTP",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "We will dispatch a 6-digit one-time code to your registered mobile SIM line:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (!isOtpSent) {
                        Button(
                            onClick = {
                                isOtpSent = true
                                forgotPinOtpInput = "849201"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Send OTP to Phone", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedTextField(
                            value = forgotPinOtpInput,
                            onValueChange = { forgotPinOtpInput = it },
                            label = { Text("Enter 6-Digit OTP Code") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "✓ Simulated Code Dispatched: 849201",
                            color = EmeraldAccent,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showForgotPinDialog = false
                        signInPin = "1234"
                        onSignIn(currentProfile.copy(isOnboarded = true))
                    },
                    enabled = isOtpSent,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Text("Verify & Sign In", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showForgotPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
