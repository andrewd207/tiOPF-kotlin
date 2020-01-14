package tiOPF

import java.sql.Connection
import java.sql.Driver
import java.sql.DriverManager
import java.util.*
import kotlin.reflect.KClass

class QueryMariaDB: QueryJDBC() {
    companion object: IQueryCompanion{
        override fun createInstance(): Query {
            return QueryMariaDB()
        }
    }

}

class DatabaseMariaDB: DatabaseJDBC(){
    companion object:  IDatabaseJDBCCompanion{

        override fun connect(url: String, user: String, password: String, props: Properties): Connection? {
            props.setProperty("user", user)
            props.setProperty("password", password)
            val drv = driver

            val connection = drv!!.connect(url, props)
            connection?.autoCommit = false
            return connection
        }
        override fun getDriverName(): String {
            return "jdbc:mariadb:"
        }

        override fun createInstance(): Database {
            return DatabaseMariaDB()
        }

        override fun klass(): KClass<Database> {
            return DatabaseMariaDB::class as KClass<Database>
        }

        init {
            // make sure the driver is registered in JDBC
            Class.forName("org.mariadb.jdbc.Driver")
        }


    }
    override fun queryClass(): KClass<Query> {
        return QueryMariaDB::class as KClass<Query>
    }

    override fun readMetadataTables(data: DBMetadata) {
        val query = createAndAttachQuery()
        try {
            query.sqlText = "show tables"
            query.open()
            while (!query.eof) {
                val table = DBMetadataTable()
                table.name = query.getFieldAsString(1) // returns a field name like "Tables_in_$databasename so use index"
                table.objectState = Object.PerObjectState.PK
                data.add(table)
                query.next()
            }
        }
        finally {
            query.close()
            query.detachDatabase()
            commit()
        }

    }

    override fun fieldMetadataToSQLCreate(fieldMetadata: DBMetadataField): String {
        val fieldName = fieldMetadata.name
        var fieldExt = ""
        if (fieldMetadata.notNull)
            fieldExt += " NOT NULL"
        if (fieldMetadata.primaryKey)
            fieldExt += " PRIMARY KEY"
        return (
                when (fieldMetadata.kind) {
                    Query.QueryFieldKind.String     -> "CHAR(${fieldMetadata.width})"
                    Query.QueryFieldKind.Int64      -> "BIGINT"
                    Query.QueryFieldKind.Integer    -> "INT"
                    Query.QueryFieldKind.Float      -> "DOUBLE"
                    Query.QueryFieldKind.Money      -> "DECIMAL(18,4)"
                    Query.QueryFieldKind.DateTime   -> "DATETIME"
                    Query.QueryFieldKind.Logical    -> "BOOLEAN"
                    Query.QueryFieldKind.Binary     -> "LONGBLOB"
                    Query.QueryFieldKind.LongString -> "LONGTEXT"
                    //Query.QueryFieldKind.Macro
                    else -> throw EtiOPFInternalException("Invalid fieldKind")
                })+fieldExt
    }
}

class PersistanceLayerMariaDB: PersistanceLayerJDBC(){
    companion object: IPersistenceLayerClass{
        override fun createInstance(): PersistenceLayer { return PersistanceLayerMariaDB()}
        init {
            GTIOPFManager().persistanceLayers.__registerPersistenceLayer(this)
        }
        // do not ust const!! otherwise it bypasses init from registering the database
        internal val layerName = "JDBC_MariaDB"
    }

    override var persistenceLayerName = CPersistJDBCMariaDB


    override val queryCompanion: IQueryCompanion
        get() = QueryMariaDB.Companion
    override val databaseCompanion: IDatabaseCompanion
        get() = DatabaseMariaDB.Companion

    override fun assignPersistenceLayerDefaults(defaults: PersistanceLayerDefaults) {
        defaults.persistanceLayerName = CPersistJDBCMariaDB
        defaults.databaseName = "$CDefaultDatabaseDirectory$CDefaultDatabaseName"
        defaults.userName = ""
        defaults.password = ""
        defaults.canDropDatabase = true
        defaults.canCreateDatabase = true
        defaults.canSupportMultiUser = true
        defaults.canSupportSQL = true
    }
}

val CPersistJDBCMariaDB = PersistanceLayerMariaDB.layerName