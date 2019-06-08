package tiOPF

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


fun testValid(obj: BaseObject?, classType: KClass<*>? = null, allowNull: Boolean = false):Boolean{
    if (obj == null)
        return false
    if (allowNull && classType == null)
        return true
    if (!allowNull && classType == null)
        return false

    return obj.testValid(classType, allowNull)
}
open class BaseObject {

    fun testValid(classType: KClass<*>? = null, allowNull: Boolean = false): Boolean{
        var result = allowNull && (classType != null)
        if (classType != null)
            result = result || this::class.isSubclassOf(classType)
        return result;

    }
    fun testValid(allowNull: Boolean): Boolean{
        return testValid(BaseObject::class, allowNull)
    }


}

interface IObject<T: BaseObject> {
    fun clone(): T
}