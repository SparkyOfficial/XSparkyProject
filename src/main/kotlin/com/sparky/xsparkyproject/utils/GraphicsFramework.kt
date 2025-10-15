/**
 * фреймворк для графіки
 *
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.awt.*
import java.awt.geom.*
import java.awt.image.*
import kotlin.math.*

/**
 * представлення інтерфейсу для роботи з графічним контекстом
 */
interface GraphicsContext {
    /**
     * встановити колір
     *
     * @param color колір
     */
    fun setColor(color: Color)
    
    /**
     * встановити товщину лінії
     *
     * @param width товщина
     */
    fun setLineWidth(width: Float)
    
    /**
     * встановити стиль закінчень ліній
     *
     * @param cap стиль
     */
    fun setLineCap(cap: LineCap)
    
    /**
     * встановити стиль з'єднань ліній
     *
     * @param join стиль
     */
    fun setLineJoin(join: LineJoin)
    
    /**
     * встановити шрифт
     *
     * @param font шрифт
     */
    fun setFont(font: Font)
    
    /**
     * перемістити точку малювання
     *
     * @param x координата x
     * @param y координата y
     */
    fun moveTo(x: Double, y: Double)
    
    /**
     * провести лінію до точки
     *
     * @param x координата x
     * @param y координата y
     */
    fun lineTo(x: Double, y: Double)
    
    /**
     * провести криву Безьє
     *
     * @param controlX1 координата x першої контрольної точки
     * @param controlY1 координата y першої контрольної точки
     * @param controlX2 координата x другої контрольної точки
     * @param controlY2 координата y другої контрольної точки
     * @param endX координата x кінцевої точки
     * @param endY координата y кінцевої точки
     */
    fun bezierCurveTo(controlX1: Double, controlY1: Double, controlX2: Double, controlY2: Double, endX: Double, endY: Double)
    
    /**
     * провести квадратичну криву Безьє
     *
     * @param controlX координата x контрольної точки
     * @param controlY координата y контрольної точки
     * @param endX координата x кінцевої точки
     * @param endY координата y кінцевої точки
     */
    fun quadraticCurveTo(controlX: Double, controlY: Double, endX: Double, endY: Double)
    
    /**
     * закрити контур
     */
    fun closePath()
    
    /**
     * заповнити поточний контур
     */
    fun fill()
    
    /**
     * обвести поточний контур
     */
    fun stroke()
    
    /**
     * очистити графічний контекст
     */
    fun clear()
    
    /**
     * зберегти графічний контекст
     */
    fun save()
    
    /**
     * відновити графічний контекст
     */
    fun restore()
    
    /**
     * перемістити графічний контекст
     *
     * @param x зміщення по x
     * @param y зміщення по y
     */
    fun translate(x: Double, y: Double)
    
    /**
     * повернути графічний контекст
     *
     * @param angle кут повороту в радіанах
     */
    fun rotate(angle: Double)
    
    /**
     * масштабувати графічний контекст
     *
     * @param scaleX масштаб по x
     * @param scaleY масштаб по y
     */
    fun scale(scaleX: Double, scaleY: Double)
    
    /**
     * малювати прямокутник
     *
     * @param x координата x
     * @param y координата y
     * @param width ширина
     * @param height висота
     */
    fun drawRect(x: Double, y: Double, width: Double, height: Double)
    
    /**
     * малювати закруглений прямокутник
     *
     * @param x координата x
     * @param y координата y
     * @param width ширина
     * @param height висота
     * @param radius радіус закруглення
     */
    fun drawRoundedRect(x: Double, y: Double, width: Double, height: Double, radius: Double)
    
    /**
     * малювати коло
     *
     * @param x координата x центру
     * @param y координата y центру
     * @param radius радіус
     */
    fun drawCircle(x: Double, y: Double, radius: Double)
    
