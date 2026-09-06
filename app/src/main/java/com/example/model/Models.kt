package com.example.model

/**
 * User Meter Identity - The core anchor of Bright
 */
data class UserProfile(
    val meterNumber: String = "01429583192",
    val customerName: String = "Chuka Obunma",
    val phoneNumber: String = "+234 803 892 4110",
    val streetAddress: String = "14 Adeola Odeku Street, Victoria Island",
    val lga: String = "Eti-Osa",
    val state: String = "Lagos State",
    val discoCode: String = "EKEDC",
    val feederName: String = "Victoria Island 33kV Injection Feeder 4",
    val feederBand: FeederBand = FeederBand.BAND_A,
    val transformerId: String = "TR-VI-ADEOLA-04B",
    val isPrepaid: Boolean = true,
    val connectedHouseholdsCount: Int = 184,
    val isOnboarded: Boolean = false
)

/**
 * Individual Personal Complaint & Fault Ticket
 */
data class Complaint(
    val id: String,
    val meterNumber: String,
    val title: String,
    val description: String,
    val faultType: FaultType,
    val isHazardEmergency: Boolean,
    val status: ComplaintStatus,
    val escalationTier: EscalationTier,
    val discoCode: String,
    val transformerId: String,
    val feederName: String,
    val reportedAt: Long,
    val updatedAt: Long,
    val escalationDeadline: Long,
    val upvotesCount: Int = 1,
    val assignedCrewName: String? = null,
    val assignedCrewPhone: String? = null,
    val etaMinutes: Int? = null,
    val resolutionNotes: String? = null,
    val userSatisfaction: Int? = null,
    val imageUri: String? = null,
    val autoClusteredCount: Int = 1,
    val resolvedAt: Long? = null,
    val isVideo: Boolean = false
)

/**
 * Vandalism and Infrastructure Theft Report
 */
data class VandalismReport(
    val id: String,
    val incidentType: String,
    val location: String,
    val landmark: String,
    val discoCode: String,
    val reportedAt: Long,
    val isAnonymous: Boolean,
    val description: String,
    val status: String, // "DISPATCHED", "SECURITY_INVESTIGATING", "SUSPECTS_NABBED", "REPAIR_REQUISITION"
    val suspectDetails: String? = null
)

/**
 * Billing Dispute Entry
 */
data class BillingDispute(
    val id: String,
    val meterNumber: String,
    val disputeType: String, // "Capped Tariff Overbilling", "Estimated Bill Without Supply", "Token Generated Not Loaded", "Faulty Meter Calibration"
    val disputedAmountNgn: Double,
    val billingMonth: String,
    val discoCode: String,
    val description: String,
    val status: String, // "Under Audit", "Approved for Refund/Credit", "Rejected", "NERC Escalation"
    val createdAt: Long
)

/**
 * Node for the Live Outage Map
 */
data class OutageGridNode(
    val id: String,
    val name: String,
    val city: String,
    val state: String,
    val discoCode: String,
    val status: OutageStatus,
    val affectedConsumers: Int,
    val xPosRatio: Float, // 0.0f - 1.0f on canvas
    val yPosRatio: Float, // 0.0f - 1.0f on canvas
    val lastUpdate: String,
    val reportedFaults: Int,
    val estimatedRestoration: String
)

enum class OutageStatus(val label: String, val colorHex: Long) {
    ACTIVE("Operational / Power ON", 0xFF10B981),
    FAULT_DOWN("Fault Blackout / Trip", 0xFFEF4444),
    MAINTENANCE("Scheduled Overhaul", 0xFFF59E0B),
    LOAD_SHEDDING("Load Shedding (TCN Quota)", 0xFF8B5CF6)
}

/**
 * Planned Maintenance schedule
 */
data class MaintenanceAlert(
    val id: String,
    val title: String,
    val discoCode: String,
    val affectedFeeders: String,
    val startDate: String,
    val durationHours: Int,
    val reason: String,
    val alternativeSupplyAvailable: Boolean
)

/**
 * Live National Grid Telemetry
 */
data class GridTelemetry(
    val nationalGenerationMw: Int = 4318,
    val peakForecastMw: Int = 5850,
    val systemFrequencyHz: Double = 50.08,
    val systemStatus: String = "Grid Stable",
    val spinningReserveMw: Int = 240,
    val activeGenCos: Int = 22,
    val lastUpdatedText: String = "Live (TCN NCC Osogbo)"
)

