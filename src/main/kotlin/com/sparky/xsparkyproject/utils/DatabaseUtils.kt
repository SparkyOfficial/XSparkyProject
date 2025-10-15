/**
 * xsparkyproject - розширена бібліотека для kotlin
 *
 * Copyright 2025 Андрій Будильников
 */

package com.sparky.xsparkyproject.utils

import java.sql.*
import java.util.*
import java.util.concurrent.*
import javax.sql.DataSource
import kotlin.reflect.KClass

/**
 * утилітарний клас для роботи з базами даних
 *
 * @author андрій будильников
 * @since 1.0.0
 */
@Suppress("unused")
class DatabaseUtils {
    
    companion object {
        // стандартні драйвери баз даних
        const val DRIVER_POSTGRESQL = "org.postgresql.Driver"
        const val DRIVER_MYSQL = "com.mysql.cj.jdbc.Driver"
        const val DRIVER_SQLITE = "org.sqlite.JDBC"
        const val DRIVER_H2 = "org.h2.Driver"
        const val DRIVER_DERBY = "org.apache.derby.jdbc.EmbeddedDriver"
        
        // стандартні url для баз даних
        const val URL_POSTGRESQL = "jdbc:postgresql://localhost:5432/"
        const val URL_MYSQL = "jdbc:mysql://localhost:3306/"
        const val URL_SQLITE = "jdbc:sqlite:"
        const val URL_H2 = "jdbc:h2:~/"
        const val URL_DERBY = "jdbc:derby:"
        
        // стандартні порти
        const val PORT_POSTGRESQL = 5432
        const val PORT_MYSQL = 3306
        const val PORT_ORACLE = 1521
        const val PORT_SQLSERVER = 1433
        
        // стандартні налаштування пулу з'єднань
        const val DEFAULT_POOL_SIZE = 10
        const val DEFAULT_MAX_POOL_SIZE = 20
        const val DEFAULT_MIN_POOL_SIZE = 5
        const val DEFAULT_CONNECTION_TIMEOUT = 30000
        const val DEFAULT_IDLE_TIMEOUT = 600000
        const val DEFAULT_MAX_LIFETIME = 1800000
    }
    
    // базові функції для роботи з jdbc
    
    /**
     * представлення з'єднання з базою даних
     */
    interface DatabaseConnection : AutoCloseable {
        /**
         * отримує jdbc з'єднання
         *
         * @return jdbc з'єднання
         */
        fun getConnection(): Connection
        
        /**
         * перевіряє, чи з'єднання активне
         *
         * @return true якщо з'єднання активне
         */
        fun isValid(): Boolean
        
        /**
         * закриває з'єднання
         */
        override fun close()
    }
    
    /**
     * базова реалізація з'єднання з базою даних
     */
    open class BaseDatabaseConnection(protected val connection: Connection) : DatabaseConnection {
        override fun getConnection(): Connection {
            return connection
        }
        
        override fun isValid(): Boolean {
            return try {
                connection.isValid(5)
            } catch (e: SQLException) {
                false
            }
        }
        
        override fun close() {
            try {
                connection.close()
            } catch (e: SQLException) {
                // ігноруємо помилки закриття
            }
        }
    }
    
    /**
     * представлення пулу з'єднань з базою даних
     */
    interface ConnectionPool : AutoCloseable {
        /**
         * отримує з'єднання з пулу
         *
         * @return з'єднання
         */
        fun getConnection(): DatabaseConnection
        
        /**
         * повертає з'єднання в пул
         *
         * @param connection з'єднання
         */
        fun releaseConnection(connection: DatabaseConnection)
        
        /**
         * отримує кількість активних з'єднань
         *
         * @return кількість активних з'єднань
         */
        fun getActiveConnections(): Int
        
        /**
         * отримує кількість вільних з'єднань
         *
         * @return кількість вільних з'єднань
         */
        fun getAvailableConnections(): Int
        
        /**
         * закриває пул
         */
        override fun close()
    }
    
    /**
     * базова реалізація пулу з'єднань
     */
    class BasicConnectionPool(
        private val dataSource: DataSource,
        private val poolSize: Int = DEFAULT_POOL_SIZE
    ) : ConnectionPool {
        private val availableConnections = ArrayBlockingQueue<DatabaseConnection>(poolSize)
        private val usedConnections = mutableListOf<DatabaseConnection>()
        private val poolLock = Any()
        private var isClosed = false
        
        init {
            // ініціалізуємо пул з'єднань
            for (i in 0 until poolSize) {
                try {
                    val connection = dataSource.connection
                    availableConnections.offer(BaseDatabaseConnection(connection))
                } catch (e: SQLException) {
                    // ігноруємо помилки створення з'єднань
                }
            }
        }
        
        override fun getConnection(): DatabaseConnection {
            synchronized(poolLock) {
                if (isClosed) {
                    throw IllegalStateException("Пул з'єднань закрито")
                }
                
                var connection = availableConnections.poll()
                if (connection == null) {
                    // якщо немає вільних з'єднань, створюємо нове
                    try {
                        connection = BaseDatabaseConnection(dataSource.connection)
                    } catch (e: SQLException) {
                        throw DatabaseException("Не вдалося створити з'єднання", e)
                    }
                }
                
                usedConnections.add(connection)
                return connection
            }
        }
        
        override fun releaseConnection(connection: DatabaseConnection) {
            synchronized(poolLock) {
                if (isClosed) return
                
                if (usedConnections.remove(connection)) {
                    if (connection.isValid()) {
                        availableConnections.offer(connection)
                    } else {
                        // якщо з'єднання недійсне, закриваємо його і створюємо нове
                        try {
                            connection.close()
                            val newConnection = BaseDatabaseConnection(dataSource.connection)
                            availableConnections.offer(newConnection)
                        } catch (e: SQLException) {
                            // ігноруємо помилки
                        }
                    }
                }
            }
        }
        
        override fun getActiveConnections(): Int {
            synchronized(poolLock) {
                return usedConnections.size
            }
        }
        
        override fun getAvailableConnections(): Int {
            synchronized(poolLock) {
                return availableConnections.size
            }
        }
        
        override fun close() {
            synchronized(poolLock) {
                if (isClosed) return
                
                isClosed = true
                
                // закриваємо всі з'єднання
                availableConnections.forEach { it.close() }
                usedConnections.forEach { it.close() }
                
                availableConnections.clear()
                usedConnections.clear()
            }
        }
    }
    
    /**
     * представлення конфігурації бази даних
     *
     * @property driver драйвер бази даних
     * @property url url бази даних
     * @property username ім'я користувача
     * @property password пароль
     */
    data class DatabaseConfig(
        val driver: String,
        val url: String,
        val username: String,
        val password: String
    )
    
