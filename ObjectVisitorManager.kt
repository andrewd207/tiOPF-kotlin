package tiOPF
// complete
class ObjectVisitorManager(manager: OPFManager): VisitorManager(manager) {
    override fun execute(groupName: String, visited: Visited) {
        execute(groupName, visited, "", "")
    }
    fun execute(groupName: String, visited: Visited, dbConnectionName: String, persistenceLayerName: String = "") {
        val visitorControllerConfig = ObjectVisitorControllerConfig(this)
        visitorControllerConfig.setDatabaseAndPersistanceLayerNames(persistenceLayerName, dbConnectionName)
        processVisitors(groupName, visited, visitorControllerConfig)
    }
}