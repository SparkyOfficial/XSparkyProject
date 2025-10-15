/**
 * xsparkyproject - розширена бібліотека для kotlin
 *
 * Copyright 2025 Андрій Будильников
 */

package com.sparky.xsparkyproject.utils

import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.*
import java.util.logging.*
import kotlin.math.min

/**
 * утилітарний клас для роботи з логуванням
 *
 * @author андрій будильников
 * @since 1.0.0
 */
@Suppress("unused")
class LoggingUtils {
    
    companion object {
        // стандартні рівні логування
        const val LEVEL_TRACE = 0
        const val LEVEL_DEBUG = 1
        const val LEVEL_INFO = 2
        const val LEVEL_WARN = 3
        const val LEVEL_ERROR = 4
        const val LEVEL_FATAL = 5
        
        // стандартні формати
        const val DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
        const val DEFAULT_LOG_FORMAT = "[%timestamp%] [%level%] [%logger%] %message%"
        const val DEFAULT_FILE_FORMAT = "[%timestamp%] [%level%] [%logger%] [%thread%] %message%"
        
        // стандартні імена файлів
        const val DEFAULT_LOG_FILE = "application.log"
        const val DEFAULT_ERROR_FILE = "error.log"
        
        // стандартні налаштування
        const val DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024L // 10MB
        const val DEFAULT_MAX_BACKUP_FILES = 5
        const val DEFAULT_BUFFER_SIZE = 8192
        const val DEFAULT_FLUSH_INTERVAL = 5000L // 5 секунд
    }
    
    // базові функції для роботи з логерами
    
    /**
     * представлення логера
     */
    interface Logger {
        /**
         * отримує ім'я логера
         *
         * @return ім'я логера
         */
        fun getName(): String
        
        /**
         * встановлює рівень логування
         *
         * @param level рівень логування
         */
        fun setLevel(level: Int)
        
        /**
         * отримує рівень логування
         *
         * @return рівень логування
         */
        fun getLevel(): Int
        
        /**
         * записує повідомлення TRACE
         *
         * @param message повідомлення
         * @param throwable виняток
         */
        fun trace(message: String, throwable: Throwable? = null)
        
        /**
         * записує повідомлення DEBUG
         *
         * @param message повідомлення
         * @param throwable виняток
         */
        fun debug(message: String, throwable: Throwable? = null)
        
        /**
         * записує повідомлення INFO
         *
         * @param message повідомлення
         * @param throwable виняток
         */
        fun info(message: String, throwable: Throwable? = null)
        
        /**
         * записує повідомлення WARN
         *
         * @param message повідомлення
         * @param throwable виняток
         */
        fun warn(message: String, throwable: Throwable? = null)
        
        /**
         * записує повідомлення ERROR
         *
         * @param message повідомлення
         * @param throwable виняток
         */
        fun error(message: String, throwable: Throwable? = null)
        
        /**
         * записує повідомлення FATAL
         *
         * @param message повідомлення
         * @param throwable виняток
         */
        fun fatal(message: String, throwable: Throwable? = null)
        
        /**
         * перевіряє, чи ввімкнено логування TRACE
         *
         * @return true якщо логування ввімкнено
         */
        fun isTraceEnabled(): Boolean
        
        /**
         * перевіряє, чи ввімкнено логування DEBUG
         *
         * @return true якщо логування ввімкнено
         */
        fun isDebugEnabled(): Boolean
        
        /**
         * перевіряє, чи ввімкнено логування INFO
         *
         * @return true якщо логування ввімкнено
         */
        fun isInfoEnabled(): Boolean
        
        /**
         * перевіряє, чи ввімкнено логування WARN
         *
         * @return true якщо логування ввімкнено
         */
        fun isWarnEnabled(): Boolean
        
        /**
         * перевіряє, чи ввімкнено логування ERROR
         *
         * @return true якщо логування ввімкнено
         */
        fun isErrorEnabled(): Boolean
        
        /**
         * перевіряє, чи ввімкнено логування FATAL
         *
         * @return true якщо логування ввімкнено
         */
        fun isFatalEnabled(): Boolean
    }
    
    /**
     * базова реалізація логера
     */
    open class BaseLogger(protected val name: String) : Logger {
        protected var level = LEVEL_INFO
        protected val appenders = mutableListOf<Appender>()
        
        override fun getName(): String {
            return name
        }
        
        override fun setLevel(level: Int) {
            this.level = level
        }
        
        override fun getLevel(): Int {
            return level
        }
        
        override fun trace(message: String, throwable: Throwable?) {
            if (isTraceEnabled()) {
                log(LEVEL_TRACE, message, throwable)
            }
        }
        
        override fun debug(message: String, throwable: Throwable?) {
            if (isDebugEnabled()) {
                log(LEVEL_DEBUG, message, throwable)
            }
        }
        
        override fun info(message: String, throwable: Throwable?) {
            if (isInfoEnabled()) {
                log(LEVEL_INFO, message, throwable)
            }
        }
        
        override fun warn(message: String, throwable: Throwable?) {
            if (isWarnEnabled()) {
                log(LEVEL_WARN, message, throwable)
            }
        }
        
        override fun error(message: String, throwable: Throwable?) {
            if (isErrorEnabled()) {
                log(LEVEL_ERROR, message, throwable)
            }
        }
        
        override fun fatal(message: String, throwable: Throwable?) {
            if (isFatalEnabled()) {
                log(LEVEL_FATAL, message, throwable)
            }
        }
        
        override fun isTraceEnabled(): Boolean {
            return level <= LEVEL_TRACE
        }
        
        override fun isDebugEnabled(): Boolean {
            return level <= LEVEL_DEBUG
        }
        
        override fun isInfoEnabled(): Boolean {
            return level <= LEVEL_INFO
        }
        
        override fun isWarnEnabled(): Boolean {
            return level <= LEVEL_WARN
        }
        
        override fun isErrorEnabled(): Boolean {
            return level <= LEVEL_ERROR
        }
        
        override fun isFatalEnabled(): Boolean {
            return level <= LEVEL_FATAL
        }
        
        /**
         * додає апендер
         *
         * @param appender апендер
         */
        fun addAppender(appender: Appender) {
            appenders.add(appender)
        }
        
        /**
         * видаляє апендер
         *
         * @param appender апендер
         */
        fun removeAppender(appender: Appender) {
            appenders.remove(appender)
        }
        
        /**
         * очищує апендери
         */
        fun clearAppenders() {
            appenders.clear()
        }
        
        /**
         * записує повідомлення з вказаним рівнем
         *
         * @param level рівень
         * @param message повідомлення
         * @param throwable виняток
         */
        protected fun log(level: Int, message: String, throwable: Throwable?) {
            val logEvent = LogEvent(
                timestamp = System.currentTimeMillis(),
                level = level,
                loggerName = name,
                threadName = Thread.currentThread().name,
                message = message,
                throwable = throwable
            )
            
            appenders.forEach { appender ->
                try {
                    appender.append(logEvent)
                } catch (e: Exception) {
                    // ігноруємо помилки апендерів
                }
            }
        }
        
