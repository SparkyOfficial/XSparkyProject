/**
 * xsparkyproject - розширена бібліотека для kotlin
 *
 * Copyright 2025 Андрій Будильников
 */

package com.sparky.xsparkyproject.utils

import java.security.*
import java.security.spec.*
import java.util.*
import javax.crypto.*
import javax.crypto.spec.*
import java.nio.charset.StandardCharsets
import java.io.*
import java.math.BigInteger
import kotlin.experimental.xor

/**
 * утилітарний клас для роботи з безпекою
 *
 * @author андрій будильников
 * @since 1.0.0
 */
@Suppress("unused")
class SecurityUtils {
    
    companion object {
        // стандартні алгоритми
        const val ALGORITHM_AES = "AES"
        const val ALGORITHM_RSA = "RSA"
        const val ALGORITHM_SHA256 = "SHA-256"
        const val ALGORITHM_SHA512 = "SHA-512"
        const val ALGORITHM_MD5 = "MD5"
        const val ALGORITHM_HMAC_SHA256 = "HmacSHA256"
        const val ALGORITHM_HMAC_SHA512 = "HmacSHA512"
        
        // стандартні розміри ключів
        const val RSA_KEY_SIZE_1024 = 1024
        const val RSA_KEY_SIZE_2048 = 2048
        const val RSA_KEY_SIZE_4096 = 4096
        
        // стандартні розміри блоків
        const val AES_BLOCK_SIZE = 16
        const val RSA_BLOCK_SIZE_1024 = 128
        const val RSA_BLOCK_SIZE_2048 = 256
        
        // стандартні ітерації для PBKDF2
        const val PBKDF2_ITERATIONS = 10000
        const val PBKDF2_KEY_LENGTH = 256
        
        // стандартні налаштування
        const val DEFAULT_SALT_LENGTH = 16
        const val DEFAULT_IV_LENGTH = 16
    }
    
    // базові функції для роботи з хешуванням
    
    /**
     * представлення хешувальника
     */
    interface Hasher {
        /**
         * обчислює хеш від даних
         *
         * @param data дані
         * @return хеш
         */
        fun hash(data: ByteArray): ByteArray
        
        /**
         * обчислює хеш від рядка
         *
         * @param data рядок
         * @param charset кодування
         * @return хеш у вигляді hex-рядка
         */
        fun hashString(data: String, charset: java.nio.charset.Charset = StandardCharsets.UTF_8): String {
            val hashBytes = hash(data.toByteArray(charset))
            return bytesToHex(hashBytes)
        }
        
        /**
         * перевіряє, чи хеш відповідає даним
         *
         * @param data дані
         * @param hash хеш
         * @return true якщо хеш відповідає даним
         */
        fun verify(data: ByteArray, hash: ByteArray): Boolean {
            val computedHash = hash(data)
            return MessageDigest.isEqual(computedHash, hash)
        }
        
