/**
 * фреймворк для обробки зображень
 *
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.awt.*
import java.awt.image.*
import java.io.*
import javax.imageio.*
import kotlin.math.*

/**
 * представлення інтерфейсу для роботи з пікселями зображення
 */
interface Image {
    /**
     * отримати ширину зображення
     *
     * @return ширина
     */
    fun getWidth(): Int
    
    /**
     * отримати висоту зображення
     *
     * @return висота
     */
    fun getHeight(): Int
    
    /**
     * отримати колір пікселя
     *
     * @param x координата x
     * @param y координата y
     * @return колір
     */
    fun getPixel(x: Int, y: Int): Color
    
    /**
     * встановити колір пікселя
     *
     * @param x координата x
     * @param y координата y
     * @param color колір
     */
    fun setPixel(x: Int, y: Int, color: Color)
    
    /**
     * отримати всі пікселі як масив
     *
     * @return масив пікселів
     */
    fun getPixels(): Array<ColorArray>
    
    /**
     * встановити всі пікселі з масиву
     *
     * @param pixels масив пікселів
     */
    fun setPixels(pixels: Array<ColorArray>)
    
    /**
     * зберегти зображення у файл
     *
     * @param file файл
     * @param format формат
     */
    fun save(file: File, format: String = "PNG")
    
    /**
     * створити копію зображення
     *
     * @return копія
     */
    fun copy(): Image
}

/**
 * представлення масиву кольорів
 */
class ColorArray(val colors: Array<Color>) {
    operator fun get(index: Int): Color = colors[index]
    operator fun set(index: Int, color: Color) { colors[index] = color }
    fun size(): Int = colors.size
}

/**
 * представлення базової реалізації зображення
 */
open class BaseImage(private val bufferedImage: BufferedImage) : Image {
    
    constructor(width: Int, height: Int) : this(BufferedImage(width, height, BufferedImage.TYPE_INT_RGB))
    
    override fun getWidth(): Int = bufferedImage.width
    
    override fun getHeight(): Int = bufferedImage.height
    
    override fun getPixel(x: Int, y: Int): Color {
        require(x >= 0 && x < getWidth()) { "Невірна координата x: $x" }
        require(y >= 0 && y < getHeight()) { "Невірна координата y: $y" }
        val rgb = bufferedImage.getRGB(x, y)
        return Color(rgb)
    }
    
    override fun setPixel(x: Int, y: Int, color: Color) {
        require(x >= 0 && x < getWidth()) { "Невірна координата x: $x" }
        require(y >= 0 && y < getHeight()) { "Невірна координата y: $y" }
        bufferedImage.setRGB(x, y, color.rgb)
    }
    
