package com.example.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlin.math.sin

class AlarmAudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var syntheticTrack: AudioTrack? = null
    private var isPlaying = false
    private var currentVolume = 0.4f
    private val handler = Handler(Looper.getMainLooper())
    private var volumeRunnable: Runnable? = null

    fun startPlaying() {
        if (isPlaying) return
        isPlaying = true
        currentVolume = 0.4f

        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                setVolume(currentVolume, currentVolume)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AlarmAudioPlayer", "MediaPlayer failed, falling back to synthetic generator", e)
            startSyntheticAlarmBeep()
        }

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxAlarmVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        try {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxAlarmVol, 0)
        } catch (_: Exception) {}

        // Escalating volume every 2 seconds up to 100% and preventing muting
        volumeRunnable = object : Runnable {
            override fun run() {
                if (!isPlaying) return
                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxAlarmVol, 0)
                } catch (_: Exception) {}
                if (currentVolume < 1.0f) {
                    currentVolume = (currentVolume + 0.2f).coerceAtMost(1.0f)
                    mediaPlayer?.setVolume(currentVolume, currentVolume)
                    syntheticTrack?.setVolume(currentVolume)
                }
                handler.postDelayed(this, 1500)
            }
        }
        handler.postDelayed(volumeRunnable!!, 1500)
    }

    private fun startSyntheticAlarmBeep() {
        Thread {
            try {
                val sampleRate = 44100
                val durationMs = 1200
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val generatedSnd = ShortArray(numSamples)

                // High energetic beep-beep alarm pattern: 900Hz and 1300Hz
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = if ((i / (sampleRate / 4)) % 2 == 0) 950.0 else 1350.0
                    val sample = sin(2.0 * Math.PI * freq * t)
                    generatedSnd[i] = (sample * 30000).toInt().toShort()
                }

                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                syntheticTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize.coerceAtLeast(generatedSnd.size * 2))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                syntheticTrack?.write(generatedSnd, 0, generatedSnd.size)
                syntheticTrack?.setLoopPoints(0, generatedSnd.size, -1)
                syntheticTrack?.play()
            } catch (ex: Exception) {
                Log.e("AlarmAudioPlayer", "Synthetic track failed", ex)
            }
        }.start()
    }

    fun stopPlaying() {
        isPlaying = false
        volumeRunnable?.let { handler.removeCallbacks(it) }
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null

        try {
            syntheticTrack?.let {
                it.stop()
                it.release()
            }
        } catch (_: Exception) {}
        syntheticTrack = null
    }
}