        /**
         * перевіряє, чи хеш відповідає рядку
         *
         * @param data рядок
         * @param hash хеш у вигляді hex-рядка
         * @param charset кодування
         * @return true якщо хеш відповідає рядку
         */
        fun verifyString(data: String, hash: String, charset: java.nio.charset.Charset = StandardCharsets.UTF_8): Boolean {
            return try {
                val hashBytes = hexToBytes(hash)
                verify(data.toByteArray(charset), hashBytes)
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * базова реалізація хешувальника
     */
    open class BaseHasher(private val algorithm: String) : Hasher {
        override fun hash(data: ByteArray): ByteArray {
            val messageDigest = MessageDigest.getInstance(algorithm)
            return messageDigest.digest(data)
        }
    }
    
    /**
     * представлення SHA-256 хешувальника
     */
    class SHA256Hasher : BaseHasher(ALGORITHM_SHA256)
    
    /**
     * представлення SHA-512 хешувальника
     */
    class SHA512Hasher : BaseHasher(ALGORITHM_SHA512)
    
    /**
     * представлення MD5 хешувальника
     */
    class MD5Hasher : BaseHasher(ALGORITHM_MD5)
    
    /**
     * представлення HMAC хешувальника
     */
    class HMACHasher(private val algorithm: String, private val key: ByteArray) : Hasher {
        override fun hash(data: ByteArray): ByteArray {
            val secretKeySpec = SecretKeySpec(key, algorithm)
            val mac = Mac.getInstance(algorithm)
            mac.init(secretKeySpec)
            return mac.doFinal(data)
        }
    }
    
    /**
     * представлення PBKDF2 хешувальника
     */
    class PBKDF2Hasher(
        private val iterations: Int = PBKDF2_ITERATIONS,
        private val keyLength: Int = PBKDF2_KEY_LENGTH
    ) : Hasher {
        override fun hash(data: ByteArray): ByteArray {
            val salt = generateSalt(DEFAULT_SALT_LENGTH)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(
                String(data, StandardCharsets.UTF_8).toCharArray(),
                salt,
                iterations,
                keyLength
            )
            val secretKey = factory.generateSecret(spec)
            return salt + secretKey.encoded
        }
        
        /**
         * перевіряє, чи хеш відповідає паролю
         *
         * @param password пароль
         * @param hash хеш
         * @return true якщо хеш відповідає паролю
         */
        fun verifyPassword(password: String, hash: ByteArray): Boolean {
            if (hash.size < DEFAULT_SALT_LENGTH) return false
            
            val salt = hash.copyOfRange(0, DEFAULT_SALT_LENGTH)
            val storedHash = hash.copyOfRange(DEFAULT_SALT_LENGTH, hash.size)
            
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength)
            val secretKey = factory.generateSecret(spec)
            val computedHash = secretKey.encoded
            
            return MessageDigest.isEqual(computedHash, storedHash)
        }
    }
    
    /**
     * створює SHA-256 хешувальник
     *
     * @return SHA-256 хешувальник
     */
    fun createSHA256Hasher(): SHA256Hasher {
        return SHA256Hasher()
    }
    
    /**
     * створює SHA-512 хешувальник
     *
     * @return SHA-512 хешувальник
     */
    fun createSHA512Hasher(): SHA512Hasher {
        return SHA512Hasher()
    }
    
    /**
     * створює MD5 хешувальник
     *
     * @return MD5 хешувальник
     */
    fun createMD5Hasher(): MD5Hasher {
        return MD5Hasher()
    }
    
    /**
     * створює HMAC хешувальник
     *
     * @param algorithm алгоритм
     * @param key ключ
     * @return HMAC хешувальник
     */
    fun createHMACHasher(algorithm: String, key: ByteArray): HMACHasher {
        return HMACHasher(algorithm, key)
    }
    
    /**
     * створює PBKDF2 хешувальник
     *
     * @param iterations кількість ітерацій
     * @param keyLength довжина ключа
     * @return PBKDF2 хешувальник
     */
    fun createPBKDF2Hasher(iterations: Int = PBKDF2_ITERATIONS, keyLength: Int = PBKDF2_KEY_LENGTH): PBKDF2Hasher {
        return PBKDF2Hasher(iterations, keyLength)
    }
    
    // функції для роботи з шифруванням
    
    /**
     * представлення шифрувальника
     */
    interface Encryptor {
        /**
         * шифрує дані
         *
         * @param data дані
         * @return зашифровані дані
         */
        fun encrypt(data: ByteArray): ByteArray
        
        /**
         * розшифровує дані
         *
         * @param data зашифровані дані
         * @return розшифровані дані
         */
        fun decrypt(data: ByteArray): ByteArray
        
        /**
         * шифрує рядок
         *
         * @param data рядок
         * @param charset кодування
         * @return зашифровані дані у вигляді base64
         */
        fun encryptString(data: String, charset: java.nio.charset.Charset = StandardCharsets.UTF_8): String {
            val encryptedBytes = encrypt(data.toByteArray(charset))
            return Base64.getEncoder().encodeToString(encryptedBytes)
        }
        
        /**
         * розшифровує рядок
         *
         * @param data зашифровані дані у вигляді base64
         * @param charset кодування
         * @return розшифрований рядок
         */
        fun decryptString(data: String, charset: java.nio.charset.Charset = StandardCharsets.UTF_8): String {
            val encryptedBytes = Base64.getDecoder().decode(data)
            val decryptedBytes = decrypt(encryptedBytes)
            return String(decryptedBytes, charset)
        }
    }
    
    /**
     * представлення AES шифрувальника
     */
    class AESEncryptor(private val key: ByteArray) : Encryptor {
        private val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        private val secretKeySpec = SecretKeySpec(key, ALGORITHM_AES)
        
        override fun encrypt(data: ByteArray): ByteArray {
            val iv = generateIV()
            val ivParameterSpec = IvParameterSpec(iv)
            
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
            val encryptedData = cipher.doFinal(data)
            
            // додаємо IV на початок зашифрованих даних
            return iv + encryptedData
        }
        
        override fun decrypt(data: ByteArray): ByteArray {
            if (data.size < DEFAULT_IV_LENGTH) {
                throw SecurityException("Недійсні зашифровані дані")
            }
            
            val iv = data.copyOfRange(0, DEFAULT_IV_LENGTH)
            val encryptedData = data.copyOfRange(DEFAULT_IV_LENGTH, data.size)
            
            val ivParameterSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
            
            return cipher.doFinal(encryptedData)
        }
    }
    
    /**
     * представлення RSA шифрувальника
     */
    class RSAEncryptor(private val publicKey: PublicKey, private val privateKey: PrivateKey) : Encryptor {
        private val encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        private val decryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        
        override fun encrypt(data: ByteArray): ByteArray {
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey)
            return encryptCipher.doFinal(data)
        }
        
        override fun decrypt(data: ByteArray): ByteArray {
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey)
            return decryptCipher.doFinal(data)
        }
    }
    
