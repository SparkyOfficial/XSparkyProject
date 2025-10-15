package com.sparky.xsparkyproject.config

import java.util.Properties
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

/**
 * утиліти для роботи з конфігурацією
 *
 * @author Андрій Будильников
 */
class ConfigManager private constructor() {
    private val properties = Properties()
    
    companion object {
        @Volatile
        private var INSTANCE: ConfigManager? = null
        private val LOCK = Any()
        
        /**
         * отримує екземпляр менеджера конфігурації (singleton)
         */
        fun getInstance(): ConfigManager {
            return INSTANCE ?: synchronized(LOCK) {
                INSTANCE ?: ConfigManager().also { INSTANCE = it }
            }
        }
        
        /**
         * створює новий екземпляр менеджера конфігурації
         */
        fun create(): ConfigManager {
            return ConfigManager()
        }
    }
    
    /**
     * завантажує конфігурацію з файлу
     */
    fun loadFromFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                return false
            }
            
            FileInputStream(file).use { fis ->
                InputStreamReader(fis, "UTF-8").use { isr ->
                    properties.load(isr)
                }
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * завантажує конфігурацію з map
     */
    fun loadFromMap(configMap: Map<String, String>) {
        properties.putAll(configMap)
    }
    
    /**
     * отримує значення конфігурації за ключем
     */
    fun getString(key: String): String? {
        return properties.getProperty(key)
    }
    
    /**
     * отримує значення конфігурації зі значенням за замовчуванням
     */
    fun getStringOrDefault(key: String, defaultValue: String): String {
        return properties.getProperty(key, defaultValue)
    }
    
    /**
     * встановлює значення конфігурації
     */
    fun setProperty(key: String, value: String) {
        properties.setProperty(key, value)
    }
    
    /**
     * отримує всі ключі конфігурації
     */
    fun getAllKeys(): Set<String> {
        return properties.stringPropertyNames()
    }
}