/**
 * фреймворк для роботи з базами даних
 *
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import java.sql.*
import javax.sql.*
import java.util.*
import java.util.concurrent.*

/**
 * представлення інтерфейсу для роботи з JDBC
 */
interface JdbcHelper {
    /**
     * виконати запит
     *
     * @param connection з'єднання
     * @param sql SQL запит
     * @param parameters параметри
     * @return результат
     */
    fun executeQuery(connection: Connection, sql: String, parameters: List<Any?> = emptyList()): List<Map<String, Any?>>
    
    /**
     * виконати оновлення
     *
     * @param connection з'єднання
     * @param sql SQL запит
     * @param parameters параметри
     * @return кількість змінених рядків
     */
    fun executeUpdate(connection: Connection, sql: String, parameters: List<Any?> = emptyList()): Int
    
    /**
     * виконати пакет оновлень
     *
     * @param connection з'єднання
     * @param sql SQL запит
     * @param batchParameters список параметрів для пакету
     * @return список кількостей змінених рядків
     */
    fun executeBatchUpdate(connection: Connection, sql: String, batchParameters: List<List<Any?>>): List<Int>
    
    /**
     * створити підготовлений вираз
     *
     * @param connection з'єднання
     * @param sql SQL запит
     * @return підготовлений вираз
     */
    fun prepareStatement(connection: Connection, sql: String): PreparedStatement
}

/**
 * представлення базової реалізації помічника з JDBC
 */
open class BaseJdbcHelper : JdbcHelper {
    
    override fun executeQuery(connection: Connection, sql: String, parameters: List<Any?>): List<Map<String, Any?>> {
        val statement = prepareStatement(connection, sql)
        setParameters(statement, parameters)
        
        val resultSet = statement.executeQuery()
        val metaData = resultSet.metaData
        val columnCount = metaData.columnCount
        val results = mutableListOf<Map<String, Any?>>()
        
        while (resultSet.next()) {
            val row = mutableMapOf<String, Any?>()
            for (i in 1..columnCount) {
                val columnName = metaData.getColumnLabel(i)
                row[columnName] = resultSet.getObject(i)
            }
            results.add(row)
        }
        
        resultSet.close()
        statement.close()
        
        return results
    }
    
    override fun executeUpdate(connection: Connection, sql: String, parameters: List<Any?>): Int {
        val statement = prepareStatement(connection, sql)
        setParameters(statement, parameters)
        
        val result = statement.executeUpdate()
        statement.close()
        
        return result
    }
    
    override fun executeBatchUpdate(connection: Connection, sql: String, batchParameters: List<List<Any?>>): List<Int> {
        val statement = prepareStatement(connection, sql)
        statement.clearBatch()
        
        batchParameters.forEach { params ->
            setParameters(statement, params)
            statement.addBatch()
        }
        
        val results = statement.executeBatch().toList()
        statement.close()
        
        return results
    }
    
    override fun prepareStatement(connection: Connection, sql: String): PreparedStatement {
        return connection.prepareStatement(sql)
    }
    
    private fun setParameters(statement: PreparedStatement, parameters: List<Any?>) {
        parameters.forEachIndexed { index, param ->
            statement.setObject(index + 1, param)
        }
    }
}

/**
 * представлення інтерфейсу для роботи з пулом з'єднань
 */
interface ConnectionPool {
    /**
     * отримати з'єднання
     *
     * @return з'єднання
     */
    fun getConnection(): Connection
    
    /**
     * повернути з'єднання
     *
     * @param connection з'єднання
     */
    fun releaseConnection(connection: Connection)
    
    /**
     * закрити пул
     */
    fun close()
    
    /**
     * отримати розмір пулу
     *
     * @return розмір пулу
     */
    fun size(): Int
}

/**
 * представлення базової реалізації пулу з'єднань
 */
