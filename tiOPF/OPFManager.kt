package tiOPF

import java.time.ZonedDateTime
import java.util.concurrent.locks.ReentrantLock
import kotlin.reflect.KClass

const val CErrorUnableToFindPerLayer = "Unable to find persistence layer <%s>"

var UOPFManager: OPFManager? = null
var UShuttingDown: Boolean = false

fun ShuttingDown(): Boolean{
    return UShuttingDown
}
fun GTIOPFManager(): OPFManager{
    if (UOPFManager == null) {
        if (ShuttingDown())
            throw Exception(CErrorCallToTIPerMgrWhileShuttingDown)

        UOPFManager = OPFManager()
    }
    return UOPFManager!!
}

class OPFManager: Object() {
    private val criticalSection = ReentrantLock()
    val persistanceLayers = PersistenceLayers()
    val visitorManager = ObjectVisitorManager(this)
    var terminated = false
    val activeThreadList = ActiveThreadList()
    val applicationData = List<Object>()
    val applicationStartTime = ZonedDateTime.now()
    var defaultPerLayer: PersistenceLayer?
        get() {return persistanceLayers.defaultPersistenceLayer}
        set(value) {persistanceLayers.defaultPersistenceLayer = value}
    var defaultOIDGenerator: OIDGenerator = OIDGeneratorGUID()
    var defaultPerLayerName: String
        get() {
            if (defaultPerLayer != null)
                return defaultPerLayer!!.persistenceLayerName
            return ""
        }
        set(value) {persistanceLayers.defaultPersistenceLayerName = value}
    val defaultDBConnectionPool: DBConnectionPool? get() { return defaultPerLayer?.defaultDBConnectionPool }
    var defaultDBConnectionName: String
        get() {
            if (defaultPerLayer != null)
                return defaultPerLayer!!.defaultDBConnectionName

            return ""
        }
        set(value) {
            if (defaultPerLayer != null)
                defaultPerLayer!!.defaultDBConnectionName = value
        }
    private var privClassDBMappingManager: ClassDBMappingManager? = null
    val classDBMappingManager: ClassDBMappingManager
        get() {
            if (privClassDBMappingManager == null) {
                privClassDBMappingManager = ClassDBMappingManager()
                privClassDBMappingManager!!.owner = this

                visitorManager.registerVisitor(CuStandardTask_ReadPK,   VisAutoCollectionPKRead::class as KClass<Visitor> )
                visitorManager.registerVisitor(CuStandardTask_ReadThis, VisAutoReadThis::class as KClass<Visitor> )
                visitorManager.registerVisitor(CuStandardTask_Read,     VisAutoReadThis::class as KClass<Visitor> )
                visitorManager.registerVisitor(CuStandardTask_Read,     VisAutoCollectionRead::class as KClass<Visitor> )
                visitorManager.registerVisitor(CuStandardTask_Save,     VisAutoDelete::class as KClass<Visitor> )
                visitorManager.registerVisitor(CuStandardTask_Save,     VisAutoUpdate::class as KClass<Visitor> )
                visitorManager.registerVisitor(CuStandardTask_Save,     VisAutoCreate::class as KClass<Visitor> )
            }

            return privClassDBMappingManager!!
        }

    fun connectDatabase(databaseName: String, username: String, password: String, params: String, persistanceLayerName: String){
        connectDatabase(databaseName, databaseName, username, password, params, persistanceLayerName)
    }

    fun connectDatabase(databaseName: String, username: String, password: String, params: String){
        connectDatabase(databaseName, databaseName, username, password, params, "")
    }
    fun connectDatabase(databaseName: String, username: String, password: String){
        connectDatabase(databaseName, databaseName, username, password, "", "")
    }

    fun connectDatabase(databaseAlias: String, databaseName: String, username: String, password: String, params: String, persistanceLayerName: String){
        val lPersistanceLayer =
            (if ( persistanceLayerName.isEmpty())
                defaultPerLayer
            else
                persistanceLayers.findByPersistanceLayerName(persistanceLayerName))
                ?: throw EtiOPFException(CErrorUnableToFindPerLayer.format(persistanceLayerName))

        lPersistanceLayer.dbConnectionPools.connect(databaseAlias, databaseName, username, password, params)

        if (lPersistanceLayer.defaultDBConnectionName.isEmpty())
            lPersistanceLayer.defaultDBConnectionName = databaseName

    }
    fun testThenConnectDatabase(){
        TODO("not implemented yet")
    }
    fun connectDatabaseWithRetry(){
        TODO("not implemented yet")
    }

    fun createTable(tableMetadata: DBMetadataTable, dbConnectionName: String ="", persistenceLayerName: String = ""){
        val db = persistanceLayers.lockDatabase(dbConnectionName, persistenceLayerName)
        try {
            db.createTable(tableMetadata)
        }
        finally {
            persistanceLayers.unlockDatabase(db, dbConnectionName, persistenceLayerName)
        }
    }

    fun createDatabase(databaseName: String, username: String, password: String, persistenceLayerName: String = ""){
        val layer = persistanceLayers.findByPersistanceLayerName(persistenceLayerName)
            ?: throw EtiOPFInternalException(CErrorUnableToFindPerLayer.format(persistenceLayerName))
        layer.databaseCompanion.createDatabase(databaseName, username, password)
    }

    fun dropDatabase(databaseName: String, username: String, password: String, persistenceLayerName: String = "") {
        val layer = persistanceLayers.findByPersistanceLayerName(persistenceLayerName)
            ?: throw EtiOPFInternalException(CErrorUnableToFindPerLayer.format(persistenceLayerName))
        layer.databaseCompanion.dropDatabase(databaseName, username, password)
    }

    fun databaseExists(databaseName: String, username: String, password: String, persistenceLayerName: String = ""): Boolean {
        val layer = persistanceLayers.findByPersistanceLayerName(persistenceLayerName)
            ?: throw EtiOPFInternalException(CErrorUnableToFindPerLayer.format(persistenceLayerName))
        return layer.databaseCompanion.databaseExists(databaseName, username, password)
    }
}