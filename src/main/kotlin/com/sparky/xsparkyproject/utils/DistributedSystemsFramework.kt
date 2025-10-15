/**
 * Фреймворк для розподілених систем
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.*
import java.io.*
import java.security.MessageDigest
import kotlin.concurrent.thread

/**
 * представлення інтерфейсу для роботи з розподіленою системою
 */
interface DistributedSystem {
    /**
     * запустити вузол
     *
     * @param nodeId ідентифікатор вузла
     * @param port порт
     */
    fun startNode(nodeId: String, port: Int)

    /**
     * зупинити вузол
     *
     * @param nodeId ідентифікатор вузла
     */
    fun stopNode(nodeId: String)

    /**
     * відправити повідомлення вузлу
     *
     * @param targetNodeId ідентифікатор цільового вузла
     * @param message повідомлення
     * @return true, якщо повідомлення відправлено
     */
    fun sendMessage(targetNodeId: String, message: String): Boolean

    /**
     * трансформувати дані
     *
     * @param data дані
     * @param transformation перетворення
     * @return результат перетворення
     */
    fun transformData(data: Any, transformation: String): Any

    /**
     * отримати статус системи
     *
     * @return статус
     */
    fun getSystemStatus(): SystemStatus

    /**
     * отримати список вузлів
     *
     * @return список вузлів
     */
    fun getNodes(): List<NodeInfo>

    /**
     * додати вузол до системи
     *
     * @param nodeId ідентифікатор вузла
     * @param address адреса
     * @param port порт
     */
    fun addNode(nodeId: String, address: String, port: Int)

    /**
     * видалити вузол з системи
     *
     * @param nodeId ідентифікатор вузла
     */
    fun removeNode(nodeId: String)
}

/**
 * представлення статусу системи
 */
data class SystemStatus(
    val totalNodes: Int,
    val activeNodes: Int,
    val failedNodes: Int,
    val totalMessages: Long,
    val processedMessages: Long,
    val failedMessages: Long,
    val uptime: Long
)

/**
 * представлення інформації про вузол
 */
data class NodeInfo(
    val nodeId: String,
    val address: String,
    val port: Int,
    val status: String,
    val lastHeartbeat: Long,
    val processedMessages: Long
)

/**
 * представлення базової реалізації розподіленої системи
 */
class BaseDistributedSystem : DistributedSystem {
    private val nodes = ConcurrentHashMap<String, NodeData>()
    private val messageCounter = AtomicInteger(0)
    private val processedMessageCounter = AtomicInteger(0)
    private val failedMessageCounter = AtomicInteger(0)
    private val startTime = System.currentTimeMillis()
    private val executor = Executors.newCachedThreadPool()
    
    override fun startNode(nodeId: String, port: Int) {
        val nodeData = NodeData(
            nodeId = nodeId,
            address = InetAddress.getLocalHost().hostAddress,
            port = port,
            status = "ACTIVE",
            lastHeartbeat = System.currentTimeMillis(),
            processedMessages = 0,
            serverSocket = ServerSocket(port)
        )
        
        nodes[nodeId] = nodeData
        
        // Запуск сервера для прийому повідомлень
        thread {
            try {
                while (!nodeData.serverSocket.isClosed) {
                    val clientSocket = nodeData.serverSocket.accept()
                    handleIncomingMessage(clientSocket, nodeId)
                }
            } catch (e: Exception) {
                // Обробка помилок
            }
        }
        
        // Запуск відправки heartbeat
        thread {
            while (nodes.containsKey(nodeId)) {
                sendHeartbeat(nodeId)
                Thread.sleep(5000) // Кожні 5 секунд
            }
        }
    }
    
    override fun stopNode(nodeId: String) {
        nodes[nodeId]?.let { nodeData ->
            try {
                nodeData.serverSocket.close()
                nodes.remove(nodeId)
            } catch (e: Exception) {
                // Обробка помилок
            }
        }
    }
    
    override fun sendMessage(targetNodeId: String, message: String): Boolean {
        messageCounter.incrementAndGet()
        
        val targetNode = nodes[targetNodeId]
        if (targetNode == null || targetNode.status != "ACTIVE") {
            failedMessageCounter.incrementAndGet()
            return false
        }
        
        try {
            val socket = Socket(targetNode.address, targetNode.port)
            val writer = PrintWriter(socket.getOutputStream(), true)
            writer.println(message)
            writer.close()
            socket.close()
            
            // Оновлення статистики
            nodes[targetNodeId] = targetNode.copy(
                processedMessages = targetNode.processedMessages + 1
            )
            
            processedMessageCounter.incrementAndGet()
            return true
        } catch (e: Exception) {
            failedMessageCounter.incrementAndGet()
            return false
        }
    }
    
