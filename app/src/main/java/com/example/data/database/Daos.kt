package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints ORDER BY reportedAt DESC")
    fun getAllComplaints(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE meterNumber = :meterNumber ORDER BY reportedAt DESC")
    fun getComplaintsForMeter(meterNumber: String): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE meterNumber = :meterNumber AND statusName NOT IN ('RESOLVED', 'CLOSED') ORDER BY reportedAt DESC")
    fun getActiveComplaintsForMeter(meterNumber: String): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE meterNumber = :meterNumber AND statusName IN ('RESOLVED', 'CLOSED') ORDER BY reportedAt DESC")
    fun getHistoricalComplaintsForMeter(meterNumber: String): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE id = :id LIMIT 1")
    suspend fun getComplaintById(id: String): ComplaintEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(complaints: List<ComplaintEntity>)

    @Update
    suspend fun updateComplaint(complaint: ComplaintEntity)

    @Query("UPDATE complaints SET upvotesCount = upvotesCount + 1 WHERE id = :id")
    suspend fun upvoteComplaint(id: String)

    @Query("UPDATE complaints SET escalationTierLevel = :tierLevel, updatedAt = :updatedAt WHERE id = :id")
    suspend fun escalateComplaint(id: String, tierLevel: Int, updatedAt: Long)

    @Query("UPDATE complaints SET statusName = :status, userSatisfaction = :rating, resolutionNotes = :notes, updatedAt = :updatedAt, resolvedAt = :resolvedAt WHERE id = :id")
    suspend fun resolveComplaint(id: String, status: String, rating: Int, notes: String, updatedAt: Long, resolvedAt: Long)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setUserProfile(profile: UserProfileEntity)
}

@Dao
interface VandalismDao {
    @Query("SELECT * FROM vandalism_reports ORDER BY reportedAt DESC")
    fun getAllVandalismReports(): Flow<List<VandalismEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: VandalismEntity)
}

@Dao
interface BillingDisputeDao {
    @Query("SELECT * FROM billing_disputes WHERE meterNumber = :meterNumber ORDER BY createdAt DESC")
    fun getDisputesForMeter(meterNumber: String): Flow<List<BillingDisputeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispute(dispute: BillingDisputeEntity)
}

@Dao
interface ApplianceClaimDao {
    @Query("SELECT * FROM appliance_claims WHERE meterNumber = :meterNumber ORDER BY createdAt DESC")
    fun getClaimsForMeter(meterNumber: String): Flow<List<ApplianceClaimEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaim(claim: ApplianceClaimEntity)
}

@Dao
interface StreetHazardDao {
    @Query("SELECT * FROM street_hazards ORDER BY reportedAt DESC")
    fun getAllStreetHazards(): Flow<List<StreetHazardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHazard(hazard: StreetHazardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(hazards: List<StreetHazardEntity>)

    @Query("UPDATE street_hazards SET verifiedCount = verifiedCount + 1 WHERE id = :id")
    suspend fun upvoteHazard(id: String)

    @Query("UPDATE street_hazards SET isDispatched = 1 WHERE id = :id")
    suspend fun markDispatched(id: String)
}

