/**
 * фреймворк для математики
 *
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import kotlin.math.*
import java.util.*
import java.math.*

/**
 * представлення інтерфейсу для комплексних чисел
 */
interface ComplexNumber {
    /**
     * отримати дійсну частину
     *
     * @return дійсна частина
     */
    fun getReal(): Double
    
    /**
     * отримати уявну частину
     *
     * @return уявна частина
     */
    fun getImaginary(): Double
    
    /**
     * додати комплексне число
     *
     * @param other інше комплексне число
     * @return результат
     */
    fun add(other: ComplexNumber): ComplexNumber
    
    /**
     * відняти комплексне число
     *
     * @param other інше комплексне число
     * @return результат
     */
    fun subtract(other: ComplexNumber): ComplexNumber
    
    /**
     * помножити на комплексне число
     *
     * @param other інше комплексне число
     * @return результат
     */
    fun multiply(other: ComplexNumber): ComplexNumber
    
    /**
     * поділити на комплексне число
     *
     * @param other інше комплексне число
     * @return результат
     */
    fun divide(other: ComplexNumber): ComplexNumber
    
    /**
     * обчислити модуль
     *
     * @return модуль
     */
    fun modulus(): Double
    
    /**
     * обчислити аргумент
     *
     * @return аргумент
     */
    fun argument(): Double
    
    /**
     * обчислити спряжене число
     *
     * @return спряжене число
     */
    fun conjugate(): ComplexNumber
    
    /**
     * обчислити експоненту
     *
     * @return експонента
     */
    fun exp(): ComplexNumber
    
    /**
     * обчислити логарифм
     *
     * @return логарифм
     */
    fun log(): ComplexNumber
    
    /**
     * обчислити степінь
     *
     * @param power степінь
     * @return результат
     */
    fun pow(power: Double): ComplexNumber
    
    /**
     * обчислити квадратний корінь
     *
     * @return квадратний корінь
     */
    fun sqrt(): ComplexNumber
}

/**
 * представлення базової реалізації комплексного числа
 */
data class BaseComplexNumber(private val real: Double, private val imaginary: Double) : ComplexNumber {
    
    override fun getReal(): Double = real
    
    override fun getImaginary(): Double = imaginary
    
    override fun add(other: ComplexNumber): ComplexNumber {
        return BaseComplexNumber(
            real + other.getReal(),
            imaginary + other.getImaginary()
        )
    }
    
    override fun subtract(other: ComplexNumber): ComplexNumber {
        return BaseComplexNumber(
            real - other.getReal(),
            imaginary - other.getImaginary()
        )
    }
    
    override fun multiply(other: ComplexNumber): ComplexNumber {
        val otherReal = other.getReal()
        val otherImaginary = other.getImaginary()
        return BaseComplexNumber(
            real * otherReal - imaginary * otherImaginary,
            real * otherImaginary + imaginary * otherReal
        )
    }
    
    override fun divide(other: ComplexNumber): ComplexNumber {
        val otherReal = other.getReal()
        val otherImaginary = other.getImaginary()
        val denominator = otherReal * otherReal + otherImaginary * otherImaginary
        if (denominator == 0.0) throw ArithmeticException("Ділення на нуль")
        
        return BaseComplexNumber(
            (real * otherReal + imaginary * otherImaginary) / denominator,
            (imaginary * otherReal - real * otherImaginary) / denominator
        )
    }
    
    override fun modulus(): Double {
        return sqrt(real * real + imaginary * imaginary)
    }
    
    override fun argument(): Double {
        return atan2(imaginary, real)
    }
    
    override fun conjugate(): ComplexNumber {
        return BaseComplexNumber(real, -imaginary)
    }
    
    override fun exp(): ComplexNumber {
        val expReal = exp(real) * cos(imaginary)
        val expImaginary = exp(real) * sin(imaginary)
        return BaseComplexNumber(expReal, expImaginary)
    }
    
    override fun log(): ComplexNumber {
        val modulus = modulus()
        if (modulus == 0.0) throw ArithmeticException("Логарифм нуля не визначений")
        val logReal = ln(modulus)
        val logImaginary = argument()
        return BaseComplexNumber(logReal, logImaginary)
    }
    
    override fun pow(power: Double): ComplexNumber {
        // z^p = e^(p * ln(z))
        val log = log()
        val logTimesPower = BaseComplexNumber(
            log.getReal() * power,
            log.getImaginary() * power
        )
        return logTimesPower.exp()
    }
    
    override fun sqrt(): ComplexNumber {
        return pow(0.5)
    }
    
    override fun toString(): String {
        return if (imaginary >= 0) {
            "$real + ${imaginary}i"
        } else {
            "$real - ${-imaginary}i"
        }
    }
    
