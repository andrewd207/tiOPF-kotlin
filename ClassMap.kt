package tiOPF

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class ClassMap: ObjectList<AttrMap>() {
    private fun isPublicProp(propName: String): Boolean{
        return propName.toLowerCase() in arrayOf("oid", "owner.oid")

    }
    val ownerAsClassDBMappingManager: ClassDBMappingManager get() = owner as ClassDBMappingManager
    private fun isPublishedProp(propName: String): Boolean{
        return isPublicProp(propName) || isPublishedProp(perObjAbsClass!!, propName)
    }
    override val caption: String get() { return perObjAbsClass!!.qualifiedName!! }

    var perObjAbsClass: KClass<*>? = null
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
    protected fun findByPerObjAbsClass(kClass: KClass<*>): ClassMap?{
        return find { it. perObjAbsClass == kClass }
    }
    fun addClassMap(kClass: KClass<*>): ClassMap{
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
    fun findCreate(kClass: KClass<*>): ClassMap{
        var result = find { it.perObjAbsClass == kClass }
        if (result == null)
            result = addClassMap(kClass)
        return result
    }
    fun findParent(kClass: KClass<*>): ClassMap?{
        val classMap = findByPerObjAbsClass(kClass)
        assert(classMap != null, {"Attempt to find parent on unregistered class <${kClass.qualifiedName}>"})
        return classMap
    }
    fun findAllParents(kClass: KClass<*>, list: ClassMaps){
        assert(!list.ownsObjects, {"list.ownsObjects is true and it should be false"});
        list.clear()
        var classMap: ClassMap? = findByPerObjAbsClass(kClass)
        assert(classMap != null, {"Request to find parent on class that is not registered<${kClass.qualifiedName}>"})
        list.add(0, classMap!!)
        while (classMap != null && classMap.parentClassMap != null){
            classMap = classMap.parentClassMap
            list.add(0, classMap!!)
        }
    }
    fun hasParent(kClass: KClass<*>): Boolean{
        return findParent(kClass) != null
    }

    fun isClassRegistered(kClass: KClass<*>): Boolean{
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