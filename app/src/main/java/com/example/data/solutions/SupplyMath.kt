package com.example.data.solutions

import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

/** Hours of supply on one calendar day (local time). */
data class DayHours(val dayStart: Long, val hours: Double)

/** Pure calculations over the Light On / Light Off log. No Android types, so it is unit-testable. */
object SupplyMath {
    const val HOUR_MS = 3_600_000L
    const val DAY_MS = 24 * HOUR_MS

    fun startOfDay(time: Long, calendar: Calendar = Calendar.getInstance()): Long {
        calendar.timeInMillis = time
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /** Intervals (start, end) when the light was on. An open ON runs until [now]. */
    fun onIntervals(events: List<SupplyEventEntity>, now: Long): List<Pair<Long, Long>> {
        val sorted = events.sortedBy { it.timestamp }
        val out = mutableListOf<Pair<Long, Long>>()
        var onSince: Long? = null
        for (e in sorted) {
            if (e.isOn) {
                if (onSince == null) onSince = e.timestamp
            } else {
                onSince?.let { out.add(it to e.timestamp) }
                onSince = null
            }
        }
        onSince?.let { if (now > it) out.add(it to now) }
        return out
    }

    /** Supply hours for each of the last [days] days, oldest first, today last. */
    fun hoursByDay(events: List<SupplyEventEntity>, days: Int, now: Long): List<DayHours> {
        val intervals = onIntervals(events, now)
        val todayStart = startOfDay(now)
        val cal = Calendar.getInstance()
        return (days - 1 downTo 0).map { back ->
            cal.timeInMillis = todayStart
            cal.add(Calendar.DAY_OF_YEAR, -back)
            val dayStart = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = min(cal.timeInMillis, now)
            var ms = 0L
            for ((s, e) in intervals) {
                val overlap = min(e, dayEnd) - max(s, dayStart)
                if (overlap > 0) ms += overlap
            }
            DayHours(dayStart, ms.toDouble() / HOUR_MS)
        }
    }

    /** Average daily hours over complete days only (today is still running, so it is left out). */
    fun averageCompleteDays(byDay: List<DayHours>): Double {
        val complete = byDay.dropLast(1)
        if (complete.isEmpty()) return 0.0
        return complete.sumOf { it.hours } / complete.size
    }

    /** Longest run of consecutive complete days below [promised] hours, ending yesterday. */
    fun consecutiveShortDays(byDay: List<DayHours>, promised: Int): Int {
        var run = 0
        for (d in byDay.dropLast(1).asReversed()) {
            if (d.hours < promised) run++ else break
        }
        return run
    }

    /** The band whose minimum hours were actually delivered. */
    fun deliveredBand(avgHours: Double): String = when {
        avgHours >= 20 -> "Band A"
        avgHours >= 16 -> "Band B"
        avgHours >= 12 -> "Band C"
        avgHours >= 8 -> "Band D"
        else -> "Band E"
    }

    /** True once at least one complete day is logged, so averages mean something. */
    fun hasCompleteDay(events: List<SupplyEventEntity>, now: Long): Boolean =
        events.isNotEmpty() && events.minOf { it.timestamp } < startOfDay(now)

    /**
     * NERC basis (Order NERC/334/2022 and Addendum NERC/2024/003): a Band A customer who
     * receives less than the promised hours is compensated for the gap between the Band A
     * tariff they paid and the tariff of the service actually delivered. For prepaid that is
     * credited as kWh token; for postpaid the bill is adjusted.
     *
     * Estimate = units bought in the period x (tariff paid - tariff for the band delivered).
     */
    fun estimateOwedNaira(
        unitsBoughtKwh: Double,
        tariffPaidPerKwh: Double,
        tariffDeliveredPerKwh: Double
    ): Double = max(0.0, unitsBoughtKwh * (tariffPaidPerKwh - tariffDeliveredPerKwh))

    /** Money-for-time view: share of spend that bought hours never delivered. */
    fun proportionalShortfallNaira(spentNaira: Double, promisedHours: Double, suppliedHours: Double): Double {
        if (promisedHours <= 0) return 0.0
        val gap = max(0.0, promisedHours - suppliedHours)
        return spentNaira * (gap / promisedHours)
    }
}
