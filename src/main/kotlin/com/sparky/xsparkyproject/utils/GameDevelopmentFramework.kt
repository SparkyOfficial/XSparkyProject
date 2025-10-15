/**
 * фреймворк для розробки ігор
 *
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.awt.*
import java.awt.event.*
import java.util.*
import java.util.concurrent.*
import javax.swing.*
import kotlin.math.*

/**
 * представлення інтерфейсу для ігрового двигуна
 */
interface GameEngine {
    /**
     * ініціалізувати гру
     */
    fun initialize()
    
    /**
     * оновити стан гри
     *
     * @param deltaTime час, що минув з попереднього оновлення
     */
    fun update(deltaTime: Double)
    
    /**
     * відмалювати гру
     *
     * @param graphics графічний контекст
     */
    fun render(graphics: Graphics2D)
    
    /**
     * запустити гру
     */
    fun start()
    
    /**
     * зупинити гру
     */
    fun stop()
    
    /**
     * додати об'єкт до гри
     *
     * @param gameObject ігровий об'єкт
     */
    fun addGameObject(gameObject: GameObject)
    
    /**
     * видалити об'єкт з гри
     *
     * @param gameObject ігровий об'єкт
     */
    fun removeGameObject(gameObject: GameObject)
    
    /**
     * отримати всі ігрові об'єкти
     *
     * @return список ігрових об'єктів
     */
    fun getGameObjects(): List<GameObject>
}

/**
 * представлення ігрового об'єкта
 */
interface GameObject {
    /**
     * отримати ім'я об'єкта
     *
     * @return ім'я
     */
    fun getName(): String
    
    /**
     * отримати позицію
     *
     * @return позиція
     */
    fun getPosition(): Vector2D
    
    /**
     * встановити позицію
     *
     * @param position позиція
     */
    fun setPosition(position: Vector2D)
    
    /**
     * отримати розмір
     *
     * @return розмір
     */
    fun getSize(): Vector2D
    
    /**
     * встановити розмір
     *
     * @param size розмір
     */
    fun setSize(size: Vector2D)
    
    /**
     * отримати теги
     *
     * @return теги
     */
    fun getTags(): Set<String>
    
    /**
     * додати тег
     *
     * @param tag тег
     */
    fun addTag(tag: String)
    
    /**
     * видалити тег
     *
     * @param tag тег
     */
    fun removeTag(tag: String)
    
    /**
     * перевірити, чи має тег
     *
     * @param tag тег
     * @return true, якщо має тег
     */
    fun hasTag(tag: String): Boolean
    
    /**
     * ініціалізувати об'єкт
     */
    fun initialize()
    
    /**
     * оновити об'єкт
     *
     * @param deltaTime час, що минув
     */
    fun update(deltaTime: Double)
    
    /**
     * відмалювати об'єкт
     *
     * @param graphics графічний контекст
     */
    fun render(graphics: Graphics2D)
    
    /**
     * знищити об'єкт
     */
    fun destroy()
}

/**
 * представлення вектора 2D
 *
 * @property x координата x
 * @property y координата y
 */
data class Vector2D(var x: Double, var y: Double) {
    /**
     * додати вектор
     *
     * @param other інший вектор
     * @return новий вектор
     */
    fun add(other: Vector2D): Vector2D {
        return Vector2D(x + other.x, y + other.y)
    }
    
    /**
     * відняти вектор
     *
     * @param other інший вектор
     * @return новий вектор
     */
    fun subtract(other: Vector2D): Vector2D {
        return Vector2D(x - other.x, y - other.y)
    }
    
    /**
     * помножити на скаляр
     *
     * @param scalar скаляр
     * @return новий вектор
     */
    fun multiply(scalar: Double): Vector2D {
        return Vector2D(x * scalar, y * scalar)
    }
    
    /**
     * поділити на скаляр
     *
     * @param scalar скаляр
     * @return новий вектор
     */
    fun divide(scalar: Double): Vector2D {
        return Vector2D(x / scalar, y / scalar)
    }
    
    /**
     * обчислити довжину вектора
     *
     * @return довжина
     */
    fun magnitude(): Double {
        return sqrt(x * x + y * y)
    }
    
    /**
     * нормалізувати вектор
     *
     * @return нормалізований вектор
     */
    fun normalize(): Vector2D {
        val mag = magnitude()
        return if (mag > 0) Vector2D(x / mag, y / mag) else Vector2D(0.0, 0.0)
    }
    
    /**
     * обчислити скалярний добуток з іншим вектором
     *
     * @param other інший вектор
     * @return скалярний добуток
     */
    fun dot(other: Vector2D): Double {
        return x * other.x + y * other.y
    }
    
    /**
     * обчислити відстань до іншого вектора
     *
     * @param other інший вектор
     * @return відстань
     */
    fun distanceTo(other: Vector2D): Double {
        return subtract(other).magnitude()
    }
    