    override fun getPixels(): Array<ColorArray> {
        val width = getWidth()
        val height = getHeight()
        val pixels = Array(height) { ColorArray(Array(width) { Color.BLACK }) }
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y][x] = getPixel(x, y)
            }
        }
        
        return pixels
    }
    
    override fun setPixels(pixels: Array<ColorArray>) {
        val width = getWidth()
        val height = getHeight()
        require(pixels.size == height) { "Невірна висота масиву пікселів" }
        
        for (y in 0 until height) {
            require(pixels[y].size() == width) { "Невірна ширина масиву пікселів у рядку $y" }
            for (x in 0 until width) {
                setPixel(x, y, pixels[y][x])
            }
        }
    }
    
    override fun save(file: File, format: String) {
        ImageIO.write(bufferedImage, format, file)
    }
    
    override fun copy(): Image {
        val copy = BaseImage(getWidth(), getHeight())
        for (y in 0 until getHeight()) {
            for (x in 0 until getWidth()) {
                copy.setPixel(x, y, getPixel(x, y))
            }
        }
        return copy
    }
    
    /**
     * отримати внутрішнє зображення BufferedImage
     *
     * @return BufferedImage
     */
    fun getBufferedImage(): BufferedImage = bufferedImage
    
    /**
     * застосувати фільтр до зображення
     *
     * @param filter фільтр
     * @return нове зображення
     */
    fun applyFilter(filter: ImageFilter): Image {
        return filter.apply(this)
    }
    
    /**
     * змінити розмір зображення
     *
     * @param newWidth нова ширина
     * @param newHeight нова висота
     * @return нове зображення
     */
    fun resize(newWidth: Int, newHeight: Int): Image {
        val resizedImage = BufferedImage(newWidth, newHeight, bufferedImage.type)
        val g = resizedImage.createGraphics()
        g.drawImage(bufferedImage, 0, 0, newWidth, newHeight, null)
        g.dispose()
        return BaseImage(resizedImage)
    }
    
    /**
     * обрізати зображення
     *
     * @param x координата x
     * @param y координата y
     * @param width ширина
     * @param height висота
     * @return нове зображення
     */
    fun crop(x: Int, y: Int, width: Int, height: Int): Image {
        require(x >= 0 && y >= 0) { "Координати мають бути невід'ємними" }
        require(x + width <= getWidth() && y + height <= getHeight()) { "Розміри обрізки виходять за межі зображення" }
        
        val croppedImage = bufferedImage.getSubimage(x, y, width, height)
        return BaseImage(croppedImage)
    }
    
    /**
     * повернути зображення
     *
     * @param angle кут повороту в градусах
     * @return нове зображення
     */
    fun rotate(angle: Double): Image {
        val radians = Math.toRadians(angle)
        val sin = abs(sin(radians))
        val cos = abs(cos(radians))
        val newWidth = (getWidth() * cos + getHeight() * sin).toInt()
        val newHeight = (getWidth() * sin + getHeight() * cos).toInt()
        
        val rotatedImage = BufferedImage(newWidth, newHeight, bufferedImage.type)
        val g = rotatedImage.createGraphics()
        
        g.translate((newWidth - getWidth()) / 2, (newHeight - getHeight()) / 2)
        g.rotate(radians, (getWidth() / 2).toDouble(), (getHeight() / 2).toDouble())
        g.drawImage(bufferedImage, 0, 0, null)
        g.dispose()
        
        return BaseImage(rotatedImage)
    }
}

/**
 * представлення інтерфейсу для фільтрів зображення
 */
interface ImageFilter {
    /**
     * застосувати фільтр до зображення
     *
     * @param image зображення
     * @return нове зображення
     */
    fun apply(image: Image): Image
}

/**
 * представлення фільтра яскравості
 *
 * @property brightness яскравість (-1.0 до 1.0)
 */
class BrightnessFilter(private val brightness: Double) : ImageFilter {
    
    override fun apply(image: Image): Image {
        require(brightness >= -1.0 && brightness <= 1.0) { "Яскравість має бути в діапазоні [-1.0, 1.0]" }
        
        val result = image.copy()
        val factor = 1.0 + brightness
        
        for (y in 0 until result.getHeight()) {
            for (x in 0 until result.getWidth()) {
                val originalColor = result.getPixel(x, y)
                val newRed = (originalColor.red * factor).toInt().coerceIn(0, 255)
                val newGreen = (originalColor.green * factor).toInt().coerceIn(0, 255)
                val newBlue = (originalColor.blue * factor).toInt().coerceIn(0, 255)
                result.setPixel(x, y, Color(newRed, newGreen, newBlue))
            }
        }
        
        return result
    }
}

/**
 * представлення фільтра контрасту
 *
 * @property contrast контраст (-1.0 до 1.0)
 */
class ContrastFilter(private val contrast: Double) : ImageFilter {
    
    override fun apply(image: Image): Image {
        require(contrast >= -1.0 && contrast <= 1.0) { "Контраст має бути в діапазоні [-1.0, 1.0]" }
        
        val result = image.copy()
        val factor = (259 * (contrast * 256 + 255)) / (255 * (259 - contrast * 256))
        
        for (y in 0 until result.getHeight()) {
            for (x in 0 until result.getWidth()) {
                val originalColor = result.getPixel(x, y)
                val newRed = (factor * (originalColor.red - 128) + 128).toInt().coerceIn(0, 255)
                val newGreen = (factor * (originalColor.green - 128) + 128).toInt().coerceIn(0, 255)
                val newBlue = (factor * (originalColor.blue - 128) + 128).toInt().coerceIn(0, 255)
                result.setPixel(x, y, Color(newRed, newGreen, newBlue))
            }
        }
        
        return result
    }
}

