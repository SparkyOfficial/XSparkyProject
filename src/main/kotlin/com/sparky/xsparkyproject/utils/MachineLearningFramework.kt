/**
 * фреймворк для машинного навчання
 *
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.util.*
import kotlin.math.*
import kotlin.random.*

/**
 * представлення інтерфейсу для роботи з датасетами
 */
interface Dataset<T> {
    /**
     * отримати розмір датасету
     *
     * @return розмір
     */
    fun size(): Int
    
    /**
     * отримати елемент за індексом
     *
     * @param index індекс
     * @return елемент
     */
    fun get(index: Int): T
    
    /**
     * додати елемент
     *
     * @param item елемент
     */
    fun add(item: T)
    
    /**
     * видалити елемент за індексом
     *
     * @param index індекс
     * @return видалений елемент
     */
    fun removeAt(index: Int): T
    
    /**
     * очистити датасет
     */
    fun clear()
    
    /**
     * отримати ітератор
     *
     * @return ітератор
     */
    fun iterator(): Iterator<T>
}

/**
 * представлення базової реалізації датасету
 */
open class BaseDataset<T> : Dataset<T> {
    protected val data = mutableListOf<T>()
    
    override fun size(): Int = data.size
    
    override fun get(index: Int): T = data[index]
    
    override fun add(item: T) {
        data.add(item)
    }
    
    override fun removeAt(index: Int): T = data.removeAt(index)
    
    override fun clear() {
        data.clear()
    }
    
    override fun iterator(): Iterator<T> = data.iterator()
}

/**
 * представлення інтерфейсу для векторів
 */
interface Vector {
    /**
     * отримати розмірність вектора
     *
     * @return розмірність
     */
    fun size(): Int
    
    /**
     * отримати елемент за індексом
     *
     * @param index індекс
     * @return елемент
     */
    fun get(index: Int): Double
    
    /**
     * встановити елемент за індексом
     *
     * @param index індекс
     * @param value значення
     */
    fun set(index: Int, value: Double)
    
    /**
     * обчислити довжину вектора
     *
     * @return довжина
     */
    fun magnitude(): Double
    
    /**
     * нормалізувати вектор
     *
     * @return нормалізований вектор
     */
    fun normalize(): Vector
}

/**
 * представлення базової реалізації вектора
 */
open class BaseVector(private val elements: DoubleArray) : Vector {
    
    override fun size(): Int = elements.size
    
    override fun get(index: Int): Double = elements[index]
    
    override fun set(index: Int, value: Double) {
        elements[index] = value
    }
    
    override fun magnitude(): Double {
        return sqrt(elements.sumOf { it * it })
    }
    
    override fun normalize(): Vector {
        val mag = magnitude()
        if (mag == 0.0) return BaseVector(DoubleArray(elements.size))
        return BaseVector(elements.map { it / mag }.toDoubleArray())
    }
    
    /**
     * обчислити скалярний добуток з іншим вектором
     *
     * @param other інший вектор
     * @return скалярний добуток
     */
    fun dot(other: Vector): Double {
        require(size() == other.size()) { "Вектори мають різну розмірність" }
        return (0 until size()).sumOf { get(it) * other.get(it) }
    }
    
    /**
     * обчислити відстань до іншого вектора
     *
     * @param other інший вектор
     * @return відстань
     */
    fun distanceTo(other: Vector): Double {
        require(size() == other.size()) { "Вектори мають різну розмірність" }
        return sqrt((0 until size()).sumOf { (get(it) - other.get(it)).pow(2) })
    }
    
    /**
     * додати інший вектор
     *
     * @param other інший вектор
     * @return результат додавання
     */
    fun add(other: Vector): Vector {
        require(size() == other.size()) { "Вектори мають різну розмірність" }
        return BaseVector(DoubleArray(size()) { get(it) + other.get(it) })
    }
    
    /**
     * відняти інший вектор
     *
     * @param other інший вектор
     * @return результат віднімання
     */
    fun subtract(other: Vector): Vector {
        require(size() == other.size()) { "Вектори мають різну розмірність" }
        return BaseVector(DoubleArray(size()) { get(it) - other.get(it) })
    }
    
