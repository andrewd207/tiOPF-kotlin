package tiOPF
// complete
import java.util.*

fun now(): Long{
    return Calendar.getInstance().time.time
}

const val CSecToTimeOutLocked: Int = 999999;

interface IPooledItemClass{
    fun createInstance(owner: Pool): PooledItem{
        return PooledItem(owner)
    }
}

open class PooledItem(var owner: Pool): BaseObject() {
    companion object: IPooledItemClass // subclasses must make their own companion object!!

    var lastUsed = now()
    var locked: Boolean = false
        set(value) {
            if (field && !value)
                lastUsed = now()
            field = value
        }
    var index: Int = -1
    var data: BaseObject? = null
    private val secInUse: Int
        get() {
            return ((now() - lastUsed) * 24 * 60 * 60).toInt()
        }
    open fun mustRemoveItemFromPool(listCount: Int): Boolean{
        return !locked && secToTimeout <= 0 && listCount > owner.minPoolSize
    }
    val secToTimeout: Int
        get() {
            if (locked)
                return CSecToTimeOutLocked
            else
                return owner.timeout * 60 - secInUse
        }


}