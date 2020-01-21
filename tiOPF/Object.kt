package tiOPF

import java.util.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
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

interface IObserverHandlesErrorState {
    fun processErrorState(subject: Object, operation: Object.NotifyOperation, errors: ObjectErrorList);
}

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

    enum class NotifyOperation {Changed, AddItem, DeleteItem, Free, Custom, ReSort}


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
    internal fun getPersistenceLayerName(): String{
        return persistenceLayerName
    }
    protected open var persistenceLayerName =""
    val updateTopicList = mutableListOf<String>()
    private var privObserverList: MutableList<Object>? = null
    var observerList: MutableList<Object>
        get() {
            if (privObserverList == null)
                privObserverList = mutableListOf()
            return privObserverList!!
        }
        set(value) { privObserverList = value}
    open var deleted: Boolean
        get() {
            return objectState == PerObjectState.Delete || objectState == PerObjectState.Deleted
        }
        set(value) {
            if (value && !deleted){
                val vis = ObjectVisitorSetObjectStateToDelete()
                iterate(vis)
            }
        }
    open var dirty: Boolean
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
                    if (oid.isNull())
                        oidGenerator().assignNextOID(oid)
                }
                PerObjectState.PK -> objectState = PerObjectState.Update
                PerObjectState.Clean -> objectState = PerObjectState.Update
                PerObjectState.Create,
                PerObjectState.Update,
                PerObjectState.Delete,
                PerObjectState.Deleted -> { } // do nothing
                else -> throw EtiOPFProgrammerException(CErrorInvalidObjectState)
            }

        }
        else {
            objectState = PerObjectState.Clean
        }
    }

    private var updateCount: Int = 0;

    open fun oidGenerator(): OIDGenerator{
        val result = GTIOPFManager().defaultOIDGenerator
        if (result == null)
            throw EtiOPFProgrammerException(CErrorDefaultOIDGeneratorNotAssigned)
        return result
    }

    open fun isValid(errors: ObjectErrorList): Boolean{
        errors.clear()
        return true
    }
    protected open fun doGetFieldBounds(fieldName: String, minValue: ValueReferenceInt, maxValue: ValueReferenceInt, hasBounds: ValueReference<Boolean>){
        val f: FieldAbs?
        val tk = getPropertyType(this, fieldName).toSimpleTypeKind()
        f = (if (tk == SimpleTypeKind.Binary) getPropValue<FieldAbs>(fieldName) else null)
        if (f != null) {
            when (f){
                is FieldString -> {
                    minValue.value = f.nullValidation.ordinal
                    maxValue.value = f.maxLength
                    hasBounds.value = (minValue.value > 0 || maxValue.value > 0)
                }
                is FieldInteger -> {
                    hasBounds.value = f.maxDigits > 0
                    if (hasBounds.value) {
                        maxValue.value = 10
                        for (i in 2..f.maxDigits)
                            maxValue.value = maxValue.value * 10
                        maxValue.value = maxValue.value - 2
                        minValue.value = -maxValue.value
                    }
                }
            }
        }
    }
    protected open fun doGetFieldBounds(fieldName: String, minValue: ValueReferenceDouble, maxValue: ValueReferenceDouble, hasBounds: ValueReference<Boolean>){
        hasBounds.value = false
    }
    protected open fun doGetFieldBounds(fieldName: String, minValue: ValueReferenceDate, maxValue: ValueReferenceDate, hasBounds: ValueReference<Boolean>){
        hasBounds.value = false
    }

    fun getFieldBounds(fieldName: String, minValue: ValueReferenceInt, maxValue: ValueReferenceInt): Boolean{
        val result = ValueReference(false)
        try {
            doGetFieldBounds(fieldName, minValue, maxValue, result)
        }
        catch (e: Exception){
            return false
        }

        return result.value
    }
    fun getFieldBounds(fieldName: String, minValue: ValueReferenceDouble, maxValue: ValueReferenceDouble): Boolean{
        val result = ValueReference(false)
        doGetFieldBounds(fieldName, minValue, maxValue, result)
        return result.value

    }
    fun getFieldBounds(fieldName: String, minValue: ValueReferenceDate, maxValue: ValueReferenceDate): Boolean{
        val result = ValueReference(false)
        doGetFieldBounds(fieldName, minValue, maxValue, result)
        return result.value
    }

    open fun attachObserver(observer: Object){
        if (privObserverList == null || observerList.find { it == observer } == null)
            observerList.add(observer)
    }

    open fun detachObserver(observer: Object){
        if (privObserverList == null)
            return
        observerList.remove(observer)

        // try to save some memory
        if (observerList.size == 0)
            privObserverList == null
    }

    open fun update(subject: Object){}
    open fun update(subject: Object, operation: NotifyOperation){
        update(subject, operation, null)
    }
    open fun update(subject: Object, operation: NotifyOperation, data: Object?){
        when (operation) {
            NotifyOperation.Changed -> update(subject)
            NotifyOperation.Free    -> stopObserving(subject)
        }
    }

    open fun notifyObservers(){
        notifyObservers(this, NotifyOperation.Changed, null, "")
    }
    open fun notifyObservers(topic: String = ""){
        notifyObservers(this, NotifyOperation.Changed, null, topic)
    }
    open fun notifyObservers(subject: Object, operation: NotifyOperation){
        notifyObservers(subject, operation, null, "")
    }
    open fun notifyObservers(subject: Object, operation: NotifyOperation, topic: String){
        notifyObservers(subject, operation, null, topic)
    }
    open fun notifyObservers(subject: Object, operation: NotifyOperation, data: Object?, topic: String){
        if (privObserverList == null)
            return
        var errors: ObjectErrorList? = null

        if (topic.isNotEmpty())
            updateTopicList.add(topic)

        val lObserverList = mutableListOf<Object>()
        lObserverList.addAll(privObserverList!!)

        var needsErrorList = false

        if (operation != NotifyOperation.Free) {
            lObserverList.forEach {

                if (it != null && it is IObserverHandlesErrorState) {
                    needsErrorList = true
                    errors = ObjectErrorList()
                    subject.isValid(errors!!)
                    return@forEach
                }
            }
        }

        lObserverList.forEachIndexed { index, it ->
            if (this.privObserverList == null)
                return@forEachIndexed


            if (index == 0 || privObserverList!!.indexOf(it) != -1){
                it.update(subject, operation, data)
                if (needsErrorList){
                    if (it is IObserverHandlesErrorState)
                        it.processErrorState(subject, operation, errors!!)
                }
            }
        }
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
        oid.assign(source.oid)
        objectState = source.objectState
        if (source.owner is ObjectList<*>)
            owner = source.owner
    }
    protected fun assignPublishedProp(source: Object, propName: String){
        setPropValue<Any>(propName, source.getPropValue(propName)!!)
    }

    protected open fun assignPublishedProps(source: Object, propFilter: TypeKinds = setOf()){
        val localFilter: TypeKinds
        if (propFilter.isEmpty())
            localFilter = CTypeKindSimple.plus(TypeKind.ENUM)
        else
            localFilter = propFilter

        val list = List<String>()
        getPropertyNames(this, list, localFilter)
        list.forEach {
            //println("assigning $it")
            if (isReadWriteProp(it) && isPublishedProp(this::class, it)){
                try {

                    assignPublishedProp(source, it)

                }
                catch (e: Exception){
                    throw Exception(CErrorSettingProperty.format(className(), it, e.message))
                }


            }
        }

    }

    open fun isReadWriteProp(propName: String): Boolean{
        return propName.equals("OID", true) || isReadWriteProp(this, propName)
    }

    open fun isReadWriteProp(property: KProperty<*>): Boolean{
        return property.name.equals("OID", true) || property is KMutableProperty<*>
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

    open fun read(dbConnectionName: String, persistenceLayerName: String = ""){
        GTIOPFManager().visitorManager.execute(CuStandardTask_Read,this, dbConnectionName, persistenceLayerName)

    }
    open fun read(){
        read("", "")
    }
    open fun readPK(dbConnectionName: String, persistenceLayerName: String){
        GTIOPFManager().visitorManager.execute(CuStandardTask_ReadPK,this, dbConnectionName, persistenceLayerName)
    }


    open fun readThis(dbConnectionName: String, persistenceLayerName: String){
        GTIOPFManager().visitorManager.execute(CuStandardTask_ReadThis,this, dbConnectionName, persistenceLayerName)
    }

    open fun save(){
        save( "", "")
    }
    open fun save(dbConnectionName: String, persistenceLayerName: String = ""){
        GTIOPFManager().visitorManager.execute(CuStandardTask_Save,this, dbConnectionName, persistenceLayerName)
    }

    open fun <T>getPropValue(propName: String): T?{
        if (propName.equals("oid", true))
            return oid.asString as T
        val result = getObjectProperty<Any>(this, propName)

        return  (if (result != null) result as T else null)

    }

    open fun stopObserving(subject: Object){
        // do nothing. child classes will implement
    }

    open fun <T>setPropValue(propName: String, value: T){
        if (propName.equals("oid", true))
            oid.asString = value as String
        else
          setObjectProperty(this, propName, value)

    }

    fun getPropCount(propFilter: Set<TypeKind> = CTypeKindSimple): Int{
        val list = List<String>()
        getPropertyNames(this, list, propFilter)
        return list.size
    }

    fun getProperties(propFilter: Set<TypeKind> = CTypeKindSimple): List<String>{
        val result = List<String>()
        getPropertyNames(this, result, propFilter)
        return result
    }

    protected fun getPublished(): Published?{
        return this::class.findAnnotation()
    }

}