/**
 * Appliance Surge Damage Statutory Compensation Claim (NERC CPR 2023)
 */
data class ApplianceDamageClaim(
    val id: String,
    val meterNumber: String,
    val applianceName: String,
    val applianceBrandModel: String,
    val estimatedLossNgn: Double,
    val surgeTimestampText: String,
    val surgeDescription: String,
    val statutoryNoticeText: String,
    val discoCode: String,
    val status: String, // "SUBMITTED_TO_DISCO", "UNDER_TECHNICAL_INSPECTION", "APPROVED_BILLING_CREDIT", "ESCALATED_NERC"
    val createdAt: Long
)

/**
 * Pinned Public Street Electrical Hazard (High Tension wires, fallen poles, etc.)
 */
data class StreetHazardPin(
    val id: String,
    val title: String,
    val hazardType: String,
    val urgency: String,
    val location: String,
    val landmark: String,
    val discoCode: String,
    val reportedBy: String,
    val verifiedCount: Int = 1,
    val isDispatched: Boolean = false,
    val xPosRatio: Float,
    val yPosRatio: Float,
    val reportedAt: Long
)

/**
 * Neighborhood Distribution Transformer Real-Time Overload & Phase Balance
 */
data class TransformerOverloadTelemetry(
    val transformerId: String = "TR-VI-ADEOLA-04B",
    val transformerCapacityKva: Int = 500,
    val currentLoadPercent: Int = 88,
    val isOverloaded: Boolean = true,
    val phaseAVolts: Int = 236,
    val phaseBVolts: Int = 182,
    val phaseCVolts: Int = 239,
    val connectedHouseholds: Int = 184,
    val designHouseholdCapacity: Int = 150,
    val peakWindowText: String = "7:00 PM – 10:30 PM",
    val oilTemperatureCelsius: Int = 74,
    val humSparkRiskLevel: String = "HIGH (Phase B Coil Heat)"
)

/**
 * Phase 2: Contractual Hour Auditing Matrix (Service Band SLA delivery ledger)
 */
data class AuditingHourRecord(
    val dateText: String,
    val dayName: String,
    val promisedBandHours: Double = 20.0,
    val actualDeliveredHours: Double,
    val isSlaBreached: Boolean = actualDeliveredHours < promisedBandHours,
    val shortfallHours: Double = (promisedBandHours - actualDeliveredHours).coerceAtLeast(0.0),
    val compensationDueNgn: Double = shortfallHours * 142.50
)

/**
 * Phase 4: Automated Escrow 20-Digit Rebate Token
 */
data class EscrowTokenRebate(
    val id: String,
    val token20Digit: String,
    val kwhValue: Double,
    val monetaryValueNgn: Double,
    val discoCode: String,
    val reason: String,
    val issuedTimestamp: Long,
    val isRedeemed: Boolean = false
)

/**
 * Phase 5: Transformer Cluster Forum Post
 */
data class CommunityForumPost(
    val id: String,
    val authorName: String,
    val isVerifiedResident: Boolean = true,
    val transformerId: String,
    val content: String,
    val timestampText: String,
    val upvotes: Int = 0,
    val isExtortionReport: Boolean = false
)

/**
 * Phase 6: Appliance Consumption Matrix Item
 */
data class ApplianceBudgetItem(
    val id: String,
    val name: String,
    val wattage: Int,
    val hoursDaily: Double,
    val isEcoMode: Boolean = false
) {
    val dailyKwh: Double get() = (wattage * hoursDaily) / 1000.0
    val monthlyCostNgn: Double get() = dailyKwh * 30.0 * 209.50 // Band A MYTO tariff ~₦209.50/kWh
}

/**
 * Phase 7: Multi-Asset Linked Meter
 */
data class LinkedMeterAsset(
    val id: String,
    val label: String, // e.g. "Primary Residence", "Workspace / Lekki Studio", "Family Home / Surulere"
    val meterNumber: String,
    val address: String,
    val discoCode: String,
    val feederBand: FeederBand,
    val transformerId: String,
    val isPrepaid: Boolean = true,
    val isSelected: Boolean = false
)

/**
 * Phase 7: Regulatory Whistleblower Report (Encrypted NERC Pipeline)
 */
