package javaFXMediators

import javafx.collections.ObservableList
import javafx.scene.control.Control
import tiOPF.Mediator.ListItemMediator
import tiOPF.Mediator.MediatorFieldInfoList
import tiOPF.Object
import tiOPF.ValueReference

class ObservableControlItemMediator<T>(): ListItemMediator(){
    lateinit var items: ObservableList<T>
    var rowIndex: Int = -1
    constructor (listMediator: ObservableListControlMediatorView<Control, Any>, model: Object, items: ObservableList<T>, fieldsInfo: MediatorFieldInfoList, rowIndex: Int, isObserving: Boolean = true): this(){
        this.items = items
        this.rowIndex = rowIndex
        this.model = model
        this.fieldsInfoPrivate = fieldsInfo
        active = isObserving
        this.listMediator = listMediator
    }

    override fun update(subject: Object) {
        if (listMediator == null)
            return
        assert(model == subject)
        var s = ""
        fieldsInfo!!.forEach {
            val fieldName = it.propName
            val value = ValueReference(model!!.getPropValue<Any>(fieldName).toString())
            onBeforeSetupField?.invoke(model!!, fieldName, value)
            if (s.isNotEmpty())
                s += ", "
            s+=value.value
        }
        (listMediator as ObservableListControlMediatorView<Control, Any>).setItemText(items[rowIndex] as Any, s)
    }
}