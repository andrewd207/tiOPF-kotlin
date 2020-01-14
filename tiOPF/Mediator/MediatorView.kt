package tiOPF.Mediator

import tiOPF.*
import java.io.Closeable
import kotlin.reflect.KClass

open class MediatorViewCompanion {
    open val allowRecursiveCopy = false
    open val componentClass: KClass<*> = Any::class
    open val compositeMediator = false
}

open class MediatorView<T> constructor() : IObserverHandlesErrorState, Closeable, Object() {
    companion object: MediatorViewCompanion()
    @Published open var fieldName = ""
    @Published var rootFieldName = ""
    @Published var guiFieldName = ""
    @Published open var objectUpdateMoment = ObjectUpdateMoment.Default
    @Published var onBeforeGUIToObject: BeforeGuiToObjectEvent? = null
    @Published var onAfterGUIToObject: AfterGuiToObjectEvent? = null
    @Published var onObjectToGUI: ObjectToGuiEvent? = null
    @Published open var active: Boolean = true; set(value) {
        if (field == value)
            return
        field = value

        if (value) {
            valueList?.attachObserver(this)
            valueList?.notifyObservers()
        }
        else
            valueList?.detachObserver(this)

        if (active) {
            subject?.attachObserver(this)
            subject?.notifyObservers()
        }
        else
            subject?.detachObserver(this)

        if (view != null) {
            if (active)
                objectUpdateMoment = objectUpdateMoment
        }

    }

    constructor(view: T, subject: Object, fieldName: String, guiFieldName: String): this(){
        settingUp = true
        this.view = view
        this.subject = subject
        this.fieldName = fieldName.substringBefore(':')
        this.rootFieldName = fieldName.substringAfter(':')
        this.guiFieldName = guiFieldName
        settingUp = false
    }

    override fun close() {
        //view?.removeFreeNotification(viewHelper)
        valueList?.detachObserver(this)
        subject = null
    }

    open var valueList: ObjectList<Object>? = null
        set(value) {
            if (field == value)
                return
            field?.detachObserver(this)
            field = value
            field?.attachObserver(this)
        }

    open var view: T? = null ; set(value) {
        objectUpdateMoment = ObjectUpdateMoment.None
        field = value
        checkSetupGUIandObject()
        if (field != null)
            objectUpdateMoment = objectUpdateMoment
    }
    open var subject: Object? = null
        set(value) {
            if (field == value)
                return
            field?.detachObserver(this)
            field = value
            if (active)
                field?.attachObserver(this)
            checkSetupGUIandObject()
            if (!settingUp && field !=null && active)
                update(field!!, NotifyOperation.Changed)
        }
    open var selectedObject: Object? get() = null; set(value) {} // descendants can implement this if needed

    fun guiToObject(){
        var err: Exception? = null
        if (copyingCount > 0 && !allowRecursiveCopy)
            return
        copyingCount++
        try {
            var b = ValueReference(false)
            onBeforeGUIToObject?.invoke(this, view!!, subject!!, b)
            if (!b.value)
                doGUIToObject()
            onAfterGUIToObject?.invoke(this, view!!, subject!!)
        }
        catch (e: Exception) {
            err = e
        }
        finally {
            copyingCount--
        }
        if (err != null)
            throw err
    }
    fun objectToGUI(forceUpdate: Boolean = false){
        if (copyingCount > 0 && !allowRecursiveCopy && !forceUpdate)
            return
        copyingCount++
        var err: Exception? = null
        try {
            val b = ValueReference(false)
            onObjectToGUI?.invoke(this, subject!!, view!!, b)
            if (!b.value)
                doObjectToGUI()
        }
        catch (e: Exception) {
            err = e
        }
        finally {
            copyingCount--
        }
        if (err != null)
            throw err
    }
    fun guiChanged(){
        if (!settingUp) {
            guiToObject()
            testIfValid()
        }
    }

    override fun update(subject: Object, operation: NotifyOperation, data: Object?) {
        super.update(subject, operation, data)
        when (subject){
            this.subject -> {
                when (operation) {
                    NotifyOperation.Changed -> objectToGUI()
                    NotifyOperation.Free    -> this.subject = null
                }
            }
            valueList -> if (operation == NotifyOperation.Free)
                valueList = null
        }

    }

    protected fun checkFieldNames(){
        if (guiFieldName.isBlank())
            throwMediatorError(SErrNoGUIFieldName)

        if (fieldName.isBlank())
            throwMediatorError(SErrNoSubjectFieldName)
    }
    protected fun testIfValid(){
        val errors = ObjectErrorList()
        subject?.isValid(errors)
        updateGUIValidStatus(errors)
    }
    protected fun checkSetupGUIandObject(){
        if (subject != null && view != null)
            setupGUIandObject()
    }
    protected open fun setupGUIandObject(){}
    protected open fun doOnChange(sender: Any){
        guiChanged()
    }
    protected open fun updateGUIValidStatus(errors: ObjectErrorList){}
    protected fun dataAndPropertyValid(){
        var result = (subject != null && (!compositeMediator || fieldName != ""))
    }
    protected open fun doGUIToObject(){
        checkFieldNames()
        subject?.setPropValue(fieldName, getObjectProperty<Any>(view!!, guiFieldName))

    }
    protected open fun doObjectToGUI(){
        val valueRef = ValueReference(getObjectProperty<Any>(subject!!, fieldName))
        setObjectProperty(view!! as Any, guiFieldName, valueRef)
    }
    protected open fun getObjectPropValue(value: ValueReference<Any>){}
    protected fun throwMediatorError(message: String){
        mediatorError(this, message)
    }
    protected fun throwMediatorError(message: String, args: Array<Any>){
        mediatorError(this, message.format(args))
    }

    private fun viewNotification(any: Any, operation: Operation){
        if (view != null && any == view && operation == Operation.Remove)
            view = null
    }
    override fun processErrorState(subject: Object, operation: NotifyOperation, errors: ObjectErrorList) {
        updateGUIValidStatus(errors)
    }
    protected var useInternalOnChange = true
    //private val viewHelper = MediatorViewComponentHelper() // was for TComponent...so ?
    private var settingUp = false
    private var copyingCount: Int = 0
}