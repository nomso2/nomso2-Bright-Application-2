package com.example.model

/**
 * Supported Smart Meter Gateway Manufacturers in the Nigerian Power Sector
 */
enum class MeterManufacturer(
    val displayName: String,
    val defaultProtocol: String,
    val supportedModels: List<String>,
    val apiEndpointPrefix: String
) {
    MOJEC(
        displayName = "Mojec International",
        defaultProtocol = "REST_MQTT_HYBRID",
        supportedModels = listOf("Mojec M-Smart 3000", "Mojec SmartCell GPRS", "Mojec Polyphase STS"),
        apiEndpointPrefix = "https://ami-gateway.mojec.com/api/v2"
    ),
    MOMAS(
        displayName = "Momas Electricity Meters (MEMCOL)",
        defaultProtocol = "DLMS_COSEM_HDLC",
        supportedModels = listOf("Momas MEM-500 Smart", "Momas STS Single-Phase 4G", "Momas Industrial Tri-Vector"),
        apiEndpointPrefix = "https://ami.momas.ng/gateway/v1"
    ),
    CONLOG(
        displayName = "Conlog Nigeria",
        defaultProtocol = "STS6_CELLULAR_APN",
        supportedModels = listOf("Conlog BEC44(09)", "Conlog Wireless In-Home AMI", "Conlog Omnicube STS"),
        apiEndpointPrefix = "https://ng-cloud.conlog.com/meter-telemetry"
    )
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
