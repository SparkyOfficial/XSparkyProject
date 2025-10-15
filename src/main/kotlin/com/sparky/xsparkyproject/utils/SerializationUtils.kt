/**
 * xsparkyproject - розширена бібліотека для kotlin
 *
 * Copyright 2025 Андрій Будильников
 */

package com.sparky.xsparkyproject.utils

import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.reflect.KClass

/**
 * утилітарний клас для роботи з серіалізацією
 *
 * @author андрій будильников
 * @since 1.0.0
 */
@Suppress("unused")
class SerializationUtils {
    
    companion object {
        // стандартні формати серіалізації
        const val FORMAT_JSON = "json"
        const val FORMAT_XML = "xml"
        const val FORMAT_YAML = "yaml"
        const val FORMAT_BINARY = "binary"
        const val FORMAT_CSV = "csv"
        
        // стандартні налаштування
        const val DEFAULT_BUFFER_SIZE = 8192
        const val DEFAULT_INDENT_SIZE = 2
        const val DEFAULT_ENCODING = "UTF-8"
    }
    
    // базові функції для роботи з серіалізаторами
    
    /**
     * представлення серіалізатора
     *
     * @param T тип об'єкта для серіалізації
     */
    interface Serializer<T> {
        /**
         * серіалізує об'єкт у байтовий масив
         *
         * @param obj об'єкт
         * @return байтовий масив
         */
        fun serialize(obj: T): ByteArray
        
        /**
         * серіалізує об'єкт у рядок
         *
         * @param obj об'єкт
         * @param charset кодування
         * @return рядок
         */
        fun serializeToString(obj: T, charset: java.nio.charset.Charset = StandardCharsets.UTF_8): String {
            return String(serialize(obj), charset)
        }
        
