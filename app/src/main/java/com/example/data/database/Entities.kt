package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Complaint
import com.example.model.ComplaintStatus
import com.example.model.EscalationTier
import com.example.model.FaultType
import com.example.model.FeederBand
import com.example.model.UserProfile
import com.example.model.VandalismReport
import com.example.model.BillingDispute

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val meterNumber: String,
    val customerName: String,
    val phoneNumber: String,
    val streetAddress: String,
    val lga: String,
    val state: String,
    val discoCode: String,
    val feederName: String,
    val feederBand: String,
    val transformerId: String,
    val isPrepaid: Boolean,
    val connectedHouseholdsCount: Int
) {
    fun toDomain(): UserProfile = UserProfile(
        meterNumber = meterNumber,
        customerName = customerName,
        phoneNumber = phoneNumber,
        streetAddress = streetAddress,
        lga = lga,
        state = state,
        discoCode = discoCode,
        feederName = feederName,
        feederBand = try { FeederBand.valueOf(feederBand) } catch (e: Exception) { FeederBand.BAND_A },
        transformerId = transformerId,
        isPrepaid = isPrepaid,
        connectedHouseholdsCount = connectedHouseholdsCount
    )

    companion object {
        fun fromDomain(u: UserProfile) = UserProfileEntity(
            meterNumber = u.meterNumber,
            customerName = u.customerName,
            phoneNumber = u.phoneNumber,
            streetAddress = u.streetAddress,
            lga = u.lga,
            state = u.state,
            discoCode = u.discoCode,
            feederName = u.feederName,
            feederBand = u.feederBand.name,
            transformerId = u.transformerId,
            isPrepaid = u.isPrepaid,
            connectedHouseholdsCount = u.connectedHouseholdsCount
        )
    }
}

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey val id: String,
    val meterNumber: String,
    val title: String,
    val description: String,
    val faultTypeName: String,
    val isHazardEmergency: Boolean,
    val statusName: String,
    val escalationTierLevel: Int,
    val discoCode: String,
    val transformerId: String,
    val feederName: String,
    val reportedAt: Long,
    val updatedAt: Long,
    val escalationDeadline: Long,
    val upvotesCount: Int,
    val assignedCrewName: String?,
    val assignedCrewPhone: String?,
    val etaMinutes: Int?,
    val resolutionNotes: String?,
    val userSatisfaction: Int?,
    val imageUri: String?,
    val autoClusteredCount: Int,
    val resolvedAt: Long? = null,
    val isVideo: Boolean = false
) {
    fun toDomain(): Complaint {
        val fault = try { FaultType.valueOf(faultTypeName) } catch (e: Exception) { FaultType.TOTAL_BLACKOUT }
        val status = try { ComplaintStatus.valueOf(statusName) } catch (e: Exception) { ComplaintStatus.LOGGED }
        val tier = EscalationTier.entries.find { it.level == escalationTierLevel } ?: EscalationTier.LEVEL_1
        return Complaint(
            id = id,
            meterNumber = meterNumber,
            title = title,
            description = description,
            faultType = fault,
            isHazardEmergency = isHazardEmergency,
            status = status,
            escalationTier = tier,
            discoCode = discoCode,
            transformerId = transformerId,
            feederName = feederName,
            reportedAt = reportedAt,
            updatedAt = updatedAt,
            escalationDeadline = escalationDeadline,
            upvotesCount = upvotesCount,
            assignedCrewName = assignedCrewName,
            assignedCrewPhone = assignedCrewPhone,
            etaMinutes = etaMinutes,
            resolutionNotes = resolutionNotes,
            userSatisfaction = userSatisfaction,
            imageUri = imageUri,
            autoClusteredCount = autoClusteredCount,
            resolvedAt = resolvedAt,
            isVideo = isVideo
        )
    }

    companion object {
        fun fromDomain(c: Complaint) = ComplaintEntity(
            id = c.id,
            meterNumber = c.meterNumber,
            title = c.title,
            description = c.description,
            faultTypeName = c.faultType.name,
            isHazardEmergency = c.isHazardEmergency,
            statusName = c.status.name,
            escalationTierLevel = c.escalationTier.level,
            discoCode = c.discoCode,
            transformerId = c.transformerId,
            feederName = c.feederName,
            reportedAt = c.reportedAt,
            updatedAt = c.updatedAt,
            escalationDeadline = c.escalationDeadline,
            upvotesCount = c.upvotesCount,
            assignedCrewName = c.assignedCrewName,
            assignedCrewPhone = c.assignedCrewPhone,
            etaMinutes = c.etaMinutes,
            resolutionNotes = c.resolutionNotes,
            userSatisfaction = c.userSatisfaction,
            imageUri = c.imageUri,
            autoClusteredCount = c.autoClusteredCount,
            resolvedAt = c.resolvedAt,
            isVideo = c.isVideo
        )
    }
}

@Entity(tableName = "vandalism_reports")
data class VandalismEntity(
    @PrimaryKey val id: String,
    val incidentType: String,
    val location: String,
    val landmark: String,
    val discoCode: String,
    val reportedAt: Long,
    val isAnonymous: Boolean,
    val description: String,
    val status: String,
    val suspectDetails: String?
) {
    fun toDomain() = VandalismReport(
        id = id,
        incidentType = incidentType,
        location = location,
        landmark = landmark,
        discoCode = discoCode,
        reportedAt = reportedAt,
        isAnonymous = isAnonymous,
        description = description,
        status = status,
        suspectDetails = suspectDetails
    )

    companion object {
        fun fromDomain(v: VandalismReport) = VandalismEntity(
            id = v.id,
            incidentType = v.incidentType,
            location = v.location,
            landmark = v.landmark,
            discoCode = v.discoCode,
            reportedAt = v.reportedAt,
            isAnonymous = v.isAnonymous,
            description = v.description,
            status = v.status,
            suspectDetails = v.suspectDetails
        )
    }
}

@Entity(tableName = "billing_disputes")
data class BillingDisputeEntity(
    @PrimaryKey val id: String,
    val meterNumber: String,
    val disputeType: String,
    val disputedAmountNgn: Double,
    val billingMonth: String,
    val discoCode: String,
    val description: String,
    val status: String,
    val createdAt: Long
) {
    fun toDomain() = BillingDispute(
        id = id,
        meterNumber = meterNumber,
        disputeType = disputeType,
        disputedAmountNgn = disputedAmountNgn,
        billingMonth = billingMonth,
        discoCode = discoCode,
        description = description,
        status = status,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(b: BillingDispute) = BillingDisputeEntity(
            id = b.id,
            meterNumber = b.meterNumber,
            disputeType = b.disputeType,
            disputedAmountNgn = b.disputedAmountNgn,
            billingMonth = b.billingMonth,
            discoCode = b.discoCode,
            description = b.description,
            status = b.status,
            createdAt = b.createdAt
        )
    }
}
