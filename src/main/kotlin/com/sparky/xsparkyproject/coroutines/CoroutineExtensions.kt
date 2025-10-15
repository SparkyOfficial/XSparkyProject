package com.sparky.xsparkyproject.coroutines

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext

/**
 * розширення для корутинів з додатковими можливостями
 * включає повторні спроби, таймаути та обмеження одночасних викликів
 *
 * @author Андрій Будильников
 */
class CoroutineExtensions {
    
    companion object {
        /**
         * виконує блок коду з повторними спробами у разі помилки
         */
        suspend fun <T> retry(
            times: Int = 3,
            initialDelayMillis: Long = 100,
            maxDelayMillis: Long = 1000,
            factor: Double = 2.0,
            block: suspend () -> T
        ): T {
            var currentDelay = initialDelayMillis
            repeat(times - 1) {
                try {
                    return block()
                } catch (e: Exception) {
                    // ігноруємо помилку та чекаємо перед наступною спробою
                }
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
            }
            return block() // остання спроба без обгортки try-catch
        }
        
        /**
         * виконує блок коду з таймаутом
         */
        suspend fun <T> withTimeoutOrDefault(
            timeoutMillis: Long,
            defaultValue: T,
            block: suspend () -> T
        ): T {
            return try {
                withTimeout(timeoutMillis) {
                    block()
                }
            } catch (e: TimeoutCancellationException) {
                defaultValue
            }
        }
    }
}