open class BaseConnectionPool(
    private val dataSource: DataSource,
    private val maxSize: Int = 10
) : ConnectionPool {
    
    private val availableConnections = ConcurrentLinkedQueue<Connection>()
    private val usedConnections = Collections.synchronizedSet(mutableSetOf<Connection>())
    private var currentSize = 0
    private val lock = Any()
    
    override fun getConnection(): Connection {
        var connection = availableConnections.poll()
        
        if (connection == null) {
            synchronized(lock) {
                if (currentSize < maxSize) {
                    connection = dataSource.connection
                    currentSize++
                }
            }
        }
        
        if (connection == null) {
            throw SQLException("Не вдалося отримати з'єднання з пулу")
        }
        
        usedConnections.add(connection)
        return connection
    }
    
    override fun releaseConnection(connection: Connection) {
        if (usedConnections.remove(connection)) {
            if (connection.isClosed) {
                synchronized(lock) {
                    currentSize--
                }
            } else {
                availableConnections.offer(connection)
            }
        }
    }
    
    override fun close() {
        availableConnections.forEach { it.close() }
        usedConnections.forEach { it.close() }
        availableConnections.clear()
        usedConnections.clear()
        currentSize = 0
    }
    
    override fun size(): Int {
        return currentSize
    }
}

/**
 * представлення інтерфейсу для роботи з транзакціями
 */
interface TransactionManager {
    /**
     * почати транзакцію
     *
     * @param connection з'єднання
     */
    fun beginTransaction(connection: Connection)
    
    /**
     * зафіксувати транзакцію
     *
     * @param connection з'єднання
     */
    fun commitTransaction(connection: Connection)
    
    /**
     * відкотити транзакцію
     *
     * @param connection з'єднання
     */
    fun rollbackTransaction(connection: Connection)
    
    /**
     * виконати блок коду в транзакції
     *
     * @param connection з'єднання
     * @param block блок коду
     */
    fun <T> executeInTransaction(connection: Connection, block: () -> T): T
}

/**
 * представлення базової реалізації менеджера транзакцій
 */
open class BaseTransactionManager : TransactionManager {
    
    override fun beginTransaction(connection: Connection) {
        connection.autoCommit = false
    }
    
    override fun commitTransaction(connection: Connection) {
        connection.commit()
        connection.autoCommit = true
    }
    
    override fun rollbackTransaction(connection: Connection) {
        connection.rollback()
        connection.autoCommit = true
    }
    
    override fun <T> executeInTransaction(connection: Connection, block: () -> T): T {
        beginTransaction(connection)
        return try {
            val result = block()
            commitTransaction(connection)
            result
        } catch (e: Exception) {
            rollbackTransaction(connection)
            throw e
        }
    }
}

/**
 * представлення інтерфейсу для роботи з ORM
 */
interface OrmHelper {
    /**
     * зберегти об'єкт
     *
     * @param connection з'єднання
     * @param entity об'єкт
     * @return збережений об'єкт
     */
    fun <T : Any> save(connection: Connection, entity: T): T
    
    /**
     * знайти об'єкт за ID
     *
     * @param connection з'єднання
     * @param clazz клас об'єкта
     * @param id ID
     * @return об'єкт або null
     */
    fun <T : Any> findById(connection: Connection, clazz: Class<T>, id: Any): T?
    
    /**
     * знайти всі об'єкти
     *
     * @param connection з'єднання
     * @param clazz клас об'єкта
     * @return список об'єктів
     */
    fun <T : Any> findAll(connection: Connection, clazz: Class<T>): List<T>
    
    /**
     * видалити об'єкт
     *
     * @param connection з'єднання
     * @param entity об'єкт
     */
    fun <T : Any> delete(connection: Connection, entity: T)
    
    /**
     * оновити об'єкт
     *
     * @param connection з'єднання
     * @param entity об'єкт
     * @return оновлений об'єкт
     */
    fun <T : Any> update(connection: Connection, entity: T): T
}

/**
 * представлення базової реалізації помічника з ORM
 */
open class BaseOrmHelper : OrmHelper {
    
