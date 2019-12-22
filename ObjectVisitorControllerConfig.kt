package tiOPF

const val CErrorDefaultPersistenceLayerNotAssigned = "Attempt to connect to the default persistence layer, but the default persistence layer has not been assigned."
const val CErrorDefaultDatabaseNotAssigned = "Attempt to connect to the default database but the default database has not been assigned."
const val CErrorAttemptToUseUnRegisteredPersistenceLayer ="Attempt to use unregistered persistence layer \"%s\""
const val CErrorAttemptToUseUnConnectedDatabase = "Attempt to use unconnected database \"%s\""

class ObjectVisitorControllerConfig(visitorManager: VisitorManager): VisitorControllerConfig(visitorManager) {
    var databaseName: String =""
    var persistanceLayerName: String = ""
    fun setDatabaseAndPersistanceLayerNames(persistanceLayerName: String, dbConnectionName: String){
        this.persistanceLayerName = (
                if (persistanceLayerName.isNotEmpty())
                    persistanceLayerName
                else {
                    if (GTIOPFManager().defaultPerLayerName.isEmpty())
                        throw EtiOPFException(CErrorDefaultPersistenceLayerNotAssigned)
                    else
                        GTIOPFManager().defaultPerLayerName
                })

        this.databaseName = (
                if (dbConnectionName.isNotEmpty())
                    dbConnectionName
                else{
                    if (GTIOPFManager().defaultDBConnectionName.isEmpty())
                        throw EtiOPFException(CErrorDefaultDatabaseNotAssigned)
                    else
                        GTIOPFManager().defaultDBConnectionName

                })

        if (!GTIOPFManager().persistanceLayers.isLoaded(this.persistanceLayerName))
            throw EtiOPFException(CErrorAttemptToUseUnRegisteredPersistenceLayer.format(this.persistanceLayerName))
        else if (!GTIOPFManager().persistanceLayers.findByPersistanceLayerName(this.persistanceLayerName)!!.dbConnectionPools.isConnected(this.databaseName))
            throw EtiOPFException(CErrorAttemptToUseUnConnectedDatabase.format(this.databaseName))
    }
}