    companion object {
        /**
         * нульове комплексне число
         */
        val ZERO = BaseComplexNumber(0.0, 0.0)
        
        /**
         * одиничне комплексне число
         */
        val ONE = BaseComplexNumber(1.0, 0.0)
        
        /**
         * уявна одиниця
         */
        val I = BaseComplexNumber(0.0, 1.0)
        
        /**
         * створити комплексне число з дійсної частини
         *
         * @param real дійсна частина
         * @return комплексне число
         */
        fun fromReal(real: Double): ComplexNumber {
            return BaseComplexNumber(real, 0.0)
        }
        
        /**
         * створити комплексне число з уявної частини
         *
         * @param imaginary уявна частина
         * @return комплексне число
         */
        fun fromImaginary(imaginary: Double): ComplexNumber {
            return BaseComplexNumber(0.0, imaginary)
        }
        
        /**
         * створити комплексне число з полярних координат
         *
         * @param modulus модуль
         * @param argument аргумент
         * @return комплексне число
         */
        fun fromPolar(modulus: Double, argument: Double): ComplexNumber {
            val real = modulus * cos(argument)
            val imaginary = modulus * sin(argument)
            return BaseComplexNumber(real, imaginary)
        }
    }
}

/**
 * представлення інтерфейсу для кватерніонів
 */
interface Quaternion {
    /**
     * отримати скалярну частину
     *
     * @return скалярна частина
     */
    fun getScalar(): Double
    
    /**
     * отримати векторну частину
     *
     * @return векторна частина
     */
    fun getVector(): Vector3D
    
    /**
     * додати кватерніон
     *
     * @param other інший кватерніон
     * @return результат
     */
    fun add(other: Quaternion): Quaternion
    
    /**
     * відняти кватерніон
     *
     * @param other інший кватерніон
     * @return результат
     */
    fun subtract(other: Quaternion): Quaternion
    
    /**
     * помножити на кватерніон
     *
     * @param other інший кватерніон
     * @return результат
     */
    fun multiply(other: Quaternion): Quaternion
    
    /**
     * обчислити спряжений кватерніон
     *
     * @return спряжений кватерніон
     */
    fun conjugate(): Quaternion
    
    /**
     * обчислити норму
     *
     * @return норма
     */
    fun norm(): Double
    
    /**
     * нормалізувати кватерніон
     *
     * @return нормалізований кватерніон
     */
    fun normalize(): Quaternion
    
    /**
     * обчислити обернений кватерніон
     *
     * @return обернений кватерніон
     */
    fun inverse(): Quaternion
    
    /**
     * обчислити експоненту
     *
     * @return експонента
     */
    fun exp(): Quaternion
    
    /**
     * обчислити логарифм
     *
     * @return логарифм
     */
    fun log(): Quaternion
}

/**
 * представлення базової реалізації кватерніона
 */