    override fun <T : Any> save(connection: Connection, entity: T): T {
        // Заглушка для реалізації збереження об'єкта
        return entity
    }
    
    override fun <T : Any> findById(connection: Connection, clazz: Class<T>, id: Any): T? {
        // Заглушка для реалізації пошуку об'єкта за ID
        return null
    }
    
    override fun <T : Any> findAll(connection: Connection, clazz: Class<T>): List<T> {
        // Заглушка для реалізації пошуку всіх об'єктів
        return emptyList()
    }
    
    override fun <T : Any> delete(connection: Connection, entity: T) {
        // Заглушка для реалізації видалення об'єкта
    }
    
    override fun <T : Any> update(connection: Connection, entity: T): T {
        // Заглушка для реалізації оновлення об'єкта
        return entity
    }
}

/**
 * представлення інтерфейсу для роботи з SQL генерацією
 */
interface SqlGenerator {
    /**
     * згенерувати INSERT запит
     *
     * @param tableName назва таблиці
     * @param columns стовпці
     * @return SQL запит
     */
    fun generateInsert(tableName: String, columns: List<String>): String
    
    /**
     * згенерувати SELECT запит
     *
     * @param tableName назва таблиці
     * @param columns стовпці
     * @param where умова WHERE
     * @return SQL запит
     */
    fun generateSelect(tableName: String, columns: List<String>, where: String? = null): String
    
    /**
     * згенерувати UPDATE запит
     *
     * @param tableName назва таблиці
     * @param columns стовпці
     * @param where умова WHERE
     * @return SQL запит
     */
    fun generateUpdate(tableName: String, columns: List<String>, where: String? = null): String
    
    /**
     * згенерувати DELETE запит
     *
     * @param tableName назва таблиці
     * @param where умова WHERE
     * @return SQL запит
     */
    fun generateDelete(tableName: String, where: String? = null): String
}

/**
 * представлення базової реалізації генератора SQL
 */
open class BaseSqlGenerator : SqlGenerator {
    
    override fun generateInsert(tableName: String, columns: List<String>): String {
        val placeholders = columns.map { "?" }.joinToString(", ")
        val columnNames = columns.joinToString(", ")
        return "INSERT INTO $tableName ($columnNames) VALUES ($placeholders)"
    }
    
    override fun generateSelect(tableName: String, columns: List<String>, where: String?): String {
        val columnNames = columns.joinToString(", ")
        val query = StringBuilder("SELECT $columnNames FROM $tableName")
        where?.let { query.append(" WHERE $it") }
        return query.toString()
    }
    
    override fun generateUpdate(tableName: String, columns: List<String>, where: String?): String {
        val setClause = columns.map { "$it = ?" }.joinToString(", ")
        val query = StringBuilder("UPDATE $tableName SET $setClause")
        where?.let { query.append(" WHERE $it") }
        return query.toString()
    }
    
    override fun generateDelete(tableName: String, where: String?): String {
        val query = StringBuilder("DELETE FROM $tableName")
        where?.let { query.append(" WHERE $it") }
        return query.toString()
    }
}

/**
 * представлення інтерфейсу для роботи з міграціями бази даних
 */
interface DatabaseMigrationHelper {
    /**
     * застосувати міграцію
     *
     * @param connection з'єднання
     * @param migration міграція
     */
    fun applyMigration(connection: Connection, migration: DatabaseMigration)
    
    /**
     * отримати історію міграцій
     *
     * @param connection з'єднання
     * @return список міграцій
     */
    fun getMigrationHistory(connection: Connection): List<AppliedMigration>
    
    /**
     * перевірити, чи міграція застосована
     *
     * @param connection з'єднання
     * @param migrationId ID міграції
     * @return true, якщо міграція застосована
     */
    fun isMigrationApplied(connection: Connection, migrationId: String): Boolean
}

/**
 * представлення міграції бази даних
 *
 * @property id ID міграції
 * @property description опис
 * @property sql SQL скрипт
 * @property version версія
 */
data class DatabaseMigration(
    val id: String,
    val description: String,
    val sql: String,
    val version: Int
)

