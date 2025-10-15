/**
 * xsparkyproject - розширена бібліотека для kotlin
 *
 * Copyright 2025 Андрій Будильников
 */

package com.sparky.xsparkyproject.utils

import java.io.*
import java.net.*
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel
import java.nio.charset.Charset
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.*
import kotlin.math.min

/**
 * утилітарний клас для роботи з мережею
 *
 * @author андрій будильников
 * @since 1.0.0
 */
@Suppress("unused")
class NetworkUtils {
    
    companion object {
        // стандартні порти
        const val HTTP_PORT = 80
        const val HTTPS_PORT = 443
        const val FTP_PORT = 21
        const val SSH_PORT = 22
        const val TELNET_PORT = 23
        const val SMTP_PORT = 25
        const val DNS_PORT = 53
        const val DHCP_PORT = 67
        const val TFTP_PORT = 69
        const val HTTP_ALT_PORT = 8080
        const val HTTPS_ALT_PORT = 8443
        
        // стандартні таймаути
        const val DEFAULT_CONNECT_TIMEOUT = 5000
        const val DEFAULT_READ_TIMEOUT = 10000
        const val DEFAULT_SOCKET_TIMEOUT = 30000
        
        // розміри буферів
        const val DEFAULT_BUFFER_SIZE = 8192
        const val SMALL_BUFFER_SIZE = 1024
        const val LARGE_BUFFER_SIZE = 32768
        
        // протоколи
        const val PROTOCOL_HTTP = "http"
        const val PROTOCOL_HTTPS = "https"
        const val PROTOCOL_FTP = "ftp"
        const val PROTOCOL_TCP = "tcp"
        const val PROTOCOL_UDP = "udp"
        
        // коди відповідей http
        const val HTTP_OK = 200
        const val HTTP_CREATED = 201
        const val HTTP_ACCEPTED = 202
        const val HTTP_NO_CONTENT = 204
        const val HTTP_MOVED_PERMANENTLY = 301
        const val HTTP_FOUND = 302
        const val HTTP_NOT_MODIFIED = 304
        const val HTTP_BAD_REQUEST = 400
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
        const val HTTP_NOT_FOUND = 404
        const val HTTP_METHOD_NOT_ALLOWED = 405
        const val HTTP_REQUEST_TIMEOUT = 408
        const val HTTP_INTERNAL_SERVER_ERROR = 500
        const val HTTP_NOT_IMPLEMENTED = 501
        const val HTTP_BAD_GATEWAY = 502
        const val HTTP_SERVICE_UNAVAILABLE = 503
    }
    
    // базові функції для роботи з url
    
    /**
     * перевіряє, чи є рядок дійсним url
     *
     * @param urlString рядок для перевірки
     * @return true якщо рядок є дійсним url
     */
    fun isValidUrl(urlString: String): Boolean {
        return try {
            URL(urlString)
            true
        } catch (e: MalformedURLException) {
            false
        }
    }
    
    /**
     * отримує протокол з url
     *
     * @param urlString url
     * @return протокол або порожній рядок якщо не вдалося розпарсити
     */
    fun getUrlProtocol(urlString: String): String {
        return try {
            URL(urlString).protocol
        } catch (e: MalformedURLException) {
            ""
        }
    }
    
    /**
     * отримує хост з url
     *
     * @param urlString url
     * @return хост або порожній рядок якщо не вдалося розпарсити
     */
    fun getUrlHost(urlString: String): String {
        return try {
            URL(urlString).host
        } catch (e: MalformedURLException) {
            ""
        }
    }
    
    /**
     * отримує порт з url
     *
     * @param urlString url
     * @return порт або -1 якщо не вдалося розпарсити
     */
    fun getUrlPort(urlString: String): Int {
        return try {
            URL(urlString).port
        } catch (e: MalformedURLException) {
            -1
        }
    }
    
    /**
     * отримує шлях з url
     *
     * @param urlString url
     * @return шлях або порожній рядок якщо не вдалося розпарсити
     */
    fun getUrlPath(urlString: String): String {
        return try {
            URL(urlString).path
        } catch (e: MalformedURLException) {
            ""
        }
    }
    
    /**
     * отримує query з url
     *
     * @param urlString url
     * @return query або порожній рядок якщо не вдалося розпарсити
     */
    fun getUrlQuery(urlString: String): String {
        return try {
            URL(urlString).query
        } catch (e: MalformedURLException) {
            ""
        }
    }
    
    /**
     * отримує fragment з url
     *
     * @param urlString url
     * @return fragment або порожній рядок якщо не вдалося розпарсити
     */
    fun getUrlFragment(urlString: String): String {
        return try {
            URL(urlString).ref
        } catch (e: MalformedURLException) {
            ""
        }
    }
    
    /**
     * будує url з компонентів
     *
     * @param protocol протокол
     * @param host хост
     * @param port порт
     * @param path шлях
     * @param query query
     * @param fragment fragment
     * @return побудований url
     */
    fun buildUrl(protocol: String, host: String, port: Int = -1, path: String = "", query: String = "", fragment: String = ""): String {
        val portPart = if (port > 0) ":$port" else ""
        val pathPart = if (path.isNotEmpty() && !path.startsWith("/")) "/$path" else path
        val queryPart = if (query.isNotEmpty()) "?$query" else ""
        val fragmentPart = if (fragment.isNotEmpty()) "#$fragment" else ""
        
        return "$protocol://$host$portPart$pathPart$queryPart$fragmentPart"
    }
    
    // функції для роботи з ip адресами
    
