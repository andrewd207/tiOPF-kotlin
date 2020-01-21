package tiOPF
// complete
import tiOPF.Log.LOG
import tiOPF.Log.LogSeverity
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.findAnnotation

class ClassDBMappingManager: Object() {
    val classMaps =  ClassMaps()
    val dbMaps = DBMaps()
    val attrColMaps = AttrColMaps()
    val collections = ClassDBCollections()
    fun registerMapping(databaseName: String, kClass: KClass<*>, tableName: String, attrName: String, colName: String, pkInfo: PKInfo = setOf()){
        val dbMap = dbMaps.findCreate(databaseName)
        val dbTableMap = dbMap.findCreate(tableName)
        val dbColMap = dbTableMap.addColMap(colName, pkInfo)

        val classMap = classMaps.findCreate(kClass)
        val attrMap = classMap.addAttrMap(attrName)
        attrMap.objectState = PerObjectState.Clean
        attrMap.property = getClassProperty(kClass, attrName) as KMutableProperty<*>
        attrColMaps.addMapping(attrMap, dbColMap)
    }
    fun registerMapping(kClass: KClass<*>, tableName: String? = null, oidField: String? = null){
        val published = kClass.findAnnotation<Published>()
        val publishedClass = kClass.findAnnotation<PublishedClass>()

        val table = (tableName?: publishedClass?.persistenceHint?: published?.persistenceHint?:
            throw Exception("Neither tableName or class annotations Published/PublishedClass(tableName) is set for ${kClass.simpleName}"))

        val oidValue = (oidField?: publishedClass?.oidField?: "OID")

        val propList = mutableListOf<String>()
        val fieldList = mutableListOf<String>()
        getPublishedPropertyNames(kClass, propList, fieldList)

        registerMapping(kClass, table, "oid", oidValue, setOf(ClassDBMapRelationshipType.Primary))
        propList.forEachIndexed { index, propName ->
            if (fieldList[index].isEmpty()){
                if (propName != "caption") {
                    val fieldName = propName.toUpperCase()
                    LOG("Auto-map is using $fieldName for ${kClass.simpleName}.$propName because @Published(field) is empty", LogSeverity.Warning)
                    registerMapping(kClass, table, propName, fieldName, emptySet())
                }
            }
            else {
                registerMapping(kClass, table, propName, fieldList[index], emptySet())
            }


        }
    }

    fun registerMapping(kClass: KClass<*>, tableName: String, attrName: String, colName: String, pkInfo: PKInfo = setOf()){
        registerMapping("", kClass, tableName, attrName, colName, pkInfo)
    }
    fun registerCollection(collectionClass: KClass<PerObjectList>, collectionOfClass: KClass<*>){
        collections.addClassCollectionMapping(collectionClass, collectionOfClass)
    }
    fun registerInheritance(parentClass: KClass<Object>, childClass: KClass<Object>){
        classMaps.registerInheritance(parentClass, childClass)    }
    init {
        attrColMaps.owner = this
        attrColMaps.itemOwner = this
        classMaps.owner = this
        classMaps.itemOwner = this
        dbMaps.owner = this
        dbMaps.itemOwner = this
        collections.owner = this
        collections.itemOwner = this

    }
}