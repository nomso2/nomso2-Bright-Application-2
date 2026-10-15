package com.example.data.meter

import android.content.Context
import com.example.model.Complaint
import com.example.model.FaultType
import org.json.JSONObject

/**
 * Compensation tokens: the only tokens Bright deals with. Bright never sells, vends or recharges
 * electricity. A compensation token is what a DisCo owes when a fault repair runs past the NERC
 * service level: 24 hours for a phase fault, 48 hours for a transformer fault.
 *
 * Entries are derived from the user's own complaint tickets (Room `complaints`, read only, no
 * schema change). What the user records about each claim (Claimed / Issued, the 20-digit token
 * the DisCo issued, the kWh on it) is kept on this phone in SharedPreferences.
 */
object CompensationSla {
    const val PHASE_FAULT_HOURS = 24
    const val TRANSFORMER_FAULT_HOURS = 48

    const val EXPLANATION =
        "Compensation tokens are what your DisCo owes you when a fault repair runs past the NERC limit: " +
            "24 hours for a phase fault, 48 hours for a transformer. Bright never sells or recharges electricity."

    /** NERC repair limit for a fault type, or null when the type isn't covered (no token owed). */
    fun slaHoursFor(type: FaultType): Int? = when (type) {
        FaultType.PHASE_FAILURE -> PHASE_FAULT_HOURS
        FaultType.BURNT_TRANSFORMER, FaultType.TRANSFORMER_SPARKING -> TRANSFORMER_FAULT_HOURS
        else -> null
    }

    fun slaLabel(type: FaultType): String = when (slaHoursFor(type)) {
        PHASE_FAULT_HOURS -> "Phase fault (24h limit)"
        TRANSFORMER_FAULT_HOURS -> "Transformer fault (48h limit)"
        else -> type.displayName
    }
}

enum class CompensationStatus(val label: String) {
    PENDING("Pending"),
    CLAIMED("Claimed"),
    ISSUED("Issued")
}

/** One overdue complaint ticket and the compensation token owed for it. */
data class CompensationEntry(
    val complaintId: String,
    val faultTitle: String,
    val faultType: FaultType,
    val slaHours: Int,
    val hoursTaken: Int,
    val hoursLate: Int,
    val stillOpen: Boolean,
    /** kWh written on the token the DisCo issued, when the user entered it. */
    val kwhIssued: Double?,
    /** Estimate from the user's monthly use, shown only until the DisCo states the kWh. */
    val kwhEstimate: Double,
    val status: CompensationStatus,
    val token: String?,
    val claimedAt: Long?,
    val issuedAt: Long?
) {
    val kwhOwedText: String
        get() = kwhIssued?.let { "%.1f kWh".format(it) } ?: "about %.1f kWh (estimate)".format(kwhEstimate)
}

object CompensationTokens {
    private const val FILE = "bright_compensation_tokens"
    private const val HOUR_MS = 3_600_000L

    /** Average kWh per hour from the user's monthly use (Refund Tracker setting), 150 kWh if unset. */
    private fun kwhPerHour(context: Context): Double {
        val monthly = context.getSharedPreferences(com.example.data.solutions.SolutionsPrefs.FILE, Context.MODE_PRIVATE)
            .getFloat("units_month_kwh", 150f).toDouble().takeIf { it > 0 } ?: 150.0
        return monthly / (30 * 24)
    }

    /** Overdue tickets only: covered fault types whose repair ran (or is running) past the limit. */
    fun entries(context: Context, complaints: List<Complaint>, now: Long = System.currentTimeMillis()): List<CompensationEntry> {
        val perHour = kwhPerHour(context)
        return complaints.mapNotNull { c ->
            val sla = CompensationSla.slaHoursFor(c.faultType) ?: return@mapNotNull null
            val end = c.resolvedAt ?: now
            val taken = ((end - c.reportedAt) / HOUR_MS).toInt()
            val late = taken - sla
            if (late <= 0) return@mapNotNull null
            val saved = record(context, c.id)
            val token = saved?.optString("token")?.takeIf { it.length == 20 }
            val status = when {
                token != null -> CompensationStatus.ISSUED
                saved?.optBoolean("claimed") == true -> CompensationStatus.CLAIMED
                else -> CompensationStatus.PENDING
            }
            CompensationEntry(
                complaintId = c.id,
                faultTitle = c.title,
                faultType = c.faultType,
                slaHours = sla,
                hoursTaken = taken,
                hoursLate = late,
                stillOpen = c.resolvedAt == null,
                kwhIssued = saved?.optDouble("kwh")?.takeIf { !it.isNaN() && it > 0 },
                kwhEstimate = late * perHour,
                status = status,
                token = token,
                claimedAt = saved?.optLong("claimedAt")?.takeIf { it > 0 },
                issuedAt = saved?.optLong("issuedAt")?.takeIf { it > 0 }
            )
        }.sortedByDescending { it.hoursLate }
    }

    private fun record(context: Context, complaintId: String): JSONObject? {
        val raw = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(complaintId, null) ?: return null
        return try { JSONObject(raw) } catch (e: Exception) { null }
    }

    private fun save(context: Context, complaintId: String, o: JSONObject) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(complaintId, o.toString()).apply()
    }

    /** The user sent the claim to the DisCo. */
    fun markClaimed(context: Context, complaintId: String) {
        val o = record(context, complaintId) ?: JSONObject()
        o.put("claimed", true).put("claimedAt", System.currentTimeMillis())
        save(context, complaintId, o)
    }

    /**
     * Records the 20-digit compensation token the DisCo issued for this ticket. Returns an error
     * message, or null when saved.
     */
    fun recordIssuedToken(context: Context, complaintId: String, token: String, kwh: Double?): String? {
        if (!StsToken.isValid(token)) return "A compensation token is exactly 20 digits."
        val o = record(context, complaintId) ?: JSONObject()
        o.put("claimed", true)
        if (!o.has("claimedAt")) o.put("claimedAt", System.currentTimeMillis())
        o.put("token", StsToken.digits(token)).put("issuedAt", System.currentTimeMillis())
        if (kwh != null && kwh > 0) o.put("kwh", kwh) else o.remove("kwh")
        save(context, complaintId, o)
        return null
    }

    fun clear(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().clear().apply()
}
