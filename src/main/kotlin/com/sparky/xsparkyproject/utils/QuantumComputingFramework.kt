/**
 * Фреймворк для квантових обчислень
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.*
import kotlinx.coroutines.*
import java.math.BigInteger

/**
 * представлення інтерфейсу для роботи з квантовими бітами
 */
interface Qubit {
    /**
     * отримати амплітуду стану |0⟩
     *
     * @return амплітуда
     */
    fun getAlpha(): ComplexNumber

    /**
     * отримати амплітуду стану |1⟩
     *
     * @return амплітуда
     */
    fun getBeta(): ComplexNumber

    /**
     * виміряти кубіт
     *
     * @return результат вимірювання (0 або 1)
     */
    fun measure(): Int

    /**
     * застосувати квантовий гейт
     *
     * @param gate гейт
     * @return новий кубіт
     */
    fun applyGate(gate: QuantumGate): Qubit

    /**
     * отримати ймовірність стану |0⟩
     *
     * @return ймовірність
     */
    fun getProbabilityZero(): Double

    /**
     * отримати ймовірність стану |1⟩
     *
     * @return ймовірність
     */
    fun getProbabilityOne(): Double
}

/**
 * представлення комплексного числа
 */
data class ComplexNumber(val real: Double, val imaginary: Double) {
    /**
     * додати комплексне число
     *
     * @param other інше число
     * @return сума
     */
    fun add(other: ComplexNumber): ComplexNumber {
        return ComplexNumber(real + other.real, imaginary + other.imaginary)
    }

    /**
     * помножити комплексне число
     *
     * @param other інше число
     * @return добуток
     */
    fun multiply(other: ComplexNumber): ComplexNumber {
        return ComplexNumber(
            real * other.real - imaginary * other.imaginary,
            real * other.imaginary + imaginary * other.real
        )
    }

    /**
     * отримати модуль комплексного числа
     *
     * @return модуль
     */
    fun magnitude(): Double {
        return sqrt(real * real + imaginary * imaginary)
    }

    /**
     * отримати комплексне спряження
     *
     * @return спряження
     */
    fun conjugate(): ComplexNumber {
        return ComplexNumber(real, -imaginary)
    }

    override fun toString(): String {
        return if (imaginary >= 0) {
            "$real+${imaginary}i"
        } else {
            "$real${imaginary}i"
        }
    }
}

/**
 * представлення базової реалізації кубіта
 */
class BaseQubit(private val alpha: ComplexNumber, private val beta: ComplexNumber) : Qubit {
    
    init {
        // Перевірка нормалізації
        val norm = alpha.magnitude() * alpha.magnitude() + beta.magnitude() * beta.magnitude()
        if (abs(norm - 1.0) > 1e-10) {
            throw IllegalArgumentException("Qubit must be normalized")
        }
    }
    
    override fun getAlpha(): ComplexNumber = alpha
    
    override fun getBeta(): ComplexNumber = beta
    
    override fun measure(): Int {
        val probabilityZero = getProbabilityZero()
        val random = Random().nextDouble()
        return if (random < probabilityZero) 0 else 1
    }
    
    override fun applyGate(gate: QuantumGate): Qubit {
        return gate.applyTo(this)
    }
    
    override fun getProbabilityZero(): Double {
        return alpha.magnitude().pow(2)
    }
    
    override fun getProbabilityOne(): Double {
        return beta.magnitude().pow(2)
    }
    
    /**
     * отримати вектор стану
     *
     * @return вектор стану
     */
    fun getStateVector(): Pair<ComplexNumber, ComplexNumber> {
        return Pair(alpha, beta)
    }
    
    /**
     * перевірити, чи є кубіт класичним
     *
     * @return true, якщо кубіт класичний
     */
    fun isClassical(): Boolean {
        return abs(alpha.imaginary) < 1e-10 && abs(beta.imaginary) < 1e-10 &&
               (abs(alpha.real) < 1e-10 || abs(beta.real) < 1e-10)
    }
}

/**
 * представлення інтерфейсу для квантового гейта
 */
interface QuantumGate {
    /**
     * застосувати гейт до кубіта
     *
     * @param qubit кубіт
     * @return новий кубіт
     */
    fun applyTo(qubit: Qubit): Qubit

    /**
     * отримати матричне представлення гейта
     *
     * @return матриця
     */
    fun getMatrix(): Array<Array<ComplexNumber>>

    /**
     * отримати назву гейта
     *
     * @return назва
     */
    fun getName(): String
}

/**
 * представлення базової реалізації квантового гейта
 */
abstract class BaseQuantumGate(protected val matrix: Array<Array<ComplexNumber>>, protected val name: String) : QuantumGate {
    
    override fun getMatrix(): Array<Array<ComplexNumber>> = matrix
    
    override fun getName(): String = name
    
    override fun applyTo(qubit: Qubit): Qubit {
        val alpha = qubit.getAlpha()
        val beta = qubit.getBeta()
        
        // Матричне множення
        val newAlpha = matrix[0][0].multiply(alpha).add(matrix[0][1].multiply(beta))
        val newBeta = matrix[1][0].multiply(alpha).add(matrix[1][1].multiply(beta))
        
        return BaseQubit(newAlpha, newBeta)
    }
}

