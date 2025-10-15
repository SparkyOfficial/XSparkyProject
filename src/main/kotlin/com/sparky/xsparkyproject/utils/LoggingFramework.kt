/**
 * фреймворк для логування
 *
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.io.*
import java.time.*
import java.time.format.*
import java.util.concurrent.*
import java.util.logging.*

/**
 * представлення інтерфейсу для логування
 */
interface Logger {
    /**
     * записати повідомлення рівня TRACE
     *
     * @param message повідомлення
     * @param throwable виключення (необов'язково)
     */
    fun trace(message: String, throwable: Throwable? = null)
    
    /**
     * записати повідомлення рівня DEBUG
     *
     * @param message повідомлення
     * @param throwable виключення (необов'язково)
     */
    fun debug(message: String, throwable: Throwable? = null)
    
    /**
     * записати повідомлення рівня INFO
     *
     * @param message повідомлення
     * @param throwable виключення (необов'язково)
     */
    fun info(message: String, throwable: Throwable? = null)
    
    /**
     * записати повідомлення рівня WARN
     *
     * @param message повідомлення
     * @param throwable виключення (необов'язково)
     */
    fun warn(message: String, throwable: Throwable? = null)
    
    /**
     * записати повідомлення рівня ERROR
     *
     * @param message повідомлення
     * @param throwable виключення (необов'язково)
     */
    fun error(message: String, throwable: Throwable? = null)
    
    /**
     * перевірити, чи ввімкнений рівень TRACE
     *
     * @return true, якщо ввімкнений
     */
    fun isTraceEnabled(): Boolean
    
    /**
     * перевірити, чи ввімкнений рівень DEBUG
     *
     * @return true, якщо ввімкнений
     */
    fun isDebugEnabled(): Boolean
    
    /**
     * перевірити, чи ввімкнений рівень INFO
     *
     * @return true, якщо ввімкнений
     */
    fun isInfoEnabled(): Boolean
    
    /**
     * перевірити, чи ввімкнений рівень WARN
     *
     * @return true, якщо ввімкнений
     */
    fun isWarnEnabled(): Boolean
    
    /**
     * перевірити, чи ввімкнений рівень ERROR
     *
     * @return true, якщо ввімкнений
     */
    fun isErrorEnabled(): Boolean
}

/**
 * представлення рівня логування
 */
enum class LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    OFF
}

/**
 * представлення базової реалізації логування
 */
open class BaseLogger(private val name: String, private var level: LogLevel = LogLevel.INFO) : Logger {
    
    override fun trace(message: String, throwable: Throwable?) {
        if (isTraceEnabled()) {
            log(LogLevel.TRACE, message, throwable)
        }
    }
    
    override fun debug(message: String, throwable: Throwable?) {
        if (isDebugEnabled()) {
            log(LogLevel.DEBUG, message, throwable)
        }
    }
    
    override fun info(message: String, throwable: Throwable?) {
        if (isInfoEnabled()) {
            log(LogLevel.INFO, message, throwable)
        }
    }
    
    override fun warn(message: String, throwable: Throwable?) {
        if (isWarnEnabled()) {
            log(LogLevel.WARN, message, throwable)
        }
    }
    
    override fun error(message: String, throwable: Throwable?) {
        if (isErrorEnabled()) {
            log(LogLevel.ERROR, message, throwable)
        }
    }
    
    override fun isTraceEnabled(): Boolean = level.ordinal <= LogLevel.TRACE.ordinal
    
    override fun isDebugEnabled(): Boolean = level.ordinal <= LogLevel.DEBUG.ordinal
    
    override fun isInfoEnabled(): Boolean = level.ordinal <= LogLevel.INFO.ordinal
    
    override fun isWarnEnabled(): Boolean = level.ordinal <= LogLevel.WARN.ordinal
    
    override fun isErrorEnabled(): Boolean = level.ordinal <= LogLevel.ERROR.ordinal
    
    private fun log(level: LogLevel, message: String, throwable: Throwable?) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        val logMessage = "[$timestamp] [$level] [$name] $message"
        
        println(logMessage)
        throwable?.printStackTrace()
    }
    
    /**
     * встановити рівень логування
     *
     * @param level рівень
     */
    fun setLevel(level: LogLevel) {
        this.level = level
    }
}

