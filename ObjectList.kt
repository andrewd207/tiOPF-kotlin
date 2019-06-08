package tiOPF

open class ObjectList<T>: Object(), MutableCollection<T> {
    override val size: Int
        get() {return count()}

    override fun contains(element: T): Boolean { return items.contains(element)  }
    override fun containsAll(elements: Collection<T>): Boolean { return items.containsAll(elements) }
    override fun isEmpty(): Boolean { return items.isEmpty() }
    override fun addAll(elements: Collection<T>): Boolean { return items.addAll(elements) }
    override fun iterator(): MutableIterator<T> { return items.iterator() }
    override fun remove(element: T): Boolean { return items.remove(element) }
    override fun removeAll(elements: Collection<T>): Boolean { return items.removeAll(elements) }
    override fun retainAll(elements: Collection<T>): Boolean { return items.retainAll(elements) }
    override fun add(element: T): Boolean{
        val item = element as Object
        if (autosetItemOwner)
            item.owner = this
        val result = items.add(element)
        notifyObservers(this, NotifyOperation.noAddItem, item, "")
        return result
    }

    val items = List<T>()
    var autosetItemOwner: Boolean = true
    var itemOwner: Object? = null
        set(value) {
            field = value
            items.forEach{
                val o = it as Object
                o.owner = value
            }
        }



    override fun assign(source: Object){
    }

    fun get(index: Int): T{
        return items[index]
    }
    fun set(index: Int, value: T){
        items[index] = value
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
    fun indexOf(element: T): Int{
        return items.indexOf(element)
    }

    fun last(): T? {
        if (count() > 0 )
            return items.last()

        return null
    }
}