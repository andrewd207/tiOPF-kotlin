package tiOPF

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.isAccessible

const val CErrorSettingProperty      = "Error setting property %s.%s Message %s"
const val CErrorGettingProperty      = "Error getting property %s.%s Message %s"
const val CErrorInvalidTypeKind      = "Invalid TtiTypeKind"
const val CErrorUnknownType          = "Unknown type"
const val CErrorCallingReadWriteProp = "Error calling tiIsReadWriteProp with class: %s and property %s"
const val CErrorUnhandledPropType    = "Invalid or unhandled property type passed to tiGetSimplePropType. ClassName <%s> Property name <%s> Property type <%s>"
const val CErrorIsNumericProp        = "Error in tiIsNumericProp. Message: %s"
const val CErrorSimpleTypeKind       = "Error in tiGetSimpleTypeKind. Property name: %s  Message: %s"
const val CErrorSettingPropValue     = "Error setting property value for <%s> on <%s> Message <%s>"

fun getPropertyNames(instance: Object, list: List<String>, propFilter: Array<TypeKind>){
    instance::class.memberProperties.forEach {
        val kClass = it.returnType.classifier as KClass<*>
        if (classToTypeKind(kClass) in propFilter)
            list.add(it.name)
    }
}

fun getPropertyClass(klass: KClass<*>, propName: String): KClass<*> {

    val property = klass.members.find { it.name === propName }
    if (property != null)
        return property::class

    return Any::class
}

fun getPropertyType(instance: Any, propName: String): TypeKind{
    val klass = getPropertyClass(instance::class, propName)
    return classToTypeKind(klass)
}

fun isReadWriteProp(instance: Any, propName: String): Boolean{
    val prop = instance::class.declaredMemberProperties.find { it.name == propName } ?: return false
    return prop is KMutableProperty<*>
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

fun isPublishedProp(instance: Any, propName: String): Boolean{
    val property = instance::class.declaredMemberProperties.find { it.name === propName }
    if (property != null)
        return property.isAccessible
    return false
}

fun <T>Object.newInstance(vararg args: Any?): T{
    return this::class.primaryConstructor!!.call(args) as T
}

fun BaseObject.className(): String{
    return this::class.qualifiedName!!
}