    /**
     * представлення пулу з'єднань з додатковими налаштуваннями
     *
     * @property config конфігурація бази даних
     * @property minPoolSize мінімальний розмір пулу
     * @property maxPoolSize максимальний розмір пулу
     * @property connectionTimeout таймаут з'єднання
     * @property idleTimeout таймаут бездіяльності
     * @property maxLifetime максимальний час життя з'єднання
     */
    class AdvancedConnectionPool(
        private val config: DatabaseConfig,
        private val minPoolSize: Int = DEFAULT_MIN_POOL_SIZE,
        private val maxPoolSize: Int = DEFAULT_MAX_POOL_SIZE,
        private val connectionTimeout: Long = DEFAULT_CONNECTION_TIMEOUT.toLong(),
        private val idleTimeout: Long = DEFAULT_IDLE_TIMEOUT.toLong(),
        private val maxLifetime: Long = DEFAULT_MAX_LIFETIME.toLong()
    ) : ConnectionPool {
        private val availableConnections = ArrayBlockingQueue<DatabaseConnection>(maxPoolSize)
        private val usedConnections = mutableMapOf<DatabaseConnection, Long>() // з'єднання та час отримання
        private val poolLock = Any()
        private var isClosed = false
        private val executorService = Executors.newScheduledThreadPool(2)
        
        init {
            // ініціалізуємо мінімальний пул з'єднань
            for (i in 0 until minPoolSize) {
                createConnection()?.let { availableConnections.offer(it) }
            }
            
            // запускаємо задачу для очищення прострочених з'єднань
            executorService.scheduleWithFixedDelay(
                { cleanupExpiredConnections() },
                idleTimeout,
                idleTimeout,
                TimeUnit.MILLISECONDS
            )
        }
        
        /**
         * створює нове з'єднання
         *
         * @return з'єднання або null якщо не вдалося створити
         */
        private fun createConnection(): DatabaseConnection? {
            return try {
                Class.forName(config.driver)
                val connection = DriverManager.getConnection(
                    config.url,
                    config.username,
                    config.password
                )
                BaseDatabaseConnection(connection)
            } catch (e: Exception) {
                null
            }
        }
        
        override fun getConnection(): DatabaseConnection {
            synchronized(poolLock) {
                if (isClosed) {
                    throw IllegalStateException("Пул з'єднань закрито")
                }
                
                var connection = availableConnections.poll()
                
                // якщо немає вільних з'єднань, створюємо нове (якщо не перевищено максимальний розмір)
                if (connection == null && usedConnections.size + availableConnections.size < maxPoolSize) {
                    connection = createConnection()
                }
                
                if (connection == null) {
                    throw DatabaseException("Немає доступних з'єднань")
                }
                
                usedConnections[connection] = System.currentTimeMillis()
                return connection
            }
        }
        
        override fun releaseConnection(connection: DatabaseConnection) {
            synchronized(poolLock) {
                if (isClosed) return
                
                if (usedConnections.remove(connection) != null) {
                    if (connection.isValid()) {
                        availableConnections.offer(connection)
                    } else {
                        // якщо з'єднання недійсне, закриваємо його
                        try {
                            connection.close()
                            // створюємо нове з'єднання для заміни
                            createConnection()?.let { availableConnections.offer(it) }
                        } catch (e: Exception) {
                            // ігноруємо помилки
                        }
                    }
                }
            }
        }
        
        override fun getActiveConnections(): Int {
            synchronized(poolLock) {
                return usedConnections.size
            }
        }
        
        override fun getAvailableConnections(): Int {
            synchronized(poolLock) {
                return availableConnections.size
            }
        }
        
        override fun close() {
            synchronized(poolLock) {
                if (isClosed) return
                
                isClosed = true
                
                // закриваємо всі з'єднання
                availableConnections.forEach { it.close() }
                usedConnections.keys.forEach { it.close() }
                
                availableConnections.clear()
                usedConnections.clear()
                
                executorService.shutdown()
            }
        }
        
        /**
         * очищує прострочені з'єднання
         */
        private fun cleanupExpiredConnections() {
            synchronized(poolLock) {
                if (isClosed) return
                
                val currentTime = System.currentTimeMillis()
                val expiredConnections = mutableListOf<DatabaseConnection>()
                
                // знаходимо прострочені з'єднання серед використовуваних
                usedConnections.entries.removeIf { entry ->
                    val (connection, timestamp) = entry
                    if (currentTime - timestamp > maxLifetime) {
                        expiredConnections.add(connection)
                        true
                    } else {
                        false
                    }
                }
                
                // закриваємо прострочені з'єднання
                expiredConnections.forEach { connection ->
                    try {
                        connection.close()
                    } catch (e: Exception) {
                        // ігноруємо помилки
                    }
                }
                
                // очищуємо прострочені з'єднання серед вільних
                val availableIterator = availableConnections.iterator()
                while (availableIterator.hasNext()) {
                    val connection = availableIterator.next()
                    if (!connection.isValid()) {
                        try {
                            connection.close()
                            availableIterator.remove()
                        } catch (e: Exception) {
                            // ігноруємо помилки
                        }
                    }
                }
            }
        }
    }
    
    /**
     * створює пул з'єднань
     *
     * @param dataSource джерело даних
     * @param poolSize розмір пулу
     * @return пул з'єднань
     */
    fun createConnectionPool(dataSource: DataSource, poolSize: Int = DEFAULT_POOL_SIZE): ConnectionPool {
        return BasicConnectionPool(dataSource, poolSize)
    }
    
    /**
     * створює розширений пул з'єднань
     *
     * @param config конфігурація бази даних
     * @param minPoolSize мінімальний розмір пулу
     * @param maxPoolSize максимальний розмір пулу
     * @param connectionTimeout таймаут з'єднання
     * @param idleTimeout таймаут бездіяльності
     * @param maxLifetime максимальний час життя з'єднання
     * @return пул з'єднань
     */
    fun createAdvancedConnectionPool(
        config: DatabaseConfig,
        minPoolSize: Int = DEFAULT_MIN_POOL_SIZE,
        maxPoolSize: Int = DEFAULT_MAX_POOL_SIZE,
        connectionTimeout: Long = DEFAULT_CONNECTION_TIMEOUT.toLong(),
        idleTimeout: Long = DEFAULT_IDLE_TIMEOUT.toLong(),
        maxLifetime: Long = DEFAULT_MAX_LIFETIME.toLong()
    ): ConnectionPool {
        return AdvancedConnectionPool(
            config,
            minPoolSize,
            maxPoolSize,
            connectionTimeout,
            idleTimeout,
            maxLifetime
        )
    }
    
    // функції для роботи з транзакціями
    
    /**
     * представлення транзакції бази даних
     */
    interface DatabaseTransaction : AutoCloseable {
        /**
         * починає транзакцію
         */
        fun begin()
        
        /**
         * фіксує транзакцію
         */
        fun commit()
        
        /**
         * скасовує транзакцію
         */
        fun rollback()
        
        /**
         * отримує з'єднання для транзакції
         *
         * @return з'єднання
         */
        fun getConnection(): Connection
        
        /**
         * закриває транзакцію
         */
        override fun close()
    }
    
    /**
     * базова реалізація транзакції
     */
    class BasicDatabaseTransaction(private val connection: Connection) : DatabaseTransaction {
        private var isClosed = false
        
        override fun begin() {
            if (isClosed) {
                throw IllegalStateException("Транзакцію закрито")
            }
            
            try {
                connection.autoCommit = false
            } catch (e: SQLException) {
                throw DatabaseException("Не вдалося почати транзакцію", e)
            }
        }
        
        override fun commit() {
            if (isClosed) {
                throw IllegalStateException("Транзакцію закрито")
            }
            
            try {
                connection.commit()
            } catch (e: SQLException) {
                throw DatabaseException("Не вдалося зафіксувати транзакцію", e)
            } finally {
                try {
                    connection.autoCommit = true
                } catch (e: SQLException) {
                    // ігноруємо помилки
                }
            }
        }
        
        override fun rollback() {
            if (isClosed) {
                throw IllegalStateException("Транзакцію закрито")
            }
            
            try {
                connection.rollback()
            } catch (e: SQLException) {
                throw DatabaseException("Не вдалося скасувати транзакцію", e)
            } finally {
                try {
                    connection.autoCommit = true
                } catch (e: SQLException) {
                    // ігноруємо помилки
                }
            }
        }
        
        override fun getConnection(): Connection {
            if (isClosed) {
                throw IllegalStateException("Транзакцію закрито")
            }
            
            return connection
        }
        
        override fun close() {
            if (isClosed) return
            
            isClosed = true
            
            try {
                connection.autoCommit = true
                connection.close()
            } catch (e: SQLException) {
                // ігноруємо помилки закриття
            }
        }
    }
    
    /**
     * представлення менеджера транзакцій
     */
    class TransactionManager(private val connectionPool: ConnectionPool) {
        private val threadLocalTransaction = ThreadLocal<DatabaseTransaction>()
        
