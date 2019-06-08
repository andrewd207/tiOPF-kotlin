package tiOPF
// complete
import java.lang.Exception

class DBMaps: ObjectList<DBMap>() {
    val ownerAsClassDBMappingManager: ClassDBMappingManager get() = owner as ClassDBMappingManager
    fun addDBMap(databaseName: String): DBMap{
        var result = find { it.databaseName === databaseName }
        if (result != null)
            throw Exception("Attempt to register duplicate DBMap\n" +
                    "DatabaseName: " + databaseName + "\n" +
                    "Called in " + className() + ".AddDBMap")
        result = DBMap()
        result.databaseName = databaseName
        result.objectState = PerObjectState.Clean
        add(result)
        return result

    }
    fun findCreate(databaseName: String): DBMap{
        var result = find { it.databaseName === databaseName }
        if (result == null)
            result = addDBMap(databaseName)
        return result

    }

}

class DBMap: ObjectList<DBTableMap>() {
    public override var databaseName: String =""
    val ownerAsClassDBMappingManager: ClassDBMappingManager get() = owner as ClassDBMappingManager
    fun addTableMap(tableName: String): DBTableMap{
        var result = find { it.tableName === tableName }
        if (result != null)
            throw Exception("Attempt to register duplicate DBTableMap\n" +
                    "DatabaseName: " + databaseName + "\n" +
                    "TableName: " + tableName + "\n" +
                    "Called in " + className() + ".AddTableMap")
        result = DBTableMap()
        result.tableName = tableName
        result.objectState = PerObjectState.Clean
        add(result)
        return result
    }
    fun findCreate(tableName: String): DBTableMap{
        var result = find { it.tableName === tableName }
        if (result == null)
            result = addTableMap(tableName)
        return result
    }

}
