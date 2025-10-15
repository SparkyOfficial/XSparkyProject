/**
 * фреймворк для обробки аудіо
 *
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.io.*
import javax.sound.sampled.*
import kotlin.math.*

/**
 * представлення інтерфейсу для роботи з аудіо даними
 */
interface AudioData {
    /**
     * отримати частоту дискретизації
     *
     * @return частота дискретизації
     */
    fun getSampleRate(): Float
    
    /**
     * отримати кількість каналів
     *
     * @return кількість каналів
     */
    fun getChannels(): Int
    
    /**
     * отримати розрядність
     *
     * @return розрядність
     */
    fun getSampleSizeInBits(): Int
    
    /**
     * отримати кількість фреймів
     *
     * @return кількість фреймів
     */
    fun getFrameCount(): Long
    
    /**
     * отримати аудіо фрейм
     *
     * @param frameIndex індекс фрейму
     * @return фрейм
     */
    fun getFrame(frameIndex: Long): AudioFrame
    
    /**
     * встановити аудіо фрейм
     *
     * @param frameIndex індекс фрейму
     * @param frame фрейм
     */
    fun setFrame(frameIndex: Long, frame: AudioFrame)
    
    /**
     * отримати всі фрейми
     *
     * @return масив фреймів
     */
    fun getFrames(): Array<AudioFrame>
    
    /**
     * встановити всі фрейми
     *
     * @param frames масив фреймів
     */
    fun setFrames(frames: Array<AudioFrame>)
    
    /**
     * зберегти аудіо у файл
     *
     * @param file файл
     * @param format формат
     */
    fun save(file: File, format: AudioFileFormat.Type = AudioFileFormat.Type.WAVE)
    
    /**
     * створити копію аудіо даних
     *
     * @return копія
     */
    fun copy(): AudioData
}

/**
 * представлення аудіо фрейму
 *
 * @property samples семпли для кожного каналу
 */
data class AudioFrame(val samples: DoubleArray) {
    operator fun get(channel: Int): Double = samples[channel]
    operator fun set(channel: Int, sample: Double) { samples[channel] = sample }
    fun size(): Int = samples.size
}

/**
 * представлення базової реалізації аудіо даних
 */