    override fun transformData(data: Any, transformation: String): Any {
        return when (transformation) {
            "uppercase" -> {
                if (data is String) data.uppercase() else data
            }
            "lowercase" -> {
                if (data is String) data.lowercase() else data
            }
            "reverse" -> {
                if (data is String) data.reversed() else data
            }
            "hash" -> {
                if (data is String) {
                    val digest = MessageDigest.getInstance("SHA-256")
                    val hashBytes = digest.digest(data.toByteArray())
                    hashBytes.joinToString("") { "%02x".format(it) }
                } else {
                    data
                }
            }
            else -> data
        }
    }
    
    override fun getSystemStatus(): SystemStatus {
        val currentTime = System.currentTimeMillis()
        val activeNodes = nodes.values.count { it.status == "ACTIVE" }
        val failedNodes = nodes.size - activeNodes
        
        return SystemStatus(
            totalNodes = nodes.size,
            activeNodes = activeNodes,
            failedNodes = failedNodes,
            totalMessages = messageCounter.toLong(),
            processedMessages = processedMessageCounter.toLong(),
            failedMessages = failedMessageCounter.toLong(),
            uptime = currentTime - startTime
        )
    }
    
    override fun getNodes(): List<NodeInfo> {
        return nodes.values.map { nodeData ->
            NodeInfo(
                nodeId = nodeData.nodeId,
                address = nodeData.address,
                port = nodeData.port,
                status = nodeData.status,
                lastHeartbeat = nodeData.lastHeartbeat,
                processedMessages = nodeData.processedMessages
            )
        }.toList()
    }
    
    override fun addNode(nodeId: String, address: String, port: Int) {
        nodes[nodeId] = NodeData(
            nodeId = nodeId,
            address = address,
            port = port,
            status = "INACTIVE",
            lastHeartbeat = System.currentTimeMillis(),
            processedMessages = 0,
            serverSocket = ServerSocket() // Тимчасовий сокет
        )
    }
    
    override fun removeNode(nodeId: String) {
        nodes.remove(nodeId)
    }
    
    /**
     * представлення даних вузла
     */
    private data class NodeData(
        val nodeId: String,
        val address: String,
        val port: Int,
        val status: String,
        val lastHeartbeat: Long,
        val processedMessages: Long,
        val serverSocket: ServerSocket
    )
    
    /**
     * обробити вхідне повідомлення
     *
     * @param socket сокет
     * @param nodeId ідентифікатор вузла
     */
    private fun handleIncomingMessage(socket: Socket, nodeId: String) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val message = reader.readLine()
            
            // Обробка повідомлення
            processMessage(message, nodeId)
            
            reader.close()
            socket.close()
        } catch (e: Exception) {
            // Обробка помилок
        }
    }
    
    /**
     * обробити повідомлення
     *
     * @param message повідомлення
     * @param nodeId ідентифікатор вузла
     */
    private fun processMessage(message: String?, nodeId: String) {
        if (message != null) {
            // Тут буде реалізація обробки повідомлення
            println("Node $nodeId received message: $message")
        }
    }
    
    /**
     * відправити heartbeat
     *
     * @param nodeId ідентифікатор вузла
     */
    private fun sendHeartbeat(nodeId: String) {
        nodes[nodeId]?.let { nodeData ->
            nodes[nodeId] = nodeData.copy(
                lastHeartbeat = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * отримати навантаження на вузол
     *
     * @param nodeId ідентифікатор вузла
     * @return навантаження
     */
    fun getNodeLoad(nodeId: String): Double {
        return nodes[nodeId]?.let { nodeData ->
            nodeData.processedMessages.toDouble() / 1000.0 // Нормалізація
        } ?: 0.0
    }
    
    /**
     * балансувати навантаження
     */
    fun balanceLoad() {
        // Це заглушка для балансування навантаження
    }
    
    /**
     * закрити систему
     */
    fun shutdown() {
        nodes.keys.forEach { nodeId ->
            stopNode(nodeId)
        }
        executor.shutdown()
    }
}

/**
 * представлення інтерфейсу для роботи з консенсусом
 */
interface ConsensusAlgorithm {
    /**
     * запропонувати значення
     *
     * @param nodeId ідентифікатор вузла
     * @param value значення
     * @return true, якщо значення прийнято
     */
    fun proposeValue(nodeId: String, value: Any): Boolean

    /**
     * отримати прийняте значення
     *
     * @return значення
     */
    fun getAcceptedValue(): Any?

    /**
     * отримати статус консенсусу
     *
     * @return статус
     */
    fun getConsensusStatus(): ConsensusStatus

    /**
     * додати вузол до консенсусу
     *
     * @param nodeId ідентифікатор вузла
     */
    fun addNodeToConsensus(nodeId: String)

    /**
     * видалити вузол з консенсусу
     *
     * @param nodeId ідентифікатор вузла
     */
    fun removeNodeFromConsensus(nodeId: String)
}

/**
 * представлення статусу консенсусу
 */
data class ConsensusStatus(
    val isConsensusReached: Boolean,
    val participatingNodes: Int,
    val votesFor: Int,
    val votesAgainst: Int,
    val undecidedNodes: Int
)

/**
 * представлення базової реалізації алгоритму консенсусу