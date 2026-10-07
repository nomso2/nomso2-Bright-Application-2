package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.payment.ActivationPaymentState
import com.example.model.DisCo
import com.example.model.FeederBand
import com.example.model.UserProfile

/** Nigerian prepaid/postpaid meter numbers are 11 or 13 digits. */
private val METER_NUMBER_LENGTHS = 11..13

/** Form values for the registration flow, kept together so each step stays small. */
@Stable
internal class RegistrationFormState(prefill: UserProfile?) {
    var meterNumber by mutableStateOf("")
    var customerName by mutableStateOf(prefill?.customerName.orEmpty())
    var phoneNumber by mutableStateOf(prefill?.phoneNumber.orEmpty())
    var streetAddress by mutableStateOf(prefill?.streetAddress.orEmpty())
    var lga by mutableStateOf(prefill?.lga.orEmpty())
    var state by mutableStateOf(prefill?.state.orEmpty())
    var disCo by mutableStateOf(prefill?.let { DisCo.fromCode(it.discoCode) } ?: DisCo.EKEDC)
    var band by mutableStateOf(prefill?.feederBand ?: FeederBand.BAND_A)
    var isPrepaid by mutableStateOf(prefill?.isPrepaid ?: true)

    var newPin by mutableStateOf("")
    var confirmPin by mutableStateOf("")
    var existingPin by mutableStateOf("")

    val trimmedMeter: String get() = meterNumber.trim()

    val meterError: String?
        get() = when {
            trimmedMeter.isEmpty() -> "Enter your meter number."
            trimmedMeter.length !in METER_NUMBER_LENGTHS -> "Meter numbers are 11 or 13 digits."
            else -> null
        }
    val nameError: String?
        get() = if (customerName.isBlank()) "Enter your full name." else null
    val phoneError: String?
        get() = when {
            phoneNumber.isBlank() -> "Enter your phone number."
            phoneNumber.count { it.isDigit() } !in 10..14 -> "Enter a valid phone number, e.g. 0803 123 4567."
            else -> null
        }
    val addressError: String?
        get() = if (streetAddress.isBlank()) "Enter your street address." else null

    val isDetailsValid: Boolean
        get() = meterError == null && nameError == null && phoneError == null && addressError == null

    /** Builds the profile to save. Keeps device settings from [base] when it is the same meter. */
    fun toProfile(base: UserProfile): UserProfile {
        val start = if (base.meterNumber.trim() == trimmedMeter) base else UserProfile(transformerId = "TR-AUTO-01")
        return start.copy(
            meterNumber = trimmedMeter,
            customerName = customerName.trim(),
            phoneNumber = phoneNumber.trim(),
            streetAddress = streetAddress.trim(),
            lga = lga.trim(),
            state = state.trim(),
            discoCode = disCo.code,
            feederName = if (start === base) base.feederName else "${disCo.code} 33kV Injection Feeder",
            feederBand = band,
            isPrepaid = isPrepaid,
            isOnboarded = true,
            isGatewayPaid = true
        )
    }
}

/**
 * Register a meter in four steps: details, PIN, activation, done.
 * Each step has one primary button; "Back" (and system back) returns to the previous step.
 */
@Composable
internal fun RegistrationFlow(
    currentProfile: UserProfile,
    prefillProfile: UserProfile?,
    isPinSet: Boolean,
    paidMeters: Set<String>,
    paymentState: ActivationPaymentState,
    verifyPin: (String) -> Boolean,
    onStartPayment: (meter: String) -> Unit,
    onResetPayment: () -> Unit,
    onCompleteSignUp: (profile: UserProfile, newPin: String?) -> Unit,
    onForgotPin: () -> Unit,
    onStepChanged: (SignUpStep) -> Unit
) {
    val form = remember { RegistrationFormState(prefill = prefillProfile) }
    var step by remember { mutableStateOf(SignUpStep.DETAILS) }

    fun goTo(next: SignUpStep) {
        step = next
        onStepChanged(next)
    }

    val isProcessingPayment = paymentState is ActivationPaymentState.InProgress
    val meter = form.trimmedMeter
    val isMeterPaid = paidMeters.contains(meter) ||
        (paymentState is ActivationPaymentState.Succeeded && paymentState.meter == meter)

    // A confirmed payment for this meter moves straight on to "Done".
    LaunchedEffect(paymentState) {
        val state = paymentState
        if (step == SignUpStep.PAYMENT && state is ActivationPaymentState.Succeeded && state.meter == form.trimmedMeter) {
            goTo(SignUpStep.DONE)
        }
    }

    BackHandler(enabled = step == SignUpStep.PIN || (step == SignUpStep.PAYMENT && !isProcessingPayment)) {
        goTo(if (step == SignUpStep.PAYMENT) SignUpStep.PIN else SignUpStep.DETAILS)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SignUpStepIndicator(current = step)

        when (step) {
            SignUpStep.DETAILS -> DetailsStep(
                form = form,
                onContinue = {
                    onResetPayment()
                    goTo(SignUpStep.PIN)
                }
            )

            SignUpStep.PIN -> PinStep(
                form = form,
                isPinSet = isPinSet,
                verifyPin = verifyPin,
                onForgotPin = onForgotPin,
                onContinue = { goTo(SignUpStep.PAYMENT) },
                onBack = { goTo(SignUpStep.DETAILS) }
            )

            SignUpStep.PAYMENT -> ActivationStep(
                meterNumber = meter,
                isMeterAlreadyPaid = paidMeters.contains(meter),
                paymentState = if (paymentState.meterOrNull() == meter) paymentState else ActivationPaymentState.Idle,
                onStartPayment = { onStartPayment(meter) },
                onContinue = { goTo(SignUpStep.DONE) },
                onBack = { goTo(SignUpStep.PIN) }
            )

            SignUpStep.DONE -> DoneStep(
                meterNumber = meter,
                canFinish = isMeterPaid,
                onFinish = {
                    onCompleteSignUp(
                        form.toProfile(currentProfile),
                        if (isPinSet) null else form.newPin
                    )
                }
            )
        }
    }
}