        /**
         * виконує код в контексті транзакції
         *
         * @param block код для виконання
         * @return результат виконання коду
         */
        fun <T> executeInTransaction(block: (Connection) -> T): T {
            val existingTransaction = threadLocalTransaction.get()
            
            return if (existingTransaction != null) {
                // якщо транзакція вже існує, використовуємо її
                block(existingTransaction.getConnection())
            } else {
                // створюємо нову транзакцію
                val connection = connectionPool.getConnection()
                val transaction = BasicDatabaseTransaction(connection.getConnection())
                
                try {
                    threadLocalTransaction.set(transaction)
                    transaction.begin()
                    val result = block(transaction.getConnection())
                    transaction.commit()
                    result
                } catch (e: Exception) {
                    try {
                        transaction.rollback()
                    } catch (rollbackException: Exception) {
                        // ігноруємо помилки скасування
                    }
                    throw e
                } finally {
                    threadLocalTransaction.remove()
                    try {
                        transaction.close()
                        connectionPool.releaseConnection(connection)
                    } catch (e: Exception) {
                        // ігноруємо помилки закриття
                    }
                }
            }
        }
        
        /**
         * отримує поточну транзакцію
         *
         * @return транзакція або null якщо немає активної транзакції
         */
        fun getCurrentTransaction(): DatabaseTransaction? {
            return threadLocalTransaction.get()
        }
    }
    
    /**
     * створює менеджер транзакцій
     *
     * @param connectionPool пул з'єднань
     * @return менеджер транзакцій
     */
    fun createTransactionManager(connectionPool: ConnectionPool): TransactionManager {
        return TransactionManager(connectionPool)
    }
    
    // функції для роботи з sql запитами
    
    /**
     * представлення sql запиту
     */
    class SqlQuery(private val connection: Connection) {
        private var statement: PreparedStatement? = null
        private var isClosed = false
        
        /**
         * підготовлює sql запит
         *
         * @param sql sql запит
         * @return sql запит
         */
        fun prepare(sql: String): SqlQuery {
            if (isClosed) {
                throw IllegalStateException("Запит закрито")
            }
            
            try {
                statement = connection.prepareStatement(sql)
            } catch (e: SQLException) {
                throw DatabaseException("Не вдалося підготувати запит", e)
            }
            
            return this
        }
        
        /**
         * встановлює параметр запиту
         *
         * @param index індекс параметра
         * @param value значення параметра
         * @return sql запит
         */
        fun setParameter(index: Int, value: Any?): SqlQuery {
            if (isClosed || statement == null) {
                throw IllegalStateException("Запит не підготовлено або закрито")
            }
            
            try {
                when (value) {
                    null -> statement?.setNull(index, Types.NULL)
                    is String -> statement?.setString(index, value)
                    is Int -> statement?.setInt(index, value)
                    is Long -> statement?.setLong(index, value)
                    is Double -> statement?.setDouble(index, value)
                    is Float -> statement?.setFloat(index, value)
                    is Boolean -> statement?.setBoolean(index, value)
                    is Date -> statement?.setDate(index, value)
                    is Timestamp -> statement?.setTimestamp(index, value)
                    else -> statement?.setObject(index, value)
                }
            } catch (e: SQLException) {
                throw DatabaseException("Не вдалося встановити параметр", e)
            }
            
            return this
        }
        
        /**
         * виконує запит на вибірку
         *
         * @return результат запиту
         */
        fun executeQuery(): SqlResult {
            if (isClosed || statement == null) {
                throw IllegalStateException("Запит не підготовлено або закрито")
            }
            
            return try {
                val resultSet = statement?.executeQuery()
                SqlResult(resultSet)
            } catch (e: SQLException) {
                throw DatabaseException("Не вдалося виконати запит", e)
            }
        }
        
        /**
         * виконує запит на оновлення
         *
         * @return кількість змінених рядків
         */
        fun executeUpdate(): Int {
            if (isClosed || statement == null) {
                throw IllegalStateException("Запит не підготовлено або закрито")
            }
            
            return try {
                statement?.executeUpdate() ?: 0
            } catch (e: SQLException) {
                throw DatabaseException("Не вдалося виконати запит", e)
            }
        }
        
        /**
         * виконує запит на вставку та повертає згенерований ключ
         *
         * @return згенерований ключ або -1 якщо ключ не згенеровано
         */
        fun executeInsert(): Long {
            if (isClosed || statement == null) {
                throw IllegalStateException("Запит не підготовлено або закрито")
            }
            
            return try {
                val result = statement?.executeUpdate()
                if (result != null && result > 0) {
                    val generatedKeys = statement?.generatedKeys
                    if (generatedKeys?.next() == true) {
                        generatedKeys.getLong(1)
                    } else {
                        -1L
                    }
                } else {
                    -1L
                }
            } catch (e: SQLException) {
                throw DatabaseException("Не вдалося виконати запит", e)
            }
        }
        
        /**
         * закриває запит
         */
        fun close() {
            if (isClosed) return
            
            isClosed = true
            
            try {
                statement?.close()
            } catch (e: SQLException) {
                // ігноруємо помилки закриття
            } finally {
                statement = null
            }
        }
    }
    
    /**
     * представлення результату sql запиту
     */
    class SqlResult(private val resultSet: ResultSet?) : AutoCloseable {
        private var isClosed = false
        
        /**
         * перевіряє, чи є наступний рядок
         *
         * @return true якщо є наступний рядок
         */
        fun next(): Boolean {
            if (isClosed || resultSet == null) {
                return false
            }
            
            return try {
                resultSet.next()
            } catch (e: SQLException) {
                throw DatabaseException("Не вдалося перейти до наступного рядка", e)
            }
        }
        
        /**
         * отримує значення стовпця за індексом
         *
         * @param index індекс стовпця
         * @return значення стовпця
         */
        fun getObject(index: Int): Any? {
            if (isClosed || resultSet == null) {
                throw IllegalStateException("Результат закрито")
            }
            
            return try {
                resultSet.getObject(index)
            } catch (e: SQLException) {
                throw DatabaseException("Не вдалося отримати значення", e)
            }
        }
        
        /**
         * отримує значення стовпця за назвою
         *
         * @param name назва стовпця
         * @return значення стовпця
         */
        fun getObject(name: String): Any? {
            if (isClosed || resultSet == null) {
                throw IllegalStateException("Результат закрито")
            }
            
            return try {
                resultSet.getObject(name)
            } catch (e: SQLException) {
                throw DatabaseException("Не вдалося отримати значення", e)
            }
        }
        
        /**
         * отримує значення стовпця типу String за індексом
         *
         * @param index індекс стовпця
         * @return значення стовпця
         */
        fun getString(index: Int): String? {
            return getObject(index) as? String
        }
        
        /**
         * отримує значення стовпця типу String за назвою
         *
         * @param name назва стовпця
         * @return значення стовпця
         */
        fun getString(name: String): String? {
            return getObject(name) as? String
        }
        
        /**
         * отримує значення стовпця типу Int за індексом
         *
         * @param index індекс стовпця
         * @return значення стовпця
         */
        fun getInt(index: Int): Int {
            return try {
                getObject(index) as? Int ?: 0
            } catch (e: Exception) {
                0
            }
        }
        
        /**
         * отримує значення стовпця типу Int за назвою
         *
         * @param name назва стовпця
         * @return значення стовпця
         */
        fun getInt(name: String): Int {
            return try {
                getObject(name) as? Int ?: 0
            } catch (e: Exception) {
                0
            }
        }
        
        /**
         * отримує значення стовпця типу Long за індексом
         *
         * @param index індекс стовпця
         * @return значення стовпця
         */
        fun getLong(index: Int): Long {
            return try {
                getObject(index) as? Long ?: 0L
            } catch (e: Exception) {
                0L
            }
        }
        