open class BaseAudioData(
    private val sampleRate: Float,
    private val channels: Int,
    private val sampleSizeInBits: Int,
    private val frames: Array<AudioFrame>
) : AudioData {
    
    constructor(sampleRate: Float, channels: Int, sampleSizeInBits: Int, frameCount: Long) : 
        this(sampleRate, channels, sampleSizeInBits, Array(frameCount.toInt()) { AudioFrame(DoubleArray(channels)) })
    
    override fun getSampleRate(): Float = sampleRate
    
    override fun getChannels(): Int = channels
    
    override fun getSampleSizeInBits(): Int = sampleSizeInBits
    
    override fun getFrameCount(): Long = frames.size.toLong()
    
    override fun getFrame(frameIndex: Long): AudioFrame {
        require(frameIndex >= 0 && frameIndex < getFrameCount()) { "Невірний індекс фрейму: $frameIndex" }
        return frames[frameIndex.toInt()]
    }
    
    override fun setFrame(frameIndex: Long, frame: AudioFrame) {
        require(frameIndex >= 0 && frameIndex < getFrameCount()) { "Невірний індекс фрейму: $frameIndex" }
        require(frame.size() == channels) { "Невірна кількість каналів у фреймі" }
        frames[frameIndex.toInt()] = frame
    }
    
    override fun getFrames(): Array<AudioFrame> = frames.copyOf()
    
    override fun setFrames(frames: Array<AudioFrame>) {
        require(frames.all { it.size() == channels }) { "Невірна кількість каналів у фреймах" }
        this.frames.forEachIndexed { index, _ ->
            if (index < frames.size) {
                this.frames[index] = frames[index]
            }
        }
    }
    
    override fun save(file: File, format: AudioFileFormat.Type) {
        val audioFormat = AudioFormat(sampleRate, sampleSizeInBits, channels, true, false)
        val audioInputStream = AudioInputStream(object : InputStream() {
            private var position = 0
            private var frameIndex = 0
            private var sampleIndex = 0
            
            override fun read(): Int {
                if (frameIndex >= frames.size) return -1
                
                val frame = frames[frameIndex]
                val sample = frame[sampleIndex]
                val byteValue = (sample * (if (sampleSizeInBits == 8) 127 else 32767)).toInt()
                
                val result = when (sampleIndex) {
                    0 -> byteValue and 0xFF
                    else -> (byteValue shr 8) and 0xFF
                }
                
                sampleIndex++
                if (sampleIndex >= channels) {
                    sampleIndex = 0
                    frameIndex++
                }
                
                return result
            }
        }, audioFormat, getFrameCount())
        
        AudioSystem.write(audioInputStream, format, file)
    }
    
    override fun copy(): AudioData {
        val copiedFrames = Array(frames.size) { frames[it].copy() }
        return BaseAudioData(sampleRate, channels, sampleSizeInBits, copiedFrames)
    }
    
    /**
     * отримати тривалість аудіо в секундах
     *
     * @return тривалість
     */
    fun getDuration(): Double {
        return getFrameCount().toDouble() / sampleRate.toDouble()
    }
    
    /**
     * застосувати аудіо ефект
     *
     * @param effect ефект
     * @return нові аудіо дані
     */
    fun applyEffect(effect: AudioEffect): AudioData {
        return effect.apply(this)
    }
    
    /**
     * об'єднати з іншими аудіо даними
     *
     * @param other інші аудіо дані
     * @return об'єднані аудіо дані
     */
    fun mixWith(other: AudioData): AudioData {
        require(sampleRate == other.getSampleRate()) { "Невідповідна частота дискретизації" }
        require(channels == other.getChannels()) { "Невідповідна кількість каналів" }
        
        val maxFrames = maxOf(getFrameCount(), other.getFrameCount())
        val mixedFrames = Array(maxFrames.toInt()) { frameIndex ->
            val frame1 = if (frameIndex < getFrameCount()) getFrame(frameIndex) else AudioFrame(DoubleArray(channels))
            val frame2 = if (frameIndex < other.getFrameCount()) other.getFrame(frameIndex) else AudioFrame(DoubleArray(channels))
            
            AudioFrame(DoubleArray(channels) { channel ->
                (frame1[channel] + frame2[channel]) / 2.0
            })
        }
        
        return BaseAudioData(sampleRate, channels, sampleSizeInBits, mixedFrames)
    }
    
    /**
     * обрізати аудіо
     *
     * @param startFrame початковий фрейм
     * @param endFrame кінцевий фрейм
     * @return обрізані аудіо дані
     */
    fun crop(startFrame: Long, endFrame: Long): AudioData {
        require(startFrame >= 0 && endFrame <= getFrameCount() && startFrame <= endFrame) { 
            "Невірні межі обрізки" 
        }
        
        val croppedFrames = Array((endFrame - startFrame).toInt()) { index ->
            getFrame(startFrame + index).copy()
        }
        
        return BaseAudioData(sampleRate, channels, sampleSizeInBits, croppedFrames)
    }
    
    /**
     * змінити швидкість відтворення
     *
     * @param factor фактор швидкості
     * @return нові аудіо дані
     */
    fun changeSpeed(factor: Double): AudioData {
        require(factor > 0) { "Фактор швидкості має бути більше 0" }
        
        val newFrameCount = (getFrameCount() / factor).toLong()
        val newFrames = Array(newFrameCount.toInt()) { frameIndex ->
            val originalFrameIndex = (frameIndex * factor).toLong()
            if (originalFrameIndex < getFrameCount()) {
                getFrame(originalFrameIndex).copy()
            } else {
                AudioFrame(DoubleArray(channels))
            }
        }
        
        return BaseAudioData(sampleRate, channels, sampleSizeInBits, newFrames)
    }
}

