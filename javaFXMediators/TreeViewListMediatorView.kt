package javaFXMediators

import javafx.collections.ObservableList
import javafx.scene.control.SelectionModel
import javafx.scene.control.TreeItem

open class TreeViewListMediatorView: ObservableListControlMediatorView<CustomFXTreeView, TreeItem<MediatedItem>>(){
    override fun listItems(): ObservableList<TreeItem<MediatedItem>> {
        return view!!.root!!.children
    }

    override fun selectionModel(): SelectionModel<TreeItem<MediatedItem>>? {
        return view!!.selectionModel
    }

    override fun addNewItem(
        itemMediator: ObservableControlItemMediator<TreeItem<MediatedItem>>,
        parent: Any?
    ): TreeItem<MediatedItem> {
        val item = TreeItem<MediatedItem>(MediatedItem(itemMediator))
        listItems().add(item)
        return item
    }

    override fun setItemText(item: TreeItem<MediatedItem>, text: String) {
        item.value.text = text
        val index = listItems().indexOf(item)
        listItems()[index] = item // to trigger observer
    }

    override fun getItemMediator(item: TreeItem<MediatedItem>): ObservableControlItemMediator<TreeItem<MediatedItem>> {
        return item.value.itemMediator as ObservableControlItemMediator<TreeItem<MediatedItem>>
    }

}