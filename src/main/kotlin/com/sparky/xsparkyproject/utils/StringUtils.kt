package com.sparky.xsparkyproject.utils

/**
 * набір утиліт для роботи з рядками
 * включає розширення функціональності стандартних строкових операцій
 *
 * @author Андрій Будильников
 */
class StringUtils {
    companion object {
        /**
         * перевіряє чи рядок є порожнім або містить лише пробіли
         * 
         * @param str рядок для перевірки
         * @return true якщо рядок порожній або містить лише пробіли
         */
        fun isBlank(str: String?): Boolean {
            return str == null || str.trim().isEmpty()
        }
        
        /**
         * перевіряє чи рядок не є порожнім і не містить лише пробіли
         * 
         * @param str рядок для перевірки
         * @return true якщо рядок не порожній і не містить лише пробіли
         */
        fun isNotBlank(str: String?): Boolean {
            return !isBlank(str)
        }
        
        /**
         * обрізає пробіли з початку і кінця рядка
         * 
         * @param str рядок для обрізання
         * @return рядок без пробілів на початку і в кінці
         */
        fun trim(str: String?): String {
            return str?.trim() ?: ""
        }
        
        /**
         * обрізає пробіли зліва від рядка
         * 
         * @param str рядок для обрізання
         * @return рядок без пробілів зліва
         */
        fun trimStart(str: String?): String {
            return str?.trimStart() ?: ""
        }
        
        /**
         * обрізає пробіли справа від рядка
         * 
         * @param str рядок для обрізання
         * @return рядок без пробілів справа
         */
        fun trimEnd(str: String?): String {
            return str?.trimEnd() ?: ""
        }
        
        /**
         * перетворює рядок в верхній регістр
         * 
         * @param str рядок для перетворення
         * @return рядок у верхньому регістрі
         */
        fun toUpperCase(str: String?): String {
            return str?.uppercase() ?: ""
        }
        
        /**
         * перетворює рядок в нижній регістр
         * 
         * @param str рядок для перетворення
         * @return рядок у нижньому регістрі
         */
        fun toLowerCase(str: String?): String {
            return str?.lowercase() ?: ""
        }
        
        /**
         * капіталізує перший символ рядка
         * 
         * @param str рядок для капіталізації
         * @return рядок з капіталізованим першим символом
         */
        fun capitalize(str: String?): String {
            if (str.isNullOrEmpty()) return ""
            return str.substring(0, 1).uppercase() + str.substring(1).lowercase()
        }
        
        /**
         * робить перший символ рядка малим
         * 
         * @param str рядок для перетворення
         * @return рядок з малим першим символом
         */
        fun uncapitalize(str: String?): String {
            if (str.isNullOrEmpty()) return ""
            return str.substring(0, 1).lowercase() + str.substring(1)
        }
        
        /**
         * повторює рядок n разів
         * 
         * @param str рядок для повторення
         * @param times кількість повторень
         * @return повторений рядок
         */
        fun repeat(str: String?, times: Int): String {
            if (str == null || times <= 0) return ""
            return str.repeat(times)
        }
        
        /**
         * з'єднує масив рядків роздільником
         * 
         * @param separator роздільник
         * @param strings масив рядків
         * @return з'єднаний рядок
         */
        fun join(separator: String, vararg strings: String?): String {
            return strings.filterNotNull().joinToString(separator)
        }
        
        /**
         * розділяє рядок на частини за роздільником
         * 
         * @param str рядок для розділення
         * @param separator роздільник
         * @return масив частин рядка
         */
        fun split(str: String?, separator: String): List<String> {
            if (str == null) return emptyList()
            return str.split(separator)
        }
        
        /**
         * замінює всі входження підрядка в рядку
         * 
         * @param str оригінальний рядок
         * @param target підрядок для заміни
         * @param replacement рядок заміни
         * @return рядок з заміненими підрядками
         */
        fun replace(str: String?, target: String, replacement: String): String {
            if (str == null) return ""
            return str.replace(target, replacement)
        }
        
        /**
         * замінює перше входження підрядка в рядку
         * 
         * @param str оригінальний рядок
         * @param target підрядок для заміни
         * @param replacement рядок заміни
         * @return рядок з заміненим першим входженням
         */
        fun replaceFirst(str: String?, target: String, replacement: String): String {
            if (str == null) return ""
            return str.replaceFirst(target, replacement)
        }
        
        /**
         * перевіряє чи рядок починається з певного префікса
         * 
         * @param str рядок для перевірки
         * @param prefix префікс
         * @return true якщо рядок починається з префікса
         */
        fun startsWith(str: String?, prefix: String): Boolean {
            if (str == null) return false
            return str.startsWith(prefix)
        }
        
        /**
         * перевіряє чи рядок закінчується певним суфіксом
         * 
         * @param str рядок для перевірки
         * @param suffix суфікс
         * @return true якщо рядок закінчується суфіксом
         */
        fun endsWith(str: String?, suffix: String): Boolean {
            if (str == null) return false
            return str.endsWith(suffix)
        }
        
        /**
         * перевіряє чи рядок містить певний підрядок
         * 
         * @param str рядок для перевірки
         * @param substring підрядок
         * @return true якщо рядок містить підрядок
         */
        fun contains(str: String?, substring: String): Boolean {
            if (str == null) return false
            return str.contains(substring)
        }
        
        /**
         * отримує підрядок з початку рядка
         * 
         * @param str оригінальний рядок
         * @param length довжина підрядка
         * @return підрядок з початку
         */
        fun substringStart(str: String?, length: Int): String {
            if (str == null) return ""
            if (length <= 0) return ""
            if (length >= str.length) return str
            return str.substring(0, length)
        }
        
        /**
         * отримує підрядок з кінця рядка
         * 
         * @param str оригінальний рядок
         * @param length довжина підрядка
         * @return підрядок з кінця
         */
        fun substringEnd(str: String?, length: Int): String {
            if (str == null) return ""
            if (length <= 0) return ""
            if (length >= str.length) return str
            return str.substring(str.length - length)
        }
        
        /**
         * отримує підрядок між двома індексами
         * 
         * @param str оригінальний рядок
         * @param startIndex початковий індекс
         * @param endIndex кінцевий індекс
         * @return підрядок між індексами
         */
        fun substring(str: String?, startIndex: Int, endIndex: Int): String {
            if (str == null) return ""
            if (startIndex < 0 || endIndex > str.length || startIndex > endIndex) return ""
            return str.substring(startIndex, endIndex)
        }
        
        /**
         * отримує довжину рядка
         * 
         * @param str рядок
         * @return довжина рядка
         */
        fun length(str: String?): Int {
            return str?.length ?: 0
        }
        
        /**
         * перевертає рядок
         * 
         * @param str рядок для перевертання
         * @return перевернутий рядок
         */
        fun reverse(str: String?): String {
            if (str == null) return ""
            return str.reversed()
        }
        
        /**
         * видаляє всі пробіли з рядка
         * 
         * @param str рядок
         * @return рядок без пробілів
         */
        fun removeSpaces(str: String?): String {
            if (str == null) return ""
            return str.replace(" ", "")
        }
        
        /**
         * видаляє всі цифри з рядка
         * 
         * @param str рядок
         * @return рядок без цифр
         */
        fun removeDigits(str: String?): String {
            if (str == null) return ""
            return str.replace(Regex("\\d"), "")
        }
        
        /**
         * видаляє всі літери з рядка
         * 
         * @param str рядок
         * @return рядок без літер
         */
        fun removeLetters(str: String?): String {
            if (str == null) return ""
            return str.replace(Regex("[a-zA-Zа-яА-ЯїЇіІєЄґҐ]"), "")
        }
        
        /**
         * перевіряє чи рядок є числом
         * 
         * @param str рядок для перевірки
         * @return true якщо рядок є числом
         */
        fun isNumeric(str: String?): Boolean {
            if (str == null) return false
            return str.matches(Regex("-?\\d+(\\.\\d+)?"))
        }
        
        /**
         * перевіряє чи рядок є цілим числом
         * 
         * @param str рядок для перевірки
         * @return true якщо рядок є цілим числом
         */
        fun isInteger(str: String?): Boolean {
            if (str == null) return false
            return str.matches(Regex("-?\\d+"))
        }
        
        /**
         * перевіряє чи рядок є дійсним числом
         * 
         * @param str рядок для перевірки
         * @return true якщо рядок є дійсним числом
         */
        fun isDecimal(str: String?): Boolean {
            if (str == null) return false
            return str.matches(Regex("-?\\d+\\.\\d+"))
        }
        
        /**
         * перевіряє чи рядок є email адресою
         * 
         * @param str рядок для перевірки
         * @return true якщо рядок є email адресою
         */
        fun isEmail(str: String?): Boolean {
            if (str == null) return false
            return str.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"))
        }
        
        /**
         * перевіряє чи рядок є URL адресою
         * 
         * @param str рядок для перевірки
         * @return true якщо рядок є URL адресою
         */
        fun isUrl(str: String?): Boolean {
            if (str == null) return false
            return str.matches(Regex("^(http|https)://[^ \"\$]+\$"))
        }
        
        /**
         * перевіряє чи рядок є телефонним номером
         * 
         * @param str рядок для перевірки
         * @return true якщо рядок є телефонним номером
         */
        fun isPhoneNumber(str: String?): Boolean {
            if (str == null) return false
            return str.matches(Regex("^\\+?\\d{10,15}\$"))
        }
        
        /**
         * перевіряє чи рядок є датою
         * 
         * @param str рядок для перевірки
         * @return true якщо рядок є датою
         */
        fun isDate(str: String?): Boolean {
            if (str == null) return false
            return str.matches(Regex("^\\d{4}-\\d{2}-\\d{2}\$")) || 
                   str.matches(Regex("^\\d{2}/\\d{2}/\\d{4}\$"))
        }
        
        /**
         * форматує рядок як ім'я (капіталізує перші літери)
         * 
         * @param str рядок для форматування
         * @return форматоване ім'я
         */
        fun formatName(str: String?): String {
            if (str == null) return ""
            return str.split(" ").joinToString(" ") { word ->
                if (word.isNotEmpty()) {
                    word.substring(0, 1).uppercase() + word.substring(1).lowercase()
                } else {
                    word
                }
            }
        }
        
        /**
         * форматує рядок як заголовок (капіталізує перші літери слів)
         * 
         * @param str рядок для форматування
         * @return форматований заголовок
         */
        fun formatTitle(str: String?): String {
            if (str == null) return ""
            return str.split(" ").joinToString(" ") { word ->
                if (word.isNotEmpty()) {
                    word.substring(0, 1).uppercase() + word.substring(1).lowercase()
                } else {
                    word
                }
            }
        }
        
        /**
         * форматує рядок як речення (капіталізує першу літеру)
         * 
         * @param str рядок для форматування
         * @return форматоване речення
         */
        fun formatSentence(str: String?): String {
            if (str == null) return ""
            if (str.isEmpty()) return str
            return str.substring(0, 1).uppercase() + str.substring(1).lowercase()
        }
        
        /**
         * обрізає рядок до певної довжини
         * 
         * @param str рядок для обрізання
         * @param maxLength максимальна довжина
         * @param suffix суфікс для додавання при обрізанні
         * @return обрізаний рядок
         */
        fun truncate(str: String?, maxLength: Int, suffix: String = "..."): String {
            if (str == null) return ""
            if (str.length <= maxLength) return str
            return str.substring(0, maxLength - suffix.length) + suffix
        }
        
        /**
         * вирівнює рядок по лівому краю
         * 
         * @param str рядок для вирівнювання
         * @param width ширина
         * @param padding символ заповнення
         * @return вирівняний рядок
         */
        fun padLeft(str: String?, width: Int, padding: Char = ' '): String {
            if (str == null) return "".padStart(width, padding)
            return str.padStart(width, padding)
        }
        
        /**
         * вирівнює рядок по правому краю
         * 
         * @param str рядок для вирівнювання
         * @param width ширина
         * @param padding символ заповнення
         * @return вирівняний рядок
         */
        fun padRight(str: String?, width: Int, padding: Char = ' '): String {
            if (str == null) return "".padEnd(width, padding)
            return str.padEnd(width, padding)
        }
        
        /**
         * вирівнює рядок по центру
         * 
         * @param str рядок для вирівнювання
         * @param width ширина
         * @param padding символ заповнення
         * @return вирівняний рядок
         */
        fun padCenter(str: String?, width: Int, padding: Char = ' '): String {
            if (str == null) return "".padEnd(width, padding)
            val paddingNeeded = width - str.length
            if (paddingNeeded <= 0) return str
            val leftPadding = paddingNeeded / 2
            val rightPadding = paddingNeeded - leftPadding
            return padding.toString().repeat(leftPadding) + str + padding.toString().repeat(rightPadding)
        }
        
        /**
         * видаляє дублікати символів з рядка
         * 
         * @param str рядок
         * @return рядок без дублікатів символів
         */
        fun removeDuplicateChars(str: String?): String {
            if (str == null) return ""
            val seen = mutableSetOf<Char>()
            val result = StringBuilder()
            for (char in str) {
                if (!seen.contains(char)) {
                    seen.add(char)
                    result.append(char)
                }
            }
            return result.toString()
        }
        
        /**
         * перемішує символи в рядку
         * 
         * @param str рядок для перемішування
         * @return перемішаний рядок
         */
        fun shuffleString(str: String?): String {
            if (str == null) return ""
            val chars = str.toCharArray()
            // тут має бути реалізація перемішування
            return String(chars)
        }
        
        /**
         * обертає слова в рядку
         * 
         * @param str рядок
         * @return рядок з оберненими словами
         */
        fun reverseWords(str: String?): String {
            if (str == null) return ""
            return str.split(" ").reversed().joinToString(" ")
        }
        
        /**
         * сортує символи в рядку
         * 
         * @param str рядок для сортування
         * @return рядок з відсортованими символами
         */
        fun sortChars(str: String?): String {
            if (str == null) return ""
            return str.toCharArray().sorted().joinToString("")
        }
        
        /**
         * обчислює хеш рядка
         * 
         * @param str рядок
         * @return хеш рядка
         */
        fun hash(str: String?): Int {
            return str?.hashCode() ?: 0
        }
        
        /**
         * обчислює кількість слів у рядку
         * 
         * @param str рядок
         * @return кількість слів
         */
        fun wordCount(str: String?): Int {
            if (str == null) return 0
            return str.split(Regex("\\s+")).filter { it.isNotEmpty() }.size
        }
        
        /**
         * обчислює кількість речень у рядку
         * 
         * @param str рядок
         * @return кількість речень
         */
        fun sentenceCount(str: String?): Int {
            if (str == null) return 0
            return str.split(Regex("[.!?]+")).filter { it.isNotBlank() }.size
        }
        
        /**
         * обчислює кількість абзаців у рядку
         * 
         * @param str рядок
         * @return кількість абзаців
         */
        fun paragraphCount(str: String?): Int {
            if (str == null) return 0
            return str.split(Regex("\n\n+")).filter { it.isNotBlank() }.size
        }
        
        /**
         * обчислює частоту символів у рядку
         * 
         * @param str рядок
         * @return мапа частот символів
         */
        fun charFrequency(str: String?): Map<Char, Int> {
            if (str == null) return emptyMap()
            val frequency = mutableMapOf<Char, Int>()
            for (char in str) {
                frequency[char] = frequency.getOrDefault(char, 0) + 1
            }
            return frequency
        }
        
        /**
         * обчислює частоту слів у рядку
         * 
         * @param str рядок
         * @return мапа частот слів
         */
        fun wordFrequency(str: String?): Map<String, Int> {
            if (str == null) return emptyMap()
            val frequency = mutableMapOf<String, Int>()
            val words = str.split(Regex("\\s+")).filter { it.isNotEmpty() }
            for (word in words) {
                val cleanWord = word.lowercase().replace(Regex("[^a-zA-Zа-яА-ЯїЇіІєЄґҐ0-9]"), "")
                if (cleanWord.isNotEmpty()) {
                    frequency[cleanWord] = frequency.getOrDefault(cleanWord, 0) + 1
                }
            }
            return frequency
        }
        
        /**
         * знаходить найдовше слово у рядку
         * 
         * @param str рядок
         * @return найдовше слово
         */
        fun longestWord(str: String?): String {
            if (str == null) return ""
            val words = str.split(Regex("\\s+")).filter { it.isNotEmpty() }
            return words.maxByOrNull { it.length } ?: ""
        }
        
        /**
         * знаходить найкоротше слово у рядку
         * 
         * @param str рядок
         * @return найкоротше слово
         */
        fun shortestWord(str: String?): String {
            if (str == null) return ""
            val words = str.split(Regex("\\s+")).filter { it.isNotEmpty() }
            return words.minByOrNull { it.length } ?: ""
        }
        
        /**
         * обчислює середню довжину слова у рядку
         * 
         * @param str рядок
         * @return середня довжина слова
         */
        fun averageWordLength(str: String?): Double {
            if (str == null) return 0.0
            val words = str.split(Regex("\\s+")).filter { it.isNotEmpty() }
            if (words.isEmpty()) return 0.0
            val totalLength = words.sumOf { it.length }
            return totalLength.toDouble() / words.size
        }
        
        /**
         * обчислює кількість голосних у рядку
         * 
         * @param str рядок
         * @return кількість голосних
         */
        fun vowelCount(str: String?): Int {
            if (str == null) return 0
            val vowels = setOf('a', 'e', 'i', 'o', 'u', 'A', 'E', 'I', 'O', 'U', 
                              'а', 'е', 'и', 'і', 'о', 'у', 'я', 'ю', 'є', 'ї')
            return str.count { it in vowels }
        }
        
        /**
         * обчислює кількість приголосних у рядку
         * 
         * @param str рядок
         * @return кількість приголосних
         */
        fun consonantCount(str: String?): Int {
            if (str == null) return 0
            val consonants = setOf('b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 
                                  'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z',
                                  'B', 'C', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 
                                  'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z',
                                  'б', 'в', 'г', 'ґ', 'д', 'ж', 'з', 'й', 'к', 'л', 'м', 
                                  'н', 'п', 'р', 'с', 'т', 'ф', 'х', 'ц', 'ч', 'ш', 'щ',
                                  'Б', 'В', 'Г', 'Ґ', 'Д', 'Ж', 'З', 'Й', 'К', 'Л', 'М', 
                                  'Н', 'П', 'Р', 'С', 'Т', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ')
            return str.count { it in consonants }
        }
        
        /**
         * обчислює кількість цифр у рядку
         * 
         * @param str рядок
         * @return кількість цифр
         */
        fun digitCount(str: String?): Int {
            if (str == null) return 0
            return str.count { it.isDigit() }
        }
        
        /**
         * обчислює кількість пробілів у рядку
         * 
         * @param str рядок
         * @return кількість пробілів
         */
        fun spaceCount(str: String?): Int {
            if (str == null) return 0
            return str.count { it == ' ' }
        }
        
        /**
         * обчислює кількість рядків у тексті
         * 
         * @param str текст
         * @return кількість рядків
         */
        fun lineCount(str: String?): Int {
            if (str == null) return 0
            return str.split('\n').size
        }
        
        /**
         * обчислює кількість символів у тексті
         * 
         * @param str текст
         * @return кількість символів
         */
        fun charCount(str: String?): Int {
            return str?.length ?: 0
        }
        
        /**
         * обчислює кількість унікальних символів у тексті
         * 
         * @param str текст
         * @return кількість унікальних символів
         */
        fun uniqueCharCount(str: String?): Int {
            if (str == null) return 0
            return str.toSet().size
        }
        
        /**
         * обчислює кількість унікальних слів у тексті
         * 
         * @param str текст
         * @return кількість унікальних слів
         */
        fun uniqueWordCount(str: String?): Int {
            if (str == null) return 0
            val words = str.split(Regex("\\s+")).filter { it.isNotEmpty() }
                .map { it.lowercase().replace(Regex("[^a-zA-Zа-яА-ЯїЇіІєЄґҐ0-9]"), "") }
                .filter { it.isNotEmpty() }
            return words.toSet().size
        }
        
        /**
         * обчислює коефіцієнт унікальності слів у тексті
         * 
         * @param str текст
         * @return коефіцієнт унікальності (від 0 до 1)
         */
        fun wordUniquenessRatio(str: String?): Double {
            if (str == null) return 0.0
            val totalWords = wordCount(str)
            if (totalWords == 0) return 0.0
            val uniqueWords = uniqueWordCount(str)
            return uniqueWords.toDouble() / totalWords
        }
        
        /**
         * обчислює коефіцієнт унікальності символів у тексті
         * 
         * @param str текст
         * @return коефіцієнт унікальності (від 0 до 1)
         */
        fun charUniquenessRatio(str: String?): Double {
            if (str == null) return 0.0
            val totalChars = charCount(str)
            if (totalChars == 0) return 0.0
            val uniqueChars = uniqueCharCount(str)
            return uniqueChars.toDouble() / totalChars
        }
        
        /**
         * обчислює індекс різноманітності символів у тексті
         * 
         * @param str текст
         * @return індекс різноманітності (від 0 до 1)
         */
        fun diversityIndex(str: String?): Double {
            if (str == null) return 0.0
            if (str.isEmpty()) return 0.0
            val frequencies = charFrequency(str)
            val totalChars = str.length
            var diversity = 0.0
            for ((_, frequency) in frequencies) {
                val probability = frequency.toDouble() / totalChars
                diversity -= probability * kotlin.math.ln(probability)
            }
            return diversity / kotlin.math.ln(frequencies.size.toDouble())
        }
        
        /**
         * обчислює ентропію тексту
         * 
         * @param str текст
         * @return ентропія тексту
         */
        fun entropy(str: String?): Double {
            if (str == null) return 0.0
            if (str.isEmpty()) return 0.0
            val frequencies = charFrequency(str)
            val totalChars = str.length
            var entropy = 0.0
            for ((_, frequency) in frequencies) {
                val probability = frequency.toDouble() / totalChars
                if (probability > 0) {
                    entropy -= probability * kotlin.math.log(probability, 2.0)
                }
            }
            return entropy
        }
        
        /**
         * обчислює коефіцієнт повторюваності символів
         * 
         * @param str текст
         * @return коефіцієнт повторюваності (від 0 до 1)
         */
        fun repetitionRatio(str: String?): Double {
            if (str == null) return 0.0
            if (str.isEmpty()) return 0.0
            val uniqueChars = uniqueCharCount(str)
            val totalChars = charCount(str)
            return 1.0 - (uniqueChars.toDouble() / totalChars)
        }
        
        /**
         * обчислює коефіцієнт повторюваності слів
         * 
         * @param str текст
         * @return коефіцієнт повторюваності (від 0 до 1)
         */
        fun wordRepetitionRatio(str: String?): Double {
            if (str == null) return 0.0
            if (str.isEmpty()) return 0.0
            val uniqueWords = uniqueWordCount(str)
            val totalWords = wordCount(str)
            return 1.0 - (uniqueWords.toDouble() / totalWords)
        }
        
        /**
         * обчислює коефіцієнт читабельності тексту
         * 
         * @param str текст
         * @return коефіцієнт читабельності (від 0 до 1)
         */
        fun readabilityScore(str: String?): Double {
            if (str == null) return 0.0
            val words = wordCount(str)
            val sentences = sentenceCount(str)
            val syllables = vowelCount(str) // приблизна кількість складів
            if (words == 0 || sentences == 0) return 0.0
            
            // формула Флеша-Кінкайда (спрощена)
            val avgWordsPerSentence = words.toDouble() / sentences
            val avgSyllablesPerWord = syllables.toDouble() / words
            
            val score = 206.835 - (1.015 * avgWordsPerSentence) - (84.6 * avgSyllablesPerWord)
            return (score / 206.835).coerceIn(0.0, 1.0)
        }
        
        /**
         * обчислює коефіцієнт складності тексту
         * 
         * @param str текст
         * @return коефіцієнт складності (від 0 до 1)
         */
        fun complexityScore(str: String?): Double {
            if (str == null) return 0.0
            val words = wordCount(str)
            if (words == 0) return 0.0
            
            // обчислюємо середню довжину слова
            val avgWordLength = averageWordLength(str)
            
            // обчислюємо кількість унікальних символів
            val uniqueChars = uniqueCharCount(str)
            val totalChars = charCount(str)
            val charDiversity = if (totalChars > 0) uniqueChars.toDouble() / totalChars else 0.0
            
            // комбінуємо метрики
            val lengthScore = (avgWordLength / 20.0).coerceIn(0.0, 1.0) // припускаємо максимум 20 символів на слово
            val diversityScore = charDiversity
            
            return (lengthScore + diversityScore) / 2.0
        }
        
        /**
         * обчислює коефіцієнт структурованості тексту
         * 
         * @param str текст
         * @return коефіцієнт структурованості (від 0 до 1)
         */
        fun structureScore(str: String?): Double {
            if (str == null) return 0.0
            val sentences = sentenceCount(str)
            val paragraphs = paragraphCount(str)
            val words = wordCount(str)
            
            if (words == 0) return 0.0
            
            // обчислюємо середню кількість речень на абзац
            val avgSentencesPerParagraph = if (paragraphs > 0) sentences.toDouble() / paragraphs else 0.0
            
            // обчислюємо середню кількість слів на речення
            val avgWordsPerSentence = if (sentences > 0) words.toDouble() / sentences else 0.0
            
            // ідеальні значення: 3-5 речень на абзац, 15-25 слів на речення
            val paragraphScore = 1.0 - kotlin.math.abs(avgSentencesPerParagraph - 4.0) / 4.0
            val sentenceScore = 1.0 - kotlin.math.abs(avgWordsPerSentence - 20.0) / 20.0
            
            return ((paragraphScore.coerceIn(0.0, 1.0) + sentenceScore.coerceIn(0.0, 1.0)) / 2.0)
        }
        
        /**
         * обчислює загальний коефіцієнт якості тексту
         * 
         * @param str текст
         * @return загальний коефіцієнт якості (від 0 до 1)
         */
        fun qualityScore(str: String?): Double {
            if (str == null) return 0.0
            
            val readability = readabilityScore(str)
            val complexity = complexityScore(str)
            val structure = structureScore(str)
            
            return (readability + complexity + structure) / 3.0
        }
        
        /**
         * генерує випадковий рядок заданої довжини
         * 
         * @param length довжина рядка
         * @param charset набір символів (за замовчуванням - латинські літери та цифри)
         * @return випадковий рядок
         */
        fun randomString(length: Int, charset: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"): String {
            if (length <= 0) return ""
            val random = java.util.Random()
            val result = StringBuilder(length)
            for (i in 0 until length) {
                result.append(charset[random.nextInt(charset.length)])
            }
            return result.toString()
        }
        
        /**
         * генерує випадковий рядок з літер
         * 
         * @param length довжина рядка
         * @return випадковий рядок з літер
         */
        fun randomLetters(length: Int): String {
            return randomString(length, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz")
        }
        
        /**
         * генерує випадковий рядок з цифр
         * 
         * @param length довжина рядка
         * @return випадковий рядок з цифр
         */
        fun randomDigits(length: Int): String {
            return randomString(length, "0123456789")
        }
        
        /**
         * генерує випадковий рядок з символів
         * 
         * @param length довжина рядка
         * @return випадковий рядок з символів
         */
        fun randomSymbols(length: Int): String {
            return randomString(length, "!@#$%^&*()_+-=[]{}|;:,.<>?")
        }
        
        /**
         * генерує випадковий рядок зі спеціальних символів
         * 
         * @param length довжина рядка
         * @return випадковий рядок зі спеціальних символів
         */
        fun randomSpecialChars(length: Int): String {
            return randomString(length, "!@#$%^&*()_+-=[]{}|;:,.<>?`~")
        }
        
        /**
         * генерує випадковий рядок з українських літер
         * 
         * @param length довжина рядка
         * @return випадковий рядок з українських літер
         */
        fun randomUkrainianLetters(length: Int): String {
            return randomString(length, "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯабвгґдеєжзиіїйклмнопрстуфхцчшщьюя")
        }
        
        /**
         * генерує випадковий рядок з російських літер
         * 
         * @param length довжина рядка
         * @return випадковий рядок з російських літер
         */
        fun randomRussianLetters(length: Int): String {
            return randomString(length, "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя")
        }
        
        /**
         * генерує випадковий email
         * 
         * @return випадковий email
         */
        fun randomEmail(): String {
            val domains = listOf("gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "example.com")
            val random = java.util.Random()
            val username = randomLetters(8).lowercase()
            val domain = domains[random.nextInt(domains.size)]
            return "$username@$domain"
        }
        
        /**
         * генерує випадковий телефонний номер
         * 
         * @return випадковий телефонний номер
         */
        fun randomPhoneNumber(): String {
            val random = java.util.Random()
            val countryCode = "+1"
            val areaCode = randomDigits(3)
            val exchange = randomDigits(3)
            val number = randomDigits(4)
            return "$countryCode$areaCode$exchange$number"
        }
        
        /**
         * генерує випадковий IP адрес
         * 
         * @return випадковий IP адрес
         */
        fun randomIpAddress(): String {
            val random = java.util.Random()
            val octets = List(4) { random.nextInt(256) }
            return octets.joinToString(".")
        }
        
        /**
         * генерує випадковий MAC адрес
         * 
         * @return випадковий MAC адрес
         */
        fun randomMacAddress(): String {
            val random = java.util.Random()
            val bytes = ByteArray(6)
            random.nextBytes(bytes)
            return bytes.joinToString(":") { "%02X".format(it) }
        }
        
        /**
         * генерує випадковий UUID
         * 
         * @return випадковий UUID
         */
        fun randomUuid(): String {
            return java.util.UUID.randomUUID().toString()
        }
        
        /**
         * генерує випадкову дату
         * 
         * @return випадкова дата у форматі YYYY-MM-DD
         */
        fun randomDate(): String {
            val random = java.util.Random()
            val year = 2000 + random.nextInt(25) // 2000-2024
            val month = 1 + random.nextInt(12) // 1-12
            val day = 1 + random.nextInt(28) // 1-28 (щоб уникнути проблем з різною кількістю днів у місяцях)
            return String.format("%04d-%02d-%02d", year, month, day)
        }
        
        /**
         * генерує випадковий час
         * 
         * @return випадковий час у форматі HH:MM:SS
         */
        fun randomTime(): String {
            val random = java.util.Random()
            val hour = random.nextInt(24) // 0-23
            val minute = random.nextInt(60) // 0-59
            val second = random.nextInt(60) // 0-59
            return String.format("%02d:%02d:%02d", hour, minute, second)
        }
        
        /**
         * генерує випадковий URL
         * 
         * @return випадковий URL
         */
        fun randomUrl(): String {
            val protocols = listOf("http", "https")
            val domains = listOf("example.com", "test.com", "demo.com", "sample.com")
            val paths = listOf("home", "about", "contact", "products", "services")
            
            val random = java.util.Random()
            val protocol = protocols[random.nextInt(protocols.size)]
            val domain = domains[random.nextInt(domains.size)]
            val path = paths[random.nextInt(paths.size)]
            
            return "$protocol://$domain/$path"
        }
        
        /**
         * генерує випадковий пароль
         * 
         * @param length довжина пароля
         * @param includeUppercase включати великі літери
         * @param includeLowercase включати малі літери
         * @param includeDigits включати цифри
         * @param includeSymbols включати символи
         * @return випадковий пароль
         */
        fun randomPassword(
            length: Int = 12,
            includeUppercase: Boolean = true,
            includeLowercase: Boolean = true,
            includeDigits: Boolean = true,
            includeSymbols: Boolean = true
        ): String {
            if (length <= 0) return ""
            
            val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            val lowercase = "abcdefghijklmnopqrstuvwxyz"
            val digits = "0123456789"
            val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"
            
            val charset = buildString {
                if (includeUppercase) append(uppercase)
                if (includeLowercase) append(lowercase)
                if (includeDigits) append(digits)
                if (includeSymbols) append(symbols)
            }
            
            if (charset.isEmpty()) return ""
            
            return randomString(length, charset)
        }
        
        /**
         * генерує випадковий PIN код
         * 
         * @param length довжина PIN коду
         * @return випадковий PIN код
         */
        fun randomPin(length: Int = 4): String {
            return randomDigits(length)
        }
        
        /**
         * генерує випадковий код активації
         * 
         * @param length довжина коду
         * @return випадковий код активації
         */
        fun randomActivationCode(length: Int = 16): String {
            return randomString(length, "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
        }
        
        /**
         * генерує випадковий токен
         * 
         * @param length довжина токена
         * @return випадковий токен
         */
        fun randomToken(length: Int = 32): String {
            return randomString(length, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789")
        }
        
        /**
         * генерує випадковий ключ API
         * 
         * @param length довжина ключа
         * @return випадковий ключ API
         */
        fun randomApiKey(length: Int = 32): String {
            return randomString(length, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789")
        }
        
        /**
         * генерує випадковий серійний номер
         * 
         * @param length довжина серійного номера
         * @return випадковий серійний номер
         */
        fun randomSerialNumber(length: Int = 20): String {
            return randomString(length, "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
        }
        
        /**
         * генерує випадковий ідентифікатор
         * 
         * @param length довжина ідентифікатора
         * @return випадковий ідентифікатор
         */
        fun randomId(length: Int = 8): String {
            return randomString(length, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789")
        }
        
        /**
         * генерує випадковий префікс
         * 
         * @param length довжина префікса
         * @return випадковий префікс
         */
        fun randomPrefix(length: Int = 3): String {
            return randomLetters(length).uppercase()
        }
        
        /**
         * генерує випадковий суфікс
         * 
         * @param length довжина суфікса
         * @return випадковий суфікс
         */
        fun randomSuffix(length: Int = 3): String {
            return randomLetters(length).lowercase()
        }
        
        /**
         * генерує випадковий тег
         * 
         * @param length довжина тега
         * @return випадковий тег
         */
        fun randomTag(length: Int = 10): String {
            return randomLetters(length).lowercase()
        }
        
        /**
         * генерує випадковий слаг
         * 
         * @param length довжина слага
         * @return випадковий слаг
         */
        fun randomSlug(length: Int = 10): String {
            return randomString(length, "abcdefghijklmnopqrstuvwxyz0123456789")
        }
        
        /**
         * генерує випадковий хеш
         * 
         * @param length довжина хеша
         * @return випадковий хеш
         */
        fun randomHash(length: Int = 64): String {
            return randomString(length, "abcdef0123456789")
        }
        
        /**
         * генерує випадковий колір у форматі HEX
         * 
         * @return випадковий колір у форматі HEX
         */
        fun randomColor(): String {
            return "#" + randomString(6, "0123456789ABCDEF")
        }
        
        /**
         * генерує випадковий код кольору RGB
         * 
         * @return випадковий код кольору RGB
         */
        fun randomRgbColor(): String {
            val random = java.util.Random()
            val r = random.nextInt(256)
            val g = random.nextInt(256)
            val b = random.nextInt(256)
            return "rgb($r, $g, $b)"
        }
        
        /**
         * генерує випадковий код кольору RGBA
         * 
         * @return випадковий код кольору RGBA
         */
        fun randomRgbaColor(): String {
            val random = java.util.Random()
            val r = random.nextInt(256)
            val g = random.nextInt(256)
            val b = random.nextInt(256)
            val a = random.nextDouble().coerceIn(0.0, 1.0)
            return "rgba($r, $g, $b, $a)"
        }
        
        /**
         * генерує випадковий код кольору HSL
         * 
         * @return випадковий код кольору HSL
         */
        fun randomHslColor(): String {
            val random = java.util.Random()
            val h = random.nextInt(361) // 0-360
            val s = random.nextInt(101) // 0-100
            val l = random.nextInt(101) // 0-100
            return "hsl($h, $s%, $l%)"
        }
        
        /**
         * генерує випадковий код кольору HSLA
         * 
         * @return випадковий код кольору HSLA
         */
        fun randomHslaColor(): String {
            val random = java.util.Random()
            val h = random.nextInt(361) // 0-360
            val s = random.nextInt(101) // 0-100
            val l = random.nextInt(101) // 0-100
            val a = random.nextDouble().coerceIn(0.0, 1.0)
            return "hsla($h, $s%, $l%, $a)"
        }
        
        /**
         * генерує випадковий код кольору CMYK
         * 
         * @return випадковий код кольору CMYK
         */
        fun randomCmykColor(): String {
            val random = java.util.Random()
            val c = random.nextInt(101) // 0-100
            val m = random.nextInt(101) // 0-100
            val y = random.nextInt(101) // 0-100
            val k = random.nextInt(101) // 0-100
            return "cmyk($c%, $m%, $y%, $k%)"
        }
        
        /**
         * генерує випадковий код кольору HSV
         * 
         * @return випадковий код кольору HSV
         */
        fun randomHsvColor(): String {
            val random = java.util.Random()
            val h = random.nextInt(361) // 0-360
            val s = random.nextInt(101) // 0-100
            val v = random.nextInt(101) // 0-100
            return "hsv($h, $s%, $v%)"
        }
        
        /**
         * генерує випадковий код кольору HWB
         * 
         * @return випадковий код кольору HWB
         */
        fun randomHwbColor(): String {
            val random = java.util.Random()
            val h = random.nextInt(361) // 0-360
            val w = random.nextInt(101) // 0-100
            val b = random.nextInt(101) // 0-100
            return "hwb($h, $w%, $b%)"
        }
        
        /**
         * генерує випадковий код кольору LAB
         * 
         * @return випадковий код кольору LAB
         */
        fun randomLabColor(): String {
            val random = java.util.Random()
            val l = random.nextInt(101) // 0-100
            val a = random.nextInt(201) - 100 // -100 to 100
            val b = random.nextInt(201) - 100 // -100 to 100
            return "lab($l% $a $b)"
        }
        
        /**
         * генерує випадковий код кольору LCH
         * 
         * @return випадковий код кольору LCH
         */
        fun randomLchColor(): String {
            val random = java.util.Random()
            val l = random.nextInt(101) // 0-100
            val c = random.nextInt(151) // 0-150
            val h = random.nextInt(361) // 0-360
            return "lch($l% $c $h)"
        }
        
        /**
         * генерує випадковий код кольору OKLAB
         * 
         * @return випадковий код кольору OKLAB
         */
        fun randomOklabColor(): String {
            val random = java.util.Random()
            val l = random.nextDouble().coerceIn(0.0, 1.0)
            val a = (random.nextDouble() - 0.5) * 0.4 // -0.2 to 0.2
            val b = (random.nextDouble() - 0.5) * 0.4 // -0.2 to 0.2
            return "oklab($l $a $b)"
        }
        
        /**
         * генерує випадковий код кольору OKLCH
         * 
         * @return випадковий код кольору OKLCH
         */
        fun randomOklchColor(): String {
            val random = java.util.Random()
            val l = random.nextDouble().coerceIn(0.0, 1.0)
            val c = random.nextDouble() * 0.4 // 0 to 0.4
            val h = random.nextInt(361) // 0-360
            return "oklch($l $c $h)"
        }
    }
}