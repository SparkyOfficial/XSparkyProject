/**
 * xsparkyproject - розширена бібліотека для kotlin
 *
 * Copyright 2025 Андрій Будильников
 */

package com.sparky.xsparkyproject.utils

import java.io.*
import java.nio.charset.Charset
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.security.MessageDigest
import java.util.zip.*
import kotlin.io.path.*

/**
 * утилітарний клас для роботи з файлами та файловою системою
 *
 * @author андрій будильников
 * @since 1.0.0
 */
@Suppress("unused")
class FileUtils {
    
    companion object {
        // стандартні розміри буферів
        const val DEFAULT_BUFFER_SIZE = 8192
        const val SMALL_BUFFER_SIZE = 1024
        const val LARGE_BUFFER_SIZE = 32768
        
        // символи для генерації імен файлів
        const val FILENAME_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    }
    
    // базові функції для роботи з файлами
    
    /**
     * перевіряє, чи існує файл
     *
     * @param filePath шлях до файлу
     * @return true якщо файл існує
     */
    fun fileExists(filePath: String): Boolean {
        return Files.exists(Paths.get(filePath))
    }
    
    /**
     * перевіряє, чи існує директорія
     *
     * @param dirPath шлях до директорії
     * @return true якщо директорія існує
     */
    fun directoryExists(dirPath: String): Boolean {
        return Files.exists(Paths.get(dirPath)) && Files.isDirectory(Paths.get(dirPath))
    }
    
    /**
     * створює директорію
     *
     * @param dirPath шлях до директорії
     * @return true якщо директорія була створена
     */
    fun createDirectory(dirPath: String): Boolean {
        return try {
            Files.createDirectories(Paths.get(dirPath))
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * видаляє файл
     *
     * @param filePath шлях до файлу
     * @return true якщо файл був видалений
     */
    fun deleteFile(filePath: String): Boolean {
        return try {
            Files.deleteIfExists(Paths.get(filePath))
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * видаляє директорію рекурсивно
     *
     * @param dirPath шлях до директорії
     * @return true якщо директорія була видалена
     */
    fun deleteDirectory(dirPath: String): Boolean {
        return try {
            Files.walkFileTree(Paths.get(dirPath), object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    Files.delete(file)
                    return FileVisitResult.CONTINUE
                }
                
                override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                    Files.delete(dir)
                    return FileVisitResult.CONTINUE
                }
            })
            true
        } catch (e: IOException) {
            false
        }
    }
    
    // функції для читання файлів
    
    /**
     * читає весь вміст файлу як текст
     *
     * @param filePath шлях до файлу
     * @param charset кодування
     * @return вміст файлу як текст
     */
    fun readTextFile(filePath: String, charset: Charset = Charsets.UTF_8): String {
        return Files.readString(Paths.get(filePath), charset)
    }
    
    /**
     * читає весь вміст файлу як байтовий масив
     *
     * @param filePath шлях до файлу
     * @return вміст файлу як байтовий масив
     */
    fun readBinaryFile(filePath: String): ByteArray {
        return Files.readAllBytes(Paths.get(filePath))
    }
    
    /**
     * читає файл построково
     *
     * @param filePath шлях до файлу
     * @param charset кодування
     * @return список рядків
     */
    fun readLines(filePath: String, charset: Charset = Charsets.UTF_8): List<String> {
        return Files.readAllLines(Paths.get(filePath), charset)
    }
    
    /**
     * читає файл построково з обробкою кожної лінії
     *
     * @param filePath шлях до файлу
     * @param charset кодування
     * @param processor функція для обробки кожної лінії
     */
    fun processLines(filePath: String, charset: Charset = Charsets.UTF_8, processor: (String) -> Unit) {
        Files.lines(Paths.get(filePath), charset).use { lines ->
            lines.forEach(processor)
        }
    }
    
    /**
     * читає файл частинами з використанням буфера
     *
     * @param filePath шлях до файлу
     * @param bufferSize розмір буфера
     * @param processor функція для обробки кожної частини
     */
    fun processFileInChunks(filePath: String, bufferSize: Int = DEFAULT_BUFFER_SIZE, processor: (ByteArray, Int) -> Unit) {
        val buffer = ByteArray(bufferSize)
        FileInputStream(filePath).use { inputStream ->
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                processor(buffer, bytesRead)
            }
        }
    }
    
    // функції для запису файлів
    
