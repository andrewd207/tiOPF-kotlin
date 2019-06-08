package tiOPF
// complete
import kotlin.reflect.KClass

class ClassDBMappingManager: Object() {
    val classMaps =  ClassMaps()
    val dbMaps = DBMaps()
    val attrColMaps = AttrColMaps()
    val collections = ClassDBCollections()
    fun registerMapping(databaseName: String, kClass: KClass<*>, tableName: String, attrName: String, colName: String, pkInfo: PKInfo = setOf()){
        val dbMap = dbMaps.findCreate(databaseName)
        val dbTableMap = dbMap.findCreate(tableName)
        val dbColMap = dbTableMap.addColMap(colName, pkInfo)

        val classMap = classMaps.findCreate(kClass as KClass<Object>)
        val attrMap = classMap.addAttrMap(attrName)
        attrMap.objectState = PerObjectState.Clean
        attrColMaps.addMapping(attrMap, dbColMap)
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