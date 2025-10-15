package com.sparky.xsparkyproject.utils

/**
 * набір утиліт для роботи з колекціями
 * включає розширення функціональності стандартних колекцій
 *
 * @author Андрій Будильников
 */
class CollectionUtils {
    companion object {
        /**
         * об'єднує дві колекції в одну
         * 
         * @param first перша колекція
         * @param second друга колекція
         * @return об'єднана колекція
         */
        fun <T> mergeCollections(first: Collection<T>, second: Collection<T>): List<T> {
            // реалізація об'єднання колекцій
            val result = mutableListOf<T>()
            result.addAll(first)
            result.addAll(second)
            return result
        }
        
        /**
         * фільтрує колекцію за предикатом
         * 
         * @param collection колекція для фільтрації
         * @param predicate предикат фільтрації
         * @return відфільтрована колекція
         */
        fun <T> filterCollection(collection: Collection<T>, predicate: (T) -> Boolean): List<T> {
            // реалізація фільтрації
            val result = mutableListOf<T>()
            for (item in collection) {
                if (predicate(item)) {
                    result.add(item)
                }
            }
            return result
        }
        
        /**
         * перетворює колекцію за функцією
         * 
         * @param collection колекція для перетворення
         * @param transform функція перетворення
         * @return перетворена колекція
         */
        fun <T, R> mapCollection(collection: Collection<T>, transform: (T) -> R): List<R> {
            // реалізація перетворення
            val result = mutableListOf<R>()
            for (item in collection) {
                result.add(transform(item))
            }
            return result
        }
        
        /**
         * згортає колекцію в одне значення
         * 
         * @param collection колекція для згортання
         * @param initial початкове значення
         * @param operation операція згортання
         * @return результат згортання
         */
        fun <T, R> reduceCollection(collection: Collection<T>, initial: R, operation: (R, T) -> R): R {
            // реалізація згортання
            var result = initial
            for (item in collection) {
                result = operation(result, item)
            }
            return result
        }
        
        /**
         * групує елементи колекції за ключем
         * 
         * @param collection колекція для групування
         * @param keySelector функція вибору ключа
         * @return групи елементів
         */
        fun <T, K> groupCollection(collection: Collection<T>, keySelector: (T) -> K): Map<K, List<T>> {
            // реалізація групування
            val groups = mutableMapOf<K, MutableList<T>>()
            for (item in collection) {
                val key = keySelector(item)
                if (!groups.containsKey(key)) {
                    groups[key] = mutableListOf()
                }
                groups[key]?.add(item)
            }
            return groups
        }
        
        /**
         * сортує колекцію за компаратором
         * 
         * @param collection колекція для сортування
         * @param comparator компаратор для сортування
         * @return відсортована колекція
         */
        fun <T> sortCollection(collection: Collection<T>, comparator: Comparator<T>): List<T> {
            // реалізація сортування
            val result = collection.toMutableList()
            result.sortWith(comparator)
            return result
        }
        
        /**
         * отримує перші n елементів колекції
         * 
         * @param collection колекція
         * @param n кількість елементів
         * @return перші n елементів
         */
        fun <T> takeFirst(collection: Collection<T>, n: Int): List<T> {
            // реалізація отримання перших елементів
            val result = mutableListOf<T>()
            var count = 0
            for (item in collection) {
                if (count >= n) break
                result.add(item)
                count++
            }
            return result
        }
        
        /**
         * пропускає перші n елементів колекції
         * 
         * @param collection колекція
         * @param n кількість елементів для пропуску
         * @return колекція без перших n елементів
         */
        fun <T> skipFirst(collection: Collection<T>, n: Int): List<T> {
            // реалізація пропуску елементів
            val result = mutableListOf<T>()
            var count = 0
            for (item in collection) {
                if (count >= n) {
                    result.add(item)
                }
                count++
            }
            return result
        }
        
        /**
         * отримує останні n елементів колекції
         * 
         * @param collection колекція
         * @param n кількість елементів
         * @return останні n елементів
         */
        fun <T> takeLast(collection: Collection<T>, n: Int): List<T> {
            // реалізація отримання останніх елементів
            val list = collection.toList()
            return if (list.size <= n) {
                list
            } else {
                list.subList(list.size - n, list.size)
            }
        }
        
        /**
         * пропускає останні n елементів колекції
         * 
         * @param collection колекція
         * @param n кількість елементів для пропуску
         * @return колекція без останніх n елементів
         */
        fun <T> skipLast(collection: Collection<T>, n: Int): List<T> {
            // реалізація пропуску останніх елементів
            val list = collection.toList()
            return if (list.size <= n) {
                emptyList()
            } else {
                list.subList(0, list.size - n)
            }
        }
        
        /**
         * отримує елементи колекції за індексами
         * 
         * @param collection колекція
         * @param indices індекси елементів
         * @return елементи за вказаними індексами
         */
        fun <T> getElementsByIndices(collection: Collection<T>, indices: Collection<Int>): List<T> {
            // реалізація отримання елементів за індексами
            val list = collection.toList()
            val result = mutableListOf<T>()
            for (index in indices) {
                if (index >= 0 && index < list.size) {
                    result.add(list[index])
                }
            }
            return result
        }
        
        /**
         * видаляє дублікати з колекції
         * 
         * @param collection колекція
         * @return колекція без дублікатів
         */
        fun <T> removeDuplicates(collection: Collection<T>): List<T> {
            // реалізація видалення дублікатів
            val result = mutableSetOf<T>()
            result.addAll(collection)
            return result.toList()
        }
        
        /**
         * перемішує елементи колекції
         * 
         * @param collection колекція
         * @return перемішана колекція
         */
        fun <T> shuffleCollection(collection: Collection<T>): List<T> {
            // реалізація перемішування
            val result = collection.toMutableList()
            // тут має бути реалізація перемішування
            return result
        }
        
        /**
         * розділяє колекцію на частини
         * 
         * @param collection колекція
         * @param size розмір кожної частини
         * @return список частин
         */
        fun <T> partitionCollection(collection: Collection<T>, size: Int): List<List<T>> {
            // реалізація розділення на частини
            val result = mutableListOf<List<T>>()
            val list = collection.toList()
            var i = 0
            while (i < list.size) {
                val end = minOf(i + size, list.size)
                result.add(list.subList(i, end))
                i += size
            }
            return result
        }
        
        /**
         * об'єднує колекції в одну
         * 
         * @param collections колекції для об'єднання
         * @return об'єднана колекція
         */
        fun <T> mergeMultipleCollections(collections: Collection<Collection<T>>): List<T> {
            // реалізація об'єднання кількох колекцій
            val result = mutableListOf<T>()
            for (collection in collections) {
                result.addAll(collection)
            }
            return result
        }
        
        /**
         * знаходить перетин двох колекцій
         * 
         * @param first перша колекція
         * @param second друга колекція
         * @return перетин колекцій
         */
        fun <T> intersectCollections(first: Collection<T>, second: Collection<T>): Set<T> {
            // реалізація знаходження перетину
            val set1 = first.toSet()
            val set2 = second.toSet()
            return set1.intersect(set2)
        }
        
        /**
         * знаходить різницю двох колекцій
         * 
         * @param first перша колекція
         * @param second друга колекція
         * @return різниця колекцій
         */
        fun <T> differenceCollections(first: Collection<T>, second: Collection<T>): Set<T> {
            // реалізація знаходження різниці
            val set1 = first.toSet()
            val set2 = second.toSet()
            return set1.subtract(set2)
        }
        
        /**
         * перевіряє чи всі елементи колекції задовольняють предикат
         * 
         * @param collection колекція
         * @param predicate предикат
         * @return true якщо всі елементи задовольняють предикат
         */
        fun <T> allMatch(collection: Collection<T>, predicate: (T) -> Boolean): Boolean {
            // реалізація перевірки всіх елементів
            for (item in collection) {
                if (!predicate(item)) {
                    return false
                }
            }
            return true
        }
        
        /**
         * перевіряє чи хоча б один елемент колекції задовольняє предикат
         * 
         * @param collection колекція
         * @param predicate предикат
         * @return true якщо хоча б один елемент задовольняє предикат
         */
        fun <T> anyMatch(collection: Collection<T>, predicate: (T) -> Boolean): Boolean {
            // реалізація перевірки хоча б одного елемента
            for (item in collection) {
                if (predicate(item)) {
                    return true
                }
            }
            return false
        }
        
        /**
         * перевіряє чи жоден елемент колекції не задовольняє предикат
         * 
         * @param collection колекція
         * @param predicate предикат
         * @return true якщо жоден елемент не задовольняє предикат
         */
        fun <T> noneMatch(collection: Collection<T>, predicate: (T) -> Boolean): Boolean {
            // реалізація перевірки відсутності елементів
            for (item in collection) {
                if (predicate(item)) {
                    return false
                }
            }
            return true
        }
        
        /**
         * отримує максимальний елемент колекції
         * 
         * @param collection колекція
         * @param comparator компаратор
         * @return максимальний елемент
         */
        fun <T> maxElement(collection: Collection<T>, comparator: Comparator<T>): T? {
            // реалізація знаходження максимального елемента
            if (collection.isEmpty()) return null
            var max = collection.first()
            for (item in collection) {
                if (comparator.compare(item, max) > 0) {
                    max = item
                }
            }
            return max
        }
        
        /**
         * отримує мінімальний елемент колекції
         * 
         * @param collection колекція
         * @param comparator компаратор
         * @return мінімальний елемент
         */
        fun <T> minElement(collection: Collection<T>, comparator: Comparator<T>): T? {
            // реалізація знаходження мінімального елемента
            if (collection.isEmpty()) return null
            var min = collection.first()
            for (item in collection) {
                if (comparator.compare(item, min) < 0) {
                    min = item
                }
            }
            return min
        }
        
        /**
         * обчислює суму числових елементів колекції
         * 
         * @param collection колекція чисел
         * @return сума елементів
         */
        fun sumCollection(collection: Collection<Number>): Double {
            // реалізація обчислення суми
            var sum = 0.0
            for (item in collection) {
                sum += item.toDouble()
            }
            return sum
        }
        
        /**
         * обчислює середнє значення числових елементів колекції
         * 
         * @param collection колекція чисел
         * @return середнє значення
         */
        fun averageCollection(collection: Collection<Number>): Double {
            // реалізація обчислення середнього
            if (collection.isEmpty()) return 0.0
            return sumCollection(collection) / collection.size
        }
        
        /**
         * обчислює медіану числових елементів колекції
         * 
         * @param collection колекція чисел
         * @return медіана
         */
        fun medianCollection(collection: Collection<Number>): Double {
            // реалізація обчислення медіани
            if (collection.isEmpty()) return 0.0
            val sorted = collection.map { it.toDouble() }.sorted()
            val size = sorted.size
            return if (size % 2 == 0) {
                (sorted[size / 2 - 1] + sorted[size / 2]) / 2.0
            } else {
                sorted[size / 2]
            }
        }
        
        /**
         * обчислює стандартне відхилення числових елементів колекції
         * 
         * @param collection колекція чисел
         * @return стандартне відхилення
         */
        fun standardDeviationCollection(collection: Collection<Number>): Double {
            // реалізація обчислення стандартного відхилення
            if (collection.isEmpty()) return 0.0
            val mean = averageCollection(collection)
            val squaredDifferences = collection.map { 
                val diff = it.toDouble() - mean
                diff * diff
            }
            val variance = squaredDifferences.sum() / collection.size
            return kotlin.math.sqrt(variance)
        }
    }
}