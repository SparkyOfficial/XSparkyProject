/**
 * фреймворк для фізики
 *
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import kotlin.math.*

/**
 * представлення інтерфейсу для фізичного тіла
 */
interface PhysicalBody {
    /**
     * отримати позицію
     *
     * @return позиція
     */
    fun getPosition(): Vector3D
    
    /**
     * встановити позицію
     *
     * @param position позиція
     */
    fun setPosition(position: Vector3D)
    
    /**
     * отримати швидкість
     *
     * @return швидкість
     */
    fun getVelocity(): Vector3D
    
    /**
     * встановити швидкість
     *
     * @param velocity швидкість
     */
    fun setVelocity(velocity: Vector3D)
    
    /**
     * отримати масу
     *
     * @return маса
     */
    fun getMass(): Double
    
    /**
     * встановити масу
     *
     * @param mass маса
     */
    fun setMass(mass: Double)
    
    /**
     * отримати прискорення
     *
     * @return прискорення
     */
    fun getAcceleration(): Vector3D
    
    /**
     * встановити прискорення
     *
     * @param acceleration прискорення
     */
    fun setAcceleration(acceleration: Vector3D)
    
    /**
     * застосувати силу
     *
     * @param force сила
     */
    fun applyForce(force: Vector3D)
    
    /**
     * оновити фізичне тіло
     *
     * @param deltaTime час, що минув
     */
    fun update(deltaTime: Double)
    
    /**
     * перевірити колізію з іншим тілом
     *
     * @param other інше тіло
     * @return true, якщо є колізія
     */
    fun checkCollision(other: PhysicalBody): Boolean
    
    /**
     * обробити колізію з іншим тілом
     *
     * @param other інше тіло
     */
    fun handleCollision(other: PhysicalBody)
}

/**
 * представлення вектора 3D
 *
 * @property x координата x
 * @property y координата y
 * @property z координата z
 */
data class Vector3D(var x: Double, var y: Double, var z: Double) {
    /**
     * додати вектор
     *
     * @param other інший вектор
     * @return новий вектор
     */
    fun add(other: Vector3D): Vector3D {
        return Vector3D(x + other.x, y + other.y, z + other.z)
    }
    
    /**
     * відняти вектор
     *
     * @param other інший вектор
     * @return новий вектор
     */
    fun subtract(other: Vector3D): Vector3D {
        return Vector3D(x - other.x, y - other.y, z - other.z)
    }
    
    /**
     * помножити на скаляр
     *
     * @param scalar скаляр
     * @return новий вектор
     */
    fun multiply(scalar: Double): Vector3D {
        return Vector3D(x * scalar, y * scalar, z * scalar)
    }
    
    /**
     * поділити на скаляр
     *
     * @param scalar скаляр
     * @return новий вектор
     */
    fun divide(scalar: Double): Vector3D {
        return Vector3D(x / scalar, y / scalar, z / scalar)
    }
    
    /**
     * обчислити довжину вектора
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
        return if (mag > 0) Vector3D(x / mag, y / mag, z / mag) else Vector3D(0.0, 0.0, 0.0)
    }
    
    /**
     * обчислити скалярний добуток з іншим вектором
     *
     * @param other інший вектор
     * @return скалярний добуток
     */
    fun dot(other: Vector3D): Double {
        return x * other.x + y * other.y + z * other.z
    }
    
    /**
     * обчислити векторний добуток з іншим вектором
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
    
    /**
     * обчислити відстань до іншого вектора
     *
     * @param other інший вектор
     * @return відстань
     */
    fun distanceTo(other: Vector3D): Double {
        return subtract(other).magnitude()
    }
    
    /**
     * змінити вектор
     *
     * @param other інший вектор
     */
    fun set(other: Vector3D) {
        x = other.x
        y = other.y
        z = other.z
    }
    
    /**
     * змінити координати
     *
     * @param x координата x
     * @param y координата y
     * @param z координата z
     */
    fun set(x: Double, y: Double, z: Double) {
        this.x = x
        this.y = y
        this.z = z
    }
    
    /**
     * скопіювати вектор
     *
     * @return копія
     */
    fun copy(): Vector3D {
        return Vector3D(x, y, z)
    }
    
    companion object {
        /**
         * нульовий вектор
         */
        val ZERO = Vector3D(0.0, 0.0, 0.0)
        
        /**
         * одиничний вектор по x
         */
        val UNIT_X = Vector3D(1.0, 0.0, 0.0)
        
        /**
         * одиничний вектор по y
         */
        val UNIT_Y = Vector3D(0.0, 1.0, 0.0)
        
        /**
         * одиничний вектор по z
         */
        val UNIT_Z = Vector3D(0.0, 0.0, 1.0)
    }
}

