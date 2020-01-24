package tiOPF.automap
// complete
import tiOPF.Object
import tiOPF.PerObjectList
import kotlin.reflect.KClass

class ClassDBCollection(var collectionClass: KClass<PerObjectList>, var perObjectAbsClass: KClass<*>): Object() {
    val ownerAsClassDBCollections: ClassDBCollections get() = owner as ClassDBCollections
    val ownerAttrMaps = AttrColMaps()
    init {
        objectState = PerObjectState.Clean
    }
}