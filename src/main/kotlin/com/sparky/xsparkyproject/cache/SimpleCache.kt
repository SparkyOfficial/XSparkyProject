package com.sparky.xsparkyproject.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * проста реалізація кешу з підтримкою ttl
 *
 * @author Андрій Будильников
 */
class SimpleCache<K, V> private constructor(
    private val defaultTtl: Duration,
    private val cleanupInterval: Duration
) {
    private val cache = ConcurrentHashMap<K, CacheEntry<V>>()
    private val mutex = Mutex()
    
    companion object {
        /**
         * створює новий екземпляр кешу
         */
        fun <K, V> create(
            defaultTtl: Duration = 10.minutes,
            cleanupInterval: Duration = 1.minutes
        ): SimpleCache<K, V> {
            return SimpleCache(defaultTtl, cleanupInterval)
        }
    }
    
    /**
     * додає значення в кеш
     */
    suspend fun put(key: K, value: V, ttl: Duration = defaultTtl) {
        mutex.withLock {
            val expirationTime = System.currentTimeMillis() + ttl.inWholeMilliseconds
            cache[key] = CacheEntry(value, expirationTime)
        }
    }
    
    /**
     * отримує значення з кешу
     */
    suspend fun get(key: K): V? {
        mutex.withLock {
            val entry = cache[key] ?: return null
            
            // перевіряємо чи не прострочений запис
            if (System.currentTimeMillis() > entry.expirationTime) {
                cache.remove(key)
                return null
            }
            
            return entry.value
        }
    }
    
    /**
     * видаляє значення з кешу
     */
    suspend fun remove(key: K) {
        mutex.withLock {
            cache.remove(key)
        }
    }
    
    /**
     * очищує весь кеш
     */
    suspend fun clear() {
        mutex.withLock {
            cache.clear()
        }
    }
    
    /**
     * отримує розмір кешу
     */
    fun size(): Int {
        return cache.size
    }
    
    /**
     * виконує очистку прострочених записів
     */
    suspend fun cleanup() {
        mutex.withLock {
            val currentTime = System.currentTimeMillis()
            val keysToRemove = cache.filter { (_, entry) ->
                currentTime > entry.expirationTime
            }.keys
            
            keysToRemove.forEach { key ->
                cache.remove(key)
            }
        }
    }
}

/**
 * запис кешу зі значенням та часом прострочення
 */
private data class CacheEntry<T>(
    val value: T,
    val expirationTime: Long
)