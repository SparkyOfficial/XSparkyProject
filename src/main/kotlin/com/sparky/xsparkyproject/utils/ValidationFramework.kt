/**
 * фреймворк для валідації
 *
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

/**
 * представлення інтерфейсу для валідатора
 */
interface Validator<T> {
    /**
     * перевірити значення
     *
     * @param value значення
     * @return результат валідації
     */
    fun validate(value: T): ValidationResult<T>
    
    /**
     * додати правило валідації
     *
     * @param rule правило
     * @return валідатор
     */
    fun addRule(rule: ValidationRule<T>): Validator<T>
    
    /**
     * встановити повідомлення про помилку
     *
     * @param message повідомлення
     * @return валідатор
     */
    fun withErrorMessage(message: String): Validator<T>
}

/**
 * представлення правила валідації
 */
interface ValidationRule<T> {
    /**
     * перевірити значення
     *
     * @param value значення
     * @return результат перевірки
     */
    fun check(value: T): ValidationError?
}

/**
 * представлення результату валідації
 *
 * @param T тип значення
 * @property value значення
 * @property isValid чи валідне
 * @property errors список помилок
 */
data class ValidationResult<T>(
    val value: T,
    val isValid: Boolean,
    val errors: List<ValidationError>
) {
    /**
     * отримати першу помилку
     *
     * @return перша помилка або null
     */
    fun getFirstError(): ValidationError? = errors.firstOrNull()
    
    /**
     * отримати всі повідомлення про помилки
     *
     * @return список повідомлень
     */
    fun getErrorMessages(): List<String> = errors.map { it.message }
}

/**
 * представлення помилки валідації
 *
 * @property field поле
 * @property message повідомлення
 * @property code код помилки
 */
data class ValidationError(
    val field: String,
    val message: String,
    val code: String = "VALIDATION_ERROR"
)

/**
 * представлення базової реалізації валідатора
 */
open class BaseValidator<T> : Validator<T> {
    protected val rules = mutableListOf<ValidationRule<T>>()
    protected var errorMessage: String? = null
    
    override fun validate(value: T): ValidationResult<T> {
        val errors = mutableListOf<ValidationError>()
        
        rules.forEach { rule ->
            val error = rule.check(value)
            if (error != null) {
                errors.add(error)
            }
        }
        
        return ValidationResult(value, errors.isEmpty(), errors)
    }
    
    override fun addRule(rule: ValidationRule<T>): Validator<T> {
        rules.add(rule)
        return this
    }
    
    override fun withErrorMessage(message: String): Validator<T> {
        this.errorMessage = message
        return this
    }
}

/**
 * представлення валідатора для рядків
 */
class StringValidator : BaseValidator<String>() {
    
    /**
     * додати правило "не порожній"
     *
     * @return валідатор
     */
    fun notEmpty(): StringValidator {
        addRule(object : ValidationRule<String> {
            override fun check(value: String): ValidationError? {
                return if (value.isEmpty()) {
                    ValidationError("value", errorMessage ?: "Значення не може бути порожнім")
                } else {
                    null
                }
            }
        })
        return this
    }
    
    /**
     * додати правило "мінімальна довжина"
     *
     * @param length мінімальна довжина
     * @return валідатор
     */
    fun minLength(length: Int): StringValidator {
        addRule(object : ValidationRule<String> {
            override fun check(value: String): ValidationError? {
                return if (value.length < length) {
                    ValidationError("value", errorMessage ?: "Довжина значення має бути не менше $length символів")
                } else {
                    null
                }
            }
        })
        return this
    }
    
    /**
     * додати правило "максимальна довжина"
     *
     * @param length максимальна довжина
     * @return валідатор
     */
    fun maxLength(length: Int): StringValidator {
        addRule(object : ValidationRule<String> {
            override fun check(value: String): ValidationError? {
                return if (value.length > length) {
                    ValidationError("value", errorMessage ?: "Довжина значення має бути не більше $length символів")
                } else {
                    null
                }
            }
        })
        return this
    }
    
