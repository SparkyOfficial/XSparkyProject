package com.sparky.xsparkyproject.data

/**
 * проста реалізація dataframe для обробки даних
 * схожа на pandas але для kotlin
 *
 * @author Андрій Будильников
 */
class DataFrame<T> private constructor(private val data: List<Map<String, T>>) {
    
    val size: Int
        get() = data.size
    
    companion object {
        /**
         * створює dataframe зі списку мапінгів
         */
        fun <T> of(data: List<Map<String, T>>): DataFrame<T> {
            return DataFrame(data)
        }
        
        /**
         * створює порожній dataframe
         */
        fun <T> empty(): DataFrame<T> {
            return DataFrame(emptyList())
        }
        
        /**
         * створює dataframe з csv-подібного рядка
         */
        fun fromCsv(csv: String, separator: String = ","): DataFrame<String> {
            val lines = csv.trim().split("\n")
            if (lines.isEmpty()) return empty()
            
            val headers = lines[0].split(separator)
            val data = lines.drop(1).map { line ->
                val values = line.split(separator)
                headers.zip(values).toMap()
            }
            
            return DataFrame(data)
        }
    }
    
    /**
     * отримати колонки як список
     */
    fun getColumns(): Set<String> {
        return if (data.isEmpty()) emptySet() else data[0].keys
    }
    
    /**
     * отримати рядки як список
     */
    fun getRows(): List<Map<String, T>> {
        return data.toList()
    }
    
    /**
     * отримати значення колонки за ім'ям
     */
    fun getColumn(name: String): List<T?> {
        return data.map { it[name] }
    }
    
    /**
     * фільтрування даних за умовою
     */
    fun filter(predicate: (Map<String, T>) -> Boolean): DataFrame<T> {
        return DataFrame(data.filter(predicate))
    }
    
    /**
     * перетворення даних
     */
    fun <R> map(transform: (Map<String, T>) -> Map<String, R>): DataFrame<R> {
        return DataFrame(data.map(transform))
    }
    
    /**
     * об'єднання двох dataframe
     */
    fun merge(other: DataFrame<T>): DataFrame<T> {
        val mergedData = this.data + other.data
        return DataFrame(mergedData)
    }
    
    /**
     * групування даних за ключем
     */
    fun <K> groupBy(keySelector: (Map<String, T>) -> K): Map<K, DataFrame<T>> {
        return data.groupBy(keySelector).mapValues { (_, group) -> DataFrame(group) }
    }
    
    /**
     * сортування даних за компаратором
     */
    fun sortedWith(comparator: Comparator<Map<String, T>>): DataFrame<T> {
        return DataFrame(data.sortedWith(comparator))
    }
    
    /**
     * обмеження кількості рядків
     */
    fun limit(n: Int): DataFrame<T> {
        return DataFrame(data.take(n))
    }
    
    /**
     * пропустити перші n рядків
     */
    fun skip(n: Int): DataFrame<T> {
        return DataFrame(data.drop(n))
    }
    
    override fun toString(): String {
        return "DataFrame(size=$size, columns=${getColumns()})"
    }
}