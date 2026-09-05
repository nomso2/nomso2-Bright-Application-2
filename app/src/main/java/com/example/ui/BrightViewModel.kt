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

    // UI state
    private val _selectedLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    private val _isLowDataMode = MutableStateFlow(false)
    val isLowDataMode: StateFlow<Boolean> = _isLowDataMode.asStateFlow()

    // Status snackbar / toast message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

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

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
            showNotification("Meter Profile #${profile.meterNumber} updated successfully.")
        }
    }
}
