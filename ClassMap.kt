package tiOPF

import kotlin.reflect.KClass

class ClassMap: ObjectList<AttrMap>() {
    val ownerAsClassDBMappingManager: ClassDBMappingManager get() = owner as ClassDBMappingManager
    var perObjAbsClass: KClass<Object>? = null
}