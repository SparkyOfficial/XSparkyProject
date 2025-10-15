/**
 * Фреймворк для спортивних технологій
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
import kotlin.math.*
import java.time.LocalDateTime

// Enums
/**
 * представлення статі
 */
enum class Gender {
    MALE,
    FEMALE,
    OTHER
}

/**
 * представлення виду спорту
 */
enum class Sport {
    FOOTBALL,
    BASKETBALL,
    TENNIS,
    SWIMMING,
    ATHLETICS,
    CYCLING,
    BOXING,
    GOLF,
    RUGBY,
    CRICKET,
    BASEBALL,
    HOCKEY,
    VOLLEYBALL,
    BADMINTON,
    TABLE_TENNIS
}

/**
 * представлення серйозності травми
 */
enum class InjurySeverity {
    MINOR,
    MODERATE,
    SEVERE,
    CRITICAL
}

/**
 * представлення типу статистики
 */
enum class StatisticType {
    SPEED,
    STRENGTH,
    ENDURANCE,
    ACCURACY,
    AGILITY,
    POWER,
    FLEXIBILITY,
    REACTION_TIME
}

/**
 * представлення типу змагання
 */
enum class CompetitionType {
    LEAGUE,
    TOURNAMENT,
    CUP,
    CHAMPIONSHIP,
    FRIENDLY,
    EXHIBITION
}

/**
 * представлення типу учасника
 */
enum class ParticipantType {
    ATHLETE,
    TEAM
}

/**
 * представлення статусу змагання
 */
enum class CompetitionStatus {
    UPCOMING,
    ONGOING,
    COMPLETED,
    CANCELLED,
    POSTPONED
}

/**
 * представлення статусу участі
 */
enum class ParticipationStatus {
    REGISTERED,
    CONFIRMED,
    WITHDRAWN,
    DISQUALIFIED
}

/**
 * представлення опції сортування результатів
 */
enum class ResultSortOption {
    POSITION,
    SCORE,
    TIME,
    POINTS,
    NAME
}

/**
 * представлення типу тренування
 */
enum class TrainingType {
    STRENGTH,
    CARDIO,
    FLEXIBILITY,
    TECHNIQUE,
    RECOVERY,
    COMPETITION
}

/**
 * представлення типу турніру
 */
enum class TournamentType {
    SINGLE_ELIMINATION,
    DOUBLE_ELIMINATION,
    ROUND_ROBIN,
    SWISS_SYSTEM,
    GROUP_STAGE_KNOCKOUT
}

/**
 * представлення типу ставки
 */
enum class BetType {
    WIN,
    PLACE,
    SHOW,
    SPREAD,
    OVER_UNDER,
    PROPS
}

/**
 * представлення інформації про контакт
 */
data class ContactInfo(
    val email: String,
    val phone: String,
    val website: String?
)

/**
 * представлення адреси
 */
data class Address(
    val street: String,
    val city: String,
    val state: String?,
    val zipCode: String?,
    val country: String
)

// Data classes that depend on previously defined classes
/**
 * представлення даних спортсмена
 */
data class AthleteData(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDateTime,
    val gender: Gender,
    val nationality: String,
    val sport: Sport,
    val position: String?,
    val height: Double?, // см
    val weight: Double?, // кг
    val teamId: String?,
    val coachId: String?,
    val contactInfo: ContactInfo
)

/**
 * представлення кар'єрної статистики
 */
data class CareerStats(
    val totalCompetitions: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val totalPoints: Int,
    val averagePerformance: Double
)

/**
 * представлення досягнення
 */
data class Achievement(
    val title: String,
    val description: String,
    val date: LocalDateTime,
    val competition: String,
    val position: Int?
)

/**
 * представлення травми
 */
data class Injury(
    val injuryId: String,
    val type: String,
    val description: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime?,
    val severity: InjurySeverity,
    val treatment: String
)

/**
 * представлення інформації про спортсмена
 */
