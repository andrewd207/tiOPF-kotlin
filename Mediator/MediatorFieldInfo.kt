package tiOPF.Mediator
// complete
import tiOPF.Object
import tiOPF.Published
import tiOPF.tiNumToken
import tiOPF.tiToken

open class MediatorFieldInfo(owner: MediatorFieldInfoList): NotifiedItem(owner) {
    enum class Alignment{
        Left, Right, Center;
        fun alignChar(): Char{
            return when (this){
                Left -> 'l'
                Right -> 'r'
                Center -> 'c'
            }
        }
        fun origAlignChar(): Char{
            return when (this){
                Left -> '<'
                Right -> '>'
                Center -> '|'
            }
        }
        companion object {
            fun fromChar(c: Char): Alignment{
                return when (c.toLowerCase()) {
                    'l', '<' -> Left
                    'r', '>' -> Right
                    'c', '|' -> Center
                    else     -> Left
                }

            }
        }

    }
    @Published var caption = ""
    @Published var propName = ""
    @Published var fieldWidth = 0
    @Published var alignment = Alignment.Left
    open fun assign(source: MediatorFieldInfo){}
    open var asString: String get() {
            return (if (origStyle)
                "%s (%d, \"%s\", %s)".format(propName, fieldWidth, caption, alignment.origAlignChar())
            else
                "%s%c%s%c%d%c%s".format(propName, CMediatorFieldSeparator, alignment.alignChar(), CMediatorFieldSeparator, fieldWidth, CMediatorFieldSeparator, caption))
    }
    set(value) {
        var i = 0
        val p1 = value.indexOf('(')
        val p2 = value.indexOf(CMediatorFieldSeparator)


        origStyle = ( p1 != 0 && (p2 == -1 || p2 > p1))
        if (origStyle){
            propName = value.tiFieldName()
            caption = value.tiFieldCaption()
            fieldWidth = value.tiFieldWidth()
            alignment = value.tiFieldAlignment()

        }
        else {
            propName = value.substringBefore(CMediatorFieldSeparator)
            if (propName.isBlank())
                mediatorError(this, SErrInvalidFieldName, arrayOf(-1)) // TODO( index +1 )
            caption = propName
            alignment = Alignment.Left
            fieldWidth = DefFieldWidth
            if (tiNumToken(value, CMediatorFieldSeparator) > 1){
                // alignment
                var s = tiToken(value, CMediatorFieldSeparator, 2)
                if (s.isNotBlank()) {
                    if (s.length != 1)
                        mediatorError(this, SErrInvalidAlignmentChar, arrayOf(s, -1) ) // TODO ( index + 1 )
                    alignment = Alignment.fromChar(s[0])
                }

                // field width
                if (tiNumToken(value, CMediatorFieldSeparator) > 2) {
                    s = tiToken(value, CMediatorFieldSeparator, 3)
                    if (s.isNotBlank()){
                        fieldWidth = ( if (s.toIntOrNull() == null){
                            mediatorError(this, SErrInvalidWidthSpecifier, arrayOf(s)) // throws an exception
                            DefFieldWidth // never reached
                        }
                        else s.toInt())
                    }

                    // caption
                    s = tiToken(value, CMediatorFieldSeparator, 4)
                    if (s.isNotBlank()){
                        caption = s
                    }
                }
            }
        }
    }
    val index: Int = owner.indexOf(this)
    private var origStyle = false
}

typealias NotifiedItemConstructor = (NotifiedArrayList<*>) -> NotifiedItem

open class NotifiedItem(val owner: NotifiedArrayList<*>)

open class NotifiedArrayList<T>(private val itemConstructor: NotifiedItemConstructor): ArrayList<T>(){
    enum class Notification {Added, Extracting, Deleting}
    protected open fun notify(item: T, action: Object.NotifyOperation){}
    override fun addAll(elements: Collection<T>): Boolean {
        val result = super.addAll(elements)
        elements.forEach { notify(it, Object.NotifyOperation.AddItem) }
        return result
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val result = super.addAll(index, elements)
        elements.forEach { notify(it, Object.NotifyOperation.AddItem) }
        return result

    }

    override fun clear() {
        forEach { notify(it, Object.NotifyOperation.DeleteItem) }
        super.clear()
    }

    override fun add(element: T): Boolean {
        val result = super.add(element)
        notify(element, Object.NotifyOperation.AddItem)
        return result
    }

    override fun add(index: Int, element: T) {
        val result = super.add(index, element)
        notify(element, Object.NotifyOperation.AddItem)
        return result
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        forEach { notify(it, Object.NotifyOperation.DeleteItem) }
        super.removeRange(fromIndex, toIndex)
    }

    override fun removeAt(index: Int): T {
        notify(get(index), Object.NotifyOperation.DeleteItem)
        return super.removeAt(index)
    }

    override fun remove(element: T): Boolean {
        notify(element, Object.NotifyOperation.DeleteItem)
        return super.remove(element)
    }
}

class MediatorFieldInfoList(itemConstructor: NotifiedItemConstructor): NotifiedArrayList<MediatorFieldInfo>(itemConstructor){
    fun fieldInfoByName(caption: String) = fieldInfoByCaption(caption)
    fun fieldInfoByCaption(caption: String): MediatorFieldInfo?{
        forEach {
            if (it.caption == caption)
                return it
        }
        return null
    }
    fun fieldInfoByPropName(name: String): MediatorFieldInfo?{
        forEach {
            if (it.propName == name)
                return it
        }
        return null
    }
    fun addItem(): MediatorFieldInfo{
        val i = MediatorFieldInfo(this)
        add(i)
        return i
    }

    fun addFieldInfo(propName: String, fieldWidth: Int): MediatorFieldInfo{
        val item = addItem()
        item.propName = propName
        item.fieldWidth = fieldWidth
        return item
    }

    fun addFieldInfo(propName: String, caption: String, fieldWidth: Int, alignment: MediatorFieldInfo.Alignment? = null): MediatorFieldInfo{
        val item = addItem()
        item.propName = propName
        item.caption = caption
        item.fieldWidth = fieldWidth
        if (alignment != null)
            item.alignment = alignment
        return item
    }

    open fun assign(list: MediatorFieldInfoList){
        clear()
        addAll(list)
        mediator = list.mediator // ?
    }

    fun asString(): String{
        var result = ""
        forEach {
            if (result != "")
                result+=';'
            result+=it.asString
        }
        return result
    }

    override fun notify(item: MediatorFieldInfo, action: Object.NotifyOperation) {
        super.notify(item, action)
        mediator?.callFieldInfoChanged(item, action)
    }

    internal var mediator: CustomListMediatorView<*>? = null


}