    /**
     * помножити на скаляр
     *
     * @param scalar скаляр
     * @return результат множення
     */
    fun multiply(scalar: Double): Vector {
        return BaseVector(elements.map { it * scalar }.toDoubleArray())
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
    fun rows(): Int
    
    /**
     * отримати кількість стовпців
     *
     * @return кількість стовпців
     */
    fun cols(): Int
    
    /**
     * отримати елемент за координатами
     *
     * @param row рядок
     * @param col стовпець
     * @return елемент
     */
    fun get(row: Int, col: Int): Double
    
    /**
     * встановити елемент за координатами
     *
     * @param row рядок
     * @param col стовпець
     * @param value значення
     */
    fun set(row: Int, col: Int, value: Double)
    
    /**
     * транспонувати матрицю
     *
     * @return транспонована матриця
     */
    fun transpose(): Matrix
    
    /**
     * перемножити на іншу матрицю
     *
     * @param other інша матриця
     * @return результат множення
     */
    fun multiply(other: Matrix): Matrix
}

/**
 * представлення базової реалізації матриці
 */
open class BaseMatrix(private val rows: Int, private val cols: Int) : Matrix {
    private val data = Array(rows) { DoubleArray(cols) }
    
    override fun rows(): Int = rows
    
    override fun cols(): Int = cols
    
    override fun get(row: Int, col: Int): Double = data[row][col]
    
    override fun set(row: Int, col: Int, value: Double) {
        data[row][col] = value
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
    
    override fun multiply(other: Matrix): Matrix {
        require(cols() == other.rows()) { "Несумісні розміри матриць для множення" }
        
        val result = BaseMatrix(rows(), other.cols())
        for (i in 0 until rows()) {
            for (j in 0 until other.cols()) {
                var sum = 0.0
                for (k in 0 until cols()) {
                    sum += get(i, k) * other.get(k, j)
                }
                result.set(i, j, sum)
            }
        }
        return result
    }
    
    /**
     * додати іншу матрицю
     *
     * @param other інша матриця
     * @return результат додавання
     */
    fun add(other: Matrix): Matrix {
        require(rows() == other.rows() && cols() == other.cols()) { "Матриці мають різні розміри" }
        
        val result = BaseMatrix(rows(), cols())
        for (i in 0 until rows()) {
            for (j in 0 until cols()) {
                result.set(i, j, get(i, j) + other.get(i, j))
            }
        }
        return result
    }
    
    /**
     * відняти іншу матрицю
     *
     * @param other інша матриця
     * @return результат віднімання
     */
    fun subtract(other: Matrix): Matrix {
        require(rows() == other.rows() && cols() == other.cols()) { "Матриці мають різні розміри" }
        
        val result = BaseMatrix(rows(), cols())
        for (i in 0 until rows()) {
            for (j in 0 until cols()) {
                result.set(i, j, get(i, j) - other.get(i, j))
            }
        }
        return result
    }
    
    /**
     * помножити на скаляр
     *
     * @param scalar скаляр
     * @return результат множення
     */
    fun multiply(scalar: Double): Matrix {
        val result = BaseMatrix(rows(), cols())
        for (i in 0 until rows()) {
            for (j in 0 until cols()) {
                result.set(i, j, get(i, j) * scalar)
            }
        }
        return result
    }
    
    /**
     * отримати рядок як вектор
     *
     * @param row індекс рядка
     * @return вектор
     */
    fun getRow(row: Int): Vector {
        return BaseVector(DoubleArray(cols()) { get(row, it) })
    }
    
    /**
     * отримати стовпець як вектор
     *
     * @param col індекс стовпця
     * @return вектор
     */
    fun getCol(col: Int): Vector {
        return BaseVector(DoubleArray(rows()) { get(it, col) })
    }
}

/**
 * представлення інтерфейсу для моделі машинного навчання
 */
interface MLModel<T, R> {
    /**
     * навчити модель
     *
     * @param dataset датасет
     */
    fun train(dataset: Dataset<Pair<T, R>>)
    
    /**
     * зробити передбачення
     *
     * @param input вхідні дані
     * @return передбачення
     */
    fun predict(input: T): R
    
    /**
     * оцінити якість моделі
     *
     * @param dataset датасет
     * @return метрика якості
     */
    fun evaluate(dataset: Dataset<Pair<T, R>>): Double
}

/**
 * представлення інтерфейсу для регресійної моделі
 */
interface RegressionModel : MLModel<Vector, Double> {
    /**
     * отримати коефіцієнти моделі
     *
     * @return коефіцієнти
     */
    fun getCoefficients(): Vector
    
