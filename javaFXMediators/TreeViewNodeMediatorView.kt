package javaFXMediators

import javafx.collections.ObservableList
import javafx.scene.control.SelectionModel
import javafx.scene.control.TreeItem
import tiOPF.Object

class TreeViewNodeMediatorView(val node: TreeItem<MediatedItem>, data: Object, model: Object? = null): TreeViewListMediatorView(){
    init {
        val m = ObservableControlItemMediator<TreeItem<MediatedItem>>()
        m.listMediator = this
        active = false
        m.listMediator!!.subject = data
        node.value.itemMediator = m
        node.value.itemMediator.model = model ?: data
    }
    override fun listItems(): ObservableList<TreeItem<MediatedItem>> {
        return node.children
    }

    override fun selectionModel(): SelectionModel<TreeItem<MediatedItem>>? {
        // TreeViewNodeMediatorView is meant to be used in addition to TreeViewListMediatorView which
        // will manage selection
        return null
    }
}