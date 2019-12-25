package tiOPF

import java.io.File
import java.io.File.separator
import kotlin.reflect.KClass

const val CDefaultDatabaseName = "Demo"
      val CDefaultDatabaseDirectory = ".. $separator _Data $separator"

class PersistenceLayers: ObjectList<PersistenceLayer>() {
    enum class LoadingStyle {StaticLinking, DynamicLoading}
    var loadingStyle = LoadingStyle.StaticLinking
    var defaultPersistenceLayer: PersistenceLayer? = null // TODO change default to subclass
    var defaultPersistenceLayerName: String
    get() {
        return defaultPersistenceLayer!!.persistenceLayerName
    }
    set(value) { defaultPersistenceLayer = findByPersistanceLayerName(value)}

    fun findByPersistanceLayerName(layerName: String): PersistenceLayer?{
        forEach {
            if (it.persistenceLayerName === layerName) return it
        }
        return null
    }
    fun findByDatabaseClass(klass: KClass<*>): PersistenceLayer?{
        assert(klass != null, { "database class != null "})
        forEach {
            if (it.databaseCompanion.klass() == klass)
                return it
        }
        return null
    }
    fun lockDatabase(dbConnectionName: String, persistenceLayerName: String): Database{
        val regPerLayer: PersistenceLayer? = if (persistenceLayerName.isNotEmpty())
            findByPersistanceLayerName(persistenceLayerName)
        else
            defaultPersistenceLayer

        assert(defaultPersistenceLayer is PersistenceLayer, { CTIErrorInvalidObject})

        val dbConnectionName = {
            if (dbConnectionName.isNotEmpty())
                dbConnectionName
            else
                regPerLayer!!.defaultDBConnectionName
        }

        return regPerLayer!!.dbConnectionPools.lock(dbConnectionName.toString())!!


    }
    fun unlockDatabase(database: Database, dbConnectionName: String, persistenceLayerName: String){
        var regPerLayer: PersistenceLayer
        if (persistenceLayerName.isNotEmpty())
            regPerLayer = findByPersistanceLayerName(persistenceLayerName)!!
        else
            regPerLayer = defaultPersistenceLayer!!

        assert(regPerLayer is PersistenceLayer, { CTIErrorInvalidObject })
        var dbConnectionName = dbConnectionName
        if (dbConnectionName.isEmpty())
            dbConnectionName = regPerLayer.defaultDBConnectionName
        regPerLayer.dbConnectionPools.unlock(dbConnectionName, database)
    }
    fun isLoaded(persistenceLayerName: String): Boolean{
        return findByPersistanceLayerName(persistenceLayerName) != null
    }
    fun isDefault(persistenceLayerName: String): Boolean{
        return defaultPersistenceLayerName.equals(persistenceLayerName, true)
    }
    fun createQuery(layerName: String): Query{
        val persistenceLayer = findByPersistanceLayerName(layerName)
        if (persistenceLayer == null)
            throw Exception("Request for unregistered persistence layer <$layerName>")
        else
            return persistenceLayer.queryCompanion.createInstance()

    }
    fun createQuery(databaseClass: KClass<Database>): Query{
        val persistenceLayer = findByDatabaseClass(databaseClass)
        if (persistenceLayer == null)
            throw Exception("Unable to find persistence layer for database class <${databaseClass.simpleName}>")
        else
            return persistenceLayer.queryCompanion.createInstance()
    }
    fun createDatabase(layerName: String): Database{
        val persistenceLayer = findByPersistanceLayerName(layerName)
        if (persistenceLayer == null)
            throw Exception("Request for unregistered persistence layer <$layerName>")
        else
            return persistenceLayer.databaseCompanion.createInstance()
    }

    // Do not call these yourself. They are called in the initialization section
    // of QueryXXX.kt that contains the concrete classes.
    fun __registerPersistenceLayer(persistenceLayerClass: IPersistenceLayerClass){
        println("Registered Layer per ${persistenceLayerClass.createInstance().persistenceLayerName}")
        assert(persistenceLayerClass != null, { "persistenceLayerClass not assigned"})
        val data = persistenceLayerClass.createInstance()
        if (!isLoaded(data.persistenceLayerName))
            add(data)
    }


/* // TODO(can packages be loaded dynamically?)
private fun packageIdToPackageName(packageId: String): String{
        return ""
    }

    fun loadPersistenceLayer(layerName: String): PersistenceLayer{
        val result = findByPersistanceLayerName(layerName)
        if (result != null)
            return result

        val packageName = packageIdToPackageName(persistenceLayerName)

        // TODO ( not sure dynamic is possible )

    }
*/
}