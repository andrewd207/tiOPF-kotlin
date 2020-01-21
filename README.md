This is a work in progress and is not well tested. Beware :)

# What is it?
The [original tiOPF](https://github.com/graemeg/tiopf/tree/tiopf2) is an excellent object persistence framework written 
by others for Freepascal and Delphi. Also implemented is some of the [Mapper](https://github.com/graemeg/tiopf_apps) 
code to create the mediators. This is a port of that code.

With tiOPF you can create objects which can be persisted to a database easily or have them auto-magically mapped to 
widgets for easy editing. It's similar to @Serializable. **It requires Kotlin.reflection.full** so currently only works 
on the JVM target.

Use the @Published annotation on the properties you want to make available to tiOPF. For example:

~~~
// Object is defined in tiOPF.Object
class Foo: Object(){
    @Published var name = ""         
    @Published var age = 1
}
....

val foo = Foo()
foo.name = "Alexander the Great"
foo.age = 36
~~~
*Note: @Published has an optional parameter which is intended to automatically map properties to a table column in a 
database but is not implemented yet.*

Save an object to a persistence layer that has been setup.

~~~
foo.save() // this saves it to the database, with a little more work!
~~~
It's also possible to have an ObjectList<Foo>

~~~
val list = ObjectList<Foo>()
list.add(foo)
list.save() // more efficient with many items to save 

// and read too!
list.read() will read all the items in your table into the list   
~~~~


# Setting up the persistence layer

If you are not persisting your objects this part is not needed. The original tiOPF could persist to several databases 
and even XML or ini. So anything is possible.
~~~~
// setup code that must be performed at program start
val perLayer = CPersistJDBCSqlite
val user = ""
val db = "tipof_test.sqlite3"
val pass = ""
val table = "test"

GTIOPFManager().defaultPerLayerName = CPersistJDBCSqlite
GTIOPFManager().connectDatabase(db, user, pass)
checkCreateTable() // you must implement this

// 'oid' is a property of Object that is a unique identifier for the object stored in the database.
// Currently it can be a guid String or an Int64  

// register the property names to the field name in the table
GTIOPFManager().classDBMappingManager.registerMapping(db, Foo::class, table ,"oid", "OID", setOf(
        ClassDBMapRelationshipType.Primary
    ))
GTIOPFManager().classDBMappingManager.registerMapping(db, Foo::class, table,"name", "C_NAME")
GTIOPFManager().classDBMappingManager.registerMapping(db, Foo::class, table,"age", "C_AGE")
 
~~~~
## Create a table
It's possible also to create tables with code.
~~~
fun checkTableExists(){
    val tableName= "test"
    val metadata = DBMetadata()
    val database = GTIOPFManager().defaultDBConnectionPool!!.lock()
    var table: DBMetadataTable? = null    
    try {
        database.readMetadataTables(metadata)
        table = metadata.findByTableName(tableName)
    }
    finally {
        GTIOPFManager().defaultDBConnectionPool!!.unlock(database)
    }

    if (table == null){
        table = DBMetadataTable()
        table.name = tableName
        table.addInstance("OID", Query.QueryFieldKind.String, 36)
        table.addInstance("C_NAME", Query.QueryFieldKind.LongString)
        table.addInstance("C_AGE", Query.QueryFieldKind.Integer)
        created = true
        GTIOPFManager().createTable(table)
    }    
}
~~~

## How an object connects to a persistence layer

The Object.read() and Object.save() functions cause the objects to be visited by the persistence layer. The visitors need 
to be registered before they can be executed. Otherwise it will attempt to use Automapped visitors if you have registered
them. Read about [automap](./README-automap.md) and [hard-coded](./README-hardcoded%20visitors.md) visitors.
 

# Using Mediators

See [mediators](./README-mediators.md) for more information on using them.

