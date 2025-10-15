/**
 * Веб-фреймворк для роботи з HTTP запитами та веб-технологіями
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

/**
 * представлення інтерфейсу для HTTP клієнта
 */
interface HttpClient {
    /**
     * виконати GET запит
     *
     * @param url URL
     * @param headers заголовки
     * @return відповідь
     */
    fun get(url: String, headers: Map<String, String> = emptyMap()): HttpResponse

    /**
     * виконати POST запит
     *
     * @param url URL
     * @param body тіло запиту
     * @param headers заголовки
     * @return відповідь
     */
    fun post(url: String, body: String, headers: Map<String, String> = emptyMap()): HttpResponse

    /**
     * виконати PUT запит
     *
     * @param url URL
     * @param body тіло запиту
     * @param headers заголовки
     * @return відповідь
     */
    fun put(url: String, body: String, headers: Map<String, String> = emptyMap()): HttpResponse

    /**
     * виконати DELETE запит
     *
     * @param url URL
     * @param headers заголовки
     * @return відповідь
     */
    fun delete(url: String, headers: Map<String, String> = emptyMap()): HttpResponse

    /**
     * встановити таймаут
     *
     * @param timeout таймаут в мілісекундах
     */
    fun setTimeout(timeout: Int)

    /**
     * встановити максимальний розмір відповіді
     *
     * @param maxSize максимальний розмір в байтах
     */
    fun setMaxResponseSize(maxSize: Long)
}

/**
 * представлення відповіді HTTP запиту
 */
data class HttpResponse(
    val statusCode: Int,
    val headers: Map<String, String>,
    val body: String,
    val url: String
)

/**
 * представлення базової реалізації HTTP клієнта
 */
class BaseHttpClient : HttpClient {
    private var timeout = 30000
    private var maxResponseSize = 10 * 1024 * 1024L // 10MB
    private val connectionPool = ConcurrentHashMap<String, HttpURLConnection>()

    override fun get(url: String, headers: Map<String, String>): HttpResponse {
        return executeRequest(url, "GET", null, headers)
    }

    override fun post(url: String, body: String, headers: Map<String, String>): HttpResponse {
        return executeRequest(url, "POST", body, headers)
    }

    override fun put(url: String, body: String, headers: Map<String, String>): HttpResponse {
        return executeRequest(url, "PUT", body, headers)
    }

    override fun delete(url: String, headers: Map<String, String>): HttpResponse {
        return executeRequest(url, "DELETE", null, headers)
    }

    override fun setTimeout(timeout: Int) {
        this.timeout = timeout
    }

    override fun setMaxResponseSize(maxSize: Long) {
        this.maxResponseSize = maxSize
    }

    private fun executeRequest(url: String, method: String, body: String?, headers: Map<String, String>): HttpResponse {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = method
            connection.connectTimeout = timeout
            connection.readTimeout = timeout

            // Встановлення заголовків
            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }

            // Встановлення тіла запиту для POST/PUT
            if (body != null && (method == "POST" || method == "PUT")) {
                connection.doOutput = true
                val outputStream: OutputStream = connection.outputStream
                outputStream.write(body.toByteArray(StandardCharsets.UTF_8))
                outputStream.close()
            }

            // Отримання відповіді
            val statusCode = connection.responseCode
            val responseHeaders = connection.headerFields
                .filterKeys { it != null }
                .mapKeys { it.key!! }
                .mapValues { it.value.joinToString(", ") }

            val responseBody = if (statusCode < 400) {
                readResponseBody(connection.inputStream)
            } else {
                readResponseBody(connection.errorStream)
            }

            connection.disconnect()
            return HttpResponse(statusCode, responseHeaders, responseBody, url)
        } catch (e: Exception) {
            return HttpResponse(0, emptyMap(), "Error: ${e.message}", url)
        }
    }

    private fun readResponseBody(inputStream: java.io.InputStream?): String {
        if (inputStream == null) return ""

        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            response.append(line).append("\n")
            // Перевірка максимального розміру
            if (response.length > maxResponseSize) {
                throw RuntimeException("Response size exceeds maximum allowed size")
            }
        }
        reader.close()
        return response.toString()
    }
}

/**
 * представлення інтерфейсу для роботи з REST API
 */
interface RestApiClient {
    /**
     * отримати ресурс
     *
     * @param endpoint кінцева точка
     * @param params параметри запиту
     * @return відповідь
     */
    fun getResource(endpoint: String, params: Map<String, String> = emptyMap()): RestResponse

