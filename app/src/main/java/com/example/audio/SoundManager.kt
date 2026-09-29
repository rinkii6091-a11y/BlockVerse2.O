package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural Audio Engine for BlockFlow.
 * Synthesizes dynamic, juicy SFX and ambient tones using AudioTrack.
 * Zero external audio files required, completely royalty-free and lightweight.
 */
class SoundManager {
    var isSfxEnabled: Boolean = true
    var isMusicEnabled: Boolean = true
    var sfxVolume: Float = 0.85f
    var musicVolume: Float = 0.5f

    private val audioScope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 44100

    /**
     * Plays a short synthesized tone buffer via AudioTrack.
     */
    private fun playToneBuffer(samples: ShortArray) {
        if (!isSfxEnabled || samples.isEmpty()) return
        audioScope.launch {
            try {
                val minSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minSize, samples.size * 2)

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
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.setVolume(sfxVolume)
                audioTrack.play()

                // Release track after playback finished
                val durationMs = (samples.size * 1000L) / sampleRate + 50L
                kotlinx.coroutines.delay(durationMs)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Ignore audio track init failures on constrained emulators
            }
        }
    }

    /**
     * Block pickup sound: subtle upward chirp.
     */
    fun playPickup() {
        val durationMs = 60
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val freq = 420.0 + progress * 240.0
            val envelope = (1.0 - progress) * progress * 4.0
            val sample = sin(2.0 * PI * freq * t) * envelope * Short.MAX_VALUE * 0.4
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }

    /**
     * Block magnetic snap: punchy wooden-crystal pop.
     */
    fun playSnap() {
        val durationMs = 85
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val freq = 580.0 * exp(-progress * 6.0) + 160.0
            val envelope = exp(-progress * 12.0)
            val sample = (sin(2.0 * PI * freq * t) + 0.3 * sin(4.0 * PI * freq * t)) * envelope * Short.MAX_VALUE * 0.7
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }

    /**
     * Invalid placement: subtle soft double-thud.
     */
    fun playInvalid() {
        val durationMs = 120
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val freq = 120.0
            val envelope = exp(-progress * 8.0) * (sin(progress * PI * 4.0).coerceAtLeast(0.0))
            val sample = sin(2.0 * PI * freq * t) * envelope * Short.MAX_VALUE * 0.5
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }

    /**
     * Line clear: sparkling ascending musical chord.
     */
    fun playLineClear(linesCount: Int = 1, combo: Int = 1) {
        val durationMs = 280
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        val baseFreq = 440.0 * (1.0 + (combo.coerceAtMost(8) * 0.12))
        val notes = when (linesCount) {
            1 -> listOf(baseFreq, baseFreq * 1.25, baseFreq * 1.5)
            2 -> listOf(baseFreq, baseFreq * 1.25, baseFreq * 1.5, baseFreq * 1.875)
            3 -> listOf(baseFreq, baseFreq * 1.25, baseFreq * 1.5, baseFreq * 2.0, baseFreq * 2.5)
            else -> listOf(baseFreq, baseFreq * 1.2, baseFreq * 1.5, baseFreq * 1.8, baseFreq * 2.0, baseFreq * 2.6)
        }

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            var mixed = 0.0
            notes.forEachIndexed { idx, freq ->
                val delayOffset = idx * 0.03
                if (t >= delayOffset) {
                    val noteT = t - delayOffset
                    val noteEnv = exp(-noteT * 9.0)
                    mixed += sin(2.0 * PI * freq * noteT) * noteEnv
                }
            }
            val sample = (mixed / notes.size) * Short.MAX_VALUE * 0.75
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }

    /**
     * Combo cheer: heavy futuristic bass hit + laser sweep.
     */
    fun playCombo(comboLevel: Int) {
        val durationMs = 350
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        val pitchShift = (comboLevel * 30.0).coerceAtMost(300.0)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val bassFreq = 95.0 + pitchShift - progress * 40.0
            val bassEnv = exp(-progress * 5.0)
            val subBass = sin(2.0 * PI * bassFreq * t) * bassEnv
            val zapFreq = 1200.0 * exp(-progress * 15.0)
            val zapEnv = exp(-progress * 14.0)
            val zap = sin(2.0 * PI * zapFreq * t) * zapEnv * 0.4
            val sample = (subBass * 0.7 + zap * 0.3) * Short.MAX_VALUE * 0.85
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }

    /**
     * Bomb explosion: deep impact rumble.
     */
    fun playBomb() {
        val durationMs = 400
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        var lastNoise = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val rawNoise = (Math.random() * 2.0 - 1.0)
            // Low pass filter
            lastNoise = lastNoise * 0.85 + rawNoise * 0.15
            val envelope = exp(-progress * 6.0)
            val boomFreq = 80.0 * exp(-progress * 3.0)
            val boom = sin(2.0 * PI * boomFreq * (i.toDouble() / sampleRate)) * envelope
            val sample = (boom * 0.6 + lastNoise * 0.4) * envelope * Short.MAX_VALUE * 0.9
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }

    /**
     * Collectible star/gem collected: sparkling magic chime.
     */
    fun playCollectible() {
        val durationMs = 220
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val freq1 = 1320.0 + progress * 400.0
            val freq2 = 1760.0 + progress * 600.0
            val env = (1.0 - progress) * exp(-progress * 4.0)
            val sample = (sin(2.0 * PI * freq1 * t) * 0.5 + sin(2.0 * PI * freq2 * t) * 0.5) * env * Short.MAX_VALUE * 0.6
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }

    /**
     * Power-up activation sound.
     */
    fun playPowerUp() {
        val durationMs = 250
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val freq = 350.0 + (progress * progress) * 1200.0
            val env = exp(-progress * 3.5)
            val sample = sin(2.0 * PI * freq * t) * env * Short.MAX_VALUE * 0.7
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }

    /**
     * Level up or Victory fanfare.
     */
    fun playLevelUp() {
        val durationMs = 500
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        val triad = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
        val step = numSamples / triad.size

        for (i in 0 until numSamples) {
            val noteIdx = (i / step).coerceAtMost(triad.size - 1)
            val freq = triad[noteIdx]
            val localT = (i % step).toDouble() / sampleRate
            val localProgress = (i % step).toDouble() / step
            val env = exp(-localProgress * 4.0)
            val sample = sin(2.0 * PI * freq * localT) * env * Short.MAX_VALUE * 0.75
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }

    /**
     * Game Over gentle tone.
     */
    fun playGameOver() {
        val durationMs = 600
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        val notes = listOf(440.0, 415.3, 392.0, 349.2)
        val step = numSamples / notes.size

        for (i in 0 until numSamples) {
            val noteIdx = (i / step).coerceAtMost(notes.size - 1)
            val freq = notes[noteIdx]
            val localT = (i % step).toDouble() / sampleRate
            val localProgress = (i % step).toDouble() / step
            val env = exp(-localProgress * 3.0)
            val sample = sin(2.0 * PI * freq * localT) * env * Short.MAX_VALUE * 0.65
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }
}
