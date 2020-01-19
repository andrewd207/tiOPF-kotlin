package javaFXMediators

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import tiOPF.*
import tiOPF.Mediator.MediatorFieldInfo
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.isSubclassOf

class TableViewListMediatorView: ObservableListControlMediatorView<CustomFXTableView, MediatedItem>(){
    open class CustomListItemCell(val propName: String): TableCell<MediatedItem, MediatedItem>(){
        fun <T: Any>getFieldValue(item: MediatedItem?): T?{
            if (item != null) {
                return getObjectProperty(item.itemMediator.model!!, propName)
            }
            return null
        }
        fun getFieldClass(item: MediatedItem?): KClass<*>?{
            if (item != null) {
                val instance = item.itemMediator.model!!
                val prop = getPropFromPath(instance::class, propName, ValueReference(instance))
                return if (prop != null) {
                    prop.getter.returnType.classifier as KClass<*>
                } else
                    null
            }
            return null

        }
        protected val mediatedItem: MediatedItem? get() {
            return if (tableView != null && tableView.editingCell != null)
                tableView.items[tableView.editingCell.row]
            else
                null
        }
        override fun updateItem(item: MediatedItem?, empty: Boolean) {
            val lItem = item?: mediatedItem
            text = if (lItem != null) {
                getFieldValue<Any>(lItem).toString()
            } else
                ""
        }

        var escapePressed = false
        // never null but allocated when needed
        private var privTextField: TextField? = null; get() {
            if (field == null) {
                field = TextField()
                field?.addEventFilter(KeyEvent.KEY_PRESSED, EventHandler { event ->
                    when (event.code) {

                        KeyCode.ESCAPE -> {
                            escapePressed = true
                            field?.text = item.text
                            cancelEdit()
                            event.consume()
                            tableView.requestFocus()
                        }
                        /*KeyCode.RIGHT,
                        KeyCode.TAB -> {
                            tableView.selectionModel.selectNext()
                            event.consume()
                        }
                        KeyCode.LEFT -> {
                            tableView.selectionModel.selectPrevious()
                            event.consume()
                        }*/
                        KeyCode.UP -> {
                            tableView.selectionModel.selectAboveCell()
                            event.consume()
                        }
                        KeyCode.DOWN -> {
                            tableView.selectionModel.selectBelowCell()
                            event.consume()
                        }
                        KeyCode.ENTER ->{
                            commitEdit(mediatedItem)
                            event.consume()
                        }
                    }
                })

                field?.focusedProperty()?.addListener(ChangeListener { observable, hadFocus, hasFocus ->
                    if (!hasFocus && !escapePressed) {
                        commitEdit(mediatedItem)
                    }
                })

                field?.onAction = EventHandler { _ ->
                    commitEdit(mediatedItem)
                }

                field?.onKeyPressed = EventHandler {
                    escapePressed = it.code == KeyCode.ESCAPE
                }
            }
            return field
        }
        val textField: TextField get() { return privTextField!!}

        var tablePosition: TablePosition<MediatedItem, *>? = null

        override fun startEdit() {
            if (!isEditable || !tableView.isEditable || !tableColumn.isEditable)
                return
            super.startEdit()

            item = mediatedItem

            if (true) {
                escapePressed = false
                textField.text = getFieldValue<Any>(mediatedItem).toString()

                text = null
                graphic = textField



                textField.selectAll()
                textField.requestFocus()
                tablePosition = tableView.editingCell
            }
            //println("startEdit customListItemCell, isEditing: ${isEditing}  textfield: $textField tp: $tablePosition item: $item")
        }

        override fun commitEdit(newValue: MediatedItem?) {
            //println("commit ${textField.text}")
            text = textField.text
            val model = item.itemMediator.model
            if (model != null) {
                when (val kClass = getFieldClass(newValue)) {
                    String::class -> model.setPropValue(propName, text)
                    Int::class -> model.setPropValue(propName, text.toInt())
                    Double::class -> model.setPropValue(propName, text.toDouble())
                    Float::class -> model.setPropValue(propName, text.toFloat())
                    Long::class -> model.setPropValue(propName, text.toLong())
                    Boolean::class -> model.setPropValue(propName, text.toBoolean())

                    null -> {}//throw Exception("field class is null")
                    else -> {
                        if (kClass.isSubclassOf(Enum::class)) {
                            val valuesCallable = kClass.declaredMembers.find { it.name == "values"}
                            if (valuesCallable != null) {
                                val values = valuesCallable.call() as Array<Enum<*>>
                                values.forEach {
                                    if (it.name.equals(text, true)) {
                                        model.setPropValue(propName, it)
                                        return@forEach
                                    }
                                }
                            }
                        }
                    }
                }
            }


            //item.itemMediator.model?.setPropValue(propName, textField.text)

            escapePressed = true

            super.commitEdit(newValue)

            //tableView?.edit(-1, null)
            graphic = null

            privTextField = null

        }

