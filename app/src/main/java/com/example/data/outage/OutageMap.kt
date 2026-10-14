package com.example.data.outage

import android.content.Context
import com.example.data.database.ComplaintEntity
import com.example.data.solutions.FaultReportEntity
import com.example.model.ComplaintStatus
import com.example.model.StateDisCoMap
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.roundToLong

/**
 * One pin on the outage map. Every pin comes from a real report: either one of the user's own
 * complaints that carries a GPS fix, or anonymised community_reports published by Bright users.
 * Nothing here is ever generated or seeded.
 */
data class OutagePin(
    val id: String,
    /** Short text on the pin itself ("YOU", or the number of reports). */
    val badge: String,
    val title: String,
    /** Where, in words: "Your GPS pin" or "Lagos" etc. */
    val place: String,
    /** Canonical state name (StateDisCoMap), or "" when unknown. */
    val state: String,
    val discoCode: String,
    val latitude: Double,
    val longitude: Double,
    val isMine: Boolean,
    /** True when the pin sits at the state capital because the exact spot was not shared. */
    val isApproximate: Boolean,
    val reportCount: Int,
    /** True when every report behind the pin says power is back / the ticket is resolved. */
    val isRestored: Boolean,
    /** Newest report time behind this pin (epoch ms). */
    val latestAt: Long,
    /** Where the pin came from, shown on the card. */
    val sourceLabel: String
) {
    val colorHex: String get() = if (isRestored) "#22C55E" else "#EF4444"
    val statusLabel: String get() = if (isRestored) "Power back" else "Outage reported"
}

/** One anonymised community report, as cached on the phone (no uid, name, phone or meter). */
data class CommunityOutageReport(
    val key: String,
    val state: String,
    val discoCode: String,
    val faultType: String,
    val startedAt: Long,
    val restoredAt: Long?,
    /** Rounded to 2 decimals (about 1 km) by the publisher, or null when not shared. */
    val approxLat: Double?,
    val approxLng: Double?
)

/** Last community fetch, kept on the phone so the map still works offline. */
data class CommunityOutageSnapshot(
    val reports: List<CommunityOutageReport> = emptyList(),
    /** When the reports were last fetched from Firestore (epoch ms), or null if never. */
    val fetchedAt: Long? = null
)

/** Offline cache for community outage reports: a small JSON file in the app's private storage. */
object CommunityOutageCache {
    private const val FILE = "community_outages_cache.json"

    fun load(context: Context?): CommunityOutageSnapshot {
        val ctx = context ?: return CommunityOutageSnapshot()
        return try {
            val f = File(ctx.filesDir, FILE)
            if (!f.exists()) return CommunityOutageSnapshot()
            val root = JSONObject(f.readText())
            val arr = root.optJSONArray("reports") ?: JSONArray()
            val list = (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                CommunityOutageReport(
                    key = o.optString("key"),
                    state = o.optString("state"),
                    discoCode = o.optString("discoCode"),
                    faultType = o.optString("faultType"),
                    startedAt = o.optLong("startedAt"),
                    restoredAt = if (o.has("restoredAt") && !o.isNull("restoredAt")) o.optLong("restoredAt") else null,
                    approxLat = if (o.has("approxLat") && !o.isNull("approxLat")) o.optDouble("approxLat") else null,
                    approxLng = if (o.has("approxLng") && !o.isNull("approxLng")) o.optDouble("approxLng") else null
                )
            }
            CommunityOutageSnapshot(list, if (root.has("fetchedAt")) root.optLong("fetchedAt") else null)
        } catch (e: Exception) {
            CommunityOutageSnapshot()
        }
    }

    fun save(context: Context?, snapshot: CommunityOutageSnapshot) {
        val ctx = context ?: return
        try {
            val arr = JSONArray()
            snapshot.reports.forEach { r ->
                arr.put(
                    JSONObject()
                        .put("key", r.key)
                        .put("state", r.state)
                        .put("discoCode", r.discoCode)
                        .put("faultType", r.faultType)
                        .put("startedAt", r.startedAt)
                        .put("restoredAt", r.restoredAt ?: JSONObject.NULL)
                        .put("approxLat", r.approxLat ?: JSONObject.NULL)
                        .put("approxLng", r.approxLng ?: JSONObject.NULL)
                )
            }
            val root = JSONObject().put("reports", arr)
            snapshot.fetchedAt?.let { root.put("fetchedAt", it) }
            File(ctx.filesDir, FILE).writeText(root.toString())
        } catch (e: Exception) {
            // Cache is best effort; the next refresh tries again.
        }
    }

