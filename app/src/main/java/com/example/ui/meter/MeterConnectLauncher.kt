package com.example.ui.meter

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Lets screens that don't hold the dialog state (e.g. Tool #9 Universal Meter Sync) open the
 * Smart Meter Gateway dialog that MainActivity owns.
 */
object MeterConnectLauncher {
    private val _requests = MutableStateFlow(0)
    val requests: StateFlow<Int> = _requests.asStateFlow()

    fun open() {
        _requests.value = _requests.value + 1
    }

    /** Called once the dialog is shown, so it doesn't reopen on recomposition. */
    fun consume() {
        _requests.value = 0
    }
}
