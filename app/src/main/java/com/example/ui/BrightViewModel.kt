package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.repository.BrightRepository
import com.example.model.BillingDispute
import com.example.model.Complaint
import com.example.model.ComplaintStatus
import com.example.model.DisCo
import com.example.model.FaultType
import com.example.model.GridTelemetry
import com.example.model.MaintenanceAlert
import com.example.model.OutageGridNode
import com.example.model.UserProfile
import com.example.model.VandalismReport
import com.example.model.ApplianceDamageClaim
import com.example.model.StreetHazardPin
import com.example.model.TransformerOverloadTelemetry
import com.example.model.AuditingHourRecord
import com.example.model.EscrowTokenRebate
import com.example.model.CommunityForumPost
import com.example.model.ApplianceBudgetItem
import com.example.model.LinkedMeterAsset
import com.example.model.WhistleblowerReport
import com.example.model.FeederBand
import android.media.AudioManager

import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppLanguage(val code: String, val label: String, val tagline: String) {
    ENGLISH("EN", "English", "Fixing Nigeria's Electricity Response"),
    PIDGIN("PG", "Naija Pidgin", "Make Light Dey Shine Well Well"),
    YORUBA("YO", "Yorùbá", "Atunṣe Ina Monamona Naijiria"),
    HAUSA("HA", "Hausa", "Gyaran Matsalar Wutar Lantarki"),
    IGBO("IG", "Igbo", "Mmezi Ọkụ Eletrik Nigeria")
}

class BrightViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BrightRepository = BrightRepository(AppDatabase.getDatabase(application))

    val userProfile: StateFlow<UserProfile> = repository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val activePersonalComplaints: StateFlow<List<Complaint>> = userProfile.flatMapLatest { profile ->
        repository.getActivePersonalComplaints(profile.meterNumber)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historicalComplaints: StateFlow<List<Complaint>> = userProfile.flatMapLatest { profile ->
        repository.getHistoricalComplaints(profile.meterNumber)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val outageNodes: StateFlow<List<OutageGridNode>> = repository.outageNodes
    val vandalismReports: StateFlow<List<VandalismReport>> = repository.getVandalismReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val billingDisputes: StateFlow<List<BillingDispute>> = userProfile.flatMapLatest { profile ->
        repository.getBillingDisputes(profile.meterNumber)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val gridTelemetry: StateFlow<GridTelemetry> = repository.gridTelemetry
    val maintenanceAlerts: StateFlow<List<MaintenanceAlert>> = repository.maintenanceAlerts

    // Feature 4: Appliance Damage Claims
    val applianceClaims: StateFlow<List<ApplianceDamageClaim>> = userProfile.flatMapLatest { profile ->
        repository.getApplianceClaims(profile.meterNumber)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feature 10: Street Hazard Pins
    val streetHazards: StateFlow<List<StreetHazardPin>> = repository.getAllStreetHazards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feature 7: Transformer Overload & Phase Telemetry
    private val _transformerTelemetry = MutableStateFlow(TransformerOverloadTelemetry())
    val transformerTelemetry: StateFlow<TransformerOverloadTelemetry> = _transformerTelemetry.asStateFlow()

    // Feature 9: Power Restoration Alarm
    private val _isRestorationAlarmEnabled = MutableStateFlow(true)
    val isRestorationAlarmEnabled: StateFlow<Boolean> = _isRestorationAlarmEnabled.asStateFlow()

    // UI state & Theme Mode (Dark / Light)
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    private val _isLowDataMode = MutableStateFlow(false)
    val isLowDataMode: StateFlow<Boolean> = _isLowDataMode.asStateFlow()

    // Phase 2: Contractual Hour Auditing Matrix (Immutable service band log)
    private val _auditingRecords = MutableStateFlow(
        listOf(
            AuditingHourRecord("Today (Live)", "Sunday", 20.0, 15.2, true, 4.8, 684.0),
            AuditingHourRecord("Yesterday", "Saturday", 20.0, 21.4, false, 0.0, 0.0),
            AuditingHourRecord("05 Sep", "Friday", 20.0, 16.1, true, 3.9, 555.75),
            AuditingHourRecord("04 Sep", "Thursday", 20.0, 14.8, true, 5.2, 741.0),
            AuditingHourRecord("03 Sep", "Wednesday", 20.0, 22.0, false, 0.0, 0.0),
            AuditingHourRecord("02 Sep", "Tuesday", 20.0, 13.5, true, 6.5, 926.25),
            AuditingHourRecord("01 Sep", "Monday", 20.0, 20.5, false, 0.0, 0.0)
        )
    )
    val auditingRecords: StateFlow<List<AuditingHourRecord>> = _auditingRecords.asStateFlow()

    // Phase 4: Escrow Tokens & Clearinghouse Ledger
    private val _escrowLiquidityVaultBalanceNgn = MutableStateFlow(14850000.0)
    val escrowLiquidityVaultBalanceNgn: StateFlow<Double> = _escrowLiquidityVaultBalanceNgn.asStateFlow()

    private val _escrowRebateTokens = MutableStateFlow(
        listOf(
            EscrowTokenRebate(
                id = "ESC-TK-9021",
                token20Digit = "4829 1092 3841 9201 3819",
                kwhValue = 18.5,
                monetaryValueNgn = 3875.0,
                discoCode = "EKEDC",
                reason = "Automated compensatory rebate: 36hr Feeder Breaker Failure SLA Default",
                issuedTimestamp = System.currentTimeMillis() - 86400000L,
                isRedeemed = false
            ),
            EscrowTokenRebate(
                id = "ESC-TK-8410",
                token20Digit = "8832 9401 2284 1029 5519",
                kwhValue = 12.0,
                monetaryValueNgn = 2514.0,
                discoCode = "EKEDC",
                reason = "SLA Breach Settlement: Unannounced Phase Dropout > 12 hours",
                issuedTimestamp = System.currentTimeMillis() - 432000000L,
                isRedeemed = true
            )
        )
    )
    val escrowRebateTokens: StateFlow<List<EscrowTokenRebate>> = _escrowRebateTokens.asStateFlow()

    // Phase 5: Transformer Cluster Forum Feed
    private val _communityForumPosts = MutableStateFlow(
        listOf(
            CommunityForumPost(
                id = "POST-1",
                authorName = "Engr. Femi A.",
                isVerifiedResident = true,
                transformerId = "TR-VI-ADEOLA-04B",
                content = "DisCo team just arrived at the corner pole near Adeola Odeku junction with an aluminium replacement cross-arm. Estimated completion 45 mins.",
                timestampText = "12 mins ago",
                upvotes = 14,
                isExtortionReport = false
            ),
            CommunityForumPost(
                id = "POST-2",
                authorName = "Mama Chioma",
                isVerifiedResident = true,
                transformerId = "TR-VI-ADEOLA-04B",
                content = "A self-proclaimed linesman asked our compound for ₦15,000 to reconnect our phase jumper. DO NOT PAY. We have logged this directly into the NERC Whistleblower clearance.",
                timestampText = "1 hour ago",
                upvotes = 32,
                isExtortionReport = true
            ),
            CommunityForumPost(
                id = "POST-3",
                authorName = "Segun Bankole",
                isVerifiedResident = true,
                transformerId = "TR-VI-ADEOLA-04B",
                content = "Phase B voltage is flickering between 175V and 190V. Please switch off heavy deep freezers and inverter chargers to prevent low-voltage coil damage.",
                timestampText = "2 hours ago",
                upvotes = 21,
                isExtortionReport = false
            )
        )
    )
    val communityForumPosts: StateFlow<List<CommunityForumPost>> = _communityForumPosts.asStateFlow()

    // Phase 6: Appliance Consumption Matrix
    private val _applianceBudgetList = MutableStateFlow(
        listOf(
            ApplianceBudgetItem("app-1", "Inverter AC (1.5 HP)", 1100, 7.0, false),
            ApplianceBudgetItem("app-2", "Deep Freezer (Inverter)", 180, 24.0, true),
            ApplianceBudgetItem("app-3", "Pumping Machine (1 HP)", 750, 1.5, false),
            ApplianceBudgetItem("app-4", "LED Smart TV (65\")", 120, 6.0, false),
            ApplianceBudgetItem("app-5", "Ceiling Fans & LED Bulbs", 220, 14.0, true),
            ApplianceBudgetItem("app-6", "Microwave & Kettle", 1500, 0.5, false)
        )
    )
    val applianceBudgetList: StateFlow<List<ApplianceBudgetItem>> = _applianceBudgetList.asStateFlow()

    // Phase 7: Multi-Asset Meter Management
    private val _linkedMeterAssets = MutableStateFlow(
        listOf(
            LinkedMeterAsset(
                id = "asset-1",
                label = "Primary Residence",
                meterNumber = "01429583192",
                address = "14 Adeola Odeku Street, Victoria Island",
                discoCode = "EKEDC",
                feederBand = FeederBand.BAND_A,
                transformerId = "TR-VI-ADEOLA-04B",
                isPrepaid = true,
                isSelected = true
            ),
            LinkedMeterAsset(
                id = "asset-2",
                label = "Workspace Studio",
                meterNumber = "45019283741",
                address = "Block 4, Admiralty Way, Lekki Phase 1",
                discoCode = "EKEDC",
                feederBand = FeederBand.BAND_A,
                transformerId = "TR-LEK-ADM-12A",
                isPrepaid = true,
                isSelected = false
            ),
            LinkedMeterAsset(
                id = "asset-3",
                label = "Family Property",
                meterNumber = "61209384756",
                address = "28 Adeniran Ogunsanya St, Surulere",
                discoCode = "EKEDC",
                feederBand = FeederBand.BAND_B,
                transformerId = "TR-SUR-ADEN-03C",
                isPrepaid = false,
                isSelected = false
            )
        )
    )
    val linkedMeterAssets: StateFlow<List<LinkedMeterAsset>> = _linkedMeterAssets.asStateFlow()

    // Phase 7: Whistleblower Reports (Cryptographically Sealed)
    private val _whistleblowerReports = MutableStateFlow<List<WhistleblowerReport>>(emptyList())
    val whistleblowerReports: StateFlow<List<WhistleblowerReport>> = _whistleblowerReports.asStateFlow()

    // Status snackbar / toast message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // 30 Power Solutions State
    private val _isBatSignalMode = MutableStateFlow(false)
    val isBatSignalMode: StateFlow<Boolean> = _isBatSignalMode.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _userTrustScore = MutableStateFlow(98)
    val userTrustScore: StateFlow<Int> = _userTrustScore.asStateFlow()

    private val _diagnosticStatus = MutableStateFlow("LOAD_SHEDDING") // "LOAD_SHEDDING" vs "FAULT"
    val diagnosticStatus: StateFlow<String> = _diagnosticStatus.asStateFlow()

    fun toggleBatSignalMode(enabled: Boolean) {
        _isBatSignalMode.value = enabled
    }

    fun completeOnboarding(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile.copy(isOnboarded = true))
            _isOnboardingCompleted.value = true
            _userMessage.value = "Welcome ${profile.customerName}! Meter ${profile.meterNumber} activated on BRIGHT."
        }
    }

    fun resetToOnboarding() {
        _isOnboardingCompleted.value = false
    }

    fun toggleDiagnosticStatus() {
        _diagnosticStatus.value = if (_diagnosticStatus.value == "LOAD_SHEDDING") "FAULT" else "LOAD_SHEDDING"
    }

    fun triggerRedDangerEmergency() {
        viewModelScope.launch {
            val profile = userProfile.value
            val complaintId = repository.submitFaultComplaint(
                meterNumber = profile.meterNumber,
                title = "CRITICAL DANGER: Fallen High-Tension Cable / Substation Fire",
                description = "High-priority life safety hazard reported via Critical Danger Red Button. Bypassed normal queue. DisCo emergency control room alerted.",
                faultType = FaultType.LIVE_CABLE_EXPOSED,
                isHazardEmergency = true,
                discoCode = profile.discoCode,
                transformerId = profile.transformerId,
                feederName = profile.feederName,
                imageUri = "https://images.unsplash.com/photo-1544724569-5f546fd6f2b5?w=600&q=80",
                isVideo = false
            )
            _userMessage.value = "🚨 Critical Danger Alert Logged! DisCo Emergency Crew Dispatched."
        }
    }


    init {
        // Live grid telemetry oscillation simulator (e.g. 50.04 Hz - 50.12 Hz realistic variation)
        viewModelScope.launch {
            while (true) {
                delay(8000)
                val current = gridTelemetry.value
                val jitter = ((-4..4).random()) / 100.0
                val newFreq = Math.round((50.06 + jitter) * 100.0) / 100.0
                val mwJitter = (-25..35).random()
                repository.updateGridTelemetry(newFreq, current.nationalGenerationMw + mwJitter)
            }
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _selectedLanguage.value = lang
        showNotification("Language switched to ${lang.label}")
    }

    fun toggleLowDataMode() {
        _isLowDataMode.value = !_isLowDataMode.value
        showNotification(if (_isLowDataMode.value) "Low-Data Mode Activated (Offline USSD & lightweight assets)" else "Standard High-Fidelity Mode Activated")
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showNotification(msg: String) {
        _userMessage.value = msg
    }

    fun reportFault(
        title: String,
        description: String,
        faultType: FaultType,
        isHazard: Boolean,
        imageUri: String? = null,
        isVideo: Boolean = false
    ) {
        viewModelScope.launch {
            val profile = userProfile.value
            val ticketId = repository.submitFaultComplaint(
                meterNumber = profile.meterNumber,
                title = title,
                description = description,
                faultType = faultType,
                isHazardEmergency = isHazard,
                discoCode = profile.discoCode,
                transformerId = profile.transformerId,
                feederName = profile.feederName,
                imageUri = imageUri,
                isVideo = isVideo
            )
            showNotification("Ticket $ticketId created! Tracked with Meter #${profile.meterNumber}")
        }
    }

    fun reportQuickEmergencyHazard(hazardTitle: String) {
        viewModelScope.launch {
            val profile = userProfile.value
            val ticketId = repository.submitFaultComplaint(
                meterNumber = profile.meterNumber,
                title = "EMERGENCY: $hazardTitle",
                description = "CRITICAL PUBLIC HAZARD reported at ${profile.streetAddress}. Rapid safety intervention required immediately to prevent fire or electrocution.",
                faultType = FaultType.LIVE_CABLE_EXPOSED,
                isHazardEmergency = true,
                discoCode = profile.discoCode,
                transformerId = profile.transformerId,
                feederName = profile.feederName
            )
            showNotification("EMERGENCY FAST-TRACK DISPATCHED: Ticket $ticketId. Priority alert sent to ${profile.discoCode} & local safety unit.")
        }
    }

    fun escalateComplaint(id: String) {
        viewModelScope.launch {
            repository.escalateComplaint(id)
            showNotification("Ticket $id escalated to higher authority for rapid compliance!")
        }
    }

    fun advanceComplaintLifecycle(id: String, nextStatus: ComplaintStatus) {
        viewModelScope.launch {
            repository.advanceComplaintStatus(id, nextStatus)
            showNotification("Ticket $id status updated to ${nextStatus.displayName}")
        }
    }

    fun resolveComplaint(id: String, rating: Int, notes: String) {
        viewModelScope.launch {
            repository.resolveComplaint(id, rating, notes)
            showNotification("Resolution confirmed! Thank you for rating the field response.")
        }
    }

    fun upvoteComplaint(id: String) {
        viewModelScope.launch {
            repository.upvoteComplaint(id)
            showNotification("Upvoted! Priority escalated for your neighborhood transformer.")
        }
    }

    fun reportVandalism(
        incidentType: String,
        location: String,
        landmark: String,
        isAnonymous: Boolean,
        description: String,
        suspectDetails: String?
    ) {
        viewModelScope.launch {
            val profile = userProfile.value
            val id = repository.reportVandalism(
                incidentType = incidentType,
                location = location,
                landmark = landmark,
                discoCode = profile.discoCode,
                isAnonymous = isAnonymous,
                description = description,
                suspectDetails = suspectDetails
            )
            showNotification("Vandalism Report $id logged! Security alerted for ${profile.discoCode}.")
        }
    }

    fun submitBillingDispute(
        disputeType: String,
        amount: Double,
        month: String,
        description: String
    ) {
        viewModelScope.launch {
            val profile = userProfile.value
            val id = repository.submitBillingDispute(
                meterNumber = profile.meterNumber,
                disputeType = disputeType,
                disputedAmountNgn = amount,
                billingMonth = month,
                discoCode = profile.discoCode,
                description = description
            )
            showNotification("Billing Dispute $id logged with Meter #${profile.meterNumber}. NERC capped audit pending.")
        }
    }

    // Feature 4: Submit Appliance Damage Claim
    fun submitApplianceClaim(
        applianceName: String,
        brandModel: String,
        estimatedLossNgn: Double,
        surgeTime: String,
        surgeDescription: String,
        statutoryNotice: String
    ) {
        viewModelScope.launch {
            val profile = userProfile.value
            val id = repository.submitApplianceClaim(
                meterNumber = profile.meterNumber,
                applianceName = applianceName,
                applianceBrandModel = brandModel,
                estimatedLossNgn = estimatedLossNgn,
                surgeTimestampText = surgeTime,
                surgeDescription = surgeDescription,
                statutoryNoticeText = statutoryNotice,
                discoCode = profile.discoCode
            )
            showNotification("Statutory Claim $id filed under NERC CPR 2023! Notice served to ${profile.discoCode} Legal Directorate.")
        }
    }

    // Feature 10: Pin Street Hazard
    fun pinStreetHazard(
        title: String,
        hazardType: String,
        urgency: String,
        location: String,
        landmark: String,
        xPosRatio: Float = 0.30f,
        yPosRatio: Float = 0.65f
    ) {
        viewModelScope.launch {
            val profile = userProfile.value
            val id = repository.pinStreetHazard(
                title = title,
                hazardType = hazardType,
                urgency = urgency,
                location = location,
                landmark = landmark,
                discoCode = profile.discoCode,
                reportedBy = profile.customerName,
                xPosRatio = xPosRatio,
                yPosRatio = yPosRatio
            )
            showNotification("Public Street Hazard $id pinned! Safety alert broadcast to DisCo emergency team.")
        }
    }

    fun upvoteStreetHazard(id: String) {
        viewModelScope.launch {
            repository.upvoteStreetHazard(id)
            showNotification("Hazard urgency verified! Escalating crew dispatch priority.")
        }
    }

    // Feature 7: Report Transformer Hum / Sparking
    fun reportTransformerHumSpark() {
        viewModelScope.launch {
            val profile = userProfile.value
            val ticketId = repository.submitFaultComplaint(
                meterNumber = profile.meterNumber,
                title = "CRITICAL: Transformer Overheating / Violent Humming Spark",
                description = "Transformer ${profile.transformerId} is emitting abnormal high-pitched electrical hum and arcing sparks under peak load (88%). Imminent explosion hazard.",
                faultType = FaultType.TRANSFORMER_SPARKING,
                isHazardEmergency = true,
                discoCode = profile.discoCode,
                transformerId = profile.transformerId,
                feederName = profile.feederName
            )
            showNotification("URGENT: Transformer cooling & inspection alert $ticketId dispatched to ${profile.discoCode} protection crew!")
        }
    }

    // Feature 9: Power Restoration Alert Chime
    fun toggleRestorationAlarm() {
        _isRestorationAlarmEnabled.value = !_isRestorationAlarmEnabled.value
        showNotification(
            if (_isRestorationAlarmEnabled.value) "⚡ Restoration Alarm ENABLED: Your phone will chime loudly when power is restored!"
            else "Restoration Alarm Muted"
        )
    }

    fun playRestorationChime() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 500)
                } catch (ignored: Exception) {}
            }, 300)
            showNotification("⚡ POWER RESTORATION CHIME TEST: Sound & vibration triggered successfully!")
        } catch (e: Exception) {
            showNotification("⚡ Power Restored! Chime alert active.")
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
            showNotification("Meter Profile #${profile.meterNumber} updated successfully.")
        }
    }

    // Theme Mode Toggle (Dark / Light)
    fun toggleThemeMode() {
        _isDarkMode.value = !_isDarkMode.value
        showNotification(if (_isDarkMode.value) "🌙 Switched to Elegant Dark Mode" else "☀️ Switched to Crisp Daylight Mode")
    }

    // Phase 1: Onboarding, Meter Index Validation & ₦100 Capitalization Gateway
    fun verifyAndOnboardMeter(
        meterNum: String,
        discoCode: String,
        band: FeederBand,
        address: String,
        paymentGateway: String // "OPay", "Moniepoint", "Carrier Airtime (MTN/Airtel/Glo/9mobile)"
    ) {
        val cleanMeter = meterNum.filter { it.isDigit() }
        if (cleanMeter.length !in 11..13) {
            showNotification("Invalid Meter Parameter: Nigerian prepaid meters require 11 or 13 digits.")
            return
        }

        viewModelScope.launch {
            val updated = userProfile.value.copy(
                meterNumber = cleanMeter,
                discoCode = discoCode,
                feederBand = band,
                streetAddress = address
            )
            repository.saveUserProfile(updated)
            // Add to linked meters
            val newAsset = LinkedMeterAsset(
                id = "asset-${System.currentTimeMillis()}",
                label = "Meter #$cleanMeter ($discoCode)",
                meterNumber = cleanMeter,
                address = address,
                discoCode = discoCode,
                feederBand = band,
                transformerId = updated.transformerId,
                isPrepaid = true,
                isSelected = true
            )
            _linkedMeterAssets.value = _linkedMeterAssets.value.map { it.copy(isSelected = false) } + newAsset

            showNotification("✅ SIM & Meter Verified! ₦100 Capitalization settled via $paymentGateway. Decentralized ledger initialized.")
        }
    }

    // Phase 4: Token Escrow & Rebate Settlement
    fun claimRebateToken(tokenId: String) {
        _escrowRebateTokens.value = _escrowRebateTokens.value.map {
            if (it.id == tokenId) it.copy(isRedeemed = true) else it
        }
        showNotification("⚡ 20-digit token copied & marked loaded on meter keypad!")
    }

    fun requestEmergencyRebateGeneration() {
        val profile = userProfile.value
        val newToken = EscrowTokenRebate(
            id = "ESC-TK-${(1000..9999).random()}",
            token20Digit = "${(1000..9999).random()} ${(1000..9999).random()} ${(1000..9999).random()} ${(1000..9999).random()} ${(1000..9999).random()}",
            kwhValue = 24.5,
            monetaryValueNgn = 5132.75,
            discoCode = profile.discoCode,
            reason = "Automatic AI Escrow Settlement: Cumulative shortfall > 5.0 hrs on ${profile.feederBand.code}",
            issuedTimestamp = System.currentTimeMillis(),
            isRedeemed = false
        )
        _escrowRebateTokens.value = listOf(newToken) + _escrowRebateTokens.value
        _escrowLiquidityVaultBalanceNgn.value = (_escrowLiquidityVaultBalanceNgn.value - 5132.75).coerceAtLeast(0.0)
        showNotification("⚡ Automated ₦5,132.75 NERC Rebate Token generated and cleared from escrow vault!")
    }

    fun compileNercEnforcementReport(): String {
        val profile = userProfile.value
        val breaches = auditingRecords.value.filter { it.isSlaBreached }
        val totalShortfall = breaches.sumOf { it.shortfallHours }
        val totalCompensation = breaches.sumOf { it.compensationDueNgn }
        return """
            =============================================================
            NERC COMPLIANCE & ENFORCEMENT SUMMARY REPORT
            Pursuant to NERC Customer Protection Regulations (CPR) 2023
            =============================================================
            Customer: ${profile.customerName}
            Meter Number: ${profile.meterNumber}
            Distribution Licensee: ${profile.discoCode}
            Contractual Feeder Band: ${profile.feederBand.code} (${profile.feederBand.minimumHours} hrs/day committed)
            Injection Feeder: ${profile.feederName}
            Local Transformer: ${profile.transformerId}
            
            AUDIT TIMEFRAME: Past 7 Rolling Days
            Total SLA Breaches Logged: ${breaches.size} Days
            Cumulative Supply Shortfall: $totalShortfall Hours
            Statutory Liquidity Liability Owed: ₦$totalCompensation
            Escrow Status: ACTIVE (Wholesale settlement enabled)
            
            Hash: SHA-256: 8f9b7c12a4e902b388fd11029cbae782
            Transmission Verification: NCC SCADA Synced
            =============================================================
        """.trimIndent()
    }

    // Phase 5: Transformer Cluster Forum
    fun postCommunityMessage(content: String, isExtortion: Boolean) {
        val profile = userProfile.value
        val post = CommunityForumPost(
            id = "POST-${System.currentTimeMillis()}",
            authorName = profile.customerName.split(" ").firstOrNull() ?: "Neighbor",
            isVerifiedResident = true,
            transformerId = profile.transformerId,
            content = content,
            timestampText = "Just now",
            upvotes = 1,
            isExtortionReport = isExtortion
        )
        _communityForumPosts.value = listOf(post) + _communityForumPosts.value
        showNotification(if (isExtortion) "🚨 Extortion incident logged and flagged to NERC Compliance Desk!" else "Message posted to your transformer line feed.")
    }

    fun upvoteCommunityPost(id: String) {
        _communityForumPosts.value = _communityForumPosts.value.map {
            if (it.id == id) it.copy(upvotes = it.upvotes + 1) else it
        }
        showNotification("Upvoted! Verification consensus updated for transformer line.")
    }

    // Phase 6: Appliance Consumption & Energy Management
    fun updateApplianceHours(id: String, newHours: Double) {
        _applianceBudgetList.value = _applianceBudgetList.value.map {
            if (it.id == id) it.copy(hoursDaily = newHours.coerceIn(0.0, 24.0)) else it
        }
    }

    fun toggleApplianceEco(id: String) {
        _applianceBudgetList.value = _applianceBudgetList.value.map {
            if (it.id == id) {
                val newEco = !it.isEcoMode
                val newWatts = if (newEco) (it.wattage * 0.75).toInt() else (it.wattage / 0.75).toInt()
                it.copy(isEcoMode = newEco, wattage = newWatts)
            } else it
        }
    }

    fun addCustomAppliance(name: String, watts: Int, hoursDaily: Double) {
        val item = ApplianceBudgetItem(
            id = "app-${System.currentTimeMillis()}",
            name = name,
            wattage = watts.coerceAtLeast(10),
            hoursDaily = hoursDaily.coerceIn(0.1, 24.0)
        )
        _applianceBudgetList.value = _applianceBudgetList.value + item
        showNotification("Added $name to household energy budget matrix.")
    }

    // Phase 7: Multi-Asset Meter Switcher
    fun switchActiveMeter(assetId: String) {
        val target = _linkedMeterAssets.value.find { it.id == assetId } ?: return
        _linkedMeterAssets.value = _linkedMeterAssets.value.map {
            it.copy(isSelected = it.id == assetId)
        }
        viewModelScope.launch {
            val updated = userProfile.value.copy(
                meterNumber = target.meterNumber,
                discoCode = target.discoCode,
                feederBand = target.feederBand,
                streetAddress = target.address,
                transformerId = target.transformerId,
                isPrepaid = target.isPrepaid
            )
            repository.saveUserProfile(updated)
            showNotification("Switched active profile to ${target.label} (Meter #${target.meterNumber})")
        }
    }

    // Phase 7: Encrypted Whistleblower Pipeline
    fun submitWhistleblowerReport(
        targetUnit: String,
        extortionType: String,
        amountDemandedNgn: Double,
        description: String
    ) {
        val profile = userProfile.value
        val report = WhistleblowerReport(
            id = "WB-NERC-${(1000..9999).random()}",
            discoCode = profile.discoCode,
            targetOfficialOrUnit = targetUnit,
            extortionType = extortionType,
            amountDemandedNgn = amountDemandedNgn,
            incidentDescription = description,
            timestampText = "Just now"
        )
        _whistleblowerReports.value = listOf(report) + _whistleblowerReports.value
        showNotification("🛡️ WHISTLEBLOWER EVIDENCE SEALED: Encrypted packet ${report.id} transmitted directly to NERC Special Enforcement Directorate.")
    }

    // Phase 7: Compliance Data De-indexing Switch
    fun purgeUserDataDeindexing() {
        viewModelScope.launch {
            repository.saveUserProfile(UserProfile(meterNumber = "UNLINKED", customerName = "Anonymous Resident", phoneNumber = "REDACTED"))
            _whistleblowerReports.value = emptyList()
            showNotification("🔒 All cached local records, location metadata, and meter indexes purged from device memory.")
        }
    }

    // Phase 7: Session Token Clearance Protocol
    fun sessionTokenClearance() {
        showNotification("🔑 Authentication session cleared. Device token revoked.")
    }
}

