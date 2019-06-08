package tiOPF
//complete

import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject

interface IPersistenceLayerClass{
    fun createInstance(): PersistenceLayer { throw Exception("persistence layer companion must implement createInstance")}}

abstract class PersistenceLayer: Object(){
    companion object : IPersistenceLayerClass
    private  var privdbConnectionPools: DBConnectionPools? = null
    val dbConnectionPools: DBConnectionPools
      get() { // all this is so we don't leak "this" in constructor
          if (privdbConnectionPools == null) {
              privdbConnectionPools = DBConnectionPools(this)
          }

          return privdbConnectionPools!!
      }
    var defaultDBConnectionPool: DBConnectionPool? = null
        get() {
            assert(testValid(field, DBConnectionPool::class, true), { CTIErrorInvalidObject })
            if (field != null)
                return field

            if (dbConnectionPools.count() == 0)
                return null

            val result = dbConnectionPools.get(0)

            assert(testValid(result, DBConnectionPool::class), { CTIErrorInvalidObject })

            return result
        }

    abstract val databaseClass: IDatabaseClass
    abstract val queryClass: IQueryClass
    var dynamicallyLoaded: Boolean = false
    var moduleId: Int = 0 // HModule?!
    var defaultDBConnectionName: String
        get() {
            val pool = defaultDBConnectionPool
            if (pool != null)
                return pool!!.databaseAlias

            return ""
        }
        set(value) {
            defaultDBConnectionPool = dbConnectionPools.find(value)
            assert(testValid(defaultDBConnectionPool, DBConnectionPool::class, true), { CTIErrorInvalidObject})
        }


    val ownerAsPersistenceLayers: PersistenceLayers? get() = super.owner as PersistenceLayers

    override var caption: String = ""
        get() = persistenceLayerName

    fun databaseExists(databaseName: String, userName: String, password: String, params: String = ""): Boolean{
        return  databaseClass.databaseExists(databaseName, userName, password, params)
    }
    fun createDatabase(databaseName: String, userName: String, password: String, params: String = ""){
        databaseClass.createDatabase(databaseName,userName, password, params)
    }
    fun dropDatabase(databaseName: String, userName: String, password: String, params: String = ""){
        databaseClass.dropDatabase(databaseName,userName, password, params)
    }
    fun testConnectionToDatabase(databaseName: String, userName: String, password: String, params: String): Boolean{
        return  databaseClass.testConnectTo(databaseName, userName, password, params)
    }

    abstract fun assignPersistenceLayerDefaults(defaults: PersistanceLayerDefaults)
}