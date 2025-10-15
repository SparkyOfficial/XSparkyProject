/**
 * Блокчейн фреймворк для роботи з блокчейн технологіями
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.security.MessageDigest
import java.security.PublicKey
import java.security.PrivateKey
import java.security.Signature
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
import java.math.BigInteger

/**
 * представлення інтерфейсу для роботи з блокчейном
 */
interface Blockchain {
    /**
     * додати новий блок
     *
     * @param data дані блоку
     * @param minerAddress адреса майнера
     * @return блок
     */
    fun addBlock(data: Any, minerAddress: String): Block

    /**
     * отримати блок за індексом
     *
     * @param index індекс блоку
     * @return блок
     */
    fun getBlock(index: Int): Block?

    /**
     * отримати блок за хешем
     *
     * @param hash хеш блоку
     * @return блок
     */
    fun getBlockByHash(hash: String): Block?

    /**
     * отримати останній блок
     *
     * @return останній блок
     */
    fun getLastBlock(): Block

    /**
     * перевірити валідність блокчейну
     *
     * @return true, якщо блокчейн валідний
     */
    fun isValid(): Boolean

    /**
     * отримати баланс адреси
     *
     * @param address адреса
     * @return баланс
     */
    fun getBalance(address: String): Double

    /**
     * отримати список всіх блоків
     *
     * @return список блоків
     */
    fun getAllBlocks(): List<Block>

    /**
     * отримати довжину блокчейну
     *
     * @return довжина
     */
    fun getLength(): Int

    /**
     * отримати загальний обсяг транзакцій
     *
     * @return обсяг транзакцій
     */
    fun getTotalTransactionVolume(): Double
}

/**
 * представлення блоку
 */
data class Block(
    val index: Int,
    val timestamp: Long,
    val data: Any,
    val previousHash: String,
    val hash: String,
    val nonce: Long,
    val minerAddress: String,
    val reward: Double
)

/**
 * представлення транзакції
 */
data class Transaction(
    val fromAddress: String,
    val toAddress: String,
    val amount: Double,
    val timestamp: Long,
    val signature: String?
) {
    /**
     * підписати транзакцію
     *
     * @param privateKey приватний ключ
     */
    fun sign(privateKey: PrivateKey) {
        // Це заглушка для підпису транзакції
    }

    /**
     * перевірити підпис транзакції
     *
     * @param publicKey публічний ключ
     * @return true, якщо підпис валідний
     */
    fun isValid(publicKey: PublicKey): Boolean {
        // Це заглушка для перевірки підпису транзакції
        return true
    }
}

/**
 * представлення базової реалізації блокчейну
 */
