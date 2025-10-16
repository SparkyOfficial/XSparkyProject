/**
 * приклад використання веб фреймворку
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.web

import io.ktor.server.application.*
import io.ktor.server.response.*

/**
 * приклад використання веб фреймворку
 */
object WebFrameworkExample {
    @JvmStatic
    fun main(args: Array<String>) {
        // створюємо веб сервер
        val server = WebFramework.createServer()
        
        // додаємо маршрути
        server.addRoute("/hello") { call ->
            call.respondText("Привіт від XSparkyProject!")
        }
        
        server.addRoute("/api/status") { call ->
            call.respondText("Сервер працює нормально")
        }
        
        // запускаємо сервер
        server.start(8080)
        
        println("веб сервер запущено, натисніть ENTER для зупинки...")
        readLine()
        
        // зупиняємо сервер
        server.stop()
    }
}