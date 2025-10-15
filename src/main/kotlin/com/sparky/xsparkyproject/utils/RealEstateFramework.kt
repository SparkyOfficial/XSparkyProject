/**
 * Фреймворк для нерухомості
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
import kotlin.math.*
import java.time.LocalDateTime

/**
 * представлення інтерфейсу для роботи з нерухомістю
 */
interface RealEstateSystem {
    /**
     * створити нерухомість
     *
     * @param propertyData дані нерухомості
     * @return ідентифікатор нерухомості
     */
    fun createProperty(propertyData: PropertyData): String

    /**
     * отримати інформацію про нерухомість
     *
     * @param propertyId ідентифікатор нерухомості
     * @return інформація про нерухомість
     */
    fun getPropertyInfo(propertyId: String): PropertyInfo?

    /**
     * оновити інформацію про нерухомість
     *
     * @param propertyId ідентифікатор нерухомості
     * @param propertyData дані нерухомості
     * @return true, якщо оновлено
     */
    fun updateProperty(propertyId: String, propertyData: PropertyData): Boolean

    /**
     * пошук нерухомості
     *
     * @param searchCriteria критерії пошуку
     * @param sortOption опція сортування
     * @param limit ліміт
     * @return список нерухомості
     */
    fun searchProperties(searchCriteria: SearchCriteria, sortOption: SortOption, limit: Int): List<Property>

    /**
     * оцінити вартість нерухомості
     *
     * @param propertyId ідентифікатор нерухомості
     * @return оцінка вартості
     */
    fun estimatePropertyValue(propertyId: String): PropertyValueEstimate

    /**
     * створити лізинг
     *
     * @param leaseData дані лізингу
     * @return ідентифікатор лізингу
     */
    fun createLease(leaseData: LeaseData): String

    /**
     * отримати лізинг
     *
     * @param leaseId ідентифікатор лізингу
     * @return лізинг
     */
    fun getLease(leaseId: String): Lease?

    /**
     * керувати транзакціями
     *
     * @param transactionData дані транзакції
     * @return ідентифікатор транзакції
     */
    fun processTransaction(transactionData: TransactionData): String

    /**
     * аналізувати ринок
     *
     * @param marketAnalysisParams параметри аналізу
     * @return аналіз ринку
     */
    fun analyzeMarket(marketAnalysisParams: MarketAnalysisParams): MarketAnalysis

    /**
     * керувати документами
     *
     * @param documentData дані документа
     * @return ідентифікатор документа
     */
    fun manageDocument(documentData: DocumentData): String

    /**
     * планувати покази
     *
     * @param showingData дані показу
     * @return ідентифікатор показу
     */
    fun scheduleShowing(showingData: ShowingData): String

    /**
     * керувати інвестиціями
     *
     * @param investmentData дані інвестиції
     * @return результат інвестиції
     */
    fun manageInvestment(investmentData: InvestmentData): InvestmentResult

    /**
     * аналізувати аренду
     *
     * @param rentalAnalysisParams параметри аналізу
     * @return аналіз аренди
     */
    fun analyzeRental(rentalAnalysisParams: RentalAnalysisParams): RentalAnalysis

    /**
     * керувати ремонтом
     *
     * @param maintenanceData дані ремонту
     * @return ідентифікатор ремонту
     */
    fun manageMaintenance(maintenanceData: MaintenanceData): String
}

/**
 * представлення даних нерухомості
 */
data class PropertyData(
    val address: Address,
    val propertyType: PropertyType,
    val listingType: ListingType,
    val price: Double,
    val currency: String,
    val size: Double, // м²
    val lotSize: Double?, // м²
    val bedrooms: Int,
    val bathrooms: Int,
    val yearBuilt: Int,
    val condition: PropertyCondition,
    val features: List<String>,
    val description: String,
    val ownerId: String,
    val agentId: String?
)

/**
 * представлення адреси
 */
