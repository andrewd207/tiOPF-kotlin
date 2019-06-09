package tiOPF

class TouchedByVisitorList: BaseObject() {
    private val list = List<TouchedByVisitor>()
    fun get(index: Int): TouchedByVisitor{
        return list.get(index)
    }
    fun set(index: Int, value: TouchedByVisitor){
        list.set(index, value)
    }
    fun size(): Int{
        return list.size

    }
    fun add(item: TouchedByVisitor){
        list.add(item)
    }
    fun appendTopDown(list: TouchedByVisitorList){
        //TODO
    }

    fun appendBottomUp(list: TouchedByVisitorList){
        //TODO
    }

    fun items(index: Int): List<TouchedByVisitor>{
        return list
    }

    fun getItems(): List<TouchedByVisitor>{
        return list
    }

    init {

    }
}