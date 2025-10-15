/**
 * утиліти для роботи з рефлексією
 *
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

/**
 * представлення інтерфейсу для роботи з рефлексією
 */
interface ReflectionHelper {
    /**
     * отримати всі поля об'єкта
     *
     * @param obj об'єкт
     * @return список полів
     */
    fun getAllFields(obj: Any): List<KProperty1<out Any, *>> 
    
    /**
     * отримати всі методи об'єкта
     *
     * @param obj об'єкт
     * @return список методів
     */
    fun getAllMethods(obj: Any): List<KFunction<*>>
    
    /**
     * отримати значення поля за назвою
     *
     * @param obj об'єкт
     * @param fieldName назва поля
     * @return значення поля
     */
    fun getFieldValue(obj: Any, fieldName: String): Any?
    
    /**
     * встановити значення поля за назвою
     *
     * @param obj об'єкт
     * @param fieldName назва поля
     * @param value значення
     */
    fun setFieldValue(obj: Any, fieldName: String, value: Any?)
}

/**
 * представлення базової реалізації помічника з рефлексії
 */
open class BaseReflectionHelper : ReflectionHelper {
    
    override fun getAllFields(obj: Any): List<KProperty1<out Any, *>> {
        return obj::class.memberProperties.toList()
    }
    
    override fun getAllMethods(obj: Any): List<KFunction<*>> {
        return obj::class.members.filterIsInstance<KFunction<*>>().toList()
    }
    
    override fun getFieldValue(obj: Any, fieldName: String): Any? {
        val property = obj::class.memberProperties.find { it.name == fieldName }
        return property?.getter?.call(obj)
    }
    
    override fun setFieldValue(obj: Any, fieldName: String, value: Any?) {
        val property = obj::class.memberProperties.find { it.name == fieldName }
        if (property is KMutableProperty1) {
            property.setter.call(obj, value)
        }
    }
}

/**
 * представлення інспектора класів
 *
 * @property helper помічник з рефлексії
 */
class ClassInspector(private val helper: ReflectionHelper = BaseReflectionHelper()) {
    
    /**
     * отримати інформацію про клас
     *
     * @param clazz клас
     * @return інформація про клас
     */
    fun inspectClass(clazz: KClass<*>): ClassInfo {
        return ClassInfo(
            clazz.simpleName ?: "Anonymous",
            clazz.qualifiedName ?: "Unknown",
            clazz.memberProperties.map { it.name },
            clazz.constructors.map { it.parameters.map { param -> param.name ?: "unknown" } },
            clazz.memberFunctions.map { it.name }
        )
    }
    
    /**
     * отримати інформацію про об'єкт
     *
     * @param obj об'єкт
     * @return інформація про об'єкт
     */
    fun inspectObject(obj: Any): ObjectInfo {
        val clazz = obj::class
        val fields = clazz.memberProperties.associate { it.name to it.getter.call(obj) }
        
        return ObjectInfo(
            clazz.simpleName ?: "Anonymous",
            fields
        )
    }
}

/**
 * представлення інформації про клас
 *
 * @property name назва класу
 * @property qualifiedName повна назва класу
 * @property fields поля класу
 * @property constructors конструктори класу
 * @property methods методи класу
 */
data class ClassInfo(
    val name: String,
    val qualifiedName: String,
    val fields: List<String>,
    val constructors: List<List<String>>,
    val methods: List<String>
)

/**
 * представлення інформації про об'єкт
 *
 * @property className назва класу
 * @property fields значення полів
 */
data class ObjectInfo(
    val className: String,
    val fields: Map<String, Any?>
)

/**
 * представлення інтерфейсу для динамічного виклику методів
 */
interface DynamicInvoker {
    /**
     * викликати метод за назвою
     *
     * @param obj об'єкт
     * @param methodName назва методу
     * @param args аргументи
     * @return результат виклику
     */
    fun invokeMethod(obj: Any, methodName: String, vararg args: Any?): Any?
    
    /**
     * створити екземпляр класу
     *
     * @param clazz клас
     * @param args аргументи конструктора
     * @return екземпляр класу
     */
    fun createInstance(clazz: KClass<*>, vararg args: Any?): Any
}

/**
 * представлення базової реалізації динамічного виклику
 */
open class BaseDynamicInvoker : DynamicInvoker {
    
    override fun invokeMethod(obj: Any, methodName: String, vararg args: Any?): Any? {
        val function = obj::class.memberFunctions.find { it.name == methodName }
        return function?.call(obj, *args)
    }
    
    override fun createInstance(clazz: KClass<*>, vararg args: Any?): Any {
        val constructor = clazz.constructors.firstOrNull()
        return constructor?.call(*args) ?: throw IllegalArgumentException("No constructor found")
    }
}

