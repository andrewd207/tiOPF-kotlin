package tiOPF

import tiOPF.Log.LOG
import tiOPF.Log.LogLevel
import tiOPF.Log.LogSeverity
import java.sql.Connection
import java.sql.Driver
import java.sql.DriverManager
import java.util.*
import kotlin.Exception
import kotlin.reflect.KClass

class QuerySqlite: QueryJDBC() {
    companion object: IQueryCompanion{
        override fun createInstance(): Query {
            return QuerySqlite()
        }
    }


}

class DatabaseSqlite: DatabaseJDBC(){
    companion object:  IDatabaseJDBCCompanion{

        override fun connect(url: String, user: String, password: String, props: Properties): Connection? {
            val drv = driver
            LOG("CONNECTING: ${getDriverName()}", LogSeverity.ConnectionPool)

            val connection = drv!!.connect(url, props)
            connection?.autoCommit = false
            return connection
        }
        override fun getDriverName(): String {
            return "jdbc:sqlite:"
        }

        override fun createInstance(): Database {
            return DatabaseSqlite()
        }
        override fun klass(): KClass<Database> {
            return DatabaseSqlite::class as KClass<Database>
        }
        init {
            // make sure the driver is registered in JDBC
            Class.forName("org.sqlite.JDBC")
        }


    }
    override fun queryClass(): KClass<Query> {
        return QuerySqlite::class as KClass<Query>
    }

    override fun readMetadataTables(data: DBMetadata) {
        val query = createAndAttachQuery()
        query.sqlText = """
            SELECT name
            FROM sqlite_master 
            WHERE type = 'table' AND name NOT LIKE 'sqlite_%'            
        """.trimIndent()
        query.execSQL()
        try {
            while (!query.eof){
                // first collect the table names
                val table = DBMetadataTable()
                table.name = query.getFieldAsString("name")
                data.add(table)
                data.objectState = Object.PerObjectState.Clean
                query.next()
            }
        }
        finally {
            query.close()
            commit()
        }
    }

    override fun readMetadataFields(data: DBMetadataTable) {
        val tableName = data.name
        val query = createAndAttachQuery()
        query.sqlText = "SELECT * from pragma_table_info('$tableName')"
        query.execSQL()
        try {
            while (!query.eof) {
                val field = DBMetadataField()
                field.name = query.getFieldAsString("name")
                field.primaryKey = query.getFieldAsBoolean("pk")
                field.notNull = query.getFieldAsBoolean("notnull")
                var type = query.getFieldAsString("type")
                val pStart = type.indexOf('(')
                val pEnd = type.indexOf(')')
                val length = type.substring(pStart + 1, pEnd - 1)
                if (!length.isBlank()) {
                    field.width = length.toInt()
                    type = type.substringBefore('(')
                }

                when (type.toUpperCase()) {
                    "INT", "INTEGER", "TINYINT", "SMALLINT", "MEDIUMINT", "BIGINT", "UNSIGNED BIG INT", "INT2", "INT8"
                    -> field.kind = Query.QueryFieldKind.Integer
                    "CHARACTER", "VARCHAR", "VARYING CHARACTER", "NCHAR", "NATIVE CHARACTER", "NVARCHAR", "TEXT", "CLOB"
                    -> field.kind = Query.QueryFieldKind.LongString
                    "REAL", "DOUBLE", "DOUBLE PRECISION", "FLOAT"
                    -> field.kind = Query.QueryFieldKind.Float
                    "NUMERIC", "DECIMAL"
                    -> field.kind = Query.QueryFieldKind.Money
                    "DATE", "DATETIME"
                    -> field.kind = Query.QueryFieldKind.DateTime
                    "BOOLEAN" -> field.kind = Query.QueryFieldKind.Logical
                    "BLOB" -> field.kind = Query.QueryFieldKind.Binary
                    else -> LOG(
                        "${className()}.readMetadataFields Unknown FieldKind ${query.getFieldAsString("type")}})",
                        LogSeverity.SQL
                    )
                }
                field.objectState = Object.PerObjectState.Clean
                data.add(field)
                query.next()
            }
        }
        finally {
            query.close()
            commit()
        }
    }

    override fun fieldMetadataToSQLCreate(fieldMetadata: DBMetadataField): String {
        when (fieldMetadata.kind) {
            Query.QueryFieldKind.String -> return (if (fieldMetadata.width == 0) "TEXT" else "VARCHAR(${fieldMetadata.width})")
            Query.QueryFieldKind.Integer -> return "INTEGER"
            Query.QueryFieldKind.Float -> return "REAL"
            Query.QueryFieldKind.DateTime -> return "DATETIME"
            Query.QueryFieldKind.Logical -> return "BOOLEAN"
            Query.QueryFieldKind.Binary -> return "BLOB"
            Query.QueryFieldKind.LongString -> return "TEXT"
            else -> throw Exception("unhandled field kind: ${fieldMetadata.kind} for ${className()}.fieldMetadataToSQLCreate")

        }
    }
}

class PersistanceLayerSqlite: PersistanceLayerJDBC(){
    companion object: IPersistenceLayerClass{
        override fun createInstance(): PersistenceLayer { return PersistanceLayerSqlite()}
        init {
            GTIOPFManager().persistanceLayers.__registerPersistenceLayer(this)
        }
        // do not ust const!! otherwise it bypasses init from registering the database
        internal val layerName = "JDBC_SQLLite"
    }

    override var persistenceLayerName = CPersistJDBCSqlite


    override val queryCompanion: IQueryCompanion
        get() = QuerySqlite.Companion
    override val databaseCompanion: IDatabaseCompanion
        get() = DatabaseSqlite.Companion

    override fun assignPersistenceLayerDefaults(defaults: PersistanceLayerDefaults) {
        defaults.persistanceLayerName = CPersistJDBCSqlite
        defaults.databaseName = "$CDefaultDatabaseDirectory$CDefaultDatabaseName.sqlite3"
        defaults.userName = ""
        defaults.password = ""
        defaults.canDropDatabase = true
        defaults.canCreateDatabase = true
        defaults.canSupportMultiUser = true
        defaults.canSupportSQL = true
    }
}

val CPersistJDBCSqlite = PersistanceLayerSqlite.layerName