    /**
     * малювати еліпс
     *
     * @param x координата x центру
     * @param y координата y центру
     * @param width ширина
     * @param height висота
     */
    fun drawEllipse(x: Double, y: Double, width: Double, height: Double)
    
    /**
     * малювати текст
     *
     * @param text текст
     * @param x координата x
     * @param y координата y
     */
    fun drawText(text: String, x: Double, y: Double)
    
    /**
     * виміряти текст
     *
     * @param text текст
     * @return розміри тексту
     */
    fun measureText(text: String): TextMetrics
}

/**
 * представлення стилю закінчень ліній
 */
enum class LineCap {
    BUTT,
    ROUND,
    SQUARE
}

/**
 * представлення стилю з'єднань ліній
 */
enum class LineJoin {
    MITER,
    ROUND,
    BEVEL
}

/**
 * представлення метрик тексту
 *
 * @property width ширина
 * @property height висота
 * @property actualBoundingBoxAscent висота над лінією
 * @property actualBoundingBoxDescent висота під лінією
 */
data class TextMetrics(
    val width: Double,
    val height: Double,
    val actualBoundingBoxAscent: Double,
    val actualBoundingBoxDescent: Double
)

/**
 * представлення базової реалізації графічного контексту
 */
open class BaseGraphicsContext(private val graphics2D: Graphics2D) : GraphicsContext {
    
    private val stateStack = mutableListOf<GraphicsState>()
    
    override fun setColor(color: Color) {
        graphics2D.color = color
    }
    
    override fun setLineWidth(width: Float) {
        graphics2D.stroke = BasicStroke(width)
    }
    
    override fun setLineCap(cap: LineCap) {
        val currentStroke = graphics2D.stroke as? BasicStroke ?: BasicStroke()
        val newCap = when (cap) {
            LineCap.BUTT -> BasicStroke.CAP_BUTT
            LineCap.ROUND -> BasicStroke.CAP_ROUND
            LineCap.SQUARE -> BasicStroke.CAP_SQUARE
        }
        graphics2D.stroke = BasicStroke(
            currentStroke.lineWidth,
            newCap,
            currentStroke.lineJoin,
            currentStroke.miterLimit,
            currentStroke.dashArray,
            currentStroke.dashPhase
        )
    }
    
    override fun setLineJoin(join: LineJoin) {
        val currentStroke = graphics2D.stroke as? BasicStroke ?: BasicStroke()
        val newJoin = when (join) {
            LineJoin.MITER -> BasicStroke.JOIN_MITER
            LineJoin.ROUND -> BasicStroke.JOIN_ROUND
            LineJoin.BEVEL -> BasicStroke.JOIN_BEVEL
        }
        graphics2D.stroke = BasicStroke(
            currentStroke.lineWidth,
            currentStroke.lineCap,
            newJoin,
            currentStroke.miterLimit,
            currentStroke.dashArray,
            currentStroke.dashPhase
        )
    }
    
    override fun setFont(font: Font) {
        graphics2D.font = font
    }
    
    override fun moveTo(x: Double, y: Double) {
        // У AWT немає прямого еквівалента, використовуємо Path2D
    }
    
    override fun lineTo(x: Double, y: Double) {
        // У AWT немає прямого еквівалента, використовуємо Path2D
    }
    
    override fun bezierCurveTo(controlX1: Double, controlY1: Double, controlX2: Double, controlY2: Double, endX: Double, endY: Double) {
        // У AWT немає прямого еквівалента, використовуємо Path2D
    }
    
    override fun quadraticCurveTo(controlX: Double, controlY: Double, endX: Double, endY: Double) {
        // У AWT немає прямого еквівалента, використовуємо Path2D
    }
    
    override fun closePath() {
        // У AWT немає прямого еквівалента, використовуємо Path2D
    }
    
    override fun fill() {
        // У AWT немає прямого еквівалента, використовуємо Path2D
    }
    
    override fun stroke() {
        // У AWT немає прямого еквівалента, використовуємо Path2D
    }
    
