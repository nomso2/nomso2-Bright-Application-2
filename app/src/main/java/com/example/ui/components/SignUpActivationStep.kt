package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.payment.ActivationFee
import com.example.data.payment.ActivationPaymentState
import com.example.ui.theme.extendedColors

/**
 * Step 3: the one-time ₦1,000 activation for this meter.
 * A single "Pay ₦1,000" action goes through [onStartPayment] (the ViewModel's
 * ActivationPaymentGateway). Meters that are already paid skip straight to "Continue".
 */
@Composable
internal fun ActivationStep(
    meterNumber: String,
    isMeterAlreadyPaid: Boolean,
    paymentState: ActivationPaymentState,
    onStartPayment: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val isProcessing = paymentState is ActivationPaymentState.InProgress
    val failure = paymentState as? ActivationPaymentState.Failed

    if (isMeterAlreadyPaid) {
        StepCard(
            title = "Your meter is already activated",
            body = "Meter #$meterNumber was activated before, so there's nothing to pay. Activation is once per meter, for life.",
            testTag = "meter_gateway_activation_card"
        ) {
            ActivatedBadge(text = "Activated for life")
            PrimaryStepButton(
                text = "Continue",
                onClick = onContinue,
                testTag = "register_and_enter_dashboard_button"
            )
            StepBackButton(onClick = onBack)
        }
        return
    }

    StepCard(
        title = "Activate your meter",
        body = "Bright needs a one-time activation for meter #$meterNumber. It isn't a token purchase and doesn't add electricity units.",
        testTag = "meter_gateway_activation_card"
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Amount",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = ActivationFee.LABEL,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("activation_fee_amount")
                )
                Text(
                    text = ActivationFee.POLICY_COPY,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("activation_fee_policy")
                )
            }
        }

        if (failure != null) {
            StepNotice(text = failure.message, isError = true, testTag = "activation_payment_error")
        }

        PrimaryStepButton(
            text = if (failure != null) "Try again" else "Pay ${ActivationFee.LABEL}",
            onClick = onStartPayment,
            isLoading = isProcessing,
            loadingText = "Confirming payment…",
            testTag = "pay_gateway_and_enter_button"
        )
        StepBackButton(onClick = onBack, enabled = !isProcessing)
    }
}

@Composable
internal fun ActivatedBadge(text: String) {
    val success = MaterialTheme.extendedColors.success
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.extendedColors.successContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = success,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = success
            )
        }
    }
}