/**
 * представлення гейта Адамара
 */
class HadamardGate : BaseQuantumGate(
    matrix = arrayOf(
        arrayOf(ComplexNumber(1.0 / sqrt(2.0), 0.0), ComplexNumber(1.0 / sqrt(2.0), 0.0)),
        arrayOf(ComplexNumber(1.0 / sqrt(2.0), 0.0), ComplexNumber(-1.0 / sqrt(2.0), 0.0))
    ),
    name = "Hadamard"
)

/**
 * представлення гейта Паулі-X (NOT)
 */
class PauliXGate : BaseQuantumGate(
    matrix = arrayOf(
        arrayOf(ComplexNumber(0.0, 0.0), ComplexNumber(1.0, 0.0)),
        arrayOf(ComplexNumber(1.0, 0.0), ComplexNumber(0.0, 0.0))
    ),
    name = "Pauli-X"
)

/**
 * представлення гейта Паулі-Y
 */
class PauliYGate : BaseQuantumGate(
    matrix = arrayOf(
        arrayOf(ComplexNumber(0.0, 0.0), ComplexNumber(0.0, -1.0)),
        arrayOf(ComplexNumber(0.0, 1.0), ComplexNumber(0.0, 0.0))
    ),
    name = "Pauli-Y"
)

/**
 * представлення гейта Паулі-Z
 */
class PauliZGate : BaseQuantumGate(
    matrix = arrayOf(
        arrayOf(ComplexNumber(1.0, 0.0), ComplexNumber(0.0, 0.0)),
        arrayOf(ComplexNumber(0.0, 0.0), ComplexNumber(-1.0, 0.0))
    ),
    name = "Pauli-Z"
)

/**
 * представлення фазового гейта
 */
class PhaseGate(private val phase: Double) : BaseQuantumGate(
    matrix = arrayOf(
        arrayOf(ComplexNumber(1.0, 0.0), ComplexNumber(0.0, 0.0)),
        arrayOf(ComplexNumber(0.0, 0.0), ComplexNumber(cos(phase), sin(phase)))
    ),
    name = "Phase"
)

/**
 * представлення квантової системи
 */
class QuantumSystem(private val numQubits: Int) {
    private val qubits = mutableListOf<Qubit>()
    private val gates = ConcurrentHashMap<String, QuantumGate>()
    
    init {
        // Ініціалізація кубітів у стані |0⟩
        for (i in 0 until numQubits) {
            qubits.add(BaseQubit(ComplexNumber(1.0, 0.0), ComplexNumber(0.0, 0.0)))
        }
        
        // Реєстрація стандартних гейтів
        registerGate("H", HadamardGate())
        registerGate("X", PauliXGate())
        registerGate("Y", PauliYGate())
        registerGate("Z", PauliZGate())
    }
    
    /**
     * отримати кубіт
     *
     * @param index індекс
     * @return кубіт
     */
    fun getQubit(index: Int): Qubit {
        if (index < 0 || index >= numQubits) {
            throw IndexOutOfBoundsException("Qubit index out of bounds")
        }
        return qubits[index]
    }
    
    /**
     * застосувати гейт до кубіта
     *
     * @param gateName назва гейта
     * @param qubitIndex індекс кубіта
     */
    fun applyGate(gateName: String, qubitIndex: Int) {
        if (qubitIndex < 0 || qubitIndex >= numQubits) {
            throw IndexOutOfBoundsException("Qubit index out of bounds")
        }
        
        val gate = gates[gateName]
        if (gate == null) {
            throw IllegalArgumentException("Gate not found: $gateName")
        }
        
        val newQubit = qubits[qubitIndex].applyGate(gate)
        qubits[qubitIndex] = newQubit
    }
    
    /**
     * зареєструвати гейт
     *
     * @param name назва
     * @param gate гейт
     */
    fun registerGate(name: String, gate: QuantumGate) {
        gates[name] = gate
    }
    
    /**
     * виміряти всі кубіти
     *
     * @return результати вимірювань
     */
    fun measureAll(): List<Int> {
        return qubits.map { it.measure() }
    }
    
    /**
     * виміряти конкретний кубіт
     *
     * @param index індекс
     * @return результат вимірювання
     */
    fun measureQubit(index: Int): Int {
        if (index < 0 || index >= numQubits) {
            throw IndexOutOfBoundsException("Qubit index out of bounds")
        }
        return qubits[index].measure()
    }
    
    /**
     * отримати кількість кубітів
     *
     * @return кількість
     */
    fun getNumQubits(): Int = numQubits
    
    /**
     * отримати список зареєстрованих гейтів
     *
     * @return список гейтів
     */
    fun getRegisteredGates(): List<String> = gates.keys.toList()
    
    /**
     * скинути систему
     */
    fun reset() {
        for (i in 0 until numQubits) {
            qubits[i] = BaseQubit(ComplexNumber(1.0, 0.0), ComplexNumber(0.0, 0.0))
        }
    }
    
    /**
     * отримати стан системи як рядок
     *
     * @return стан
     */
    fun getStateString(): String {
        return qubits.joinToString(", ") { qubit ->
            "|${qubit.getAlpha()}|0⟩ + |${qubit.getBeta()}|1⟩"
        }
    }
}

/**
 * представлення інтерфейсу для квантового алгоритму