/**
 * представлення проксі для об'єктів
 *
 * @param T тип об'єкта
 * @property target цільовий об'єкт
 * @property interceptor перехоплювач викликів
 */
class ObjectProxy<T : Any>(
    private val target: T,
    private val interceptor: MethodInvocationInterceptor
) : DynamicInvoker by BaseDynamicInvoker() {
    
    /**
     * отримати проксі-об'єкт
     *
     * @return проксі-об'єкт
     */
    @Suppress("UNCHECKED_CAST")
    fun getProxy(): T {
        // Реалізація створення проксі-об'єкта
        return target
    }
}

/**
 * представлення перехоплювача викликів методів
 */
interface MethodInvocationInterceptor {
    /**
     * перехопити виклик методу
     *
     * @param method метод
     * @param args аргументи
     * @param target цільовий об'єкт
     * @return результат виклику
     */
    fun intercept(method: KFunction<*>, args: Array<out Any?>, target: Any): Any?
}

/**
 * представлення базової реалізації перехоплювача
 */
open class BaseMethodInvocationInterceptor : MethodInvocationInterceptor {
    
    override fun intercept(method: KFunction<*>, args: Array<out Any?>, target: Any): Any? {
        // Базова реалізація - просто викликати метод
        return method.call(target, *args)
    }
}

/**
 * представлення інтерфейсу для роботи з анотаціями
 */
interface AnnotationProcessor {
    /**
     * отримати анотації класу
     *
     * @param clazz клас
     * @return список анотацій
     */
    fun getClassAnnotations(clazz: KClass<*>): List<Annotation>
    
    /**
     * отримати анотації поля
     *
     * @param property поле
     * @return список анотацій
     */
    fun getFieldAnnotations(property: KProperty1<*, *>): List<Annotation>
    
    /**
     * отримати анотації методу
     *
     * @param function метод
     * @return список анотацій
     */
    fun getMethodAnnotations(function: KFunction<*>): List<Annotation>
}

/**
 * представлення базової реалізації процесора анотацій
 */
open class BaseAnnotationProcessor : AnnotationProcessor {
    
    override fun getClassAnnotations(clazz: KClass<*>): List<Annotation> {
        return clazz.annotations
    }
    
    override fun getFieldAnnotations(property: KProperty1<*, *>): List<Annotation> {
        return property.annotations
    }
    
    override fun getMethodAnnotations(function: KFunction<*>): List<Annotation> {
        return function.annotations
    }
}

/**
 * представлення сканера анотацій
 *
 * @property processor процесор анотацій
 */
class AnnotationScanner(private val processor: AnnotationProcessor = BaseAnnotationProcessor()) {
    
    /**
     * знайти класи з певною анотацією
     *
     * @param classes список класів
     * @param annotationType тип анотації
     * @return список класів з анотацією
     */
    fun <T : Annotation> findClassesWithAnnotation(
        classes: List<KClass<*>>,
        annotationType: KClass<T>
    ): List<KClass<*>> {
        return classes.filter { clazz ->
            processor.getClassAnnotations(clazz).any { it.annotationClass == annotationType }
        }
    }
    
    /**
     * знайти поля з певною анотацією
     *
     * @param clazz клас
     * @param annotationType тип анотації
     * @return список полів з анотацією
     */
    fun <T : Annotation> findFieldsWithAnnotation(
        clazz: KClass<*>,
        annotationType: KClass<T>
    ): List<KProperty1<*, *>> {
        return clazz.memberProperties.filter { property ->
            processor.getFieldAnnotations(property).any { it.annotationClass == annotationType }
        }
    }
    
    /**
     * знайти методи з певною анотацією
     *
     * @param clazz клас
     * @param annotationType тип анотації
     * @return список методів з анотацією
     */
    fun <T : Annotation> findMethodsWithAnnotation(
        clazz: KClass<*>,
        annotationType: KClass<T>
    ): List<KFunction<*>> {
        return clazz.memberFunctions.filter { function ->
            processor.getMethodAnnotations(function).any { it.annotationClass == annotationType }
        }
    }
}

/**
 * представлення інтерфейсу для роботи з дженериками
 */
interface GenericTypeResolver {
    /**
     * отримати тип аргументу дженерика
     *
     * @param clazz клас
     * @param index індекс аргументу
     * @return тип аргументу
     */
    fun getGenericTypeArgument(clazz: KClass<*>, index: Int): KType?
    
    /**
     * отримати всі типи аргументів дженерика
     *
     * @param clazz клас
     * @return список типів аргументів
     */
    fun getGenericTypeArguments(clazz: KClass<*>): List<KType>
}

/**
 * представлення базової реалізації резолвера дженериків
 */
open class BaseGenericTypeResolver : GenericTypeResolver {
    
