/**
 * xsparkyproject - розширена бібліотека для kotlin
 *
 * Copyright 2025 Андрій Будильников
 */

package com.sparky.xsparkyproject.utils

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.*

/**
 * утилітарний клас для математичних операцій
 *
 * @author андрій будильников
 * @since 1.0.0
 */
@Suppress("unused")
class MathUtils {
    
    companion object {
        const val PI_DOUBLE = 3.141592653589793
        const val E_DOUBLE = 2.718281828459045
        const val GOLDEN_RATIO = 1.618033988749895
        const val SQRT_2 = 1.4142135623730951
        const val SQRT_3 = 1.7320508075688772
        
        // константи для тригонометрії
        const val DEG_TO_RAD = PI_DOUBLE / 180.0
        const val RAD_TO_DEG = 180.0 / PI_DOUBLE
    }
    
    // базові арифметичні операції
    
    /**
     * додає два числа з перевіркою на переповнення
     *
     * @param a перший операнд
     * @param b другий операнд
     * @return сума чисел
     */
    fun safeAdd(a: Int, b: Int): Long {
        return a.toLong() + b.toLong()
    }
    
    /**
     * множить два числа з перевіркою на переповнення
     *
     * @param a перший операнд
     * @param b другий операнд
     * @return добуток чисел
     */
    fun safeMultiply(a: Int, b: Int): Long {
        return a.toLong() * b.toLong()
    }
    
    /**
     * ділить два числа з обробкою ділення на нуль
     *
     * @param dividend ділене
     * @param divisor дільник
     * @return результат ділення або null при діленні на нуль
     */
    fun safeDivide(dividend: Double, divisor: Double): Double? {
        return if (divisor != 0.0) dividend / divisor else null
    }
    
    // операції з числами з плаваючою комою
    
    /**
     * порівнює два числа з плаваючою комою з заданою точністю
     *
     * @param a перше число
     * @param b друге число
     * @param epsilon точність порівняння
     * @return true якщо числа рівні з заданою точністю
     */
    fun doubleEquals(a: Double, b: Double, epsilon: Double = 1e-9): Boolean {
        return abs(a - b) < epsilon
    }
    
    /**
     * округляє число до заданої кількості знаків після коми
     *
     * @param value число для округлення
     * @param decimals кількість знаків після коми
     * @return округлене число
     */
    fun roundToDecimals(value: Double, decimals: Int): Double {
        val multiplier = 10.0.pow(decimals.toDouble())
        return round(value * multiplier) / multiplier
    }
    
    // статистичні функції
    
    /**
     * обчислює середнє арифметичне значення масиву чисел
     *
     * @param numbers масив чисел
     * @return середнє значення
     */
    fun mean(numbers: DoubleArray): Double {
        if (numbers.isEmpty()) return 0.0
        return numbers.sum() / numbers.size
    }
    
