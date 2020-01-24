# Mediators

So far, some mediators for JavaFX widgets are implemented. Text Widgets, ComboBoxes, Lists, Trees, etc. See the 
javaFXMediators folder.

You can create code by hand but for brevity I'm using FXML and a Controller class.
Lookup some examples for Controller classes and FXML to learn how to use those together.

~~~
class FooController{
    @FXML lateinit var nameEdit: TextField
    @FXML lateinit var ageSpinner: Spinner<Int>
    private val nameMediator = TextFieldMediatorView()
    private val ageMediator = SpinnerMediatorViewInt()

    var foo: Foo? = null ; set (value) {
        field = value
        nameMediator.setup(nameEdit, foo, "name")        
        ageMediator.setup(ageSpinner, foo, "age")                
    }    
}
~~~
Now changing the value in the GUI will automatically update the object. You can save it to a persistence layer with 
*foo.save()*.

## More complicated mediators

### TableViews
TableViews have several columns which complicates the fieldName somewhat.

Imagine you have a list of Foo, a mediator, and a TableView.
~~~
val list = ObjectList<Foo>
@FXML lateinit var peopleTable: CustomFXTableView // typealias for TableView<MediatedItem>
val mediator
~~~

The *fieldName* of the mediator is actually a composite name. in the following format:
~~~
$fieldName[($columnWidth[, "$columnTitle"[, '$alignment']])][;]
~~~
Each column is separated by ';' so Foo would look like this:
~~~
val fieldNames = "name(-1, \"Name\"); age(-1, \"Age\")"
mediator.setup(peopleTable, list, fieldNames)

// or quick and dirty
val fieldNames = "name;age"
// the column titles will be set to the field names
~~~

### TreeView
Ok. Now things get really interesting. In JavaFX each TreeItem has it's own list of children.
A mediator must be made for each item in the list.

It's not possible currently to have the mediator do all the work for you.
But, it's also not impossible to accomplish.

Some generic code to add a mediated TreeItem that will need to be adapted for your specific needs.
~~~
private fun addChildNodeMediator(parent: TreeItem<MediatedItem>?, itemPropName: String, thisNodeName: String, 
                subject: Object, obj: SomeObject? = null, activateMediator: Boolean = true): TreeItem<MediatedItem>{
    val newItem: TreeItem<MediatedItem> = TreeItem(MediatedItem())
    newItem.value.text = thisNodeName
    val mediator = TreeViewNodeMediatorView(newItem, subject, obj)
    mediator.fieldsInfo!!.addFieldInfo(itemPropName, thisNodeName, -1)

    parent?.children?.add(newItem)
    obj?.listen(newItem) // obj must have a method 'listen(item)'

    mediator.subject = subject
    mediator.active = activateMediator

    return newItem
}
~~~

It can be used like so:

~~~
treeView.root = addChildNodeMediator(null, "fieldName", "Root Name", listObj, ChildObj())
mediator = CustomFXTreeMediatorView()
mediator.setup(treeView, list, "fieldName")
~~~

Now your tree will have it's first depth of items added automatically. If you only have one depth you are done. 
If not then you must use a listener to listen to the childernProperty of the new items as they are created.

~~~
abstract class Obj(){
    abstract fun listen(treeItem: TreeItem<MediatedItem>)
}  

class ChildObj: Obj(){
    override fun listen(treeItem: TreeItem<MediatedItem>) {
        treeItem.children.addListener(ListChangeListener {
            while (it.next()) {
                if (it.wasAdded() && !it.wasReplaced()) {
                    it.addedSubList.forEach { item ->
                        when (val model = item.value.itemMediator.model) {                                
                            // this code is pretty generic and will need you own specific types
                            is SubjectList.Item -> {
                                addChildNodeMediator(item, "someName", "Child Items", model.someOtherList, 
                                                                SomeOtherList(model.someOtherList))                                    
                            }
                        }
                    }
                }
            }
        })
    }
}
~~~
