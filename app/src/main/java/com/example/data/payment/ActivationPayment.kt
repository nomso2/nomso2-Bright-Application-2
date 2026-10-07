package com.example.data.payment

import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

/**
 * Meter activation fee: paid once per meter for lifetime access to Bright.
 * A meter is linked to a single account, and the paid-meter record is kept even if the
 * account is deleted, so re-registering the same meter never charges twice.
 */
object ActivationFee {
    /** ₦1,000 expressed in kobo (1 naira = 100 kobo). */
    const val AMOUNT_KOBO: Long = 100_000L

    /** Display label, e.g. "₦1,000". */
    val LABEL: String = formatNaira(AMOUNT_KOBO)

    /** The one line of policy copy shown wherever the fee is mentioned. */
    val POLICY_COPY: String = "One-time $LABEL per meter. Lifetime access."

    fun formatNaira(amountKobo: Long): String {
        val naira = amountKobo / 100
        return "₦" + NumberFormat.getIntegerInstance(Locale.US).format(naira)
    }
}

/** Outcome of an activation payment attempt. */
sealed interface PaymentResult {
    /** Payment confirmed. [reference] identifies the transaction for support and receipts. */
    data class Success(val reference: String) : PaymentResult

    /** The user backed out before paying. Nothing was charged. */
    data object Cancelled : PaymentResult

    /** Payment did not go through. [message] is plain-language copy safe to show the user. */
    data class Failed(val message: String) : PaymentResult
}

/**
 * Takes the one-time activation payment for a meter.
 *
 * TODO(payments): replace [SimulatedActivationPaymentGateway] with a Google Play Billing
 *  implementation before release:
 *  - Product: a consumable in-app product (e.g. "meter_activation_1000"), priced at ₦1,000.
 *    It is consumable because each new meter is a separate purchase; consume it only after the
 *    server has recorded the meter as paid.
 *  - Flow: BillingClient.launchBillingFlow needs an Activity, so the real implementation will take
 *    one (constructor or extra parameter). Pass the meter number as obfuscatedAccountId /
 *    obfuscatedProfileId so the purchase can be tied to the meter.
 *  - Verification must happen SERVER-SIDE: send the purchase token + meter number to the Bright
 *    backend, which verifies it with the Google Play Developer API (purchases.products.get),
 *    records the meter as paid, then tells the app to acknowledge/consume. Never trust a
 *    client-only "success" for granting access.
 *  - The paid-meter record should then come from the server, not only local preferences.
 */
interface ActivationPaymentGateway {
    suspend fun startPayment(meter: String, amountKobo: Long): PaymentResult
}

/**
 * Stand-in used until Google Play Billing is wired up. It does NOT take real money: it waits
 * briefly and reports success so the rest of the sign-up flow can be built and tested.
 */
class SimulatedActivationPaymentGateway(
    private val simulatedDelayMillis: Long = 1_200L
) : ActivationPaymentGateway {
    override suspend fun startPayment(meter: String, amountKobo: Long): PaymentResult {
        if (meter.isBlank()) return PaymentResult.Failed("Enter your meter number before paying.")
        if (amountKobo <= 0L) return PaymentResult.Failed("Something went wrong with the amount. Please try again.")
        delay(simulatedDelayMillis)
        return PaymentResult.Success(reference = "SIM-" + UUID.randomUUID().toString().take(8).uppercase(Locale.US))
    }
}

/** What the activation step shows. Held by the ViewModel so it survives recomposition. */
sealed interface ActivationPaymentState {
    data object Idle : ActivationPaymentState
    data class InProgress(val meter: String) : ActivationPaymentState
    data class Succeeded(val meter: String, val reference: String) : ActivationPaymentState
    data class Failed(val meter: String, val message: String) : ActivationPaymentState
}
