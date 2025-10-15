/**
 * утиліти для криптографії
 *
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.security.*
import java.security.spec.*
import javax.crypto.*
import javax.crypto.spec.*
import java.util.*
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.security.KeyFactory
import java.security.Signature

/**
 * представлення інтерфейсу для хешування
 */
interface HashHelper {
    /**
     * обчислити MD5 хеш
     *
     * @param data дані
     * @return хеш
     */
    fun md5(data: ByteArray): ByteArray
    
    /**
     * обчислити SHA-1 хеш
     *
     * @param data дані
     * @return хеш
     */
    fun sha1(data: ByteArray): ByteArray
    
    /**
     * обчислити SHA-256 хеш
     *
     * @param data дані
     * @return хеш
     */
    fun sha256(data: ByteArray): ByteArray
    
    /**
     * обчислити SHA-512 хеш
     *
     * @param data дані
     * @return хеш
     */
    fun sha512(data: ByteArray): ByteArray
    
    /**
     * обчислити хеш з використанням алгоритму
     *
     * @param data дані
     * @param algorithm алгоритм
     * @return хеш
     */
    fun hash(data: ByteArray, algorithm: String): ByteArray
}

/**
 * представлення базової реалізації помічника з хешуванням
 */
open class BaseHashHelper : HashHelper {
    
    override fun md5(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("MD5").digest(data)
    }
    
    override fun sha1(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("SHA-1").digest(data)
    }
    
    override fun sha256(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(data)
    }
    
    override fun sha512(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("SHA-512").digest(data)
    }
    
    override fun hash(data: ByteArray, algorithm: String): ByteArray {
        return MessageDigest.getInstance(algorithm).digest(data)
    }
}

/**
 * представлення інтерфейсу для симетричного шифрування
 */
interface SymmetricEncryptionHelper {
    /**
     * зашифрувати дані
     *
     * @param data дані
     * @param key ключ
     * @param algorithm алгоритм
     * @return зашифровані дані
     */
    fun encrypt(data: ByteArray, key: ByteArray, algorithm: String = "AES"): ByteArray
    
    /**
     * розшифрувати дані
     *
     * @param data зашифровані дані
     * @param key ключ
     * @param algorithm алгоритм
     * @return розшифровані дані
     */
    fun decrypt(data: ByteArray, key: ByteArray, algorithm: String = "AES"): ByteArray
    
    /**
     * згенерувати ключ
     *
     * @param algorithm алгоритм
     * @param keySize розмір ключа
     * @return ключ
     */
    fun generateKey(algorithm: String = "AES", keySize: Int = 256): ByteArray
}

/**
 * представлення базової реалізації помічника з симетричним шифруванням
 */
open class BaseSymmetricEncryptionHelper : SymmetricEncryptionHelper {
    
    override fun encrypt(data: ByteArray, key: ByteArray, algorithm: String): ByteArray {
        val secretKey = SecretKeySpec(key, algorithm)
        val cipher = Cipher.getInstance("$algorithm/CBC/PKCS5Padding")
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val encryptedData = cipher.doFinal(data)
        return iv + encryptedData
    }
    
    override fun decrypt(data: ByteArray, key: ByteArray, algorithm: String): ByteArray {
        val secretKey = SecretKeySpec(key, algorithm)
        val cipher = Cipher.getInstance("$algorithm/CBC/PKCS5Padding")
        val iv = data.copyOfRange(0, 16)
        val ivSpec = IvParameterSpec(iv)
        val encryptedData = data.copyOfRange(16, data.size)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        return cipher.doFinal(encryptedData)
    }
    
    override fun generateKey(algorithm: String, keySize: Int): ByteArray {
        val keyGen = KeyGenerator.getInstance(algorithm)
        keyGen.init(keySize)
        return keyGen.generateKey().encoded
    }
}

/**
 * представлення інтерфейсу для асиметричного шифрування
 */
interface AsymmetricEncryptionHelper {
    /**
     * зашифрувати дані публічним ключем
     *
     * @param data дані
     * @param publicKey публічний ключ
     * @param algorithm алгоритм
     * @return зашифровані дані
     */
    fun encryptWithPublicKey(data: ByteArray, publicKey: PublicKey, algorithm: String = "RSA"): ByteArray
    