    /**
     * отримати вільний член
     *
     * @return вільний член
     */
    fun getIntercept(): Double
}

/**
 * представлення лінійної регресії
 */
class LinearRegression : RegressionModel {
    private var coefficients: Vector? = null
    private var intercept = 0.0
    
    override fun train(dataset: Dataset<Pair<Vector, Double>>) {
        if (dataset.size() == 0) return
        
        val n = dataset.size()
        val features = dataset[0].first.size()
        
        // Створити матрицю ознак
        val X = BaseMatrix(n, features + 1) // +1 для вільного члена
        val y = BaseVector(DoubleArray(n) { dataset[it].second })
        
        // Заповнити матрицю ознак
        for (i in 0 until n) {
            X.set(i, 0, 1.0) // Вільний член
            val featuresVector = dataset[i].first
            for (j in 0 until features) {
                X.set(i, j + 1, featuresVector.get(j))
            }
        }
        
        // Обчислити коефіцієнти за формулою: (X^T * X)^(-1) * X^T * y
        val Xt = X.transpose()
        val XtX = Xt.multiply(X)
        // Для спрощення використовуємо градієнтний спуск
        gradientDescent(X, y)
    }
    
    private fun gradientDescent(X: Matrix, y: Vector) {
        val learningRate = 0.01
        val iterations = 1000
        val n = X.rows()
        val m = X.cols()
        
        // Ініціалізувати коефіцієнти
        val theta = BaseVector(DoubleArray(m))
        
        // Градієнтний спуск
        for (iter in 0 until iterations) {
            val predictions = BaseVector(DoubleArray(n) { i ->
                var sum = 0.0
                for (j in 0 until m) {
                    sum += X.get(i, j) * theta.get(j)
                }
                sum
            })
            
            val errors = predictions.subtract(y)
            
            // Оновити коефіцієнти
            for (j in 0 until m) {
                var gradient = 0.0
                for (i in 0 until n) {
                    gradient += errors.get(i) * X.get(i, j)
                }
                gradient /= n
                theta.set(j, theta.get(j) - learningRate * gradient)
            }
        }
        
        // Зберегти коефіцієнти
        intercept = theta.get(0)
        coefficients = BaseVector(DoubleArray(theta.size() - 1) { theta.get(it + 1) })
    }
    
    override fun predict(input: Vector): Double {
        val coeffs = coefficients ?: throw IllegalStateException("Модель не навчена")
        require(input.size() == coeffs.size()) { "Невідповідна розмірність вхідних даних" }
        
        var result = intercept
        for (i in 0 until input.size()) {
            result += input.get(i) * coeffs.get(i)
        }
        return result
    }
    
    override fun evaluate(dataset: Dataset<Pair<Vector, Double>>): Double {
        if (dataset.size() == 0) return 0.0
        
        var sumSquaredErrors = 0.0
        var sumSquaredTotal = 0.0
        val mean = dataset.sumOf { it.second } / dataset.size()
        
        for (i in 0 until dataset.size()) {
            val (features, actual) = dataset[i]
            val predicted = predict(features)
            sumSquaredErrors += (actual - predicted).pow(2)
            sumSquaredTotal += (actual - mean).pow(2)
        }
        
        return 1.0 - (sumSquaredErrors / sumSquaredTotal) // R-квадрат
    }
    
    override fun getCoefficients(): Vector {
        return coefficients ?: throw IllegalStateException("Модель не навчена")
    }
    
    override fun getIntercept(): Double {
        return intercept
    }
}

/**
 * представлення інтерфейсу для класифікаційної моделі
 */
interface ClassificationModel : MLModel<Vector, Int> {
    /**
     * отримати кількість класів
     *
     * @return кількість класів
     */
    fun getNumClasses(): Int
    
    /**
     * отримати ймовірності для кожного класу
     *
     * @param input вхідні дані
     * @return ймовірності
     */
    fun predictProbabilities(input: Vector): DoubleArray
}

/**
 * представлення логістичної регресії
 */
class LogisticRegression(private val numClasses: Int = 2) : ClassificationModel {
    private var weights: Matrix? = null
    private var intercepts: Vector? = null
    
