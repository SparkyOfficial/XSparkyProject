/**
 * фреймворк для роботи з мережею
 *
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.net.*
import java.io.*
import java.util.concurrent.*
import javax.net.ssl.*
import java.security.*
import java.security.cert.*

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
     * виконати довільний HTTP запит
     *
     * @param request запит
     * @return відповідь
     */
    fun execute(request: HttpRequest): HttpResponse
}

/**
 * представлення HTTP запиту
 *
 * @property method метод
 * @property url URL
 * @property headers заголовки
 * @property body тіло
 */
data class HttpRequest(
    val method: HttpMethod,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
)

/**
 * представлення HTTP відповіді
 *
 * @property statusCode код статусу
 * @property headers заголовки
 * @property body тіло
 */
data class HttpResponse(
    val statusCode: Int,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
)

/**
 * представлення HTTP методу
 */
enum class HttpMethod {
    GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
}

/**
 * представлення базової реалізації HTTP клієнта
 */
open class BaseHttpClient : HttpClient {
    
    override fun get(url: String, headers: Map<String, String>): HttpResponse {
        return execute(HttpRequest(HttpMethod.GET, url, headers))
    }
    
    override fun post(url: String, body: String, headers: Map<String, String>): HttpResponse {
        return execute(HttpRequest(HttpMethod.POST, url, headers, body))
    }
    
    override fun put(url: String, body: String, headers: Map<String, String>): HttpResponse {
        return execute(HttpRequest(HttpMethod.PUT, url, headers, body))
    }
    
    override fun delete(url: String, headers: Map<String, String>): HttpResponse {
        return execute(HttpRequest(HttpMethod.DELETE, url, headers))
    }
    
    override fun execute(request: HttpRequest): HttpResponse {
        try {
            val url = URL(request.url)
            val connection = url.openConnection() as HttpURLConnection
            
            // Налаштування методу
            connection.requestMethod = request.method.name
            
            // Налаштування заголовків
            request.headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }
            
            // Налаштування тіла для методів, які його підтримують
            if (request.body != null && 
                (request.method == HttpMethod.POST || 
                 request.method == HttpMethod.PUT || 
                 request.method == HttpMethod.PATCH)) {
                connection.doOutput = true
                val outputStream = connection.outputStream
                outputStream.write(request.body.toByteArray())
                outputStream.close()
            }
            
            // Отримання відповіді
            val statusCode = connection.responseCode
            val responseHeaders = mutableMapOf<String, String>()
            
            connection.headerFields.forEach { (key, values) ->
                if (key != null && values.isNotEmpty()) {
                    responseHeaders[key] = values[0]
                }
            }
            
            val responseBody = try {
                val inputStream = connection.inputStream
                inputStream.bufferedReader().use { it.readText() }
            } catch (e: IOException) {
                // Якщо є помилка, спробуємо прочитати з errorStream
                try {
                    val errorStream = connection.errorStream
                    if (errorStream != null) {
                        errorStream.bufferedReader().use { it.readText() }
                    } else {
                        null
                    }
                } catch (ee: IOException) {
                    null
                }
            }
            
            connection.disconnect()
            
            return HttpResponse(statusCode, responseHeaders, responseBody)
        } catch (e: Exception) {
            return HttpResponse(0, emptyMap(), "Помилка: ${e.message}")
        }
    }
}

/**
 * представлення інтерфейсу для роботи з URL
 */
interface UrlHelper {
    /**
     * побудувати URL
     *
     * @param baseUrl базовий URL
     * @param path шлях
     * @param queryParams параметри запиту
     * @return URL
     */
    fun buildUrl(baseUrl: String, path: String, queryParams: Map<String, String> = emptyMap()): String
    
    /**
     * розібрати URL
     *
     * @param url URL
     * @return компоненти URL
     */
    fun parseUrl(url: String): UrlComponents
    
    /**
     * закодувати URL
     *
     * @param value значення
     * @return закодоване значення
     */
    fun encodeUrl(value: String): String
    
    /**
     * декодувати URL
     *
     * @param value закодоване значення
     * @return декодоване значення
     */
    fun decodeUrl(value: String): String
}