    /**
     * розшифрувати дані приватним ключем
     *
     * @param data зашифровані дані
     * @param privateKey приватний ключ
     * @param algorithm алгоритм
     * @return розшифровані дані
     */
    fun decryptWithPrivateKey(data: ByteArray, privateKey: PrivateKey, algorithm: String = "RSA"): ByteArray
    
    /**
     * зашифрувати дані приватним ключем
     *
     * @param data дані
     * @param privateKey приватний ключ
     * @param algorithm алгоритм
     * @return зашифровані дані
     */
    fun encryptWithPrivateKey(data: ByteArray, privateKey: PrivateKey, algorithm: String = "RSA"): ByteArray
    
    /**
     * розшифрувати дані публічним ключем
     *
     * @param data зашифровані дані
     * @param publicKey публічний ключ
     * @param algorithm алгоритм
     * @return розшифровані дані
     */
    fun decryptWithPublicKey(data: ByteArray, publicKey: PublicKey, algorithm: String = "RSA"): ByteArray
    
    /**
     * згенерувати пару ключів
     *
     * @param algorithm алгоритм
     * @param keySize розмір ключа
     * @return пара ключів
     */
    fun generateKeyPair(algorithm: String = "RSA", keySize: Int = 2048): KeyPair
}

/**
 * представлення базової реалізації помічника з асиметричним шифруванням
 */
open class BaseAsymmetricEncryptionHelper : AsymmetricEncryptionHelper {
    
    override fun encryptWithPublicKey(data: ByteArray, publicKey: PublicKey, algorithm: String): ByteArray {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(data)
    }
    
    override fun decryptWithPrivateKey(data: ByteArray, privateKey: PrivateKey, algorithm: String): ByteArray {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return cipher.doFinal(data)
    }
    
    override fun encryptWithPrivateKey(data: ByteArray, privateKey: PrivateKey, algorithm: String): ByteArray {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, privateKey)
        return cipher.doFinal(data)
    }
    
    override fun decryptWithPublicKey(data: ByteArray, publicKey: PublicKey, algorithm: String): ByteArray {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, publicKey)
        return cipher.doFinal(data)
    }
    
    override fun generateKeyPair(algorithm: String, keySize: Int): KeyPair {
        val keyGen = KeyPairGenerator.getInstance(algorithm)
        keyGen.initialize(keySize)
        return keyGen.generateKeyPair()
    }
}

/**
 * представлення інтерфейсу для цифрових підписів
 */
interface DigitalSignatureHelper {
    /**
     * створити цифровий підпис
     *
     * @param data дані
     * @param privateKey приватний ключ
     * @param algorithm алгоритм
     * @return підпис
     */
    fun sign(data: ByteArray, privateKey: PrivateKey, algorithm: String = "SHA256withRSA"): ByteArray
    
    /**
     * перевірити цифровий підпис
     *
     * @param data дані
     * @param signature підпис
     * @param publicKey публічний ключ
     * @param algorithm алгоритм
     * @return true, якщо підпис дійсний
     */
    fun verify(data: ByteArray, signature: ByteArray, publicKey: PublicKey, algorithm: String = "SHA256withRSA"): Boolean
    
    /**
     * згенерувати пару ключів для підпису
     *
     * @param algorithm алгоритм
     * @param keySize розмір ключа
     * @return пара ключів
     */
    fun generateSignatureKeyPair(algorithm: String = "RSA", keySize: Int = 2048): KeyPair
}

/**
 * представлення базової реалізації помічника з цифровими підписами
 */
open class BaseDigitalSignatureHelper : DigitalSignatureHelper {
    
    override fun sign(data: ByteArray, privateKey: PrivateKey, algorithm: String): ByteArray {
        val signature = Signature.getInstance(algorithm)
        signature.initSign(privateKey)
        signature.update(data)
        return signature.sign()
    }
    
