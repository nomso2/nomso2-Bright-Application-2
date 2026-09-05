package com.example.model

/**
 * 11 Nigerian Distribution Companies (DisCos)
 */
enum class DisCo(
    val code: String,
    val fullName: String,
    val statesCovered: String,
    val customerCarePhone: String,
    val headOffice: String,
    val averageResolutionHours: Double,
    val resolvedRatePercent: Int,
    val customerRating: Double,
    val gridAvailabilityPercent: Int
) {
    AEDC("AEDC", "Abuja Electricity Distribution Company", "FCT, Kogi, Nasarawa, Niger", "08039070070", "Abuja", 6.2, 86, 3.8, 72),
    BEDC("BEDC", "Benin Electricity Distribution Company", "Edo, Delta, Ondo, Ekiti", "08035888888", "Benin City", 10.4, 71, 3.1, 58),
    EKEDC("EKEDC", "Eko Electricity Distribution Company", "Lagos South & Island, Agbara", "07080655555", "Marina, Lagos", 3.8, 92, 4.3, 84),
    EEDC("EEDC", "Enugu Electricity Distribution Company", "Abia, Anambra, Ebonyi, Enugu, Imo", "084700100", "Enugu", 11.5, 68, 2.9, 54),
    IBEDC("IBEDC", "Ibadan Electricity Distribution Company", "Oyo, Ogun, Osun, Kwara, Niger (part)", "07001239999", "Ibadan", 7.8, 79, 3.5, 66),
    IE("IE", "Ikeja Electric", "Lagos North & Mainland, Ikorodu", "01-7000-250", "Alausa, Ikeja", 4.1, 91, 4.2, 82),
    JED("JED", "Jos Electricity Distribution Company", "Plateau, Bauchi, Benue, Gombe", "07000533267", "Jos", 12.8, 64, 2.8, 51),
    KAEDC("KAEDC", "Kaduna Electricity Distribution Company", "Kaduna, Kebbi, Sokoto, Zamfara", "08031230000", "Kaduna", 14.2, 60, 2.6, 48),
    KEDCO("KEDCO", "Kano Electricity Distribution Company", "Kano, Katsina, Jigawa", "07005555555", "Kano", 9.6, 74, 3.3, 62),
    PHED("PHED", "Port Harcourt Electricity Distribution Company", "Rivers, Bayelsa, Cross River, Akwa Ibom", "08139834000", "Port Harcourt", 8.9, 75, 3.4, 64),
    YEDC("YEDC", "Yola Electricity Distribution Company", "Adamawa, Borno, Taraba, Yobe", "08031234567", "Yola", 15.0, 58, 2.5, 45);

    companion object {
        fun fromCode(code: String): DisCo {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: EKEDC
        }
    }
}

/**
 * Feeder band service standards set by NERC
 */
enum class FeederBand(val code: String, val minimumHours: Int, val description: String) {
    BAND_A("Band A", 20, "20+ hours daily supply"),
    BAND_B("Band B", 16, "16 - 20 hours daily supply"),
    BAND_C("Band C", 12, "12 - 16 hours daily supply"),
    BAND_D("Band D", 8, "8 - 12 hours daily supply"),
    BAND_E("Band E", 4, "4 - 8 hours daily supply")
}

/**
 * Fault Category in the Nigerian Electrical Distribution Network
 */
enum class FaultType(val displayName: String, val iconName: String, val isEmergency: Boolean) {
    BURNT_TRANSFORMER("Burnt / Blown Transformer", "transformer", false),
    SNAPPED_POLE("Fallen / Snapped Concrete Pole", "pole", true),
    LIVE_CABLE_EXPOSED("Exposed / Low Hanging Live Cable", "warning", true),
    TRANSFORMER_SPARKING("Sparking Transformer / Arcing", "flash", true),
    FEEDER_TRIPPED("33kV / 11kV Feeder Tripped", "power_off", false),
    PHASE_FAILURE("Single / Low Phase Power (Half-Current)", "tune", false),
    METER_TAMPER_FAULT("Meter Tamper / Prepaid Keypad Error", "pin", false),
    SUBSTATION_VANDALISM("Cable Theft / Substation Vandalism", "shield", true),
    TOTAL_BLACKOUT("Unannounced Total Blackout", "lightbulb_off", false)
}

/**
 * 4-Tier Automated Escalation System for Accountability
 */
enum class EscalationTier(val level: Int, val title: String, val authority: String, val maxSlaHours: Int) {
    LEVEL_1(1, "Field Technical Crew", "Local DisCo Undertaking / Sub-Station", 4),
    LEVEL_2(2, "Business District Office", "Area Technical Engineer & Customer Service Lead", 12),
    LEVEL_3(3, "DisCo HQ Operations", "Head of Distribution & Managing Director's Office", 24),
    LEVEL_4(4, "NERC Regulatory Enforcement", "Nigerian Electricity Regulatory Commission Consumer Desk", 48);

    fun nextTier(): EscalationTier? {
        return when (this) {
            LEVEL_1 -> LEVEL_2
            LEVEL_2 -> LEVEL_3
            LEVEL_3 -> LEVEL_4
            LEVEL_4 -> null
        }
    }
}

/**
 * Complaint Status Lifecycle
 */
enum class ComplaintStatus(val displayName: String) {
    LOGGED("Fault Logged"),
    ASSIGNED("Technician Assigned"),
    DISPATCHED("Crew En-Route"),
    WORK_IN_PROGRESS("Repair In Progress"),
    TESTING("Supply Restored & Testing"),
    RESOLVED("Confirmed Resolved"),
    ESCALATED("Escalated to NERC")
}