    override fun train(dataset: Dataset<Pair<Vector, Int>>) {
        if (dataset.size() == 0) return
        
        val n = dataset.size()
        val features = dataset[0].first.size()
        
        if (numClasses == 2) {
            // Бінарна класифікація
            trainBinary(dataset)
        } else {
            // Багатокласова класифікація (one-vs-rest)
            trainMulticlass(dataset)
        }
    }
    
    private fun trainBinary(dataset: Dataset<Pair<Vector, Int>>) {
        val n = dataset.size()
        val features = dataset[0].first.size()
        
        // Створити матрицю ознак
        val X = BaseMatrix(n, features + 1) // +1 для вільного члена
        val y = DoubleArray(n) { if (dataset[it].second == 1) 1.0 else 0.0 }
        
        // Заповнити матрицю ознак
        for (i in 0 until n) {
            X.set(i, 0, 1.0) // Вільний член
            val featuresVector = dataset[i].first
            for (j in 0 until features) {
                X.set(i, j + 1, featuresVector.get(j))
            }
        }
        
        // Градієнтний спуск
        val learningRate = 0.01
        val iterations = 1000
        val weightsVector = BaseVector(DoubleArray(features + 1))
        
        for (iter in 0 until iterations) {
            val predictions = DoubleArray(n) { i ->
                var z = 0.0
                for (j in 0 until features + 1) {
                    z += X.get(i, j) * weightsVector.get(j)
                }
                sigmoid(z)
            }
            
            // Оновити ваги
            for (j in 0 until features + 1) {
                var gradient = 0.0
                for (i in 0 until n) {
                    gradient += (predictions[i] - y[i]) * X.get(i, j)
                }
                gradient /= n
                weightsVector.set(j, weightsVector.get(j) - learningRate * gradient)
            }
        }
        
        // Зберегти ваги
        intercepts = BaseVector(doubleArrayOf(weightsVector.get(0)))
        val coeffs = DoubleArray(features) { weightsVector.get(it + 1) }
        weights = object : Matrix {
            override fun rows(): Int = 1
            override fun cols(): Int = features
            override fun get(row: Int, col: Int): Double = coeffs[col]
            override fun set(row: Int, col: Int, value: Double) { coeffs[col] = value }
            override fun transpose(): Matrix = throw UnsupportedOperationException()
            override fun multiply(other: Matrix): Matrix = throw UnsupportedOperationException()
        }
    }
    
    private fun trainMulticlass(dataset: Dataset<Pair<Vector, Int>>) {
        val n = dataset.size()
        val features = dataset[0].first.size()
        
        // Створити ваги для кожного класу
        val weightMatrix = BaseMatrix(numClasses, features + 1) // +1 для вільного члена
        
        // Навчити класифікатор для кожного класу
        for (classIndex in 0 until numClasses) {
            // Створити бінарну задачу для поточного класу
            val binaryDataset = object : Dataset<Pair<Vector, Int>> {
                override fun size(): Int = n
                override fun get(index: Int): Pair<Vector, Int> {
                    val (features, label) = dataset[index]
                    return Pair(features, if (label == classIndex) 1 else 0)
                }
                override fun add(item: Pair<Vector, Int>) = throw UnsupportedOperationException()
                override fun removeAt(index: Int): Pair<Vector, Int> = throw UnsupportedOperationException()
                override fun clear() = throw UnsupportedOperationException()
                override fun iterator(): Iterator<Pair<Vector, Int>> = throw UnsupportedOperationException()
            }
            
            // Навчити бінарний класифікатор
            val tempModel = LogisticRegression(2)
            tempModel.train(binaryDataset)
            
            // Зберегти ваги
            weightMatrix.set(classIndex, 0, tempModel.intercepts?.get(0) ?: 0.0)
            val coeffs = tempModel.weights
            if (coeffs != null) {
                for (j in 0 until features) {
                    weightMatrix.set(classIndex, j + 1, coeffs.get(0, j))
                }
            }
        }
        
        weights = weightMatrix
        intercepts = BaseVector(DoubleArray(numClasses) { weightMatrix.get(it, 0) })
    }
    
    private fun sigmoid(z: Double): Double {
        return 1.0 / (1.0 + exp(-z))
    }
    
    override fun predict(input: Vector): Int {
        val probabilities = predictProbabilities(input)
        return probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
    }
    
