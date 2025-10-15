/**
 * xsparkyproject - розширена бібліотека для kotlin
 *
 * Copyright 2025 Андрій Будильников
 */

package com.sparky.xsparkyproject.utils

import java.util.regex.Pattern
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.reflect.KClass

/**
 * утилітарний клас для роботи з валідацією
 *
 * @author андрій будильников
 * @since 1.0.0
 */
@Suppress("unused")
class ValidationUtils {
    
    companion object {
        // стандартні регулярні вирази
        const val REGEX_EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        const val REGEX_PHONE = "^\\+?[1-9]\\d{1,14}$"
        const val REGEX_URL = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$"
        const val REGEX_IPV4 = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        const val REGEX_IPV6 = "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))"
        const val REGEX_CREDIT_CARD = "^(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|3[0-9]{13}|6(?:011|5[0-9]{2})[0-9]{12})$"
        const val REGEX_UUID = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
        
        // стандартні довжини
        const val MIN_PASSWORD_LENGTH = 8
        const val MAX_PASSWORD_LENGTH = 128
        const val MIN_USERNAME_LENGTH = 3
        const val MAX_USERNAME_LENGTH = 50
        
        // стандартні значення
        const val MIN_AGE = 0
        const val MAX_AGE = 150
    }
    
    // базові функції для роботи з валідаторами
    
    /**
     * представлення валідатора
     *
     * @param T тип значення для валідації
     */
    interface Validator<T> {
        /**
         * валідує значення
         *
         * @param value значення
         * @return результат валідації
         */
        fun validate(value: T): ValidationResult
        
        /**
         * валідує значення та кидає виняток, якщо валідація не пройшла
         *
         * @param value значення
         * @throws ValidationException якщо валідація не пройшла
         */
        fun validateOrThrow(value: T) {
            val result = validate(value)
            if (!result.isValid) {
                throw ValidationException(result.errors.joinToString(", "))
            }
        }
    }
    
    /**
     * представлення результату валідації
     *
     * @property isValid чи значення дійсне
     * @property errors список помилок
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList()
    ) {
        companion object {
            /**
             * створює дійсний результат
             *
             * @return дійсний результат
             */
            fun valid(): ValidationResult {
                return ValidationResult(true)
            }
            
            /**
             * створює недійсний результат з помилками
             *
             * @param errors помилки
             * @return недійсний результат
             */
            fun invalid(errors: List<String>): ValidationResult {
                return ValidationResult(false, errors)
            }
            