    override fun verify(data: ByteArray, signature: ByteArray, publicKey: PublicKey, algorithm: String): Boolean {
        val sig = Signature.getInstance(algorithm)
        sig.initVerify(publicKey)
        sig.update(data)
        return sig.verify(signature)
    }
    
    override fun generateSignatureKeyPair(algorithm: String, keySize: Int): KeyPair {
        val keyGen = KeyPairGenerator.getInstance(algorithm)
        keyGen.initialize(keySize)
        return keyGen.generateKeyPair()
    }
}

/**
 * представлення інтерфейсу для кодування/декодування
 */
interface EncodingHelper {
    /**
     * закодувати в Base64
     *
     * @param data дані
     * @return закодовані дані
     */
    fun encodeBase64(data: ByteArray): String
    
    /**
     * декодувати з Base64
     *
     * @param data закодовані дані
     * @return розкодовані дані
     */
    fun decodeBase64(data: String): ByteArray
    
    /**
     * закодувати в Hex
     *
     * @param data дані
     * @return закодовані дані
     */
    fun encodeHex(data: ByteArray): String
    
    /**
     * декодувати з Hex
     *
     * @param data закодовані дані
     * @return розкодовані дані
     */
    fun decodeHex(data: String): ByteArray
    
    /**
     * закодувати в URL-safe Base64
     *
     * @param data дані
     * @return закодовані дані
     */
    fun encodeBase64Url(data: ByteArray): String
    
    /**
     * декодувати з URL-safe Base64
     *
     * @param data закодовані дані
     * @return розкодовані дані
     */
    fun decodeBase64Url(data: String): ByteArray
}

/**
 * представлення базової реалізації помічника з кодуванням
 */
open class BaseEncodingHelper : EncodingHelper {
    
    override fun encodeBase64(data: ByteArray): String {
        return Base64.getEncoder().encodeToString(data)
    }
    
    override fun decodeBase64(data: String): ByteArray {
        return Base64.getDecoder().decode(data)
    }
    
    override fun encodeHex(data: ByteArray): String {
        val hexChars = "0123456789abcdef"
        val result = StringBuilder(data.size * 2)
        for (b in data) {
            val v = b.toInt() and 0xFF
            result.append(hexChars[v ushr 4])
            result.append(hexChars[v and 0x0F])
        }
        return result.toString()
    }
    
    override fun decodeHex(data: String): ByteArray {
        val len = data.length
        val result = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            result[i / 2] = ((Character.digit(data[i], 16) shl 4)
                    + Character.digit(data[i + 1], 16)).toByte()
        }
        return result
    }
    
    override fun encodeBase64Url(data: ByteArray): String {
        return Base64.getUrlEncoder().encodeToString(data)
    }
    
    override fun decodeBase64Url(data: String): ByteArray {
        return Base64.getUrlDecoder().decode(data)
    }
}

/**
 * представлення інтерфейсу для HMAC
 */
interface HmacHelper {
    /**
     * обчислити HMAC-SHA256
     *
     * @param data дані
     * @param key ключ
     * @return HMAC
     */
    fun hmacSha256(data: ByteArray, key: ByteArray): ByteArray
    
    /**
     * обчислити HMAC-SHA512
     *
     * @param data дані
     * @param key ключ
     * @return HMAC
     */
    fun hmacSha512(data: ByteArray, key: ByteArray): ByteArray
    
    /**
     * обчислити HMAC з використанням алгоритму
     *
     * @param data дані
     * @param key ключ
     * @param algorithm алгоритм
     * @return HMAC
     */
    fun hmac(data: ByteArray, key: ByteArray, algorithm: String): ByteArray
}

/**
 * представлення базової реалізації помічника з HMAC
 */
open class BaseHmacHelper : HmacHelper {
    
    override fun hmacSha256(data: ByteArray, key: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKey)
        return mac.doFinal(data)
    }
    
    override fun hmacSha512(data: ByteArray, key: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, "HmacSHA512")
        val mac = Mac.getInstance("HmacSHA512")
        mac.init(secretKey)
        return mac.doFinal(data)
    }
    
    override fun hmac(data: ByteArray, key: ByteArray, algorithm: String): ByteArray {
        val secretKey = SecretKeySpec(key, algorithm)
        val mac = Mac.getInstance(algorithm)
        mac.init(secretKey)
        return mac.doFinal(data)
    }
}