    override fun predictProbabilities(input: Vector): DoubleArray {
        val weightsMatrix = weights ?: throw IllegalStateException("Модель не навчена")
        val interceptsVector = intercepts ?: throw IllegalStateException("Модель не навчена")
        
        if (numClasses == 2) {
            // Бінарна класифікація
            var z = interceptsVector.get(0)
            for (i in 0 until input.size()) {
                z += input.get(i) * weightsMatrix.get(0, i)
            }
            val prob = sigmoid(z)
            return doubleArrayOf(1.0 - prob, prob)
        } else {
            // Багатокласова класифікація
            val scores = DoubleArray(numClasses)
            for (i in 0 until numClasses) {
                var z = interceptsVector.get(i)
                for (j in 0 until input.size()) {
                    z += input.get(j) * weightsMatrix.get(i, j + 1)
                }
                scores[i] = z
            }
            
            // Softmax
            val maxScore = scores.maxOrNull() ?: 0.0
            val expScores = scores.map { exp(it - maxScore) }.toDoubleArray()
            val sumExpScores = expScores.sum()
            
            return expScores.map { it / sumExpScores }.toDoubleArray()
        }
    }
    
    override fun evaluate(dataset: Dataset<Pair<Vector, Int>>): Double {
        if (dataset.size() == 0) return 0.0
        
        var correct = 0
        for (i in 0 until dataset.size()) {
            val (features, actual) = dataset[i]
            val predicted = predict(features)
            if (predicted == actual) correct++
        }
        
        return correct.toDouble() / dataset.size()
    }
    
    override fun getNumClasses(): Int = numClasses
}

/**
 * представлення інтерфейсу для кластеризації
 */
interface ClusteringAlgorithm {
    /**
     * навчити модель кластеризації
     *
     * @param dataset датасет
     * @param k кількість кластерів
     */
    fun fit(dataset: Dataset<Vector>, k: Int)
    
    /**
     * призначити кластер для вектора
     *
     * @param vector вектор
     * @return номер кластера
     */
    fun predict(vector: Vector): Int
    
    /**
     * отримати центроїди кластерів
     *
     * @return центроїди
     */
    fun getCentroids(): List<Vector>
}

/**
 * представлення алгоритму k-середніх
 */
class KMeans : ClusteringAlgorithm {
    private var centroids: List<Vector> = emptyList()
    private var k = 0
    
    override fun fit(dataset: Dataset<Vector>, k: Int) {
        this.k = k
        if (dataset.size() == 0) return
        
        val n = dataset.size()
        val dimensions = dataset[0].size()
        
        // Ініціалізувати центроїди випадково
        val random = Random.Default
        centroids = (0 until k).map {
            BaseVector(DoubleArray(dimensions) { random.nextDouble() })
        }
        
        // Ітеративно оновлювати центроїди
        val maxIterations = 100
        for (iter in 0 until maxIterations) {
            // Призначити кожен вектор до найближчого кластера
            val clusters = (0 until k).map { mutableListOf<Vector>() }
            
            for (i in 0 until n) {
                val vector = dataset[i]
                val clusterIndex = findClosestCentroid(vector)
                clusters[clusterIndex].add(vector)
            }
            
            // Оновити центроїди
            val newCentroids = clusters.map { cluster ->
                if (cluster.isEmpty()) {
                    // Якщо кластер порожній, залишити старий центроїд
                    centroids[clusters.indexOf(cluster)]
                } else {
                    // Обчислити середнє значення векторів у кластері
                    val sum = BaseVector(DoubleArray(dimensions))
                    cluster.forEach { vector ->
                        for (j in 0 until dimensions) {
                            sum.set(j, sum.get(j) + vector.get(j))
                        }
                    }
                    for (j in 0 until dimensions) {
                        sum.set(j, sum.get(j) / cluster.size)
                    }
                    sum
                }
            }
            
            // Перевірити збіжність
            var converged = true
            for (i in 0 until k) {
                if (centroids[i].distanceTo(newCentroids[i]) > 1e-6) {
                    converged = false
                    break
                }
            }
            
            centroids = newCentroids
            
            if (converged) break
        }
    }
    
    private fun findClosestCentroid(vector: Vector): Int {
        return centroids.indices.minByOrNull { centroids[it].distanceTo(vector) } ?: 0
    }
    
