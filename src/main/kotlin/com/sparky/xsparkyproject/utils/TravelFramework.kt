/**
 * Фреймворк для туристичних технологій
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
import kotlin.math.*
import java.time.LocalDateTime
import java.time.LocalDate

/**
 * представлення опції сортування
 */
enum class SortOption {
    PRICE_ASC,
    PRICE_DESC,
    DURATION_ASC,
    DURATION_DESC,
    DEPARTURE_TIME,
    ARRIVAL_TIME,
    RATING
}

/**
 * представлення критеріїв пошуку рейсів
 */
data class FlightSearchCriteria(
    val origin: String,
    val destination: String,
    val departureDate: LocalDate,
    val returnDate: LocalDate?,
    val passengers: PassengerInfo,
    val cabinClass: CabinClass,
    val maxPrice: Double?,
    val airlines: List<String>?,
    val stops: Int?, // 0 - прямі, 1 - 1 пересадка, 2+ - будь-які
    val departureTimeRange: TimeRange?
)

/**
 * представлення інформації про пасажирів
 */
data class PassengerInfo(
    val adults: Int,
    val children: Int,
    val infants: Int
)

/**
 * представлення класу кабіни
 */
enum class CabinClass {
    ECONOMY,
    PREMIUM_ECONOMY,
    BUSINESS,
    FIRST
}

/**
 * представлення часових рамок
 */
data class TimeRange(
    val startTime: String, // HH:MM
    val endTime: String // HH:MM
)

/**
 * представлення рейсу
 */
data class Flight(
    val flightId: String,
    val airline: String,
    val flightNumber: String,
    val origin: Airport,
    val destination: Airport,
    val departureTime: LocalDateTime,
    val arrivalTime: LocalDateTime,
    val duration: Int, // хвилини
    val stops: Int,
    val layovers: List<Layover>,
    val cabinClass: CabinClass,
    val price: Double,
    val currency: String,
    val availability: Int, // кількість місць
    val aircraft: String
)

/**
 * представлення аеропорту
 */