/**
 * представлення застосованої міграції
 *
 * @property id ID міграції
 * @property description опис
 * @property version версія
 * @property appliedAt час застосування
 */
data class AppliedMigration(
    val id: String,
    val description: String,
    val version: Int,
    val appliedAt: Timestamp
)

/**
 * представлення базової реалізації помічника з міграціями
 */
open class BaseDatabaseMigrationHelper : DatabaseMigrationHelper {
    
    override fun applyMigration(connection: Connection, migration: DatabaseMigration) {
        val statement = connection.createStatement()
        statement.execute(migration.sql)
        statement.close()
        
        // Зберегти інформацію про застосовану міграцію
        val insertSql = """
            INSERT INTO migration_history (id, description, version, applied_at)
            VALUES (?, ?, ?, ?)
        """.trimIndent()
        
        val insertStatement = connection.prepareStatement(insertSql)
        insertStatement.setString(1, migration.id)
        insertStatement.setString(2, migration.description)
        insertStatement.setInt(3, migration.version)
        insertStatement.setTimestamp(4, Timestamp(System.currentTimeMillis()))
        insertStatement.execute()
        insertStatement.close()
    }
    
    override fun getMigrationHistory(connection: Connection): List<AppliedMigration> {
        val sql = "SELECT id, description, version, applied_at FROM migration_history ORDER BY version"
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery(sql)
        
        val migrations = mutableListOf<AppliedMigration>()
        while (resultSet.next()) {
            migrations.add(
                AppliedMigration(
                    resultSet.getString("id"),
                    resultSet.getString("description"),
                    resultSet.getInt("version"),
                    resultSet.getTimestamp("applied_at")
                )
            )
        }
        
        resultSet.close()
        statement.close()
        
        return migrations
    }
    
    override fun isMigrationApplied(connection: Connection, migrationId: String): Boolean {
        val sql = "SELECT COUNT(*) FROM migration_history WHERE id = ?"
        val statement = connection.prepareStatement(sql)
        statement.setString(1, migrationId)
        
        val resultSet = statement.executeQuery()
        val count = if (resultSet.next()) resultSet.getInt(1) else 0
        
        resultSet.close()
        statement.close()
        
        return count > 0
    }
}

/**
 * представлення інтерфейсу для роботи з кешем даних
 */
interface DataCache {
    /**
     * отримати дані з кешу
     *
     * @param key ключ
     * @return дані або null
     */
    fun <T> get(key: String): T?
    
    /**
     * зберегти дані в кеш
     *
     * @param key ключ
     * @param value дані
     * @param ttl час життя в мілісекундах
     */
    fun <T> put(key: String, value: T, ttl: Long = 300000) // 5 хвилин за замовчуванням
    
    /**
     * видалити дані з кешу
     *
     * @param key ключ
     */
    fun remove(key: String)
    
    /**
     * очистити кеш
     */
    fun clear()
    
    /**
     * очистити прострочені записи
     */
    fun cleanupExpired()
}

/**
 * представлення запису кешу
 *
 * @param T тип даних
 * @property value дані
 * @property timestamp мітка часу
 * @property ttl час життя в мілісекундах
 */
data class CacheEntry<T>(
    val value: T,
    val timestamp: Long,
    val ttl: Long
) {
    /**
     * перевірити, чи запис прострочений
     *
     * @return true, якщо запис прострочений
     */
    fun isExpired(): Boolean {
        return (System.currentTimeMillis() - timestamp) > ttl
    }
}

/**
 * представлення базової реалізації кешу даних
 */
