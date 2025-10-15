/**
 * Фреймворк для електронної комерції
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
import kotlin.math.*
import java.time.LocalDateTime
import java.math.BigDecimal

/**
 * представлення інтерфейсу для роботи з електронною комерцією
 */
interface EcommercePlatform {
    /**
     * створити продукт
     *
     * @param productData дані продукту
     * @return ідентифікатор продукту
     */
    fun createProduct(productData: ProductData): String

    /**
     * отримати продукт
     *
     * @param productId ідентифікатор продукту
     * @return продукт
     */
    fun getProduct(productId: String): Product?

    /**
     * оновити продукт
     *
     * @param productId ідентифікатор продукту
     * @param productData дані продукту
     * @return true, якщо оновлено
     */
    fun updateProduct(productId: String, productData: ProductData): Boolean

    /**
     * видалити продукт
     *
     * @param productId ідентифікатор продукту
     * @return true, якщо видалено
     */
    fun deleteProduct(productId: String): Boolean

    /**
     * створити замовлення
     *
     * @param orderData дані замовлення
     * @return ідентифікатор замовлення
     */
    fun createOrder(orderData: OrderData): String

    /**
     * отримати замовлення
     *
     * @param orderId ідентифікатор замовлення
     * @return замовлення
     */
    fun getOrder(orderId: String): Order?

    /**
     * оновити статус замовлення
     *
     * @param orderId ідентифікатор замовлення
     * @param status статус
     * @return true, якщо оновлено
     */
    fun updateOrderStatus(orderId: String, status: OrderStatus): Boolean

    /**
     * додати товар до кошика
     *
     * @param userId ідентифікатор користувача
     * @param productId ідентифікатор продукту
     * @param quantity кількість
     * @return true, якщо додано
     */
    fun addToCart(userId: String, productId: String, quantity: Int): Boolean

    /**
     * видалити товар з кошика
     *
     * @param userId ідентифікатор користувача
     * @param productId ідентифікатор продукту
     * @return true, якщо видалено
     */
    fun removeFromCart(userId: String, productId: String): Boolean

    /**
     * отримати кошик користувача
     *
     * @param userId ідентифікатор користувача
     * @return кошик
     */
    fun getCart(userId: String): Cart

    /**
     * здійснити оплату
     *
     * @param paymentData дані оплати
     * @return результат оплати
     */
    fun processPayment(paymentData: PaymentData): PaymentResult

    /**
     * пошук продуктів
     *
     * @param query запит
     * @param filters фільтри
     * @param sort сортування
     * @param limit ліміт
     * @return список продуктів
     */
    fun searchProducts(query: String, filters: Map<String, Any>, sort: SortOption, limit: Int): List<Product>

    /**
     * отримати історію замовлень користувача
     *
     * @param userId ідентифікатор користувача
     * @param limit ліміт
     * @return список замовлень
     */
    fun getUserOrderHistory(userId: String, limit: Int): List<Order>
}

/**
 * представлення даних продукту
 */
data class ProductData(
    val name: String,
    val description: String,
    val price: BigDecimal,
    val currency: String,
    val categories: List<String>,
    val tags: List<String>,
    val images: List<String>,
    val attributes: Map<String, String>,
    val inventory: Int,
    val weight: Double,
    val dimensions: ProductDimensions,
    val brand: String,
    val sku: String
)

/**
 * представлення продукту
 */
data class Product(
    val productId: String,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val currency: String,
    val categories: List<String>,
    val tags: List<String>,
    val images: List<String>,
    val attributes: Map<String, String>,
    val inventory: Int,
    val weight: Double,
    val dimensions: ProductDimensions,
    val brand: String,
    val sku: String,
    val rating: Double,
    val reviewCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isActive: Boolean
)

/**
 * представлення розмірів продукту
 */
data class ProductDimensions(
    val length: Double,
    val width: Double,
    val height: Double,
    val unit: String
)

/**
 * представлення даних замовлення
 */
data class OrderData(
    val userId: String,
    val items: List<OrderItem>,
    val shippingAddress: Address,
    val billingAddress: Address,
    val paymentMethod: PaymentMethod,
    val shippingMethod: ShippingMethod,
    val promoCode: String?
)

/**
 * представлення замовлення