package com.example.logic

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class SoundEvent {
    BUTTON_TAP,
    WORD_FOUND,
    WRONG_SELECTION,
    LEVEL_COMPLETE,
    COUNTDOWN_TICK,
    DRAG_SWIPE,
    BONUS_WORD,
    GAME_OVER
}

class SoundManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    var isSoundEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    // Pre-rendered PCM buffers for instantaneous playback
    private val sampleRate = 44100
    private val audioBuffers = mutableMapOf<SoundEvent, ShortArray>()

    init {
        scope.launch {
            initBuffers()
        }
    }

    private fun initBuffers() {
        audioBuffers[SoundEvent.BUTTON_TAP] = generateTapSound()
        audioBuffers[SoundEvent.WORD_FOUND] = generateChimeSound()
        audioBuffers[SoundEvent.WRONG_SELECTION] = generateBuzzSound()
        audioBuffers[SoundEvent.LEVEL_COMPLETE] = generateFanfareSound()
        audioBuffers[SoundEvent.COUNTDOWN_TICK] = generateTickSound()
        audioBuffers[SoundEvent.DRAG_SWIPE] = generateSwooshSound()
        audioBuffers[SoundEvent.BONUS_WORD] = generateCoinSparkleSound()
        audioBuffers[SoundEvent.GAME_OVER] = generateGameOverSound()
    }

    fun play(event: SoundEvent) {
        if (!isSoundEnabled) return
        scope.launch {
            val buffer = audioBuffers[event] ?: return@launch
            playPcm(buffer)
        }
        triggerHaptic(event)
    }

    private fun triggerHaptic(event: SoundEvent) {
        try {
            when (event) {
                SoundEvent.BUTTON_TAP -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(20)
                    }
                }
                SoundEvent.WORD_FOUND -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(80)
                    }
                }
                SoundEvent.BONUS_WORD -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(
                            VibrationEffect.createWaveform(longArrayOf(0, 40, 50, 40), intArrayOf(0, 180, 0, 240), -1)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(60)
                    }
                }
                SoundEvent.WRONG_SELECTION -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(
                            VibrationEffect.createWaveform(longArrayOf(0, 50, 60, 50), intArrayOf(0, 150, 0, 150), -1)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(100)
                    }
                }
                SoundEvent.LEVEL_COMPLETE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(
                            VibrationEffect.createWaveform(longArrayOf(0, 100, 80, 150), intArrayOf(0, 200, 0, 255), -1)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(250)
                    }
                }
                else -> {}
            }
        } catch (_: Exception) {}
    }

    private fun playPcm(buffer: ShortArray) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
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
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // Release after playback finishes
            val durationMs = (buffer.size * 1000L) / sampleRate + 50
            Thread.sleep(durationMs.coerceAtMost(3000))
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {}
    }

    // Tone generators
    private fun generateTapSound(): ShortArray {
        val durationMs = 25
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 120.0)
            val sample = sin(2 * PI * 800.0 * t) * decay * 0.4
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    private fun generateChimeSound(): ShortArray {
        // Arpeggio: C5 (523Hz), E5 (659Hz), G5 (784Hz), C6 (1046Hz)
        val noteDuration = 0.08
        val frequencies = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
        val totalDuration = noteDuration * frequencies.size + 0.25
        val numSamples = (sampleRate * totalDuration).toInt()
        val buffer = ShortArray(numSamples)

        for ((idx, freq) in frequencies.withIndex()) {
            val startSample = (idx * noteDuration * sampleRate).toInt()
            val noteSamples = (sampleRate * (noteDuration + 0.25)).toInt()
            for (i in 0 until noteSamples) {
                val targetIdx = startSample + i
                if (targetIdx >= numSamples) break
                val t = i.toDouble() / sampleRate
                val envelope = exp(-t * 8.0)
                val sample = sin(2 * PI * freq * t) * envelope * 0.35
                val current = buffer[targetIdx].toInt()
                val mixed = (current + (sample * Short.MAX_VALUE).toInt()).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                buffer[targetIdx] = mixed.toShort()
            }
        }
        return buffer
    }

    private fun generateBuzzSound(): ShortArray {
        val durationMs = 150
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 12.0)
            // Low sawtooth-ish tone
            val fundamental = sin(2 * PI * 130.0 * t)
            val harmonic = sin(2 * PI * 260.0 * t) * 0.5
            val sample = (fundamental + harmonic) * decay * 0.35
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    private fun generateFanfareSound(): ShortArray {
        // Triumphant Fanfare: C5 (0.1s), E5 (0.1s), G5 (0.1s), C6 (0.35s)
        val notes = listOf(
            Pair(523.25, 0.09),
            Pair(659.25, 0.09),
            Pair(783.99, 0.09),
            Pair(1046.50, 0.40)
        )
        val totalDuration = 0.8
        val numSamples = (sampleRate * totalDuration).toInt()
        val buffer = ShortArray(numSamples)

        var timeOffset = 0.0
        for ((freq, duration) in notes) {
            val startSample = (timeOffset * sampleRate).toInt()
            val noteSamples = (sampleRate * (duration + 0.15)).toInt()
            for (i in 0 until noteSamples) {
                val targetIdx = startSample + i
                if (targetIdx >= numSamples) break
                val t = i.toDouble() / sampleRate
                val decay = exp(-t * 5.0)
                val sample = sin(2 * PI * freq * t) * decay * 0.4
                val current = buffer[targetIdx].toInt()
                val mixed = (current + (sample * Short.MAX_VALUE).toInt()).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                buffer[targetIdx] = mixed.toShort()
            }
            timeOffset += duration
        }
        return buffer
    }

    private fun generateTickSound(): ShortArray {
        val durationMs = 12
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 250.0)
            val sample = sin(2 * PI * 1800.0 * t) * decay * 0.25
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    private fun generateSwooshSound(): ShortArray {
        val durationMs = 40
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val freq = 300.0 + (t / (durationMs / 1000.0)) * 300.0
            val decay = sin(PI * (i.toDouble() / numSamples))
            val sample = sin(2 * PI * freq * t) * decay * 0.15
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    private fun generateCoinSparkleSound(): ShortArray {
        // High sparkle arpeggio: 1318Hz, 1568Hz, 2093Hz
        val freqs = doubleArrayOf(1318.5, 1567.98, 2093.00)
        val noteDur = 0.05
        val totalDuration = 0.35
        val numSamples = (sampleRate * totalDuration).toInt()
        val buffer = ShortArray(numSamples)

        for ((idx, freq) in freqs.withIndex()) {
            val startSample = (idx * noteDur * sampleRate).toInt()
            val noteSamples = (sampleRate * (noteDur + 0.15)).toInt()
            for (i in 0 until noteSamples) {
                val targetIdx = startSample + i
                if (targetIdx >= numSamples) break
                val t = i.toDouble() / sampleRate
                val envelope = exp(-t * 15.0)
                val sample = sin(2 * PI * freq * t) * envelope * 0.3
                val current = buffer[targetIdx].toInt()
                val mixed = (current + (sample * Short.MAX_VALUE).toInt()).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                buffer[targetIdx] = mixed.toShort()
            }
        }
        return buffer
    }

    private fun generateGameOverSound(): ShortArray {
        // Descending tone: 440 -> 370 -> 311 -> 240
        val freqs = doubleArrayOf(440.0, 369.99, 311.13, 246.94)
        val noteDur = 0.12
        val totalDuration = 0.7
        val numSamples = (sampleRate * totalDuration).toInt()
        val buffer = ShortArray(numSamples)

        var timeOffset = 0.0
        for (freq in freqs) {
            val startSample = (timeOffset * sampleRate).toInt()
            val noteSamples = (sampleRate * (noteDur + 0.1)).toInt()
            for (i in 0 until noteSamples) {
                val targetIdx = startSample + i
                if (targetIdx >= numSamples) break
                val t = i.toDouble() / sampleRate
                val decay = exp(-t * 8.0)
                val sample = sin(2 * PI * freq * t) * decay * 0.35
                val current = buffer[targetIdx].toInt()
                val mixed = (current + (sample * Short.MAX_VALUE).toInt()).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                buffer[targetIdx] = mixed.toShort()
            }
            timeOffset += noteDur
        }
        return buffer
    }
}