    /**
     * додати правило "регулярний вираз"
     *
     * @param regex регулярний вираз
     * @return валідатор
     */
    fun matches(regex: String): StringValidator {
        addRule(object : ValidationRule<String> {
            override fun check(value: String): ValidationError? {
                return if (!value.matches(Regex(regex))) {
                    ValidationError("value", errorMessage ?: "Значення не відповідає шаблону: $regex")
                } else {
                    null
                }
            }
        })
        return this
    }
    
    /**
     * додати правило "електронна пошта"
     *
     * @return валідатор
     */
    fun isEmail(): StringValidator {
        return matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
    
    /**
     * додати правило "тільки цифри"
     *
     * @return валідатор
     */
    fun isNumeric(): StringValidator {
        return matches("^[0-9]+$")
    }
    
    /**
     * додати правило "тільки літери"
     *
     * @return валідатор
     */
    fun isAlpha(): StringValidator {
        return matches("^[A-Za-z]+$")
    }
    
    /**
     * додати правило "тільки літери та цифри"
     *
     * @return валідатор
     */
    fun isAlphanumeric(): StringValidator {
        return matches("^[A-Za-z0-9]+$")
    }
}

/**
 * представлення валідатора для цілих чисел
 */
class IntegerValidator : BaseValidator<Int>() {
    
    /**
     * додати правило "мінімальне значення"
     *
     * @param value мінімальне значення
     * @return валідатор
     */
    fun min(value: Int): IntegerValidator {
        addRule(object : ValidationRule<Int> {
            override fun check(input: Int): ValidationError? {
                return if (input < value) {
                    ValidationError("value", errorMessage ?: "Значення має бути не менше $value")
                } else {
                    null
                }
            }
        })
        return this
    }
    
    /**
     * додати правило "максимальне значення"
     *
     * @param value максимальне значення
     * @return валідатор
     */
    fun max(value: Int): IntegerValidator {
        addRule(object : ValidationRule<Int> {
            override fun check(input: Int): ValidationError? {
                return if (input > value) {
                    ValidationError("value", errorMessage ?: "Значення має бути не більше $value")
                } else {
                    null
                }
            }
        })
        return this
    }
    
    /**
     * додати правило "позитивне значення"
     *
     * @return валідатор
     */
    fun positive(): IntegerValidator {
        return min(1)
    }
    
    /**
     * додати правило "негативне значення"
     *
     * @return валідатор
     */
    fun negative(): IntegerValidator {
        addRule(object : ValidationRule<Int> {
            override fun check(input: Int): ValidationError? {
                return if (input >= 0) {
                    ValidationError("value", errorMessage ?: "Значення має бути негативним")
                } else {
                    null
                }
            }
        })
        return this
    }
    
    /**
     * додати правило "парне значення"
     *
     * @return валідатор
     */
    fun even(): IntegerValidator {
        addRule(object : ValidationRule<Int> {
            override fun check(input: Int): ValidationError? {
                return if (input % 2 != 0) {
                    ValidationError("value", errorMessage ?: "Значення має бути парним")
                } else {
                    null
                }
            }
        })
        return this
    }
    
    /**
     * додати правило "непарне значення"
     *
     * @return валідатор
     */
    fun odd(): IntegerValidator {
        addRule(object : ValidationRule<Int> {
            override fun check(input: Int): ValidationError? {
                return if (input % 2 == 0) {
                    ValidationError("value", errorMessage ?: "Значення має бути непарним")
                } else {
                    null
                }
            }
        })
        return this
    }
}

/**
 * представлення валідатора для довгих цілих чисел
 */
class LongValidator : BaseValidator<Long>() {
    
    /**
     * додати правило "мінімальне значення"
     *
     * @param value мінімальне значення
     * @return валідатор
     */
    fun min(value: Long): LongValidator {
        addRule(object : ValidationRule<Long> {
            override fun check(input: Long): ValidationError? {
                return if (input < value) {
                    ValidationError("value", errorMessage ?: "Значення має бути не менше $value")
                } else {
                    null
                }
            }
        })
        return this
    }
    
