/**
 * веб фреймворк на основі ktor
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.web

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.request.*
import kotlinx.coroutines.*

/**
 * представлення інтерфейсу для веб сервера
 */
interface WebServer {
    /**
     * запустити сервер
     *
     * @param port порт
     * @param host хост
     */
    fun start(port: Int = 8080, host: String = "0.0.0.0")
    
    /**
     * зупинити сервер
     */
    fun stop()
    
    /**
     * додати маршрут
     *
     * @param path шлях
     * @param handler обробник
     */
    fun addRoute(path: String, handler: suspend (ApplicationCall) -> Unit)
    
    /**
     * отримати статус сервера
     *
     * @return статус сервера
     */
    fun getStatus(): String
}

/**
 * представлення базової реалізації веб сервера на основі ktor
 */
class KtorWebServer : WebServer {
    private var server: NettyApplicationEngine? = null
    private val routes = mutableMapOf<String, suspend (ApplicationCall) -> Unit>()
    private var isRunning = false
    
    override fun start(port: Int, host: String) {
        if (isRunning) {
            println("сервер вже запущено")
            return
        }
        
        server = embeddedServer(Netty, port = port, host = host) {
            routing {
                // додаємо всі зареєстровані маршрути
                routes.forEach { (path, handler) ->
                    get(path) {
                        handler(this.call)
                    }
                }
                
                // маршрут за замовчуванням
                get("/") {
                    call.respondText("Ласкаво просимо до XSparkyProject Web Framework!")
                }
            }
        }
        
        server?.start(wait = false)
        isRunning = true
        println("веб сервер запущено на $host:$port")
    }
    
    override fun stop() {
        if (!isRunning) {
            println("сервер не запущено")
            return
        }
        
        server?.stop(1000, 5000)
        isRunning = false
        println("веб сервер зупинено")
    }
    
    override fun addRoute(path: String, handler: suspend (ApplicationCall) -> Unit) {
        routes[path] = handler
        // якщо сервер вже запущено, нам потрібно перезапустити його для застосування нових маршрутів
        if (isRunning) {
            println("маршрут додано, але сервер потрібно перезапустити для застосування змін")
        }
    }
    
    override fun getStatus(): String {
        return if (isRunning) "Запущено" else "Зупинено"
    }
}

/**
 * представлення об'єкта для створення веб сервера
 */
object WebFramework {
    /**
     * створити веб сервер
     *
     * @return веб сервер
     */
    fun createServer(): WebServer {
        return KtorWebServer()
    }
    
    /**
     * створити маршрут
     *
     * @param path шлях
     * @param handler обробник
     * @return маршрут
     */
    fun route(path: String, handler: suspend (ApplicationCall) -> Unit): RouteDefinition {
        return RouteDefinition(path, handler)
    }
}

/**
 * представлення визначення маршруту
 */
data class RouteDefinition(
    val path: String,
    val handler: suspend (ApplicationCall) -> Unit
)