    override fun predict(vector: Vector): Int {
        if (centroids.isEmpty()) throw IllegalStateException("Модель не навчена")
        return findClosestCentroid(vector)
    }
    
    override fun getCentroids(): List<Vector> = centroids
}

/**
 * представлення інтерфейсу для нормалізації даних
 */
interface DataNormalizer {
    /**
     * нормалізувати датасет
     *
     * @param dataset датасет
     * @return нормалізований датасет
     */
    fun normalize(dataset: Dataset<Vector>): Dataset<Vector>
    
    /**
     * нормалізувати вектор
     *
     * @param vector вектор
     * @return нормалізований вектор
     */
    fun normalize(vector: Vector): Vector
    
    /**
     * денормалізувати вектор
     *
     * @param vector вектор
     * @return денормалізований вектор
     */
    fun denormalize(vector: Vector): Vector
}

/**
 * представлення мінімакс нормалізації
 */
class MinMaxNormalizer : DataNormalizer {
    private var minValues: Vector? = null
    private var maxValues: Vector? = null
    
    override fun normalize(dataset: Dataset<Vector>): Dataset<Vector> {
        if (dataset.size() == 0) return dataset
        
        val dimensions = dataset[0].size()
        
        // Обчислити мінімальні та максимальні значення для кожного виміру
        val minVals = DoubleArray(dimensions) { Double.POSITIVE_INFINITY }
        val maxVals = DoubleArray(dimensions) { Double.NEGATIVE_INFINITY }
        
        for (i in 0 until dataset.size()) {
            val vector = dataset[i]
            for (j in 0 until dimensions) {
                val value = vector.get(j)
                if (value < minVals[j]) minVals[j] = value
                if (value > maxVals[j]) maxVals[j] = value
            }
        }
        
        minValues = BaseVector(minVals)
        maxValues = BaseVector(maxVals)
        
        // Нормалізувати датасет
        val normalizedDataset = BaseDataset<Vector>()
        for (i in 0 until dataset.size()) {
            normalizedDataset.add(normalize(dataset[i]))
        }
        
        return normalizedDataset
    }
    
    override fun normalize(vector: Vector): Vector {
        val minVals = minValues ?: throw IllegalStateException("Нормалізатор не навчений")
        val maxVals = maxValues ?: throw IllegalStateException("Нормалізатор не навчений")
        require(vector.size() == minVals.size()) { "Невідповідна розмірність вектора" }
        
        val normalized = BaseVector(DoubleArray(vector.size()))
        for (i in 0 until vector.size()) {
            val min = minVals.get(i)
            val max = maxVals.get(i)
            val value = vector.get(i)
            
            if (max == min) {
                normalized.set(i, 0.0)
            } else {
                normalized.set(i, (value - min) / (max - min))
            }
        }
        
        return normalized
    }
    
    override fun denormalize(vector: Vector): Vector {
        val minVals = minValues ?: throw IllegalStateException("Нормалізатор не навчений")
        val maxVals = maxValues ?: throw IllegalStateException("Нормалізатор не навчений")
        require(vector.size() == minVals.size()) { "Невідповідна розмірність вектора" }
        
        val denormalized = BaseVector(DoubleArray(vector.size()))
        for (i in 0 until vector.size()) {
            val min = minVals.get(i)
            val max = maxVals.get(i)
            val value = vector.get(i)
            
            denormalized.set(i, value * (max - min) + min)
        }
        
        return denormalized
    }
}

/**
 * представлення стандартної нормалізації (Z-score)
 */
class StandardNormalizer : DataNormalizer {
    private var means: Vector? = null
    private var stdDevs: Vector? = null
    
    override fun normalize(dataset: Dataset<Vector>): Dataset<Vector> {
        if (dataset.size() == 0) return dataset
        
        val dimensions = dataset[0].size()
        
        // Обчислити середні значення та стандартні відхилення
        val sums = DoubleArray(dimensions)
        val sumSquares = DoubleArray(dimensions)
        
        for (i in 0 until dataset.size()) {
            val vector = dataset[i]
            for (j in 0 until dimensions) {
                val value = vector.get(j)
                sums[j] += value
                sumSquares[j] += value * value
            }
        }
        
        val meansArray = DoubleArray(dimensions) { sums[it] / dataset.size() }
        val stdDevsArray = DoubleArray(dimensions) {
            val mean = meansArray[it]
            val variance = (sumSquares[it] / dataset.size()) - (mean * mean)
            sqrt(maxOf(variance, 0.0))
        }
        
        means = BaseVector(meansArray)
        stdDevs = BaseVector(stdDevsArray)
        
        // Нормалізувати датасет
        val normalizedDataset = BaseDataset<Vector>()
        for (i in 0 until dataset.size()) {
            normalizedDataset.add(normalize(dataset[i]))
        }
        
        return normalizedDataset
    }
    