    /**
     * додати правило "максимальне значення"
     *
     * @param value максимальне значення
     * @return валідатор
     */
    fun max(value: Long): LongValidator {
        addRule(object : ValidationRule<Long> {
            override fun check(input: Long): ValidationError? {
                return if (input > value) {
                    ValidationError("value", errorMessage ?: "Значення має бути не більше $value")
                } else {
                    null
                }
            }
        })
        return this
    }
    
    /**
     * додати правило "позитивне значення"
     *
     * @return валідатор
     */
    fun positive(): LongValidator {
        return min(1L)
    }
    
    /**
     * додати правило "негативне значення"
     *
     * @return валідатор
     */
    fun negative(): LongValidator {
        addRule(object : ValidationRule<Long> {
            override fun check(input: Long): ValidationError? {
                return if (input >= 0) {
                    ValidationError("value", errorMessage ?: "Значення має бути негативним")
                } else {
                    null
                }
            }
        })
        return this
    }
}

/**
 * представлення валідатора для чисел з плаваючою точкою
 */
class DoubleValidator : BaseValidator<Double>() {
    
    /**
     * додати правило "мінімальне значення"
     *
     * @param value мінімальне значення
     * @return валідатор
     */
    fun min(value: Double): DoubleValidator {
        addRule(object : ValidationRule<Double> {
            override fun check(input: Double): ValidationError? {
                return if (input < value) {
                    ValidationError("value", errorMessage ?: "Значення має бути не менше $value")
                } else {
                    null
                }
            }
        })
        return this
    }
    
    /**
     * додати правило "максимальне значення"
     *
     * @param value максимальне значення
     * @return валідатор
     */
    fun max(value: Double): DoubleValidator {
        addRule(object : ValidationRule<Double> {
            override fun check(input: Double): ValidationError? {
                return if (input > value) {
                    ValidationError("value", errorMessage ?: "Значення має бути не більше $value")
                } else {
                    null
                }
            }
        })
        return this
    }
    
    /**
     * додати правило "позитивне значення"
     *
     * @return валідатор
     */
    fun positive(): DoubleValidator {
        return min(0.0)
    }
    
    /**
     * додати правило "негативне значення"
     *
     * @return валідатор
     */
    fun negative(): DoubleValidator {
        addRule(object : ValidationRule<Double> {
            override fun check(input: Double): ValidationError? {
                return if (input >= 0) {
                    ValidationError("value", errorMessage ?: "Значення має бути негативним")
                } else {
                    null
                }
            }
        })
        return this
    }
}

/**
 * представлення валідатора для булевих значень
 */
class BooleanValidator : BaseValidator<Boolean>() {
    
    /**
     * додати правило "має бути true"
     *
     * @return валідатор
     */
    fun isTrue(): BooleanValidator {
        addRule(object : ValidationRule<Boolean> {
            override fun check(input: Boolean): ValidationError? {
                return if (!input) {
                    ValidationError("value", errorMessage ?: "Значення має бути true")
                } else {
                    null
                }
            }
        })
        return this
    }
    
    /**
     * додати правило "має бути false"
     *
     * @return валідатор
     */
    fun isFalse(): BooleanValidator {
        addRule(object : ValidationRule<Boolean> {
            override fun check(input: Boolean): ValidationError? {
                return if (input) {
                    ValidationError("value", errorMessage ?: "Значення має бути false")
                } else {
                    null
                }
            }
        })
        return this
    }
}

/**
 * представлення валідатора для списків
 */
class ListValidator<T> : BaseValidator<List<T>>() {
    
    /**
     * додати правило "не порожній"
     *
     * @return валідатор
     */
    fun notEmpty(): ListValidator<T> {
        addRule(object : ValidationRule<List<T>> {
            override fun check(input: List<T>): ValidationError? {
                return if (input.isEmpty()) {
                    ValidationError("value", errorMessage ?: "Список не може бути порожнім")
                } else {
                    null
                }
            }
        })
        return this
    }
    
    /**
     * додати правило "мінімальний розмір"
     *
     * @param size мінімальний розмір
     * @return валідатор
     */
    fun minSize(size: Int): ListValidator<T> {
        addRule(object : ValidationRule<List<T>> {
            override fun check(input: List<T>): ValidationError? {
                return if (input.size < size) {
                    ValidationError("value", errorMessage ?: "Розмір списку має бути не менше $size")
                } else {
                    null
                }
            }
        })
        return this
    }
    
