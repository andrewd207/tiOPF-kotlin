package tiOPF.Mapper

import tiOPF.BaseObject
import tiOPF.FilteredObjectList
import java.lang.Exception
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor


abstract class MappedFilteredObjectList<T:BaseObject>: FilteredObjectList<T>() {
    var enumType = EnumType.Int
    var sql = ""
    abstract var objectClass: KClass<T>
    fun createItemInstance(): T{
        var newClass = objectClass
        if (newClass != null)
          return newClass.primaryConstructor!!.call()
        throw Exception("objectClass is null")
    }
    val params = SelectParamList()
    fun addParam(name: String, sqlParamName: String, paramType: MapPropType, value: Any){
        if (params.findByName(name) != null)
            return

        val item = SelectParam(name, paramType, sqlParamName, value)
        params.add(item)
    }
}