    /**
     * створити ресурс
     *
     * @param endpoint кінцева точка
     * @param data дані
     * @return відповідь
     */
    fun createResource(endpoint: String, data: Map<String, Any>): RestResponse

    /**
     * оновити ресурс
     *
     * @param endpoint кінцева точка
     * @param data дані
     * @return відповідь
     */
    fun updateResource(endpoint: String, data: Map<String, Any>): RestResponse

    /**
     * видалити ресурс
     *
     * @param endpoint кінцева точка
     * @return відповідь
     */
    fun deleteResource(endpoint: String): RestResponse

    /**
     * встановити базовий URL
     *
     * @param baseUrl базовий URL
     */
    fun setBaseUrl(baseUrl: String)

    /**
     * встановити токен авторизації
     *
     * @param token токен
     */
    fun setAuthorizationToken(token: String)
}

/**
 * представлення відповіді REST API
 */
data class RestResponse(
    val success: Boolean,
    val data: Map<String, Any>,
    val message: String,
    val statusCode: Int
)

/**
 * представлення базової реалізації REST API клієнта
 */
class BaseRestApiClient(private val httpClient: HttpClient) : RestApiClient {
    private var baseUrl = ""
    private var authToken = ""

    override fun getResource(endpoint: String, params: Map<String, String>): RestResponse {
        val url = buildUrl(endpoint, params)
        val headers = buildHeaders()
        val response = httpClient.get(url, headers)

        return parseRestResponse(response)
    }

    override fun createResource(endpoint: String, data: Map<String, Any>): RestResponse {
        val url = buildUrl(endpoint)
        val headers = buildHeaders()
        val body = data.entries.joinToString(",") { "\"${it.key}\":\"${it.value}\"" }
        val response = httpClient.post(url, "{$body}", headers)

        return parseRestResponse(response)
    }

    override fun updateResource(endpoint: String, data: Map<String, Any>): RestResponse {
        val url = buildUrl(endpoint)
        val headers = buildHeaders()
        val body = data.entries.joinToString(",") { "\"${it.key}\":\"${it.value}\"" }
        val response = httpClient.put(url, "{$body}", headers)

        return parseRestResponse(response)
    }

    override fun deleteResource(endpoint: String): RestResponse {
        val url = buildUrl(endpoint)
        val headers = buildHeaders()
        val response = httpClient.delete(url, headers)

        return parseRestResponse(response)
    }

    override fun setBaseUrl(baseUrl: String) {
        this.baseUrl = baseUrl
    }

    override fun setAuthorizationToken(token: String) {
        this.authToken = token
    }

    private fun buildUrl(endpoint: String, params: Map<String, String> = emptyMap()): String {
        val url = "$baseUrl/$endpoint"
        return if (params.isNotEmpty()) {
            val queryString = params.entries.joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}" }
            "$url?$queryString"
        } else {
            url
        }
    }

    private fun buildHeaders(): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        headers["Content-Type"] = "application/json"
        if (authToken.isNotEmpty()) {
            headers["Authorization"] = "Bearer $authToken"
        }
        return headers
    }

    private fun parseRestResponse(httpResponse: HttpResponse): RestResponse {
        return if (httpResponse.statusCode in 200..299) {
            RestResponse(
                success = true,
                data = mapOf("response" to httpResponse.body),
                message = "Success",
                statusCode = httpResponse.statusCode
            )
        } else {
            RestResponse(
                success = false,
                data = emptyMap(),
                message = httpResponse.body,
                statusCode = httpResponse.statusCode
            )
        }
    }
}

/**
 * представлення інтерфейсу для роботи з веб-сокетами
 */
interface WebSocketClient {
    /**
     * підключитися до сервера
     *
     * @param url URL сервера
     */
    fun connect(url: String)

    /**
     * відправити повідомлення
     *
     * @param message повідомлення
     */
    fun sendMessage(message: String)

    /**
     * відключитися від сервера
     */
    fun disconnect()

    /**
     * встановити обробник отримання повідомлень
     *
     * @param handler обробник
     */
    fun setOnMessageHandler(handler: (String) -> Unit)

    /**
     * встановити обробник помилок
     *
     * @param handler обробник
     */
    fun setOnErrorHandler(handler: (Exception) -> Unit)

    /**
     * встановити обробник закриття з'єднання
     *
     * @param handler обробник
     */
    fun setOnCloseHandler(handler: () -> Unit)
}

