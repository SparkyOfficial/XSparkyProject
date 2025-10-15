/**
 * фреймворк для обробки даних
 *
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.util.*
import java.util.concurrent.*
import java.util.stream.*
import kotlin.math.*

/**
 * представлення інтерфейсу для роботи з датафреймами
 */
interface DataFrame {
    /**
     * отримати кількість рядків
     *
     * @return кількість рядків
     */
    fun rowCount(): Int
    
    /**
     * отримати кількість стовпців
     *
     * @return кількість стовпців
     */
    fun columnCount(): Int
    
    /**
     * отримати назви стовпців
     *
     * @return список назв
     */
    fun columnNames(): List<String>
    
    /**
     * отримати значення за координатами
     *
     * @param row рядок
     * @param column стовпець
     * @return значення
     */
    fun get(row: Int, column: Int): Any?
    
    /**
     * отримати значення за координатами
     *
     * @param row рядок
     * @param columnName назва стовпця
     * @return значення
     */
    fun get(row: Int, columnName: String): Any?
    
    /**
     * встановити значення за координатами
     *
     * @param row рядок
     * @param column стовпець
     * @param value значення
     */
    fun set(row: Int, column: Int, value: Any?)
    
    /**
     * встановити значення за координатами
     *
     * @param row рядок
     * @param columnName назва стовпця
     * @param value значення
     */
    fun set(row: Int, columnName: String, value: Any?)
    
    /**
     * отримати стовпець як список
     *
     * @param column індекс стовпця
     * @return список значень
     */
    fun getColumn(column: Int): List<Any?>
    
    /**
     * отримати стовпець як список
     *
     * @param columnName назва стовпця
     * @return список значень
     */
    fun getColumn(columnName: String): List<Any?>
    
    /**
     * отримати рядок як мапу
     *
     * @param row індекс рядка
     * @return мапа значень
     */
    fun getRow(row: Int): Map<String, Any?>
    
    /**
     * додати стовпець
     *
     * @param columnName назва стовпця
     * @param values значення
     */
    fun addColumn(columnName: String, values: List<Any?>)
    
    /**
     * видалити стовпець
     *
     * @param columnName назва стовпця
     */
    fun removeColumn(columnName: String)
    
    /**
     * фільтрувати рядки
     *
     * @param predicate предикат
     * @return новий датафрейм
     */
    fun filter(predicate: (Map<String, Any?>) -> Boolean): DataFrame
    
    /**
     * трансформувати рядки
     *
     * @param transformer трансформер
     * @return новий датафрейм
     */
    fun map(transformer: (Map<String, Any?>) -> Map<String, Any?>): DataFrame
    
    /**
     * згрупувати за стовпцями
     *
     * @param columns стовпці
     * @return групований датафрейм
     */
    fun groupBy(vararg columns: String): GroupedDataFrame
}

/**
 * представлення групованого датафрейму
 */
interface GroupedDataFrame {
    /**
     * обчислити агрегації
     *
     * @param aggregations агрегації
     * @return датафрейм з агрегаціями
     */
    fun agg(vararg aggregations: Pair<String, AggregationFunction>): DataFrame
    
    /**
     * обчислити агрегації для всіх числових стовпців
     *
     * @param function функція агрегації
     * @return датафрейм з агрегаціями
     */
    fun aggAll(function: AggregationFunction): DataFrame
}

/**
 * представлення функції агрегації
 */
interface AggregationFunction {
    /**
     * застосувати функцію до списку значень
     *
     * @param values значення
     * @return результат
     */
    fun apply(values: List<Any?>): Any?
}

/**
 * представлення базової реалізації датафрейму
 */