    /**
     * обчислює медіану масиву чисел
     *
     * @param numbers масив чисел
     * @return медіана
     */
    fun median(numbers: DoubleArray): Double {
        if (numbers.isEmpty()) return 0.0
        val sorted = numbers.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[middle - 1] + sorted[middle]) / 2.0
        } else {
            sorted[middle].toDouble()
        }
    }
    
    /**
     * обчислює стандартне відхилення масиву чисел
     *
     * @param numbers масив чисел
     * @return стандартне відхилення
     */
    fun standardDeviation(numbers: DoubleArray): Double {
        if (numbers.size <= 1) return 0.0
        val meanValue = mean(numbers)
        val variance = numbers.sumOf { (it - meanValue) * (it - meanValue) } / (numbers.size - 1)
        return sqrt(variance)
    }
    
    /**
     * обчислює дисперсію масиву чисел
     *
     * @param numbers масив чисел
     * @return дисперсія
     */
    fun variance(numbers: DoubleArray): Double {
        if (numbers.size <= 1) return 0.0
        val meanValue = mean(numbers)
        return numbers.sumOf { (it - meanValue) * (it - meanValue) } / (numbers.size - 1)
    }
    
    // тригонометричні функції
    
    /**
     * обчислює синус кута в градусах
     *
     * @param degrees кут в градусах
     * @return синус кута
     */
    fun sinDegrees(degrees: Double): Double {
        return sin(degrees * DEG_TO_RAD)
    }
    
    /**
     * обчислює косинус кута в градусах
     *
     * @param degrees кут в градусах
     * @return косинус кута
     */
    fun cosDegrees(degrees: Double): Double {
        return cos(degrees * DEG_TO_RAD)
    }
    
    /**
     * обчислює тангенс кута в градусах
     *
     * @param degrees кут в градусах
     * @return тангенс кута
     */
    fun tanDegrees(degrees: Double): Double {
        return tan(degrees * DEG_TO_RAD)
    }
    
    // експоненціальні та логарифмічні функції
    
    /**
     * обчислює квадрат числа
     *
     * @param value число
     * @return квадрат числа
     */
    fun square(value: Double): Double {
        return value * value
    }
    
    /**
     * обчислює куб числа
     *
     * @param value число
     * @return куб числа
     */
    fun cube(value: Double): Double {
        return value * value * value
    }
    
    /**
     * обчислює факторіал числа
     *
     * @param n число
     * @return факторіал числа
     */
    fun factorial(n: Int): BigInteger {
        if (n < 0) throw IllegalArgumentException("факторіал визначений лише для невід'ємних цілих чисел")
        var result = BigInteger.ONE
        for (i in 2..n) {
            result = result.multiply(BigInteger.valueOf(i.toLong()))
        }
        return result
    }
    
    /**
     * обчислює натуральній логарифм числа
     *
     * @param value число
     * @return натуральний логарифм
     */
    fun ln(value: Double): Double {
        return ln(value)
    }
    
    /**
     * обчислює логарифм за основою 10
     *
     * @param value число
     * @return логарифм за основою 10
     */
    fun log10(value: Double): Double {
        return log10(value)
    }
    
    /**
     * обчислює логарифм за довільною основою
     *
     * @param value число
     * @param base основа логарифма
     * @return логарифм за основою base
     */
    fun logBase(value: Double, base: Double): Double {
        return ln(value) / ln(base)
    }
    
    // функції для роботи з комплексними числами
    
    /**
     * представлення комплексного числа
     *
     * @property real дійсна частина
     * @property imaginary уявна частина
     */
    data class Complex(val real: Double, val imaginary: Double) {
        
        /**
         * додає два комплексних числа
         *
         * @param other інше комплексне число
         * @return сума комплексних чисел
         */
        operator fun plus(other: Complex): Complex {
            return Complex(real + other.real, imaginary + other.imaginary)
        }
        
        /**
         * віднімає два комплексних числа
         *
         * @param other інше комплексне число
         * @return різниця комплексних чисел
         */
        operator fun minus(other: Complex): Complex {
            return Complex(real - other.real, imaginary - other.imaginary)
        }
        
        /**
         * множить два комплексних числа
         *
         * @param other інше комплексне число
         * @return добуток комплексних чисел
         */
        operator fun times(other: Complex): Complex {
            val newReal = real * other.real - imaginary * other.imaginary
            val newImaginary = real * other.imaginary + imaginary * other.real
            return Complex(newReal, newImaginary)
        }
        
        /**
         * ділить два комплексних числа
         *
         * @param other інше комплексне число
         * @return частка комплексних чисел
         */
        operator fun div(other: Complex): Complex {
            val denominator = other.real * other.real + other.imaginary * other.imaginary
            if (denominator == 0.0) throw ArithmeticException("ділення на нуль")
            
            val newReal = (real * other.real + imaginary * other.imaginary) / denominator
            val newImaginary = (imaginary * other.real - real * other.imaginary) / denominator
            return Complex(newReal, newImaginary)
        }
        
        /**
         * обчислює модуль комплексного числа
         *
         * @return модуль комплексного числа
         */
        fun modulus(): Double {
            return sqrt(real * real + imaginary * imaginary)
        }
        
        /**
         * обчислює аргумент комплексного числа
         *
         * @return аргумент комплексного числа в радіанах
         */
        fun argument(): Double {
            return atan2(imaginary, real)
        }
        
        override fun toString(): String {
            return when {
                imaginary >= 0 -> "$real+$imaginary i"
                else -> "$real${imaginary}i"
            }
        }
    }
    
    /**
     * створює комплексне число
     *
     * @param real дійсна частина
     * @param imaginary уявна частина
     * @return комплексне число
     */
    fun complex(real: Double, imaginary: Double): Complex {
        return Complex(real, imaginary)
    }
    
    // функції для роботи з матрицями
    
    /**
     * представлення матриці
     *
     * @property rows кількість рядків
     * @property cols кількість стовпців
     * @property data двовимірний масив даних
     */
    class Matrix(private val data: Array<DoubleArray>) {
        val rows: Int = data.size
        val cols: Int = if (data.isNotEmpty()) data[0].size else 0
        
        init {
            // перевіряємо, що всі рядки мають однакову довжину
            for (row in data) {
                if (row.size != cols) {
                    throw IllegalArgumentException("всі рядки матриці повинні мати однакову довжину")
                }
            }
        }
        
        /**
         * отримує елемент матриці
         *
         * @param row номер рядка
         * @param col номер стовпця
         * @return значення елемента
         */
        operator fun get(row: Int, col: Int): Double {
            if (row < 0 || row >= rows || col < 0 || col >= cols) {
                throw IndexOutOfBoundsException("індекс поза межами матриці")
            }
            return data[row][col]
        }
        
        /**
         * встановлює значення елемента матриці
         *
         * @param row номер рядка
         * @param col номер стовпця
         * @param value нове значення
         */
        operator fun set(row: Int, col: Int, value: Double) {
            if (row < 0 || row >= rows || col < 0 || col >= cols) {
                throw IndexOutOfBoundsException("індекс поза межами матриці")
            }
            data[row][col] = value
        }
        
        /**
         * додає дві матриці
         *
         * @param other інша матриця
         * @return сума матриць
         */
        operator fun plus(other: Matrix): Matrix {
            if (rows != other.rows || cols != other.cols) {
                throw IllegalArgumentException("матриці повинні мати однакові розміри для додавання")
            }
            
            val resultData = Array(rows) { DoubleArray(cols) }
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    resultData[i][j] = this[i, j] + other[i, j]
                }
            }
            return Matrix(resultData)
        }
        
        /**
         * віднімає дві матриці
         *
         * @param other інша матриця
         * @return різниця матриць
         */
        operator fun minus(other: Matrix): Matrix {
            if (rows != other.rows || cols != other.cols) {
                throw IllegalArgumentException("матриці повинні мати однакові розміри для віднімання")
            }
            
            val resultData = Array(rows) { DoubleArray(cols) }
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    resultData[i][j] = this[i, j] - other[i, j]
                }
            }
            return Matrix(resultData)
        }
        
        /**
         * множить матрицу на скаляр
         *
         * @param scalar скаляр
         * @return добуток матриці на скаляр
         */
        operator fun times(scalar: Double): Matrix {
            val resultData = Array(rows) { DoubleArray(cols) }
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    resultData[i][j] = this[i, j] * scalar
                }
            }
            return Matrix(resultData)
        }
        
        /**
         * множить дві матриці
         *
         * @param other інша матриця
         * @return добуток матриць
         */
        operator fun times(other: Matrix): Matrix {
            if (cols != other.rows) {
                throw IllegalArgumentException("кількість стовпців першої матриці повинна дорівнювати кількості рядків другої матриці")
            }
            
            val resultData = Array(rows) { DoubleArray(other.cols) }
            for (i in 0 until rows) {
                for (j in 0 until other.cols) {
                    var sum = 0.0
                    for (k in 0 until cols) {
                        sum += this[i, k] * other[k, j]
                    }
                    resultData[i][j] = sum
                }
            }
            return Matrix(resultData)
        }
        
        /**
         * транспонує матрицю
         *
         * @return транспонована матриця
         */
        fun transpose(): Matrix {
            val resultData = Array(cols) { DoubleArray(rows) }
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    resultData[j][i] = this[i, j]
                }
            }
            return Matrix(resultData)
        }
        
        /**
         * обчислює детермінант матриці (тільки для квадратних матриць)
         *
         * @return детермінант матриці
         */
        fun determinant(): Double {
            if (rows != cols) {
                throw IllegalArgumentException("детермінант можна обчислити лише для квадратних матриць")
            }
            
            return when (rows) {
                1 -> this[0, 0]
                2 -> this[0, 0] * this[1, 1] - this[0, 1] * this[1, 0]
                else -> {
                    var det = 0.0
                    for (j in 0 until cols) {
                        det += this[0, j] * cofactor(0, j)
                    }
                    det
                }
            }
        }
        
        /**
         * обчислює алгебраїчне доповнення елемента
         *
         * @param row номер рядка
         * @param col номер стовпця
         * @return алгебраїчне доповнення
         */
        private fun cofactor(row: Int, col: Int): Double {
            val sign = if ((row + col) % 2 == 0) 1.0 else -1.0
            return sign * minor(row, col).determinant()
        }
        
        /**
         * обчислює мінор елемента
         *
         * @param row номер рядка
         * @param col номер стовпця
         * @return мінор
         */
        private fun minor(row: Int, col: Int): Matrix {
            val resultData = Array(rows - 1) { DoubleArray(cols - 1) }
            var resultRow = 0
            
            for (i in 0 until rows) {
                if (i == row) continue
                
                var resultCol = 0
                for (j in 0 until cols) {
                    if (j == col) continue
                    
                    resultData[resultRow][resultCol] = this[i, j]
                    resultCol++
                }
                resultRow++
            }
            
            return Matrix(resultData)
        }
        
        /**
         * обчислює обернену матрицю
         *
         * @return обернена матриця
         */
        fun inverse(): Matrix {
            if (rows != cols) {
                throw IllegalArgumentException("обернену матрицю можна обчислити лише для квадратних матриць")
            }
            
            val det = determinant()
            if (det == 0.0) {
                throw ArithmeticException("матриця сингулярна, обернена матриця не існує")
            }
            
            if (rows == 1) {
                return Matrix(arrayOf(doubleArrayOf(1.0 / this[0, 0])))
            }
            
            val resultData = Array(rows) { DoubleArray(cols) }
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    resultData[i][j] = cofactor(j, i) / det
                }
            }
            
            return Matrix(resultData)
        }
        
        /**
         * створює одиничну матрицю заданого розміру
         *
         * @param size розмір матриці
         * @return одинична матриця
         */
        companion object {
            fun identity(size: Int): Matrix {
                val data = Array(size) { DoubleArray(size) }
                for (i in 0 until size) {
                    data[i][i] = 1.0
                }
                return Matrix(data)
            }
            
            /**
             * створює нульову матрицю заданого розміру
             *
             * @param rows кількість рядків
             * @param cols кількість стовпців
             * @return нульова матриця
             */
            fun zero(rows: Int, cols: Int): Matrix {
                return Matrix(Array(rows) { DoubleArray(cols) })
            }
            
            /**
             * створює матрицю з масиву даних
             *
             * @param data двовимірний масив даних
             * @return матриця
             */
            fun of(data: Array<DoubleArray>): Matrix {
                return Matrix(data.copyOf().map { it.copyOf() }.toTypedArray())
            }
        }
        
        override fun toString(): String {
            val sb = StringBuilder()
            for (i in 0 until rows) {
                sb.append("[")
                for (j in 0 until cols) {
                    sb.append(if (j > 0) ", " else "").append(this[i, j])
                }
                sb.append("]\n")
            }
            return sb.toString()
        }
        
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Matrix) return false
            
            if (rows != other.rows) return false
            if (cols != other.cols) return false
            
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    if (this[i, j] != other[i, j]) return false
                }
            }
            
            return true
        }
        
        override fun hashCode(): Int {
            var result = rows
            result = 31 * result + cols
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    result = 31 * result + this[i, j].hashCode()
                }
            }
            return result
        }
    }
    
    // чисельні методи
    
    /**
     * знаходить корінь рівняння методом бісекції
     *
     * @param function функція
     * @param a ліва межа інтервалу
     * @param b права межа інтервалу
     * @param tolerance точність
     * @return корінь рівняння
     */
    fun bisectionMethod(function: (Double) -> Double, a: Double, b: Double, tolerance: Double = 1e-10): Double {
        var left = a
        var right = b
        var fLeft = function(left)
        var fRight = function(right)
        
        if (fLeft * fRight >= 0) {
            throw IllegalArgumentException("функція повинна мати різні знаки на кінцях інтервалу")
        }
        
        while (right - left > tolerance) {
            val mid = (left + right) / 2.0
            val fMid = function(mid)
            
            if (abs(fMid) < tolerance) {
                return mid
            }
            
            if (fLeft * fMid < 0) {
                right = mid
                fRight = fMid
            } else {
                left = mid
                fLeft = fMid
            }
        }
        
        return (left + right) / 2.0
    }
    
    /**
     * знаходить корінь рівняння методом Ньютона
     *
     * @param function функція
     * @param derivative похідна функції
     * @param initialGuess початкове наближення
     * @param tolerance точність
     * @param maxIterations максимальна кількість ітерацій
     * @return корінь рівняння
     */
    fun newtonMethod(
        function: (Double) -> Double,
        derivative: (Double) -> Double,
        initialGuess: Double,
        tolerance: Double = 1e-10,
        maxIterations: Int = 1000
    ): Double {
        var x = initialGuess
        
        for (i in 0 until maxIterations) {
            val fx = function(x)
            if (abs(fx) < tolerance) {
                return x
            }
            
            val dfx = derivative(x)
            if (abs(dfx) < tolerance) {
                throw ArithmeticException("похідна близька до нуля, неможливо продовжити")
            }
            
            val newX = x - fx / dfx
            if (abs(newX - x) < tolerance) {
                return newX
            }
            
            x = newX
        }
        
        throw ArithmeticException("метод не збігся за вказану кількість ітерацій")
    }
    
    /**
     * чисельне інтегрування методом трапецій
     *
     * @param function функція для інтегрування
     * @param a нижня межа інтегрування
     * @param b верхня межа інтегрування
     * @param n кількість інтервалів
     * @return значення інтеграла
     */
    fun trapezoidalRule(function: (Double) -> Double, a: Double, b: Double, n: Int): Double {
        if (n <= 0) throw IllegalArgumentException("кількість інтервалів повинна бути додатною")
        
        val h = (b - a) / n
        var sum = (function(a) + function(b)) / 2.0
        
        for (i in 1 until n) {
            sum += function(a + i * h)
        }
        
        return sum * h
    }
    
    /**
     * чисельне інтегрування методом Сімпсона
     *
     * @param function функція для інтегрування
     * @param a нижня межа інтегрування
     * @param b верхня межа інтегрування
     * @param n кількість інтервалів (повинно бути парним)
     * @return значення інтеграла
     */
    fun simpsonRule(function: (Double) -> Double, a: Double, b: Double, n: Int): Double {
        if (n <= 0 || n % 2 != 0) throw IllegalArgumentException("кількість інтервалів повинна бути додатною і парною")
        
        val h = (b - a) / n
        var sum = function(a) + function(b)
        
        // додаємо члени з непарними індексами
        for (i in 1 until n step 2) {
            sum += 4 * function(a + i * h)
        }
        
        // додаємо члени з парними індексами
        for (i in 2 until n step 2) {
            sum += 2 * function(a + i * h)
        }
        
        return sum * h / 3.0
    }
    
    // функції для роботи з поліномами
    
    /**
     * представлення поліному
     *
     * @property coefficients коефіцієнти поліному (від нульового до найвищого степеня)
     */
    data class Polynomial(val coefficients: DoubleArray) {
        
        val degree: Int = coefficients.size - 1
        
        /**
         * обчислює значення поліному в точці
         *
         * @param x точка
         * @return значення поліному
         */
        fun evaluate(x: Double): Double {
            var result = 0.0
            var power = 1.0
            
            for (coefficient in coefficients) {
                result += coefficient * power
                power *= x
            }
            
            return result
        }
        
        /**
         * обчислює похідну поліному
         *
         * @return похідна поліному
         */
        fun derivative(): Polynomial {
            if (degree <= 0) return Polynomial(doubleArrayOf(0.0))
            
            val derivativeCoefficients = DoubleArray(degree)
            for (i in 1..degree) {
                derivativeCoefficients[i - 1] = coefficients[i] * i
            }
            
            return Polynomial(derivativeCoefficients)
        }
        
        /**
         * додає два поліноми
         *
         * @param other інший поліном
         * @return сума поліномів
         */
        operator fun plus(other: Polynomial): Polynomial {
            val maxSize = maxOf(coefficients.size, other.coefficients.size)
            val resultCoefficients = DoubleArray(maxSize)
            
            for (i in coefficients.indices) {
                resultCoefficients[i] += coefficients[i]
            }
            
            for (i in other.coefficients.indices) {
                resultCoefficients[i] += other.coefficients[i]
            }
            
            return Polynomial(resultCoefficients)
        }
        
        /**
         * віднімає два поліноми
         *
         * @param other інший поліном
         * @return різниця поліномів
         */
        operator fun minus(other: Polynomial): Polynomial {
            val maxSize = maxOf(coefficients.size, other.coefficients.size)
            val resultCoefficients = DoubleArray(maxSize)
            
            for (i in coefficients.indices) {
                resultCoefficients[i] += coefficients[i]
            }
            
            for (i in other.coefficients.indices) {
                resultCoefficients[i] -= other.coefficients[i]
            }
            
            return Polynomial(resultCoefficients)
        }
        
        /**
         * множить поліном на скаляр
         *
         * @param scalar скаляр
         * @return добуток поліному на скаляр
         */
        operator fun times(scalar: Double): Polynomial {
            val resultCoefficients = DoubleArray(coefficients.size)
            for (i in coefficients.indices) {
                resultCoefficients[i] = coefficients[i] * scalar
            }
            return Polynomial(resultCoefficients)
        }
        
        /**
         * множить два поліноми
         *
         * @param other інший поліном
         * @return добуток поліномів
         */
        operator fun times(other: Polynomial): Polynomial {
            val resultDegree = degree + other.degree
            val resultCoefficients = DoubleArray(resultDegree + 1)
            
            for (i in coefficients.indices) {
                for (j in other.coefficients.indices) {
                    resultCoefficients[i + j] += coefficients[i] * other.coefficients[j]
                }
            }
            
            return Polynomial(resultCoefficients)
        }
        
        override fun toString(): String {
            if (coefficients.isEmpty()) return "0"
            
            val sb = StringBuilder()
            var first = true
            
            for (i in coefficients.size - 1 downTo 0) {
                val coeff = coefficients[i]
                if (coeff == 0.0) continue
                
                if (!first) {
                    sb.append(if (coeff > 0) " + " else " - ")
                } else if (coeff < 0) {
                    sb.append("-")
                }
                
                val absCoeff = abs(coeff)
                when {
                    i == 0 -> sb.append(absCoeff)
                    i == 1 -> {
                        if (absCoeff == 1.0) sb.append("x")
                        else sb.append("$absCoeff x")
                    }
                    else -> {
                        if (absCoeff == 1.0) sb.append("x^$i")
                        else sb.append("$absCoeff x^$i")
                    }
                }
                
                first = false
            }
            
            return if (sb.isEmpty()) "0" else sb.toString()
        }
        
        companion object {
            /**
             * створює поліном з коефіцієнтів
             *
             * @param coefficients коефіцієнти
             * @return поліном
             */
            fun of(vararg coefficients: Double): Polynomial {
                return Polynomial(coefficients.copyOf())
            }
        }
    }
    
    // функції для роботи з ймовірностями
    
    /**
     * обчислює факторіал числа (рекурсивно)
     *
     * @param n число
     * @return факторіал числа
     */
    tailrec fun factorialRecursive(n: Int, accumulator: Long = 1): Long {
        return when {
            n < 0 -> throw IllegalArgumentException("факторіал визначений лише для невід'ємних цілих чисел")
            n <= 1 -> accumulator
            else -> factorialRecursive(n - 1, n * accumulator)
        }
    }
    
    /**
     * обчислює біноміальний коефіцієнт
     *
     * @param n верхнє число
     * @param k нижнє число
     * @return біноміальний коефіцієнт
     */
    fun binomialCoefficient(n: Int, k: Int): Long {
        if (k < 0 || k > n) return 0
        if (k == 0 || k == n) return 1
        
        // використовуємо симетрію: c(n,k) = c(n,n-k)
        val actualK = minOf(k, n - k)
        
        var result = 1L
        for (i in 0 until actualK) {
            result = result * (n - i) / (i + 1)
        }
        
        return result
    }
    
    /**
     * обчислює значення функції нормального розподілу
     *
     * @param x точка
     * @param mean середнє значення
     * @param std стандартне відхилення
     * @return значення функції нормального розподілу
     */
    fun normalDistribution(x: Double, mean: Double = 0.0, std: Double = 1.0): Double {
        val exponent = -0.5 * ((x - mean) / std).pow(2)
        return exp(exponent) / (std * sqrt(2 * PI_DOUBLE))
    }
    
    /**
     * обчислює значення кумулятивної функції нормального розподілу
     *
     * @param x точка
     * @param mean середнє значення
     * @param std стандартне відхилення
     * @return значення кумулятивної функції нормального розподілу
     */
    fun cumulativeNormalDistribution(x: Double, mean: Double = 0.0, std: Double = 1.0): Double {
        return 0.5 * (1 + erf((x - mean) / (std * sqrt(2.0))))
    }
    
    /**
     * обчислює помилкову функцію
     *
     * @param x точка
     * @return значення помилкової функції
     */
    fun erf(x: Double): Double {
        // апроксимація функції помилок за допомогою ряду Тейлора
        val t = 1.0 / (1.0 + 0.5 * abs(x))
        
        val tau = t * exp(
            -x * x - 1.26551223 + t * (
                    1.00002368 + t * (
                            0.37409196 + t * (
                                    0.09678418 + t * (
                                            -0.18628806 + t * (
                                                    0.27886807 + t * (
                                                            -1.13520398 + t * (
                                                                    1.48851587 + t * (
                                                                            -0.82215223 + t * 0.17087277
                                                                            )
                                                                    )
                                                            )
                                                    )
                                            )
                                    )
                            )
                    )
        )
        
        return if (x >= 0) 1.0 - tau else tau - 1.0
    }
    
    // генерація псевдовипадкових чисел
    
    /**
     * представлення генератора псевдовипадкових чисел
     *
     * @property seed початкове значення
     */
    class RandomGenerator(seed: Long = System.currentTimeMillis()) {
        private var state = seed
        
        /**
         * генерує наступне псевдовипадкове число
         *
         * @return псевдовипадкове число в діапазоні [0, 1)
         */
        fun nextDouble(): Double {
            state = state * 1103515245L + 12345L
            return ((state ushr 16) and 0x7FFFFFFFL).toDouble() / (0x7FFFFFFF + 1.0)
        }
        
        /**
         * генерує псевдовипадкове ціле число в заданому діапазоні
         *
         * @param min мінімальне значення (включно)
         * @param max максимальне значення (виключно)
         * @return псевдовипадкове ціле число
         */
        fun nextInt(min: Int = 0, max: Int = Int.MAX_VALUE): Int {
            return (min + (nextDouble() * (max - min))).toInt()
        }
        
        /**
         * генерує псевдовипадкове число з нормального розподілу
         *
         * @param mean середнє значення
         * @param std стандартне відхилення
         * @return псевдовипадкове число з нормального розподілу
         */
        fun nextGaussian(mean: Double = 0.0, std: Double = 1.0): Double {
            // використовуємо перетворення Бокса-Мюллера
            val u1 = nextDouble()
            val u2 = nextDouble()
            
            val z0 = sqrt(-2.0 * ln(u1)) * cos(2 * PI_DOUBLE * u2)
            return mean + std * z0
        }
    }
    
    // функції для роботи з геометрією
    
    /**
     * представлення точки в двовимірному просторі
     *
     * @property x координата x
     * @property y координата y
     */
    data class Point2D(val x: Double, val y: Double) {
        
        /**
         * обчислює відстань до іншої точки
         *
         * @param other інша точка
         * @return відстань між точками
         */
        fun distanceTo(other: Point2D): Double {
            val dx = x - other.x
            val dy = y - other.y
            return sqrt(dx * dx + dy * dy)
        }
        
        /**
         * обчислює кут між двома точками відносно цієї точки
         *
         * @param a перша точка
         * @param b друга точка
         * @return кут у радіанах
         */
        fun angleBetween(a: Point2D, b: Point2D): Double {
            val vector1 = Point2D(a.x - x, a.y - y)
            val vector2 = Point2D(b.x - x, b.y - y)
            
            val dotProduct = vector1.x * vector2.x + vector1.y * vector2.y
            val magnitude1 = sqrt(vector1.x * vector1.x + vector1.y * vector1.y)
            val magnitude2 = sqrt(vector2.x * vector2.x + vector2.y * vector2.y)
            
            if (magnitude1 == 0.0 || magnitude2 == 0.0) {
                return 0.0
            }
            
            val cosine = dotProduct / (magnitude1 * magnitude2)
            return acos(cosine.coerceIn(-1.0, 1.0))
        }
    }
    
    /**
     * представлення прямої в двовимірному просторі
     *
     * @property a коефіцієнт a в рівнянні ax + by + c = 0
     * @property b коефіцієнт b в рівнянні ax + by + c = 0
     * @property c коефіцієнт c в рівнянні ax + by + c = 0
     */
    data class Line2D(val a: Double, val b: Double, val c: Double) {
        
        /**
         * перевіряє, чи лежить точка на прямій
         *
         * @param point точка
         * @param tolerance точність
         * @return true якщо точка лежить на прямій
         */
        fun contains(point: Point2D, tolerance: Double = 1e-10): Boolean {
            return abs(a * point.x + b * point.y + c) < tolerance
        }
        
        /**
         * знаходить точку перетину з іншою прямою
         *
         * @param other інша пряма
         * @return точка перетину або null якщо прямі паралельні
         */
        fun intersectionWith(other: Line2D): Point2D? {
            val denominator = a * other.b - b * other.a
            
            if (abs(denominator) < 1e-10) {
                return null // прямі паралельні
            }
            
            val x = (b * other.c - c * other.b) / denominator
            val y = (c * other.a - a * other.c) / denominator
            
            return Point2D(x, y)
        }
        
        companion object {
            /**
             * створює пряму за двома точками
             *
             * @param p1 перша точка
             * @param p2 друга точка
             * @return пряма
             */
            fun fromPoints(p1: Point2D, p2: Point2D): Line2D {
                if (p1.x == p2.x && p1.y == p2.y) {
                    throw IllegalArgumentException("точки повинні бути різними")
                }
                
                val a = p1.y - p2.y
                val b = p2.x - p1.x
                val c = p1.x * p2.y - p2.x * p1.y
                
                return Line2D(a, b, c)
            }
        }
    }
    
    /**
     * представлення кола
     *
     * @property center центр кола
     * @property radius радіус кола
     */
    data class Circle(val center: Point2D, val radius: Double) {
        
        /**
         * перевіряє, чи лежить точка всередині кола
         *
         * @param point точка
         * @return true якщо точка всередині кола
         */
        fun contains(point: Point2D): Boolean {
            return center.distanceTo(point) <= radius
        }
        
        /**
         * обчислює площу кола
         *
         * @return площа кола
         */
        fun area(): Double {
            return PI_DOUBLE * radius * radius
        }
        
        /**
         * обчислює довжину кола
         *
         * @return довжина кола
         */
        fun circumference(): Double {
            return 2 * PI_DOUBLE * radius
        }
    }
    
    // числові послідовності
    
    /**
     * генерує послідовність чисел Фібоначчі
     *
     * @param count кількість чисел
     * @return список чисел Фібоначчі
     */
    fun fibonacciSequence(count: Int): List<Long> {
        if (count <= 0) return emptyList()
        if (count == 1) return listOf(0L)
        if (count == 2) return listOf(0L, 1L)
        
        val sequence = mutableListOf<Long>(0L, 1L)
        for (i in 2 until count) {
            sequence.add(sequence[i - 1] + sequence[i - 2])
        }
        
        return sequence
    }
    
    /**
     * обчислює n-те число Фібоначчі
     *
     * @param n номер числа
     * @return n-те число Фібоначчі
     */
    fun fibonacci(n: Int): Long {
        if (n < 0) throw IllegalArgumentException("номер повинен бути невід'ємним")
        if (n <= 1) return n.toLong()
        
        var prev = 0L
        var curr = 1L
        
        for (i in 2..n) {
            val temp = curr
            curr += prev
            prev = temp
        }
        
        return curr
    }
    
    /**
     * генерує послідовність простих чисел методом решета Ератосфена
     *
     * @param limit верхня межа
     * @return список простих чисел
     */
    fun sieveOfEratosthenes(limit: Int): List<Int> {
        if (limit < 2) return emptyList()
        
        val isPrime = BooleanArray(limit + 1) { true }
        isPrime[0] = false
        isPrime[1] = false
        
        for (i in 2..sqrt(limit.toDouble()).toInt()) {
            if (isPrime[i]) {
                for (j in i * i..limit step i) {
                    isPrime[j] = false
                }
            }
        }
        
        return (2..limit).filter { isPrime[it] }
    }
    
    /**
     * перевіряє, чи є число простим
     *
     * @param n число
     * @return true якщо число просте
     */
    fun isPrime(n: Int): Boolean {
        if (n < 2) return false
        if (n == 2) return true
        if (n % 2 == 0) return false
        
        for (i in 3..sqrt(n.toDouble()).toInt() step 2) {
            if (n % i == 0) return false
        }
        
        return true
    }
    
    /**
     * знаходить найбільший спільний дільник двох чисел
     *
     * @param a перше число
     * @param b друге число
     * @return найбільший спільний дільник
     */
    tailrec fun gcd(a: Int, b: Int): Int {
        return if (b == 0) abs(a) else gcd(b, a % b)
    }
    
    /**
     * знаходить найменше спільне кратне двох чисел
     *
     * @param a перше число
     * @param b друге число
     * @return найменше спільне кратне
     */
    fun lcm(a: Int, b: Int): Int {
        return if (a == 0 || b == 0) 0 else abs(a * b) / gcd(a, b)
    }
    
    // функції для роботи з системами числення
    
    /**
     * переводить число з десяткової системи в іншу
     *
     * @param number число в десятковій системі
     * @param base основа нової системи числення (2-36)
     * @return число в новій системі числення
     */
    fun convertToBase(number: Int, base: Int): String {
        if (base !in 2..36) throw IllegalArgumentException("основа повинна бути в діапазоні 2-36")
        if (number == 0) return "0"
        
        val digits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        var num = abs(number)
        val result = StringBuilder()
        
        while (num > 0) {
            result.insert(0, digits[num % base])
            num /= base
        }
        
        return if (number < 0) "-$result" else result.toString()
    }
    
    /**
     * переводить число з будь-якої системи в десяткову
     *
     * @param number число в заданій системі числення
     * @param base основа системи числення (2-36)
     * @return число в десятковій системі
     */
    fun convertFromBase(number: String, base: Int): Long {
        if (base !in 2..36) throw IllegalArgumentException("основа повинна бути в діапазоні 2-36")
        
        val digits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        var result = 0L
        var power = 1L
        
        for (i in number.length - 1 downTo 0) {
            val char = number[i].uppercaseChar()
            val digitValue = digits.indexOf(char)
            
            if (digitValue == -1 || digitValue >= base) {
                throw IllegalArgumentException("неправильний символ '$char' для системи числення з основою $base")
            }
            
            result += digitValue * power
            power *= base
        }
        
        return result
    }
    
    // додаткові математичні функції
    
    /**
     * обчислює гамма-функцію (розширення факторіала для дійсних чисел)
     *
     * @param x аргумент
     * @return значення гамма-функції
     */
    fun gamma(x: Double): Double {
        // використовуємо формулу Стірлінга для наближеного обчислення
        if (x <= 0) throw IllegalArgumentException("гамма-функція визначена лише для додатних чисел")
        
        if (x > 171) throw ArithmeticException("переповнення при обчисленні гамма-функції")
        
        // для цілих чисел повертаємо факторіал
        if (x == x.toInt().toDouble() && x.toInt() <= 20) {
            return factorial(x.toInt() - 1).toDouble()
        }
        
        // формула Стірлінга
        val stirling = sqrt(2 * PI_DOUBLE / x) * (x / E_DOUBLE).pow(x)
        
        // поправка для покращення точності
        val correction = 1.0 + 1.0 / (12 * x) + 1.0 / (288 * x * x)
        
        return stirling * correction
    }
    
    /**
     * обчислює бета-функцію
     *
     * @param x перший аргумент
     * @param y другий аргумент
     * @return значення бета-функції
     */
    fun beta(x: Double, y: Double): Double {
        return gamma(x) * gamma(y) / gamma(x + y)
    }
    
    /**
     * обчислює значення дзета-функції Рімана
     *
     * @param s аргумент
     * @param terms кількість членів ряду для обчислення
     * @return значення дзета-функції
     */
    fun riemannZeta(s: Double, terms: Int = 1000): Double {
        if (s <= 1) throw IllegalArgumentException("дзета-функція збігається лише для s > 1")
        
        var sum = 0.0
        for (n in 1..terms) {
            sum += 1.0 / pow(n.toDouble(), s)
        }
        
        return sum
    }
    
    /**
     * обчислює значення еліптичного інтеграла першого роду
     *
     * @param phi амплітуда
     * @param k модуль
     * @param terms кількість членів ряду
     * @return значення еліптичного інтеграла
     */
    fun ellipticIntegralFirstKind(phi: Double, k: Double, terms: Int = 100): Double {
        var sum = 0.0
        var powerOfK = 1.0
        var factorialSquared = 1.0
        
        for (n in 0 until terms) {
            val term = powerOfK * factorialSquared / pow(4.0, n.toDouble()) * pow(sin(phi), 2 * n + 1) / (2 * n + 1)
            sum += term
            
            powerOfK *= k * k
            factorialSquared = factorialSquared * (2 * n + 1) * (2 * n + 1) / ((n + 1) * (n + 1))
        }
        
        return sum
    }
    
    // функції для роботи з векторами
    
    /**
     * представлення тривимірного вектора
     *
     * @property x компонента x
     * @property y компонента y
     * @property z компонента z
     */
    data class Vector3D(val x: Double, val y: Double, val z: Double) {
        
        /**
         * обчислює довжину вектора
         *
         * @return довжина вектора
         */
        fun magnitude(): Double {
            return sqrt(x * x + y * y + z * z)
        }
        
        /**
         * нормалізує вектор
         *
         * @return нормалізований вектор
         */
        fun normalize(): Vector3D {
            val mag = magnitude()
            if (mag == 0.0) return Vector3D(0.0, 0.0, 0.0)
            return Vector3D(x / mag, y / mag, z / mag)
        }
        
        /**
         * обчислює скалярний добуток з іншим вектором
         *
         * @param other інший вектор
         * @return скалярний добуток
         */
        fun dot(other: Vector3D): Double {
            return x * other.x + y * other.y + z * other.z
        }
        
        /**
         * обчислює векторний добуток з іншим вектором
         *
         * @param other інший вектор
         * @return векторний добуток
         */
        fun cross(other: Vector3D): Vector3D {
            val newX = y * other.z - z * other.y
            val newY = z * other.x - x * other.z
            val newZ = x * other.y - y * other.x
            return Vector3D(newX, newY, newZ)
        }
        
        /**
         * обчислює відстань до іншого вектора
         *
         * @param other інший вектор
         * @return відстань між векторами
         */
        fun distanceTo(other: Vector3D): Double {
            val dx = x - other.x
            val dy = y - other.y
            val dz = z - other.z
            return sqrt(dx * dx + dy * dy + dz * dz)
        }
        
        /**
         * додає два вектори
         *
         * @param other інший вектор
         * @return сума векторів
         */
        operator fun plus(other: Vector3D): Vector3D {
            return Vector3D(x + other.x, y + other.y, z + other.z)
        }
        
        /**
         * віднімає два вектори
         *
         * @param other інший вектор
         * @return різниця векторів
         */
        operator fun minus(other: Vector3D): Vector3D {
            return Vector3D(x - other.x, y - other.y, z - other.z)
        }
        
        /**
         * множить вектор на скаляр
         *
         * @param scalar скаляр
         * @return добуток вектора на скаляр
         */
        operator fun times(scalar: Double): Vector3D {
            return Vector3D(x * scalar, y * scalar, z * scalar)
        }
        
        /**
         * ділить вектор на скаляр
         *
         * @param scalar скаляр
         * @return частка вектора і скаляра
         */
        operator fun div(scalar: Double): Vector3D {
            if (scalar == 0.0) throw ArithmeticException("ділення на нуль")
            return Vector3D(x / scalar, y / scalar, z / scalar)
        }
        
        companion object {
            /**
             * нульовий вектор
             */
            val ZERO = Vector3D(0.0, 0.0, 0.0)
            
            /**
             * одиничний вектор по осі x
             */
            val UNIT_X = Vector3D(1.0, 0.0, 0.0)
            
            /**
             * одиничний вектор по осі y
             */
            val UNIT_Y = Vector3D(0.0, 1.0, 0.0)
            
            /**
             * одиничний вектор по осі z
             */
            val UNIT_Z = Vector3D(0.0, 0.0, 1.0)
        }
    }
    
    // функції для роботи з комплексними числами (продовження)
    
    /**
     * обчислює експоненту комплексного числа
     *
     * @param complex комплексне число
     * @return експонента комплексного числа
     */
    fun exp(complex: Complex): Complex {
        val expReal = exp(complex.real)
        val cosImag = cos(complex.imaginary)
        val sinImag = sin(complex.imaginary)
        return Complex(expReal * cosImag, expReal * sinImag)
    }
    
    /**
     * обчислює логарифм комплексного числа
     *
     * @param complex комплексне число
     * @return логарифм комплексного числа
     */
    fun log(complex: Complex): Complex {
        val modulus = complex.modulus()
        val argument = complex.argument()
        return Complex(ln(modulus), argument)
    }
    
    /**
     * обчислює степінь комплексного числа
     *
     * @param base основа
     * @param exponent показник
     * @return степінь комплексного числа
     */
    fun pow(base: Complex, exponent: Complex): Complex {
        return if (exponent.imaginary == 0.0) {
            pow(base, exponent.real)
        } else {
            exp(log(base) * exponent)
        }
    }
    
    /**
     * обчислює степінь комплексного числа з дійсним показником
     *
     * @param base основа
     * @param exponent показник
     * @return степінь комплексного числа
     */
    fun pow(base: Complex, exponent: Double): Complex {
        val modulus = base.modulus()
        val argument = base.argument()
        val newModulus = pow(modulus, exponent)
        val newArgument = argument * exponent
        return Complex(newModulus * cos(newArgument), newModulus * sin(newArgument))
    }
    
    /**
     * обчислює квадратний корінь комплексного числа
     *
     * @param complex комплексне число
     * @return квадратний корінь комплексного числа
     */
    fun sqrt(complex: Complex): Complex {
        val modulus = complex.modulus()
        val argument = complex.argument()
        val sqrtModulus = sqrt(modulus)
        val halfArgument = argument / 2.0
        return Complex(sqrtModulus * cos(halfArgument), sqrtModulus * sin(halfArgument))
    }
    
    /**
     * обчислює синус комплексного числа
     *
     * @param complex комплексне число
     * @return синус комплексного числа
     */
    fun sin(complex: Complex): Complex {
        val realPart = sin(complex.real) * cosh(complex.imaginary)
        val imaginaryPart = cos(complex.real) * sinh(complex.imaginary)
        return Complex(realPart, imaginaryPart)
    }
    
    /**
     * обчислює косинус комплексного числа
     *
     * @param complex комплексне число
     * @return косинус комплексного числа
     */
    fun cos(complex: Complex): Complex {
        val realPart = cos(complex.real) * cosh(complex.imaginary)
        val imaginaryPart = -sin(complex.real) * sinh(complex.imaginary)
        return Complex(realPart, imaginaryPart)
    }
    
    /**
     * обчислює тангенс комплексного числа
     *
     * @param complex комплексне число
     * @return тангенс комплексного числа
     */
    fun tan(complex: Complex): Complex {
        val sinValue = sin(complex)
        val cosValue = cos(complex)
        return sinValue / cosValue
    }
    
    // гіперболічні функції
    
    /**
     * обчислює гіперболічний синус
     *
     * @param x аргумент
     * @return гіперболічний синус
     */
    fun sinh(x: Double): Double {
        return (exp(x) - exp(-x)) / 2.0
    }
    
    /**
     * обчислює гіперболічний косинус
     *
     * @param x аргумент
     * @return гіперболічний косинус
     */
    fun cosh(x: Double): Double {
        return (exp(x) + exp(-x)) / 2.0
    }
    
    /**
     * обчислює гіперболічний тангенс
     *
     * @param x аргумент
     * @return гіперболічний тангенс
     */
    fun tanh(x: Double): Double {
        val exp2x = exp(2 * x)
        return (exp2x - 1) / (exp2x + 1)
    }
    
    // спеціальні функції
    
    /**
     * обчислює інтегральний синус
     *
     * @param x аргумент
     * @param terms кількість членів ряду
     * @return інтегральний синус
     */
    fun sineIntegral(x: Double, terms: Int = 100): Double {
        var sum = 0.0
        var powerOfX = x
        var factorial = 1.0
        var sign = 1.0
        
        for (n in 0 until terms) {
            val term = sign * powerOfX / (factorial * (2 * n + 1))
            sum += term
            
            powerOfX *= x * x
            factorial *= (2 * n + 2) * (2 * n + 3)
            sign *= -1
        }
        
        return sum
    }
    
    /**
     * обчислює інтегральний косинус
     *
     * @param x аргумент
     * @param terms кількість членів ряду
     * @return інтегральний косинус
     */
    fun cosineIntegral(x: Double, terms: Int = 100): Double {
        var sum = 0.0
        var powerOfX = x * x
        var factorial = 2.0
        var sign = -1.0
        
        for (n in 1..terms) {
            val term = sign * powerOfX / (factorial * (2 * n))
            sum += term
            
            powerOfX *= x * x
            factorial *= (2 * n + 1) * (2 * n + 2)
            sign *= -1
        }
        
        return ln(x) + EULER_MASCHERONI + sum
    }
    
    companion object {
        const val EULER_MASCHERONI = 0.5772156649015329
    }
    
    // функції для роботи з інтерполяцією
    
    /**
     * лінійна інтерполяція між двома точками
     *
     * @param x1 перша точка x
     * @param y1 перша точка y
     * @param x2 друга точка x
     * @param y2 друга точка y
     * @param x точка для інтерполяції
     * @return інтерпольоване значення
     */
    fun linearInterpolation(x1: Double, y1: Double, x2: Double, y2: Double, x: Double): Double {
        if (x1 == x2) return (y1 + y2) / 2.0
        return y1 + (y2 - y1) * (x - x1) / (x2 - x1)
    }
    
    /**
     * поліноміальна інтерполяція методом Лагранжа
     *
     * @param points точки для інтерполяції
     * @param x точка для інтерполяції
     * @return інтерпольоване значення
     */
    fun lagrangeInterpolation(points: List<Pair<Double, Double>>, x: Double): Double {
        if (points.size < 2) throw IllegalArgumentException("потрібно принаймні 2 точки для інтерполяції")
        
        var result = 0.0
        for (i in points.indices) {
            var term = points[i].second
            for (j in points.indices) {
                if (i != j) {
                    if (points[i].first == points[j].first) {
                        throw IllegalArgumentException("точки не повинні мати однакові x-координати")
                    }
                    term *= (x - points[j].first) / (points[i].first - points[j].first)
                }
            }
            result += term
        }
        
        return result
    }
    
    // функції для роботи з перетвореннями
    
    /**
     * швидке перетворення фур'є
     *
     * @param real дійсні частини
     * @param imag уявні частини
     */
    fun fft(real: DoubleArray, imag: DoubleArray) {
        val n = real.size
        if (n <= 1) return
        
        // перевірка, чи n є степенем двійки
        if (n and (n - 1) != 0) throw IllegalArgumentException("розмір масиву повинен бути степенем двійки")
        
        // перестановка бітів
        var j = 0
        for (i in 0 until n) {
            if (i < j) {
                val tempReal = real[i]
                val tempImag = imag[i]
                real[i] = real[j]
                imag[i] = imag[j]
                real[j] = tempReal
                imag[j] = tempImag
            }
            
            var k = n shr 1
            while (k <= j) {
                j -= k
                k = k shr 1
            }
            j += k
        }
        
        // обчислення перетворення
        var length = 2
        while (length <= n) {
            val angle = -2 * PI_DOUBLE / length
            val wReal = cos(angle)
            val wImag = sin(angle)
            
            var i = 0
            while (i < n) {
                var wr = 1.0
                var wi = 0.0
                
                for (j in 0 until length / 2) {
                    val idx1 = i + j
                    val idx2 = i + j + length / 2
                    
                    val tr = wr * real[idx2] - wi * imag[idx2]
                    val ti = wr * imag[idx2] + wi * real[idx2]
                    
                    real[idx2] = real[idx1] - tr
                    imag[idx2] = imag[idx1] - ti
                    real[idx1] += tr
                    imag[idx1] += ti
                    
                    val tempWr = wr
                    wr = wr * wReal - wi * wImag
                    wi = wi * wReal + tempWr * wImag
                }
                
                i += length
            }
            
            length = length shl 1
        }
    }
    
    /**
     * обернене швидке перетворення фур'є
     *
     * @param real дійсні частини
     * @param imag уявні частини
     */
    fun ifft(real: DoubleArray, imag: DoubleArray) {
        // комплексне спряження
        for (i in imag.indices) {
            imag[i] = -imag[i]
        }
        
        // виконуємо прямий fft
        fft(real, imag)
        
        // комплексне спряження знову і нормалізація
        for (i in imag.indices) {
            imag[i] = -imag[i] / real.size
        }
        for (i in real.indices) {
            real[i] = real[i] / real.size
        }
    }
    
    // функції для роботи з криптографією
    
    /**
     * обчислює хеш-функцію md5 (спрощена реалізація)
     *
     * @param input вхідні дані
     * @return хеш-значення
     */
    fun simpleMD5(input: String): String {
        // це спрощена реалізація для демонстраційних цілей
        var hash = 0L
        for (char in input) {
            hash = ((hash shl 5) - hash + char.code) and 0xFFFFFFFFL
        }
        return hash.toString(16).padStart(8, '0')
    }
    
    /**
     * обчислює контрольну суму crc32
     *
     * @param data вхідні дані
     * @return контрольна сума
     */
    fun crc32(data: ByteArray): Long {
        val polynomial = 0xEDB88320L
        var crc = 0xFFFFFFFFL
        
        for (byte in data) {
            crc = crc xor (byte.toLong() and 0xFF)
            for (i in 0 until 8) {
                if ((crc and 1) != 0L) {
                    crc = (crc ushr 1) xor polynomial
                } else {
                    crc = crc ushr 1
                }
            }
        }
        
        return crc xor 0xFFFFFFFFL
    }
    
    // функції для роботи з теорією графів
    
    /**
     * представлення графа
     *
     * @property vertices кількість вершин
     * @property edges ребра графа
     */
    class Graph(val vertices: Int) {
        private val adjacencyList = Array(vertices) { mutableListOf<Int>() }
        
        /**
         * додає ребро до графа
         *
         * @param from початкова вершина
         * @param to кінцева вершина
         */
        fun addEdge(from: Int, to: Int) {
            if (from < 0 || from >= vertices || to < 0 || to >= vertices) {
                throw IndexOutOfBoundsException("індекс вершини поза межами графа")
            }
            adjacencyList[from].add(to)
        }
        
        /**
         * отримує список суміжних вершин
         *
         * @param vertex вершина
         * @return список суміжних вершин
         */
        fun getAdjacentVertices(vertex: Int): List<Int> {
            if (vertex < 0 || vertex >= vertices) {
                throw IndexOutOfBoundsException("індекс вершини поза межами графа")
            }
            return adjacencyList[vertex]
        }
    }
}