        /**
         * отримує значення стовпця типу Long за назвою
         *
         * @param name назва стовпця
         * @return значення стовпця
         */
        fun getLong(name: String): Long {
            return try {
                getObject(name) as? Long ?: 0L
            } catch (e: Exception) {
                0L
            }
        }
        
        /**
         * отримує значення стовпця типу Double за індексом
         *
         * @param index індекс стовпця
         * @return значення стовпця
         */
        fun getDouble(index: Int): Double {
            return try {
                getObject(index) as? Double ?: 0.0
            } catch (e: Exception) {
                0.0
            }
        }
        
        /**
         * отримує значення стовпця типу Double за назвою
         *
         * @param name назва стовпця
         * @return значення стовпця
         */
        fun getDouble(name: String): Double {
            return try {
                getObject(name) as? Double ?: 0.0
            } catch (e: Exception) {
                0.0
            }
        }
        
        /**
         * отримує значення стовпця типу Boolean за індексом
         *
         * @param index індекс стовпця
         * @return значення стовпця
         */
        fun getBoolean(index: Int): Boolean {
            return try {
                getObject(index) as? Boolean ?: false
            } catch (e: Exception) {
                false
            }
        }
        
        /**
         * отримує значення стовпця типу Boolean за назвою
         *
         * @param name назва стовпця
         * @return значення стовпця
         */
        fun getBoolean(name: String): Boolean {
            return try {
                getObject(name) as? Boolean ?: false
            } catch (e: Exception) {
                false
            }
        }
        
        /**
         * отримує значення стовпця типу Date за індексом
         *
         * @param index індекс стовпця
         * @return значення стовпця
         */
        fun getDate(index: Int): Date? {
            return getObject(index) as? Date
        }
        
        /**
         * отримує значення стовпця типу Date за назвою
         *
         * @param name назва стовпця
         * @return значення стовпця
         */
        fun getDate(name: String): Date? {
            return getObject(name) as? Date
        }
        
        /**
         * отримує значення стовпця типу Timestamp за індексом
         *
         * @param index індекс стовпця
         * @return значення стовпця
         */
        fun getTimestamp(index: Int): Timestamp? {
            return getObject(index) as? Timestamp
        }
        
        /**
         * отримує значення стовпця типу Timestamp за назвою
         *
         * @param name назва стовпця
         * @return значення стовпця
         */
        fun getTimestamp(name: String): Timestamp? {
            return getObject(name) as? Timestamp
        }
        
        /**
         * закриває результат
         */
        override fun close() {
            if (isClosed) return
            
            isClosed = true
            
            try {
                resultSet?.close()
            } catch (e: SQLException) {
                // ігноруємо помилки закриття
            }
        }
    }
    
    /**
     * створює sql запит
     *
     * @param connection з'єднання
     * @return sql запит
     */
    fun createSqlQuery(connection: Connection): SqlQuery {
        return SqlQuery(connection)
    }
    
    // функції для роботи з orm
    
    /**
     * анотація для позначення таблиці бази даних
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Table(val name: String)
    
    /**
     * анотація для позначення стовпця бази даних
     */
    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Column(
        val name: String = "",
        val nullable: Boolean = true,
        val unique: Boolean = false,
        val length: Int = 255
    )
    
    /**
     * анотація для позначення первинного ключа
     */
    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Id(val autoGenerate: Boolean = true)
    
    /**
     * представлення мапера об'єктів бази даних
     *
     * @param T тип сутності
     * @property entityClass клас сутності
     */
    class ObjectMapper<T : Any>(private val entityClass: KClass<T>) {
        
        /**
         * отримує назву таблиці
         *
         * @return назва таблиці
         */
        fun getTableName(): String {
            val tableAnnotation = entityClass.annotations.find { it is Table } as? Table
            return tableAnnotation?.name ?: entityClass.simpleName ?: ""
        }
        
        /**
         * отримує мапу стовпців
         *
         * @return мапа стовпців
         */
        fun getColumnMap(): Map<String, String> {
            val columnMap = mutableMapOf<String, String>()
            
            // в реальному застосунку тут потрібно використовувати рефлексію для отримання полів класу
            // та їх анотацій
            
            return columnMap
        }
        
        /**
         * створює сутність з результату запиту
         *
         * @param result результат запиту
         * @return сутність
         */
        fun createEntity(result: SqlResult): T? {
            // в реальному застосунку тут потрібно використовувати рефлексію для створення екземпляра класу
            // та заповнення його полів значеннями з результату запиту
            throw NotImplementedError("Ця функція не реалізована в спрощеній версії")
        }
        
        /**
         * отримує sql запит для вставки сутності
         *
         * @param entity сутність
         * @return sql запит
         */
        fun getInsertSql(entity: T): String {
            // в реальному застосунку тут потрібно генерувати sql запит для вставки сутності
            throw NotImplementedError("Ця функція не реалізована в спрощеній версії")
        }
        
        /**
         * отримує sql запит для оновлення сутності
         *
         * @param entity сутність
         * @return sql запит
         */
        fun getUpdateSql(entity: T): String {
            // в реальному застосунку тут потрібно генерувати sql запит для оновлення сутності
            throw NotImplementedError("Ця функція не реалізована в спрощеній версії")
        }
        
        /**
         * отримує sql запит для видалення сутності
         *
         * @param entity сутність
         * @return sql запит
         */
        fun getDeleteSql(entity: T): String {
            // в реальному застосунку тут потрібно генерувати sql запит для видалення сутності
            throw NotImplementedError("Ця функція не реалізована в спрощеній версії")
        }
        
        /**
         * отримує sql запит для вибірки сутності за id
         *
         * @param id id сутності
         * @return sql запит
         */
        fun getSelectByIdSql(id: Any): String {
            // в реальному застосунку тут потрібно генерувати sql запит для вибірки сутності за id
            throw NotImplementedError("Ця функція не реалізована в спрощеній версії")
        }
    }
    
    /**
     * створює мапер об'єктів
     *
     * @param entityClass клас сутності
     * @return мапер об'єктів
     */
    fun <T : Any> createObjectMapper(entityClass: KClass<T>): ObjectMapper<T> {
        return ObjectMapper(entityClass)
    }
    
    // функції для роботи з репозиторіями
    
    /**
     * представлення репозиторію
     *
     * @param T тип сутності
     * @property connectionPool пул з'єднань
     * @property objectMapper мапер об'єктів
     */
    class Repository<T : Any>(
        private val connectionPool: ConnectionPool,
        private val objectMapper: ObjectMapper<T>
    ) {
        
        /**
         * зберігає сутність
         *
         * @param entity сутність
         * @return збережена сутність
         */
        fun save(entity: T): T {
            val connection = connectionPool.getConnection()
            try {
                val sql = if (isNewEntity(entity)) {
                    objectMapper.getInsertSql(entity)
                } else {
                    objectMapper.getUpdateSql(entity)
                }
                
                val query = createSqlQuery(connection.getConnection()).prepare(sql)
                // в реальному застосунку тут потрібно встановити параметри запиту
                query.executeUpdate()
                
                return entity
            } finally {
                connectionPool.releaseConnection(connection)
            }
        }
        
        /**
         * видаляє сутність
         *
         * @param entity сутність
         */
        fun delete(entity: T) {
            val connection = connectionPool.getConnection()
            try {
                val sql = objectMapper.getDeleteSql(entity)
                val query = createSqlQuery(connection.getConnection()).prepare(sql)
                query.executeUpdate()
            } finally {
                connectionPool.releaseConnection(connection)
            }
        }
        
        /**
         * отримує сутність за id
         *
         * @param id id сутності
         * @return сутність або null якщо не знайдено
         */
        fun findById(id: Any): T? {
            val connection = connectionPool.getConnection()
            try {
                val sql = objectMapper.getSelectByIdSql(id)
                val query = createSqlQuery(connection.getConnection()).prepare(sql)
                val result = query.executeQuery()
                
                return if (result.next()) {
                    objectMapper.createEntity(result)
                } else {
                    null
                }
            } finally {
                connectionPool.releaseConnection(connection)
            }
        }
        
        /**
         * отримує всі сутності
         *
         * @return список сутностей
         */
        fun findAll(): List<T> {
            val connection = connectionPool.getConnection()
            val entities = mutableListOf<T>()
            
            try {
                val tableName = objectMapper.getTableName()
                val sql = "SELECT * FROM $tableName"
                val query = createSqlQuery(connection.getConnection()).prepare(sql)
                val result = query.executeQuery()
                
                while (result.next()) {
                    val entity = objectMapper.createEntity(result)
                    if (entity != null) {
                        entities.add(entity)
                    }
                }
            } finally {
                connectionPool.releaseConnection(connection)
            }
            
            return entities
        }
        
        /**
         * перевіряє, чи сутність є новою
         *
         * @param entity сутність
         * @return true якщо сутність є новою
         */
        private fun isNewEntity(entity: T): Boolean {
            // в реальному застосунку тут потрібно перевірити, чи сутність є новою
            // (наприклад, чи встановлено значення первинного ключа)
            return true
        }
    }
    
