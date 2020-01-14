package tiOPF


import tiOPF.Log.LOG
import tiOPF.Log.LogSeverity
import java.sql.*
import java.time.LocalDate
import java.util.*
import java.util.Date
import kotlin.Exception
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

open class QueryJDBC : QuerySQL(){
    companion object: IQueryCompanion{
        override fun createInstance(): Query {
            return QueryJDBC()
        }
    }
    override var active: Boolean
        get() {
            return statement != null && !statement!!.isClosed
        }
        set(value) {
            if (value)
                statement?.executeQuery()
            else
                statement?.close()
        }
    protected class ParamEntry(val name: String, var index: Int) {
        var value: Any? = null
        fun valueIsNull(): Boolean { return value == null}
    }
    protected val sqlParamMap = mutableMapOf<String, ParamEntry>()
    private var nextWasNull = false
    private fun assignParamMap(){
        // turn :SOME_VALUE into ? and record the index
        sqlParamMap.clear()
        var text = sqlText
        val indexes = mutableListOf<Int>()
        // first find all : chars that lead the variable names
        var index = text.indexOf(':')
        while (index >= 0) {
            indexes.add(index)
            index = text.indexOf(':', index+1)
        }

        indexes.forEach{
            val start = it+1
            var i = start
            var cont = true
            while (cont) {


                val isChar = (i <= text.lastIndex &&
                        (text[i] in 'a'..'z' ||
                         text[i] in 'A'..'Z' ||
                         text[i] == '_' ||
                         text[i] in '0'..'9'))

                if (i <= text.lastIndex && isChar)
                    i++
                else {
                    val end = i
                    val entry = text.substring(start, end)
                    LOG("converting '$entry' to '?' for JDBC at index ${sqlParamMap.count()+1}", LogSeverity.SQL)
                    sqlParamMap[entry] = ParamEntry(entry, sqlParamMap.count()+1)
                    i -= end-start+1 //+1 because of ?
                    cont = false
                }
            }
        }


        sqlParamMap.forEach{ (str, i) ->
            text = text.replaceFirst(":$str", "?")
        }

        preparedSqlText = text

        LOG("sql after: $text", LogSeverity.SQL)




    }

    override var sqlText: String
        get() = super.sqlText
        set(value) {super.sqlText = value ; assignParamMap() }
    protected var preparedSqlText  = ""
    private var connection: Connection? = null
    private var privSql = List<String>()
    override val sql: List<String> = privSql
    private var statementCleared: Boolean = true
    private var resultSet: ResultSet? = null
    private val resultMetadata: ResultSetMetaData? get() {
        if (resultSet != null)
            return resultSet!!.metaData
        return null
    }
    private var statement: PreparedStatement? = null
    private fun prepare(){
        if (statement == null){
           // statement = PreparedStatement()
            statement = connection!!.prepareStatement(preparedSqlText)
        } else {
            throw Exception("tried to prepare an already prepared statement")
        }

    }

    override fun attachDatabase(database: Database) {
        super.attachDatabase(database)
        if (database is DatabaseJDBC) {
            connection = database.connection
        }
    }

    override fun detachDatabase() {
        super.detachDatabase()
        if (resultSet != null && !resultSet!!.isClosed){
            resultSet!!.close()
            resultSet = null
        }
    }

    override fun reset() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    protected open fun checkPrepared(){
        if (statement == null)
            prepare()

    }

    override val eof: Boolean
        get() {
            return resultSet == null || resultSet!!.isAfterLast || nextWasNull || (resultSet!!.isBeforeFirst && resultSet!!.isAfterLast)
        }

    override fun setParamAsString(name: String, value: String) {
        checkPrepared()
        sqlParamMap[name]!!.value = value
        statement!!.setString(sqlParamMap[name]!!.index, value)
    }

    override fun getParamAsString(name: String): String {
        if (sqlParamMap[name]!!.valueIsNull()) return ""
        val value = sqlParamMap[name]!!.value
        return when (value) {
            is String -> value
            is Number -> value.toString()
            else -> value.toString()
        }

    }

    override fun getParamAsInteger(name: String): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setParamAsInteger(name: String, value: Long) {
        checkPrepared()
        sqlParamMap[name]!!.value = value
        statement!!.setLong(sqlParamMap[name]!!.index, value)
    }

