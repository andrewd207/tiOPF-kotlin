package tiOPF
//complete

import kotlin.reflect.KClass

open class DBConnectionPool(protected val dbConnectionPools: DBConnectionPools, val databaseAlias: String,val dbConnectionParams: DBConnectionParams): Pool(dbConnectionPools.minPoolSize, dbConnectionPools.maxPoolSize) {
    override fun pooledItemInterface(): IPooledItemClass {
        return PooledDatabase.Companion
    }

    override fun afterAddPooledItem(item: PooledItem) {
        val layer = dbConnectionPools.persistenceLayer
        val database = layer.databaseCompanion.createInstance()
        item.data = database
        database.connect(   dbConnectionParams.databaseName,
                            dbConnectionParams.userName,
                            dbConnectionParams.password,
                            dbConnectionParams.params)
    }

    override fun lock(): Database{
        return super.lock() as Database

    }

    fun unlock(database: Database){
        assert(database is Database, { CTIErrorInvalidObject })
        if (database.inTransaction())
            database.rollback()
        super.unlock(database)

    }

    fun detailsAsString(): String{
        assert(dbConnectionPools is DBConnectionPools, { CTIErrorInvalidObject})
        assert(dbConnectionPools.persistenceLayer is PersistenceLayer, { CTIErrorInvalidObject})
        val persistenceLayer = dbConnectionPools.persistenceLayer
        return "Persistence layer:   " + persistenceLayer.persistenceLayerName + tiLineEnd() +
        "Database alias:      " + databaseAlias + tiLineEnd() +
                "Database name:       " + dbConnectionParams.databaseName + tiLineEnd() +
                "User name:           " + dbConnectionParams.userName     + tiLineEnd() +
                "Password:            " + CPasswordMasked + tiLineEnd() +
                "Number in pool:      " + count.toString();

    }


}