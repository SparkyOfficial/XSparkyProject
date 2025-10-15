/**
 * фреймворк для роботи з конфігурацією
 *
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.io.*
import java.util.*
import java.util.concurrent.*

/**
 * представлення інтерфейсу для роботи з конфігурацією
 */
interface Configuration {
    /**
     * отримати значення за ключем
     *
     * @param key ключ
     * @return значення або null
     */
    fun getProperty(key: String): String?
    
    /**
     * отримати значення за ключем зі значенням за замовчуванням
     *
     * @param key ключ
     * @param defaultValue значення за замовчуванням
     * @return значення
     */
    fun getProperty(key: String, defaultValue: String): String
    
    /**
     * встановити значення за ключем
     *
     * @param key ключ
     * @param value значення
     */
    fun setProperty(key: String, value: String)
    
    /**
     * видалити значення за ключем
     *
     * @param key ключ
     * @return видалене значення або null
     */
    fun removeProperty(key: String): String?
    
    /**
     * перевірити, чи існує ключ
     *
     * @param key ключ
     * @return true, якщо ключ існує
     */
    fun containsProperty(key: String): Boolean
    
    /**
     * отримати всі ключі
     *
     * @return набір ключів
     */
    fun getPropertyKeys(): Set<String>
    
    /**
     * очистити всі властивості
     */
    fun clear()
}

/**
 * представлення базової реалізації конфігурації
 */
open class BaseConfiguration : Configuration {
    protected val properties = ConcurrentHashMap<String, String>()
    
    override fun getProperty(key: String): String? {
        return properties[key]
    }
    
    override fun getProperty(key: String, defaultValue: String): String {
        return properties.getOrDefault(key, defaultValue)
    }
    
    override fun setProperty(key: String, value: String) {
        properties[key] = value
    }
    
    override fun removeProperty(key: String): String? {
        return properties.remove(key)
    }
    
    override fun containsProperty(key: String): Boolean {
        return properties.containsKey(key)
    }
    
    override fun getPropertyKeys(): Set<String> {
        return properties.keys.toSet()
    }
    
    override fun clear() {
        properties.clear()
    }
}

/**
 * представлення інтерфейсу для завантаження конфігурації
 */
interface ConfigurationLoader {
    /**
     * завантажити конфігурацію з файлу
     *
     * @param file файл
     * @return конфігурація
     */
    fun loadFromFile(file: File): Configuration
    
    /**
     * завантажити конфігурацію з потоку
     *
     * @param inputStream потік
     * @return конфігурація
     */
    fun loadFromStream(inputStream: InputStream): Configuration
    
    /**
     * завантажити конфігурацію з ресурсу
     *
     * @param resourceName назва ресурсу
     * @return конфігурація
     */
    fun loadFromResource(resourceName: String): Configuration
}

/**
 * представлення базової реалізації завантажувача конфігурації
 */
open class BaseConfigurationLoader : ConfigurationLoader {
    
    override fun loadFromFile(file: File): Configuration {
        val config = BaseConfiguration()
        val properties = Properties()
        FileInputStream(file).use { fis ->
            properties.load(fis)
        }
        
        properties.forEach { key, value ->
            config.setProperty(key.toString(), value.toString())
        }
        
        return config
    }
    
    override fun loadFromStream(inputStream: InputStream): Configuration {
        val config = BaseConfiguration()
        val properties = Properties()
        properties.load(inputStream)
        
        properties.forEach { key, value ->
            config.setProperty(key.toString(), value.toString())
        }
        
        return config
    }
    
    override fun loadFromResource(resourceName: String): Configuration {
        val config = BaseConfiguration()
        val properties = Properties()
        val classLoader = Thread.currentThread().contextClassLoader
        val resourceAsStream = classLoader.getResourceAsStream(resourceName)
            ?: throw IllegalArgumentException("Ресурс не знайдено: $resourceName")
        
        properties.load(resourceAsStream)
        
        properties.forEach { key, value ->
            config.setProperty(key.toString(), value.toString())
        }
        
        return config
    }
}

