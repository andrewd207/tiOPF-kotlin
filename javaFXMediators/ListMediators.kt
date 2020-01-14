package javaFXMediators

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.control.*
import tiOPF.*
import tiOPF.Mediator.*


class MediatedItem(){
    constructor(mediator: ObservableControlItemMediator<*>): this(){
        this.itemMediator = mediator
    }
    lateinit var itemMediator: ObservableControlItemMediator<*>
    var text: String = ""
    override fun toString(): String {
        return text
    }
}
typealias CustomFXListView = ListView<MediatedItem>
typealias CustomFXTreeView = TreeView<MediatedItem>
typealias CustomFXTableView = TableView<MediatedItem>
typealias CustomFXColumn = TableColumn<MediatedItem, MediatedItem>