    override fun clear() {
        graphics2D.clearRect(0, 0, graphics2D.clipBounds?.width ?: 0, graphics2D.clipBounds?.height ?: 0)
    }
    
    override fun save() {
        stateStack.add(GraphicsState(
            graphics2D.color,
            graphics2D.font,
            graphics2D.stroke,
            graphics2D.transform
        ))
        graphics2D.save()
    }
    
    override fun restore() {
        if (stateStack.isNotEmpty()) {
            val state = stateStack.removeAt(stateStack.size - 1)
            graphics2D.color = state.color
            graphics2D.font = state.font
            graphics2D.stroke = state.stroke
            graphics2D.transform = state.transform
        }
        graphics2D.restore()
    }
    
    override fun translate(x: Double, y: Double) {
        graphics2D.translate(x, y)
    }
    
    override fun rotate(angle: Double) {
        graphics2D.rotate(angle)
    }
    
    override fun scale(scaleX: Double, scaleY: Double) {
        graphics2D.scale(scaleX, scaleY)
    }
    
    override fun drawRect(x: Double, y: Double, width: Double, height: Double) {
        graphics2D.drawRect(x.toInt(), y.toInt(), width.toInt(), height.toInt())
    }
    
    override fun drawRoundedRect(x: Double, y: Double, width: Double, height: Double, radius: Double) {
        graphics2D.drawRoundRect(x.toInt(), y.toInt(), width.toInt(), height.toInt(), radius.toInt(), radius.toInt())
    }
    
    override fun drawCircle(x: Double, y: Double, radius: Double) {
        graphics2D.drawOval((x - radius).toInt(), (y - radius).toInt(), (radius * 2).toInt(), (radius * 2).toInt())
    }
    
    override fun drawEllipse(x: Double, y: Double, width: Double, height: Double) {
        graphics2D.drawOval(x.toInt(), y.toInt(), width.toInt(), height.toInt())
    }
    
    override fun drawText(text: String, x: Double, y: Double) {
        graphics2D.drawString(text, x.toFloat(), y.toFloat())
    }
    
    override fun measureText(text: String): TextMetrics {
        val fontMetrics = graphics2D.fontMetrics
        val width = fontMetrics.stringWidth(text).toDouble()
        val height = fontMetrics.height.toDouble()
        val ascent = fontMetrics.ascent.toDouble()
        val descent = fontMetrics.descent.toDouble()
        
        return TextMetrics(width, height, ascent, descent)
    }
    
    /**
     * отримати внутрішній Graphics2D
     *
     * @return Graphics2D
     */
    fun getGraphics2D(): Graphics2D = graphics2D
}

/**
 * представлення стану графічного контексту
 */
data class GraphicsState(
    val color: Color,
    val font: Font,
    val stroke: Stroke,
    val transform: AffineTransform
)

/**
 * представлення інтерфейсу для роботи з векторною графікою
 */
interface VectorGraphics {
    /**
     * створити шлях
     *
     * @return шлях
     */
    fun createPath(): Path
    
    /**
     * малювати шлях
     *
     * @param path шлях
     * @param fillStyle стиль заповнення
     * @param strokeStyle стиль обведення
     */
    fun drawPath(path: Path, fillStyle: FillStyle? = null, strokeStyle: StrokeStyle? = null)
    
    /**
     * малювати лінію
     *
     * @param startX початкова координата x
     * @param startY початкова координата y
     * @param endX кінцева координата x
     * @param endY кінцева координата y
     * @param strokeStyle стиль обведення
     */
    fun drawLine(startX: Double, startY: Double, endX: Double, endY: Double, strokeStyle: StrokeStyle? = null)
    
    /**
     * малювати полігон
     *
     * @param points точки
     * @param fillStyle стиль заповнення
     * @param strokeStyle стиль обведення
     */
    fun drawPolygon(points: List<Point2D>, fillStyle: FillStyle? = null, strokeStyle: StrokeStyle? = null)
    
