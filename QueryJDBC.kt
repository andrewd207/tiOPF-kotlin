package tiOPF


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
    protected val sqlParamMap = mutableMapOf<String, Int>()
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
                val isChar = (text[i] in 'a'..'z' || text[i] in 'A'..'Z' || text[i] == '_')
                if (i < text.lastIndex && isChar)
                    i++
                else {
                    val end = i
                    val entry = text.substring(start, end)
                    println("entry: $entry")
                    sqlParamMap[entry] = sqlParamMap.count()+1
                    i -= end-start+1 //+1 because of ?
                    cont = false
                }
            }
        }


        sqlParamMap.forEach{ (str, i) ->
            text = text.replaceFirst(":$str", "?")
        }

        preparedSqlText = text

        println("sql after: $text")




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
            return resultSet == null || resultSet!!.isAfterLast
        }

    override fun setParamAsString(name: String, value: String) {
        checkPrepared()
        statement!!.setString(sqlParamMap[name]!!, value)
    }

    override fun getParamAsString(name: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        //checkPrepared()
        //statement!!.getString(sqlParamMap[name]!!, value)
    }

    override fun getParamAsInteger(name: String): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setParamAsInteger(name: String, value: Long) {
        checkPrepared()
        statement!!.setLong(sqlParamMap[name]!!, value)
    }

    override fun getParamAsFloat(name: String): Float {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setParamAsFloat(name: String, value: Double) {
        checkPrepared()
        statement!!.setFloat(sqlParamMap[name]!!, value.toFloat())
    }

    override fun getParamAsBoolean(name: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setParamAsBoolean(name: String, value: Boolean) {
        checkPrepared()
        statement!!.setBoolean(sqlParamMap[name]!!, value)
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
        statement!!.setBytes(sqlParamMap[name]!!, value.toByteArray())
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setParamIsNull(name: String, value: Boolean) {
        checkPrepared()
        statement!!.setNull(sqlParamMap[name]!!, 0)
    }

    override fun getFieldAsString(name: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFieldAsString(index: Int): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setFieldAsString(name: String, value: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setFieldAsString(index: Int, value: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFieldAsFloat(name: String): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFieldAsFloat(index: Int): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setFieldAsFloat(name: String, value: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setFieldAsFloat(index: Int, value: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFieldAsBoolean(name: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFieldAsBoolean(index: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setFieldAsBoolean(name: String, value: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setFieldAsBoolean(index: Int, value: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFieldAsInteger(name: String): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFieldAsInteger(index: Int): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setFieldAsInteger(name: String, value: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setFieldAsInteger(index: Int, value: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFieldAsDate(name: String): LocalDate {
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFieldIsNull(index: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun open() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        if (resultSet != null && !resultSet!!.isClosed)
            resultSet!!.close()
    }

    override fun execSQL(): Long {
        LOG(className() + ": [Prepare] "+ preparedSqlText, LogSeverity.lsSQL)
        checkPrepared()
//        logParams()
        var result: Long = 0
        resultSet = null
        when (queryType) {
            QueryType.Select -> resultSet = statement!!.executeQuery()
            QueryType.Insert -> result = statement!!.executeUpdate().toLong()
            QueryType.Update -> result = statement!!.executeUpdate().toLong()
            QueryType.Delete -> result = statement!!.executeUpdate().toLong()
            QueryType.DDL    -> statement!!.execute()

        }
        if (resultSet != null){
            resultSet!!.last()
            result = resultSet!!.row.toLong()
        }

        return result
    }

    override fun next() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun paramCount(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun paramName(index: Long): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fieldCount(): Int {
        if (resultSet != null)
            return resultSet!!.metaData.columnCount
        return 0
    }

    override fun fieldName(index: Long): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hasNativeLogicalType(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

interface IDatabaseJDBCCompanion: IDatabaseCompanion{
    val driver: Driver? get() {
            var result: Driver?
            try{
                result = DriverManager.getDriver(getDriverName())
            }
            catch (e: Exception){
                result = null
            }
            return result
        }

    fun connect(url: String, user: String, password: String, props: Properties): Connection? {
        props.setProperty("user", user)
        props.setProperty("password", password)
        val drv = driver
        if (drv == null) {
            println(drv)
        }
        return drv?.connect(url, props)
    }
    fun getDriverName(): String{  // just the name i.e. "firebirdsql"
        throw Exception("getDriverName is abstract")
    }
    override fun databaseExists(databaseName: String, userName: String, password: String, params: String): Boolean {
        val props = Properties()
        val connection =
            DatabaseJDBC.connect("jdbc:${DatabaseJDBC.getDriverName()}://$databaseName", userName, password, props)
        var result: Boolean
        if ( connection != null) {
            connection.use { connection ->
                val resultSet = connection.metaData.catalogs
                resultSet.use { resultSet ->
                    while (resultSet.next()) {
                        val dbName = resultSet.getString(1)
                        if (databaseName.equals(dbName)) {
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
        val connection =
            DatabaseJDBC.connect("jdbc:${DatabaseJDBC.getDriverName()}://$databaseName", userName, password, props)
        val statement = connection?.createStatement()
        if (statement != null){
            val sql = "CREATE DATABASE $databaseName"
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

abstract class DatabaseJDBC: DatabaseSQL(){
    companion object: IDatabaseJDBCCompanion
    private var privInTransaction = false

    //private fun getDriverName(): String {
    //    return (this::class.companionObjectInstance as IDatabaseJDBCCompanion).getDriverName()
    //}
    internal var connection: Connection? = null
    override fun queryClass(): KClass<Query> {
        return QueryJDBC::class as KClass<Query>
    }

    override fun readMetadataTables(data: DBMetadata) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readMetadataFields(data: DBMetadataTable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fieldMetadataToSQLCreate(fieldMetadata: DBMetadataField) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override var connected: Boolean
        get() {
            return (connection != null && !connection!!.isClosed)
        }
        set(value) {
            if (value && !connected){
                val props = Properties()
                var dbName: String = ""
                var dbHost = ""
                var tokenCount = tiNumToken(databaseName, ':')
                if (tokenCount > 0){
                    if (tokenCount > 2)
                        tokenCount = 2
                    dbHost = tiToken(databaseName, ':', 0, tokenCount)
                    dbName  = databaseName.substring(dbHost.length+2, databaseName.lastIndex+1)
                }

                val companion = this::class.companionObjectInstance as IDatabaseJDBCCompanion
                try {
                    connection = companion.connect("jdbc:${companion.getDriverName()}://$dbHost/$dbName", userName, password, props)
                }
                catch (e: Exception){
                    throw EtiOPFDBExceptionUserNamePassword("jdbc", databaseName, userName, password, "Error attempting to connect to database.\n"+e.message)
                }
                catch (e: Exception) {
                    throw EtiOPFDBException("jdbc", databaseName, userName, password, "Error attempting to connect to database.\n${e.message}")
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