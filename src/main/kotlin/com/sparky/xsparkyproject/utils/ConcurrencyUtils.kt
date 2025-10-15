/**
 * утиліти для роботи з конкурентністю
 *
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import kotlinx.coroutines.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*
import java.util.concurrent.locks.*
import kotlin.coroutines.*

/**
 * представлення інтерфейсу для роботи з потоками
 */
interface ThreadHelper {
    /**
     * створити новий потік
     *
     * @param name назва потоку
     * @param daemon чи є потік демоном
     * @param block блок коду для виконання
     * @return потік
     */
    fun createThread(name: String, daemon: Boolean = false, block: () -> Unit): Thread
    
    /**
     * виконати блок коду в новому потоці
     *
     * @param name назва потоку
     * @param block блок коду для виконання
     * @return потік
     */
    fun runInNewThread(name: String, block: () -> Unit): Thread
    
    /**
     * очікувати завершення потоку
     *
     * @param thread потік
     * @param timeout таймаут в мілісекундах
     * @return true, якщо потік завершився, false при таймауті
     */
    fun joinThread(thread: Thread, timeout: Long = 0): Boolean
}

/**
 * представлення базової реалізації помічника з потоками
 */
open class BaseThreadHelper : ThreadHelper {
    
    override fun createThread(name: String, daemon: Boolean, block: () -> Unit): Thread {
        val thread = Thread(block, name)
        thread.isDaemon = daemon
        return thread
    }
    
    override fun runInNewThread(name: String, block: () -> Unit): Thread {
        val thread = createThread(name, false, block)
        thread.start()
        return thread
    }
    
    override fun joinThread(thread: Thread, timeout: Long): Boolean {
        try {
            if (timeout > 0) {
                thread.join(timeout)
            } else {
                thread.join()
            }
            return !thread.isAlive
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            return false
        }
    }
}

/**
 * представлення інтерфейсу для роботи з пулами потоків
 */
interface ThreadPoolHelper {
    /**
     * створити фіксований пул потоків
     *
     * @param size розмір пулу
     * @param namePrefix префікс імен потоків
     * @return пул потоків
     */
    fun createFixedThreadPool(size: Int, namePrefix: String = "pool"): ExecutorService
    
    /**
     * створити кешований пул потоків
     *
     * @param namePrefix префікс імен потоків
     * @return пул потоків
     */
    fun createCachedThreadPool(namePrefix: String = "cached"): ExecutorService
    
    /**
     * створити пул потоків з одиночним потоком
     *
     * @param namePrefix префікс імен потоків
     * @return пул потоків
     */
    fun createSingleThreadExecutor(namePrefix: String = "single"): ExecutorService
    
    /**
     * закрити пул потоків
     *
     * @param executor пул потоків
     * @param timeout таймаут в мілісекундах
     */
    fun shutdownExecutor(executor: ExecutorService, timeout: Long = 5000)
}

/**
 * представлення базової реалізації помічника з пулами потоків
 */
open class BaseThreadPoolHelper : ThreadPoolHelper {
    
    override fun createFixedThreadPool(size: Int, namePrefix: String): ExecutorService {
        return Executors.newFixedThreadPool(size, NamedThreadFactory(namePrefix))
    }
    
    override fun createCachedThreadPool(namePrefix: String): ExecutorService {
        return Executors.newCachedThreadPool(NamedThreadFactory(namePrefix))
    }
    
    override fun createSingleThreadExecutor(namePrefix: String): ExecutorService {
        return Executors.newSingleThreadExecutor(NamedThreadFactory(namePrefix))
    }
    