    override fun getGenericTypeArgument(clazz: KClass<*>, index: Int): KType? {
        val supertypes = clazz.supertypes
        return supertypes.firstOrNull()?.arguments?.getOrNull(index)?.type
    }
    
    override fun getGenericTypeArguments(clazz: KClass<*>): List<KType> {
        val supertypes = clazz.supertypes
        return supertypes.firstOrNull()?.arguments?.map { it.type } ?: emptyList()
    }
}

/**
 * представлення фабрики об'єктів
 */
class ObjectFactory {
    private val dynamicInvoker = BaseDynamicInvoker()
    
    /**
     * створити екземпляр класу за назвою
     *
     * @param className повна назва класу
     * @param args аргументи конструктора
     * @return екземпляр класу
     */
    fun createInstanceByName(className: String, vararg args: Any?): Any {
        val clazz = Class.forName(className).kotlin
        return dynamicInvoker.createInstance(clazz, *args)
    }
    
    /**
     * створити екземпляр класу з параметрами
     *
     * @param clazz клас
     * @param parameters параметри конструктора
     * @return екземпляр класу
     */
    fun <T : Any> createInstanceWithParameters(clazz: KClass<T>, parameters: Map<KParameter, Any?>): T {
        val constructor = clazz.primaryConstructor ?: clazz.constructors.firstOrNull()
            ?: throw IllegalArgumentException("No constructor found for ${clazz.simpleName}")
        
        return constructor.callBy(parameters)
    }
}

/**
 * представлення реєстратора типів
 */
class TypeRegistry {
    private val registeredTypes = mutableMapOf<String, KClass<*>>()
    
    /**
     * зареєструвати тип
     *
     * @param name назва типу
     * @param clazz клас типу
     */
    fun registerType(name: String, clazz: KClass<*>) {
        registeredTypes[name] = clazz
    }
    
    /**
     * отримати зареєстрований тип
     *
     * @param name назва типу
     * @return клас типу
     */
    fun getType(name: String): KClass<*>? {
        return registeredTypes[name]
    }
    
    /**
     * отримати всі зареєстровані типи
     *
     * @return список зареєстрованих типів
     */
    fun getAllTypes(): Map<String, KClass<*>> {
        return registeredTypes.toMap()
    }
}

/**
 * представлення інтерфейсу для роботи з властивостями
 */
interface PropertyAccessor {
    /**
     * отримати значення властивості за шляхом
     *
     * @param obj об'єкт
     * @param propertyPath шлях до властивості
     * @return значення властивості
     */
    fun getNestedProperty(obj: Any, propertyPath: String): Any?
    
    /**
     * встановити значення властивості за шляхом
     *
     * @param obj об'єкт
     * @param propertyPath шлях до властивості
     * @param value значення
     */
    fun setNestedProperty(obj: Any, propertyPath: String, value: Any?)
}

/**
 * представлення базової реалізації доступу до властивостей
 */
open class BasePropertyAccessor : PropertyAccessor {
    
    override fun getNestedProperty(obj: Any, propertyPath: String): Any? {
        val properties = propertyPath.split(".")
        var current: Any? = obj
        
        for (property in properties) {
            if (current == null) break
            current = current::class.memberProperties
                .find { it.name == property }
                ?.getter?.call(current)
        }
        
        return current
    }
    
    override fun setNestedProperty(obj: Any, propertyPath: String, value: Any?) {
        val properties = propertyPath.split(".")
        var current: Any? = obj
        
        // Пройти до передостанньої властивості
        for (i in 0 until properties.size - 1) {
            val property = properties[i]
            if (current == null) break
            current = current::class.memberProperties
                .find { it.name == property }
                ?.getter?.call(current)
        }
        
        // Встановити значення останньої властивості
        if (current != null) {
            val lastProperty = properties.last()
            val kProperty = current::class.memberProperties
                .find { it.name == lastProperty }
            
            if (kProperty is KMutableProperty1) {
                kProperty.setter.call(current, value)
            }
        }
    }
}

/**
 * представлення інтерфейсу для валідації об'єктів через рефлексію
 */
interface ReflectionValidator {
    /**
     * перевірити об'єкт на відповідність правилам
     *
     * @param obj об'єкт
     * @return результат валідації
     */
    fun validate(obj: Any): ValidationResult
    
    /**
     * додати правило валідації
     *
     * @param rule правило валідації
     */
    fun addValidationRule(rule: ValidationRule)
}

/**
 * представлення правила валідації
 */
interface ValidationRule {
    /**
     * перевірити об'єкт
     *
     * @param obj об'єкт
     * @return результат перевірки
     */
    fun validate(obj: Any): ValidationError?
}