        /**
         * отримує назву рівня логування
         *
         * @param level рівень
         * @return назва рівня
         */
        protected fun getLevelName(level: Int): String {
            return when (level) {
                LEVEL_TRACE -> "TRACE"
                LEVEL_DEBUG -> "DEBUG"
                LEVEL_INFO -> "INFO"
                LEVEL_WARN -> "WARN"
                LEVEL_ERROR -> "ERROR"
                LEVEL_FATAL -> "FATAL"
                else -> "UNKNOWN"
            }
        }
    }
    
    /**
     * представлення події логування
     *
     * @property timestamp час події
     * @property level рівень логування
     * @property loggerName ім'я логера
     * @property threadName ім'я потоку
     * @property message повідомлення
     * @property throwable виняток
     */
    data class LogEvent(
        val timestamp: Long,
        val level: Int,
        val loggerName: String,
        val threadName: String,
        val message: String,
        val throwable: Throwable?
    )
    
    /**
     * представлення апендера
     */
    interface Appender {
        /**
         * додає подію логування
         *
         * @param event подія логування
         */
        fun append(event: LogEvent)
        
        /**
         * закриває апендер
         */
        fun close()
    }
    
    /**
     * базова реалізація апендера
     */
    abstract class BaseAppender : Appender {
        protected var closed = false
        
        override fun close() {
            closed = true
        }
        
        /**
         * перевіряє, чи апендер закрито
         *
         * @return true якщо апендер закрито
         */
        protected fun isClosed(): Boolean {
            return closed
        }
    }
    
    /**
     * представлення форматувальника
     */
    interface Formatter {
        /**
         * форматує подію логування
         *
         * @param event подія логування
         * @return форматований рядок
         */
        fun format(event: LogEvent): String
    }
    
    /**
     * базова реалізація форматувальника
     */
    open class BaseFormatter(
        private val pattern: String = DEFAULT_LOG_FORMAT,
        private val dateFormat: String = DEFAULT_DATE_FORMAT
    ) : Formatter {
        
        override fun format(event: LogEvent): String {
            val timestamp = LocalDateTime.ofEpochSecond(
                event.timestamp / 1000,
                ((event.timestamp % 1000) * 1000000).toInt(),
                java.time.ZoneOffset.UTC
            ).format(DateTimeFormatter.ofPattern(dateFormat))
            
            val levelName = when (event.level) {
                LEVEL_TRACE -> "TRACE"
                LEVEL_DEBUG -> "DEBUG"
                LEVEL_INFO -> "INFO"
                LEVEL_WARN -> "WARN"
                LEVEL_ERROR -> "ERROR"
                LEVEL_FATAL -> "FATAL"
                else -> "UNKNOWN"
            }
            
            var formatted = pattern
                .replace("%timestamp%", timestamp)
                .replace("%level%", levelName)
                .replace("%logger%", event.loggerName)
                .replace("%thread%", event.threadName)
                .replace("%message%", event.message)
            
            if (event.throwable != null) {
                formatted += "\n" + getStackTraceAsString(event.throwable)
            }
            
            return formatted
        }
        
        /**
         * отримує стек-трейс як рядок
         *
         * @param throwable виняток
         * @return стек-трейс як рядок
         */
        protected fun getStackTraceAsString(throwable: Throwable): String {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            return sw.toString()
        }
    }
    
    /**
     * представлення консольного апендера
     */
    class ConsoleAppender(
        private val formatter: Formatter = BaseFormatter(),
        private val errorStream: Boolean = false
    ) : BaseAppender() {
        private val printStream = if (errorStream) System.err else System.out
        
        override fun append(event: LogEvent) {
            if (isClosed()) return
            
            try {
                printStream.println(formatter.format(event))
                printStream.flush()
            } catch (e: Exception) {
                // ігноруємо помилки запису в консоль
            }
        }
    }
    
    /**
     * представлення файлового апендера
     */
    class FileAppender(
        private val fileName: String,
        private val formatter: Formatter = BaseFormatter(DEFAULT_FILE_FORMAT),
        private val append: Boolean = true,
        private val bufferSize: Int = DEFAULT_BUFFER_SIZE
    ) : BaseAppender() {
        private var fileWriter: BufferedWriter? = null
        private val buffer = mutableListOf<String>()
        private val bufferLock = Any()
        
        init {
            openFile()
        }
        
        /**
         * відкриває файл
         */
        private fun openFile() {
            try {
                val file = File(fileName)
                file.parentFile?.mkdirs()
                fileWriter = BufferedWriter(FileWriter(file, append), bufferSize)
            } catch (e: IOException) {
                // ігноруємо помилки відкриття файлу
            }
        }
        
        override fun append(event: LogEvent) {
            if (isClosed()) return
            
            try {
                val formattedMessage = formatter.format(event)
                synchronized(bufferLock) {
                    buffer.add(formattedMessage)
                    if (buffer.size >= 100) { // записуємо буфер кожні 100 повідомлень
                        flushBuffer()
                    }
                }
            } catch (e: Exception) {
                // ігноруємо помилки
            }
        }
        
        /**
         * очищує буфер
         */
        private fun flushBuffer() {
            if (fileWriter == null) return
            
            try {
                synchronized(bufferLock) {
                    buffer.forEach { message ->
                        fileWriter?.write(message)
                        fileWriter?.newLine()
                    }
                    fileWriter?.flush()
                    buffer.clear()
                }
            } catch (e: IOException) {
                // ігноруємо помилки запису в файл
            }
        }
        
        override fun close() {
            if (isClosed()) return
            
            try {
                flushBuffer()
                fileWriter?.close()
            } catch (e: IOException) {
                // ігноруємо помилки закриття файлу
            } finally {
                super.close()
            }
        }
    }
    
    /**
     * представлення ротаційного файлового апендера
     */
    class RollingFileAppender(
        private val fileName: String,
        private val formatter: Formatter = BaseFormatter(DEFAULT_FILE_FORMAT),
        private val maxFileSize: Long = DEFAULT_MAX_FILE_SIZE,
        private val maxBackupFiles: Int = DEFAULT_MAX_BACKUP_FILES,
        private val bufferSize: Int = DEFAULT_BUFFER_SIZE
    ) : BaseAppender() {
        private var fileWriter: BufferedWriter? = null
        private var currentFileSize = 0L
        private val buffer = mutableListOf<String>()
        private val bufferLock = Any()
        
        init {
            openFile()
        }
        
        /**
         * відкриває файл
         */
        private fun openFile() {
            try {
                val file = File(fileName)
                file.parentFile?.mkdirs()
                currentFileSize = file.length()
                fileWriter = BufferedWriter(FileWriter(file, true), bufferSize)
            } catch (e: IOException) {
                // ігноруємо помилки відкриття файлу
            }
        }
        
        override fun append(event: LogEvent) {
            if (isClosed()) return
            
            try {
                val formattedMessage = formatter.format(event)
                val messageSize = formattedMessage.toByteArray().size.toLong()
                
                synchronized(bufferLock) {
                    // перевіряємо, чи потрібно ротувати файл
                    if (currentFileSize + messageSize > maxFileSize) {
                        rotateFile()
                    }
                    
                    buffer.add(formattedMessage)
                    currentFileSize += messageSize
                    
                    if (buffer.size >= 100) { // записуємо буфер кожні 100 повідомлень
                        flushBuffer()
                    }
                }
            } catch (e: Exception) {
                // ігноруємо помилки
            }
        }
        
        /**
         * ротує файл
         */
        private fun rotateFile() {
            try {
                flushBuffer()
                fileWriter?.close()
                
                // переміщуємо існуючі файли
                for (i in maxBackupFiles downTo 1) {
                    val oldFile = File("$fileName.$i")
                    if (oldFile.exists()) {
                        if (i == maxBackupFiles) {
                            oldFile.delete()
                        } else {
                            oldFile.renameTo(File("$fileName.${i + 1}"))
                        }
                    }
                }
                
                // переміщуємо основний файл
                val mainFile = File(fileName)
                if (mainFile.exists()) {
                    mainFile.renameTo(File("$fileName.1"))
                }
                
                // відкриваємо новий файл
                openFile()
            } catch (e: Exception) {
                // ігноруємо помилки ротації
            }
        }
        
        /**
         * очищує буфер
         */
        private fun flushBuffer() {
            if (fileWriter == null) return
            
            try {
                synchronized(bufferLock) {
                    buffer.forEach { message ->
                        fileWriter?.write(message)
                        fileWriter?.newLine()
                    }
                    fileWriter?.flush()
                    buffer.clear()
                }
            } catch (e: IOException) {
                // ігноруємо помилки запису в файл
            }
        }
        
        override fun close() {
            if (isClosed()) return
            
            try {
                flushBuffer()
                fileWriter?.close()
            } catch (e: IOException) {
                // ігноруємо помилки закриття файлу
            } finally {
                super.close()
            }
        }
    }
    