    override fun shutdownExecutor(executor: ExecutorService, timeout: Long) {
        executor.shutdown()
        try {
            if (!executor.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow()
                if (!executor.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                    System.err.println("Пул потоків не завершився")
                }
            }
        } catch (e: InterruptedException) {
            executor.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }
}

/**
 * представлення фабрики іменованих потоків
 *
 * @property namePrefix префікс імен потоків
 * @property daemon чи є потоки демонами
 */
class NamedThreadFactory(
    private val namePrefix: String,
    private val daemon: Boolean = false
) : ThreadFactory {
    private val group: ThreadGroup
    private val threadNumber = AtomicInteger(1)
    private val namePrefixFull: String
    
    init {
        val s = System.getSecurityManager()
        group = s?.threadGroup ?: Thread.currentThread().threadGroup ?: throw IllegalStateException("Не вдалося отримати групу потоків")
        namePrefixFull = "$namePrefix-thread-"
    }
    
    override fun newThread(r: Runnable): Thread {
        val t = Thread(group, r, namePrefixFull + threadNumber.getAndIncrement(), 0)
        t.isDaemon = daemon
        if (t.priority != Thread.NORM_PRIORITY) {
            t.priority = Thread.NORM_PRIORITY
        }
        return t
    }
}

/**
 * представлення інтерфейсу для роботи з блокуваннями
 */
interface LockHelper {
    /**
     * створити reentrant блокування
     *
     * @return блокування
     */
    fun createReentrantLock(): ReentrantLock
    
    /**
     * створити читабельне блокування
     *
     * @return читабельне блокування
     */
    fun createReadLock(): ReadWriteLock
    
    /**
     * створити записувальне блокування
     *
     * @return записувальне блокування
     */
    fun createWriteLock(): ReadWriteLock
    
    /**
     * виконати блок коду з блокуванням
     *
     * @param lock блокування
     * @param block блок коду
     * @return результат виконання
     */
    fun <T> withLock(lock: Lock, block: () -> T): T
    
    /**
     * виконати блок коду з читабельним блокуванням
     *
     * @param lock блокування
     * @param block блок коду
     * @return результат виконання
     */
    fun <T> withReadLock(lock: ReadWriteLock, block: () -> T): T
    
    /**
     * виконати блок коду з записувальним блокуванням
     *
     * @param lock блокування
     * @param block блок коду
     * @return результат виконання
     */
    fun <T> withWriteLock(lock: ReadWriteLock, block: () -> T): T
}

/**
 * представлення базової реалізації помічника з блокуваннями
 */
open class BaseLockHelper : LockHelper {
    
    override fun createReentrantLock(): ReentrantLock {
        return ReentrantLock()
    }
    
    override fun createReadLock(): ReadWriteLock {
        return ReentrantReadWriteLock()
    }
    
    override fun createWriteLock(): ReadWriteLock {
        return ReentrantReadWriteLock()
    }
    
    override fun <T> withLock(lock: Lock, block: () -> T): T {
        lock.lock()
        try {
            return block()
        } finally {
            lock.unlock()
        }
    }
    
    override fun <T> withReadLock(lock: ReadWriteLock, block: () -> T): T {
        val readLock = lock.readLock()
        readLock.lock()
        try {
            return block()
        } finally {
            readLock.unlock()
        }
    }
    
    override fun <T> withWriteLock(lock: ReadWriteLock, block: () -> T): T {
        val writeLock = lock.writeLock()
        writeLock.lock()
        try {
            return block()
        } finally {
            writeLock.unlock()
        }
    }
}

/**
 * представлення інтерфейсу для роботи з атомарними змінними
 */
interface AtomicHelper {
    /**
     * створити атомарну цілочисельну змінну
     *
     * @param initialValue початкове значення
     * @return атомарна змінна
     */
    fun createAtomicInteger(initialValue: Int = 0): AtomicInteger
    
    /**
     * створити атомарну довгочисельну змінну
     *
     * @param initialValue початкове значення
     * @return атомарна змінна
     */
    fun createAtomicLong(initialValue: Long = 0L): AtomicLong
    
    /**
     * створити атомарну булеву змінну
     *
     * @param initialValue початкове значення
     * @return атомарна змінна
     */
    fun createAtomicBoolean(initialValue: Boolean = false): AtomicBoolean
    
