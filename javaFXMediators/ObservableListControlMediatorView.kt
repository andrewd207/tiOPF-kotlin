package javaFXMediators

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.scene.control.Control
import javafx.scene.control.SelectionModel
import tiOPF.Mediator.CustomListMediatorView
import tiOPF.Mediator.ListItemMediator
import tiOPF.Object
import tiOPF.getObjectProperty

abstract class ObservableListControlMediatorView<T: Control, R>: ChangeListener<R>, CustomListMediatorView<T>(){
    protected abstract fun listItems(): ObservableList<R>
    protected abstract fun selectionModel(): SelectionModel<R>?
    protected abstract fun addNewItem(itemMediator: ObservableControlItemMediator<R>, parent: Any? = null): R
    internal abstract fun setItemText(item: R, text: String)
    protected abstract fun getItemMediator(item: R): ObservableControlItemMediator<R>

    override fun doCreateItemMediator(data: Object?, rowIndex: Int): ListItemMediator {

        val text: String
        text = if (fieldsInfo!!.isNotEmpty()){
            val fieldName = fieldsInfo!![0].propName
            getObjectProperty(data!!, fieldName)!!
        } else
            data!!.caption

        val result =  ObservableControlItemMediator(this as ObservableListControlMediatorView<Control, Any>, data, listItems(), fieldsInfo!!, rowIndex, active)
        setItemText(addNewItem(result, null), text)

        return result
    }

    override fun doDeleteItemMediator(index: Int, mediator: ListItemMediator) {
        //println("deleting ${listItems()[index]}")
        listItems().removeAt(index)

        super.doDeleteItemMediator(index, mediator)
    }

    fun getObjectFromRow(row: Int): Object?{
        if (listItems().isEmpty() || row == -1)
            return null

        val item = listItems()[row]

        if (item != null)
            return getItemMediator(item).model

        return null
    }

    override var selectedObject: Object?
        get() {
            return (if (selectionModel() != null ) getObjectFromRow(selectionModel()!!.selectedIndex) else null)
        }
        set(value) {
            listItems().forEach {
                val selectModel = selectionModel()
                val mediator = getItemMediator(it)
                if (selectModel != null && mediator != null && mediator.model == value) {
                    selectModel.select(it)
                    return
                }
            }
        }

    override fun createColumns(){  } // nothing
    override fun clearList() {
        mediatorList?.clear()
        listItems().clear()
    }

    override fun rebuildList() {
        setupGUIandObject()
        mediatorList?.clear()
        createSubMediators()
    }

    override fun setupGUIandObject() {
        super.setupGUIandObject()
        listItems().clear()
        selectionModel()?.selectedItemProperty()?.removeListener(this)
        selectionModel()?.selectedItemProperty()?.addListener(this)
    }


    override fun changed(
        observable: ObservableValue<out R>?,
        oldValue: R?,
        newValue: R?
    ) {
        if (newValue != null) {
            val mediator = getItemMediator(newValue)
            selectedObject = mediator.model!!
        }
    }
}