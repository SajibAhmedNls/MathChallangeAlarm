package com.hellosajib.alarmapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hellosajib.alarmapp.alarm.ActiveAlarmData
import com.hellosajib.alarmapp.alarm.AlarmRingingService
import com.hellosajib.alarmapp.alarm.AlarmScheduler
import com.hellosajib.alarmapp.alarm.AlarmStateHolder
import com.hellosajib.alarmapp.data.AlarmDao
import com.hellosajib.alarmapp.data.AlarmEntity
import com.hellosajib.alarmapp.data.AlarmRepository
import com.hellosajib.alarmapp.data.AppDatabase
import com.hellosajib.alarmapp.math.MathEngine
import com.hellosajib.alarmapp.math.MathProblem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class MathChallengeState(
    val currentProblem: MathProblem = MathEngine.generateProblem(),
    val streak: Int = 0,
    val targetStreak: Int = 3,
    val currentInput: String = "",
    val errorMessage: String? = null,
    val isSolved: Boolean = false,
    val isShaking: Boolean = false
)

data class NextAlarmInfo(
    val countdown: String,
    val time: String,
    val label: String
)

const val DEFAULT_ALARM_LABEL = "Wake up"

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AlarmRepository
    private val alarmScheduler: AlarmScheduler = AlarmScheduler(application)

    val allAlarms: StateFlow<List<AlarmEntity>>

    val activeRingingAlarm: StateFlow<ActiveAlarmData?> = AlarmStateHolder.activeAlarm

    private val _mathState = MutableStateFlow(MathChallengeState())
    val mathState: StateFlow<MathChallengeState> = _mathState.asStateFlow()

    private val _isEditDialogOpen = MutableStateFlow(false)
    val isEditDialogOpen: StateFlow<Boolean> = _isEditDialogOpen.asStateFlow()

    private val _editingAlarm = MutableStateFlow<AlarmEntity?>(null)
    val editingAlarm: StateFlow<AlarmEntity?> = _editingAlarm.asStateFlow()

    private val _timeToNextAlarm = MutableStateFlow<NextAlarmInfo?>(null)
    val timeToNextAlarm: StateFlow<NextAlarmInfo?> = _timeToNextAlarm.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AlarmRepository(db.alarmDao())
        allAlarms = repository.allAlarms.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed only once on initial installation
        val prefs = application.getSharedPreferences("uthosajib_prefs", android.content.Context.MODE_PRIVATE)
        val hasSeeded = prefs.getBoolean("has_seeded_initial_alarms_v2", false)
        if (!hasSeeded) {
            prefs.edit().putBoolean("has_seeded_initial_alarms_v2", true).apply()
            seedInitialAlarms()
        }

        // Periodically refresh next alarm countdown
        viewModelScope.launch {
            allAlarms.collect { alarms ->
                updateNextAlarmCountdown(alarms)
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(30_000)
                updateNextAlarmCountdown(allAlarms.value)
            }
        }
    }

    private fun seedInitialAlarms() {
        viewModelScope.launch {
            val default1 = AlarmEntity(
                hour = 6,
                minute = 30,
                isEnabled = true,
                daysOfWeek = "1,2,3,4,5",
                label = "Work days",
                vibrate = true
            )
            val default2 = AlarmEntity(
                hour = 8,
                minute = 0,
                isEnabled = false,
                daysOfWeek = "6,7",
                label = "Weekend",
                vibrate = true
            )
            val id1 = repository.insertAlarm(default1)
            repository.insertAlarm(default2)
            alarmScheduler.scheduleAlarm(default1.copy(id = id1.toInt()))
        }
    }

    fun deleteAllAlarms() {
        viewModelScope.launch {
            for (alarm in allAlarms.value) {
                alarmScheduler.cancelAlarm(alarm.id)
            }
            repository.deleteAllAlarms()
        }
    }

    private fun updateNextAlarmCountdown(alarms: List<AlarmEntity>) {
        val enabledAlarms = alarms.filter { it.isEnabled }
        if (enabledAlarms.isEmpty()) {
            _timeToNextAlarm.value = null
            return
        }

        val now = System.currentTimeMillis()
        var nearestTrigger = Long.MAX_VALUE
        var nearestAlarm: AlarmEntity? = null

        for (alarm in enabledAlarms) {
            val trigger = AlarmScheduler.computeNextTriggerTime(alarm)
            if (trigger in (now + 1)..<nearestTrigger) {
                nearestTrigger = trigger
                nearestAlarm = alarm
            }
        }

        if (nearestAlarm == null || nearestTrigger == Long.MAX_VALUE) {
            _timeToNextAlarm.value = null
            return
        }

        val diffMs = nearestTrigger - now
        val diffMins = (diffMs / (1000 * 60)).toInt()
        val hours = diffMins / 60
        val mins = diffMins % 60

        val days = hours / 24
        val timeString = when {
            days > 0 -> "${days}d ${hours % 24}h"
            hours > 0 -> "${hours}h ${mins}m"
            mins > 0 -> "${mins}m"
            else -> "less than 1m"
        }
        _timeToNextAlarm.value = NextAlarmInfo(
            countdown = timeString,
            time = nearestAlarm.getFormattedTime(),
            label = nearestAlarm.label
        )
    }

    fun startMathChallenge(force: Boolean = false) {
        val current = _mathState.value
        // If an active problem is already on screen, do NOT overwrite it unless explicitly forced!
        if (!force && !current.isSolved && current.currentProblem.expression.isNotBlank()) {
            return
        }
        _mathState.value = MathChallengeState(
            currentProblem = MathEngine.generateProblem(),
            streak = 0,
            targetStreak = 3,
            currentInput = "",
            errorMessage = null,
            isSolved = false,
            isShaking = false
        )
    }

    fun resetMathChallenge() {
        startMathChallenge(force = true)
    }

    fun onKeypadDigit(digit: String) {
        val current = _mathState.value
        if (current.isSolved) return
        if (current.currentInput.length >= 4) return // answer limit

        _mathState.value = current.copy(
            currentInput = current.currentInput + digit,
            errorMessage = null
        )
    }

    fun onKeypadBackspace() {
        val current = _mathState.value
        if (current.isSolved) return
        if (current.currentInput.isNotEmpty()) {
            _mathState.value = current.copy(
                currentInput = current.currentInput.dropLast(1),
                errorMessage = null
            )
        }
    }

    fun onKeypadClear() {
        val current = _mathState.value
        if (current.isSolved) return
        _mathState.value = current.copy(
            currentInput = "",
            errorMessage = null
        )
    }

    fun onKeypadSubmit() {
        val current = _mathState.value
        if (current.isSolved) return
        if (current.currentInput.isBlank()) return

        val isCorrect = MathEngine.isAnswerCorrect(current.currentProblem, current.currentInput)
        if (isCorrect) {
            val newStreak = current.streak + 1
            if (newStreak >= current.targetStreak) {
                // All 3 solved consecutively! STOP THE ALARM!
                _mathState.value = current.copy(
                    streak = newStreak,
                    isSolved = true,
                    currentInput = "",
                    errorMessage = null
                )
                stopActiveAlarm()
            } else {
                // Solved 1 or 2, generate next problem
                _mathState.value = current.copy(
                    streak = newStreak,
                    currentProblem = MathEngine.generateProblem(current.currentProblem),
                    currentInput = "",
                    errorMessage = null
                )
            }
        } else {
            // Incorrect answer! STREAK RESETS TO 0!
            viewModelScope.launch {
                _mathState.value = current.copy(
                    streak = 0, // Reset!
                    errorMessage = "Not quite. Progress reset, try this one.",
                    isShaking = true,
                    currentInput = ""
                )
                delay(600)
                _mathState.value = _mathState.value.copy(
                    currentProblem = MathEngine.generateProblem(current.currentProblem),
                    isShaking = false
                )
            }
        }
    }

    fun stopActiveAlarm() {
        com.hellosajib.alarmapp.alarm.AlarmLockManager.clearRinging(getApplication())
        AlarmRingingService.stop(getApplication())
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            repository.updateAlarm(updated)
            if (updated.isEnabled) {
                alarmScheduler.scheduleAlarm(updated)
            } else {
                alarmScheduler.cancelAlarm(updated.id)
            }
        }
    }

    fun openCreateDialog() {
        _editingAlarm.value = null
        _isEditDialogOpen.value = true
    }

    fun openEditDialog(alarm: AlarmEntity) {
        _editingAlarm.value = alarm
        _isEditDialogOpen.value = true
    }

    fun closeDialog() {
        _isEditDialogOpen.value = false
        _editingAlarm.value = null
    }

    fun saveAlarm(
        hour: Int,
        minute: Int,
        daysOfWeek: String,
        label: String,
        vibrate: Boolean
    ) {
        viewModelScope.launch {
            val editing = _editingAlarm.value
            val labelText = if (label.isBlank()) DEFAULT_ALARM_LABEL else label.trim()

            if (editing != null) {
                val updated = editing.copy(
                    hour = hour,
                    minute = minute,
                    daysOfWeek = daysOfWeek,
                    label = labelText,
                    vibrate = vibrate,
                    isEnabled = true
                )
                repository.updateAlarm(updated)
                alarmScheduler.scheduleAlarm(updated)
            } else {
                val newAlarm = AlarmEntity(
                    hour = hour,
                    minute = minute,
                    isEnabled = true,
                    daysOfWeek = daysOfWeek,
                    label = labelText,
                    vibrate = vibrate
                )
                val id = repository.insertAlarm(newAlarm)
                alarmScheduler.scheduleAlarm(newAlarm.copy(id = id.toInt()))
            }
            closeDialog()
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            alarmScheduler.cancelAlarm(alarm.id)
            repository.deleteAlarm(alarm)
        }
    }
}
