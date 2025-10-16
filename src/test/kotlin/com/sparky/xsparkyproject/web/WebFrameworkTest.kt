/**
 * тест для веб фреймворку
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.web

import kotlin.test.*
import io.ktor.server.testing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

/**
 * тест для веб фреймворку
 */
class WebFrameworkTest {
    
    @Test
    fun testWebServerCreation() {
        val server = WebFramework.createServer()
        assertNotNull(server)
        assertEquals("Зупинено", server.getStatus())
    }
    
    @Test
    fun testRouteDefinition() {
        val route = WebFramework.route("/test") { call ->
            call.respondText("test")
        }
        
        assertNotNull(route)
        assertEquals("/test", route.path)
        assertNotNull(route.handler)
    }
    
    @Test
    fun testServerStartAndStop() {
        val server = WebFramework.createServer()
        assertEquals("Зупинено", server.getStatus())
        
        // не можемо реально запустити сервер в тесті, але можемо перевірити методи
        server.stop() // має вивести повідомлення, що сервер не запущено
    }
}