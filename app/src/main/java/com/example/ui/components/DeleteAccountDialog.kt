package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.payment.ActivationFee

/** The word the user types to confirm deletion. */
private const val DELETE_CONFIRMATION_WORD = "DELETE"

/**
 * Confirms permanent account deletion. The user must type DELETE, or enter their PIN
 * (when a PIN exists on this phone), before the destructive button is enabled.
 */
@Composable
fun DeleteAccountDialog(
    meterNumber: String,
    isPinSet: Boolean,
    verifyPin: (String) -> Boolean,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var confirmation by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val trimmed = confirmation.trim()
    val typedDelete = trimmed == DELETE_CONFIRMATION_WORD
    val looksLikePin = isPinSet && trimmed.isNotEmpty() && trimmed.all { it.isDigit() }

    fun tryConfirm() {
        when {
            typedDelete -> onConfirmDelete()
            looksLikePin && verifyPin(trimmed) -> onConfirmDelete()
            looksLikePin -> errorText = "That PIN isn't right. Try again, or type DELETE."
            else -> errorText = "Type DELETE in capital letters to confirm."
        }
    }

    val meterLabel = meterNumber.trim().ifBlank { "your meter" }.let { if (it.first().isDigit()) "meter #$it" else it }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("delete_account_dialog"),
        icon = {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "Delete your account?",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "This permanently removes your profile, PIN, complaints and report history, and all saved data from this phone. It can't be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Your ${ActivationFee.LABEL} activation stays with $meterLabel. If you register the same meter again, you won't be charged again.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = confirmation,
                    onValueChange = {
                        confirmation = it.take(12)
                        errorText = null
                    },
                    label = { Text(if (isPinSet) "Type DELETE or enter your PIN" else "Type DELETE") },
                    singleLine = true,
                    isError = errorText != null,
                    supportingText = {
                        Text(errorText ?: "This confirms you want to delete everything.")
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Text
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delete_account_confirm_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { tryConfirm() },
                enabled = trimmed.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.testTag("delete_account_confirm_button")
            ) {
                Text("Delete account", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("delete_account_cancel_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
