/**
 * фреймворк для роботи з повідомленнями
 *
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.util.*
import java.util.concurrent.*
import kotlin.coroutines.*

/**
 * представлення інтерфейсу для роботи з повідомленнями
 */
interface Message {
    /**
     * отримати ідентифікатор повідомлення
     *
     * @return ідентифікатор
     */
    fun getId(): String
    
    /**
     * отримати тип повідомлення
     *
     * @return тип
     */
    fun getType(): String
    
    /**
     * отримати вміст повідомлення
     *
     * @return вміст
     */
    fun getPayload(): Any?
    
    /**
     * отримати мітку часу
     *
     * @return мітка часу
     */
    fun getTimestamp(): Long
    
    /**
     * отримати заголовки
     *
     * @return заголовки
     */
    fun getHeaders(): Map<String, Any?>
}

/**
 * представлення базової реалізації повідомлення
 */
open class BaseMessage(
    private val id: String = UUID.randomUUID().toString(),
    private val type: String,
    private val payload: Any?,
    private val timestamp: Long = System.currentTimeMillis(),
    private val headers: Map<String, Any?> = emptyMap()
) : Message {
    
    override fun getId(): String = id
    
    override fun getType(): String = type
    
    override fun getPayload(): Any? = payload
    
    override fun getTimestamp(): Long = timestamp
    
    override fun getHeaders(): Map<String, Any?> = headers
}

/**
 * представлення інтерфейсу для роботи з брокером повідомлень
 */
interface MessageBroker {
    /**
     * надіслати повідомлення
     *
     * @param message повідомлення
     */
    fun sendMessage(message: Message)
    
    /**
     * підписатися на повідомлення
     *
     * @param topic тема
     * @param listener слухач
     */
    fun subscribe(topic: String, listener: MessageListener)
    
    /**
     * відписатися від повідомлень
     *
     * @param topic тема
     * @param listener слухач
     */
    fun unsubscribe(topic: String, listener: MessageListener)
    
    /**
     * створити тему
     *
     * @param topic тема
     */
    fun createTopic(topic: String)
    
    /**
     * видалити тему
     *
     * @param topic тема
     */
    fun deleteTopic(topic: String)
}

/**
 * представлення слухача повідомлень
 */
interface MessageListener {
    /**
     * обробити повідомлення
     *
     * @param message повідомлення
     */
    fun onMessage(message: Message)
}

/**
 * представлення базової реалізації брокера повідомлень
 */
open class BaseMessageBroker : MessageBroker {
    private val topics = ConcurrentHashMap<String, MutableList<MessageListener>>()
    private val executor = Executors.newCachedThreadPool()
    
    override fun sendMessage(message: Message) {
        val topic = message.getType()
        val listeners = topics[topic] ?: return
        
        listeners.forEach { listener ->
            executor.submit {
                try {
                    listener.onMessage(message)
                } catch (e: Exception) {
                    // Ігнорувати помилки слухачів
                }
            }
        }
    }
    
    override fun subscribe(topic: String, listener: MessageListener) {
        topics.computeIfAbsent(topic) { mutableListOf() }.add(listener)
    }
    
    override fun unsubscribe(topic: String, listener: MessageListener) {
        topics[topic]?.remove(listener)
    }
    
    override fun createTopic(topic: String) {
        topics.computeIfAbsent(topic) { mutableListOf() }
    }
    
    override fun deleteTopic(topic: String) {
        topics.remove(topic)
    }
    
    /**
     * закрити брокер
     */
    fun close() {
        executor.shutdown()
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executor.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }
}

/**
 * представлення інтерфейсу для роботи з чергою повідомлень
 */
interface MessageQueue<T> {
    /**
     * додати повідомлення до черги
     *
     * @param message повідомлення
     */
    fun enqueue(message: T)
    
    /**
     * отримати повідомлення з черги
     *
     * @return повідомлення або null
     */
    fun dequeue(): T?
    
    /**
     * перевірити, чи черга порожня
     *
     * @return true, якщо черга порожня
     */
    fun isEmpty(): Boolean
    
    /**
     * отримати розмір черги
     *
     * @return розмір
     */
    fun size(): Int
}

/**
 * представлення базової реалізації черги повідомлень
 */
open class BaseMessageQueue<T> : MessageQueue<T> {
    private val queue = LinkedBlockingQueue<T>()
    
    override fun enqueue(message: T) {
        queue.offer(message)
    }
    
    override fun dequeue(): T? {
        return queue.poll()
    }
    
    override fun isEmpty(): Boolean {
        return queue.isEmpty()
    }
    
    override fun size(): Int {
        return queue.size
    }
}

/**
 * представлення інтерфейсу для роботи з обмінником повідомлень
 */
interface MessageExchange {
    /**
     * надіслати повідомлення
     *
     * @param routingKey ключ маршрутизації
     * @param message повідомлення
     */
    fun publish(routingKey: String, message: Message)
    
    /**
     * підписатися на повідомлення
     *
     * @param bindingKey ключ прив'язки
     * @param listener слухач
     */
    fun bind(bindingKey: String, listener: MessageListener)
    
    /**
     * відписатися від повідомлень
     *
     * @param bindingKey ключ прив'язки
     * @param listener слухач
     */
    fun unbind(bindingKey: String, listener: MessageListener)
}

/**
 * представлення базової реалізації обмінника повідомлень
 */
open class BaseMessageExchange : MessageExchange {
    private val bindings = ConcurrentHashMap<String, MutableList<Pair<String, MessageListener>>>()
    private val broker = BaseMessageBroker()
    
