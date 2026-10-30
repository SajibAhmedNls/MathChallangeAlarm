package com.hellosajib.alarmapp.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.hellosajib.alarmapp.MainActivity
import com.hellosajib.alarmapp.R

class AlarmRingingService : Service() {
    private var audioPlayer: AlarmAudioPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isExplicitStop = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "HeyUtho:AlarmRingingWakeLock"
        ).apply {
            acquire(15 * 60 * 1000L) // 15 minutes max
        }

        audioPlayer = AlarmAudioPlayer(this)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator ?: (getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ACTION_STOP_SERVICE) {
            isExplicitStop = true
            AlarmLockManager.clearRinging(applicationContext)
            stopRinging()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val alarmId = intent?.getIntExtra(EXTRA_ALARM_ID, 0) ?: 0
        val label = intent?.getStringExtra(EXTRA_ALARM_LABEL) ?: "HeyUtho Alarm"
        val timeFormatted = intent?.getStringExtra(EXTRA_ALARM_TIME) ?: "07:00 AM"

        isExplicitStop = false
        AlarmLockManager.markRinging(applicationContext, alarmId, label, timeFormatted)

        AlarmStateHolder.setRinging(
            ActiveAlarmData(
                alarmId = alarmId,
                label = label,
                timeFormatted = timeFormatted,
                isTestAlarm = false
            )
        )

        val notification = buildForegroundNotification(label, timeFormatted)
        startForeground(NOTIFICATION_ID, notification)

        audioPlayer?.startPlaying()
        startVibrating()

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (AlarmLockManager.isRinging(applicationContext) && !isExplicitStop) {
            // App was removed from recents/app switcher while ringing!
            // Relaunch full-screen ringing UI and keep service active!
            AlarmReceiver.resurrectAlarm(applicationContext)
        }
    }

    private fun startVibrating() {
        val pattern = longArrayOf(0, 500, 200, 500, 200, 800, 300)
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, 0)
                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                v.vibrate(effect, attributes)
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(pattern, 0)
            }
        }
    }

    private fun stopRinging() {
        audioPlayer?.stopPlaying()
        try {
            vibrator?.cancel()
        } catch (_: Exception) {}
        AlarmStateHolder.clearRinging()
    }

    private fun buildForegroundNotification(label: String, timeFormatted: String): Notification {
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_FULL_SCREEN_ALARM
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_ALARM_LABEL, label)
            putExtra(EXTRA_ALARM_TIME, timeFormatted)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            1001,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("HeyUtho: $label")
            .setContentText("Solve 3 math problems to turn off the alarm ($timeFormatted)")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "HeyUtho Wake-up Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority alarms with math challenge"
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        if (!isExplicitStop && AlarmLockManager.isRinging(applicationContext)) {
            AlarmReceiver.resurrectAlarm(applicationContext)
        } else {
            stopRinging()
        }
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "uthosajib_alarm_channel"
        const val NOTIFICATION_ID = 9001
        const val ACTION_START_ALARM = "com.hellosajib.alarmapp.ACTION_START_ALARM"
        const val ACTION_STOP_SERVICE = "com.hellosajib.alarmapp.ACTION_STOP_SERVICE"
        const val ACTION_FULL_SCREEN_ALARM = "com.hellosajib.alarmapp.ACTION_FULL_SCREEN_ALARM"

        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_ALARM_TIME = "extra_alarm_time"

        fun start(context: Context, alarmId: Int, label: String, timeFormatted: String, isTest: Boolean = false) {
            val intent = Intent(context, AlarmRingingService::class.java).apply {
                action = ACTION_START_ALARM
                putExtra(EXTRA_ALARM_ID, alarmId)
                putExtra(EXTRA_ALARM_LABEL, label)
                putExtra(EXTRA_ALARM_TIME, timeFormatted)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AlarmRingingService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }
}
