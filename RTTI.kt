package tiOPF

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.reflect

const val CErrorSettingProperty      = "Error setting property %s.%s Message %s"
const val CErrorGettingProperty      = "Error getting property %s.%s Message %s"
const val CErrorInvalidTypeKind      = "Invalid TtiTypeKind"
const val CErrorUnknownType          = "Unknown type"
const val CErrorCallingReadWriteProp = "Error calling tiIsReadWriteProp with class: %s and property %s"
const val CErrorUnhandledPropType    = "Invalid or unhandled property type passed to tiGetSimplePropType. ClassName <%s> Property name <%s> Property type <%s>"
const val CErrorIsNumericProp        = "Error in tiIsNumericProp. Message: %s"
const val CErrorSimpleTypeKind       = "Error in tiGetSimpleTypeKind. Property name: %s  Message: %s"
const val CErrorSettingPropValue     = "Error setting property value for <%s> on <%s> Message <%s>"

fun getPropertyNames(instance: BaseObject, list: List<String>, propFilter: Set<TypeKind>){
    instance::class.memberProperties.forEach {
        val kClass = it.returnType.classifier as KClass<*>
        if (classToTypeKind(kClass) in propFilter && it.visibility == KVisibility.PUBLIC)
            list.add(it.name)
    }
}

fun getPublishedPropertyNames(instance: BaseObject, list: MutableList<String>, fieldNameList: MutableList<String>? = null) =
    getPublishedPropertyNames(instance::class, list, fieldNameList)

fun getPublishedPropertyNames(kKlass: KClass<*>, list: MutableList<String>, fieldNameList: MutableList<String>? = null){
    kKlass.memberProperties.forEach {
        val annotation = it.findAnnotation<Published>()
        if (annotation != null) {
            list.add(it.name)
            fieldNameList?.add(annotation.persistenceHint)
        }
    }
}

fun getPropertyClass(klass: KClass<*>, propName: String): KClass<*> {

    val property = klass.memberProperties.find { it.name == propName }
    if (property != null)
        return property.getter.returnType.classifier as KClass<*>

    return Any::class
}

fun getPropertyType(instance: Any, propName: String): TypeKind{
    val klass = getPropertyClass(instance::class, propName)
    return classToTypeKind(klass)
}

fun isReadWriteProp(instance: Any, propName: String): Boolean{
    val prop = instance::class.memberProperties.find { it.name == propName } ?: return false
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
    val property = instance::class.memberProperties.find{it.name == propName}
    if (property != null && /*property.isAccessible &&*/ property.visibility == KVisibility.PUBLIC)
        return property.call(instance) as T

    return null
}

fun <T>setObjectProperty(instance: Any, propName: String, value: T){
    val property = instance::class.memberProperties.find{ it.name == propName }
    if (property != null && /*property.isAccessible &&*/ property.visibility == KVisibility.PUBLIC && property is KMutableProperty<*>)
        property.setter.call(instance, value)
}
fun isPublishedProp(kClass: KClass<*>, propName: String): Boolean{
    //kClass.memberProperties.forEach { println("popr: ${it.name}") }
    val property = kClass.memberProperties.find { it.name.equals(propName) }
    property?: return false

    return property.findAnnotation<Published>() != null
    //return property.visibility == KVisibility.PUBLIC

}

fun isPublicProp(instance: Any, propName: String): Boolean{
    val property = instance::class.memberProperties.find { it.name === propName }
    if (property != null)
        return property.visibility == KVisibility.PUBLIC
    return false
}

fun <T>Object.newInstance(vararg args: Any?): T{
    return this::class.primaryConstructor!!.call(args) as T
}

fun BaseObject.className(): String{
    return this::class.qualifiedName!!
}

/*fun KClass<Object>.createInstance(): Object{
    return this.primaryConstructor!!.call()
}*/
