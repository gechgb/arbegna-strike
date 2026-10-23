package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Robust, low-latency audio synthesizer for Arbegna Strike.
 * Generates Ethiopian militia action sound effects procedurally and plays them
 * via Android's native SoundPool (with bounded streams) to eliminate native AudioTrack
 * exhaustion crashes (SIGABRT).
 */
class GameAudio(private val context: Context? = null) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 22050
    private val soundBuffers = ConcurrentHashMap<String, ShortArray>()
    private val soundIds = ConcurrentHashMap<String, Int>()
    private val lastPlayedMs = ConcurrentHashMap<String, Long>()
    private var soundPool: SoundPool? = null
    var isMuted = false

    init {
        try {
            soundPool = SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .build()
        } catch (e: Throwable) {
            Log.e("GameAudio", "SoundPool initialization error", e)
        }

        scope.launch(Dispatchers.IO) {
            try {
                generateAllSounds()
                loadSoundsIntoPool()
            } catch (e: Throwable) {
                Log.e("GameAudio", "Sound generation error", e)
            }
        }
    }

    private fun generateAllSounds() {
        soundBuffers["arrow"] = generateArrowSound()
        soundBuffers["spear"] = generateSpearSound()
        soundBuffers["musket"] = generateMusketSound()
        soundBuffers["shotgun"] = generateShotgunSound()
        soundBuffers["rifle"] = generateRifleSound()
        soundBuffers["sniper"] = generateSniperSound()
        soundBuffers["rocket_launch"] = generateRocketLaunchSound()
        soundBuffers["explosion"] = generateExplosionSound()
        soundBuffers["plasma"] = generatePlasmaSound()
        soundBuffers["slash"] = generateSlashSound()
        soundBuffers["hit"] = generateHitSound()
        soundBuffers["jetpack"] = generateJetpackBurst()
        soundBuffers["pickup"] = generatePickupSound()
        soundBuffers["fanfare"] = generateVictoryFanfare()
    }

    private fun loadSoundsIntoPool() {
        val ctx = context ?: return
        val sp = soundPool ?: return
        val cacheDir = File(ctx.cacheDir, "arbegna_audio")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        soundBuffers.forEach { (name, buffer) ->
            try {
                val wavFile = File(cacheDir, "$name.wav")
                writeWavFile(wavFile, buffer, sampleRate)
                val sId = sp.load(wavFile.absolutePath, 1)
                if (sId != 0) {
                    soundIds[name] = sId
                }
            } catch (e: Throwable) {
                Log.e("GameAudio", "Failed to cache and load sound: $name", e)
            }
        }
    }

    fun playSound(name: String) {
        if (isMuted) return
        val now = System.currentTimeMillis()
        val last = lastPlayedMs[name] ?: 0L
        // Cooldown between duplicate sounds (prevents audio clipping and AudioFlinger overload)
        val cooldown = when (name) {
            "jetpack" -> 80L
            "hit" -> 50L
            "slash" -> 60L
            else -> 40L
        }
        if (now - last < cooldown) return
        lastPlayedMs[name] = now

        try {
            val sp = soundPool ?: return
            val sId = soundIds[name] ?: return
            val volume = when (name) {
                "jetpack" -> 0.45f
                "explosion" -> 1.0f
                "fanfare" -> 0.9f
                else -> 0.75f
            }
            sp.play(sId, volume, volume, 1, 0, 1.0f)
        } catch (_: Throwable) {
            // Audio error must never crash the game
        }
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            soundIds.clear()
            soundBuffers.clear()
        } catch (_: Throwable) {}
    }

    private fun writeWavFile(file: File, pcmData: ShortArray, sampleRate: Int) {
        val totalAudioLen = pcmData.size * 2
        val totalDataLen = totalAudioLen + 36
        val channels = 1
        val byteRate = sampleRate * channels * 2

        FileOutputStream(file).use { out ->
            val header = ByteArray(44)
            // RIFF chunk descriptor
            header[0] = 'R'.code.toByte()
            header[1] = 'I'.code.toByte()
            header[2] = 'F'.code.toByte()
            header[3] = 'F'.code.toByte()
            header[4] = (totalDataLen and 0xff).toByte()
            header[5] = ((totalDataLen shr 8) and 0xff).toByte()
            header[6] = ((totalDataLen shr 16) and 0xff).toByte()
            header[7] = ((totalDataLen shr 24) and 0xff).toByte()
            header[8] = 'W'.code.toByte()
            header[9] = 'A'.code.toByte()
            header[10] = 'V'.code.toByte()
            header[11] = 'E'.code.toByte()

            // fmt sub-chunk
            header[12] = 'f'.code.toByte()
            header[13] = 'm'.code.toByte()
            header[14] = 't'.code.toByte()
            header[15] = ' '.code.toByte()
            header[16] = 16 // 16 for PCM
            header[17] = 0
            header[18] = 0
            header[19] = 0
            header[20] = 1 // Audio format: 1 (PCM)
            header[21] = 0
            header[22] = channels.toByte()
            header[23] = 0
            header[24] = (sampleRate and 0xff).toByte()
            header[25] = ((sampleRate shr 8) and 0xff).toByte()
            header[26] = ((sampleRate shr 16) and 0xff).toByte()
            header[27] = ((sampleRate shr 24) and 0xff).toByte()
            header[28] = (byteRate and 0xff).toByte()
            header[29] = ((byteRate shr 8) and 0xff).toByte()
            header[30] = ((byteRate shr 16) and 0xff).toByte()
            header[31] = ((byteRate shr 24) and 0xff).toByte()
            header[32] = (channels * 2).toByte() // Block align
            header[33] = 0
            header[34] = 16 // Bits per sample
            header[35] = 0

            // data sub-chunk
            header[36] = 'd'.code.toByte()
            header[37] = 'a'.code.toByte()
            header[38] = 't'.code.toByte()
            header[39] = 'a'.code.toByte()
            header[40] = (totalAudioLen and 0xff).toByte()
            header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
            header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
            header[43] = ((totalAudioLen shr 24) and 0xff).toByte()
            out.write(header, 0, 44)

            val byteBuffer = ByteBuffer.allocate(totalAudioLen).order(ByteOrder.LITTLE_ENDIAN)
            for (sample in pcmData) {
                byteBuffer.putShort(sample)
            }
            out.write(byteBuffer.array())
        }
    }

    // Procedural sound generators:
    private fun generateArrowSound(): ShortArray {
        val samples = (sampleRate * 0.18).toInt()
        val buffer = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val freq = 400.0 + 800.0 * (1.0 - t / 0.18)
            val sinVal = sin(2.0 * PI * freq * t)
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.3
            val env = exp(-t * 22.0)
            val v = ((sinVal * 0.7 + noise) * env * Short.MAX_VALUE * 0.8).toInt()
            buffer[i] = v.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateSpearSound(): ShortArray {
        val samples = (sampleRate * 0.22).toInt()
        val buffer = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val freq = 180.0 - 60.0 * (t / 0.22)
            val sinVal = sin(2.0 * PI * freq * t)
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.4
            val env = exp(-t * 14.0)
            val v = ((sinVal * 0.6 + noise) * env * Short.MAX_VALUE * 0.7).toInt()
            buffer[i] = v.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateMusketSound(): ShortArray {
        val samples = (sampleRate * 0.35).toInt()
        val buffer = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val noise = Random.nextDouble() * 2.0 - 1.0
            val sub = sin(2.0 * PI * 70.0 * t) * 0.5
            val env = exp(-t * 11.0)
            val v = ((noise * 0.8 + sub) * env * Short.MAX_VALUE * 0.9).toInt()
            buffer[i] = v.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateShotgunSound(): ShortArray {
        val samples = (sampleRate * 0.28).toInt()
        val buffer = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val noise = Random.nextDouble() * 2.0 - 1.0
            val sub = sin(2.0 * PI * 85.0 * t) * 0.6
            val env = exp(-t * 15.0)
            val v = ((noise * 0.85 + sub) * env * Short.MAX_VALUE * 0.95).toInt()
            buffer[i] = v.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateRifleSound(): ShortArray {
        val samples = (sampleRate * 0.15).toInt()
        val buffer = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val noise = Random.nextDouble() * 2.0 - 1.0
            val click = sin(2.0 * PI * 350.0 * t) * 0.4
            val env = exp(-t * 28.0)
            val v = ((noise * 0.8 + click) * env * Short.MAX_VALUE * 0.85).toInt()
            buffer[i] = v.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateSniperSound(): ShortArray {
        val samples = (sampleRate * 0.32).toInt()
        val buffer = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val noise = Random.nextDouble() * 2.0 - 1.0
            val sub = sin(2.0 * PI * 90.0 * t) * 0.7
            val env = exp(-t * 12.0)
            val v = ((noise * 0.75 + sub) * env * Short.MAX_VALUE * 0.95).toInt()
            buffer[i] = v.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateRocketLaunchSound(): ShortArray {
        val samples = (sampleRate * 0.3).toInt()
        val buffer = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val freq = 200.0 + 600.0 * (t / 0.3)
            val tone = sin(2.0 * PI * freq * t)
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.5
            val env = exp(-t * 6.0)
            val v = ((tone * 0.5 + noise) * env * Short.MAX_VALUE * 0.7).toInt()
            buffer[i] = v.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateExplosionSound(): ShortArray {
        val samples = (sampleRate * 0.55).toInt()
        val buffer = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val noise = Random.nextDouble() * 2.0 - 1.0
            val sub = sin(2.0 * PI * 48.0 * t) * 0.8
            val env = exp(-t * 7.0)
            val v = ((noise * 0.7 + sub) * env * Short.MAX_VALUE * 0.95).toInt()
            buffer[i] = v.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generatePlasmaSound(): ShortArray {
        val samples = (sampleRate * 0.22).toInt()
        val buffer = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val freq = 1200.0 - 800.0 * (t / 0.22)
            val tone = sin(2.0 * PI * freq * t)
            val env = exp(-t * 18.0)
            val v = (tone * env * Short.MAX_VALUE * 0.8).toInt()
            buffer[i] = v.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateSlashSound(): ShortArray {
        val samples = (sampleRate * 0.16).toInt()
        val buffer = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val freq = 800.0 - 400.0 * (t / 0.16)
            val tone = sin(2.0 * PI * freq * t)
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.3
            val env = exp(-t * 25.0)
            val v = ((tone * 0.7 + noise) * env * Short.MAX_VALUE * 0.75).toInt()
            buffer[i] = v.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateHitSound(): ShortArray {
        val samples = (sampleRate * 0.12).toInt()
        val buffer = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val sub = sin(2.0 * PI * 95.0 * t)
            val env = exp(-t * 30.0)
            val v = (sub * env * Short.MAX_VALUE * 0.8).toInt()
            buffer[i] = v.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateJetpackBurst(): ShortArray {
        val samples = (sampleRate * 0.15).toInt()
        val buffer = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val noise = Random.nextDouble() * 2.0 - 1.0
            val env = (1.0 - t / 0.15) * 0.5
            val v = (noise * env * Short.MAX_VALUE * 0.5).toInt()
            buffer[i] = v.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generatePickupSound(): ShortArray {
        val samples = (sampleRate * 0.25).toInt()
        val buffer = ShortArray(samples)
        val notes = doubleArrayOf(330.0, 392.0, 493.88, 587.33)
        val step = samples / notes.size
        for (i in 0 until samples) {
            val noteIdx = (i / step).coerceAtMost(notes.size - 1)
            val t = (i % step).toDouble() / sampleRate
            val tone = sin(2.0 * PI * notes[noteIdx] * t)
            val env = exp(-t * 12.0)
            val v = (tone * env * Short.MAX_VALUE * 0.6).toInt()
            buffer[i] = v.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateVictoryFanfare(): ShortArray {
        val samples = (sampleRate * 0.8).toInt()
        val buffer = ShortArray(samples)
        val notes = doubleArrayOf(261.63, 329.63, 392.0, 523.25)
        val step = samples / notes.size
        for (i in 0 until samples) {
            val noteIdx = (i / step).coerceAtMost(notes.size - 1)
            val t = (i % step).toDouble() / sampleRate
            val tone = sin(2.0 * PI * notes[noteIdx] * t) + 0.3 * sin(2.0 * PI * notes[noteIdx] * 2.0 * t)
            val env = exp(-t * 4.0)
            val v = (tone * env * Short.MAX_VALUE * 0.7).toInt()
            buffer[i] = v.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }
}