/**
 * представлення компонентів URL
 *
 * @property protocol протокол
 * @property host хост
 * @property port порт
 * @property path шлях
 * @property queryParams параметри запиту
 * @property fragment фрагмент
 */
data class UrlComponents(
    val protocol: String,
    val host: String,
    val port: Int,
    val path: String,
    val queryParams: Map<String, String>,
    val fragment: String?
)

/**
 * представлення базової реалізації помічника з URL
 */
open class BaseUrlHelper : UrlHelper {
    
    override fun buildUrl(baseUrl: String, path: String, queryParams: Map<String, String>): String {
        val urlBuilder = StringBuilder(baseUrl)
        
        // Додати шлях
        if (!path.startsWith("/")) {
            urlBuilder.append("/")
        }
        urlBuilder.append(path)
        
        // Додати параметри запиту
        if (queryParams.isNotEmpty()) {
            urlBuilder.append("?")
            val queryParamStrings = queryParams.map { "${encodeUrl(it.key)}=${encodeUrl(it.value)}" }
            urlBuilder.append(queryParamStrings.joinToString("&"))
        }
        
        return urlBuilder.toString()
    }
    
    override fun parseUrl(url: String): UrlComponents {
        val uri = URI(url)
        val queryParams = mutableMapOf<String, String>()
        
        // Розібрати параметри запиту
        if (uri.query != null) {
            uri.query.split("&").forEach { param ->
                val parts = param.split("=")
                if (parts.size == 2) {
                    queryParams[decodeUrl(parts[0])] = decodeUrl(parts[1])
                }
            }
        }
        
        return UrlComponents(
            uri.scheme ?: "",
            uri.host ?: "",
            uri.port,
            uri.path ?: "",
            queryParams,
            uri.fragment
        )
    }
    