open class BaseDataCache : DataCache {
    private val cache = mutableMapOf<String, CacheEntry<*>>()
    private val lock = Any()
    
    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): T? {
        synchronized(lock) {
            val entry = cache[key]
            return if (entry != null && !entry.isExpired()) {
                entry.value as T
            } else {
                if (entry != null) {
                    cache.remove(key)
                }
                null
            }
        }
    }
    
    override fun <T> put(key: String, value: T, ttl: Long) {
        synchronized(lock) {
            cache[key] = CacheEntry(value, System.currentTimeMillis(), ttl)
        }
    }
    
    override fun remove(key: String) {
        synchronized(lock) {
            cache.remove(key)
        }
    }
    
    override fun clear() {
        synchronized(lock) {
            cache.clear()
        }
    }
    
    override fun cleanupExpired() {
        synchronized(lock) {
            val expiredKeys = cache.filter { it.value.isExpired() }.map { it.key }
            expiredKeys.forEach { cache.remove(it) }
        }
    }
}

/**
 * представлення інтерфейсу для роботи з репозиторіями
 */
interface Repository<T, ID> {
    /**
     * зберегти сутність
     *
     * @param entity сутність
     * @return збережена сутність
     */
    fun save(entity: T): T
    
    /**
     * знайти сутність за ID
     *
     * @param id ID
     * @return сутність або null
     */
    fun findById(id: ID): T?
    
    /**
     * знайти всі сутності
     *
     * @return список сутностей
     */
    fun findAll(): List<T>
    
    /**
     * видалити сутність
     *
     * @param entity сутність
     */
    fun delete(entity: T)
    
    /**
     * видалити сутність за ID
     *
     * @param id ID
     */
    fun deleteById(id: ID)
    
    /**
     * перевірити, чи існує сутність за ID
     *
     * @param id ID
     * @return true, якщо сутність існує
     */
    fun existsById(id: ID): Boolean
}

/**
 * представлення базової реалізації репозиторію
 */
open class BaseRepository<T, ID>(
    private val connectionPool: ConnectionPool,
    private val jdbcHelper: JdbcHelper = BaseJdbcHelper()
) : Repository<T, ID> {
    
    override fun save(entity: T): T {
        val connection = connectionPool.getConnection()
        return try {
            // Реалізація збереження сутності
            entity
        } finally {
            connectionPool.releaseConnection(connection)
        }
    }
    
    override fun findById(id: ID): T? {
        val connection = connectionPool.getConnection()
        return try {
            // Реалізація пошуку сутності за ID
            null
        } finally {
            connectionPool.releaseConnection(connection)
        }
    }
    
    override fun findAll(): List<T> {
        val connection = connectionPool.getConnection()
        return try {
            // Реалізація пошуку всіх сутностей
            emptyList()
        } finally {
            connectionPool.releaseConnection(connection)
        }
    }
    
    override fun delete(entity: T) {
        val connection = connectionPool.getConnection()
        try {
            // Реалізація видалення сутності
        } finally {
            connectionPool.releaseConnection(connection)
        }
    }
    
    override fun deleteById(id: ID) {
        val connection = connectionPool.getConnection()
        try {
            // Реалізація видалення сутності за ID
        } finally {
            connectionPool.releaseConnection(connection)
        }
    }
    
    override fun existsById(id: ID): Boolean {
        val connection = connectionPool.getConnection()
        return try {
            // Реалізація перевірки існування сутності за ID
            false
        } finally {
            connectionPool.releaseConnection(connection)
        }
    }
}

/**
 * представлення інтерфейсу для роботи з DAO
 */
interface Dao<T, ID> {
    /**
     * створити сутність
     *
     * @param entity сутність
     * @return створена сутність
     */
    fun create(entity: T): T
    
    /**
     * отримати сутність за ID
     *
     * @param id ID
     * @return сутність або null
     */
    fun get(id: ID): T?
    
    /**
     * оновити сутність
     *
     * @param entity сутність
     * @return оновлена сутність
     */
    fun update(entity: T): T
    
    /**
     * видалити сутність
     *
     * @param entity сутність
     */
    fun delete(entity: T)
    
    /**
     * видалити сутність за ID
     *
     * @param id ID
     */
    fun deleteById(id: ID)
    
    /**
     * отримати всі сутності
     *
     * @return список сутностей
     */
    fun getAll(): List<T>
}