open class BaseDataFrame(
    private val data: MutableList<MutableMap<String, Any?>>,
    private val columnNames: MutableList<String>
) : DataFrame {
    
    constructor() : this(mutableListOf(), mutableListOf())
    
    override fun rowCount(): Int = data.size
    
    override fun columnCount(): Int = columnNames.size
    
    override fun columnNames(): List<String> = columnNames.toList()
    
    override fun get(row: Int, column: Int): Any? {
        require(row >= 0 && row < data.size) { "Невірний індекс рядка: $row" }
        require(column >= 0 && column < columnNames.size) { "Невірний індекс стовпця: $column" }
        return data[row][columnNames[column]]
    }
    
    override fun get(row: Int, columnName: String): Any? {
        require(row >= 0 && row < data.size) { "Невірний індекс рядка: $row" }
        require(columnNames.contains(columnName)) { "Стовпець не знайдено: $columnName" }
        return data[row][columnName]
    }
    
    override fun set(row: Int, column: Int, value: Any?) {
        require(row >= 0 && row < data.size) { "Невірний індекс рядка: $row" }
        require(column >= 0 && column < columnNames.size) { "Невірний індекс стовпця: $column" }
        data[row][columnNames[column]] = value
    }
    
    override fun set(row: Int, columnName: String, value: Any?) {
        require(row >= 0 && row < data.size) { "Невірний індекс рядка: $row" }
        require(columnNames.contains(columnName)) { "Стовпець не знайдено: $columnName" }
        data[row][columnName] = value
    }
    
    override fun getColumn(column: Int): List<Any?> {
        require(column >= 0 && column < columnNames.size) { "Невірний індекс стовпця: $column" }
        return data.map { it[columnNames[column]] }
    }
    
    override fun getColumn(columnName: String): List<Any?> {
        require(columnNames.contains(columnName)) { "Стовпець не знайдено: $columnName" }
        return data.map { it[columnName] }
    }
    
    override fun getRow(row: Int): Map<String, Any?> {
        require(row >= 0 && row < data.size) { "Невірний індекс рядка: $row" }
        return data[row].toMap()
    }
    
    override fun addColumn(columnName: String, values: List<Any?>) {
        require(values.size == data.size || data.isEmpty()) { 
            "Кількість значень не відповідає кількості рядків" 
        }
        
        if (columnNames.contains(columnName)) {
            throw IllegalArgumentException("Стовпець вже існує: $columnName")
        }
        
        columnNames.add(columnName)
        
        if (data.isEmpty() && values.isNotEmpty()) {
            // Створити нові рядки
            repeat(values.size) {
                data.add(mutableMapOf())
            }
        }
        
        // Додати значення до існуючих рядків
        for (i in data.indices) {
            data[i][columnName] = values.getOrNull(i)
        }
    }
    
    override fun removeColumn(columnName: String) {
        if (!columnNames.remove(columnName)) {
            throw IllegalArgumentException("Стовпець не знайдено: $columnName")
        }
        
        // Видалити значення з усіх рядків
        data.forEach { row ->
            row.remove(columnName)
        }
    }
    
    override fun filter(predicate: (Map<String, Any?>) -> Boolean): DataFrame {
        val filteredData = data.filter { predicate(it.toMap()) }
        return BaseDataFrame(filteredData.toMutableList(), columnNames.toMutableList())
    }
    
    override fun map(transformer: (Map<String, Any?>) -> Map<String, Any?>): DataFrame {
        val mappedData = data.map { transformer(it.toMap()) }
        val newColumnNames = mappedData.flatMap { it.keys }.distinct().toMutableList()
        val newData = mappedData.map { row ->
            val newRow = mutableMapOf<String, Any?>()
            newColumnNames.forEach { columnName ->
                newRow[columnName] = row[columnName]
            }
            newRow
        }.toMutableList()
        
        return BaseDataFrame(newData, newColumnNames)
    }
    
    override fun groupBy(vararg columns: String): GroupedDataFrame {
        columns.forEach { column ->
            require(columnNames.contains(column)) { "Стовпець не знайдено: $column" }
        }
        
        val groups = mutableMapOf<Map<String, Any?>, MutableList<Map<String, Any?>>>()
        
        data.forEach { row ->
            val key = columns.associateWith { row[it] }
            groups.getOrPut(key) { mutableListOf() }.add(row.toMap())
        }
        
        return BaseGroupedDataFrame(groups, columnNames.toMutableList())
    }
    
    /**
     * додати рядок
     *
     * @param row рядок
     */
    fun addRow(row: Map<String, Any?>) {
        val newRow = mutableMapOf<String, Any?>()
        columnNames.forEach { columnName ->
            newRow[columnName] = row[columnName]
        }
        data.add(newRow)
    }
    
    /**
     * видалити рядок
     *
     * @param row індекс рядка
     * @return видалений рядок
     */
    fun removeRow(row: Int): Map<String, Any?> {
        require(row >= 0 && row < data.size) { "Невірний індекс рядка: $row" }
        return data.removeAt(row)
    }
    
    /**
     * сортувати за стовпцем
     *
     * @param columnName назва стовпця
     * @param ascending порядок сортування
     * @return новий датафрейм
     */
    fun sortBy(columnName: String, ascending: Boolean = true): DataFrame {
        require(columnNames.contains(columnName)) { "Стовпець не знайдено: $columnName" }
        
        val sortedData = if (ascending) {
            data.sortedBy { it[columnName] }
        } else {
            data.sortedByDescending { it[columnName] }
        }
        
        return BaseDataFrame(sortedData.toMutableList(), columnNames.toMutableList())
    }
    
    /**
     * об'єднати з іншим датафреймом
     *
     * @param other інший датафрейм
     * @param joinType тип об'єднання
     * @param on ключ об'єднання
     * @return новий датафрейм
     */
    fun join(other: DataFrame, joinType: JoinType = JoinType.INNER, on: String? = null): DataFrame {
        // Заглушка для реалізації об'єднання
        return BaseDataFrame()
    }
}