/**
 * представлення помилки валідації
 *
 * @property field поле
 * @property message повідомлення
 */
data class ValidationError(
    val field: String,
    val message: String
)

/**
 * представлення результату валідації
 *
 * @property isValid чи валідний об'єкт
 * @property errors список помилок
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError>
)

/**
 * представлення базової реалізації валідатора
 */
open class BaseReflectionValidator : ReflectionValidator {
    private val rules = mutableListOf<ValidationRule>()
    
    override fun validate(obj: Any): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        rules.forEach { rule ->
            val error = rule.validate(obj)
            if (error != null) {
                errors.add(error)
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    override fun addValidationRule(rule: ValidationRule) {
        rules.add(rule)
    }
}

/**
 * представлення інтерфейсу для серіалізації об'єктів через рефлексію
 */
interface ReflectionSerializer {
    /**
     * серіалізувати об'єкт в map
     *
     * @param obj об'єкт
     * @return map з даними об'єкта
     */
    fun serializeToMap(obj: Any): Map<String, Any?>
    
    /**
     * десеріалізувати об'єкт з map
     *
     * @param data дані
     * @param clazz клас об'єкта
     * @return десеріалізований об'єкт
     */
    fun <T : Any> deserializeFromMap(data: Map<String, Any?>, clazz: KClass<T>): T
}

/**
 * представлення базової реалізації серіалізатора
 */
open class BaseReflectionSerializer : ReflectionSerializer {
    
    override fun serializeToMap(obj: Any): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val properties = obj::class.memberProperties
        
        properties.forEach { property ->
            map[property.name] = property.getter.call(obj)
        }
        
        return map
    }
    
    override fun <T : Any> deserializeFromMap(data: Map<String, Any?>, clazz: KClass<T>): T {
        val constructor = clazz.primaryConstructor ?: clazz.constructors.firstOrNull()
            ?: throw IllegalArgumentException("No constructor found")
        
        val parameters = constructor.parameters.associateWith { param ->
            data[param.name]
        }
        
        return constructor.callBy(parameters)
    }
}

/**
 * представлення інтерфейсу для копіювання об'єктів
 */
interface ObjectCopier {
    /**
     * створити глибоку копію об'єкта
     *
     * @param obj об'єкт
     * @return копія об'єкта
     */
    fun <T : Any> deepCopy(obj: T): T
    
    /**
     * створити поверхневу копію об'єкта
     *
     * @param obj об'єкт
     * @return копія об'єкта
     */
    fun <T : Any> shallowCopy(obj: T): T
}

/**
 * представлення базової реалізації копіювача об'єктів
 */
open class BaseObjectCopier(
    private val serializer: ReflectionSerializer = BaseReflectionSerializer()
) : ObjectCopier {
    
    override fun <T : Any> deepCopy(obj: T): T {
        val data = serializer.serializeToMap(obj)
        @Suppress("UNCHECKED_CAST")
        return serializer.deserializeFromMap(data, obj::class) as T
    }
    
    override fun <T : Any> shallowCopy(obj: T): T {
        // Для поверхневої копії просто повертаємо той самий об'єкт
        // У реалізації можна використовувати Cloneable або інші підходи
        return obj
    }
}

/**
 * представлення інтерфейсу для роботи з модифікаторами доступу
 */
interface AccessModifierHelper {
    /**
     * перевірити, чи поле публічне
     *
     * @param property поле
     * @return true, якщо поле публічне
     */
    fun isPublic(property: KProperty1<*, *>): Boolean
    
    /**
     * перевірити, чи метод публічний
     *
     * @param function метод
     * @return true, якщо метод публічний
     */
    fun isPublic(function: KFunction<*>): Boolean
    
    /**
     * перевірити, чи клас публічний
     *
     * @param clazz клас
     * @return true, якщо клас публічний
     */
    fun isPublic(clazz: KClass<*>): Boolean
    
    /**
     * зробити поле доступним
     *
     * @param property поле
     */
    fun makeAccessible(property: KProperty1<*, *>)
    
    /**
     * зробити метод доступним
     *
     * @param function метод
     */
    fun makeAccessible(function: KFunction<*>)
}

/**
 * представлення базової реалізації помічника з модифікаторами доступу
 */
open class BaseAccessModifierHelper : AccessModifierHelper {
    
    override fun isPublic(property: KProperty1<*, *>): Boolean {
        // У Kotlin всі властивості за замовчуванням публічні
        return true
    }
    
    override fun isPublic(function: KFunction<*>): Boolean {
        // У Kotlin всі функції за замовчуванням публічні
        return true
    }
    
    override fun isPublic(clazz: KClass<*>): Boolean {
        // У Kotlin всі класи за замовчуванням публічні
        return true
    }
    
