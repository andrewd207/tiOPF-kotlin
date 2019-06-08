package tiOPF
// complete
import kotlin.reflect.KClass

abstract class OIDGenerator: BaseObject(){
    abstract fun getOIDClass(): KClass<OID>
    abstract fun assignNextOID(assignTo: OID, databaseAliasName: String ="", persistenceLayerName: String ="")
}