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
        private var privDriver: Driver? = null
        override val driver: Driver?
            get() {
                if (privDriver == null) {
                    privDriver = DriverManager.getDriver("jdbc:firebirdsql:")
                }
                return privDriver
            }

        override fun connect(url: String, user: String, password: String, props: Properties): Connection? {
            props.setProperty("user", user)
            props.setProperty("password", password)
            props.setProperty("lc_ctype", "utf8")
            //props.setProperty("wireCrypt", "ENABLED")
            val drv = driver
            if (drv != null) {
                println(drv)
            }

            val connection = drv!!.connect(url, props)
            connection?.autoCommit = false
            return connection
        }
        override fun getDriverName(): String {
            return "firebirdsql"
        }

        override fun createInstance(): Database {
            return DatabaseFB()
        }


    }
    override fun queryClass(): KClass<Query> {
        return QueryFB::class as KClass<Query>
    }
}

class PersistanceLayerFB: PersistanceLayerJDBC(){
    companion object: IPersistenceLayerClass{
        override fun createInstance(): PersistenceLayer { return PersistanceLayerFB()}
        init {
            GTIOPFManager().persistanceLayers.__registerPersistenceLayer(this)
        }
        // do not ust const!! otherwise it interrupts the init from registering the database
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