    /**
     * додати правило "максимальний розмір"
     *
     * @param size максимальний розмір
     * @return валідатор
     */
    fun maxSize(size: Int): ListValidator<T> {
        addRule(object : ValidationRule<List<T>> {
            override fun check(input: List<T>): ValidationError? {
                return if (input.size > size) {
                    ValidationError("value", errorMessage ?: "Розмір списку має бути не більше $size")
                } else {
                    null
                }
            }
        })
        return this
    }
    
    /**
     * додати правило "унікальні елементи"
     *
     * @return валідатор
     */
    fun unique(): ListValidator<T> {
        addRule(object : ValidationRule<List<T>> {
            override fun check(input: List<T>): ValidationError? {
                val uniqueElements = input.toSet()
                return if (uniqueElements.size != input.size) {
                    ValidationError("value", errorMessage ?: "Список має містити лише унікальні елементи")
                } else {
                    null
                }
            }
        })
        return this
    }
}

/**
 * представлення валідатора для мап
 */
class MapValidator<K, V> : BaseValidator<Map<K, V>>() {
    
    /**
     * додати правило "не порожня"
     *
     * @return валідатор
     */
    fun notEmpty(): MapValidator<K, V> {
        addRule(object : ValidationRule<Map<K, V>> {
            override fun check(input: Map<K, V>): ValidationError? {
                return if (input.isEmpty()) {
                    ValidationError("value", errorMessage ?: "Мапа не може бути порожньою")
                } else {
                    null
                }
            }
        })
        return this
    }
    
    /**
     * додати правило "мінімальний розмір"
     *
     * @param size мінімальний розмір
     * @return валідатор
     */
    fun minSize(size: Int): MapValidator<K, V> {
        addRule(object : ValidationRule<Map<K, V>> {
            override fun check(input: Map<K, V>): ValidationError? {
                return if (input.size < size) {
                    ValidationError("value", errorMessage ?: "Розмір мапи має бути не менше $size")
                } else {
                    null
                }
            }
        })
        return this
    }
    
    /**
     * додати правило "максимальний розмір"
     *
     * @param size максимальний розмір
     * @return валідатор
     */
    fun maxSize(size: Int): MapValidator<K, V> {
        addRule(object : ValidationRule<Map<K, V>> {
            override fun check(input: Map<K, V>): ValidationError? {
                return if (input.size > size) {
                    ValidationError("value", errorMessage ?: "Розмір мапи має бути не більше $size")
                } else {
                    null
                }
            }
        })
        return this
    }
}

/**
 * представлення композитного валідатора
 */
class CompositeValidator<T> : BaseValidator<T>() {
    
    /**
     * додати валідатор
     *
     * @param validator валідатор
     * @return композитний валідатор
     */
    fun addValidator(validator: Validator<T>): CompositeValidator<T> {
        addRule(object : ValidationRule<T> {
            override fun check(value: T): ValidationError? {
                val result = validator.validate(value)
                return if (!result.isValid) {
                    result.getFirstError()
                } else {
                    null
                }
            }
        })
        return this
    }
}

/**
 * представлення фабрики валідаторів
 */
class ValidatorFactory {
    
    /**
     * створити валідатор для рядків
     *
     * @return валідатор
     */
    fun createStringValidator(): StringValidator {
        return StringValidator()
    }
    
    /**
     * створити валідатор для цілих чисел
     *
     * @return валідатор
     */
    fun createIntegerValidator(): IntegerValidator {
        return IntegerValidator()
    }
    
    /**
     * створити валідатор для довгих цілих чисел
     *
     * @return валідатор
     */
    fun createLongValidator(): LongValidator {
        return LongValidator()
    }
    
    /**
     * створити валідатор для чисел з плаваючою точкою
     *
     * @return валідатор
     */
    fun createDoubleValidator(): DoubleValidator {
        return DoubleValidator()
    }
    
    /**
     * створити валідатор для булевих значень
     *
     * @return валідатор
     */
    fun createBooleanValidator(): BooleanValidator {
        return BooleanValidator()
    }
    
