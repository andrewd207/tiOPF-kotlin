package tiOPF

class ObjectVisitorControllerConfig(visitorManager: VisitorManager): VisitorControllerConfig(visitorManager) {
    var databaseName: String =""
    var persistanceLayerName: String = ""
    fun setDatabaseAndPersistanceLayerNames(persistanceLayerName: String, dbConnectionName: String){
        this.databaseName = dbConnectionName
        this.persistanceLayerName = persistanceLayerName
    }
}