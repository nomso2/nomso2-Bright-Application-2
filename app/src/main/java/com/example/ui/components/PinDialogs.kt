package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.security.PinResetCheck

/*
 * PIN dialogs written for older users: one question per step, plain words, body text 16sp or
 * larger and 56dp buttons. Nothing here deletes data.
 */

/** Calm wording for each forgot-PIN outcome that isn't a success. */
internal fun pinResetMessage(result: PinResetCheck): String? = when (result) {
    is PinResetCheck.Verified -> null
    is PinResetCheck.Wrong ->
        "Those details don't match the account on this phone. Please check your meter number " +
            "and your full name as you registered them. " +
            if (result.triesLeft == 1) "You have 1 try left." else "You have ${result.triesLeft} tries left."
    is PinResetCheck.LockedOut ->
        "Let's take a short break. For your safety, you can try again in " +
            (if (result.minutesLeft == 1) "1 minute" else "${result.minutesLeft} minutes") +
            ". Your account and your data are safe."
    is PinResetCheck.NoAccount ->
        "There is no account on this phone yet, so there is no PIN to reset. You can register your meter instead."
}

/** Shared frame: a scrollable card with a big title. */
@Composable
private fun PinDialogFrame(
    title: String,
    testTag: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                content()
            }
        }
    }
}