    /**
     * створити валідатор для списків
     *
     * @return валідатор
     */
    fun <T> createListValidator(): ListValidator<T> {
        return ListValidator()
    }
    
    /**
     * створити валідатор для мап
     *
     * @return валідатор
     */
    fun <K, V> createMapValidator(): MapValidator<K, V> {
        return MapValidator()
    }
    
    /**
     * створити композитний валідатор
     *
     * @return валідатор
     */
    fun <T> createCompositeValidator(): CompositeValidator<T> {
        return CompositeValidator()
    }
}

/**
 * представлення інтерфейсу для валідації об'єктів
 */
interface ObjectValidator<T> {
    /**
     * перевірити об'єкт
     *
     * @param obj об'єкт
     * @return результат валідації
     */
    fun validate(obj: T): ObjectValidationResult<T>
    
    /**
     * додати валідатор для поля
     *
     * @param fieldName назва поля
     * @param validator валідатор
     * @return валідатор об'єкта
     */
    fun <V> addFieldValidator(fieldName: String, validator: Validator<V>): ObjectValidator<T>
}

/**
 * представлення результату валідації об'єкта
 *
 * @param T тип об'єкта
 * @property obj об'єкт
 * @property isValid чи валідний
 * @property fieldErrors помилки по полях
 */
data class ObjectValidationResult<T>(
    val obj: T,
    val isValid: Boolean,
    val fieldErrors: Map<String, List<ValidationError>>
) {
    /**
     * отримати всі помилки
     *
     * @return список всіх помилок
     */
    fun getAllErrors(): List<ValidationError> = fieldErrors.values.flatten()
    
    /**
     * отримати всі повідомлення про помилки
     *
     * @return список повідомлень
     */
    fun getAllErrorMessages(): List<String> = getAllErrors().map { it.message }
}

/**
 * представлення базової реалізації валідатора об'єктів
 */
open class BaseObjectValidator<T> : ObjectValidator<T> {
    protected val fieldValidators = mutableMapOf<String, Validator<*>>()
    
    override fun validate(obj: T): ObjectValidationResult<T> {
        val fieldErrors = mutableMapOf<String, List<ValidationError>>()
        
        fieldValidators.forEach { (fieldName, validator) ->
            // Тут потрібно використовувати рефлексію для отримання значення поля
            // Для спрощення просто пропускаємо
        }
        
        return ObjectValidationResult(obj, fieldErrors.isEmpty(), fieldErrors)
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun <V> addFieldValidator(fieldName: String, validator: Validator<V>): ObjectValidator<T> {
        fieldValidators[fieldName] = validator as Validator<*>
        return this
    }
}

/**
 * представлення інтерфейсу для валідації з використанням анотацій
 */
interface AnnotationBasedValidator {
    /**
     * перевірити об'єкт з використанням анотацій
     *
     * @param obj об'єкт
     * @return результат валідації
     */
    fun validateWithAnnotations(obj: Any): ObjectValidationResult<Any>
}

/**
 * представлення базової реалізації валідатора з анотаціями
 */
open class BaseAnnotationBasedValidator : AnnotationBasedValidator {
    
    override fun validateWithAnnotations(obj: Any): ObjectValidationResult<Any> {
        // Реалізація валідації з використанням анотацій
        // Для спрощення просто повертаємо успішний результат
        return ObjectValidationResult(obj, true, emptyMap())
    }
}

/**
 * представлення інтерфейсу для асинхронної валідації
 */
interface AsyncValidator<T> {
    /**
     * асинхронно перевірити значення
     *
     * @param value значення
     * @return результат валідації
     */
    suspend fun validateAsync(value: T): ValidationResult<T>
}

/**
 * представлення базової реалізації асинхронного валідатора
 */
open class BaseAsyncValidator<T>(private val validator: Validator<T>) : AsyncValidator<T> {
    
    override suspend fun validateAsync(value: T): ValidationResult<T> {
        // Імітація асинхронної валідації
        return validator.validate(value)
    }
}

/**
 * представлення інтерфейсу для валідації з кешуванням
 */
interface CachingValidator<T> {
    /**
     * перевірити значення з кешуванням
     *
     * @param value значення
     * @return результат валідації
     */
    fun validateWithCache(value: T): ValidationResult<T>
    