data class Address(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * представлення типу нерухомості
 */
enum class PropertyType {
    SINGLE_FAMILY_HOME,
    CONDO,
    TOWNHOUSE,
    APARTMENT,
    COMMERCIAL,
    LAND,
    MULTI_FAMILY,
    MOBILE_HOME
}

/**
 * представлення типу лістингу
 */
enum class ListingType {
    FOR_SALE,
    FOR_RENT,
    SOLD,
    RENTED,
    PENDING
}

/**
 * представлення стану нерухомості
 */
enum class PropertyCondition {
    NEW,
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    NEEDS_REPAIR
)

/**
 * представлення інформації про нерухомість
 */
data class PropertyInfo(
    val propertyId: String,
    val address: Address,
    val propertyType: PropertyType,
    val listingType: ListingType,
    val price: Double,
    val currency: String,
    val size: Double,
    val lotSize: Double?,
    val bedrooms: Int,
    val bathrooms: Int,
    val yearBuilt: Int,
    val condition: PropertyCondition,
    val features: List<String>,
    val description: String,
    val ownerId: String,
    val agentId: String?,
    val photos: List<String>,
    val virtualTour: String?,
    val listingDate: LocalDateTime,
    val lastUpdated: LocalDateTime,
    val views: Int,
    val favorites: Int
)

/**
 * представлення нерухомості
 */
data class Property(
    val propertyId: String,
    val address: Address,
    val propertyType: PropertyType,
    val listingType: ListingType,
    val price: Double,
    val currency: String,
    val size: Double,
    val bedrooms: Int,
    val bathrooms: Int,
    val yearBuilt: Int,
    val features: List<String>,
    val mainPhoto: String,
    val listingDate: LocalDateTime
)

/**
 * представлення критеріїв пошуку
 */
data class SearchCriteria(
    val location: String?,
    val propertyType: PropertyType?,
    val minPrice: Double?,
    val maxPrice: Double?,
    val minBedrooms: Int?,
    val minBathrooms: Int?,
    val minSize: Double?,
    val maxSize: Double?,
    val yearBuiltFrom: Int?,
    val yearBuiltTo: Int?,
    val features: List<String>?,
    val listingType: ListingType?
)

/**
 * представлення опції сортування
 */
enum class SortOption {
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    NEWEST,
    OLDEST,
    SIZE_LARGE_TO_SMALL,
    SIZE_SMALL_TO_LARGE
}

/**
 * представлення оцінки вартості нерухомості
 */
data class PropertyValueEstimate(
    val propertyId: String,
    val estimatedValue: Double,
    val currency: String,
    val confidence: Double, // 0.0 - 1.0
    val comparableProperties: List<ComparableProperty>,
    val factors: List<ValuationFactor>,
    val lastUpdated: LocalDateTime
)

/**
 * представлення порівнянної нерухомості
 */
data class ComparableProperty(
    val propertyId: String,
    val address: Address,
    val price: Double,
    val size: Double,
    val bedrooms: Int,
    val bathrooms: Int,
    val saleDate: LocalDateTime
)

/**
 * представлення фактору оцінки
 */
data class ValuationFactor(
    val factor: String,
    val weight: Double, // 0.0 - 1.0
    val impact: Double // позитивний або негативний
)

/**
 * представлення даних лізингу
 */
data class LeaseData(
    val propertyId: String,
    val landlordId: String,
    val tenantId: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val monthlyRent: Double,
    val currency: String,
    val securityDeposit: Double,
    val leaseTerms: List<LeaseTerm>,
    val specialConditions: List<String>
)

/**
 * представлення умови лізингу
 */
data class LeaseTerm(
    val term: String,
    val value: String
)

/**
 * представлення лізингу
 */
data class Lease(
    val leaseId: String,
    val propertyId: String,
    val landlordId: String,
    val tenantId: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val monthlyRent: Double,
    val currency: String,
    val securityDeposit: Double,
    val leaseTerms: List<LeaseTerm>,
    val specialConditions: List<String>,
    val status: LeaseStatus,
    val paymentHistory: List<PaymentRecord>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * представлення статусу лізингу
 */
enum class LeaseStatus {
    ACTIVE,
    EXPIRED,
    TERMINATED,
    PENDING_RENEWAL
}

/**
 * представлення запису платежу
 */
data class PaymentRecord(
    val paymentId: String,
    val amount: Double,
    val currency: String,
    val dueDate: LocalDateTime,
    val paymentDate: LocalDateTime?,
    val status: PaymentStatus,
    val paymentMethod: PaymentMethod
)

/**
 * представлення статусу платежу
 */
enum class PaymentStatus {
    PAID,
    PENDING,
    OVERDUE,
    CANCELLED
}

/**
 * представлення методу платежу
 */
enum class PaymentMethod {
    BANK_TRANSFER,
    CHECK,
    CASH,
    CREDIT_CARD,
    ONLINE_PAYMENT
}

/**
 * представлення даних транзакції