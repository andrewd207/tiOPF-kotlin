package tiOPF

import java.time.ZonedDateTime
import java.util.concurrent.locks.ReentrantLock
import kotlin.reflect.KClass

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
    var defaultPerLayer: PersistenceLayer? = persistanceLayers.defaultPersistenceLayer
    var defaultOIDGenerator: OIDGenerator = OIDGeneratorGUID()
    val defaultPerLayerName: String
        get() {
            if (defaultPerLayer != null)
                return defaultPerLayer!!.persistenceLayerName
            return ""
        }
    val defaultDBConnectionPool: DBConnectionPool? get() { return defaultPerLayer?.defaultDBConnectionPool }
    var defaultDBConnectionName: String
        get() {
            var result = ""
            if (defaultPerLayer != null)
                result = defaultPerLayer!!.defaultDBConnectionName

            return result
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

    fun read(visited: Visited, dbConnectionName: String, persistanceLayerName: String = ""){
        visitorManager.execute(CuStandardTask_Read, visited, dbConnectionName, persistenceLayerName)
    }

    init {

    }



}