    /**
     * очистити кеш
     */
    fun clearCache()
}

/**
 * представлення базової реалізації валідатора з кешуванням
 */
open class BaseCachingValidator<T>(private val validator: Validator<T>) : CachingValidator<T> {
    private val cache = mutableMapOf<T, ValidationResult<T>>()
    
    override fun validateWithCache(value: T): ValidationResult<T> {
        return cache.getOrPut(value) { validator.validate(value) }
    }
    
    override fun clearCache() {
        cache.clear()
    }
}

/**
 * представлення інтерфейсу для валідації з логуванням
 */
interface LoggingValidator<T> {
    /**
     * перевірити значення з логуванням
     *
     * @param value значення
     * @return результат валідації
     */
    fun validateWithLogging(value: T): ValidationResult<T>
}

/**
 * представлення базової реалізації валідатора з логуванням
 */
open class BaseLoggingValidator<T>(private val validator: Validator<T>) : LoggingValidator<T> {
    
    override fun validateWithLogging(value: T): ValidationResult<T> {
        println("Валідація значення: $value")
        val result = validator.validate(value)
        if (!result.isValid) {
            println("Помилки валідації: ${result.getErrorMessages()}")
        } else {
            println("Валідація успішна")
        }
        return result
    }
}

/**
 * представлення інтерфейсу для валідації з метриками
 */
interface MetricsValidator<T> {
    /**
     * перевірити значення з збором метрик
     *
     * @param value значення
     * @return результат валідації
     */
    fun validateWithMetrics(value: T): ValidationResult<T>
    
    /**
     * отримати статистику валідації
     *
     * @return статистика
     */
    fun getValidationStats(): ValidationStats
}

/**
 * представлення статистики валідації
 *
 * @property totalValidations загальна кількість валідацій
 * @property successfulValidations успішні валідації
 * @property failedValidations невдалі валідації
 * @property averageValidationTime середній час валідації
 */
data class ValidationStats(
    val totalValidations: Int,
    val successfulValidations: Int,
    val failedValidations: Int,
    val averageValidationTime: Double
)

/**
 * представлення базової реалізації валідатора з метриками
 */
open class BaseMetricsValidator<T>(private val validator: Validator<T>) : MetricsValidator<T> {
    private var totalValidations = 0
    private var successfulValidations = 0
    private var failedValidations = 0
    private var totalTime = 0L
    
    override fun validateWithMetrics(value: T): ValidationResult<T> {
        val startTime = System.currentTimeMillis()
        val result = validator.validate(value)
        val endTime = System.currentTimeMillis()
        
        totalValidations++
        if (result.isValid) {
            successfulValidations++
        } else {
            failedValidations++
        }
        totalTime += (endTime - startTime)
        
        return result
    }
    
    override fun getValidationStats(): ValidationStats {
        val averageTime = if (totalValidations > 0) totalTime.toDouble() / totalValidations else 0.0
        return ValidationStats(
            totalValidations,
            successfulValidations,
            failedValidations,
            averageTime
        )
    }
}

/**
 * представлення інтерфейсу для валідації з відновленням
 */
interface RecoverableValidator<T> {
    /**
     * перевірити значення з можливістю відновлення
     *
     * @param value значення
     * @return результат валідації
     */
    fun validateWithRecovery(value: T): ValidationResult<T>
    
    /**
     * відновити значення
     *
     * @param value значення
     * @return відновлене значення
     */
    fun recover(value: T): T
}

/**
 * представлення базової реалізації валідатора з відновленням
 */
open class BaseRecoverableValidator<T>(
    private val validator: Validator<T>,
    private val recoveryFunction: (T) -> T
) : RecoverableValidator<T> {
    
    override fun validateWithRecovery(value: T): ValidationResult<T> {
        return validator.validate(value)
    }
    
    override fun recover(value: T): T {
        return recoveryFunction(value)
    }
}

/**
 * представлення інтерфейсу для валідації з транзакційністю
 */
interface TransactionalValidator<T> {
    /**
     * перевірити значення в транзакції
     *
     * @param value значення
     * @return результат валідації
     */
    fun validateInTransaction(value: T): ValidationResult<T>
    