/**
 * представлення інтерфейсу для фабрики логування
 */
interface LoggerFactory {
    /**
     * отримати логувальник за назвою
     *
     * @param name назва
     * @return логувальник
     */
    fun getLogger(name: String): Logger
    
    /**
     * отримати логувальник за класом
     *
     * @param clazz клас
     * @return логувальник
     */
    fun getLogger(clazz: Class<*>): Logger
}

/**
 * представлення базової реалізації фабрики логування
 */
open class BaseLoggerFactory : LoggerFactory {
    private val loggers = ConcurrentHashMap<String, Logger>()
    
    override fun getLogger(name: String): Logger {
        return loggers.getOrPut(name) { BaseLogger(name) }
    }
    
    override fun getLogger(clazz: Class<*>): Logger {
        return getLogger(clazz.name)
    }
}

/**
 * представлення інтерфейсу для апендера логів
 */
interface LogAppender {
    /**
     * додати запис логу
     *
     * @param logEvent подія логу
     */
    fun append(logEvent: LogEvent)
    
    /**
     * закрити апендер
     */
    fun close()
}

/**
 * представлення події логу
 *
 * @property loggerName назва логувальника
 * @property level рівень
 * @property message повідомлення
 * @property timestamp мітка часу
 * @property threadName назва потоку
 * @property throwable виключення
 */
data class LogEvent(
    val loggerName: String,
    val level: LogLevel,
    val message: String,
    val timestamp: Long,
    val threadName: String,
    val throwable: Throwable?
)

/**
 * представлення апендера консолі
 */
class ConsoleAppender : LogAppender {
    
    override fun append(logEvent: LogEvent) {
        val timestamp = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(logEvent.timestamp),
            ZoneId.systemDefault()
        ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        
        val logMessage = "[$timestamp] [${logEvent.level}] [${logEvent.loggerName}] [${logEvent.threadName}] ${logEvent.message}"
        println(logMessage)
        logEvent.throwable?.printStackTrace()
    }
    
    override fun close() {
        // Нічого не потрібно закривати для консолі
    }
}

/**
 * представлення апендера файлу
 *
 * @property fileName назва файлу
 * @property bufferSize розмір буфера
 */
class FileAppender(private val fileName: String, private val bufferSize: Int = 8192) : LogAppender {
    private val writer: BufferedWriter
    
    init {
        val file = File(fileName)
        file.parentFile?.mkdirs()
        writer = BufferedWriter(FileWriter(file, true), bufferSize)
    }
    
    override fun append(logEvent: LogEvent) {
        val timestamp = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(logEvent.timestamp),
            ZoneId.systemDefault()
        ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        
        val logMessage = "[$timestamp] [${logEvent.level}] [${logEvent.loggerName}] [${logEvent.threadName}] ${logEvent.message}"
        writer.write(logMessage)
        writer.newLine()
        
        if (logEvent.throwable != null) {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            logEvent.throwable.printStackTrace(pw)
            writer.write(sw.toString())
            writer.newLine()
        }
        
        writer.flush()
    }
    
    override fun close() {
        writer.close()
    }
}

/**
 * представлення апендера з обертанням файлів
 *
 * @property fileName назва файлу
 * @property maxFileSize максимальний розмір файлу
 * @property maxBackupIndex максимальна кількість резервних файлів
 */