    /**
     * створює репозиторій
     *
     * @param connectionPool пул з'єднань
     * @param objectMapper мапер об'єктів
     * @return репозиторій
     */
    fun <T : Any> createRepository(
        connectionPool: ConnectionPool,
        objectMapper: ObjectMapper<T>
    ): Repository<T> {
        return Repository(connectionPool, objectMapper)
    }
    
    // функції для роботи з міграціями бази даних
    
    /**
     * представлення міграції бази даних
     */
    abstract class DatabaseMigration(
        val version: Int,
        val description: String
    ) {
        /**
         * виконує міграцію
         *
         * @param connection з'єднання
         */
        abstract fun up(connection: Connection)
        
        /**
         * скасовує міграцію
         *
         * @param connection з'єднання
         */
        abstract fun down(connection: Connection)
    }
    
    /**
     * представлення менеджера міграцій
     */
    class MigrationManager(private val connectionPool: ConnectionPool) {
        private val migrations = mutableMapOf<Int, DatabaseMigration>()
        
        /**
         * додає міграцію
         *
         * @param migration міграція
         */
        fun addMigration(migration: DatabaseMigration) {
            migrations[migration.version] = migration
        }
        
        /**
         * виконує всі незастосовані міграції
         */
        fun migrate() {
            val connection = connectionPool.getConnection()
            try {
                val currentVersion = getCurrentVersion(connection.getConnection())
                val sortedMigrations = migrations.values.sortedBy { it.version }
                
                sortedMigrations.forEach { migration ->
                    if (migration.version > currentVersion) {
                        try {
                            migration.up(connection.getConnection())
                            updateVersion(connection.getConnection(), migration.version)
                        } catch (e: Exception) {
                            throw DatabaseException("Не вдалося виконати міграцію ${migration.version}: ${migration.description}", e)
                        }
                    }
                }
            } finally {
                connectionPool.releaseConnection(connection)
            }
        }
        
        /**
         * скасовує міграції до певної версії
         *
         * @param targetVersion цільова версія
         */
        fun rollbackTo(targetVersion: Int) {
            val connection = connectionPool.getConnection()
            try {
                val currentVersion = getCurrentVersion(connection.getConnection())
                val sortedMigrations = migrations.values.sortedByDescending { it.version }
                
                sortedMigrations.forEach { migration ->
                    if (migration.version <= currentVersion && migration.version > targetVersion) {
                        try {
                            migration.down(connection.getConnection())
                            updateVersion(connection.getConnection(), migration.version - 1)
                        } catch (e: Exception) {
                            throw DatabaseException("Не вдалося скасувати міграцію ${migration.version}: ${migration.description}", e)
                        }
                    }
                }
            } finally {
                connectionPool.releaseConnection(connection)
            }
        }
        
        /**
         * отримує поточну версію бази даних
         *
         * @param connection з'єднання
         * @return поточна версія
         */
        private fun getCurrentVersion(connection: Connection): Int {
            return try {
                // створюємо таблицю для відстеження версій, якщо її ще немає
                val createTableSql = """
                    CREATE TABLE IF NOT EXISTS schema_version (
                        version INT PRIMARY KEY,
                        description VARCHAR(255),
                        applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """.trimIndent()
                
                val createTableQuery = createSqlQuery(connection).prepare(createTableSql)
                createTableQuery.executeUpdate()
                
                // отримуємо поточну версію
                val selectSql = "SELECT MAX(version) FROM schema_version"
                val selectQuery = createSqlQuery(connection).prepare(selectSql)
                val result = selectQuery.executeQuery()
                
                if (result.next()) {
                    result.getInt(1)
                } else {
                    0
                }
            } catch (e: SQLException) {
                0
            }
        }
        
        /**
         * оновлює версію бази даних
         *
         * @param connection з'єднання
         * @param version версія
         */
        private fun updateVersion(connection: Connection, version: Int) {
            try {
                val insertSql = """
                    INSERT INTO schema_version (version, description) 
                    VALUES (?, ?)
                    ON CONFLICT(version) DO UPDATE SET description = ?
                """.trimIndent()
                
                val insertQuery = createSqlQuery(connection).prepare(insertSql)
                // в реальному застосунку тут потрібно встановити параметри запиту
                insertQuery.executeUpdate()
            } catch (e: SQLException) {
                // ігноруємо помилки оновлення версії
            }
        }
    }
    
    /**
     * створює менеджер міграцій
     *
     * @param connectionPool пул з'єднань
     * @return менеджер міграцій
     */
    fun createMigrationManager(connectionPool: ConnectionPool): MigrationManager {
        return MigrationManager(connectionPool)
    }
    
    // функції для роботи з кешуванням запитів
    
    /**
     * представлення кешу запитів
     */
    class QueryCache {
        private val cache = mutableMapOf<String, CachedResult>()
        private val cacheLock = Any()
        private val maxSize = 1000
        private val defaultTtl = 300000L // 5 хвилин
        
        /**
         * представлення кешованого результату
         *
         * @property result результат
         * @property timestamp час кешування
         * @property ttl час життя в мілісекундах
         */
        data class CachedResult(val result: List<Map<String, Any?>>, val timestamp: Long, val ttl: Long) {
            /**
             * перевіряє, чи кеш ще дійсний
             *
             * @return true якщо кеш ще дійсний
             */
            fun isExpired(): Boolean {
                return System.currentTimeMillis() - timestamp > ttl
            }
        }
        
        /**
         * отримує результат з кешу
         *
         * @param key ключ кешу
         * @return результат або null якщо не знайдено або прострочено
         */
        fun get(key: String): List<Map<String, Any?>>? {
            synchronized(cacheLock) {
                val cached = cache[key]
                return if (cached != null && !cached.isExpired()) {
                    cached.result
                } else {
                    cache.remove(key)
                    null
                }
            }
        }
        
        /**
         * зберігає результат у кеш
         *
         * @param key ключ кешу
         * @param result результат
         * @param ttl час життя в мілісекундах
         */
        fun put(key: String, result: List<Map<String, Any?>>, ttl: Long = defaultTtl) {
            synchronized(cacheLock) {
                // видаляємо найстаріші записи, якщо кеш переповнено
                if (cache.size >= maxSize) {
                    val oldestKey = cache.entries.minByOrNull { it.value.timestamp }?.key
                    if (oldestKey != null) {
                        cache.remove(oldestKey)
                    }
                }
                
                cache[key] = CachedResult(result, System.currentTimeMillis(), ttl)
            }
        }
        
        /**
         * видаляє запис з кешу
         *
         * @param key ключ кешу
         */
        fun remove(key: String) {
            synchronized(cacheLock) {
                cache.remove(key)
            }
        }
        
        /**
         * очищує кеш
         */
        fun clear() {
            synchronized(cacheLock) {
                cache.clear()
            }
        }
        