/**
 * представлення фільтра насиченості
 *
 * @property saturation насиченість (-1.0 до 1.0)
 */
class SaturationFilter(private val saturation: Double) : ImageFilter {
    
    override fun apply(image: Image): Image {
        require(saturation >= -1.0 && saturation <= 1.0) { "Насиченість має бути в діапазоні [-1.0, 1.0]" }
        
        val result = image.copy()
        val factor = 1.0 + saturation
        
        for (y in 0 until result.getHeight()) {
            for (x in 0 until result.getWidth()) {
                val originalColor = result.getPixel(x, y)
                val gray = (0.299 * originalColor.red + 0.587 * originalColor.green + 0.114 * originalColor.blue)
                val newRed = (gray + factor * (originalColor.red - gray)).toInt().coerceIn(0, 255)
                val newGreen = (gray + factor * (originalColor.green - gray)).toInt().coerceIn(0, 255)
                val newBlue = (gray + factor * (originalColor.blue - gray)).toInt().coerceIn(0, 255)
                result.setPixel(x, y, Color(newRed, newGreen, newBlue))
            }
        }
        
        return result
    }
}

/**
 * представлення фільтра гауссового розмиття
 *
 * @property radius радіус розмиття
 */
class GaussianBlurFilter(private val radius: Int) : ImageFilter {
    
    override fun apply(image: Image): Image {
        require(radius > 0) { "Радіус має бути більше 0" }
        
        val result = image.copy()
        val kernel = createGaussianKernel(radius)
        val width = image.getWidth()
        val height = image.getHeight()
        
        // Горизонтальне розмиття
        for (y in 0 until height) {
            for (x in 0 until width) {
                var r = 0.0
                var g = 0.0
                var b = 0.0
                
                for (i in -radius..radius) {
                    val nx = (x + i).coerceIn(0, width - 1)
                    val color = image.getPixel(nx, y)
                    val weight = kernel[i + radius]
                    r += color.red * weight
                    g += color.green * weight
                    b += color.blue * weight
                }
                
                result.setPixel(x, y, Color(r.toInt().coerceIn(0, 255), g.toInt().coerceIn(0, 255), b.toInt().coerceIn(0, 255)))
            }
        }
        
        // Вертикальне розмиття
        val temp = result.copy()
        for (y in 0 until height) {
            for (x in 0 until width) {
                var r = 0.0
                var g = 0.0
                var b = 0.0
                
                for (i in -radius..radius) {
                    val ny = (y + i).coerceIn(0, height - 1)
                    val color = temp.getPixel(x, ny)
                    val weight = kernel[i + radius]
                    r += color.red * weight
                    g += color.green * weight
                    b += color.blue * weight
                }
                
                result.setPixel(x, y, Color(r.toInt().coerceIn(0, 255), g.toInt().coerceIn(0, 255), b.toInt().coerceIn(0, 255)))
            }
        }
        
        return result
    }
    
    private fun createGaussianKernel(radius: Int): DoubleArray {
        val kernel = DoubleArray(2 * radius + 1)
        val sigma = radius / 3.0
        val sigmaSquared2 = 2.0 * sigma * sigma
        var sum = 0.0
        
        for (i in -radius..radius) {
            val value = exp(-(i * i) / sigmaSquared2)
            kernel[i + radius] = value
            sum += value
        }
        
        // Нормалізація
        for (i in kernel.indices) {
            kernel[i] /= sum
        }
        
        return kernel
    }
}

/**
 * представлення фільтра різкості
 *
 * @property strength сила різкості
 */
class SharpnessFilter(private val strength: Double) : ImageFilter {
    
    override fun apply(image: Image): Image {
        val result = image.copy()
        val width = image.getWidth()
        val height = image.getHeight()
        
        // Матриця різкості
        val kernel = arrayOf(
            doubleArrayOf(0.0, -strength, 0.0),
            doubleArrayOf(-strength, 1.0 + 4 * strength, -strength),
            doubleArrayOf(0.0, -strength, 0.0)
        )
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var r = 0.0
                var g = 0.0
                var b = 0.0
                
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val color = image.getPixel(x + kx, y + ky)
                        val weight = kernel[ky + 1][kx + 1]
                        r += color.red * weight
                        g += color.green * weight
                        b += color.blue * weight
                    }
                }
                
                result.setPixel(x, y, Color(r.toInt().coerceIn(0, 255), g.toInt().coerceIn(0, 255), b.toInt().coerceIn(0, 255)))
            }
        }
        
        return result
    }
}

