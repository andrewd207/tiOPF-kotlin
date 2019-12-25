package tiOPF

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


open class BaseObject {
}

interface IObject<T: BaseObject> {
    fun clone(): T
}