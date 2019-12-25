package tiOPF
//complete

interface IPersistenceLayerClass{
    fun createInstance(): PersistenceLayer { throw Exception("persistence layer companion must implement createInstance")}}

abstract class PersistenceLayer: Object(){
    companion object : IPersistenceLayerClass

    public override var persistenceLayerName: String
        get() = super.persistenceLayerName
        set(value) { super.persistenceLayerName = value}
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
            assert(field == null || field is DBConnectionPool, { CTIErrorInvalidObject })
            if (field != null)
                return field

            if (dbConnectionPools.count() == 0)
                return null

            val result = dbConnectionPools.get(0)

            assert(result is DBConnectionPool, { CTIErrorInvalidObject })

            return result
        }

    abstract val databaseCompanion: IDatabaseCompanion
    abstract val queryCompanion: IQueryCompanion
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
            assert(defaultDBConnectionPool == null || defaultDBConnectionPool is DBConnectionPool, { CTIErrorInvalidObject})
        }


    val ownerAsPersistenceLayers: PersistenceLayers? get() = super.owner as PersistenceLayers

    override var caption: String = ""
        get() = persistenceLayerName

    fun databaseExists(databaseName: String, userName: String, password: String, params: String = ""): Boolean{
        return  databaseCompanion.databaseExists(databaseName, userName, password, params)
    }
    fun createDatabase(databaseName: String, userName: String, password: String, params: String = ""){
        databaseCompanion.createDatabase(databaseName,userName, password, params)
    }
    fun dropDatabase(databaseName: String, userName: String, password: String, params: String = ""){
        databaseCompanion.dropDatabase(databaseName,userName, password, params)
    }
    fun testConnectionToDatabase(databaseName: String, userName: String, password: String, params: String): Boolean{
        return  databaseCompanion.testConnectTo(databaseName, userName, password, params)
    }

    abstract fun assignPersistenceLayerDefaults(defaults: PersistanceLayerDefaults)
}