    fun clear(context: Context?) {
        try {
            context?.let { File(it.filesDir, FILE).delete() }
        } catch (e: Exception) {
            // Nothing to clear.
        }
    }
}

/**
 * State capitals, used only to place community reports that did not share a location.
 * Such pins are always marked "approximate" on screen.
 */
object StateCentroids {
    private val capitals: Map<String, Triple<String, Double, Double>> = mapOf(
        "Abia" to Triple("Umuahia", 5.53, 7.49),
        "Adamawa" to Triple("Yola", 9.21, 12.48),
        "Akwa Ibom" to Triple("Uyo", 5.04, 7.91),
        "Anambra" to Triple("Awka", 6.21, 7.07),
        "Bauchi" to Triple("Bauchi", 10.31, 9.84),
        "Bayelsa" to Triple("Yenagoa", 4.93, 6.27),
        "Benue" to Triple("Makurdi", 7.73, 8.54),
        "Borno" to Triple("Maiduguri", 11.83, 13.15),
        "Cross River" to Triple("Calabar", 4.95, 8.32),
        "Delta" to Triple("Asaba", 6.20, 6.73),
        "Ebonyi" to Triple("Abakaliki", 6.32, 8.11),
        "Edo" to Triple("Benin City", 6.34, 5.62),
        "Ekiti" to Triple("Ado-Ekiti", 7.62, 5.22),
        "Enugu" to Triple("Enugu", 6.44, 7.49),
        "FCT" to Triple("Abuja", 9.06, 7.49),
        "Gombe" to Triple("Gombe", 10.29, 11.17),
        "Imo" to Triple("Owerri", 5.48, 7.03),
        "Jigawa" to Triple("Dutse", 11.76, 9.34),
        "Kaduna" to Triple("Kaduna", 10.52, 7.44),
        "Kano" to Triple("Kano", 12.00, 8.52),
        "Katsina" to Triple("Katsina", 12.99, 7.60),
        "Kebbi" to Triple("Birnin Kebbi", 12.45, 4.20),
        "Kogi" to Triple("Lokoja", 7.80, 6.74),
        "Kwara" to Triple("Ilorin", 8.50, 4.55),
        "Lagos" to Triple("Ikeja", 6.60, 3.35),
        "Nasarawa" to Triple("Lafia", 8.49, 8.52),
        "Niger" to Triple("Minna", 9.61, 6.56),
        "Ogun" to Triple("Abeokuta", 7.15, 3.35),
        "Ondo" to Triple("Akure", 7.25, 5.19),
        "Osun" to Triple("Osogbo", 7.77, 4.56),
        "Oyo" to Triple("Ibadan", 7.38, 3.95),
        "Plateau" to Triple("Jos", 9.90, 8.86),
        "Rivers" to Triple("Port Harcourt", 4.82, 7.03),
        "Sokoto" to Triple("Sokoto", 13.06, 5.24),
        "Taraba" to Triple("Jalingo", 8.89, 11.36),
        "Yobe" to Triple("Damaturu", 11.75, 11.96),
        "Zamfara" to Triple("Gusau", 12.16, 6.66)
    )

    /** Canonical state name for a free-text value ("Lagos State" -> "Lagos"), or "". */
    fun canonical(raw: String?): String = StateDisCoMap.forState(raw)?.state ?: ""

    /** (capital, lat, lng) for a state, or null. */
    fun capitalOf(raw: String?): Triple<String, Double, Double>? = capitals[canonical(raw)]
}

/** Builds the map pins from real sources only. */
object OutageMapBuilder {
    /** Community reports older than this are not shown. */
    const val COMMUNITY_WINDOW_MS = 48L * 60 * 60 * 1000

