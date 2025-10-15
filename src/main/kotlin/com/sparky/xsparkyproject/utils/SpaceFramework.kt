/**
 * Фреймворк для космічних технологій
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
import kotlin.math.*
import java.time.LocalDateTime

/**
 * представлення інтерфейсу для роботи з орбітальними розрахунками
 */
interface OrbitalCalculator {
    /**
     * обчислити орбітальні параметри
     *
     * @param position позиція
     * @param velocity швидкість
     * @param centralBodyMass маса центрального тіла
     * @return орбітальні параметри
     */
    fun calculateOrbitalParameters(position: Vector3D, velocity: Vector3D, centralBodyMass: Double): OrbitalParameters

    /**
     * обчислити позицію на орбіті
     *
     * @param parameters орбітальні параметри
     * @param time час
     * @return позиція
     */
    fun calculatePositionOnOrbit(parameters: OrbitalParameters, time: Double): Vector3D

    /**
     * обчислити дельта-V
     *
     * @param initialVelocity початкова швидкість
     * @param finalVelocity кінцева швидкість
     * @return дельта-V
     */
    fun calculateDeltaV(initialVelocity: Vector3D, finalVelocity: Vector3D): Double

    /**
     * обчислити період орбіти
     *
     * @param semiMajorAxis велика піввісь
     * @param centralBodyMass маса центрального тіла
     * @return період
     */
    fun calculateOrbitalPeriod(semiMajorAxis: Double, centralBodyMass: Double): Double

    /**
     * обчислити енергію орбіти
     *
     * @param semiMajorAxis велика піввісь
     * @param mass маса об'єкта
     * @param centralBodyMass маса центрального тіла
     * @return енергія
     */
    fun calculateOrbitalEnergy(semiMajorAxis: Double, mass: Double, centralBodyMass: Double): Double

    /**
     * обчислити радіус орбіти в заданому відстані
     *
     * @param semiMajorAxis велика піввісь
     * @param eccentricity ексцентриситет
     * @param trueAnomaly справжня аномалія
     * @return радіус
     */
    fun calculateOrbitalRadius(semiMajorAxis: Double, eccentricity: Double, trueAnomaly: Double): Double

    /**
     * обчислити швидкість на орбіті
     *
     * @param radius радіус
     * @param semiMajorAxis велика піввісь
     * @param centralBodyMass маса центрального тіла
     * @return швидкість
     */
    fun calculateOrbitalVelocity(radius: Double, semiMajorAxis: Double, centralBodyMass: Double): Double
}

/**
 * представлення вектора 3D
 */
data class Vector3D(val x: Double, val y: Double, val z: Double) {
    /**
     * додати вектор
     *
     * @param other інший вектор
     * @return сума
     */
    fun add(other: Vector3D): Vector3D {
        return Vector3D(x + other.x, y + other.y, z + other.z)
    }

    /**
     * відняти вектор
     *
     * @param other інший вектор
     * @return різниця
     */
    fun subtract(other: Vector3D): Vector3D {
        return Vector3D(x - other.x, y - other.y, z - other.z)
    }

    /**
     * помножити на скаляр
     *
     * @param scalar скаляр
     * @return добуток
     */
    fun multiply(scalar: Double): Vector3D {
        return Vector3D(x * scalar, y * scalar, z * scalar)
    }

    /**
     * отримати довжину вектора
     *
     * @return довжина
     */
    fun magnitude(): Double {
        return sqrt(x * x + y * y + z * z)
    }

    /**
     * нормалізувати вектор
     *
     * @return нормалізований вектор
     */
    fun normalize(): Vector3D {
        val mag = magnitude()
        return if (mag > 0) {
            Vector3D(x / mag, y / mag, z / mag)
        } else {
            Vector3D(0.0, 0.0, 0.0)
        }
    }

    /**
     * обчислити скалярний добуток
     *
     * @param other інший вектор
     * @return скалярний добуток
     */
    fun dot(other: Vector3D): Double {
        return x * other.x + y * other.y + z * other.z
    }

    /**
     * обчислити векторний добуток
     *
     * @param other інший вектор
     * @return векторний добуток
     */
    fun cross(other: Vector3D): Vector3D {
        return Vector3D(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    override fun toString(): String {
        return "($x, $y, $z)"
    }
}

/**
 * представлення орбітальних параметрів
 */
data class OrbitalParameters(
    val semiMajorAxis: Double,
    val eccentricity: Double,
    val inclination: Double,
    val longitudeOfAscendingNode: Double,
    val argumentOfPeriapsis: Double,
    val trueAnomaly: Double,
    val period: Double,
    val energy: Double
)

/**
 * представлення космічного апарату
 */
data class Spacecraft(
    val id: String,
    val name: String,
    val mass: Double,
    val dimensions: SpacecraftDimensions,
    val propulsionSystem: PropulsionSystem,
    val powerSystem: PowerSystem,
    val communicationSystem: CommunicationSystem,
    val currentPosition: Vector3D,
    val currentVelocity: Vector3D,
    val status: SpacecraftStatus
)

/**
 * представлення розмірів космічного апарату
 */
data class SpacecraftDimensions(
    val length: Double,
    val width: Double,
    val height: Double
)

/**
 * представлення двигуна
 */
data class PropulsionSystem(
    val type: PropulsionType,
    val thrust: Double,
    val isp: Double,
    val fuelCapacity: Double,
    val currentFuel: Double
)

/**
 * представлення типу двигуна
 */
enum class PropulsionType {
    CHEMICAL,
    ION,
    NUCLEAR,
    SOLAR_SAIL
}

/**
 * представлення енергетичної системи
 */
data class PowerSystem(
    val type: PowerType,
    val powerOutput: Double,
    val batteryCapacity: Double,
    val currentCharge: Double
)

/**
 * представлення типу енергетичної системи
 */
enum class PowerType {
    SOLAR,
    NUCLEAR,
    BATTERY
}

/**
 * представлення системи зв'язку
 */
data class CommunicationSystem(
    val frequency: Double,
    val bandwidth: Double,
    val maxRange: Double,
    val dataRate: Double
)

/**
 * представлення статусу космічного апарату
 */
enum class SpacecraftStatus {
    OPERATIONAL,
    DEGRADED,
    FAILED,
    OFFLINE
}

/**
 * представлення базової реалізації орбітального калькулятора