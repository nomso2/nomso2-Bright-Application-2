package com.example.data.cloud

import android.content.Context
import com.example.data.database.ComplaintEntity
import com.google.firebase.firestore.SetOptions

/**
 * Backs up the user's complaint history to Firestore at users/{uid}/complaints/{id} so it
 * survives a reinstall, and restores it into Room. Private to the signed-in user (firestore.rules).
 *
 * Every call is best effort and returns null/false when Firebase is not configured or offline;
 * Room on the phone stays the source of truth.
 */
class ComplaintCloudBackup(context: Context) {
    private val appContext = context.applicationContext

    private suspend fun collection() = BrightFirebase.uid(appContext)?.let { uid ->
        BrightFirebase.firestore(appContext)
            ?.collection("users")?.document(uid)?.collection("complaints")
    }

    suspend fun upload(entity: ComplaintEntity): Boolean {
        val col = collection() ?: return false
        return BrightFirebase.network {
            col.document(entity.id).set(toMap(entity), SetOptions.merge()).await()
            true
        } ?: false
    }

    /** All backed-up complaints, or null when the cloud can't be reached. */
    suspend fun fetchAll(): List<ComplaintEntity>? {
        val col = collection() ?: return null
        return BrightFirebase.network {
            col.get().await().documents.mapNotNull { doc ->
                doc.data?.let { fromMap(doc.id, it) }
            }
        }
    }

    /** Delete account: removes every backed-up complaint for this user. */
    suspend fun deleteAll(): Boolean {
        val col = collection() ?: return false
        return BrightFirebase.network {
            col.get().await().documents.forEach { it.reference.delete().await() }
            true
        } ?: false
    }

    companion object {
        fun toMap(e: ComplaintEntity): Map<String, Any?> = mapOf(
            "id" to e.id,
            "meterNumber" to e.meterNumber,
            "title" to e.title,
            "description" to e.description,
            "faultTypeName" to e.faultTypeName,
            "isHazardEmergency" to e.isHazardEmergency,
            "statusName" to e.statusName,
            "escalationTierLevel" to e.escalationTierLevel,
            "discoCode" to e.discoCode,
            "transformerId" to e.transformerId,
            "feederName" to e.feederName,
            "reportedAt" to e.reportedAt,
            "updatedAt" to e.updatedAt,
            "escalationDeadline" to e.escalationDeadline,
            "upvotesCount" to e.upvotesCount,
            "assignedCrewName" to e.assignedCrewName,
            "assignedCrewPhone" to e.assignedCrewPhone,
            "etaMinutes" to e.etaMinutes,
            "resolutionNotes" to e.resolutionNotes,
            "userSatisfaction" to e.userSatisfaction,
            "imageUri" to e.imageUri,
            "autoClusteredCount" to e.autoClusteredCount,
            "resolvedAt" to e.resolvedAt,
            "isVideo" to e.isVideo
        )

        fun fromMap(id: String, m: Map<String, Any?>): ComplaintEntity? {
            fun str(k: String) = m[k] as? String
            fun long(k: String) = (m[k] as? Number)?.toLong()
            fun int(k: String) = (m[k] as? Number)?.toInt()
            fun bool(k: String) = m[k] as? Boolean
            val reportedAt = long("reportedAt") ?: return null
            return ComplaintEntity(
                id = id,
                meterNumber = str("meterNumber") ?: "",
                title = str("title") ?: "Fault report",
                description = str("description") ?: "",
                faultTypeName = str("faultTypeName") ?: "TOTAL_BLACKOUT",
                isHazardEmergency = bool("isHazardEmergency") ?: false,
                statusName = str("statusName") ?: "LOGGED",
                escalationTierLevel = int("escalationTierLevel") ?: 1,
                discoCode = str("discoCode") ?: "",
                transformerId = str("transformerId") ?: "",
                feederName = str("feederName") ?: "",
                reportedAt = reportedAt,
                updatedAt = long("updatedAt") ?: reportedAt,
                escalationDeadline = long("escalationDeadline") ?: reportedAt,
                upvotesCount = int("upvotesCount") ?: 1,
                assignedCrewName = str("assignedCrewName"),
                assignedCrewPhone = str("assignedCrewPhone"),
                etaMinutes = int("etaMinutes"),
                resolutionNotes = str("resolutionNotes"),
                userSatisfaction = int("userSatisfaction"),
                imageUri = str("imageUri"),
                autoClusteredCount = int("autoClusteredCount") ?: 1,
                resolvedAt = long("resolvedAt"),
                isVideo = bool("isVideo") ?: false
            )
        }
    }
}
