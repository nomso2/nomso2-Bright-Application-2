package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.payment.ActivationPaymentState
import com.example.model.UserProfile

/**
 * Sign in, or register a meter step by step:
 *   1. Meter & name details  2. Create a PIN (mandatory)  3. One-time ₦1,000 activation  4. Done.
 *
 * The pieces live in SignUpSignInForm.kt, SignUpRegistrationFlow.kt, SignUpActivationStep.kt and
 * SignUpComponents.kt. Payment goes through the ViewModel's ActivationPaymentGateway.
 */
@Composable
fun SignUpOnboardingScreen(
    currentProfile: UserProfile = UserProfile(),
    initialSignInMode: Boolean = true,
    isDismissible: Boolean = false,
    paidMeters: Set<String> = emptySet(),
    paymentState: ActivationPaymentState = ActivationPaymentState.Idle,
    onStartPayment: (meter: String) -> Unit = {},
    onResetPayment: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onCompleteSignUp: (profile: UserProfile, newPin: String?) -> Unit,
    onSignIn: (UserProfile) -> Unit = {},
    isPinSet: Boolean = false,
    verifyPin: (String) -> Boolean = { false },
    onDeleteAccount: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isSignInMode by remember { mutableStateOf(initialSignInMode) }
    var registrationStep by remember { mutableStateOf(SignUpStep.DETAILS) }
    var showForgotPinDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var registrationGeneration by remember { mutableStateOf(0) }

    // The Sign in / Register switch is only offered before registration gets going,
    // so each later step shows just its own content and one primary button.
    val showModeSwitch = isSignInMode || registrationStep == SignUpStep.DETAILS

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SignUpHeader(
                title = if (isSignInMode) "Sign in" else "Register your meter",
                isDismissible = isDismissible,
                onDismiss = onDismiss
            )

            if (showModeSwitch) {
                AuthModeSwitch(
                    isSignInMode = isSignInMode,
                    onSelectSignIn = { isSignInMode = true },
                    onSelectRegister = {
                        isSignInMode = false
                        registrationStep = SignUpStep.DETAILS
                    }
                )
            }

            if (isSignInMode) {
                SignInForm(
                    currentProfile = currentProfile,
                    isPinSet = isPinSet,
                    paidMeters = paidMeters,
                    verifyPin = verifyPin,
                    onSignIn = onSignIn,
                    onGoToRegister = {
                        isSignInMode = false
                        registrationStep = SignUpStep.DETAILS
                    },
                    onForgotPin = { showForgotPinDialog = true }
                )
            } else {
                // Keyed so the flow starts fresh (empty form, step 1) after an account is deleted.
                key(registrationGeneration) {
                    RegistrationFlow(
                        currentProfile = currentProfile,
                        // Prefill personal details only from this phone's own account, and never
                        // after it has just been deleted.
                        prefillProfile = if (isPinSet && registrationGeneration == 0) currentProfile else null,
                        isPinSet = isPinSet,
                        paidMeters = paidMeters,
                        paymentState = paymentState,
                        verifyPin = verifyPin,
                        onStartPayment = onStartPayment,
                        onResetPayment = onResetPayment,
                        onCompleteSignUp = onCompleteSignUp,
                        onForgotPin = { showForgotPinDialog = true },
                        onStepChanged = { registrationStep = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showForgotPinDialog) {
        // PIN reset by SMS needs a real OTP backend. Until one exists, the only way back in is
        // deleting this phone's account (which wipes its data) and registering again.
        AlertDialog(
            onDismissRequest = { showForgotPinDialog = false },
            modifier = Modifier.testTag("forgot_pin_dialog"),
            title = {
                Text(
                    text = "Forgot your PIN?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "We can't reset PINs by SMS yet. To start again, delete the account on this phone and register your meter again. Your meter stays activated, so you won't pay twice.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { showForgotPinDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = if (isPinSet) {
                {
                    TextButton(
                        onClick = {
                            showForgotPinDialog = false
                            showDeleteAccountDialog = true
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("forgot_pin_delete_account_button")
                    ) {
                        Text("Delete account")
                    }
                }
            } else {
                null
            }
        )
    }

    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            meterNumber = currentProfile.meterNumber,
            isPinSet = isPinSet,
            verifyPin = verifyPin,
            onConfirmDelete = {
                showDeleteAccountDialog = false
                isSignInMode = false
                registrationStep = SignUpStep.DETAILS
                registrationGeneration += 1
                onDeleteAccount()
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }
}