class RollingFileAppender(
    private val fileName: String,
    private val maxFileSize: Long = 10 * 1024 * 1024, // 10 MB за замовчуванням
    private val maxBackupIndex: Int = 5
) : LogAppender {
    
    private var writer: BufferedWriter? = null
    private var currentFileSize: Long = 0
    
    init {
        openWriter()
    }
    
    override fun append(logEvent: LogEvent) {
        val timestamp = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(logEvent.timestamp),
            ZoneId.systemDefault()
        ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        
        val logMessage = "[$timestamp] [${logEvent.level}] [${logEvent.loggerName}] [${logEvent.threadName}] ${logEvent.message}"
        
        writer?.let { w ->
            w.write(logMessage)
            w.newLine()
            
            if (logEvent.throwable != null) {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                logEvent.throwable.printStackTrace(pw)
                w.write(sw.toString())
                w.newLine()
            }
            
            w.flush()
            
            currentFileSize += logMessage.length + 1 // +1 для нового рядка
            if (logEvent.throwable != null) {
                currentFileSize += logEvent.throwable.stackTrace.size * 100 // Приблизний розмір трасування
            }
            
            if (currentFileSize > maxFileSize) {
                rotate()
            }
        }
    }
    
    override fun close() {
        writer?.close()
    }
    
    private fun openWriter() {
        val file = File(fileName)
        file.parentFile?.mkdirs()
        writer = BufferedWriter(FileWriter(file, true))
        currentFileSize = file.length()
    }
    
    private fun rotate() {
        writer?.close()
        
        // Перейменувати існуючі файли
        for (i in maxBackupIndex downTo 1) {
            val oldFile = File("$fileName.$i")
            if (oldFile.exists()) {
                if (i == maxBackupIndex) {
                    oldFile.delete()
                } else {
                    oldFile.renameTo(File("$fileName.${i + 1}"))
                }
            }
        }
        
        // Перейменувати основний файл
        val mainFile = File(fileName)
        if (mainFile.exists()) {
            mainFile.renameTo(File("$fileName.1"))
        }
        
        openWriter()
    }
}

/**
 * представлення інтерфейсу для форматування логів
 */
interface LogFormatter {
    /**
     * форматувати подію логу
     *
     * @param logEvent подія логу
     * @return форматований рядок
     */
    fun format(logEvent: LogEvent): String
}

/**
 * представлення базового форматувальника логів
 */
class BaseLogFormatter : LogFormatter {
    
    override fun format(logEvent: LogEvent): String {
        val timestamp = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(logEvent.timestamp),
            ZoneId.systemDefault()
        ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        
        return "[$timestamp] [${logEvent.level}] [${logEvent.loggerName}] [${logEvent.threadName}] ${logEvent.message}"
    }
}

/**
 * представлення форматувальника JSON логів
 */
class JsonLogFormatter : LogFormatter {
    
    override fun format(logEvent: LogEvent): String {
        val timestamp = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(logEvent.timestamp),
            ZoneId.systemDefault()
        ).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        
        val throwableStr = logEvent.throwable?.let { throwable ->
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            sw.toString()
        }
        
        return """{
            |  "timestamp": "$timestamp",
            |  "level": "${logEvent.level}",
            |  "logger": "${logEvent.loggerName}",
            |  "thread": "${logEvent.threadName}",
            |  "message": "${logEvent.message.replace("\"", "\\\"")}"${throwableStr?.let { ",\n  \"throwable\": \"${it.replace("\"", "\\\"").replace("\n", "\\n")}\"" } ?: ""}
            |}""".trimMargin()
    }
}

/**
 * представлення інтерфейсу для фільтрації логів
 */
interface LogFilter {
    /**
     * перевірити, чи подія логу відповідає критеріям фільтра
     *
     * @param logEvent подія логу
     * @return true, якщо подія відповідає критеріям
     */
    fun accept(logEvent: LogEvent): Boolean
}

/**
 * представлення фільтра рівня логування
 *
 * @property level рівень
 */
class LevelFilter(private val level: LogLevel) : LogFilter {
    
    override fun accept(logEvent: LogEvent): Boolean {
        return logEvent.level.ordinal >= level.ordinal
    }
}

/**
 * представлення фільтра назви логувальника
 *
 * @property loggerName назва логувальника
 */
class LoggerNameFilter(private val loggerName: String) : LogFilter {
    
    override fun accept(logEvent: LogEvent): Boolean {
        return logEvent.loggerName == loggerName
    }
}

/**
 * представлення комбінованого фільтра
 *
 * @property filters список фільтрів
 */
class CompositeFilter(private val filters: List<LogFilter>) : LogFilter {
    
    override fun accept(logEvent: LogEvent): Boolean {
        return filters.all { it.accept(logEvent) }
    }
}

/**
 * представлення інтерфейсу для асинхронного логування
 */
interface AsyncLogger : Logger {
    /**
     * додати апендер
     *
     * @param appender апендер
     */
    fun addAppender(appender: LogAppender)
    
    /**
     * встановити форматувальник
     *
     * @param formatter форматувальник
     */
    fun setFormatter(formatter: LogFormatter)
    