/**
 * представлення базової реалізації веб-сокет клієнта
 */
class BaseWebSocketClient : WebSocketClient {
    private var onMessageHandler: ((String) -> Unit)? = null
    private var onErrorHandler: ((Exception) -> Unit)? = null
    private var onCloseHandler: (() -> Unit)? = null
    private var isConnected = false

    override fun connect(url: String) {
        // Це заглушка для підключення до веб-сокет сервера
        isConnected = true
    }

    override fun sendMessage(message: String) {
        // Це заглушка для відправки повідомлення
        if (!isConnected) {
            onErrorHandler?.invoke(RuntimeException("Not connected to WebSocket server"))
        }
    }

    override fun disconnect() {
        // Це заглушка для відключення від веб-сокет сервера
        isConnected = false
        onCloseHandler?.invoke()
    }

    override fun setOnMessageHandler(handler: (String) -> Unit) {
        this.onMessageHandler = handler
    }

    override fun setOnErrorHandler(handler: (Exception) -> Unit) {
        this.onErrorHandler = handler
    }

    override fun setOnCloseHandler(handler: () -> Unit) {
        this.onCloseHandler = handler
    }
}

/**
 * представлення інтерфейсу для роботи з HTML парсером
 */
interface HtmlParser {
    /**
     * розпарсити HTML документ
     *
     * @param html HTML документ
     * @return розібраний документ
     */
    fun parse(html: String): HtmlDocument

    /**
     * знайти елементи за CSS селектором
     *
     * @param document документ
     * @param selector CSS селектор
     * @return список елементів
     */
    fun findElementsByCss(document: HtmlDocument, selector: String): List<HtmlElement>

    /**
     * отримати текст елемента
     *
     * @param element елемент
     * @return текст
     */
    fun getText(element: HtmlElement): String

    /**
     * отримати атрибут елемента
     *
     * @param element елемент
     * @param attributeName ім'я атрибута
     * @return значення атрибута
     */
    fun getAttribute(element: HtmlElement, attributeName: String): String?
}

/**
 * представлення HTML документа
 */
data class HtmlDocument(val content: String, val elements: List<HtmlElement>)

/**
 * представлення HTML елемента
 */
data class HtmlElement(val tagName: String, val attributes: Map<String, String>, val text: String)

/**
 * представлення базової реалізації HTML парсера
 */
class BaseHtmlParser : HtmlParser {
    override fun parse(html: String): HtmlDocument {
        // Це заглушка для парсингу HTML документа
        return HtmlDocument(html, emptyList())
    }

    override fun findElementsByCss(document: HtmlDocument, selector: String): List<HtmlElement> {
        // Це заглушка для пошуку елементів за CSS селектором
        return emptyList()
    }

    override fun getText(element: HtmlElement): String {
        return element.text
    }

    override fun getAttribute(element: HtmlElement, attributeName: String): String? {
        return element.attributes[attributeName]
    }
}

/**
 * представлення інтерфейсу для роботи з URL
 */
interface UrlUtils {
    /**
     * побудувати URL з параметрів
     *
     * @param baseUrl базовий URL
     * @param params параметри
     * @return URL
     */
    fun buildUrl(baseUrl: String, params: Map<String, String>): String

    /**
     * розібрати URL на компоненти
     *
     * @param url URL
     * @return компоненти URL
     */
    fun parseUrl(url: String): UrlComponents

    /**
     * закодувати URL
     *
     * @param url URL
     * @return закодований URL
     */
    fun encodeUrl(url: String): String

    /**
     * декодувати URL
     *
     * @param url URL
     * @return декодований URL
     */
    fun decodeUrl(url: String): String
}

/**
 * представлення компонентів URL
 */
data class UrlComponents(
    val protocol: String,
    val host: String,
    val port: Int,
    val path: String,
    val query: String,
    val fragment: String
)

/**
 * представлення базової реалізації утиліт для роботи з URL
 */
class BaseUrlUtils : UrlUtils {
    override fun buildUrl(baseUrl: String, params: Map<String, String>): String {
        if (params.isEmpty()) return baseUrl

        val queryString = params.entries.joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}" }
        return if (baseUrl.contains("?")) {
            "$baseUrl&$queryString"
        } else {
            "$baseUrl?$queryString"
        }
    }

    override fun parseUrl(url: String): UrlComponents {
        // Це заглушка для розбору URL на компоненти
        return UrlComponents(
            protocol = "http",
            host = "localhost",
            port = 80,
            path = "/",
            query = "",
            fragment = ""
        )
    }

    override fun encodeUrl(url: String): String {
        return URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
    }

    override fun decodeUrl(url: String): String {
        return java.net.URLDecoder.decode(url, StandardCharsets.UTF_8.toString())
    }
}

