/**
 * Фреймворк для робототехніки
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
import kotlin.math.*
import java.awt.geom.Point2D

/**
 * представлення інтерфейсу для роботи з роботом
 */
interface Robot {
    /**
     * ініціалізувати робота
     *
     * @param config конфігурація
     */
    fun initialize(config: RobotConfig)

    /**
     * перемістити робота
     *
     * @param position нова позиція
     * @param rotation нове обертання
     */
    fun move(position: Point2D.Double, rotation: Double)

    /**
     * отримати поточну позицію
     *
     * @return позиція
     */
    fun getPosition(): Point2D.Double

    /**
     * отримати поточне обертання
     *
     * @return обертання
     */
    fun getRotation(): Double

    /**
     * виконати дію
     *
     * @param action дія
     * @param parameters параметри
     * @return результат
     */
    fun executeAction(action: String, parameters: Map<String, Any>): ActionResult

    /**
     * отримати статус робота
     *
     * @return статус
     */
    fun getStatus(): RobotStatus

    /**
     * встановити швидкість
     *
     * @param linear лінійна швидкість
     * @param angular кутова швидкість
     */
    fun setVelocity(linear: Double, angular: Double)

    /**
     * отримати сенсорні дані
     *
     * @return дані сенсорів
     */
    fun getSensorData(): Map<String, Any>

    /**
     * додати завдання
     *
     * @param task завдання
     */
    fun addTask(task: RobotTask)

    /**
     * отримати список завдань
     *
     * @return список завдань
     */
    fun getTasks(): List<RobotTask>
}

/**
 * представлення конфігурації робота
 */
data class RobotConfig(
    val id: String,
    val model: String,
    val dimensions: RobotDimensions,
    val sensors: List<SensorConfig>,
    val actuators: List<ActuatorConfig>,
    val maxSpeed: Double,
    val maxAngularSpeed: Double
)

/**
 * представлення розмірів робота
 */
data class RobotDimensions(
    val length: Double,
    val width: Double,
    val height: Double
)

/**
 * представлення конфігурації сенсора
 */
data class SensorConfig(
    val id: String,
    val type: String,
    val position: Point2D.Double,
    val orientation: Double,
    val range: Double,
    val accuracy: Double
)

/**
 * представлення конфігурації актуатора
 */
data class ActuatorConfig(
    val id: String,
    val type: String,
    val position: Point2D.Double,
    val maxForce: Double,
    val maxSpeed: Double
)

/**
 * представлення результату дії
 */
data class ActionResult(
    val success: Boolean,
    val message: String,
    val data: Map<String, Any>?
)

/**
 * представлення статусу робота
 */
data class RobotStatus(
    val isOperational: Boolean,
    val batteryLevel: Double,
    val temperature: Double,
    val errorCode: Int,
    val errorMessage: String?
)

/**
 * представлення завдання робота
 */
data class RobotTask(
    val id: String,
    val name: String,
    val priority: Int,
    val status: TaskStatus,
    val parameters: Map<String, Any>,
    val startTime: Long,
    val estimatedDuration: Long
)

/**
 * представлення статусу завдання
 */
enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * представлення базової реалізації робота