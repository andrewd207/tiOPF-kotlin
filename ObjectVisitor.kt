package tiOPF

import tiOPF.Log.LOG
import tiOPF.Log.LogSeverity
import kotlin.reflect.KClass

// complete

// Adds an owned query object
// Note: It is not necessary to manually lock and unlock DBConnections
// from this level and below - ObjectVisitorController does this for you.
// Adds a pooled database connection
open class ObjectVisitor: Visitor() {
    companion object: IVisitor  {
        override  fun visitorControllerClass(): KClass<VisitorController> {
            return ObjectVisitorController::class as KClass<VisitorController>
        }
    }
    override var visited: Object?
    get() {
        return if (privVisited != null)
            privVisited as Object
        else
            null
    }
    set(value) {privVisited = value}
    internal fun logQueryTiming(queryName: String, queryTime: ULong, scanTime: ULong){
        val lClassName = this::class.simpleName
        val classNames = arrayOf( "VisReadGroupPK",
            "VisReadQueryPK",
            "VisReadQueryDetail",
            "VisReadParams",
            "VisReadQueryByName")

        if (classNames.contains(lClassName))
            return

        LOG(
            queryName.padEnd(20) + ' ' + queryTime.toString().padEnd(7) + scanTime.toString().padEnd(
                7
            ), LogSeverity.QueryTiming
        )
    }
    var persistenceLayer: PersistenceLayer? = null
    var database: Database? = null
    var query: Query? = null
        get() {
            assert(field != null, {"Query not assigned"})
            return field
        }
        set(value) {
            assert(field == null, {"Query already assigned"} )
            field = value
        }

    protected open fun init(){
        // do nothing
    }
    protected open fun setupParams(){
        // do nothing
    }
    protected open fun unInit(){
        // do nothing
    }
    internal open fun final(visited: Object){
        when (visited.objectState) {
            Object.PerObjectState.Deleted -> {}
            Object.PerObjectState.Delete -> visited.objectState = Object.PerObjectState.Deleted
            else ->  visited.objectState = Object.PerObjectState.Clean
        }

    }




}