data class BaseQuaternion(
    private val scalar: Double,
    private val vector: Vector3D
) : Quaternion {
    
    constructor(w: Double, x: Double, y: Double, z: Double) : this(w, Vector3D(x, y, z))
    
    override fun getScalar(): Double = scalar
    
    override fun getVector(): Vector3D = vector.copy()
    
    override fun add(other: Quaternion): Quaternion {
        return BaseQuaternion(
            scalar + other.getScalar(),
            vector.add(other.getVector())
        )
    }
    
    override fun subtract(other: Quaternion): Quaternion {
        return BaseQuaternion(
            scalar - other.getScalar(),
            vector.subtract(other.getVector())
        )
    }
    
    override fun multiply(other: Quaternion): Quaternion {
        val otherScalar = other.getScalar()
        val otherVector = other.getVector()
        
        val newScalar = scalar * otherScalar - vector.dot(otherVector)
        val newVector = vector.multiply(otherScalar)
            .add(otherVector.multiply(scalar))
            .add(vector.cross(otherVector))
        
        return BaseQuaternion(newScalar, newVector)
    }
    
    override fun conjugate(): Quaternion {
        return BaseQuaternion(scalar, vector.multiply(-1.0))
    }
    
    override fun norm(): Double {
        return sqrt(scalar * scalar + vector.dot(vector))
    }
    
    override fun normalize(): Quaternion {
        val norm = norm()
        if (norm == 0.0) throw ArithmeticException("Нормалізація нульового кватерніона")
        return BaseQuaternion(scalar / norm, vector.divide(norm))
    }
    
    override fun inverse(): Quaternion {
        val normSquared = scalar * scalar + vector.dot(vector)
        if (normSquared == 0.0) throw ArithmeticException("Обернений кватерніон нуля не визначений")
        val conjugate = conjugate()
        return BaseQuaternion(
            conjugate.getScalar() / normSquared,
            conjugate.getVector().divide(normSquared)
        )
    }
    
    override fun exp(): Quaternion {
        val vectorNorm = vector.magnitude()
        if (vectorNorm == 0.0) {
            return BaseQuaternion(exp(scalar), Vector3D.ZERO)
        }
        
        val expScalar = exp(scalar)
        val sinVectorNorm = sin(vectorNorm)
        val cosVectorNorm = cos(vectorNorm)
        
        val newScalar = expScalar * cosVectorNorm
        val newVector = vector.normalize().multiply(expScalar * sinVectorNorm)
        
        return BaseQuaternion(newScalar, newVector)
    }
    
    override fun log(): Quaternion {
        val norm = norm()
        if (norm == 0.0) throw ArithmeticException("Логарифм нульового кватерніона не визначений")
        
        val vectorNorm = vector.magnitude()
        if (vectorNorm == 0.0) {
            return BaseQuaternion(ln(norm), Vector3D.ZERO)
        }
        
        val newScalar = ln(norm)
        val vectorArg = acos(scalar / norm) / vectorNorm
        val newVector = vector.multiply(vectorArg)
        
        return BaseQuaternion(newScalar, newVector)
    }
    
    override fun toString(): String {
        return "(${scalar}, ${vector.x}, ${vector.y}, ${vector.z})"
    }
    
    companion object {
        /**
         * нульовий кватерніон
         */
        val ZERO = BaseQuaternion(0.0, Vector3D.ZERO)
        
        /**
         * одиничний кватерніон
         */
        val ONE = BaseQuaternion(1.0, Vector3D.ZERO)
        
        /**
         * створити кватерніон з кута повороту та осі
         *
         * @param angle кут повороту в радіанах
         * @param axis вісь повороту
         * @return кватерніон
         */
        fun fromAngleAxis(angle: Double, axis: Vector3D): Quaternion {
            val normalizedAxis = axis.normalize()
            val halfAngle = angle / 2.0
            val sinHalfAngle = sin(halfAngle)
            val cosHalfAngle = cos(halfAngle)
            
            return BaseQuaternion(
                cosHalfAngle,
                normalizedAxis.multiply(sinHalfAngle)
            )
        }
    }
}

/**
 * представлення інтерфейсу для матриць
 */
interface Matrix {
    /**
     * отримати кількість рядків
     *
     * @return кількість рядків
     */
    fun getRows(): Int
    
    /**
     * отримати кількість стовпців
     *
     * @return кількість стовпців
     */
    fun getCols(): Int
    
    /**
     * отримати елемент
     *
     * @param row рядок
     * @param col стовпець
     * @return елемент
     */
    fun get(row: Int, col: Int): Double
    
    /**
     * встановити елемент
     *
     * @param row рядок
     * @param col стовпець
     * @param value значення
     */
    fun set(row: Int, col: Int, value: Double)
    
    /**
     * додати матрицю
     *
     * @param other інша матриця
     * @return результат
     */
    fun add(other: Matrix): Matrix
    
    /**
     * відняти матрицю
     *
     * @param other інша матриця
     * @return результат
     */
    fun subtract(other: Matrix): Matrix
    
    /**
     * помножити на матрицю
     *
     * @param other інша матриця
     * @return результат
     */
    fun multiply(other: Matrix): Matrix
    
    /**
     * помножити на скаляр
     *
     * @param scalar скаляр
     * @return результат
     */
    fun multiply(scalar: Double): Matrix
    
    /**
     * транспонувати матрицю
     *
     * @return транспонована матриця
     */
    fun transpose(): Matrix
    
    /**
     * обчислити визначник
     *
     * @return визначник
     */
    fun determinant(): Double
    
    /**
     * обчислити обернену матрицю
     *
     * @return обернена матриця
     */
    fun inverse(): Matrix
    
    /**
     * обчислити слід матриці
     *
     * @return слід
     */
    fun trace(): Double
}

/**
 * представлення базової реалізації матриці
 */
class BaseMatrix(private val rows: Int, private val cols: Int) : Matrix {
    private val data = Array(rows) { DoubleArray(cols) }
    