    /**
     * створити атомарну змінну з посиланням
     *
     * @param initialValue початкове значення
     * @return атомарна змінна
     */
    fun <T> createAtomicReference(initialValue: T): AtomicReference<T>
    
    /**
     * атомарно збільшити цілочисельну змінну
     *
     * @param atomic атомарна змінна
     * @return нове значення
     */
    fun incrementAndGet(atomic: AtomicInteger): Int
    
    /**
     * атомарно збільшити довгочисельну змінну
     *
     * @param atomic атомарна змінна
     * @return нове значення
     */
    fun incrementAndGet(atomic: AtomicLong): Long
}

/**
 * представлення базової реалізації помічника з атомарними змінними
 */
open class BaseAtomicHelper : AtomicHelper {
    
    override fun createAtomicInteger(initialValue: Int): AtomicInteger {
        return AtomicInteger(initialValue)
    }
    
    override fun createAtomicLong(initialValue: Long): AtomicLong {
        return AtomicLong(initialValue)
    }
    
    override fun createAtomicBoolean(initialValue: Boolean): AtomicBoolean {
        return AtomicBoolean(initialValue)
    }
    
    override fun <T> createAtomicReference(initialValue: T): AtomicReference<T> {
        return AtomicReference(initialValue)
    }
    
    override fun incrementAndGet(atomic: AtomicInteger): Int {
        return atomic.incrementAndGet()
    }
    
    override fun incrementAndGet(atomic: AtomicLong): Long {
        return atomic.incrementAndGet()
    }
}

/**
 * представлення інтерфейсу для роботи з синхронізаторами
 */
interface Synchronizer {
    /**
     * створити лічильний семафор
     *
     * @param permits кількість дозволів
     * @return семафор
     */
    fun createSemaphore(permits: Int): Semaphore
    
    /**
     * створити бар'єр
     *
     * @param parties кількість сторін
     * @param barrierAction дія при досягненні бар'єру
     * @return бар'єр
     */
    fun createBarrier(parties: Int, barrierAction: Runnable? = null): CyclicBarrier
    
    /**
     * створити защілку
     *
     * @param count кількість
     * @return защілка
     */
    fun createCountDownLatch(count: Int): CountDownLatch
    
    /**
     * створити обмінник
     *
     * @return обмінник
     */
    fun <T> createExchanger(): Exchanger<T>
}

/**
 * представлення базової реалізації синхронізатора
 */
open class BaseSynchronizer : Synchronizer {
    
    override fun createSemaphore(permits: Int): Semaphore {
        return Semaphore(permits)
    }
    
    override fun createBarrier(parties: Int, barrierAction: Runnable?): CyclicBarrier {
        return if (barrierAction != null) {
            CyclicBarrier(parties, barrierAction)
        } else {
            CyclicBarrier(parties)
        }
    }
    
    override fun createCountDownLatch(count: Int): CountDownLatch {
        return CountDownLatch(count)
    }
    
    override fun <T> createExchanger(): Exchanger<T> {
        return Exchanger()
    }
}

/**
 * представлення інтерфейсу для роботи з чергами
 */
interface QueueHelper {
    /**
     * створити блокуючу чергу
     *
     * @param capacity місткість
     * @return блокуюча черга
     */
    fun <T> createBlockingQueue(capacity: Int): BlockingQueue<T>
    
    /**
     * створити чергу з пріоритетами
     *
     * @param comparator компаратор
     * @return черга з пріоритетами
     */
    fun <T> createPriorityQueue(comparator: Comparator<T>? = null): PriorityQueue<T>
    
    /**
     * створити concurrent чергу
     *
     * @return concurrent черга
     */
    fun <T> createConcurrentQueue(): Queue<T>
    
    /**
     * створити concurrent мапу
     *
     * @return concurrent мапа
     */
    fun <K, V> createConcurrentMap(): ConcurrentMap<K, V>
}

/**
 * представлення базової реалізації помічника з чергами
 */
open class BaseQueueHelper : QueueHelper {
    