    /**
     * додати фільтр
     *
     * @param filter фільтр
     */
    fun addFilter(filter: LogFilter)
    
    /**
     * закрити логувальник
     */
    fun close()
}

/**
 * представлення базової реалізації асинхронного логування
 */
class BaseAsyncLogger(
    name: String,
    level: LogLevel = LogLevel.INFO
) : BaseLogger(name, level), AsyncLogger {
    
    private val appenders = mutableListOf<LogAppender>()
    private val filters = mutableListOf<LogFilter>()
    private var formatter: LogFormatter = BaseLogFormatter()
    private val executor = Executors.newSingleThreadExecutor()
    
    override fun addAppender(appender: LogAppender) {
        appenders.add(appender)
    }
    
    override fun setFormatter(formatter: LogFormatter) {
        this.formatter = formatter
    }
    
    override fun addFilter(filter: LogFilter) {
        filters.add(filter)
    }
    
    override fun close() {
        executor.shutdown()
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executor.shutdownNow()
            Thread.currentThread().interrupt()
        }
        
        appenders.forEach { it.close() }
    }
    
    override fun trace(message: String, throwable: Throwable?) {
        if (isTraceEnabled()) {
            logAsync(LogLevel.TRACE, message, throwable)
        }
    }
    
    override fun debug(message: String, throwable: Throwable?) {
        if (isDebugEnabled()) {
            logAsync(LogLevel.DEBUG, message, throwable)
        }
    }
    
    override fun info(message: String, throwable: Throwable?) {
        if (isInfoEnabled()) {
            logAsync(LogLevel.INFO, message, throwable)
        }
    }
    
    override fun warn(message: String, throwable: Throwable?) {
        if (isWarnEnabled()) {
            logAsync(LogLevel.WARN, message, throwable)
        }
    }
    
    override fun error(message: String, throwable: Throwable?) {
        if (isErrorEnabled()) {
            logAsync(LogLevel.ERROR, message, throwable)
        }
    }
    
    private fun logAsync(level: LogLevel, message: String, throwable: Throwable?) {
        val logEvent = LogEvent(
            name,
            level,
            message,
            System.currentTimeMillis(),
            Thread.currentThread().name,
            throwable
        )
        
        // Перевірити фільтри
        if (filters.isNotEmpty() && !filters.all { it.accept(logEvent) }) {
            return
        }
        
        executor.submit {
            appenders.forEach { appender ->
                try {
                    appender.append(logEvent)
                } catch (e: Exception) {
                    // Ігнорувати помилки апендера, щоб не зламати основний потік
                    System.err.println("Помилка апендера: ${e.message}")
                }
            }
        }
    }
}

/**
 * представлення інтерфейсу для контексту логування
 */
interface LoggingContext {
    /**
     * додати змінну контексту
     *
     * @param key ключ
     * @param value значення
     */
    fun put(key: String, value: String)
    
    /**
     * отримати змінну контексту
     *
     * @param key ключ
     * @return значення або null
     */
    fun get(key: String): String?
    
    /**
     * видалити змінну контексту
     *
     * @param key ключ
     */
    fun remove(key: String)
    
    /**
     * очистити контекст
     */
    fun clear()
    
    /**
     * отримати всі змінні контексту
     *
     * @return мапа змінних
     */
    fun getAll(): Map<String, String>
}

/**
 * представлення базової реалізації контексту логування
 */
class BaseLoggingContext : LoggingContext {
    private val context = ThreadLocal<Map<String, String>>()
    
    override fun put(key: String, value: String) {
        val currentContext = context.get() ?: emptyMap()
        context.set(currentContext + (key to value))
    }
    
    override fun get(key: String): String? {
        return context.get()?.get(key)
    }
    
    override fun remove(key: String) {
        val currentContext = context.get() ?: return
        context.set(currentContext - key)
    }
    
    override fun clear() {
        context.remove()
    }
    
    override fun getAll(): Map<String, String> {
        return context.get() ?: emptyMap()
    }
}

/**
 * представлення інтерфейсу для MDC (Mapped Diagnostic Context)
 */
interface MDC {
    /**
     * додати змінну в MDC
     *
     * @param key ключ
     * @param value значення
     */
    fun put(key: String, value: String)
    
    /**
     * отримати змінну з MDC
     *
     * @param key ключ
     * @return значення або null
     */
    fun get(key: String): String?
    