/**
 * представлення інтерфейсу для збереження конфігурації
 */
interface ConfigurationSaver {
    /**
     * зберегти конфігурацію у файл
     *
     * @param config конфігурація
     * @param file файл
     */
    fun saveToFile(config: Configuration, file: File)
    
    /**
     * зберегти конфігурацію у потік
     *
     * @param config конфігурація
     * @param outputStream потік
     */
    fun saveToStream(config: Configuration, outputStream: OutputStream)
    
    /**
     * зберегти конфігурацію у властивості
     *
     * @param config конфігурація
     * @return властивості
     */
    fun saveToProperties(config: Configuration): Properties
}

/**
 * представлення базової реалізації зберігача конфігурації
 */
open class BaseConfigurationSaver : ConfigurationSaver {
    
    override fun saveToFile(config: Configuration, file: File) {
        FileOutputStream(file).use { fos ->
            saveToStream(config, fos)
        }
    }
    
    override fun saveToStream(config: Configuration, outputStream: OutputStream) {
        val properties = saveToProperties(config)
        properties.store(outputStream, "Збережена конфігурація")
    }
    
    override fun saveToProperties(config: Configuration): Properties {
        val properties = Properties()
        config.getPropertyKeys().forEach { key ->
            config.getProperty(key)?.let { value ->
                properties.setProperty(key, value)
            }
        }
        return properties
    }
}

/**
 * представлення інтерфейсу для роботи з вкладеною конфігурацією
 */
interface HierarchicalConfiguration : Configuration {
    /**
     * отримати підконфігурацію
     *
     * @param prefix префікс
     * @return підконфігурація
     */
    fun subset(prefix: String): HierarchicalConfiguration
    
    /**
     * отримати конфігурацію за шляхом
     *
     * @param path шлях
     * @return конфігурація або null
     */
    fun getConfigurationAt(path: String): Configuration?
    
    /**
     * отримати список конфігурацій за шляхом
     *
     * @param path шлях
     * @return список конфігурацій
     */
    fun getConfigurationsAt(path: String): List<Configuration>
}

/**
 * представлення базової реалізації вкладеної конфігурації
 */
open class BaseHierarchicalConfiguration : BaseConfiguration(), HierarchicalConfiguration {
    
    override fun subset(prefix: String): HierarchicalConfiguration {
        val subsetConfig = BaseHierarchicalConfiguration()
        val prefixWithDot = if (prefix.endsWith(".")) prefix else "$prefix."
        
        properties.forEach { key, value ->
            if (key.startsWith(prefixWithDot)) {
                val subKey = key.substring(prefixWithDot.length)
                subsetConfig.setProperty(subKey, value)
            }
        }
        
        return subsetConfig
    }
    
    override fun getConfigurationAt(path: String): Configuration? {
        // Заглушка для реалізації отримання конфігурації за шляхом
        return null
    }
    
    override fun getConfigurationsAt(path: String): List<Configuration> {
        // Заглушка для реалізації отримання списку конфігурацій за шляхом
        return emptyList()
    }
}

/**
 * представлення інтерфейсу для роботи з типізованими властивостями
 */
interface TypedConfiguration : Configuration {
    /**
     * отримати ціле число
     *
     * @param key ключ
     * @param defaultValue значення за замовчуванням
     * @return ціле число
     */
    fun getInt(key: String, defaultValue: Int): Int
    
    /**
     * отримати довге ціле число
     *
     * @param key ключ
     * @param defaultValue значення за замовчуванням
     * @return довге ціле число
     */
    fun getLong(key: String, defaultValue: Long): Long
    
    /**
     * отримати число з плаваючою точкою
     *
     * @param key ключ
     * @param defaultValue значення за замовчуванням
     * @return число з плаваючою точкою
     */
    fun getDouble(key: String, defaultValue: Double): Double
    
    /**
     * отримати булеве значення
     *
     * @param key ключ
     * @param defaultValue значення за замовчуванням
     * @return булеве значення
     */
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    
    /**
     * отримати список рядків
     *
     * @param key ключ
     * @param delimiter роздільник
     * @return список рядків
     */
    fun getStringList(key: String, delimiter: String = ","): List<String>
}