/**
 * представлення інтерфейсу для роботи з кукі
 */
interface CookieManager {
    /**
     * встановити кукі
     *
     * @param name ім'я
     * @param value значення
     * @param domain домен
     * @param path шлях
     * @param maxAge максимальний вік
     * @param secure безпечний
     * @param httpOnly тільки HTTP
     */
    fun setCookie(
        name: String,
        value: String,
        domain: String = "",
        path: String = "/",
        maxAge: Int = -1,
        secure: Boolean = false,
        httpOnly: Boolean = false
    )

    /**
     * отримати кукі
     *
     * @param name ім'я
     * @return значення
     */
    fun getCookie(name: String): String?

    /**
     * видалити кукі
     *
     * @param name ім'я
     */
    fun removeCookie(name: String)

    /**
     * отримати всі кукі
     *
     * @return мапа кукі
     */
    fun getAllCookies(): Map<String, String>

    /**
     * очистити всі кукі
     */
    fun clearAllCookies()
}

/**
 * представлення базової реалізації менеджера кукі
 */
class BaseCookieManager : CookieManager {
    private val cookies = ConcurrentHashMap<String, String>()

    override fun setCookie(
        name: String,
        value: String,
        domain: String,
        path: String,
        maxAge: Int,
        secure: Boolean,
        httpOnly: Boolean
    ) {
        cookies[name] = value
    }

    override fun getCookie(name: String): String? {
        return cookies[name]
    }

    override fun removeCookie(name: String) {
        cookies.remove(name)
    }

    override fun getAllCookies(): Map<String, String> {
        return cookies.toMap()
    }

    override fun clearAllCookies() {
        cookies.clear()
    }
}

/**
 * представлення інтерфейсу для роботи з кешем HTTP
 */
interface HttpCache {
    /**
     * зберегти відповідь у кеш
     *
     * @param key ключ
     * @param response відповідь
     * @param ttl час життя в мілісекундах
     */
    fun put(key: String, response: HttpResponse, ttl: Long)

    /**
     * отримати відповідь з кешу
     *
     * @param key ключ
     * @return відповідь
     */
    fun get(key: String): HttpResponse?

    /**
     * видалити відповідь з кешу
     *
     * @param key ключ
     */
    fun remove(key: String)

    /**
     * очистити кеш
     */
    fun clear()

    /**
     * отримати розмір кешу
     *
     * @return розмір
     */
    fun size(): Int
}

/**
 * представлення базової реалізації кешу HTTP
 */
class BaseHttpCache : HttpCache {
    private val cache = ConcurrentHashMap<String, CachedHttpResponse>()
    private val cacheCleanupJob: Job

    init {
        cacheCleanupJob = GlobalScope.launch {
            while (isActive) {
                delay(60000) // Перевірка кожну хвилину
                cleanupExpiredEntries()
            }
        }
    }

    override fun put(key: String, response: HttpResponse, ttl: Long) {
        val expirationTime = System.currentTimeMillis() + ttl
        cache[key] = CachedHttpResponse(response, expirationTime)
    }

    override fun get(key: String): HttpResponse? {
        val cachedResponse = cache[key]
        return if (cachedResponse != null && cachedResponse.expirationTime > System.currentTimeMillis()) {
            cachedResponse.response
        } else {
            cache.remove(key)
            null
        }
    }

    override fun remove(key: String) {
        cache.remove(key)
    }

    override fun clear() {
        cache.clear()
    }

    override fun size(): Int {
        return cache.size
    }

    private fun cleanupExpiredEntries() {
        val currentTime = System.currentTimeMillis()
        cache.entries.removeIf { it.value.expirationTime <= currentTime }
    }

    /**
     * представлення кешованої відповіді HTTP
     */
    private data class CachedHttpResponse(val response: HttpResponse, val expirationTime: Long)
}

/**
 * представлення інтерфейсу для роботи з веб-хуками
 */
interface WebHookManager {
    /**
     * зареєструвати веб-хук
     *
     * @param url URL веб-хука
     * @param eventType тип події
     * @param handler обробник
     */
    fun registerWebHook(url: String, eventType: String, handler: (Map<String, Any>) -> Unit)

