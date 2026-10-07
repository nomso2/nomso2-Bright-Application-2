package com.example

import com.example.data.payment.ActivationFee
import com.example.data.payment.PaymentResult
import com.example.data.payment.SimulatedActivationPaymentGateway
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivationPaymentTest {

    @Test
    fun activationFeeIsOneThousandNaira() {
        assertEquals(100_000L, ActivationFee.AMOUNT_KOBO)
        assertEquals("₦1,000", ActivationFee.LABEL)
        assertEquals("One-time ₦1,000 per meter. Lifetime access.", ActivationFee.POLICY_COPY)
    }

    @Test
    fun simulatedGatewaySucceedsForAMeter() = runTest {
        val result = SimulatedActivationPaymentGateway(simulatedDelayMillis = 0L)
            .startPayment(meter = "01234567890", amountKobo = ActivationFee.AMOUNT_KOBO)
        assertTrue(result is PaymentResult.Success)
    }

    @Test
    fun simulatedGatewayRejectsBlankMeter() = runTest {
        val result = SimulatedActivationPaymentGateway(simulatedDelayMillis = 0L)
            .startPayment(meter = " ", amountKobo = ActivationFee.AMOUNT_KOBO)
        assertTrue(result is PaymentResult.Failed)
    }
}
