/**
 * xsparkyproject - розширена бібліотека для kotlin
 *
 * Copyright 2025 Андрій Будильников
 */

package com.sparky.xsparkyproject.utils

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.*
import kotlin.math.abs

/**
 * утилітарний клас для роботи з датами та часом
 *
 * @author андрій будильников
 * @since 1.0.0
 */
@Suppress("unused")
class DateUtils {
    
    companion object {
        // стандартні формати дат
        const val ISO_DATE_FORMAT = "yyyy-MM-dd"
        const val ISO_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
        const val US_DATE_FORMAT = "MM/dd/yyyy"
        const val EU_DATE_FORMAT = "dd/MM/yyyy"
        const val RU_DATE_FORMAT = "dd.MM.yyyy"
        const val UK_DATE_FORMAT = "dd.MM.yyyy"
        const val TIME_FORMAT = "HH:mm:ss"
        const val TIME_FORMAT_WITH_MILLIS = "HH:mm:ss.SSS"
        
        // часові пояси
        val UTC_ZONE = ZoneId.of("UTC")
        val KYIV_ZONE = ZoneId.of("Europe/Kiev")
        val MOSCOW_ZONE = ZoneId.of("Europe/Moscow")
        val NEW_YORK_ZONE = ZoneId.of("America/New_York")
        val LONDON_ZONE = ZoneId.of("Europe/London")
    }
    
    // базові функції для створення дат
    
    /**
     * створює об'єкт LocalDate з року, місяця та дня
     *
     * @param year рік
     * @param month місяць (1-12)
     * @param day день місяця
     * @return об'єкт LocalDate
     */
    fun createDate(year: Int, month: Int, day: Int): LocalDate {
        return LocalDate.of(year, month, day)
    }
    
    /**
     * створює об'єкт LocalDateTime з дати та часу
     *
     * @param year рік
     * @param month місяць (1-12)
     * @param day день місяця
     * @param hour година (0-23)
     * @param minute хвилина (0-59)
     * @param second секунда (0-59)
     * @return об'єкт LocalDateTime
     */
    fun createDateTime(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0, second: Int = 0): LocalDateTime {
        return LocalDateTime.of(year, month, day, hour, minute, second)
    }
    
    /**
     * створює об'єкт ZonedDateTime з дати, часу та часового поясу
     *
     * @param year рік
     * @param month місяць (1-12)
     * @param day день місяця
     * @param hour година (0-23)
     * @param minute хвилина (0-59)
     * @param second секунда (0-59)
     * @param zoneId часовий пояс
     * @return об'єкт ZonedDateTime
     */
    fun createZonedDateTime(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 0,
        minute: Int = 0,
        second: Int = 0,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): ZonedDateTime {
        val localDateTime = LocalDateTime.of(year, month, day, hour, minute, second)
        return ZonedDateTime.of(localDateTime, zoneId)
    }
    
    // функції для парсингу дат
    