    /**
     * змінити вектор
     *
     * @param other інший вектор
     */
    fun set(other: Vector2D) {
        x = other.x
        y = other.y
    }
    
    /**
     * змінити координати
     *
     * @param x координата x
     * @param y координата y
     */
    fun set(x: Double, y: Double) {
        this.x = x
        this.y = y
    }
    
    /**
     * скопіювати вектор
     *
     * @return копія
     */
    fun copy(): Vector2D {
        return Vector2D(x, y)
    }
    
    companion object {
        /**
         * нульовий вектор
         */
        val ZERO = Vector2D(0.0, 0.0)
        
        /**
         * одиничний вектор по x
         */
        val UNIT_X = Vector2D(1.0, 0.0)
        
        /**
         * одиничний вектор по y
         */
        val UNIT_Y = Vector2D(0.0, 1.0)
    }
}

/**
 * представлення базової реалізації ігрового двигуна
 */
open class BaseGameEngine(private val windowWidth: Int = 800, private val windowHeight: Int = 600) : GameEngine {
    private val gameObjects = mutableListOf<GameObject>()
    private val pendingAdditions = mutableListOf<GameObject>()
    private val pendingRemovals = mutableListOf<GameObject>()
    private var isRunning = false
    private var lastTime = 0L
    private lateinit var gamePanel: GamePanel
    private lateinit var gameFrame: JFrame
    private val executor = Executors.newSingleThreadScheduledExecutor()
    
    override fun initialize() {
        // Ініціалізація вікна гри
        gameFrame = JFrame("Game Engine")
        gamePanel = GamePanel()
        gameFrame.add(gamePanel)
        gameFrame.setSize(windowWidth, windowHeight)
        gameFrame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        gameFrame.isVisible = true
        
        // Ініціалізація ігрових об'єктів
        gameObjects.forEach { it.initialize() }
    }
    
    override fun update(deltaTime: Double) {
        // Застосувати очікувані додавання та видалення
        processPendingOperations()
        
        // Оновити всі ігрові об'єкти
        gameObjects.forEach { it.update(deltaTime) }
    }
    
    override fun render(graphics: Graphics2D) {
        // Очистити екран
        graphics.color = Color.BLACK
        graphics.fillRect(0, 0, windowWidth, windowHeight)
        
        // Відмалювати всі ігрові об'єкти
        gameObjects.forEach { it.render(graphics) }
    }
    
    override fun start() {
        if (isRunning) return
        
        isRunning = true
        lastTime = System.nanoTime()
        
        // Запустити ігровий цикл
        executor.scheduleAtFixedRate({
            val currentTime = System.nanoTime()
            val deltaTime = (currentTime - lastTime) / 1_000_000_000.0
            lastTime = currentTime
            
            update(deltaTime)
            gamePanel.repaint()
        }, 0, 16, TimeUnit.MILLISECONDS) // Приблизно 60 FPS
    }
    
    override fun stop() {
        isRunning = false
        executor.shutdown()
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executor.shutdownNow()
            Thread.currentThread().interrupt()
        }
        gameFrame.dispose()
    }
    
    override fun addGameObject(gameObject: GameObject) {
        pendingAdditions.add(gameObject)
    }
    
    override fun removeGameObject(gameObject: GameObject) {
        pendingRemovals.add(gameObject)
    }
    
    override fun getGameObjects(): List<GameObject> {
        return gameObjects.toList()
    }
    
    private fun processPendingOperations() {
        // Додати нові об'єкти
        pendingAdditions.forEach { gameObject ->
            gameObjects.add(gameObject)
            gameObject.initialize()
        }
        pendingAdditions.clear()
        
        // Видалити об'єкти
        pendingRemovals.forEach { gameObject ->
            gameObjects.remove(gameObject)
            gameObject.destroy()
        }
        pendingRemovals.clear()
    }
    
    /**
     * внутрішній клас для панелі гри
     */
    private inner class GamePanel : JPanel() {
        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            render(g as Graphics2D)
        }
    }
}

/**
 * представлення базової реалізації ігрового об'єкта
 */
abstract class BaseGameObject(
    private var name: String,
    private var position: Vector2D,
    private var size: Vector2D
) : GameObject {
    
    private val tags = mutableSetOf<String>()
    
    override fun getName(): String = name
    
    override fun getPosition(): Vector2D = position.copy()
    
    override fun setPosition(position: Vector2D) {
        this.position.set(position)
    }
    
    override fun getSize(): Vector2D = size.copy()
    
    override fun setSize(size: Vector2D) {
        this.size.set(size)
    }
    
    override fun getTags(): Set<String> = tags.toSet()
    
    override fun addTag(tag: String) {
        tags.add(tag)
    }
    
    override fun removeTag(tag: String) {
        tags.remove(tag)
    }
    
    override fun hasTag(tag: String): Boolean = tags.contains(tag)
    
    override fun initialize() {
        // Базова ініціалізація
    }
    
    override fun update(deltaTime: Double) {
        // Базове оновлення
    }
    
    override fun render(graphics: Graphics2D) {
        // Базове відмалювання
    }
    
    override fun destroy() {
        // Базове знищення
    }
}