data class AthleteInfo(
    val athleteId: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDateTime,
    val gender: Gender,
    val nationality: String,
    val sport: Sport,
    val position: String?,
    val height: Double?,
    val weight: Double?,
    val teamId: String?,
    val coachId: String?,
    val contactInfo: ContactInfo,
    val careerStats: CareerStats,
    val achievements: List<Achievement>,
    val injuries: List<Injury>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * представлення даних статистики спортсмена
 */
data class AthleteStatsData(
    val athleteId: String,
    val statType: StatisticType,
    val value: Double,
    val date: LocalDateTime,
    val competitionId: String?
)

/**
 * представлення даних команди
 */
data class TeamData(
    val name: String,
    val sport: Sport,
    val foundedDate: LocalDateTime,
    val location: Address,
    val coach: String,
    val league: String,
    val colors: List<String>,
    val stadium: String?
)

/**
 * представлення члена команди
 */
data class TeamMember(
    val athleteId: String,
    val firstName: String,
    val lastName: String,
    val position: String,
    val jerseyNumber: Int?
)

/**
 * представлення статистики команди
 */
data class TeamStats(
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val points: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDifference: Int
)

/**
 * представлення досягнення команди
 */
data class TeamAchievement(
    val title: String,
    val description: String,
    val date: LocalDateTime,
    val competition: String,
    val position: Int?
)

/**
 * представлення інформації про команду
 */
data class TeamInfo(
    val teamId: String,
    val name: String,
    val sport: Sport,
    val foundedDate: LocalDateTime,
    val location: Address,
    val coach: String,
    val league: String,
    val colors: List<String>,
    val stadium: String?,
    val roster: List<TeamMember>,
    val statistics: TeamStats,
    val achievements: List<TeamAchievement>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * представлення даних змагання
 */
data class CompetitionData(
    val name: String,
    val sport: Sport,
    val type: CompetitionType,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val location: Address,
    val organizer: String,
    val participantsLimit: Int?,
    val entryFee: Double?,
    val prizePool: Double?
)

/**
 * представлення учасника
 */
data class Participant(
    val participantId: String,
    val participantType: ParticipantType,
    val name: String
)

/**
 * представлення події змагання
 */
data class CompetitionEvent(
    val eventId: String,
    val name: String,
    val dateTime: LocalDateTime,
    val venue: String,
    val participants: List<String> // список ідентифікаторів учасників
)

/**
 * представлення інформації про змагання
 */
data class CompetitionInfo(
    val competitionId: String,
    val name: String,
    val sport: Sport,
    val type: CompetitionType,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val location: Address,
    val organizer: String,
    val participantsLimit: Int?,
    val entryFee: Double?,
    val prizePool: Double?,
    val participants: List<Participant>,
    val schedule: List<CompetitionEvent>,
    val status: CompetitionStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * представлення даних участі у змаганні
 */
data class CompetitionParticipationData(
    val competitionId: String,
    val participantId: String,
    val participantType: ParticipantType,
    val registrationDate: LocalDateTime,
    val entryFeePaid: Double,
    val status: ParticipationStatus
)

/**
 * представлення даних результату змагання
 */
data class CompetitionResultData(
    val competitionId: String,
    val participantId: String,
    val score: Double,
    val position: Int?,
    val time: Double?, // для бігу, плавання тощо
    val points: Int?,
    val remarks: String?
)

/**
 * представлення результату змагання
 */
data class CompetitionResult(
    val resultId: String,
    val competitionId: String,
    val participantId: String,
    val participantName: String,
    val score: Double,
    val position: Int?,
    val time: Double?,
    val points: Int?,
    val remarks: String?,
    val recordedBy: String,
    val recordedAt: LocalDateTime
)

/**
 * представлення метрики продуктивності
 */
data class PerformanceMetric(
    val metricType: StatisticType,
    val value: Double,
    val date: LocalDateTime,
    val context: String?
)

/**
 * представлення даних аналізу продуктивності
 */
data class PerformanceAnalysisData(
    val athleteId: String,
    val competitionId: String?,
    val periodStart: LocalDateTime,
    val periodEnd: LocalDateTime,
    val metrics: List<PerformanceMetric>
)

/**
 * представлення аспекту продуктивності
 */
data class PerformanceAspect(
    val aspect: String,
    val score: Double,
    val improvement: Double?
)

/**
 * представлення результату аналізу продуктивності
 */
data class PerformanceAnalysisResult(
    val athleteId: String,
    val analysisId: String,
    val periodStart: LocalDateTime,
    val periodEnd: LocalDateTime,
    val overallPerformance: Double,
    val strengths: List<PerformanceAspect>,
    val weaknesses: List<PerformanceAspect>,
    val recommendations: List<String>,
    val createdAt: LocalDateTime
)

/**
 * представлення цілі тренувань
 */
data class TrainingGoal(
    val goalId: String,
    val description: String,
    val targetDate: LocalDateTime,
    val targetValue: Double,
    val metric: StatisticType
)

/**
 * представлення вправи
 */
data class Exercise(
    val exerciseId: String,
    val name: String,
    val sets: Int,
    val reps: Int,
    val weight: Double?,
    val duration: Int?, // секунди
    val intensity: Int? // 1-10
)

/**
 * представлення тренувального сеансу
 */
data class TrainingSession(
    val sessionId: String,
    val date: LocalDateTime,
    val duration: Int, // хвилини
    val type: TrainingType,
    val intensity: Int, // 1-10
    val exercises: List<Exercise>,
    val location: String?
)

/**
 * представлення даних плану тренувань
 */
data class TrainingPlanData(
    val athleteId: String,
    val coachId: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val goals: List<TrainingGoal>,
    val sessions: List<TrainingSession>
)

/**
 * представлення даних відстеження тренування
 */
data class TrainingTrackingData(
    val sessionId: String,
    val athleteId: String,
    val metrics: Map<String, Double>,
    val feedback: String?
)

/**
 * представлення результату відстеження тренування
 */
data class TrainingTrackingResult(
    val trackingId: String,
    val sessionId: String,
    val athleteId: String,
    val metrics: Map<String, Double>,
    val feedback: String?,
    val timestamp: LocalDateTime
)

/**
 * представлення даних турніру
 */
data class TournamentData(
    val name: String,
    val sport: Sport,
    val type: TournamentType,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val location: Address,
    val organizer: String,
    val entryFee: Double?,
    val prizePool: Double?,
    val maxParticipants: Int?
)

/**
 * представлення запису в таблиці
 */
data class StandingEntry(
    val participantId: String,
    val participantName: String,
    val position: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val points: Int,
    val goalDifference: Int
)

/**
 * представлення турнірної таблиці
 */
data class TournamentStandings(
    val tournamentId: String,
    val standings: List<StandingEntry>,
    val lastUpdated: LocalDateTime
)

/**
 * представлення даних ставки
 */
data class BettingData(
    val userId: String,
    val competitionId: String,
    val participantId: String,
    val amount: Double,
    val currency: String,
    val betType: BetType,
    val odds: Double,
    val placedAt: LocalDateTime
)

/**
 * представлення інтерфейсу для роботи зі спортивною системою
 */
interface SportsSystem {
    /**
     * створити спортсмена
     *
     * @param athleteData дані спортсмена
     * @return ідентифікатор спортсмена
     */
    fun createAthlete(athleteData: AthleteData): String

    /**
     * отримати інформацію про спортсмена
     *
     * @param athleteId ідентифікатор спортсмена
     * @return інформація про спортсмена
     */
    fun getAthleteInfo(athleteId: String): AthleteInfo?

    /**
     * оновити статистику спортсмена
     *
     * @param athleteId ідентифікатор спортсмена
     * @param statsData дані статистики
     * @return true, якщо оновлено
     */
    fun updateAthleteStats(athleteId: String, statsData: AthleteStatsData): Boolean

    /**
     * створити команду
     *
     * @param teamData дані команди
     * @return ідентифікатор команди
     */
    fun createTeam(teamData: TeamData): String

    /**
     * отримати інформацію про команду
     *
     * @param teamId ідентифікатор команди
     * @return інформація про команду
     */
    fun getTeamInfo(teamId: String): TeamInfo?

    /**
     * створити змагання
     *
     * @param competitionData дані змагання
     * @return ідентифікатор змагання
     */
    fun createCompetition(competitionData: CompetitionData): String

    /**
     * отримати інформацію про змагання
     *
     * @param competitionId ідентифікатор змагання
     * @return інформація про змагання
     */
    fun getCompetitionInfo(competitionId: String): CompetitionInfo?

    /**
     * додати участь у змаганні
     *
     * @param participationData дані участі
     * @return ідентифікатор участі
     */
    fun addCompetitionParticipation(participationData: CompetitionParticipationData): String

    /**
     * записати результат змагання
     *
     * @param resultData дані результату
     * @return ідентифікатор результату
     */
    fun recordCompetitionResult(resultData: CompetitionResultData): String

    /**
     * отримати результати змагання
     *
     * @param competitionId ідентифікатор змагання
     * @param sortBy критерій сортування
     * @return список результатів
     */
    fun getCompetitionResults(competitionId: String, sortBy: ResultSortOption): List<CompetitionResult>

    /**
     * аналізувати продуктивність
     *
     * @param analysisData дані аналізу
     * @return результат аналізу
     */
    fun analyzePerformance(analysisData: PerformanceAnalysisData): PerformanceAnalysisResult

    /**
     * планувати тренування
     *
     * @param trainingPlanData дані плану тренувань
     * @return ідентифікатор плану
     */
    fun planTraining(trainingPlanData: TrainingPlanData): String

    /**
     * відстежувати тренування
     *
     * @param trackingData дані відстеження
     * @return результат відстеження
     */
    fun trackTraining(trackingData: TrainingTrackingData): TrainingTrackingResult

    /**
     * керувати турнірами
     *
     * @param tournamentData дані турніру
     * @return ідентифікатор турніру
     */
    fun manageTournament(tournamentData: TournamentData): String

    /**
     * отримати турнірну таблицю
     *
     * @param tournamentId ідентифікатор турніру
     * @return турнірна таблиця
     */
    fun getTournamentStandings(tournamentId: String): TournamentStandings

    /**
     * керувати ставками
     *
     * @param bettingData дані ставки
     * @return ідентифікатор ставки
     */
    fun manageBetting(bettingData: BettingData): String
}