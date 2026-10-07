package com.example

import com.example.model.DisCo
import com.example.model.DisCoContacts
import com.example.model.PhoneNumbers
import com.example.model.StateDisCoMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DisCoContactsTest {

    @Test
    fun `every state and FCT maps to known distributors`() {
        assertEquals(37, StateDisCoMap.states.size)
        assertEquals(37, StateDisCoMap.stateNames.toSet().size)
        StateDisCoMap.states.forEach { row ->
            assertTrue(row.state, row.discoCodes.isNotEmpty())
            assertEquals(row.state, row.discoCodes.size, row.contacts.size)
        }
    }

    @Test
    fun `every enum DisCo has an official contact with a phone`() {
        DisCo.entries.forEach { disco ->
            val contact = DisCoContacts.forCode(disco.code)
            assertNotNull(disco.code, contact)
            assertTrue(disco.code, disco.customerCarePhone.isNotBlank())
        }
        assertFalse(DisCo.entries.any { it.customerCarePhone == "08031234567" })
    }

    @Test
    fun `profile state strings resolve`() {
        assertEquals("Lagos", StateDisCoMap.forState("Lagos State")?.state)
        assertEquals("FCT", StateDisCoMap.forState("Abuja")?.state)
        assertEquals("FCT", StateDisCoMap.forState("Federal Capital Territory")?.state)
        assertEquals("Akwa Ibom", StateDisCoMap.forState("Akwa-Ibom")?.state)
        assertEquals(listOf("MAINPOWER"), StateDisCoMap.forState("Enugu")?.discoCodes)
        assertNull(StateDisCoMap.forState("Atlantis"))
    }

    @Test
    fun `KAEDC is flagged for verification`() {
        assertFalse(DisCoContacts.forCode("KAEDC")!!.verifiedOfficial)
    }

    @Test
    fun `whatsapp numbers normalise to wa me digits`() {
        assertEquals("2348152141414", PhoneNumbers.toWhatsAppDigits("08152141414"))
        assertEquals("2348188206515", PhoneNumbers.toWhatsAppDigits("+2348188206515"))
        assertEquals("2349088951626", PhoneNumbers.toWhatsAppDigits("0908 895 1626"))
        assertNull(PhoneNumbers.toWhatsAppDigits("12"))
        assertEquals("+2348039070070", PhoneNumbers.toDialable("+234 803 907 0070"))
    }
}