/**
 * представлення інтерфейсу для роботи з сертифікатами
 */
interface CertificateHelper {
    /**
     * згенерувати самопідписаний сертифікат
     *
     * @param keyPair пара ключів
     * @param dn розрізнювальне ім'я
     * @param validityDays термін дії в днях
     * @return сертифікат
     */
    fun generateSelfSignedCertificate(keyPair: KeyPair, dn: String, validityDays: Int = 365): ByteArray
    
    /**
     * перевірити сертифікат
     *
     * @param certificate сертифікат
     * @param publicKey публічний ключ
     * @return true, якщо сертифікат дійсний
     */
    fun verifyCertificate(certificate: ByteArray, publicKey: PublicKey): Boolean
    
    /**
     * отримати публічний ключ з сертифіката
     *
     * @param certificate сертифікат
     * @return публічний ключ
     */
    fun getPublicKeyFromCertificate(certificate: ByteArray): PublicKey
}

/**
 * представлення базової реалізації помічника з сертифікатами
 */
open class BaseCertificateHelper : CertificateHelper {
    
    override fun generateSelfSignedCertificate(keyPair: KeyPair, dn: String, validityDays: Int): ByteArray {
        // Заглушка для генерації самопідписаного сертифіката
        return ByteArray(0)
    }
    
    override fun verifyCertificate(certificate: ByteArray, publicKey: PublicKey): Boolean {
        // Заглушка для перевірки сертифіката
        return true
    }
    
    override fun getPublicKeyFromCertificate(certificate: ByteArray): PublicKey {
        // Заглушка для отримання публічного ключа з сертифіката
        throw UnsupportedOperationException("Не реалізовано")
    }
}

/**
 * представлення інтерфейсу для роботи з безпечними випадковими числами
 */
interface SecureRandomHelper {
    /**
     * згенерувати безпечне випадкове число
     *
     * @param length довжина в байтах
     * @return випадкові дані
     */
    fun generateSecureRandom(length: Int): ByteArray
    
    /**
     * згенерувати безпечне випадкове число в діапазоні
     *
     * @param min мінімальне значення
     * @param max максимальне значення
     * @return випадкове число
     */
    fun generateSecureRandomInt(min: Int, max: Int): Int
    
    /**
     * згенерувати безпечне випадкове число в діапазоні
     *
     * @param min мінімальне значення
     * @param max максимальне значення
     * @return випадкове число
     */
    fun generateSecureRandomLong(min: Long, max: Long): Long
}

/**
 * представлення базової реалізації помічника з безпечними випадковими числами
 */
open class BaseSecureRandomHelper : SecureRandomHelper {
    private val secureRandom = SecureRandom()
    
    override fun generateSecureRandom(length: Int): ByteArray {
        val bytes = ByteArray(length)
        secureRandom.nextBytes(bytes)
        return bytes
    }
    
    override fun generateSecureRandomInt(min: Int, max: Int): Int {
        return secureRandom.nextInt(max - min + 1) + min
    }
    
    override fun generateSecureRandomLong(min: Long, max: Long): Long {
        return (secureRandom.nextDouble() * (max - min + 1)).toLong() + min
    }
}

/**
 * представлення інтерфейсу для роботи з ключами
 */
interface KeyHelper {
    /**
     * зберегти приватний ключ
     *
     * @param privateKey приватний ключ
     * @return закодовані дані ключа
     */
    fun savePrivateKey(privateKey: PrivateKey): ByteArray
    
    /**
     * завантажити приватний ключ
     *
     * @param data закодовані дані ключа
     * @param algorithm алгоритм
     * @return приватний ключ
     */
    fun loadPrivateKey(data: ByteArray, algorithm: String): PrivateKey
    
    /**
     * зберегти публічний ключ
     *
     * @param publicKey публічний ключ
     * @return закодовані дані ключа
     */
    fun savePublicKey(publicKey: PublicKey): ByteArray
    
