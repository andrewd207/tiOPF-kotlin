package tiOPF
// complete
import java.util.*
import kotlin.reflect.KClass

class OIDGeneratorGUID: OIDGenerator() {
    override fun getOIDClass(): KClass<OID> {
        return this::class as KClass<OID>
    }
    override fun assignNextOID(assignTo: OID, databaseAliasName: String, persistenceLayerName: String) {
        val guid = UUID.randomUUID()
        assignTo.asString = guid.toString()
    }
}