    /**
     * представлення асинхронного апендера
     */
    class AsyncAppender(
        private val delegate: Appender,
        private val queueSize: Int = 1000
    ) : BaseAppender() {
        private val eventQueue = ArrayBlockingQueue<LogEvent>(queueSize)
        private val executorService = Executors.newSingleThreadExecutor()
        private val flushInterval = DEFAULT_FLUSH_INTERVAL
        
        init {
            startProcessingThread()
        }
        
        /**
         * запускає потік обробки подій
         */
        private fun startProcessingThread() {
            executorService.submit {
                try {
                    while (!isClosed() || eventQueue.isNotEmpty()) {
                        val event = eventQueue.poll(flushInterval, TimeUnit.MILLISECONDS)
                        if (event != null) {
                            delegate.append(event)
                        }
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                } catch (e: Exception) {
                    // ігноруємо помилки
                } finally {
                    // обробляємо залишкові події
                    while (eventQueue.isNotEmpty()) {
                        val event = eventQueue.poll()
                        if (event != null) {
                            try {
                                delegate.append(event)
                            } catch (e: Exception) {
                                // ігноруємо помилки
                            }
                        }
                    }
                }
            }
        }
        
        override fun append(event: LogEvent) {
            if (isClosed()) return
            
            // якщо черга повна, викидаємо старі події
            if (!eventQueue.offer(event)) {
                eventQueue.poll() // видаляємо найстарішу подію
                eventQueue.offer(event) // додаємо нову подію
            }
        }
        
        override fun close() {
            if (isClosed()) return
            
            try {
                super.close()
                executorService.shutdown()
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow()
                }
                delegate.close()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                // ігноруємо помилки закриття
            }
        }
    }
    
    /**
     * представлення фільтру
     */
    interface Filter {
        /**
         * перевіряє, чи подія відповідає критеріям фільтру
         *
         * @param event подія логування
         * @return true якщо подія відповідає критеріям
         */
        fun accept(event: LogEvent): Boolean
    }
    
    /**
     * представлення фільтру за рівнем
     */
    class LevelFilter(private val minLevel: Int, private val maxLevel: Int) : Filter {
        override fun accept(event: LogEvent): Boolean {
            return event.level >= minLevel && event.level <= maxLevel
        }
    }
    
    /**
     * представлення фільтру за ім'ям логера
     */
    class LoggerNameFilter(private val loggerName: String, private val exactMatch: Boolean = true) : Filter {
        override fun accept(event: LogEvent): Boolean {
            return if (exactMatch) {
                event.loggerName == loggerName
            } else {
                event.loggerName.startsWith(loggerName)
            }
        }
    }
    
    /**
     * представлення фільтру за регулярним виразом
     */
    class RegexFilter(private val regex: String) : Filter {
        private val pattern = Regex(regex)
        
        override fun accept(event: LogEvent): Boolean {
            return pattern.containsMatchIn(event.message)
        }
    }
    
    /**
     * представлення апендера з фільтрацією
     */
    class FilteredAppender(
        private val delegate: Appender,
        private val filter: Filter
    ) : BaseAppender() {
        
        override fun append(event: LogEvent) {
            if (isClosed()) return
            
            if (filter.accept(event)) {
                delegate.append(event)
            }
        }
        
        override fun close() {
            if (isClosed()) return
            
            try {
                super.close()
                delegate.close()
            } catch (e: Exception) {
                // ігноруємо помилки закриття
            }
        }
    }
    
    /**
     * представлення менеджера логерів
     */
    class LogManager {
        private val loggers = mutableMapOf<String, Logger>()
        private val loggerLock = Any()
        private var defaultLevel = LEVEL_INFO
        
        /**
         * отримує логер за ім'ям
         *
         * @param name ім'я логера
         * @return логер
         */
        fun getLogger(name: String): Logger {
            synchronized(loggerLock) {
                return loggers.getOrPut(name) {
                    val logger = BaseLogger(name)
                    logger.setLevel(defaultLevel)
                    logger
                }
            }
        }
        
        /**
         * встановлює рівень логування за замовчуванням
         *
         * @param level рівень логування
         */
        fun setDefaultLevel(level: Int) {
            synchronized(loggerLock) {
                defaultLevel = level
                loggers.values.forEach { logger ->
                    logger.setLevel(level)
                }
            }
        }
        
        /**
         * отримує рівень логування за замовчуванням
         *
         * @return рівень логування
         */
        fun getDefaultLevel(): Int {
            synchronized(loggerLock) {
                return defaultLevel
            }
        }
        
        /**
         * додає апендер до всіх логерів
         *
         * @param appender апендер
         */
        fun addAppenderToAllLoggers(appender: Appender) {
            synchronized(loggerLock) {
                loggers.values.forEach { logger ->
                    if (logger is BaseLogger) {
                        logger.addAppender(appender)
                    }
                }
            }
        }
        
        /**
         * видаляє апендер з усіх логерів
         *
         * @param appender апендер
         */
        fun removeAppenderFromAllLoggers(appender: Appender) {
            synchronized(loggerLock) {
                loggers.values.forEach { logger ->
                    if (logger is BaseLogger) {
                        logger.removeAppender(appender)
                    }
                }
            }
        }
        
        /**
         * закриває всі логери
         */
        fun closeAllLoggers() {
            synchronized(loggerLock) {
                loggers.values.forEach { logger ->
                    if (logger is BaseLogger) {
                        logger.clearAppenders()
                    }
                }
                loggers.clear()
            }
        }
    }
    
    /**
     * створює менеджер логерів
     *
     * @return менеджер логерів
     */
    fun createLogManager(): LogManager {
        return LogManager()
    }
    
    // функції для роботи з різними форматами логів
    
