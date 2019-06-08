package tiOPF

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.isAccessible

fun tiLineEnd(count: Int = 1): String{
    return tiReplicate(CLineEnding.toString(), count)
}

fun tiReplicate(value: String, replicateCount: Int): String{
    var result = value
    for (i in 1..replicateCount ){
        result += value
    }
    return result
}

fun tiAddTrailingValue(line: String, value: String, duplicates: Boolean = true): String{
    if (line.isEmpty())
        return line

    if (duplicates)
        return line + value

    val start = line.length - value.length + 1
    val subString = line.substring( start, start + value.length)

    if (value === subString)
        return line + value

    return line
}



fun tiAddEllipsis(string: String, width: Int = 20): String{
    val len = string.length
    when {
        len < width -> return string
        len > width -> return string.substring(0, width-4) + "..."
        else -> return string.substring(0, len-4) + "..."
    }
}

fun getPropertyNames(instance: Object, list: List<String>, propFilter: Array<TypeKind>){
    instance::class.memberProperties.forEach {
        val kClass = it.returnType.classifier as KClass<*>
        if (classToTypeKind(kClass) in propFilter)
            list.add(it.name)
    }
}

fun getPropertyClass(klass: KClass<*>, propName: String): KClass<*>{

    val property = klass.members.find { it.name === propName }
        if (property != null)
            return property::class

    return Any::class
}

fun getPropertyInheritsFrom(instance: Any, propName: String, klass: KClass<*>): Boolean {

    return getPropertyInheritsFrom(instance::class, propName, klass)
}

fun getPropertyInheritsFrom(typeClass: KClass<*>, propName: String, propKlass: KClass<*>): Boolean{
    val theProp = getPropertyClass(typeClass, propName)
    return theProp == propKlass || theProp.superclasses.contains(propKlass) && theProp != Any::class

}

fun <T: Any>getObjectProperty(instance: Any, propName: String): T?{
    val property = instance::class.declaredMemberProperties.find{it.name === propName}
        if (property != null && property.isAccessible && property.visibility == KVisibility.PUBLIC)
            return property.getter.call() as T

    return null
}

fun <T>setObjectProperty(instance: Any, propName: String, value: T){
    val property = instance::class.declaredMemberProperties.find { it.name === propName }
        if (property is KMutableProperty<*>)
            property.setter.call(value)
}

fun <T>Object.newInstance(vararg args: Any?): T{
    return this::class.primaryConstructor!!.call(args) as T
}

fun BaseObject.className(): String{
    return this::class.qualifiedName!!
}