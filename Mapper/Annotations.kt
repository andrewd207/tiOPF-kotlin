package tiOPF.Mapper

import kotlin.reflect.KClass

annotation class Item(val name: String)
annotation class ItemClass(val kClass: KClass<*>)
annotation class NoParent
annotation class CDATA
annotation class Comment(val value: String)