package tiOPF.Mediator

import tiOPF.*

open class CustomListMediatorViewCompanion: MediatorViewCompanion(){
    override val compositeMediator: Boolean = true
}
abstract class CustomListMediatorView: MediatorView() {
    companion object: CustomListMediatorViewCompanion()
    @Published var onBeforeSetupField: OnBeforeSetupField? = null
    @Published val model: ObjectList<Object>? get() {return (if (subject is ObjectList<*>) subject as ObjectList<Object> else null)}
    @Published var displayNames: String get() = fieldsInfo.asString(); set(value)  {fieldName = value}
    @Published var showDeleted: Boolean = false ; set(value) {
        if (value == field)
            return
        beginUpdate()
        try {
            field = value
            rebuildList()
        }
        finally {
            endUpdate()
        }
    }
    @Published var fieldsInfo: MediatorFieldInfoList = null as MediatorFieldInfoList; get() {
        if (field == null)
            field = MediatorFieldInfoList(getMediatorFieldInfoConstructor())
        return field
    } set(value) { field.assign(value)}
    override fun close() {
        active = false
        super.close()
    }

    override fun update(subject: Object, operation: NotifyOperation, data: Object?) {
        when (operation){
            NotifyOperation.AddItem -> {
                val m = doCreateItemMediator(data, model!!.count()-1)
                m.listMediator = this
            }
            NotifyOperation.DeleteItem -> itemDeleted(data!!)
            NotifyOperation.Free -> if (subject == this.subject) this.subject = null
            NotifyOperation.Changed,
            NotifyOperation.ReSort -> rebuildList()
        }
    }
    open fun handleSelectionChanged() {}
    open fun itemDeleted(subject: Object){
        val index = ValueOut<Int>()
        val mediator = findObjectMediator(subject, index)
        if (mediator != null)
            doDeleteItemMediator(index.value!!, mediator)
    }
    internal fun callFieldInfoChanged(item: MediatorFieldInfo, action: NotifyOperation){
        fieldInfoChanged(item,action)
    }
    protected open fun fieldInfoChanged(item: MediatorFieldInfo, action: NotifyOperation){
        if (active)
            throwMediatorError(SErrActive)
    }
    protected abstract fun createColumns()
    protected abstract fun clearList()
    protected abstract fun doCreateItemMediator(data: Object?, rowIndex: Int): ListItemMediator
    protected open fun doDeleteItemMediator(index: Int, mediator: ListItemMediator){
        mediatorList.removeAt(index)
    }
    protected fun parseDisplayNames(value: String){
        fieldsInfo.clear()
        for (i in 1 .. tiNumToken(value, CFieldDelimiter)) {
            val field = tiToken(value, CFieldDelimiter, i)
            val info = fieldsInfo.addItem()
            info.asString = field
        }

    }
    protected open fun createSubMediators(){
        createColumns()
        var index = -1
        model?.forEach {
            if (!it.deleted || showDeleted){
                index++
                if (index < mediatorList.count())
                    (mediatorList[index] as ListItemMediator).model = it
                else {
                    val itemMediator = doCreateItemMediator(it, index)
                    itemMediator.listMediator = this

                }
            }
        }
        val remove = mediatorList.subList(index+1, mediatorList.lastIndex)
        mediatorList.removeAll(remove)
    }
    protected abstract fun rebuildList()
    protected fun dataAndPropertyValid(data: Object): Boolean{
        if (subject == null || fieldsInfo.isEmpty())
            return false
        fieldsInfo.forEach {
            if (!isPublishedProp(it::class, it.propName))
                throwMediatorError(SErrInvalidPropertyName.format(it.propName, data.className()))
        }
        return true
    }
    override fun doGUIToObject() {
        // do nothing. List is essentially read only
        //super.doGUIToObject()
    }
    override fun doObjectToGUI() {
        rebuildList()
    }
    override  var subject = super.subject; get() = super.subject; set(value) {
        if (value == field)
            return
        if (value != null){
            if (value !is ObjectList<*>)
                throwMediatorError(SErrNotListObject.format(value::class.simpleName))
            else
                clearList()
        }

        super.subject = value
        }
    override var fieldName: String; get() = super.fieldName; set(value) {
        super.fieldName = value
        parseDisplayNames(value)
    }
    override var active: Boolean; get() = super.active; set(value) {
        super.active = value
        mediatorList.forEach {it as ListItemMediator
            it.active = value
        }
    }
    protected fun findObjectMediator(obj: Object, index: ValueOut<Int>): ListItemMediator?{
        index.value = mediatorList.count()-1
        while (index.value!! >= 0 && (mediatorList[index.value!!] as ListItemMediator).model != obj)
            index.value = index.value!!-1
        if (index.value == -1)
            return null
        return mediatorList[index.value!!] as ListItemMediator
    }
    protected open fun getMediatorFieldInfoConstructor(): NotifiedItemConstructor{
        return ::MediatorFieldInfo as NotifiedItemConstructor
    }
    protected val mediatorList = ObjectList<Object>()






}