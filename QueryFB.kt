package tiOPF

import java.sql.Connection
import java.sql.Driver
import java.sql.DriverManager
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

class QueryFB: QueryJDBC() {
    companion object: IQueryCompanion{
        override fun createInstance(): Query {
            return QueryFB()
        }
    }

}



class DatabaseFB: DatabaseJDBC(){
    companion object:  IDatabaseJDBCCompanion{
        override fun connect(url: String, user: String, password: String, props: Properties): Connection? {
            props.setProperty("user", user)
            props.setProperty("password", password)
            props.setProperty("lc_ctype", "utf8")
            //props.setProperty("wireCrypt", "ENABLED")
            val drv = driver

            val connection = drv!!.connect(url, props)
            connection?.autoCommit = false
            return connection
        }
        override fun getDriverName(): String {
            return "jdbc:firebirdsql:"
        }
        override fun createInstance(): Database {
            return DatabaseFB()
        }

        override fun klass(): KClass<Database> {
            return DatabaseFB::class as KClass<Database>
        }
        init {
            // make sure the driver is registered in JDBC
            Class.forName("org.firebirdsql.jdbc.FBDriver");
        }
    }
    override fun queryClass(): KClass<Query> {
        return QueryFB::class as KClass<Query>
    }
    override fun readMetadataTables(data: DBMetadata) {
        val query = GTIOPFManager().persistanceLayers.createQuery(DatabaseFB::class as KClass<Database>)
        startTransaction()
        try
        {
            query.attachDatabase(this)
            query.sqlText = """
                SELECT RDB${'$'}RELATION_NAME as TABLE_NAME FROM RDB${'$'}RELATIONS 
                WHERE ((RDB${'$'}SYSTEM_FLAG = 0) OR (RDB${'$'}SYSTEM_FLAG IS NULL)) 
                ORDER BY TABLE_NAME                            
            """.trimIndent()

            query.open()
            while (!query.eof) {
                val table = DBMetadataTable()
                table.name = query.getFieldAsString("TABLE_NAME").trim()
                table.objectState = Object.PerObjectState.PK
                data.add(table)
                query.next()
            }
            query.detachDatabase()
            data.objectState = Object.PerObjectState.Clean
        }
        finally {
            commit()
        }
    }
    private enum class FieldType(val value: Int){
        SHORT(7),
        LONG(8),
        QUAD(9),
        FLOAT(10),
        DATE(12),
        TIME(13),
        TEXT(14),
        INT64(16),
        BOOLEAN(23),
        DOUBLE(27),
        TIMESTAMP(35),
        VARYING(37),
        CSTRING(40),
        BLOB_ID(45),
        BLOB(261);
        companion object {
            private val map = values().associateBy(FieldType::value)
            fun fromInt(type: Int) = map[type]
        }
    }
    override fun readMetadataFields(data: DBMetadataTable) {
        val table = data
        val tableName = table.name.toUpperCase()
        val query = GTIOPFManager().persistanceLayers.createQuery(DatabaseFB::class as KClass<Database>)

        startTransaction()
        try {
            query.attachDatabase(this)
            query.sqlText = """
                SELECT r.RDB${"$"}FIELD_NAME AS field_name,
                RDB${"$"}FIELD_TYPE AS field_type,
                RDB${"$"}FIELD_SUB_TYPE AS field_sub_type,
                RDB${"$"}FIELD_LENGTH AS field_length                
                FROM RDB${"$"}RELATION_FIELDS r, RDB${"$"}FIELDS f 
                WHERE r.RDB${"$"}RELATION_NAME = '$tableName' 
                AND f.RDB${"$"}FIELD_NAME = r.RDB${"$"}FIELD_SOURCE 
            """.trimIndent()
            query.open()
            while (!query.eof) {
                val field = DBMetadataField()
                val fieldType = FieldType.fromInt (query.getFieldAsInteger("field_type").toInt())
                val fieldLength =  query.getFieldAsInteger("field_length")
                field.name = query.getFieldAsString("field_name").trim()
                field.width = 0
                when ( fieldType) {
                    FieldType.SHORT,
                    FieldType.LONG ->
                        field.kind = Query.QueryFieldKind.Integer
                    FieldType.INT64 ->
                        field.kind = Query.QueryFieldKind.Int64
                    FieldType.DOUBLE ->
                        field.kind = Query.QueryFieldKind.Float
                    FieldType.TIMESTAMP,
                    FieldType.DATE,
                    FieldType.TIME ->
                        field.kind = Query.QueryFieldKind.DateTime
                    FieldType.VARYING,
                    FieldType.TEXT -> {
                        field.kind = Query.QueryFieldKind.String
                        field.width = fieldLength.toInt()
                    }
                    FieldType.BLOB ->   {
                        assert(query.getFieldIsNull("field_sub_type"), {"field_sub_type_is null"})
                        val subType = query.getFieldAsInteger("field_sub_type")
                        if(subType == 1.toLong())
                            field.kind = Query.QueryFieldKind.LongString
                        else
                            throw EtiOPFInternalException("Invalid field_sub_type<$subType>")
                    }
                    else -> throw EtiOPFInternalException("Invalid fieldType <$fieldType>")
                }
                field.objectState = Object.PerObjectState.Clean
                table.add(field)
                query.next()
            }

        }
        finally {
            commit()
        }
        query.detachDatabase()
        table.objectState = Object.PerObjectState.Clean
    }
    override fun fieldMetadataToSQLCreate(fieldMetadata: DBMetadataField):String {
        val fieldName = fieldMetadata.name
        var fieldExt = ""
        if (fieldMetadata.notNull)
            fieldExt += " NOT NULL"
        if (fieldMetadata.primaryKey)
            fieldExt += " PRIMARY KEY"
        return (
                when (fieldMetadata.kind) {
                    Query.QueryFieldKind.String     -> "VARCHAR(${fieldMetadata.width})"
                    Query.QueryFieldKind.Int64      -> "INT64"
                    Query.QueryFieldKind.Integer    -> "INTEGER"
                    Query.QueryFieldKind.Float      -> "DOUBLE"
                    Query.QueryFieldKind.Money      -> "DECIMAL(18,4)"
                    Query.QueryFieldKind.DateTime   -> "TIMESTAMP"
                    Query.QueryFieldKind.Logical    -> "CHAR(1) default 'F' check($fieldName) in ('T', 'F'))"
                    Query.QueryFieldKind.Binary     -> "BLOB"
                    Query.QueryFieldKind.LongString -> "BLOB SUB_TYPE TEXT"
                    //Query.QueryFieldKind.Macro
                    else -> throw EtiOPFInternalException("Invalid fieldKind")
                })+fieldExt
    }
}

class PersistanceLayerFB: PersistanceLayerJDBC(){
    companion object: IPersistenceLayerClass{
        override fun createInstance(): PersistenceLayer { return PersistanceLayerFB()}
        init {
            GTIOPFManager().persistanceLayers.__registerPersistenceLayer(this)
        }
        // do not ust const!! otherwise it bypasses init from registering the database
        internal val layerName = "JDBC_Firebird"
    }

    override var persistenceLayerName = CPersistJDBCFirebird


    override val queryCompanion: IQueryCompanion
        get() = QueryFB.Companion
    override val databaseCompanion: IDatabaseCompanion
        get() = DatabaseFB.Companion

    override fun assignPersistenceLayerDefaults(defaults: PersistanceLayerDefaults) {
        defaults.persistanceLayerName = CPersistJDBCFirebird
        defaults.databaseName = "$CDefaultDatabaseDirectory$CDefaultDatabaseName.fdb"
        defaults.userName = "SYSDBA"
        defaults.password = "masterkey"
        defaults.canDropDatabase = true
        defaults.canCreateDatabase = true
        defaults.canSupportMultiUser = true
        defaults.canSupportSQL = true
    }
}

val CPersistJDBCFirebird = PersistanceLayerFB.layerName