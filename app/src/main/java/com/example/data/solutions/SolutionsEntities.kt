package com.example.data.solutions

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local records behind the 30 solutions. Everything here lives only on this phone until a
 * DisCo / NERC backend exists; each feature says so on screen.
 */

/** A fault report raised from any solution (tier form, SOS, SMS, photo, voice, tamper...). */
@Entity(tableName = "sol_fault_reports")
data class FaultReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Short reference shown to the user, e.g. BR-4F2A. */
    val reference: String,
    /** TIER, GPS, SOS, SMS, USSD, PHOTO, VOICE, TAMPER, BATSIGNAL, MANUAL */
    val source: String,
    /** 1 = my house, 2 = my street / transformer, 3 = whole area / feeder. */
    val tier: Int,
    val title: String,
    val details: String = "",
    val transformerId: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photoPath: String? = null,
    val voicePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    /** 0 Created, 1 Acknowledged, 2 Crew dispatched, 3 Repair in progress, 4 Restored, 5 Verified closed. */
    val stage: Int = 0,
    val stageUpdatedAt: Long = System.currentTimeMillis(),
    /** null = not judged yet, true = verified real fault, false = marked false alarm. */
    val verified: Boolean? = null,
    val isHazard: Boolean = false
)

/** One tap of "Light ON" or "Light OFF". */
@Entity(tableName = "sol_supply_events")
data class SupplyEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isOn: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    /** APP, NOTIFICATION, NEIGHBOUR_DEMO */
    val source: String = "APP"
)

/** A wasted-time compensation claim filed with a DisCo. */
@Entity(tableName = "sol_refund_claims")
data class RefundClaimEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reference: String,
    val discoCode: String,
    val periodStart: Long,
    val periodEnd: Long,
    val promisedHours: Double,
    val suppliedHours: Double,
    val estimatedOwedNaira: Double,
    /** CALL, WHATSAPP, EMAIL, SMS */
    val channel: String,
    /** FILED, ACKNOWLEDGED, CREDITED, ESCALATED */
    val status: String = "FILED",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "sol_voice_notes")
data class VoiceNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val durationMs: Long,
    val language: String,
    val transcript: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sol_forum_posts")
data class ForumPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transformerId: String,
    val author: String,
    val body: String,
    val isExtortionAlert: Boolean = false,
    val isMine: Boolean = true,
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sol_whistleblow_reports")
data class WhistleblowEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reference: String,
    val technicianId: String,
    val demandType: String,
    val amountNaira: Double?,
    val description: String,
    val location: String,
    val photoPath: String? = null,
    /** SAVED, SENT */
    val status: String = "SAVED",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sol_meter_waitlist")
data class MeterWaitlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val discoCode: String,
    val paidOn: Long,
    val amountNaira: Double?,
    val paymentReference: String,
    val installedOn: Long? = null
)

@Entity(tableName = "sol_inventory_requests")
data class InventoryRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketReference: String,
    val partName: String,
    val statedBy: String,
    val statedOn: Long,
    val promisedBy: Long,
    val deliveredOn: Long? = null
)

/** A token purchase queued while offline, finished later through a bank USSD menu. */
@Entity(tableName = "sol_token_queue")
data class TokenQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val meterNumber: String,
    val amountNaira: Double,
    val bankName: String,
    val createdAt: Long = System.currentTimeMillis(),
    /** 20-digit STS token once received. */
    val token: String? = null
)

/** A fault the user remembers on their transformer (for the recurrent-failure log). */
@Entity(tableName = "sol_transformer_faults")
data class TransformerFaultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transformerId: String,
    val description: String,
    val occurredOn: Long
)
