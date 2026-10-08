package com.example

import com.example.data.security.PinHasher
import com.example.data.security.PinResetCheck
import com.example.data.security.PinStore
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinStoreTest {

    @Test
    fun hashIsSaltedAndVerifies() {
        val saltA = PinHasher.newSalt()
        val saltB = PinHasher.newSalt()
        val hashA = PinHasher.hash("1234", saltA)
        assertNotEquals(PinHasher.toHex(hashA), PinHasher.toHex(PinHasher.hash("1234", saltB)))
        assertTrue(PinHasher.matches("1234", saltA, hashA))
        assertFalse(PinHasher.matches("1235", saltA, hashA))
        assertArrayEquals(saltA, PinHasher.fromHex(PinHasher.toHex(saltA)))
    }

    @Test
    fun setVerifyAndClear() {
        val store = PinStore(prefs = null)
        assertFalse(store.isPinSet)
        assertFalse(store.verify("1234"))
        store.setPin("4821")
        assertTrue(store.isPinSet)
        assertTrue(store.verify("4821"))
        assertFalse(store.verify("4822"))
        store.clear()
        assertFalse(store.isPinSet)
    }

    @Test
    fun fiveWrongTriesStartFifteenMinuteBreak() {
        var now = 1_000_000L
        val store = PinStore(prefs = null, clock = { now })
        repeat(4) { i ->
            assertEquals(PinResetCheck.Wrong(4 - i), store.recordResetFailure())
        }
        assertEquals(PinResetCheck.LockedOut(15), store.recordResetFailure())
        assertEquals(15, store.resetLockoutMinutesLeft())
        now += 14 * 60_000L
        assertEquals(1, store.resetLockoutMinutesLeft())
        now += 60_000L
        assertEquals(0, store.resetLockoutMinutesLeft())
        // The counter starts again after the break.
        assertEquals(PinResetCheck.Wrong(4), store.recordResetFailure())
        store.clearResetFailures()
        assertEquals(PinResetCheck.Wrong(4), store.recordResetFailure())
    }
}
