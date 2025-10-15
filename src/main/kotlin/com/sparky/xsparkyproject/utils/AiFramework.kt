/**
 * Штучний інтелект фреймворк для роботи з алгоритмами штучного інтелекту
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
import kotlin.math.*
import java.io.File

/**
 * представлення інтерфейсу для роботи з нейронними мережами
 */
interface NeuralNetwork {
    /**
     * навчити нейронну мережу
     *
     * @param trainingData навчальні дані
     * @param epochs кількість епох
     * @param learningRate швидкість навчання
     */
    fun train(trainingData: List<TrainingExample>, epochs: Int, learningRate: Double)

    /**
     * передбачити результат
     *
     * @param input вхідні дані
     * @return передбачення
     */
    fun predict(input: DoubleArray): DoubleArray

    /**
     * оцінити точність моделі
     *
     * @param testData тестові дані
     * @return точність
     */
    fun evaluate(testData: List<TrainingExample>): Double

    /**
     * зберегти модель
     *
     * @param filePath шлях до файлу
     */
    fun saveModel(filePath: String)

    /**
     * завантажити модель
     *
     * @param filePath шлях до файлу
     */
    fun loadModel(filePath: String)

    /**
     * отримати архітектуру мережі
     *
     * @return архітектура
     */
    fun getArchitecture(): NetworkArchitecture
}

/**
 * представлення навчального прикладу
 */
data class TrainingExample(
    val input: DoubleArray,
    val output: DoubleArray
)

/**
 * представлення архітектури мережі
 */
data class NetworkArchitecture(
    val inputSize: Int,
    val hiddenLayers: List<Int>,
    val outputSize: Int,
    val activationFunction: String
)

/**
 * представлення базової реалізації нейронної мережі
 */
