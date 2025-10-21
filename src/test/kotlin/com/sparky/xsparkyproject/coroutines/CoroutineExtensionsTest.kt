/**
 * тест для розширень корутинів
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.coroutines

import kotlinx.coroutines.*
import kotlin.test.*

/**
 * тест для розширень корутинів
 */
class CoroutineExtensionsTest {
    
    @Test
    fun testRetrySuccess() = runBlocking {
        var attempts = 0
        val result = CoroutineExtensions.retry(times = 3) {
            attempts++
            "success"
        }
        
        assertEquals("success", result)
        assertEquals(1, attempts)
    }
    
    @Test
    fun testRetryWithFailures() = runBlocking {
        var attempts = 0
        val result = CoroutineExtensions.retry(times = 3) {
            attempts++
            if (attempts < 3) {
                throw RuntimeException("Помилка спроби $attempts")
            }
            "success"
        }
        
        assertEquals("success", result)
        assertEquals(3, attempts)
    }
    
    @Test
    fun testRetryAllFailures() = runBlocking {
        var attempts = 0
        assertFailsWith<RuntimeException> {
            CoroutineExtensions.retry(times = 3) {
                attempts++
                throw RuntimeException("Помилка спроби $attempts")
            }
        }
        
        assertEquals(3, attempts)
    }
    
    @Test
    fun testWithTimeoutOrDefaultSuccess() = runBlocking {
        val result = CoroutineExtensions.withTimeoutOrDefault(
            timeoutMillis = 1000,
            defaultValue = "default"
        ) {
            delay(100)
            "success"
        }
        
        assertEquals("success", result)
    }
    
    @Test
    fun testWithTimeoutOrDefaultTimeout() = runBlocking {
        val result = CoroutineExtensions.withTimeoutOrDefault(
            timeoutMillis = 100,
            defaultValue = "default"
        ) {
            delay(500)
            "success"
        }
        
        assertEquals("default", result)
    }
    
    @Test
    fun testWithTimeoutOrDefaultException() = runBlocking {
        val result = CoroutineExtensions.withTimeoutOrDefault(
            timeoutMillis = 1000,
            defaultValue = "default"
        ) {
            throw RuntimeException("Помилка в блоці")
        }
        
        assertEquals("default", result)
    }
    
    @Test
    fun testRetryWithZeroAttempts() = runBlocking {
        var attempts = 0
        assertFailsWith<IllegalArgumentException> {
            CoroutineExtensions.retry(times = 0) {
                attempts++
                throw RuntimeException("Помилка")
            }
        }
        
        // With times = 0, the block should never be executed
        // Our implementation now throws IllegalArgumentException for times <= 0
        assertEquals(0, attempts)
    }
    
    @Test
    fun testRetryWithOneAttempt() = runBlocking {
        var attempts = 0
        assertFailsWith<RuntimeException> {
            CoroutineExtensions.retry(times = 1) {
                attempts++
                throw RuntimeException("Помилка")
            }
        }
        
        assertEquals(1, attempts)
    }
    

    
}