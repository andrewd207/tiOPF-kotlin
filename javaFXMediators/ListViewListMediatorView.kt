package javaFXMediators

import javafx.collections.ObservableList
import javafx.scene.control.SelectionModel

class ListViewListMediatorView: ObservableListControlMediatorView<CustomFXListView, MediatedItem>(){
    override fun listItems(): ObservableList<MediatedItem> {
        return view!!.items
    }

    override fun selectionModel(): SelectionModel<MediatedItem>? {
        return view!!.selectionModel
    }

    override fun addNewItem(itemMediator: ObservableControlItemMediator<MediatedItem>, parent: Any?): MediatedItem {
        val item = MediatedItem(itemMediator)
        listItems().add(item)
        return item
    }

    override fun setItemText(item: MediatedItem, text: String) {
        item.text = text
    }

    override fun getItemMediator(item: MediatedItem): ObservableControlItemMediator<MediatedItem> {
        return item.itemMediator as ObservableControlItemMediator<MediatedItem>
    }
}