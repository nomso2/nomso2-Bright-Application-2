package com.example.ui.screens

import androidx.compose.runtime.Immutable
import com.example.data.service.CitizenMeterStatus
import com.example.model.AuditingHourRecord
import com.example.model.Complaint
import com.example.model.GridTelemetry
import com.example.model.TransformerOverloadTelemetry
import com.example.model.UserProfile

/**
 * Everything HomeScreen displays. Built in MainActivity from the ViewModel's StateFlows.
 * (Replaces ~15 individual HomeScreen parameters.)
 */
@Immutable
data class HomeUiState(
    val userProfile: UserProfile,
    val personalComplaints: List<Complaint>,
    val telemetry: GridTelemetry,
    val isDarkMode: Boolean = true,
    val themeMode: com.example.ui.theme.ThemeMode = com.example.ui.theme.ThemeMode.SYSTEM,
    val auditingRecords: List<AuditingHourRecord> = emptyList(),
    val transformerTelemetry: TransformerOverloadTelemetry = TransformerOverloadTelemetry(),
    val isRestorationAlarmEnabled: Boolean = true,
    val diagnosticStatus: String = "LOAD_SHEDDING",
    val userTrustScore: Int = 98,
    val citizenMeterStatus: CitizenMeterStatus? = null,
    val surgeWarningActive: Boolean = false,
    val surgeCountdownSeconds: Int = 180,
    val pendingSyncCount: Int = 0,
    val isBatSignalMode: Boolean = false
)

/**
 * Every callback HomeScreen can fire. All default to no-ops so previews/tests only pass what they need.
 * (Replaces ~30 individual HomeScreen lambda parameters.)
 */
@Immutable
data class HomeActions(
    val onSetThemeMode: (com.example.ui.theme.ThemeMode) -> Unit = { _ -> },
    val onReportFaultClicked: () -> Unit = {},
    val onEmergencyHazardTriggered: (String) -> Unit = { _ -> },
    val onEscalateComplaint: (String) -> Unit = { _ -> },
    val onUpvoteComplaint: (String) -> Unit = { _ -> },
    val onConfirmResolution: (String) -> Unit = { _ -> },
    val onEditProfileClicked: () -> Unit = {},
    val onOpenOnboarding: () -> Unit = {},
    val onOpenClearinghouse: () -> Unit = {},
    val onOpenTransformerForum: () -> Unit = {},
    val onOpenEnergyOptimization: () -> Unit = {},
    val onOpenProfileAdmin: () -> Unit = {},
    val onReportTransformerHumSpark: () -> Unit = {},
    val onToggleRestorationAlarm: () -> Unit = {},
    val onPlayRestorationChime: () -> Unit = {},
    val onNavigateMap: () -> Unit = {},
    val onNavigateVandalism: () -> Unit = {},
    val onNavigateHistory: () -> Unit = {},
    val onNavigateHub: () -> Unit = {},
    val onNavigateMore: () -> Unit = {},
    val onOpenHelp: () -> Unit = {},
    val onOpenRedDangerSOS: () -> Unit = {},
    val onToggleDiagnosticStatus: () -> Unit = {},
    val onOpenEstateExcoDossier: () -> Unit = {},
    val onOpenSmartMeterGateway: () -> Unit = {},
    val onAutoDetectSmartMeter: () -> Unit = {},
    val onLockApp: () -> Unit = {},
    val onLogOut: () -> Unit = {},
    val onTriggerSurgeSiren: () -> Unit = {},
    val onDismissSurgeWarning: () -> Unit = {},
    val onSyncNow: () -> Unit = {},
    val onToggleBatSignalMode: (Boolean) -> Unit = { _ -> }
)
