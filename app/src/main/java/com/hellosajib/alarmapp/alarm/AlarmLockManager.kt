package com.hellosajib.alarmapp.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import java.io.File

object AlarmLockManager {
    private const val FLAG_FILE = "utho_sajib_ringing.lock"

    @Volatile
    var isUiForeground: Boolean = false

    fun markRinging(context: Context, alarmId: Int, label: String, time: String) {
        try {
            val file = File(context.filesDir, FLAG_FILE)
            file.writeText("$alarmId\n$label\n$time")
        } catch (e: Exception) {
            Log.e("AlarmLockManager", "Failed to write lock file", e)
        }
        scheduleWatchdog(context)
    }

    fun clearRinging(context: Context) {
        try {
            val file = File(context.filesDir, FLAG_FILE)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.e("AlarmLockManager", "Failed to delete lock file", e)
        }
        cancelWatchdog(context)
    }

    fun isRinging(context: Context): Boolean {
        return try {
            File(context.filesDir, FLAG_FILE).exists()
        } catch (_: Exception) {
            false
        }
    }

    fun getRingingInfo(context: Context): Triple<Int, String, String>? {
        return try {
            val file = File(context.filesDir, FLAG_FILE)
            if (!file.exists()) return null
            val lines = file.readLines()
            val id = lines.getOrNull(0)?.toIntOrNull() ?: 0
            val label = lines.getOrNull(1) ?: "HeyUtho Alarm"
            val time = lines.getOrNull(2) ?: "07:00 AM"
            Triple(id, label, time)
        } catch (e: Exception) {
            Log.e("AlarmLockManager", "Failed to read lock file", e)
            null
        }
    }

    fun scheduleWatchdog(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_WATCHDOG_CHECK
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            8888,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Fire after 3500ms to resurrect if app is closed or removed from recents
        val triggerAt = SystemClock.elapsedRealtime() + 3500L
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerAt,
                pendingIntent
            )
        } catch (e: Exception) {
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerAt,
                pendingIntent
            )
        }
    }

    fun cancelWatchdog(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_WATCHDOG_CHECK
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            8888,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
