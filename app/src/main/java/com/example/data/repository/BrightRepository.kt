package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.database.BillingDisputeEntity
import com.example.data.database.ComplaintEntity
import com.example.data.database.UserProfileEntity
import com.example.data.database.VandalismEntity
import com.example.model.BillingDispute
import com.example.model.Complaint
import com.example.model.ComplaintStatus
import com.example.model.DisCo
import com.example.model.EscalationTier
import com.example.model.FaultType
import com.example.model.FeederBand
import com.example.model.GridTelemetry
import com.example.model.MaintenanceAlert
import com.example.model.OutageGridNode
import com.example.model.OutageStatus
import com.example.model.UserProfile
import com.example.model.VandalismReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BrightRepository(private val database: AppDatabase) {

    private val complaintDao = database.complaintDao()
    private val profileDao = database.userProfileDao()
    private val vandalismDao = database.vandalismDao()
    private val disputeDao = database.billingDisputeDao()

    // Real-time Telemetry state
    private val _gridTelemetry = MutableStateFlow(GridTelemetry())
    val gridTelemetry: StateFlow<GridTelemetry> = _gridTelemetry.asStateFlow()

    // Live Outage Map nodes
    private val _outageNodes = MutableStateFlow<List<OutageGridNode>>(emptyList())
    val outageNodes: StateFlow<List<OutageGridNode>> = _outageNodes.asStateFlow()

    // Maintenance Alerts
    private val _maintenanceAlerts = MutableStateFlow<List<MaintenanceAlert>>(emptyList())
    val maintenanceAlerts: StateFlow<List<MaintenanceAlert>> = _maintenanceAlerts.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDefaultDataIfEmpty()
            seedLiveOutageData()
            seedMaintenanceAlerts()
        }
    }

    fun getUserProfile(): Flow<UserProfile> {
        return profileDao.getUserProfile().map { entity ->
            entity?.toDomain() ?: UserProfile()
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        profileDao.setUserProfile(UserProfileEntity.fromDomain(profile))
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
            statusName = if (isHazardEmergency) ComplaintStatus.DISPATCHED.name else ComplaintStatus.LOGGED.name,
            escalationTierLevel = EscalationTier.LEVEL_1.level,
            discoCode = discoCode,
            transformerId = transformerId,
            feederName = feederName,
            reportedAt = now,
            updatedAt = now,
            escalationDeadline = deadline,
            upvotesCount = 1,
            assignedCrewName = if (isHazardEmergency) "Rapid Safety Response Team Alpha" else "Engr. Babatunde Lawal (Field Tech)",
            assignedCrewPhone = "0802-991-3820",
            etaMinutes = if (isHazardEmergency) 25 else 75,
            resolutionNotes = null,
            userSatisfaction = null,
            imageUri = imageUri,
            autoClusteredCount = if (faultType == FaultType.BURNT_TRANSFORMER || faultType == FaultType.FEEDER_TRIPPED) 38 else 1,
            resolvedAt = null,
            isVideo = isVideo
        )

        complaintDao.insertComplaint(newComplaint)
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
    }

    suspend fun advanceComplaintStatus(complaintId: String, nextStatus: ComplaintStatus) {
        val existing = complaintDao.getComplaintById(complaintId) ?: return
        complaintDao.updateComplaint(
            existing.copy(
                statusName = nextStatus.name,
                updatedAt = System.currentTimeMillis()
            )
        )
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
    }

    suspend fun upvoteComplaint(complaintId: String) {
        complaintDao.upvoteComplaint(complaintId)
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
        return id
    }

    fun updateGridTelemetry(frequency: Double, generationMw: Int) {
        _gridTelemetry.value = _gridTelemetry.value.copy(
            systemFrequencyHz = frequency,
            nationalGenerationMw = generationMw,
            systemStatus = if (frequency in 49.8..50.2) "Grid Stable" else "Frequency Disturbance Detected"
        )
    }

    private suspend fun seedDefaultDataIfEmpty() {
        val defaultProfile = UserProfile(
            meterNumber = "01429583192",
            customerName = "Chuka Obunma",
            phoneNumber = "+234 803 892 4110",
            streetAddress = "14 Adeola Odeku Street, Victoria Island",
            lga = "Eti-Osa",
            state = "Lagos State",
            discoCode = "EKEDC",
            feederName = "Victoria Island 33kV Injection Feeder 4",
            feederBand = FeederBand.BAND_A,
            transformerId = "TR-VI-ADEOLA-04B",
            isPrepaid = true,
            connectedHouseholdsCount = 184
        )
        profileDao.setUserProfile(UserProfileEntity.fromDomain(defaultProfile))

        val now = System.currentTimeMillis()

        // Seed 1 active personal complaint and 2 historical complaints so user immediately experiences all features
        val seedComplaints = listOf(
            ComplaintEntity(
                id = "BRT-2026-EK-4821",
                meterNumber = "01429583192",
                title = "Blown 500kVA Distribution Transformer",
                description = "Transformer sparked heavily around 3:15 PM with loud explosion. Oil leaking from base. Entire street in darkness.",
                faultTypeName = FaultType.BURNT_TRANSFORMER.name,
                isHazardEmergency = false,
                statusName = ComplaintStatus.WORK_IN_PROGRESS.name,
                escalationTierLevel = EscalationTier.LEVEL_2.level, // Auto-escalated to District Office
                discoCode = "EKEDC",
                transformerId = "TR-VI-ADEOLA-04B",
                feederName = "Victoria Island 33kV Injection Feeder 4",
                reportedAt = now - (6 * 60 * 60 * 1000L), // 6 hours ago
                updatedAt = now - (45 * 60 * 1000L),
                escalationDeadline = now + (6 * 60 * 60 * 1000L),
                upvotesCount = 42, // Community neighbors upvoted
                assignedCrewName = "Engr. Kelechi Okafor (Area District Lead)",
                assignedCrewPhone = "0803-455-8910",
                etaMinutes = 45,
                resolutionNotes = "Replacement 500kVA transformer core dispatched from Ijora Central Workshop. Crew rigging crane for installation.",
                userSatisfaction = null,
                imageUri = null,
                autoClusteredCount = 184,
                resolvedAt = null,
                isVideo = false
            ),
            ComplaintEntity(
                id = "BRT-2026-EK-1109",
                meterNumber = "01429583192",
                title = "Low Phase Power & Brownout",
                description = "Refrigerators and pumps cannot start due to half-current (140V measured on multimeter).",
                faultTypeName = FaultType.PHASE_FAILURE.name,
                isHazardEmergency = false,
                statusName = ComplaintStatus.RESOLVED.name,
                escalationTierLevel = EscalationTier.LEVEL_1.level,
                discoCode = "EKEDC",
                transformerId = "TR-VI-ADEOLA-04B",
                feederName = "Victoria Island 33kV Injection Feeder 4",
                reportedAt = now - (5 * 24 * 60 * 60 * 1000L),
                updatedAt = now - (5 * 24 * 60 * 60 * 1000L) + (4 * 60 * 60 * 1000L),
                escalationDeadline = now - (5 * 24 * 60 * 60 * 1000L) + (8 * 60 * 60 * 1000L),
                upvotesCount = 15,
                assignedCrewName = "Technician Fatai Sanusi",
                assignedCrewPhone = "0812-700-1122",
                etaMinutes = 0,
                resolutionNotes = "Loose jumper replaced at pole 12 crossarm. Voltage restored to 232V balanced.",
                userSatisfaction = 5,
                imageUri = "https://images.unsplash.com/photo-1513828583688-c52646db42da?w=600&q=80",
                autoClusteredCount = 38,
                resolvedAt = now - (5 * 24 * 60 * 60 * 1000L) + (4 * 60 * 60 * 1000L),
                isVideo = false
            ),
            ComplaintEntity(
                id = "BRT-2026-EK-0943",
                meterNumber = "01429583192",
                title = "Sparking Underground Feeder Cable",
                description = "Smoking underground armored cable joint near junction box with visible arcing.",
                faultTypeName = FaultType.LIVE_CABLE_EXPOSED.name,
                isHazardEmergency = true,
                statusName = ComplaintStatus.RESOLVED.name,
                escalationTierLevel = EscalationTier.LEVEL_1.level,
                discoCode = "EKEDC",
                transformerId = "TR-VI-ADEOLA-04B",
                feederName = "Victoria Island 33kV Injection Feeder 4",
                reportedAt = now - (14 * 24 * 60 * 60 * 1000L),
                updatedAt = now - (14 * 24 * 60 * 60 * 1000L) + (90 * 60 * 1000L),
                escalationDeadline = now - (14 * 24 * 60 * 60 * 1000L) + (120 * 60 * 1000L),
                upvotesCount = 89,
                assignedCrewName = "Emergency Rapid Intervention Squad",
                assignedCrewPhone = "0708-065-5555",
                etaMinutes = 0,
                resolutionNotes = "Cable isolated within 18 minutes. Joint re-sleeved with resin splice kit and buried according to NERC safety standard.",
                userSatisfaction = 4,
                imageUri = "https://images.unsplash.com/photo-1544724569-5f546fd6f2b5?w=600&q=80",
                autoClusteredCount = 184,
                resolvedAt = now - (14 * 24 * 60 * 60 * 1000L) + (90 * 60 * 1000L),
                isVideo = true
            ),
            ComplaintEntity(
                id = "BRT-2026-EK-0641",
                meterNumber = "01429583192",
                title = "Blown 300A Feeder Pillar Fuse",
                description = "Feeder pillar fuse blown after heavy rainfall storm surge.",
                faultTypeName = FaultType.FEEDER_TRIPPED.name,
                isHazardEmergency = false,
                statusName = ComplaintStatus.RESOLVED.name,
                escalationTierLevel = EscalationTier.LEVEL_1.level,
                discoCode = "EKEDC",
                transformerId = "TR-VI-ADEOLA-04B",
                feederName = "Victoria Island 33kV Injection Feeder 4",
                reportedAt = now - (28 * 24 * 60 * 60 * 1000L),
                updatedAt = now - (28 * 24 * 60 * 60 * 1000L) + (135 * 60 * 1000L),
                escalationDeadline = now - (28 * 24 * 60 * 60 * 1000L) + (240 * 60 * 1000L),
                upvotesCount = 22,
                assignedCrewName = "Engr. Segun Adeyemi",
                assignedCrewPhone = "0802-334-1189",
                etaMinutes = 0,
                resolutionNotes = "Replaced blown 300A HRC fuse link and cleaned carbon traces on busbar.",
                userSatisfaction = 5,
                imageUri = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=600&q=80",
                autoClusteredCount = 64,
                resolvedAt = now - (28 * 24 * 60 * 60 * 1000L) + (135 * 60 * 1000L),
                isVideo = false
            )
        )
        complaintDao.insertAll(seedComplaints)

        // Seed initial vandalism report
        val seedVandalism = VandalismEntity(
            id = "VAN-NG-8831",
            incidentType = "Substation Armored Copper Cable Theft",
            location = "Bishop Oluwole Substation",
            landmark = "Behind Silverbird Galleria",
            discoCode = "EKEDC",
            reportedAt = now - (2 * 24 * 60 * 60 * 1000L),
            isAnonymous = false,
            description = "Three men in unauthorized jumpsuits cut copper earthing wires around 2:00 AM.",
            status = "Security Dispatched & Police Report Filed",
            suspectDetails = "White unmarked Hiace bus with missing rear bumper"
        )
        vandalismDao.insertReport(seedVandalism)

        // Seed initial billing dispute
        val seedDispute = BillingDisputeEntity(
            id = "DSP-NG-3042",
            meterNumber = "01429583192",
            disputeType = "Capped Tariff Overbilling (Band A supply shortfall)",
            disputedAmountNgn = 28450.0,
            billingMonth = "August 2026",
            discoCode = "EKEDC",
            description = "Supply logged for August was 14.2 hours daily average instead of minimum 20 hours required for Band A rate.",
            status = "NERC Audit Approved - NGN 18,200 Token Credit Queued",
            createdAt = now - (8 * 24 * 60 * 60 * 1000L)
        )
        disputeDao.insertDispute(seedDispute)
    }

    private fun seedLiveOutageData() {
        val nodes = listOf(
            OutageGridNode(
                id = "NODE-LAG-01",
                name = "Victoria Island & Ikoyi Feeder Ring",
                city = "Lagos",
                state = "Lagos State",
                discoCode = "EKEDC",
                status = OutageStatus.FAULT_DOWN,
                affectedConsumers = 14200,
                xPosRatio = 0.28f,
                yPosRatio = 0.65f,
                lastUpdate = "12 mins ago",
                reportedFaults = 48,
                estimatedRestoration = "2 hrs 15 mins"
            ),
            OutageGridNode(
                id = "NODE-LAG-02",
                name = "Ikeja Industrial & Alausa Substation",
                city = "Ikeja",
                state = "Lagos State",
                discoCode = "IE",
                status = OutageStatus.ACTIVE,
                affectedConsumers = 0,
                xPosRatio = 0.26f,
                yPosRatio = 0.61f,
                lastUpdate = "Live",
                reportedFaults = 3,
                estimatedRestoration = "Operational"
            ),
            OutageGridNode(
                id = "NODE-ABJ-01",
                name = "Central Area & Maitama Injection Station",
                city = "Abuja",
                state = "FCT",
                discoCode = "AEDC",
                status = OutageStatus.ACTIVE,
                affectedConsumers = 0,
                xPosRatio = 0.48f,
                yPosRatio = 0.42f,
                lastUpdate = "Live",
                reportedFaults = 1,
                estimatedRestoration = "Operational"
            ),
            OutageGridNode(
                id = "NODE-ABJ-02",
                name = "Gwarinpa & Kubwa 33kV Line",
                city = "Gwarinpa",
                state = "FCT",
                discoCode = "AEDC",
                status = OutageStatus.MAINTENANCE,
                affectedConsumers = 8900,
                xPosRatio = 0.46f,
                yPosRatio = 0.39f,
                lastUpdate = "34 mins ago",
                reportedFaults = 12,
                estimatedRestoration = "4 hrs 00 mins"
            ),
            OutageGridNode(
                id = "NODE-IBD-01",
                name = "Bodija & University Feeder Corridor",
                city = "Ibadan",
                state = "Oyo State",
                discoCode = "IBEDC",
                status = OutageStatus.LOAD_SHEDDING,
                affectedConsumers = 11300,
                xPosRatio = 0.30f,
                yPosRatio = 0.58f,
                lastUpdate = "18 mins ago",
                reportedFaults = 22,
                estimatedRestoration = "Load-shed rotation (ends 6 PM)"
            ),
            OutageGridNode(
                id = "NODE-ENU-01",
                name = "Independence Layout 11kV Feeder",
                city = "Enugu",
                state = "Enugu State",
                discoCode = "EEDC",
                status = OutageStatus.FAULT_DOWN,
                affectedConsumers = 7400,
                xPosRatio = 0.62f,
                yPosRatio = 0.62f,
                lastUpdate = "5 mins ago",
                reportedFaults = 31,
                estimatedRestoration = "6 hrs 30 mins"
            ),
            OutageGridNode(
                id = "NODE-PH-01",
                name = "Trans-Amadi Industrial & GRA Phase 2",
                city = "Port Harcourt",
                state = "Rivers State",
                discoCode = "PHED",
                status = OutageStatus.ACTIVE,
                affectedConsumers = 0,
                xPosRatio = 0.54f,
                yPosRatio = 0.74f,
                lastUpdate = "Live",
                reportedFaults = 4,
                estimatedRestoration = "Operational"
            ),
            OutageGridNode(
                id = "NODE-KAN-01",
                name = "Bompai & Nassarawa Injection Station",
                city = "Kano",
                state = "Kano State",
                discoCode = "KEDCO",
                status = OutageStatus.LOAD_SHEDDING,
                affectedConsumers = 16500,
                xPosRatio = 0.65f,
                yPosRatio = 0.18f,
                lastUpdate = "22 mins ago",
                reportedFaults = 19,
                estimatedRestoration = "Rotation till 8 PM"
            ),
            OutageGridNode(
                id = "NODE-KAD-01",
                name = "Barnawa & Kakuri Feeder Network",
                city = "Kaduna",
                state = "Kaduna State",
                discoCode = "KAEDC",
                status = OutageStatus.MAINTENANCE,
                affectedConsumers = 9200,
                xPosRatio = 0.52f,
                yPosRatio = 0.28f,
                lastUpdate = "40 mins ago",
                reportedFaults = 14,
                estimatedRestoration = "3 hrs"
            ),
            OutageGridNode(
                id = "NODE-BEN-01",
                name = "Uselu & Ring Road Feeder",
                city = "Benin City",
                state = "Edo State",
                discoCode = "BEDC",
                status = OutageStatus.FAULT_DOWN,
                affectedConsumers = 6800,
                xPosRatio = 0.42f,
                yPosRatio = 0.66f,
                lastUpdate = "1 hour ago",
                reportedFaults = 27,
                estimatedRestoration = "5 hrs"
            ),
            OutageGridNode(
                id = "NODE-JOS-01",
                name = "Bukuru & Rayfield Feeder",
                city = "Jos",
                state = "Plateau State",
                discoCode = "JED",
                status = OutageStatus.ACTIVE,
                affectedConsumers = 0,
                xPosRatio = 0.68f,
                yPosRatio = 0.38f,
                lastUpdate = "Live",
                reportedFaults = 2,
                estimatedRestoration = "Operational"
            )
        )
        _outageNodes.value = nodes
    }

    private fun seedMaintenanceAlerts() {
        val alerts = listOf(
            MaintenanceAlert(
                id = "MNT-01",
                title = "TCN 330kV Egbin-Ikeja West Line Overhaul",
                discoCode = "EKEDC / IE",
                affectedFeeders = "Victoria Island, Ikoyi, Marina, Surulere 33kV feeders",
                startDate = "Tomorrow, 08:00 AM - 02:00 PM",
                durationHours = 6,
                reason = "Annual preventive substation maintenance and replacement of cracked disc insulators",
                alternativeSupplyAvailable = true
            ),
            MaintenanceAlert(
                id = "MNT-02",
                title = "AEDC Injection Substation Transformer Service",
                discoCode = "AEDC",
                affectedFeeders = "Gwarinpa Estate 11kV lines 1, 2, 3",
                startDate = "Saturday, 10:00 AM - 04:00 PM",
                durationHours = 6,
                reason = "Routine silica gel replacement and oil filtration on 15MVA power transformer",
                alternativeSupplyAvailable = false
            ),
            MaintenanceAlert(
                id = "MNT-03",
                title = "Right of Way Vegetation & Tree Trimming",
                discoCode = "IBEDC",
                affectedFeeders = "Ibadan North-East 33kV transmission corridor",
                startDate = "Sunday, 07:00 AM - 01:00 PM",
                durationHours = 6,
                reason = "Clearing heavy canopy branches touching high-voltage conductors to prevent stormy arc-trips",
                alternativeSupplyAvailable = true
            )
        )
        _maintenanceAlerts.value = alerts
    }
}