    /**
     * перевіряє, чи є рядок дійсною ipv4 адресою
     *
     * @param ipAddress рядок для перевірки
     * @return true якщо рядок є дійсною ipv4 адресою
     */
    fun isValidIPv4Address(ipAddress: String): Boolean {
        return try {
            val parts = ipAddress.split(".")
            if (parts.size != 4) return false
            
            for (part in parts) {
                val num = part.toInt()
                if (num < 0 || num > 255) return false
                // перевіряємо, чи немає зайвих нулів
                if (part.length > 1 && part.startsWith("0")) return false
            }
            
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * перевіряє, чи є рядок дійсною ipv6 адресою
     *
     * @param ipAddress рядок для перевірки
     * @return true якщо рядок є дійсною ipv6 адресою
     */
    fun isValidIPv6Address(ipAddress: String): Boolean {
        return try {
            InetAddress.getByName(ipAddress)
            ipAddress.contains(":")
        } catch (e: UnknownHostException) {
            false
        }
    }
    
    /**
     * перевіряє, чи є рядок дійсною ip адресою (ipv4 або ipv6)
     *
     * @param ipAddress рядок для перевірки
     * @return true якщо рядок є дійсною ip адресою
     */
    fun isValidIPAddress(ipAddress: String): Boolean {
        return isValidIPv4Address(ipAddress) || isValidIPv6Address(ipAddress)
    }
    
    /**
     * конвертує ipv4 адресу з рядка в ціле число
     *
     * @param ipAddress ipv4 адреса у вигляді рядка
     * @return ipv4 адреса у вигляді цілого числа
     */
    fun ipv4ToLong(ipAddress: String): Long {
        if (!isValidIPv4Address(ipAddress)) return -1L
        
        val parts = ipAddress.split(".")
        var result = 0L
        for (i in parts.indices) {
            result = (result shl 8) + parts[i].toLong()
        }
        return result
    }
    
    /**
     * конвертує ipv4 адресу з цілого числа в рядок
     *
     * @param ipAddress ipv4 адреса у вигляді цілого числа
     * @return ipv4 адреса у вигляді рядка
     */
    fun longToIPv4(ipAddress: Long): String {
        if (ipAddress < 0 || ipAddress > 0xFFFFFFFFL) return ""
        
        return "${(ipAddress shr 24) and 0xFF}.${(ipAddress shr 16) and 0xFF}.${(ipAddress shr 8) and 0xFF}.${ipAddress and 0xFF}"
    }
    
    /**
     * отримує локальні ip адреси
     *
     * @return список локальних ip адрес
     */
    fun getLocalIPAddresses(): List<String> {
        val addresses = mutableListOf<String>()
        
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                if (networkInterface.isUp && !networkInterface.isLoopback) {
                    val interfaceAddresses = networkInterface.interfaceAddresses
                    for (interfaceAddress in interfaceAddresses) {
                        val address = interfaceAddress.address
                        if (address is Inet4Address) {
                            addresses.add(address.hostAddress)
                        }
                    }
                }
            }
        } catch (e: SocketException) {
            // ігноруємо помилки
        }
        
        return addresses
    }
    
    /**
     * отримує локальну ipv4 адресу
     *
     * @return локальна ipv4 адреса або порожній рядок
     */
    fun getLocalIPv4Address(): String {
        val addresses = getLocalIPAddresses()
        return addresses.firstOrNull { it != "127.0.0.1" } ?: ""
    }
    
    /**
     * перевіряє, чи ip адреса належить до приватної мережі
     *
     * @param ipAddress ip адреса
     * @return true якщо адреса належить до приватної мережі
     */
    fun isPrivateIPAddress(ipAddress: String): Boolean {
        if (!isValidIPv4Address(ipAddress)) return false
        
        val parts = ipAddress.split(".").map { it.toInt() }
        val first = parts[0]
        val second = parts[1]
        
        // 10.0.0.0 - 10.255.255.255
        if (first == 10) return true
        
        // 172.16.0.0 - 172.31.255.255
        if (first == 172 && second >= 16 && second <= 31) return true
        
        // 192.168.0.0 - 192.168.255.255
        if (first == 192 && second == 168) return true
        
        return false
    }
    
    /**
     * перевіряє, чи ip адреса є петльовою (loopback)
     *
     * @param ipAddress ip адреса
     * @return true якщо адреса є петльовою
     */
    fun isLoopbackIPAddress(ipAddress: String): Boolean {
        return try {
            val address = InetAddress.getByName(ipAddress)
            address.isLoopbackAddress
        } catch (e: UnknownHostException) {
            false
        }
    }
    
    // функції для роботи з http запитами
    
