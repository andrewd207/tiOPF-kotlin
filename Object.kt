package tiOPF

import com.sun.org.apache.xpath.internal.operations.Bool
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

const val CErrorFieldNotAssigned = "Field <%s> is null. OID=%d"
const val CErrorFieldTooLong = "Field <%s> field too long. Allowed length %d, current length %d"
const val CErrorUnableToDetermineFieldName = "Unable to determine field name on <%s>"
const val CErrorInvalidObjectState   = "Invalid ObjectState"
const val CErrorNoFindMethodAssigned = "No find method assigned"
const val CErrorAttemptToSetNonPublishedProperty = "Attempt to set non-published property %s.%s to %s";
const val CErrorInvalidSortType = "Invalid PerObjListSortType"
const val CErrorDefaultOIDGeneratorNotAssigned = "Default OIDGenerator not assigned. You must register an instance of OIDGenerator with the global GTIOPFManager."
const val CError = "Error: "
const val CErrorInvalidDate = "A DateTime was passed when a Date was expected. DateTime=\"%s\""



open class Object(): Visited(), IObject<Object> {
    enum class PerObjectState {
        Empty,
        PK,
        Create,
        Update,
        Delete,
        Deleted,
        Clean,
        Loading
    }

    enum class NotifyOperation {noChanged, noAddItem, noDeleteItem, noFree, noCustom, noReSort}


    constructor(owner: Object?, databaseName: String ="", persistenceLayerName:String = ""): this(){
        this.owner = owner
        this.databaseName = databaseName
        this.persistenceLayerName = persistenceLayerName
    }
    constructor(databaseName: String ="", persistenceLayerName:String = ""): this(){
        this.databaseName = databaseName
        this.persistenceLayerName = persistenceLayerName
    }

    private var privOID: OID? =null
    var oid: OID
        get() {
            if (privOID == null){
                privOID = oidGenerator().getOIDClass().primaryConstructor!!.call()
            }
            return privOID!!
        }
        set(value) {privOID = value}

    open var owner: Object? = null
    var objectState = PerObjectState.Empty
    protected open var databaseName =""
    open var persistenceLayerName =""
    val updateTopicList = mutableListOf<String>()
    var observerList: MutableList<Object>? = mutableListOf() // TODO different type?
    open var deleted: Boolean
        get() {
            return objectState == PerObjectState.Delete || objectState == PerObjectState.Deleted
        }
        set(value) {
            if (value && !deleted){

            }
        }
    open var dirty: Boolean = false
        get() {
            val vis = VisPerObjIsDirty()
            this.iterate(vis)
            return vis.dirty

        }
    set(value) {
        if (value){
            when (objectState) {
                PerObjectState.Empty -> {
                    objectState = PerObjectState.Create
                    if ()
                }
            }

        }
    }

    private var updateCount: Int = 0;

    open fun oidGenerator(): OIDGenerator{
        val result = GTIOPFManager().defaultOIDGenerator
        if (result == null)
            throw EtiOPFProgrammerException(CErrorDefaultOIDGeneratorNotAssigned)
        return result
    }

    open fun notifyObservers(){
        notifyObservers(this, NotifyOperation.noChanged, null, "")
    }
    open fun notifyObservers(topic: String = ""){
        notifyObservers(this, NotifyOperation.noChanged, null, topic)
    }
    open fun notifyObservers(subject: Object, operation: NotifyOperation){
        notifyObservers(subject, operation, null, "")
    }
    open fun notifyObservers(subject: Object, operation: NotifyOperation, topic: String){
        notifyObservers(subject, operation, null, topic)
    }
    open fun notifyObservers(subject: Object, operation: NotifyOperation, data: Object?, topic: String){
        if (observerList != null)
            return
    }


    fun beginUpdate(topic: String = ""){
        updateCount++
        if (topic.isNotBlank())
            updateTopicList.add(topic)
    }
    fun endUpdate(){
        if (updateCount > 0){
            updateCount--
            if (updateCount == 0)
                notifyObservers()
        }
    }
    protected open fun assignClassProps(source: Object){
        // TODO
        //assert(countPropsByType(source, [tkClass]) = 0,
        //    'Trying to call ' + ClassName + '.Assign() on a class that contains ' +
        //            'object type properties. AssignClassProps() must be overridden in the concrete class.');

    }
    protected open fun assignPublicProps(source: Object){
        // TODO
    }
    protected open fun assignPublishedProps(source: Object){
        // TODO
    }

    override fun clone(): Object{
        val result = this::class.primaryConstructor!!.call()
        result.assign(this)
        return result
    }

    open fun assign(source: Object) {
        val error = "${source::class.simpleName} and ${this::class.simpleName} are not assignment compatible"
        assert(source::class.isSubclassOf(this::class) || this::class.isSubclassOf(source::class), { error })

        assignClassProps(source)
        assignPublicProps(source)
        assignPublishedProps(source)
    }

    open fun read(dbConnectionName: String = "", persistanceName: String = "")
    open fun read(){
        read("", "")
    }

}