    /**
     * завантажити публічний ключ
     *
     * @param data закодовані дані ключа
     * @param algorithm алгоритм
     * @return публічний ключ
     */
    fun loadPublicKey(data: ByteArray, algorithm: String): PublicKey
    
    /**
     * згенерувати симетричний ключ
     *
     * @param algorithm алгоритм
     * @param keySize розмір ключа
     * @return ключ
     */
    fun generateSymmetricKey(algorithm: String = "AES", keySize: Int = 256): SecretKey
}

/**
 * представлення базової реалізації помічника з ключами
 */
open class BaseKeyHelper : KeyHelper {
    
    override fun savePrivateKey(privateKey: PrivateKey): ByteArray {
        return privateKey.encoded
    }
    
    override fun loadPrivateKey(data: ByteArray, algorithm: String): PrivateKey {
        val keyFactory = KeyFactory.getInstance(algorithm)
        val keySpec = PKCS8EncodedKeySpec(data)
        return keyFactory.generatePrivate(keySpec)
    }
    
    override fun savePublicKey(publicKey: PublicKey): ByteArray {
        return publicKey.encoded
    }
    
    override fun loadPublicKey(data: ByteArray, algorithm: String): PublicKey {
        val keyFactory = KeyFactory.getInstance(algorithm)
        val keySpec = X509EncodedKeySpec(data)
        return keyFactory.generatePublic(keySpec)
    }
    
    override fun generateSymmetricKey(algorithm: String, keySize: Int): SecretKey {
        val keyGen = KeyGenerator.getInstance(algorithm)
        keyGen.init(keySize)
        return keyGen.generateKey()
    }
}

/**
 * представлення інтерфейсу для роботи з безпечним зберіганням
 */
interface SecureStorageHelper {
    /**
     * зашифрувати та зберегти дані
     *
     * @param data дані
     * @param key ключ
     * @param filePath шлях до файлу
     */
    fun encryptAndSave(data: ByteArray, key: ByteArray, filePath: String)
    
    /**
     * завантажити та розшифрувати дані
     *
     * @param key ключ
     * @param filePath шлях до файлу
     * @return розшифровані дані
     */
    fun loadAndDecrypt(key: ByteArray, filePath: String): ByteArray
    
    /**
     * безпечно видалити файл
     *
     * @param filePath шлях до файлу
     */
    fun secureDelete(filePath: String)
}

/**
 * представлення базової реалізації помічника з безпечним зберіганням
 */
open class BaseSecureStorageHelper(
    private val encryptionHelper: SymmetricEncryptionHelper = BaseSymmetricEncryptionHelper()
) : SecureStorageHelper {
    
    override fun encryptAndSave(data: ByteArray, key: ByteArray, filePath: String) {
        val encryptedData = encryptionHelper.encrypt(data, key)
        java.io.File(filePath).writeBytes(encryptedData)
    }
    
    override fun loadAndDecrypt(key: ByteArray, filePath: String): ByteArray {
        val encryptedData = java.io.File(filePath).readBytes()
        return encryptionHelper.decrypt(encryptedData, key)
    }
    
    override fun secureDelete(filePath: String) {
        val file = java.io.File(filePath)
        if (file.exists()) {
            val length = file.length()
            val random = SecureRandom()
            for (i in 0..2) { // Перезаписати 3 рази
                val randomBytes = ByteArray(length.toInt())
                random.nextBytes(randomBytes)
                file.writeBytes(randomBytes)
            }
            file.delete()
        }
    }
}

/**
 * представлення інтерфейсу для роботи з безпечними паролями
 */
interface PasswordHelper {
    /**
     * хешувати пароль
     *
     * @param password пароль
     * @param salt сіль
     * @return хеш пароля
     */
    fun hashPassword(password: String, salt: ByteArray): ByteArray
    
    /**
     * перевірити пароль
     *
     * @param password пароль
     * @param salt сіль
     * @param hashedPassword хеш пароля
     * @return true, якщо пароль правильний
     */
    fun verifyPassword(password: String, salt: ByteArray, hashedPassword: ByteArray): Boolean
    
