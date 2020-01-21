package tiOPF

import kotlin.reflect.KMutableProperty

// complete
class AttrMap: Object() {
    var attrName: String = ""
    override var caption: String
        get() = attrName
        set(value) { attrName = value}
    val ownerAsClassMap: ClassMap get () = owner as ClassMap
    lateinit var property: KMutableProperty<*>
}