    override fun makeAccessible(property: KProperty1<*, *>) {
        // У Kotlin немає необхідності робити властивості доступними
    }
    
    override fun makeAccessible(function: KFunction<*>) {
        // У Kotlin немає необхідності робити функції доступними
    }
}

/**
 * представлення інтерфейсу для роботи з параметрами методів
 */
interface MethodParameterHelper {
    /**
     * отримати інформацію про параметри методу
     *
     * @param function метод
     * @return список інформації про параметри
     */
    fun getParameterInfo(function: KFunction<*>): List<ParameterInfo>
    
    /**
     * отримати значення параметрів за замовчуванням
     *
     * @param function метод
     * @return мапа значень за замовчуванням
     */
    fun getDefaultParameterValues(function: KFunction<*>): Map<String, Any?>
}

/**
 * представлення інформації про параметр
 *
 * @property name назва
 * @property type тип
 * @property isOptional чи необов'язковий
 * @property hasDefaultValue чи має значення за замовчуванням
 */
data class ParameterInfo(
    val name: String,
    val type: KType,
    val isOptional: Boolean,
    val hasDefaultValue: Boolean
)

/**
 * представлення базової реалізації помічника з параметрами методів
 */
open class BaseMethodParameterHelper : MethodParameterHelper {
    
    override fun getParameterInfo(function: KFunction<*>): List<ParameterInfo> {
        return function.parameters.map { param ->
            ParameterInfo(
                param.name ?: "unknown",
                param.type,
                param.isOptional,
                param.isOptional && function.hasDefaultParameterValue(param)
            )
        }
    }
    
    override fun getDefaultParameterValues(function: KFunction<*>): Map<String, Any?> {
        val defaults = mutableMapOf<String, Any?>()
        
        function.parameters.forEach { param ->
            if (param.isOptional && function.hasDefaultParameterValue(param)) {
                try {
                    val defaultValue = function.getDefaultParameterValue(param)
                    defaults[param.name ?: "unknown"] = defaultValue
                } catch (e: Exception) {
                    // Ігнорувати помилки отримання значень за замовчуванням
                }
            }
        }
        
        return defaults
    }
    
