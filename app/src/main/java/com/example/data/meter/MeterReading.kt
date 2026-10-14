package com.example.data.meter

/** Where a meter reading came from. Only these sources may fill the telemetry panels. */
enum class ReadingSource(val label: String, val isDemo: Boolean) {
    OPTICAL("Optical probe (IEC 62056-21)", false),
    HES("DisCo HES / MDMS", false),
    DEMO("DEMO meter (simulated)", true)
}

enum class RelayStatus(val label: String) {
    CONNECTED("Connected"),
    TRIPPED_OVERLOAD("Tripped (overload)"),
    OPEN_ZERO_BALANCE("Open (zero balance)"),
    OPEN_OTHER("Open"),
    UNKNOWN("Not reported")
}

enum class TamperFlag(val label: String) {
    TERMINAL_COVER("Terminal cover opened"),
    MAGNETIC("Magnetic interference"),
    REVERSE_ENERGY("Reverse energy"),
    NEUTRAL_MISSING("Neutral missing"),
    METER_FAULT("Meter fault / error register set")
}

/**
 * One reading. Every value is nullable: a field is shown only if the source actually reported
 * it. Nothing is filled in with defaults.
 */
data class MeterReading(
    val source: ReadingSource,
    val meterNumber: String,
    val takenAt: Long,
    val creditKwh: Double? = null,
    val totalImportKwh: Double? = null,
    val totalExportKwh: Double? = null,
    val activePowerW: Double? = null,
    val voltageV: Double? = null,
    val currentA: Double? = null,
    val powerFactor: Double? = null,
    val frequencyHz: Double? = null,
    val relay: RelayStatus = RelayStatus.UNKNOWN,
    val tamperFlags: Set<TamperFlag> = emptySet(),
    /** False when the source doesn't report tamper state at all (vs. reporting "none"). */
    val tamperReported: Boolean = false,
    val meterIdentification: String? = null,
    /** Raw lines as received (optical readout), for anyone who wants to check. */
    val rawLines: List<String> = emptyList()
)

sealed class MeterResult<out T> {
    data class Ok<T>(val value: T) : MeterResult<T>()
    data class Fail(val message: String) : MeterResult<Nothing>()
}