    override fun <T> createBlockingQueue(capacity: Int): BlockingQueue<T> {
        return ArrayBlockingQueue(capacity)
    }
    
    override fun <T> createPriorityQueue(comparator: Comparator<T>?): PriorityQueue<T> {
        return if (comparator != null) {
            PriorityQueue(comparator)
        } else {
            PriorityQueue()
        }
    }
    
    override fun <T> createConcurrentQueue(): Queue<T> {
        return ConcurrentLinkedQueue()
    }
    
    override fun <K, V> createConcurrentMap(): ConcurrentMap<K, V> {
        return ConcurrentHashMap()
    }
}

/**
 * представлення інтерфейсу для роботи з футурами
 */
interface FutureHelper {
    /**
     * створити CompletableFuture
     *
     * @return CompletableFuture
     */
    fun <T> createCompletableFuture(): CompletableFuture<T>
    
    /**
     * створити завершений футур
     *
     * @param value значення
     * @return футур
     */
    fun <T> createCompletedFuture(value: T): CompletableFuture<T>
    
    /**
     * створити провалений футур
     *
     * @param exception виключення
     * @return футур
     */
    fun <T> createFailedFuture(exception: Throwable): CompletableFuture<T>
    
    /**
     * об'єднати кілька футурів
     *
     * @param futures футури
     * @return футур зі списком результатів
     */
    fun <T> allOf(vararg futures: CompletableFuture<T>): CompletableFuture<List<T>>
    
    /**
     * об'єднати кілька футурів (будь-який)
     *
     * @param futures футури
     * @return футур з будь-яким результатом
     */
    fun <T> anyOf(vararg futures: CompletableFuture<T>): CompletableFuture<T>
}

/**
 * представлення базової реалізації помічника з футурами
 */
open class BaseFutureHelper : FutureHelper {
    
    override fun <T> createCompletableFuture(): CompletableFuture<T> {
        return CompletableFuture()
    }
    
    override fun <T> createCompletedFuture(value: T): CompletableFuture<T> {
        return CompletableFuture.completedFuture(value)
    }
    
    override fun <T> createFailedFuture(exception: Throwable): CompletableFuture<T> {
        return CompletableFuture.failedFuture(exception)
    }
    
    override fun <T> allOf(vararg futures: CompletableFuture<T>): CompletableFuture<List<T>> {
        return CompletableFuture.allOf(*futures)
            .thenApply {
                futures.map { it.join() }
            }
    }
    
    override fun <T> anyOf(vararg futures: CompletableFuture<T>): CompletableFuture<T> {
        @Suppress("UNCHECKED_CAST")
        return CompletableFuture.anyOf(*futures)
            .thenApply { it as T }
    }
}

/**
 * представлення інтерфейсу для роботи з корутинами
 */
interface CoroutineHelper {
    /**
     * створити скоуп
     *
     * @param context контекст
     * @return скоуп
     */
    fun createScope(context: CoroutineContext = Dispatchers.Default): CoroutineScope
    
    /**
     * запустити корутину
     *
     * @param scope скоуп
     * @param block блок коду
     * @return Job
     */
    fun launchCoroutine(scope: CoroutineScope, block: suspend CoroutineScope.() -> Unit): Job
    
    /**
     * запустити корутину з результатом
     *
     * @param scope скоуп
     * @param block блок коду
     * @return Deferred
     */
    fun <T> asyncCoroutine(scope: CoroutineScope, block: suspend CoroutineScope.() -> T): Deferred<T>
    
    /**
     * очікувати завершення корутин
     *
     * @param scope скоуп
     */
    suspend fun joinScope(scope: CoroutineScope)
}

/**
 * представлення базової реалізації помічника з корутинами
 */
open class BaseCoroutineHelper : CoroutineHelper {
    
    override fun createScope(context: CoroutineContext): CoroutineScope {
        return CoroutineScope(context)
    }
    
    override fun launchCoroutine(scope: CoroutineScope, block: suspend CoroutineScope.() -> Unit): Job {
        return scope.launch(block = block)
    }
    
