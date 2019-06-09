package tiOPF

import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance

// complete
@Suppress("UNCHECKED_CAST")
open class VisitorManager(val manager: OPFManager): BaseObject() {
    private val syncronizer = MultiReadExclusiveWriteSynchronizer()
    protected open fun findVisitorMappingGroup(groupName: String): VisitorMappingGroup?{
        assert(groupName.isNotEmpty(), { "groupName not assigned" })
        visitorMappings.forEach { if (it.groupName === groupName) return it}
        return null
    }
    protected val visitorMappings = List<VisitorMappingGroup>()
    fun registerVisitor(groupName: String, visitorClass: KClass<Visitor>) {

        syncronizer.beginWrite()
        try {
            var visitorMappingGroup = findVisitorMappingGroup(groupName)
            if (visitorMappingGroup == null) {
                visitorMappingGroup = VisitorMappingGroup(groupName, getControllerClass(visitorClass))
                visitorMappings.add(visitorMappingGroup)
            }
            visitorMappingGroup.add(visitorClass)

        }
        finally {
            syncronizer.endWrite()
        }
    }
    fun unregisterVisitors(groupName: String){
        assert(groupName.isNotEmpty(), { "groupName not assigned" })
        syncronizer.beginWrite()
        try {
            var visitingMappingGroup = findVisitorMappingGroup(groupName)
            assert(visitingMappingGroup != null, {"Request to UnRegister visitor group that's not registered \"%s\"".format(groupName)})
            visitorMappings.remove(visitingMappingGroup)
        }
        finally {
            syncronizer.endWrite()
        }
    }

    open fun execute(groupName: String, visited: Visited){
        assert(groupName.isNotEmpty(), { "groupName not assigned" })
        val visitorControllerConfig = VisitorControllerConfig(this)
        processVisitors(groupName, visited, visitorControllerConfig)
    }
    protected open fun processVisitors(groupName: String, visited: Visited?, visitorControllerConfig: VisitorControllerConfig){
        assert(groupName.isNotEmpty(), { "groupName not assigned" })
        var logMessage = if (visited != null) {
            "About to process visitors for <%s> Visited: %s ()".format(
                groupName,
                visited!!::class.simpleName,
                visited!!.caption
            )
        } else{
            "About to process visitors for <%s> Visited: (null)".format(groupName)
        }
        LOG(logMessage, LogSeverity.lsVisitor)
        val visitors = List<Visitor>()
        val visitorMappingGroup = findVisitorMappingGroup(groupName)
        visitorMappingGroup?: throw Exception(CErrorInvalidVisitorGroup.format(groupName))
        val visitorController = visitorMappingGroup.createControllerClassInstance(this, visitorControllerConfig)
        assignVisitorInstances(visitorMappingGroup, visitors)

        visitorController.beforeExecuteVisitorGroup()
        try {
            executeVisitors(visitorController, visitors, visited)
            visitorController.afterExecuteVisitorGroup(visitorController.touchedByVisitorList)
        } catch (e: java.lang.Exception){
                visitorController.afterExecuteVisitorGroupError()
        }

        logMessage = if (visited != null) {
            "Finished process visitors for <%s> Visited: %s ()".format(
                groupName,
                visited!!::class.simpleName,
                visited!!.caption
            )
        } else{ "Finished process visitors for <%s> Visited: (null)".format(groupName) }
        LOG(logMessage, LogSeverity.lsVisitor)

    }
    fun executeVisitors(visitorController: VisitorController, visitors: List<Visitor>, visited: Visited?){
        visitors.forEach {
            visitorController.beforeExecuteVisitor(it)
            try {
                visited?.intIterateAssignTouched(it, visitorController.touchedByVisitorList)
                visited?: it.execute(null)
            }
            finally {
                visitorController.afterExecuteVisitor(it)
            }
        }
    }
    private fun assignVisitorInstances(visitorMappingGroup: VisitorMappingGroup, visitors: List<Visitor>){
        syncronizer.beginRead()
        try { visitorMappingGroup.assignVisitorInstances(visitors) }
        finally { syncronizer.endRead() }
    }

}