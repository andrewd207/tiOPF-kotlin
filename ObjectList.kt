package tiOPF

typealias PerObjectList = ObjectList<*>
//typealias PerObjList = ObjectList<Object>

open class ObjectList<T: BaseObject>: Object(), MutableList<T> {
    override val size: Int
        get() {return items.count()}

    override fun contains(element: T): Boolean { return items.contains(element)  }
    override fun containsAll(elements: Collection<T>): Boolean { return items.containsAll(elements) }
    override fun isEmpty(): Boolean { return items.isEmpty() }
    override fun addAll(elements: Collection<T>): Boolean { return items.addAll(elements) }
    override fun iterator(): MutableIterator<T> { return items.iterator() }
    override fun remove(element: T): Boolean { return items.remove(element) }
    override fun removeAll(elements: Collection<T>): Boolean { return items.removeAll(elements) }
    override fun retainAll(elements: Collection<T>): Boolean { return items.retainAll(elements) }
    override fun add(element: T): Boolean{
        val item = element as BaseObject
        if (autosetItemOwner && element is Object)
            (item as Object).owner = this
        val result = items.add(element)
        if (element is Object)
            notifyObservers(this, NotifyOperation.noAddItem, item as Object, "")

        return result
    }
    override fun add(index: Int, element: T) {
        items.add(index, element)
    }
    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        return items.addAll(index, elements)
    }
    override fun lastIndexOf(element: T): Int {
        return items.lastIndexOf(element)
    }
    override fun listIterator(): MutableListIterator<T> {
        return items.listIterator()
    }
    override fun listIterator(index: Int): MutableListIterator<T> {
        return items.listIterator(index)    }
    override fun removeAt(index: Int): T {
        return items.removeAt(index)
    }
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        val subList = items.subList(fromIndex, toIndex)
        val newList = ObjectList<T>()
        newList.items = subList as List<T>
        return newList
    }
    override fun indexOf(element: T): Int{
        return items.indexOf(element)
    }

    private var items = List<T>()
    var autosetItemOwner: Boolean = true
    var itemOwner: Object? = null
        set(value) {
            field = value
            items.forEach{
                if (it is Object) {
                    val o = it as Object
                    o.owner = value
                }
            }
        }
    var ownsObjects: Boolean = true


    override fun assign(source: Object){
    }

    override fun get(index: Int): T{
        return items[index]
    }
    override fun set(index: Int, element: T): T{
        items[index] = element
        return element
    }

    override fun clear(){
        beginUpdate()
        try {
            items.clear()
            objectState = PerObjectState.Empty
        }
        finally { endUpdate() }
    }

    open fun empty(){
        beginUpdate()
        try {
            items.clear()
        }
        finally {
            endUpdate()
        }
    }


    fun last(): T? {
        if (count() > 0 )
            return items.last()

        return null
    }

    fun find(oidToFind: OID): T?{
        return find { (it as Object).oid.equals(oidToFind) }

    }
    fun find(oidToFindAsString: String): T?{
        val tmpOID = oidGenerator().createOIDInstance()
        tmpOID.asString = oidToFindAsString
        return  find { (it as Object).oid.equals(tmpOID)}
    }
}