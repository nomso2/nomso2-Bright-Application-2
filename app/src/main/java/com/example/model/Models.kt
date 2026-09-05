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
    val connectedHouseholdsCount: Int = 184
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
