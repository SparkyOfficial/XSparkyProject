/**
 * xsparkyproject - розширена бібліотека для kotlin
 *
 * Copyright 2025 Андрій Будильников
 */

package com.sparky.xsparkyproject.utils

import java.io.*
import java.nio.file.*
import java.util.*
import java.util.concurrent.*
import kotlin.reflect.KClass

/**
 * утилітарний клас для роботи з конфігураціями
 *
 * @author андрій будильников
 * @since 1.0.0
 */
@Suppress("unused")
class ConfigUtils {
    
    companion object {
        // стандартні формати конфігураційних файлів
        const val FORMAT_PROPERTIES = "properties"
        const val FORMAT_JSON = "json"
        const val FORMAT_XML = "xml"
        const val FORMAT_YAML = "yaml"
        const val FORMAT_TOML = "toml"
        
        // стандартні імена файлів
        const val DEFAULT_CONFIG_FILENAME = "config.properties"
        const val DEFAULT_APP_CONFIG_FILENAME = "application.properties"
        
        // роздільники
        const val DEFAULT_KEY_VALUE_SEPARATOR = "="
        const val DEFAULT_COMMENT_PREFIX = "#"
        const val DEFAULT_SECTION_PREFIX = "["
        const val DEFAULT_SECTION_SUFFIX = "]"
    }
    
    // базові функції для роботи з конфігураціями
    
    /**
     * представлення конфігурації
     */
    interface Configuration {
        /**
         * отримує значення за ключем
         *
         * @param key ключ
         * @return значення або null якщо ключ не знайдено
         */
        fun get(key: String): String?
        
        /**
         * встановлює значення за ключем
         *
         * @param key ключ
         * @param value значення
         */
        fun set(key: String, value: String)
        
        /**
         * перевіряє, чи існує ключ
         *
         * @param key ключ
         * @return true якщо ключ існує
         */
        fun contains(key: String): Boolean
        
        /**
         * видаляє ключ
         *
         * @param key ключ
         * @return true якщо ключ був видалений
         */
        fun remove(key: String): Boolean
        
        /**
         * отримує всі ключі
         *
         * @return набір ключів
         */
        fun keys(): Set<String>
        
        /**
         * очищує конфігурацію
         */
        fun clear()
        
        /**
         * отримує кількість ключів
         *
         * @return кількість ключів
         */
        fun size(): Int
        
        /**
         * перевіряє, чи конфігурація порожня
         *
         * @return true якщо конфігурація порожня
         */
        fun isEmpty(): Boolean
    }
    
    /**
     * базова реалізація конфігурації
     */
    open class BaseConfiguration : Configuration {
        protected val properties = mutableMapOf<String, String>()
        
        override fun get(key: String): String? {
            return properties[key]
        }
        
        override fun set(key: String, value: String) {
            properties[key] = value
        }
        
        override fun contains(key: String): Boolean {
            return properties.containsKey(key)
        }
        
        override fun remove(key: String): Boolean {
            return properties.remove(key) != null
        }
        
        override fun keys(): Set<String> {
            return properties.keys
        }
        
        override fun clear() {
            properties.clear()
        }
        
        override fun size(): Int {
            return properties.size
        }
        
        override fun isEmpty(): Boolean {
            return properties.isEmpty()
        }
    }
    
    /**
     * представлення конфігурації з типізованими значеннями
     */
    class TypedConfiguration(private val config: Configuration) {
        
        /**
         * отримує значення типу String
         *
         * @param key ключ
         * @param defaultValue значення за замовчуванням
         * @return значення
         */
        fun getString(key: String, defaultValue: String = ""): String {
            return config.get(key) ?: defaultValue
        }
        
        /**
         * отримує значення типу Int
         *
         * @param key ключ
         * @param defaultValue значення за замовчуванням
         * @return значення
         */
        fun getInt(key: String, defaultValue: Int = 0): Int {
            return try {
                config.get(key)?.toInt() ?: defaultValue
            } catch (e: NumberFormatException) {
                defaultValue
            }
        }
        
        /**
         * отримує значення типу Long
         *
         * @param key ключ
         * @param defaultValue значення за замовчуванням
         * @return значення
         */
        fun getLong(key: String, defaultValue: Long = 0L): Long {
            return try {
                config.get(key)?.toLong() ?: defaultValue
            } catch (e: NumberFormatException) {
                defaultValue
            }
        }
        
        /**
         * отримує значення типу Double
         *
         * @param key ключ
         * @param defaultValue значення за замовчуванням
         * @return значення
         */
        fun getDouble(key: String, defaultValue: Double = 0.0): Double {
            return try {
                config.get(key)?.toDouble() ?: defaultValue
            } catch (e: NumberFormatException) {
                defaultValue
            }
        }
        
        /**
         * отримує значення типу Boolean
         *
         * @param key ключ
         * @param defaultValue значення за замовчуванням
         * @return значення
         */
        fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
            return config.get(key)?.toBoolean() ?: defaultValue
        }
        
        /**
         * отримує значення типу List<String>
         *
         * @param key ключ
         * @param separator роздільник
         * @param defaultValue значення за замовчуванням
         * @return значення
         */
        fun getStringList(key: String, separator: String = ",", defaultValue: List<String> = emptyList()): List<String> {
            val value = config.get(key)
            return if (value != null) {
                value.split(separator).map { it.trim() }
            } else {
                defaultValue
            }
        }
        
        /**
         * отримує значення типу Set<String>
         *
         * @param key ключ
         * @param separator роздільник
         * @param defaultValue значення за замовчуванням
         * @return значення
         */
        fun getStringSet(key: String, separator: String = ",", defaultValue: Set<String> = emptySet()): Set<String> {
            val value = config.get(key)
            return if (value != null) {
                value.split(separator).map { it.trim() }.toSet()
            } else {
                defaultValue
            }
        }
        
        /**
         * встановлює значення типу String
         *
         * @param key ключ
         * @param value значення
         */
        fun setString(key: String, value: String) {
            config.set(key, value)
        }
        
        /**
         * встановлює значення типу Int
         *
         * @param key ключ
         * @param value значення
         */
        fun setInt(key: String, value: Int) {
            config.set(key, value.toString())
        }
        
        /**
         * встановлює значення типу Long
         *
         * @param key ключ
         * @param value значення
         */
        fun setLong(key: String, value: Long) {
            config.set(key, value.toString())
        }
        
        /**
         * встановлює значення типу Double
         *
         * @param key ключ
         * @param value значення
         */
        fun setDouble(key: String, value: Double) {
            config.set(key, value.toString())
        }
        
        /**
         * встановлює значення типу Boolean
         *
         * @param key ключ
         * @param value значення
         */
        fun setBoolean(key: String, value: Boolean) {
            config.set(key, value.toString())
        }
        
        /**
         * встановлює значення типу List<String>
         *
         * @param key ключ
         * @param value значення
         * @param separator роздільник
         */
        fun setStringList(key: String, value: List<String>, separator: String = ",") {
            config.set(key, value.joinToString(separator))
        }
        