/**
 * представлення типу об'єднання
 */
enum class JoinType {
    INNER,
    LEFT,
    RIGHT,
    OUTER
}

/**
 * представлення базової реалізації групованого датафрейму
 */
class BaseGroupedDataFrame(
    private val groups: Map<Map<String, Any?>, List<Map<String, Any?>>>,
    private val allColumnNames: List<String>
) : GroupedDataFrame {
    
    override fun agg(vararg aggregations: Pair<String, AggregationFunction>): DataFrame {
        val resultData = mutableListOf<MutableMap<String, Any?>>()
        val resultColumnNames = mutableListOf<String>()
        
        // Додати ключі групування
        val groupKeys = groups.keys.firstOrNull()?.keys ?: emptySet()
        resultColumnNames.addAll(groupKeys)
        
        // Додати назви стовпців агрегацій
        aggregations.forEach { (columnName, _) ->
            resultColumnNames.add(columnName)
        }
        
        // Обчислити агрегації для кожної групи
        groups.forEach { (groupKey, groupData) ->
            val row = mutableMapOf<String, Any?>()
            
            // Додати значення ключів групування
            groupKey.forEach { (key, value) ->
                row[key] = value
            }
            
            // Обчислити агрегації
            aggregations.forEach { (columnName, function) ->
                val values = groupData.map { it[columnName] }
                row[columnName] = function.apply(values)
            }
            
            resultData.add(row)
        }
        
        return BaseDataFrame(resultData, resultColumnNames)
    }
    
    override fun aggAll(function: AggregationFunction): DataFrame {
        val resultData = mutableListOf<MutableMap<String, Any?>>()
        val resultColumnNames = mutableListOf<String>()
        
        // Додати ключі групування
        val groupKeys = groups.keys.firstOrNull()?.keys ?: emptySet()
        resultColumnNames.addAll(groupKeys)
        
        // Додати всі числові стовпці
        val numericColumns = allColumnNames.filter { columnName ->
            groups.values.firstOrNull()?.firstOrNull()?.get(columnName) is Number
        }
        resultColumnNames.addAll(numericColumns)
        
        // Обчислити агрегації для кожної групи
        groups.forEach { (groupKey, groupData) ->
            val row = mutableMapOf<String, Any?>()
            
            // Додати значення ключів групування
            groupKey.forEach { (key, value) ->
                row[key] = value
            }
            
            // Обчислити агрегації для всіх числових стовпців
            numericColumns.forEach { columnName ->
                val values = groupData.map { it[columnName] }
                row[columnName] = function.apply(values)
            }
            
            resultData.add(row)
        }
        
        return BaseDataFrame(resultData, resultColumnNames)
    }
}