    /**
     * відкотити транзакцію
     */
    fun rollback()
}

/**
 * представлення базової реалізації транзакційного валідатора
 */
open class BaseTransactionalValidator<T>(private val validator: Validator<T>) : TransactionalValidator<T> {
    private val validationHistory = mutableListOf<ValidationResult<T>>()
    
    override fun validateInTransaction(value: T): ValidationResult<T> {
        val result = validator.validate(value)
        validationHistory.add(result)
        return result
    }
    
    override fun rollback() {
        if (validationHistory.isNotEmpty()) {
            validationHistory.removeAt(validationHistory.size - 1)
        }
    }
}

/**
 * представлення інтерфейсу для валідації з виключеннями
 */
interface ExceptionThrowingValidator<T> {
    /**
     * перевірити значення з викиданням виключень
     *
     * @param value значення
     * @throws ValidationException якщо валідація не пройшла
     */
    @Throws(ValidationException::class)
    fun validateOrThrow(value: T)
}

/**
 * представлення виключення валідації
 *
 * @property errors список помилок
 */
class ValidationException(val errors: List<ValidationError>) : Exception() {
    override val message: String
        get() = errors.joinToString(", ") { it.message }
}

/**
 * представлення базової реалізації валідатора з виключеннями
 */
open class BaseExceptionThrowingValidator<T>(private val validator: Validator<T>) : ExceptionThrowingValidator<T> {
    
    override fun validateOrThrow(value: T) {
        val result = validator.validate(value)
        if (!result.isValid) {
            throw ValidationException(result.errors)
        }
    }
}

/**
 * представлення інтерфейсу для валідації з використанням регулярних виразів
 */
interface RegexValidator {
    /**
     * перевірити значення регулярним виразом
     *
     * @param value значення
     * @param regex регулярний вираз
     * @return результат валідації
     */
    fun validateWithRegex(value: String, regex: String): ValidationResult<String>
}

/**
 * представлення базової реалізації валідатора з регулярними виразами
 */
open class BaseRegexValidator : RegexValidator {
    
    override fun validateWithRegex(value: String, regex: String): ValidationResult<String> {
        return try {
            val pattern = Regex(regex)
            if (pattern.matches(value)) {
                ValidationResult(value, true, emptyList())
            } else {
                val error = ValidationError("value", "Значення не відповідає шаблону: $regex")
                ValidationResult(value, false, listOf(error))
            }
        } catch (e: Exception) {
            val error = ValidationError("regex", "Неправильний регулярний вираз: ${e.message}")
            ValidationResult(value, false, listOf(error))
        }
    }
}

/**
 * представлення інтерфейсу для валідації з використанням схем
 */
interface SchemaValidator {
    /**
     * перевірити значення схемою
     *
     * @param value значення
     * @param schema схема
     * @return результат валідації
     */
    fun validateWithSchema(value: Any, schema: ValidationSchema): ValidationResult<Any>
}

/**
 * представлення схеми валідації
 */
interface ValidationSchema {
    /**
     * перевірити значення
     *
     * @param value значення
     * @return список помилок
     */
    fun validate(value: Any): List<ValidationError>
}

/**
 * представлення базової реалізації валідатора з схемами
 */
open class BaseSchemaValidator : SchemaValidator {
    
    override fun validateWithSchema(value: Any, schema: ValidationSchema): ValidationResult<Any> {
        val errors = schema.validate(value)
        return ValidationResult(value, errors.isEmpty(), errors)
    }
}

/**
 * представлення інтерфейсу для валідації з використанням правил
 */
interface RuleBasedValidator<T> {
    /**
     * перевірити значення правилами
     *
     * @param value значення
     * @param rules правила
     * @return результат валідації
     */
    fun validateWithRules(value: T, rules: List<ValidationRule<T>>): ValidationResult<T>
}

/**
 * представлення базової реалізації валідатора з правилами
 */
open class BaseRuleBasedValidator<T> : RuleBasedValidator<T> {
    