/**
 * представлення фільтра виявлення країв
 */
class EdgeDetectionFilter : ImageFilter {
    
    override fun apply(image: Image): Image {
        val result = image.copy()
        val width = image.getWidth()
        val height = image.getHeight()
        
        // Матриця Собеля для виявлення країв
        val gx = arrayOf(
            intArrayOf(-1, 0, 1),
            intArrayOf(-2, 0, 2),
            intArrayOf(-1, 0, 1)
        )
        
        val gy = arrayOf(
            intArrayOf(-1, -2, -1),
            intArrayOf(0, 0, 0),
            intArrayOf(1, 2, 1)
        )
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var rx = 0
                var gxValue = 0
                var bx = 0
                var ry = 0
                var gyValue = 0
                var by = 0
                
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val color = image.getPixel(x + kx, y + ky)
                        val weightX = gx[ky + 1][kx + 1]
                        val weightY = gy[ky + 1][kx + 1]
                        
                        rx += color.red * weightX
                        gxValue += color.green * weightX
                        bx += color.blue * weightX
                        
                        ry += color.red * weightY
                        gyValue += color.green * weightY
                        by += color.blue * weightY
                    }
                }
                
                val r = sqrt((rx * rx + ry * ry).toDouble()).toInt().coerceIn(0, 255)
                val g = sqrt((gxValue * gxValue + gyValue * gyValue).toDouble()).toInt().coerceIn(0, 255)
                val b = sqrt((bx * bx + by * by).toDouble()).toInt().coerceIn(0, 255)
                
                result.setPixel(x, y, Color(r, g, b))
            }
        }
        
        return result
    }
}

/**
 * представлення фільтра сепії
 */
class SepiaFilter : ImageFilter {
    
    override fun apply(image: Image): Image {
        val result = image.copy()
        
        for (y in 0 until result.getHeight()) {
            for (x in 0 until result.getWidth()) {
                val originalColor = result.getPixel(x, y)
                val r = originalColor.red
                val g = originalColor.green
                val b = originalColor.blue
                
                val tr = (0.393 * r + 0.769 * g + 0.189 * b).toInt().coerceIn(0, 255)
                val tg = (0.349 * r + 0.686 * g + 0.168 * b).toInt().coerceIn(0, 255)
                val tb = (0.272 * r + 0.534 * g + 0.131 * b).toInt().coerceIn(0, 255)
                
                result.setPixel(x, y, Color(tr, tg, tb))
            }
        }
        
        return result
    }
}

/**
 * представлення фільтра чорно-білого зображення
 */
class GrayscaleFilter : ImageFilter {
    
    override fun apply(image: Image): Image {
        val result = image.copy()
        
        for (y in 0 until result.getHeight()) {
            for (x in 0 until result.getWidth()) {
                val originalColor = result.getPixel(x, y)
                val gray = (0.299 * originalColor.red + 0.587 * originalColor.green + 0.114 * originalColor.blue).toInt()
                result.setPixel(x, y, Color(gray, gray, gray))
            }
        }
        
        return result
    }
}

/**
 * представлення фільтра інверсії кольорів
 */
class InvertFilter : ImageFilter {
    
    override fun apply(image: Image): Image {
        val result = image.copy()
        
        for (y in 0 until result.getHeight()) {
            for (x in 0 until result.getWidth()) {
                val originalColor = result.getPixel(x, y)
                val r = 255 - originalColor.red
                val g = 255 - originalColor.green
                val b = 255 - originalColor.blue
                result.setPixel(x, y, Color(r, g, b))
            }
        }
        
        return result
    }
}

/**
 * представлення інтерфейсу для роботи з гістограмою зображення
 */
interface Histogram {
    /**
     * отримати гістограму червоного каналу
     *
     * @return масив значень
     */
    fun getRedHistogram(): IntArray
    
    /**
     * отримати гістограму зеленого каналу
     *
     * @return масив значень
     */
    fun getGreenHistogram(): IntArray
    