/**
 * представлення інтерфейсу для компонентів ігрових об'єктів
 */
interface Component {
    /**
     * ініціалізувати компонент
     */
    fun initialize()
    
    /**
     * оновити компонент
     *
     * @param deltaTime час, що минув
     */
    fun update(deltaTime: Double)
    
    /**
     * відмалювати компонент
     *
     * @param graphics графічний контекст
     */
    fun render(graphics: Graphics2D)
    
    /**
     * знищити компонент
     */
    fun destroy()
}

/**
 * представлення компонента трансформації
 */
class TransformComponent(
    private val position: Vector2D = Vector2D(0.0, 0.0),
    private val rotation: Double = 0.0,
    private val scale: Vector2D = Vector2D(1.0, 1.0)
) : Component {
    
    override fun initialize() {
        // Ініціалізація трансформації
    }
    
    override fun update(deltaTime: Double) {
        // Оновлення трансформації
    }
    
    override fun render(graphics: Graphics2D) {
        // Застосування трансформації до графічного контексту
    }
    
    override fun destroy() {
        // Знищення трансформації
    }
    
    /**
     * отримати позицію
     *
     * @return позиція
     */
    fun getPosition(): Vector2D = position.copy()
    
    /**
     * встановити позицію
     *
     * @param position позиція
     */
    fun setPosition(position: Vector2D) {
        this.position.set(position)
    }
    
    /**
     * отримати обертання
     *
     * @return обертання
     */
    fun getRotation(): Double = rotation
    
    /**
     * встановити обертання
     *
     * @param rotation обертання
     */
    fun setRotation(rotation: Double) {
        this.rotation = rotation
    }
    
    /**
     * отримати масштаб
     *
     * @return масштаб
     */
    fun getScale(): Vector2D = scale.copy()
    
    /**
     * встановити масштаб
     *
     * @param scale масштаб
     */
    fun setScale(scale: Vector2D) {
        this.scale.set(scale)
    }
}

/**
 * представлення компонента спрайта
 */
class SpriteComponent(private val imagePath: String) : Component {
    private var image: Image? = null
    
    override fun initialize() {
        try {
            image = Toolkit.getDefaultToolkit().getImage(imagePath)
        } catch (e: Exception) {
            // Обробка помилки завантаження зображення
        }
    }
    
    override fun update(deltaTime: Double) {
        // Оновлення спрайта
    }
    
    override fun render(graphics: Graphics2D) {
        image?.let { img ->
            // Відмалювання спрайта
        }
    }
    
    override fun destroy() {
        // Знищення спрайта
        image = null
    }
    
    /**
     * встановити зображення
     *
     * @param image зображення
     */
    fun setImage(image: Image) {
        this.image = image
    }
    
    /**
     * отримати зображення
     *
     * @return зображення
     */
    fun getImage(): Image? = image
}

/**
 * представлення компонента фізики
 */
class PhysicsComponent(
    private val velocity: Vector2D = Vector2D(0.0, 0.0),
    private val acceleration: Vector2D = Vector2D(0.0, 0.0),
    private val mass: Double = 1.0,
    private val friction: Double = 0.98
) : Component {
    
    override fun initialize() {
        // Ініціалізація фізики
    }
    
    override fun update(deltaTime: Double) {
        // Оновлення швидкості
        velocity.set(velocity.add(acceleration.multiply(deltaTime)))
        
        // Застосування тертя
        velocity.set(velocity.multiply(friction))
        
        // Оновлення прискорення
        acceleration.set(Vector2D.ZERO)
    }
    
    override fun render(graphics: Graphics2D) {
        // Відмалювання фізичних властивостей (для налагодження)
    }
    
    override fun destroy() {
        // Знищення фізики
    }
    
    /**
     * застосувати силу
     *
     * @param force сила
     */
    fun applyForce(force: Vector2D) {
        acceleration.set(acceleration.add(force.divide(mass)))
    }
    
    /**
     * отримати швидкість
     *
     * @return швидкість
     */
    fun getVelocity(): Vector2D = velocity.copy()
    
    /**
     * встановити швидкість
     *
     * @param velocity швидкість
     */
    fun setVelocity(velocity: Vector2D) {
        this.velocity.set(velocity)
    }
    
    /**
     * отримати масу
     *
     * @return маса
     */
    fun getMass(): Double = mass
    
    /**
     * встановити масу
     *
     * @param mass маса
     */
    fun setMass(mass: Double) {
        require(mass > 0) { "Маса має бути більше 0" }
        // Оновлення маси впливає на існуючу фізику
    }
}

/**
 * представлення інтерфейсу для системи введення