    override fun getParamAsFloat(name: String): Float {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setParamAsFloat(name: String, value: Double) {
        checkPrepared()
        sqlParamMap[name]!!.value = value
        statement!!.setFloat(sqlParamMap[name]!!.index, value.toFloat())
    }

    override fun getParamAsBoolean(name: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setParamAsBoolean(name: String, value: Boolean) {
        checkPrepared()
        sqlParamMap[name]!!.value = value
        statement!!.setBoolean(sqlParamMap[name]!!.index, value)
    }

    override fun getParamAsDate(name: String): Date {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setParamAsDate(name: String, value: Date) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        //checkPrepared()
        //statement!!.setDate(sqlParamMap[name]!!, value.)
    }

    override fun getParamAsTextBLOB(name: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setParamAsTextBLOB(name: String, value: String) {
        checkPrepared()
        sqlParamMap[name]!!.value = value
        statement!!.setBytes(sqlParamMap[name]!!.index, value.toByteArray())
    }

    override fun getParamAsMacro(name: String): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setParamAsMacro(name: String, value: ByteArray) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        //checkPrepared()
        //statement!!.setXXX(sqlParamMap[name]!!, value)
    }

    override fun getParamIsNull(name: String): Boolean {
        return sqlParamMap[name]!!.valueIsNull()
    }

    override fun setParamIsNull(name: String, value: Boolean) {
        checkPrepared()
        if (value) {
            sqlParamMap[name]!!.value = null
            statement!!.setNull(sqlParamMap[name]!!.index, 0)
        }
    }

    override fun getFieldAsString(name: String): String {
        return resultSet!!.getString(name)
    }

    override fun getFieldAsString(index: Int): String {
        return resultSet!!.getString(index)
    }

    override fun setFieldAsString(name: String, value: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setFieldAsString(index: Int, value: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFieldAsFloat(name: String): Double {
        return resultSet!!.getFloat(name).toDouble()
    }

    override fun getFieldAsFloat(index: Int): Double {
        return resultSet!!.getFloat(index).toDouble()
    }

    override fun setFieldAsFloat(name: String, value: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setFieldAsFloat(index: Int, value: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFieldAsBoolean(name: String): Boolean {
        return resultSet!!.getBoolean(name)
    }

    override fun getFieldAsBoolean(index: Int): Boolean {
        return resultSet!!.getBoolean(index)
    }

    override fun setFieldAsBoolean(name: String, value: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setFieldAsBoolean(index: Int, value: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFieldAsInteger(name: String): Long {
        return resultSet!!.getLong(name)
    }

    override fun getFieldAsInteger(index: Int): Long {
        return resultSet!!.getLong(index)
    }

    override fun setFieldAsInteger(name: String, value: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setFieldAsInteger(index: Int, value: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFieldAsDate(name: String): LocalDate {
        //return resultSet!!.getDate(name)
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFieldAsDate(index: Int): LocalDate {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setFieldAsDate(name: String, value: LocalDate) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setFieldAsDate(index: Int, value: LocalDate) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFieldIsNull(name: String): Boolean {
        val index = resultSet!!.findColumn(name)
        return getFieldIsNull(index)
    }

    override fun getFieldIsNull(index: Int): Boolean {
        val type = resultSet!!.metaData.getColumnType(index)
        when (type) {
            //Types.BLOB
            //    -> { return true}
            Types.BIGINT,
            Types.SMALLINT,
            Types.INTEGER
            -> {
                getFieldAsInteger(index)
                return resultSet!!.wasNull()
            }
            Types.FLOAT,
            Types.DOUBLE,
            Types.DECIMAL
            -> {
                getFieldAsFloat(index)
                return resultSet!!.wasNull()
            }
            Types.BIT,
            Types.BOOLEAN
            -> {
                getFieldAsBoolean(index)
                return resultSet!!.wasNull()
            }
            Types.CHAR,
            Types.VARCHAR
            -> {
                getFieldAsString(index)
                return resultSet!!.wasNull()
            }
            Types.DATE
            -> {
                resultSet!!.getDate(index)
                return resultSet!!.wasNull()

            }
        }

        throw Exception("Unhandled field type: %s".format(type.toString()))
    }

    override fun open() {
        execSQL()
        /*checkPrepared()
        resultSet = statement!!.executeQuery()
        resultSet?.next()*/
    }

    override fun assignFieldAsByteArray(index: Int, data: ValueOut<ByteArray>) {
        val blob = resultSet!!.getBlob(index)
        data.value = blob.getBytes(0, blob.length().toInt())
    }

    override fun assignFieldAsByteArray(name: String, data: ValueOut<ByteArray>) {
        val blob = resultSet!!.getBlob(name)
        data.value = blob.getBytes(0, blob.length().toInt())
    }

    override fun close() {
        if (resultSet != null) {
            resultSet!!.close()
            resultSet = null
        }

    }

    override fun execSQL(): Long {
        LOG(
            className() + ": [Prepare] " + preparedSqlText,
            LogSeverity.SQL
        )
        nextWasNull = false
        checkPrepared()
        logParams()
        var result: Long = 0
        resultSet = null
        when (queryType) {
            QueryType.Select -> resultSet = statement!!.executeQuery()
            QueryType.Insert -> result = statement!!.executeUpdate().toLong()
            QueryType.Update -> result = statement!!.executeUpdate().toLong()
            QueryType.Delete -> result = statement!!.executeUpdate().toLong()
            QueryType.DDL    -> statement!!.execute()

        }

        if (resultSet != null) {
            next() // will set nextWasNull or move us to the first row
        }

        if (resultSet != null && !nextWasNull && resultSet!!.type != ResultSet.TYPE_FORWARD_ONLY) {
            // hack to get the row count, not supported on all databases
                resultSet!!.last()
                result = resultSet!!.row.toLong()
                resultSet!!.first()
        }
        return result
    }

    override fun next() {
        nextWasNull = !resultSet!!.next()
    }

    override fun paramCount(): Long {
        return sqlParamMap.size.toLong()
    }

    override fun paramName(index: Long): String {
        var result = ""
        sqlParamMap.forEach { (name, i) ->
            if (i.index == (index +1).toInt())
                result = name
                return@forEach
        }
        return result
    }

    override fun fieldCount(): Int {
        if (resultSet != null)
            return resultSet!!.metaData.columnCount
        return 0
    }

    override fun fieldName(index: Long): String {
        return (
            if (resultMetadata != null)
                resultMetadata!!.getColumnName(index.toInt())
            else
               "")
    }

    override fun fieldIndex(name: String): Int {
        if (resultSet != null)
            for (i in 0 until resultSet!!.metaData.columnCount){
                if (resultSet!!.metaData.getColumnName(i).equals(name))
                    return i
            }
        return -1

    }

    override fun fieldKind(index: Int): QueryFieldKind {
        if (resultSet != null) {
            val type = resultSet!!.metaData.getColumnType(index)
            when (type) {
                java.sql.Types.BLOB
                            -> return QueryFieldKind.Binary
                java.sql.Types.BIGINT,
                java.sql.Types.SMALLINT,
                java.sql.Types.INTEGER
                            -> return QueryFieldKind.Integer
                java.sql.Types.FLOAT,
                java.sql.Types.DOUBLE,
                java.sql.Types.DECIMAL
                            -> return QueryFieldKind.Float
                java.sql.Types.BIT,
                java.sql.Types.BOOLEAN
                            -> return QueryFieldKind.Logical
                java.sql.Types.CHAR,
                java.sql.Types.VARCHAR
                            -> return QueryFieldKind.String
                java.sql.Types.DATE
                            -> return QueryFieldKind.DateTime

            }
            throw EtiOPFException(CTIOPFUnsupportedFieldType.format(type))

        }


        throw EtiOPFException("resultSet is null")

    }

    override fun fieldSize(index: Int): Long {
        return 0
    }

    override fun hasNativeLogicalType(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

interface IDatabaseJDBCCompanion: IDatabaseCompanion{
    val driver: Driver? get() {
            LOG("Requesting driver instance from JDBC: ${getDriverName()}", LogSeverity.ConnectionPool)
            return DriverManager.getDriver(getDriverName())
            // may throw an exception but it's better to catch it somewhere else
        }

    fun connect(url: String, user: String, password: String, props: Properties): Connection? {
        props.setProperty("user", user)
        props.setProperty("password", password)
        val drv = driver

        return drv?.connect(url, props)
    }
    fun getDriverName(): String{  // just the name i.e. "jdbc:firebirdsql:"
        throw Exception("getDriverName is abstract")
    }
    override fun databaseExists(databaseName: String, userName: String, password: String, params: String): Boolean {
        val props = Properties()
        val dbParts = DatabaseNameAsParts(databaseName, getDriverName())
        val url = dbParts.urlHostOnly()
        val connection =
            connect(url, userName, password, props)
        var result: Boolean
        if ( connection != null) {
            connection.use { connection ->
                val resultSet = connection.metaData.catalogs
                resultSet.use { resultSet ->
                    while (resultSet.next()) {
                        val dbName = resultSet.getString(1)
                        if (dbParts.name.equals(dbName, true)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }
    override fun createDatabase(databaseName: String, userName: String, password: String, params: String) {
        val props = Properties()
        val dbParts = DatabaseNameAsParts(databaseName, getDriverName())
        val url = dbParts.urlHostOnly()
        val connection =
            connect(url, userName, password, props)
        val statement = connection?.createStatement()
        if (statement != null){
            val sql = "CREATE DATABASE ${dbParts.name}"
            statement.execute(sql)
        }
        connection?.close()
    }
    override fun dropDatabase(databaseName: String, userName: String, password: String, params: String) {
        val props = Properties()
        val connection =
            DatabaseJDBC.connect("jdbc:${DatabaseJDBC.getDriverName()}://$databaseName", userName, password, props)
        if (connection != null)
            connection.use { connection ->
                val statement = connection?.createStatement()
                if (statement != null) {
                    val sql = "DROP DATABASE $databaseName"
                    statement.executeUpdate(sql)
                }
            }
    }
}

class DatabaseNameAsParts(val databaseName: String, val driverName: String){
    var host = ""
    var name = ""
    private var hostPrefix = ""
    private var namePrefix = ""
    init {
        var tokenCount = tiNumToken(databaseName, ':')
        if (tokenCount > 1){
            if (tokenCount > 3)
                tokenCount = 3
            hostPrefix = "//"
            namePrefix = "/"
            host = tiToken(databaseName, ':', 1, tokenCount)
            name  = databaseName.substring(host.length+2, databaseName.lastIndex+1)
        }
        else
            name = databaseName
    }
    fun url(): String {
        return "$driverName$hostPrefix$host$namePrefix$name"

    }
    fun urlHostOnly(): String {
        return "$driverName$hostPrefix$host"
    }
}

abstract class DatabaseJDBC: DatabaseSQL(){
    companion object: IDatabaseJDBCCompanion{
        var privDriver: Driver? = null
        override val driver: Driver?
            get() {
                if (privDriver == null)
                    privDriver = super.driver
                return privDriver
            }
    }
    private var privInTransaction = false

    private fun getDriverName(): String {
        return (this::class.companionObjectInstance as IDatabaseJDBCCompanion).getDriverName()
    }
    internal var connection: Connection? = null
    override fun queryClass(): KClass<Query> {
        return QueryJDBC::class as KClass<Query>
    }

    override fun readMetadataTables(data: DBMetadata) {
        TODO("readMetadataTables must be implemented in subclass")
    }

    override fun readMetadataFields(data: DBMetadataTable) {
        TODO("readMetadataFields must be implemented in subclass")
    }

    override fun fieldMetadataToSQLCreate(fieldMetadata: DBMetadataField):String {
        TODO("fieldMetadataToSQLCreate must be implemented in subclass")
    }

    override var connected: Boolean
        get() {
            return (connection != null && !connection!!.isClosed)
        }
        set(value) {
            if (value && !connected){
                val props = Properties()
                val companion = this::class.companionObjectInstance as IDatabaseJDBCCompanion
                val dbParts = DatabaseNameAsParts(databaseName, companion.getDriverName())


                try {
                    val url = dbParts.url()
                    LOG("JDBC: $url", LogSeverity.SQL)
                    connection = companion.connect(url, userName, password, props)
                }
                catch (e: Exception){
                    throw EtiOPFDBExceptionUserNamePassword("${className()}\n", databaseName, userName, password, "Error attempting to connect to database.\n${e}")
                }
            }
        }

    override fun startTransaction() {
        if (inTransaction())
            throw EtiOPFException("Attempted to start a new transaction during a transaction")

        connection!!.autoCommit = false
        privInTransaction = true
    }

    override fun inTransaction(): Boolean {
        return privInTransaction
    }

    override fun commit() {
        connection!!.commit()

        privInTransaction = false
    }

    override fun rollback() {
        connection!!.rollback()
        privInTransaction = false
    }

    override fun test(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

abstract class PersistanceLayerJDBC: PersistenceLayer(){
    override val queryCompanion: IQueryCompanion
        get() = QueryJDBC::class.companionObjectInstance as IQueryCompanion
}