/**
 * представлення базової реалізації фізичного тіла
 */
open class BasePhysicalBody(
    private var position: Vector3D,
    private var velocity: Vector3D,
    private var mass: Double,
    private var acceleration: Vector3D
) : PhysicalBody {
    
    constructor(position: Vector3D, mass: Double) : this(position, Vector3D.ZERO, mass, Vector3D.ZERO)
    
    override fun getPosition(): Vector3D = position.copy()
    
    override fun setPosition(position: Vector3D) {
        this.position.set(position)
    }
    
    override fun getVelocity(): Vector3D = velocity.copy()
    
    override fun setVelocity(velocity: Vector3D) {
        this.velocity.set(velocity)
    }
    
    override fun getMass(): Double = mass
    
    override fun setMass(mass: Double) {
        require(mass > 0) { "Маса має бути більше 0" }
        this.mass = mass
    }
    
    override fun getAcceleration(): Vector3D = acceleration.copy()
    
    override fun setAcceleration(acceleration: Vector3D) {
        this.acceleration.set(acceleration)
    }
    
    override fun applyForce(force: Vector3D) {
        // F = ma => a = F/m
        val forceAcceleration = force.divide(mass)
        this.acceleration.set(this.acceleration.add(forceAcceleration))
    }
    
    override fun update(deltaTime: Double) {
        // Оновлення швидкості: v = v0 + a * t
        velocity.set(velocity.add(acceleration.multiply(deltaTime)))
        
        // Оновлення позиції: s = s0 + v * t
        position.set(position.add(velocity.multiply(deltaTime)))
        
        // Скидання прискорення (сили)
        acceleration.set(Vector3D.ZERO)
    }
    
    override fun checkCollision(other: PhysicalBody): Boolean {
        // Базова перевірка колізії - перевірка відстані
        val distance = position.distanceTo(other.getPosition())
        val minDistance = getCollisionRadius() + other.getCollisionRadius()
        return distance < minDistance
    }
    
    override fun handleCollision(other: PhysicalBody) {
        // Базова обробка колізії - відбивання
        if (checkCollision(other)) {
            // Обчислення напрямку відбивання
            val collisionNormal = other.getPosition().subtract(position).normalize()
            
            // Відбиття швидкості
            val dotProduct = velocity.dot(collisionNormal)
            val reflection = collisionNormal.multiply(2 * dotProduct)
            velocity.set(velocity.subtract(reflection))
        }
    }
    
    /**
     * отримати радіус для перевірки колізій
     *
     * @return радіус
     */
    protected open fun getCollisionRadius(): Double = 1.0
}

/**
 * представлення інтерфейсу для фізичного світу
 */
interface PhysicsWorld {
    /**
     * додати фізичне тіло
     *
     * @param body тіло
     */
    fun addBody(body: PhysicalBody)
    
    /**
     * видалити фізичне тіло
     *
     * @param body тіло
     */
    fun removeBody(body: PhysicalBody)
    
    /**
     * отримати всі фізичні тіла
     *
     * @return список тіл
     */
    fun getBodies(): List<PhysicalBody>
    
    /**
     * оновити фізичний світ
     *
     * @param deltaTime час, що минув
     */
    fun update(deltaTime: Double)
    
    /**
     * встановити гравітацію
     *
     * @param gravity гравітація
     */
    fun setGravity(gravity: Vector3D)
    
    /**
     * отримати гравітацію
     *
     * @return гравітація
     */
    fun getGravity(): Vector3D
}

/**
 * представлення базової реалізації фізичного світу
 */
class BasePhysicsWorld : PhysicsWorld {
    private val bodies = mutableListOf<PhysicalBody>()
    private var gravity = Vector3D(0.0, -9.81, 0.0) // Земна гравітація
    
    override fun addBody(body: PhysicalBody) {
        bodies.add(body)
    }
    
    override fun removeBody(body: PhysicalBody) {
        bodies.remove(body)
    }
    
    override fun getBodies(): List<PhysicalBody> = bodies.toList()
    
    override fun update(deltaTime: Double) {
        // Застосувати гравітацію до всіх тіл
        bodies.forEach { body ->
            val gravitationalForce = gravity.multiply(body.getMass())
            body.applyForce(gravitationalForce)
        }
        
        // Оновити всі тіла
        bodies.forEach { body ->
            body.update(deltaTime)
        }
        
        // Перевірити та обробити колізії
        for (i in bodies.indices) {
            for (j in i + 1 until bodies.size) {
                val bodyA = bodies[i]
                val bodyB = bodies[j]
                
                if (bodyA.checkCollision(bodyB)) {
                    bodyA.handleCollision(bodyB)
                    bodyB.handleCollision(bodyA)
                }
            }
        }
    }
    
