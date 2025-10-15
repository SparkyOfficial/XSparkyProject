package com.sparky.xsparkyproject.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec

/**
 * утиліти безпеки для шифрування, хешування та управління ключами
 *
 * @author Андрій Будильников
 */
class SecurityUtils {
    
    companion object {
        private const val AES_TRANSFORMATION = "AES/CBC/PKCS5Padding"
        private const val HMAC_SHA256 = "HmacSHA256"
        
        /**
         * обчислює sha-256 хеш від рядка
         */
        fun sha256(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
        
        /**
         * обчислює md5 хеш від рядка
         */
        fun md5(input: String): String {
            val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
        
        /**
         * обчислює sha-1 хеш від рядка
         */
        fun sha1(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-1").digest(input.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
        
        /**
         * генерує випадковий секретний ключ для aes
         */
        fun generateAESKey(): SecretKey {
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(256)
            return keyGenerator.generateKey()
        }
        
        /**
         * генерує випадковий вектор ініціалізації
         */
        fun generateIV(): ByteArray {
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
            return iv
        }
        
        /**
         * шифрує дані за допомогою aes з використанням cbc
         */
        fun encryptAES(data: String, key: SecretKey, iv: ByteArray = generateIV()): EncryptedData {
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
            val encryptedBytes = cipher.doFinal(data.toByteArray())
            return EncryptedData(
                Base64.getEncoder().encodeToString(encryptedBytes),
                Base64.getEncoder().encodeToString(iv)
            )
        }
        
        /**
         * дешифрує дані за допомогою aes з використанням cbc
         */
        fun decryptAES(encryptedData: EncryptedData, key: SecretKey): String {
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            val iv = Base64.getDecoder().decode(encryptedData.iv)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
            val decodedBytes = Base64.getDecoder().decode(encryptedData.data)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            return String(decryptedBytes)
        }
        
        /**
         * створює secretkey з масиву байтів
         */
        fun secretKeyFromBytes(keyBytes: ByteArray): SecretKey {
            return SecretKeySpec(keyBytes, "AES")
        }
        
        /**
         * обчислює hmac-sha256 від даних з використанням ключа
         */
        fun hmacSha256(data: String, key: SecretKey): String {
            val mac = Mac.getInstance(HMAC_SHA256)
            mac.init(key)
            val result = mac.doFinal(data.toByteArray())
            return Base64.getEncoder().encodeToString(result)
        }
        
        /**
         * генерує випадковий секретний ключ для hmac
         */
        fun generateHmacKey(): SecretKey {
            val keyBytes = ByteArray(32)
            SecureRandom().nextBytes(keyBytes)
            return SecretKeySpec(keyBytes, HMAC_SHA256)
        }
    }
}

/**
 * дані після шифрування
 */
data class EncryptedData(val data: String, val iv: String)