    /**
     * видалити змінну з MDC
     *
     * @param key ключ
     */
    fun remove(key: String)
    
    /**
     * очистити MDC
     */
    fun clear()
}

/**
 * представлення базової реалізації MDC
 */
object BaseMDC : MDC {
    private val mdc = ThreadLocal<Map<String, String>>()
    
    override fun put(key: String, value: String) {
        val currentMdc = mdc.get() ?: emptyMap()
        mdc.set(currentMdc + (key to value))
    }
    
    override fun get(key: String): String? {
        return mdc.get()?.get(key)
    }
    
    override fun remove(key: String) {
        val currentMdc = mdc.get() ?: return
        mdc.set(currentMdc - key)
    }
    
    override fun clear() {
        mdc.remove()
    }
}

/**
 * представлення інтерфейсу для NDC (Nested Diagnostic Context)
 */
interface NDC {
    /**
     * додати повідомлення в NDC
     *
     * @param message повідомлення
     */
    fun push(message: String)
    
    /**
     * видалити останнє повідомлення з NDC
     *
     * @return останнє повідомлення або null
     */
    fun pop(): String?
    
    /**
     * отримати всі повідомлення з NDC
     *
     * @return список повідомлень
     */
    fun getAll(): List<String>
    
    /**
     * очистити NDC
     */
    fun clear()
}

/**
 * представлення базової реалізації NDC
 */
object BaseNDC : NDC {
    private val ndc = ThreadLocal<Stack<String>>()
    
    override fun push(message: String) {
        val stack = ndc.get() ?: Stack<String>().also { ndc.set(it) }
        stack.push(message)
    }
    
    override fun pop(): String? {
        val stack = ndc.get() ?: return null
        return if (stack.isEmpty()) null else stack.pop()
    }
    
    override fun getAll(): List<String> {
        val stack = ndc.get() ?: return emptyList()
        return stack.toList()
    }
    
    override fun clear() {
        ndc.remove()
    }
}

/**
 * представлення інтерфейсу для конфігурації логування
 */
interface LoggingConfiguration {
    /**
     * отримати рівень логування за замовчуванням
     *
     * @return рівень логування
     */
    fun getDefaultLevel(): LogLevel
    
    /**
     * отримати конфігурацію логувальника
     *
     * @param loggerName назва логувальника
     * @return конфігурація або null
     */
    fun getLoggerConfiguration(loggerName: String): LoggerConfiguration?
    
    /**
     * додати апендер за замовчуванням
     *
     * @param appender апендер
     */
    fun addDefaultAppender(appender: LogAppender)
    
    /**
     * встановити форматувальник за замовчуванням
     *
     * @param formatter форматувальник
     */
    fun setDefaultFormatter(formatter: LogFormatter)
}

/**
 * представлення конфігурації логувальника
 *
 * @property level рівень логування
 * @property appenders список апендерів
 * @property formatter форматувальник
 * @property filters список фільтрів
 */
data class LoggerConfiguration(
    val level: LogLevel,
    val appenders: List<LogAppender>,
    val formatter: LogFormatter,
    val filters: List<LogFilter>
)

/**
 * представлення базової реалізації конфігурації логування
 */
class BaseLoggingConfiguration : LoggingConfiguration {
    private var defaultLevel: LogLevel = LogLevel.INFO
    private val defaultAppenders = mutableListOf<LogAppender>()
    private var defaultFormatter: LogFormatter = BaseLogFormatter()
    private val loggerConfigurations = mutableMapOf<String, LoggerConfiguration>()
    
    override fun getDefaultLevel(): LogLevel = defaultLevel
    
    override fun getLoggerConfiguration(loggerName: String): LoggerConfiguration? {
        return loggerConfigurations[loggerName]
    }
    
    override fun addDefaultAppender(appender: LogAppender) {
        defaultAppenders.add(appender)
    }
    
    override fun setDefaultFormatter(formatter: LogFormatter) {
        this.defaultFormatter = formatter
    }
    
    /**
     * встановити рівень логування за замовчуванням
     *
     * @param level рівень
     */
    fun setDefaultLevel(level: LogLevel) {
        this.defaultLevel = level
    }
    
