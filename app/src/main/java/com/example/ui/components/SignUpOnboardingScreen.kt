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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.payment.ActivationPaymentState
import com.example.data.security.PinResetCheck
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
    /** Forgot PIN step 1: checks the registered meter number and full name. */
    onCheckPinResetDetails: (meterNumber: String, fullName: String) -> PinResetCheck = { _, _ -> PinResetCheck.NoAccount },
    /** Forgot PIN step 2: saves the new PIN after a successful check. */
    onResetPin: (String) -> Boolean = { false },
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
        // Forgot PIN: confirm the registered meter number and full name, then choose a new PIN.
        // Nothing is deleted. Delete account stays available as the last resort.
        ForgotPinDialog(
            onCheckDetails = onCheckPinResetDetails,
            onSaveNewPin = onResetPin,
            onDismiss = { showForgotPinDialog = false },
            onDeleteAccount = if (isPinSet) {
                {
                    showForgotPinDialog = false
                    showDeleteAccountDialog = true
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
