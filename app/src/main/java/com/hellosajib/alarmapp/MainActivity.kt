package com.hellosajib.alarmapp

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hellosajib.alarmapp.alarm.ActiveAlarmData
import com.hellosajib.alarmapp.alarm.AlarmRingingService
import com.hellosajib.alarmapp.alarm.AlarmStateHolder
import com.hellosajib.alarmapp.ui.AlarmViewModel
import com.hellosajib.alarmapp.ui.dialogs.AlarmEditDialog
import com.hellosajib.alarmapp.ui.screens.AlarmListScreen
import com.hellosajib.alarmapp.ui.screens.AlarmRingingScreen
import com.hellosajib.alarmapp.ui.theme.UthoSajibTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AlarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Turn on screen and show over keyguard/lock screen during ringing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            km?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        checkAndRestoreRingingState()
        handleAlarmIntent(intent)

        setContent {
            UthoSajibTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NotificationPermissionRequester()

                    val activeAlarm by viewModel.activeRingingAlarm.collectAsStateWithLifecycle()
                    val alarms by viewModel.allAlarms.collectAsStateWithLifecycle()
                    val mathState by viewModel.mathState.collectAsStateWithLifecycle()
                    val isEditDialogOpen by viewModel.isEditDialogOpen.collectAsStateWithLifecycle()
                    val editingAlarm by viewModel.editingAlarm.collectAsStateWithLifecycle()
                    val nextCountdown by viewModel.timeToNextAlarm.collectAsStateWithLifecycle()

                    // Match status/navigation bar icons to the screen actually showing
                    val darkBars = activeAlarm != null || isSystemInDarkTheme()
                    DisposableEffect(darkBars) {
                        val transparent = android.graphics.Color.TRANSPARENT
                        val style = if (darkBars) SystemBarStyle.dark(transparent)
                        else SystemBarStyle.light(transparent, transparent)
                        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                        onDispose { }
                    }

                    if (activeAlarm != null) {
                        // Always dark while ringing so a light theme doesn't blind anyone at 6 AM
                        UthoSajibTheme(darkTheme = true) {
                            AlarmRingingScreen(
                                activeAlarm = activeAlarm,
                                mathState = mathState,
                                onKeypadDigit = { digit -> viewModel.onKeypadDigit(digit) },
                                onKeypadBackspace = { viewModel.onKeypadBackspace() },
                                onKeypadSubmit = { viewModel.onKeypadSubmit() },
                                onFinishDismiss = {
                                    viewModel.resetMathChallenge()
                                    com.hellosajib.alarmapp.alarm.AlarmLockManager.clearRinging(this@MainActivity)
                                    com.hellosajib.alarmapp.alarm.AlarmRingingService.stop(this@MainActivity)
                                    AlarmStateHolder.clearRinging()
                                }
                            )
                        }
                    } else {
                        AlarmListScreen(
                            alarms = alarms,
                            nextAlarm = nextCountdown,
                            onToggleAlarm = { alarm -> viewModel.toggleAlarm(alarm) },
                            onEditAlarm = { alarm -> viewModel.openEditDialog(alarm) },
                            onDeleteAlarm = { alarm -> viewModel.deleteAlarm(alarm) },
                            onDeleteAllAlarms = { viewModel.deleteAllAlarms() },
                            onAddAlarmClick = { viewModel.openCreateDialog() }
                        )

                        if (isEditDialogOpen) {
                            AlarmEditDialog(
                                initialAlarm = editingAlarm,
                                onDismiss = { viewModel.closeDialog() },
                                onSave = { h, m, days, label, vib ->
                                    viewModel.saveAlarm(h, m, days, label, vib)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        com.hellosajib.alarmapp.alarm.AlarmLockManager.isUiForeground = true
    }

    override fun onStop() {
        super.onStop()
        com.hellosajib.alarmapp.alarm.AlarmLockManager.isUiForeground = false
    }

    override fun onResume() {
        super.onResume()
        com.hellosajib.alarmapp.alarm.AlarmLockManager.isUiForeground = true
        checkAndRestoreRingingState()
    }

    private fun checkAndRestoreRingingState() {
        if (com.hellosajib.alarmapp.alarm.AlarmLockManager.isRinging(this)) {
            val info = com.hellosajib.alarmapp.alarm.AlarmLockManager.getRingingInfo(this) ?: Triple(0, "uthoSajib", "07:00 AM")
            if (AlarmStateHolder.activeAlarm.value == null) {
                AlarmStateHolder.setRinging(
                    ActiveAlarmData(
                        alarmId = info.first,
                        label = info.second,
                        timeFormatted = info.third,
                        isTestAlarm = false
                    )
                )
            }
            viewModel.startMathChallenge(force = false)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (AlarmStateHolder.isRinging) {
            // Block volume keys and mute from silencing the alarm
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
                keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
                keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAlarmIntent(intent)
    }

    private fun handleAlarmIntent(intent: Intent?) {
        val action = intent?.action
        if (action == AlarmRingingService.ACTION_FULL_SCREEN_ALARM) {
            checkAndRestoreRingingState()
            viewModel.startMathChallenge(force = false)
        }
    }
}

@Composable
private fun NotificationPermissionRequester() {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { _ -> }

        LaunchedEffect(Unit) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(permission)
            }
        }
    }
}
