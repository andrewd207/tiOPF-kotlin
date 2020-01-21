# Hard-coded Visitors

Hard coded visitors give you the greatest flexibility and control as well as speed when loading and saving items.

~~~
// for each item
GTIOPFManager().visitorManager.registerVisitor("ReadFoo", Foo_Read::class as KClass<Visitor>)
GTIOPFManager().visitorManager.registerVisitor("SaveFoo", Foo_Delete::class as KClass<Visitor>)
GTIOPFManager().visitorManager.registerVisitor("SaveFoo", Foo_Update::class as KClass<Visitor>)
GTIOPFManager().visitorManager.registerVisitor("SaveFoo", Foo_Create::class as KClass<Visitor>)
// for lists
GTIOPFManager().visitorManager.registerVisitor("ReadFooObjectList", FooObjectList_Read::class as KClass<Visitor>)
~~~
*Foo_Read* might look like this:
~~~
class Foo_Read: VisitorSelect(){
    override fun acceptVisitor(): Boolean {
        return (visited is Foo && (visited!!.objectState in setOf(Object.PerObjectState.PK, Object.PerObjectState.Clean )))
    }

    override fun init() {
        query!!.sqlText = "SELECT * FROM TEST WHERE OID = :OID"
    }

    override fun setupParams() {
        (visited as Foo).oid.assignToQuery("OID", query!!)
    }

    override fun mapRowToObject() {
        val obj = visited as MyObj
        obj.oid.assignFromQuery("OID",query!!)
        obj.name = query!!.getFieldAsString("C_NAME")
        obj.age  = query!!.getFieldAsInteger("C_AGE").toInt()
    }
}
~~~

Then in Foo override the .read() function
~~~
override fun read(dbConnectionName: String, persistenceLayerName: String) {
    GTIOPFManager().visitorManager.execute("ReadFoo", this, dbConnectionName, persistenceLayerName)
}
~~~

And the list
~~~
class FooObjectList_Read: VisitorSelect(){
    override fun beforeRow() {
        // nothing
    }

    override fun mapRowToObject() {        
        var obj = (visited as FooObjectList).createItemInstance()!!
        obj.oid.assignFromQuery("OID", query!!)
        obj.name = query!!.getFieldAsString("C_NAME")
        obj.age = query!!.getFieldAsInteger("C_AGE").toInt()
        (visited as FooObjectList).add(obj)
    }

    override fun init() {
        var where = ""
        var order = ""
        if (visited is IFiltered){
            if ((visited as IFiltered).criteria!!.hasCriteria)
                where = " WHERE ${tiCriteriaAsSQL((visited as IFiltered).criteria!!)}"

            if ((visited as IFiltered).criteria!!.hasOrderBy)
                order = tiCriteriaAsSQL((visited as IFiltered).criteria!!)
        }

        val sql = "SELECT OID, C_NAME, C_AGE FROM test $where $order".trim()
        // TODO SQLParser()!!
        query!!.sqlText = sql
    }

    override fun acceptVisitor(): Boolean {        
        return (visited is FooObjectList && visited!!.objectState == Object.PerObjectState.Empty)
    }
}
---
// override the list read and save functions
class FooObjectList<FooObject>{
    override read(){
        GTIOPFManager().visitorManager.execute("ReadFooObjectList", this)
    }
    override fun save() {            
        GTIOPFManager().visitorManager.execute("SaveFooObject", this)
    }
}
~~~

### Wait! This is a lot of code!
Yes. It also comes with the benefit/drawback of extreme flexibility.

There exists a program "mapper" mentioned in the main [README](./README.md) that I am currently working on creating a Kotlin version of.
You define your objects in xml and it generates all this code for you. As an alternative, I think it also may be 
relatively easy to generate this generic sql using tiOPF's AutoMap. Famous last words? :/