        /**
         * серіалізує об'єкт у файл
         *
         * @param obj об'єкт
         * @param filePath шлях до файлу
         * @return true якщо серіалізація вдалася
         */
        fun serializeToFile(obj: T, filePath: String): Boolean {
            return try {
                val data = serialize(obj)
                FileUtils().writeBinaryFile(filePath, data)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * представлення десеріалізатора
     *
     * @param T тип об'єкта для десеріалізації
     */
    interface Deserializer<T> {
        /**
         * десеріалізує об'єкт з байтового масиву
         *
         * @param data байтовий масив
         * @return об'єкт
         */
        fun deserialize(data: ByteArray): T
        
        /**
         * десеріалізує об'єкт з рядка
         *
         * @param data рядок
         * @param charset кодування
         * @return об'єкт
         */
        fun deserializeFromString(data: String, charset: java.nio.charset.Charset = StandardCharsets.UTF_8): T {
            return deserialize(data.toByteArray(charset))
        }
        
        /**
         * десеріалізує об'єкт з файлу
         *
         * @param filePath шлях до файлу
         * @return об'єкт
         */
        fun deserializeFromFile(filePath: String): T? {
            return try {
                val data = FileUtils().readBinaryFile(filePath)
                deserialize(data)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * представлення серіалізатора/десеріалізатора
     *
     * @param T тип об'єкта
     */
    interface SerDe<T> : Serializer<T>, Deserializer<T>
    
    /**
     * базова реалізація серіалізатора/десеріалізатора
     */
    abstract class BaseSerDe<T> : SerDe<T>
    
    // функції для роботи з JSON серіалізацією
    
    /**
     * представлення JSON серіалізатора/десеріалізатора
     *
     * @param T тип об'єкта
     */
    class JsonSerializer<T> : BaseSerDe<T>() {
        private var indent = false
        private var indentSize = DEFAULT_INDENT_SIZE
        private var prettyPrint = false
        
        /**
         * встановлює форматування з відступами
         *
         * @param enabled чи ввімкнено
         * @param size розмір відступу
         * @return серіалізатор
         */
        fun withIndent(enabled: Boolean = true, size: Int = DEFAULT_INDENT_SIZE): JsonSerializer<T> {
            this.indent = enabled
            this.indentSize = size
            this.prettyPrint = enabled
            return this
        }
        
        /**
         * встановлює pretty print форматування
         *
         * @param enabled чи ввімкнено
         * @return серіалізатор
         */
        fun withPrettyPrint(enabled: Boolean = true): JsonSerializer<T> {
            this.prettyPrint = enabled
            return this
        }
        
        override fun serialize(obj: T): ByteArray {
            val jsonString = serializeToJson(obj)
            return jsonString.toByteArray(StandardCharsets.UTF_8)
        }
        
        override fun deserialize(data: ByteArray): T {
            val jsonString = String(data, StandardCharsets.UTF_8)
            return deserializeFromJson(jsonString)
        }
        
        /**
         * серіалізує об'єкт у JSON рядок
         *
         * @param obj об'єкт
         * @return JSON рядок
         */
        private fun serializeToJson(obj: T): String {
            // спрощена реалізація JSON серіалізації
            // в реальному застосунку слід використовувати спеціалізовану бібліотеку
            return when (obj) {
                null -> "null"
                is String -> "\"${escapeJsonString(obj)}\""
                is Number -> obj.toString()
                is Boolean -> obj.toString()
                is List<*> -> serializeListToJson(obj)
                is Map<*, *> -> serializeMapToJson(obj)
                else -> serializeObjectToJson(obj)
            }
        }
        
        /**
         * десеріалізує об'єкт з JSON рядка
         *
         * @param jsonString JSON рядок
         * @return об'єкт
         */
        @Suppress("UNUSED_PARAMETER")
        private fun deserializeFromJson(jsonString: String): T {
            // спрощена реалізація JSON десеріалізації
            // в реальному застосунку слід використовувати спеціалізовану бібліотеку
            throw NotImplementedError("JSON десеріалізація не реалізована в спрощеному прикладі")
        }
        
        /**
         * серіалізує список у JSON
         *
         * @param list список
         * @return JSON рядок
         */
        private fun serializeListToJson(list: List<*>): String {
            val sb = StringBuilder()
            sb.append("[")
            
            list.forEachIndexed { index, item ->
                if (index > 0) sb.append(",")
                if (prettyPrint) sb.append(" ")
                sb.append(serializeToJson(item))
            }
            
            if (prettyPrint && list.isNotEmpty()) sb.append(" ")
            sb.append("]")
            return sb.toString()
        }
        
        /**
         * серіалізує мапу у JSON
         *
         * @param map мапа
         * @return JSON рядок
         */
        private fun serializeMapToJson(map: Map<*, *>): String {
            val sb = StringBuilder()
            sb.append("{")
            
            var first = true
            map.forEach { (key, value) ->
                if (!first) sb.append(",")
                if (prettyPrint) sb.append(" ")
                
                sb.append("\"${escapeJsonString(key.toString())}\":")
                if (prettyPrint) sb.append(" ")
                sb.append(serializeToJson(value))
                
                first = false
            }
            
            if (prettyPrint && map.isNotEmpty()) sb.append(" ")
            sb.append("}")
            return sb.toString()
        }
        
        /**
         * серіалізує об'єкт у JSON
         *
         * @param obj об'єкт
         * @return JSON рядок
         */
        private fun serializeObjectToJson(obj: Any): String {
            // в реальному застосунку тут потрібно використовувати рефлексію
            // для отримання полів об'єкта та їх серіалізації
            return "\"${escapeJsonString(obj.toString())}\""
        }
        
        /**
         * екранує рядок для JSON
         *
         * @param str рядок
         * @return екранований рядок
         */
        private fun escapeJsonString(str: String): String {
            return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
        }
    }
    
    /**
     * створює JSON серіалізатор/десеріалізатор
     *
     * @param T тип об'єкта
     * @return JSON серіалізатор/десеріалізатор
     */
    fun <T> createJsonSerializer(): JsonSerializer<T> {
        return JsonSerializer()
    }
    
    // функції для роботи з XML серіалізацією
    
    /**
     * представлення XML серіалізатора/десеріалізатора
     *
     * @param T тип об'єкта
     */
    class XmlSerializer<T> : BaseSerDe<T>() {
        private var indent = false
        private var indentSize = DEFAULT_INDENT_SIZE
        private var encoding = DEFAULT_ENCODING
        
        /**
         * встановлює форматування з відступами
         *
         * @param enabled чи ввімкнено
         * @param size розмір відступу
         * @return серіалізатор
         */
        fun withIndent(enabled: Boolean = true, size: Int = DEFAULT_INDENT_SIZE): XmlSerializer<T> {
            this.indent = enabled
            this.indentSize = size
            return this
        }
        
        /**
         * встановлює кодування
         *
         * @param encoding кодування
         * @return серіалізатор
         */
        fun withEncoding(encoding: String): XmlSerializer<T> {
            this.encoding = encoding
            return this
        }
        
        override fun serialize(obj: T): ByteArray {
            val xmlString = serializeToXml(obj)
            return xmlString.toByteArray(Charsets.UTF_8)
        }
        
        override fun deserialize(data: ByteArray): T {
            val xmlString = String(data, Charsets.UTF_8)
            return deserializeFromXml(xmlString)
        }
        
        /**
         * серіалізує об'єкт у XML рядок
         *
         * @param obj об'єкт
         * @return XML рядок
         */
        private fun serializeToXml(obj: T): String {
            val sb = StringBuilder()
            sb.append("<?xml version=\"1.0\" encoding=\"$encoding\"?>\n")
            serializeToXml(obj, sb, 0)
            return sb.toString()
        }
        
        /**
         * серіалізує об'єкт у XML
         *
         * @param obj об'єкт
         * @param sb StringBuilder
         * @param level рівень вкладеності
         */
        private fun serializeToXml(obj: Any?, sb: StringBuilder, level: Int) {
            when (obj) {
                null -> sb.append("null")
                is String -> sb.append(escapeXmlString(obj))
                is Number -> sb.append(obj.toString())
                is Boolean -> sb.append(obj.toString())
                is List<*> -> serializeListToXml(obj, sb, level)
                is Map<*, *> -> serializeMapToXml(obj, sb, level)
                else -> serializeObjectToXml(obj, sb, level)
            }
        }
        
        /**
         * серіалізує список у XML
         *
         * @param list список
         * @param sb StringBuilder
         * @param level рівень вкладеності
         */
        private fun serializeListToXml(list: List<*>, sb: StringBuilder, level: Int) {
            val indentStr = if (indent) " ".repeat(level * indentSize) else ""
            
            list.forEach { item ->
                sb.append("$indentStr<item>\n")
                serializeToXml(item, sb, level + 1)
                sb.append("$indentStr</item>\n")
            }
        }
        
        /**
         * серіалізує мапу у XML
         *
         * @param map мапа
         * @param sb StringBuilder
         * @param level рівень вкладеності
         */
        private fun serializeMapToXml(map: Map<*, *>, sb: StringBuilder, level: Int) {
            val indentStr = if (indent) " ".repeat(level * indentSize) else ""
            
            map.forEach { (key, value) ->
                val keyName = key?.toString() ?: "null"
                sb.append("$indentStr<$keyName>\n")
                serializeToXml(value, sb, level + 1)
                sb.append("$indentStr</$keyName>\n")
            }
        }
        
        /**
         * серіалізує об'єкт у XML
         *
         * @param obj об'єкт
         * @param sb StringBuilder
         * @param level рівень вкладеності
         */
        private fun serializeObjectToXml(obj: Any, sb: StringBuilder, level: Int) {
            val indentStr = if (indent) " ".repeat(level * indentSize) else ""
            // в реальному застосунку тут потрібно використовувати рефлексію
            // для отримання полів об'єкта та їх серіалізації
            sb.append("$indentStr${escapeXmlString(obj.toString())}\n")
        }
        
        /**
         * десеріалізує об'єкт з XML рядка
         *
         * @param xmlString XML рядок
         * @return об'єкт
         */
        @Suppress("UNUSED_PARAMETER")
        private fun deserializeFromXml(xmlString: String): T {
            // спрощена реалізація XML десеріалізації
            // в реальному застосунку слід використовувати спеціалізовану бібліотеку
            throw NotImplementedError("XML десеріалізація не реалізована в спрощеному прикладі")
        }
        
        /**
         * екранує рядок для XML
         *
         * @param str рядок
         * @return екранований рядок
         */
        private fun escapeXmlString(str: String): String {
            return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;")
        }
    }
    
    /**
     * створює XML серіалізатор/десеріалізатор
     *
     * @param T тип об'єкта
     * @return XML серіалізатор/десеріалізатор
     */
    fun <T> createXmlSerializer(): XmlSerializer<T> {
        return XmlSerializer()
    }
    
    // функції для роботи з YAML серіалізацією
    
    /**
     * представлення YAML серіалізатора/десеріалізатора
     *
     * @param T тип об'єкта
     */
    class YamlSerializer<T> : BaseSerDe<T>() {
        private var indentSize = 2
        
        /**
         * встановлює розмір відступу
         *
         * @param size розмір відступу
         * @return серіалізатор
         */
        fun withIndentSize(size: Int): YamlSerializer<T> {
            this.indentSize = size
            return this
        }
        
        override fun serialize(obj: T): ByteArray {
            val yamlString = serializeToYaml(obj)
            return yamlString.toByteArray(StandardCharsets.UTF_8)
        }
        
        override fun deserialize(data: ByteArray): T {
            val yamlString = String(data, StandardCharsets.UTF_8)
            return deserializeFromYaml(yamlString)
        }
        
        /**
         * серіалізує об'єкт у YAML рядок
         *
         * @param obj об'єкт
         * @return YAML рядок
         */
        private fun serializeToYaml(obj: T): String {
            val sb = StringBuilder()
            serializeToYaml(obj, sb, 0)
            return sb.toString()
        }
        
        /**
         * серіалізує об'єкт у YAML
         *
         * @param obj об'єкт
         * @param sb StringBuilder
         * @param level рівень вкладеності
         */
        private fun serializeToYaml(obj: Any?, sb: StringBuilder, level: Int) {
            val indentStr = " ".repeat(level * indentSize)
            
            when (obj) {
                null -> sb.append("null\n")
                is String -> {
                    if (obj.contains("\n")) {
                        sb.append("|\n")
                        val lines = obj.split("\n")
                        lines.forEach { line ->
                            sb.append("$indentStr  $line\n")
                        }
                    } else {
                        sb.append("\"${escapeYamlString(obj)}\"\n")
                    }
                }
                is Number -> sb.append("${obj}\n")
                is Boolean -> sb.append("${obj}\n")
                is List<*> -> serializeListToYaml(obj, sb, level)
                is Map<*, *> -> serializeMapToYaml(obj, sb, level)
                else -> {
                    // в реальному застосунку тут потрібно використовувати рефлексію
                    sb.append("${escapeYamlString(obj.toString())}\n")
                }
            }
        }
        
        /**
         * серіалізує список у YAML
         *
         * @param list список
         * @param sb StringBuilder
         * @param level рівень вкладеності
         */
        private fun serializeListToYaml(list: List<*>, sb: StringBuilder, level: Int) {
            val indentStr = " ".repeat(level * indentSize)
            
            list.forEach { item ->
                sb.append("$indentStr- ")
                if (item is Map<*, *> || item is List<*>) {
                    sb.append("\n")
                    serializeToYaml(item, sb, level + 1)
                } else {
                    serializeToYaml(item, sb, 0) // 0 бо значення вже на новому рядку
                }
            }
        }
        
        /**
         * серіалізує мапу у YAML
         *
         * @param map мапа
         * @param sb StringBuilder
         * @param level рівень вкладеності
         */
        private fun serializeMapToYaml(map: Map<*, *>, sb: StringBuilder, level: Int) {
            val indentStr = " ".repeat(level * indentSize)
            
            map.forEach { (key, value) ->
                val keyName = key?.toString() ?: "null"
                sb.append("$indentStr$keyName: ")
                if (value is Map<*, *> || value is List<*>) {
                    sb.append("\n")
                    serializeToYaml(value, sb, level + 1)
                } else {
                    serializeToYaml(value, sb, 0) // 0 бо значення вже на новому рядку
                }
            }
        }
        
        /**
         * десеріалізує об'єкт з YAML рядка
         *
         * @param yamlString YAML рядок
         * @return об'єкт
         */
        @Suppress("UNUSED_PARAMETER")
        private fun deserializeFromYaml(yamlString: String): T {
            // спрощена реалізація YAML десеріалізації
            // в реальному застосунку слід використовувати спеціалізовану бібліотеку
            throw NotImplementedError("YAML десеріалізація не реалізована в спрощеному прикладі")
        }
        
        /**
         * екранує рядок для YAML
         *
         * @param str рядок
         * @return екранований рядок
         */
        private fun escapeYamlString(str: String): String {
            return str.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
        }
    }
    
    /**
     * створює YAML серіалізатор/десеріалізатор
     *
     * @param T тип об'єкта
     * @return YAML серіалізатор/десеріалізатор
     */
    fun <T> createYamlSerializer(): YamlSerializer<T> {
        return YamlSerializer()
    }
    
    // функції для роботи з бінарною серіалізацією
    
    /**
     * представлення бінарного серіалізатора/десеріалізатора
     *
     * @param T тип об'єкта
     */
    class BinarySerializer<T : Serializable> : BaseSerDe<T> {
        
        override fun serialize(obj: T): ByteArray {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
            objectOutputStream.writeObject(obj)
            objectOutputStream.close()
            return byteArrayOutputStream.toByteArray()
        }
        
        override fun deserialize(data: ByteArray): T {
            val byteArrayInputStream = ByteArrayInputStream(data)
            val objectInputStream = ObjectInputStream(byteArrayInputStream)
            @Suppress("UNCHECKED_CAST")
            val obj = objectInputStream.readObject() as T
            objectInputStream.close()
            return obj
        }
    }
    
    /**
     * створює бінарний серіалізатор/десеріалізатор
     *
     * @param T тип об'єкта
     * @return бінарний серіалізатор/десеріалізатор
     */
    fun <T : Serializable> createBinarySerializer(): BinarySerializer<T> {
        return BinarySerializer()
    }
    
    // функції для роботи з CSV серіалізацією
    
    /**
     * представлення CSV серіалізатора/десеріалізатора
     *
     * @param T тип об'єкта
     */
    class CsvSerializer<T> : BaseSerDe<List<T>> {
        private var delimiter = ','
        private var quote = '"'
        private var escape = '\\'
        private var hasHeader = true
        
        /**
         * встановлює роздільник
         *
         * @param delimiter роздільник
         * @return серіалізатор
         */
        fun withDelimiter(delimiter: Char): CsvSerializer<T> {
            this.delimiter = delimiter
            return this
        }
        
        /**
         * встановлює символ лапок
         *
         * @param quote символ лапок
         * @return серіалізатор
         */
        fun withQuote(quote: Char): CsvSerializer<T> {
            this.quote = quote
            return this
        }
        
        /**
         * встановлює символ екранування
         *
         * @param escape символ екранування
         * @return серіалізатор
         */
        fun withEscape(escape: Char): CsvSerializer<T> {
            this.escape = escape
            return this
        }
        
        /**
         * встановлює наявність заголовка
         *
         * @param hasHeader чи є заголовок
         * @return серіалізатор
         */
        fun withHeader(hasHeader: Boolean): CsvSerializer<T> {
            this.hasHeader = hasHeader
            return this
        }
        
        override fun serialize(obj: List<T>): ByteArray {
            val csvString = serializeToCsv(obj)
            return csvString.toByteArray(StandardCharsets.UTF_8)
        }
        
        override fun deserialize(data: ByteArray): List<T> {
            val csvString = String(data, StandardCharsets.UTF_8)
            return deserializeFromCsv(csvString)
        }
        
        /**
         * серіалізує список об'єктів у CSV рядок
         *
         * @param objects список об'єктів
         * @return CSV рядок
         */
        private fun serializeToCsv(objects: List<T>): String {
            if (objects.isEmpty()) return ""
            
            val sb = StringBuilder()
            
            // в реальному застосунку тут потрібно використовувати рефлексію
            // для отримання полів об'єкта та їх серіалізації
            
            // як приклад, серіалізуємо прості об'єкти
            objects.forEach { obj ->
                when (obj) {
                    is String -> sb.append(escapeCsvField(obj)).append("\n")
                    is Number -> sb.append(obj.toString()).append("\n")
                    is Boolean -> sb.append(obj.toString()).append("\n")
                    else -> sb.append(escapeCsvField(obj.toString())).append("\n")
                }
            }
            
            return sb.toString()
        }
        
        /**
         * десеріалізує список об'єктів з CSV рядка
         *
         * @param csvString CSV рядок
         * @return список об'єктів
         */
        @Suppress("UNUSED_PARAMETER")
        private fun deserializeFromCsv(csvString: String): List<T> {
            // спрощена реалізація CSV десеріалізації
            // в реальному застосунку слід використовувати спеціалізовану бібліотеку
            return emptyList()
        }
        
        /**
         * екранує поле CSV
         *
         * @param field поле
         * @return екрановане поле
         */
        private fun escapeCsvField(field: String): String {
            return if (field.contains(delimiter) || field.contains(quote) || field.contains('\n')) {
                val escaped = field.replace(quote.toString(), "${quote}${quote}")
                "${quote}$escaped${quote}"
            } else {
                field
            }
        }
    }
    
    /**
     * створює CSV серіалізатор/десеріалізатор
     *
     * @param T тип об'єкта
     * @return CSV серіалізатор/десеріалізатор
     */
    fun <T> createCsvSerializer(): CsvSerializer<T> {
        return CsvSerializer()
    }
    
    // функції для роботи з композитними серіалізаторами
    
    /**
     * представлення композитного серіалізатора/десеріалізатора
     *
     * @param T тип об'єкта
     */
    class CompositeSerDe<T> : BaseSerDe<T> {
        private val serializers = mutableMapOf<String, SerDe<T>>()
        private var defaultFormat = FORMAT_JSON
        
        /**
         * додає серіалізатор для формату
         *
         * @param format формат
         * @param serde серіалізатор/десеріалізатор
         * @return композитний серіалізатор
         */
        fun addFormat(format: String, serde: SerDe<T>): CompositeSerDe<T> {
            serializers[format] = serde
            return this
        }
        
        /**
         * встановлює формат за замовчуванням
         *
         * @param format формат
         * @return композитний серіалізатор
         */
        fun defaultFormat(format: String): CompositeSerDe<T> {
            this.defaultFormat = format
            return this
        }
        
        /**
         * серіалізує об'єкт у певний формат
         *
         * @param obj об'єкт
         * @param format формат
         * @return байтовий масив
         */
        fun serialize(obj: T, format: String): ByteArray {
            val serde = serializers[format] ?: serializers[defaultFormat]
                ?: throw IllegalArgumentException("Невідомий формат серіалізації: $format")
            return serde.serialize(obj)
        }
        
        /**
         * десеріалізує об'єкт з певного формату
         *
         * @param data байтовий масив
         * @param format формат
         * @return об'єкт
         */
        fun deserialize(data: ByteArray, format: String): T {
            val serde = serializers[format] ?: serializers[defaultFormat]
                ?: throw IllegalArgumentException("Невідомий формат серіалізації: $format")
            return serde.deserialize(data)
        }
        
        override fun serialize(obj: T): ByteArray {
            return serialize(obj, defaultFormat)
        }
        
        override fun deserialize(data: ByteArray): T {
            return deserialize(data, defaultFormat)
        }
    }
    
    /**
     * створює композитний серіалізатор/десеріалізатор
     *
     * @param T тип об'єкта
     * @return композитний серіалізатор/десеріалізатор
     */
    fun <T> createCompositeSerDe(): CompositeSerDe<T> {
        return CompositeSerDe()
    }
    
    // функції для роботи з серіалізаторами з підтримкою компресії
    
    /**
     * представлення серіалізатора/десеріалізатора з компресією
     *
     * @param T тип об'єкта
     * @param delegate делегат серіалізатора/десеріалізатора
     */
    class CompressedSerDe<T>(private val delegate: SerDe<T>) : BaseSerDe<T> {
        
        override fun serialize(obj: T): ByteArray {
            val uncompressedData = delegate.serialize(obj)
            return compress(uncompressedData)
        }
        
        override fun deserialize(data: ByteArray): T {
            val uncompressedData = decompress(data)
            return delegate.deserialize(uncompressedData)
        }
        
        /**
         * компресує дані
         *
         * @param data дані
         * @return компресовані дані
         */
        private fun compress(data: ByteArray): ByteArray {
            val outputStream = ByteArrayOutputStream()
            val gzipOutputStream = java.util.zip.GZIPOutputStream(outputStream)
            gzipOutputStream.write(data)
            gzipOutputStream.close()
            return outputStream.toByteArray()
        }
        
        /**
         * декомпресує дані
         *
         * @param data дані
         * @return декомпресовані дані
         */
        private fun decompress(data: ByteArray): ByteArray {
            val inputStream = ByteArrayInputStream(data)
            val gzipInputStream = java.util.zip.GZIPInputStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var bytesRead: Int
            while (gzipInputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            
            gzipInputStream.close()
            outputStream.close()
            return outputStream.toByteArray()
        }
    }
    
    /**
     * створює серіалізатор/десеріалізатор з компресією
     *
     * @param T тип об'єкта
     * @param delegate делегат серіалізатора/десеріалізатора
     * @return серіалізатор/десеріалізатор з компресією
     */
    fun <T> createCompressedSerDe(delegate: SerDe<T>): CompressedSerDe<T> {
        return CompressedSerDe(delegate)
    }
    
    // функції для роботи з серіалізаторами з підтримкою шифрування
    
    /**
     * представлення серіалізатора/десеріалізатора з шифруванням
     *
     * @param T тип об'єкта
     * @param delegate делегат серіалізатора/десеріалізатора
     * @param key ключ шифрування
     */
    class EncryptedSerDe<T>(
        private val delegate: SerDe<T>,
        private val key: ByteArray
    ) : BaseSerDe<T> {
        
        override fun serialize(obj: T): ByteArray {
            val uncompressedData = delegate.serialize(obj)
            val encryptedData = encrypt(uncompressedData)
            return encryptedData
        }
        
        override fun deserialize(data: ByteArray): T {
            val decryptedData = decrypt(data)
            return delegate.deserialize(decryptedData)
        }
        
        /**
         * шифрує дані
         *
         * @param data дані
         * @return зашифровані дані
         */
        private fun encrypt(data: ByteArray): ByteArray {
            // спрощена реалізація шифрування
            // в реальному застосунку слід використовувати надійний алгоритм шифрування
            val result = ByteArray(data.size)
            for (i in data.indices) {
                result[i] = (data[i].toInt() xor key[i % key.size].toInt()).toByte()
            }
            return result
        }
        
        /**
         * розшифровує дані
         *
         * @param data дані
         * @return розшифровані дані
         */
        private fun decrypt(data: ByteArray): ByteArray {
            // спрощена реалізація розшифрування
            // в реальному застосунку слід використовувати надійний алгоритм шифрування
            val result = ByteArray(data.size)
            for (i in data.indices) {
                result[i] = (data[i].toInt() xor key[i % key.size].toInt()).toByte()
            }
            return result
        }
    }
    
    /**
     * створює серіалізатор/десеріалізатор з шифруванням
     *
     * @param T тип об'єкта
     * @param delegate делегат серіалізатора/десеріалізатора
     * @param key ключ шифрування
     * @return серіалізатор/десеріалізатор з шифруванням
     */
    fun <T> createEncryptedSerDe(delegate: SerDe<T>, key: ByteArray): EncryptedSerDe<T> {
        return EncryptedSerDe(delegate, key)
    }
    
    // функції для роботи з серіалізаторами з підтримкою кешування
    
    /**
     * представлення серіалізатора/десеріалізатора з кешуванням
     *
     * @param T тип об'єкта
     * @param delegate делегат серіалізатора/десеріалізатора
     */
    class CachedSerDe<T>(
        private val delegate: SerDe<T>
    ) : BaseSerDe<T> {
        private val serializeCache = mutableMapOf<T, ByteArray>()
        private val deserializeCache = mutableMapOf<ByteArray, T>()
        private val cacheLock = Any()
        private val maxSize = 1000
        
        override fun serialize(obj: T): ByteArray {
            synchronized(cacheLock) {
                val cached = serializeCache[obj]
                if (cached != null) {
                    return cached
                }
            }
            
            val result = delegate.serialize(obj)
            
            synchronized(cacheLock) {
                // видаляємо найстаріші записи, якщо кеш переповнено
                if (serializeCache.size >= maxSize) {
                    val oldestKey = serializeCache.keys.firstOrNull()
                    if (oldestKey != null) {
                        serializeCache.remove(oldestKey)
                    }
                }
                
                serializeCache[obj] = result
            }
            
            return result
        }
        
        override fun deserialize(data: ByteArray): T {
            synchronized(cacheLock) {
                val cached = deserializeCache[data]
                if (cached != null) {
                    @Suppress("UNCHECKED_CAST")
                    return cached as T
                }
            }
            
            val result = delegate.deserialize(data)
            
            synchronized(cacheLock) {
                // видаляємо найстаріші записи, якщо кеш переповнено
                if (deserializeCache.size >= maxSize) {
                    val oldestKey = deserializeCache.keys.firstOrNull()
                    if (oldestKey != null) {
                        deserializeCache.remove(oldestKey)
                    }
                }
                
                deserializeCache[data] = result
            }
            
            return result
        }
        
        /**
         * очищує кеш
         */
        fun clearCache() {
            synchronized(cacheLock) {
                serializeCache.clear()
                deserializeCache.clear()
            }
        }
    }
    
    /**
     * створює серіалізатор/десеріалізатор з кешуванням
     *
     * @param T тип об'єкта
     * @param delegate делегат серіалізатора/десеріалізатора
     * @return серіалізатор/десеріалізатор з кешуванням
     */
    fun <T> createCachedSerDe(delegate: SerDe<T>): CachedSerDe<T> {
        return CachedSerDe(delegate)
    }
    
    // функції для роботи з серіалізаторами з підтримкою валідації
    
    /**
     * представлення серіалізатора/десеріалізатора з валідацією
     *
     * @param T тип об'єкта
     * @param delegate делегат серіалізатора/десеріалізатора
     * @param validator валідатор
     */
    class ValidatedSerDe<T>(
        private val delegate: SerDe<T>,
        private val validator: ValidationUtils.Validator<T>
    ) : BaseSerDe<T> {
        
        override fun serialize(obj: T): ByteArray {
            val validationResult = validator.validate(obj)
            if (!validationResult.isValid) {
                throw ValidationUtils.ValidationException("Об'єкт не пройшов валідацію: ${validationResult.errors.joinToString(", ")}")
            }
            return delegate.serialize(obj)
        }
        
        override fun deserialize(data: ByteArray): T {
            val obj = delegate.deserialize(data)
            val validationResult = validator.validate(obj)
            if (!validationResult.isValid) {
                throw ValidationUtils.ValidationException("Десеріалізований об'єкт не пройшов валідацію: ${validationResult.errors.joinToString(", ")}")
            }
            return obj
        }
    }
    
    /**
     * створює серіалізатор/десеріалізатор з валідацією
     *
     * @param T тип об'єкта
     * @param delegate делегат серіалізатора/десеріалізатора
     * @param validator валідатор
     * @return серіалізатор/десеріалізатор з валідацією
     */
    fun <T> createValidatedSerDe(
        delegate: SerDe<T>,
        validator: ValidationUtils.Validator<T>
    ): ValidatedSerDe<T> {
        return ValidatedSerDe(delegate, validator)
    }
    
    // функції для роботи з серіалізаторами з підтримкою метрик
    
    /**
     * представлення серіалізатора/десеріалізатора з метриками
     *
     * @param T тип об'єкта
     * @param delegate делегат серіалізатора/десеріалізатора
     */
    class MetricsSerDe<T>(
        private val delegate: SerDe<T>
    ) : BaseSerDe<T> {
        private var totalSerializations = 0L
        private var totalDeserializations = 0L
        private var totalSerializationTime = 0L
        private var totalDeserializationTime = 0L
        private var totalSerializedSize = 0L
        private val metricsLock = Any()
        
        override fun serialize(obj: T): ByteArray {
            val startTime = System.currentTimeMillis()
            val result = delegate.serialize(obj)
            val endTime = System.currentTimeMillis()
            
            synchronized(metricsLock) {
                totalSerializations++
                totalSerializationTime += (endTime - startTime)
                totalSerializedSize += result.size
            }
            
            return result
        }
        
        override fun deserialize(data: ByteArray): T {
            val startTime = System.currentTimeMillis()
            val result = delegate.deserialize(data)
            val endTime = System.currentTimeMillis()
            
            synchronized(metricsLock) {
                totalDeserializations++
                totalDeserializationTime += (endTime - startTime)
            }
            
            return result
        }
        
        /**
         * отримує метрики серіалізації/десеріалізації
         *
         * @return метрики
         */
        fun getMetrics(): SerializationMetrics {
            synchronized(metricsLock) {
                return SerializationMetrics(
                    totalSerializations = totalSerializations,
                    totalDeserializations = totalDeserializations,
                    totalSerializationTime = totalSerializationTime,
                    totalDeserializationTime = totalDeserializationTime,
                    totalSerializedSize = totalSerializedSize,
                    averageSerializationTime = if (totalSerializations > 0) totalSerializationTime.toDouble() / totalSerializations else 0.0,
                    averageDeserializationTime = if (totalDeserializations > 0) totalDeserializationTime.toDouble() / totalDeserializations else 0.0,
                    averageSerializedSize = if (totalSerializations > 0) totalSerializedSize.toDouble() / totalSerializations else 0.0
                )
            }
        }
        
        /**
         * скидає метрики
         */
        fun resetMetrics() {
            synchronized(metricsLock) {
                totalSerializations = 0
                totalDeserializations = 0
                totalSerializationTime = 0
                totalDeserializationTime = 0
                totalSerializedSize = 0
            }
        }
    }
    
    /**
     * представлення метрик серіалізації/десеріалізації
     *
     * @property totalSerializations загальна кількість серіалізацій
     * @property totalDeserializations загальна кількість десеріалізацій
     * @property totalSerializationTime загальний час серіалізацій в мілісекундах
     * @property totalDeserializationTime загальний час десеріалізацій в мілісекундах
     * @property totalSerializedSize загальний розмір серіалізованих даних в байтах
     * @property averageSerializationTime середній час серіалізації в мілісекундах
     * @property averageDeserializationTime середній час десеріалізації в мілісекундах
     * @property averageSerializedSize середній розмір серіалізованих даних в байтах
     */
    data class SerializationMetrics(
        val totalSerializations: Long,
        val totalDeserializations: Long,
        val totalSerializationTime: Long,
        val totalDeserializationTime: Long,
        val totalSerializedSize: Long,
        val averageSerializationTime: Double,
        val averageDeserializationTime: Double,
        val averageSerializedSize: Double
    )
    
    /**
     * створює серіалізатор/десеріалізатор з метриками
     *
     * @param T тип об'єкта
     * @param delegate делегат серіалізатора/десеріалізатора
     * @return серіалізатор/десеріалізатор з метриками
     */
    fun <T> createMetricsSerDe(delegate: SerDe<T>): MetricsSerDe<T> {
        return MetricsSerDe(delegate)
    }
    
    // функції для роботи з серіалізаторами з підтримкою логування
    
    /**
     * представлення серіалізатора/десеріалізатора з логуванням
     *
     * @param T тип об'єкта
     * @param delegate делегат серіалізатора/десеріалізатора
     * @param logger логер
     */
    class LoggingSerDe<T>(
        private val delegate: SerDe<T>,
        private val logger: LoggingUtils.Logger
    ) : BaseSerDe<T> {
        
        override fun serialize(obj: T): ByteArray {
            logger.debug("Початок серіалізації об'єкта: ${obj?.javaClass?.simpleName}")
            val startTime = System.currentTimeMillis()
            
            val result = delegate.serialize(obj)
            
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            logger.info("Серіалізація успішна (тривалість: ${duration}ms, розмір: ${result.size} байтів)")
            return result
        }
        
        override fun deserialize(data: ByteArray): T {
            logger.debug("Початок десеріалізації даних (розмір: ${data.size} байтів)")
            val startTime = System.currentTimeMillis()
            
            val result = delegate.deserialize(data)
            
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            logger.info("Десеріалізація успішна (тривалість: ${duration}ms, тип: ${result?.javaClass?.simpleName})")
            return result
        }
    }
    
    /**
     * створює серіалізатор/десеріалізатор з логуванням
     *
     * @param T тип об'єкта
     * @param delegate делегат серіалізатора/десеріалізатора
     * @param logger логер
     * @return серіалізатор/десеріалізатор з логуванням
     */
    fun <T> createLoggingSerDe(
        delegate: SerDe<T>,
        logger: LoggingUtils.Logger
    ): LoggingSerDe<T> {
        return LoggingSerDe(delegate, logger)
    }
    
    // функції для роботи з серіалізаторами з підтримкою асинхронної обробки
    
    /**
     * представлення асинхронного серіалізатора/десеріалізатора
     *
     * @param T тип об'єкта
     * @param delegate делегат серіалізатора/десеріалізатора
     */
    class AsyncSerDe<T>(
        private val delegate: SerDe<T>
    ) : BaseSerDe<T> {
        
        override fun serialize(obj: T): ByteArray {
            // в реальному застосунку тут може бути асинхронна серіалізація
            // наприклад, з використанням корутин або пулу потоків
            return delegate.serialize(obj)
        }
        
        override fun deserialize(data: ByteArray): T {
            // в реальному застосунку тут може бути асинхронна десеріалізація
            // наприклад, з використанням корутин або пулу потоків
            return delegate.deserialize(data)
        }
        
        /**
         * асинхронно серіалізує об'єкт
         *
         * @param obj об'єкт
         * @param callback зворотний виклик з результатом серіалізації
         */
        fun serializeAsync(obj: T, callback: (ByteArray) -> Unit) {
            // в реальному застосунку тут може бути справжня асинхронна серіалізація
            val result = serialize(obj)
            callback(result)
        }
        
        /**
         * асинхронно десеріалізує об'єкт
         *
         * @param data дані
         * @param callback зворотний виклик з результатом десеріалізації
         */
        fun deserializeAsync(data: ByteArray, callback: (T) -> Unit) {
            // в реальному застосунку тут може бути справжня асинхронна десеріалізація
            val result = deserialize(data)
            callback(result)
        }
    }
    
    /**
     * створює асинхронний серіалізатор/десеріалізатор
     *
     * @param T тип об'єкта
     * @param delegate делегат серіалізатора/десеріалізатора
     * @return асинхронний серіалізатор/десеріалізатор
     */
    fun <T> createAsyncSerDe(delegate: SerDe<T>): AsyncSerDe<T> {
        return AsyncSerDe(delegate)
    }
    
    // функції для роботи з серіалізаторами з підтримкою транзакцій
    
    /**
     * представлення серіалізатора/десеріалізатора з транзакційною підтримкою
     *
     * @param T тип об'єкта
     * @param delegate делегат серіалізатора/десеріалізатора
     */
    class TransactionalSerDe<T>(
        private val delegate: SerDe<T>
    ) : BaseSerDe<T> {
        private val transactionStack = mutableListOf<Pair<ByteArray, T?>>()
        
        override fun serialize(obj: T): ByteArray {
            val result = delegate.serialize(obj)
            transactionStack.add(Pair(result, obj))
            return result
        }
        
        override fun deserialize(data: ByteArray): T {
            val result = delegate.deserialize(data)
            transactionStack.add(Pair(data, result))
            return result
        }
        
        /**
         * відкатує останню операцію
         *
         * @return true якщо відкат вдався
         */
        fun rollback(): Boolean {
            return if (transactionStack.isNotEmpty()) {
                transactionStack.removeAt(transactionStack.size - 1)
                true
            } else {
                false
            }
        }
        
        /**
         * отримує історію операцій
         *
         * @return список пар (дані, об'єкт)
         */
        fun getTransactionHistory(): List<Pair<ByteArray, T?>> {
            return transactionStack.toList()
        }
        
        /**
         * очищує історію операцій
         */
        fun clearHistory() {
            transactionStack.clear()
        }
    }
    
    /**
     * створює серіалізатор/десеріалізатор з транзакційною підтримкою
     *
     * @param T тип об'єкта
     * @param delegate делегат серіалізатора/десеріалізатора
     * @return серіалізатор/десеріалізатор з транзакційною підтримкою
     */
    fun <T> createTransactionalSerDe(delegate: SerDe<T>): TransactionalSerDe<T> {
        return TransactionalSerDe(delegate)
    }
    
    // функції для роботи з серіалізаторами з підтримкою версій
    
    /**
     * представлення серіалізатора/десеріалізатора з підтримкою версій
     *
     * @param T тип об'єкта
     */
    class VersionedSerDe<T> : BaseSerDe<T> {
        private val versionedSerializers = mutableMapOf<Int, SerDe<T>>()
        private val currentVersion = 1
        
        /**
         * додає серіалізатор для версії
         *
         * @param version версія
         * @param serde серіалізатор/десеріалізатор
         * @return серіалізатор з підтримкою версій
         */
        fun addVersion(version: Int, serde: SerDe<T>): VersionedSerDe<T> {
            versionedSerializers[version] = serde
            return this
        }
        
        override fun serialize(obj: T): ByteArray {
            val serde = versionedSerializers[currentVersion]
                ?: throw IllegalStateException("Немає серіалізатора для поточної версії: $currentVersion")
            
            val data = serde.serialize(obj)
            // додаємо версію на початок даних
            val versionBytes = intToBytes(currentVersion)
            return versionBytes + data
        }
        
        override fun deserialize(data: ByteArray): T {
            if (data.size < 4) {
                throw IllegalArgumentException("Недійсні дані для десеріалізації")
            }
            
            val version = bytesToInt(data.copyOfRange(0, 4))
            val payload = data.copyOfRange(4, data.size)
            
            val serde = versionedSerializers[version]
                ?: throw IllegalArgumentException("Немає десеріалізатора для версії: $version")
            
            return serde.deserialize(payload)
        }
        
        /**
         * перетворює Int у ByteArray
         *
         * @param value значення
         * @return байтовий масив
         */
        private fun intToBytes(value: Int): ByteArray {
            return byteArrayOf(
                (value shr 24).toByte(),
                (value shr 16).toByte(),
                (value shr 8).toByte(),
                value.toByte()
            )
        }
        
        /**
         * перетворює ByteArray у Int
         *
         * @param bytes байтовий масив
         * @return значення
         */
        private fun bytesToInt(bytes: ByteArray): Int {
            return ((bytes[0].toInt() and 0xFF) shl 24) or
                   ((bytes[1].toInt() and 0xFF) shl 16) or
                   ((bytes[2].toInt() and 0xFF) shl 8) or
                   (bytes[3].toInt() and 0xFF)
        }
    }
    
    /**
     * створює серіалізатор/десеріалізатор з підтримкою версій
     *
     * @param T тип об'єкта
     * @return серіалізатор/десеріалізатор з підтримкою версій
     */
    fun <T> createVersionedSerDe(): VersionedSerDe<T> {
        return VersionedSerDe()
    }
    
    // функції для роботи з серіалізаторами з підтримкою конвертації між форматами
    
    /**
     * представлення конвертера між форматами серіалізації
     *
     * @param T тип об'єкта
     */
    class FormatConverter<T> {
        private val serializers = mutableMapOf<String, SerDe<T>>()
        
        /**
         * додає серіалізатор для формату
         *
         * @param format формат
         * @param serde серіалізатор/десеріалізатор
         * @return конвертер
         */
        fun addFormat(format: String, serde: SerDe<T>): FormatConverter<T> {
            serializers[format] = serde
            return this
        }
        
        /**
         * конвертує дані з одного формату в інший
         *
         * @param data дані
         * @param fromFormat вихідний формат
         * @param toFormat цільовий формат
         * @return конвертовані дані
         */
        fun convert(data: ByteArray, fromFormat: String, toFormat: String): ByteArray {
            val fromSerde = serializers[fromFormat]
                ?: throw IllegalArgumentException("Невідомий вихідний формат: $fromFormat")
            
            val toSerde = serializers[toFormat]
                ?: throw IllegalArgumentException("Невідомий цільовий формат: $toFormat")
            
            val obj = fromSerde.deserialize(data)
            return toSerde.serialize(obj)
        }
        
        /**
         * конвертує об'єкт у певний формат
         *
         * @param obj об'єкт
         * @param toFormat цільовий формат
         * @return дані у цільовому форматі
         */
        fun convertObject(obj: T, toFormat: String): ByteArray {
            val serde = serializers[toFormat]
                ?: throw IllegalArgumentException("Невідомий формат: $toFormat")
            
            return serde.serialize(obj)
        }
    }
    
    /**
     * створює конвертер між форматами серіалізації
     *
     * @param T тип об'єкта
     * @return конвертер
     */
    fun <T> createFormatConverter(): FormatConverter<T> {
        return FormatConverter()
    }
    
    // функції для роботи з серіалізаторами з підтримкою стрімінгу
    
    /**
     * представлення стрімінгового серіалізатора
     *
     * @param T тип об'єкта
     */
    class StreamingSerializer<T> {
        private val serializer: Serializer<T>
        
        constructor(serializer: Serializer<T>) {
            this.serializer = serializer
        }
        
        /**
         * серіалізує потік об'єктів у OutputStream
         *
         * @param objects потік об'єктів
         * @param outputStream вихідний потік
         */
        fun serializeStream(objects: Sequence<T>, outputStream: OutputStream) {
            val bufferedOutputStream = BufferedOutputStream(outputStream)
            
            objects.forEach { obj ->
                val data = serializer.serialize(obj)
                // записуємо розмір даних
                val sizeBytes = intToBytes(data.size)
                bufferedOutputStream.write(sizeBytes)
                // записуємо дані
                bufferedOutputStream.write(data)
            }
            
            bufferedOutputStream.flush()
        }
        
        /**
         * десеріалізує потік об'єктів з InputStream
         *
         * @param inputStream вхідний потік
         * @return потік об'єктів
         */
        fun deserializeStream(inputStream: InputStream): Sequence<T> {
            return sequence {
                val bufferedInputStream = BufferedInputStream(inputStream)
                
                while (true) {
                    // читаємо розмір даних
                    val sizeBytes = ByteArray(4)
                    val bytesRead = bufferedInputStream.read(sizeBytes)
                    if (bytesRead == -1) break
                    
                    val size = bytesToInt(sizeBytes)
                    if (size <= 0) break
                    
                    // читаємо дані
                    val data = ByteArray(size)
                    bufferedInputStream.read(data)
                    
                    // десеріалізуємо об'єкт
                    val obj = serializer.deserialize(data)
                    yield(obj)
                }
            }
        }
        
        /**
         * перетворює Int у ByteArray
         *
         * @param value значення
         * @return байтовий масив
         */
        private fun intToBytes(value: Int): ByteArray {
            return byteArrayOf(
                (value shr 24).toByte(),
                (value shr 16).toByte(),
                (value shr 8).toByte(),
                value.toByte()
            )
        }
        
        /**
         * перетворює ByteArray у Int
         *
         * @param bytes байтовий масив
         * @return значення
         */
        private fun bytesToInt(bytes: ByteArray): Int {
            return ((bytes[0].toInt() and 0xFF) shl 24) or
                   ((bytes[1].toInt() and 0xFF) shl 16) or
                   ((bytes[2].toInt() and 0xFF) shl 8) or
                   (bytes[3].toInt() and 0xFF)
        }
    }
    
    /**
     * створює стрімінговий серіалізатор
     *
     * @param T тип об'єкта
     * @param serializer серіалізатор
     * @return стрімінговий серіалізатор
     */
    fun <T> createStreamingSerializer(serializer: Serializer<T>): StreamingSerializer<T> {
        return StreamingSerializer(serializer)
    }
    
    // функції для роботи з серіалізаторами з підтримкою батчінгу
    
    /**
     * представлення батч-серіалізатора
     *
     * @param T тип об'єкта
     */
    class BatchSerializer<T>(private val serializer: Serializer<T>) {
        
        /**
         * серіалізує батч об'єктів
         *
         * @param objects об'єкти
         * @return байтовий масив
         */
        fun serializeBatch(objects: List<T>): ByteArray {
            val outputStream = ByteArrayOutputStream()
            
            // записуємо кількість об'єктів
            val countBytes = intToBytes(objects.size)
            outputStream.write(countBytes)
            
            // записуємо кожен об'єкт
            objects.forEach { obj ->
                val data = serializer.serialize(obj)
                // записуємо розмір даних
                val sizeBytes = intToBytes(data.size)
                outputStream.write(sizeBytes)
                // записуємо дані
                outputStream.write(data)
            }
            
            return outputStream.toByteArray()
        }
        
        /**
         * десеріалізує батч об'єктів
         *
         * @param data дані
         * @return список об'єктів
         */
        fun deserializeBatch(data: ByteArray): List<T> {
            val inputStream = ByteArrayInputStream(data)
            val objects = mutableListOf<T>()
            
            // читаємо кількість об'єктів
            val countBytes = ByteArray(4)
            inputStream.read(countBytes)
            val count = bytesToInt(countBytes)
            
            // читаємо кожен об'єкт
            repeat(count) {
                // читаємо розмір даних
                val sizeBytes = ByteArray(4)
                inputStream.read(sizeBytes)
                val size = bytesToInt(sizeBytes)
                
                // читаємо дані
                val objData = ByteArray(size)
                inputStream.read(objData)
                
                // десеріалізуємо об'єкт
                val obj = serializer.deserialize(objData)
                objects.add(obj)
            }
            
            return objects
        }
        
        /**
         * перетворює Int у ByteArray
         *
         * @param value значення
         * @return байтовий масив
         */
        private fun intToBytes(value: Int): ByteArray {
            return byteArrayOf(
                (value shr 24).toByte(),
                (value shr 16).toByte(),
                (value shr 8).toByte(),
                value.toByte()
            )
        }
        
        /**
         * перетворює ByteArray у Int
         *
         * @param bytes байтовий масив
         * @return значення
         */
        private fun bytesToInt(bytes: ByteArray): Int {
            return ((bytes[0].toInt() and 0xFF) shl 24) or
                   ((bytes[1].toInt() and 0xFF) shl 16) or
                   ((bytes[2].toInt() and 0xFF) shl 8) or
                   (bytes[3].toInt() and 0xFF)
        }
    }
    
    /**
     * створює батч-серіалізатор
     *
     * @param T тип об'єкта
     * @param serializer серіалізатор
     * @return батч-серіалізатор
     */
    fun <T> createBatchSerializer(serializer: Serializer<T>): BatchSerializer<T> {
        return BatchSerializer(serializer)
    }
}