    /**
     * представлення гібридного шифрувальника (AES + RSA)
     */
    class HybridEncryptor(private val rsaEncryptor: RSAEncryptor) : Encryptor {
        override fun encrypt(data: ByteArray): ByteArray {
            // генеруємо випадковий AES ключ
            val aesKey = generateAESKey()
            
            // шифруємо дані AES ключем
            val aesEncryptor = AESEncryptor(aesKey.encoded)
            val encryptedData = aesEncryptor.encrypt(data)
            
            // шифруємо AES ключ RSA ключем
            val encryptedKey = rsaEncryptor.encrypt(aesKey.encoded)
            
            // додаємо зашифрований ключ на початок зашифрованих даних
            return encryptedKey.size.toByteArray() + encryptedKey + encryptedData
        }
        
        override fun decrypt(data: ByteArray): ByteArray {
            if (data.size < 4) {
                throw SecurityException("Недійсні зашифровані дані")
            }
            
            // отримуємо розмір зашифрованого ключа
            val keySize = ByteArray(4).let { bytes ->
                bytes[0] = data[0]
                bytes[1] = data[1]
                bytes[2] = data[2]
                bytes[3] = data[3]
                bytes.toInt()
            }
            
            if (data.size < 4 + keySize) {
                throw SecurityException("Недійсні зашифровані дані")
            }
            
            // отримуємо зашифрований ключ
            val encryptedKey = data.copyOfRange(4, 4 + keySize)
            val encryptedData = data.copyOfRange(4 + keySize, data.size)
            
            // розшифровуємо AES ключ
            val decryptedKey = rsaEncryptor.decrypt(encryptedKey)
            
            // розшифровуємо дані AES ключем
            val aesEncryptor = AESEncryptor(decryptedKey)
            return aesEncryptor.decrypt(encryptedData)
        }
        
        /**
         * перетворює Int у ByteArray
         *
         * @return ByteArray
         */
        private fun Int.toByteArray(): ByteArray {
            return byteArrayOf(
                (this shr 24).toByte(),
                (this shr 16).toByte(),
                (this shr 8).toByte(),
                this.toByte()
            )
        }
        
        /**
         * перетворює ByteArray у Int
         *
         * @return Int
         */
        private fun ByteArray.toInt(): Int {
            return ((this[0].toInt() and 0xFF) shl 24) or
                   ((this[1].toInt() and 0xFF) shl 16) or
                   ((this[2].toInt() and 0xFF) shl 8) or
                   (this[3].toInt() and 0xFF)
        }
    }
    
    /**
     * створює AES шифрувальник
     *
     * @param key ключ
     * @return AES шифрувальник
     */
    fun createAESEncryptor(key: ByteArray): AESEncryptor {
        return AESEncryptor(key)
    }
    