        override fun cancelEdit() {

            if (escapePressed) {
                super.cancelEdit()
                updateItem(item, false)
                //text = mediatedItem?.itemMediator?.model?.getPropValue(propName)
            }
            else {
                commitEdit(item)
            }
            graphic = null
            privTextField = null
        }
    }

    class CustomBooleanCell(propName: String): CustomListItemCell(propName){
        val checkBox = CheckBox()

        init {
            //checkBox.isDisable = true
            checkBox.selectedProperty().addListener(ChangeListener<Boolean>{ observable, oldValue, newValue ->
                val value:Boolean = newValue ?: false
                if (isEditing) {
                    val fieldsInfo = item.itemMediator.fieldsInfo!!
                    val field = fieldsInfo.fieldInfoByCaption(tableColumn.text)
                    if (field != null){
                        setObjectProperty(item.itemMediator.model!!, field.propName, value)

                    }
                    commitEdit(item)
                }

            })
            checkBox.onAction = EventHandler { event ->
                commitEdit(item)
            }
            graphic = checkBox
            contentDisplay = ContentDisplay.GRAPHIC_ONLY
            isEditable = true
        }

        override fun startEdit() {
            //super.startEdit()
            item = mediatedItem
            //println("checkbox start $item")
            if (isEmpty)
                return
            checkBox.isSelected = !checkBox.isSelected
            checkBox.isDisable = false
            checkBox.requestFocus()
        }

        override fun cancelEdit() {
            super.cancelEdit()
            checkBox.isDisable = true
        }

        override fun commitEdit(newValue: MediatedItem?) {
            newValue?.itemMediator?.model?.setPropValue(propName, checkBox.isSelected)
        }

        override fun updateItem(item: MediatedItem?, empty: Boolean) {
            if (item != null) {
                checkBox.isVisible = true
                checkBox.isSelected = getFieldValue(item) ?: false
            }
            else {
                checkBox.isSelected = false
                checkBox.isVisible = false
            }
        }




    }

    override fun listItems(): ObservableList<MediatedItem> {
        return view!!.items
    }

    override fun selectionModel(): SelectionModel<MediatedItem> {
        return view!!.selectionModel
    }



    private fun createColumn(fieldInfo: MediatorFieldInfo): CustomFXColumn{
        val column = CustomFXColumn(fieldInfo.caption)
        column.isEditable = true
        column.setCellValueFactory { SimpleObjectProperty<MediatedItem>(it.value) }

        if (fieldInfo.fieldWidth != -1) {
            column.prefWidth = fieldInfo.fieldWidth.toDouble()
        }

        val isBoolean = if (model != null && model!!.count() > 0) {
            val p = getPropertyClass(model!![0]::class , fieldInfo.propName)
            (p != null && p == Boolean::class)
        } else false

        if (!isBoolean)
            column.setCellFactory { CustomListItemCell(fieldInfo.propName) }
        else {
            column.setCellFactory { CustomBooleanCell(fieldInfo.propName) }
        }

        //column.onEditCommit = EventHandler { event ->
        //    println("edit commit ${event.newValue}")
        //}

       // column.onEditStart = EventHandler { event ->
       //     println("edit start ${event.newValue}")
       // }

        column.style = when (fieldInfo.alignment) {
            MediatorFieldInfo.Alignment.Left ->  "-fx-alignment: CENTER-LEFT;"
            MediatorFieldInfo.Alignment.Right -> "-fx-alignment: CENTER-RIGHT;"
            MediatorFieldInfo.Alignment.Center -> "-fx-alignment: CENTER;"
        }

        column.isEditable = true//view!!.isEditable


        //column.cellValueFactory = PropertyValueFactory<CustomListItem, String>("text")



        return column

    }

    override fun createColumns() {
        if (fieldsInfo!!.count() != view!!.columns.count())
            view!!.columns.clear()
        fieldsInfo!!.forEachIndexed{ index, field ->
            if (view!!.columns.count()-1 < index){
                val column = createColumn(field)


                view!!.columns.add(column)
            }
            else {
                var column = view!!.columns[index]
                if (column.cellFactory.call(null) !is CustomListItemCell){
                    column = createColumn(field)
                    view!!.columns[index] = column
                }
                if (column.text != field.caption)
                    column.text = field.caption
            }

        }
    }

    override fun addNewItem(itemMediator: ObservableControlItemMediator<MediatedItem>, parent: Any?): MediatedItem {
        val item = MediatedItem(itemMediator)
        listItems().add(item)
        return item
    }

    override fun setItemText(item: MediatedItem, text: String) {
        item.text = text
        val index = view!!.items.indexOf(item)
        if (index > -1) {
            view!!.items[index] = item // to trigger update
        }
    }
    override fun getItemMediator(item: MediatedItem): ObservableControlItemMediator<MediatedItem> {
        return item.itemMediator as ObservableControlItemMediator<MediatedItem>
    }
}