    /**
     * додати конфігурацію логувальника
     *
     * @param loggerName назва логувальника
     * @param configuration конфігурація
     */
    fun addLoggerConfiguration(loggerName: String, configuration: LoggerConfiguration) {
        loggerConfigurations[loggerName] = configuration
    }
}

/**
 * представлення інтерфейсу для метрик логування
 */
interface LoggingMetrics {
    /**
     * отримати кількість записів логу за рівнем
     *
     * @param level рівень
     * @return кількість записів
     */
    fun getLogCount(level: LogLevel): Long
    
    /**
     * отримати загальну кількість записів логу
     *
     * @return кількість записів
     */
    fun getTotalLogCount(): Long
    
    /**
     * отримати кількість помилок апендерів
     *
     * @return кількість помилок
     */
    fun getAppenderErrorCount(): Long
    
    /**
     * скинути метрики
     */
    fun resetMetrics()
}

/**
 * представлення базової реалізації метрик логування
 */
class BaseLoggingMetrics : LoggingMetrics {
    private val logCounts = EnumMap<LogLevel, AtomicLong>(LogLevel::class.java)
    private val totalLogCount = AtomicLong(0)
    private val appenderErrorCount = AtomicLong(0)
    
    init {
        LogLevel.values().forEach { level ->
            logCounts[level] = AtomicLong(0)
        }
    }
    
    override fun getLogCount(level: LogLevel): Long {
        return logCounts[level]?.get() ?: 0
    }
    
    override fun getTotalLogCount(): Long {
        return totalLogCount.get()
    }
    
    override fun getAppenderErrorCount(): Long {
        return appenderErrorCount.get()
    }
    
    override fun resetMetrics() {
        LogLevel.values().forEach { level ->
            logCounts[level]?.set(0)
        }
        totalLogCount.set(0)
        appenderErrorCount.set(0)
    }
    
    /**
     * збільшити лічильник записів логу
     *
     * @param level рівень
     */
    fun incrementLogCount(level: LogLevel) {
        logCounts[level]?.incrementAndGet()
        totalLogCount.incrementAndGet()
    }
    
    /**
     * збільшити лічильник помилок апендерів
     */
    fun incrementAppenderErrorCount() {
        appenderErrorCount.incrementAndGet()
    }
}

/**
 * представлення інтерфейсу для логування з метриками
 */
interface MetricsLogger : Logger {
    /**
     * отримати метрики
     *
     * @return метрики
     */
    fun getMetrics(): LoggingMetrics
}

/**
 * представлення базової реалізації логування з метриками
 */
class BaseMetricsLogger(
    name: String,
    level: LogLevel = LogLevel.INFO,
    private val metrics: LoggingMetrics = BaseLoggingMetrics()
) : BaseLogger(name, level), MetricsLogger {
    
    override fun getMetrics(): LoggingMetrics = metrics
    
    override fun trace(message: String, throwable: Throwable?) {
        if (isTraceEnabled()) {
            metrics.incrementLogCount(LogLevel.TRACE)
            super.trace(message, throwable)
        }
    }
    
    override fun debug(message: String, throwable: Throwable?) {
        if (isDebugEnabled()) {
            metrics.incrementLogCount(LogLevel.DEBUG)
            super.debug(message, throwable)
        }
    }
    
    override fun info(message: String, throwable: Throwable?) {
        if (isInfoEnabled()) {
            metrics.incrementLogCount(LogLevel.INFO)
            super.info(message, throwable)
        }
    }
    
    override fun warn(message: String, throwable: Throwable?) {
        if (isWarnEnabled()) {
            metrics.incrementLogCount(LogLevel.WARN)
            super.warn(message, throwable)
        }
    }
    
    override fun error(message: String, throwable: Throwable?) {
        if (isErrorEnabled()) {
            metrics.incrementLogCount(LogLevel.ERROR)
            super.error(message, throwable)
        }
    }
}

/**
 * представлення інтерфейсу для логування з контекстом
 */
interface ContextualLogger : Logger {
    /**
     * встановити контекст логування
     *
     * @param context контекст
     */
    fun setContext(context: LoggingContext)
    
    /**
     * отримати контекст логування
     *
     * @return контекст
     */
    fun getContext(): LoggingContext
}

/**
 * представлення базової реалізації логування з контекстом