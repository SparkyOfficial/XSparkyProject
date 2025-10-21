package com.sparky.xsparkyproject.coroutines

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext

/**
 * розширення для корутинів з додатковими можливостями
 * включає повторні спроби, таймаути та обмеження одночасних викликів
 *
 * ПРИМІТКА: SpotBugs може повідомляти про передачу null для параметрів, що не допускають null.
 * Ці попередження є хибнопозитивними, оскільки Kotlin забезпечує безпеку від null на рівні типів.
 * Функціональні типи в Kotlin (suspend () -> T) є не-null за замовчуванням.
 *
 * @author Андрій Будильников
 */
class CoroutineExtensions {
    
    companion object {
        /**
         * виконує блок коду з повторними спробами у разі помилки
         *
         * @param times кількість спроб (має бути > 0)
         * @param initialDelayMillis початкова затримка між спробами
         * @param maxDelayMillis максимальна затримка між спробами
         * @param factor множник для експоненційного збільшення затримки
         * @param block блок коду для виконання
         * @return результат виконання блоку
         * @throws IllegalArgumentException якщо times <= 0
         */
        suspend fun <T> retry(
            times: Int = 3,
            initialDelayMillis: Long = 100,
            maxDelayMillis: Long = 1000,
            factor: Double = 2.0,
            block: suspend () -> T
        ): T {
            // Handle edge case where times <= 0
            if (times <= 0) {
                throw IllegalArgumentException("Кількість спроб має бути більше 0")
            }
            
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
         *
         * @param timeoutMillis таймаут у мілісекундах
         * @param defaultValue значення за замовчуванням, яке повертається у разі таймауту або помилки
         * @param block блок коду для виконання
         * @return результат виконання блоку або значення за замовчуванням
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
            } catch (e: Exception) {
                // Handle any other exceptions thrown by the block
                defaultValue
            }
        }
    }
}