        /**
         * встановлює значення типу Set<String>
         *
         * @param key ключ
         * @param value значення
         * @param separator роздільник
         */
        fun setStringSet(key: String, value: Set<String>, separator: String = ",") {
            config.set(key, value.joinToString(separator))
        }
    }
    
    /**
     * створює типізовану конфігурацію
     *
     * @param config конфігурація
     * @return типізована конфігурація
     */
    fun createTypedConfiguration(config: Configuration): TypedConfiguration {
        return TypedConfiguration(config)
    }
    
    // функції для роботи з properties файлами
    
    /**
     * представлення конфігурації на основі properties файлу
     *
     * @property filePath шлях до файлу
     */
    class PropertiesConfiguration(private val filePath: String) : BaseConfiguration() {
        private val fileLock = Any()
        private var lastModified: Long = 0
        
        init {
            load()
        }
        
        /**
         * завантажує конфігурацію з файлу
         *
         * @return true якщо завантаження вдалося
         */
        fun load(): Boolean {
            return synchronized(fileLock) {
                try {
                    if (Files.exists(Paths.get(filePath))) {
                        val properties = Properties()
                        FileInputStream(filePath).use { inputStream ->
                            properties.load(inputStream)
                        }
                        
                        this.properties.clear()
                        properties.forEach { key, value ->
                            this.properties[key.toString()] = value.toString()
                        }
                        
                        lastModified = FileUtils().getLastModifiedTime(filePath)
                        true
                    } else {
                        false
                    }
                } catch (e: IOException) {
                    false
                }
            }
        }
        
        /**
         * зберігає конфігурацію у файл
         *
         * @param comments коментарі до файлу
         * @return true якщо збереження вдалося
         */
        fun save(comments: String = ""): Boolean {
            return synchronized(fileLock) {
                try {
                    val properties = Properties()
                    this.properties.forEach { key, value ->
                        properties.setProperty(key, value)
                    }
                    
                    FileOutputStream(filePath).use { outputStream ->
                        properties.store(outputStream, comments)
                    }
                    
                    lastModified = FileUtils().getLastModifiedTime(filePath)
                    true
                } catch (e: IOException) {
                    false
                }
            }
        }
        
        /**
         * перевіряє, чи файл конфігурації був змінений
         *
         * @return true якщо файл був змінений
         */
        fun isFileModified(): Boolean {
            return FileUtils().getLastModifiedTime(filePath) > lastModified
        }
        
        /**
         * перезавантажує конфігурацію з файлу, якщо файл був змінений
         *
         * @return true якщо конфігурація була перезавантажена
         */
        fun reloadIfModified(): Boolean {
            return if (isFileModified()) {
                load()
            } else {
                false
            }
        }
    }
    
    /**
     * створює конфігурацію з properties файлу
     *
     * @param filePath шлях до файлу
     * @return конфігурація
     */
    fun createPropertiesConfiguration(filePath: String): PropertiesConfiguration {
        return PropertiesConfiguration(filePath)
    }
    
    // функції для роботи з json конфігураціями
    
    /**
     * представлення конфігурації на основі json файлу
     *
     * @property filePath шлях до файлу
     */
    class JsonConfiguration(private val filePath: String) : BaseConfiguration() {
        private val fileLock = Any()
        private var lastModified: Long = 0
        
        init {
            load()
        }
        
        /**
         * завантажує конфігурацію з json файлу
         *
         * @return true якщо завантаження вдалося
         */
        fun load(): Boolean {
            return synchronized(fileLock) {
                try {
                    if (Files.exists(Paths.get(filePath))) {
                        val content = FileUtils().readTextFile(filePath)
                        val map = parseJson(content)
                        
                        this.properties.clear()
                        flattenMap(map, "").forEach { key, value ->
                            this.properties[key] = value.toString()
                        }
                        
                        lastModified = FileUtils().getLastModifiedTime(filePath)
                        true
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }
            }
        }
        
        /**
         * зберігає конфігурацію у json файл
         *
         * @return true якщо збереження вдалося
         */
        fun save(): Boolean {
            return synchronized(fileLock) {
                try {
                    val map = unflattenMap(properties)
                    val json = toJson(map)
                    
                    FileUtils().writeTextFile(filePath, json)
                    lastModified = FileUtils().getLastModifiedTime(filePath)
                    true
                } catch (e: Exception) {
                    false
                }
            }
        }
        
        /**
         * парсить json рядок у map
         *
         * @param json json рядок
         * @return map
         */
        private fun parseJson(json: String): Map<String, Any?> {
            // спрощена реалізація парсингу json
            // в реальному застосунку слід використовувати спеціалізовану бібліотеку
            val map = mutableMapOf<String, Any?>()
            
            // видаляємо фігурні дужки на початку та в кінці
            val content = json.trim().removePrefix("{").removeSuffix("}").trim()
            
            // розбиваємо на пари ключ-значення
            var i = 0
            while (i < content.length) {
                // пропускаємо пробіли
                while (i < content.length && content[i].isWhitespace()) i++
                if (i >= content.length) break
                
                // знаходимо ключ
                if (content[i] == '"') {
                    i++ // пропускаємо відкриваючу лапку
                    val keyStart = i
                    while (i < content.length && content[i] != '"') i++
                    val key = content.substring(keyStart, i)
                    i++ // пропускаємо закриваючу лапку
                    
                    // пропускаємо пробіли та двокрапку
                    while (i < content.length && (content[i].isWhitespace() || content[i] == ':')) i++
                    
                    // знаходимо значення
                    when {
                        content[i] == '"' -> {
                            // строкове значення
                            i++ // пропускаємо відкриваючу лапку
                            val valueStart = i
                            while (i < content.length && content[i] != '"') i++
                            val value = content.substring(valueStart, i)
                            i++ // пропускаємо закриваючу лапку
                            map[key] = value
                        }
                        content[i].isDigit() || content[i] == '-' -> {
                            // числове значення
                            val valueStart = i
                            while (i < content.length && (content[i].isDigit() || content[i] == '.' || content[i] == '-')) i++
                            val value = content.substring(valueStart, i)
                            map[key] = if (value.contains(".")) value.toDouble() else value.toLong()
                        }
                        content[i] == 't' || content[i] == 'f' -> {
                            // булеве значення
                            if (content.substring(i, minOf(i + 4, content.length)) == "true") {
                                map[key] = true
                                i += 4
                            } else if (content.substring(i, minOf(i + 5, content.length)) == "false") {
                                map[key] = false
                                i += 5
                            }
                        }
                        content[i] == 'n' -> {
                            // null значення
                            if (content.substring(i, minOf(i + 4, content.length)) == "null") {
                                map[key] = null
                                i += 4
                            }
                        }
                    }
                    
                    // пропускаємо пробіли та кому
                    while (i < content.length && (content[i].isWhitespace() || content[i] == ',')) i++
                } else {
                    i++
                }
            }
            
            return map
        }
        
        /**
         * перетворює map у json рядок
         *
         * @param map map
         * @return json рядок
         */
        private fun toJson(map: Map<String, Any?>): String {
            val sb = StringBuilder()
            sb.append("{")
            
            var first = true
            map.forEach { key, value ->
                if (!first) sb.append(",")
                sb.append("\"").append(key).append("\":")
                
                when (value) {
                    is String -> sb.append("\"").append(value).append("\"")
                    is Number -> sb.append(value)
                    is Boolean -> sb.append(value)
                    null -> sb.append("null")
                    else -> sb.append("\"").append(value.toString()).append("\"")
                }
                
                first = false
            }
            
            sb.append("}")
            return sb.toString()
        }
        
        /**
         * перетворює вкладений map у плоский з крапковим роздільником ключів
         *
         * @param map вкладений map
         * @param prefix префікс для ключів
         * @return плоский map
         */
        private fun flattenMap(map: Map<String, Any?>, prefix: String): Map<String, Any?> {
            val flatMap = mutableMapOf<String, Any?>()
            
            map.forEach { key, value ->
                val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
                
                when (value) {
                    is Map<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        flattenMap(value as Map<String, Any?>, fullKey).forEach { nestedKey, nestedValue ->
                            flatMap[nestedKey] = nestedValue
                        }
                    }
                    else -> flatMap[fullKey] = value
                }
            }
            
            return flatMap
        }
        
        /**
         * перетворює плоский map у вкладений з використанням крапкового роздільника ключів
         *
         * @param flatMap плоский map
         * @return вкладений map
         */
        private fun unflattenMap(flatMap: Map<String, String>): Map<String, Any?> {
            val nestedMap = mutableMapOf<String, Any?>()
            
            flatMap.forEach { key, value ->
                val parts = key.split(".")
                var currentMap = nestedMap
                
                for (i in 0 until parts.size - 1) {
                    val part = parts[i]
                    if (!currentMap.containsKey(part)) {
                        currentMap[part] = mutableMapOf<String, Any?>()
                    }
                    @Suppress("UNCHECKED_CAST")
                    currentMap = currentMap[part] as MutableMap<String, Any?>
                }
                
                // встановлюємо значення
                val lastPart = parts[parts.size - 1]
                // спробуємо перетворити значення у відповідний тип
                currentMap[lastPart] = when {
                    value == "true" -> true
                    value == "false" -> false
                    value == "null" -> null
                    value.all { it.isDigit() || it == '-' } -> value.toLong()
                    value.all { it.isDigit() || it == '-' || it == '.' } -> value.toDouble()
                    else -> value
                }
            }
            
            return nestedMap
        }
        
        /**
         * перевіряє, чи файл конфігурації був змінений
         *
         * @return true якщо файл був змінений
         */
        fun isFileModified(): Boolean {
            return FileUtils().getLastModifiedTime(filePath) > lastModified
        }
        
        /**
         * перезавантажує конфігурацію з файлу, якщо файл був змінений
         *
         * @return true якщо конфігурація була перезавантажена
         */
        fun reloadIfModified(): Boolean {
            return if (isFileModified()) {
                load()
            } else {
                false
            }
        }
    }
    
    /**
     * створює конфігурацію з json файлу
     *
     * @param filePath шлях до файлу
     * @return конфігурація
     */
    fun createJsonConfiguration(filePath: String): JsonConfiguration {
        return JsonConfiguration(filePath)
    }
    
    // функції для роботи з ієрархічними конфігураціями
    
    /**
     * представлення ієрархічної конфігурації
     */
    class HierarchicalConfiguration : BaseConfiguration() {
        private val sections = mutableMapOf<String, MutableMap<String, String>>()
        
        /**
         * отримує значення з секції
         *
         * @param section секція
         * @param key ключ
         * @return значення або null якщо ключ не знайдено
         */
        fun get(section: String, key: String): String? {
            return sections[section]?.get(key)
        }
        
        /**
         * встановлює значення в секції
         *
         * @param section секція
         * @param key ключ
         * @param value значення
         */
        fun set(section: String, key: String, value: String) {
            if (!sections.containsKey(section)) {
                sections[section] = mutableMapOf()
            }
            sections[section]?.set(key, value)
            
            // також зберігаємо в глобальній конфігурації
            super.set("$section.$key", value)
        }
        
        /**
         * перевіряє, чи існує ключ в секції
         *
         * @param section секція
         * @param key ключ
         * @return true якщо ключ існує
         */
        fun contains(section: String, key: String): Boolean {
            return sections[section]?.containsKey(key) ?: false
        }
        
        /**
         * видаляє ключ з секції
         *
         * @param section секція
         * @param key ключ
         * @return true якщо ключ був видалений
         */
        fun remove(section: String, key: String): Boolean {
            val removed = sections[section]?.remove(key) != null
            if (removed) {
                super.remove("$section.$key")
            }
            return removed
        }
        
        /**
         * отримує всі ключі з секції
         *
         * @param section секція
         * @return набір ключів
         */
        fun keys(section: String): Set<String> {
            return sections[section]?.keys ?: emptySet()
        }
        
        /**
         * отримує всі секції
         *
         * @return набір секцій
         */
        fun sections(): Set<String> {
            return sections.keys
        }
        
        /**
         * очищує секцію
         *
         * @param section секція
         */
        fun clearSection(section: String) {
            sections[section]?.keys?.forEach { key ->
                super.remove("$section.$key")
            }
            sections.remove(section)
        }
        
        /**
         * отримує кількість ключів в секції
         *
         * @param section секція
         * @return кількість ключів
         */
        fun size(section: String): Int {
            return sections[section]?.size ?: 0
        }
        
        /**
         * перевіряє, чи секція порожня
         *
         * @param section секція
         * @return true якщо секція порожня
         */
        fun isEmpty(section: String): Boolean {
            return sections[section]?.isEmpty() ?: true
        }
        
        override fun clear() {
            super.clear()
            sections.clear()
        }
    }
    
    /**
     * створює ієрархічну конфігурацію
     *
     * @return ієрархічна конфігурація
     */
    fun createHierarchicalConfiguration(): HierarchicalConfiguration {
        return HierarchicalConfiguration()
    }
    
    // функції для роботи з конфігураціями з підтримкою середовищ
    
    /**
     * представлення конфігурації з підтримкою середовищ
     */
    class EnvironmentConfiguration : BaseConfiguration() {
        private val environments = mutableMapOf<String, MutableMap<String, String>>()
        private var currentEnvironment = "default"
        
        /**
         * встановлює поточне середовище
         *
         * @param environment середовище
         */
        fun setEnvironment(environment: String) {
            currentEnvironment = environment
        }
        
        /**
         * отримує поточне середовище
         *
         * @return поточне середовище
         */
        fun getEnvironment(): String {
            return currentEnvironment
        }
        
        /**
         * отримує значення для поточного середовища
         *
         * @param key ключ
         * @return значення або null якщо ключ не знайдено
         */
        override fun get(key: String): String? {
            // спочатку шукаємо в поточному середовищі
            val value = environments[currentEnvironment]?.get(key)
            if (value != null) {
                return value
            }
            
            // якщо не знайдено, шукаємо в середовищі за замовчуванням
            return environments["default"]?.get(key)
        }
        
        /**
         * встановлює значення для середовища
         *
         * @param environment середовище
         * @param key ключ
         * @param value значення
         */
        fun set(environment: String, key: String, value: String) {
            if (!environments.containsKey(environment)) {
                environments[environment] = mutableMapOf()
            }
            environments[environment]?.set(key, value)
            
            // якщо це поточне середовище, оновлюємо базову конфігурацію
            if (environment == currentEnvironment) {
                super.set(key, value)
            }
        }
        
        /**
         * встановлює значення для поточного середовища
         *
         * @param key ключ
         * @param value значення
         */
        override fun set(key: String, value: String) {
            set(currentEnvironment, key, value)
        }
        
        /**
         * отримує всі значення для середовища
         *
         * @param environment середовище
         * @return мапа значень
         */
        fun getAllForEnvironment(environment: String): Map<String, String> {
            return environments[environment]?.toMap() ?: emptyMap()
        }
        
        /**
         * отримує всі середовища
         *
         * @return набір середовищ
         */
        fun getEnvironments(): Set<String> {
            return environments.keys
        }
        
        /**
         * очищує середовище
         *
         * @param environment середовище
         */
        fun clearEnvironment(environment: String) {
            environments.remove(environment)
            
            // якщо це було поточне середовище, оновлюємо базову конфігурацію
            if (environment == currentEnvironment) {
                super.clear()
                // завантажуємо значення з середовища за замовчуванням
                environments["default"]?.forEach { key, value ->
                    super.set(key, value)
                }
            }
        }
    }
    
    /**
     * створює конфігурацію з підтримкою середовищ
     *
     * @return конфігурація з підтримкою середовищ
     */
    fun createEnvironmentConfiguration(): EnvironmentConfiguration {
        return EnvironmentConfiguration()
    }
    
    // функції для роботи з конфігураціями з підтримкою шифрування
    
    /**
     * представлення конфігурації з підтримкою шифрування
     */
    class EncryptedConfiguration : BaseConfiguration() {
        private var encryptionKey: String = ""
        
        /**
         * встановлює ключ шифрування
         *
         * @param key ключ шифрування
         */
        fun setEncryptionKey(key: String) {
            encryptionKey = key
        }
        
        /**
         * отримує значення (автоматично розшифровує зашифровані значення)
         *
         * @param key ключ
         * @return значення або null якщо ключ не знайдено
         */
        override fun get(key: String): String? {
            val value = super.get(key)
            return if (value != null && value.startsWith("ENC(") && value.endsWith(")")) {
                // це зашифроване значення
                val encryptedValue = value.substring(4, value.length - 1)
                decrypt(encryptedValue)
            } else {
                value
            }
        }
        
        /**
         * встановлює значення (автоматично шифрує чутливі значення)
         *
         * @param key ключ
         * @param value значення
         * @param encrypt чи потрібно шифрувати значення
         */
        override fun set(key: String, value: String, encrypt: Boolean = false) {
            val finalValue = if (encrypt && encryptionKey.isNotEmpty()) {
                "ENC(${encrypt(value)})"
            } else {
                value
            }
            super.set(key, finalValue)
        }
        
        /**
         * встановлює зашифроване значення
         *
         * @param key ключ
         * @param value значення
         */
        fun setEncrypted(key: String, value: String) {
            set(key, value, true)
        }
        
        /**
         * шифрує значення
         *
         * @param value значення
         * @return зашифроване значення
         */
        private fun encrypt(value: String): String {
            // спрощена реалізація шифрування
            // в реальному застосунку слід використовувати надійний алгоритм шифрування
            val sb = StringBuilder()
            for (i in value.indices) {
                val char = value[i]
                val keyChar = encryptionKey[i % encryptionKey.length]
                val encryptedChar = (char.code xor keyChar.code).toChar()
                sb.append(encryptedChar)
            }
            return Base64.getEncoder().encodeToString(sb.toString().toByteArray())
        }
        
        /**
         * розшифровує значення
         *
         * @param encryptedValue зашифроване значення
         * @return розшифроване значення
         */
        private fun decrypt(encryptedValue: String): String {
            // спрощена реалізація розшифрування
            // в реальному застосунку слід використовувати надійний алгоритм шифрування
            return try {
                val decoded = String(Base64.getDecoder().decode(encryptedValue))
                val sb = StringBuilder()
                for (i in decoded.indices) {
                    val char = decoded[i]
                    val keyChar = encryptionKey[i % encryptionKey.length]
                    val decryptedChar = (char.code xor keyChar.code).toChar()
                    sb.append(decryptedChar)
                }
                sb.toString()
            } catch (e: Exception) {
                "" // повертаємо порожній рядок у разі помилки
            }
        }
    }
    
    /**
     * створює конфігурацію з підтримкою шифрування
     *
     * @return конфігурація з підтримкою шифрування
     */
    fun createEncryptedConfiguration(): EncryptedConfiguration {
        return EncryptedConfiguration()
    }
    
    // функції для роботи з конфігураціями з підтримкою валідації
    
    /**
     * представлення конфігурації з підтримкою валідації
     */
    class ValidatedConfiguration(private val delegate: Configuration) : Configuration by delegate {
        private val validators = mutableMapOf<String, (String) -> Boolean>()
        private val validationErrors = mutableListOf<String>()
        
        /**
         * додає валідатор для ключа
         *
         * @param key ключ
         * @param validator валідатор
         */
        fun addValidator(key: String, validator: (String) -> Boolean) {
            validators[key] = validator
        }
        
        /**
         * додає валідатор для ключа з регулярним виразом
         *
         * @param key ключ
         * @param regex регулярний вираз
         */
        fun addRegexValidator(key: String, regex: String) {
            val pattern = Regex(regex)
            validators[key] = { value -> pattern.matches(value) }
        }
        
        /**
         * додає валідатор для числового значення
         *
         * @param key ключ
         * @param minValue мінімальне значення
         * @param maxValue максимальне значення
         */
        fun addNumericValidator(key: String, minValue: Int = Int.MIN_VALUE, maxValue: Int = Int.MAX_VALUE) {
            validators[key] = { value ->
                try {
                    val num = value.toInt()
                    num >= minValue && num <= maxValue
                } catch (e: NumberFormatException) {
                    false
                }
            }
        }
        
        /**
         * додає валідатор для булевого значення
         *
         * @param key ключ
         */
        fun addBooleanValidator(key: String) {
            validators[key] = { value -> value == "true" || value == "false" }
        }
        
        /**
         * встановлює значення з валідацією
         *
         * @param key ключ
         * @param value значення
         * @throws ValidationException якщо валідація не пройшла
         */
        override fun set(key: String, value: String) {
            val validator = validators[key]
            if (validator != null && !validator(value)) {
                throw ValidationException("Значення '$value' для ключа '$key' не пройшло валідацію")
            }
            delegate.set(key, value)
        }
        
        /**
         * перевіряє всю конфігурацію
         *
         * @return true якщо всі значення пройшли валідацію
         */
        fun validate(): Boolean {
            validationErrors.clear()
            
            delegate.keys().forEach { key ->
                val value = delegate.get(key)
                val validator = validators[key]
                
                if (value != null && validator != null && !validator(value)) {
                    validationErrors.add("Значення '$value' для ключа '$key' не пройшло валідацію")
                }
            }
            
            return validationErrors.isEmpty()
        }
        
        /**
         * отримує помилки валідації
         *
         * @return список помилок валідації
         */
        fun getValidationErrors(): List<String> {
            return validationErrors.toList()
        }
        
        /**
         * очищує валідатори
         */
        fun clearValidators() {
            validators.clear()
        }
    }
    
    /**
     * виняток валідації конфігурації
     *
     * @property message повідомлення про помилку
     */
    class ValidationException(message: String) : Exception(message)
    
    /**
     * створює конфігурацію з підтримкою валідації
     *
     * @param delegate делегат конфігурації
     * @return конфігурація з підтримкою валідації
     */
    fun createValidatedConfiguration(delegate: Configuration): ValidatedConfiguration {
        return ValidatedConfiguration(delegate)
    }
    
    // функції для роботи з конфігураціями з підтримкою спостереження
    
    /**
     * інтерфейс для спостерігача за змінами конфігурації
     */
    interface ConfigurationChangeListener {
        /**
         * викликається при зміні значення
         *
         * @param key ключ
         * @param oldValue старе значення
         * @param newValue нове значення
         */
        fun onConfigurationChanged(key: String, oldValue: String?, newValue: String?)
    }
    
    /**
     * представлення конфігурації з підтримкою спостереження
     */
    class ObservableConfiguration(private val delegate: Configuration) : Configuration by delegate {
        private val listeners = mutableListOf<ConfigurationChangeListener>()
        
        /**
         * додає спостерігача
         *
         * @param listener спостерігач
         */
        fun addListener(listener: ConfigurationChangeListener) {
            listeners.add(listener)
        }
        
        /**
         * видаляє спостерігача
         *
         * @param listener спостерігач
         */
        fun removeListener(listener: ConfigurationChangeListener) {
            listeners.remove(listener)
        }
        
        /**
         * встановлює значення з повідомленням спостерігачам
         *
         * @param key ключ
         * @param value значення
         */
        override fun set(key: String, value: String) {
            val oldValue = delegate.get(key)
            delegate.set(key, value)
            notifyListeners(key, oldValue, value)
        }
        
        /**
         * видаляє ключ з повідомленням спостерігачам
         *
         * @param key ключ
         * @return true якщо ключ був видалений
         */
        override fun remove(key: String): Boolean {
            val oldValue = delegate.get(key)
            val result = delegate.remove(key)
            if (result) {
                notifyListeners(key, oldValue, null)
            }
            return result
        }
        
        /**
         * очищує конфігурацію з повідомленням спостерігачам
         */
        override fun clear() {
            val oldProperties = delegate.keys().associateWith { delegate.get(it) }
            delegate.clear()
            oldProperties.forEach { (key, value) ->
                notifyListeners(key, value, null)
            }
        }
        
        /**
         * повідомляє спостерігачів про зміни
         *
         * @param key ключ
         * @param oldValue старе значення
         * @param newValue нове значення
         */
        private fun notifyListeners(key: String, oldValue: String?, newValue: String?) {
            listeners.forEach { listener ->
                try {
                    listener.onConfigurationChanged(key, oldValue, newValue)
                } catch (e: Exception) {
                    // ігноруємо помилки спостерігачів
                }
            }
        }
    }
    
    /**
     * створює конфігурацію з підтримкою спостереження
     *
     * @param delegate делегат конфігурації
     * @return конфігурація з підтримкою спостереження
     */
    fun createObservableConfiguration(delegate: Configuration): ObservableConfiguration {
        return ObservableConfiguration(delegate)
    }
    
    // функції для роботи з конфігураціями з підтримкою кешування
    
    /**
     * представлення конфігурації з підтримкою кешування
     */
    class CachedConfiguration(private val delegate: Configuration) : Configuration by delegate {
        private val cache = mutableMapOf<String, String?>()
        private val cacheLock = Any()
        
        /**
         * отримує значення з кешу або делегата
         *
         * @param key ключ
         * @return значення або null якщо ключ не знайдено
         */
        override fun get(key: String): String? {
            synchronized(cacheLock) {
                if (cache.containsKey(key)) {
                    return cache[key]
                }
            }
            
            val value = delegate.get(key)
            synchronized(cacheLock) {
                cache[key] = value
            }
            
            return value
        }
        
        /**
         * встановлює значення з оновленням кешу
         *
         * @param key ключ
         * @param value значення
         */
        override fun set(key: String, value: String) {
            delegate.set(key, value)
            synchronized(cacheLock) {
                cache[key] = value
            }
        }
        
        /**
         * видаляє ключ з оновленням кешу
         *
         * @param key ключ
         * @return true якщо ключ був видалений
         */
        override fun remove(key: String): Boolean {
            val result = delegate.remove(key)
            if (result) {
                synchronized(cacheLock) {
                    cache.remove(key)
                }
            }
            return result
        }
        
        /**
         * очищує конфігурацію з очищенням кешу
         */
        override fun clear() {
            delegate.clear()
            synchronized(cacheLock) {
                cache.clear()
            }
        }
        
        /**
         * очищує кеш
         */
        fun clearCache() {
            synchronized(cacheLock) {
                cache.clear()
            }
        }
        
        /**
         * видаляє ключ з кешу
         *
         * @param key ключ
         */
        fun removeFromCache(key: String) {
            synchronized(cacheLock) {
                cache.remove(key)
            }
        }
    }
    
    /**
     * створює конфігурацію з підтримкою кешування
     *
     * @param delegate делегат конфігурації
     * @return конфігурація з підтримкою кешування
     */
    fun createCachedConfiguration(delegate: Configuration): CachedConfiguration {
        return CachedConfiguration(delegate)
    }
    
    // функції для роботи з конфігураціями з підтримкою фабрик
    
    /**
     * інтерфейс для фабрики конфігурацій
     */
    interface ConfigurationFactory {
        /**
         * створює конфігурацію
         *
         * @return конфігурація
         */
        fun createConfiguration(): Configuration
    }
    
    /**
     * фабрика properties конфігурацій
     *
     * @property filePath шлях до файлу
     */
    class PropertiesConfigurationFactory(private val filePath: String) : ConfigurationFactory {
        override fun createConfiguration(): Configuration {
            return PropertiesConfiguration(filePath)
        }
    }
    
    /**
     * фабрика json конфігурацій
     *
     * @property filePath шлях до файлу
     */
    class JsonConfigurationFactory(private val filePath: String) : ConfigurationFactory {
        override fun createConfiguration(): Configuration {
            return JsonConfiguration(filePath)
        }
    }
    
    /**
     * фабрика ієрархічних конфігурацій
     */
    class HierarchicalConfigurationFactory : ConfigurationFactory {
        override fun createConfiguration(): Configuration {
            return HierarchicalConfiguration()
        }
    }
    
    /**
     * фабрика конфігурацій з середовищами
     */
    class EnvironmentConfigurationFactory : ConfigurationFactory {
        override fun createConfiguration(): Configuration {
            return EnvironmentConfiguration()
        }
    }
    
    /**
     * менеджер конфігурацій
     */
    class ConfigurationManager {
        private val factories = mutableMapOf<String, ConfigurationFactory>()
        private val configurations = mutableMapOf<String, Configuration>()
        
        /**
         * реєструє фабрику конфігурацій
         *
         * @param name ім'я
         * @param factory фабрика
         */
        fun registerFactory(name: String, factory: ConfigurationFactory) {
            factories[name] = factory
        }
        
        /**
         * створює конфігурацію за ім'ям
         *
         * @param name ім'я
         * @return конфігурація або null якщо фабрику не знайдено
         */
        fun createConfiguration(name: String): Configuration? {
            val factory = factories[name]
            return factory?.createConfiguration()
        }
        
        /**
         * отримує конфігурацію за ім'ям (створює, якщо не існує)
         *
         * @param name ім'я
         * @return конфігурація
         */
        fun getConfiguration(name: String): Configuration {
            if (!configurations.containsKey(name)) {
                configurations[name] = createConfiguration(name) ?: BaseConfiguration()
            }
            return configurations[name]!!
        }
        
        /**
         * встановлює конфігурацію за ім'ям
         *
         * @param name ім'я
         * @param configuration конфігурація
         */
        fun setConfiguration(name: String, configuration: Configuration) {
            configurations[name] = configuration
        }
        
        /**
         * видаляє конфігурацію за ім'ям
         *
         * @param name ім'я
         */
        fun removeConfiguration(name: String) {
            configurations.remove(name)
        }
        
        /**
         * отримує всі зареєстровані імена конфігурацій
         *
         * @return набір імен
         */
        fun getConfigurationNames(): Set<String> {
            return configurations.keys
        }
    }
    
    /**
     * створює менеджер конфігурацій
     *
     * @return менеджер конфігурацій
     */
    fun createConfigurationManager(): ConfigurationManager {
        return ConfigurationManager()
    }
    
    // функції для роботи з конфігураціями з підтримкою анотацій
    
    /**
     * анотація для позначення конфігураційних класів
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ConfigClass(val name: String = "")
    
    /**
     * анотація для позначення конфігураційних властивостей
     */
    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ConfigProperty(
        val key: String = "",
        val defaultValue: String = "",
        val required: Boolean = false
    )
    
    /**
     * представлення конфігурації на основі класу
     *
     * @param T тип конфігураційного класу
     * @property configClass клас конфігурації
     * @property configuration конфігурація
     */
    class ClassBasedConfiguration<T : Any>(private val configClass: KClass<T>, private val configuration: Configuration) {
        
        /**
         * створює екземпляр конфігураційного класу
         *
         * @return екземпляр конфігураційного класу
         */
        fun createInstance(): T {
            // в реальному застосунку тут потрібно використовувати рефлексію для створення екземпляра
            // та заповнення полів значеннями з конфігурації
            throw NotImplementedError("Ця функція не реалізована в спрощеній версії")
        }
        
        /**
         * заповнює екземпляр конфігураційного класу значеннями з конфігурації
         *
         * @param instance екземпляр
         */
        fun populateInstance(instance: T) {
            // в реальному застосунку тут потрібно використовувати рефлексію для заповнення полів
            throw NotImplementedError("Ця функція не реалізована в спрощеній версії")
        }
        
        /**
         * оновлює конфігурацію значеннями з екземпляра конфігураційного класу
         *
         * @param instance екземпляр
         */
        fun updateFromInstance(instance: T) {
            // в реальному застосунку тут потрібно використовувати рефлексію для отримання значень полів
            throw NotImplementedError("Ця функція не реалізована в спрощеній версії")
        }
    }
    
    /**
     * створює конфігурацію на основі класу
     *
     * @param configClass клас конфігурації
     * @param configuration конфігурація
     * @return конфігурація на основі класу
     */
    fun <T : Any> createClassBasedConfiguration(configClass: KClass<T>, configuration: Configuration): ClassBasedConfiguration<T> {
        return ClassBasedConfiguration(configClass, configuration)
    }
    
    // функції для роботи з конфігураціями з підтримкою автоматичного перезавантаження
    
    /**
     * представлення конфігурації з автоматичним перезавантаженням
     *
     * @property delegate делегат конфігурації
     * @property checkInterval інтервал перевірки в мілісекундах
     */
    class AutoReloadConfiguration(
        private val delegate: Configuration,
        private val checkInterval: Long = 5000
    ) : Configuration by delegate {
        private var lastCheckTime: Long = 0
        private val checkLock = Any()
        
        /**
         * отримує значення з автоматичною перевіркою необхідності перезавантаження
         *
         * @param key ключ
         * @return значення або null якщо ключ не знайдено
         */
        override fun get(key: String): String? {
            checkAndReload()
            return delegate.get(key)
        }
        
        /**
         * перевіряє, чи потрібно перезавантажити конфігурацію, і перезавантажує, якщо потрібно
         */
        private fun checkAndReload() {
            synchronized(checkLock) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastCheckTime > checkInterval) {
                    lastCheckTime = currentTime
                    // в реальному застосунку тут потрібно перевірити, чи файл конфігурації був змінений
                    // і перезавантажити його, якщо потрібно
                }
            }
        }
    }
    
    /**
     * створює конфігурацію з автоматичним перезавантаженням
     *
     * @param delegate делегат конфігурації
     * @param checkInterval інтервал перевірки в мілісекундах
     * @return конфігурація з автоматичним перезавантаженням
     */
    fun createAutoReloadConfiguration(delegate: Configuration, checkInterval: Long = 5000): AutoReloadConfiguration {
        return AutoReloadConfiguration(delegate, checkInterval)
    }
    
    // функції для роботи з конфігураціями з підтримкою злиття
    
    /**
     * представлення конфігурації з підтримкою злиття
     */
    class MergeableConfiguration : BaseConfiguration() {
        
        /**
         * зливає іншу конфігурацію з поточною
         *
         * @param other інша конфігурація
         * @param override чи перевизначати існуючі значення
         */
        fun merge(other: Configuration, override: Boolean = true) {
            other.keys().forEach { key ->
                val value = other.get(key)
                if (value != null && (override || !contains(key))) {
                    set(key, value)
                }
            }
        }
        
        /**
         * створює нову конфігурацію, яка є результатом злиття поточної з іншою
         *
         * @param other інша конфігурація
         * @param override чи перевизначати існуючі значення
         * @return нова конфігурація
         */
        fun mergedWith(other: Configuration, override: Boolean = true): MergeableConfiguration {
            val result = MergeableConfiguration()
            result.merge(this, false)
            result.merge(other, override)
            return result
        }
        
        /**
         * отримує різницю між поточною конфігурацією та іншою
         *
         * @param other інша конфігурація
         * @return конфігурація з різницею
         */
        fun diff(other: Configuration): MergeableConfiguration {
            val result = MergeableConfiguration()
            
            // ключі, які є в поточній конфігурації, але відсутні в іншій
            keys().forEach { key ->
                if (!other.contains(key)) {
                    result.set(key, get(key) ?: "")
                }
            }
            
            // ключі з різними значеннями
            other.keys().forEach { key ->
                val thisValue = get(key)
                val otherValue = other.get(key)
                if (thisValue != otherValue) {
                    result.set("$key.current", thisValue ?: "")
                    result.set("$key.other", otherValue ?: "")
                }
            }
            
            return result
        }
    }
    
    /**
     * створює конфігурацію з підтримкою злиття
     *
     * @return конфігурація з підтримкою злиття
     */
    fun createMergeableConfiguration(): MergeableConfiguration {
        return MergeableConfiguration()
    }
    
    // функції для роботи з конфігураціями з підтримкою історії змін
    
    /**
     * представлення конфігурації з підтримкою історії змін
     */
    class HistoryConfiguration(private val delegate: Configuration) : Configuration by delegate {
        private val history = mutableListOf<ConfigurationChange>()
        private val maxHistorySize = 100
        
        /**
         * представлення зміни конфігурації
         *
         * @property timestamp час зміни
         * @property key ключ
         * @property oldValue старе значення
         * @property newValue нове значення
         * @property operation операція (SET, REMOVE, CLEAR)
         */
        data class ConfigurationChange(
            val timestamp: Long,
            val key: String,
            val oldValue: String?,
            val newValue: String?,
            val operation: String
        )
        
        companion object {
            const val OPERATION_SET = "SET"
            const val OPERATION_REMOVE = "REMOVE"
            const val OPERATION_CLEAR = "CLEAR"
        }
        
        /**
         * встановлює значення з записом в історію
         *
         * @param key ключ
         * @param value значення
         */
        override fun set(key: String, value: String) {
            val oldValue = delegate.get(key)
            delegate.set(key, value)
            addToHistory(ConfigurationChange(System.currentTimeMillis(), key, oldValue, value, OPERATION_SET))
        }
        
        /**
         * видаляє ключ з записом в історію
         *
         * @param key ключ
         * @return true якщо ключ був видалений
         */
        override fun remove(key: String): Boolean {
            val oldValue = delegate.get(key)
            val result = delegate.remove(key)
            if (result) {
                addToHistory(ConfigurationChange(System.currentTimeMillis(), key, oldValue, null, OPERATION_REMOVE))
            }
            return result
        }
        
        /**
         * очищує конфігурацію з записом в історію
         */
        override fun clear() {
            val oldProperties = delegate.keys().associateWith { delegate.get(it) }
            delegate.clear()
            addToHistory(ConfigurationChange(System.currentTimeMillis(), "", null, null, OPERATION_CLEAR))
            
            // додаємо окремі записи для кожного видаленого ключа
            oldProperties.forEach { (key, value) ->
                addToHistory(ConfigurationChange(System.currentTimeMillis(), key, value, null, OPERATION_REMOVE))
            }
        }
        
        /**
         * додає запис в історію
         *
         * @param change зміна
         */
        private fun addToHistory(change: ConfigurationChange) {
            synchronized(history) {
                history.add(change)
                if (history.size > maxHistorySize) {
                    history.removeAt(0)
                }
            }
        }
        
        /**
         * отримує історію змін
         *
         * @return список змін
         */
        fun getHistory(): List<ConfigurationChange> {
            synchronized(history) {
                return history.toList()
            }
        }
        
        /**
         * очищує історію змін
         */
        fun clearHistory() {
            synchronized(history) {
                history.clear()
            }
        }
        
        /**
         * отримує історію змін для ключа
         *
         * @param key ключ
         * @return список змін для ключа
         */
        fun getHistoryForKey(key: String): List<ConfigurationChange> {
            synchronized(history) {
                return history.filter { it.key == key }
            }
        }
        
        /**
         * скасовує останню зміну
         *
         * @return true якщо зміну було скасовано
         */
        fun undo(): Boolean {
            synchronized(history) {
                if (history.isNotEmpty()) {
                    val lastChange = history.removeAt(history.size - 1)
                    when (lastChange.operation) {
                        OPERATION_SET -> {
                            if (lastChange.oldValue != null) {
                                delegate.set(lastChange.key, lastChange.oldValue)
                            } else {
                                delegate.remove(lastChange.key)
                            }
                        }
                        OPERATION_REMOVE -> {
                            if (lastChange.oldValue != null) {
                                delegate.set(lastChange.key, lastChange.oldValue)
                            }
                        }
                        OPERATION_CLEAR -> {
                            // не можемо скасувати операцію очищення без зберігання всіх даних
                        }
                    }
                    return true
                }
            }
            return false
        }
    }
    
    /**
     * створює конфігурацію з підтримкою історії змін
     *
     * @param delegate делегат конфігурації
     * @return конфігурація з підтримкою історії змін
     */
    fun createHistoryConfiguration(delegate: Configuration): HistoryConfiguration {
        return HistoryConfiguration(delegate)
    }
    
    // функції для роботи з конфігураціями з підтримкою транзакцій
    
    /**
     * представлення конфігурації з підтримкою транзакцій
     */
    class TransactionalConfiguration(private val delegate: Configuration) : Configuration by delegate {
        private val transactionStack = mutableListOf<MutableMap<String, String?>>()
        
        /**
         * починає транзакцію
         */
        fun beginTransaction() {
            transactionStack.add(mutableMapOf())
        }
        
        /**
         * фіксує транзакцію
         *
         * @return true якщо транзакцію було зафіксовано
         */
        fun commit(): Boolean {
            if (transactionStack.isEmpty()) {
                return false
            }
            
            val transaction = transactionStack.removeAt(transactionStack.size - 1)
            transaction.forEach { (key, value) ->
                if (value != null) {
                    delegate.set(key, value)
                } else {
                    delegate.remove(key)
                }
            }
            
            return true
        }
        
        /**
         * скасовує транзакцію
         *
         * @return true якщо транзакцію було скасовано
         */
        fun rollback(): Boolean {
            if (transactionStack.isEmpty()) {
                return false
            }
            
            transactionStack.removeAt(transactionStack.size - 1)
            return true
        }
        
        /**
         * перевіряє, чи активна транзакція
         *
         * @return true якщо транзакція активна
         */
        fun isInTransaction(): Boolean {
            return transactionStack.isNotEmpty()
        }
        
        /**
         * встановлює значення в контексті транзакції
         *
         * @param key ключ
         * @param value значення
         */
        override fun set(key: String, value: String) {
            if (isInTransaction()) {
                val currentTransaction = transactionStack[transactionStack.size - 1]
                currentTransaction[key] = value
            } else {
                delegate.set(key, value)
            }
        }
        
        /**
         * видаляє ключ в контексті транзакції
         *
         * @param key ключ
         * @return true якщо ключ був видалений
         */
        override fun remove(key: String): Boolean {
            if (isInTransaction()) {
                val currentTransaction = transactionStack[transactionStack.size - 1]
                val oldValue = delegate.get(key)
                currentTransaction[key] = null
                return oldValue != null
            } else {
                return delegate.remove(key)
            }
        }
        
        /**
         * очищує конфігурацію в контексті транзакції
         */
        override fun clear() {
            if (isInTransaction()) {
                val currentTransaction = transactionStack[transactionStack.size - 1]
                delegate.keys().forEach { key ->
                    currentTransaction[key] = null
                }
            } else {
                delegate.clear()
            }
        }
    }
    
    /**
     * створює конфігурацію з підтримкою транзакцій
     *
     * @param delegate делегат конфігурації
     * @return конфігурація з підтримкою транзакцій
     */
    fun createTransactionalConfiguration(delegate: Configuration): TransactionalConfiguration {
        return TransactionalConfiguration(delegate)
    }
    
    // функції для роботи з конфігураціями з підтримкою шаблонів
    
    /**
     * представлення конфігурації з підтримкою шаблонів
     */
    class TemplateConfiguration(private val delegate: Configuration) : Configuration by delegate {
        private val templates = mutableMapOf<String, String>()
        
        /**
         * додає шаблон
         *
         * @param name ім'я шаблону
         * @param template шаблон
         */
        fun addTemplate(name: String, template: String) {
            templates[name] = template
        }
        
        /**
         * отримує значення з підтримкою шаблонів
         *
         * @param key ключ
         * @return значення або null якщо ключ не знайдено
         */
        override fun get(key: String): String? {
            val value = delegate.get(key)
            return if (value != null) {
                expandTemplates(value)
            } else {
                null
            }
        }
        
        /**
         * розгортає шаблони в значенні
         *
         * @param value значення
         * @return значення з розгорнутими шаблонами
         */
        private fun expandTemplates(value: String): String {
            var result = value
            
            // замінюємо змінні оточення
            System.getenv().forEach { (key, envValue) ->
                result = result.replace("\${env:$key}", envValue)
            }
            
            // замінюємо системні властивості
            System.getProperties().forEach { key, propValue ->
                if (key is String && propValue is String) {
                    result = result.replace("\${sys:$key}", propValue)
                }
            }
            
            // замінюємо значення з конфігурації
            delegate.keys().forEach { configKey ->
                val configValue = delegate.get(configKey)
                if (configValue != null) {
                    result = result.replace("\${config:$configKey}", configValue)
                }
            }
            
            // замінюємо шаблони
            templates.forEach { (templateName, templateValue) ->
                result = result.replace("\${template:$templateName}", templateValue)
            }
            
            return result
        }
        
        /**
         * встановлює значення з підтримкою шаблонів
         *
         * @param key ключ
         * @param value значення
         */
        override fun set(key: String, value: String) {
            delegate.set(key, value)
        }
        
        /**
         * отримує всі шаблони
         *
         * @return мапа шаблонів
         */
        fun getTemplates(): Map<String, String> {
            return templates.toMap()
        }
        
        /**
         * видаляє шаблон
         *
         * @param name ім'я шаблону
         */
        fun removeTemplate(name: String) {
            templates.remove(name)
        }
        
        /**
         * очищує шаблони
         */
        fun clearTemplates() {
            templates.clear()
        }
    }
    
    /**
     * створює конфігурацію з підтримкою шаблонів
     *
     * @param delegate делегат конфігурації
     * @return конфігурація з підтримкою шаблонів
     */
    fun createTemplateConfiguration(delegate: Configuration): TemplateConfiguration {
        return TemplateConfiguration(delegate)
    }
    
    // функції для роботи з конфігураціями з підтримкою профілів
    
    /**
     * представлення конфігурації з підтримкою профілів
     */
    class ProfileConfiguration : BaseConfiguration() {
        private val profiles = mutableMapOf<String, MutableMap<String, String>>()
        private var activeProfiles = mutableSetOf<String>()
        
        /**
         * активує профіль
         *
         * @param profile профіль
         */
        fun activateProfile(profile: String) {
            activeProfiles.add(profile)
            reloadActiveProfiles()
        }
        
        /**
         * деактивує профіль
         *
         * @param profile профіль
         */
        fun deactivateProfile(profile: String) {
            activeProfiles.remove(profile)
            reloadActiveProfiles()
        }
        
        /**
         * встановлює активні профілі
         *
         * @param profiles профілі
         */
        fun setActiveProfiles(profiles: Set<String>) {
            activeProfiles = profiles.toMutableSet()
            reloadActiveProfiles()
        }
        
        /**
         * отримує активні профілі
         *
         * @return набір активних профілів
         */
        fun getActiveProfiles(): Set<String> {
            return activeProfiles.toSet()
        }
        
        /**
         * встановлює значення для профілю
         *
         * @param profile профіль
         * @param key ключ
         * @param value значення
         */
        fun set(profile: String, key: String, value: String) {
            if (!profiles.containsKey(profile)) {
                profiles[profile] = mutableMapOf()
            }
            profiles[profile]?.set(key, value)
            
            // якщо профіль активний, оновлюємо базову конфігурацію
            if (activeProfiles.contains(profile)) {
                super.set(key, value)
            }
        }
        
        /**
         * отримує значення (з урахуванням активних профілів)
         *
         * @param key ключ
         * @return значення або null якщо ключ не знайдено
         */
        override fun get(key: String): String? {
            // шукаємо в активних профілях (в порядку зворотної активності)
            for (profile in activeProfiles.reversed()) {
                val value = profiles[profile]?.get(key)
                if (value != null) {
                    return value
                }
            }
            
            // якщо не знайдено, повертаємо значення з базової конфігурації
            return super.get(key)
        }
        
        /**
         * перевантажує активні профілі
         */
        private fun reloadActiveProfiles() {
            // очищуємо базову конфігурацію
            super.clear()
            
            // завантажуємо значення з активних профілів (в порядку активності)
            for (profile in activeProfiles) {
                profiles[profile]?.forEach { key, value ->
                    super.set(key, value)
                }
            }
        }
        
        /**
         * отримує всі значення для профілю
         *
         * @param profile профіль
         * @return мапа значень
         */
        fun getAllForProfile(profile: String): Map<String, String> {
            return profiles[profile]?.toMap() ?: emptyMap()
        }
        
        /**
         * отримує всі профілі
         *
         * @return набір профілів
         */
        fun getProfiles(): Set<String> {
            return profiles.keys
        }
        
        /**
         * очищує профіль
         *
         * @param profile профіль
         */
        fun clearProfile(profile: String) {
            profiles.remove(profile)
            
            // якщо це був активний профіль, перезавантажуємо активні профілі
            if (activeProfiles.contains(profile)) {
                reloadActiveProfiles()
            }
        }
    }
    
    /**
     * створює конфігурацію з підтримкою профілів
     *
     * @return конфігурація з підтримкою профілів
     */
    fun createProfileConfiguration(): ProfileConfiguration {
        return ProfileConfiguration()
    }
    
    // функції для роботи з конфігураціями з підтримкою зовнішніх джерел
    
    /**
     * інтерфейс для зовнішнього джерела конфігурації
     */
    interface ExternalConfigurationSource {
        /**
         * завантажує конфігурацію
         *
         * @return мапа ключів та значень
         */
        fun load(): Map<String, String>
        
        /**
         * зберігає конфігурацію
         *
         * @param config мапа ключів та значень
         */
        fun save(config: Map<String, String>)
    }
    
    /**
     * представлення конфігурації з підтримкою зовнішніх джерел
     */
    class ExternalSourceConfiguration(private val delegate: Configuration) : Configuration by delegate {
        private val sources = mutableListOf<ExternalConfigurationSource>()
        
        /**
         * додає зовнішнє джерело
         *
         * @param source джерело
         */
        fun addSource(source: ExternalConfigurationSource) {
            sources.add(source)
        }
        
        /**
         * завантажує конфігурацію з усіх зовнішніх джерел
         */
        fun loadFromExternalSources() {
            sources.forEach { source ->
                try {
                    val externalConfig = source.load()
                    externalConfig.forEach { key, value ->
                        delegate.set(key, value)
                    }
                } catch (e: Exception) {
                    // ігноруємо помилки завантаження з окремих джерел
                }
            }
        }
        
        /**
         * зберігає конфігурацію в усі зовнішні джерела
         */
        fun saveToExternalSources() {
            val configMap = delegate.keys().associateWith { delegate.get(it) ?: "" }
            sources.forEach { source ->
                try {
                    source.save(configMap)
                } catch (e: Exception) {
                    // ігноруємо помилки збереження в окремі джерела
                }
            }
        }
        
        /**
         * видаляє зовнішнє джерело
         *
         * @param source джерело
         */
        fun removeSource(source: ExternalConfigurationSource) {
            sources.remove(source)
        }
        
        /**
         * очищує зовнішні джерела
         */
        fun clearSources() {
            sources.clear()
        }
    }
    
    /**
     * створює конфігурацію з підтримкою зовнішніх джерел
     *
     * @param delegate делегат конфігурації
     * @return конфігурація з підтримкою зовнішніх джерел
     */
    fun createExternalSourceConfiguration(delegate: Configuration): ExternalSourceConfiguration {
        return ExternalSourceConfiguration(delegate)
    }
    
    // функції для роботи з конфігураціями з підтримкою автоматичного резервного копіювання
    
    /**
     * представлення конфігурації з автоматичним резервним копіюванням
     *
     * @property delegate делегат конфігурації
     * @property backupPath шлях до резервної копії
     * @property backupInterval інтервал резервного копіювання в мілісекундах
     */
    class BackupConfiguration(
        private val delegate: Configuration,
        private val backupPath: String,
        private val backupInterval: Long = 60000
    ) : Configuration by delegate {
        private var lastBackupTime: Long = 0
        private val backupLock = Any()
        
        /**
         * встановлює значення з автоматичним резервним копіюванням
         *
         * @param key ключ
         * @param value значення
         */
        override fun set(key: String, value: String) {
            delegate.set(key, value)
            checkAndBackup()
        }
        
        /**
         * видаляє ключ з автоматичним резервним копіюванням
         *
         * @param key ключ
         * @return true якщо ключ був видалений
         */
        override fun remove(key: String): Boolean {
            val result = delegate.remove(key)
            if (result) {
                checkAndBackup()
            }
            return result
        }
        
        /**
         * очищує конфігурацію з автоматичним резервним копіюванням
         */
        override fun clear() {
            delegate.clear()
            checkAndBackup()
        }
        
        /**
         * перевіряє, чи потрібно зробити резервну копію, і робить її, якщо потрібно
         */
        private fun checkAndBackup() {
            synchronized(backupLock) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastBackupTime > backupInterval) {
                    lastBackupTime = currentTime
                    performBackup()
                }
            }
        }
        
        /**
         * виконує резервне копіювання
         */
        private fun performBackup() {
            try {
                // створюємо резервну копію у форматі properties
                val properties = Properties()
                delegate.keys().forEach { key ->
                    val value = delegate.get(key)
                    if (value != null) {
                        properties.setProperty(key, value)
                    }
                }
                
                // додаємо часову мітку до імені файлу
                val timestamp = System.currentTimeMillis()
                val backupFile = "$backupPath.$timestamp"
                
                FileOutputStream(backupFile).use { outputStream ->
                    properties.store(outputStream, "Backup created at $timestamp")
                }
            } catch (e: Exception) {
                // ігноруємо помилки резервного копіювання
            }
        }
        
        /**
         * відновлює конфігурацію з резервної копії
         *
         * @param backupFile файл резервної копії
         * @return true якщо відновлення вдалося
         */
        fun restoreFromBackup(backupFile: String): Boolean {
            return try {
                val properties = Properties()
                FileInputStream(backupFile).use { inputStream ->
                    properties.load(inputStream)
                }
                
                delegate.clear()
                properties.forEach { key, value ->
                    delegate.set(key.toString(), value.toString())
                }
                
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * створює конфігурацію з автоматичним резервним копіюванням
     *
     * @param delegate делегат конфігурації
     * @param backupPath шлях до резервної копії
     * @param backupInterval інтервал резервного копіювання в мілісекундах
     * @return конфігурація з автоматичним резервним копіюванням
     */
    fun createBackupConfiguration(
        delegate: Configuration,
        backupPath: String,
        backupInterval: Long = 60000
    ): BackupConfiguration {
        return BackupConfiguration(delegate, backupPath, backupInterval)
    }
    
    // функції для роботи з конфігураціями з підтримкою автоматичного збереження
    
    /**
     * представлення конфігурації з автоматичним збереженням
     *
     * @property delegate делегат конфігурації
     * @property savePath шлях до файлу збереження
     * @property autoSaveDelay затримка автоматичного збереження в мілісекундах
     */
    class AutoSaveConfiguration(
        private val delegate: Configuration,
        private val savePath: String,
        private val autoSaveDelay: Long = 1000
    ) : Configuration by delegate {
        private var lastChangeTime: Long = 0
        private var autoSaveScheduled = false
        private val autoSaveLock = Any()
        
        /**
         * встановлює значення з плануванням автоматичного збереження
         *
         * @param key ключ
         * @param value значення
         */
        override fun set(key: String, value: String) {
            delegate.set(key, value)
            scheduleAutoSave()
        }
        
        /**
         * видаляє ключ з плануванням автоматичного збереження
         *
         * @param key ключ
         * @return true якщо ключ був видалений
         */
        override fun remove(key: String): Boolean {
            val result = delegate.remove(key)
            if (result) {
                scheduleAutoSave()
            }
            return result
        }
        
        /**
         * очищує конфігурацію з плануванням автоматичного збереження
         */
        override fun clear() {
            delegate.clear()
            scheduleAutoSave()
        }
        
        /**
         * планує автоматичне збереження
         */
        private fun scheduleAutoSave() {
            synchronized(autoSaveLock) {
                lastChangeTime = System.currentTimeMillis()
                if (!autoSaveScheduled) {
                    autoSaveScheduled = true
                    Thread {
                        try {
                            Thread.sleep(autoSaveDelay)
                            synchronized(autoSaveLock) {
                                if (System.currentTimeMillis() - lastChangeTime >= autoSaveDelay) {
                                    performAutoSave()
                                    autoSaveScheduled = false
                                } else {
                                    // якщо були нові зміни, переплановуємо
                                    autoSaveScheduled = false
                                    scheduleAutoSave()
                                }
                            }
                        } catch (e: InterruptedException) {
                            Thread.currentThread().interrupt()
                        }
                    }.start()
                }
            }
        }
        
        /**
         * виконує автоматичне збереження
         */
        private fun performAutoSave() {
            try {
                // зберігаємо у форматі properties
                val properties = Properties()
                delegate.keys().forEach { key ->
                    val value = delegate.get(key)
                    if (value != null) {
                        properties.setProperty(key, value)
                    }
                }
                
                FileOutputStream(savePath).use { outputStream ->
                    properties.store(outputStream, "Auto-saved configuration")
                }
            } catch (e: Exception) {
                // ігноруємо помилки збереження
            }
        }
    }
    
    /**
     * створює конфігурацію з автоматичним збереженням
     *
     * @param delegate делегат конфігурації
     * @param savePath шлях до файлу збереження
     * @param autoSaveDelay затримка автоматичного збереження в мілісекундах
     * @return конфігурація з автоматичним збереженням
     */
    fun createAutoSaveConfiguration(
        delegate: Configuration,
        savePath: String,
        autoSaveDelay: Long = 1000
    ): AutoSaveConfiguration {
        return AutoSaveConfiguration(delegate, savePath, autoSaveDelay)
    }
}