            /**
             * створює недійсний результат з однією помилкою
             *
             * @param error помилка
             * @return недійсний результат
             */
            fun invalid(error: String): ValidationResult {
                return ValidationResult(false, listOf(error))
            }
        }
    }
    
    /**
     * виняток валідації
     *
     * @property message повідомлення про помилку
     */
    class ValidationException(message: String) : Exception(message)
    
    /**
     * базова реалізація валідатора
     */
    abstract class BaseValidator<T> : Validator<T> {
        override fun validate(value: T): ValidationResult {
            val errors = mutableListOf<String>()
            validateValue(value, errors)
            return if (errors.isEmpty()) {
                ValidationResult.valid()
            } else {
                ValidationResult.invalid(errors)
            }
        }
        
        /**
         * валідує значення та додає помилки до списку
         *
         * @param value значення
         * @param errors список помилок
         */
        protected abstract fun validateValue(value: T, errors: MutableList<String>)
    }
    
    // функції для роботи з валідаторами рядків
    
    /**
     * представлення валідатора рядків
     */
    class StringValidator : BaseValidator<String>() {
        private val rules = mutableListOf<(String) -> String?>()
        
        /**
         * додає правило "не порожній"
         *
         * @return валідатор
         */
        fun notEmpty(): StringValidator {
            rules.add { value ->
                if (value.isEmpty()) "Значення не може бути порожнім" else null
            }
            return this
        }
        
        /**
         * додає правило "не null"
         *
         * @return валідатор
         */
        fun notNull(): StringValidator {
            rules.add { value ->
                if (value == null) "Значення не може бути null" else null
            }
            return this
        }
        
        /**
         * додає правило мінімальної довжини
         *
         * @param minLength мінімальна довжина
         * @return валідатор
         */
        fun minLength(minLength: Int): StringValidator {
            rules.add { value ->
                if (value.length < minLength) "Довжина значення повинна бути не менше $minLength символів" else null
            }
            return this
        }
        
        /**
         * додає правило максимальної довжини
         *
         * @param maxLength максимальна довжина
         * @return валідатор
         */
        fun maxLength(maxLength: Int): StringValidator {
            rules.add { value ->
                if (value.length > maxLength) "Довжина значення повинна бути не більше $maxLength символів" else null
            }
            return this
        }
        
        /**
         * додає правило точної довжини
         *
         * @param length точна довжина
         * @return валідатор
         */
        fun length(length: Int): StringValidator {
            rules.add { value ->
                if (value.length != length) "Довжина значення повинна бути рівно $length символів" else null
            }
            return this
        }
        
        /**
         * додає правило регулярного виразу
         *
         * @param regex регулярний вираз
         * @param errorMessage повідомлення про помилку
         * @return валідатор
         */
        fun matches(regex: String, errorMessage: String = "Значення не відповідає шаблону"): StringValidator {
            val pattern = Pattern.compile(regex)
            rules.add { value ->
                if (!pattern.matcher(value).matches()) errorMessage else null
            }
            return this
        }
        
        /**
         * додає правило email
         *
         * @return валідатор
         */
        fun email(): StringValidator {
            return matches(REGEX_EMAIL, "Недійсна адреса електронної пошти")
        }
        
        /**
         * додає правило телефону
         *
         * @return валідатор
         */
        fun phone(): StringValidator {
            return matches(REGEX_PHONE, "Недійсний номер телефону")
        }
        
        /**
         * додає правило URL
         *
         * @return валідатор
         */
        fun url(): StringValidator {
            return matches(REGEX_URL, "Недійсна URL-адреса")
        }
        
        /**
         * додає правило IPv4
         *
         * @return валідатор
         */
        fun ipv4(): StringValidator {
            return matches(REGEX_IPV4, "Недійсна IPv4 адреса")
        }
        
        /**
         * додає правило IPv6
         *
         * @return валідатор
         */
        fun ipv6(): StringValidator {
            return matches(REGEX_IPV6, "Недійсна IPv6 адреса")
        }
        
        /**
         * додає правило кредитної картки
         *
         * @return валідатор
         */
        fun creditCard(): StringValidator {
            return matches(REGEX_CREDIT_CARD, "Недійсний номер кредитної картки")
        }
        
        /**
         * додає правило UUID
         *
         * @return валідатор
         */
        fun uuid(): StringValidator {
            return matches(REGEX_UUID, "Недійсний UUID")
        }
        
        /**
         * додає користувацьке правило
         *
         * @param rule правило
         * @return валідатор
         */
        fun custom(rule: (String) -> String?): StringValidator {
            rules.add(rule)
            return this
        }
        
        override fun validateValue(value: String, errors: MutableList<String>) {
            rules.forEach { rule ->
                rule(value)?.let { errors.add(it) }
            }
        }
    }
    
    /**
     * створює валідатор рядків
     *
     * @return валідатор рядків
     */
    fun createStringValidator(): StringValidator {
        return StringValidator()
    }
    
    // функції для роботи з валідаторами чисел
    
    /**
     * представлення валідатора цілих чисел
     */
    class IntegerValidator : BaseValidator<Int>() {
        private val rules = mutableListOf<(Int) -> String?>()
        
        /**
         * додає правило мінімального значення
         *
         * @param minValue мінімальне значення
         * @return валідатор
         */
        fun min(minValue: Int): IntegerValidator {
            rules.add { value ->
                if (value < minValue) "Значення повинно бути не менше $minValue" else null
            }
            return this
        }
        
        /**
         * додає правило максимального значення
         *
         * @param maxValue максимальне значення
         * @return валідатор
         */
        fun max(maxValue: Int): IntegerValidator {
            rules.add { value ->
                if (value > maxValue) "Значення повинно бути не більше $maxValue" else null
            }
            return this
        }
        
        /**
         * додає правило діапазону
         *
         * @param minValue мінімальне значення
         * @param maxValue максимальне значення
         * @return валідатор
         */
        fun range(minValue: Int, maxValue: Int): IntegerValidator {
            rules.add { value ->
                if (value < minValue || value > maxValue) "Значення повинно бути в діапазоні від $minValue до $maxValue" else null
            }
            return this
        }
        
        /**
         * додає правило позитивного значення
         *
         * @return валідатор
         */
        fun positive(): IntegerValidator {
            rules.add { value ->
                if (value <= 0) "Значення повинно бути позитивним" else null
            }
            return this
        }
        
        /**
         * додає правило негативного значення
         *
         * @return валідатор
         */
        fun negative(): IntegerValidator {
            rules.add { value ->
                if (value >= 0) "Значення повинно бути негативним" else null
            }
            return this
        }
        
        /**
         * додає правило ненульового значення
         *
         * @return валідатор
         */
        fun nonZero(): IntegerValidator {
            rules.add { value ->
                if (value == 0) "Значення не може бути нулем" else null
            }
            return this
        }
        
        /**
         * додає користувацьке правило
         *
         * @param rule правило
         * @return валідатор
         */
        fun custom(rule: (Int) -> String?): IntegerValidator {
            rules.add(rule)
            return this
        }
        
        override fun validateValue(value: Int, errors: MutableList<String>) {
            rules.forEach { rule ->
                rule(value)?.let { errors.add(it) }
            }
        }
    }
    
    /**
     * створює валідатор цілих чисел
     *
     * @return валідатор цілих чисел
     */
    fun createIntegerValidator(): IntegerValidator {
        return IntegerValidator()
    }
    
    /**
     * представлення валідатора довгих цілих чисел
     */
    class LongValidator : BaseValidator<Long>() {
        private val rules = mutableListOf<(Long) -> String?>()
        
        /**
         * додає правило мінімального значення
         *
         * @param minValue мінімальне значення
         * @return валідатор
         */
        fun min(minValue: Long): LongValidator {
            rules.add { value ->
                if (value < minValue) "Значення повинно бути не менше $minValue" else null
            }
            return this
        }
        
        /**
         * додає правило максимального значення
         *
         * @param maxValue максимальне значення
         * @return валідатор
         */
        fun max(maxValue: Long): LongValidator {
            rules.add { value ->
                if (value > maxValue) "Значення повинно бути не більше $maxValue" else null
            }
            return this
        }
        
        /**
         * додає правило діапазону
         *
         * @param minValue мінімальне значення
         * @param maxValue максимальне значення
         * @return валідатор
         */
        fun range(minValue: Long, maxValue: Long): LongValidator {
            rules.add { value ->
                if (value < minValue || value > maxValue) "Значення повинно бути в діапазоні від $minValue до $maxValue" else null
            }
            return this
        }
        
        /**
         * додає правило позитивного значення
         *
         * @return валідатор
         */
        fun positive(): LongValidator {
            rules.add { value ->
                if (value <= 0) "Значення повинно бути позитивним" else null
            }
            return this
        }
        
        /**
         * додає правило негативного значення
         *
         * @return валідатор
         */
        fun negative(): LongValidator {
            rules.add { value ->
                if (value >= 0) "Значення повинно бути негативним" else null
            }
            return this
        }
        
        /**
         * додає правило ненульового значення
         *
         * @return валідатор
         */
        fun nonZero(): LongValidator {
            rules.add { value ->
                if (value == 0L) "Значення не може бути нулем" else null
            }
            return this
        }
        
        /**
         * додає користувацьке правило
         *
         * @param rule правило
         * @return валідатор
         */
        fun custom(rule: (Long) -> String?): LongValidator {
            rules.add(rule)
            return this
        }
        
        override fun validateValue(value: Long, errors: MutableList<String>) {
            rules.forEach { rule ->
                rule(value)?.let { errors.add(it) }
            }
        }
    }
    
    /**
     * створює валідатор довгих цілих чисел
     *
     * @return валідатор довгих цілих чисел
     */
    fun createLongValidator(): LongValidator {
        return LongValidator()
    }
    
    /**
     * представлення валідатора чисел з плаваючою комою
     */
    class DoubleValidator : BaseValidator<Double>() {
        private val rules = mutableListOf<(Double) -> String?>()
        
        /**
         * додає правило мінімального значення
         *
         * @param minValue мінімальне значення
         * @return валідатор
         */
        fun min(minValue: Double): DoubleValidator {
            rules.add { value ->
                if (value < minValue) "Значення повинно бути не менше $minValue" else null
            }
            return this
        }
        
        /**
         * додає правило максимального значення
         *
         * @param maxValue максимальне значення
         * @return валідатор
         */
        fun max(maxValue: Double): DoubleValidator {
            rules.add { value ->
                if (value > maxValue) "Значення повинно бути не більше $maxValue" else null
            }
            return this
        }
        
        /**
         * додає правило діапазону
         *
         * @param minValue мінімальне значення
         * @param maxValue максимальне значення
         * @return валідатор
         */
        fun range(minValue: Double, maxValue: Double): DoubleValidator {
            rules.add { value ->
                if (value < minValue || value > maxValue) "Значення повинно бути в діапазоні від $minValue до $maxValue" else null
            }
            return this
        }
        
        /**
         * додає правило позитивного значення
         *
         * @return валідатор
         */
        fun positive(): DoubleValidator {
            rules.add { value ->
                if (value <= 0.0) "Значення повинно бути позитивним" else null
            }
            return this
        }
        
        /**
         * додає правило негативного значення
         *
         * @return валідатор
         */
        fun negative(): DoubleValidator {
            rules.add { value ->
                if (value >= 0.0) "Значення повинно бути негативним" else null
            }
            return this
        }
        
        /**
         * додає правило ненульового значення
         *
         * @return валідатор
         */
        fun nonZero(): DoubleValidator {
            rules.add { value ->
                if (value == 0.0) "Значення не може бути нулем" else null
            }
            return this
        }
        
        /**
         * додає правило скінченного значення
         *
         * @return валідатор
         */
        fun finite(): DoubleValidator {
            rules.add { value ->
                if (!value.isFinite()) "Значення повинно бути скінченним" else null
            }
            return this
        }
        
        /**
         * додає користувацьке правило
         *
         * @param rule правило
         * @return валідатор
         */
        fun custom(rule: (Double) -> String?): DoubleValidator {
            rules.add(rule)
            return this
        }
        
        override fun validateValue(value: Double, errors: MutableList<String>) {
            rules.forEach { rule ->
                rule(value)?.let { errors.add(it) }
            }
        }
    }
    
    /**
     * створює валідатор чисел з плаваючою комою
     *
     * @return валідатор чисел з плаваючою комою
     */
    fun createDoubleValidator(): DoubleValidator {
        return DoubleValidator()
    }
    
    // функції для роботи з валідаторами дат
    
    /**
     * представлення валідатора дат
     */
    class DateValidator : BaseValidator<LocalDate>() {
        private val rules = mutableListOf<(LocalDate) -> String?>()
        
        /**
         * додає правило мінімальної дати
         *
         * @param minDate мінімальна дата
         * @return валідатор
         */
        fun min(minDate: LocalDate): DateValidator {
            rules.add { value ->
                if (value.isBefore(minDate)) "Дата повинна бути не раніше $minDate" else null
            }
            return this
        }
        
        /**
         * додає правило максимальної дати
         *
         * @param maxDate максимальна дата
         * @return валідатор
         */
        fun max(maxDate: LocalDate): DateValidator {
            rules.add { value ->
                if (value.isAfter(maxDate)) "Дата повинна бути не пізніше $maxDate" else null
            }
            return this
        }
        
        /**
         * додає правило діапазону дат
         *
         * @param minDate мінімальна дата
         * @param maxDate максимальна дата
         * @return валідатор
         */
        fun range(minDate: LocalDate, maxDate: LocalDate): DateValidator {
            rules.add { value ->
                if (value.isBefore(minDate) || value.isAfter(maxDate)) "Дата повинна бути в діапазоні від $minDate до $maxDate" else null
            }
            return this
        }
        
        /**
         * додає правило майбутньої дати
         *
         * @return валідатор
         */
        fun future(): DateValidator {
            val today = LocalDate.now()
            rules.add { value ->
                if (!value.isAfter(today)) "Дата повинна бути в майбутньому" else null
            }
            return this
        }
        
        /**
         * додає правило минулої дати
         *
         * @return валідатор
         */
        fun past(): DateValidator {
            val today = LocalDate.now()
            rules.add { value ->
                if (!value.isBefore(today)) "Дата повинна бути в минулому" else null
            }
            return this
        }
        
        /**
         * додає правило сьогоднішньої або майбутньої дати
         *
         * @return валідатор
         */
        fun todayOrFuture(): DateValidator {
            val today = LocalDate.now()
            rules.add { value ->
                if (value.isBefore(today)) "Дата повинна бути сьогодні або в майбутньому" else null
            }
            return this
        }
        
        /**
         * додає правило сьогоднішньої або минулої дати
         *
         * @return валідатор
         */
        fun todayOrPast(): DateValidator {
            val today = LocalDate.now()
            rules.add { value ->
                if (value.isAfter(today)) "Дата повинна бути сьогодні або в минулому" else null
            }
            return this
        }
        
        /**
         * додає користувацьке правило
         *
         * @param rule правило
         * @return валідатор
         */
        fun custom(rule: (LocalDate) -> String?): DateValidator {
            rules.add(rule)
            return this
        }
        
        override fun validateValue(value: LocalDate, errors: MutableList<String>) {
            rules.forEach { rule ->
                rule(value)?.let { errors.add(it) }
            }
        }
    }
    
    /**
     * створює валідатор дат
     *
     * @return валідатор дат
     */
    fun createDateValidator(): DateValidator {
        return DateValidator()
    }
    
    // функції для роботи з валідаторами колекцій
    
    /**
     * представлення валідатора колекцій
     *
     * @param T тип елемента колекції
     */
    class CollectionValidator<T> : BaseValidator<Collection<T>>() {
        private val rules = mutableListOf<(Collection<T>) -> String?>()
        private val elementValidator: Validator<T>? = null
        
        /**
         * додає правило мінімального розміру
         *
         * @param minSize мінімальний розмір
         * @return валідатор
         */
        fun minSize(minSize: Int): CollectionValidator<T> {
            rules.add { value ->
                if (value.size < minSize) "Колекція повинна містити не менше $minSize елементів" else null
            }
            return this
        }
        
        /**
         * додає правило максимального розміру
         *
         * @param maxSize максимальний розмір
         * @return валідатор
         */
        fun maxSize(maxSize: Int): CollectionValidator<T> {
            rules.add { value ->
                if (value.size > maxSize) "Колекція повинна містити не більше $maxSize елементів" else null
            }
            return this
        }
        
        /**
         * додає правило точного розміру
         *
         * @param size точний розмір
         * @return валідатор
         */
        fun size(size: Int): CollectionValidator<T> {
            rules.add { value ->
                if (value.size != size) "Колекція повинна містити рівно $size елементів" else null
            }
            return this
        }
        
        /**
         * додає правило непорожньої колекції
         *
         * @return валідатор
         */
        fun notEmpty(): CollectionValidator<T> {
            rules.add { value ->
                if (value.isEmpty()) "Колекція не може бути порожньою" else null
            }
            return this
        }
        
        /**
         * додає правило унікальних елементів
         *
         * @return валідатор
         */
        fun unique(): CollectionValidator<T> {
            rules.add { value ->
                if (value.distinct().size != value.size) "Колекція повинна містити лише унікальні елементи" else null
            }
            return this
        }
        
        /**
         * додає валідатор для елементів колекції
         *
         * @param validator валідатор елементів
         * @return валідатор
         */
        fun elements(validator: Validator<T>): CollectionValidator<T> {
            rules.add { value ->
                val errors = mutableListOf<String>()
                value.forEachIndexed { index, element ->
                    val result = validator.validate(element)
                    if (!result.isValid) {
                        errors.addAll(result.errors.map { "Елемент $index: $it" })
                    }
                }
                if (errors.isNotEmpty()) errors.joinToString("; ") else null
            }
            return this
        }
        
        /**
         * додає користувацьке правило
         *
         * @param rule правило
         * @return валідатор
         */
        fun custom(rule: (Collection<T>) -> String?): CollectionValidator<T> {
            rules.add(rule)
            return this
        }
        
        override fun validateValue(value: Collection<T>, errors: MutableList<String>) {
            rules.forEach { rule ->
                rule(value)?.let { errors.add(it) }
            }
        }
    }
    
    /**
     * створює валідатор колекцій
     *
     * @param T тип елемента колекції
     * @return валідатор колекцій
     */
    fun <T> createCollectionValidator(): CollectionValidator<T> {
        return CollectionValidator()
    }
    
    // функції для роботи з валідаторами об'єктів
    
    /**
     * представлення валідатора об'єктів
     *
     * @param T тип об'єкта
     */
    class ObjectValidator<T : Any> : BaseValidator<T>() {
        private val rules = mutableListOf<(T) -> String?>()
        private val fieldValidators = mutableMapOf<String, Validator<*>>()
        
        /**
         * додає правило для об'єкта
         *
         * @param rule правило
         * @return валідатор
         */
        fun rule(rule: (T) -> String?): ObjectValidator<T> {
            rules.add(rule)
            return this
        }
        
        /**
         * додає валідатор для поля
         *
         * @param fieldName ім'я поля
         * @param validator валідатор
         * @return валідатор
         */
        fun field(fieldName: String, validator: Validator<*>): ObjectValidator<T> {
            fieldValidators[fieldName] = validator
            return this
        }
        
        /**
         * додає користувацьке правило
         *
         * @param rule правило
         * @return валідатор
         */
        fun custom(rule: (T) -> String?): ObjectValidator<T> {
            rules.add(rule)
            return this
        }
        
        override fun validateValue(value: T, errors: MutableList<String>) {
            rules.forEach { rule ->
                rule(value)?.let { errors.add(it) }
            }
            
            // валідація полів (в реальному застосунку потрібно використовувати рефлексію)
            fieldValidators.forEach { (fieldName, validator) ->
                // тут має бути логіка отримання значення поля та його валідації
                // оскільки це спрощений приклад, ми просто ігноруємо це
            }
        }
    }
    
    /**
     * створює валідатор об'єктів
     *
     * @param T тип об'єкта
     * @return валідатор об'єктів
     */
    fun <T : Any> createObjectValidator(): ObjectValidator<T> {
        return ObjectValidator()
    }
    
    // функції для роботи з композитними валідаторами
    
    /**
     * представлення композитного валідатора
     *
     * @param T тип значення для валідації
     */
    class CompositeValidator<T> : Validator<T> {
        private val validators = mutableListOf<Validator<T>>()
        
        /**
         * додає валідатор
         *
         * @param validator валідатор
         * @return композитний валідатор
         */
        fun addValidator(validator: Validator<T>): CompositeValidator<T> {
            validators.add(validator)
            return this
        }
        
        override fun validate(value: T): ValidationResult {
            val allErrors = mutableListOf<String>()
            
            validators.forEach { validator ->
                val result = validator.validate(value)
                if (!result.isValid) {
                    allErrors.addAll(result.errors)
                }
            }
            
            return if (allErrors.isEmpty()) {
                ValidationResult.valid()
            } else {
                ValidationResult.invalid(allErrors)
            }
        }
    }
    
    /**
     * створює композитний валідатор
     *
     * @param T тип значення для валідації
     * @return композитний валідатор
     */
    fun <T> createCompositeValidator(): CompositeValidator<T> {
        return CompositeValidator()
    }
    
    // функції для роботи з умовними валідаторами
    
    /**
     * представлення умовного валідатора
     *
     * @param T тип значення для валідації
     */
    class ConditionalValidator<T> : Validator<T> {
        private var condition: ((T) -> Boolean)? = null
        private var thenValidator: Validator<T>? = null
        private var elseValidator: Validator<T>? = null
        
        /**
         * встановлює умову
         *
         * @param condition умова
         * @return умовний валідатор
         */
        fun whenCondition(condition: (T) -> Boolean): ConditionalValidator<T> {
            this.condition = condition
            return this
        }
        
        /**
         * встановлює валідатор для випадку, коли умова істинна
         *
         * @param validator валідатор
         * @return умовний валідатор
         */
        fun then(validator: Validator<T>): ConditionalValidator<T> {
            this.thenValidator = validator
            return this
        }
        
        /**
         * встановлює валідатор для випадку, коли умова хибна
         *
         * @param validator валідатор
         * @return умовний валідатор
         */
        fun otherwise(validator: Validator<T>): ConditionalValidator<T> {
            this.elseValidator = validator
            return this
        }
        
        override fun validate(value: T): ValidationResult {
            val conditionResult = condition?.invoke(value) ?: false
            
            val validator = if (conditionResult) thenValidator else elseValidator
            
            return validator?.validate(value) ?: ValidationResult.valid()
        }
    }
    
    /**
     * створює умовний валідатор
     *
     * @param T тип значення для валідації
     * @return умовний валідатор
     */
    fun <T> createConditionalValidator(): ConditionalValidator<T> {
        return ConditionalValidator()
    }
    
    // функції для роботи з валідаторами з підтримкою локалізації
    
    /**
     * представлення валідатора з підтримкою локалізації
     *
     * @param T тип значення для валідації
     */
    class LocalizedValidator<T>(private val locale: String = "uk") : BaseValidator<T>() {
        private val rules = mutableListOf<(T, String) -> String?>()
        private val messages = mutableMapOf<String, String>()
        
        init {
            // ініціалізуємо стандартні повідомлення
            initializeMessages()
        }
        
        /**
         * ініціалізує стандартні повідомлення
         */
        private fun initializeMessages() {
            when (locale) {
                "uk" -> {
                    messages["not_empty"] = "Значення не може бути порожнім"
                    messages["min_length"] = "Довжина значення повинна бути не менше {0} символів"
                    messages["max_length"] = "Довжина значення повинна бути не більше {0} символів"
                    messages["min_value"] = "Значення повинно бути не менше {0}"
                    messages["max_value"] = "Значення повинно бути не більше {0}"
                }
                "en" -> {
                    messages["not_empty"] = "Value cannot be empty"
                    messages["min_length"] = "Value length must be at least {0} characters"
                    messages["max_length"] = "Value length must be at most {0} characters"
                    messages["min_value"] = "Value must be at least {0}"
                    messages["max_value"] = "Value must be at most {0}"
                }
                "ru" -> {
                    messages["not_empty"] = "Значение не может быть пустым"
                    messages["min_length"] = "Длина значения должна быть не менее {0} символов"
                    messages["max_length"] = "Длина значения должна быть не более {0} символов"
                    messages["min_value"] = "Значение должно быть не менее {0}"
                    messages["max_value"] = "Значение должно быть не более {0}"
                }
            }
        }
        
        /**
         * отримує повідомлення за ключем
         *
         * @param key ключ
         * @param args аргументи
         * @return повідомлення
         */
        private fun getMessage(key: String, vararg args: Any): String {
            var message = messages[key] ?: key
            args.forEachIndexed { index, arg ->
                message = message.replace("{$index}", arg.toString())
            }
            return message
        }
        
        /**
         * додає правило "не порожній"
         *
         * @return валідатор
         */
        fun notEmpty(): LocalizedValidator<T> where T : CharSequence {
            @Suppress("UNCHECKED_CAST")
            rules.add { value, locale ->
                if ((value as CharSequence).isEmpty()) getMessage("not_empty") else null
            }
            return this
        }
        
        /**
         * додає правило мінімальної довжини
         *
         * @param minLength мінімальна довжина
         * @return валідатор
         */
        fun minLength(minLength: Int): LocalizedValidator<T> where T : CharSequence {
            @Suppress("UNCHECKED_CAST")
            rules.add { value, locale ->
                if ((value as CharSequence).length < minLength) getMessage("min_length", minLength) else null
            }
            return this
        }
        
        /**
         * додає правило максимальної довжини
         *
         * @param maxLength максимальна довжина
         * @return валідатор
         */
        fun maxLength(maxLength: Int): LocalizedValidator<T> where T : CharSequence {
            @Suppress("UNCHECKED_CAST")
            rules.add { value, locale ->
                if ((value as CharSequence).length > maxLength) getMessage("max_length", maxLength) else null
            }
            return this
        }
        
        /**
         * додає правило мінімального значення
         *
         * @param minValue мінімальне значення
         * @return валідатор
         */
        fun min(minValue: Int): LocalizedValidator<T> where T : Int {
            @Suppress("UNCHECKED_CAST")
            rules.add { value, locale ->
                if (value as Int < minValue) getMessage("min_value", minValue) else null
            }
            return this
        }
        
        /**
         * додає правило максимального значення
         *
         * @param maxValue максимальне значення
         * @return валідатор
         */
        fun max(maxValue: Int): LocalizedValidator<T> where T : Int {
            @Suppress("UNCHECKED_CAST")
            rules.add { value, locale ->
                if (value as Int > maxValue) getMessage("max_value", maxValue) else null
            }
            return this
        }
        
        override fun validateValue(value: T, errors: MutableList<String>) {
            rules.forEach { rule ->
                rule(value, locale)?.let { errors.add(it) }
            }
        }
    }
    
    /**
     * створює валідатор з підтримкою локалізації
     *
     * @param T тип значення для валідації
     * @param locale локаль
     * @return валідатор з підтримкою локалізації
     */
    fun <T> createLocalizedValidator(locale: String = "uk"): LocalizedValidator<T> {
        return LocalizedValidator(locale)
    }
    
    // функції для роботи з валідаторами з підтримкою анотацій
    
    /**
     * анотація для позначення обов'язкового поля
     */
    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Required(val message: String = "Поле є обов'язковим")
    
    /**
     * анотація для позначення поля з мінімальною довжиною
     */
    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class MinLength(val value: Int, val message: String = "Мінімальна довжина {value} символів")
    
    /**
     * анотація для позначення поля з максимальною довжиною
     */
    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class MaxLength(val value: Int, val message: String = "Максимальна довжина {value} символів")
    
    /**
     * анотація для позначення поля з мінімальним значенням
     */
    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Min(val value: Long, val message: String = "Мінімальне значення {value}")
    
    /**
     * анотація для позначення поля з максимальним значенням
     */
    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Max(val value: Long, val message: String = "Максимальне значення {value}")
    
    /**
     * анотація для позначення поля з регулярним виразом
     */
    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Pattern(val value: String, val message: String = "Значення не відповідає шаблону")
    
    /**
     * анотація для позначення поля email
     */
    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Email(val message: String = "Недійсна адреса електронної пошти")
    
    /**
     * представлення валідатора з підтримкою анотацій
     *
     * @param T тип об'єкта
     */
    class AnnotationValidator<T : Any>(private val clazz: KClass<T>) : BaseValidator<T>() {
        
        override fun validateValue(value: T, errors: MutableList<String>) {
            // в реальному застосунку тут потрібно використовувати рефлексію
            // для отримання полів об'єкта та їх анотацій
            // та застосування відповідних правил валідації
            
            // оскільки це спрощений приклад, ми просто додаємо фіктивну помилку
            // щоб показати структуру
            // errors.add("Анотаційна валідація не реалізована в спрощеному прикладі")
        }
    }
    
    /**
     * створює валідатор з підтримкою анотацій
     *
     * @param T тип об'єкта
     * @param clazz клас об'єкта
     * @return валідатор з підтримкою анотацій
     */
    fun <T : Any> createAnnotationValidator(clazz: KClass<T>): AnnotationValidator<T> {
        return AnnotationValidator(clazz)
    }
    
    // функції для роботи з валідаторами з підтримкою кастомних правил
    
    /**
     * представлення кастомного валідатора
     *
     * @param T тип значення для валідації
     */
    class CustomValidator<T> : BaseValidator<T>() {
        private val rules = mutableListOf<(T) -> ValidationResult>()
        
        /**
         * додає кастомне правило
         *
         * @param rule правило
         * @return кастомний валідатор
         */
        fun addRule(rule: (T) -> ValidationResult): CustomValidator<T> {
            rules.add(rule)
            return this
        }
        
        override fun validateValue(value: T, errors: MutableList<String>) {
            rules.forEach { rule ->
                val result = rule(value)
                if (!result.isValid) {
                    errors.addAll(result.errors)
                }
            }
        }
    }
    
    /**
     * створює кастомний валідатор
     *
     * @param T тип значення для валідації
     * @return кастомний валідатор
     */
    fun <T> createCustomValidator(): CustomValidator<T> {
        return CustomValidator()
    }
    
    // функції для роботи з валідаторами з підтримкою груп правил
    
    /**
     * представлення валідатора з групами правил
     *
     * @param T тип значення для валідації
     */
    class GroupedValidator<T> : Validator<T> {
        private val groups = mutableMapOf<String, List<Validator<T>>>()
        private val defaultGroup = "default"
        
        /**
         * додає валідатори до групи
         *
         * @param groupName ім'я групи
         * @param validators валідатори
         * @return валідатор з групами правил
         */
        fun addGroup(groupName: String, validators: List<Validator<T>>): GroupedValidator<T> {
            groups[groupName] = validators
            return this
        }
        
        /**
         * встановлює валідатори за замовчуванням
         *
         * @param validators валідатори
         * @return валідатор з групами правил
         */
        fun default(validators: List<Validator<T>>): GroupedValidator<T> {
            return addGroup(defaultGroup, validators)
        }
        
        override fun validate(value: T): ValidationResult {
            return validate(value, defaultGroup)
        }
        
        /**
         * валідує значення з використанням певної групи
         *
         * @param value значення
         * @param groupName ім'я групи
         * @return результат валідації
         */
        fun validate(value: T, groupName: String): ValidationResult {
            val groupValidators = groups[groupName] ?: groups[defaultGroup] ?: emptyList()
            
            val allErrors = mutableListOf<String>()
            
            groupValidators.forEach { validator ->
                val result = validator.validate(value)
                if (!result.isValid) {
                    allErrors.addAll(result.errors)
                }
            }
            
            return if (allErrors.isEmpty()) {
                ValidationResult.valid()
            } else {
                ValidationResult.invalid(allErrors)
            }
        }
        
        /**
         * валідує значення з використанням кількох груп
         *
         * @param value значення
         * @param groupNames імена груп
         * @return результат валідації
         */
        fun validate(value: T, groupNames: List<String>): ValidationResult {
            val allErrors = mutableListOf<String>()
            
            groupNames.forEach { groupName ->
                val result = validate(value, groupName)
                if (!result.isValid) {
                    allErrors.addAll(result.errors)
                }
            }
            
            return if (allErrors.isEmpty()) {
                ValidationResult.valid()
            } else {
                ValidationResult.invalid(allErrors)
            }
        }
    }
    
    /**
     * створює валідатор з групами правил
     *
     * @param T тип значення для валідації
     * @return валідатор з групами правил
     */
    fun <T> createGroupedValidator(): GroupedValidator<T> {
        return GroupedValidator()
    }
    
    // функції для роботи з валідаторами з підтримкою ланцюгів правил
    
    /**
     * представлення ланцюга валідаторів
     *
     * @param T тип значення для валідації
     */
    class ValidationChain<T> : Validator<T> {
        private val validators = mutableListOf<Validator<T>>()
        
        /**
         * додає валідатор до ланцюга
         *
         * @param validator валідатор
         * @return ланцюг валідаторів
         */
        fun add(validator: Validator<T>): ValidationChain<T> {
            validators.add(validator)
            return this
        }
        
        override fun validate(value: T): ValidationResult {
            validators.forEach { validator ->
                val result = validator.validate(value)
                if (!result.isValid) {
                    return result
                }
            }
            
            return ValidationResult.valid()
        }
    }
    
    /**
     * створює ланцюг валідаторів
     *
     * @param T тип значення для валідації
     * @return ланцюг валідаторів
     */
    fun <T> createValidationChain(): ValidationChain<T> {
        return ValidationChain()
    }
    
    // функції для роботи з валідаторами з підтримкою асинхронної валідації
    
    /**
     * представлення асинхронного валідатора
     *
     * @param T тип значення для валідації
     */
    class AsyncValidator<T> : Validator<T> {
        private val validator: Validator<T>
        
        constructor(validator: Validator<T>) {
            this.validator = validator
        }
        
        override fun validate(value: T): ValidationResult {
            // в реальному застосунку тут може бути асинхронна валідація
            // наприклад, перевірка унікальності в базі даних
            return validator.validate(value)
        }
        
        /**
         * асинхронно валідує значення
         *
         * @param value значення
         * @param callback зворотний виклик з результатом валідації
         */
        fun validateAsync(value: T, callback: (ValidationResult) -> Unit) {
            // в реальному застосунку тут може бути справжня асинхронна валідація
            // наприклад, з використанням корутин або пулу потоків
            val result = validate(value)
            callback(result)
        }
    }
    
    /**
     * створює асинхронний валідатор
     *
     * @param T тип значення для валідації
     * @param validator валідатор
     * @return асинхронний валідатор
     */
    fun <T> createAsyncValidator(validator: Validator<T>): AsyncValidator<T> {
        return AsyncValidator(validator)
    }
    
    // функції для роботи з валідаторами з підтримкою кешування
    
    /**
     * представлення валідатора з кешуванням
     *
     * @param T тип значення для валідації
     */
    class CachedValidator<T> : Validator<T> {
        private val validator: Validator<T>
        private val cache = mutableMapOf<T, ValidationResult>()
        private val cacheLock = Any()
        private val maxSize = 1000
        
        constructor(validator: Validator<T>) {
            this.validator = validator
        }
        
        override fun validate(value: T): ValidationResult {
            synchronized(cacheLock) {
                val cached = cache[value]
                if (cached != null) {
                    return cached
                }
            }
            
            val result = validator.validate(value)
            
            synchronized(cacheLock) {
                // видаляємо найстаріші записи, якщо кеш переповнено
                if (cache.size >= maxSize) {
                    val oldestKey = cache.keys.firstOrNull()
                    if (oldestKey != null) {
                        cache.remove(oldestKey)
                    }
                }
                
                cache[value] = result
            }
            
            return result
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
         * видаляє значення з кешу
         *
         * @param value значення
         */
        fun removeFromCache(value: T) {
            synchronized(cacheLock) {
                cache.remove(value)
            }
        }
    }
    
    /**
     * створює валідатор з кешуванням
     *
     * @param T тип значення для валідації
     * @param validator валідатор
     * @return валідатор з кешуванням
     */
    fun <T> createCachedValidator(validator: Validator<T>): CachedValidator<T> {
        return CachedValidator(validator)
    }
    
    // функції для роботи з валідаторами з підтримкою метрик
    
    /**
     * представлення валідатора з метриками
     *
     * @param T тип значення для валідації
     */
    class MetricsValidator<T> : Validator<T> {
        private val validator: Validator<T>
        private var totalValidations = 0L
        private var failedValidations = 0L
        private var totalValidationTime = 0L
        private val metricsLock = Any()
        
        constructor(validator: Validator<T>) {
            this.validator = validator
        }
        
        override fun validate(value: T): ValidationResult {
            val startTime = System.currentTimeMillis()
            val result = validator.validate(value)
            val endTime = System.currentTimeMillis()
            
            synchronized(metricsLock) {
                totalValidations++
                if (!result.isValid) {
                    failedValidations++
                }
                totalValidationTime += (endTime - startTime)
            }
            
            return result
        }
        
        /**
         * отримує метрики валідації
         *
         * @return метрики валідації
         */
        fun getMetrics(): ValidationMetrics {
            synchronized(metricsLock) {
                return ValidationMetrics(
                    totalValidations = totalValidations,
                    failedValidations = failedValidations,
                    totalValidationTime = totalValidationTime,
                    averageValidationTime = if (totalValidations > 0) totalValidationTime.toDouble() / totalValidations else 0.0,
                    successRate = if (totalValidations > 0) (totalValidations - failedValidations).toDouble() / totalValidations else 0.0
                )
            }
        }
        
        /**
         * скидає метрики
         */
        fun resetMetrics() {
            synchronized(metricsLock) {
                totalValidations = 0
                failedValidations = 0
                totalValidationTime = 0
            }
        }
    }
    
    /**
     * представлення метрик валідації
     *
     * @property totalValidations загальна кількість валідацій
     * @property failedValidations кількість невдалих валідацій
     * @property totalValidationTime загальний час валідацій в мілісекундах
     * @property averageValidationTime середній час валідації в мілісекундах
     * @property successRate відсоток успішних валідацій
     */
    data class ValidationMetrics(
        val totalValidations: Long,
        val failedValidations: Long,
        val totalValidationTime: Long,
        val averageValidationTime: Double,
        val successRate: Double
    )
    
    /**
     * створює валідатор з метриками
     *
     * @param T тип значення для валідації
     * @param validator валідатор
     * @return валідатор з метриками
     */
    fun <T> createMetricsValidator(validator: Validator<T>): MetricsValidator<T> {
        return MetricsValidator(validator)
    }
    
    // функції для роботи з валідаторами з підтримкою логування
    
    /**
     * представлення валідатора з логуванням
     *
     * @param T тип значення для валідації
     */
    class LoggingValidator<T>(private val validator: Validator<T>, private val logger: LoggingUtils.Logger) : Validator<T> {
        
        override fun validate(value: T): ValidationResult {
            logger.debug("Початок валідації значення: $value")
            val startTime = System.currentTimeMillis()
            
            val result = validator.validate(value)
            
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            if (result.isValid) {
                logger.info("Валідація успішна для значення: $value (тривалість: ${duration}ms)")
            } else {
                logger.warn("Валідація неуспішна для значення: $value (тривалість: ${duration}ms, помилки: ${result.errors.joinToString(", ")})")
            }
            
            return result
        }
    }
    
    /**
     * створює валідатор з логуванням
     *
     * @param T тип значення для валідації
     * @param validator валідатор
     * @param logger логер
     * @return валідатор з логуванням
     */
    fun <T> createLoggingValidator(validator: Validator<T>, logger: LoggingUtils.Logger): LoggingValidator<T> {
        return LoggingValidator(validator, logger)
    }
    
    // функції для роботи з валідаторами з підтримкою транзакцій
    
    /**
     * представлення валідатора з транзакційною підтримкою
     *
     * @param T тип значення для валідації
     */
    class TransactionalValidator<T>(private val validator: Validator<T>) : Validator<T> {
        private val validationStack = mutableListOf<ValidationResult>()
        
        override fun validate(value: T): ValidationResult {
            val result = validator.validate(value)
            validationStack.add(result)
            return result
        }
        
        /**
         * відкатує останню валідацію
         *
         * @return true якщо відкат вдався
         */
        fun rollback(): Boolean {
            return if (validationStack.isNotEmpty()) {
                validationStack.removeAt(validationStack.size - 1)
                true
            } else {
                false
            }
        }
        
        /**
         * отримує історію валідацій
         *
         * @return список результатів валідацій
         */
        fun getValidationHistory(): List<ValidationResult> {
            return validationStack.toList()
        }
        
        /**
         * очищує історію валідацій
         */
        fun clearHistory() {
            validationStack.clear()
        }
    }
    
    /**
     * створює валідатор з транзакційною підтримкою
     *
     * @param T тип значення для валідації
     * @param validator валідатор
     * @return валідатор з транзакційною підтримкою
     */
    fun <T> createTransactionalValidator(validator: Validator<T>): TransactionalValidator<T> {
        return TransactionalValidator(validator)
    }
    
    // функції для роботи з валідаторами з підтримкою профілювання
    
    /**
     * представлення валідатора з профілюванням
     *
     * @param T тип значення для валідації
     */
    class ProfilingValidator<T>(private val validator: Validator<T>) : Validator<T> {
        private val profilingData = mutableListOf<ValidationProfile>()
        private val profilingLock = Any()
        private val maxSize = 10000
        
        override fun validate(value: T): ValidationResult {
            val startTime = System.nanoTime()
            val startMemory = getUsedMemory()
            
            val result = validator.validate(value)
            
            val endTime = System.nanoTime()
            val endMemory = getUsedMemory()
            
            val duration = (endTime - startTime) / 1_000_000 // конвертуємо в мілісекунди
            val memoryUsed = endMemory - startMemory
            
            synchronized(profilingLock) {
                // видаляємо найстаріші записи, якщо профіль переповнено
                if (profilingData.size >= maxSize) {
                    profilingData.removeAt(0)
                }
                
                profilingData.add(
                    ValidationProfile(
                        timestamp = System.currentTimeMillis(),
                        duration = duration,
                        memoryUsed = memoryUsed,
                        isValid = result.isValid,
                        errorCount = result.errors.size
                    )
                )
            }
            
            return result
        }
        
        /**
         * отримує дані профілювання
         *
         * @return список даних профілювання
         */
        fun getProfilingData(): List<ValidationProfile> {
            synchronized(profilingLock) {
                return profilingData.toList()
            }
        }
        
        /**
         * отримує статистику профілювання
         *
         * @return статистика профілювання
         */
        fun getProfilingStats(): ProfilingStats {
            synchronized(profilingLock) {
                if (profilingData.isEmpty()) {
                    return ProfilingStats(0, 0.0, 0.0, 0.0, 0.0, 0, 0)
                }
                
                val totalValidations = profilingData.size
                val totalDuration = profilingData.sumOf { it.duration }
                val totalMemory = profilingData.sumOf { it.memoryUsed }
                val successfulValidations = profilingData.count { it.isValid }
                val failedValidations = totalValidations - successfulValidations
                val totalErrors = profilingData.sumOf { it.errorCount }
                
                return ProfilingStats(
                    totalValidations = totalValidations,
                    averageDuration = totalDuration.toDouble() / totalValidations,
                    averageMemory = totalMemory.toDouble() / totalValidations,
                    successRate = successfulValidations.toDouble() / totalValidations,
                    errorRate = totalErrors.toDouble() / totalValidations,
                    minDuration = profilingData.minByOrNull { it.duration }?.duration ?: 0,
                    maxDuration = profilingData.maxByOrNull { it.duration }?.duration ?: 0
                )
            }
        }
        
        /**
         * очищує дані профілювання
         */
        fun clearProfilingData() {
            synchronized(profilingLock) {
                profilingData.clear()
            }
        }
        
        /**
         * отримує використану пам'ять
         *
         * @return використана пам'ять в байтах
         */
        private fun getUsedMemory(): Long {
            val runtime = Runtime.getRuntime()
            return runtime.totalMemory() - runtime.freeMemory()
        }
    }
    
    /**
     * представлення профілю валідації
     *
     * @property timestamp час валідації
     * @property duration тривалість валідації в мілісекундах
     * @property memoryUsed використана пам'ять в байтах
     * @property isValid чи валідація була успішною
     * @property errorCount кількість помилок
     */
    data class ValidationProfile(
        val timestamp: Long,
        val duration: Long,
        val memoryUsed: Long,
        val isValid: Boolean,
        val errorCount: Int
    )
    
    /**
     * представлення статистики профілювання
     *
     * @property totalValidations загальна кількість валідацій
     * @property averageDuration середня тривалість валідації в мілісекундах
     * @property averageMemory середнє використання пам'яті в байтах
     * @property successRate відсоток успішних валідацій
     * @property errorRate середня кількість помилок на валідацію
     * @property minDuration мінімальна тривалість валідації в мілісекундах
     * @property maxDuration максимальна тривалість валідації в мілісекундах
     */
    data class ProfilingStats(
        val totalValidations: Int,
        val averageDuration: Double,
        val averageMemory: Double,
        val successRate: Double,
        val errorRate: Double,
        val minDuration: Long,
        val maxDuration: Long
    )
    
    /**
     * створює валідатор з профілюванням
     *
     * @param T тип значення для валідації
     * @param validator валідатор
     * @return валідатор з профілюванням
     */
    fun <T> createProfilingValidator(validator: Validator<T>): ProfilingValidator<T> {
        return ProfilingValidator(validator)
    }
}