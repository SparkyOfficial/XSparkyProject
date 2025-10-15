package com.sparky.xsparkyproject.logging

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * простий логер з підтримкою різних рівнів
 *
 * @author Андрій Будильников
 */
class SimpleLogger private constructor(
    private val name: String,
    private val level: LogLevel
) {
    
    companion object {
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        
        /**
         * створює новий екземпляр логера
         */
        fun create(
            name: String,
            level: LogLevel = LogLevel.INFO
        ): SimpleLogger {
            return SimpleLogger(name, level)
        }
    }
    
    /**
     * логує повідомлення з рівнем trace
     */
    fun trace(message: String) {
        log(LogLevel.TRACE, message)
    }
    
    /**
     * логує повідомлення з рівнем debug
     */
    fun debug(message: String) {
        log(LogLevel.DEBUG, message)
    }
    
    /**
     * логує повідомлення з рівнем info
     */
    fun info(message: String) {
        log(LogLevel.INFO, message)
    }
    
    /**
     * логує повідомлення з рівнем warn
     */
    fun warn(message: String) {
        log(LogLevel.WARN, message)
    }
    
    /**
     * логує повідомлення з рівнем error
     */
    fun error(message: String) {
        log(LogLevel.ERROR, message)
    }
    
    /**
     * внутрішня функція для логування
     */
    private fun log(level: LogLevel, message: String) {
        // перевіряємо чи потрібно логувати на цьому рівні
        if (!shouldLog(level)) return
        
        val timestamp = LocalDateTime.now().format(formatter)
        val logMessage = "[$timestamp] [${level.name}] [$name] $message"
        
        // виводимо в консоль
        println(logMessage)
    }
    
    /**
     * перевіряє чи потрібно логувати на цьому рівні
     */
    private fun shouldLog(logLevel: LogLevel): Boolean {
        return logLevel.ordinal >= this.level.ordinal
    }
}

/**
 * рівні логування
 */
enum class LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR
}