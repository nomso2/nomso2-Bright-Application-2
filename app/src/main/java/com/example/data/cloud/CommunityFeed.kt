package com.example.data.cloud

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.database.CommunityStatsEntity
import com.example.data.database.ComplaintEntity
import com.example.data.database.CrewSightingEntity
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * Real community data pooled in Firestore by Bright users.
 *
 * community_reports: one anonymised summary per complaint. It holds the transformer, feeder, DisCo,
 * state, fault type, start/restore times, supply hours and the Firebase uid only. It never
 * holds a name, phone number or meter number.
 *
 * crew_sightings: residents' "crew seen / working / left" posts for their transformer.
 *
 * Results are cached in Room so the screens still show the last figures offline. Every call
 * is best effort and returns null when Firebase isn't configured or reachable.
 */
object CommunityFeed {
    private const val REPORTS = "community_reports"
    private const val SIGHTINGS = "crew_sightings"
    private const val WINDOW_MS = 30L * 24 * 60 * 60 * 1000

    enum class CrewKind(val label: String) { SEEN("Crew seen"), WORKING("Crew working"), LEFT("Crew left") }

    /** Publishes the anonymised summary of one complaint. */
    suspend fun publishReport(
        context: Context,
        complaint: ComplaintEntity,
        state: String,
        supplyHoursPerDay: Double?
    ): Boolean {
        val uid = BrightFirebase.uid(context) ?: return false
        val db = BrightFirebase.firestore(context) ?: return false
        if (complaint.transformerId.isBlank() && complaint.feederName.isBlank()) return false
        val restoredAt = complaint.resolvedAt
        val data = hashMapOf<String, Any?>(
            "uid" to uid,
            "transformerId" to complaint.transformerId,
            "feederName" to complaint.feederName,
            "discoCode" to complaint.discoCode,
            "state" to state,
            "faultType" to complaint.faultTypeName,
            "startedAt" to complaint.reportedAt,
            "restoredAt" to restoredAt,
            "restoreHours" to restoredAt?.let { (it - complaint.reportedAt) / 3_600_000.0 }?.takeIf { it >= 0 },
            "supplyHoursPerDay" to supplyHoursPerDay
        )
        return BrightFirebase.network {
            db.collection(REPORTS).document("${uid}_${complaint.id}").set(data, SetOptions.merge()).await()
            true
        } ?: false
    }

    /** Delete account: removes this user's community summaries and crew sightings. */
    suspend fun deleteMine(context: Context) {
        val uid = BrightFirebase.currentUid(context) ?: return
        val db = BrightFirebase.firestore(context) ?: return
        BrightFirebase.network {
            listOf(REPORTS, SIGHTINGS).forEach { col ->
                db.collection(col).whereEqualTo("uid", uid).get().await().documents.forEach { it.reference.delete().await() }
            }
        }
    }

    /**
     * Recomputes community averages for one scope from Firestore and caches them.
     * [field] is "transformerId", "feederName" or "discoCode".
     */
    suspend fun refreshStats(context: Context, field: String, value: String): CommunityStatsEntity? {
        if (value.isBlank()) return null
        val db = BrightFirebase.firestore(context) ?: return null
        BrightFirebase.uid(context) ?: return null
        val docs = BrightFirebase.network {
            db.collection(REPORTS).whereEqualTo(field, value).limit(1000).get().await().documents
        } ?: return null
        val cutoff = System.currentTimeMillis() - WINDOW_MS
        val recent = docs.mapNotNull { it.data }.filter { ((it["startedAt"] as? Number)?.toLong() ?: 0L) >= cutoff }
        val restore = recent.mapNotNull { (it["restoreHours"] as? Number)?.toDouble() }
        val supply = recent.mapNotNull { (it["supplyHoursPerDay"] as? Number)?.toDouble() }
        val transformers = recent.mapNotNull { (it["transformerId"] as? String)?.takeIf { t -> t.isNotBlank() } }.toSet()
        val stats = CommunityStatsEntity(
            scope = scopeKey(field, value),
            reportCount = recent.size,
            neighbourCount = recent.mapNotNull { it["uid"] as? String }.toSet().size,
            avgRestoreHours = restore.takeIf { it.isNotEmpty() }?.average(),
            avgSupplyHours = supply.takeIf { it.isNotEmpty() }?.average(),
            reportsPerTransformer = if (transformers.isEmpty()) null else recent.size.toDouble() / transformers.size,
            fetchedAt = System.currentTimeMillis()
        )
        withContext(Dispatchers.IO) { AppDatabase.getDatabase(context).communityStatsDao().upsert(stats) }
        return stats
    }

    fun scopeKey(field: String, value: String) = when (field) {
        "transformerId" -> "T:$value"
        "feederName" -> "F:$value"
        else -> "D:$value"
    }

    /** Posts a crew sighting for [transformerId]. */
    suspend fun postSighting(context: Context, transformerId: String, discoCode: String, kind: CrewKind, note: String): Boolean {
        if (transformerId.isBlank()) return false
        val uid = BrightFirebase.uid(context) ?: return false
        val db = BrightFirebase.firestore(context) ?: return false
        val data = hashMapOf<String, Any?>(
            "uid" to uid,
            "transformerId" to transformerId,
            "discoCode" to discoCode,
            "kind" to kind.name,
            "note" to note.take(140),
            "createdAt" to System.currentTimeMillis()
        )
        val ok = BrightFirebase.network {
            db.collection(SIGHTINGS).add(data).await()
            true
        } ?: false
        if (ok) refreshSightings(context, transformerId)
        return ok
    }

    /** Loads the latest sightings (last 48 h) for [transformerId] into the Room cache. */
    suspend fun refreshSightings(context: Context, transformerId: String): Boolean {
        if (transformerId.isBlank()) return false
        val db = BrightFirebase.firestore(context) ?: return false
        val me = BrightFirebase.uid(context) ?: return false
        val docs = BrightFirebase.network {
            db.collection(SIGHTINGS).whereEqualTo("transformerId", transformerId).limit(300).get().await().documents
        } ?: return false
        val cutoff = System.currentTimeMillis() - 48L * 60 * 60 * 1000
        val items = docs.mapNotNull { d ->
            val m = d.data ?: return@mapNotNull null
            val created = (m["createdAt"] as? Number)?.toLong() ?: return@mapNotNull null
            if (created < cutoff) return@mapNotNull null
            val uid = m["uid"] as? String ?: ""
            CrewSightingEntity(
                id = d.id,
                transformerId = transformerId,
                kind = m["kind"] as? String ?: CrewKind.SEEN.name,
                note = m["note"] as? String ?: "",
                createdAt = created,
                reporterKey = hash(uid),
                isMine = uid == me
            )
        }
        withContext(Dispatchers.IO) {
            val dao = AppDatabase.getDatabase(context).crewSightingDao()
            dao.clearFor(transformerId)
            dao.upsertAll(items)
        }
        return true
    }

    /** One-way short hash so the phone can count distinct reporters without keeping their uid. */
    private fun hash(s: String): String =
        MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).take(6).joinToString("") { "%02x".format(it) }
}