class BaseNeuralNetwork(
    private val inputSize: Int,
    private val hiddenLayers: List<Int>,
    private val outputSize: Int,
    private val activationFunction: String = "sigmoid"
) : NeuralNetwork {
    
    private val weights = mutableListOf<Array<DoubleArray>>()
    private val biases = mutableListOf<DoubleArray>()
    private var isTrained = false
    
    init {
        initializeNetwork()
    }
    
    private fun initializeNetwork() {
        val layerSizes = listOf(inputSize) + hiddenLayers + listOf(outputSize)
        
        for (i in 0 until layerSizes.size - 1) {
            val currentSize = layerSizes[i]
            val nextSize = layerSizes[i + 1]
            
            // Ініціалізація ваг
            val layerWeights = Array(nextSize) { DoubleArray(currentSize) }
            for (j in 0 until nextSize) {
                for (k in 0 until currentSize) {
                    layerWeights[j][k] = Random().nextGaussian() * sqrt(2.0 / currentSize)
                }
            }
            weights.add(layerWeights)
            
            // Ініціалізація зсувів
            biases.add(DoubleArray(nextSize) { Random().nextGaussian() * 0.1 })
        }
    }
    
    override fun train(trainingData: List<TrainingExample>, epochs: Int, learningRate: Double) {
        for (epoch in 0 until epochs) {
            var totalError = 0.0
            
            trainingData.forEach { example ->
                // Прямий прохід
                val (activations, zs) = forwardPass(example.input)
                
                // Зворотний прохід
                val error = backwardPass(activations, zs, example.output, learningRate)
                totalError += error
            }
            
            // Логування прогресу
            if (epoch % 100 == 0) {
                println("Epoch $epoch, Average Error: ${totalError / trainingData.size}")
            }
        }
        
        isTrained = true
    }
    
    override fun predict(input: DoubleArray): DoubleArray {
        if (!isTrained) {
            throw IllegalStateException("Network must be trained before prediction")
        }
        
        val (activations, _) = forwardPass(input)
        return activations.last()
    }
    
    override fun evaluate(testData: List<TrainingExample>): Double {
        if (!isTrained) {
            throw IllegalStateException("Network must be trained before evaluation")
        }
        
        var correctPredictions = 0
        
        testData.forEach { example ->
            val prediction = predict(example.input)
            if (isPredictionCorrect(prediction, example.output)) {
                correctPredictions++
            }
        }
        
        return correctPredictions.toDouble() / testData.size
    }
    
    override fun saveModel(filePath: String) {
        // Це заглушка для збереження моделі
        val modelData = mapOf(
            "inputSize" to inputSize,
            "hiddenLayers" to hiddenLayers,
            "outputSize" to outputSize,
            "activationFunction" to activationFunction,
            "weights" to weights.map { layer -> layer.map { it.toList() } },
            "biases" to biases.map { it.toList() }
        )
        
        File(filePath).writeText(modelData.toString())
    }
    
    override fun loadModel(filePath: String) {
        // Це заглушка для завантаження моделі
        val fileContent = File(filePath).readText()
        // Тут буде реалізація завантаження моделі з файлу
        isTrained = true
    }
    
    override fun getArchitecture(): NetworkArchitecture {
        return NetworkArchitecture(inputSize, hiddenLayers, outputSize, activationFunction)
    }
    
    /**
     * прямий прохід через мережу
     *
     * @param input вхідні дані
     * @return активації та z-значення
     */
    private fun forwardPass(input: DoubleArray): Pair<List<DoubleArray>, List<DoubleArray>> {
        val activations = mutableListOf<DoubleArray>()
        val zs = mutableListOf<DoubleArray>()
        
        var currentActivation = input
        activations.add(currentActivation)
        
        for (i in weights.indices) {
            val layerWeights = weights[i]
            val layerBiases = biases[i]
            
            val z = DoubleArray(layerWeights.size) { j ->
                var sum = layerBiases[j]
                for (k in currentActivation.indices) {
                    sum += layerWeights[j][k] * currentActivation[k]
                }
                sum
            }
            
            zs.add(z)
            currentActivation = z.map { applyActivation(it) }.toDoubleArray()
            activations.add(currentActivation)
        }
        
        return Pair(activations, zs)
    }
    
    /**
     * зворотний прохід через мережу
     *
     * @param activations активації
     * @param zs z-значення
     * @param expectedOutput очікуваний результат
     * @param learningRate швидкість навчання
     * @return помилка
     */
    private fun backwardPass(
        activations: List<DoubleArray>,
        zs: List<DoubleArray>,
        expectedOutput: DoubleArray,
        learningRate: Double
    ): Double {
        // Обчислення помилки на виході
        val output = activations.last()
        val outputErrors = DoubleArray(output.size) { i ->
            (output[i] - expectedOutput[i]) * applyActivationDerivative(zs.last()[i])
        }
        
        // Зворотне поширення помилки
        var errors = outputErrors
        for (i in weights.size - 1 downTo 0) {
            val layerWeights = weights[i]
            val layerBiases = biases[i]
            val prevActivations = activations[i]
            
            // Оновлення ваг та зсувів
            for (j in layerWeights.indices) {
                for (k in layerWeights[j].indices) {
                    val weightGradient = errors[j] * prevActivations[k]
                    layerWeights[j][k] -= learningRate * weightGradient
                }
                layerBiases[j] -= learningRate * errors[j]
            }
            
            // Обчислення помилок для попереднього шару
            if (i > 0) {
                val prevErrors = DoubleArray(prevActivations.size)
                for (j in layerWeights.indices) {
                    for (k in layerWeights[j].indices) {
                        prevErrors[k] += errors[j] * layerWeights[j][k] * applyActivationDerivative(zs[i - 1][k])
                    }
                }
                errors = prevErrors
            }
        }
        
        // Обчислення загальної помилки
        return outputErrors.sumOf { it * it } / outputErrors.size
    }
    
    /**
     * застосувати функцію активації
     *
     * @param x вхідне значення
     * @return результат активації
     */
    private fun applyActivation(x: Double): Double {
        return when (activationFunction.lowercase()) {
            "sigmoid" -> 1.0 / (1.0 + exp(-x))
            "tanh" -> tanh(x)
            "relu" -> max(0.0, x)
            "linear" -> x
            else -> 1.0 / (1.0 + exp(-x)) // sigmoid за замовчуванням
        }
    }
    
    /**
     * застосувати похідну функції активації
     *
     * @param x вхідне значення
     * @return похідна
     */
    private fun applyActivationDerivative(x: Double): Double {
        return when (activationFunction.lowercase()) {
            "sigmoid" -> {
                val sigmoid = 1.0 / (1.0 + exp(-x))
                sigmoid * (1.0 - sigmoid)
            }
            "tanh" -> 1.0 - tanh(x) * tanh(x)
            "relu" -> if (x > 0) 1.0 else 0.0
            "linear" -> 1.0
            else -> {
                val sigmoid = 1.0 / (1.0 + exp(-x))
                sigmoid * (1.0 - sigmoid)
            }
        }
    }
    
    /**
     * перевірити правильність передбачення
     *
     * @param prediction передбачення
     * @param expected очікуваний результат
     * @return true, якщо передбачення правильне
     */
    private fun isPredictionCorrect(prediction: DoubleArray, expected: DoubleArray): Boolean {
        // Для задач класифікації
        if (expected.size > 1) {
            val predictedClass = prediction.indices.maxByOrNull { prediction[it] } ?: 0
            val expectedClass = expected.indices.maxByOrNull { expected[it] } ?: 0
            return predictedClass == expectedClass
        } else {
            // Для задач регресії
            return abs(prediction[0] - expected[0]) < 0.1
        }
    }
    
    /**
     * отримати кількість параметрів мережі
     *
     * @return кількість параметрів
     */
    fun getParameterCount(): Int {
        var count = 0
        for (i in weights.indices) {
            count += weights[i].size * weights[i][0].size // Ваги
            count += biases[i].size // Зсуви
        }
        return count
    }
    
    /**
     * скинути ваги мережі
     */
    fun resetWeights() {
        weights.clear()
        biases.clear()
        initializeNetwork()
        isTrained = false
    }
}