/**
 * представлення інтерфейсу для аудіо ефектів
 */
interface AudioEffect {
    /**
     * застосувати ефект до аудіо даних
     *
     * @param audioData аудіо дані
     * @return нові аудіо дані
     */
    fun apply(audioData: AudioData): AudioData
}

/**
 * представлення ефекту підсилення
 *
 * @property gain підсилення
 */
class GainEffect(private val gain: Double) : AudioEffect {
    
    override fun apply(audioData: AudioData): AudioData {
        val result = audioData.copy()
        
        for (frameIndex in 0 until result.getFrameCount()) {
            val frame = result.getFrame(frameIndex)
            for (channel in 0 until frame.size()) {
                val amplifiedSample = frame[channel] * gain
                frame[channel] = amplifiedSample.coerceIn(-1.0, 1.0)
            }
        }
        
        return result
    }
}

/**
 * представлення ефекту затухання
 *
 * @property decay коефіцієнт затухання
 */
class DecayEffect(private val decay: Double) : AudioEffect {
    
    override fun apply(audioData: AudioData): AudioData {
        require(decay >= 0 && decay <= 1) { "Коефіцієнт затухання має бути в діапазоні [0, 1]" }
        
        val result = audioData.copy()
        val frameCount = result.getFrameCount()
        
        for (frameIndex in 0 until frameCount) {
            val frame = result.getFrame(frameIndex)
            val factor = 1.0 - (frameIndex.toDouble() / frameCount.toDouble()) * (1.0 - decay)
            
            for (channel in 0 until frame.size()) {
                frame[channel] = frame[channel] * factor
            }
        }
        
        return result
    }
}

/**
 * представлення ефекту ревербератора
 *
 * @property roomSize розмір кімнати
 * @property damping затухання
 * @property wetLevel рівень ефекту
 */
class ReverbEffect(
    private val roomSize: Double = 0.5,
    private val damping: Double = 0.5,
    private val wetLevel: Double = 0.33
) : AudioEffect {
    
    override fun apply(audioData: AudioData): AudioData {
        val result = audioData.copy()
        val channels = audioData.getChannels()
        val frameCount = audioData.getFrameCount()
        
        // Проста реалізація реверберації з використанням затримок
        val delayBuffer = Array(channels) { DoubleArray(44100) } // 1 секунда затримки при 44.1 кГц
        val delayIndices = IntArray(channels)
        
        for (frameIndex in 0 until frameCount) {
            val frame = result.getFrame(frameIndex)
            
            for (channel in 0 until channels) {
                val inputSample = frame[channel]
                val delayIndex = delayIndices[channel]
                val delayedSample = delayBuffer[channel][delayIndex]
                
                // Комбінуємо вхідний сигнал з затриманим
                val outputSample = inputSample + delayedSample * wetLevel
                frame[channel] = outputSample
                
                // Зберігаємо затриманий сигнал з затуханням
                delayBuffer[channel][delayIndex] = (inputSample + delayedSample * damping) * roomSize
                
                // Оновлюємо індекс затримки
                delayIndices[channel] = (delayIndex + 1) % delayBuffer[channel].size
            }
        }
        
        return result
    }
}

/**
 * представлення ефекту еквалайзера
 *
 * @property bands смуги еквалайзера
 */
class EqualizerEffect(private val bands: Map<Int, Double>) : AudioEffect {
    
    override fun apply(audioData: AudioData): AudioData {
        val result = audioData.copy()
        val sampleRate = audioData.getSampleRate().toInt()
        
        // Застосовуємо простий еквалайзер з використанням фільтрів нижніх і верхніх частот
        bands.forEach { (frequency, gain) ->
            applyFilter(result, frequency, gain, sampleRate)
        }
        
        return result
    }
    
