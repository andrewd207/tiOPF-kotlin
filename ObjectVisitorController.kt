package tiOPF

open class ObjectVisitorController(visitorManager: VisitorManager, config: VisitorControllerConfig): VisitorController(visitorManager, config) {
    protected var database: Database? = null
    private var persistenceLayer: PersistenceLayer? = null
    protected open fun manager(): BaseObject{ return visitorManager.manager }
    protected val persistanceLayerName: String
        get() { return (config as ObjectVisitorControllerConfig).persistanceLayerName}
    protected val databaseName: String
        get() { return (config as ObjectVisitorControllerConfig).databaseName}

    override fun beforeExecuteVisitorGroup() {
        persistenceLayer = (manager() as OPFManager).persistanceLayers.findByPersistanceLayerName(persistanceLayerName)
        assert(persistenceLayer != null, {"Unable to find RegPerLayer <${persistanceLayerName}>"})
        database = persistenceLayer!!.db
    }
    override fun beforeExecuteVisitor(visitor: Visitor) {
        super.beforeExecuteVisitor(visitor)
    }

    override fun afterExecuteVisitor(visitor: Visitor) {
        super.afterExecuteVisitor(visitor)
    }

    override fun afterExecuteVisitorGroup(touchedByVisitorList: TouchedByVisitorList) {
        super.afterExecuteVisitorGroup(touchedByVisitorList)
    }

    override fun afterExecuteVisitorGroupError() {
        super.afterExecuteVisitorGroupError()
    }

}