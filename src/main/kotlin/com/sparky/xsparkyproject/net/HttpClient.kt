package com.sparky.xsparkyproject.net

import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * простий http клієнт з підтримкою корутинів
 * для зручної роботи з мережею
 *
 * @author Андрій Будильников
 */
class HttpClient {
    companion object {
        /**
         * виконує get запит до вказаного url
         */
        suspend fun get(url: String, headers: Map<String, String> = emptyMap()): HttpResponse = suspendCancellableCoroutine { continuation ->
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                
                // встановлюємо заголовки
                headers.forEach { (key, value) ->
                    connection.setRequestProperty(key, value)
                }
                
                // встановлюємо таймаут
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                
                val responseCode = connection.responseCode
                val responseBody = if (responseCode in 200..299) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                }
                
                // отримуємо заголовки відповіді
                val responseHeaders = mutableMapOf<String, String>()
                connection.headerFields.forEach { (key, value) ->
                    if (key != null && value != null) {
                        responseHeaders[key] = value.joinToString(", ")
                    }
                }
                
                connection.disconnect()
                
                continuation.resume(HttpResponse(responseCode, responseBody, responseHeaders))
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
        
        /**
         * виконує post запит до вказаного url з даними
         */
        suspend fun post(url: String, body: String, contentType: String = "application/json", headers: Map<String, String> = emptyMap()): HttpResponse = 
            suspendCancellableCoroutine { continuation ->
                try {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", contentType)
                    
                    // встановлюємо заголовки
                    headers.forEach { (key, value) ->
                        connection.setRequestProperty(key, value)
                    }
                    
                    // встановлюємо таймаут
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    
                    // записуємо тіло запиту
                    connection.outputStream.use { outputStream ->
                        outputStream.write(body.toByteArray())
                    }
                    
                    val responseCode = connection.responseCode
                    val responseBody = if (responseCode in 200..299) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    }
                    
                    // отримуємо заголовки відповіді
                    val responseHeaders = mutableMapOf<String, String>()
                    connection.headerFields.forEach { (key, value) ->
                        if (key != null && value != null) {
                            responseHeaders[key] = value.joinToString(", ")
                        }
                    }
                    
                    connection.disconnect()
                    
                    continuation.resume(HttpResponse(responseCode, responseBody, responseHeaders))
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }
    }
}

/**
 * представляє відповідь http запиту
 */
data class HttpResponse(
    val statusCode: Int, 
    val body: String,
    val headers: Map<String, String> = emptyMap()
)