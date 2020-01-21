# Using Automap

Automap is the easiest way to connect your objects to a persistence layer. All you need to do is register each property
of your objects with the mapping manager. 

It uses reflection to assign properties and so will be slower than hardcoded 
visitors. Consider using hardcoded visitors for a measurable speed improvement.  

With an object and list like this:
~~~
class MyObject(): Object(){    
    @Published var name: String = ""
    @Published var age: Int = 0                    
    companion object {
        fun createNew(name: String, age: String): MyObject{
            val result = MyObject()
            result.name = name
            result.age = age
            // without this the object will not be saved and will not recieve a unique oid.
            result.objectState = Object.PerObjectState.Create
            return result        
        }
    }
}

class MyObjectList: ObjectList<MyObject>()
~~~

## Register properties
You would register them like this:
~~~
val tableName = "PEOPLE"

// Register class. Each property must be annotated with @Published([fieldName])
// even tableName can be ommited here if the class is annotated with Published(tableName)
val manager = GTIOPFManager().classDBMappingManager 
manager.registerMapping(MyObj::class, tableName)

// register list type
manager.registerCollection(MyObjectList::class as KClass<PerObjectList>, MyObj::class)
~~~

## The Table
This assumes you have a table *PEOPLE* with the matching columns:

| OID  | C_NAME | C_AGE |
|------|--------|-------|


## Database
Set the default layer name and check if the database exists. I'm using Sqlite for this example.
~~~
val perLayer = CPersistJDBCSqlite
GTIOPFManager().defaultPerLayerName = perLayer

if (!GTIOPFManager().databaseExists(db, user, pass, perLayer))
    GTIOPFManager().createDatabase(db, user, pass, perLayer)
~~~

Connect to the database:
~~~
GTIOPFManager().connectDatabase(db, user, pass)

// check if table exists. You implement this
checkCreateTable()
~~~


## Adding items to the table 
Now we're set! You just create your Objects and save them.
~~~
val list = MyObjectList()

list.add(MyObject.createNew("Rita", 32))
list.add(MyObject.createNew("John", 29))
list.add(MyObject.createNew("Rick", 47))

list.save() 

// also works with individual objects
val myObject = MyObject.createNew("Jill", 30)
myObject.save()

~~~

## Reading items from the table
Your table now looks something like this:

| OID  | C_NAME | C_AGE |
|------|--------|-------|
| f343 | Rita   | 32    |
| ae2d | John   | 29    |
| 372b | Rick   | 47    |
| 4d3a | Jill   | 30    |

*Note: OID is a unique identifier assigned by the OID generator. Usually a GUID string.*

To retrieve your saved rows is very easy:
~~~
val list = MyObjectList()
list.read()
list.forEach{
    // do stuff
}
~~~


