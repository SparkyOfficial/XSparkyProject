package com.sparky.xsparkyproject

import com.sparky.xsparkyproject.data.DataFrame
import com.sparky.xsparkyproject.fp.Option
import com.sparky.xsparkyproject.fp.Some
import com.sparky.xsparkyproject.fp.None
import com.sparky.xsparkyproject.fp.getOrElse
import com.sparky.xsparkyproject.validation.Validator
import com.sparky.xsparkyproject.validation.ValidationRule
import com.sparky.xsparkyproject.validation.NotNullRule
import com.sparky.xsparkyproject.validation.StringLengthRule
import com.sparky.xsparkyproject.validation.ValidationErrorList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * тест для основних компонентів бібліотеки
 *
 * @author Андрій Будильников
 */
class XSparkyProjectTest {
    
    @Test
    fun testDataFrameCreation() {
        val data = listOf(
            mapOf("name" to "Alice", "age" to 30),
            mapOf("name" to "Bob", "age" to 25)
        )
        
        val df = DataFrame.of(data)
        assertEquals(2, df.size)
        assertTrue(df.getColumns().contains("name"))
        assertTrue(df.getColumns().contains("age"))
    }
    
    @Test
    fun testOptionUsage() {
        val someValue = Some("Hello")
        val noneValue = None
        
        // Test Option functionality using the extension functions
        val someResult = someValue.getOrElse { "Default" }
        assertEquals("Hello", someResult)
        
        val noneResult = noneValue.getOrElse { "Default" }
        assertEquals("Default", noneResult)
    }
    
    @Test
    fun testValidation() {
        val validator = Validator.of<String>()
            .addRule(NotNullRule<String?>() as ValidationRule<String>)
            .addRule(StringLengthRule(3, 10))
            .build()
            
        val result = validator.validate("Hi")
        // Повинна бути помилка через коротку довжину
        assertTrue(result is ValidationErrorList)
    }
    
    @Test
    fun testLibraryInfo() {
        val info = XSparkyProject.getInfo()
        assertTrue(info.contains("XSparkyProject"))
        assertTrue(info.contains("Comprehensive Kotlin Library"))
    }
}