/**
 * представлення базової реалізації DAO
 */
open class BaseDao<T, ID>(
    private val connectionPool: ConnectionPool,
    private val jdbcHelper: JdbcHelper = BaseJdbcHelper()
) : Dao<T, ID> {
    
    override fun create(entity: T): T {
        val connection = connectionPool.getConnection()
        return try {
            // Реалізація створення сутності
            entity
        } finally {
            connectionPool.releaseConnection(connection)
        }
    }
    
    override fun get(id: ID): T? {
        val connection = connectionPool.getConnection()
        return try {
            // Реалізація отримання сутності за ID
            null
        } finally {
            connectionPool.releaseConnection(connection)
        }
    }
    
    override fun update(entity: T): T {
        val connection = connectionPool.getConnection()
        return try {
            // Реалізація оновлення сутності
            entity
        } finally {
            connectionPool.releaseConnection(connection)
        }
    }
    
    override fun delete(entity: T) {
        val connection = connectionPool.getConnection()
        try {
            // Реалізація видалення сутності
        } finally {
            connectionPool.releaseConnection(connection)
        }
    }
    
    override fun deleteById(id: ID) {
        val connection = connectionPool.getConnection()
        try {
            // Реалізація видалення сутності за ID
        } finally {
            connectionPool.releaseConnection(connection)
        }
    }
    
    override fun getAll(): List<T> {
        val connection = connectionPool.getConnection()
        return try {
            // Реалізація отримання всіх сутностей
            emptyList()
        } finally {
            connectionPool.releaseConnection(connection)
        }
    }
}

/**
 * представлення інтерфейсу для роботи з транзакційними DAO
 */
interface TransactionalDao<T, ID> : Dao<T, ID> {
    /**
     * виконати блок коду в транзакції
     *
     * @param block блок коду
     * @return результат
     */
    fun <R> executeInTransaction(block: (Dao<T, ID>) -> R): R
}

/**
 * представлення базової реалізації транзакційного DAO
 */
open class BaseTransactionalDao<T, ID>(
    private val connectionPool: ConnectionPool,
    private val transactionManager: TransactionManager = BaseTransactionManager(),
    private val jdbcHelper: JdbcHelper = BaseJdbcHelper()
) : TransactionalDao<T, ID>, BaseDao<T, ID>(connectionPool, jdbcHelper) {
    
    override fun <R> executeInTransaction(block: (Dao<T, ID>) -> R): R {
        val connection = connectionPool.getConnection()
        return try {
            transactionManager.executeInTransaction(connection) {
                block(this)
            }
        } finally {
            connectionPool.releaseConnection(connection)
        }
    }
}

/**
 * представлення інтерфейсу для роботи з пагінацією
 */
interface PaginationHelper {
    /**
     * отримати сторінку даних
     *
     * @param connection з'єднання
     * @param sql SQL запит
     * @param parameters параметри
     * @param page номер сторінки
     * @param size розмір сторінки
     * @return сторінка даних
     */
    fun <T> getPage(
        connection: Connection,
        sql: String,
        parameters: List<Any?>,
        page: Int,
        size: Int,
        mapper: (Map<String, Any?>) -> T
    ): Page<T>
    
    /**
     * отримати кількість записів
     *
     * @param connection з'єднання
     * @param countSql SQL запит для підрахунку
     * @param parameters параметри
     * @return кількість записів
     */
    fun getCount(connection: Connection, countSql: String, parameters: List<Any?>): Long
}

/**
 * представлення сторінки даних
 *
 * @param T тип даних
 * @property content вміст сторінки
 * @property page номер сторінки
 * @property size розмір сторінки
 * @property totalElements загальна кількість елементів
 * @property totalPages загальна кількість сторінок
 */