    /**
     * згенерувати сіль
     *
     * @param length довжина солі
     * @return сіль
     */
    fun generateSalt(length: Int = 16): ByteArray
    
    /**
     * згенерувати безпечний пароль
     *
     * @param length довжина пароля
     * @return пароль
     */
    fun generateSecurePassword(length: Int = 12): String
}

/**
 * представлення базової реалізації помічника з паролями
 */
open class BasePasswordHelper(
    private val hashHelper: HashHelper = BaseHashHelper(),
    private val randomHelper: SecureRandomHelper = BaseSecureRandomHelper()
) : PasswordHelper {
    
    override fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val passwordBytes = password.toByteArray()
        val combined = salt + passwordBytes
        return hashHelper.sha256(combined)
    }
    
    override fun verifyPassword(password: String, salt: ByteArray, hashedPassword: ByteArray): Boolean {
        val computedHash = hashPassword(password, salt)
        return computedHash.contentEquals(hashedPassword)
    }
    
    override fun generateSalt(length: Int): ByteArray {
        return randomHelper.generateSecureRandom(length)
    }
    
    override fun generateSecurePassword(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        val result = StringBuilder(length)
        for (i in 0 until length) {
            val randomIndex = randomHelper.generateSecureRandomInt(0, chars.length - 1)
            result.append(chars[randomIndex])
        }
        return result.toString()
    }
}

/**
 * представлення інтерфейсу для роботи з безпечними сесіями
 */
interface SecureSessionHelper {
    /**
     * створити ідентифікатор сесії
     *
     * @return ідентифікатор сесії
     */
    fun createSessionId(): String
    
    /**
     * створити токен
     *
     * @param sessionId ідентифікатор сесії
     * @param secret секрет
     * @return токен
     */
    fun createToken(sessionId: String, secret: ByteArray): String
    
    /**
     * перевірити токен
     *
     * @param token токен
     * @param secret секрет
     * @return ідентифікатор сесії або null, якщо токен недійсний
     */
    fun verifyToken(token: String, secret: ByteArray): String?
    
    /**
     * створити безпечний токен з терміном дії
     *
     * @param sessionId ідентифікатор сесії
     * @param secret секрет
     * @param expirationTime термін дії
     * @return токен
     */
    fun createSecureToken(sessionId: String, secret: ByteArray, expirationTime: Long): String
}

/**
 * представлення базової реалізації помічника з безпечними сесіями
 */
open class BaseSecureSessionHelper(
    private val encodingHelper: EncodingHelper = BaseEncodingHelper(),
    private val hmacHelper: HmacHelper = BaseHmacHelper(),
    private val randomHelper: SecureRandomHelper = BaseSecureRandomHelper()
) : SecureSessionHelper {
    
    override fun createSessionId(): String {
        val randomBytes = randomHelper.generateSecureRandom(32)
        return encodingHelper.encodeBase64(randomBytes)
    }
    
    override fun createToken(sessionId: String, secret: ByteArray): String {
        val data = sessionId.toByteArray()
        val signature = hmacHelper.hmacSha256(data, secret)
        val tokenData = data + signature
        return encodingHelper.encodeBase64(tokenData)
    }
    
    override fun verifyToken(token: String, secret: ByteArray): String? {
        try {
            val tokenData = encodingHelper.decodeBase64(token)
            if (tokenData.size < 32) return null // Мінімальний розмір
            
            val data = tokenData.copyOfRange(0, tokenData.size - 32)
            val signature = tokenData.copyOfRange(tokenData.size - 32, tokenData.size)
            val computedSignature = hmacHelper.hmacSha256(data, secret)
            
            return if (computedSignature.contentEquals(signature)) {
                String(data)
            } else {
                null
            }
        } catch (e: Exception) {
            return null
        }
    }
    
    override fun createSecureToken(sessionId: String, secret: ByteArray, expirationTime: Long): String {
        val data = "$sessionId:$expirationTime".toByteArray()
        val signature = hmacHelper.hmacSha256(data, secret)
        val tokenData = data + signature
        return encodingHelper.encodeBase64(tokenData)
    }
}

/**
 * представлення інтерфейсу для роботи з безпечними мережевими з'єднаннями