    /**
     * створює RSA шифрувальник
     *
     * @param publicKey відкритий ключ
     * @param privateKey приватний ключ
     * @return RSA шифрувальник
     */
    fun createRSAEncryptor(publicKey: PublicKey, privateKey: PrivateKey): RSAEncryptor {
        return RSAEncryptor(publicKey, privateKey)
    }
    
    /**
     * створює гібридний шифрувальник
     *
     * @param rsaEncryptor RSA шифрувальник
     * @return гібридний шифрувальник
     */
    fun createHybridEncryptor(rsaEncryptor: RSAEncryptor): HybridEncryptor {
        return HybridEncryptor(rsaEncryptor)
    }
    
    // функції для роботи з генерацією ключів
    
    /**
     * представлення генератора ключів
     */
    interface KeyGenerator {
        /**
         * генерує ключ
         *
         * @return ключ
         */
        fun generateKey(): ByteArray
    }
    
    /**
     * представлення генератора AES ключів
     */
    class AESKeyGenerator : KeyGenerator {
        private val keyGenerator = javax.crypto.KeyGenerator.getInstance(ALGORITHM_AES)
        
        init {
            keyGenerator.init(256) // 256 бітний ключ
        }
        
        override fun generateKey(): ByteArray {
            return keyGenerator.generateKey().encoded
        }
    }
    
    /**
     * представлення генератора RSA ключів
     *
     * @property keySize розмір ключа
     */
    class RSAKeyGenerator(private val keySize: Int = RSA_KEY_SIZE_2048) : KeyGenerator {
        private val keyPairGenerator = java.security.KeyPairGenerator.getInstance(ALGORITHM_RSA)
        
        init {
            keyPairGenerator.initialize(keySize)
        }
        
        override fun generateKey(): ByteArray {
            throw UnsupportedOperationException("Для RSA потрібно генерувати пару ключів")
        }
        
        /**
         * генерує пару ключів
         *
         * @return пара ключів
         */
        fun generateKeyPair(): KeyPair {
            return keyPairGenerator.generateKeyPair()
        }
    }
    
    /**
     * представлення генератора солі
     */
    class SaltGenerator(private val length: Int = DEFAULT_SALT_LENGTH) : KeyGenerator {
        override fun generateKey(): ByteArray {
            return generateSalt(length)
        }
    }
    
    /**
     * представлення генератора вектора ініціалізації
     */
    class IVGenerator(private val length: Int = DEFAULT_IV_LENGTH) : KeyGenerator {
        override fun generateKey(): ByteArray {
            return generateIV()
        }
    }
    
    /**
     * створює генератор AES ключів
     *
     * @return генератор AES ключів
     */
    fun createAESKeyGenerator(): AESKeyGenerator {
        return AESKeyGenerator()
    }
    
    /**
     * створює генератор RSA ключів
     *
     * @param keySize розмір ключа
     * @return генератор RSA ключів
     */
    fun createRSAKeyGenerator(keySize: Int = RSA_KEY_SIZE_2048): RSAKeyGenerator {
        return RSAKeyGenerator(keySize)
    }
    
    /**
     * створює генератор солі
     *
     * @param length довжина солі
     * @return генератор солі
     */
    fun createSaltGenerator(length: Int = DEFAULT_SALT_LENGTH): SaltGenerator {
        return SaltGenerator(length)
    }
    
    /**
     * створює генератор вектора ініціалізації
     *
     * @param length довжина вектора
     * @return генератор вектора ініціалізації
     */
    fun createIVGenerator(length: Int = DEFAULT_IV_LENGTH): IVGenerator {
        return IVGenerator(length)
    }
    
    // функції для роботи з цифровими підписами
    
    /**
     * представлення підписувальника
     */
    interface Signer {
        /**
         * створює підпис
         *
         * @param data дані
         * @return підпис
         */
        fun sign(data: ByteArray): ByteArray
        
        /**
         * перевіряє підпис
         *
         * @param data дані
         * @param signature підпис
         * @return true якщо підпис дійсний
         */
        fun verify(data: ByteArray, signature: ByteArray): Boolean
        
        /**
         * створює підпис для рядка
         *
         * @param data рядок
         * @param charset кодування
         * @return підпис у вигляді base64
         */
        fun signString(data: String, charset: java.nio.charset.Charset = StandardCharsets.UTF_8): String {
            val signature = sign(data.toByteArray(charset))
            return Base64.getEncoder().encodeToString(signature)
        }
        