    private fun applyFilter(audioData: AudioData, frequency: Int, gain: Double, sampleRate: Int) {
        val channels = audioData.getChannels()
        val frameCount = audioData.getFrameCount()
        val rc = 1.0 / (2 * PI * frequency)
        val dt = 1.0 / sampleRate
        val alpha = dt / (rc + dt)
        
        for (channel in 0 until channels) {
            var prevSample = 0.0
            
            for (frameIndex in 0 until frameCount) {
                val frame = audioData.getFrame(frameIndex)
                val sample = frame[channel]
                val filteredSample = prevSample + alpha * (sample - prevSample)
                frame[channel] = filteredSample * gain
                prevSample = filteredSample
            }
        }
    }
}

/**
 * представлення ефекту компресора
 *
 * @property threshold поріг
 * @property ratio співвідношення
 * @property attack атака
 * @property release відпустка
 */
class CompressorEffect(
    private val threshold: Double = -20.0,
    private val ratio: Double = 4.0,
    private val attack: Double = 0.001,
    private val release: Double = 0.1
) : AudioEffect {
    
    override fun apply(audioData: AudioData): AudioData {
        val result = audioData.copy()
        val channels = audioData.getChannels()
        val frameCount = audioData.getFrameCount()
        
        val thresholdLinear = 10.0.pow(threshold / 20.0)
        val attackCoef = exp(-1.0 / (attack * audioData.getSampleRate()))
        val releaseCoef = exp(-1.0 / (release * audioData.getSampleRate()))
        
        val envelope = DoubleArray(channels)
        
        for (frameIndex in 0 until frameCount) {
            val frame = result.getFrame(frameIndex)
            
            for (channel in 0 until channels) {
                val sample = abs(frame[channel])
                
                // Відстежуємо огинаючу
                envelope[channel] = if (sample > envelope[channel]) {
                    attackCoef * envelope[channel] + (1 - attackCoef) * sample
                } else {
                    releaseCoef * envelope[channel] + (1 - releaseCoef) * sample
                }
                
                // Застосовуємо компресію
                if (envelope[channel] > thresholdLinear) {
                    val gainReduction = thresholdLinear + (envelope[channel] - thresholdLinear) / ratio
                    val gain = gainReduction / envelope[channel]
                    frame[channel] = frame[channel] * gain
                }
            }
        }
        
        return result
    }
}

/**
 * представлення ефекту фланжера
 *
 * @property depth глибина
 * @property rate частота
 * @property feedback зворотній зв'язок
 */
class FlangerEffect(
    private val depth: Double = 0.002,
    private val rate: Double = 0.5,
    private val feedback: Double = 0.7
) : AudioEffect {
    
    override fun apply(audioData: AudioData): AudioData {
        val result = audioData.copy()
        val channels = audioData.getChannels()
        val frameCount = audioData.getFrameCount()
        val sampleRate = audioData.getSampleRate()
        
        val delayBuffer = Array(channels) { DoubleArray((sampleRate * 0.01).toInt()) } // 10 мс буфер
        val delayIndices = IntArray(channels)
        val lfoPhase = DoubleArray(channels)
        
        for (frameIndex in 0 until frameCount) {
            val frame = result.getFrame(frameIndex)
            
            for (channel in 0 until channels) {
                val inputSample = frame[channel]
                val delayIndex = delayIndices[channel]
                
                // Генеруємо LFO (низькочастотна осциляція)
                val lfo = sin(2 * PI * rate * frameIndex / sampleRate + lfoPhase[channel])
                lfoPhase[channel] += 2 * PI * rate / sampleRate
                if (lfoPhase[channel] >= 2 * PI) lfoPhase[channel] -= 2 * PI
                
                // Обчислюємо затримку
                val delaySamples = (delayBuffer[channel].size * depth * (1 + lfo) / 2).toInt()
                val delayedIndex = (delayIndex - delaySamples + delayBuffer[channel].size) % delayBuffer[channel].size
                val delayedSample = delayBuffer[channel][delayedIndex]
                
                // Вихідний сигнал
                val outputSample = inputSample + delayedSample * 0.5
                frame[channel] = outputSample
                
                // Зберігаємо в буфер з зворотнім зв'язком
                delayBuffer[channel][delayIndex] = inputSample + delayedSample * feedback
                delayIndices[channel] = (delayIndex + 1) % delayBuffer[channel].size
            }
        }
        
        return result
    }
}