/**
 * представлення функції агрегації суми
 */
class SumAggregation : AggregationFunction {
    override fun apply(values: List<Any?>): Any? {
        return values.filterIsInstance<Number>().sumOf { it.toDouble() }
    }
}

/**
 * представлення функції агрегації середнього
 */
class MeanAggregation : AggregationFunction {
    override fun apply(values: List<Any?>): Any? {
        val numbers = values.filterIsInstance<Number>()
        return if (numbers.isNotEmpty()) numbers.sumOf { it.toDouble() } / numbers.size else null
    }
}

/**
 * представлення функції агрегації мінімуму
 */
class MinAggregation : AggregationFunction {
    override fun apply(values: List<Any?>): Any? {
        val numbers = values.filterIsInstance<Number>()
        return numbers.minOrNull()?.toDouble()
    }
}

/**
 * представлення функції агрегації максимуму
 */
class MaxAggregation : AggregationFunction {
    override fun apply(values: List<Any?>): Any? {
        val numbers = values.filterIsInstance<Number>()
        return numbers.maxOrNull()?.toDouble()
    }
}

/**
 * представлення функції агрегації кількості
 */
class CountAggregation : AggregationFunction {
    override fun apply(values: List<Any?>): Any? {
        return values.size
    }
}

/**
 * представлення інтерфейсу для роботи з серіями даних
 */
interface DataSeries<T> {
    /**
     * отримати розмір серії
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
    fun get(index: Int): T?
    
    /**
     * встановити елемент за індексом
     *
     * @param index індекс
     * @param value значення
     */
    fun set(index: Int, value: T?)
    
    /**
     * додати елемент
     *
     * @param value значення
     */
    fun add(value: T?)
    
    /**
     * видалити елемент за індексом
     *
     * @param index індекс
     * @return видалений елемент
     */
    fun removeAt(index: Int): T?
    
    /**
     * очистити серію
     */
    fun clear()
    
    /**
     * отримати всі значення
     *
     * @return список значень
     */
    fun values(): List<T?>
    
    /**
     * відфільтрувати значення
     *
     * @param predicate предикат
     * @return нова серія
     */
    fun filter(predicate: (T?) -> Boolean): DataSeries<T>
    
    /**
     * трансформувати значення
     *
     * @param transformer трансформер
     * @return нова серія
     */
    fun <R> map(transformer: (T?) -> R?): DataSeries<R>
    
    /**
     * згорнути значення
     *
     * @param initial початкове значення
     * @param operation операція
     * @return результат
     */
    fun <R> reduce(initial: R, operation: (R, T?) -> R): R
}

/**
 * представлення базової реалізації серії даних
 */
open class BaseDataSeries<T> : DataSeries<T> {
    protected val data = mutableListOf<T?>()
    
    override fun size(): Int = data.size
    
    override fun get(index: Int): T? = data.getOrNull(index)
    
    override fun set(index: Int, value: T?) {
        if (index >= data.size) {
            data.addAll(List(index - data.size + 1) { null })
        }
        data[index] = value
    }
    
    override fun add(value: T?) {
        data.add(value)
    }
    
    override fun removeAt(index: Int): T? {
        return if (index >= 0 && index < data.size) data.removeAt(index) else null
    }
    
    override fun clear() {
        data.clear()
    }
    
    override fun values(): List<T?> = data.toList()
    