/**
 * представлення базової реалізації типізованої конфігурації
 */
open class BaseTypedConfiguration(private val configuration: Configuration) : TypedConfiguration, Configuration by configuration {
    
    override fun getInt(key: String, defaultValue: Int): Int {
        return try {
            configuration.getProperty(key)?.toInt() ?: defaultValue
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }
    
    override fun getLong(key: String, defaultValue: Long): Long {
        return try {
            configuration.getProperty(key)?.toLong() ?: defaultValue
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }
    
    override fun getDouble(key: String, defaultValue: Double): Double {
        return try {
            configuration.getProperty(key)?.toDouble() ?: defaultValue
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return configuration.getProperty(key)?.toBoolean() ?: defaultValue
    }
    
    override fun getStringList(key: String, delimiter: String): List<String> {
        return configuration.getProperty(key)?.split(delimiter)?.map { it.trim() } ?: emptyList()
    }
}

/**
 * представлення інтерфейсу для роботи з конфігурацією зі спостереженням
 */
interface ObservableConfiguration : Configuration {
    /**
     * додати спостерігача
     *
     * @param listener спостерігач
     */
    fun addConfigurationListener(listener: ConfigurationListener)
    
    /**
     * видалити спостерігача
     *
     * @param listener спостерігач
     */
    fun removeConfigurationListener(listener: ConfigurationListener)
}

/**
 * представлення спостерігача конфігурації
 */
interface ConfigurationListener {
    /**
     * викликається при зміні властивості
     *
     * @param event подія
     */
    fun configurationChanged(event: ConfigurationEvent)
}

/**
 * представлення події конфігурації
 *
 * @property type тип події
 * @property key ключ
 * @property value значення
 * @property beforeUpdate чи перед оновленням
 */
data class ConfigurationEvent(
    val type: ConfigurationEventType,
    val key: String,
    val value: String?,
    val beforeUpdate: Boolean
)

/**
 * представлення типу події конфігурації
 */
enum class ConfigurationEventType {
    ADD_PROPERTY,
    SET_PROPERTY,
    CLEAR_PROPERTY,
    CLEAR
}

/**
 * представлення базової реалізації конфігурації зі спостереженням
 */
open class BaseObservableConfiguration : BaseConfiguration(), ObservableConfiguration {
    private val listeners = mutableListOf<ConfigurationListener>()
    
    override fun addConfigurationListener(listener: ConfigurationListener) {
        listeners.add(listener)
    }
    
    override fun removeConfigurationListener(listener: ConfigurationListener) {
        listeners.remove(listener)
    }
    
    override fun setProperty(key: String, value: String) {
        val oldValue = getProperty(key)
        if (oldValue != value) {
            fireConfigurationEvent(ConfigurationEvent(ConfigurationEventType.SET_PROPERTY, key, oldValue, true))
            super.setProperty(key, value)
            fireConfigurationEvent(ConfigurationEvent(ConfigurationEventType.SET_PROPERTY, key, value, false))
        }
    }
    
    override fun removeProperty(key: String): String? {
        val oldValue = super.removeProperty(key)
        if (oldValue != null) {
            fireConfigurationEvent(ConfigurationEvent(ConfigurationEventType.CLEAR_PROPERTY, key, oldValue, true))
            fireConfigurationEvent(ConfigurationEvent(ConfigurationEventType.CLEAR_PROPERTY, key, oldValue, false))
        }
        return oldValue
    }
    
    override fun clear() {
        fireConfigurationEvent(ConfigurationEvent(ConfigurationEventType.CLEAR, "", null, true))
        super.clear()
        fireConfigurationEvent(ConfigurationEvent(ConfigurationEventType.CLEAR, "", null, false))
    }
    
    private fun fireConfigurationEvent(event: ConfigurationEvent) {
        listeners.forEach { listener ->
            try {
                listener.configurationChanged(event)
            } catch (e: Exception) {
                // Ігнорувати помилки спостерігачів
            }
        }
    }
}

/**
 * представлення інтерфейсу для роботи з конфігурацією з кешуванням
 */
interface CachingConfiguration : Configuration {
    /**
     * очистити кеш
     */
    fun clearCache()
    
    /**
     * встановити час життя кешу
     *
     * @param ttl час життя в мілісекундах
     */
    fun setCacheTtl(ttl: Long)
}

/**
 * представлення запису кешу конфігурації
 *
 * @property value значення
 * @property timestamp мітка часу
 * @property ttl час життя в мілісекундах
 */
data class ConfigurationCacheEntry(
    val value: String?,
    val timestamp: Long,
    val ttl: Long
) {
    /**
     * перевірити, чи запис прострочений
     *
     * @return true, якщо запис прострочений
     */
    fun isExpired(): Boolean {
        return (System.currentTimeMillis() - timestamp) > ttl
    }
}

/**
 * представлення базової реалізації конфігурації з кешуванням
 */
open class BaseCachingConfiguration(
    private val delegate: Configuration,
    private var cacheTtl: Long = 300000 // 5 хвилин за замовчуванням
) : CachingConfiguration, Configuration by delegate {
    
    private val cache = ConcurrentHashMap<String, ConfigurationCacheEntry>()
    
    override fun getProperty(key: String): String? {
        val cachedEntry = cache[key]
        if (cachedEntry != null && !cachedEntry.isExpired()) {
            return cachedEntry.value
        }
        
        val value = delegate.getProperty(key)
        cache[key] = ConfigurationCacheEntry(value, System.currentTimeMillis(), cacheTtl)
        return value
    }
    
    override fun clearCache() {
        cache.clear()
    }
    
    override fun setCacheTtl(ttl: Long) {
        this.cacheTtl = ttl
    }
    
    override fun setProperty(key: String, value: String) {
        delegate.setProperty(key, value)
        cache[key] = ConfigurationCacheEntry(value, System.currentTimeMillis(), cacheTtl)
    }
    
    override fun removeProperty(key: String): String? {
        val value = delegate.removeProperty(key)
        cache.remove(key)
        return value
    }
    
    override fun clear() {
        delegate.clear()
        cache.clear()
    }
}

/**
 * представлення інтерфейсу для роботи з конфігурацією з шифруванням
 */
interface EncryptedConfiguration : Configuration {
    /**
     * встановити шифрувальник
     *
     * @param encryptor шифрувальник
     */
    fun setEncryptor(encryptor: ConfigurationEncryptor)
}

/**
 * представлення шифрувальника конфігурації
 */
interface ConfigurationEncryptor {
    /**
     * зашифрувати значення
     *
     * @param value значення
     * @return зашифроване значення
     */
    fun encrypt(value: String): String
    
    /**
     * розшифрувати значення
     *
     * @param value зашифроване значення
     * @return розшифроване значення
     */
    fun decrypt(value: String): String
}

/**
 * представлення базової реалізації конфігурації з шифруванням
 */
open class BaseEncryptedConfiguration(
    private val delegate: Configuration,
    private var encryptor: ConfigurationEncryptor? = null
) : EncryptedConfiguration, Configuration by delegate {
    
    override fun getProperty(key: String): String? {
        val encryptedValue = delegate.getProperty(key)
        return if (encryptedValue != null && encryptor != null) {
            try {
                encryptor?.decrypt(encryptedValue)
            } catch (e: Exception) {
                // Якщо не вдалося розшифрувати, повертаємо оригінальне значення
                encryptedValue
            }
        } else {
            encryptedValue
        }
    }
    
    override fun setProperty(key: String, value: String) {
        val encryptedValue = encryptor?.encrypt(value) ?: value
        delegate.setProperty(key, encryptedValue)
    }
    
    override fun setEncryptor(encryptor: ConfigurationEncryptor) {
        this.encryptor = encryptor
    }
}

/**
 * представлення інтерфейсу для роботи з конфігурацією з перевагами
 */
interface CompositeConfiguration : Configuration {
    /**
     * додати конфігурацію
     *
     * @param config конфігурація
     */
    fun addConfiguration(config: Configuration)
    
    /**
     * видалити конфігурацію
     *
     * @param config конфігурація
     */
    fun removeConfiguration(config: Configuration)
    
    /**
     * отримати список конфігурацій
     *
     * @return список конфігурацій
     */
    fun getConfigurations(): List<Configuration>
}

/**
 * представлення базової реалізації композитної конфігурації
 */
open class BaseCompositeConfiguration : BaseConfiguration(), CompositeConfiguration {
    private val configurations = mutableListOf<Configuration>()
    
    override fun addConfiguration(config: Configuration) {
        configurations.add(config)
    }
    
    override fun removeConfiguration(config: Configuration) {
        configurations.remove(config)
    }
    
    override fun getConfigurations(): List<Configuration> {
        return configurations.toList()
    }
    
    override fun getProperty(key: String): String? {
        // Шукаємо властивість у всіх конфігураціях у порядку додавання
        configurations.forEach { config ->
            val value = config.getProperty(key)
            if (value != null) {
                return value
            }
        }
        
        // Якщо не знайдено, шукаємо в базовій конфігурації
        return super.getProperty(key)
    }
    
    override fun getPropertyKeys(): Set<String> {
        val keys = mutableSetOf<String>()
        configurations.forEach { config ->
            keys.addAll(config.getPropertyKeys())
        }
        keys.addAll(super.getPropertyKeys())
        return keys
    }
}

/**
 * представлення інтерфейсу для роботи з конфігурацією з перевагами за середовищем
 */
interface EnvironmentConfiguration : Configuration {
    /**
     * встановити префікс середовища
     *
     * @param prefix префікс
     */
    fun setEnvironmentPrefix(prefix: String)
    
    /**
     * встановити мапу заміни імен властивостей
     *
     * @param mapping мапа заміни
     */
    fun setPropertyMapping(mapping: Map<String, String>)
}

/**
 * представлення базової реалізації конфігурації з перевагами за середовищем
 */
open class BaseEnvironmentConfiguration(
    private val delegate: Configuration,
    private var environmentPrefix: String = "APP_",
    private val propertyMapping: MutableMap<String, String> = mutableMapOf()
) : EnvironmentConfiguration, Configuration by delegate {
    
    override fun getProperty(key: String): String? {
        // Спочатку перевіряємо змінні середовища
        val envKey = propertyMapping[key] ?: "${environmentPrefix}${key.uppercase().replace(".", "_")}"
        val envValue = System.getenv(envKey)
        if (envValue != null) {
            return envValue
        }
        
        // Потім перевіряємо системні властивості
        val sysValue = System.getProperty(key)
        if (sysValue != null) {
            return sysValue
        }
        
        // Нарешті, перевіряємо делегат
        return delegate.getProperty(key)
    }
    
    override fun setEnvironmentPrefix(prefix: String) {
        this.environmentPrefix = prefix
    }
    
    override fun setPropertyMapping(mapping: Map<String, String>) {
        this.propertyMapping.clear()
        this.propertyMapping.putAll(mapping)
    }
}

/**
 * представлення інтерфейсу для роботи з конфігурацією з валідацією
 */
interface ValidatingConfiguration : Configuration {
    /**
     * додати валідатор
     *
     * @param key ключ
     * @param validator валідатор
     */
    fun addValidator(key: String, validator: ConfigurationValidator)
    
    /**
     * видалити валідатор
     *
     * @param key ключ
     */
    fun removeValidator(key: String)
    
    /**
     * перевірити конфігурацію
     *
     * @return список помилок
     */
    fun validate(): List<ValidationError>
}

/**
 * представлення валідатора конфігурації
 */
interface ConfigurationValidator {
    /**
     * перевірити значення
     *
     * @param value значення
     * @return помилка або null
     */
    fun validate(value: String?): ValidationError?
}

/**
 * представлення помилки валідації
 *
 * @property key ключ
 * @property message повідомлення
 */
data class ValidationError(
    val key: String,
    val message: String
)

/**
 * представлення базової реалізації конфігурації з валідацією