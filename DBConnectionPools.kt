package tiOPF
// complete
import tiOPF.Log.LOG
import tiOPF.Log.LogSeverity
import java.util.concurrent.locks.ReentrantLock

const val CErrorAttemptToAddDuplicateDBConnectionPool = "Attempt to register a duplicate database connection: \"%s\""
const val CErrorUnableToFindDBConnectionPool = "Attempt to lock a database connection for a database that has not been registered: \"%s\""

class DBConnectionPools(val persistenceLayer: PersistenceLayer): BaseObject() {
    val maxPoolSize: Int
    val list = ObjectList<DBConnectionPool>()
    val critSect = ReentrantLock()
    val minPoolSize: Int = CDefaultMinPoolSize

    init {
        val defaults = PersistanceLayerDefaults()
        persistenceLayer.assignPersistenceLayerDefaults(defaults)
        maxPoolSize = if (defaults.canSupportMultiUser)
            CDefaultMaxPoolSizeMultiUser
        else
            CDefaultMaxPoolSizeSingleUser
    }

    fun find(databaseAlias: String): DBConnectionPool?{
        // may be called from within an already locked object!
        var result: DBConnectionPool? = null
        list.forEach {
            if (it.databaseAlias.equals(databaseAlias)){
                result = it
                return@forEach
            }
        }

        return result

    }
    fun lock(databaseAlias: String): Database?{
        critSect.lock()
        try {
            val dbConnectionPool = find(databaseAlias) ?: throw EtiOPFProgrammerException(CErrorUnableToFindDBConnectionPool.format(databaseAlias))
            return  dbConnectionPool.lock()
        }
        finally {
            critSect.unlock()
        }
    }
    fun unlock(databaseAlias: String, database: Database){
        critSect.lock()
        try {
            val dbConnectionPool = find(databaseAlias) ?: throw EtiOPFProgrammerException(CErrorUnableToFindDBConnectionPool.format(databaseAlias))
            dbConnectionPool.unlock(database)
        }
        finally {
            critSect.unlock()
        }

    }
    fun connect(databaseAlias: String, databaseName: String, userName: String, password: String, params: String){
        val connectParams = DBConnectionParams(databaseName, userName, password, params)
        critSect.lock()
        try {
            var dbConnectionPool = find(databaseAlias)
            if (dbConnectionPool != null)
                throw EtiOPFProgrammerException(CErrorAttemptToAddDuplicateDBConnectionPool.format(databaseName+'/'+userName))
            LOG(
                "Creating database connection pool for %s/%s".format(databaseName, userName),
                LogSeverity.ConnectionPool
            )
            dbConnectionPool = DBConnectionPool(this, databaseAlias, connectParams)
            val database = dbConnectionPool.lock()
            dbConnectionPool.unlock(database)
            list.add(dbConnectionPool)
        }
        finally {
            critSect.unlock()
        }
    }
    fun addInstance(databaseAlias: String, databaseName: String, userName: String, password: String, params: String){
        val dbConnectionParams = DBConnectionParams(databaseName,userName,password,params)
        critSect.lock()
        try {
            var dbConnectionPool = find(databaseAlias)
            if (dbConnectionPool != null)
                throw EtiOPFProgrammerException(CErrorAttemptToAddDuplicateDBConnectionPool.format(databaseName+"/"+userName))
            dbConnectionPool = DBConnectionPool(this, databaseAlias, dbConnectionParams)
            list.add(dbConnectionPool)
        }
        finally {
            critSect.unlock()
        }
    }
    fun disconnect(databaseAlias: String){
        critSect.lock()
        try {
            var dbConnectionPool = find(databaseAlias)
            if (dbConnectionPool == null)
                throw EtiOPFProgrammerException(CErrorUnableToFindDBConnectionPool.format(databaseAlias))
            val wasDefault = persistenceLayer.defaultDBConnectionPool == dbConnectionPool
            list.remove(dbConnectionPool)

            if (wasDefault && list.size > 0)
                persistenceLayer.defaultDBConnectionName = list.get(0).databaseAlias
            else if (wasDefault && list.size == 0)
                persistenceLayer.defaultDBConnectionName = ""
        }
        finally {
            critSect.unlock()
        }

    }
    fun count(): Int{
        critSect.lock()
        try {
            return list.size
        }
        finally {
            critSect.unlock()
        }
    }
    fun get(index: Int): DBConnectionPool{
        return list.get(index)
    }
    fun disconnectAll(){
        for (i in count()-1 downTo 0){
            disconnect(get(i).databaseAlias)
        }
    }
    fun detailsAsString(): String {
        critSect.lock()
        var result = ""
        try {
            list.forEach {
                result = tiAddTrailingValue(result, tiLineEnd(2), true)
                result += it.detailsAsString()
            }

        } finally {
            critSect.unlock()
        }
        return result
    }
    fun clear(){
        critSect.lock()
        try {
            list.clear()
        }
        finally {
            critSect.unlock()
        }
    }
    fun isConnected(databaseAlias: String): Boolean{
       return find(databaseAlias) != null
    }
}