    /**
     * отримати гістограму синього каналу
     *
     * @return масив значень
     */
    fun getBlueHistogram(): IntArray
    
    /**
     * отримати гістограму яскравості
     *
     * @return масив значень
     */
    fun getBrightnessHistogram(): IntArray
    
    /**
     * обчислити середню яскравість
     *
     * @return середня яскравість
     */
    fun getAverageBrightness(): Double
    
    /**
     * обчислити стандартне відхилення яскравості
     *
     * @return стандартне відхилення
     */
    fun getBrightnessStdDev(): Double
}

/**
 * представлення базової реалізації гістограми
 */
class BaseHistogram(private val image: Image) : Histogram {
    
    override fun getRedHistogram(): IntArray {
        val histogram = IntArray(256)
        for (y in 0 until image.getHeight()) {
            for (x in 0 until image.getWidth()) {
                val color = image.getPixel(x, y)
                histogram[color.red]++
            }
        }
        return histogram
    }
    
    override fun getGreenHistogram(): IntArray {
        val histogram = IntArray(256)
        for (y in 0 until image.getHeight()) {
            for (x in 0 until image.getWidth()) {
                val color = image.getPixel(x, y)
                histogram[color.green]++
            }
        }
        return histogram
    }
    
    override fun getBlueHistogram(): IntArray {
        val histogram = IntArray(256)
        for (y in 0 until image.getHeight()) {
            for (x in 0 until image.getWidth()) {
                val color = image.getPixel(x, y)
                histogram[color.blue]++
            }
        }
        return histogram
    }
    
    override fun getBrightnessHistogram(): IntArray {
        val histogram = IntArray(256)
        for (y in 0 until image.getHeight()) {
            for (x in 0 until image.getWidth()) {
                val color = image.getPixel(x, y)
                val brightness = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue).toInt().coerceIn(0, 255)
                histogram[brightness]++
            }
        }
        return histogram
    }
    
    override fun getAverageBrightness(): Double {
        var sum = 0.0
        val totalPixels = image.getWidth() * image.getHeight()
        
        for (y in 0 until image.getHeight()) {
            for (x in 0 until image.getWidth()) {
                val color = image.getPixel(x, y)
                val brightness = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
                sum += brightness
            }
        }
        
        return sum / totalPixels
    }
    
    override fun getBrightnessStdDev(): Double {
        val average = getAverageBrightness()
        var sumSquaredDiff = 0.0
        val totalPixels = image.getWidth() * image.getHeight()
        
        for (y in 0 until image.getHeight()) {
            for (x in 0 until image.getWidth()) {
                val color = image.getPixel(x, y)
                val brightness = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
                val diff = brightness - average
                sumSquaredDiff += diff * diff
            }
        }
        
        return sqrt(sumSquaredDiff / totalPixels)
    }
}

/**
 * представлення інтерфейсу для роботи з морфологічними операціями
 */
interface MorphologicalOperation {
    /**
     * застосувати морфологічну операцію
     *
     * @param image зображення
     * @return нове зображення
     */
    fun apply(image: Image): Image
}

/**
 * представлення ерозії
 *
 * @property kernelSize розмір ядра
 */
class ErosionOperation(private val kernelSize: Int = 3) : MorphologicalOperation {
    
    override fun apply(image: Image): Image {
        require(kernelSize % 2 == 1) { "Розмір ядра має бути непарним" }
        require(kernelSize > 0) { "Розмір ядра має бути більше 0" }
        
        val result = image.copy()
        val width = image.getWidth()
        val height = image.getHeight()
        val radius = kernelSize / 2
        
        for (y in radius until height - radius) {
            for (x in radius until width - radius) {
                var minRed = 255
                var minGreen = 255
                var minBlue = 255
                
                for (ky in -radius..radius) {
                    for (kx in -radius..radius) {
                        val color = image.getPixel(x + kx, y + ky)
                        if (color.red < minRed) minRed = color.red
                        if (color.green < minGreen) minGreen = color.green
                        if (color.blue < minBlue) minBlue = color.blue
                    }
                }
                
                result.setPixel(x, y, Color(minRed, minGreen, minBlue))
            }
        }
        
        return result
    }
}

/**
 * представлення дилатації
 *
 * @property kernelSize розмір ядра