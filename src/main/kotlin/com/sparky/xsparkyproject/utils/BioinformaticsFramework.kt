/**
 * Фреймворк для біоінформатики
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
import kotlin.math.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * представлення інтерфейсу для роботи з ДНК послідовностями
 */
interface DnaSequenceAnalyzer {
    /**
     * проаналізувати ДНК послідовність
     *
     * @param sequence послідовність
     * @return аналіз
     */
    fun analyzeDnaSequence(sequence: String): DnaAnalysisResult

    /**
     * знайти гени в послідовності
     *
     * @param sequence послідовність
     * @return список генів
     */
    fun findGenes(sequence: String): List<Gene>

    /**
     * транскрибувати ДНК в РНК
     *
     * @param dnaSequence ДНК послідовність
     * @return РНК послідовність
     */
    fun transcribeDnaToRna(dnaSequence: String): String

    /**
     * транслювати РНК в білок
     *
     * @param rnaSequence РНК послідовність
     * @return білок
     */
    fun translateRnaToProtein(rnaSequence: String): String

    /**
     * порівняти дві послідовності
     *
     * @param seq1 послідовність 1
     * @param seq2 послідовність 2
     * @return результат порівняння
     */
    fun compareSequences(seq1: String, seq2: String): SequenceComparisonResult

    /**
     * знайти мотиви в послідовності
     *
     * @param sequence послідовність
     * @param motifs мотиви
     * @return знайдені мотиви
     */
    fun findMotifs(sequence: String, motifs: List<String>): List<MotifMatch>

    /**
     * обчислити GC-вміст
     *
     * @param sequence послідовність
     * @return GC-вміст
     */
    fun calculateGcContent(sequence: String): Double
}

/**
 * представлення результату аналізу ДНК
 */
data class DnaAnalysisResult(
    val sequenceLength: Int,
    val nucleotideCounts: Map<Char, Int>,
    val gcContent: Double,
    val meltingTemperature: Double,
    val molecularWeight: Double
)

/**
 * представлення гена
 */
data class Gene(
    val name: String,
    val startPosition: Int,
    val endPosition: Int,
    val strand: String,
    val sequence: String,
    val proteinSequence: String?
)

/**
 * представлення результату порівняння послідовностей
 */
data class SequenceComparisonResult(
    val similarity: Double,
    val identity: Double,
    val gaps: Int,
    val matches: Int,
    val mismatches: Int
)

/**
 * представлення збігу мотиву
 */
data class MotifMatch(
    val motif: String,
    val position: Int,
    val sequence: String,
    val score: Double
)

/**
 * представлення базової реалізації аналізатора ДНК послідовностей