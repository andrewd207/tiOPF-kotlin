package tiOPF

class DBMetadata: ObjectList<DBMetadataTable>() {
    fun findByTableName(tableName: String): DBMetadataTable?{
        forEach {
            if (it.name.equals(tableName))
                return it
        }
        return null
    }

    override fun read(dbConnectionName: String, persistenceLayerName: String) {
        if (objectState != PerObjectState.Empty)
            return

        val database = GTIOPFManager().persistanceLayers.lockDatabase(dbConnectionName, persistenceLayerName)
        try {
            clear()
            database.readMetadataTables(this)
        }
        finally {
            GTIOPFManager().persistanceLayers.unlockDatabase(database, dbConnectionName, persistenceLayerName)
        }
    }
}