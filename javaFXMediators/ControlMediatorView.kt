package javaFXMediators

import javafx.beans.property.ObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.Control
import javafx.scene.control.Tooltip
import javafx.scene.paint.Color
import tiOPF.Mediator.MediatorView
import tiOPF.Mediator.MediatorViewCompanion
import tiOPF.Mediator.ObjectUpdateMoment
import tiOPF.ObjectErrorList
import tiOPF.ValueReference
import tiOPF.getObjectProperty
import tiOPF.getPropFromPath
import kotlin.math.roundToInt
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberFunctions

open class ControlMediatorViewCompanion: MediatorViewCompanion(){
    override val componentClass: KClass<*>
        get() = Control::class
}

fun Color.cssColor(): String{
    val r = (red * 255.0).roundToInt()
    val g = (green * 255.0).roundToInt()
    val b = (blue * 255.0).roundToInt()
    val o = (opacity * 255.0).roundToInt()
    return "#%02x%02x%02x%02x".format(r,g,b,o)
}

abstract class ControlMediatorView<T: Control>: MediatorView<T>(){
    companion object: ControlMediatorViewCompanion()
    protected var viewHint = ""
    protected fun setViewState(colorCSS: String, hint: String){
        if (view != null && view is Control) {
            if (view!!.tooltip != null){
                view!!.tooltip.text = hint
            }
            else if (hint.isNotEmpty())
                view!!.tooltip = Tooltip(hint)
            //view!!.stylesheets.forEach { println(it) }
            //println("current style: ${view!!.style}")
            view!!.style = colorCSS
            //view!!.background = Background(BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY))
        }


    }
    init {
        guiFieldName = "text"
    }

    var viewErrorColor: String = "-fx-background-color: RED"; set(value) {
        field = value
        if (viewErrorVisible)
            testIfValid() // update view
    }
    fun getCurrentControlColor(): String{
        if (view != null) {
            return  view!!.style
        }

        return ""
    }
    var viewErrorVisible = true; set(value) {
        if (value != field) {
            field = value
            if (field)
                testIfValid()
            else
                setViewState(viewErrorColor, "")
        }
    }

    override fun doObjectToGUI() {
        //super.doObjectToGUI()
        // java has "text" field private and it's actually a TextProperty. so getObjectProperty returns null
        // getText and setText + text: TextProperty I think cause a bit of confusion, so just set it directly

        // using Any and toString() allow this to work with any type.
        val valueRef = ValueReference(getObjectProperty<Any>(subject!!, fieldName))
        if (valueRef.value == null)
            valueRef.value = ""
        var prop = getPropFromPath(view!!::class, guiFieldName, ValueReference(view!!))
        if (prop == null) {
            val setterName = "set" + guiFieldName.capitalize()
            val setter = view!!::class.memberFunctions.find { it.name == setterName }
            if (setter != null) {
                setter.call(view!!, valueRef.value.toString())
            } else {
                when (prop) {
                    is ObjectProperty<*> -> (prop as ObjectProperty<String>).value = valueRef.value.toString()
                    is KMutableProperty1<*, *> -> prop.setter.call(prop, valueRef.value)
                    //else -> println("prop is ${prop} g = $guiFieldName")
                }
            }
        }
    }

    override fun updateGUIValidStatus(errors: ObjectErrorList) {
        super.updateGUIValidStatus(errors)

        if (viewErrorVisible) {
            val error = errors.findByErrorProperty(rootFieldName)
            if (error != null)
                setViewState(viewErrorColor, error.errorMessage)
            else
                setViewState(getCurrentControlColor(), viewHint)

        }
    }

    private var observablePropertyListener: ChangeListener<Any>? = null
    private var focusChangedListener: ChangeListener<Boolean>? = null

    private var ignoreChange = true

    protected abstract fun setupChangeListener()
    protected fun observedValueChanged(){
        if (ignoreChange)
            return
        doOnChange(this)
    }

    override var objectUpdateMoment = super.objectUpdateMoment; set(value) {
        super.objectUpdateMoment = value
        field = value
        when (value) {
            ObjectUpdateMoment.OnChange, ObjectUpdateMoment.Custom, ObjectUpdateMoment.Default -> {
                ignoreChange = false
            }
            ObjectUpdateMoment.OnExit -> {
                if (focusChangedListener == null)
                    focusChangedListener = ChangeListener{ observable, wasFocused, isFocused ->
                        if (!isFocused)
                            doOnChange(this)
                    }
                view!!.focusedProperty().addListener(focusChangedListener)
            }
            ObjectUpdateMoment.None -> {
                if (focusChangedListener != null)
                    view!!.focusedProperty().removeListener(focusChangedListener)
                ignoreChange = true
                focusChangedListener = null
            }

        }

    }

    override fun setupGUIandObject() {
        super.setupGUIandObject()
        setupChangeListener()
    }
}