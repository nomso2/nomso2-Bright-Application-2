package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.DisCo
import com.example.model.EscalationTier
import com.example.model.FaultType
import com.example.model.FeederBand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies Bright app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Bright", appName)
    }

    @Test
    fun `verify all 11 Nigerian DisCos are modeled with valid contacts`() {
        assertEquals(11, DisCo.entries.size)
        val ekedc = DisCo.fromCode("EKEDC")
        assertEquals("Eko Electricity Distribution Company", ekedc.fullName)
        assertNotNull(ekedc.customerCarePhone)
    }

    @Test
    fun `verify 4-tier NERC escalation sequence`() {
        val l1 = EscalationTier.LEVEL_1
        val l2 = l1.nextTier()
        val l3 = l2?.nextTier()
        val l4 = l3?.nextTier()

        assertEquals(EscalationTier.LEVEL_2, l2)
        assertEquals(EscalationTier.LEVEL_3, l3)
        assertEquals(EscalationTier.LEVEL_4, l4)
        assertEquals(null, l4?.nextTier())
    }

    @Test
    fun `verify feeder band supply minimum hours compliance`() {
        assertEquals(20, FeederBand.BAND_A.minimumHours)
        assertEquals(16, FeederBand.BAND_B.minimumHours)
        assertEquals(12, FeederBand.BAND_C.minimumHours)
    }

    @Test
    fun `verify emergency hazards have priority fast track`() {
        assertTrue(FaultType.LIVE_CABLE_EXPOSED.isEmergency)
        assertTrue(FaultType.SNAPPED_POLE.isEmergency)
        assertTrue(FaultType.TRANSFORMER_SPARKING.isEmergency)
    }
}