        /**
         * отримує розмір кешу
         *
         * @return розмір кешу
         */
        fun size(): Int {
            synchronized(cacheLock) {
                return cache.size
            }
        }
        
        /**
         * очищує прострочені записи
         */
        fun cleanup() {
            synchronized(cacheLock) {
                val expiredKeys = cache.entries.filter { it.value.isExpired() }.map { it.key }
                expiredKeys.forEach { cache.remove(it) }
            }
        }
    }
    
    /**
     * представлення репозиторію з кешуванням
     *
     * @param T тип сутності
     * @property delegate делегат репозиторію
     * @property queryCache кеш запитів
     */
    class CachedRepository<T : Any>(
        private val delegate: Repository<T>,
        private val queryCache: QueryCache
    ) {
        // в реальному застосунку тут потрібно реалізувати кешування результатів запитів
        // для різних операцій репозиторію
    }
    
    /**
     * створює репозиторій з кешуванням
     *
     * @param delegate делегат репозиторію
     * @param queryCache кеш запитів
     * @return репозиторій з кешуванням
     */
    fun <T : Any> createCachedRepository(
        delegate: Repository<T>,
        queryCache: QueryCache
    ): CachedRepository<T> {
        return CachedRepository(delegate, queryCache)
    }
    
    // функції для роботи з моніторингом бази даних
    
    /**
     * представлення статистики бази даних
     *
     * @property totalConnections загальна кількість з'єднань
     * @property activeConnections кількість активних з'єднань
     * @property idleConnections кількість вільних з'єднань
     * @property totalQueries загальна кількість запитів
     * @property slowQueries кількість повільних запитів
     * @property averageQueryTime середній час виконання запиту
     * @property maxQueryTime максимальний час виконання запиту
     * @property timestamp час отримання статистики
     */
    data class DatabaseStats(
        val totalConnections: Int,
        val activeConnections: Int,
        val idleConnections: Int,
        val totalQueries: Long,
        val slowQueries: Long,
        val averageQueryTime: Double,
        val maxQueryTime: Long,
        val timestamp: Long
    )
    
    /**
     * представлення монітора бази даних
     */
    class DatabaseMonitor {
        private val stats = mutableListOf<DatabaseStats>()
        private val statsLock = Any()
        private val maxStatsSize = 1000
        
        /**
         * додає статистику
         *
         * @param stat статистика
         */
        fun addStats(stat: DatabaseStats) {
            synchronized(statsLock) {
                stats.add(stat)
                if (stats.size > maxStatsSize) {
                    stats.removeAt(0)
                }
            }
        }
        
        /**
         * отримує останню статистику
         *
         * @return остання статистика або null якщо немає даних
         */
        fun getLastStats(): DatabaseStats? {
            synchronized(statsLock) {
                return stats.lastOrNull()
            }
        }
        
        /**
         * отримує статистику за певний період
         *
         * @param startTime початковий час
         * @param endTime кінцевий час
         * @return список статистик
         */
        fun getStatsForPeriod(startTime: Long, endTime: Long): List<DatabaseStats> {
            synchronized(statsLock) {
                return stats.filter { it.timestamp >= startTime && it.timestamp <= endTime }
            }
        }
        
        /**
         * очищує статистику
         */
        fun clearStats() {
            synchronized(statsLock) {
                stats.clear()
            }
        }
    }
    
    /**
     * створює монітор бази даних
     *
     * @return монітор бази даних
     */
    fun createDatabaseMonitor(): DatabaseMonitor {
        return DatabaseMonitor()
    }
    
    // функції для роботи з резервним копіюванням бази даних
    
    /**
     * представлення резервної копії бази даних
     */
    class DatabaseBackup {
        
        /**
         * створює резервну копію бази даних
         *
         * @param connection з'єднання
         * @param backupPath шлях до файлу резервної копії
         * @return true якщо резервне копіювання вдалося
         */
        fun createBackup(connection: Connection, backupPath: String): Boolean {
            // в реальному застосунку тут потрібно реалізувати резервне копіювання бази даних
            // залежно від типу бази даних
            return false
        }
        
        /**
         * відновлює базу даних з резервної копії
         *
         * @param connection з'єднання
         * @param backupPath шлях до файлу резервної копії
         * @return true якщо відновлення вдалося
         */
        fun restoreBackup(connection: Connection, backupPath: String): Boolean {
            // в реальному застосунку тут потрібно реалізувати відновлення бази даних
            // залежно від типу бази даних
            return false
        }
    }
    
    /**
     * створює резервну копію бази даних
     *
     * @return резервна копія бази даних
     */
    fun createDatabaseBackup(): DatabaseBackup {
        return DatabaseBackup()
    }
    
    // функції для роботи з тестуванням бази даних
    
    /**
     * представлення тестової бази даних
     */
    class TestDatabase {
        private var connection: Connection? = null
        
        /**
         * ініціалізує тестову базу даних
         *
         * @param config конфігурація
         * @return true якщо ініціалізація вдалася
         */
        fun initialize(config: DatabaseConfig): Boolean {
            return try {
                Class.forName(config.driver)
                connection = DriverManager.getConnection(
                    config.url,
                    config.username,
                    config.password
                )
                true
            } catch (e: Exception) {
                false
            }
        }
        
        /**
         * виконує sql скрипт
         *
         * @param script sql скрипт
         * @return true якщо виконання вдалося
         */
        fun executeScript(script: String): Boolean {
            return try {
                val statement = connection?.createStatement()
                statement?.execute(script)
                statement?.close()
                true
            } catch (e: SQLException) {
                false
            }
        }
        
        /**
         * закриває тестову базу даних
         */
        fun close() {
            try {
                connection?.close()
            } catch (e: SQLException) {
                // ігноруємо помилки закриття
            } finally {
                connection = null
            }
        }
        
        /**
         * отримує з'єднання
         *
         * @return з'єднання
         */
        fun getConnection(): Connection? {
            return connection
        }
    }
    
    /**
     * створює тестову базу даних
     *
     * @return тестова база даних
     */
    fun createTestDatabase(): TestDatabase {
        return TestDatabase()
    }
    
    // функції для роботи з конфігурацією бази даних
    
    /**
     * представлення конфігурації бази даних з підтримкою різних оточень
     */
    class EnvironmentDatabaseConfig {
        private val configs = mutableMapOf<String, DatabaseConfig>()
        
        /**
         * додає конфігурацію для оточення
         *
         * @param environment оточення
         * @param config конфігурація
         */
        fun addConfig(environment: String, config: DatabaseConfig) {
            configs[environment] = config
        }
        
        /**
         * отримує конфігурацію для поточного оточення
         *
         * @param environment оточення
         * @return конфігурація або null якщо не знайдено
         */
        fun getConfig(environment: String): DatabaseConfig? {
            return configs[environment]
        }
        
        /**
         * отримує конфігурацію для поточного оточення з урахуванням змінних оточення
         *
         * @return конфігурація
         */
        fun getCurrentConfig(): DatabaseConfig {
            val environment = System.getenv("DB_ENVIRONMENT") ?: "default"
            return configs[environment] ?: configs["default"] ?: throw DatabaseException("Конфігурацію не знайдено")
        }
    }
    
    /**
     * створює конфігурацію бази даних з підтримкою різних оточень
     *
     * @return конфігурація бази даних
     */
    fun createEnvironmentDatabaseConfig(): EnvironmentDatabaseConfig {
        return EnvironmentDatabaseConfig()
    }
    
    // функції для роботи з пагінацією
    