    override fun normalize(vector: Vector): Vector {
        val meansVector = means ?: throw IllegalStateException("Нормалізатор не навчений")
        val stdDevsVector = stdDevs ?: throw IllegalStateException("Нормалізатор не навчений")
        require(vector.size() == meansVector.size()) { "Невідповідна розмірність вектора" }
        
        val normalized = BaseVector(DoubleArray(vector.size()))
        for (i in 0 until vector.size()) {
            val mean = meansVector.get(i)
            val stdDev = stdDevsVector.get(i)
            val value = vector.get(i)
            
            if (stdDev == 0.0) {
                normalized.set(i, 0.0)
            } else {
                normalized.set(i, (value - mean) / stdDev)
            }
        }
        
        return normalized
    }
    
    override fun denormalize(vector: Vector): Vector {
        val meansVector = means ?: throw IllegalStateException("Нормалізатор не навчений")
        val stdDevsVector = stdDevs ?: throw IllegalStateException("Нормалізатор не навчений")
        require(vector.size() == meansVector.size()) { "Невідповідна розмірність вектора" }
        
        val denormalized = BaseVector(DoubleArray(vector.size()))
        for (i in 0 until vector.size()) {
            val mean = meansVector.get(i)
            val stdDev = stdDevsVector.get(i)
            val value = vector.get(i)
            
            denormalized.set(i, value * stdDev + mean)
        }
        
        return denormalized
    }
}

/**
 * представлення інтерфейсу для розбиття даних
 */
interface DataSplitter<T> {
    /**
     * розбити датасет на навчальну та тестову вибірки
     *
     * @param dataset датасет
     * @param trainRatio частка навчальної вибірки
     * @return пара (навчальна вибірка, тестова вибірка)
     */
    fun split(dataset: Dataset<T>, trainRatio: Double = 0.8): Pair<Dataset<T>, Dataset<T>>
    
    /**
     * розбити датасет на навчальну, валідаційну та тестову вибірки
     *
     * @param dataset датасет
     * @param trainRatio частка навчальної вибірки
     * @param validationRatio частка валідаційної вибірки
     * @return трійка (навчальна вибірка, валідаційна вибірка, тестова вибірка)
     */
    fun split(dataset: Dataset<T>, trainRatio: Double, validationRatio: Double): Triple<Dataset<T>, Dataset<T>, Dataset<T>>
}

/**
 * представлення базової реалізації розбивки даних
 */
class BaseDataSplitter<T> : DataSplitter<T> {
    
    override fun split(dataset: Dataset<T>, trainRatio: Double): Pair<Dataset<T>, Dataset<T>> {
        require(trainRatio > 0 && trainRatio < 1) { "Невірне значення trainRatio" }
        
        val n = dataset.size()
        val trainSize = (n * trainRatio).toInt()
        
        val trainDataset = BaseDataset<T>()
        val testDataset = BaseDataset<T>()
        
        // Перемішати індекси
        val indices = (0 until n).shuffled()
        
        // Розподілити дані
        for (i in 0 until n) {
            if (i < trainSize) {
                trainDataset.add(dataset[indices[i]])
            } else {
                testDataset.add(dataset[indices[i]])
            }
        }
        
        return Pair(trainDataset, testDataset)
    }
    
