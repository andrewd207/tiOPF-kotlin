package tiOPF
// complete
import tiOPF.Log.LOG
import tiOPF.Log.LogSeverity
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

interface IDatabaseCompanion {
    fun createInstance(): Database{
        throw Exception("companion must implement IDatabaseCompanion.createInstance()")
    }
    fun databaseExists(databaseName: String, userName: String, password: String, params: String = ""): Boolean{return false}
    fun createDatabase(databaseName: String, userName: String, password: String, params: String = ""){}
    fun dropDatabase(databaseName: String, userName: String, password: String, params: String = ""){}
    fun testConnectTo(databaseName: String, userName: String, password: String, params: String = ""): Boolean{return false}
    fun klass(): KClass<Database>{
        throw Exception("abstract class \"Database\" cannot be used directly")
    }

}

abstract class Database: BaseObject() {
    companion object: IDatabaseCompanion
    var databaseName = ""
    var userName = ""
    var password = ""
    abstract var connected: Boolean
    var errorInLastCall = false
    protected val params = Properties()

    abstract fun startTransaction()
    abstract fun inTransaction(): Boolean
    abstract fun commit()
    abstract fun rollback()
    abstract fun test(): Boolean
    abstract fun queryClass(): KClass<Query>
    fun createQuery(): Query{
        assert(queryClass() != null, {"queryCompanion not assigned"})
        return queryClass().primaryConstructor!!.call()
    }
    fun createAndAttachQuery(): Query{
        assert(queryClass() != null, {"queryCompanion not assigned"})
        val result: Query = queryClass().primaryConstructor!!.call()
        result.attachDatabase(this)
        return result
    }

    abstract fun readMetadataTables(data: DBMetadata)
    abstract fun readMetadataFields(data: DBMetadataTable)
    open fun execSQL(sql: String, params: QueryParams? = null){
        val query = createAndAttachQuery()
        val hadToStartTransaction = !inTransaction()
        if (hadToStartTransaction)
            startTransaction()
        query.sqlText = sql
        query.assignParams(params)
        try {
            query.execSQL()
            if (hadToStartTransaction)
                commit()
        }
        catch (e: Exception) {
            var message = e.message!!
            if (hadToStartTransaction)
                try {
                    rollback()
                }
                catch (e: Exception) {
                    message += tiLineEnd() + "Error rolling transaction after SQL failed:" + tiLineEnd() + e.message
                }
            throw EtiOPFProgrammerException(message)
        }
    }
    fun dropTable(tableName: String){
        val dbMetadataTable = DBMetadataTable()
        dbMetadataTable.name = tableName
        dropTable(dbMetadataTable)

    }
    abstract fun dropTable(tableMetadata: DBMetadataTable)
    abstract fun createTable(tableMetadata: DBMetadataTable)
    open fun deleteRow(tableName: String, where: QueryParams){
        val query = createAndAttachQuery()
        val hadToStartTransaction = !inTransaction()
        if (hadToStartTransaction)
            startTransaction()
        try {
            query.deleteRow(tableName, where)
            if (hadToStartTransaction)
                commit()
        }
        catch (e: Exception){
            if (hadToStartTransaction)
                rollback()
            throw e
        }
    }
    open fun insertRow(tableName: String, params: QueryParams){
        val query = createAndAttachQuery()
        val hadToStartTransaction = !inTransaction()
        if (hadToStartTransaction)
            startTransaction()
        try {
            query.insertRow(tableName, params)
            if (hadToStartTransaction)
                commit()
        }
        catch (e: Exception){
            if (hadToStartTransaction)
                rollback()
            throw e
        }
    }

    open fun updateRow(tableName: String, params: QueryParams, where: QueryParams){
        val query = createAndAttachQuery()
        val hadToStartTransaction = !inTransaction()
        if (hadToStartTransaction)
            startTransaction()
        try {
            query.updateRow(tableName,params, where)
            if (hadToStartTransaction)
                commit()
        }
        catch (e: Exception){
            if (hadToStartTransaction)
                rollback()
            throw e
        }
    }

    fun connect(databaseName: String, userName: String, password: String, params: String){
        LOG(
            "Attempting to connect to: %s Params: %s".format(databaseName, params),
            LogSeverity.ConnectionPool
        );
        this.databaseName = databaseName
        this.userName = userName
        this.password = password

        if (params.isNotEmpty()) {
            val paramsList = params.split(",").map { it.trim() }
            paramsList.forEach {
                val list = it.split("=")
                this.params.setProperty(list[0], list[1])
            }
        }

        // if verboseDBConnection
        //LOG("Connected. Database: $databaseName UserName: $userName Password: password")
        if (connected)
            connected = false

        connected = true

        LOG(
            "Connect to $databaseName successful.",
            LogSeverity.ConnectionPool
        )
    }
}