    /**
     * представлення результату з пагінацією
     *
     * @param T тип елемента
     * @property items елементи
     * @property totalCount загальна кількість елементів
     * @property currentPage поточна сторінка
     * @property pageSize розмір сторінки
     */
    data class PagedResult<T>(
        val items: List<T>,
        val totalCount: Long,
        val currentPage: Int,
        val pageSize: Int
    ) {
        /**
         * отримує кількість сторінок
         *
         * @return кількість сторінок
         */
        fun getPageCount(): Int {
            return if (pageSize > 0) ((totalCount + pageSize - 1) / pageSize).toInt() else 0
        }
        
        /**
         * перевіряє, чи є наступна сторінка
         *
         * @return true якщо є наступна сторінка
         */
        fun hasNextPage(): Boolean {
            return currentPage < getPageCount()
        }
        
        /**
         * перевіряє, чи є попередня сторінка
         *
         * @return true якщо є попередня сторінка
         */
        fun hasPreviousPage(): Boolean {
            return currentPage > 1
        }
    }
    
    /**
     * представлення репозиторію з пагінацією
     *
     * @param T тип сутності
     * @property delegate делегат репозиторію
     */
    class PagedRepository<T : Any>(private val delegate: Repository<T>) {
        
        /**
         * отримує сутності з пагінацією
         *
         * @param page номер сторінки
         * @param size розмір сторінки
         * @return результат з пагінацією
         */
        fun findAll(page: Int, size: Int): PagedResult<T> {
            // в реальному застосунку тут потрібно реалізувати пагінацію
            // з використанням sql запитів з LIMIT та OFFSET
            throw NotImplementedError("Ця функція не реалізована в спрощеній версії")
        }
        
        /**
         * отримує сутності з пагінацією за критеріями
         *
         * @param criteria критерії пошуку
         * @param page номер сторінки
         * @param size розмір сторінки
         * @return результат з пагінацією
         */
        fun findByCriteria(criteria: Map<String, Any>, page: Int, size: Int): PagedResult<T> {
            // в реальному застосунку тут потрібно реалізувати пагінацію з критеріями пошуку
            throw NotImplementedError("Ця функція не реалізована в спрощеній версії")
        }
    }
    
    /**
     * створює репозиторій з пагінацією
     *
     * @param delegate делегат репозиторію
     * @return репозиторій з пагінацією
     */
    fun <T : Any> createPagedRepository(delegate: Repository<T>): PagedRepository<T> {
        return PagedRepository(delegate)
    }
    
    // функції для роботи з асинхронними операціями
    
    /**
     * представлення асинхронного репозиторію
     *
     * @param T тип сутності
     * @property delegate делегат репозиторію
     * @property executorService сервіс виконання
     */
    class AsyncRepository<T : Any>(
        private val delegate: Repository<T>,
        private val executorService: ExecutorService
    ) {
        
        /**
         * асинхронно зберігає сутність
         *
         * @param entity сутність
         * @return футура з результатом
         */
        fun saveAsync(entity: T): Future<T> {
            return executorService.submit(Callable {
                delegate.save(entity)
            })
        }
        
        /**
         * асинхронно видаляє сутність
         *
         * @param entity сутність
         * @return футура з результатом
         */
        fun deleteAsync(entity: T): Future<Void> {
            return executorService.submit(Callable {
                delegate.delete(entity)
                null
            })
        }
        
        /**
         * асинхронно отримує сутність за id
         *
         * @param id id сутності
         * @return футура з результатом
         */
        fun findByIdAsync(id: Any): Future<T?> {
            return executorService.submit(Callable {
                delegate.findById(id)
            })
        }
        
        /**
         * асинхронно отримує всі сутності
         *
         * @return футура з результатом
         */
        fun findAllAsync(): Future<List<T>> {
            return executorService.submit(Callable {
                delegate.findAll()
            })
        }
    }
    
    /**
     * створює асинхронний репозиторій
     *
     * @param delegate делегат репозиторію
     * @param executorService сервіс виконання
     * @return асинхронний репозиторій
     */
    fun <T : Any> createAsyncRepository(
        delegate: Repository<T>,
        executorService: ExecutorService
    ): AsyncRepository<T> {
        return AsyncRepository(delegate, executorService)
    }
    
    // функції для роботи з валідацією даних
    
    /**
     * інтерфейс для валідатора сутності
     *
     * @param T тип сутності
     */
    interface EntityValidator<T> {
        /**
         * валідує сутність
         *
         * @param entity сутність
         * @return список помилок валідації
         */
        fun validate(entity: T): List<String>
    }
    
    /**
     * представлення репозиторію з валідацією
     *
     * @param T тип сутності
     * @property delegate делегат репозиторію
     * @property validator валідатор
     */
    class ValidatedRepository<T : Any>(
        private val delegate: Repository<T>,
        private val validator: EntityValidator<T>
    ) {
        
        /**
         * зберігає сутність з валідацією
         *
         * @param entity сутність
         * @return збережена сутність
         * @throws ValidationException якщо валідація не пройшла
         */
        fun save(entity: T): T {
            val errors = validator.validate(entity)
            if (errors.isNotEmpty()) {
                throw ValidationException("Помилки валідації: ${errors.joinToString(", ")}")
            }
            return delegate.save(entity)
        }
        
        /**
         * видаляє сутність
         *
         * @param entity сутність
         */
        fun delete(entity: T) {
            delegate.delete(entity)
        }
        
        /**
         * отримує сутність за id
         *
         * @param id id сутності
         * @return сутність або null якщо не знайдено
         */
        fun findById(id: Any): T? {
            return delegate.findById(id)
        }
        
        /**
         * отримує всі сутності
         *
         * @return список сутностей
         */
        fun findAll(): List<T> {
            return delegate.findAll()
        }
    }
    
    /**
     * виняток валідації
     *
     * @property message повідомлення про помилку
     */
    class ValidationException(message: String) : Exception(message)
    
    /**
     * створює репозиторій з валідацією
     *
     * @param delegate делегат репозиторію
     * @param validator валідатор
     * @return репозиторій з валідацією
     */
    fun <T : Any> createValidatedRepository(
        delegate: Repository<T>,
        validator: EntityValidator<T>
    ): ValidatedRepository<T> {
        return ValidatedRepository(delegate, validator)
    }
    
    // функції для роботи з історією змін
    
    /**
     * представлення історії змін сутності
     *
     * @property entityId id сутності
     * @property entityType тип сутності
     * @property operation операція (INSERT, UPDATE, DELETE)
     * @property oldValue старе значення
     * @property newValue нове значення
     * @property timestamp час зміни
     * @property userId id користувача
     */
    data class ChangeHistory(
        val entityId: Any,
        val entityType: String,
        val operation: String,
        val oldValue: String?,
        val newValue: String?,
        val timestamp: Long,
        val userId: String?
    )
    
    /**
     * представлення репозиторію з історією змін
     *
     * @param T тип сутності
     * @property delegate делегат репозиторію
     * @property historyRepository репозиторій історії змін
     */
    class HistoryRepository<T : Any>(
        private val delegate: Repository<T>,
        private val historyRepository: Repository<ChangeHistory>
    ) {
        
        /**
         * зберігає сутність з записом в історію
         *
         * @param entity сутність
         * @param userId id користувача
         * @return збережена сутність
         */
        fun save(entity: T, userId: String? = null): T {
            // в реальному застосунку тут потрібно реалізувати запис в історію змін
            return delegate.save(entity)
        }
        
        /**
         * видаляє сутність з записом в історію
         *
         * @param entity сутність
         * @param userId id користувача
         */
        fun delete(entity: T, userId: String? = null) {
            // в реальному застосунку тут потрібно реалізувати запис в історію змін
            delegate.delete(entity)
        }
        
        /**
         * отримує історію змін сутності
         *
         * @param entityId id сутності
         * @return список змін
         */
        fun getHistory(entityId: Any): List<ChangeHistory> {
            // в реальному застосунку тут потрібно реалізувати отримання історії змін
            return emptyList()
        }
    }
    
    /**
     * створює репозиторій з історією змін
     *
     * @param delegate делегат репозиторію
     * @param historyRepository репозиторій історії змін
     * @return репозиторій з історією змін
     */
    fun <T : Any> createHistoryRepository(
        delegate: Repository<T>,
        historyRepository: Repository<ChangeHistory>
    ): HistoryRepository<T> {
        return HistoryRepository(delegate, historyRepository)
    }
    