    /**
     * парсить дату з рядка у форматі yyyy-MM-dd
     *
     * @param dateString рядок з датою
     * @return об'єкт LocalDate
     */
    fun parseIsoDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(ISO_DATE_FORMAT))
    }
    
    /**
     * парсить дату та час з рядка у форматі yyyy-MM-dd HH:mm:ss
     *
     * @param dateTimeString рядок з датою та часом
     * @return об'єкт LocalDateTime
     */
    fun parseIsoDateTime(dateTimeString: String): LocalDateTime {
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(ISO_DATE_TIME_FORMAT))
    }
    
    /**
     * парсить дату з рядка у заданому форматі
     *
     * @param dateString рядок з датою
     * @param pattern формат дати
     * @return об'єкт LocalDate
     */
    fun parseDate(dateString: String, pattern: String): LocalDate {
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern))
    }
    
    /**
     * парсить дату та час з рядка у заданому форматі
     *
     * @param dateTimeString рядок з датою та часом
     * @param pattern формат дати та часу
     * @return об'єкт LocalDateTime
     */
    fun parseDateTime(dateTimeString: String, pattern: String): LocalDateTime {
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(pattern))
    }
    
    /**
     * парсить дату та час з рядка у заданому форматі та часовому поясі
     *
     * @param dateTimeString рядок з датою та часом
     * @param pattern формат дати та часу
     * @param zoneId часовий пояс
     * @return об'єкт ZonedDateTime
     */
    fun parseZonedDateTime(dateTimeString: String, pattern: String, zoneId: ZoneId): ZonedDateTime {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        val localDateTime = LocalDateTime.parse(dateTimeString, formatter)
        return ZonedDateTime.of(localDateTime, zoneId)
    }
    
    // функції для форматування дат
    
    /**
     * форматує дату у форматі yyyy-MM-dd
     *
     * @param date дата
     * @return форматований рядок
     */
    fun formatIsoDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern(ISO_DATE_FORMAT))
    }
    
    /**
     * форматує дату та час у форматі yyyy-MM-dd HH:mm:ss
     *
     * @param dateTime дата та час
     * @return форматований рядок
     */
    fun formatIsoDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(DateTimeFormatter.ofPattern(ISO_DATE_TIME_FORMAT))
    }
    
    /**
     * форматує дату у заданому форматі
     *
     * @param date дата
     * @param pattern формат
     * @return форматований рядок
     */
    fun formatDate(date: LocalDate, pattern: String): String {
        return date.format(DateTimeFormatter.ofPattern(pattern))
    }
    
    /**
     * форматує дату та час у заданому форматі
     *
     * @param dateTime дата та час
     * @param pattern формат
     * @return форматований рядок
     */
    fun formatDateTime(dateTime: LocalDateTime, pattern: String): String {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern))
    }
    
    /**
     * форматує дату та час у заданому форматі та часовому поясі
     *
     * @param dateTime дата та час
     * @param pattern формат
     * @param zoneId часовий пояс
     * @return форматований рядок
     */
    fun formatZonedDateTime(dateTime: ZonedDateTime, pattern: String, zoneId: ZoneId): String {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return dateTime.withZoneSameInstant(zoneId).format(formatter)
    }
    
    // функції для порівняння дат
    
    /**
     * перевіряє, чи перша дата раніше за другу
     *
     * @param first перша дата
     * @param second друга дата
     * @return true якщо перша дата раніше
     */
    fun isBefore(first: LocalDate, second: LocalDate): Boolean {
        return first.isBefore(second)
    }
    
    /**
     * перевіряє, чи перша дата пізніше за другу
     *
     * @param first перша дата
     * @param second друга дата
     * @return true якщо перша дата пізніше
     */
    fun isAfter(first: LocalDate, second: LocalDate): Boolean {
        return first.isAfter(second)
    }
    
    /**
     * перевіряє, чи дві дати рівні
     *
     * @param first перша дата
     * @param second друга дата
     * @return true якщо дати рівні
     */
    fun isEqual(first: LocalDate, second: LocalDate): Boolean {
        return first.isEqual(second)
    }
    
    /**
     * перевіряє, чи перший момент часу раніше за другий
     *
     * @param first перший момент часу
     * @param second другий момент часу
     * @return true якщо перший момент часу раніше
     */
    fun isBefore(first: LocalDateTime, second: LocalDateTime): Boolean {
        return first.isBefore(second)
    }
    
    /**
     * перевіряє, чи перший момент часу пізніше за другий
     *
     * @param first перший момент часу
     * @param second другий момент часу
     * @return true якщо перший момент часу пізніше
     */
    fun isAfter(first: LocalDateTime, second: LocalDateTime): Boolean {
        return first.isAfter(second)
    }
    
    /**
     * перевіряє, чи два моменти часу рівні
     *
     * @param first перший момент часу
     * @param second другий момент часу
     * @return true якщо моменти часу рівні
     */
    fun isEqual(first: LocalDateTime, second: LocalDateTime): Boolean {
        return first.isEqual(second)
    }
    
    // функції для арифметики дат
    
    /**
     * додає дні до дати
     *
     * @param date дата
     * @param days кількість днів
     * @return нова дата
     */
    fun addDays(date: LocalDate, days: Long): LocalDate {
        return date.plusDays(days)
    }
    
    /**
     * додає місяці до дати
     *
     * @param date дата
     * @param months кількість місяців
     * @return нова дата
     */
    fun addMonths(date: LocalDate, months: Long): LocalDate {
        return date.plusMonths(months)
    }
    
    /**
     * додає роки до дати
     *
     * @param date дата
     * @param years кількість років
     * @return нова дата
     */
    fun addYears(date: LocalDate, years: Long): LocalDate {
        return date.plusYears(years)
    }
    
    /**
     * додає години до моменту часу
     *
     * @param dateTime момент часу
     * @param hours кількість годин
     * @return новий момент часу
     */
    fun addHours(dateTime: LocalDateTime, hours: Long): LocalDateTime {
        return dateTime.plusHours(hours)
    }
    
    /**
     * додає хвилини до моменту часу
     *
     * @param dateTime момент часу
     * @param minutes кількість хвилин
     * @return новий момент часу
     */
    fun addMinutes(dateTime: LocalDateTime, minutes: Long): LocalDateTime {
        return dateTime.plusMinutes(minutes)
    }
    
    /**
     * додає секунди до моменту часу
     *
     * @param dateTime момент часу
     * @param seconds кількість секунд
     * @return новий момент часу
     */
    fun addSeconds(dateTime: LocalDateTime, seconds: Long): LocalDateTime {
        return dateTime.plusSeconds(seconds)
    }
    
    /**
     * віднімає дні від дати
     *
     * @param date дата
     * @param days кількість днів
     * @return нова дата
     */
    fun subtractDays(date: LocalDate, days: Long): LocalDate {
        return date.minusDays(days)
    }
    
    /**
     * віднімає місяці від дати
     *
     * @param date дата
     * @param months кількість місяців
     * @return нова дата
     */
    fun subtractMonths(date: LocalDate, months: Long): LocalDate {
        return date.minusMonths(months)
    }
    
    /**
     * віднімає роки від дати
     *
     * @param date дата
     * @param years кількість років
     * @return нова дата
     */
    fun subtractYears(date: LocalDate, years: Long): LocalDate {
        return date.minusYears(years)
    }
    
    // функції для обчислення різниці між датами
    
    /**
     * обчислює різницю між датами в днях
     *
     * @param start початкова дата
     * @param end кінцева дата
     * @return різниця в днях
     */
    fun daysBetween(start: LocalDate, end: LocalDate): Long {
        return ChronoUnit.DAYS.between(start, end)
    }
    
    /**
     * обчислює різницю між датами в місяцях
     *
     * @param start початкова дата
     * @param end кінцева дата
     * @return різниця в місяцях
     */
    fun monthsBetween(start: LocalDate, end: LocalDate): Long {
        return ChronoUnit.MONTHS.between(start, end)
    }
    
    /**
     * обчислює різницю між датами в роках
     *
     * @param start початкова дата
     * @param end кінцева дата
     * @return різниця в роках
     */
    fun yearsBetween(start: LocalDate, end: LocalDate): Long {
        return ChronoUnit.YEARS.between(start, end)
    }
    
    /**
     * обчислює різницю між моментами часу в годинах
     *
     * @param start початковий момент часу
     * @param end кінцевий момент часу
     * @return різниця в годинах
     */
    fun hoursBetween(start: LocalDateTime, end: LocalDateTime): Long {
        return ChronoUnit.HOURS.between(start, end)
    }
    
    /**
     * обчислює різницю між моментами часу в хвилинах
     *
     * @param start початковий момент часу
     * @param end кінцевий момент часу
     * @return різниця в хвилинах
     */
    fun minutesBetween(start: LocalDateTime, end: LocalDateTime): Long {
        return ChronoUnit.MINUTES.between(start, end)
    }
    
    /**
     * обчислює різницю між моментами часу в секундах
     *
     * @param start початковий момент часу
     * @param end кінцевий момент часу
     * @return різниця в секундах
     */
    fun secondsBetween(start: LocalDateTime, end: LocalDateTime): Long {
        return ChronoUnit.SECONDS.between(start, end)
    }
    
    // функції для роботи з тижнями
    
    /**
     * отримує перший день тижня для заданої дати
     *
     * @param date дата
     * @return перший день тижня (понеділок)
     */
    fun startOfWeek(date: LocalDate): LocalDate {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
    
    /**
     * отримує останній день тижня для заданої дати
     *
     * @param date дата
     * @return останній день тижня (неділя)
     */
    fun endOfWeek(date: LocalDate): LocalDate {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    }
    
    /**
     * перевіряє, чи дата є початком тижня (понеділок)
     *
     * @param date дата
     * @return true якщо дата є початком тижня
     */
    fun isStartOfWeek(date: LocalDate): Boolean {
        return date.dayOfWeek == DayOfWeek.MONDAY
    }
    
    /**
     * перевіряє, чи дата є кінцем тижня (неділя)
     *
     * @param date дата
     * @return true якщо дата є кінцем тижня
     */
    fun isEndOfWeek(date: LocalDate): Boolean {
        return date.dayOfWeek == DayOfWeek.SUNDAY
    }
    
    // функції для роботи з місяцями
    
    /**
     * отримує перший день місяця для заданої дати
     *
     * @param date дата
     * @return перший день місяця
     */
    fun startOfMonth(date: LocalDate): LocalDate {
        return date.withDayOfMonth(1)
    }
    
    /**
     * отримує останній день місяця для заданої дати
     *
     * @param date дата
     * @return останній день місяця
     */
    fun endOfMonth(date: LocalDate): LocalDate {
        return date.with(TemporalAdjusters.lastDayOfMonth())
    }
    
    /**
     * перевіряє, чи дата є початком місяця
     *
     * @param date дата
     * @return true якщо дата є початком місяця
     */
    fun isStartOfMonth(date: LocalDate): Boolean {
        return date.dayOfMonth == 1
    }
    
    /**
     * перевіряє, чи дата є кінцем місяця
     *
     * @param date дата
     * @return true якщо дата є кінцем місяця
     */
    fun isEndOfMonth(date: LocalDate): Boolean {
        return date.dayOfMonth == date.lengthOfMonth()
    }
    
    /**
     * отримує назву місяця українською мовою
     *
     * @param month місяць
     * @return назва місяця
     */
    fun getMonthNameUkrainian(month: Month): String {
        return when (month) {
            Month.JANUARY -> "січень"
            Month.FEBRUARY -> "лютий"
            Month.MARCH -> "березень"
            Month.APRIL -> "квітень"
            Month.MAY -> "травень"
            Month.JUNE -> "червень"
            Month.JULY -> "липень"
            Month.AUGUST -> "серпень"
            Month.SEPTEMBER -> "вересень"
            Month.OCTOBER -> "жовтень"
            Month.NOVEMBER -> "листопад"
            Month.DECEMBER -> "грудень"
        }
    }
    
    /**
     * отримує назву місяця російською мовою
     *
     * @param month місяць
     * @return назва місяця
     */
    fun getMonthNameRussian(month: Month): String {
        return when (month) {
            Month.JANUARY -> "январь"
            Month.FEBRUARY -> "февраль"
            Month.MARCH -> "март"
            Month.APRIL -> "апрель"
            Month.MAY -> "май"
            Month.JUNE -> "июнь"
            Month.JULY -> "июль"
            Month.AUGUST -> "август"
            Month.SEPTEMBER -> "сентябрь"
            Month.OCTOBER -> "октябрь"
            Month.NOVEMBER -> "ноябрь"
            Month.DECEMBER -> "декабрь"
        }
    }
    
    /**
     * отримує назву місяця англійською мовою
     *
     * @param month місяць
     * @return назва місяця
     */
    fun getMonthNameEnglish(month: Month): String {
        return when (month) {
            Month.JANUARY -> "january"
            Month.FEBRUARY -> "february"
            Month.MARCH -> "march"
            Month.APRIL -> "april"
            Month.MAY -> "may"
            Month.JUNE -> "june"
            Month.JULY -> "july"
            Month.AUGUST -> "august"
            Month.SEPTEMBER -> "september"
            Month.OCTOBER -> "october"
            Month.NOVEMBER -> "november"
            Month.DECEMBER -> "december"
        }
    }
    
    // функції для роботи з роками
    
    /**
     * отримує перший день року для заданої дати
     *
     * @param date дата
     * @return перший день року
     */
    fun startOfYear(date: LocalDate): LocalDate {
        return date.withDayOfYear(1)
    }
    
    /**
     * отримує останній день року для заданої дати
     *
     * @param date дата
     * @return останній день року
     */
    fun endOfYear(date: LocalDate): LocalDate {
        return date.with(TemporalAdjusters.lastDayOfYear())
    }
    
    /**
     * перевіряє, чи дата є початком року
     *
     * @param date дата
     * @return true якщо дата є початком року
     */
    fun isStartOfYear(date: LocalDate): Boolean {
        return date.dayOfYear == 1
    }
    
    /**
     * перевіряє, чи дата є кінцем року
     *
     * @param date дата
     * @return true якщо дата є кінцем року
     */
    fun isEndOfYear(date: LocalDate): Boolean {
        return date.dayOfYear == date.lengthOfYear()
    }
    
    /**
     * перевіряє, чи рік є високосним
     *
     * @param year рік
     * @return true якщо рік є високосним
     */
    fun isLeapYear(year: Int): Boolean {
        return Year.of(year).isLeap
    }
    
    /**
     * отримує кількість днів у році
     *
     * @param year рік
     * @return кількість днів у році
     */
    fun daysInYear(year: Int): Int {
        return Year.of(year).length()
    }
    
    /**
     * отримує кількість днів у місяці
     *
     * @param year рік
     * @param month місяць
     * @return кількість днів у місяці
     */
    fun daysInMonth(year: Int, month: Int): Int {
        return YearMonth.of(year, month).lengthOfMonth()
    }
    
    // функції для роботи з днями тижня
    
    /**
     * отримує назву дня тижня українською мовою
     *
     * @param dayOfWeek день тижня
     * @return назва дня тижня
     */
    fun getDayOfWeekNameUkrainian(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "понеділок"
            DayOfWeek.TUESDAY -> "вівторок"
            DayOfWeek.WEDNESDAY -> "середа"
            DayOfWeek.THURSDAY -> "четвер"
            DayOfWeek.FRIDAY -> "п'ятниця"
            DayOfWeek.SATURDAY -> "субота"
            DayOfWeek.SUNDAY -> "неділя"
        }
    }
    
    /**
     * отримує назву дня тижня російською мовою
     *
     * @param dayOfWeek день тижня
     * @return назва дня тижня
     */
    fun getDayOfWeekNameRussian(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "понедельник"
            DayOfWeek.TUESDAY -> "вторник"
            DayOfWeek.WEDNESDAY -> "среда"
            DayOfWeek.THURSDAY -> "четверг"
            DayOfWeek.FRIDAY -> "пятница"
            DayOfWeek.SATURDAY -> "суббота"
            DayOfWeek.SUNDAY -> "воскресенье"
        }
    }
    
    /**
     * отримує назву дня тижня англійською мовою
     *
     * @param dayOfWeek день тижня
     * @return назва дня тижня
     */
    fun getDayOfWeekNameEnglish(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "monday"
            DayOfWeek.TUESDAY -> "tuesday"
            DayOfWeek.WEDNESDAY -> "wednesday"
            DayOfWeek.THURSDAY -> "thursday"
            DayOfWeek.FRIDAY -> "friday"
            DayOfWeek.SATURDAY -> "saturday"
            DayOfWeek.SUNDAY -> "sunday"
        }
    }
    
    /**
     * перевіряє, чи день є вихідним
     *
     * @param dayOfWeek день тижня
     * @return true якщо день є вихідним
     */
    fun isWeekend(dayOfWeek: DayOfWeek): Boolean {
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
    }
    
    /**
     * перевіряє, чи дата є вихідним днем
     *
     * @param date дата
     * @return true якщо дата є вихідним днем
     */
    fun isWeekend(date: LocalDate): Boolean {
        return isWeekend(date.dayOfWeek)
    }
    
    /**
     * перевіряє, чи день є робочим
     *
     * @param dayOfWeek день тижня
     * @return true якщо день є робочим
     */
    fun isWorkday(dayOfWeek: DayOfWeek): Boolean {
        return !isWeekend(dayOfWeek)
    }
    
    /**
     * перевіряє, чи дата є робочим днем
     *
     * @param date дата
     * @return true якщо дата є робочим днем
     */
    fun isWorkday(date: LocalDate): Boolean {
        return isWorkday(date.dayOfWeek)
    }
    
    // функції для роботи з часовими поясами
    
    /**
     * конвертує дату та час з одного часового поясу в інший
     *
     * @param dateTime дата та час
     * @param fromZone початковий часовий пояс
     * @param toZone цільовий часовий пояс
     * @return дата та час у новому часовому поясі
     */
    fun convertTimeZone(dateTime: LocalDateTime, fromZone: ZoneId, toZone: ZoneId): ZonedDateTime {
        val zonedDateTime = ZonedDateTime.of(dateTime, fromZone)
        return zonedDateTime.withZoneSameInstant(toZone)
    }
    
    /**
     * отримує поточний час у заданому часовому поясі
     *
     * @param zoneId часовий пояс
     * @return поточний час
     */
    fun getCurrentTimeInZone(zoneId: ZoneId): ZonedDateTime {
        return ZonedDateTime.now(zoneId)
    }
    
    /**
     * отримує різницю між часовими поясами в годинах
     *
     * @param zone1 перший часовий пояс
     * @param zone2 другий часовий пояс
     * @return різниця в годинах
     */
    fun getTimeZoneDifference(zone1: ZoneId, zone2: ZoneId): Int {
        val now = Instant.now()
        val offset1 = zone1.rules.getOffset(now)
        val offset2 = zone2.rules.getOffset(now)
        return (offset2.totalSeconds - offset1.totalSeconds) / 3600
    }
    
    // функції для роботи з інтервалами
    
    /**
     * представлення інтервалу часу
     *
     * @property start початок інтервалу
     * @property end кінець інтервалу
     */
    data class DateTimeInterval(val start: LocalDateTime, val end: LocalDateTime) {
        
        init {
            if (start.isAfter(end)) {
                throw IllegalArgumentException("початок інтервалу не може бути пізніше за кінець")
            }
        }
        
        /**
         * перевіряє, чи інтервал містить заданий момент часу
         *
         * @param dateTime момент часу
         * @return true якщо інтервал містить момент часу
         */
        fun contains(dateTime: LocalDateTime): Boolean {
            return !dateTime.isBefore(start) && !dateTime.isAfter(end)
        }
        
        /**
         * перевіряє, чи інтервал перетинається з іншим інтервалом
         *
         * @param other інший інтервал
         * @return true якщо інтервали перетинаються
         */
        fun intersects(other: DateTimeInterval): Boolean {
            return !(this.end.isBefore(other.start) || this.start.isAfter(other.end))
        }
        
        /**
         * обчислює тривалість інтервалу в хвилинах
         *
         * @return тривалість в хвилинах
         */
        fun durationInMinutes(): Long {
            return ChronoUnit.MINUTES.between(start, end)
        }
        
        /**
         * обчислює тривалість інтервалу в годинах
         *
         * @return тривалість в годинах
         */
        fun durationInHours(): Long {
            return ChronoUnit.HOURS.between(start, end)
        }
        
        /**
         * обчислює тривалість інтервалу в днях
         *
         * @return тривалість в днях
         */
        fun durationInDays(): Long {
            return ChronoUnit.DAYS.between(start, end)
        }
    }
    
    /**
     * створює інтервал часу
     *
     * @param start початок інтервалу
     * @param end кінець інтервалу
     * @return інтервал часу
     */
    fun createInterval(start: LocalDateTime, end: LocalDateTime): DateTimeInterval {
        return DateTimeInterval(start, end)
    }
    
    /**
     * створює інтервал часу з дат
     *
     * @param startDate початкова дата
     * @param endDate кінцева дата
     * @return інтервал часу
     */
    fun createInterval(startDate: LocalDate, endDate: LocalDate): DateTimeInterval {
        val start = startDate.atStartOfDay()
        val end = endDate.atTime(23, 59, 59)
        return DateTimeInterval(start, end)
    }
    
    // функції для роботи з святковими днями
    
    /**
     * представлення святкового дня
     *
     * @property date дата свята
     * @property name назва свята
     * @property countryCode код країни
     */
    data class Holiday(val date: LocalDate, val name: String, val countryCode: String)
    
    /**
     * отримує список святкових днів для України
     *
     * @param year рік
     * @return список святкових днів
     */
    fun getUkrainianHolidays(year: Int): List<Holiday> {
        val holidays = mutableListOf<Holiday>()
        
        // новий рік
        holidays.add(Holiday(LocalDate.of(year, 1, 1), "Новий рік", "UA"))
        
        // день незалежності
        holidays.add(Holiday(LocalDate.of(year, 8, 24), "День незалежності України", "UA"))
        
        // день захисника україни
        holidays.add(Holiday(LocalDate.of(year, 10, 14), "День захисника України", "UA"))
        
        // католицьке різдво
        holidays.add(Holiday(LocalDate.of(year, 12, 25), "Різдво Христове (католицьке)", "UA"))
        
        // православне різдво
        holidays.add(Holiday(LocalDate.of(year, 1, 7), "Різдво Христове (православне)", "UA"))
        
        // день пам'яті та примирення
        holidays.add(Holiday(LocalDate.of(year, 5, 8), "День пам'яті та примирення", "UA"))
        
        // день перемоги над нацизмом у другій світовій війні
        holidays.add(Holiday(LocalDate.of(year, 5, 9), "День перемоги над нацизмом у Другій світовій війні", "UA"))
        
        // день конституції україни
        holidays.add(Holiday(LocalDate.of(year, 6, 28), "День Конституції України", "UA"))
        
        // день української державності
        holidays.add(Holiday(LocalDate.of(year, 7, 15), "День української державності", "UA"))
        
        // день знань
        holidays.add(Holiday(LocalDate.of(year, 9, 1), "День знань", "UA"))
        
        // день вчителя
        holidays.add(Holiday(LocalDate.of(year, 10, 5), "День вчителя", "UA"))
        
        // день гідності та свободи
        holidays.add(Holiday(LocalDate.of(year, 11, 21), "День гідності та свободи", "UA"))
        
        return holidays
    }
    
    /**
     * отримує список святкових днів для Росії
     *
     * @param year рік
     * @return список святкових днів
     */
    fun getRussianHolidays(year: Int): List<Holiday> {
        val holidays = mutableListOf<Holiday>()
        
        // новий рік
        holidays.add(Holiday(LocalDate.of(year, 1, 1), "Новый год", "RU"))
        
        // день захисника вітчизни
        holidays.add(Holiday(LocalDate.of(year, 2, 23), "День защитника Отечества", "RU"))
        
        // міжнародний жіночий день
        holidays.add(Holiday(LocalDate.of(year, 3, 8), "Международный женский день", "RU"))
        
        // день праці
        holidays.add(Holiday(LocalDate.of(year, 5, 1), "Праздник Весны и Труда", "RU"))
        
        // день перемоги
        holidays.add(Holiday(LocalDate.of(year, 5, 9), "День Победы", "RU"))
        
        // день росії
        holidays.add(Holiday(LocalDate.of(year, 6, 12), "День России", "RU"))
        
        // день народного єднання
        holidays.add(Holiday(LocalDate.of(year, 11, 4), "День народного единства", "RU"))
        
        return holidays
    }
    
    /**
     * перевіряє, чи дата є святковим днем для заданої країни
     *
     * @param date дата
     * @param countryCode код країни
     * @return true якщо дата є святковим днем
     */
    fun isHoliday(date: LocalDate, countryCode: String): Boolean {
        val holidays = when (countryCode.uppercase()) {
            "UA" -> getUkrainianHolidays(date.year)
            "RU" -> getRussianHolidays(date.year)
            else -> emptyList()
        }
        
        return holidays.any { it.date.isEqual(date) }
    }
    
    /**
     * отримує назву свята для заданої дати та країни
     *
     * @param date дата
     * @param countryCode код країни
     * @return назва свята або null якщо дата не є святковою
     */
    fun getHolidayName(date: LocalDate, countryCode: String): String? {
        val holidays = when (countryCode.uppercase()) {
            "UA" -> getUkrainianHolidays(date.year)
            "RU" -> getRussianHolidays(date.year)
            else -> emptyList()
        }
        
        return holidays.find { it.date.isEqual(date) }?.name
    }
    
    // функції для роботи з віком
    
    /**
     * обчислює вік на задану дату
     *
     * @param birthDate дата народження
     * @param currentDate поточна дата
     * @return вік в роках
     */
    fun calculateAge(birthDate: LocalDate, currentDate: LocalDate = LocalDate.now()): Int {
        return Period.between(birthDate, currentDate).years
    }
    
    /**
     * перевіряє, чи особа досягла певного віку
     *
     * @param birthDate дата народження
     * @param requiredAge необхідний вік
     * @param currentDate поточна дата
     * @return true якщо особа досягла необхідного віку
     */
    fun hasReachedAge(birthDate: LocalDate, requiredAge: Int, currentDate: LocalDate = LocalDate.now()): Boolean {
        return calculateAge(birthDate, currentDate) >= requiredAge
    }
    
    // функції для роботи з кварталами
    
    /**
     * отримує номер кварталу для заданої дати
     *
     * @param date дата
     * @return номер кварталу (1-4)
     */
    fun getQuarter(date: LocalDate): Int {
        return (date.monthValue - 1) / 3 + 1
    }
    
    /**
     * отримує перший день кварталу для заданої дати
     *
     * @param date дата
     * @return перший день кварталу
     */
    fun startOfQuarter(date: LocalDate): LocalDate {
        val quarter = getQuarter(date)
        val month = (quarter - 1) * 3 + 1
        return LocalDate.of(date.year, month, 1)
    }
    
    /**
     * отримує останній день кварталу для заданої дати
     *
     * @param date дата
     * @return останній день кварталу
     */
    fun endOfQuarter(date: LocalDate): LocalDate {
        val quarter = getQuarter(date)
        val month = quarter * 3
        val lastDay = YearMonth.of(date.year, month).lengthOfMonth()
        return LocalDate.of(date.year, month, lastDay)
    }
    
    // функції для роботи з півріччями
    
    /**
     * отримує номер півріччя для заданої дати
     *
     * @param date дата
     * @return номер півріччя (1-2)
     */
    fun getHalfYear(date: LocalDate): Int {
        return (date.monthValue - 1) / 6 + 1
    }
    
    /**
     * отримує перший день півріччя для заданої дати
     *
     * @param date дата
     * @return перший день півріччя
     */
    fun startOfHalfYear(date: LocalDate): LocalDate {
        val halfYear = getHalfYear(date)
        val month = (halfYear - 1) * 6 + 1
        return LocalDate.of(date.year, month, 1)
    }
    
    /**
     * отримує останній день півріччя для заданої дати
     *
     * @param date дата
     * @return останній день півріччя
     */
    fun endOfHalfYear(date: LocalDate): LocalDate {
        val halfYear = getHalfYear(date)
        val month = halfYear * 6
        val lastDay = YearMonth.of(date.year, month).lengthOfMonth()
        return LocalDate.of(date.year, month, lastDay)
    }
    
    // функції для роботи з робочими днями
    
    /**
     * обчислює кількість робочих днів між двома датами
     *
     * @param start початкова дата
     * @param end кінцева дата
     * @param countryCode код країни
     * @return кількість робочих днів
     */
    fun workDaysBetween(start: LocalDate, end: LocalDate, countryCode: String = "UA"): Long {
        var workDays = 0L
        var currentDate = start
        
        while (!currentDate.isAfter(end)) {
            if (isWorkday(currentDate) && !isHoliday(currentDate, countryCode)) {
                workDays++
            }
            currentDate = currentDate.plusDays(1)
        }
        
        return workDays
    }
    
    /**
     * додає робочі дні до дати
     *
     * @param date дата
     * @param workDays кількість робочих днів
     * @param countryCode код країни
     * @return нова дата
     */
    fun addWorkDays(date: LocalDate, workDays: Int, countryCode: String = "UA"): LocalDate {
        var resultDate = date
        var remainingDays = workDays
        
        while (remainingDays > 0) {
            resultDate = resultDate.plusDays(1)
            if (isWorkday(resultDate) && !isHoliday(resultDate, countryCode)) {
                remainingDays--
            }
        }
        
        return resultDate
    }
    
    /**
     * віднімає робочі дні від дати
     *
     * @param date дата
     * @param workDays кількість робочих днів
     * @param countryCode код країни
     * @return нова дата
     */
    fun subtractWorkDays(date: LocalDate, workDays: Int, countryCode: String = "UA"): LocalDate {
        var resultDate = date
        var remainingDays = workDays
        
        while (remainingDays > 0) {
            resultDate = resultDate.minusDays(1)
            if (isWorkday(resultDate) && !isHoliday(resultDate, countryCode)) {
                remainingDays--
            }
        }
        
        return resultDate
    }
    
    // функції для роботи з форматами дат
    
    /**
     * отримує список стандартних форматів дат
     *
     * @return список форматів дат
     */
    fun getDateFormats(): List<String> {
        return listOf(
            ISO_DATE_FORMAT,
            US_DATE_FORMAT,
            EU_DATE_FORMAT,
            RU_DATE_FORMAT,
            UK_DATE_FORMAT
        )
    }
    
    /**
     * отримує список стандартних форматів часу
     *
     * @return список форматів часу
     */
    fun getTimeFormats(): List<String> {
        return listOf(
            TIME_FORMAT,
            TIME_FORMAT_WITH_MILLIS
        )
    }
    
    /**
     * отримує список стандартних форматів дати та часу
     *
     * @return список форматів дати та часу
     */
    fun getDateTimeFormats(): List<String> {
        val formats = mutableListOf<String>()
        for (dateFormat in getDateFormats()) {
            for (timeFormat in getTimeFormats()) {
                formats.add("$dateFormat $timeFormat")
            }
        }
        return formats
    }
    
    /**
     * намагається парсити дату з рядка, використовуючи різні формати
     *
     * @param dateString рядок з датою
     * @return об'єкт LocalDate або null якщо парсинг не вдався
     */
    fun tryParseDate(dateString: String): LocalDate? {
        for (format in getDateFormats()) {
            try {
                return parseDate(dateString, format)
            } catch (e: DateTimeParseException) {
                // продовжуємо з наступним форматом
            }
        }
        return null
    }
    
    /**
     * намагається парсити дату та час з рядка, використовуючи різні формати
     *
     * @param dateTimeString рядок з датою та часом
     * @return об'єкт LocalDateTime або null якщо парсинг не вдався
     */
    fun tryParseDateTime(dateTimeString: String): LocalDateTime? {
        for (format in getDateTimeFormats()) {
            try {
                return parseDateTime(dateTimeString, format)
            } catch (e: DateTimeParseException) {
                // продовжуємо з наступним форматом
            }
        }
        return null
    }
    
    // функції для роботи з таймерами
    
    /**
     * представлення таймера
     *
     * @property name назва таймера
     */
    class Timer(val name: String) {
        private var startTime: Instant? = null
        private var endTime: Instant? = null
        
        /**
         * запускає таймер
         */
        fun start() {
            startTime = Instant.now()
            endTime = null
        }
        
        /**
         * зупиняє таймер
         */
        fun stop() {
            if (startTime != null) {
                endTime = Instant.now()
            }
        }
        
        /**
         * отримує тривалість у наносекундах
         *
         * @return тривалість у наносекундах
         */
        fun getDurationNanos(): Long {
            return if (startTime != null && endTime != null) {
                Duration.between(startTime, endTime).toNanos()
            } else {
                0L
            }
        }
        
        /**
         * отримує тривалість у мілісекундах
         *
         * @return тривалість у мілісекундах
         */
        fun getDurationMillis(): Long {
            return if (startTime != null && endTime != null) {
                Duration.between(startTime, endTime).toMillis()
            } else {
                0L
            }
        }
        
        /**
         * отримує тривалість у секундах
         *
         * @return тривалість у секундах
         */
        fun getDurationSeconds(): Long {
            return if (startTime != null && endTime != null) {
                Duration.between(startTime, endTime).seconds
            } else {
                0L
            }
        }
        
        /**
         * отримує тривалість у хвилинах
         *
         * @return тривалість у хвилинах
         */
        fun getDurationMinutes(): Long {
            return if (startTime != null && endTime != null) {
                Duration.between(startTime, endTime).toMinutes()
            } else {
                0L
            }
        }
        
        /**
         * отримує тривалість у годинах
         *
         * @return тривалість у годинах
         */
        fun getDurationHours(): Long {
            return if (startTime != null && endTime != null) {
                Duration.between(startTime, endTime).toHours()
            } else {
                0L
            }
        }
        
        /**
         * отримує форматовану тривалість
         *
         * @return форматована тривалість
         */
        fun getFormattedDuration(): String {
            val nanos = getDurationNanos()
            return when {
                nanos < 1_000 -> "${nanos}ns"
                nanos < 1_000_000 -> "${nanos / 1_000}μs"
                nanos < 1_000_000_000 -> "${nanos / 1_000_000}ms"
                else -> "${nanos / 1_000_000_000}s"
            }
        }
    }
    
    /**
     * створює таймер
     *
     * @param name назва таймера
     * @return таймер
     */
    fun createTimer(name: String): Timer {
        return Timer(name)
    }
    
    // функції для роботи з календарними періодами
    
    /**
     * представлення календарного періоду
     *
     * @property years роки
     * @property months місяці
     * @property days дні
     */
    data class CalendarPeriod(val years: Int, val months: Int, val days: Int) {
        
        /**
         * додає два календарних періоди
         *
         * @param other інший період
         * @return сума періодів
         */
        operator fun plus(other: CalendarPeriod): CalendarPeriod {
            return CalendarPeriod(
                this.years + other.years,
                this.months + other.months,
                this.days + other.days
            )
        }
        
        /**
         * віднімає два календарних періоди
         *
         * @param other інший період
         * @return різниця періодів
         */
        operator fun minus(other: CalendarPeriod): CalendarPeriod {
            return CalendarPeriod(
                this.years - other.years,
                this.months - other.months,
                this.days - other.days
            )
        }
        
        /**
         * множить період на скаляр
         *
         * @param scalar скаляр
         * @return добуток періоду на скаляр
         */
        operator fun times(scalar: Int): CalendarPeriod {
            return CalendarPeriod(
                this.years * scalar,
                this.months * scalar,
                this.days * scalar
            )
        }
        
        /**
         * обчислює тривалість періоду в днях (приблизно)
         *
         * @return тривалість в днях
         */
        fun toDays(): Int {
            return years * 365 + months * 30 + days
        }
        
        /**
         * обчислює тривалість періоду в місяцях (приблизно)
         *
         * @return тривалість в місяцях
         */
        fun toMonths(): Int {
            return years * 12 + months + days / 30
        }
        
        /**
         * обчислює тривалість періоду в роках (приблизно)
         *
         * @return тривалість в роках
         */
        fun toYears(): Int {
            return years + months / 12 + days / 365
        }
        
        companion object {
            /**
             * порожній період
             */
            val ZERO = CalendarPeriod(0, 0, 0)
            
            /**
             * період в один день
             */
            val ONE_DAY = CalendarPeriod(0, 0, 1)
            
            /**
             * період в один місяць
             */
            val ONE_MONTH = CalendarPeriod(0, 1, 0)
            
            /**
             * період в один рік
             */
            val ONE_YEAR = CalendarPeriod(1, 0, 0)
        }
    }
    
    /**
     * обчислює календарний період між двома датами
     *
     * @param start початкова дата
     * @param end кінцева дата
     * @return календарний період
     */
    fun periodBetween(start: LocalDate, end: LocalDate): CalendarPeriod {
        val period = Period.between(start, end)
        return CalendarPeriod(period.years, period.months, period.days)
    }
    
    /**
     * додає календарний період до дати
     *
     * @param date дата
     * @param period період
     * @return нова дата
     */
    fun addPeriod(date: LocalDate, period: CalendarPeriod): LocalDate {
        return date.plusYears(period.years.toLong())
            .plusMonths(period.months.toLong())
            .plusDays(period.days.toLong())
    }
    
    /**
     * віднімає календарний період від дати
     *
     * @param date дата
     * @param period період
     * @return нова дата
     */
    fun subtractPeriod(date: LocalDate, period: CalendarPeriod): LocalDate {
        return date.minusYears(period.years.toLong())
            .minusMonths(period.months.toLong())
            .minusDays(period.days.toLong())
    }
    
    // функції для роботи з часовими інтервалами
    
    /**
     * представлення інтервалу часу
     *
     * @property duration тривалість
     * @property unit одиниця виміру
     */
    data class TimePeriod(val duration: Long, val unit: ChronoUnit) {
        
        /**
         * додає інтервал часу до моменту часу
         *
         * @param dateTime момент часу
         * @return новий момент часу
         */
        fun addTo(dateTime: LocalDateTime): LocalDateTime {
            return when (unit) {
                ChronoUnit.NANOS -> dateTime.plusNanos(duration)
                ChronoUnit.MICROS -> dateTime.plusNanos(duration * 1000)
                ChronoUnit.MILLIS -> dateTime.plusNanos(duration * 1_000_000)
                ChronoUnit.SECONDS -> dateTime.plusSeconds(duration)
                ChronoUnit.MINUTES -> dateTime.plusMinutes(duration)
                ChronoUnit.HOURS -> dateTime.plusHours(duration)
                ChronoUnit.HALF_DAYS -> dateTime.plusHours(duration * 12)
                ChronoUnit.DAYS -> dateTime.plusDays(duration)
                else -> throw IllegalArgumentException("непідтримувана одиниця часу: $unit")
            }
        }
        
        /**
         * віднімає інтервал часу від моменту часу
         *
         * @param dateTime момент часу
         * @return новий момент часу
         */
        fun subtractFrom(dateTime: LocalDateTime): LocalDateTime {
            return when (unit) {
                ChronoUnit.NANOS -> dateTime.minusNanos(duration)
                ChronoUnit.MICROS -> dateTime.minusNanos(duration * 1000)
                ChronoUnit.MILLIS -> dateTime.minusNanos(duration * 1_000_000)
                ChronoUnit.SECONDS -> dateTime.minusSeconds(duration)
                ChronoUnit.MINUTES -> dateTime.minusMinutes(duration)
                ChronoUnit.HOURS -> dateTime.minusHours(duration)
                ChronoUnit.HALF_DAYS -> dateTime.minusHours(duration * 12)
                ChronoUnit.DAYS -> dateTime.minusDays(duration)
                else -> throw IllegalArgumentException("непідтримувана одиниця часу: $unit")
            }
        }
        
        /**
         * обчислює тривалість у наносекундах
         *
         * @return тривалість у наносекундах
         */
        fun toNanos(): Long {
            return when (unit) {
                ChronoUnit.NANOS -> duration
                ChronoUnit.MICROS -> duration * 1000
                ChronoUnit.MILLIS -> duration * 1_000_000
                ChronoUnit.SECONDS -> duration * 1_000_000_000
                ChronoUnit.MINUTES -> duration * 60 * 1_000_000_000
                ChronoUnit.HOURS -> duration * 3600 * 1_000_000_000
                ChronoUnit.HALF_DAYS -> duration * 12 * 3600 * 1_000_000_000
                ChronoUnit.DAYS -> duration * 24 * 3600 * 1_000_000_000
                else -> throw IllegalArgumentException("непідтримувана одиниця часу: $unit")
            }
        }
        
        /**
         * обчислює тривалість у мілісекундах
         *
         * @return тривалість у мілісекундах
         */
        fun toMillis(): Long {
            return toNanos() / 1_000_000
        }
        
        /**
         * обчислює тривалість у секундах
         *
         * @return тривалість у секундах
         */
        fun toSeconds(): Long {
            return toNanos() / 1_000_000_000
        }
        
        /**
         * обчислює тривалість у хвилинах
         *
         * @return тривалість у хвилинах
         */
        fun toMinutes(): Long {
            return toSeconds() / 60
        }
        
        /**
         * обчислює тривалість у годинах
         *
         * @return тривалість у годинах
         */
        fun toHours(): Long {
            return toMinutes() / 60
        }
        
        /**
         * обчислює тривалість у днях
         *
         * @return тривалість у днях
         */
        fun toDays(): Long {
            return toHours() / 24
        }
        
        companion object {
            /**
             * інтервал в одну наносекунду
             */
            val ONE_NANO = TimePeriod(1, ChronoUnit.NANOS)
            
            /**
             * інтервал в одну мілісекунду
             */
            val ONE_MILLI = TimePeriod(1, ChronoUnit.MILLIS)
            
            /**
             * інтервал в одну секунду
             */
            val ONE_SECOND = TimePeriod(1, ChronoUnit.SECONDS)
            
            /**
             * інтервал в одну хвилину
             */
            val ONE_MINUTE = TimePeriod(1, ChronoUnit.MINUTES)
            
            /**
             * інтервал в одну годину
             */
            val ONE_HOUR = TimePeriod(1, ChronoUnit.HOURS)
            
            /**
             * інтервал в один день
             */
            val ONE_DAY = TimePeriod(1, ChronoUnit.DAYS)
        }
    }
    
    /**
     * створює інтервал часу
     *
     * @param duration тривалість
     * @param unit одиниця виміру
     * @return інтервал часу
     */
    fun createTimePeriod(duration: Long, unit: ChronoUnit): TimePeriod {
        return TimePeriod(duration, unit)
    }
    
    /**
     * створює інтервал часу в секундах
     *
     * @param seconds кількість секунд
     * @return інтервал часу
     */
    fun seconds(seconds: Long): TimePeriod {
        return TimePeriod(seconds, ChronoUnit.SECONDS)
    }
    
    /**
     * створює інтервал часу в хвилинах
     *
     * @param minutes кількість хвилин
     * @return інтервал часу
     */
    fun minutes(minutes: Long): TimePeriod {
        return TimePeriod(minutes, ChronoUnit.MINUTES)
    }
    
    /**
     * створює інтервал часу в годинах
     *
     * @param hours кількість годин
     * @return інтервал часу
     */
    fun hours(hours: Long): TimePeriod {
        return TimePeriod(hours, ChronoUnit.HOURS)
    }
    
    /**
     * створює інтервал часу в днях
     *
     * @param days кількість днів
     * @return інтервал часу
     */
    fun days(days: Long): TimePeriod {
        return TimePeriod(days, ChronoUnit.DAYS)
    }
    
    // функції для роботи з різними календарями
    
    /**
     * представлення юліанської дати
     *
     * @property julianDay номер юліанського дня
     */
    data class JulianDate(val julianDay: Double) {
        
        /**
         * конвертує юліанську дату в григоріанську
         *
         * @return григоріанська дата
         */
        fun toGregorian(): LocalDate {
            val a = ((julianDay + 0.5).toInt() + 32044).toDouble()
            val b = ((4 * a + 3) / 146097).toInt()
            val c = a - ((146097 * b) / 4).toDouble()
            val d = ((4 * c + 3) / 1461).toInt()
            val e = c - ((1461 * d) / 4).toDouble()
            val m = ((5 * e + 2) / 153).toInt()
            
            val day = (e - ((153 * m + 2) / 5).toDouble() + 1).toInt()
            val month = if (m < 10) m + 3 else m - 9
            val year = if (m < 10) b * 100 + d - 4800 else b * 100 + d - 4801
            
            return LocalDate.of(year, month, day)
        }
        
        /**
     