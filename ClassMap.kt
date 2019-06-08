package tiOPF

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class ClassMap: ObjectList<AttrMap>() {
    private fun isPublicProp(propName: String): Boolean{
        return propName.toLowerCase() in arrayOf("oid", "owner.oid")

    }
    private fun isPublishedProp(propName: String): Boolean{
        return isPublicProp(propName) || isPublishedProp(perObjAbsClass!!::class, propName)
    }
    val ownerAsClassDBMappingManager: ClassDBMappingManager get() = owner as ClassDBMappingManager
    override val caption: String get() { return perObjAbsClass!!.qualifiedName!! }

    var perObjAbsClass: KClass<Object>? = null
    var parentClassMap: ClassMap? = null

    fun addAttrMap(attrName: String): AttrMap{
        var result = find { it.attrName === attrName }
        if (result != null)
            throw Exception("Attempt to register duplicate AttrMap\n" +
                    "ClassName: " + perObjAbsClass!!.qualifiedName+"\n" +
                    "AttrName:  " + attrName +"+\n" +
                    ". Called in " + className() + ".addAttrMap")
        if (!isPublishedProp(attrName))
            throw Exception(attrName+" is not a published property on ${perObjAbsClass!!.qualifiedName}\n" +
                    "called in ${className()}.addAttrMap")
        result = AttrMap()
        result.attrName = attrName
        result.objectState = PerObjectState.Clean
        add(result)
        return result

    }
}

class ClassMaps: ObjectList<ClassMap>(){
    val ownerAsClassDBMappingManager: ClassDBMappingManager get() = owner as ClassDBMappingManager
    fun addClassMap(kClass: KClass<Object>): ClassMap{
        var result = find { it.perObjAbsClass == kClass }
        if (result != null)
            throw Exception("Attempt to register duplicate TtiClassMap\n" +
                    "Classname: " + kClass.qualifiedName + "\n" +
                    "Called in " + className() + ".AddClassMap")
        result = ClassMap()
        result.perObjAbsClass = kClass
        result.objectState = PerObjectState.Clean
        add(result)
        return result
    }
    fun findCreate(kClass: KClass<Object>): ClassMap{
        var result = find { it.perObjAbsClass == kClass }
        if (result == null)
            result = addClassMap(kClass)
        return result
    }

    fun isClassRegistered(kClass: KClass<Object>): Boolean{
        return find { it.perObjAbsClass == kClass } != null
    }

    fun registerInheritance(parentClass: KClass<Object>, childClass: KClass<Object>){
        if (!childClass.isSubclassOf(parentClass))
            throw Exception("Attempt to register inheritance on a child class <${childClass.qualifiedName}> that does not inherit from <${parentClass.qualifiedName}>")
        val classMapChild = find { it.perObjAbsClass == childClass }
        classMapChild?: throw Exception("Attempt to register inheritance on a child class <${childClass.qualifiedName}> that has not yet been registered")
        val classMapParent = find { it.perObjAbsClass == parentClass }
        classMapParent?:throw Exception("Attempt to register inheritance on a parent class <${childClass.qualifiedName}> that has not yet been registered")

        classMapChild.parentClassMap = classMapParent
    }
}