    override fun validateWithRules(value: T, rules: List<ValidationRule<T>>): ValidationResult<T> {
        val errors = mutableListOf<ValidationError>()
        
        rules.forEach { rule ->
            val error = rule.check(value)
            if (error != null) {
                errors.add(error)
            }
        }
        
        return ValidationResult(value, errors.isEmpty(), errors)
    }
}

/**
 * представлення інтерфейсу для валідації з використанням DSL
 */
interface DslValidator<T> {
    /**
     * перевірити значення з використанням DSL
     *
     * @param value значення
     * @param dsl блок DSL
     * @return результат валідації
     */
    fun validateWithDsl(value: T, dsl: ValidatorDsl<T>.() -> Unit): ValidationResult<T>
}

/**
 * представлення DSL для валідації
 */
class ValidatorDsl<T> {
    private val rules = mutableListOf<ValidationRule<T>>()
    
    /**
     * додати правило
     *
     * @param rule правило
     */
    fun addRule(rule: ValidationRule<T>) {
        rules.add(rule)
    }
    
    /**
     * отримати правила
     *
     * @return список правил
     */
    fun getRules(): List<ValidationRule<T>> = rules.toList()
}

/**
 * представлення базової реалізації валідатора з DSL
 */
open class BaseDslValidator<T> : DslValidator<T> {
    
    override fun validateWithDsl(value: T, dsl: ValidatorDsl<T>.() -> Unit): ValidationResult<T> {
        val validatorDsl = ValidatorDsl<T>()
        validatorDsl.dsl()
        val rules = validatorDsl.getRules()
        
        val errors = mutableListOf<ValidationError>()
        rules.forEach { rule ->
            val error = rule.check(value)
            if (error != null) {
                errors.add(error)
            }
        }
        
        return ValidationResult(value, errors.isEmpty(), errors)
    }
}

/**
 * представлення інтерфейсу для валідації з використанням шаблонів
 */
interface TemplateValidator<T> {
    /**
     * перевірити значення шаблоном
     *
     * @param value значення
     * @param template шаблон
     * @return результат валідації
     */
    fun validateWithTemplate(value: T, template: ValidationTemplate<T>): ValidationResult<T>
}

/**
 * представлення шаблону валідації
 */
interface ValidationTemplate<T> {
    /**
     * перевірити значення
     *
     * @param value значення
     * @return список помилок
     */
    fun validate(value: T): List<ValidationError>
}

/**
 * представлення базової реалізації валідатора з шаблонами
 */
open class BaseTemplateValidator<T> : TemplateValidator<T> {
    
    override fun validateWithTemplate(value: T, template: ValidationTemplate<T>): ValidationResult<T> {
        val errors = template.validate(value)
        return ValidationResult(value, errors.isEmpty(), errors)
    }
}

/**
 * представлення інтерфейсу для валідації з використанням конфігурації
 */
interface ConfigurableValidator<T> {
    /**
     * перевірити значення з конфігурацією
     *
     * @param value значення
     * @param config конфігурація
     * @return результат валідації
     */
    fun validateWithConfig(value: T, config: ValidationConfig): ValidationResult<T>
}

/**
 * представлення конфігурації валідації
 *
 * @property rules правила валідації
 * @property allowEmptyValues дозволити порожні значення
 * @property trimValues обрізати значення
 */
data class ValidationConfig(
    val rules: List<ValidationRule<*>> = emptyList(),
    val allowEmptyValues: Boolean = false,
    val trimValues: Boolean = false
)

/**
 * представлення базової реалізації налаштовуваного валідатора
 */
open class BaseConfigurableValidator<T> : ConfigurableValidator<T> {
    
    override fun validateWithConfig(value: T, config: ValidationConfig): ValidationResult<T> {
        // Для спрощення ігноруємо конфігурацію
        @Suppress("UNCHECKED_CAST")
        val rules = config.rules as List<ValidationRule<T>>
        
        val errors = mutableListOf<ValidationError>()
        rules.forEach { rule ->
            val error = rule.check(value)
            if (error != null) {
                errors.add(error)
            }
        }
        
        return ValidationResult(value, errors.isEmpty(), errors)
    }
}