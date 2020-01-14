package tiOPF
// complete
import kotlin.reflect.KClass

class AttrColMaps: ObjectList<AttrColMap>() {
    val ownerAsClassDBMappingManager: ClassDBMappingManager get() = owner as ClassDBMappingManager
    fun addMapping(attrMap: AttrMap, colMap: DBColMap){
        val map = AttrColMap(attrMap, colMap)
        map.objectState = PerObjectState.Clean
        add(map)
    }
    fun findAllMappingsByMapToClass(klass: KClass<*>, list: AttrColMaps){
        list.clear()
        forEach{
            if (it.attrMap.ownerAsClassMap.perObjAbsClass == klass)
                list.add(it)
        }
    }
    fun findAllPKMappingsByMapToClass(klass: KClass<*>, list: AttrColMaps){
        list.clear()
        forEach {
            if (it.attrMap.ownerAsClassMap.perObjAbsClass == klass && it.dbColMap.pkInfo.contains(ClassDBMapRelationshipType.Primary))
                list.add(it)
        }
    }
    fun findByClassAttrMap(klass: KClass<*>, attrName: String): AttrColMap?{
        forEach {
            if (it.attrMap.ownerAsClassMap.perObjAbsClass == klass && it.attrMap.attrName.equals(attrName, true))
                return it
        }
        return null
    }
    fun tableName(): String{
        var result = ""
        forEach {
            if ( result.isEmpty() )
                result = it.dbColMap.ownerAsDBTableMap.tableName

            //each item must share the same tableName!
            if (result != it.dbColMap.ownerAsDBTableMap.tableName)
                throw EtiOPFProgrammerException(CErrorInconsistentTableNames)

        }
        return result
    }
}