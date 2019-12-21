package tiOPF

open class DBMetadataTable: ObjectList<DBMetadataField>() {
    open val maxFieldNameWidth = 0
    var ownerAsDBMetadata: DBMetadata?
        get() = super.owner as DBMetadata
        set(value) { super.owner = value}
    fun addInstance(fieldName: String, fieldKind: Query.QueryFieldKind, fieldWidth: Int = 0): DBMetadataField{
        val result =  addInstance()
        result.name = fieldName
        result.kind = fieldKind
        if (fieldKind == Query.QueryFieldKind.Logical)
            result.width = 1
        else
            result.width = fieldWidth

        result.objectState = PerObjectState.Clean
        return result
    }

    fun addInstance(): DBMetadataField{
        val result = DBMetadataField()
        add(result)
        return result
    }
    override fun read(dbConnectionName: String, persistenceName: String){
        if (objectState != PerObjectState.PK)
            return

        val database: Database = GTIOPFManager().persistanceLayers.lockDatabase(dbConnectionName, persistenceLayerName)
        try {
            database.readMetadataFields(this)
        }
        finally {
            GTIOPFManager().persistanceLayers.unlockDatabase(database, dbConnectionName, persistenceLayerName)
        }

    }

    var name: String =""

}