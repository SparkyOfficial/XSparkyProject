/**
 * Фреймворк для медичних технологій
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
 * представлення інтерфейсу для роботи з медичною інформацією
 */
interface HealthcareSystem {
    /**
     * створити медичний запис пацієнта
     *
     * @param recordData дані запису
     * @return ідентифікатор запису
     */
    fun createPatientRecord(recordData: PatientRecordData): String

    /**
     * отримати медичний запис пацієнта
     *
     * @param patientId ідентифікатор пацієнта
     * @param recordId ідентифікатор запису
     * @return медичний запис
     */
    fun getPatientRecord(patientId: String, recordId: String): PatientRecord?

    /**
     * оновити медичний запис пацієнта
     *
     * @param patientId ідентифікатор пацієнта
     * @param recordId ідентифікатор запису
     * @param recordData дані запису
     * @return true, якщо оновлено
     */
    fun updatePatientRecord(patientId: String, recordId: String, recordData: PatientRecordData): Boolean

    /**
     * додати діагноз пацієнту
     *
     * @param patientId ідентифікатор пацієнта
     * @param diagnosisData дані діагнозу
     * @return ідентифікатор діагнозу
     */
    fun addDiagnosis(patientId: String, diagnosisData: DiagnosisData): String

    /**
     * додати лікування пацієнту
     *
     * @param patientId ідентифікатор пацієнта
     * @param treatmentData дані лікування
     * @return ідентифікатор лікування
     */
    fun addTreatment(patientId: String, treatmentData: TreatmentData): String

    /**
     * записати пацієнта на прийом
     *
     * @param appointmentData дані прийому
     * @return ідентифікатор прийому
     */
    fun scheduleAppointment(appointmentData: AppointmentData): String

    /**
     * отримати записи прийомів пацієнта
     *
     * @param patientId ідентифікатор пацієнта
     * @param startDate початкова дата
     * @param endDate кінцева дата
     * @return список прийомів
     */
    fun getPatientAppointments(patientId: String, startDate: LocalDate, endDate: LocalDate): List<Appointment>

    /**
     * додати медичний тест
     *
     * @param testData дані тесту
     * @return ідентифікатор тесту
     */
    fun addMedicalTest(testData: MedicalTestData): String

    /**
     * отримати результати тестів пацієнта
     *
     * @param patientId ідентифікатор пацієнта
     * @param testType тип тесту
     * @param limit ліміт
     * @return список результатів
     */
    fun getPatientTestResults(patientId: String, testType: String, limit: Int): List<MedicalTestResult>

    /**
     * аналізувати симптоми
     *
     * @param symptoms симптоми
     * @return можливі діагнози
     */
    fun analyzeSymptoms(symptoms: List<String>): List<PotentialDiagnosis>

    /**
     * отримати медичну статистику
     *
     * @param parameters параметри
     * @return статистика
     */
    fun getMedicalStatistics(parameters: Map<String, Any>): MedicalStatistics
}

/**
 * представлення даних медичного запису пацієнта
 */
data class PatientRecordData(
    val patientId: String,
    val doctorId: String,
    val visitDate: LocalDateTime,
    val chiefComplaint: String,
    val historyOfPresentIllness: String,
    val pastMedicalHistory: String,
    val familyHistory: String,
    val socialHistory: String,
    val reviewOfSystems: String,
    val physicalExamination: String,
    val vitalSigns: VitalSigns,
    val notes: String
)

/**
 * представлення медичного запису пацієнта
 */