        /**
         * перевіряє підпис для рядка
         *
         * @param data рядок
         * @param signature підпис у вигляді base64
         * @param charset кодування
         * @return true якщо підпис дійсний
         */
        fun verifyString(data: String, signature: String, charset: java.nio.charset.Charset = StandardCharsets.UTF_8): Boolean {
            return try {
                val signatureBytes = Base64.getDecoder().decode(signature)
                verify(data.toByteArray(charset), signatureBytes)
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * представлення RSA підписувальника
     */
    class RSASigner(private val privateKey: PrivateKey, private val publicKey: PublicKey) : Signer {
        private val signSignature = Signature.getInstance("SHA256withRSA")
        private val verifySignature = Signature.getInstance("SHA256withRSA")
        
        override fun sign(data: ByteArray): ByteArray {
            signSignature.initSign(privateKey)
            signSignature.update(data)
            return signSignature.sign()
        }
        
        override fun verify(data: ByteArray, signature: ByteArray): Boolean {
            verifySignature.initVerify(publicKey)
            verifySignature.update(data)
            return verifySignature.verify(signature)
        }
    }
    
    /**
     * представлення HMAC підписувальника
     */
    class HMACSigner(private val key: ByteArray) : Signer {
        private val mac = Mac.getInstance(ALGORITHM_HMAC_SHA256)
        private val secretKeySpec = SecretKeySpec(key, ALGORITHM_HMAC_SHA256)
        
        init {
            mac.init(secretKeySpec)
        }
        
        override fun sign(data: ByteArray): ByteArray {
            return mac.doFinal(data)
        }
        
        override fun verify(data: ByteArray, signature: ByteArray): Boolean {
            val computedSignature = mac.doFinal(data)
            return MessageDigest.isEqual(computedSignature, signature)
        }
    }
    
    /**
     * створює RSA підписувальника
     *
     * @param privateKey приватний ключ
     * @param publicKey відкритий ключ
     * @return RSA підписувальник
     */
    fun createRSASigner(privateKey: PrivateKey, publicKey: PublicKey): RSASigner {
        return RSASigner(privateKey, publicKey)
    }
    
    /**
     * створює HMAC підписувальника
     *
     * @param key ключ
     * @return HMAC підписувальник
     */
    fun createHMACSigner(key: ByteArray): HMACSigner {
        return HMACSigner(key)
    }
    
    // функції для роботи з сертифікатами
    
    /**
     * представлення менеджера сертифікатів
     */
    class CertificateManager {
        
        /**
         * завантажує сертифікат з файлу
         *
         * @param filePath шлях до файлу
         * @return сертифікат
         */
        fun loadCertificateFromFile(filePath: String): java.security.cert.Certificate? {
            return try {
                val certificateFactory = java.security.cert.CertificateFactory.getInstance("X.509")
                FileInputStream(filePath).use { inputStream ->
                    certificateFactory.generateCertificate(inputStream)
                }
            } catch (e: Exception) {
                null
            }
        }
        
        /**
         * завантажує сертифікат з рядка
         *
         * @param certificateString рядок сертифіката
         * @return сертифікат
         */
        fun loadCertificateFromString(certificateString: String): java.security.cert.Certificate? {
            return try {
                val certificateFactory = java.security.cert.CertificateFactory.getInstance("X.509")
                val certificateBytes = Base64.getDecoder().decode(certificateString)
                val inputStream = ByteArrayInputStream(certificateBytes)
                certificateFactory.generateCertificate(inputStream)
            } catch (e: Exception) {
                null
            }
        }
        
        /**
         * зберігає сертифікат у файл
         *
         * @param certificate сертифікат
         * @param filePath шлях до файлу
         * @return true якщо збереження вдалося
         */
        fun saveCertificateToFile(certificate: java.security.cert.Certificate, filePath: String): Boolean {
            return try {
                FileOutputStream(filePath).use { outputStream ->
                    outputStream.write(certificate.encoded)
                }
                true
            } catch (e: Exception) {
                false
            }
        }
        
        /**
         * отримує сертифікат у вигляді рядка
         *
         * @param certificate сертифікат
         * @return сертифікат у вигляді рядка
         */
        fun getCertificateString(certificate: java.security.cert.Certificate): String {
            return try {
                Base64.getEncoder().encodeToString(certificate.encoded)
            } catch (e: Exception) {
                ""
            }
        }
        
        /**
         * перевіряє дійсність сертифіката
         *
         * @param certificate сертифікат
         * @return true якщо сертифікат дійсний
         */
        fun isCertificateValid(certificate: java.security.cert.Certificate): Boolean {
            return try {
                (certificate as java.security.cert.X509Certificate).checkValidity()
                true
            } catch (e: Exception) {
                false
            }
        }
        
        /**
         * отримує інформацію про сертифікат
         *
         * @param certificate сертифікат
         * @return інформація про сертифікат
         */
        fun getCertificateInfo(certificate: java.security.cert.Certificate): Map<String, String> {
            return try {
                val x509Certificate = certificate as java.security.cert.X509Certificate
                mapOf(
                    "subject" to x509Certificate.subjectDN.name,
                    "issuer" to x509Certificate.issuerDN.name,
                    "serialNumber" to x509Certificate.serialNumber.toString(),
                    "notBefore" to x509Certificate.notBefore.toString(),
                    "notAfter" to x509Certificate.notAfter.toString(),
                    "signatureAlgorithm" to x509Certificate.sigAlgName
                )
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }
    
    /**
     * створює менеджер сертифікатів
     *
     * @return менеджер сертифікатів
     */
    fun createCertificateManager(): CertificateManager {
        return CertificateManager()
    }
    
    // функції для роботи з безпечним зберіганням
    
    /**
     * представлення безпечного сховища
     */
    class SecureStorage {
        private val storage = mutableMapOf<String, ByteArray>()
        private val storageLock = Any()
        
        /**
         * зберігає дані
         *
         * @param key ключ
         * @param data дані
         */
        fun store(key: String, data: ByteArray) {
            synchronized(storageLock) {
                storage[key] = data.copyOf()
            }
        }
        
        /**
         * отримує дані
         *
         * @param key ключ
         * @return дані або null якщо не знайдено
         */
        fun retrieve(key: String): ByteArray? {
            synchronized(storageLock) {
                return storage[key]?.copyOf()
            }
        }
        
        /**
         * видаляє дані
         *
         * @param key ключ
         * @return true якщо дані були видалені
         */
        fun remove(key: String): Boolean {
            synchronized(storageLock) {
                return storage.remove(key) != null
            }
        }
        
        /**
         * перевіряє, чи існує ключ
         *
         * @param key ключ
         * @return true якщо ключ існує
         */
        fun contains(key: String): Boolean {
            synchronized(storageLock) {
                return storage.containsKey(key)
            }
        }
        
        /**
         * очищує сховище
         */
        fun clear() {
            synchronized(storageLock) {
                storage.clear()
            }
        }
        
        /**
         * отримує кількість елементів
         *
         * @return кількість елементів
         */
        fun size(): Int {
            synchronized(storageLock) {
                return storage.size
            }
        }
    }
    
    /**
     * представлення зашифрованого сховища
     */
    class EncryptedStorage(private val encryptor: Encryptor) : SecureStorage() {
        private val encryptedStorage = mutableMapOf<String, ByteArray>()
        private val storageLock = Any()
        
        override fun store(key: String, data: ByteArray) {
            synchronized(storageLock) {
                val encryptedData = encryptor.encrypt(data)
                encryptedStorage[key] = encryptedData
            }
        }
        
        override fun retrieve(key: String): ByteArray? {
            synchronized(storageLock) {
                val encryptedData = encryptedStorage[key]
                return encryptedData?.let { encryptor.decrypt(it) }
            }
        }
        
        override fun remove(key: String): Boolean {
            synchronized(storageLock) {
                return encryptedStorage.remove(key) != null
            }
        }
        
        override fun contains(key: String): Boolean {
            synchronized(storageLock) {
                return encryptedStorage.containsKey(key)
            }
        }
        
        override fun clear() {
            synchronized(storageLock) {
                encryptedStorage.clear()
            }
        }
        
        override fun size(): Int {
            synchronized(storageLock) {
                return encryptedStorage.size
            }
        }
    }
    
    /**
     * створює безпечне сховище
     *
     * @return безпечне сховище
     */
    fun createSecureStorage(): SecureStorage {
        return SecureStorage()
    }
    
    /**
     * створює зашифроване сховище
     *
     * @param encryptor шифрувальник
     * @return зашифроване сховище
     */
    fun createEncryptedStorage(encryptor: Encryptor): EncryptedStorage {
        return EncryptedStorage(encryptor)
    }
    
    // функції для роботи з безпечними токенами
    
    /**
     * представлення токена
     *
     * @property id ідентифікатор токена
     * @property payload корисне навантаження
     * @property expirationDate дата закінчення терміну дії
     * @property issuer видавець
     * @property audience аудиторія
     */
    data class Token(
        val id: String,
        val payload: Map<String, Any>,
        val expirationDate: Long,
        val issuer: String,
        val audience: String
    ) {
        /**
         * перевіряє, чи токен дійсний
         *
         * @return true якщо токен дійсний
         */
        fun isValid(): Boolean {
            return System.currentTimeMillis() < expirationDate
        }
        
        /**
         * перевіряє, чи токен прострочений
         *
         * @return true якщо токен прострочений
         */
        fun isExpired(): Boolean {
            return !isValid()
        }
    }
    
    /**
     * представлення генератора токенів
     */
    class TokenGenerator(private val signer: Signer) {
        private val random = SecureRandom()
        
        /**
         * генерує токен
         *
         * @param payload корисне навантаження
         * @param expirationTime термін дії в мілісекундах
         * @param issuer видавець
         * @param audience аудиторія
         * @return токен
         */
        fun generateToken(
            payload: Map<String, Any>,
            expirationTime: Long,
            issuer: String,
            audience: String
        ): Token {
            val id = generateTokenId()
            val expirationDate = System.currentTimeMillis() + expirationTime
            return Token(id, payload, expirationDate, issuer, audience)
        }
        
        /**
         * підписує токен
         *
         * @param token токен
         * @return підписаний токен у вигляді рядка
         */
        fun signToken(token: Token): String {
            val tokenString = tokenToString(token)
            return signer.signString(tokenString)
        }
        
        /**
         * перевіряє підпис токена
         *
         * @param tokenString рядок токена
         * @param signature підпис
         * @return true якщо підпис дійсний
         */
        fun verifyToken(tokenString: String, signature: String): Boolean {
            return signer.verifyString(tokenString, signature)
        }
        
        /**
         * генерує ідентифікатор токена
         *
         * @return ідентифікатор токена
         */
        private fun generateTokenId(): String {
            val bytes = ByteArray(16)
            random.nextBytes(bytes)
            return bytesToHex(bytes)
        }
        
        /**
         * перетворює токен у рядок
         *
         * @param token токен
         * @return рядок токена
         */
        private fun tokenToString(token: Token): String {
            return "${token.id}|${token.payload}|${token.expirationDate}|${token.issuer}|${token.audience}"
        }
    }
    
    /**
     * представлення менеджера токенів
     */
    class TokenManager(private val signer: Signer) {
        private val tokenGenerator = TokenGenerator(signer)
        private val validTokens = mutableMapOf<String, Token>()
        private val tokenLock = Any()
        
        /**
         * створює токен
         *
         * @param payload корисне навантаження
         * @param expirationTime термін дії в мілісекундах
         * @param issuer видавець
         * @param audience аудиторія
         * @return пара (токен, підпис)
         */
        fun createToken(
            payload: Map<String, Any>,
            expirationTime: Long,
            issuer: String,
            audience: String
        ): Pair<Token, String> {
            val token = tokenGenerator.generateToken(payload, expirationTime, issuer, audience)
            val signature = tokenGenerator.signToken(token)
            
            synchronized(tokenLock) {
                validTokens[token.id] = token
            }
            
            return Pair(token, signature)
        }
        
        /**
         * перевіряє токен
         *
         * @param token токен
         * @param signature підпис
         * @return true якщо токен дійсний
         */
        fun validateToken(token: Token, signature: String): Boolean {
            if (token.isExpired()) return false
            
            val tokenString = tokenToString(token)
            if (!tokenGenerator.verifyToken(tokenString, signature)) return false
            
            synchronized(tokenLock) {
                val storedToken = validTokens[token.id]
                return storedToken != null && storedToken.expirationDate == token.expirationDate
            }
        }
        
        /**
         * відклик
