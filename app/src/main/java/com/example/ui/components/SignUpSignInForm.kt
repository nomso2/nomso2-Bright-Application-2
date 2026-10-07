package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.UserProfile
import com.example.ui.security.BiometricAuthenticator
import com.example.ui.security.findFragmentActivity

/** Keeps only digits, so "+234 803 892 4110" and "08038924110" compare sensibly. */
private fun digitsOnly(value: String): String = value.filter { it.isDigit() }

/** True when [identifier] is this phone's meter number or registered phone number. */
private fun matchesAccount(identifier: String, profile: UserProfile): Boolean {
    val id = digitsOnly(identifier)
    if (id.isEmpty()) return false
    val meter = digitsOnly(profile.meterNumber)
    val phone = digitsOnly(profile.phoneNumber)
    if (id == meter) return true
    // Accept 0803... and 234803... forms of the same phone number.
    return phone.isNotEmpty() && (id == phone || id.takeLast(10) == phone.takeLast(10))
}

/**
 * Sign in to the account already on this phone. Bright keeps accounts on the phone (there is
 * no server yet), so sign-in always checks the PIN stored here. With no account on the phone,
 * it points the user to registration instead.
 */
@Composable
internal fun SignInForm(
    currentProfile: UserProfile,
    isPinSet: Boolean,
    paidMeters: Set<String>,
    verifyPin: (String) -> Boolean,
    onSignIn: (UserProfile) -> Unit,
    onGoToRegister: () -> Unit,
    onForgotPin: () -> Unit
) {
    if (!isPinSet) {
        StepCard(
            title = "No account on this phone yet",
            body = "Register your meter to get started. If your meter is already activated, you won't pay again.",
            testTag = "sign_in_card"
        ) {
            PrimaryStepButton(
                text = "Register my meter",
                onClick = onGoToRegister,
                testTag = "sign_in_go_to_register_button"
            )
        }
        return
    }

    var identifier by remember { mutableStateOf(currentProfile.meterNumber) }
    var pin by remember { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val canUseBiometrics = remember(context) { BiometricAuthenticator.canAuthenticate(context) }

    fun finishSignIn() {
        if (!paidMeters.contains(currentProfile.meterNumber.trim())) {
            errorMessage = "This meter's activation isn't finished. Choose \"Register a meter\" to complete it."
            return
        }
        onSignIn(currentProfile.copy(isOnboarded = true))
    }

    fun submit() {
        when {
            identifier.isBlank() -> errorMessage = "Enter your meter number or phone number."
            !matchesAccount(identifier, currentProfile) ->
                errorMessage = "That number isn't registered on this phone. Check it, or register it as a new meter."
            pin.isBlank() -> errorMessage = "Enter your PIN."
            !verifyPin(pin) -> {
                errorMessage = "That PIN isn't right. Try again."
                pin = ""
            }
            else -> finishSignIn()
        }
    }

    StepCard(
        title = "Welcome back",
        body = "Sign in with your meter number (or phone number) and your PIN.",
        testTag = "sign_in_card"
    ) {
        OutlinedTextField(
            value = identifier,
            onValueChange = {
                identifier = it
                errorMessage = null
            },
            label = { Text("Meter number or phone number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sign_in_identifier_input")
        )

        PinTextField(
            value = pin,
            onValueChange = {
                pin = it
                errorMessage = null
            },
            label = "PIN",
            testTag = "sign_in_pin_input",
            isVisible = isPinVisible,
            onToggleVisible = { isPinVisible = !isPinVisible },
            // Older accounts may have PINs of up to 8 digits.
            maxLength = 8
        )

        errorMessage?.let { StepNotice(text = it, isError = true, testTag = "sign_in_error") }

        PrimaryStepButton(
            text = "Sign in",
            onClick = { submit() },
            testTag = "submit_sign_in_button"
        )

        // Real BiometricPrompt, only for the account already on this phone.
        if (canUseBiometrics && activity != null) {
            OutlinedButton(
                onClick = {
                    BiometricAuthenticator.authenticate(
                        activity = activity,
                        title = "Sign in to Bright",
                        subtitle = "Confirm it's you to open meter #${currentProfile.meterNumber}",
                        onSuccess = { finishSignIn() },
                        onError = { message -> errorMessage = message }
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("biometric_sign_in_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Use fingerprint or face", style = MaterialTheme.typography.labelLarge)
            }
        }

        TextButton(
            onClick = onForgotPin,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("forgot_pin_button")
        ) {
            Text("Forgot your PIN?", style = MaterialTheme.typography.labelLarge)
        }
    }
}