data class Airport(
    val code: String,
    val name: String,
    val city: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * представлення пересадки
 */
data class Layover(
    val airport: Airport,
    val duration: Int // хвилини
)

/**
 * представлення даних бронювання рейсу
 */
data class FlightBookingData(
    val flightId: String,
    val passengers: List<Passenger>,
    val contactInfo: ContactInfo,
    val paymentInfo: PaymentInfo,
    val specialRequests: List<String>?
)

/**
 * представлення пасажира
 */
data class Passenger(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val passportNumber: String?,
    val frequentFlyerNumber: String?,
    val seatPreference: SeatPreference?
)

/**
 * представлення уподобань місця
 */
enum class SeatPreference {
    WINDOW,
    AISLE,
    MIDDLE
}

/**
 * представлення контактної інформації
 */
data class ContactInfo(
    val email: String,
    val phone: String,
    val address: Address?
)

/**
 * представлення адреси
 */
data class Address(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String
)

/**
 * представлення інформації про оплату
 */
data class PaymentInfo(
    val cardNumber: String,
    val expiryDate: String, // MM/YY
    val cvv: String,
    val cardholderName: String
)

/**
 * представлення критеріїв пошуку готелів
 */
data class HotelSearchCriteria(
    val destination: String,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val guests: GuestInfo,
    val rooms: Int,
    val maxPrice: Double?,
    val starRating: Int?, // 1-5
    val amenities: List<String>?,
    val propertyType: PropertyType?
)

/**
 * представлення інформації про гостей
 */
data class GuestInfo(
    val adults: Int,
    val children: Int
)

/**
 * представлення типу власності
 */
enum class PropertyType {
    HOTEL,
    RESORT,
    VILLA,
    APARTMENT,
    HOSTEL,
    BOUTIQUE
}

/**
 * представлення готелю
 */
data class Hotel(
    val hotelId: String,
    val name: String,
    val address: Address,
    val starRating: Int,
    val amenities: List<String>,
    val propertyType: PropertyType,
    val description: String,
    val photos: List<String>,
    val rating: Double, // 1.0 - 5.0
    val reviewCount: Int,
    val pricePerNight: Double,
    val currency: String,
    val availability: Boolean,
    val distanceFromCenter: Double // км
)

/**
 * представлення даних бронювання готелю
 */
data class HotelBookingData(
    val hotelId: String,
    val roomId: String,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val guests: List<Guest>,
    val contactInfo: ContactInfo,
    val paymentInfo: PaymentInfo,
    val specialRequests: List<String>?
)

/**
 * представлення гостя
 */
data class Guest(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate
)

/**
 * представлення критеріїв пошуку оренди авто
 */
data class CarRentalSearchCriteria(
    val pickupLocation: String,
    val dropoffLocation: String,
    val pickupDate: LocalDateTime,
    val dropoffDate: LocalDateTime,
    val driverAge: Int,
    val carType: CarType?,
    val maxPrice: Double?,
    val transmission: TransmissionType?,
    val fuelType: FuelType?
)

/**
 * представлення типу авто
 */
enum class CarType {
    ECONOMY,
    COMPACT,
    MIDSIZE,
    STANDARD,
    FULLSIZE,
    PREMIUM,
    LUXURY,
    SUV,
    VAN,
    TRUCK
}

/**
 * представлення типу трансмісії
 */
enum class TransmissionType {
    AUTOMATIC,
    MANUAL
}

/**
 * представлення типу палива
 */
enum class FuelType {
    GASOLINE,
    DIESEL,
    HYBRID,
    ELECTRIC
}

/**
 * представлення оренди авто
 */
data class CarRental(
    val rentalId: String,
    val provider: String,
    val carType: CarType,
    val make: String,
    val model: String,
    val transmission: TransmissionType,
    val fuelType: FuelType,
    val seats: Int,
    val doors: Int,
    val airConditioning: Boolean,
    val luggageCapacity: Int,
    val pricePerDay: Double,
    val currency: String,
    val totalPrice: Double,
    val insuranceIncluded: Boolean,
    val unlimitedMileage: Boolean,
    val availability: Boolean
)

/**
 * представлення даних паспорта
 */
data class PassportData(
    val passportId: String,
    val userId: String,
    val passportNumber: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val nationality: String,
    val issueDate: LocalDate,
    val expiryDate: LocalDate,
    val issuingCountry: String,
    val issuingAuthority: String
)

/**
 * представлення даних бронювання оренди авто
 */
data class CarRentalBookingData(
    val rentalId: String,
    val userId: String,
    val carRentalId: String,
    val pickupLocation: String,
    val dropoffLocation: String,
    val pickupDate: LocalDateTime,
    val dropoffDate: LocalDateTime,
    val driverAge: Int,
    val insuranceType: String,
    val totalPrice: Double,
    val currency: String,
    val paymentInfo: PaymentInfo
)

/**
 * представлення даних туру
 */
data class TourData(
    val tourId: String,
    val name: String,
    val description: String,
    val duration: Int, // дні
    val price: Double,
    val currency: String,
    val destinations: List<String>,
    val inclusions: List<String>,
    val exclusions: List<String>,
    val maxGroupSize: Int,
    val difficultyLevel: String,
    val rating: Double
)

/**
 * представлення туру
 */
data class Tour(
    val tourId: String,
    val name: String,
    val description: String,
    val duration: Int,
    val price: Double,
    val currency: String,
    val destinations: List<String>,
    val inclusions: List<String>,
    val exclusions: List<String>,
    val maxGroupSize: Int,
    val difficultyLevel: String,
    val rating: Double,
    val reviews: List<String>,
    val availability: Map<LocalDate, Boolean>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * представлення даних ітнерару
 */
data class ItineraryData(
    val itineraryId: String,
    val userId: String,
    val title: String,
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val destinations: List<String>,
    val budget: Double,
    val currency: String,
    val travelers: Int,
    val preferences: Map<String, Any>
)

/**
 * представлення ітнерару
 */
data class Itinerary(
    val itineraryId: String,
    val userId: String,
    val title: String,
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val destinations: List<String>,
    val budget: Double,
    val currency: String,
    val travelers: Int,
    val preferences: Map<String, Any>,
    val dailyPlans: List<DailyPlan>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * представлення щоденного плану
 */
data class DailyPlan(
    val date: LocalDate,
    val activities: List<Activity>,
    val accommodation: Accommodation?,
    val transportation: Transportation?
)

/**
 * представлення активності
 */
data class Activity(
    val activityId: String,
    val name: String,
    val description: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val location: String,
    val cost: Double,
    val currency: String
)

/**
 * представлення проживання
 */
data class Accommodation(
    val name: String,
    val address: String,
    val checkIn: LocalDateTime,
    val checkOut: LocalDateTime,
    val cost: Double,
    val currency: String
)

/**
 * представлення транспорту
 */
data class Transportation(
    val type: String,
    val from: String,
    val to: String,
    val departureTime: LocalDateTime,
    val arrivalTime: LocalDateTime,
    val cost: Double,
    val currency: String
)

/**
 * представлення візових вимог
 */
data class VisaRequirements(
    val destination: String,
    val nationality: String,
    val required: Boolean,
    val visaType: String,
    val processingTime: Int, // дні
    val cost: Double,
    val currency: String,
    val requiredDocuments: List<String>,
    val additionalInfo: String
)

/**
 * представлення даних страхування
 */
data class InsuranceData(
    val insuranceId: String,
    val userId: String,
    val provider: String,
    val policyNumber: String,
    val coverageType: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val coverageAmount: Double,
    val currency: String,
    val premium: Double,
    val beneficiaries: List<String>
)

/**
 * представлення параметрів рекомендацій
 */
data class RecommendationParams(
    val userId: String,
    val destination: String?,
    val interests: List<String>,
    val budget: Double?,
    val travelDates: Pair<LocalDate, LocalDate>?,
    val travelers: Int,
    val preferences: Map<String, Any>
)

/**
 * представлення туристичної рекомендації
 */
data class TravelRecommendation(
    val recommendationId: String,
    val type: String,
    val title: String,
    val description: String,
    val location: String,
    val rating: Double,
    val price: Double?,
    val currency: String?,
    val imageUrl: String?,
    val tags: List<String>
)

/**
 * представлення даних витрат
 */
data class ExpenseData(
    val userId: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val expenses: List<Expense>
)

/**
 * представлення витрати
 */
data class Expense(
    val expenseId: String,
    val date: LocalDate,
    val category: String,
    val description: String,
    val amount: Double,
    val currency: String,
    val location: String
)

/**
 * представлення аналізу витрат
 */
data class ExpenseAnalysis(
    val totalExpenses: Double,
    val currency: String,
    val categoryBreakdown: Map<String, Double>,
    val dailyExpenses: Map<LocalDate, Double>,
    val topExpenses: List<Expense>,
    val recommendations: List<String>
)

/**
 * представлення інтерфейсу для роботи з туристичною системою
 */
interface TravelSystem {
    /**
     * пошук рейсів
     *
     * @param searchCriteria критерії пошуку
     * @param sortOption опція сортування
     * @param limit ліміт
     * @return список рейсів
     */
    fun searchFlights(searchCriteria: FlightSearchCriteria, sortOption: SortOption, limit: Int): List<Flight>

    /**
     * бронювання рейсу
     *
     * @param bookingData дані бронювання
     * @return ідентифікатор бронювання
     */
    fun bookFlight(bookingData: FlightBookingData): String

    /**
     * пошук готелів
     *
     * @param searchCriteria критерії пошуку
     * @param sortOption опція сортування
     * @param limit ліміт
     * @return список готелів
     */
    fun searchHotels(searchCriteria: HotelSearchCriteria, sortOption: SortOption, limit: Int): List<Hotel>

    /**
     * бронювання готелю
     *
     * @param bookingData дані бронювання
     * @return ідентифікатор бронювання
     */
    fun bookHotel(bookingData: HotelBookingData): String

    /**
     * пошук орендованих авто
     *
     * @param searchCriteria критерії пошуку
     * @param sortOption опція сортування
     * @param limit ліміт
     * @return список авто
     */
    fun searchCarRentals(searchCriteria: CarRentalSearchCriteria, sortOption: SortOption, limit: Int): List<CarRental>

    /**
     * бронювання авто
     *
     * @param bookingData дані бронювання
     * @return ідентифікатор бронювання
     */
    fun bookCarRental(bookingData: CarRentalBookingData): String

    /**
     * створити тур
     *
     * @param tourData дані туру
     * @return ідентифікатор туру
     */
    fun createTour(tourData: TourData): String

    /**
     * отримати інформацію про тур
     *
     * @param tourId ідентифікатор туру
     * @return інформація про тур
     */
    fun getTourInfo(tourId: String): Tour?

    /**
     * планування подорожі
     *
     * @param itineraryData дані ітнерару
     * @return ідентифікатор ітнерару
     */
    fun planItinerary(itineraryData: ItineraryData): String

    /**
     * отримати ітнерар
     *
     * @param itineraryId ідентифікатор ітнерару
     * @return ітнерар
     */
    fun getItinerary(itineraryId: String): Itinerary?

    /**
     * керування паспортами
     *
     * @param passportData дані паспорта
     * @return ідентифікатор паспорта
     */
    fun managePassport(passportData: PassportData): String

    /**
     * отримати візові вимоги
     *
     * @param destination країна призначення
     * @param nationality громадянство
     * @return візові вимоги
     */
    fun getVisaRequirements(destination: String, nationality: String): VisaRequirements

    /**
     * керування страховками
     *
     * @param insuranceData дані страхування
     * @return ідентифікатор страхування
     */
    fun manageInsurance(insuranceData: InsuranceData): String

    /**
     * отримати рекомендації
     *
     * @param recommendationParams параметри рекомендацій
     * @return рекомендації
     */
    fun getRecommendations(recommendationParams: RecommendationParams): List<TravelRecommendation>

    /**
     * аналізувати витрати
     *
     * @param expenseData дані витрат
     * @return аналіз витрат
     */
    fun analyzeExpenses(expenseData: ExpenseData): ExpenseAnalysis
}
