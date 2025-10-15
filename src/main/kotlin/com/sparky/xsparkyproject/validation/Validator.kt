package com.sparky.xsparkyproject.validation

/**
 * фреймворк валідації даних з декларативним dsl
 *
 * @author Андрій Будильников
 */
class Validator<T> private constructor(private val rules: List<ValidationRule<T>>) {
    
    companion object {
        /**
         * створює валідатор для типу t
         */
        fun <T> of(): ValidatorBuilder<T> {
            return ValidatorBuilder()
        }
    }
    
    /**
     * валідує значення проти всіх правил
     */
    fun validate(value: T): ValidationResult {
        val errors = mutableListOf<String>()
        
        for (rule in rules) {
            val result = rule.validate(value)
            if (result is ValidationError) {
                errors.add(result.message)
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationSuccess
        } else {
            ValidationErrorList(errors)
        }
    }
    
    class ValidatorBuilder<T> {
        private val rules = mutableListOf<ValidationRule<T>>()
        
        /**
         * додає правило валідації
         */
        fun addRule(rule: ValidationRule<T>): ValidatorBuilder<T> {
            rules.add(rule)
            return this
        }
        
        /**
         * створює валідатор з усіх доданих правил
         */
        fun build(): Validator<T> {
            return Validator(rules)
        }
    }
}

/**
 * базовий інтерфейс для правил валідації
 */
interface ValidationRule<T> {
    fun validate(value: T): ValidationResult
}

/**
 * результат валідації
 */
sealed class ValidationResult
object ValidationSuccess : ValidationResult()
data class ValidationError(val message: String) : ValidationResult()
data class ValidationErrorList(val errors: List<String>) : ValidationResult()

/**
 * правило перевірки на null
 */
class NotNullRule<T> : ValidationRule<T?> {
    override fun validate(value: T?): ValidationResult {
        return if (value == null) {
            ValidationError("Value cannot be null")
        } else {
            ValidationSuccess
        }
    }
}

/**
 * правило перевірки довжини рядка
 */
class StringLengthRule(private val min: Int, private val max: Int) : ValidationRule<String> {
    override fun validate(value: String): ValidationResult {
        return if (value.length < min || value.length > max) {
            ValidationError("String length must be between $min and $max characters")
        } else {
            ValidationSuccess
        }
    }
}

/**
 * правило перевірки email
 */
class EmailRule : ValidationRule<String> {
    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$".toRegex()
    
    override fun validate(value: String): ValidationResult {
        return if (emailRegex.matches(value)) {
            ValidationSuccess
        } else {
            ValidationError("Invalid email format")
        }
    }
}

/**
 * правило перевірки числового діапазону
 */
class RangeRule<T : Comparable<T>>(private val min: T, private val max: T) : ValidationRule<T> {
    override fun validate(value: T): ValidationResult {
        return if (value >= min && value <= max) {
            ValidationSuccess
        } else {
            ValidationError("Value must be between $min and $max")
        }
    }
}

/**
 * правило перевірки регулярного виразу
 */
class RegexRule(private val regex: Regex, private val errorMessage: String = "Value does not match pattern") : ValidationRule<String> {
    override fun validate(value: String): ValidationResult {
        return if (regex.matches(value)) {
            ValidationSuccess
        } else {
            ValidationError(errorMessage)
        }
    }
}

/**
 * правило обов'язкового заповнення
 */
class RequiredRule<T> : ValidationRule<T?> {
    override fun validate(value: T?): ValidationResult {
        return if (value != null && value.toString().isNotBlank()) {
            ValidationSuccess
        } else {
            ValidationError("Field is required")
        }
    }
}

/**
 * комбіноване правило, що виконує всі передані правила
 */
class CompositeRule<T>(private val rules: List<ValidationRule<T>>) : ValidationRule<T> {
    override fun validate(value: T): ValidationResult {
        val errors = mutableListOf<String>()
        
        for (rule in rules) {
            val result = rule.validate(value)
            if (result is ValidationError) {
                errors.add(result.message)
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationSuccess
        } else {
            ValidationErrorList(errors)
        }
    }
}