    /**
     * представлення форматувальника JSON
     */
    class JsonFormatter : Formatter {
        override fun format(event: LogEvent): String {
            val timestamp = LocalDateTime.ofEpochSecond(
                event.timestamp / 1000,
                ((event.timestamp % 1000) * 1000000).toInt(),
                java.time.ZoneOffset.UTC
            )
            
            val sb = StringBuilder()
            sb.append("{")
            sb.append("\"timestamp\":\"").append(timestamp).append("\",")
            sb.append("\"level\":\"").append(getLevelName(event.level)).append("\",")
            sb.append("\"logger\":\"").append(event.loggerName).append("\",")
            sb.append("\"thread\":\"").append(event.threadName).append("\",")
            sb.append("\"message\":\"").append(escapeJsonString(event.message)).append("\"")
            
            if (event.throwable != null) {
                sb.append(",\"throwable\":\"").append(escapeJsonString(getStackTraceAsString(event.throwable))).append("\"")
            }
            
            sb.append("}")
            return sb.toString()
        }
        
        /**
         * отримує назву рівня логування
         *
         * @param level рівень
         * @return назва рівня
         */
        private fun getLevelName(level: Int): String {
            return when (level) {
                LEVEL_TRACE -> "TRACE"
                LEVEL_DEBUG -> "DEBUG"
                LEVEL_INFO -> "INFO"
                LEVEL_WARN -> "WARN"
                LEVEL_ERROR -> "ERROR"
                LEVEL_FATAL -> "FATAL"
                else -> "UNKNOWN"
            }
        }
        