    override fun filter(predicate: (T?) -> Boolean): DataSeries<T> {
        val filtered = data.filter(predicate)
        val series = BaseDataSeries<T>()
        filtered.forEach { series.add(it) }
        return series
    }
    
    override fun <R> map(transformer: (T?) -> R?): DataSeries<R> {
        val mapped = data.map(transformer)
        val series = BaseDataSeries<R>()
        mapped.forEach { series.add(it) }
        return series
    }
    
    override fun <R> reduce(initial: R, operation: (R, T?) -> R): R {
        return data.fold(initial, operation)
    }
    
    /**
     * отримати статистику серії
     *
     * @return статистика
     */
    fun getStatistics(): SeriesStatistics? {
        val numbers = data.filterIsInstance<Number>().map { it.toDouble() }
        if (numbers.isEmpty()) return null
        
        return SeriesStatistics(
            count = numbers.size,
            mean = numbers.average(),
            min = numbers.minOrNull() ?: 0.0,
            max = numbers.maxOrNull() ?: 0.0,
            stdDev = sqrt(numbers.map { (it - numbers.average()).pow(2) }.average())
        )
    }
}

/**
 * представлення статистики серії
 *
 * @property count кількість елементів
 * @property mean середнє значення
 * @property min мінімальне значення
 * @property max максимальне значення
 * @property stdDev стандартне відхилення
 */
data class SeriesStatistics(
    val count: Int,
    val mean: Double,
    val min: Double,
    val max: Double,
    val stdDev: Double
)

/**
 * представлення інтерфейсу для роботи з часовими рядами
 */
interface TimeSeries<T> : DataSeries<T> {
    /**
     * отримати часову мітку за індексом
     *
     * @param index індекс
     * @return часова мітка
     */
    fun getTimestamp(index: Int): Long?
    
    /**
     * встановити часову мітку за індексом
     *
     * @param index індекс
     * @param timestamp часова мітка
     */
    fun setTimestamp(index: Int, timestamp: Long)
    
    /**
     * додати елемент з часовою міткою
     *
     * @param timestamp часова мітка
     * @param value значення
     */
    fun add(timestamp: Long, value: T?)
    
    /**
     * отримати значення за часовим діапазоном
     *
     * @param start початок
     * @param end кінець
     * @return список значень
     */
    fun getRange(start: Long, end: Long): List<Pair<Long, T?>>
    
    /**
     * інтерполювати значення
     *
     * @param timestamp часова мітка
     * @return інтерпольоване значення
     */
    fun interpolate(timestamp: Long): T?
}

/**
 * представлення базової реалізації часових рядів
 */
open class BaseTimeSeries<T> : BaseDataSeries<T>(), TimeSeries<T> {
    private val timestamps = mutableListOf<Long?>()
    
    override fun getTimestamp(index: Int): Long? {
        return timestamps.getOrNull(index)
    }
    
    override fun setTimestamp(index: Int, timestamp: Long) {
        if (index >= timestamps.size) {
            timestamps.addAll(List(index - timestamps.size + 1) { null })
        }
        timestamps[index] = timestamp
    }
    
    override fun add(timestamp: Long, value: T?) {
        super.add(value)
        timestamps.add(timestamp)
    }
    
    override fun add(value: T?) {
        super.add(value)
        timestamps.add(System.currentTimeMillis())
    }
    
    override fun set(index: Int, value: T?) {
        super.set(index, value)
        if (index >= timestamps.size) {
            timestamps.addAll(List(index - timestamps.size + 1) { null })
        }
        if (timestamps[index] == null) {
            timestamps[index] = System.currentTimeMillis()
        }
    }
    
    override fun removeAt(index: Int): T? {
        val value = super.removeAt(index)
        if (index < timestamps.size) {
            timestamps.removeAt(index)
        }
        return value
    }
    
    override fun clear() {
        super.clear()
        timestamps.clear()
    }
    