data class PatientRecord(
    val recordId: String,
    val patientId: String,
    val doctorId: String,
    val visitDate: LocalDateTime,
    val chiefComplaint: String,
    val historyOfPresentIllness: String,
    val pastMedicalHistory: String,
    val familyHistory: String,
    val socialHistory: String,
    val reviewOfSystems: String,
    val physicalExamination: String,
    val vitalSigns: VitalSigns,
    val diagnoses: List<Diagnosis>,
    val treatments: List<Treatment>,
    val notes: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * представлення життєво важливих показників
 */
data class VitalSigns(
    val bloodPressure: BloodPressure,
    val heartRate: Int,
    val respiratoryRate: Int,
    val temperature: Double,
    val oxygenSaturation: Double,
    val weight: Double,
    val height: Double
)

/**
 * представлення артеріального тиску
 */
data class BloodPressure(
    val systolic: Int,
    val diastolic: Int
)

/**
 * представлення даних діагнозу
 */
data class DiagnosisData(
    val patientId: String,
    val doctorId: String,
    val diagnosisCode: String,
    val diagnosisName: String,
    val description: String,
    val certainty: DiagnosisCertainty,
    val startDate: LocalDate,
    val endDate: LocalDate?
)

/**
 * представлення діагнозу
 */
data class Diagnosis(
    val diagnosisId: String,
    val patientId: String,
    val doctorId: String,
    val diagnosisCode: String,
    val diagnosisName: String,
    val description: String,
    val certainty: DiagnosisCertainty,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val createdAt: LocalDateTime
)

/**
 * представлення впевненості в діагнозі
 */
enum class DiagnosisCertainty {
    SUSPECTED,
    PROBABLE,
    DEFINITIVE
}

/**
 * представлення даних лікування
 */
data class TreatmentData(
    val patientId: String,
    val doctorId: String,
    val treatmentType: TreatmentType,
    val medication: String?,
    val dosage: String?,
    val frequency: String?,
    val duration: String?,
    val procedure: String?,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val notes: String
)

/**
 * представлення лікування
 */
data class Treatment(
    val treatmentId: String,
    val patientId: String,
    val doctorId: String,
    val treatmentType: TreatmentType,
    val medication: String?,
    val dosage: String?,
    val frequency: String?,
    val duration: String?,
    val procedure: String?,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val notes: String,
    val createdAt: LocalDateTime
)

/**
 * представлення типу лікування
 */
enum class TreatmentType {
    MEDICATION,
    PROCEDURE,
    SURGERY,
    THERAPY,
    LIFESTYLE
}

/**
 * представлення даних прийому
 */
data class AppointmentData(
    val patientId: String,
    val doctorId: String,
    val appointmentDate: LocalDateTime,
    val appointmentType: AppointmentType,
    val reason: String,
    val duration: Int, // в хвилинах
    val location: String
)

/**
 * представлення прийому
 */
data class Appointment(
    val appointmentId: String,
    val patientId: String,
    val doctorId: String,
    val appointmentDate: LocalDateTime,
    val appointmentType: AppointmentType,
    val reason: String,
    val duration: Int,
    val location: String,
    val status: AppointmentStatus,
    val notes: String,
    val createdAt: LocalDateTime
)

/**
 * представлення типу прийому
 */
enum class AppointmentType {
    ROUTINE_CHECKUP,
    FOLLOW_UP,
    EMERGENCY,
    CONSULTATION,
    PROCEDURE
}

/**
 * представлення статусу прийому
 */
enum class AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}

/**
 * представлення даних медичного тесту
 */
data class MedicalTestData(
    val patientId: String,
    val doctorId: String,
    val testType: String,
    val testName: String,
    val orderedDate: LocalDateTime,
    val performedDate: LocalDateTime?,
    val labId: String,
    val priority: TestPriority
)

/**
 * представлення результату медичного тесту
 */
data class MedicalTestResult(
    val testId: String,
    val patientId: String,
    val testType: String,
    val testName: String,
    val result: String,
    val unit: String,
    val referenceRange: String,
    val interpretation: String,
    val performedDate: LocalDateTime,
    val reportedDate: LocalDateTime,
    val labId: String,
    val technicianId: String
)

/**
 * представлення пріоритету тесту
 */
enum class TestPriority {
    ROUTINE,
    URGENT,
    STAT
}

/**
 * представлення потенційного діагнозу
 */
data class PotentialDiagnosis(
    val diagnosisCode: String,
    val diagnosisName: String,
    val probability: Double,
    val supportingSymptoms: List<String>
)

/**
 * представлення медичної статистики
 */
data class MedicalStatistics(
    val totalPatients: Int,
    val totalAppointments: Int,
    val completedAppointments: Int,
    val cancelledAppointments: Int,
    val noShowAppointments: Int,
    val averageWaitTime: Double, // в хвилинах
    val commonDiagnoses: List<DiagnosisCount>,
    val treatmentOutcomes: List<TreatmentOutcome>
)

/**
 * представлення кількості діагнозів
 */
data class DiagnosisCount(
    val diagnosisCode: String,
    val diagnosisName: String,
    val count: Int
)

/**
 * представлення результату лікування
 */
data class TreatmentOutcome(
    val treatmentType: TreatmentType,
    val successRate: Double,
    val averageDuration: Double, // в днях
    val complicationRate: Double
)

/**
 * представлення медичного пристрою