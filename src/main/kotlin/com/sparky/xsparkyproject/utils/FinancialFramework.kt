/**
 * Фінансовий фреймворк
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
import kotlin.math.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * представлення інтерфейсу для роботи з фінансовими розрахунками
 */
interface FinancialCalculator {
    /**
     * обчислити майбутню вартість
     *
     * @param presentValue поточна вартість
     * @param interestRate процентна ставка
     * @param periods кількість періодів
     * @return майбутня вартість
     */
    fun calculateFutureValue(presentValue: BigDecimal, interestRate: Double, periods: Int): BigDecimal

    /**
     * обчислити поточну вартість
     *
     * @param futureValue майбутня вартість
     * @param interestRate процентна ставка
     * @param periods кількість періодів
     * @return поточна вартість
     */
    fun calculatePresentValue(futureValue: BigDecimal, interestRate: Double, periods: Int): BigDecimal

    /**
     * обчислити аннуїтет
     *
     * @param payment платіж
     * @param interestRate процентна ставка
     * @param periods кількість періодів
     * @return аннуїтет
     */
    fun calculateAnnuity(payment: BigDecimal, interestRate: Double, periods: Int): BigDecimal

    /**
     * обчислити внутрішню норму доходності
     *
     * @param cashFlows грошові потоки
     * @return внутрішня норма доходності
     */
    fun calculateIrr(cashFlows: List<BigDecimal>): Double

    /**
     * обчислити чисту приведену вартість
     *
     * @param cashFlows грошові потоки
     * @param discountRate ставка дисконтування
     * @return чиста приведена вартість
     */
    fun calculateNpv(cashFlows: List<BigDecimal>, discountRate: Double): BigDecimal

    /**
     * обчислити амортизацію
     *
     * @param cost вартість
     * @param salvageValue ліквідаційна вартість
     * @param usefulLife корисний термін
     * @param method метод амортизації
     * @return амортизація
     */
    fun calculateDepreciation(cost: BigDecimal, salvageValue: BigDecimal, usefulLife: Int, method: DepreciationMethod): List<BigDecimal>

    /**
     * обчислити ризик портфеля
     *
     * @param weights ваги активів
     * @param returns доходності активів
     * @param covariances коваріації
     * @return ризик портфеля
     */
    fun calculatePortfolioRisk(weights: List<BigDecimal>, returns: List<List<BigDecimal>>, covariances: List<List<BigDecimal>>): BigDecimal
}

/**
 * представлення методу амортизації
 */
enum class DepreciationMethod {
    STRAIGHT_LINE,
    DECLINING_BALANCE,
    SUM_OF_YEARS_DIGITS
}

/**
 * представлення фінансового інструменту
 */
data class FinancialInstrument(
    val id: String,
    val name: String,
    val type: InstrumentType,
    val symbol: String,
    val currentPrice: BigDecimal,
    val volatility: Double,
    val dividendYield: Double,
    val lastUpdated: LocalDateTime
)

/**
 * представлення типу інструменту
 */
enum class InstrumentType {
    STOCK,
    BOND,
    OPTION,
    FUTURE,
    CURRENCY,
    COMMODITY
}

/**
 * представлення фінансової транзакції
 */
data class FinancialTransaction(
    val id: String,
    val instrumentId: String,
    val transactionType: TransactionType,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val timestamp: LocalDateTime,
    val fees: BigDecimal,
    val accountId: String
)

/**
 * представлення типу транзакції
 */
enum class TransactionType {
    BUY,
    SELL,
    DIVIDEND,
    INTEREST,
    FEE
}

/**
 * представлення фінансового портфеля
 */
data class Portfolio(
    val id: String,
    val name: String,
    val ownerId: String,
    val holdings: Map<String, Holding>,
    val cashBalance: BigDecimal,
    val createdAt: LocalDateTime,
    val lastUpdated: LocalDateTime
)

/**
 * представлення утримання в портфелі
 */
data class Holding(
    val instrumentId: String,
    val quantity: BigDecimal,
    val averageCost: BigDecimal,
    val marketValue: BigDecimal,
    val unrealizedGainLoss: BigDecimal
)

/**
 * представлення базової реалізації фінансового калькулятора