    override fun setGravity(gravity: Vector3D) {
        this.gravity.set(gravity)
    }
    
    override fun getGravity(): Vector3D = gravity.copy()
}

/**
 * представлення тіла зіткнення сфери
 */
class SphereBody(
    position: Vector3D,
    mass: Double,
    private val radius: Double
) : BasePhysicalBody(position, mass) {
    
    override fun getCollisionRadius(): Double = radius
}

/**
 * представлення тіла зіткнення коробки
 */
class BoxBody(
    position: Vector3D,
    mass: Double,
    private val size: Vector3D
) : BasePhysicalBody(position, mass) {
    
    override fun getCollisionRadius(): Double = size.magnitude() / 2.0
}

/**
 * представлення інтерфейсу для фізичних матеріалів
 */
interface PhysicsMaterial {
    /**
     * отримати коефіцієнт тертя
     *
     * @return коефіцієнт тертя
     */
    fun getFriction(): Double
    
    /**
     * отримати коефіцієнт відбиття
     *
     * @return коефіцієнт відбиття
     */
    fun getRestitution(): Double
    
    /**
     * отримати густину
     *
     * @return густина
     */
    fun getDensity(): Double
}

/**
 * представлення базової реалізації фізичного матеріалу
 */
data class BasePhysicsMaterial(
    private val friction: Double,
    private val restitution: Double,
    private val density: Double
) : PhysicsMaterial {
    
    override fun getFriction(): Double = friction
    
    override fun getRestitution(): Double = restitution
    
    override fun getDensity(): Double = density
    
    companion object {
        /**
         * матеріал для сталі
         */
        val STEEL = BasePhysicsMaterial(0.74, 0.8, 7850.0)
        
        /**
         * матеріал для алюмінію
         */
        val ALUMINUM = BasePhysicsMaterial(0.61, 0.8, 2700.0)
        
        /**
         * матеріал для деревини
         */
        val WOOD = BasePhysicsMaterial(0.4, 0.6, 700.0)
        
        /**
         * матеріал для гуми
         */
        val RUBBER = BasePhysicsMaterial(1.0, 0.9, 1500.0)
    }
}

/**
 * представлення інтерфейсу для обмежень
 */
interface Constraint {
    /**
     * застосувати обмеження
     *
     * @param deltaTime час, що минув
     */
    fun apply(deltaTime: Double)
}

/**
 * представлення пружинного обмеження
 *
 * @property bodyA перше тіло
 * @property bodyB друге тіло
 * @property restLength довжина спокою
 * @property stiffness жорсткість
 * @property damping загасання
 */
class SpringConstraint(
    private val bodyA: PhysicalBody,
    private val bodyB: PhysicalBody,
    private val restLength: Double,
    private val stiffness: Double,
    private val damping: Double
) : Constraint {
    
    override fun apply(deltaTime: Double) {
        val positionA = bodyA.getPosition()
        val positionB = bodyB.getPosition()
        
        // Обчислити вектор між тілами
        val delta = positionB.subtract(positionA)
        val distance = delta.magnitude()
        
        if (distance > 0) {
            // Нормалізувати вектор
            val direction = delta.normalize()
            
            // Обчислити величину сили
            val displacement = distance - restLength
            val springForce = displacement * stiffness
            
            // Обчислити сили загасання
            val velocityA = bodyA.getVelocity()
            val velocityB = bodyB.getVelocity()
            val relativeVelocity = velocityB.subtract(velocityA)
            val dampingForce = relativeVelocity.dot(direction) * damping
            
            // Загальна сила
            val totalForce = springForce + dampingForce
            
            // Застосувати сили до тіл
            val force = direction.multiply(totalForce)
            bodyA.applyForce(force.multiply(-1.0))
            bodyB.applyForce(force)
        }
    }
}

/**
 * представлення обмеження відстані
 *
 * @property bodyA перше тіло
 * @property bodyB друге тіло
 * @property distance відстань
 */
class DistanceConstraint(
    private val bodyA: PhysicalBody,
    private val bodyB: PhysicalBody,
    private val distance: Double
) : Constraint {
    
    override fun apply(deltaTime: Double) {
        val positionA = bodyA.getPosition()
        val positionB = bodyB.getPosition()
        
        // Обчислити вектор між тілами
        val delta = positionB.subtract(positionA)
        val currentDistance = delta.magnitude()
        
        if (currentDistance > 0) {
            // Обчислити корекцію
            val correction = (currentDistance - distance) / currentDistance
            val halfCorrection = delta.multiply(correction * 0.5)
            
            // Застосувати корекцію до позицій
            bodyA.setPosition(positionA.add(halfCorrection))
            bodyB.setPosition(positionB.subtract(halfCorrection))
        }
    }
}

/**
 * представлення інтерфейсу для детектора колізій