    override fun <T> asyncCoroutine(scope: CoroutineScope, block: suspend CoroutineScope.() -> T): Deferred<T> {
        return scope.async(block = block)
    }
    
    override suspend fun joinScope(scope: CoroutineScope) {
        scope.coroutineContext[Job]?.join()
    }
}

/**
 * представлення інтерфейсу для роботи з диспетчерами
 */
interface DispatcherHelper {
    /**
     * отримати диспетчер за замовчуванням
     *
     * @return диспетчер
     */
    fun getDefaultDispatcher(): CoroutineDispatcher
    
    /**
     * отримати IO диспетчер
     *
     * @return диспетчер
     */
    fun getIODispatcher(): CoroutineDispatcher
    
    /**
     * отримати диспетчер для обчислень
     *
     * @return диспетчер
     */
    fun getComputationDispatcher(): CoroutineDispatcher
    
    /**
     * отримати диспетчер для основного потоку
     *
     * @return диспетчер
     */
    fun getMainDispatcher(): CoroutineDispatcher
    
    /**
     * створити диспетчер з фіксованим розміром пулу
     *
     * @param size розмір пулу
     * @return диспетчер
     */
    fun createFixedThreadPoolDispatcher(size: Int): CoroutineDispatcher
}

/**
 * представлення базової реалізації помічника з диспетчерами
 */
open class BaseDispatcherHelper : DispatcherHelper {
    
    override fun getDefaultDispatcher(): CoroutineDispatcher {
        return Dispatchers.Default
    }
    
    override fun getIODispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }
    
    override fun getComputationDispatcher(): CoroutineDispatcher {
        return Dispatchers.Default
    }
    
    override fun getMainDispatcher(): CoroutineDispatcher {
        return Dispatchers.Main
    }
    
    override fun createFixedThreadPoolDispatcher(size: Int): CoroutineDispatcher {
        return Executors.newFixedThreadPool(size).asCoroutineDispatcher()
    }
}

/**
 * представлення інтерфейсу для роботи з каналами
 */
interface ChannelHelper {
    /**
     * створити канал
     *
     * @param capacity місткість
     * @return канал
     */
    fun <T> createChannel(capacity: Int = Channel.RENDEZVOUS): Channel<T>
    
    /**
     * створити буферизований канал
     *
     * @param capacity місткість
     * @return канал
     */
    fun <T> createBufferedChannel(capacity: Int): Channel<T>
    
    /**
     * створити конфлейтний канал
     *
     * @return канал
     */
    fun <T> createConflatedChannel(): Channel<T>
    
    /**
     * створити канал з пріоритетами
     *
     * @param comparator компаратор
     * @return канал
     */
    fun <T> createPriorityChannel(comparator: Comparator<T>): Channel<T>
}

/**
 * представлення базової реалізації помічника з каналами
 */
open class BaseChannelHelper : ChannelHelper {
    
    override fun <T> createChannel(capacity: Int): Channel<T> {
        return Channel(capacity)
    }
    
    override fun <T> createBufferedChannel(capacity: Int): Channel<T> {
        return Channel(capacity)
    }
    
    override fun <T> createConflatedChannel(): Channel<T> {
        return Channel(Channel.CONFLATED)
    }
    
    override fun <T> createPriorityChannel(comparator: Comparator<T>): Channel<T> {
        return Channel(Channel.RENDEZVOUS) // Заглушка, насправді потрібно реалізувати
    }
}

/**
 * представлення інтерфейсу для роботи з акторами
 */
interface ActorHelper {
    /**
     * створити актора
     *
     * @param scope скоуп
     * @param capacity місткість
     * @param block блок обробки повідомлень
     * @return актор
     */
    fun <T> createActor(
        scope: CoroutineScope,
        capacity: Int = Channel.RENDEZVOUS,
        block: suspend ActorScope<T>.(T) -> Unit
    ): SendChannel<T>
    