    override fun getRange(start: Long, end: Long): List<Pair<Long, T?>> {
        val result = mutableListOf<Pair<Long, T?>>()
        for (i in 0 until size()) {
            val timestamp = getTimestamp(i)
            if (timestamp != null && timestamp >= start && timestamp <= end) {
                result.add(Pair(timestamp, get(i)))
            }
        }
        return result
    }
    
    override fun interpolate(timestamp: Long): T? {
        // Знайти найближчі точки
        var prevIndex = -1
        var nextIndex = -1
        
        for (i in 0 until size()) {
            val ts = getTimestamp(i)
            if (ts != null) {
                if (ts <= timestamp) {
                    prevIndex = i
                } else if (ts > timestamp) {
                    nextIndex = i
                    break
                }
            }
        }
        
        if (prevIndex == -1 && nextIndex == -1) return null
        if (prevIndex == -1) return get(nextIndex)
        if (nextIndex == -1) return get(prevIndex)
        
        val prevTs = getTimestamp(prevIndex) ?: return null
        val nextTs = getTimestamp(nextIndex) ?: return null
        val prevValue = get(prevIndex)
        val nextValue = get(nextIndex)
        
        // Лінійна інтерполяція
        if (prevValue is Number && nextValue is Number) {
            val ratio = (timestamp - prevTs).toDouble() / (nextTs - prevTs)
            @Suppress("UNCHECKED_CAST")
            return (prevValue.toDouble() + ratio * (nextValue.toDouble() - prevValue.toDouble())) as T
        }
        
        // Якщо не числові значення, повертаємо найближче
        return if (timestamp - prevTs < nextTs - timestamp) prevValue else nextValue
    }
}

/**
 * представлення інтерфейсу для роботи з віконними функціями
 */
interface WindowFunction<T, R> {
    /**
     * застосувати функцію до вікна
     *
     * @param window вікно
     * @return результат
     */
    fun apply(window: List<T?>): R?
}

/**
 * представлення ковзного вікна
 */
class SlidingWindow<T> {
    private val data = mutableListOf<T?>()
    private val size: Int
    
    constructor(size: Int) {
        this.size = size
    }
    
    /**
     * додати елемент
     *
     * @param value значення
     */
    fun add(value: T?) {
        data.add(value)
        if (data.size > size) {
            data.removeAt(0)
        }
    }
    
    /**
     * отримати поточне вікно
     *
     * @return список значень
     */
    fun getWindow(): List<T?> = data.toList()
    
    /**
     * застосувати функцію до вікна
     *
     * @param function функція
     * @return результат
     */
    fun <R> apply(function: WindowFunction<T, R>): R? {
        return function.apply(data)
    }
    
    /**
     * очистити вікно
     */
    fun clear() {
        data.clear()
    }
    
    /**
     * отримати розмір вікна
     *
     * @return розмір
     */
    fun getSize(): Int = size
    
    /**
     * перевірити, чи вікно заповнене
     *
     * @return true, якщо заповнене
     */
    fun isFull(): Boolean = data.size == size
}

/**
 * представлення функції ковзного середнього
 */
class MovingAverageFunction : WindowFunction<Number, Double> {
    override fun apply(window: List<Number?>): Double? {
        val numbers = window.filterNotNull().filterIsInstance<Number>()
        return if (numbers.isNotEmpty()) numbers.sumOf { it.toDouble() } / numbers.size else null
    }
}

/**
 * представлення функції ковзного максимуму
 */
class MovingMaxFunction : WindowFunction<Number, Double> {
    override fun apply(window: List<Number?>): Double? {
        val numbers = window.filterNotNull().filterIsInstance<Number>()
        return numbers.maxOrNull()?.toDouble()
    }
}

/**
 * представлення функції ковзного мінімуму
 */
class MovingMinFunction : WindowFunction<Number, Double> {
    override fun apply(window: List<Number?>): Double? {
        val numbers = window.filterNotNull().filterIsInstance<Number>()
        return numbers.minOrNull()?.toDouble()
    }
}

/**
 * представлення інтерфейсу для роботи з потоковою обробкою даних