data class Page<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
) {
    /**
     * чи є перша сторінка
     *
     * @return true, якщо перша сторінка
     */
    val isFirst: Boolean = page == 0
    
    /**
     * чи є остання сторінка
     *
     * @return true, якщо остання сторінка
     */
    val isLast: Boolean = page == totalPages - 1
    
    /**
     * чи є попередня сторінка
     *
     * @return true, якщо є попередня сторінка
     */
    val hasPrevious: Boolean = page > 0
    
    /**
     * чи є наступна сторінка
     *
     * @return true, якщо є наступна сторінка
     */
    val hasNext: Boolean = page < totalPages - 1
}

/**
 * представлення базової реалізації помічника з пагінацією
 */
open class BasePaginationHelper(
    private val jdbcHelper: JdbcHelper = BaseJdbcHelper()
) : PaginationHelper {
    
    override fun <T> getPage(
        connection: Connection,
        sql: String,
        parameters: List<Any?>,
        page: Int,
        size: Int,
        mapper: (Map<String, Any?>) -> T
    ): Page<T> {
        // Додати LIMIT та OFFSET до SQL запиту
        val paginatedSql = "$sql LIMIT ? OFFSET ?"
        val paginatedParameters = parameters + listOf(size, page * size)
        
        val results = jdbcHelper.executeQuery(connection, paginatedSql, paginatedParameters)
        val content = results.map(mapper)
        
        // Отримати загальну кількість елементів
        val countSql = "SELECT COUNT(*) FROM ($sql) AS count_query"
        val countResult = jdbcHelper.executeQuery(connection, countSql, parameters)
        val totalElements = if (countResult.isNotEmpty()) {
            countResult[0].values.firstOrNull() as? Long ?: 0L
        } else {
            0L
        }
        
        val totalPages = if (size > 0) ((totalElements + size - 1) / size).toInt() else 0
        
        return Page(content, page, size, totalElements, totalPages)
    }
    
    override fun getCount(connection: Connection, countSql: String, parameters: List<Any?>): Long {
        val results = jdbcHelper.executeQuery(connection, countSql, parameters)
        return if (results.isNotEmpty()) {
            results[0].values.firstOrNull() as? Long ?: 0L
        } else {
            0L
        }
    }
}

/**
 * представлення інтерфейсу для роботи з метаданими бази даних
 */
interface DatabaseMetadataHelper {
    /**
     * отримати список таблиць
     *
     * @param connection з'єднання
     * @return список таблиць
     */
    fun getTables(connection: Connection): List<TableInfo>
    
    /**
     * отримати інформацію про таблицю
     *
     * @param connection з'єднання
     * @param tableName назва таблиці
     * @return інформація про таблицю
     */
    fun getTableInfo(connection: Connection, tableName: String): TableInfo?
    
    /**
     * отримати список стовпців таблиці
     *
     * @param connection з'єднання
     * @param tableName назва таблиці
     * @return список стовпців
     */
    fun getColumns(connection: Connection, tableName: String): List<ColumnInfo>
    
    /**
     * отримати список індексів таблиці
     *
     * @param connection з'єднання
     * @param tableName назва таблиці
     * @return список індексів
     */
    fun getIndexes(connection: Connection, tableName: String): List<IndexInfo>
}

/**
 * представлення інформації про таблицю
 *
 * @property name назва
 * @property type тип
 * @property remarks коментарі
 */
data class TableInfo(
    val name: String,
    val type: String,
    val remarks: String?
)

/**
 * представлення інформації про стовпець
 *
 * @property name назва
 * @property typeName тип
 * @property size розмір
 * @property nullable чи може бути null
 * @property defaultValue значення за замовчуванням
 * @property primaryKey чи є первинним ключем
 */
data class ColumnInfo(
    val name: String,
    val typeName: String,
    val size: Int,
    val nullable: Boolean,
    val defaultValue: String?,
    val primaryKey: Boolean
)

/**
 * представлення інформації про індекс
 *
 * @property name назва
 * @property columns стовпці
 * @property unique чи унікальний
 * @property type тип
 */
data class IndexInfo(
    val name: String,
    val columns: List<String>,
    val unique: Boolean,
    val type: String
)

/**
 * представлення базової реалізації помічника з метаданими