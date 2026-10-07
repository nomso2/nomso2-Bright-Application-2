package com.example

import com.example.data.solutions.SupplyEventEntity
import com.example.data.solutions.SupplyMath
import org.junit.Assert.assertEquals
import org.junit.Test

class SupplyMathTest {
    private val h = SupplyMath.HOUR_MS

    @Test
    fun hoursCountOnlyWhileLightIsOn() {
        val day = SupplyMath.startOfDay(System.currentTimeMillis()) - SupplyMath.DAY_MS
        val events = listOf(
            SupplyEventEntity(isOn = true, timestamp = day + 2 * h),
            SupplyEventEntity(isOn = false, timestamp = day + 8 * h),
            SupplyEventEntity(isOn = true, timestamp = day + 10 * h),
            SupplyEventEntity(isOn = false, timestamp = day + 20 * h)
        )
        val byDay = SupplyMath.hoursByDay(events, 2, day + SupplyMath.DAY_MS + h)
        assertEquals(16.0, byDay[0].hours, 0.01)
        assertEquals("Band C", SupplyMath.deliveredBand(byDay[0].hours))
    }

    @Test
    fun owedEstimateUsesTariffGap() {
        assertEquals(14650.0, SupplyMath.estimateOwedNaira(100.0, 209.5, 63.0), 0.01)
        assertEquals(0.0, SupplyMath.estimateOwedNaira(100.0, 63.0, 209.5), 0.01)
    }
}