    constructor(data: Array<DoubleArray>) : this(data.size, data[0].size) {
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                this.data[i][j] = data[i][j]
            }
        }
    }
    
    override fun getRows(): Int = rows
    
    override fun getCols(): Int = cols
    
    override fun get(row: Int, col: Int): Double {
        require(row >= 0 && row < rows) { "Невірний індекс рядка: $row" }
        require(col >= 0 && col < cols) { "Невірний індекс стовпця: $col" }
        return data[row][col]
    }
    
    override fun set(row: Int, col: Int, value: Double) {
        require(row >= 0 && row < rows) { "Невірний індекс рядка: $row" }
        require(col >= 0 && col < cols) { "Невірний індекс стовпця: $col" }
        data[row][col] = value
    }
    
    override fun add(other: Matrix): Matrix {
        require(rows == other.getRows() && cols == other.getCols()) { "Невідповідні розміри матриць" }
        
        val result = BaseMatrix(rows, cols)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result.set(i, j, get(i, j) + other.get(i, j))
            }
        }
        return result
    }
    
    override fun subtract(other: Matrix): Matrix {
        require(rows == other.getRows() && cols == other.getCols()) { "Невідповідні розміри матриць" }
        
        val result = BaseMatrix(rows, cols)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result.set(i, j, get(i, j) - other.get(i, j))
            }
        }
        return result
    }
    
    override fun multiply(other: Matrix): Matrix {
        require(cols == other.getRows()) { "Невідповідні розміри матриць для множення" }
        
        val result = BaseMatrix(rows, other.getCols())
        for (i in 0 until rows) {
            for (j in 0 until other.getCols()) {
                var sum = 0.0
                for (k in 0 until cols) {
                    sum += get(i, k) * other.get(k, j)
                }
                result.set(i, j, sum)
            }
        }
        return result
    }
    
    override fun multiply(scalar: Double): Matrix {
        val result = BaseMatrix(rows, cols)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result.set(i, j, get(i, j) * scalar)
            }
        }
        return result
    }
    
    override fun transpose(): Matrix {
        val result = BaseMatrix(cols, rows)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result.set(j, i, get(i, j))
            }
        }
        return result
    }
    
    override fun determinant(): Double {
        require(rows == cols) { "Визначник можна обчислити лише для квадратних матриць" }
        
        return when (rows) {
            1 -> get(0, 0)
            2 -> get(0, 0) * get(1, 1) - get(0, 1) * get(1, 0)
            else -> {
                var det = 0.0
                for (j in 0 until cols) {
                    det += get(0, j) * cofactor(0, j)
                }
                det
            }
        }
    }
    
    private fun cofactor(row: Int, col: Int): Double {
        val sign = if ((row + col) % 2 == 0) 1.0 else -1.0
        return sign * minor(row, col)
    }
    
    private fun minor(row: Int, col: Int): Double {
        val subMatrix = createSubMatrix(row, col)
        return subMatrix.determinant()
    }
    
    private fun createSubMatrix(excludeRow: Int, excludeCol: Int): BaseMatrix {
        val subData = Array(rows - 1) { DoubleArray(cols - 1) }
        var subRow = 0
        for (i in 0 until rows) {
            if (i == excludeRow) continue
            var subCol = 0
            for (j in 0 until cols) {
                if (j == excludeCol) continue
                subData[subRow][subCol] = get(i, j)
                subCol++
            }
            subRow++
        }
        return BaseMatrix(subData)
    }
    
    override fun inverse(): Matrix {
        val det = determinant()
        if (det == 0.0) throw ArithmeticException("Матриця не має оберненої")
        
        val adjugate = createAdjugateMatrix()
        return adjugate.multiply(1.0 / det)
    }
    
    private fun createAdjugateMatrix(): BaseMatrix {
        val adjugate = BaseMatrix(rows, cols)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                adjugate.set(j, i, cofactor(i, j))
            }
        }
        return adjugate
    }
    
    override fun trace(): Double {
        require(rows == cols) { "Слід можна обчислити лише для квадратних матриць" }
        
        var sum = 0.0
        for (i in 0 until rows) {
            sum += get(i, i)
        }
        return sum
    }
    
    override fun toString(): String {
        val sb = StringBuilder()
        for (i in 0 until rows) {
            sb.append("[")
            for (j in 0 until cols) {
                sb.append(String.format("%10.4f", get(i, j)))
                if (j < cols - 1) sb.append(", ")
            }
            sb.append("]\n")
        }
        return sb.toString()
    }
    
    companion object {
        /**
         * створити одиничну матрицю
         *
         * @param size розмір
         * @return одинична матриця
         */
        fun identity(size: Int): BaseMatrix {
            val matrix = BaseMatrix(size, size)
            for (i in 0 until size) {
                matrix.set(i, i, 1.0)
            }
            return matrix
        }
        
        /**
         * створити нульову матрицю
         *
         * @param rows рядки
         * @param cols стовпці
         * @return нульова матриця
         */
        fun zero(rows: Int, cols: Int): BaseMatrix {
            return BaseMatrix(rows, cols)
        }
        
        /**
         * створити діагональну матрицю
         *
         * @param diagonal елементи діагоналі
         * @return діагональна матриця
         */
        fun diagonal(diagonal: DoubleArray): BaseMatrix {
            val matrix = BaseMatrix(diagonal.size, diagonal.size)
            for (i in diagonal.indices) {
                matrix.set(i, i, diagonal[i])
            }
            return matrix
        }
    }
}

/**
 * представлення інтерфейсу для статистичних обчислень