    /**
     * створити актора з буфером
     *
     * @param scope скоуп
     * @param capacity місткість
     * @param block блок обробки повідомлень
     * @return актор
     */
    fun <T> createBufferedActor(
        scope: CoroutineScope,
        capacity: Int,
        block: suspend ActorScope<T>.(T) -> Unit
    ): SendChannel<T>
}

/**
 * представлення базової реалізації помічника з акторами
 */
open class BaseActorHelper : ActorHelper {
    
    override fun <T> createActor(
        scope: CoroutineScope,
        capacity: Int,
        block: suspend ActorScope<T>.(T) -> Unit
    ): SendChannel<T> {
        return scope.actor(capacity = capacity, block = block)
    }
    
    override fun <T> createBufferedActor(
        scope: CoroutineScope,
        capacity: Int,
        block: suspend ActorScope<T>.(T) -> Unit
    ): SendChannel<T> {
        return scope.actor(capacity = capacity, block = block)
    }
}

/**
 * представлення інтерфейсу для роботи з мутексами
 */
interface MutexHelper {
    /**
     * створити мутекс
     *
     * @param locked чи заблокований
     * @return мутекс
     */
    fun createMutex(locked: Boolean = false): Mutex
    
    /**
     * створити сенд-канал
     *
     * @param capacity місткість
     * @return сенд-канал
     */
    fun <T> createSendChannel(capacity: Int = Channel.RENDEZVOUS): SendChannel<T>
    
    /**
     * створити receive-канал
     *
     * @param capacity місткість
     * @return receive-канал
     */
    fun <T> createReceiveChannel(capacity: Int = Channel.RENDEZVOUS): ReceiveChannel<T>
}

/**
 * представлення базової реалізації помічника з мутексами
 */
open class BaseMutexHelper : MutexHelper {
    
    override fun createMutex(locked: Boolean): Mutex {
        return Mutex(locked)
    }
    
    override fun <T> createSendChannel(capacity: Int): SendChannel<T> {
        return Channel<T>(capacity)
    }
    
    override fun <T> createReceiveChannel(capacity: Int): ReceiveChannel<T> {
        return Channel<T>(capacity)
    }
}

/**
 * представлення інтерфейсу для роботи з селекторами
 */
interface SelectorHelper {
    /**
     * створити селектор
     *
     * @return селектор
     */
    fun createSelector(): SelectBuilder<*>
    
    /**
     * виконати вибір
     *
     * @param builder будівельник селектора
     * @return результат
     */
    suspend fun <R> select(builder: SelectBuilder<R>.() -> Unit): R
}

/**
 * представлення базової реалізації помічника з селекторами
 */
open class BaseSelectorHelper : SelectorHelper {
    
    override fun createSelector(): SelectBuilder<*> {
        // Заглушка, насправді потрібно реалізувати
        return object : SelectBuilder<Any?> {
            override fun <R> SelectBuilder<R>.invoke(block: suspend () -> R) {
                TODO("Реалізація селектора")
            }
        }
    }
    
    override suspend fun <R> select(builder: SelectBuilder<R>.() -> Unit): R {
        return suspendCoroutine { continuation ->
            // Заглушка, насправді потрібно реалізувати
            TODO("Реалізація вибору")
        }
    }
}

/**
 * представлення інтерфейсу для роботи з таймерами
 */
interface TimerHelper {
    /**
     * створити таймер
     *
     * @param delay затримка в мілісекундах
     * @param repeat чи повторювати
     * @param block блок коду
     * @return таймер
     */
    fun createTimer(delay: Long, repeat: Boolean = false, block: () -> Unit): TimerTask
    
    /**
     * створити запланований таймер
     *
     * @param delay затримка в мілісекундах
     * @param period період
     * @param block блок коду
     * @return таймер
     */
    fun createScheduledTimer(delay: Long, period: Long, block: () -> Unit): TimerTask
    
    /**
     * скасувати таймер
     *
     * @param timerTask таймер
     */
    fun cancelTimer(timerTask: TimerTask)
}

/**
 * представлення базової реалізації помічника з таймерами