    /** The user's own resolved reports stay on the map this long; open ones stay until resolved. */
    const val OWN_RESOLVED_WINDOW_MS = 48L * 60 * 60 * 1000

    fun roundCoord(v: Double): Double = (v * 100.0).roundToLong() / 100.0

    fun build(
        complaints: List<ComplaintEntity>,
        solutionReports: List<FaultReportEntity>,
        community: CommunityOutageSnapshot,
        userState: String,
        now: Long = System.currentTimeMillis()
    ): List<OutagePin> {
        val pins = mutableListOf<OutagePin>()

        // 1. The user's own complaints that carry a GPS fix (reports raised with location are
        //    mirrored into the complaints table as SOL-<reference>).
        val byRef = solutionReports.associateBy { "SOL-${it.reference}" }
        complaints.forEach { c ->
            if (c.id.startsWith("SEED-") || c.id.startsWith("CMP-SEED")) return@forEach
            val r = byRef[c.id] ?: return@forEach
            val lat = r.latitude ?: return@forEach
            val lng = r.longitude ?: return@forEach
            if (lat == 0.0 && lng == 0.0) return@forEach
            val resolved = c.statusName == ComplaintStatus.RESOLVED.name || c.statusName == "CLOSED"
            val lastChange = c.resolvedAt ?: c.updatedAt
            if (resolved && now - lastChange > OWN_RESOLVED_WINDOW_MS) return@forEach
            pins += OutagePin(
                id = "MINE-${c.id}",
                badge = "YOU",
                title = c.title.ifBlank { "Your outage report" },
                place = "Your GPS pin",
                state = StateCentroids.canonical(userState),
                discoCode = c.discoCode,
                latitude = lat,
                longitude = lng,
                isMine = true,
                isApproximate = false,
                reportCount = 1,
                isRestored = resolved,
                latestAt = c.reportedAt,
                sourceLabel = "Your report ${c.id.removePrefix("SOL-")}"
            )
        }

        // 2. Anonymised community reports from the last 48 hours, grouped so no single
        //    neighbour can be singled out: by ~1 km cell when a rough location was shared,
        //    otherwise by state (drawn at the state capital and marked approximate).
        val cutoff = now - COMMUNITY_WINDOW_MS
        val recent = community.reports.filter { it.startedAt in cutoff..(now + 10 * 60 * 1000L) }
        val groups = recent.groupBy { r ->
            if (r.approxLat != null && r.approxLng != null) "G:${roundCoord(r.approxLat)},${roundCoord(r.approxLng)}"
            else "S:${StateCentroids.canonical(r.state)}"
        }
        groups.forEach { (key, reports) ->
            val first = reports.first()
            val state = StateCentroids.canonical(first.state)
            val approximate = key.startsWith("S:")
            val coords: Pair<Double, Double> = if (!approximate) {
                roundCoord(first.approxLat!!) to roundCoord(first.approxLng!!)
            } else {
                val cap = StateCentroids.capitalOf(state) ?: return@forEach
                cap.second to cap.third
            }
            val restored = reports.all { it.restoredAt != null }
            val discos = reports.map { it.discoCode }.filter { it.isNotBlank() }.distinct()
            val n = reports.size
            pins += OutagePin(
                id = "COM-$key",
                badge = n.toString(),
                title = if (n == 1) "1 outage report" else "$n outage reports",
                place = when {
                    state.isBlank() -> "Location not shared"
                    approximate -> (if (state == "FCT") "FCT" else "$state State") + " (exact spot not shared)"
                    else -> "Within about 1 km, " + (if (state == "FCT") "FCT" else "$state State")
                },
                state = state,
                discoCode = discos.joinToString(" / "),
                latitude = coords.first,
                longitude = coords.second,
                isMine = false,
                isApproximate = approximate,
                reportCount = n,
                isRestored = restored,
                latestAt = reports.maxOf { it.startedAt },
                sourceLabel = "Bright users, last 48 hours"
            )
        }

        return pins.sortedWith(compareByDescending<OutagePin> { it.isMine }.thenByDescending { it.latestAt })
    }
}
