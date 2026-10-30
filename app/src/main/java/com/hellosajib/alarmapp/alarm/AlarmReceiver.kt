package com.hellosajib.alarmapp.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hellosajib.alarmapp.MainActivity
import com.hellosajib.alarmapp.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d("AlarmReceiver", "Received action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val activeAlarms = db.alarmDao().getActiveAlarms().first()
                    val scheduler = AlarmScheduler(context)
                    for (alarm in activeAlarms) {
                        scheduler.scheduleAlarm(alarm)
                    }
                    Log.d("AlarmReceiver", "Rescheduled ${activeAlarms.size} alarms on boot")
                } catch (e: Exception) {
                    Log.e("AlarmReceiver", "Failed to reschedule on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        if (action == ACTION_WATCHDOG_CHECK) {
            if (AlarmLockManager.isRinging(context)) {
                val info = AlarmLockManager.getRingingInfo(context) ?: Triple(0, "uthoSajib", "07:00 AM")
                AlarmRingingService.start(context, info.first, info.second, info.third, false)

                if (!AlarmLockManager.isUiForeground) {
                    val activityIntent = Intent(context, MainActivity::class.java).apply {
                        this.action = AlarmRingingService.ACTION_FULL_SCREEN_ALARM
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra(AlarmRingingService.EXTRA_ALARM_ID, info.first)
                        putExtra(AlarmRingingService.EXTRA_ALARM_LABEL, info.second)
                        putExtra(AlarmRingingService.EXTRA_ALARM_TIME, info.third)
                    }
                    context.startActivity(activityIntent)
                }

                // Keep watchdog alive while ringing
                AlarmLockManager.scheduleWatchdog(context)
            }
            return
        }

        if (action == ACTION_TRIGGER_ALARM) {
            val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, 0)
            val label = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: "uthoSajib"
            val hour = intent.getIntExtra(EXTRA_ALARM_HOUR, 7)
            val minute = intent.getIntExtra(EXTRA_ALARM_MINUTE, 0)

            val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            val m = String.format("%02d", minute)
            val amPm = if (hour >= 12) "PM" else "AM"
            val formattedTime = String.format("%02d:%s %s", h, m, amPm)

            // Mark ringing file lock & schedule anti-kill watchdog
            AlarmLockManager.markRinging(context, alarmId, label, formattedTime)

            // Start foreground ringing service
            AlarmRingingService.start(
                context = context,
                alarmId = alarmId,
                label = label,
                timeFormatted = formattedTime,
                isTest = false
            )

            // Launch MainActivity into full-screen ringing UI
            val activityIntent = Intent(context, MainActivity::class.java).apply {
                this.action = AlarmRingingService.ACTION_FULL_SCREEN_ALARM
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(AlarmRingingService.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmRingingService.EXTRA_ALARM_LABEL, label)
                putExtra(AlarmRingingService.EXTRA_ALARM_TIME, formattedTime)
            }
            context.startActivity(activityIntent)
        }
    }

    companion object {
        const val ACTION_TRIGGER_ALARM = "com.hellosajib.alarmapp.ACTION_TRIGGER_ALARM"
        const val ACTION_WATCHDOG_CHECK = "com.hellosajib.alarmapp.ACTION_WATCHDOG_CHECK"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_ALARM_HOUR = "extra_alarm_hour"
        const val EXTRA_ALARM_MINUTE = "extra_alarm_minute"

        fun resurrectAlarm(context: Context) {
            if (!AlarmLockManager.isRinging(context)) return

            val info = AlarmLockManager.getRingingInfo(context) ?: Triple(0, "uthoSajib Alarm", "07:00 AM")
            AlarmRingingService.start(context, info.first, info.second, info.third, false)

            val activityIntent = Intent(context, MainActivity::class.java).apply {
                this.action = AlarmRingingService.ACTION_FULL_SCREEN_ALARM
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(AlarmRingingService.EXTRA_ALARM_ID, info.first)
                putExtra(AlarmRingingService.EXTRA_ALARM_LABEL, info.second)
                putExtra(AlarmRingingService.EXTRA_ALARM_TIME, info.third)
            }
            context.startActivity(activityIntent)
            AlarmLockManager.scheduleWatchdog(context)
        }
    }
}