class BaseBlockchain(
    private val miningReward: Double = 100.0,
    private val difficulty: Int = 4
) : Blockchain {
    
    private val blocks = mutableListOf<Block>()
    private val pendingTransactions = mutableListOf<Transaction>()
    private val balances = ConcurrentHashMap<String, Double>()
    
    init {
        // Створення генезис блоку
        createGenesisBlock()
    }
    
    private fun createGenesisBlock() {
        val genesisBlock = Block(
            index = 0,
            timestamp = System.currentTimeMillis(),
            data = "Genesis Block",
            previousHash = "0",
            hash = calculateHash(0, System.currentTimeMillis(), "Genesis Block", "0", 0, "genesis", 0.0),
            nonce = 0,
            minerAddress = "genesis",
            reward = 0.0
        )
        blocks.add(genesisBlock)
    }
    
    override fun addBlock(data: Any, minerAddress: String): Block {
        val previousBlock = getLastBlock()
        val blockIndex = previousBlock.index + 1
        val timestamp = System.currentTimeMillis()
        var nonce = 0L
        val reward = miningReward
        
        // Майнінг блоку
        val blockData = mapOf(
            "transactions" to pendingTransactions.toList(),
            "customData" to data
        )
        
        var hash = calculateHash(blockIndex, timestamp, blockData, previousBlock.hash, nonce, minerAddress, reward)
        
        // Пошук_nonce, який задовольняє умову складності
        while (!hash.startsWith("0".repeat(difficulty))) {
            nonce++
            hash = calculateHash(blockIndex, timestamp, blockData, previousBlock.hash, nonce, minerAddress, reward)
        }
        
        val newBlock = Block(
            index = blockIndex,
            timestamp = timestamp,
            data = blockData,
            previousHash = previousBlock.hash,
            hash = hash,
            nonce = nonce,
            minerAddress = minerAddress,
            reward = reward
        )
        
        blocks.add(newBlock)
        
        // Очищення очікуючих транзакцій
        processTransactions(pendingTransactions.toList(), minerAddress, reward)
        pendingTransactions.clear()
        
        return newBlock
    }
    
    override fun getBlock(index: Int): Block? {
        return if (index >= 0 && index < blocks.size) {
            blocks[index]
        } else {
            null
        }
    }
    
    override fun getBlockByHash(hash: String): Block? {
        return blocks.find { it.hash == hash }
    }
    
    override fun getLastBlock(): Block {
        return blocks.last()
    }
    
    override fun isValid(): Boolean {
        // Перевірка генезис блоку
        if (blocks[0].index != 0 || blocks[0].previousHash != "0") {
            return false
        }
        
        // Перевірка всіх блоків
        for (i in 1 until blocks.size) {
            val currentBlock = blocks[i]
            val previousBlock = blocks[i - 1]
            
            // Перевірка хешу блоку
            val calculatedHash = calculateHash(
                currentBlock.index,
                currentBlock.timestamp,
                currentBlock.data,
                currentBlock.previousHash,
                currentBlock.nonce,
                currentBlock.minerAddress,
                currentBlock.reward
            )
            
            if (currentBlock.hash != calculatedHash) {
                return false
            }
            
            // Перевірка попереднього хешу
            if (currentBlock.previousHash != previousBlock.hash) {
                return false
            }
            
            // Перевірка складності
            if (!currentBlock.hash.startsWith("0".repeat(difficulty))) {
                return false
            }
        }
        
        return true
    }
    
    override fun getBalance(address: String): Double {
        return balances[address] ?: 0.0
    }
    
    override fun getAllBlocks(): List<Block> {
        return blocks.toList()
    }
    
    override fun getLength(): Int {
        return blocks.size
    }
    
    override fun getTotalTransactionVolume(): Double {
        return blocks.sumOf { block ->
            when (block.data) {
                is Map<*, *> -> {
                    val transactions = block.data["transactions"]
                    if (transactions is List<*>) {
                        transactions.sumOf { 
                            if (it is Transaction) it.amount else 0.0 
                        }
                    } else {
                        0.0
                    }
                }
                else -> 0.0
            }
        }
    }
    
    /**
     * додати транзакцію
     *
     * @param transaction транзакція
     * @return true, якщо транзакцію додано
     */
    fun addTransaction(transaction: Transaction): Boolean {
        // Перевірка валідності транзакції
        if (transaction.fromAddress.isEmpty() || transaction.toAddress.isEmpty()) {
            return false
        }
        
        if (transaction.amount <= 0) {
            return false
        }
        
        // Перевірка балансу
        if (getBalance(transaction.fromAddress) < transaction.amount) {
            return false
        }
        
        pendingTransactions.add(transaction)
        return true
    }
    
    /**
     * обчислити хеш блоку
     *
     * @param index індекс
     * @param timestamp часова мітка
     * @param data дані
     * @param previousHash попередній хеш
     * @param nonce nonce
     * @param minerAddress адреса майнера
     * @param reward винагорода
     * @return хеш
     */
    private fun calculateHash(
        index: Int,
        timestamp: Long,
        data: Any,
        previousHash: String,
        nonce: Long,
        minerAddress: String,
        reward: Double
    ): String {
        val dataString = "$index$timestamp$data$previousHash$nonce$minerAddress$reward"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(dataString.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * обробити транзакції
     *
     * @param transactions транзакції
     * @param minerAddress адреса майнера
     * @param reward винагорода
     */
    private fun processTransactions(transactions: List<Transaction>, minerAddress: String, reward: Double) {
        // Обробка транзакцій
        transactions.forEach { transaction ->
            // Зняття коштів з адреси відправника
            balances[transaction.fromAddress] = (balances[transaction.fromAddress] ?: 0.0) - transaction.amount
            
            // Поповнення коштів на адресу отримувача
            balances[transaction.toAddress] = (balances[transaction.toAddress] ?: 0.0) + transaction.amount
        }
        
        // Додавання винагороди майнеру
        balances[minerAddress] = (balances[minerAddress] ?: 0.0) + reward
    }
    
    /**
     * отримати список очікуючих транзакцій
     *
     * @return список транзакцій
     */
    fun getPendingTransactions(): List<Transaction> {
        return pendingTransactions.toList()
    }
    
    /**
     * очистити очікуючі транзакції
     */
    fun clearPendingTransactions() {
        pendingTransactions.clear()
    }
}

/**
 * представлення інтерфейсу для роботи з гаманцем
 */
interface Wallet {
    /**
     * отримати публічний ключ
     *
     * @return публічний ключ
     */
    fun getPublicKey(): PublicKey

    /**
     * отримати приватний ключ
     *
     * @return приватний ключ
     */
    fun getPrivateKey(): PrivateKey

    /**
     * отримати адресу
     *
     * @return адреса
     */
    fun getAddress(): String

    /**
     * створити транзакцію
     *
     * @param toAddress адреса отримувача
     * @param amount сума
     * @return транзакція
     */
    fun createTransaction(toAddress: String, amount: Double): Transaction

    /**
     * підписати дані
     *
     * @param data дані
     * @return підпис
     */
    fun signData(data: ByteArray): ByteArray

    /**
     * перевірити підпис
     *
     * @param data дані
     * @param signature підпис
     * @param publicKey публічний ключ
     * @return true, якщо підпис валідний
     */
    fun verifySignature(data: ByteArray, signature: ByteArray, publicKey: PublicKey): Boolean

    /**
     * отримати баланс
     *
     * @param blockchain блокчейн
     * @return баланс
     */
    fun getBalance(blockchain: Blockchain): Double
}

/**
 * представлення базової реалізації гаманця
 */
class BaseWallet : Wallet {
    private val keyPair = generateKeyPair()
    private val address = generateAddress(keyPair.public)
    
    override fun getPublicKey(): PublicKey {
        return keyPair.public
    }
    
    override fun getPrivateKey(): PrivateKey {
        return keyPair.private
    }
    
    override fun getAddress(): String {
        return address
    }
    
    override fun createTransaction(toAddress: String, amount: Double): Transaction {
        return Transaction(
            fromAddress = address,
            toAddress = toAddress,
            amount = amount,
            timestamp = System.currentTimeMillis(),
            signature = null
        )
    }
    
    override fun signData(data: ByteArray): ByteArray {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(keyPair.private)
        signature.update(data)
        return signature.sign()
    }
    
    override fun verifySignature(data: ByteArray, signature: ByteArray, publicKey: PublicKey): Boolean {
        val sig = Signature.getInstance("SHA256withRSA")
        sig.initVerify(publicKey)
        sig.update(data)
        return sig.verify(signature)
    }
    
    override fun getBalance(blockchain: Blockchain): Double {
        return blockchain.getBalance(address)
    }
    
    /**
     * згенерувати пару ключів
     *
     * @return пара ключів
     */
    private fun generateKeyPair(): java.security.KeyPair {
        val keyGen = java.security.KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        return keyGen.generateKeyPair()
    }
    
    /**
     * згенерувати адресу
     *
     * @param publicKey публічний ключ
     * @return адреса
     */
    private fun generateAddress(publicKey: PublicKey): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(publicKey.encoded)
        return hashBytes.joinToString("") { "%02x".format(it) }.take(40)
    }
}

/**
 * представлення інтерфейсу для роботи з майнінгом
 */
interface Miner {
    /**
     * почати майнінг
     *
     * @param blockchain блокчейн
     * @param wallet гаманець майнера
     */
    fun startMining(blockchain: Blockchain, wallet: Wallet)

    /**
     * зупинити майнінг
     */
    fun stopMining()

    /**
     * отримати статус майнінгу
     *
     * @return статус
     */
    fun getMiningStatus(): String

    /**
     * отримати статистику майнінгу
     *
     * @return статистика
     */
    fun getMiningStats(): MiningStats

    /**
     * встановити складність
     *
     * @param difficulty складність
     */
    fun setDifficulty(difficulty: Int)

    /**
     * встановити винагороду
     *
     * @param reward винагорода
     */
    fun setMiningReward(reward: Double)
}

/**
 * представлення статистики майнінгу
 */
data class MiningStats(
    val blocksMined: Int,
    val totalReward: Double,
    val miningTime: Long,
    val hashRate: Double
)

/**
 * представлення базової реалізації майнера