data class WhistleblowerReport(
    val id: String,
    val discoCode: String,
    val targetOfficialOrUnit: String,
    val extortionType: String,
    val amountDemandedNgn: Double,
    val incidentDescription: String,
    val timestampText: String,
    val status: String = "CRYPTOGRAPHICALLY_SEALED_NERC_DISPATCHED"
)

/**
 * CDA / Transformer Dues Ledger Entry
 */
data class TransformerDuesEntry(
    val id: String,
    val residentName: String,
    val houseAddress: String,
    val meterNumber: String,
    val purpose: String, // "Jumper Cable Replacement", "500kVA Transformer Oil Top-Up", "Street Security Light Repairs", "Lineman Requisition"
    val amountNgn: Double,
    val dateText: String,
    val paymentMethod: String, // "Bank Transfer", "OPay", "Cash to Chairman"
    val verifiedByChairman: Boolean = true
)

/**
 * NERC SLA Compensation Assessment Record
 */
data class SlaCompensationAssessment(
    val ticketId: String,
    val faultTitle: String,
    val discoCode: String,
    val reportedTimeFormatted: String,
    val nercStandardHoursLimit: Int, // e.g. 24 hours for fuse, 48 hours for transformer
    val actualResolutionHours: Int,
    val excessHoursBreached: Int,
    val statutoryHourlyRateNgn: Double = 93.75, // NERC approved CPR minimum tariff compensation unit
    val totalCompensationPayableNgn: Double,
    val eligibleCreditToken: String,
    val demandLetterText: String,
    val isClaimDispatched: Boolean = false
)

/**
 * Smart Meter Server Gateway Configuration
 * Enables connecting the mobile app and backend server to smart meters across Nigeria
 */
data class SmartMeterServerConfig(
    val serverUrl: String = "https://ami.brightgrid.ng/api/v1",
    val protocol: String = "REST_MQTT_HYBRID", // "REST_MQTT_HYBRID", "DLMS_COSEM_HDLC", "STS6_CELLULAR_APN", "WEBSOCKET_STREAM"
    val mqttBrokerHost: String = "mqtt.brightgrid.ng:8883",
    val apiKey: String = "ami_live_ng_98fa01c27e",
    val webhookEndpoint: String = "/api/v1/telemetry/webhook",
    val syncIntervalSeconds: Int = 15,
    val isConnected: Boolean = true,
    val lastHeartbeatTime: String = "Just now",
    val latencyMs: Int = 42,
    val connectedMetersCount: Int = 8,
    val tlsEnabled: Boolean = true
)

/**
 * Smart Meter Device deployed across Nigeria (Mojec, Momas, Conlog, Hexing, Inhemeter, etc.)
 */
data class SmartMeterDevice(
    val meterNumber: String,
    val manufacturer: String, // "Mojec International", "Momas (MEMCOL)", "Conlog Nigeria", "Hexing Electrical", "Inhemeter"
    val modelNumber: String,
    val discoCode: String, // "EKEDC", "IKEDC", "AEDC", "IBEDC", "EEDC", "KEDCO", "PHED"
    val locationState: String, // "Lagos", "Abuja FCT", "Oyo", "Enugu", "Kano", "Rivers"
    val feederName: String,
    val ipOrSimImei: String,
    val protocol: String, // "DLMS/COSEM", "STS-6/Cellular", "MQTT/JSON", "NB-IoT"
    val isOnline: Boolean = true,
    val voltageV: Double = 228.5,
    val currentA: Double = 12.4,
    val frequencyHz: Double = 50.02,
    val activePowerKw: Double = 2.83,
    val accumulatedKwh: Double = 1420.8,
    val powerFactor: Double = 0.96,
    val relayStatusClosed: Boolean = true, // true = Power Connected, false = Disconnected
    val tamperDetected: Boolean = false,
    val lastPingSecondsAgo: Int = 4,
    val signalStrengthDbm: Int = -72, // Cell RSSI
    val firmwareVersion: String = "v4.2.1-NG-MAP"
)

/**
 * Remote Command dispatched to a Smart Meter via the App Server
 */
data class SmartMeterCommand(
    val id: String,
    val meterNumber: String,
    val commandType: String, // "RELAY_DISCONNECT", "RELAY_RECONNECT", "OTA_TOKEN_INJECTION", "INSTANT_TELEMETRY_READ", "TIME_SYNC"
    val payload: String = "",
    val timestampText: String,
    val status: String // "DELIVERED_ACK", "PENDING_QUEUE", "FAILED"
)