    override fun encodeUrl(value: String): String {
        return try {
            URLEncoder.encode(value, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            value // Повернути оригінальне значення у разі помилки
        }
    }
    
    override fun decodeUrl(value: String): String {
        return try {
            URLDecoder.decode(value, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            value // Повернути оригінальне значення у разі помилки
        }
    }
}

/**
 * представлення інтерфейсу для роботи з сокетами
 */
interface SocketHelper {
    /**
     * створити TCP сокет
     *
     * @param host хост
     * @param port порт
     * @param timeout таймаут
     * @return сокет
     */
    fun createTcpSocket(host: String, port: Int, timeout: Int = 5000): Socket
    
    /**
     * створити SSL сокет
     *
     * @param host хост
     * @param port порт
     * @param timeout таймаут
     * @return SSL сокет
     */
    fun createSslSocket(host: String, port: Int, timeout: Int = 5000): SSLSocket
    
    /**
     * створити серверний сокет
     *
     * @param port порт
     * @return серверний сокет
     */
    fun createServerSocket(port: Int): ServerSocket
    
    /**
     * закрити сокет
     *
     * @param socket сокет
     */
    fun closeSocket(socket: Socket)
    
    /**
     * закрити серверний сокет
     *
     * @param socket серверний сокет
     */
    fun closeServerSocket(socket: ServerSocket)
}

/**
 * представлення базової реалізації помічника з сокетами
 */
open class BaseSocketHelper : SocketHelper {
    
    override fun createTcpSocket(host: String, port: Int, timeout: Int): Socket {
        val socket = Socket()
        socket.soTimeout = timeout
        socket.connect(InetSocketAddress(host, port), timeout)
        return socket
    }
    
    override fun createSslSocket(host: String, port: Int, timeout: Int): SSLSocket {
        val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
        val socket = factory.createSocket(host, port) as SSLSocket
        socket.soTimeout = timeout
        return socket
    }
    
    override fun createServerSocket(port: Int): ServerSocket {
        return ServerSocket(port)
    }
    
    override fun closeSocket(socket: Socket) {
        try {
            socket.close()
        } catch (e: IOException) {
            // Ігнорувати помилки закриття
        }
    }
    
    override fun closeServerSocket(socket: ServerSocket) {
        try {
            socket.close()
        } catch (e: IOException) {
            // Ігнорувати помилки закриття
        }
    }
}

/**
 * представлення інтерфейсу для роботи з DNS
 */
interface DnsHelper {
    /**
     * отримати IP адреси за доменним ім'ям
     *
     * @param hostname доменне ім'я
     * @return список IP адрес
     */
    fun lookup(hostname: String): List<InetAddress>
    
    /**
     * зворотний DNS пошук
     *
     * @param address IP адреса
     * @return доменне ім'я
     */
    fun reverseLookup(address: InetAddress): String
    
    /**
     * перевірити доступність хоста
     *
     * @param hostname доменне ім'я
     * @param timeout таймаут в мілісекундах
     * @return true, якщо хост доступний
     */
    fun isHostReachable(hostname: String, timeout: Int = 5000): Boolean
}

/**
 * представлення базової реалізації помічника з DNS
 */
open class BaseDnsHelper : DnsHelper {
    
    override fun lookup(hostname: String): List<InetAddress> {
        return try {
            InetAddress.getAllByName(hostname).toList()
        } catch (e: UnknownHostException) {
            emptyList()
        }
    }
    
    override fun reverseLookup(address: InetAddress): String {
        return try {
            address.hostName
        } catch (e: UnknownHostException) {
            address.hostAddress
        }
    }
    
    override fun isHostReachable(hostname: String, timeout: Int): Boolean {
        return try {
            val address = InetAddress.getByName(hostname)
            address.isReachable(timeout)
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * представлення інтерфейсу для роботи з HTTP сервером
 */
interface HttpServer {
    /**
     * запустити сервер
     *
     * @param port порт
     */
    fun start(port: Int)
    
    /**
     * зупинити сервер
     */
    fun stop()
    
    /**
     * додати обробник для шляху
     *
     * @param path шлях
     * @param handler обробник
     */
    fun addHandler(path: String, handler: HttpRequestHandler)
    
    /**
     * встановити обробник помилок
     *
     * @param handler обробник помилок
     */
    fun setErrorHandler(handler: HttpErrorHandler)
}

/**
 * представлення обробника HTTP запитів
 */
interface HttpRequestHandler {
    /**
     * обробити запит
     *
     * @param request запит
     * @return відповідь
     */
    fun handle(request: HttpRequest): HttpResponse
}

/**
 * представлення обробника помилок HTTP
 */
interface HttpErrorHandler {
    /**
     * обробити помилку
     *
     * @param exception виключення
     * @param request запит
     * @return відповідь
     */
    fun handle(exception: Exception, request: HttpRequest): HttpResponse
}

/**
 * представлення базової реалізації HTTP сервера
 */
open class BaseHttpServer : HttpServer {
    private var serverSocket: ServerSocket? = null
    private val handlers = mutableMapOf<String, HttpRequestHandler>()
    private var errorHandler: HttpErrorHandler? = null
    private var isRunning = false
    
    override fun start(port: Int) {
        if (isRunning) return
        
        serverSocket = ServerSocket(port)
        isRunning = true
        
        // Запустити потік для обробки з'єднань
        Thread {
            while (isRunning) {
                try {
                    val socket = serverSocket?.accept() ?: break
                    handleConnection(socket)
                } catch (e: IOException) {
                    if (isRunning) {
                        e.printStackTrace()
                    }
                }
            }
        }.start()
    }
    
    override fun stop() {
        isRunning = false
        serverSocket?.close()
        serverSocket = null
    }
    
    override fun addHandler(path: String, handler: HttpRequestHandler) {
        handlers[path] = handler
    }
    
    override fun setErrorHandler(handler: HttpErrorHandler) {
        errorHandler = handler
    }
    
    private fun handleConnection(socket: Socket) {
        Thread {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val writer = PrintWriter(socket.getOutputStream(), true)
                
                // Прочитати запит
                val requestLine = reader.readLine() ?: return@Thread
                val parts = requestLine.split(" ")
                if (parts.size < 3) return@Thread
                
                val method = HttpMethod.valueOf(parts[0])
                val path = parts[1]
                
                // Прочитати заголовки
                val headers = mutableMapOf<String, String>()
                var line: String?
                while (reader.readLine().also { line = it } != null && line!!.isNotEmpty()) {
                    val headerParts = line!!.split(":", limit = 2)
                    if (headerParts.size == 2) {
                        headers[headerParts[0].trim()] = headerParts[1].trim()
                    }
                }
                
                // Прочитати тіло (якщо є)
                val contentLength = headers["Content-Length"]?.toIntOrNull() ?: 0
                val body = if (contentLength > 0) {
                    val bodyBuffer = CharArray(contentLength)
                    reader.read(bodyBuffer, 0, contentLength)
                    String(bodyBuffer)
                } else {
                    null
                }
                
                // Створити об'єкт запиту
                val request = HttpRequest(method, path, headers, body)
                
                // Обробити запит
                val handler = handlers[path]
                val response = if (handler != null) {
                    try {
                        handler.handle(request)
                    } catch (e: Exception) {
                        errorHandler?.handle(e, request) ?: HttpResponse(500, emptyMap(), "Internal Server Error")
                    }
                } else {
                    HttpResponse(404, emptyMap(), "Not Found")
                }
                
                // Надіслати відповідь
                writer.println("HTTP/1.1 ${response.statusCode}")
                response.headers.forEach { (key, value) ->
                    writer.println("$key: $value")
                }
                writer.println() // Порожній рядок після заголовків
                response.body?.let { writer.println(it) }
                
                writer.close()
                reader.close()
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    socket.close()
                } catch (ee: IOException) {
                    // Ігнорувати
                }
            }
        }.start()
    }
}

/**
 * представлення інтерфейсу для роботи з WebSocket
 */
interface WebSocketClient {
    /**
     * підключитися до WebSocket сервера
     *
     * @param url URL сервера
     */
    fun connect(url: String)
    
    /**
     * відправити текстове повідомлення
     *
     * @param message повідомлення
     */
    fun sendText(message: String)
    
    /**
     * відправити бінарне повідомлення
     *
     * @param data дані
     */
    fun sendBinary(data: ByteArray)
    
    /**
     * закрити з'єднання
     */
    fun close()
    
    /**
     * встановити обробник повідомлень
     *
     * @param handler обробник
     */
    fun setMessageHandler(handler: WebSocketMessageHandler)
    
    /**
     * встановити обробник подій
     *
     * @param handler обробник
     */
    fun setEventHandler(handler: WebSocketEventHandler)
}

/**
 * представлення обробника WebSocket повідомлень
 */
interface WebSocketMessageHandler {
    /**
     * обробити текстове повідомлення
     *
     * @param message повідомлення
     */
    fun onTextMessage(message: String)
    
    /**
     * обробити бінарне повідомлення
     *
     * @param data дані
     */
    fun onBinaryMessage(data: ByteArray)
}

/**
 * представлення обробника WebSocket подій
 */
interface WebSocketEventHandler {
    /**
     * викликається при відкритті з'єднання
     */
    fun onOpen()
    
    /**
     * викликається при закритті з'єднання
     *
     * @param code код закриття
     * @param reason причина закриття
     */
    fun onClose(code: Int, reason: String)
    
    /**
     * викликається при помилці
     *
     * @param error помилка
     */
    fun onError(error: Exception)
}

/**
 * представлення базової реалізації WebSocket клієнта
 */
open class BaseWebSocketClient : WebSocketClient {
    private var messageHandler: WebSocketMessageHandler? = null
    private var eventHandler: WebSocketEventHandler? = null
    
    override fun connect(url: String) {
        // Заглушка для реалізації WebSocket з'єднання
        eventHandler?.onOpen()
    }
    
    override fun sendText(message: String) {
        // Заглушка для відправки текстового повідомлення
    }
    
    override fun sendBinary(data: ByteArray) {
        // Заглушка для відправки бінарного повідомлення
    }
    
    override fun close() {
        // Заглушка для закриття з'єднання
        eventHandler?.onClose(1000, "Normal closure")
    }
    
    override fun setMessageHandler(handler: WebSocketMessageHandler) {
        messageHandler = handler
    }
    
    override fun setEventHandler(handler: WebSocketEventHandler) {
        eventHandler = handler
    }
}

/**
 * представлення інтерфейсу для роботи з HTTP cookies
 */
interface CookieHelper {
    /**
     * розібрати cookies з заголовка
     *
     * @param header заголовок
     * @return мапа cookies
     */
    fun parseCookies(header: String): Map<String, String>
    
    /**
     * побудувати заголовок cookies
     *
     * @param cookies мапа cookies
     * @return заголовок
     */
    fun buildCookieHeader(cookies: Map<String, String>): String
    
    /**
     * встановити cookie
     *
     * @param name назва
     * @param value значення
     * @param domain домен
     * @param path шлях
     * @param maxAge максимальний вік
     * @param secure чи безпечний
     * @param httpOnly чи тільки HTTP
     * @return заголовок Set-Cookie
     */
    fun setCookie(
        name: String,
        value: String,
        domain: String? = null,
        path: String? = null,
        maxAge: Int? = null,
        secure: Boolean = false,
        httpOnly: Boolean = false
    ): String
}

/**
 * представлення базової реалізації помічника з cookies
 */
open class BaseCookieHelper : CookieHelper {
    
    override fun parseCookies(header: String): Map<String, String> {
        val cookies = mutableMapOf<String, String>()
        
        header.split(";").forEach { cookie ->
            val parts = cookie.split("=", limit = 2)
            if (parts.size == 2) {
                val name = parts[0].trim()
                val value = parts[1].trim()
                cookies[name] = value
            }
        }
        
        return cookies
    }
    
    override fun buildCookieHeader(cookies: Map<String, String>): String {
        return cookies.map { "${it.key}=${it.value}" }.joinToString("; ")
    }
    
    override fun setCookie(
        name: String,
        value: String,
        domain: String?,
        path: String?,
        maxAge: Int?,
        secure: Boolean,
        httpOnly: Boolean
    ): String {
        val cookieBuilder = StringBuilder("$name=$value")
        
        domain?.let { cookieBuilder.append("; Domain=$it") }
        path?.let { cookieBuilder.append("; Path=$it") }
        maxAge?.let { cookieBuilder.append("; Max-Age=$it") }
        if (secure) cookieBuilder.append("; Secure")
        if (httpOnly) cookieBuilder.append("; HttpOnly")
        
        return cookieBuilder.toString()
    }
}

/**
 * представлення інтерфейсу для роботи з HTTP сесіями
 */
interface HttpSessionManager {
    /**
     * створити сесію
     *
     * @param sessionId ідентифікатор сесії
     * @return сесія
     */
    fun createSession(sessionId: String): HttpSession
    
    /**
     * отримати сесію
     *
     * @param sessionId ідентифікатор сесії
     * @return сесія або null
     */
    fun getSession(sessionId: String): HttpSession?
    
    /**
     * видалити сесію
     *
     * @param sessionId ідентифікатор сесії
     */
    fun removeSession(sessionId: String)
    
    /**
     * очистити прострочені сесії
     */
    fun cleanupExpiredSessions()
}

/**
 * представлення HTTP сесії
 *
 * @property id ідентифікатор
 * @property creationTime час створення
 * @property lastAccessedTime час останнього доступу
 * @property maxInactiveInterval максимальний інтервал неактивності
 * @property attributes атрибути
 */
data class HttpSession(
    val id: String,
    val creationTime: Long,
    var lastAccessedTime: Long,
    var maxInactiveInterval: Int, // в секундах
    val attributes: MutableMap<String, Any> = mutableMapOf()
) {
    /**
     * отримати атрибут
     *
     * @param name назва
     * @return значення або null
     */
    fun getAttribute(name: String): Any? = attributes[name]
    
    /**
     * встановити атрибут
     *
     * @param name назва
     * @param value значення
     */
    fun setAttribute(name: String, value: Any) {
        attributes[name] = value
    }
    
    /**
     * видалити атрибут
     *
     * @param name назва
     * @return значення або null
     */
    fun removeAttribute(name: String): Any? = attributes.remove(name)
    
    /**
     * перевірити, чи сесія прострочена
     *
     * @return true, якщо сесія прострочена
     */
    fun isExpired(): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastAccessedTime) > (maxInactiveInterval * 1000L)
    }
}

/**
 * представлення базової реалізації менеджера HTTP сесій
 */
open class BaseHttpSessionManager : HttpSessionManager {
    private val sessions = mutableMapOf<String, HttpSession>()
    