    override fun publish(routingKey: String, message: Message) {
        // Знайти всі відповідні прив'язки
        bindings.forEach { (bindingKey, listeners) ->
            if (matches(routingKey, bindingKey)) {
                listeners.forEach { (_, listener) ->
                    broker.subscribe(routingKey, listener)
                }
            }
        }
        
        broker.sendMessage(message)
    }
    
    override fun bind(bindingKey: String, listener: MessageListener) {
        bindings.computeIfAbsent(bindingKey) { mutableListOf() }.add(Pair(bindingKey, listener))
    }
    
    override fun unbind(bindingKey: String, listener: MessageListener) {
        bindings[bindingKey]?.removeIf { it.second == listener }
    }
    
    private fun matches(routingKey: String, bindingKey: String): Boolean {
        // Проста реалізація зірочкового збігу
        if (bindingKey == "#") return true
        if (bindingKey == "*") return routingKey.isNotEmpty()
        
        val routingParts = routingKey.split(".")
        val bindingParts = bindingKey.split(".")
        
        if (bindingParts.size != routingParts.size) return false
        
        return bindingParts.zip(routingParts).all { (bindingPart, routingPart) ->
            bindingPart == "*" || bindingPart == routingPart
        }
    }
}

/**
 * представлення інтерфейсу для роботи з шаблоном повідомлення
 */
interface MessageTemplate {
    /**
     * створити повідомлення
     *
     * @param type тип
     * @param payload вміст
     * @param headers заголовки
     * @return повідомлення
     */
    fun createMessage(type: String, payload: Any?, headers: Map<String, Any?> = emptyMap()): Message
    
    /**
     * створити повідомлення з відповіддю
     *
     * @param request запит
     * @param payload вміст
     * @param headers заголовки
     * @return повідомлення
     */
    fun createReplyMessage(request: Message, payload: Any?, headers: Map<String, Any?> = emptyMap()): Message
}

/**
 * представлення базової реалізації шаблону повідомлення
 */
open class BaseMessageTemplate : MessageTemplate {
    
    override fun createMessage(type: String, payload: Any?, headers: Map<String, Any?>): Message {
        return BaseMessage(
            UUID.randomUUID().toString(),
            type,
            payload,
            System.currentTimeMillis(),
            headers
        )
    }
    
    override fun createReplyMessage(request: Message, payload: Any?, headers: Map<String, Any?>): Message {
        val replyHeaders = mutableMapOf<String, Any?>()
        replyHeaders.putAll(request.getHeaders())
        replyHeaders.putAll(headers)
        replyHeaders["replyTo"] = request.getId()
        
        return BaseMessage(
            UUID.randomUUID().toString(),
            "${request.getType()}.reply",
            payload,
            System.currentTimeMillis(),
            replyHeaders
        )
    }
}

/**
 * представлення інтерфейсу для роботи з конвертером повідомлень
 */
interface MessageConverter {
    /**
     * конвертувати об'єкт в повідомлення
     *
     * @param obj об'єкт
     * @param type тип
     * @return повідомлення
     */
    fun toMessage(obj: Any, type: String): Message
    
    /**
     * конвертувати повідомлення в об'єкт
     *
     * @param message повідомлення
     * @param clazz клас
     * @return об'єкт
     */
    fun <T> fromMessage(message: Message, clazz: Class<T>): T
}

/**
 * представлення базової реалізації конвертера повідомлень
 */
open class BaseMessageConverter : MessageConverter {
    
    override fun toMessage(obj: Any, type: String): Message {
        // Заглушка для реалізації конвертації об'єкта в повідомлення
        return BaseMessage(
            UUID.randomUUID().toString(),
            type,
            obj,
            System.currentTimeMillis()
        )
    }
    
    override fun <T> fromMessage(message: Message, clazz: Class<T>): T {
        // Заглушка для реалізації конвертації повідомлення в об'єкт
        @Suppress("UNCHECKED_CAST")
        return message.getPayload() as T
    }
}

/**
 * представлення інтерфейсу для роботи з каналом повідомлень
 */
interface MessageChannel {
    /**
     * надіслати повідомлення
     *
     * @param message повідомлення
     * @return true, якщо успішно
     */
    fun send(message: Message): Boolean
    
    /**
     * отримати повідомлення
     *
     * @param timeout таймаут
     * @return повідомлення або null
     */
    fun receive(timeout: Long = 0): Message?
    
    /**
     * закрити канал
     */
    fun close()
}

/**
 * представлення базової реалізації каналу повідомлень
 */
open class BaseMessageChannel : MessageChannel {
    private val queue = LinkedBlockingQueue<Message>()
    private var closed = false
    
    override fun send(message: Message): Boolean {
        if (closed) return false
        return queue.offer(message)
    }
    
    override fun receive(timeout: Long): Message? {
        return if (timeout > 0) {
            queue.poll(timeout, TimeUnit.MILLISECONDS)
        } else {
            queue.poll()
        }
    }
    
    override fun close() {
        closed = true
    }
}

/**
 * представлення інтерфейсу для роботи з маршрутизатором повідомлень
 */
interface MessageRouter {
    /**
     * маршрутизувати повідомлення
     *
     * @param message повідомлення
     * @return список каналів
     */
    fun route(message: Message): List<MessageChannel>
    
    /**
     * додати маршрут
     *
     * @param condition умова
     * @param channel канал
     */
    fun addRoute(condition: (Message) -> Boolean, channel: MessageChannel)
    
    /**
     * видалити маршрут
     *
     * @param channel канал
     */
    fun removeRoute(channel: MessageChannel)
}

/**
 * представлення базової реалізації маршрутизатора повідомлень