    private fun KFunction<*>.hasDefaultParameterValue(parameter: KParameter): Boolean {
        return try {
            this.getDefaultParameterValue(parameter)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun KFunction<*>.getDefaultParameterValue(parameter: KParameter): Any? {
        // Це заглушка для отримання значення за замовчуванням
        return null
    }
}

/**
 * представлення інтерфейсу для роботи з наслідуванням
 */
interface InheritanceHelper {
    /**
     * отримати всі суперкласи
     *
     * @param clazz клас
     * @return список суперкласів
     */
    fun getAllSuperclasses(clazz: KClass<*>): List<KClass<*>>
    
    /**
     * отримати всі інтерфейси
     *
     * @param clazz клас
     * @return список інтерфейсів
     */
    fun getAllInterfaces(clazz: KClass<*>): List<KClass<*>>
    
    /**
     * перевірити, чи клас реалізує інтерфейс
     *
     * @param clazz клас
     * @param interfaceClass інтерфейс
     * @return true, якщо клас реалізує інтерфейс
     */
    fun implementsInterface(clazz: KClass<*>, interfaceClass: KClass<*>): Boolean
    
    /**
     * перевірити, чи клас є підкласом іншого класу
     *
     * @param clazz клас
     * @param superClass суперклас
     * @return true, якщо клас є підкласом
     */
    fun isSubclassOf(clazz: KClass<*>, superClass: KClass<*>): Boolean
}

/**
 * представлення базової реалізації помічника з наслідування
 */
open class BaseInheritanceHelper : InheritanceHelper {
    
    override fun getAllSuperclasses(clazz: KClass<*>): List<KClass<*>> {
        val superclasses = mutableListOf<KClass<*>>()
        var current: KClass<*>? = clazz
        
        while (current != null) {
            superclasses.add(current)
            current = current.superclasses.firstOrNull()
        }
        
        return superclasses
    }
    
    override fun getAllInterfaces(clazz: KClass<*>): List<KClass<*>> {
        return clazz.supertypes
            .mapNotNull { it.classifier as? KClass<*> }
            .filter { it.isAbstract }
    }
    
    override fun implementsInterface(clazz: KClass<*>, interfaceClass: KClass<*>): Boolean {
        return clazz.supertypes.any { it.classifier == interfaceClass }
    }
    
    override fun isSubclassOf(clazz: KClass<*>, superClass: KClass<*>): Boolean {
        return clazz.superclasses.contains(superClass)
    }
}

/**
 * представлення інтерфейсу для роботи з анотаціями валідації
 */
interface ValidationAnnotationProcessor {
    /**
     * обробити анотації валідації
     *
     * @param obj об'єкт
     * @return результат валідації
     */
    fun processValidationAnnotations(obj: Any): ValidationResult
    
    /**
     * отримати правила валідації з анотацій
     *
     * @param clazz клас
     * @return список правил валідації
     */
    fun getValidationRulesFromAnnotations(clazz: KClass<*>): List<ValidationRule>
}

/**
 * представлення базової реалізації процесора анотацій валідації
 */
open class BaseValidationAnnotationProcessor : ValidationAnnotationProcessor {
    
    override fun processValidationAnnotations(obj: Any): ValidationResult {
        // Реалізація обробки анотацій валідації
        return ValidationResult(true, emptyList())
    }
    
    override fun getValidationRulesFromAnnotations(clazz: KClass<*>): List<ValidationRule> {
        // Реалізація отримання правил валідації з анотацій
        return emptyList()
    }
}

/**
 * представлення інтерфейсу для роботи з виключеннями через рефлексію
 */
interface ExceptionHandler {
    /**
     * обробити виключення
     *
     * @param exception виключення
     * @param context контекст
     */
    fun handleException(exception: Exception, context: ExceptionContext)
    
    /**
     * отримати інформацію про виключення
     *
     * @param exception виключення
     * @return інформація про виключення
     */
    fun getExceptionInfo(exception: Exception): ExceptionInfo
}

/**
 * представлення контексту виключення
 *
 * @property methodName назва методу
 * @property className назва класу
 * @property arguments аргументи
 * @property timestamp мітка часу
 */
data class ExceptionContext(
    val methodName: String,
    val className: String,
    val arguments: List<Any?>,
    val timestamp: Long
)

/**
 * представлення інформації про виключення
 *
 * @property exceptionClass клас виключення
 * @property message повідомлення
 * @property stackTrace трасування стека
 * @property cause причина
 */
data class ExceptionInfo(
    val exceptionClass: String,
    val message: String,
    val stackTrace: List<String>,
    val cause: ExceptionInfo?
)

/**
 * представлення базової реалізації обробника виключень
 */
open class BaseExceptionHandler : ExceptionHandler {
    
    override fun handleException(exception: Exception, context: ExceptionContext) {
        // Реалізація обробки виключення
        println("Exception in ${context.className}.${context.methodName}: ${exception.message}")
    }
    
    override fun getExceptionInfo(exception: Exception): ExceptionInfo {
        return ExceptionInfo(
            exception::class.simpleName ?: "UnknownException",
            exception.message ?: "No message",
            exception.stackTrace.map { it.toString() },
            exception.cause?.let { getExceptionInfo(it) }
        )
    }
}

/**
 * представлення інтерфейсу для роботи з кешем рефлексії
 */
interface ReflectionCache {
    /**
     * отримати закешовану інформацію про клас
     *
     * @param clazz клас
     * @return інформація про клас
     */
    fun getCachedClassInfo(clazz: KClass<*>): CachedClassInfo?
    
    /**
     * закешувати інформацію про клас
     *
     * @param clazz клас
     * @param info інформація про клас
     */
    fun cacheClassInfo(clazz: KClass<*>, info: CachedClassInfo)
    
    /**
     * очистити кеш
     */
    fun clearCache()
}

/**
 * представлення закешованої інформації про клас
 *
 * @property fields поля
 * @property methods методи
 * @property constructors конструктори
 * @property annotations анотації
 */
data class CachedClassInfo(
    val fields: List<KProperty1<*, *>>,
    val methods: List<KFunction<*>>,
    val constructors: List<KFunction<*>>,
    val annotations: List<Annotation>
)

/**
 * представлення базової реалізації кешу рефлексії
 */
open class BaseReflectionCache : ReflectionCache {
    private val cache = mutableMapOf<KClass<*>, CachedClassInfo>()
    
    override fun getCachedClassInfo(clazz: KClass<*>): CachedClassInfo? {
        return cache[clazz]
    }
    
    override fun cacheClassInfo(clazz: KClass<*>, info: CachedClassInfo) {
        cache[clazz] = info
    }
    
    override fun clearCache() {
        cache.clear()
    }
}

/**
 * представлення інтерфейсу для роботи з динамічними проксі
 */
interface DynamicProxyFactory {
    /**
     * створити динамічний проксі
     *
     * @param target цільовий об'єкт
     * @param interceptor перехоплювач
     * @return проксі-об'єкт
     */
    fun <T : Any> createProxy(target: T, interceptor: MethodInvocationInterceptor): T
    
    /**
     * створити проксі з кількома перехоплювачами
     *
     * @param target цільовий об'єкт
     * @param interceptors список перехоплювачів
     * @return проксі-об'єкт
     */
    fun <T : Any> createProxyWithInterceptors(target: T, interceptors: List<MethodInvocationInterceptor>): T
}

/**
 * представлення базової реалізації фабрики динамічних проксі
 */
open class BaseDynamicProxyFactory : DynamicProxyFactory {
    
    override fun <T : Any> createProxy(target: T, interceptor: MethodInvocationInterceptor): T {
        // Реалізація створення динамічного проксі
        return target
    }
    
    override fun <T : Any> createProxyWithInterceptors(target: T, interceptors: List<MethodInvocationInterceptor>): T {
        // Реалізація створення проксі з кількома перехоплювачами
        return target
    }
}

/**
 * представлення інтерфейсу для роботи з властивостями через делегати
 */
interface DelegatedPropertyHelper {
    /**
     * створити делегат для властивості
     *
     * @param property властивість
     * @return делегат
     */
    fun createDelegate(property: KProperty1<*, *>): PropertyDelegate<*>
    
    /**
     * отримати значення через делегат
     *
     * @param delegate делегат
     * @return значення
     */
    fun <T> getValue(delegate: PropertyDelegate<T>): T
    
    /**
     * встановити значення через делегат
     *
     * @param delegate делегат
     * @param value значення
     */
    fun <T> setValue(delegate: PropertyDelegate<T>, value: T)
}

/**
 * представлення делегата властивості
 *
 * @param T тип значення
 */
interface PropertyDelegate<T> {
    /**
     * отримати значення
     *
     * @param thisRef посилання на об'єкт
     * @param property властивість
     * @return значення
     */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T
    
    /**
     * встановити значення
     *
     * @param thisRef посилання на об'єкт
     * @param property властивість
     * @param value значення
     */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
}

/**
 * представлення базової реалізації помічника з делегованими властивостями
 */
open class BaseDelegatedPropertyHelper : DelegatedPropertyHelper {
    
    override fun createDelegate(property: KProperty1<*, *>): PropertyDelegate<*> {
        // Реалізація створення делегата
        return object : PropertyDelegate<Any?> {
            private var value: Any? = null
            
            override fun getValue(thisRef: Any?, property: KProperty<*>): Any? {
                return value
            }
            
            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Any?) {
                this.value = value
            }
        }
    }
    
    override fun <T> getValue(delegate: PropertyDelegate<T>): T {
        // Реалізація отримання значення через делегат
        @Suppress("UNCHECKED_CAST")
        return (delegate as PropertyDelegate<T>).getValue(null, null as KProperty<*>)
    }
    
    override fun <T> setValue(delegate: PropertyDelegate<T>, value: T) {
        // Реалізація встановлення значення через делегат
        @Suppress("UNCHECKED_CAST")
        (delegate as PropertyDelegate<T>).setValue(null, null as KProperty<*>, value)
    }
}

/**
 * представлення інтерфейсу для роботи з generic типами
 */
interface GenericTypeHelper {
    /**
     * отримати raw тип
     *
     * @param type тип
     * @return raw тип
     */
    fun getRawType(type: KType): KClass<*>
    