    /**
     * відправити подію веб-хука
     *
     * @param eventType тип події
     * @param data дані
     */
    fun triggerWebHook(eventType: String, data: Map<String, Any>)

    /**
     * видалити веб-хук
     *
     * @param url URL веб-хука
     * @param eventType тип події
     */
    fun removeWebHook(url: String, eventType: String)

    /**
     * отримати всі зареєстровані веб-хуки
     *
     * @return мапа веб-хуків
     */
    fun getAllWebHooks(): Map<String, List<String>>
}

/**
 * представлення базової реалізації менеджера веб-хуків
 */
class BaseWebHookManager : WebHookManager {
    private val webHooks = ConcurrentHashMap<String, MutableMap<String, (Map<String, Any>) -> Unit>>()

    override fun registerWebHook(url: String, eventType: String, handler: (Map<String, Any>) -> Unit) {
        webHooks.computeIfAbsent(url) { ConcurrentHashMap() }[eventType] = handler
    }

    override fun triggerWebHook(eventType: String, data: Map<String, Any>) {
        webHooks.forEach { (url, handlers) ->
            handlers[eventType]?.invoke(data)
        }
    }

    override fun removeWebHook(url: String, eventType: String) {
        webHooks[url]?.remove(eventType)
        if (webHooks[url]?.isEmpty() == true) {
            webHooks.remove(url)
        }
    }

    override fun getAllWebHooks(): Map<String, List<String>> {
        return webHooks.mapValues { it.value.keys.toList() }
    }
}

/**
 * представлення інтерфейсу для роботи з OAuth
 */
interface OAuthManager {
    /**
     * отримати URL авторизації
     *
     * @param clientId ідентифікатор клієнта
     * @param redirectUri URI перенаправлення
     * @param scope область доступу
     * @param state стан
     * @return URL авторизації
     */
    fun getAuthorizationUrl(clientId: String, redirectUri: String, scope: String, state: String): String

    /**
     * обміняти код на токен
     *
     * @param clientId ідентифікатор клієнта
     * @param clientSecret секрет клієнта
     * @param code код авторизації
     * @param redirectUri URI перенаправлення
     * @return токен доступу
     */
    fun exchangeCodeForToken(clientId: String, clientSecret: String, code: String, redirectUri: String): OAuthToken

    /**
     * оновити токен доступу
     *
     * @param clientId ідентифікатор клієнта
     * @param clientSecret секрет клієнта
     * @param refreshToken токен оновлення
     * @return новий токен доступу
     */
    fun refreshAccessToken(clientId: String, clientSecret: String, refreshToken: String): OAuthToken

    /**
     * перевірити токен
     *
     * @param token токен
     * @return інформація про токен
     */
    fun validateToken(token: String): TokenInfo
}

/**
 * представлення токена OAuth
 */
data class OAuthToken(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val refreshToken: String?,
    val scope: String?
)

/**
 * представлення інформації про токен
 */
data class TokenInfo(
    val valid: Boolean,
    val userId: String?,
    val expiresAt: Long?,
    val scope: String?
)

/**
 * представлення базової реалізації менеджера OAuth
 */
class BaseOAuthManager : OAuthManager {
    override fun getAuthorizationUrl(clientId: String, redirectUri: String, scope: String, state: String): String {
        return "https://oauth.example.com/authorize?client_id=$clientId&redirect_uri=$redirectUri&scope=$scope&state=$state"
    }

    override fun exchangeCodeForToken(clientId: String, clientSecret: String, code: String, redirectUri: String): OAuthToken {
        // Це заглушка для обміну коду на токен
        return OAuthToken(
            accessToken = "access_token_$code",
            tokenType = "Bearer",
            expiresIn = 3600,
            refreshToken = "refresh_token_$code",
            scope = "read write"
        )
    }

    override fun refreshAccessToken(clientId: String, clientSecret: String, refreshToken: String): OAuthToken {
        // Це заглушка для оновлення токена доступу
        return OAuthToken(
            accessToken = "new_access_token_$refreshToken",
            tokenType = "Bearer",
            expiresIn = 3600,
            refreshToken = "new_refresh_token_$refreshToken",
            scope = "read write"
        )
    }

    override fun validateToken(token: String): TokenInfo {
        // Це заглушка для перевірки токена
        return TokenInfo(
            valid = true,
            userId = "user_123",
            expiresAt = System.currentTimeMillis() + 3600000,
            scope = "read write"
        )
    }
}