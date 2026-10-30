package com.example.alarm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActiveAlarmData(
    val alarmId: Int,
    val label: String,
    val timeFormatted: String,
    val isTestAlarm: Boolean = false
)

object AlarmStateHolder {
    private val _activeAlarm = MutableStateFlow<ActiveAlarmData?>(null)
    val activeAlarm: StateFlow<ActiveAlarmData?> = _activeAlarm.asStateFlow()

    fun setRinging(alarm: ActiveAlarmData) {
        _activeAlarm.value = alarm
    }

    fun clearRinging() {
        _activeAlarm.value = null
    }

    val isRinging: Boolean
        get() = _activeAlarm.value != null
}