private fun ActivationPaymentState.meterOrNull(): String? = when (this) {
    is ActivationPaymentState.Idle -> null
    is ActivationPaymentState.InProgress -> meter
    is ActivationPaymentState.Succeeded -> meter
    is ActivationPaymentState.Failed -> meter
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsStep(
    form: RegistrationFormState,
    onContinue: () -> Unit
) {
    var showErrors by remember { mutableStateOf(false) }
    var isDisCoMenuOpen by remember { mutableStateOf(false) }
    var isBandMenuOpen by remember { mutableStateOf(false) }

    StepCard(
        title = "Your meter and details",
        body = "We use these to match your reports to your meter and your area.",
        testTag = "register_meter_card"
    ) {
        RegistrationTextField(
            value = form.meterNumber,
            onValueChange = { input -> form.meterNumber = input.filter { it.isDigit() }.take(13) },
            label = "Meter number",
            helper = "You'll find it on your meter or a recent token receipt.",
            error = if (showErrors) form.meterError else null,
            keyboardType = KeyboardType.Number,
            testTag = "new_meter_input"
        )
        RegistrationTextField(
            value = form.customerName,
            onValueChange = { form.customerName = it },
            label = "Full name",
            error = if (showErrors) form.nameError else null,
            capitalization = KeyboardCapitalization.Words,
            testTag = "new_customer_name_input"
        )
        RegistrationTextField(
            value = form.phoneNumber,
            onValueChange = { form.phoneNumber = it },
            label = "Phone number",
            error = if (showErrors) form.phoneError else null,
            keyboardType = KeyboardType.Phone,
            testTag = "new_phone_number_input"
        )

        ExposedDropdownMenuBox(
            expanded = isDisCoMenuOpen,
            onExpandedChange = { isDisCoMenuOpen = it }
        ) {
            OutlinedTextField(
                value = "${form.disCo.code} – ${form.disCo.fullName}",
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = { Text("Electricity company") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDisCoMenuOpen) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .testTag("disco_dropdown_select")
            )
            ExposedDropdownMenu(
                expanded = isDisCoMenuOpen,
                onDismissRequest = { isDisCoMenuOpen = false }
            ) {
                DisCo.entries.forEach { disco ->
                    DropdownMenuItem(
                        text = { Text("${disco.code} – ${disco.fullName}") },
                        onClick = {
                            form.disCo = disco
                            isDisCoMenuOpen = false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = isBandMenuOpen,
            onExpandedChange = { isBandMenuOpen = it }
        ) {
            OutlinedTextField(
                value = "${form.band.code} (${form.band.description})",
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = { Text("Tariff band (shown on your bill)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBandMenuOpen) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .testTag("band_dropdown_select")
            )
            ExposedDropdownMenu(
                expanded = isBandMenuOpen,
                onDismissRequest = { isBandMenuOpen = false }
            ) {
                FeederBand.entries.forEach { band ->
                    DropdownMenuItem(
                        text = { Text("${band.code} (${band.description})") },
                        onClick = {
                            form.band = band
                            isBandMenuOpen = false
                        }
                    )
                }
            }
        }

        Text(
            text = "Meter type",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = form.isPrepaid,
                onClick = { form.isPrepaid = true },
                label = { Text("Prepaid") },
                modifier = Modifier.testTag("meter_type_prepaid")
            )
            FilterChip(
                selected = !form.isPrepaid,
                onClick = { form.isPrepaid = false },
                label = { Text("Postpaid") },
                modifier = Modifier.testTag("meter_type_postpaid")
            )
        }

        RegistrationTextField(
            value = form.streetAddress,
            onValueChange = { form.streetAddress = it },
            label = "Street address",
            error = if (showErrors) form.addressError else null,
            capitalization = KeyboardCapitalization.Words,
            testTag = "new_street_address_input"
        )
        RegistrationTextField(
            value = form.lga,
            onValueChange = { form.lga = it },
            label = "Local government area (optional)",
            capitalization = KeyboardCapitalization.Words,
            testTag = "new_lga_input"
        )
        RegistrationTextField(
            value = form.state,
            onValueChange = { form.state = it },
            label = "State (optional)",
            capitalization = KeyboardCapitalization.Words,
            testTag = "new_state_input"
        )

        PrimaryStepButton(
            text = "Continue",
            onClick = {
                showErrors = true
                if (form.isDetailsValid) onContinue()
            },
            testTag = "signup_details_continue_button"
        )
    }
}

@Composable
private fun PinStep(
    form: RegistrationFormState,
    isPinSet: Boolean,
    verifyPin: (String) -> Boolean,
    onForgotPin: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var confirmErrorText by remember { mutableStateOf<String?>(null) }

    if (isPinSet) {
        // This phone already has an account: registering another meter needs its PIN, so a
        // logged-out phone can't be re-registered by someone else to get at the saved data.
        StepCard(
            title = "Enter your PIN",
            body = "This phone already has a Bright account. Enter its PIN to register this meter.",
            testTag = "signup_pin_card"
        ) {
            PinTextField(
                value = form.existingPin,
                onValueChange = {
                    form.existingPin = it
                    errorText = null
                },
                label = "PIN",
                testTag = "signup_existing_pin_input",
                isVisible = isVisible,
                onToggleVisible = { isVisible = !isVisible },
                maxLength = 8,
                errorText = errorText
            )
            PrimaryStepButton(
                text = "Continue",
                onClick = {
                    if (verifyPin(form.existingPin)) {
                        form.existingPin = ""
                        onContinue()
                    } else {
                        errorText = "That PIN isn't right. Try again."
                        form.existingPin = ""
                    }
                },
                enabled = form.existingPin.isNotEmpty(),
                testTag = "signup_pin_continue_button"
            )
            TextButton(
                onClick = onForgotPin,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_forgot_pin_button")
            ) {
                Text("Forgot your PIN?", style = MaterialTheme.typography.labelLarge)
            }
            StepBackButton(onClick = onBack)
        }
        return
    }

    StepCard(
        title = "Create a PIN",
        body = "You'll use this PIN to open Bright and sign in on this phone. Choose $SIGN_UP_PIN_MIN to $SIGN_UP_PIN_MAX digits that others can't guess.",
        testTag = "signup_pin_card"
    ) {
        PinTextField(
            value = form.newPin,
            onValueChange = {
                form.newPin = it
                errorText = null
            },
            label = "New PIN",
            testTag = "new_account_pin_input",
            isVisible = isVisible,
            onToggleVisible = { isVisible = !isVisible },
            errorText = errorText,
            helperText = "$SIGN_UP_PIN_MIN to $SIGN_UP_PIN_MAX digits"
        )
        PinTextField(
            value = form.confirmPin,
            onValueChange = {
                form.confirmPin = it
                confirmErrorText = null
            },
            label = "Confirm PIN",
            testTag = "new_account_pin_confirm_input",
            isVisible = isVisible,
            onToggleVisible = { isVisible = !isVisible },
            errorText = confirmErrorText
        )
        PrimaryStepButton(
            text = "Continue",
            onClick = {
                when {
                    !isValidSignUpPin(form.newPin) ->
                        errorText = "Your PIN needs $SIGN_UP_PIN_MIN to $SIGN_UP_PIN_MAX digits."
                    form.newPin != form.confirmPin ->
                        confirmErrorText = "The PINs don't match. Type the same PIN twice."
                    else -> onContinue()
                }
            },
            testTag = "signup_pin_continue_button"
        )
        StepBackButton(onClick = onBack)
    }
}

@Composable
private fun DoneStep(
    meterNumber: String,
    canFinish: Boolean,
    onFinish: () -> Unit
) {
    StepCard(
        title = "You're all set",
        body = "Meter #$meterNumber is activated for life, and your PIN protects Bright on this phone.",
        testTag = "signup_done_card"
    ) {
        ActivatedBadge(text = "Meter activated")
        PrimaryStepButton(
            text = "Go to my dashboard",
            onClick = onFinish,
            enabled = canFinish,
            testTag = "signup_finish_button"
        )
    }
}

@Composable
private fun RegistrationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    testTag: String,
    helper: String? = null,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None
) {
    val supportingMessage: String? = error ?: helper
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        supportingText = if (supportingMessage != null) {
            { Text(supportingMessage) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, capitalization = capitalization),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}