/**
 * представлення інтерфейсу для роботи з генетичними алгоритмами
 */
interface GeneticAlgorithm {
    /**
     * запустити генетичний алгоритм
     *
     * @param populationSize розмір популяції
     * @param generations кількість поколінь
     * @param mutationRate швидкість мутації
     * @param crossoverRate швидкість схрещування
     * @return найкращий індивід
     */
    fun run(
        populationSize: Int,
        generations: Int,
        mutationRate: Double,
        crossoverRate: Double
    ): Individual

    /**
     * оцінити придатність індивіда
     *
     * @param individual індивід
     * @return придатність
     */
    fun evaluateFitness(individual: Individual): Double

    /**
     * схрещування двох індивідів
     *
     * @param parent1 батько 1
     * @param parent2 батько 2
     * @return нащадки
     */
    fun crossover(parent1: Individual, parent2: Individual): Pair<Individual, Individual>

    /**
     * мутація індивіда
     *
     * @param individual індивід
     * @param mutationRate швидкість мутації
     * @return мутований індивід
     */
    fun mutate(individual: Individual, mutationRate: Double): Individual

    /**
     * відібрати індивідів для наступного покоління
     *
     * @param population популяція
     * @param tournamentSize розмір турніру
     * @return відібраний індивід
     */
    fun select(population: List<Individual>, tournamentSize: Int): Individual
}

/**
 * представлення індивіда в генетичному алгоритмі
 */
data class Individual(
    val genes: DoubleArray,
    val fitness: Double = 0.0
)

/**
 * представлення базової реалізації генетичного алгоритму