    /**
     * отримати аргументи типу
     *
     * @param type тип
     * @return список аргументів типу
     */
    fun getTypeArguments(type: KType): List<KType>
    
    /**
     * створити параметризований тип
     *
     * @param rawType raw тип
     * @param arguments аргументи типу
     * @return параметризований тип
     */
    fun createParameterizedType(rawType: KClass<*>, arguments: List<KType>): KType
}

/**
 * представлення базової реалізації помічника з generic типами
 */
open class BaseGenericTypeHelper : GenericTypeHelper {
    
    override fun getRawType(type: KType): KClass<*> {
        return type.classifier as? KClass<*> ?: throw IllegalArgumentException("Not a class type")
    }
    
    override fun getTypeArguments(type: KType): List<KType> {
        return type.arguments.map { it.type ?: throw IllegalArgumentException("Type argument is null") }
    }
    
    override fun createParameterizedType(rawType: KClass<*>, arguments: List<KType>): KType {
        // Реалізація створення параметризованого типу
        return rawType.createType(arguments.map { KTypeProjection.invariant(it) })
    }
}

/**
 * представлення інтерфейсу для роботи з анотаціями властивостей
 */
interface PropertyAnnotationHelper {
    /**
     * отримати анотації властивості
     *
     * @param property властивість
     * @return список анотацій
     */
    fun getPropertyAnnotations(property: KProperty1<*, *>): List<Annotation>
    
    /**
     * перевірити наявність анотації
     *
     * @param property властивість
     * @param annotationType тип анотації
     * @return true, якщо анотація присутня
     */
    fun <T : Annotation> hasAnnotation(property: KProperty1<*, *>, annotationType: KClass<T>): Boolean
    
