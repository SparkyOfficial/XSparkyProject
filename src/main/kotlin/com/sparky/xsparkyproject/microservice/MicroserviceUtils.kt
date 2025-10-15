package com.sparky.xsparkyproject.microservice

import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * утиліти для побудови мікросервісів
 * включає перевірку здоров'я, балансування навантаження та відстеження
 *
 * @author Андрій Будильников
 */
class MicroserviceUtils {
    
    companion object {
        private val serviceRegistry = ConcurrentHashMap<String, ServiceInfo>()
        private val callCounters = ConcurrentHashMap<String, AtomicInteger>()
        
        /**
         * перевіряє стан сервісу
         */
        suspend fun healthCheck(serviceUrl: String): HealthStatus {
            // тут має бути реальна перевірка, але для прикладу просто імітуємо
            delay(100)
            return HealthStatus.UP
        }
        
        /**
         * реєструє сервіс у реєстрі
         */
        fun registerService(serviceName: String, serviceUrl: String, version: String = "1.0.0"): Boolean {
            val serviceInfo = ServiceInfo(serviceName, serviceUrl, version)
            serviceRegistry[serviceName] = serviceInfo
            callCounters[serviceName] = AtomicInteger(0)
            println("Registering service $serviceName at $serviceUrl")
            return true
        }
        
        /**
         * відміняє реєстрацію сервісу
         */
        fun unregisterService(serviceName: String): Boolean {
            serviceRegistry.remove(serviceName)
            callCounters.remove(serviceName)
            println("Unregistering service $serviceName")
            return true
        }
        
        /**
         * отримує інформацію про сервіс
         */
        fun getServiceInfo(serviceName: String): ServiceInfo? {
            return serviceRegistry[serviceName]
        }
        
        /**
         * отримує список всіх зареєстрованих сервісів
         */
        fun getAllServices(): List<ServiceInfo> {
            return serviceRegistry.values.toList()
        }
        
        /**
         * симулює виклик сервісу з підрахунком викликів
         */
        fun callService(serviceName: String): ServiceCallResult {
            val counter = callCounters[serviceName]
            val count = counter?.incrementAndGet() ?: 0
            return ServiceCallResult(serviceName, count)
        }
        
        /**
         * отримує статистику викликів сервісу
         */
        fun getServiceCallCount(serviceName: String): Int {
            return callCounters[serviceName]?.get() ?: 0
        }
        
        /**
         * реалізація простого round-robin балансування навантаження
         */
        fun roundRobinLoadBalance(services: List<String>): String? {
            if (services.isEmpty()) return null
            
            val counter = AtomicInteger()
            val index = counter.getAndIncrement() % services.size
            return services[index]
        }
    }
}

/**
 * статус здоров'я сервісу
 */
enum class HealthStatus {
    UP, DOWN, UNKNOWN
}

/**
 * інформація про сервіс для реєстрації
 */
data class ServiceInfo(
    val name: String,
    val url: String,
    val version: String = "1.0.0"
)

/**
 * результат виклику сервісу
 */
data class ServiceCallResult(
    val serviceName: String,
    val callCount: Int
)