    override fun createSession(sessionId: String): HttpSession {
        val session = HttpSession(
            sessionId,
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            1800 // 30 хвилин за замовчуванням
        )
        sessions[sessionId] = session
        return session
    }
    
    override fun getSession(sessionId: String): HttpSession? {
        val session = sessions[sessionId]
        return if (session != null && !session.isExpired()) {
            session.lastAccessedTime = System.currentTimeMillis()
            session
        } else {
            if (session != null) {
                sessions.remove(sessionId)
            }
            null
        }
    }
    
    override fun removeSession(sessionId: String) {
        sessions.remove(sessionId)
    }
    
    override fun cleanupExpiredSessions() {
        val expiredSessions = sessions.filter { it.value.isExpired() }.map { it.key }
        expiredSessions.forEach { sessions.remove(it) }
    }
}

/**
 * представлення інтерфейсу для роботи з HTTP аутентифікацією
 */
interface HttpAuthenticator {
    /**
     * створити Basic Auth заголовок
     *
     * @param username ім'я користувача
     * @param password пароль
     * @return заголовок
     */
    fun createBasicAuthHeader(username: String, password: String): String
    
    /**
     * створити Bearer Token заголовок
     *
     * @param token токен
     * @return заголовок
     */
    fun createBearerTokenHeader(token: String): String
    