    override fun split(dataset: Dataset<T>, trainRatio: Double, validationRatio: Double): Triple<Dataset<T>, Dataset<T>, Dataset<T>> {
        require(trainRatio > 0 && validationRatio > 0 && (trainRatio + validationRatio) < 1) { 
            "Невірні значення trainRatio або validationRatio" 
        }
        
        val n = dataset.size()
        val trainSize = (n * trainRatio).toInt()
        val validationSize = (n * validationRatio).toInt()
        
        val trainDataset = BaseDataset<T>()
        val validationDataset = BaseDataset<T>()
        val testDataset = BaseDataset<T>()
        
        // Перемішати індекси
        val indices = (0 until n).shuffled()
        
        // Розподілити дані
        for (i in 0 until n) {
            when {
                i < trainSize -> trainDataset.add(dataset[indices[i]])
                i < trainSize + validationSize -> validationDataset.add(dataset[indices[i]])
                else -> testDataset.add(dataset[indices[i]])
            }
        }
        
        return Triple(trainDataset, validationDataset, testDataset)
    }
}

/**
 * представлення інтерфейсу для крос-валідації
 */
interface CrossValidator<T> {
    /**
     * виконати крос-валідацію
     *
     * @param dataset датасет
     * @param model модель
     * @param folds кількість фолдів
     * @return список результатів для кожного фолду
     */
    fun <R> crossValidate(dataset: Dataset<Pair<T, R>>, model: MLModel<T, R>, folds: Int): List<Double>
}

/**
 * представлення базової реалізації крос-валідації
 */
class BaseCrossValidator<T> : CrossValidator<T> {
    
    override fun <R> crossValidate(dataset: Dataset<Pair<T, R>>, model: MLModel<T, R>, folds: Int): List<Double> {
        require(folds > 1) { "Кількість фолдів має бути більше 1" }
        
        val n = dataset.size()
        val foldSize = n / folds
        val indices = (0 until n).shuffled()
        val results = mutableListOf<Double>()
        
        for (fold in 0 until folds) {
            // Створити навчальну та тестову вибірки для поточного фолду
            val trainDataset = BaseDataset<Pair<T, R>>()
            val testDataset = BaseDataset<Pair<T, R>>()
            
            for (i in 0 until n) {
                if (i >= fold * foldSize && i < (fold + 1) * foldSize) {
                    testDataset.add(dataset[indices[i]])
                } else {
                    trainDataset.add(dataset[indices[i]])
                }
            }
            
            // Навчити модель
            model.train(trainDataset)
            
            // Оцінити модель
            val score = model.evaluate(testDataset)
            results.add(score)
        }
        
        return results
    }
}

/**
 * представлення інтерфейсу для метрик оцінки
 */
interface EvaluationMetrics {
    /**
     * обчислити середню абсолютну помилку
     *
     * @param actual справжні значення
     * @param predicted передбачені значення
     * @return MAE
     */
    fun meanAbsoluteError(actual: DoubleArray, predicted: DoubleArray): Double
    
    /**
     * обчислити середню квадратичну помилку
     *
     * @param actual справжні значення
     * @param predicted передбачені значення
     * @return MSE
     */
    fun meanSquaredError(actual: DoubleArray, predicted: DoubleArray): Double
    
    /**
     * обчислити корінь середньоквадратичної помилки
     *
     * @param actual справжні значення
     * @param predicted передбачені значення
     * @return RMSE
     */
    fun rootMeanSquaredError(actual: DoubleArray, predicted: DoubleArray): Double
    
    /**
     * обчислити R-квадрат
     *
     * @param actual справжні значення
     * @param predicted передбачені значення
     * @return R-квадрат
     */
    fun rSquared(actual: DoubleArray, predicted: DoubleArray): Double
    
    /**
     * обчислити точність
     *
     * @param actual справжні мітки
     * @param predicted передбачені мітки
     * @return точність
     */
    fun accuracy(actual: IntArray, predicted: IntArray): Double
    
    /**
     * обчислити повноту
     *
     * @param actual справжні мітки
     * @param predicted передбачені мітки
     * @param positiveClass позитивний клас
     * @return повнота
     */
    fun recall(actual: IntArray, predicted: IntArray, positiveClass: Int): Double
    
    /**
     * обчислити точність (precision)
     *
     * @param actual справжні мітки
     * @param predicted передбачені мітки
     * @param positiveClass позитивний клас
     * @return точність
     */
    fun precision(actual: IntArray, predicted: IntArray, positiveClass: Int): Double
    
    /**
     * обчислити F1-оцінку
     *
     * @param actual справжні мітки
     * @param predicted передбачені мітки
     * @param positiveClass позитивний клас
     * @return F1-оцінка
     */
    fun f1Score(actual: IntArray, predicted: IntArray, positiveClass: Int): Double
}

/**
 * представлення базової реалізації метрик оцінки