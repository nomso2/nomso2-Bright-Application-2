package com.example.model

/**
 * Supported Smart Meter Gateway Manufacturers in the Nigerian Power Sector
 */
enum class MeterManufacturer(
    val displayName: String,
    val defaultProtocol: String,
    val supportedModels: List<String>,
    /**
     * Vendor HES base URL. Empty: none of these manufacturers publishes a public consumer API.
     * A DisCo-issued HES endpoint is configured by the user in the gateway screen instead.
     */
    val apiEndpointPrefix: String,
    /** Words that identify the brand when they appear in a nameplate barcode / QR payload. */
    val nameplateKeywords: List<String>
) {
    MOJEC(
        displayName = "Mojec International",
        defaultProtocol = "DLMS/COSEM + STS",
        supportedModels = listOf("Mojec single-phase STS", "Mojec three-phase STS"),
        apiEndpointPrefix = "",
        nameplateKeywords = listOf("MOJEC")
    ),
    MOMAS(
        displayName = "Momas (MEMMCOL)",
        defaultProtocol = "DLMS/COSEM + STS",
        supportedModels = listOf("Momas single-phase STS", "Momas three-phase STS"),
        apiEndpointPrefix = "",
        nameplateKeywords = listOf("MOMAS", "MEMMCOL", "MEMCOL")
    ),
    CONLOG(
        displayName = "Conlog",
        defaultProtocol = "STS",
        supportedModels = listOf("Conlog BEC series", "Conlog split prepayment"),
        apiEndpointPrefix = "",
        nameplateKeywords = listOf("CONLOG")
    ),
    HEXING(
        displayName = "Hexing Electrical",
        defaultProtocol = "DLMS/COSEM + STS",
        supportedModels = listOf("Hexing single-phase STS", "Hexing three-phase STS"),
        apiEndpointPrefix = "",
        nameplateKeywords = listOf("HEXING")
    ),
    SANXING(
        displayName = "Ningbo Sanxing",
        defaultProtocol = "DLMS/COSEM + STS",
        supportedModels = listOf("Sanxing single-phase STS", "Sanxing three-phase STS"),
        apiEndpointPrefix = "",
        nameplateKeywords = listOf("SANXING", "SAN XING")
    ),
    CHINT(
        displayName = "Shenzhen Chint",
        defaultProtocol = "DLMS/COSEM + STS",
        supportedModels = listOf("Chint single-phase STS", "Chint three-phase STS"),
        apiEndpointPrefix = "",
        nameplateKeywords = listOf("CHINT")
    );

    companion object {
        /** Brand named in a scanned payload, or null when the payload doesn't say. */
        fun guessFromText(raw: String): MeterManufacturer? {
            val up = raw.uppercase()
            return entries.firstOrNull { m -> m.nameplateKeywords.any { up.contains(it) } }
        }
    }
}

/**
 * Status and relay states returned by physical or simulated meter gateways
 */
enum class MeterRelayState {
    CONNECTED,
    DISCONNECTED,
    TRIPPED_OVERLOAD,
    TAMPER_SUSPENDED
}

/**
 * Comprehensive real-time meter telemetry snapshot from manufacturer gateways
 */
data class MeterGatewayTelemetry(
    val meterNumber: String,
    val manufacturer: MeterManufacturer,
    val model: String,
    val discoCode: String,
    val voltageV: Double,
    val currentA: Double,
    val activePowerKw: Double,
    val reactivePowerKvar: Double,
    val frequencyHz: Double,
    val powerFactor: Double,
    val accumulatedKwh: Double,
    val remainingCreditUnitsKwh: Double,
    val relayState: MeterRelayState,
    val isTamperDetected: Boolean,
    val signalStrengthDbm: Int,
    val timestamp: Long,
    val latencyMs: Int,
    val gatewayStatus: String = "HEALTHY_TELEMETRY_SYNC"
)

/**
 * Result wrapper for Meter Gateway operations
 */
sealed class GatewayResult<out T> {
    data class Success<T>(val data: T, val latencyMs: Int) : GatewayResult<T>()
    data class Error(val message: String, val errorCode: String? = null) : GatewayResult<Nothing>()
}
