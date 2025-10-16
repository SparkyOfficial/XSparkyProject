package com.sparky.xsparkyproject

/**
 * основний клас для бібліотеки xsparkyproject
 * це об'єднання 10+ потужних інструментів в одному
 *
 * @author Андрій Будильников
 */
class XSparkyProject {
    companion object {
        const val VERSION = "1.0.0"
        
        fun getInfo(): String {
            return """
                XSparkyProject v$VERSION - Comprehensive Kotlin Library
                
                Modules:
                1. Data Processing & Analytics (data)
                2. Networking Utilities (net)
                3. Coroutines Extensions (coroutines)
                4. Functional Programming Toolkit (fp)
                5. Validation & Serialization Framework (validation, serialization)
                6. Microservice Toolkit (microservice)
                7. Security Framework (security)
                8. Caching Utilities (cache)
                9. Configuration Management (config)
                10. Logging Utilities (logging)
                11. Web Framework (web) - NEW!
            """.trimIndent()
        }
    }
}