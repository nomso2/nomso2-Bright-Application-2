package com.example.data.solutions

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SolutionsDao {
    // Fault reports
    @Query("SELECT * FROM sol_fault_reports ORDER BY createdAt DESC")
    fun reports(): Flow<List<FaultReportEntity>>

    @Query("SELECT * FROM sol_fault_reports WHERE id = :id LIMIT 1")
    suspend fun reportById(id: Long): FaultReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: FaultReportEntity): Long

    @Update
    suspend fun updateReport(report: FaultReportEntity)

    @Delete
    suspend fun deleteReport(report: FaultReportEntity)

    // Supply log
    @Query("SELECT * FROM sol_supply_events ORDER BY timestamp ASC")
    fun supplyEvents(): Flow<List<SupplyEventEntity>>

    @Query("SELECT * FROM sol_supply_events ORDER BY timestamp DESC LIMIT 1")
    suspend fun lastSupplyEvent(): SupplyEventEntity?

    @Insert
    suspend fun insertSupplyEvent(event: SupplyEventEntity): Long

    @Delete
    suspend fun deleteSupplyEvent(event: SupplyEventEntity)

    // Refund claims
    @Query("SELECT * FROM sol_refund_claims ORDER BY createdAt DESC")
    fun refundClaims(): Flow<List<RefundClaimEntity>>

    @Insert
    suspend fun insertRefundClaim(claim: RefundClaimEntity): Long

    @Update
    suspend fun updateRefundClaim(claim: RefundClaimEntity)

    // Voice notes
    @Query("SELECT * FROM sol_voice_notes ORDER BY createdAt DESC")
    fun voiceNotes(): Flow<List<VoiceNoteEntity>>

    @Insert
    suspend fun insertVoiceNote(note: VoiceNoteEntity): Long

    @Update
    suspend fun updateVoiceNote(note: VoiceNoteEntity)

    @Delete
    suspend fun deleteVoiceNote(note: VoiceNoteEntity)

    // Forum
    @Query("SELECT * FROM sol_forum_posts WHERE transformerId = :transformerId ORDER BY createdAt DESC")
    fun forumPosts(transformerId: String): Flow<List<ForumPostEntity>>

    @Query("SELECT COUNT(*) FROM sol_forum_posts WHERE transformerId = :transformerId")
    suspend fun forumPostCount(transformerId: String): Int

    @Insert
    suspend fun insertForumPost(post: ForumPostEntity): Long

    @Delete
    suspend fun deleteForumPost(post: ForumPostEntity)

    // Whistleblowing
    @Query("SELECT * FROM sol_whistleblow_reports ORDER BY createdAt DESC")
    fun whistleblowReports(): Flow<List<WhistleblowEntity>>

    @Insert
    suspend fun insertWhistleblow(report: WhistleblowEntity): Long

    @Update
    suspend fun updateWhistleblow(report: WhistleblowEntity)

    @Delete
    suspend fun deleteWhistleblow(report: WhistleblowEntity)

    // Meter waitlist
    @Query("SELECT * FROM sol_meter_waitlist ORDER BY paidOn DESC")
    fun meterWaitlist(): Flow<List<MeterWaitlistEntity>>

    @Insert
    suspend fun insertMeterWait(entry: MeterWaitlistEntity): Long

    @Update
    suspend fun updateMeterWait(entry: MeterWaitlistEntity)

    @Delete
    suspend fun deleteMeterWait(entry: MeterWaitlistEntity)

    // Inventory
    @Query("SELECT * FROM sol_inventory_requests ORDER BY statedOn DESC")
    fun inventoryRequests(): Flow<List<InventoryRequestEntity>>

    @Insert
    suspend fun insertInventoryRequest(entry: InventoryRequestEntity): Long

    @Update
    suspend fun updateInventoryRequest(entry: InventoryRequestEntity)

    @Delete
    suspend fun deleteInventoryRequest(entry: InventoryRequestEntity)

    // Token queue
    @Query("SELECT * FROM sol_token_queue ORDER BY createdAt DESC")
    fun tokenQueue(): Flow<List<TokenQueueEntity>>

    @Insert
    suspend fun insertToken(entry: TokenQueueEntity): Long

    @Update
    suspend fun updateToken(entry: TokenQueueEntity)

    @Delete
    suspend fun deleteToken(entry: TokenQueueEntity)

    // Transformer fault history
    @Query("SELECT * FROM sol_transformer_faults ORDER BY occurredOn DESC")
    fun transformerFaults(): Flow<List<TransformerFaultEntity>>

    @Insert
    suspend fun insertTransformerFault(entry: TransformerFaultEntity): Long

    @Delete
    suspend fun deleteTransformerFault(entry: TransformerFaultEntity)
}
