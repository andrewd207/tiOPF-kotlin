package tiOPF
// complete
import kotlin.reflect.KClass

class ClassDBCollection(var collectionClass: KClass<PerObjectList>, var perObjectAbsClass: KClass<Object>): Object() {
    val ownerAsClassDBCollections: ClassDBCollections get() = owner as ClassDBCollections
    val ownerAttrMaps = AttrColMaps()
    init {
        objectState = PerObjectState.Clean
    }
}