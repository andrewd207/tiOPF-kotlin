package tiOPF

enum class Duplicate {
    Ignore,
    Accept,
    Error
}

class List<E>: MutableList<E> {


    var onChange: NotifyCallback<List<E>>? = null
    var onChanging: NotifyCallback<List<E>>? = null
    private var list: MutableList<E> = mutableListOf()
    override val size: Int get() = list.size
    override fun add(element: E): Boolean {
        onChanging?.invoke(this)
        val result = list.add(element)
        onChange?.invoke(this)
        return result
    }
    override fun add(index: Int, element: E) {
        onChanging?.invoke(this)
        val result = list.add(index, element)
        onChange?.invoke(this)
        return result
    }
    override fun addAll(elements: Collection<E>): Boolean {
        onChanging?.invoke(this)
        val result = list.addAll(elements)
        onChange?.invoke(this)
        return result
    }

    override fun clear() {
        onChanging?.invoke(this)
        list.clear()
        onChange?.invoke(this)
    }
    override fun contains(element: E): Boolean { return  list.contains(element) }
    override fun get(index: Int): E { return list.get(index) }
    override fun indexOf(element: E): Int { return list.indexOf(element) }
    override fun iterator(): MutableIterator<E> { return list.iterator() }
    override fun containsAll(elements: Collection<E>): Boolean { return list.containsAll(elements) }
    override fun isEmpty(): Boolean { return list.isEmpty() }
    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        onChanging?.invoke(this)
        val result = list.addAll(index, elements)
        onChange?.invoke(this)
        return result
    }
    override fun lastIndexOf(element: E): Int { return list.lastIndexOf(element) }
    override fun listIterator(): MutableListIterator<E> { return list.listIterator() }
    override fun listIterator(index: Int): MutableListIterator<E> { return list.listIterator(index) }
    override fun remove(element: E): Boolean {
        if (!contains(element))
            return false
        onChanging?.invoke(this)
        val result = list.remove(element)
        onChange?.invoke(this)
        return result
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        onChanging?.invoke(this)
        val result = list.removeAll(elements)
        onChange?.invoke(this)
        return result
    }

    override fun removeAt(index: Int): E {
        onChanging?.invoke(this)
        val result = list.removeAt(index)
        onChange?.invoke(this)
        return result
    }

    override fun retainAll(elements: Collection<E>): Boolean { return list.retainAll(elements) }
    override fun set(index: Int, element: E): E {
        onChanging?.invoke(this)
        val result = list.set(index, element)
        onChange?.invoke(this)
        return result
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        val result = List<E>()
        result.list = list.subList(fromIndex, toIndex)

        return result
    }

}