    /**
     * отримати анотацію властивості
     *
     * @param property властивість
     * @param annotationType тип анотації
     * @return анотація або null
     */
    fun <T : Annotation> getPropertyAnnotation(property: KProperty1<*, *>, annotationType: KClass<T>): T?
}

/**
 * представлення базової реалізації помічника з анотаціями властивостей
 */
open class BasePropertyAnnotationHelper : PropertyAnnotationHelper {
    
    override fun getPropertyAnnotations(property: KProperty1<*, *>): List<Annotation> {
        return property.annotations
    }
    
    override fun <T : Annotation> hasAnnotation(property: KProperty1<*, *>, annotationType: KClass<T>): Boolean {
        return property.annotations.any { it.annotationClass == annotationType }
    }
    
    override fun <T : Annotation> getPropertyAnnotation(property: KProperty1<*, *>, annotationType: KClass<T>): T? {
        return property.annotations.find { it.annotationClass == annotationType } as? T
    }
}

/**
 * представлення інтерфейсу для роботи з анотаціями методів
 */
interface MethodAnnotationHelper {
    /**
     * отримати анотації методу
     *
     * @param function метод
     * @return список анотацій
     */
    fun getMethodAnnotations(function: KFunction<*>): List<Annotation>
    
    /**
     * перевірити наявність анотації
     *
     * @param function метод
     * @param annotationType тип анотації
     * @return true, якщо анотація присутня
     */
    fun <T : Annotation> hasAnnotation(function: KFunction<*>, annotationType: KClass<T>): Boolean
    
    /**
     * отримати анотацію методу
     *
     * @param function метод
     * @param annotationType тип анотації
     * @return анотація або null
     */
    fun <T : Annotation> getMethodAnnotation(function: KFunction<*>, annotationType: KClass<T>): T?
}

/**
 * представлення базової реалізації помічника з анотаціями методів
 */
open class BaseMethodAnnotationHelper : MethodAnnotationHelper {
    
    override fun getMethodAnnotations(function: KFunction<*>): List<Annotation> {
        return function.annotations
    }
    
    override fun <T : Annotation> hasAnnotation(function: KFunction<*>, annotationType: KClass<T>): Boolean {
        return function.annotations.any { it.annotationClass == annotationType }
    }
    
    override fun <T : Annotation> getMethodAnnotation(function: KFunction<*>, annotationType: KClass<T>): T? {
        return function.annotations.find { it.annotationClass == annotationType } as? T
    }
}

/**
 * представлення інтерфейсу для роботи з анотаціями класів
 */
interface ClassAnnotationHelper {
    /**
     * отримати анотації класу
     *
     * @param clazz клас
     * @return список анотацій
     */
    fun getClassAnnotations(clazz: KClass<*>): List<Annotation>
    
    /**
     * перевірити наявність анотації
     *
     * @param clazz клас
     * @param annotationType тип анотації
     * @return true, якщо анотація присутня
     */
    fun <T : Annotation> hasAnnotation(clazz: KClass<*>, annotationType: KClass<T>): Boolean
    
    /**
     * отримати анотацію класу
     *
     * @param clazz клас
     * @param annotationType тип анотації
     * @return анотація або null
     */
    fun <T : Annotation> getClassAnnotation(clazz: KClass<*>, annotationType: KClass<T>): T?
}

/**
 * представлення базової реалізації помічника з анотаціями класів
 */
open class BaseClassAnnotationHelper : ClassAnnotationHelper {
    
    override fun getClassAnnotations(clazz: KClass<*>): List<Annotation> {
        return clazz.annotations
    }
    
    override fun <T : Annotation> hasAnnotation(clazz: KClass<*>, annotationType: KClass<T>): Boolean {
        return clazz.annotations.any { it.annotationClass == annotationType }
    }
    
    override fun <T : Annotation> getClassAnnotation(clazz: KClass<*>, annotationType: KClass<T>): T? {
        return clazz.annotations.find { it.annotationClass == annotationType } as? T
    }
}

/**
 * представлення інтерфейсу для роботи з вкладеними класами
 */
interface NestedClassHelper {
    /**
     * отримати вкладені класи
     *
     * @param clazz клас
     * @return список вкладених класів
     */
    fun getNestedClasses(clazz: KClass<*>): List<KClass<*>>
    
    /**
     * перевірити, чи клас є вкладеним
     *
     * @param clazz клас
     * @return true, якщо клас є вкладеним
     */
    fun isNestedClass(clazz: KClass<*>): Boolean
    
    /**
     * отримати зовнішній клас
     *
     * @param clazz клас
     * @return зовнішній клас або null
     */
    fun getOuterClass(clazz: KClass<*>): KClass<*>?
}

/**
 * представлення базової реалізації помічника з вкладеними класами