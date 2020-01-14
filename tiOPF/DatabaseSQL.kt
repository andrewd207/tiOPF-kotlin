package tiOPF

abstract class DatabaseSQL: Database() {
    abstract fun fieldMetadataToSQLCreate(fieldMetadata: DBMetadataField): String
    override fun dropTable(tableMetadata: DBMetadataTable) {
        val sql = "drop table ${tableMetadata.name}"
        execSQL(sql)
    }

    override fun createTable(tableMetadata: DBMetadataTable) {
        var sql = ""
        tableMetadata.forEach {
            sql = tiAddTrailingValue(sql, ",\n")
            sql+= it.name + " " + fieldMetadataToSQLCreate(it)
        }
        sql = "create table " + tableMetadata.name + "\n(\n"+sql+"\n)"
        execSQL(sql)
    }
}