/**
 * представлення ефекту хоруса
 *
 * @property depth глибина
 * @property rate частота
 * @property voices кількість голосів
 */
class ChorusEffect(
    private val depth: Double = 0.002,
    private val rate: Double = 1.0,
    private val voices: Int = 2
) : AudioEffect {
    
    override fun apply(audioData: AudioData): AudioData {
        val result = audioData.copy()
        val channels = audioData.getChannels()
        val frameCount = audioData.getFrameCount()
        val sampleRate = audioData.getSampleRate()
        
        val delayBuffers = Array(voices) { 
            Array(channels) { DoubleArray((sampleRate * 0.05).toInt()) } // 50 мс буфер
        }
        val delayIndices = Array(voices) { IntArray(channels) }
        val lfoPhases = Array(voices) { DoubleArray(channels) }
        
        for (frameIndex in 0 until frameCount) {
            val frame = result.getFrame(frameIndex)
            
            for (channel in 0 until channels) {
                val inputSample = frame[channel]
                var outputSample = inputSample // Оригінальний сигнал
                
                for (voice in 0 until voices) {
                    val delayBuffer = delayBuffers[voice][channel]
                    val delayIndex = delayIndices[voice][channel]
                    
                    // Генеруємо LFO з різними фазами для кожного голосу
                    val lfo = sin(2 * PI * rate * frameIndex / sampleRate + lfoPhases[voice][channel] + voice * PI / voices)
                    lfoPhases[voice][channel] += 2 * PI * rate / sampleRate
                    if (lfoPhases[voice][channel] >= 2 * PI) lfoPhases[voice][channel] -= 2 * PI
                    
                    // Обчислюємо затримку
                    val delaySamples = (delayBuffer.size * depth * (1 + lfo) / 2).toInt()
                    val delayedIndex = (delayIndex - delaySamples + delayBuffer.size) % delayBuffer.size
                    val delayedSample = delayBuffer[delayedIndex]
                    
                    // Додаємо затриманий сигнал
                    outputSample += delayedSample * 0.5
                    
                    // Зберігаємо в буфер
                    delayBuffer[delayIndex] = inputSample
                    delayIndices[voice][channel] = (delayIndex + 1) % delayBuffer.size
                }
                
                // Усереднюємо всі голоси
                frame[channel] = outputSample / (1 + voices * 0.5)
            }
        }
        
        return result
    }
}

/**
 * представлення інтерфейсу для аудіо аналізу
 */
interface AudioAnalyzer {
    /**
     * обчислити рівень сигналу
     *
     * @param audioData аудіо дані
     * @return рівень сигналу
     */
    fun getRMSLevel(audioData: AudioData): Double
    
    /**
     * обчислити пік рівень
     *
     * @param audioData аудіо дані
     * @return пік рівень
     */
    fun getPeakLevel(audioData: AudioData): Double
    
    /**
     * обчислити спектр
     *
     * @param audioData аудіо дані
     * @param frameIndex індекс фрейму
     * @return спектр
     */
    fun getSpectrum(audioData: AudioData, frameIndex: Long): DoubleArray
    
    /**
     * виявити темп
     *
     * @param audioData аудіо дані
     * @return темп у BPM
     */
    fun detectTempo(audioData: AudioData): Double
}

/**
 * представлення базової реалізації аудіо аналізатора