package tiOPF.automap
//complete
import tiOPF.ObjectList
import tiOPF.PerObjectList
import tiOPF.List
import kotlin.reflect.KClass

class ClassDBCollections: ObjectList<ClassDBCollection>() {
    val ownerAsClassDBMappingManager: ClassDBMappingManager get() = owner as ClassDBMappingManager
    fun addClassCollectionMapping(collectionClass: KClass<PerObjectList>, kClass: KClass<*>): ClassDBCollection{
        val result = ClassDBCollection(collectionClass, kClass)
        add(result)
        return result
    }
    fun findByCollectionOf(kClass: KClass<*>): ClassDBCollection?{
        return find { it.perObjectAbsClass == kClass }
    }
    fun findByCollection(kClass: KClass<*>, list: List<ClassDBCollection>){
        forEach{
            if (it.collectionClass == kClass)
                list.add(it)
        }
    }
    fun isCollection(kClass: KClass<*>): Boolean{
        val list = List<ClassDBCollection>()
        findByCollection(kClass, list)

        return list.isNotEmpty()
    }

    fun isInCollection(kClass: KClass<*>): Boolean{
        return findByCollectionOf(kClass) != null
    }

}