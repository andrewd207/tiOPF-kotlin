package tiOPF
//complete
open class ObjectVisitorController(visitorManager: VisitorManager, config: VisitorControllerConfig): VisitorController(visitorManager, config) {
    protected var database: Database? = null
    private var persistenceLayer: PersistenceLayer? = null
    protected open fun manager(): OPFManager{ return visitorManager.manager }
    protected val persistanceLayerName: String
        get() { return (config as ObjectVisitorControllerConfig).persistanceLayerName}
    protected val databaseName: String
        get() { return (config as ObjectVisitorControllerConfig).databaseName}

    override fun beforeExecuteVisitorGroup() {
        persistenceLayer = manager().persistanceLayers.findByPersistanceLayerName(persistanceLayerName)
        assert(persistenceLayer != null, {"Unable to find RegPerLayer <${persistanceLayerName}>"})
        database = persistenceLayer!!.dbConnectionPools.lock(databaseName)
    }
    override fun beforeExecuteVisitor(visitor: Visitor) {
        assert(visitor is ObjectVisitor, { CTIErrorInvalidObject })
        if (visitor is ObjectVisitor) {
            visitor.persistenceLayer = persistenceLayer
            visitor.database = database
            visitor.query = database!!.createQuery()
            visitor.query!!.attachDatabase(database!!)
        }
    }

    override fun afterExecuteVisitor(visitor: Visitor) {
        assert(database != null, { CTIErrorInvalidObject })
        if (database != null) {
            (visitor as ObjectVisitor).database = null
            visitor.persistenceLayer = null
        }
    }

    override fun afterExecuteVisitorGroup(touchedByVisitorList: TouchedByVisitorList) {
        database!!.commit()
        touchedByVisitorList.getItems().forEach {
            val visitor = it.visitor as ObjectVisitor
            val visited = it.visited as Object
            visitor.final(visited)
        }
        persistenceLayer!!.dbConnectionPools.unlock(databaseName, database!!)
        database = null
    }

    override fun afterExecuteVisitorGroupError() {
        assert(database != null, { CTIErrorInvalidObject })
        assert(persistenceLayer != null, {CTIErrorInvalidObject})
        if (database != null) {
            database!!.rollback()
            persistenceLayer!!.dbConnectionPools.unlock(databaseName, database!!)
            database = null
        }

    }

}