    /**
     * малювати полілінію
     *
     * @param points точки
     * @param strokeStyle стиль обведення
     */
    fun drawPolyline(points: List<Point2D>, strokeStyle: StrokeStyle? = null)
    
    /**
     * малювати дугу
     *
     * @param centerX координата x центру
     * @param centerY координата y центру
     * @param radius радіус
     * @param startAngle початковий кут
     * @param endAngle кінцевий кут
     * @param strokeStyle стиль обведення
     */
    fun drawArc(centerX: Double, centerY: Double, radius: Double, startAngle: Double, endAngle: Double, strokeStyle: StrokeStyle? = null)
    
    /**
     * малювати сектор
     *
     * @param centerX координата x центру
     * @param centerY координата y центру
     * @param radius радіус
     * @param startAngle початковий кут
     * @param endAngle кінцевий кут
     * @param fillStyle стиль заповнення
     * @param strokeStyle стиль обведення
     */
    fun drawSector(centerX: Double, centerY: Double, radius: Double, startAngle: Double, endAngle: Double, fillStyle: FillStyle? = null, strokeStyle: StrokeStyle? = null)
}

/**
 * представлення точки 2D
 *
 * @property x координата x
 * @property y координата y
 */
data class Point2D(val x: Double, val y: Double)

/**
 * представлення інтерфейсу для шляху
 */
interface Path {
    /**
     * перемістити точку малювання
     *
     * @param x координата x
     * @param y координата y
     */
    fun moveTo(x: Double, y: Double)
    
    /**
     * провести лінію до точки
     *
     * @param x координата x
     * @param y координата y
     */
    fun lineTo(x: Double, y: Double)
    
    /**
     * провести криву Безьє
     *
     * @param controlX1 координата x першої контрольної точки
     * @param controlY1 координата y першої контрольної точки
     * @param controlX2 координата x другої контрольної точки
     * @param controlY2 координата y другої контрольної точки
     * @param endX координата x кінцевої точки
     * @param endY координата y кінцевої точки
     */
    fun bezierCurveTo(controlX1: Double, controlY1: Double, controlX2: Double, controlY2: Double, endX: Double, endY: Double)
    
    /**
     * провести квадратичну криву Безьє
     *
     * @param controlX координата x контрольної точки
     * @param controlY координата y контрольної точки
     * @param endX координата x кінцевої точки
     * @param endY координата y кінцевої точки
     */
    fun quadraticCurveTo(controlX: Double, controlY: Double, endX: Double, endY: Double)
    
    /**
     * закрити контур
     */
    fun closePath()
    
    /**
     * очистити шлях
     */
    fun clear()
}

/**
 * представлення стилю заповнення
 */
sealed class FillStyle {
    /**
     * представлення суцільного заповнення
     *
     * @property color колір
     */
    data class Solid(val color: Color) : FillStyle()
    
    /**
     * представлення градієнтного заповнення
     *
     * @property gradient градієнт
     */
    data class Gradient(val gradient: Gradient) : FillStyle()
    
    /**
     * представлення текстурного заповнення
     *
     * @property texture текстура
     */
    data class Texture(val texture: Texture) : FillStyle()
}

/**
 * представлення стилю обведення
 */
data class StrokeStyle(
    val color: Color = Color.BLACK,
    val width: Float = 1.0f,
    val lineCap: LineCap = LineCap.BUTT,
    val lineJoin: LineJoin = LineJoin.MITER,
    val dashArray: FloatArray? = null,
    val dashOffset: Float = 0.0f
)

/**
 * представлення інтерфейсу для градієнта
 */
interface Gradient {
    /**
     * додати колірну зупинку
     *
     * @param offset зміщення
     * @param color колір
     */
    fun addColorStop(offset: Float, color: Color)
}

/**
 * представлення лінійного градієнта
 *
 * @property startX початкова координата x
 * @property startY початкова координата y
 * @property endX кінцева координата x
 * @property endY кінцева координата y