    /**
     * записує текст у файл
     *
     * @param filePath шлях до файлу
     * @param content текст для запису
     * @param charset кодування
     * @param append додавати до кінця файлу
     */
    fun writeTextFile(filePath: String, content: String, charset: Charset = Charsets.UTF_8, append: Boolean = false) {
        val path = Paths.get(filePath)
        if (append) {
            Files.writeString(path, content, charset, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
        } else {
            Files.writeString(path, content, charset, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
        }
    }
    
    /**
     * записує байтовий масив у файл
     *
     * @param filePath шлях до файлу
     * @param data дані для запису
     * @param append додавати до кінця файлу
     */
    fun writeBinaryFile(filePath: String, data: ByteArray, append: Boolean = false) {
        val path = Paths.get(filePath)
        if (append) {
            Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
        } else {
            Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
        }
    }
    
    /**
     * записує список рядків у файл
     *
     * @param filePath шлях до файлу
     * @param lines список рядків
     * @param charset кодування
     * @param append додавати до кінця файлу
     */
    fun writeLines(filePath: String, lines: List<String>, charset: Charset = Charsets.UTF_8, append: Boolean = false) {
        val path = Paths.get(filePath)
        if (append) {
            Files.write(path, lines, charset, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
        } else {
            Files.write(path, lines, charset, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
        }
    }
    
    /**
     * записує дані у файл частинами з використанням буфера
     *
     * @param filePath шлях до файлу
     * @param data дані для запису
     * @param bufferSize розмір буфера
     */
    fun writeInChunks(filePath: String, data: ByteArray, bufferSize: Int = DEFAULT_BUFFER_SIZE) {
        val path = Paths.get(filePath)
        FileOutputStream(path.toFile()).use { outputStream ->
            var offset = 0
            while (offset < data.size) {
                val length = minOf(bufferSize, data.size - offset)
                outputStream.write(data, offset, length)
                offset += length
            }
        }
    }
    
    // функції для копіювання файлів
    
    /**
     * копіює файл
     *
     * @param sourcePath шлях до вихідного файлу
     * @param targetPath шлях до цільового файлу
     * @param replaceExisting замінювати існуючий файл
     * @return true якщо копіювання вдалося
     */
    fun copyFile(sourcePath: String, targetPath: String, replaceExisting: Boolean = true): Boolean {
        return try {
            val options = if (replaceExisting) {
                arrayOf(StandardCopyOption.REPLACE_EXISTING)
            } else {
                emptyArray()
            }
            Files.copy(Paths.get(sourcePath), Paths.get(targetPath), *options)
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * копіює директорію рекурсивно
     *
     * @param sourcePath шлях до вихідної директорії
     * @param targetPath шлях до цільової директорії
     * @return true якщо копіювання вдалося
     */
    fun copyDirectory(sourcePath: String, targetPath: String): Boolean {
        return try {
            Files.walkFileTree(Paths.get(sourcePath), object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    val targetDir = Paths.get(targetPath, dir.toString().substring(sourcePath.length))
                    Files.createDirectories(targetDir)
                    return FileVisitResult.CONTINUE
                }
                
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    val targetFile = Paths.get(targetPath, file.toString().substring(sourcePath.length))
                    Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING)
                    return FileVisitResult.CONTINUE
                }
            })
            true
        } catch (e: IOException) {
            false
        }
    }
    
    // функції для переміщення файлів
    
    /**
     * переміщує файл
     *
     * @param sourcePath шлях до вихідного файлу
     * @param targetPath шлях до цільового файлу
     * @param replaceExisting замінювати існуючий файл
     * @return true якщо переміщення вдалося
     */
    fun moveFile(sourcePath: String, targetPath: String, replaceExisting: Boolean = true): Boolean {
        return try {
            val options = if (replaceExisting) {
                arrayOf(StandardCopyOption.REPLACE_EXISTING)
            } else {
                emptyArray()
            }
            Files.move(Paths.get(sourcePath), Paths.get(targetPath), *options)
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * переміщує директорію
     *
     * @param sourcePath шлях до вихідної директорії
     * @param targetPath шлях до цільової директорії
     * @return true якщо переміщення вдалося
     */
    fun moveDirectory(sourcePath: String, targetPath: String): Boolean {
        return try {
            Files.move(Paths.get(sourcePath), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING)
            true
        } catch (e: IOException) {
            false
        }
    }
    
    // функції для отримання інформації про файли
    
    /**
     * отримує розмір файлу
     *
     * @param filePath шлях до файлу
     * @return розмір файлу в байтах
     */
    fun getFileSize(filePath: String): Long {
        return try {
            Files.size(Paths.get(filePath))
        } catch (e: IOException) {
            -1L
        }
    }
    
    /**
     * отримує час останньої модифікації файлу
     *
     * @param filePath шлях до файлу
     * @return час останньої модифікації
     */
    fun getLastModifiedTime(filePath: String): Long {
        return try {
            Files.getLastModifiedTime(Paths.get(filePath)).toMillis()
        } catch (e: IOException) {
            -1L
        }
    }
    
    /**
     * перевіряє, чи файл є прихованим
     *
     * @param filePath шлях до файлу
     * @return true якщо файл є прихованим
     */
    fun isHidden(filePath: String): Boolean {
        return try {
            Files.isHidden(Paths.get(filePath))
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * перевіряє, чи файл є директорією
     *
     * @param filePath шлях до файлу
     * @return true якщо файл є директорією
     */
    fun isDirectory(filePath: String): Boolean {
        return try {
            Files.isDirectory(Paths.get(filePath))
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * перевіряє, чи файл є звичайним файлом
     *
     * @param filePath шлях до файлу
     * @return true якщо файл є звичайним файлом
     */
    fun isRegularFile(filePath: String): Boolean {
        return try {
            Files.isRegularFile(Paths.get(filePath))
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * отримує розширення файлу
     *
     * @param filePath шлях до файлу
     * @return розширення файлу
     */
    fun getFileExtension(filePath: String): String {
        val fileName = Paths.get(filePath).fileName.toString()
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex > 0 && dotIndex < fileName.length - 1) {
            fileName.substring(dotIndex + 1)
        } else {
            ""
        }
    }
    
    /**
     * отримує ім'я файлу без розширення
     *
     * @param filePath шлях до файлу
     * @return ім'я файлу без розширення
     */
    fun getFileNameWithoutExtension(filePath: String): String {
        val fileName = Paths.get(filePath).fileName.toString()
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex > 0) {
            fileName.substring(0, dotIndex)
        } else {
            fileName
        }
    }
    
    // функції для роботи з архівами
    
    /**
     * створює zip-архів з файлу
     *
     * @param filePath шлях до файлу
     * @param zipPath шлях до zip-архіву
     * @return true якщо архівування вдалося
     */
    fun createZipFromFile(filePath: String, zipPath: String): Boolean {
        return try {
            val path = Paths.get(filePath)
            val zipOutputStream = ZipOutputStream(FileOutputStream(zipPath))
            zipOutputStream.use { zos ->
                val zipEntry = ZipEntry(path.fileName.toString())
                zos.putNextEntry(zipEntry)
                
                Files.newInputStream(path).use { inputStream ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        zos.write(buffer, 0, length)
                    }
                }
                
                zos.closeEntry()
            }
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * створює zip-архів з директорії
     *
     * @param dirPath шлях до директорії
     * @param zipPath шлях до zip-архіву
     * @return true якщо архівування вдалося
     */
    fun createZipFromDirectory(dirPath: String, zipPath: String): Boolean {
        return try {
            val dir = Paths.get(dirPath)
            val zipOutputStream = ZipOutputStream(FileOutputStream(zipPath))
            zipOutputStream.use { zos ->
                Files.walk(dir).filter { path -> !Files.isDirectory(path) }.forEach { path ->
                    val zipEntry = ZipEntry(dir.relativize(path).toString())
                    zos.putNextEntry(zipEntry)
                    
                    Files.newInputStream(path).use { inputStream ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var length: Int
                        while (inputStream.read(buffer).also { length = it } > 0) {
                            zos.write(buffer, 0, length)
                        }
                    }
                    
                    zos.closeEntry()
                }
            }
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * розпаковує zip-архів
     *
     * @param zipPath шлях до zip-архіву
     * @param destDir шлях до директорії призначення
     * @return true якщо розпакування вдалося
     */
    fun extractZip(zipPath: String, destDir: String): Boolean {
        return try {
            val destPath = Paths.get(destDir)
            Files.createDirectories(destPath)
            
            ZipInputStream(FileInputStream(zipPath)).use { zis ->
                var zipEntry = zis.nextEntry
                while (zipEntry != null) {
                    val newFile = destPath.resolve(zipEntry.name).normalize()
                    
                    // перевірка на шлях обходу (zip slip)
                    if (!newFile.startsWith(destPath)) {
                        throw IOException("неправильний шлях в архіві: ${zipEntry.name}")
                    }
                    
                    if (zipEntry.isDirectory) {
                        Files.createDirectories(newFile)
                    } else {
                        Files.createDirectories(newFile.parent)
                        Files.newOutputStream(newFile).use { fos ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            var length: Int
                            while (zis.read(buffer).also { length = it } > 0) {
                                fos.write(buffer, 0, length)
                            }
                        }
                    }
                    
                    zipEntry = zis.nextEntry
                }
            }
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * створює gz-архів з файлу
     *
     * @param filePath шлях до файлу
     * @param gzPath шлях до gz-архіву
     * @return true якщо архівування вдалося
     */
    fun createGzipFromFile(filePath: String, gzPath: String): Boolean {
        return try {
            val path = Paths.get(filePath)
            val gzipOutputStream = GZIPOutputStream(FileOutputStream(gzPath))
            gzipOutputStream.use { gzos ->
                Files.newInputStream(path).use { inputStream ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        gzos.write(buffer, 0, length)
                    }
                }
            }
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * розпаковує gz-архів
     *
     * @param gzPath шлях до gz-архіву
     * @param destPath шлях до файлу призначення
     * @return true якщо розпакування вдалося
     */
    fun extractGzip(gzPath: String, destPath: String): Boolean {
        return try {
            val gzipInputStream = GZIPInputStream(FileInputStream(gzPath))
            gzipInputStream.use { gzis ->
                Files.newOutputStream(Paths.get(destPath)).use { outputStream ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var length: Int
                    while (gzis.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                }
            }
            true
        } catch (e: IOException) {
            false
        }
    }
    
    // функції для обчислення хешів
    
    /**
     * обчислює md5-хеш файлу
     *
     * @param filePath шлях до файлу
     * @return md5-хеш у вигляді hex-рядка
     */
    fun calculateMD5(filePath: String): String {
        return calculateHash(filePath, "MD5")
    }
    
    /**
     * обчислює sha1-хеш файлу
     *
     * @param filePath шлях до файлу
     * @return sha1-хеш у вигляді hex-рядка
     */
    fun calculateSHA1(filePath: String): String {
        return calculateHash(filePath, "SHA-1")
    }
    
    /**
     * обчислює sha256-хеш файлу
     *
     * @param filePath шлях до файлу
     * @return sha256-хеш у вигляді hex-рядка
     */
    fun calculateSHA256(filePath: String): String {
        return calculateHash(filePath, "SHA-256")
    }
    
    /**
     * обчислює хеш файлу з використанням заданого алгоритму
     *
     * @param filePath шлях до файлу
     * @param algorithm алгоритм хешування
     * @return хеш у вигляді hex-рядка
     */
    fun calculateHash(filePath: String, algorithm: String): String {
        return try {
            val messageDigest = MessageDigest.getInstance(algorithm)
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            
            FileInputStream(filePath).use { inputStream ->
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    messageDigest.update(buffer, 0, bytesRead)
                }
            }
            
            val hashBytes = messageDigest.digest()
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
    
    // функції для роботи з тимчасовими файлами
    
    /**
     * створює тимчасовий файл
     *
     * @param prefix префікс імені файлу
     * @param suffix суфікс імені файлу
     * @param directory директорія для тимчасового файлу
     * @return шлях до тимчасового файлу
     */
    fun createTempFile(prefix: String = "temp", suffix: String = ".tmp", directory: String? = null): String {
        val tempFile = if (directory != null) {
            Files.createTempFile(Paths.get(directory), prefix, suffix)
        } else {
            Files.createTempFile(prefix, suffix)
        }
        return tempFile.toString()
    }
    
    /**
     * створює тимчасову директорію
     *
     * @param prefix префікс імені директорії
     * @return шлях до тимчасової директорії
     */
    fun createTempDirectory(prefix: String = "temp"): String {
        val tempDir = Files.createTempDirectory(prefix)
        return tempDir.toString()
    }
    
    /**
     * видаляє тимчасовий файл
     *
     * @param tempFilePath шлях до тимчасового файлу
     * @return true якщо файл був видалений
     */
    fun deleteTempFile(tempFilePath: String): Boolean {
        return deleteFile(tempFilePath)
    }
    
    /**
     * видаляє тимчасову директорію
     *
     * @param tempDirPath шлях до тимчасової директорії
     * @return true якщо директорія була видалена
     */
    fun deleteTempDirectory(tempDirPath: String): Boolean {
        return deleteDirectory(tempDirPath)
    }
    
    // функції для пошуку файлів
    
    /**
     * знаходить файли за шаблоном імені
     *
     * @param directory директорія для пошуку
     * @param pattern шаблон імені файлу (наприклад, "*.txt")
     * @param recursive шукати рекурсивно в піддиректоріях
     * @return список шляхів до знайдених файлів
     */
    fun findFiles(directory: String, pattern: String, recursive: Boolean = true): List<String> {
        val dirPath = Paths.get(directory)
        val files = mutableListOf<String>()
        
        try {
            val matcher = FileSystems.getDefault().getPathMatcher("glob:$pattern")
            
            val visitor = object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (matcher.matches(file.fileName)) {
                        files.add(file.toString())
                    }
                    return FileVisitResult.CONTINUE
                }
                
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    return if (recursive || dir == dirPath) {
                        FileVisitResult.CONTINUE
                    } else {
                        FileVisitResult.SKIP_SUBTREE
                    }
                }
            }
            
            Files.walkFileTree(dirPath, visitor)
        } catch (e: IOException) {
            // ігноруємо помилки
        }
        
        return files
    }
    
    /**
     * знаходить файли за розширенням
     *
     * @param directory директорія для пошуку
     * @param extension розширення файлу (наприклад, "txt")
     * @param recursive шукати рекурсивно в піддиректоріях
     * @return список шляхів до знайдених файлів
     */
    fun findFilesByExtension(directory: String, extension: String, recursive: Boolean = true): List<String> {
        val pattern = "*.$extension"
        return findFiles(directory, pattern, recursive)
    }
    
    /**
     * знаходить файли за розміром
     *
     * @param directory директорія для пошуку
     * @param minSize мінімальний розмір файлу в байтах
     * @param maxSize максимальний розмір файлу в байтах (-1 для необмеженого)
     * @param recursive шукати рекурсивно в піддиректоріях
     * @return список шляхів до знайдених файлів
     */
    fun findFilesBySize(directory: String, minSize: Long, maxSize: Long = -1, recursive: Boolean = true): List<String> {
        val dirPath = Paths.get(directory)
        val files = mutableListOf<String>()
        
        try {
            val visitor = object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    val size = attrs.size()
                    if (size >= minSize && (maxSize == -1L || size <= maxSize)) {
                        files.add(file.toString())
                    }
                    return FileVisitResult.CONTINUE
                }
                
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    return if (recursive || dir == dirPath) {
                        FileVisitResult.CONTINUE
                    } else {
                        FileVisitResult.SKIP_SUBTREE
                    }
                }
            }
            
            Files.walkFileTree(dirPath, visitor)
        } catch (e: IOException) {
            // ігноруємо помилки
        }
        
        return files
    }
    
    // функції для роботи з правами доступу
    
    /**
     * отримує права доступу до файлу
     *
     * @param filePath шлях до файлу
     * @return права доступу
     */
    fun getFilePermissions(filePath: String): Set<PosixFilePermission>? {
        return try {
            Files.getPosixFilePermissions(Paths.get(filePath))
        } catch (e: IOException) {
            null
        } catch (e: UnsupportedOperationException) {
            null
        }
    }
    
    /**
     * встановлює права доступу до файлу
     *
     * @param filePath шлях до файлу
     * @param permissions права доступу
     * @return true якщо права були встановлені
     */
    fun setFilePermissions(filePath: String, permissions: Set<PosixFilePermission>): Boolean {
        return try {
            Files.setPosixFilePermissions(Paths.get(filePath), permissions)
            true
        } catch (e: IOException) {
            false
        } catch (e: UnsupportedOperationException) {
            false
        }
    }
    
    /**
     * перевіряє, чи файл доступний для читання
     *
     * @param filePath шлях до файлу
     * @return true якщо файл доступний для читання
     */
    fun isReadable(filePath: String): Boolean {
        return Files.isReadable(Paths.get(filePath))
    }
    
    /**
     * перевіряє, чи файл доступний для запису
     *
     * @param filePath шлях до файлу
     * @return true якщо файл доступний для запису
     */
    fun isWritable(filePath: String): Boolean {
        return Files.isWritable(Paths.get(filePath))
    }
    
    /**
     * перевіряє, чи файл доступний для виконання
     *
     * @param filePath шлях до файлу
     * @return true якщо файл доступний для виконання
     */
    fun isExecutable(filePath: String): Boolean {
        return Files.isExecutable(Paths.get(filePath))
    }
    
    // функції для роботи з символічними посиланнями
    
    /**
     * створює символічне посилання
     *
     * @param linkPath шлях до символічного посилання
     * @param targetPath шлях до цільового файлу
     * @return true якщо посилання було створено
     */
    fun createSymbolicLink(linkPath: String, targetPath: String): Boolean {
        return try {
            Files.createSymbolicLink(Paths.get(linkPath), Paths.get(targetPath))
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * отримує ціль символічного посилання
     *
     * @param linkPath шлях до символічного посилання
     * @return шлях до цільового файлу
     */
    fun readSymbolicLink(linkPath: String): String? {
        return try {
            Files.readSymbolicLink(Paths.get(linkPath)).toString()
        } catch (e: IOException) {
            null
        }
    }
    
    /**
     * перевіряє, чи файл є символічним посиланням
     *
     * @param filePath шлях до файлу
     * @return true якщо файл є символічним посиланням
     */
    fun isSymbolicLink(filePath: String): Boolean {
        return try {
            Files.isSymbolicLink(Paths.get(filePath))
        } catch (e: IOException) {
            false
        }
    }
    
    // функції для роботи з історією файлів
    
    /**
     * представлення запису історії файлу
     *
     * @property filePath шлях до файлу
     * @property operation операція (створення, читання, запис, видалення)
     * @property timestamp час операції
     * @property details додаткові деталі
     */
    data class FileHistoryEntry(
        val filePath: String,
        val operation: String,
        val timestamp: Long,
        val details: String = ""
    )
    
    /**
     * менеджер історії файлових операцій
     */
    class FileHistoryManager {
        private val history = mutableListOf<FileHistoryEntry>()
        
        /**
         * додає запис до історії
         *
         * @param entry запис історії
         */
        fun addEntry(entry: FileHistoryEntry) {
            history.add(entry)
        }
        
        /**
         * отримує історію для конкретного файлу
         *
         * @param filePath шлях до файлу
         * @return список записів історії
         */
        fun getFileHistory(filePath: String): List<FileHistoryEntry> {
            return history.filter { it.filePath == filePath }
        }
        
        /**
         * отримує всю історію
         *
         * @return список всіх записів історії
         */
        fun getAllHistory(): List<FileHistoryEntry> {
            return history.toList()
        }
        
        /**
         * очищує історію
         */
        fun clearHistory() {
            history.clear()
        }
        
        /**
         * отримує кількість записів в історії
         *
         * @return кількість записів
         */
        fun getHistorySize(): Int {
            return history.size
        }
    }
    
    /**
     * створює менеджер історії файлових операцій
     *
     * @return менеджер історії
     */
    fun createFileHistoryManager(): FileHistoryManager {
        return FileHistoryManager()
    }
    
    // функції для роботи з файловими блокуваннями
    
    /**
     * представлення файлового блокування
     *
     * @property filePath шлях до файлу
     * @property fileChannel канал файлу
     * @property fileLock блокування файлу
     */
    class FileLock(
        private val filePath: String,
        private val fileChannel: FileChannel,
        private val fileLock: java.nio.channels.FileLock
    ) : AutoCloseable {
        
        /**
         * перевіряє, чи блокування дійсне
         *
         * @return true якщо блокування дійсне
         */
        fun isValid(): Boolean {
            return fileLock.isValid
        }
        
        /**
         * перевіряє, чи блокування є спільним
         *
         * @return true якщо блокування є спільним
         */
        fun isShared(): Boolean {
            return fileLock.isShared
        }
        
        /**
         * звільняє блокування
         */
        override fun close() {
            try {
                fileLock.release()
                fileChannel.close()
            } catch (e: IOException) {
                // ігноруємо помилки при закритті
            }
        }
    }
    
    /**
     * отримує ексклюзивне блокування файлу
     *
     * @param filePath шлях до файлу
     * @return файлове блокування або null якщо блокування не вдалося
     */
    fun lockFileExclusive(filePath: String): FileLock? {
        return try {
            val fileChannel = FileChannel.open(
                Paths.get(filePath),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
            )
            val fileLock = fileChannel.tryLock()
            if (fileLock != null) {
                FileLock(filePath, fileChannel, fileLock)
            } else {
                fileChannel.close()
                null
            }
        } catch (e: IOException) {
            null
        }
    }
    
    /**
     * отримує спільне блокування файлу
     *
     * @param filePath шлях до файлу
     * @return файлове блокування або null якщо блокування не вдалося
     */
    fun lockFileShared(filePath: String): FileLock? {
        return try {
            val fileChannel = FileChannel.open(
                Paths.get(filePath),
                StandardOpenOption.READ
            )
            val fileLock = fileChannel.tryLock(0, Long.MAX_VALUE, true)
            if (fileLock != null) {
                FileLock(filePath, fileChannel, fileLock)
            } else {
                fileChannel.close()
                null
            }
        } catch (e: IOException) {
            null
        }
    }
    
    // функції для роботи з файловими потоками
    
    /**
     * представлення файлового потоку з буферизацією
     *
     * @property filePath шлях до файлу
     * @property bufferSize розмір буфера
     */
    class BufferedFileStream(private val filePath: String, private val bufferSize: Int = DEFAULT_BUFFER_SIZE) {
        private var inputStream: BufferedInputStream? = null
        private var outputStream: BufferedOutputStream? = null
        
        /**
         * відкриває потік для читання
         *
         * @return потік для читання
         */
        fun openForReading(): BufferedInputStream {
            close()
            inputStream = BufferedInputStream(FileInputStream(filePath), bufferSize)
            return inputStream!!
        }
        
        /**
         * відкриває потік для запису
         *
         * @param append додавати до кінця файлу
         * @return потік для запису
         */
        fun openForWriting(append: Boolean = false): BufferedOutputStream {
            close()
            val fileOutputStream = if (append) {
                FileOutputStream(filePath, true)
            } else {
                FileOutputStream(filePath)
            }
            outputStream = BufferedOutputStream(fileOutputStream, bufferSize)
            return outputStream!!
        }
        
        /**
         * закриває потоки
         */
        fun close() {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: IOException) {
                // ігноруємо помилки при закритті
            } finally {
                inputStream = null
                outputStream = null
            }
        }
    }
    
    /**
     * створює буферизований файловий потік
     *
     * @param filePath шлях до файлу
     * @param bufferSize розмір буфера
     * @return файловий потік
     */
    fun createBufferedFileStream(filePath: String, bufferSize: Int = DEFAULT_BUFFER_SIZE): BufferedFileStream {
        return BufferedFileStream(filePath, bufferSize)
    }
    
    // функції для роботи з файловими системами
    
    /**
     * отримує список кореневих директорій файлової системи
     *
     * @return список шляхів до кореневих директорій
     */
    fun getRootDirectories(): List<String> {
        return try {
            FileSystems.getDefault().rootDirectories.map { it.toString() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * отримує роздільник шляхів для поточної операційної системи
     *
     * @return роздільник шляхів
     */
    fun getPathSeparator(): String {
        return FileSystems.getDefault().separator
    }
    
    /**
     * нормалізує шлях до файлу
     *
     * @param filePath шлях до файлу
     * @return нормалізований шлях
     */
    fun normalizePath(filePath: String): String {
        return try {
            Paths.get(filePath).normalize().toString()
        } catch (e: Exception) {
            filePath
        }
    }
    
    /**
     * об'єднує частини шляху
     *
     * @param parts частини шляху
     * @return об'єднаний шлях
     */
    fun joinPath(vararg parts: String): String {
        if (parts.isEmpty()) return ""
        
        val normalizedParts = parts.map { it.trimEnd(FileSystems.getDefault().separator.toCharArray()) }
        return normalizedParts.joinToString(FileSystems.getDefault().separator)
    }
    
    // функції для роботи з файловими атрибутами
    
    /**
     * отримує базові атрибути файлу
     *
     * @param filePath шлях до файлу
     * @return базові атрибути або null якщо не вдалося отримати
     */
    fun getBasicFileAttributes(filePath: String): BasicFileAttributes? {
        return try {
            Files.readAttributes(Paths.get(filePath), BasicFileAttributes::class.java)
        } catch (e: IOException) {
            null
        }
    }
    
    /**
     * отримує розмір файлу
     *
     * @param attributes атрибути файлу
     * @return розмір файлу в байтах
     */
    fun getFileSize(attributes: BasicFileAttributes): Long {
        return attributes.size()
    }
    
    /**
     * перевіряє, чи файл є директорією
     *
     * @param attributes атрибути файлу
     * @return true якщо файл є директорією
     */
    fun isDirectory(attributes: BasicFileAttributes): Boolean {
        return attributes.isDirectory
    }
    
    /**
     * перевіряє, чи файл є звичайним файлом
     *
     * @param attributes атрибути файлу
     * @return true якщо файл є звичайним файлом
     */
    fun isRegularFile(attributes: BasicFileAttributes): Boolean {
        return attributes.isRegularFile
    }
    
    /**
     * перевіряє, чи файл є символічним посиланням
     *
     * @param attributes атрибути файлу
     * @return true якщо файл є символічним посиланням
     */
    fun isSymbolicLink(attributes: BasicFileAttributes): Boolean {
        return attributes.isSymbolicLink
    }
    
    /**
     * отримує час створення файлу
     *
     * @param attributes атрибути файлу
     * @return час створення
     */
    fun getCreationTime(attributes: BasicFileAttributes): Long {
        return attributes.creationTime().toMillis()
    }
    
    /**
     * отримує час останнього доступу до файлу
     *
     * @param attributes атрибути файлу
     * @return час останнього доступу
     */
    fun getLastAccessTime(attributes: BasicFileAttributes): Long {
        return attributes.lastAccessTime().toMillis()
    }
    
    /**
     * отримує час останньої модифікації файлу
     *
     * @param attributes атрибути файлу
     * @return час останньої модифікації
     */
    fun getLastModifiedTime(attributes: BasicFileAttributes): Long {
        return attributes.lastModifiedTime().toMillis()
    }
    
    // функції для роботи з файловими операціями вищого рівня
    
    /**
     * копіює вміст одного файлу в інший з використанням буферизації
     *
     * @param sourcePath шлях до вихідного файлу
     * @param targetPath шлях до цільового файлу
     * @param bufferSize розмір буфера
     * @param replaceExisting замінювати існуючий файл
     * @return true якщо копіювання вдалося
     */
    fun copyFileWithBuffer(sourcePath: String, targetPath: String, bufferSize: Int = DEFAULT_BUFFER_SIZE, replaceExisting: Boolean = true): Boolean {
        return try {
            val options = if (replaceExisting) {
                arrayOf(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
            } else {
                arrayOf(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
            }
            
            val buffer = ByteArray(bufferSize)
            FileInputStream(sourcePath).use { input ->
                FileOutputStream(targetPath).use { output ->
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * переміщує файл з використанням копіювання та видалення
     *
     * @param sourcePath шлях до вихідного файлу
     * @param targetPath шлях до цільового файлу
     * @param bufferSize розмір буфера
     * @param replaceExisting замінювати існуючий файл
     * @return true якщо переміщення вдалося
     */
    fun moveFileByCopy(sourcePath: String, targetPath: String, bufferSize: Int = DEFAULT_BUFFER_SIZE, replaceExisting: Boolean = true): Boolean {
        if (copyFileWithBuffer(sourcePath, targetPath, bufferSize, replaceExisting)) {
            return deleteFile(sourcePath)
        }
        return false
    }
    
    /**
     * синхронізує два файли (копіює з вихідного в цільовий, якщо вихідний новіший)
     *
     * @param sourcePath шлях до вихідного файлу
     * @param targetPath шлях до цільового файлу
     * @param bufferSize розмір буфера
     * @return true якщо синхронізація вдалася
     */
    fun syncFile(sourcePath: String, targetPath: String, bufferSize: Int = DEFAULT_BUFFER_SIZE): Boolean {
        val sourceTime = getLastModifiedTime(sourcePath)
        val targetTime = getLastModifiedTime(targetPath)
        
        // якщо вихідний файл новіший або цільовий файл не існує
        if (sourceTime > targetTime || targetTime == -1L) {
            return copyFileWithBuffer(sourcePath, targetPath, bufferSize, true)
        }
        
        return true // файли вже синхронізовані
    }
    
    /**
     * об'єднує кілька файлів в один
     *
     * @param inputFiles список шляхів до вхідних файлів
     * @param outputFile шлях до вихідного файлу
     * @param separator роздільник між файлами
     * @param bufferSize розмір буфера
     * @return true якщо об'єднання вдалося
     */
    fun mergeFiles(inputFiles: List<String>, outputFile: String, separator: String = "\n", bufferSize: Int = DEFAULT_BUFFER_SIZE): Boolean {
        return try {
            FileOutputStream(outputFile).use { outputStream ->
                val writer = OutputStreamWriter(outputStream, Charsets.UTF_8)
                
                inputFiles.forEachIndexed { index, filePath ->
                    FileInputStream(filePath).use { inputStream ->
                        val reader = InputStreamReader(inputStream, Charsets.UTF_8)
                        val buffer = CharArray(bufferSize)
                        var charsRead: Int
                        
                        while (reader.read(buffer).also { charsRead = it } != -1) {
                            writer.write(buffer, 0, charsRead)
                        }
                    }
                    
                    // додаємо роздільник між файлами (крім останнього)
                    if (index < inputFiles.size - 1) {
                        writer.write(separator)
                    }
                }
                
                writer.flush()
            }
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * розділяє файл на частини
     *
     * @param filePath шлях до файлу
     * @param partSize розмір кожної частини в байтах
     * @param outputDir директорія для вихідних файлів
     * @return список шляхів до частин файлу
     */
    fun splitFile(filePath: String, partSize: Long, outputDir: String): List<String> {
        val partFiles = mutableListOf<String>()
        
        try {
            val fileName = Paths.get(filePath).fileName.toString()
            val buffer = ByteArray(partSize.toInt().coerceAtMost(DEFAULT_BUFFER_SIZE))
            
            FileInputStream(filePath).use { inputStream ->
                var partNumber = 1
                var bytesRead: Int
                
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    val partFileName = "${fileName}.part${partNumber.toString().padStart(3, '0')}"
                    val partFilePath = joinPath(outputDir, partFileName)
                    
                    FileOutputStream(partFilePath).use { outputStream ->
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    
                    partFiles.add(partFilePath)
                    partNumber++
                }
            }
        } catch (e: IOException) {
            // у випадку помилки повертаємо порожній список
            partFiles.clear()
        }
        
        return partFiles
    }
    
    /**
     * об'єднує частини файлу
     *
     * @param partFiles список шляхів до частин файлу
     * @param outputFile шлях до вихідного файлу
     * @return true якщо об'єднання вдалося
     */
    fun joinFileParts(partFiles: List<String>, outputFile: String): Boolean {
        return try {
            FileOutputStream(outputFile).use { outputStream ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                
                partFiles.forEach { partFile ->
                    FileInputStream(partFile).use { inputStream ->
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                    }
                }
            }
            true
        } catch (e: IOException) {
            false
        }
    }
    
    // функції для роботи з файловими шаблонами
    
    /**
     * представлення файлового шаблону
     *
     * @property pattern шаблон
     * @property variables змінні шаблону
     */
    class FileTemplate(private val pattern: String, private val variables: Map<String, String> = emptyMap()) {
        
        /**
         * застосовує шаблон до даних
         *
         * @param data дані для підстановки
         * @return результат застосування шаблону
         */
        fun apply(data: Map<String, String> = emptyMap()): String {
            var result = pattern
            
            // підставляємо змінні шаблону
            variables.forEach { (key, value) ->
                result = result.replace("{{$key}}", value)
            }
            
            // підставляємо передані дані
            data.forEach { (key, value) ->
                result = result.replace("{{$key}}", value)
            }
            
            return result
        }
        
        /**
         * зберігає результат застосування шаблону у файл
         *
         * @param filePath шлях до файлу
         * @param data дані для підстановки
         * @param charset кодування
         * @return true якщо збереження вдалося
         */
        fun saveToFile(filePath: String, data: Map<String, String> = emptyMap(), charset: Charset = Charsets.UTF_8): Boolean {
            return try {
                val content = apply(data)
                Files.writeString(Paths.get(filePath), content, charset)
                true
            } catch (e: IOException) {
                false
            }
        }
    }
    
    /**
     * створює файловий шаблон
     *
     * @param pattern шаблон
     * @param variables змінні шаблону
     * @return файловий шаблон
     */
    fun createFileTemplate(pattern: String, variables: Map<String, String> = emptyMap()): FileTemplate {
        return FileTemplate(pattern, variables)
    }
    
    /**
     * створює файловий шаблон з файлу
     *
     * @param templateFilePath шлях до файлу шаблону
     * @param charset кодування
     * @param variables змінні шаблону
     * @return файловий шаблон
     */
    fun createFileTemplateFromFile(templateFilePath: String, charset: Charset = Charsets.UTF_8, variables: Map<String, String> = emptyMap()): FileTemplate {
        val pattern = readTextFile(templateFilePath, charset)
        return FileTemplate(pattern, variables)
    }
    
    // функції для роботи з файловими моніторами
    
    /**
     * інтерфейс для спостерігача за файлами
     */
    interface FileWatcher {
        /**
         * викликається при створенні файлу
         *
         * @param filePath шлях до файлу
         */
        fun onFileCreated(filePath: String)
        
        /**
         * викликається при зміні файлу
         *
         * @param filePath шлях до файлу
         */
        fun onFileModified(filePath: String)
        
        /**
         * викликається при видаленні файлу
         *
         * @param filePath шлях до файлу
         */
        fun onFileDeleted(filePath: String)
    }
    
    /**
     * монітор файлової системи
     *
     * @property watchDir директорія для спостереження
     * @property recursive спостерігати рекурсивно
     */
    class FileSystemMonitor(private val watchDir: String, private val recursive: Boolean = true) {
        private var watchService: WatchService? = null
        private var isWatching = false
        private val watchers = mutableListOf<FileWatcher>()
        
        /**
         * додає спостерігача
         *
         * @param watcher спостерігач
         */
        fun addWatcher(watcher: FileWatcher) {
            watchers.add(watcher)
        }
        
        /**
         * видаляє спостерігача
         *
         * @param watcher спостерігач
         */
        fun removeWatcher(watcher: FileWatcher) {
            watchers.remove(watcher)
        }
        
        /**
         * починає спостереження
         */
        fun startWatching() {
            if (isWatching) return
            
            try {
                watchService = FileSystems.getDefault().newWatchService()
                val dirPath = Paths.get(watchDir)
                
                if (recursive) {
                    Files.walkFileTree(dirPath, object : SimpleFileVisitor<Path>() {
                        override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                            dir.register(
                                watchService,
                                StandardWatchEventKinds.ENTRY_CREATE,
                                StandardWatchEventKinds.ENTRY_MODIFY,
                                StandardWatchEventKinds.ENTRY_DELETE
                            )
                            return FileVisitResult.CONTINUE
                        }
                    })
                } else {
                    dirPath.register(
                        watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE
                    )
                }
                
                isWatching = true
                
                // запускаємо окремий потік для обробки подій
                Thread {
                    while (isWatching) {
                        try {
                            val key = watchService?.take() ?: break
                            
                            for (event in key.pollEvents()) {
                                val kind = event.kind()
                                val fileName = event.context() as? Path
                                
                                if (fileName != null) {
                                    val filePath = key.watchable().toString() + FileSystems.getDefault().separator + fileName.toString()
                                    
                                    when (kind) {
                                        StandardWatchEventKinds.ENTRY_CREATE -> {
                                            watchers.forEach { it.onFileCreated(filePath) }
                                        }
                                        StandardWatchEventKinds.ENTRY_MODIFY -> {
                                            watchers.forEach { it.onFileModified(filePath) }
                                        }
                                        StandardWatchEventKinds.ENTRY_DELETE -> {
                                            watchers.forEach { it.onFileDeleted(filePath) }
                                        }
                                    }
                                }
                            }
                            
                            if (!key.reset()) {
                                break
                            }
                        } catch (e: InterruptedException) {
                            Thread.currentThread().interrupt()
                            break
                        } catch (e: Exception) {
                            // ігноруємо інші помилки
                        }
                    }
                }.start()
            } catch (e: IOException) {
                // ігноруємо помилки при створенні спостерігача
            }
        }
        
        /**
         * зупиняє спостереження
         */
        fun stopWatching() {
            isWatching = false
            try {
                watchService?.close()
            } catch (e: IOException) {
                // ігноруємо помилки при закритті
            }
            watchService = null
        }
    }
    
    /**
     * створює монітор файлової системи
     *
     * @param watchDir директорія для спостереження
     * @param recursive спостерігати рекурсивно
     * @return монітор файлової системи
     */
    fun createFileSystemMonitor(watchDir: String, recursive: Boolean = true): FileSystemMonitor {
        return FileSystemMonitor(watchDir, recursive)
    }
    
    // функції для роботи з файловими кешами
    
    /**
     * простий файловий кеш
     *
     * @property cacheDir директорія для кешу
     * @property maxSize максимальний розмір кешу в байтах
     */
    class FileCache(private val cacheDir: String, private val maxSize: Long = 100 * 1024 * 1024) { // 100MB за замовчуванням
        private val cacheInfoFile = joinPath(cacheDir, "cache.info")
        private val cacheInfo = mutableMapOf<String, CacheEntry>()
        private var currentSize = 0L
        
        init {
            loadCacheInfo()
        }
        
        /**
     