        /**
         * отримує стек-трейс як рядок
         *
         * @param throwable виняток
         * @return стек-трейс як рядок
         */
        private fun getStackTraceAsString(throwable: Throwable): String {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            return sw.toString()
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
     * представлення форматувальника XML
     */
    class XmlFormatter : Formatter {
        override fun format(event: LogEvent): String {
            val timestamp = LocalDateTime.ofEpochSecond(
                event.timestamp / 1000,
                ((event.timestamp % 1000) * 1000000).toInt(),
                java.time.ZoneOffset.UTC
            )
            
            val sb = StringBuilder()
            sb.append("<log>")
            sb.append("<timestamp>").append(timestamp).append("</timestamp>")
            sb.append("<level>").append(getLevelName(event.level)).append("</level>")
            sb.append("<logger>").append(escapeXmlString(event.loggerName)).append("</logger>")
            sb.append("<thread>").append(escapeXmlString(event.threadName)).append("</thread>")
            sb.append("<message>").append(escapeXmlString(event.message)).append("</message>")
            
            if (event.throwable != null) {
                sb.append("<throwable>").append(escapeXmlString(getStackTraceAsString(event.throwable))).append("</throwable>")
            }
            
            sb.append("</log>")
            return sb.toString()
        }
        
        /**
         * отримує назву рівня логування
         *
         * @param level рівень
         * @return назва рівня
         */
        private fun getLevelName(level: Int): String {
            return when (level) {
                LEVEL_TRACE -> "TRACE"
                LEVEL_DEBUG -> "DEBUG"
                LEVEL_INFO -> "INFO"
                LEVEL_WARN -> "WARN"
                LEVEL_ERROR -> "ERROR"
                LEVEL_FATAL -> "FATAL"
                else -> "UNKNOWN"
            }
        }
        
        /**
         * отримує стек-трейс як рядок
         *
         * @param throwable виняток
         * @return стек-трейс як рядок
         */
        private fun getStackTraceAsString(throwable: Throwable): String {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            return sw.toString()
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
     * представлення кольорового форматувальника
     */
    class ColoredFormatter(
        private val pattern: String = DEFAULT_LOG_FORMAT,
        private val dateFormat: String = DEFAULT_DATE_FORMAT
    ) : Formatter {
        
        override fun format(event: LogEvent): String {
            val timestamp = LocalDateTime.ofEpochSecond(
                event.timestamp / 1000,
                ((event.timestamp % 1000) * 1000000).toInt(),
                java.time.ZoneOffset.UTC
            ).format(DateTimeFormatter.ofPattern(dateFormat))
            
            val levelColor = getLevelColor(event.level)
            val levelName = getLevelName(event.level)
            
            var formatted = pattern
                .replace("%timestamp%", "${getColorCode("gray")}$timestamp${getColorCode("reset")}")
                .replace("%level%", "${levelColor}$levelName${getColorCode("reset")}")
                .replace("%logger%", "${getColorCode("blue")}${event.loggerName}${getColorCode("reset")}")
                .replace("%thread%", "${getColorCode("magenta")}${event.threadName}${getColorCode("reset")}")
                .replace("%message%", event.message)
            
            if (event.throwable != null) {
                formatted += "\n${getColorCode("red")}${getStackTraceAsString(event.throwable)}${getColorCode("reset")}"
            }
            
            return formatted
        }
        
        /**
         * отримує код кольору
         *
         * @param color назва кольору
         * @return код кольору
         */
        private fun getColorCode(color: String): String {
            return when (color.lowercase()) {
                "reset" -> "\u001B[0m"
                "black" -> "\u001B[30m"
                "red" -> "\u001B[31m"
                "green" -> "\u001B[32m"
                "yellow" -> "\u001B[33m"
                "blue" -> "\u001B[34m"
                "magenta" -> "\u001B[35m"
                "cyan" -> "\u001B[36m"
                "white" -> "\u001B[37m"
                "gray" -> "\u001B[90m"
                else -> "\u001B[0m"
            }
        }
        
        /**
         * отримує колір для рівня логування
         *
         * @param level рівень
         * @return код кольору
         */
        private fun getLevelColor(level: Int): String {
            return when (level) {
                LEVEL_TRACE -> getColorCode("gray")
                LEVEL_DEBUG -> getColorCode("cyan")
                LEVEL_INFO -> getColorCode("green")
                LEVEL_WARN -> getColorCode("yellow")
                LEVEL_ERROR -> getColorCode("red")
                LEVEL_FATAL -> getColorCode("magenta")
                else -> getColorCode("white")
            }
        }
        
        /**
         * отримує назву рівня логування
         *
         * @param level рівень
         * @return назва рівня
         */
        private fun getLevelName(level: Int): String {
            return when (level) {
                LEVEL_TRACE -> "TRACE"
                LEVEL_DEBUG -> "DEBUG"
                LEVEL_INFO -> "INFO"
                LEVEL_WARN -> "WARN"
                LEVEL_ERROR -> "ERROR"
                LEVEL_FATAL -> "FATAL"
                else -> "UNKNOWN"
            }
        }
        
        /**
         * отримує стек-трейс як рядок
         *
         * @param throwable виняток
         * @return стек-трейс як рядок
         */
        private fun getStackTraceAsString(throwable: Throwable): String {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            return sw.toString()
        }
    }
    
    // функції для роботи з мережевим логуванням
    
    /**
     * представлення мережевого апендера
     */
    class NetworkAppender(
        private val host: String,
        private val port: Int,
        private val formatter: Formatter = BaseFormatter(),
        private val connectionTimeout: Int = 5000
    ) : BaseAppender() {
        private var socket: java.net.Socket? = null
        private var printWriter: PrintWriter? = null
        private val socketLock = Any()
        
        init {
            connect()
        }
        
        /**
         * підключається до сервера
         */
        private fun connect() {
            synchronized(socketLock) {
                try {
                    socket = java.net.Socket()
                    socket?.connect(java.net.InetSocketAddress(host, port), connectionTimeout)
                    printWriter = PrintWriter(socket?.getOutputStream(), true)
                } catch (e: Exception) {
                    // ігноруємо помилки підключення
                }
            }
        }
        
        override fun append(event: LogEvent) {
            if (isClosed()) return
            
            synchronized(socketLock) {
                try {
                    if (socket?.isConnected != true || socket?.isClosed == true) {
                        connect()
                    }
                    
                    printWriter?.println(formatter.format(event))
                    printWriter?.flush()
                } catch (e: Exception) {
                    // ігноруємо помилки запису в мережу
                }
            }
        }
        
        override fun close() {
            if (isClosed()) return
            
            try {
                synchronized(socketLock) {
                    printWriter?.close()
                    socket?.close()
                }
            } catch (e: Exception) {
                // ігноруємо помилки закриття
            } finally {
                super.close()
            }
        }
    }
    
    /**
     * представлення HTTP апендера
     */
    class HttpAppender(
        private val url: String,
        private val formatter: Formatter = JsonFormatter(),
        private val connectTimeout: Int = 5000,
        private val readTimeout: Int = 10000
    ) : BaseAppender() {
        private val httpClient = java.net.http.HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofMillis(connectTimeout.toLong()))
            .build()
        
        override fun append(event: LogEvent) {
            if (isClosed()) return
            
            try {
                val requestBody = formatter.format(event)
                val request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                    .build()
                
                httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
            } catch (e: Exception) {
                // ігноруємо помилки HTTP запиту
            }
        }
    }
    
    // функції для роботи з логуванням у базу даних
    
    /**
     * представлення бази даних апендера
     */
    class DatabaseAppender(
        private val connectionPool: DatabaseUtils.ConnectionPool,
        private val tableName: String = "logs",
        private val bufferSize: Int = 100
    ) : BaseAppender() {
        private val buffer = mutableListOf<LogEvent>()
        private val bufferLock = Any()
        private val executorService = Executors.newSingleThreadExecutor()
        
        init {
            // створюємо таблицю, якщо її ще немає
            createTable()
            // запускаємо потік для запису логів у базу даних
            startFlushThread()
        }
        
        /**
         * створює таблицю для логів
         */
        private fun createTable() {
            val connection = connectionPool.getConnection()
            try {
                val sql = """
                    CREATE TABLE IF NOT EXISTS $tableName (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        timestamp TIMESTAMP,
                        level VARCHAR(10),
                        logger VARCHAR(255),
                        thread VARCHAR(255),
                        message TEXT,
                        throwable TEXT
                    )
                """.trimIndent()
                
                val statement = connection.getConnection().createStatement()
                statement.execute(sql)
                statement.close()
            } catch (e: Exception) {
                // ігноруємо помилки створення таблиці
            } finally {
                connectionPool.releaseConnection(connection)
            }
        }
        
        /**
         * запускає потік для запису логів у базу даних
         */
        private fun startFlushThread() {
            executorService.submit {
                try {
                    while (!isClosed()) {
                        Thread.sleep(5000) // записуємо кожні 5 секунд
                        flushBuffer()
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                } catch (e: Exception) {
                    // ігноруємо помилки
                } finally {
                    // обробляємо залишкові події
                    flushBuffer()
                }
            }
        }
        
        override fun append(event: LogEvent) {
            if (isClosed()) return
            
            synchronized(bufferLock) {
                buffer.add(event)
                if (buffer.size >= bufferSize) {
                    // створюємо копію буфера і очищуємо оригінал
                    val eventsToFlush = buffer.toList()
                    buffer.clear()
                    // записуємо події в окремому потоці
                    executorService.submit {
                        flushEvents(eventsToFlush)
                    }
                }
            }
        }
        
        /**
         * очищує буфер
         */
        private fun flushBuffer() {
            synchronized(bufferLock) {
                if (buffer.isNotEmpty()) {
                    val eventsToFlush = buffer.toList()
                    buffer.clear()
                    // записуємо події в окремому потоці
                    executorService.submit {
                        flushEvents(eventsToFlush)
                    }
                }
            }
        }
        
        /**
         * записує події в базу даних
         *
         * @param events події
         */
        private fun flushEvents(events: List<LogEvent>) {
            if (events.isEmpty()) return
            
            val connection = connectionPool.getConnection()
            try {
                val sql = """
                    INSERT INTO $tableName (timestamp, level, logger, thread, message, throwable) 
                    VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent()
                
                val preparedStatement = connection.getConnection().prepareStatement(sql)
                
                events.forEach { event ->
                    preparedStatement.setTimestamp(1, java.sql.Timestamp(event.timestamp))
                    preparedStatement.setString(2, getLevelName(event.level))
                    preparedStatement.setString(3, event.loggerName)
                    preparedStatement.setString(4, event.threadName)
                    preparedStatement.setString(5, event.message)
                    preparedStatement.setString(6, event.throwable?.let { getStackTraceAsString(it) } ?: "")
                    preparedStatement.addBatch()
                }
                
                preparedStatement.executeBatch()
                preparedStatement.close()
            } catch (e: Exception) {
                // ігноруємо помилки запису в базу даних
            } finally {
                connectionPool.releaseConnection(connection)
            }
        }
        
        /**
         * отримує назву рівня логування
         *
         * @param level рівень
         * @return назва рівня
         */
        private fun getLevelName(level: Int): String {
            return when (level) {
                LEVEL_TRACE -> "TRACE"
                LEVEL_DEBUG -> "DEBUG"
                LEVEL_INFO -> "INFO"
                LEVEL_WARN -> "WARN"
                LEVEL_ERROR -> "ERROR"
                LEVEL_FATAL -> "FATAL"
                else -> "UNKNOWN"
            }
        }
        
        /**
         * отримує стек-трейс як рядок
         *
         * @param throwable виняток
         * @return стек-трейс як рядок
         */
        private fun getStackTraceAsString(throwable: Throwable): String {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            return sw.toString()
        }
        
        override fun close() {
            if (isClosed()) return
            
            try {
                super.close()
                flushBuffer()
                executorService.shutdown()
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow()
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                // ігноруємо помилки закриття
            }
        }
    }
    
    // функції для роботи з логуванням у хмару
    
    /**
     * представлення хмарного апендера
     */
    class CloudAppender(
        private val apiKey: String,
        private val endpoint: String,
        private val formatter: Formatter = JsonFormatter()
    ) : BaseAppender() {
        private val httpClient = java.net.http.HttpClient.newHttpClient()
        
        override fun append(event: LogEvent) {
            if (isClosed()) return
            
            try {
                val requestBody = formatter.format(event)
                val request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer $apiKey")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                    .build()
                
                httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
            } catch (e: Exception) {
                // ігноруємо помилки хмарного запиту
            }
        }
    }
    
    // функції для роботи з логуванням з підтримкою контексту
    
    /**
     * представлення контексту логування
     */
    class LoggingContext {
        private val contextMap = mutableMapOf<String, String>()
        private val contextLock = Any()
        
        /**
         * додає значення до контексту
         *
         * @param key ключ
         * @param value значення
         */
        fun put(key: String, value: String) {
            synchronized(contextLock) {
                contextMap[key] = value
            }
        }
        
        /**
         * отримує значення з контексту
         *
         * @param key ключ
         * @return значення або null якщо не знайдено
         */
        fun get(key: String): String? {
            synchronized(contextLock) {
                return contextMap[key]
            }
        }
        
        /**
         * видаляє значення з контексту
         *
         * @param key ключ
         */
        fun remove(key: String) {
            synchronized(contextLock) {
                contextMap.remove(key)
            }
        }
        
        /**
         * очищує контекст
         */
        fun clear() {
            synchronized(contextLock) {
                contextMap.clear()
            }
        }
        
        /**
         * отримує всі значення контексту
         *
         * @return мапа значень
         */
        fun getAll(): Map<String, String> {
            synchronized(contextLock) {
                return contextMap.toMap()
            }
        }
    }
    
    /**
     * представлення логера з контекстом
     */
    class ContextualLogger(
        name: String,
        private val context: LoggingContext
    ) : BaseLogger(name) {
        
        override fun trace(message: String, throwable: Throwable?) {
            if (isTraceEnabled()) {
                log(LEVEL_TRACE, enrichMessageWithContext(message), throwable)
            }
        }
        
        override fun debug(message: String, throwable: Throwable?) {
            if (isDebugEnabled()) {
                log(LEVEL_DEBUG, enrichMessageWithContext(message), throwable)
            }
        }
        
        override fun info(message: String, throwable: Throwable?) {
            if (isInfoEnabled()) {
                log(LEVEL_INFO, enrichMessageWithContext(message), throwable)
            }
        }
        
        override fun warn(message: String, throwable: Throwable?) {
            if (isWarnEnabled()) {
                log(LEVEL_WARN, enrichMessageWithContext(message), throwable)
            }
        }
        
        override fun error(message: String, throwable: Throwable?) {
            if (isErrorEnabled()) {
                log(LEVEL_ERROR, enrichMessageWithContext(message), throwable)
            }
        }
        
        override fun fatal(message: String, throwable: Throwable?) {
            if (isFatalEnabled()) {
                log(LEVEL_FATAL, enrichMessageWithContext(message), throwable)
            }
        }
        
        /**
         * збагачує повідомлення контекстом
         *
         * @param message повідомлення
         * @return збагачене повідомлення
         */
        private fun enrichMessageWithContext(message: String): String {
            val contextEntries = context.getAll()
            if (contextEntries.isEmpty()) {
                return message
            }
            
            val contextString = contextEntries.entries.joinToString(", ") { "${it.key}=${it.value}" }
            return "[$contextString] $message"
        }
    }
    
    /**
     * створює логер з контекстом
     *
     * @param name ім'я логера
     * @param context контекст
     * @return логер з контекстом
     */
    fun createContextualLogger(name: String, context: LoggingContext): Logger {
        return ContextualLogger(name, context)
    }
    
    // функції для роботи з логуванням з підтримкою маркерів
    
    /**
     * представлення маркера
     *
     * @property name назва маркера
     */
    data class Marker(val name: String) {
        private val references = mutableSetOf<Marker>()
        
        /**
         * додає посилання на інший маркер
         *
         * @param marker маркер
         */
        fun addReference(marker: Marker) {
            references.add(marker)
        }
        
        /**
         * перевіряє, чи містить маркер посилання
         *
         * @param marker маркер
         * @return true якщо містить посилання
         */
        fun contains(marker: Marker): Boolean {
            if (this == marker) return true
            return references.any { it.contains(marker) }
        }
        
        /**
         * отримує всі посилання
         *
         * @return набір посилань
         */
        fun getReferences(): Set<Marker> {
            return references.toSet()
        }
    }
    
    /**
     * представлення логера з маркерами
     */
    class MarkerLogger(name: String) : BaseLogger(name) {
        
        /**
         * записує повідомлення TRACE з маркером
         *
         * @param marker маркер
         * @param message повідомлення
         * @param throwable виняток
         */
        fun trace(marker: Marker, message: String, throwable: Throwable? = null) {
            if (isTraceEnabled()) {
                log(LEVEL_TRACE, "[$marker] $message", throwable)
            }
        }
        
        /**
         * записує повідомлення DEBUG з маркером
         *
         * @param marker маркер
         * @param message повідомлення
         * @param throwable виняток
         */
        fun debug(marker: Marker, message: String, throwable: Throwable? = null) {
            if (isDebugEnabled()) {
                log(LEVEL_DEBUG, "[$marker] $message", throwable)
            }
        }
        
        /**
         * записує повідомлення INFO з маркером
         *
         * @param marker маркер
         * @param message повідомлення
         * @param throwable виняток
         */
        fun info(marker: Marker, message: String, throwable: Throwable? = null) {
            if (isInfoEnabled()) {
                log(LEVEL_INFO, "[$marker] $message", throwable)
            }
        }
        
        /**
         * записує повідомлення WARN з маркером
         *
         * @param marker маркер
         * @param message повідомлення
         * @param throwable виняток
         */
        fun warn(marker: Marker, message: String, throwable: Throwable? = null) {
            if (isWarnEnabled()) {
                log(LEVEL_WARN, "[$marker] $message", throwable)
            }
        }
        
        /**
         * записує повідомлення ERROR з маркером
         *
         * @param marker маркер
         * @param message повідомлення
         * @param throwable виняток
         */
        fun error(marker: Marker, message: String, throwable: Throwable? = null) {
            if (isErrorEnabled()) {
                log(LEVEL_ERROR, "[$marker] $message", throwable)
            }
        }
        
        /**
         * записує повідомлення FATAL з маркером
         *
         * @param marker маркер
         * @param message повідомлення
         * @param throwable виняток
         */
        fun fatal(marker: Marker, message: String, throwable: Throwable? = null) {
            if (isFatalEnabled()) {
                log(LEVEL_FATAL, "[$marker] $message", throwable)
            }
        }
    }
    
    /**
     * створює логер з маркерами
     *
     * @param name ім'я логера
     * @return логер з маркерами
     */
    fun createMarkerLogger(name: String): MarkerLogger {
        return MarkerLogger(name)
    }
    
    // функції для роботи з логуванням з підтримкою метрик
    
    /**
     * представлення метрик логування
     *
     * @property totalLogs загальна кількість логів
     * @property traceLogs кількість TRACE логів
     * @property debugLogs кількість DEBUG логів
     * @property infoLogs кількість INFO логів
     * @property warnLogs кількість WARN логів
     * @property errorLogs кількість ERROR логів
     * @property fatalLogs кількість FATAL логів
     * @property totalSize загальний розмір логів в байтах
     * @property timestamp час останнього оновлення
     */
    data class LoggingMetrics(
        val totalLogs: Long,
        val traceLogs: Long,
        val debugLogs: Long,
        val infoLogs: Long,
        val warnLogs: Long,
        val errorLogs: Long,
        val fatalLogs: Long,
        val totalSize: Long,
        val timestamp: Long
    )
    
    /**
     * представлення апендера з метриками
     */
    class MetricsAppender(private val delegate: Appender) : BaseAppender() {
        private var totalLogs = 0L
        private var traceLogs = 0L
        private var debugLogs = 0L
        private var infoLogs = 0L
        private var warnLogs = 0L
        private var errorLogs = 0L
        private var fatalLogs = 0L
        private var totalSize = 0L
        private val metricsLock = Any()
        
        override fun append(event: LogEvent) {
            if (isClosed()) return
            
            // оновлюємо метрики
            synchronized(metricsLock) {
                totalLogs++
                when (event.level) {
                    LEVEL_TRACE -> traceLogs++
                    LEVEL_DEBUG -> debugLogs++
                    LEVEL_INFO -> infoLogs++
                    LEVEL_WARN -> warnLogs++
                    LEVEL_ERROR -> errorLogs++
                    LEVEL_FATAL -> fatalLogs++
                }
                totalSize += event.message.toByteArray().size
            }
            
            // передаємо подію делегату
            delegate.append(event)
        }
        
        /**
         * отримує метрики
         *
         * @return метрики
         */
        fun getMetrics(): LoggingMetrics {
            synchronized(metricsLock) {
                return LoggingMetrics(
                    totalLogs = totalLogs,
                    traceLogs = traceLogs,
                    debugLogs = debugLogs,
                    infoLogs = infoLogs,
                    warnLogs = warnLogs,
                    errorLogs = errorLogs,
                    fatalLogs = fatalLogs,
                    totalSize = totalSize,
                    timestamp = System.currentTimeMillis()
                )
            }
        }
        
        /**
         * скидає метрики
         */
        fun resetMetrics() {
            synchronized(metricsLock) {
                totalLogs = 0
                traceLogs = 0
                debugLogs = 0
                infoLogs = 0
                warnLogs = 0
                errorLogs = 0
                fatalLogs = 0
                totalSize = 0
            }
        }
        
        override fun close() {
            if (isClosed()) return
            
            try {
                super.close()
                delegate.close()
            } catch (e: Exception) {
                // ігноруємо помилки закриття
            }
        }
    }
    
    /**
     * створює апендер з метриками
     *
     * @param delegate делегат
     * @return апендер з метриками
     */
    fun createMetricsAppender(delegate: Appender): MetricsAppender {
        return MetricsAppender(delegate)
    }
    
    // функції для роботи з логуванням з підтримкою фільтрації за часом
    
    /**
     * представлення фільтру за часом
     */
    class TimeFilter(
        private val startTime: Long? = null,
        private val endTime: Long? = null
    ) : Filter {
        override fun accept(event: LogEvent): Boolean {
            return (startTime == null || event.timestamp >= startTime) &&
                   (endTime == null || event.timestamp <= endTime)
        }
    }
    
    /**
     * представлення фільтру за тривалістю
     */
    class DurationFilter(
        private val minDuration: Long = 0,
        private val maxDuration: Long = Long.MAX_VALUE
    ) : Filter {
        private val startTime = System.currentTimeMillis()
        
        override fun accept(event: LogEvent): Boolean {
            val duration = System.currentTimeMillis() - startTime
            return duration >= minDuration && duration <= maxDuration
        }
    }
    
    // функції для роботи з логуванням з підтримкою групування
    
    /**
     * представлення групового апендера
     */
    class GroupAppender(private val appenders: List<Appender>) : BaseAppender() {
        
        override fun append(event: LogEvent) {
            if (isClosed()) return
            
            appenders.forEach { appender ->
                try {
                    appender.append(event)
                } catch (e: Exception) {
                    // ігноруємо помилки окремих апендерів
                }
            }
        }
        
        override fun close() {
            if (isClosed()) return
            
            try {
                super.close()
                appenders.forEach { appender ->
                    try {
                        appender.close()
                    } catch (e: Exception) {
                        // ігноруємо помилки закриття
                    }
                }
            } catch (e: Exception) {
                // ігноруємо помилки закриття
            }
        }
    }
    
    /**
     * створює груповий апендер
     *
     * @param appenders список апендерів
     * @return груповий апендер
     */
    fun createGroupAppender(appenders: List<Appender>): GroupAppender {
        return GroupAppender(appenders)
    }
    
    // функції для роботи з логуванням з підтримкою рівнів для різних пакетів
    
    /**
     * представлення конфігурації рівнів для пакетів
     */
    class PackageLevelConfig {
        private val packageLevels = mutableMapOf<String, Int>()
        private val configLock = Any()
        
        /**
         * встановлює рівень для пакета
         *
         * @param packageName ім'я пакета
         * @param level рівень
         */
        fun setLevel(packageName: String, level: Int) {
            synchronized(configLock) {
                packageLevels[packageName] = level
            }
        }
        
        /**
         * отримує рівень для пакета
         *
         * @param packageName ім'я пакета
         * @return рівень або null якщо не встановлено
         */
        fun getLevel(packageName: String): Int? {
            synchronized(configLock) {
                // шукаємо точний збіг
                packageLevels[packageName]?.let { return it }
                
                // шукаємо найбільш специфічний збіг
                val matchingEntries = packageLevels.entries.filter { packageName.startsWith(it.key) }
                if (matchingEntries.isNotEmpty()) {
                    return matchingEntries.maxByOrNull { it.key.length }?.value
                }
                
                return null
            }
        }
        
        /**
         * видаляє рівень для пакета
         *
         * @param packageName ім'я пакета
         */
        fun removeLevel(packageName: String) {
            synchronized(configLock) {
                packageLevels.remove(packageName)
            }
        }
        
        /**
         * очищує конфігурацію
         */
        fun clear() {
            synchronized(configLock) {
                packageLevels.clear()
            }
        }
    }
    
    /**
     * представлення логера з підтримкою рівнів для пакетів
     */
    class PackageLevelLogger(
        name: String,
        private val packageLevelConfig: PackageLevelConfig,
        private val defaultLevel: Int = LEVEL_INFO
    ) : BaseLogger(name) {
        
        override fun getLevel(): Int {
            // отримуємо рівень для пакета
            val packageLevel = packageLevelConfig.getLevel(name)
            return packageLevel ?: defaultLevel
        }
    }
    
    /**
     * створює логер з підтримкою рівнів для пакетів
     *
     * @param name ім'я логера
     * @param packageLevelConfig конфігурація рівнів для пакетів
     * @param defaultLevel рівень за замовчуванням
     * @return логер з підтримкою рівнів для пакетів
     */
    fun createPackageLevelLogger(
        name: String,
        packageLevelConfig: PackageLevelConfig,
        defaultLevel: Int = LEVEL_INFO
    ): PackageLevelLogger {
        return PackageLevelLogger(name, packageLevelConfig, defaultLevel)
    }
    
    // функції для роботи з логуванням з підтримкою автоматичного вимкнення
    
    /**
     * представлення логера з автоматичним вимкненням
     */
    class AutoDisableLogger(
        name: String,
        private val disableAfterErrors: Int = 100
    ) : BaseLogger(name) {
        private var errorCount = 0
        private var disabled = false
        private val disableLock = Any()
        
        override fun error(message: String, throwable: Throwable?) {
            if (isErrorEnabled()) {
                synchronized(disableLock) {
                    errorCount++
                    if (errorCount >= disableAfterErrors) {
                        disabled = true
                        warn("Логер автоматично вимкнено після $disableAfterErrors помилок")
                    }
                }
                
                if (!disabled) {
                    log(LEVEL_ERROR, message, throwable)
                }
            }
        }
        
        override fun isErrorEnabled(): Boolean {
            return super.isErrorEnabled() && !disabled
        }
        
        /**
         * скидає лічильник помилок
         */
        fun resetErrorCount() {
            synchronized(disableLock) {
                errorCount = 0
                disabled = false
            }
        }
        
        /**
         * перевіряє, чи логер вимкнено
         *
         * @return true якщо логер вимкнено
         */
        fun isDisabled(): Boolean {
            synchronized(disableLock) {
                return disabled
            }
        }
    }
    
    /**
     * створює логер з автоматичним вимкненням
     *
     * @param name ім'я логера
     * @param disableAfterErrors кількість помилок для автоматичного вимкнення
     * @return логер з автоматичним вимкненням
     */
    fun createAutoDisableLogger(name: String, disableAfterErrors: Int = 100): AutoDisableLogger {
        return AutoDisableLogger(name, disableAfterErrors)
    }
    
    // функції для роботи з логуванням з підтримкою буферизації
    
    /**
     * представлення буферизованого апендера
     */
    class BufferedAppender(
        private val delegate: Appender,
        private val bufferSize: Int = 100,
        private val flushInterval: Long = 5000
    ) : BaseAppender() {
        private val buffer = mutableListOf<LogEvent>()
        private val bufferLock = Any()
        private val executorService = Executors.newSingleThreadScheduledExecutor()
        
        init {
            // запускаємо періодичне очищення буфера
            executorService.scheduleWithFixedDelay(
                { flushBuffer() },
                flushInterval,
                flushInterval,
                TimeUnit.MILLISECONDS
            )
        }
        
        override fun append(event: LogEvent) {
            if (isClosed()) return
            
            synchronized(bufferLock) {
                buffer.add(event)
                if (buffer.size >= bufferSize) {
                    flushBuffer()
                }
            }
        }
        
        /**
         * очищує буфер
         */
        private fun flushBuffer() {
            synchronized(bufferLock) {
                if (buffer.isNotEmpty()) {
                    val eventsToFlush = buffer.toList()
                    buffer.clear()
                    
                    // записуємо події в окремому потоці
                    executorService.submit {
                        eventsToFlush.forEach { event ->
                            try {
                                delegate.append(event)
                            } catch (e: Exception) {
                                // ігноруємо помилки делегата
                            }
                        }
                    }
                }
            }
        }
        
        override fun close() {
            if (isClosed()) return
            
            try {
                flushBuffer()
                super.close()
                delegate.close()
                executorService.shutdown()
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow()
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                // ігноруємо помилки закриття
            }
        }
    }
    
    /**
     * створює буферизований апендер
     *
     * @param delegate делегат
     * @param bufferSize розмір буфера
     * @param flushInterval інтервал очищення буфера в мілісекундах
     * @return буферизований апендер
     */
    fun createBufferedAppender(
        delegate: Appender,
        bufferSize: Int = 100,
        flushInterval: Long = 5000
    ): BufferedAppender {
        return BufferedAppender(delegate, bufferSize, flushInterval)
    }
    
    // функції для роботи з логуванням з підтримкою компресії
    
    /**
     * представлення апендера з компресією
     */
    class CompressedAppender(
        private val delegate: Appender,
        private val compressionThreshold: Int = 1024
    ) : BaseAppender() {
        
        override fun append(event: LogEvent) {
            if (isClosed()) return
            
            try {
                val compressedEvent = if (event.message.toByteArray().size > compressionThreshold) {
                    event.copy(message = compressString(event.message))
                } else {
                    event
                }
                
                delegate.append(compressedEvent)
            } catch (e: Exception) {
                // якщо компресія не вдалася, передаємо оригінальну подію
                delegate.append(event)
            }
        }
        
        /**
         * компресує рядок
         *
         * @param str рядок
         * @return компресований рядок
         */
        private fun compressString(str: String): String {
            try {
                val outputStream = ByteArrayOutputStream()
                val gzipOutputStream = java.util.zip.GZIPOutputStream(outputStream)
                gzipOutputStream.write(str.toByteArray(Charsets.UTF_8))
                gzipOutputStream.close()
                return "[COMPRESSED] " + java.util.Base64.getEncoder().encodeToString(outputStream.toByteArray())
            } catch (e: Exception) {
                return str
            }
        }
        
        override fun close() {
            if (isClosed()) return
            
            try {
                super.close()
                delegate.close()
            } catch (e: Exception) {
                // ігноруємо помилки закриття
            }
        }
    }
    
    /**
     * створює апендер з компресією
     *
     * @param delegate делегат
     * @param compressionThreshold поріг компресії в байтах
     * @return апендер з компресією
     */
    fun createCompressedAppender(
        delegate: Appender,
        compressionThreshold: Int = 1024
    ): CompressedAppender {
        return CompressedAppender(delegate, compressionThreshold)
    }
    
    // функції для роботи з логуванням з підтримкою шифрування
    
    /**
     * представлення апендера з шифруванням
     */
    class EncryptedAppender(
        private val delegate: Appender,
        private val encryptionKey: String,
        private val encryptionThreshold: Int = 512
    ) : BaseAppender() {
        
        override fun append(event: LogEvent) {
            if (isClosed()) return
            
            try {
                val encryptedEvent = if (event.message.toByteArray().size > encryptionThreshold) {
                    event.copy(message = encryptString(event.message))
                } else {
                    event
                }
                
                delegate.append(encryptedEvent)
            } catch (e: Exception) {
                // якщо шифрування не вдалося, передаємо оригінальну подію
                delegate.append(event)
            }
        }
        
        /**
         * шифрує рядок
         *
         * @param str рядок
         * @return зашифрований рядок
         */
        private fun encryptString(str: String): String {
            try {
                // спрощена реалізація шифрування
                // в реальному застосунку слід використовувати надійний алгоритм шифрування
                val sb = StringBuilder()
                for (i in str.indices) {
                    val char = str[i]
                    val keyChar = encryptionKey[i % encryptionKey.length]
                    val encryptedChar = (char.code xor keyChar.code).toChar()
                    sb.append(encryptedChar)
                }
                return "[ENCRYPTED] " + java.util.Base64.getEncoder().encodeToString(sb.toString().toByteArray())
            } catch (e: Exception) {
                return str
            }
        }
        
        override fun close() {
            if (isClosed()) return
            
            try {
                super.close()
                delegate.close()
            } catch (e: Exception) {
                // ігноруємо помилки закриття
            }
        }
    }
    
    /**
     * створює апендер з шифруванням
     *
     * @param delegate делегат
     * @param encryptionKey ключ шифрування
     * @param encryptionThreshold поріг шифрування в байтах
     * @return апендер з шифруванням
     */
    fun createEncryptedAppender(
        delegate: Appender,
        encryptionKey: String,
        encryptionThreshold: Int = 512
    ): EncryptedAppender {
        return EncryptedAppender(delegate, encryptionKey, encryptionThreshold)
    }
    
    // функції для роботи з логуванням з підтримкою пошуку
    
    /**
     * представлення пошуку логів