    // функції для роботи з винятками бази даних
    
    /**
     * виняток бази даних
     *
     * @property message повідомлення про помилку
     * @property cause причина помилки
     */
    class DatabaseException(message: String, cause: Throwable? = null) : Exception(message, cause)
    
    /**
     * виняток з'єднання з базою даних
     *
     * @property message повідомлення про помилку
     * @property cause причина помилки
     */
    class ConnectionException(message: String, cause: Throwable? = null) : DatabaseException(message, cause)
    
    /**
     * виняток sql запиту
     *
     * @property message повідомлення про помилку
     * @property cause причина помилки
     */
    class QueryException(message: String, cause: Throwable? = null) : DatabaseException(message, cause)
    
    /**
     * виняток транзакції
     *
     * @property message повідомлення про помилку
     * @property cause причина помилки
     */
    class TransactionException(message: String, cause: Throwable? = null) : DatabaseException(message, cause)
    
    // функції для роботи з метаданими бази даних
    
    /**
     * представлення метаданих таблиці
     *
     * @property name назва таблиці
     * @property columns стовпці таблиці
     * @property primaryKey первинний ключ
     * @property foreignKeys зовнішні ключі
     * @property indexes індекси
     */
    data class TableMetadata(
        val name: String,
        val columns: List<ColumnMetadata>,
        val primaryKey: List<String>,
        val foreignKeys: List<ForeignKeyMetadata>,
        val indexes: List<IndexMetadata>
    )
    
    /**
     * представлення метаданих стовпця
     *
     * @property name назва стовпця
     * @property type тип даних
     * @property nullable чи може бути null
     * @property defaultValue значення за замовчуванням
     * @property isPrimaryKey чи є первинним ключем
     * @property isUnique чи є унікальним
     * @property length довжина
     * @property precision точність
     * @property scale масштаб
     */
    data class ColumnMetadata(
        val name: String,
        val type: String,
        val nullable: Boolean,
        val defaultValue: String?,
        val isPrimaryKey: Boolean,
        val isUnique: Boolean,
        val length: Int,
        val precision: Int,
        val scale: Int
    )
    
    /**
     * представлення метаданих зовнішнього ключа
     *
     * @property name назва
     * @property columnName назва стовпця
     * @property referencedTable таблиця, на яку посилається
     * @property referencedColumn стовпець, на який посилається
     */
    data class ForeignKeyMetadata(
        val name: String,
        val columnName: String,
        val referencedTable: String,
        val referencedColumn: String
    )
    
    /**
     * представлення метаданих індекса
     *
     * @property name назва
     * @property columns стовпці
     * @property isUnique чи є унікальним
     */
    data class IndexMetadata(
        val name: String,
        val columns: List<String>,
        val isUnique: Boolean
    )
    
    /**
     * представлення менеджера метаданих
     */
    class MetadataManager {
        
        /**
         * отримує метадані таблиці
         *
         * @param connection з'єднання
         * @param tableName назва таблиці
         * @return метадані таблиці
         */
        fun getTableMetadata(connection: Connection, tableName: String): TableMetadata? {
            return try {
                val databaseMetaData = connection.metaData
                val resultSet = databaseMetaData.getColumns(null, null, tableName, null)
                
                val columns = mutableListOf<ColumnMetadata>()
                while (resultSet.next()) {
                    columns.add(
                        ColumnMetadata(
                            name = resultSet.getString("COLUMN_NAME"),
                            type = resultSet.getString("TYPE_NAME"),
                            nullable = resultSet.getString("IS_NULLABLE") == "YES",
                            defaultValue = resultSet.getString("COLUMN_DEF"),
                            isPrimaryKey = false, // потрібно отримати окремо
                            isUnique = false, // потрібно отримати окремо
                            length = resultSet.getInt("COLUMN_SIZE"),
                            precision = resultSet.getInt("DECIMAL_DIGITS"),
                            scale = 0
                        )
                    )
                }
                
                resultSet.close()
                
                TableMetadata(
                    name = tableName,
                    columns = columns,
                    primaryKey = emptyList(), // потрібно отримати окремо
                    foreignKeys = emptyList(), // потрібно отримати окремо
                    indexes = emptyList() // потрібно отримати окремо
                )
            } catch (e: SQLException) {
                null
            }
        }
        
        /**
         * отримує метадані всіх таблиць
         *
         * @param connection з'єднання
         * @return список метаданих таблиць
         */
        fun getAllTableMetadata(connection: Connection): List<TableMetadata> {
            val tables = mutableListOf<TableMetadata>()
            
            return try {
                val databaseMetaData = connection.metaData
                val resultSet = databaseMetaData.getTables(null, null, null, arrayOf("TABLE"))
                
                while (resultSet.next()) {
                    val tableName = resultSet.getString("TABLE_NAME")
                    val tableMetadata = getTableMetadata(connection, tableName)
                    if (tableMetadata != null) {
                        tables.add(tableMetadata)
                    }
                }
                
                resultSet.close()
                tables
            } catch (e: SQLException) {
                tables
            }
        }
    }
    
    /**
     * створює менеджер метаданих
     *
     * @return менеджер метаданих
     */
    fun createMetadataManager(): MetadataManager {
        return MetadataManager()
    }
    
    // функції для роботи з генерацією коду
    
    /**
     * представлення генератора коду сутностей
     */
    class EntityCodeGenerator {
        
        /**
         * генерує код сутності kotlin
         *
         * @param tableMetadata метадані таблиці
         * @return код сутності
         */
        fun generateKotlinEntity(tableMetadata: TableMetadata): String {
            val sb = StringBuilder()
            
            // генеруємо анотацію таблиці
            sb.append("@Table(name = \"${tableMetadata.name}\")\n")
            sb.append("data class ${tableMetadata.name.capitalize()}(\n")
            
            // генеруємо поля
            tableMetadata.columns.forEachIndexed { index, column ->
                val kotlinType = mapSqlTypeToKotlin(column.type)
                val separator = if (index < tableMetadata.columns.size - 1) "," else ""
                
                sb.append("    @Column(name = \"${column.name}\")\n")
                sb.append("    val ${column.name.decapitalize()}: $kotlinType$separator\n")
            }
            
            sb.append(")")
            
            return sb.toString()
        }
        
        /**
         * генерує код репозиторію kotlin
         *
         * @param tableMetadata метадані таблиці
         * @return код репозиторію
         */
        fun generateKotlinRepository(tableMetadata: TableMetadata): String {
            val entityName = tableMetadata.name.capitalize()
            val sb = StringBuilder()
            
            sb.append("class ${entityName}Repository(connectionPool: ConnectionPool) : Repository<${entityName}>(connectionPool) {\n")
            sb.append("    // тут можна додати специфічні методи для репозиторію\n")
            sb.append("}")
            
            return sb.toString()
        }
        
        /**
         * відображає sql тип у kotlin тип
         *
         * @param sqlType sql тип
         * @return kotlin тип
         */
        private fun mapSqlTypeToKotlin(sqlType: String): String {
            return when (sqlType.uppercase()) {
                "VARCHAR", "CHAR", "TEXT" -> "String"
                "INT", "INTEGER" -> "Int"
                "BIGINT" -> "Long"
                "DECIMAL", "NUMERIC" -> "BigDecimal"
                "FLOAT" -> "Float"
                "DOUBLE" -> "Double"
                "BOOLEAN" -> "Boolean"
                "DATE" -> "LocalDate"
                "TIMESTAMP" -> "LocalDateTime"
                else -> "Any"
            }
        }
    }
    
    /**
     * створює генератор коду сутностей
     *
     * @return генератор коду сутностей
     */
    fun createEntityCodeGenerator(): EntityCodeGenerator {
        return EntityCodeGenerator()
    }
}