    /**
     * виконує http get запит
     *
     * @param url url для запиту
     * @param connectTimeout таймаут підключення в мілісекундах
     * @param readTimeout таймаут читання в мілісекундах
     * @param headers заголовки запиту
     * @return відповідь або null якщо запит не вдався
     */
    fun httpGet(
        url: String,
        connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT,
        readTimeout: Int = DEFAULT_READ_TIMEOUT,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = connectTimeout
            connection.readTimeout = readTimeout
            
            // встановлюємо заголовки
            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }
            
            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }
            
            val responseHeaders = mutableMapOf<String, String>()
            connection.headerFields.forEach { (key, value) ->
                if (key != null && value != null && value.isNotEmpty()) {
                    responseHeaders[key] = value[0]
                }
            }
            
            connection.disconnect()
            
            HttpResponse(responseCode, responseBody, responseHeaders)
        } catch (e: IOException) {
            null
        }
    }
    
    /**
     * виконує http post запит
     *
     * @param url url для запиту
     * @param data дані для відправки
     * @param contentType тип вмісту
     * @param connectTimeout таймаут підключення в мілісекундах
     * @param readTimeout таймаут читання в мілісекундах
     * @param headers заголовки запиту
     * @return відповідь або null якщо запит не вдався
     */
    fun httpPost(
        url: String,
        data: String,
        contentType: String = "application/json",
        connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT,
        readTimeout: Int = DEFAULT_READ_TIMEOUT,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = connectTimeout
            connection.readTimeout = readTimeout
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", contentType)
            
            // встановлюємо заголовки
            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }
            
            // відправляємо дані
            connection.outputStream.use { outputStream ->
                outputStream.write(data.toByteArray(Charsets.UTF_8))
            }
            
            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }
            
            val responseHeaders = mutableMapOf<String, String>()
            connection.headerFields.forEach { (key, value) ->
                if (key != null && value != null && value.isNotEmpty()) {
                    responseHeaders[key] = value[0]
                }
            }
            
            connection.disconnect()
            
            HttpResponse(responseCode, responseBody, responseHeaders)
        } catch (e: IOException) {
            null
        }
    }
    
    /**
     * виконує http put запит
     *
     * @param url url для запиту
     * @param data дані для відправки
     * @param contentType тип вмісту
     * @param connectTimeout таймаут підключення в мілісекундах
     * @param readTimeout таймаут читання в мілісекундах
     * @param headers заголовки запиту
     * @return відповідь або null якщо запит не вдався
     */
    fun httpPut(
        url: String,
        data: String,
        contentType: String = "application/json",
        connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT,
        readTimeout: Int = DEFAULT_READ_TIMEOUT,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"
            connection.connectTimeout = connectTimeout
            connection.readTimeout = readTimeout
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", contentType)
            
            // встановлюємо заголовки
            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }
            
            // відправляємо дані
            connection.outputStream.use { outputStream ->
                outputStream.write(data.toByteArray(Charsets.UTF_8))
            }
            
            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }
            
            val responseHeaders = mutableMapOf<String, String>()
            connection.headerFields.forEach { (key, value) ->
                if (key != null && value != null && value.isNotEmpty()) {
                    responseHeaders[key] = value[0]
                }
            }
            
            connection.disconnect()
            
            HttpResponse(responseCode, responseBody, responseHeaders)
        } catch (e: IOException) {
            null
        }
    }
    
    /**
     * виконує http delete запит
     *
     * @param url url для запиту
     * @param connectTimeout таймаут підключення в мілісекундах
     * @param readTimeout таймаут читання в мілісекундах
     * @param headers заголовки запиту
     * @return відповідь або null якщо запит не вдався
     */
    fun httpDelete(
        url: String,
        connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT,
        readTimeout: Int = DEFAULT_READ_TIMEOUT,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "DELETE"
            connection.connectTimeout = connectTimeout
            connection.readTimeout = readTimeout
            
            // встановлюємо заголовки
            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }
            
            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }
            
            val responseHeaders = mutableMapOf<String, String>()
            connection.headerFields.forEach { (key, value) ->
                if (key != null && value != null && value.isNotEmpty()) {
                    responseHeaders[key] = value[0]
                }
            }
            
            connection.disconnect()
            
            HttpResponse(responseCode, responseBody, responseHeaders)
        } catch (e: IOException) {
            null
        }
    }
    
    /**
     * представлення http відповіді
     *
     * @property statusCode код статусу
     * @property body тіло відповіді
     * @property headers заголовки відповіді
     */
    data class HttpResponse(val statusCode: Int, val body: String, val headers: Map<String, String>) {
        
        /**
         * перевіряє, чи запит був успішним
         *
         * @return true якщо код статусу в діапазоні 200-299
         */
        fun isSuccessful(): Boolean {
            return statusCode in 200..299
        }
        
        /**
         * отримує значення заголовка
         *
         * @param name ім'я заголовка
         * @return значення заголовка або null
         */
        fun getHeader(name: String): String? {
            return headers[name]
        }
        
        /**
         * перевіряє, чи існує заголовок
         *
         * @param name ім'я заголовка
         * @return true якщо заголовок існує
         */
        fun hasHeader(name: String): Boolean {
            return headers.containsKey(name)
        }
    }
    
    // функції для роботи з tcp сокетами
    
    /**
     * представлення tcp клієнта
     *
     * @property host хост
     * @property port порт
     * @property connectTimeout таймаут підключення
     */
    class TcpClient(private val host: String, private val port: Int, private val connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT) {
        private var socket: Socket? = null
        private var inputStream: InputStream? = null
        private var outputStream: OutputStream? = null
        
        /**
         * підключається до сервера
         *
         * @return true якщо підключення вдалося
         */
        fun connect(): Boolean {
            return try {
                socket = Socket()
                socket?.connect(InetSocketAddress(host, port), connectTimeout)
                inputStream = socket?.getInputStream()
                outputStream = socket?.getOutputStream()
                true
            } catch (e: IOException) {
                false
            }
        }
        
        /**
         * відправляє дані серверу
         *
         * @param data дані для відправки
         * @return true якщо відправка вдалася
         */
        fun send(data: ByteArray): Boolean {
            return try {
                outputStream?.write(data)
                outputStream?.flush()
                true
            } catch (e: IOException) {
                false
            }
        }
        
        /**
         * відправляє текст серверу
         *
         * @param text текст для відправки
         * @param charset кодування
         * @return true якщо відправка вдалася
         */
        fun sendText(text: String, charset: Charset = Charsets.UTF_8): Boolean {
            return send(text.toByteArray(charset))
        }
        
        /**
         * отримує дані від сервера
         *
         * @param maxLength максимальна довжина даних для отримання
         * @return отримані дані або null якщо отримання не вдалося
         */
        fun receive(maxLength: Int = DEFAULT_BUFFER_SIZE): ByteArray? {
            return try {
                val buffer = ByteArray(minOf(maxLength, DEFAULT_BUFFER_SIZE))
                val bytesRead = inputStream?.read(buffer)
                if (bytesRead != null && bytesRead > 0) {
                    buffer.copyOf(bytesRead)
                } else {
                    null
                }
            } catch (e: IOException) {
                null
            }
        }
        
        /**
         * отримує текст від сервера
         *
         * @param maxLength максимальна довжина тексту для отримання
         * @param charset кодування
         * @return отриманий текст або null якщо отримання не вдалося
         */
        fun receiveText(maxLength: Int = DEFAULT_BUFFER_SIZE, charset: Charset = Charsets.UTF_8): String? {
            val data = receive(maxLength)
            return data?.toString(charset)
        }
        
        /**
         * закриває з'єднання
         */
        fun close() {
            try {
                inputStream?.close()
                outputStream?.close()
                socket?.close()
            } catch (e: IOException) {
                // ігноруємо помилки при закритті
            } finally {
                inputStream = null
                outputStream = null
                socket = null
            }
        }
        
        /**
         * перевіряє, чи з'єднання відкрите
         *
         * @return true якщо з'єднання відкрите
         */
        fun isConnected(): Boolean {
            return socket?.isConnected == true && !socket?.isClosed == true
        }
    }
    
    /**
     * створює tcp клієнта
     *
     * @param host хост
     * @param port порт
     * @param connectTimeout таймаут підключення
     * @return tcp клієнт
     */
    fun createTcpClient(host: String, port: Int, connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT): TcpClient {
        return TcpClient(host, port, connectTimeout)
    }
    
    /**
     * представлення tcp сервера
     *
     * @property port порт
     * @property backlog розмір черги підключень
     */
    class TcpServer(private val port: Int, private val backlog: Int = 50) {
        private var serverSocket: ServerSocket? = null
        private var isRunning = false
        private val clientHandlers = mutableListOf<Thread>()
        
        /**
         * запускає сервер
         *
         * @param clientHandler обробник клієнтських з'єднань
         * @return true якщо запуск вдалося
         */
        fun start(clientHandler: (Socket) -> Unit): Boolean {
            return try {
                serverSocket = ServerSocket(port, backlog)
                isRunning = true
                
                // запускаємо потік для прийняття з'єднань
                Thread {
                    while (isRunning) {
                        try {
                            val clientSocket = serverSocket?.accept()
                            if (clientSocket != null) {
                                val handlerThread = Thread {
                                    try {
                                        clientHandler(clientSocket)
                                    } catch (e: Exception) {
                                        // ігноруємо помилки обробки клієнта
                                    } finally {
                                        try {
                                            clientSocket.close()
                                        } catch (e: IOException) {
                                            // ігноруємо помилки закриття
                                        }
                                    }
                                }
                                clientHandlers.add(handlerThread)
                                handlerThread.start()
                            }
                        } catch (e: IOException) {
                            if (isRunning) {
                                // якщо сервер все ще повинен працювати, але сталася помилка
                                break
                            }
                        }
                    }
                }.start()
                
                true
            } catch (e: IOException) {
                false
            }
        }
        
        /**
         * зупиняє сервер
         */
        fun stop() {
            isRunning = false
            
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                // ігноруємо помилки закриття
            }
            
            // чекаємо завершення всіх обробників клієнтів
            clientHandlers.forEach { it.join(1000) } // чекаємо максимум 1 секунду
            clientHandlers.clear()
        }
        
        /**
         * перевіряє, чи сервер працює
         *
         * @return true якщо сервер працює
         */
        fun isRunning(): Boolean {
            return isRunning
        }
    }
    
    /**
     * створює tcp сервер
     *
     * @param port порт
     * @param backlog розмір черги підключень
     * @return tcp сервер
     */
    fun createTcpServer(port: Int, backlog: Int = 50): TcpServer {
        return TcpServer(port, backlog)
    }
    
    // функції для роботи з udp сокетами
    
    /**
     * представлення udp клієнта
     *
     * @property host хост
     * @property port порт
     */
    class UdpClient(private val host: String, private val port: Int) {
        private var socket: DatagramSocket? = null
        private var targetAddress: InetAddress? = null
        
        init {
            try {
                targetAddress = InetAddress.getByName(host)
            } catch (e: UnknownHostException) {
                // ігноруємо помилку ініціалізації адреси
            }
        }
        
        /**
         * підключається до сервера
         *
         * @return true якщо підключення вдалося
         */
        fun connect(): Boolean {
            return try {
                socket = DatagramSocket()
                true
            } catch (e: SocketException) {
                false
            }
        }
        
        /**
         * відправляє дані серверу
         *
         * @param data дані для відправки
         * @return true якщо відправка вдалася
         */
        fun send(data: ByteArray): Boolean {
            return try {
                val packet = DatagramPacket(data, data.size, targetAddress, port)
                socket?.send(packet)
                true
            } catch (e: IOException) {
                false
            }
        }
        
        /**
         * відправляє текст серверу
         *
         * @param text текст для відправки
         * @param charset кодування
         * @return true якщо відправка вдалася
         */
        fun sendText(text: String, charset: Charset = Charsets.UTF_8): Boolean {
            return send(text.toByteArray(charset))
        }
        
        /**
         * отримує дані від сервера
         *
         * @param maxLength максимальна довжина даних для отримання
         * @return отримані дані або null якщо отримання не вдалося
         */
        fun receive(maxLength: Int = DEFAULT_BUFFER_SIZE): UdpPacket? {
            return try {
                val buffer = ByteArray(minOf(maxLength, DEFAULT_BUFFER_SIZE))
                val packet = DatagramPacket(buffer, buffer.size)
                socket?.receive(packet)
                
                UdpPacket(
                    packet.data.copyOf(packet.length),
                    packet.address.hostAddress,
                    packet.port
                )
            } catch (e: IOException) {
                null
            }
        }
        
        /**
         * отримує текст від сервера
         *
         * @param maxLength максимальна довжина тексту для отримання
         * @param charset кодування
         * @return отриманий текст або null якщо отримання не вдалося
         */
        fun receiveText(maxLength: Int = DEFAULT_BUFFER_SIZE, charset: Charset = Charsets.UTF_8): UdpTextPacket? {
            val packet = receive(maxLength)
            return packet?.let {
                UdpTextPacket(
                    it.data.toString(charset),
                    it.host,
                    it.port
                )
            }
        }
        
        /**
         * закриває з'єднання
         */
        fun close() {
            socket?.close()
            socket = null
        }
        
        /**
         * перевіряє, чи з'єднання відкрите
         *
         * @return true якщо з'єднання відкрите
         */
        fun isConnected(): Boolean {
            return socket?.isClosed == false
        }
    }
    
    /**
     * представлення udp пакета
     *
     * @property data дані
     * @property host хост відправника
     * @property port порт відправника
     */
    data class UdpPacket(val data: ByteArray, val host: String, val port: Int)
    
    /**
     * представлення udp текстового пакета
     *
     * @property text текст
     * @property host хост відправника
     * @property port порт відправника
     */
    data class UdpTextPacket(val text: String, val host: String, val port: Int)
    
    /**
     * створює udp клієнта
     *
     * @param host хост
     * @param port порт
     * @return udp клієнт
     */
    fun createUdpClient(host: String, port: Int): UdpClient {
        return UdpClient(host, port)
    }
    
    /**
     * представлення udp сервера
     *
     * @property port порт
     */
    class UdpServer(private val port: Int) {
        private var socket: DatagramSocket? = null
        private var isRunning = false
        
        /**
         * запускає сервер
         *
         * @param packetHandler обробник отриманих пакетів
         * @return true якщо запуск вдалося
         */
        fun start(packetHandler: (UdpPacket) -> Unit): Boolean {
            return try {
                socket = DatagramSocket(port)
                isRunning = true
                
                // запускаємо потік для прийняття пакетів
                Thread {
                    while (isRunning) {
                        try {
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            val packet = DatagramPacket(buffer, buffer.size)
                            socket?.receive(packet)
                            
                            val udpPacket = UdpPacket(
                                packet.data.copyOf(packet.length),
                                packet.address.hostAddress,
                                packet.port
                            )
                            
                            packetHandler(udpPacket)
                        } catch (e: IOException) {
                            if (isRunning) {
                                // якщо сервер все ще повинен працювати, але сталася помилка
                                break
                            }
                        }
                    }
                }.start()
                
                true
            } catch (e: SocketException) {
                false
            }
        }
        
        /**
         * зупиняє сервер
         */
        fun stop() {
            isRunning = false
            socket?.close()
            socket = null
        }
        
        /**
         * перевіряє, чи сервер працює
         *
         * @return true якщо сервер працює
         */
        fun isRunning(): Boolean {
            return isRunning
        }
    }
    
    /**
     * створює udp сервер
     *
     * @param port порт
     * @return udp сервер
     */
    fun createUdpServer(port: Int): UdpServer {
        return UdpServer(port)
    }
    
    // функції для роботи з ssl/tls
    
    /**
     * представлення ssl клієнта
     *
     * @property host хост
     * @property port порт
     * @property connectTimeout таймаут підключення
     */
    class SslClient(private val host: String, private val port: Int, private val connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT) {
        private var socket: SSLSocket? = null
        private var inputStream: InputStream? = null
        private var outputStream: OutputStream? = null
        
        /**
         * підключається до сервера
         *
         * @return true якщо підключення вдалося
         */
        fun connect(): Boolean {
            return try {
                val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
                socket = factory.createSocket() as SSLSocket
                socket?.connect(InetSocketAddress(host, port), connectTimeout)
                socket?.startHandshake()
                inputStream = socket?.getInputStream()
                outputStream = socket?.getOutputStream()
                true
            } catch (e: Exception) {
                false
            }
        }
        
        /**
         * відправляє дані серверу
         *
         * @param data дані для відправки
         * @return true якщо відправка вдалася
         */
        fun send(data: ByteArray): Boolean {
            return try {
                outputStream?.write(data)
                outputStream?.flush()
                true
            } catch (e: IOException) {
                false
            }
        }
        
        /**
         * відправляє текст серверу
         *
         * @param text текст для відправки
         * @param charset кодування
         * @return true якщо відправка вдалася
         */
        fun sendText(text: String, charset: Charset = Charsets.UTF_8): Boolean {
            return send(text.toByteArray(charset))
        }
        
        /**
         * отримує дані від сервера
         *
         * @param maxLength максимальна довжина даних для отримання
         * @return отримані дані або null якщо отримання не вдалося
         */
        fun receive(maxLength: Int = DEFAULT_BUFFER_SIZE): ByteArray? {
            return try {
                val buffer = ByteArray(minOf(maxLength, DEFAULT_BUFFER_SIZE))
                val bytesRead = inputStream?.read(buffer)
                if (bytesRead != null && bytesRead > 0) {
                    buffer.copyOf(bytesRead)
                } else {
                    null
                }
            } catch (e: IOException) {
                null
            }
        }
        
        /**
         * отримує текст від сервера
         *
         * @param maxLength максимальна довжина тексту для отримання
         * @param charset кодування
         * @return отриманий текст або null якщо отримання не вдалося
         */
        fun receiveText(maxLength: Int = DEFAULT_BUFFER_SIZE, charset: Charset = Charsets.UTF_8): String? {
            val data = receive(maxLength)
            return data?.toString(charset)
        }
        
        /**
         * закриває з'єднання
         */
        fun close() {
            try {
                inputStream?.close()
                outputStream?.close()
                socket?.close()
            } catch (e: IOException) {
                // ігноруємо помилки при закритті
            } finally {
                inputStream = null
                outputStream = null
                socket = null
            }
        }
        
        /**
         * перевіряє, чи з'єднання відкрите
         *
         * @return true якщо з'єднання відкрите
         */
        fun isConnected(): Boolean {
            return socket?.isConnected == true && !socket?.isClosed == true
        }
        
        /**
         * отримує інформацію про ssl сертифікат
         *
         * @return інформація про сертифікат або null
         */
        fun getCertificateInfo(): String? {
            return try {
                val session = socket?.session
                val cert = session?.peerCertificates?.firstOrNull()
                cert?.toString()
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * створює ssl клієнта
     *
     * @param host хост
     * @param port порт
     * @param connectTimeout таймаут підключення
     * @return ssl клієнт
     */
    fun createSslClient(host: String, port: Int, connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT): SslClient {
        return SslClient(host, port, connectTimeout)
    }
    
    // функції для роботи з мережевими інтерфейсами
    
    /**
     * представлення мережевого інтерфейсу
     *
     * @property name ім'я інтерфейсу
     * @property displayName відображуване ім'я
     * @property addresses ip адреси
     * @property isUp інтерфейс активний
     * @property isLoopback інтерфейс є петльовим
     * @property isVirtual інтерфейс є віртуальним
     * @property macAddress mac адреса
     */
    data class NetworkInterfaceInfo(
        val name: String,
        val displayName: String,
        val addresses: List<String>,
        val isUp: Boolean,
        val isLoopback: Boolean,
        val isVirtual: Boolean,
        val macAddress: String
    )
    
    /**
     * отримує інформацію про всі мережеві інтерфейси
     *
     * @return список інформації про мережеві інтерфейси
     */
    fun getNetworkInterfaces(): List<NetworkInterfaceInfo> {
        val interfaces = mutableListOf<NetworkInterfaceInfo>()
        
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                
                val addresses = mutableListOf<String>()
                val interfaceAddresses = networkInterface.interfaceAddresses
                for (interfaceAddress in interfaceAddresses) {
                    addresses.add(interfaceAddress.address.hostAddress)
                }
                
                val macAddress = try {
                    val mac = networkInterface.hardwareAddress
                    if (mac != null) {
                        mac.joinToString(":") { "%02x".format(it) }
                    } else {
                        ""
                    }
                } catch (e: SocketException) {
                    ""
                }
                
                interfaces.add(
                    NetworkInterfaceInfo(
                        networkInterface.name,
                        networkInterface.displayName,
                        addresses,
                        networkInterface.isUp,
                        networkInterface.isLoopback,
                        networkInterface.isVirtual,
                        macAddress
                    )
                )
            }
        } catch (e: SocketException) {
            // ігноруємо помилки
        }
        
        return interfaces
    }
    
    /**
     * отримує інформацію про активні мережеві інтерфейси
     *
     * @return список інформації про активні мережеві інтерфейси
     */
    fun getActiveNetworkInterfaces(): List<NetworkInterfaceInfo> {
        return getNetworkInterfaces().filter { it.isUp && !it.isLoopback }
    }
    
    /**
     * отримує mac адресу мережевого інтерфейсу
     *
     * @param interfaceName ім'я інтерфейсу
     * @return mac адреса або порожній рядок
     */
    fun getMacAddress(interfaceName: String): String {
        return try {
            val networkInterface = NetworkInterface.getByName(interfaceName)
            val mac = networkInterface?.hardwareAddress
            if (mac != null) {
                mac.joinToString(":") { "%02x".format(it) }
            } else {
                ""
            }
        } catch (e: SocketException) {
            ""
        }
    }
    
    // функції для роботи з dns
    
    /**
     * виконує dns lookup для хоста
     *
     * @param host хост
     * @return список ip адрес або порожній список якщо lookup не вдався
     */
    fun dnsLookup(host: String): List<String> {
        return try {
            val addresses = InetAddress.getAllByName(host)
            addresses.map { it.hostAddress }
        } catch (e: UnknownHostException) {
            emptyList()
        }
    }
    
    /**
     * виконує зворотний dns lookup для ip адреси
     *
     * @param ipAddress ip адреса
     * @return ім'я хоста або порожній рядок якщо lookup не вдався
     */
    fun reverseDnsLookup(ipAddress: String): String {
        return try {
            val address = InetAddress.getByName(ipAddress)
            address.hostName
        } catch (e: UnknownHostException) {
            ""
        }
    }
    
    /**
     * перевіряє, чи хост доступний через ping
     *
     * @param host хост
     * @param timeout таймаут в мілісекундах
     * @return true якщо хост доступний
     */
    fun pingHost(host: String, timeout: Int = 5000): Boolean {
        return try {
            val address = InetAddress.getByName(host)
            address.isReachable(timeout)
        } catch (e: IOException) {
            false
        }
    }
    
    // функції для роботи з мережевими утилітами
    
    /**
     * представлення мережевого сканера портів
     *
     * @property host хост для сканування
     * @property startPort початковий порт
     * @property endPort кінцевий порт
     */
    class PortScanner(private val host: String, private val startPort: Int, private val endPort: Int) {
        private val openPorts = mutableListOf<Int>()
        private val executorService = Executors.newFixedThreadPool(50)
        private val semaphore = Semaphore(1000) // обмежуємо кількість одночасних з'єднань
        
        /**
         * сканує порти
         *
         * @param timeout таймаут для кожного з'єднання в мілісекундах
         * @return список відкритих портів
         */
        fun scan(timeout: Int = 1000): List<Int> {
            openPorts.clear()
            
            val futures = mutableListOf<Future<*>>()
            
            for (port in startPort..endPort) {
                semaphore.acquire()
                
                val future = executorService.submit {
                    try {
                        val socket = Socket()
                        socket.connect(InetSocketAddress(host, port), timeout)
                        socket.close()
                        synchronized(openPorts) {
                            openPorts.add(port)
                        }
                    } catch (e: IOException) {
                        // порт закритий або недоступний
                    } finally {
                        semaphore.release()
                    }
                }
                
                futures.add(future)
            }
            
            // чекаємо завершення всіх задач
            for (future in futures) {
                try {
                    future.get(5, TimeUnit.SECONDS)
                } catch (e: Exception) {
                    // ігноруємо помилки
                }
            }
            
            return openPorts.sorted()
        }
        
        /**
         * зупиняє сканування
         */
        fun stop() {
            executorService.shutdownNow()
        }
    }
    
    /**
     * створює сканер портів
     *
     * @param host хост для сканування
     * @param startPort початковий порт
     * @param endPort кінцевий порт
     * @return сканер портів
     */
    fun createPortScanner(host: String, startPort: Int, endPort: Int): PortScanner {
        return PortScanner(host, startPort, endPort)
    }
    
    /**
     * представлення мережевого монітора
     *
     * @property interfaceName ім'я мережевого інтерфейсу
     */
    class NetworkMonitor(private val interfaceName: String) {
        private var isMonitoring = false
        private var previousStats: NetworkStats? = null
        private val listeners = mutableListOf<NetworkStatsListener>()
        
        /**
         * інтерфейс для слухачів статистики мережі
         */
        interface NetworkStatsListener {
            /**
             * викликається при отриманні нової статистики
             *
             * @param stats статистика мережі
             */
            fun onStatsUpdate(stats: NetworkStats)
        }
        
        /**
         * представлення статистики мережі
         *
         * @property timestamp час отримання статистики
         * @property bytesReceived кількість отриманих байтів
         * @property bytesSent кількість відправлених байтів
         * @property packetsReceived кількість отриманих пакетів
         * @property packetsSent кількість відправлених пакетів
         * @property bytesReceivedPerSecond кількість отриманих байтів в секунду
         * @property bytesSentPerSecond кількість відправлених байтів в секунду
         */
        data class NetworkStats(
            val timestamp: Long,
            val bytesReceived: Long,
            val bytesSent: Long,
            val packetsReceived: Long,
            val packetsSent: Long,
            val bytesReceivedPerSecond: Double,
            val bytesSentPerSecond: Double
        )
        
        /**
         * додає слухача статистики
         *
         * @param listener слухач
         */
        fun addListener(listener: NetworkStatsListener) {
            listeners.add(listener)
        }
        
        /**
         * видаляє слухача статистики
         *
         * @param listener слухач
         */
        fun removeListener(listener: NetworkStatsListener) {
            listeners.remove(listener)
        }
        
        /**
         * починає моніторинг
         *
         * @param interval інтервал моніторингу в мілісекундах
         */
        fun startMonitoring(interval: Long = 1000) {
            if (isMonitoring) return
            
            isMonitoring = true
            
            Thread {
                while (isMonitoring) {
                    try {
                        val currentStats = getNetworkStats()
                        if (currentStats != null) {
                            if (previousStats != null) {
                                val timeDiff = (currentStats.timestamp - previousStats!!.timestamp) / 1000.0
                                if (timeDiff > 0) {
                                    val bytesReceivedDiff = currentStats.bytesReceived - previousStats!!.bytesReceived
                                    val bytesSentDiff = currentStats.bytesSent - previousStats!!.bytesSent
                                    
                                    val stats = NetworkStats(
                                        currentStats.timestamp,
                                        currentStats.bytesReceived,
                                        currentStats.bytesSent,
                                        currentStats.packetsReceived,
                                        currentStats.packetsSent,
                                        bytesReceivedDiff / timeDiff,
                                        bytesSentDiff / timeDiff
                                    )
                                    
                                    listeners.forEach { it.onStatsUpdate(stats) }
                                }
                            }
                            previousStats = currentStats
                        }
                        
                        Thread.sleep(interval)
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                        break
                    } catch (e: Exception) {
                        // ігноруємо інші помилки
                    }
                }
            }.start()
        }
        
        /**
         * зупиняє моніторинг
         */
        fun stopMonitoring() {
            isMonitoring = false
        }
        
        /**
         * отримує статистику мережі
         *
         * @return статистика мережі або null якщо не вдалося отримати
         */
        private fun getNetworkStats(): NetworkStats? {
            return try {
                val networkInterface = NetworkInterface.getByName(interfaceName)
                if (networkInterface != null) {
                    val stats = networkInterface.interfaceAddresses.firstOrNull()
                    if (stats != null) {
                        // для спрощення просто повертаємо фіктивні дані
                        // в реальному застосунку тут потрібно отримати реальну статистику
                        NetworkStats(
                            System.currentTimeMillis(),
                            SecureRandom().nextLong().coerceAtLeast(0),
                            SecureRandom().nextLong().coerceAtLeast(0),
                            SecureRandom().nextLong().coerceAtLeast(0),
                            SecureRandom().nextLong().coerceAtLeast(0),
                            0.0,
                            0.0
                        )
                    } else {
                        null
                    }
                } else {
                    null
                }
            } catch (e: SocketException) {
                null
            }
        }
    }
    
    /**
     * створює мережевий монітор
     *
     * @param interfaceName ім'я мережевого інтерфейсу
     * @return мережевий монітор
     */
    fun createNetworkMonitor(interfaceName: String): NetworkMonitor {
        return NetworkMonitor(interfaceName)
    }
    
    // функції для роботи з мережевими протоколами
    
    /**
     * представлення http сервера
     *
     * @property port порт
     */
    class HttpServer(private val port: Int) {
        private var serverSocket: ServerSocket? = null
        private var isRunning = false
        private val requestHandlers = mutableMapOf<String, HttpRequestHandler>()
        private val executorService = Executors.newCachedThreadPool()
        
        /**
         * інтерфейс для обробника http запитів
         */
        interface HttpRequestHandler {
            /**
             * обробляє http запит
             *
             * @param request запит
             * @return відповідь
             */
            fun handle(request: HttpRequest): HttpResponse
        }
        
        /**
         * представлення http запиту
         *
         * @property method метод запиту
         * @property path шлях запиту
         * @property headers заголовки запиту
         * @property body тіло запиту
         */
        data class HttpRequest(
            val method: String,
            val path: String,
            val headers: Map<String, String>,
            val body: String
        )
        
        /**
         * представлення http відповіді
         *
         * @property statusCode код статусу
         * @property headers заголовки відповіді
         * @property body тіло відповіді
         */
        data class HttpResponse(
            val statusCode: Int,
            val headers: Map<String, String> = emptyMap(),
            val body: String = ""
        )
        
        /**
         * додає обробник для шляху
         *
         * @param path шлях
         * @param handler обробник
         */
        fun addHandler(path: String, handler: HttpRequestHandler) {
            requestHandlers[path] = handler
        }
        
        /**
         * додає обробник для методу get
         *
         * @param path шлях
         * @param handler обробник
         */
        fun get(path: String, handler: HttpRequestHandler) {
            addHandler("GET:$path", handler)
        }
        
        /**
         * додає обробник для методу post
         *
         * @param path шлях
         * @param handler обробник
         */
        fun post(path: String, handler: HttpRequestHandler) {
            addHandler("POST:$path", handler)
        }
        
        /**
         * додає обробник для методу put
         *
         * @param path шлях
         * @param handler обробник
         */
        fun put(path: String, handler: HttpRequestHandler) {
            addHandler("PUT:$path", handler)
        }
        
        /**
         * додає обробник для методу delete
         *
         * @param path шлях
         * @param handler обробник
         */
        fun delete(path: String, handler: HttpRequestHandler) {
            addHandler("DELETE:$path", handler)
        }
        
        /**
         * запускає сервер
         *
         * @return true якщо запуск вдалося
         */
        fun start(): Boolean {
            return try {
                serverSocket = ServerSocket(port)
                isRunning = true
                
                // запускаємо потік для прийняття з'єднань
                Thread {
                    while (isRunning) {
                        try {
                            val clientSocket = serverSocket?.accept()
                            if (clientSocket != null) {
                                executorService.submit {
                                    handleClient(clientSocket)
                                }
                            }
                        } catch (e: IOException) {
                            if (isRunning) {
                                // якщо сервер все ще повинен працювати, але сталася помилка
                                break
                            }
                        }
                    }
                }.start()
                
                true
            } catch (e: IOException) {
                false
            }
        }
        
        /**
         * обробляє клієнтське з'єднання
         *
         * @param clientSocket сокет клієнта
         */
        private fun handleClient(clientSocket: Socket) {
            try {
                val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                val writer = BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream()))
                
                // читаємо запит
                val requestLine = reader.readLine()
                if (requestLine != null) {
                    val parts = requestLine.split(" ")
                    if (parts.size >= 2) {
                        val method = parts[0]
                        val path = parts[1]
                        
                        // читаємо заголовки
                        val headers = mutableMapOf<String, String>()
                        var line: String?
                        while (reader.readLine().also { line = it } != null && line!!.isNotEmpty()) {
                            val headerParts = line!!.split(":", limit = 2)
                            if (headerParts.size == 2) {
                                headers[headerParts[0].trim()] = headerParts[1].trim()
                            }
                        }
                        
                        // читаємо тіло (якщо є)
                        val contentLength = headers["Content-Length"]?.toIntOrNull() ?: 0
                        val body = if (contentLength > 0) {
                            val bodyBuffer = CharArray(contentLength)
                            reader.read(bodyBuffer, 0, contentLength)
                            bodyBuffer.joinToString("")
                        } else {
                            ""
                        }
                        
                        // створюємо об'єкт запиту
                        val request = HttpRequest(method, path, headers, body)
                        
                        // знаходимо обробник
                        val handlerKey = "$method:$path"
                        val handler = requestHandlers[handlerKey] ?: requestHandlers[path]
                        
                        // обробляємо запит
                        val response = if (handler != null) {
                            handler.handle(request)
                        } else {
                            HttpResponse(404, emptyMap(), "Not Found")
                        }
                        
                        // відправляємо відповідь
                        writer.write("HTTP/1.1 ${response.statusCode} ${getStatusMessage(response.statusCode)}\r\n")
                        writer.write("Content-Length: ${response.body.length}\r\n")
                        writer.write("Connection: close\r\n")
                        
                        for ((key, value) in response.headers) {
                            writer.write("$key: $value\r\n")
                        }
                        
                        writer.write("\r\n")
                        writer.write(response.body)
                        writer.flush()
                    }
                }
            } catch (e: Exception) {
                // ігноруємо помилки обробки клієнта
            } finally {
                try {
                    clientSocket.close()
                } catch (e: IOException) {
                    // ігноруємо помилки закриття
                }
            }
        }
        
        /**
         * отримує повідомлення статусу за кодом
         *
         * @param statusCode код статусу
         * @return повідомлення статусу
         */
        private fun getStatusMessage(statusCode: Int): String {
            return when (statusCode) {
                200 -> "OK"
                201 -> "Created"
                204 -> "No Content"
                400 -> "Bad Request"
                401 -> "Unauthorized"
                403 -> "Forbidden"
                404 -> "Not Found"
                500 -> "Internal Server Error"
                else -> "Unknown"
            }
        }
        
        /**
         * зупиняє сервер
         */
        fun stop() {
            isRunning = false
            
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                // ігноруємо помилки закриття
            }
            
            executorService.shutdown()
        }
        
        /**
         * перевіряє, чи сервер працює
         *
         * @return true якщо сервер працює
         */
        fun isRunning(): Boolean {
            return isRunning
        }
    }
    
    /**
     * створює http сервер
     *
     * @param port порт
     * @return http сервер
     */
    fun createHttpServer(port: Int): HttpServer {
        return HttpServer(port)
    }
    
    // функції для роботи з мережевими безпекою
    
    /**
     * представлення мережевого брандмауера
     */
    class NetworkFirewall {
        private val blockedIps = mutableSetOf<String>()
        private val blockedPorts = mutableSetOf<Int>()
        private val allowedIps = mutableSetOf<String>()
        
        /**
         * блокує ip адресу
         *
         * @param ipAddress ip адреса
         */
        fun blockIp(ipAddress: String) {
            blockedIps.add(ipAddress)
        }
        
        /**
         * розблоковує ip адресу
         *
         * @param ipAddress ip адреса
         */
        fun unblockIp(ipAddress: String) {
            blockedIps.remove(ipAddress)
        }
        
        /**
         * блокує порт
         *
         * @param port порт
         */
        fun blockPort(port: Int) {
            blockedPorts.add(port)
        }
        
        /**
         * розблоковує порт
         *
         * @param port порт
         */
        fun unblockPort(port: Int) {
            blockedPorts.remove(port)
        }
        
        /**
         * дозволяє ip адресу
         *
         * @param ipAddress ip адреса
         */
        fun allowIp(ipAddress: String) {
            allowedIps.add(ipAddress)
        }
        
        /**
         * забороняє ip адресу
         *
         * @param ipAddress ip адреса
         */
        fun denyIp(ipAddress: String) {
            allowedIps.remove(ipAddress)
        }
        
        /**
         * перевіряє, чи ip адреса заблокована
         *
         * @param ipAddress ip адреса
         * @return true якщо адреса заблокована
         */
        fun isIpBlocked(ipAddress: String): Boolean {
            return blockedIps.contains(ipAddress)
        }
        
        /**
         * перевіряє, чи порт заблокований
         *
         * @param port порт
         * @return true якщо порт заблокований
         */
        fun isPortBlocked(port: Int): Boolean {
            return blockedPorts.contains(port)
        }
        
        /**
         * перевіряє, чи ip адреса дозволена
         *
         * @param ipAddress ip адреса
         * @return true якщо адреса дозволена
         */
        fun isIpAllowed(ipAddress: String): Boolean {
            return allowedIps.contains(ipAddress)
        }
        
        /**
         * очищує всі правила
         */
        fun clearRules() {
            blockedIps.clear()
            blockedPorts.clear()
            allowedIps.clear()
        }
        
        /**
         * отримує список заблокованих ip адрес
         *
         * @return список заблокованих ip адрес
         */
        fun getBlockedIps(): Set<String> {
            return blockedIps.toSet()
        }
        
        /**
         * отримує список заблокованих портів
         *
         * @return список заблокованих портів
         */
        fun getBlockedPorts(): Set<Int> {
            return blockedPorts.toSet()
        }
        
        /**
         * отримує список дозволених ip адрес
         *
         * @return список дозволених ip адрес
         */
        fun getAllowedIps(): Set<String> {
            return allowedIps.toSet()
        }
    }
    
    /**
     * створює мережевий брандмауер
     *
     * @return мережевий брандмауер
     */
    fun createNetworkFirewall(): NetworkFirewall {
        return NetworkFirewall()
    }
    
    // функції для роботи з мережевими утилітами вищого рівня
    
    /**
     * представлення мережевого клієнта з підтримкою кешування
     *
     * @property connectTimeout таймаут підключення
     * @property readTimeout таймаут читання
     */
    class CachedNetworkClient(
        private val connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT,
        private val readTimeout: Int = DEFAULT_READ_TIMEOUT
    ) {
        private val cache = mutableMapOf<String, CachedResponse>()
        private val cacheLock = Any()
        
        /**
         * представлення кешованої відповіді
         *
         * @property response відповідь
         * @property timestamp час кешування
         * @property ttl час життя в мілісекундах
         */
        data class CachedResponse(val response: HttpResponse, val timestamp: Long, val ttl: Long) {
            /**
             * перевіряє, чи кеш ще дійсний
             *
             * @return true якщо кеш ще дійсний
             */
            fun isExpired(): Boolean {
                return System.currentTimeMillis() - timestamp > ttl
            }
        }
        
        /**
         * виконує http get запит з кешуванням
         *
         * @param url url для запиту
         * @param ttl час життя кешу в мілісекундах
         * @param headers заголовки запиту
         * @return відповідь
         */
        fun httpGetWithCache(
            url: String,
            ttl: Long = 60000, // 1 хвилина за замовчуванням
            headers: Map<String, String> = emptyMap()
        ): HttpResponse? {
            // перевіряємо кеш
            synchronized(cacheLock) {
                val cached = cache[url]
                if (cached != null && !cached.isExpired()) {
                    return cached.response
                }
            }
            
            // виконуємо запит
            val response = httpGet(url, connectTimeout, readTimeout, headers)
            
            // зберігаємо в кеш
            if (response != null) {
                synchronized(cacheLock) {
                    cache[url] = CachedResponse(response, System.currentTimeMillis(), ttl)
                }
            }
            
            return response
        }
        
        /**
         * очищує кеш
         */
        fun clearCache() {
            synchronized(cacheLock) {
                cache.clear()
            }
        }
        
        /**
         * видаляє запис з кешу
         *
         * @param url url
         */
        fun removeFromCache(url: String) {
            synchronized(cacheLock) {
                cache.remove(url)
            }
        }
        
        /**
         * отримує розмір кешу
         *
         * @return розмір кешу
         */
        fun getCacheSize(): Int {
            synchronized(cacheLock) {
                return cache.size
            }
        }
    }
    
    /**
     * створює мережевий клієнт з кешуванням
     *
     * @param connectTimeout таймаут підключення
     * @param readTimeout таймаут читання
     * @return мережевий клієнт з кешуванням
     */
    fun createCachedNetworkClient(
        connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT,
        readTimeout: Int = DEFAULT_READ_TIMEOUT
    ): CachedNetworkClient {
        return CachedNetworkClient(connectTimeout, readTimeout)
    }
    
    /**
     * представлення мережевого пулу з'єднань
     *
     * @property maxSize максимальний розмір пулу
     * @property connectTimeout таймаут підключення
     * @property readTimeout таймаут читання
     */
    class ConnectionPool(
        private val maxSize: Int = 10,
        private val connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT,
        private val readTimeout: Int = DEFAULT_READ_TIMEOUT
    ) {
        private val pool = mutableListOf<HttpConnection>()
        private val poolLock = Any()
        private val currentSize = AtomicInteger(0)
        
        /**
     