    /**
     * перевірити Basic Auth заголовок
     *
     * @param header заголовок
     * @return пара (ім'я користувача, пароль) або null
     */
    fun parseBasicAuthHeader(header: String): Pair<String, String>?
}

/**
 * представлення базової реалізації HTTP аутентифікатора
 */
open class BaseHttpAuthenticator : HttpAuthenticator {
    
    override fun createBasicAuthHeader(username: String, password: String): String {
        val credentials = "$username:$password"
        val encodedCredentials = java.util.Base64.getEncoder().encodeToString(credentials.toByteArray())
        return "Basic $encodedCredentials"
    }
    
    override fun createBearerTokenHeader(token: String): String {
        return "Bearer $token"
    }
    
    override fun parseBasicAuthHeader(header: String): Pair<String, String>? {
        if (!header.startsWith("Basic ")) return null
        
        return try {
            val encodedCredentials = header.substring(6) // Пропустити "Basic "
            val decodedCredentials = String(java.util.Base64.getDecoder().decode(encodedCredentials))
            val parts = decodedCredentials.split(":", limit = 2)
            if (parts.size == 2) {
                Pair(parts[0], parts[1])
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * представлення інтерфейсу для роботи з HTTP кешем
 */
interface HttpCache {
    /**
     * отримати закешований вміст
     *
     * @param url URL
     * @return вміст або null
     */
    fun get(url: String): CachedHttpResponse?
    
    /**
     * зберегти вміст у кеш
     *
     * @param url URL
     * @param response відповідь
     */
    fun put(url: String, response: CachedHttpResponse)
    
    /**
     * видалити з кешу
     *
     * @param url URL
     */
    fun remove(url: String)
    
    /**
     * очистити кеш
     */
    fun clear()
    
    /**
     * очистити прострочені записи
     */
    fun cleanupExpired()
}

/**
 * представлення закешованої HTTP відповіді
 *
 * @property statusCode код статусу
 * @property headers заголовки
 * @property body тіло
 * @property timestamp мітка часу
 * @property ttl час життя в мілісекундах
 */
data class CachedHttpResponse(
    val statusCode: Int,
    val headers: Map<String, String>,
    val body: String?,
    val timestamp: Long,
    val ttl: Long
) {
    /**
     * перевірити, чи запис прострочений
     *
     * @return true, якщо запис прострочений
     */
    fun isExpired(): Boolean {
        return (System.currentTimeMillis() - timestamp) > ttl
    }
}

/**
 * представлення базової реалізації HTTP кешу