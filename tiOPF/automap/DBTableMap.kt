package tiOPF.automap
// complete
import tiOPF.ObjectList
import tiOPF.tiOPF.automap.DBColMap
import tiOPF.tiOPF.automap.PKInfo

class DBTableMap: ObjectList<DBColMap>() {
    val ownerAsDBColMap: DBColMap get () = owner as DBColMap
    var tableName = ""
    override val caption: String
        get() = tableName
    fun findByColName(colName: String): DBColMap?{
        forEach {
            if (it.colName.equals(colName, true))
                return it
        }
        return null
    }
    fun addColMap(colName: String, pkInfo: PKInfo): DBColMap {
        var result = findByColName(colName)
        if (result != null)
            return result

        result = DBColMap(colName, pkInfo)
        result.objectState = PerObjectState.Clean
        add(result)
        return result

    }
}