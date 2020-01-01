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


    }
    override fun queryClass(): KClass<Query> {
        return QueryMariaDB::class as KClass<Query>
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