@Composable
private fun PinDialogBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun PinDialogMessage(text: String, isError: Boolean, testTag: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
            color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
private fun PinDialogPrimaryButton(text: String, testTag: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .testTag(testTag)
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
private fun PinDialogSecondaryButton(text: String, testTag: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .testTag(testTag)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

private enum class ForgotPinStep { DETAILS, NEW_PIN, DONE }

/**
 * Forgot PIN without losing data: the user confirms the meter number and full name they
 * registered with, then chooses a new PIN. Five wrong tries start a 15-minute break (counted
 * by the caller). Delete account stays available as a last resort.
 *
 * @param onCheckDetails checks meter number + full name; see [PinResetCheck].
 * @param onSaveNewPin saves the new PIN after a successful check; true when saved.
 * @param onDeleteAccount opens the Delete account confirmation, or null to hide that link.
 */
@Composable
fun ForgotPinDialog(
    onCheckDetails: (meterNumber: String, fullName: String) -> PinResetCheck,
    onSaveNewPin: (String) -> Boolean,
    onDismiss: () -> Unit,
    onDeleteAccount: (() -> Unit)? = null,
    prefillMeterNumber: String = ""
) {
    var step by remember { mutableStateOf(ForgotPinStep.DETAILS) }
    var meterNumber by remember { mutableStateOf(prefillMeterNumber) }
    var fullName by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    PinDialogFrame(
        title = when (step) {
            ForgotPinStep.DETAILS -> "Forgot your PIN?"
            ForgotPinStep.NEW_PIN -> "Choose a new PIN"
            ForgotPinStep.DONE -> "Your new PIN is saved"
        },
        testTag = "forgot_pin_dialog",
        onDismiss = onDismiss
    ) {
        when (step) {
            ForgotPinStep.DETAILS -> {
                PinDialogBody(
                    "No problem. Type the meter number and the full name you registered with. " +
                        "Your reports and history will stay on this phone."
                )
                OutlinedTextField(
                    value = meterNumber,
                    onValueChange = {
                        meterNumber = it.filter { c -> c.isDigit() }
                        message = null
                    },
                    label = { Text("Meter number", fontSize = 16.sp) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("forgot_pin_meter_input")
                )
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        message = null
                    },
                    label = { Text("Full name", fontSize = 16.sp) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("forgot_pin_name_input")
                )
                message?.let { PinDialogMessage(it, isError = true, testTag = "forgot_pin_message") }
                PinDialogPrimaryButton("Check my details", "forgot_pin_check_button") {
                    if (meterNumber.isBlank() || fullName.isBlank()) {
                        message = "Please type both your meter number and your full name."
                        return@PinDialogPrimaryButton
                    }
                    val result = onCheckDetails(meterNumber, fullName)
                    if (result is PinResetCheck.Verified) {
                        message = null
                        step = ForgotPinStep.NEW_PIN
                    } else {
                        message = pinResetMessage(result)
                    }
                }
                PinDialogSecondaryButton("Cancel", "forgot_pin_cancel_button", onDismiss)
                if (onDeleteAccount != null) {
                    TextButton(
                        onClick = onDeleteAccount,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("forgot_pin_delete_account_button")
                    ) {
                        Text("Still can't get in? Delete account", fontSize = 16.sp)
                    }
                }
            }

            ForgotPinStep.NEW_PIN -> {
                PinDialogBody("Thank you, that's you. Choose a new PIN of 4 to 6 numbers, then type it again.")
                PinTextField(
                    value = newPin,
                    onValueChange = {
                        newPin = it
                        message = null
                    },
                    label = "New PIN",
                    testTag = "forgot_pin_new_input",
                    isVisible = isPinVisible,
                    onToggleVisible = { isPinVisible = !isPinVisible }
                )
                PinTextField(
                    value = confirmPin,
                    onValueChange = {
                        confirmPin = it
                        message = null
                    },
                    label = "Type the new PIN again",
                    testTag = "forgot_pin_confirm_input",
                    isVisible = isPinVisible,
                    onToggleVisible = { isPinVisible = !isPinVisible }
                )
                message?.let { PinDialogMessage(it, isError = true, testTag = "forgot_pin_message") }
                PinDialogPrimaryButton("Save my new PIN", "forgot_pin_save_button") {
                    when {
                        !isValidSignUpPin(newPin) -> message = "Your PIN needs 4 to 6 numbers."
                        newPin != confirmPin -> message = "The two PINs are different. Please type them again."
                        onSaveNewPin(newPin) -> {
                            message = null
                            step = ForgotPinStep.DONE
                        }
                        else -> {
                            // The check expired (took over 10 minutes): start again calmly.
                            message = "That took a little long, so please check your details once more."
                            newPin = ""
                            confirmPin = ""
                            step = ForgotPinStep.DETAILS
                        }
                    }
                }
                PinDialogSecondaryButton("Cancel", "forgot_pin_cancel_button", onDismiss)
            }

            ForgotPinStep.DONE -> {
                PinDialogMessage(
                    "All done. Your reports and history are safe. Use your new PIN from now on.",
                    isError = false,
                    testTag = "forgot_pin_done_message"
                )
                PinDialogPrimaryButton("OK", "forgot_pin_done_button", onDismiss)
            }
        }
    }
}

private enum class ChangePinStep { CURRENT, NEW_PIN, CONFIRM, DONE }

/**
 * Change PIN, one question per screen: current PIN, then new PIN (4 to 6 digits), then the new
 * PIN again.
 *
 * @param verifyPin checks the current PIN.
 * @param onChangePin saves the new PIN (re-checking the current one); true when saved.
 * @param onForgotPin optional link for users who don't remember their current PIN.
 */
@Composable
fun ChangePinDialog(
    verifyPin: (String) -> Boolean,
    onChangePin: (currentPin: String, newPin: String) -> Boolean,
    onDismiss: () -> Unit,
    onForgotPin: (() -> Unit)? = null
) {
    var step by remember { mutableStateOf(ChangePinStep.CURRENT) }
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    PinDialogFrame(
        title = when (step) {
            ChangePinStep.CURRENT -> "Change your PIN"
            ChangePinStep.NEW_PIN -> "Choose a new PIN"
            ChangePinStep.CONFIRM -> "Type the new PIN again"
            ChangePinStep.DONE -> "Your PIN is changed"
        },
        testTag = "change_pin_dialog",
        onDismiss = onDismiss
    ) {
        when (step) {
            ChangePinStep.CURRENT -> {
                PinDialogBody("First, type the PIN you use now.")
                PinTextField(
                    value = currentPin,
                    onValueChange = {
                        currentPin = it
                        message = null
                    },
                    label = "Current PIN",
                    testTag = "change_pin_current_input",
                    isVisible = isPinVisible,
                    onToggleVisible = { isPinVisible = !isPinVisible },
                    // Older accounts may have PINs of up to 8 digits.
                    maxLength = 8
                )
                message?.let { PinDialogMessage(it, isError = true, testTag = "change_pin_message") }
                PinDialogPrimaryButton("Next", "change_pin_next_button") {
                    if (verifyPin(currentPin)) {
                        message = null
                        step = ChangePinStep.NEW_PIN
                    } else {
                        message = "That isn't your current PIN. Please try again."
                        currentPin = ""
                    }
                }
                PinDialogSecondaryButton("Cancel", "change_pin_cancel_button", onDismiss)
                if (onForgotPin != null) {
                    TextButton(
                        onClick = onForgotPin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("change_pin_forgot_button")
                    ) {
                        Text("Forgot your PIN?", fontSize = 16.sp)
                    }
                }
            }

            ChangePinStep.NEW_PIN -> {
                PinDialogBody("Choose a new PIN of 4 to 6 numbers that others can't guess.")
                PinTextField(
                    value = newPin,
                    onValueChange = {
                        newPin = it
                        message = null
                    },
                    label = "New PIN",
                    testTag = "change_pin_new_input",
                    isVisible = isPinVisible,
                    onToggleVisible = { isPinVisible = !isPinVisible }
                )
                message?.let { PinDialogMessage(it, isError = true, testTag = "change_pin_message") }
                PinDialogPrimaryButton("Next", "change_pin_next_button") {
                    if (isValidSignUpPin(newPin)) {
                        message = null
                        step = ChangePinStep.CONFIRM
                    } else {
                        message = "Your PIN needs 4 to 6 numbers."
                    }
                }
                PinDialogSecondaryButton("Back", "change_pin_back_button") {
                    message = null
                    step = ChangePinStep.CURRENT
                }
            }

            ChangePinStep.CONFIRM -> {
                PinDialogBody("Type your new PIN once more, so we know it's right.")
                PinTextField(
                    value = confirmPin,
                    onValueChange = {
                        confirmPin = it
                        message = null
                    },
                    label = "New PIN again",
                    testTag = "change_pin_confirm_input",
                    isVisible = isPinVisible,
                    onToggleVisible = { isPinVisible = !isPinVisible }
                )
                message?.let { PinDialogMessage(it, isError = true, testTag = "change_pin_message") }
                PinDialogPrimaryButton("Save my new PIN", "change_pin_save_button") {
                    when {
                        confirmPin != newPin -> {
                            message = "The two PINs are different. Please type the new PIN again."
                            confirmPin = ""
                        }
                        onChangePin(currentPin, newPin) -> {
                            message = null
                            step = ChangePinStep.DONE
                        }
                        else -> {
                            message = "We couldn't save that PIN. Please start again."
                            currentPin = ""
                            newPin = ""
                            confirmPin = ""
                            step = ChangePinStep.CURRENT
                        }
                    }
                }
                PinDialogSecondaryButton("Back", "change_pin_back_button") {
                    message = null
                    confirmPin = ""
                    step = ChangePinStep.NEW_PIN
                }
            }

            ChangePinStep.DONE -> {
                PinDialogMessage(
                    "All done. Use your new PIN next time you open Bright.",
                    isError = false,
                    testTag = "change_pin_done_message"
                )
                PinDialogPrimaryButton("OK", "change_pin_done_button", onDismiss)
            }
        }
    }
}
