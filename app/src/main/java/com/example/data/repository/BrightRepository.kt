package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.database.BillingDisputeEntity
import com.example.data.database.ComplaintEntity
import com.example.data.database.UserProfileEntity
import com.example.data.database.VandalismEntity
import com.example.data.database.ApplianceClaimEntity
import com.example.data.database.StreetHazardEntity
import com.example.data.database.OutageGridNodeEntity
import com.example.data.database.MaintenanceAlertEntity
import com.example.data.database.GridTelemetryEntity
import com.example.data.database.OfflineSyncQueueEntity
import com.example.model.ApplianceDamageClaim
import com.example.model.BillingDispute
import com.example.model.Complaint
import com.example.model.ComplaintStatus
import com.example.model.DisCo
import com.example.model.EscalationTier
import com.example.model.FaultType
import com.example.model.GridTelemetry
import com.example.model.MaintenanceAlert
import com.example.model.OutageGridNode
import com.example.model.OutageStatus
import com.example.model.StreetHazardPin
import com.example.model.UserProfile
import com.example.model.VandalismReport
import android.content.Context
import com.example.data.security.PinResetCheck
import com.example.data.security.PinStore
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.example.data.cloud.BrightFirebase
import com.example.data.cloud.ComplaintCloudBackup
import com.example.data.cloud.CommunityFeed
import com.example.data.cloud.NisoGridFeed
import com.example.data.solutions.FaultReportEntity
import com.example.data.solutions.SolutionsDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import com.example.data.outage.CommunityOutageCache
import com.example.data.outage.CommunityOutageSnapshot
import com.example.data.outage.OutageMapBuilder
import com.example.data.outage.OutagePin
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BrightRepository(
    private val database: AppDatabase,
    private val context: Context? = null
) {

    private val complaintDao = database.complaintDao()
    private val profileDao = database.userProfileDao()
    private val vandalismDao = database.vandalismDao()
    private val disputeDao = database.billingDisputeDao()
    private val applianceClaimDao = database.applianceClaimDao()
    private val streetHazardDao = database.streetHazardDao()
    private val outageDao = database.outageGridNodeDao()
    private val maintenanceDao = database.maintenanceAlertDao()
    private val telemetryDao = database.gridTelemetryDao()
    private val offlineQueueDao = database.offlineSyncQueueDao()

    private val prefs: SharedPreferences? = context?.getSharedPreferences("bright_security_prefs", Context.MODE_PRIVATE)

    /** Background work that must outlive a single screen (cloud backup, report mirroring). */
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Firestore backup of complaint history (users/{uid}/complaints). Null-safe when offline. */
    private val complaintBackup: ComplaintCloudBackup? = context?.let { ComplaintCloudBackup(it) }

    // Re-login & Session Lock preferences
    private val _requireLoginOnLeave = MutableStateFlow(
        prefs?.getBoolean(KEY_REQUIRE_LOGIN_ON_LEAVE, true) ?: true
    )
    val requireLoginOnLeave: StateFlow<Boolean> = _requireLoginOnLeave.asStateFlow()

    fun setRequireLoginOnLeave(enabled: Boolean) {
        _requireLoginOnLeave.value = enabled
        prefs?.edit()?.putBoolean(KEY_REQUIRE_LOGIN_ON_LEAVE, enabled)?.apply()
    }

    // Security PIN: stored only as a salted SHA-256 hash (see PinStore). Creating the store also
    // migrates a plain-text PIN left by older versions. There is deliberately no default PIN.
    private val pinStore = PinStore(prefs)
    private val _isPinSet = MutableStateFlow(pinStore.isPinSet)
    val isPinSet: StateFlow<Boolean> = _isPinSet.asStateFlow()

    fun setUserPin(pin: String) {
        pinStore.setPin(pin.trim())
        _isPinSet.value = true
    }

    /** Checks [pin] against the stored hash. Always false when no PIN is set. */
    fun verifyUserPin(pin: String): Boolean = pinStore.verify(pin.trim())

    /** Minutes left in a forgot-PIN break (0 = may try now). */
    fun pinResetLockoutMinutesLeft(): Int = pinStore.resetLockoutMinutesLeft()

    fun recordPinResetFailure(): PinResetCheck = pinStore.recordResetFailure()

    fun clearPinResetFailures() = pinStore.clearResetFailures()

    // Meter activation tracker: the ₦1,000 activation is paid once per meter, for life.
    // This record is deliberately kept when an account is deleted, so re-registering the same
    // meter never charges twice. (No meters are pre-marked as paid.)
    // TODO(payments): source this from the server once Google Play Billing is verified server-side.
    private val _paidMeterNumbers = MutableStateFlow<Set<String>>(
        prefs?.getStringSet(KEY_PAID_METERS, null)?.toSet() ?: emptySet()
    )
    val paidMeterNumbers: StateFlow<Set<String>> = _paidMeterNumbers.asStateFlow()

    fun isMeterPaid(meterNumber: String): Boolean {
        return _paidMeterNumbers.value.contains(meterNumber.trim())
    }

    fun markMeterPaid(meterNumber: String) {
        val updated = _paidMeterNumbers.value + meterNumber.trim()
        _paidMeterNumbers.value = updated
        prefs?.edit()?.putStringSet(KEY_PAID_METERS, updated)?.apply()
    }

    // Live Outage Map nodes - Persisted & cached in Room database
    val outageNodes: Flow<List<OutageGridNode>> = outageDao.getAllNodes().map { list ->
        list.map { it.toDomain() }
    }

    // Maintenance Alerts - Persisted & cached in Room database
    val maintenanceAlerts: Flow<List<MaintenanceAlert>> = maintenanceDao.getAllAlerts().map { list ->
        list.map { it.toDomain() }
    }

    // Outage map: real pins only (the user's own located complaints + recent community reports).
    private val _communityOutages = MutableStateFlow(CommunityOutageCache.load(context))
    val communityOutages: StateFlow<CommunityOutageSnapshot> = _communityOutages.asStateFlow()

    private val solutionReportsFlow: Flow<List<FaultReportEntity>> =
        context?.let { SolutionsDatabase.get(it).dao().reports() } ?: flowOf(emptyList())

    val outagePins: Flow<List<OutagePin>> = combine(
        complaintDao.getAllComplaints(),
        solutionReportsFlow,
        _communityOutages,
        profileDao.getUserProfile()
    ) { complaints, reports, community, profile ->
        OutageMapBuilder.build(complaints, reports, community, profile?.state ?: "")
    }

    /**
     * Fetches the last 48 h of anonymised community reports and caches them on the phone.
     * Returns true on success; false when offline or Firebase isn't configured (the cached
     * reports stay on the map with their fetch time).
     */
    suspend fun refreshCommunityOutages(): Boolean = withContext(Dispatchers.IO) {
        val ctx = context ?: return@withContext false
        val fetched: CommunityOutageSnapshot? = try {
            CommunityFeed.fetchRecentOutages(ctx)
        } catch (e: Exception) {
            null
        }
        val fresh = fetched ?: return@withContext false
        _communityOutages.value = fresh
        CommunityOutageCache.save(ctx, fresh)
        true
    }

    // Offline Sync Outbox Queue - Count of pending offline operations
    val pendingSyncCount: Flow<Int> = offlineQueueDao.getPendingCount()
    val pendingSyncActions: Flow<List<OfflineSyncQueueEntity>> = offlineQueueDao.getPendingActions()

    // National grid: NISO's published daily figures, cached in Room (never a simulated reading).
    private val _gridTelemetry = MutableStateFlow(GridTelemetry())
    val gridTelemetry: StateFlow<GridTelemetry> = _gridTelemetry.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            cleanOldDummyData()
            seedDefaultDataIfEmpty()
            initTelemetryCache()
        }
        mirrorSolutionReportsIntoHistory()
        // Restore history from the cloud on first launch / reinstall (no-op offline).
        ioScope.launch { syncComplaintHistoryWithCloud() }
    }

    // ---------------------------------------------------------------------------------------
    // Complaint history: cloud backup + one history for every kind of report
    // ---------------------------------------------------------------------------------------

    /** Uploads one complaint to the user's cloud backup in the background (no-op offline). */
    private fun backupComplaint(id: String) {
        val backup = complaintBackup ?: return
        ioScope.launch {
            try {
                complaintDao.getComplaintById(id)?.let { complaint ->
                    backup.upload(complaint)
                    // Anonymised summary for community averages (no name / phone / meter).
                    context?.let { ctx ->
                        val profile = profileDao.getUserProfileSync()
                        CommunityFeed.publishReport(
                            ctx, complaint, profile?.state ?: "", recentSupplyHoursPerDay(),
                            approxLocationFor(complaint.id)
                        )
                    }
                }
            } catch (e: Exception) {
                // Best effort: the next full sync uploads anything missed.
            }
        }
    }

    /** GPS fix of a report raised with location (SOL-<reference>), or null. */
    private suspend fun approxLocationFor(complaintId: String): Pair<Double, Double>? {
        val ctx = context ?: return null
        if (!complaintId.startsWith("SOL-")) return null
        val ref = complaintId.removePrefix("SOL-")
        return try {
            val r = SolutionsDatabase.get(ctx).dao().reports().firstOrNull()?.firstOrNull { it.reference == ref }
            val lat = r?.latitude
            val lng = r?.longitude
            if (lat != null && lng != null && !(lat == 0.0 && lng == 0.0)) lat to lng else null
        } catch (e: Exception) {
            null
        }
    }

    /** Average daily supply hours over the last 7 full logged days (Light On/Off log), or null. */
    private suspend fun recentSupplyHoursPerDay(): Double? {
        val ctx = context ?: return null
        return try {
            val events = SolutionsDatabase.get(ctx).dao().supplyEvents().firstOrNull() ?: return null
            val firstLog = events.minOfOrNull { it.timestamp } ?: return null
            val now = System.currentTimeMillis()
            val days = com.example.data.solutions.SupplyMath.hoursByDay(events, 8, now).dropLast(1)
                .filter { it.dayStart >= com.example.data.solutions.SupplyMath.startOfDay(firstLog) }
            if (days.isEmpty()) null else days.sumOf { it.hours } / days.size
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Two-way merge of complaint history with Firestore (users/{uid}/complaints):
     * restores anything missing or newer from the cloud into Room (reinstall / new phone), then
     * uploads local complaints the cloud doesn't have yet. Returns how many were restored.
     * Does nothing when Firebase isn't configured or the phone is offline.
     */
    suspend fun syncComplaintHistoryWithCloud(): Int = withContext(Dispatchers.IO) {
        val backup = complaintBackup ?: return@withContext 0
        try {
            val remote = backup.fetchAll() ?: return@withContext 0
            val remoteById = remote.associateBy { it.id }
            var restored = 0
            for (r in remote) {
                if (r.id.startsWith("SEED-") || r.id.startsWith("CMP-SEED")) continue
                val local = complaintDao.getComplaintById(r.id)
                if (local == null || r.updatedAt > local.updatedAt) {
                    complaintDao.insertComplaint(r)
                    restored++
                }
            }
            val locals = complaintDao.getAllComplaints().firstOrNull() ?: emptyList()
            for (l in locals) {
                val r = remoteById[l.id]
                if (r == null || l.updatedAt > r.updatedAt) backup.upload(l)
            }
            restored
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Reports raised from the 30-solutions tools (SOS, voice, SMS fallback, photo, GPS...) live in
     * the solutions database. Mirror each one into the complaints table so History shows every
     * report the user ever made, and back it up with the rest.
     */
    private fun mirrorSolutionReportsIntoHistory() {
        val ctx = context ?: return
        ioScope.launch {
            try {
                SolutionsDatabase.get(ctx).dao().reports().collect { reports ->
                    reports.forEach { mirrorSolutionReport(it) }
                }
            } catch (e: Exception) {
                // Mirroring is additive; a failure never blocks reporting itself.
            }
        }
    }

    private suspend fun mirrorSolutionReport(r: FaultReportEntity) {
        val id = "SOL-${r.reference}"
        val existing = complaintDao.getComplaintById(id)
        val stageTime = maxOf(r.stageUpdatedAt, r.createdAt)
        if (existing != null && existing.updatedAt >= stageTime) return
        val status = when (r.stage) {
            0 -> ComplaintStatus.LOGGED
            1 -> ComplaintStatus.ASSIGNED
            2 -> ComplaintStatus.DISPATCHED
            3 -> ComplaintStatus.WORK_IN_PROGRESS
            4 -> ComplaintStatus.TESTING
            else -> ComplaintStatus.RESOLVED
        }
        if (existing != null) {
            complaintDao.updateComplaint(
                existing.copy(
                    statusName = status.name,
                    updatedAt = stageTime,
                    resolvedAt = if (status == ComplaintStatus.RESOLVED) (existing.resolvedAt ?: stageTime) else existing.resolvedAt
                )
            )
        } else {
            val profile = profileDao.getUserProfileSync()
            val faultType = when {
                r.isHazard -> FaultType.LIVE_CABLE_EXPOSED
                r.source == "TAMPER" -> FaultType.METER_TAMPER_FAULT
                r.tier >= 3 -> FaultType.FEEDER_TRIPPED
                else -> FaultType.TOTAL_BLACKOUT
            }
            val slaHours = if (r.isHazard) 2 else 4
            val via = when (r.source) {
                "SOS" -> "SOS button"
                "SMS" -> "SMS fallback"
                "USSD" -> "USSD"
                "VOICE" -> "voice report"
                "PHOTO" -> "photo report"
                "GPS" -> "GPS pin"
                "TIER" -> "urgency check"
                "TAMPER" -> "tamper report"
                "BATSIGNAL" -> "neighbour alert"
                else -> "Bright tools"
            }
            complaintDao.insertComplaint(
                ComplaintEntity(
                    id = id,
                    meterNumber = profile?.meterNumber ?: "",
                    title = r.title,
                    description = listOf(r.details, "Sent via $via.").filter { it.isNotBlank() }.joinToString("\n"),
                    faultTypeName = faultType.name,
                    isHazardEmergency = r.isHazard,
                    statusName = status.name,
                    escalationTierLevel = EscalationTier.LEVEL_1.level,
                    discoCode = profile?.discoCode ?: "",
                    transformerId = r.transformerId.ifBlank { profile?.transformerId ?: "" },
                    feederName = profile?.feederName ?: "",
                    reportedAt = r.createdAt,
                    updatedAt = stageTime,
                    escalationDeadline = r.createdAt + slaHours * 60 * 60 * 1000L,
                    upvotesCount = 1,
                    assignedCrewName = null,
                    assignedCrewPhone = null,
                    etaMinutes = null,
                    resolutionNotes = null,
                    userSatisfaction = null,
                    imageUri = r.photoPath,
                    autoClusteredCount = 1,
                    resolvedAt = if (status == ComplaintStatus.RESOLVED) stageTime else null,
                    isVideo = false
                )
            )
        }
        backupComplaint(id)
    }

    private suspend fun initTelemetryCache() {
        try {
            telemetryDao.getTelemetrySync()?.let { cached ->
                if (!_gridTelemetry.value.hasData) _gridTelemetry.value = cached.toDomain(fromCache = true)
            }
        } catch (e: Exception) {
            // Nothing saved yet: the ticker shows "waiting for NISO figures".
        }
    }

    /** Fetches NISO's daily grid figures (falls back to the last saved ones with their date). */
    suspend fun refreshGridTelemetry(): GridTelemetry {
        val ctx = context ?: return _gridTelemetry.value
        val result = NisoGridFeed.refresh(ctx)
        if (result.hasData || !_gridTelemetry.value.hasData) _gridTelemetry.value = result
        return result
    }

    private suspend fun cleanOldDummyData() {
        try {
            complaintDao.deleteSeedComplaints()
            vandalismDao.deleteSeedReports()
            disputeDao.deleteSeedDisputes()
            applianceClaimDao.deleteSeedClaims()
            streetHazardDao.deleteSeedHazards()
            // Older versions seeded made-up outage map nodes (NODE-*) and maintenance notices
            // (MNT-*). Remove them; the map and notices now show real data only.
            outageDao.deleteSeedNodes()
            maintenanceDao.deleteSeedAlerts()
        } catch (e: Exception) {
            // Ignored if tables are clean
        }
    }

    fun getUserProfile(): Flow<UserProfile> {
        return profileDao.getUserProfile().map { entity ->
            entity?.toDomain() ?: UserProfile()
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        // isGatewayPaid is derived from the paid-meter record, never the other way round: only a
        // confirmed activation payment (markMeterPaid) can mark a meter as paid. Previously, editing
        // or switching the meter number on a paid profile marked the new meter as paid for free.
        val paid = isMeterPaid(profile.meterNumber)
        profileDao.setUserProfile(UserProfileEntity.fromDomain(profile.copy(isGatewayPaid = paid)))
    }

    fun getActivePersonalComplaints(meterNumber: String): Flow<List<Complaint>> {
        return complaintDao.getActiveComplaintsForMeter(meterNumber).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getHistoricalComplaints(meterNumber: String): Flow<List<Complaint>> {
        return complaintDao.getHistoricalComplaintsForMeter(meterNumber).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getAllComplaints(): Flow<List<Complaint>> {
        return complaintDao.getAllComplaints().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getVandalismReports(): Flow<List<VandalismReport>> {
        return vandalismDao.getAllVandalismReports().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getBillingDisputes(meterNumber: String): Flow<List<BillingDispute>> {
        return disputeDao.getDisputesForMeter(meterNumber).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun submitFaultComplaint(
        meterNumber: String,
        title: String,
        description: String,
        faultType: FaultType,
        isHazardEmergency: Boolean,
        discoCode: String,
        transformerId: String,
        feederName: String,
        imageUri: String? = null,
        isVideo: Boolean = false
    ): String {
        val now = System.currentTimeMillis()
        val randomNum = (1000..9999).random()
        val ticketId = "BRT-2026-${discoCode.take(2).uppercase()}-$randomNum"
        
        // Emergency hazards have shorter SLA (2 hours), standard is 4 hours before Level 2 escalation
        val slaHours = if (isHazardEmergency) 2 else 4
        val deadline = now + (slaHours * 60 * 60 * 1000L)

        val newComplaint = ComplaintEntity(
            id = ticketId,
            meterNumber = meterNumber,
            title = title,
            description = description,
            faultTypeName = faultType.name,
            isHazardEmergency = isHazardEmergency,
            // Bright has no DisCo dispatch feed, so every ticket starts as "logged". (It used to
            // claim a crew was en route with a made-up name, phone and ETA.)
            statusName = ComplaintStatus.LOGGED.name,
            escalationTierLevel = EscalationTier.LEVEL_1.level,
            discoCode = discoCode,
            transformerId = transformerId,
            feederName = feederName,
            reportedAt = now,
            updatedAt = now,
            escalationDeadline = deadline,
            upvotesCount = 1,
            assignedCrewName = null,
            assignedCrewPhone = null,
            etaMinutes = null,
            resolutionNotes = null,
            userSatisfaction = null,
            imageUri = imageUri,
            autoClusteredCount = 1,
            resolvedAt = null,
            isVideo = isVideo
        )

        complaintDao.insertComplaint(newComplaint)
        backupComplaint(ticketId)
        enqueueOfflineSyncAction("FAULT_REPORT", ticketId, """{"title":"$title","faultType":"${faultType.name}"}""")
        return ticketId
    }

    suspend fun escalateComplaint(complaintId: String) {
        val existing = complaintDao.getComplaintById(complaintId) ?: return
        val currentTier = EscalationTier.entries.find { it.level == existing.escalationTierLevel } ?: EscalationTier.LEVEL_1
        val nextTier = currentTier.nextTier() ?: currentTier
        val now = System.currentTimeMillis()
        val newDeadline = now + (nextTier.maxSlaHours * 60 * 60 * 1000L)

        val updated = existing.copy(
            escalationTierLevel = nextTier.level,
            statusName = if (nextTier == EscalationTier.LEVEL_4) ComplaintStatus.ESCALATED.name else existing.statusName,
            escalationDeadline = newDeadline,
            updatedAt = now,
            assignedCrewName = when (nextTier) {
                EscalationTier.LEVEL_2 -> "Area Technical Engineer & District Manager"
                EscalationTier.LEVEL_3 -> "DisCo Head of Operations & Chief Safety Officer"
                EscalationTier.LEVEL_4 -> "NERC Consumer Protection & Enforcement Directorate"
                else -> existing.assignedCrewName
            }
        )
        complaintDao.updateComplaint(updated)
        backupComplaint(complaintId)
    }

    suspend fun advanceComplaintStatus(complaintId: String, nextStatus: ComplaintStatus) {
        val existing = complaintDao.getComplaintById(complaintId) ?: return
        complaintDao.updateComplaint(
            existing.copy(
                statusName = nextStatus.name,
                updatedAt = System.currentTimeMillis()
            )
        )
        backupComplaint(complaintId)
        enqueueOfflineSyncAction("STATUS_UPDATE", complaintId, """{"nextStatus":"${nextStatus.name}"}""")
    }

    suspend fun resolveComplaint(complaintId: String, rating: Int, notes: String) {
        val now = System.currentTimeMillis()
        complaintDao.resolveComplaint(
            id = complaintId,
            status = ComplaintStatus.RESOLVED.name,
            rating = rating,
            notes = notes,
            updatedAt = now,
            resolvedAt = now
        )
        backupComplaint(complaintId)
        enqueueOfflineSyncAction("RESOLUTION_CONFIRMED", complaintId, """{"rating":$rating,"notes":"$notes"}""")
    }

    suspend fun upvoteComplaint(complaintId: String) {
        complaintDao.upvoteComplaint(complaintId)
        backupComplaint(complaintId)
        enqueueOfflineSyncAction("UPVOTE", complaintId, """{}""")
    }

    suspend fun reportVandalism(
        incidentType: String,
        location: String,
        landmark: String,
        discoCode: String,
        isAnonymous: Boolean,
        description: String,
        suspectDetails: String?
    ): String {
        val id = "VAN-NG-${(1000..9999).random()}"
        val report = VandalismEntity(
            id = id,
            incidentType = incidentType,
            location = location,
            landmark = landmark,
            discoCode = discoCode,
            reportedAt = System.currentTimeMillis(),
            isAnonymous = isAnonymous,
            description = description,
            status = "SECURITY_DISPATCHED",
            suspectDetails = suspectDetails
        )
        vandalismDao.insertReport(report)
        enqueueOfflineSyncAction("VANDALISM_REPORT", id, """{"type":"$incidentType","location":"$location"}""")
        return id
    }

    suspend fun submitBillingDispute(
        meterNumber: String,
        disputeType: String,
        disputedAmountNgn: Double,
        billingMonth: String,
        discoCode: String,
        description: String
    ): String {
        val id = "DSP-NG-${(1000..9999).random()}"
        val dispute = BillingDisputeEntity(
            id = id,
            meterNumber = meterNumber,
            disputeType = disputeType,
            disputedAmountNgn = disputedAmountNgn,
            billingMonth = billingMonth,
            discoCode = discoCode,
            description = description,
            status = "Under NERC Capping Audit",
            createdAt = System.currentTimeMillis()
        )
        disputeDao.insertDispute(dispute)
        enqueueOfflineSyncAction("BILLING_DISPUTE", id, """{"type":"$disputeType","amount":$disputedAmountNgn}""")
        return id
    }

    fun getApplianceClaims(meterNumber: String): Flow<List<ApplianceDamageClaim>> {
        return applianceClaimDao.getClaimsForMeter(meterNumber).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun submitApplianceClaim(
        meterNumber: String,
        applianceName: String,
        applianceBrandModel: String,
        estimatedLossNgn: Double,
        surgeTimestampText: String,
        surgeDescription: String,
        statutoryNoticeText: String,
        discoCode: String
    ): String {
        val id = "CLM-SRG-${(1000..9999).random()}"
        val claim = ApplianceClaimEntity(
            id = id,
            meterNumber = meterNumber,
            applianceName = applianceName,
            applianceBrandModel = applianceBrandModel,
            estimatedLossNgn = estimatedLossNgn,
            surgeTimestampText = surgeTimestampText,
            surgeDescription = surgeDescription,
            statutoryNoticeText = statutoryNoticeText,
            discoCode = discoCode,
            status = "SUBMITTED_TO_DISCO_LEGAL",
            createdAt = System.currentTimeMillis()
        )
        applianceClaimDao.insertClaim(claim)
        enqueueOfflineSyncAction("APPLIANCE_CLAIM", id, """{"appliance":"$applianceName","loss":$estimatedLossNgn}""")
        return id
    }

    fun getAllStreetHazards(): Flow<List<StreetHazardPin>> {
        return streetHazardDao.getAllStreetHazards().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun pinStreetHazard(
        title: String,
        hazardType: String,
        urgency: String,
        location: String,
        landmark: String,
        discoCode: String,
        reportedBy: String,
        xPosRatio: Float,
        yPosRatio: Float
    ): String {
        val id = "HZD-PIN-${(1000..9999).random()}"
        val hazard = StreetHazardEntity(
            id = id,
            title = title,
            hazardType = hazardType,
            urgency = urgency,
            location = location,
            landmark = landmark,
            discoCode = discoCode,
            reportedBy = reportedBy,
            verifiedCount = 1,
            isDispatched = true,
            xPosRatio = xPosRatio,
            yPosRatio = yPosRatio,
            reportedAt = System.currentTimeMillis()
        )
        streetHazardDao.insertHazard(hazard)
        enqueueOfflineSyncAction("STREET_HAZARD", id, """{"title":"$title","hazardType":"$hazardType"}""")
        return id
    }

    suspend fun upvoteStreetHazard(id: String) {
        streetHazardDao.upvoteHazard(id)
    }

    suspend fun markHazardDispatched(id: String) {
        streetHazardDao.markDispatched(id)
    }


    private suspend fun seedDefaultDataIfEmpty() {
        // Deliberately no default profile: a fresh install (or a deleted account) must go through
        // sign-up, which creates the PIN and takes the activation payment. The old seed created an
        // already-onboarded, already-paid demo profile, which skipped both.
        // Clean slate: 0 fake stranger reports, 0 fake vandalism, 0 fake claims.
    }

    /**
     * Delete account: permanently removes everything Bright keeps about the user on this phone:
     * profile, PIN, complaints and history, reports, claims, the offline outbox, cached grid data
     * and cached files (evidence photos, generated dossiers).
     *
     * Also deletes the cloud backup of the complaint history (users/{uid}/complaints) and signs
     * out of Firebase. Plain "Log out" never calls this, so history survives sign-out.
     *
     * Kept on purpose: the paid-meter record, so re-registering the same meter isn't charged again.
     * Nothing is re-seeded: the outage map and maintenance notices only ever show real reports.
     */
    suspend fun deleteAllLocalUserData() {
        withContext(Dispatchers.IO) {
            // Cloud copy first, while we still hold the Firebase session that owns it.
            try {
                complaintBackup?.deleteAll()
                context?.let {
                    CommunityFeed.deleteMine(it)
                    BrightFirebase.signOut(it)
                }
            } catch (e: Exception) {
                // Best effort; local data is still wiped below.
            }
            // PIN and preferences first, so the UI never shows a "signed in" state without data.
            // Wipe everything except the paid-meter record.
            val paidMeters = _paidMeterNumbers.value
            prefs?.edit()
                ?.clear()
                ?.putStringSet(KEY_PAID_METERS, paidMeters)
                ?.commit()
            pinStore.clear()
            _isPinSet.value = false
            _requireLoginOnLeave.value = true

            complaintDao.clearAll()
            vandalismDao.clearAll()
            disputeDao.clearAll()
            applianceClaimDao.clearAll()
            streetHazardDao.clearAll()
            offlineQueueDao.clearAll()
            telemetryDao.clearAll()
            database.communityStatsDao().clearAll()
            database.crewSightingDao().clearAll()
            outageDao.clearAll()
            maintenanceDao.clearAll()
            profileDao.clearProfile()

            // The 30-solutions data (supply log, claims, voice notes, evidence photos, reports).
            context?.let { ctx ->
                com.example.data.solutions.SolutionsDatabase.wipe(ctx)
                com.example.data.meter.ManualMeterReadings.clear(ctx)
                com.example.data.meter.TokenHistory.clear(ctx)
                com.example.data.meter.CompensationTokens.clear(ctx)
                com.example.data.meter.MeterIdentity.clear(ctx)
                com.example.data.meter.HesRegistry.clearAll(ctx)
                com.example.ui.solutions.common.SolutionsNotifier.hideQuickLog(ctx)
                com.example.ui.solutions.common.SolutionsNotifier.cancelSurgeWarning(ctx)
            }

            // Cached files (evidence photos, PDF dossiers).
            try {
                context?.cacheDir?.listFiles()?.forEach { it.deleteRecursively() }
            } catch (e: Exception) {
                // Best effort: anything left is cleaned up by the system later.
            }

            _communityOutages.value = CommunityOutageSnapshot()
            CommunityOutageCache.clear(context)
        }
    }

    suspend fun enqueueOfflineSyncAction(actionType: String, referenceId: String, payloadJson: String = "") {
        offlineQueueDao.enqueueAction(
            OfflineSyncQueueEntity(
                id = "SYNC-${UUID.randomUUID()}",
                actionType = actionType,
                referenceId = referenceId,
                payloadJson = payloadJson,
                timestamp = System.currentTimeMillis(),
                isSynced = false
            )
        )
    }

    suspend fun flushOfflineSyncQueue(): Int {
        val pending = offlineQueueDao.getPendingActions().firstOrNull() ?: emptyList()
        val count = pending.size
        for (item in pending) {
            offlineQueueDao.markSynced(item.id)
        }
        offlineQueueDao.clearCompleted()
        return count
    }

    private companion object {
        const val KEY_PAID_METERS = "paid_meters"
        const val KEY_REQUIRE_LOGIN_ON_LEAVE = "require_login_on_leave"
    }
}
