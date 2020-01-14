package javaFXMediators

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.ComboBoxBase
import tiOPF.Mediator.ObjectUpdateMoment
import tiOPF.getObjectProperty
import kotlin.reflect.KClass

open class ComboBoxBaseMediatorViewCompanion: ControlMediatorViewCompanion(){
    override val componentClass: KClass<*>
        get() = ComboBoxBase::class
}

open class ComboBoxBaseMediatorView<T: Any>: ControlMediatorView<ComboBoxBase<T>>() {
    init {
        guiFieldName = "value"
    }

    override fun doObjectToGUI() {
        view!!.value = getObjectProperty(subject!!, fieldName)
    }


    override fun doGUIToObject() {
        //super.doGUIToObject()
        checkFieldNames()
        //println("setting $subject.$fieldName to ${view!!.value} : ${view!!.value::class.simpleName} of ${view!!}")
        subject!!.setPropValue(fieldName, view!!.value)
        //setObjectProperty(subject!!, fieldName, view!!.value)
        //subject?.setPropValue(fieldName, view!!.value)
    }

    private val changedListener =  ChangeListener<T>{_, _, newValue -> doOnChange(this) }
    private val focusLeaveListener =  ChangeListener<Boolean>{_, _, newValue -> if (!newValue) doOnChange(this) }

    /*override var objectUpdateMoment: ObjectUpdateMoment
        get() = super.objectUpdateMoment
        set(value) {
            super.objectUpdateMoment = value
            if (view != null){
                when (value) {
                    ObjectUpdateMoment.OnChange, ObjectUpdateMoment.Default, ObjectUpdateMoment.Custom -> {
                        try { view!!.valueProperty().removeListener(changedListener) } finally {}
                        view!!.valueProperty().addListener(changedListener)
                    }
                    ObjectUpdateMoment.OnExit -> {
                        try { view!!.focusedProperty().removeListener(focusLeaveListener) } finally {}
                        view!!.focusedProperty().addListener(focusLeaveListener)
                    }
                    ObjectUpdateMoment.None -> {
                        try { view!!.valueProperty().removeListener(changedListener) } finally {}
                        try { view!!.focusedProperty().removeListener(focusLeaveListener) } finally {}
                    }

                }
            }
        }
    */

    private var contentChangedListener: ChangeListener<T> = ChangeListener { observable, oldValue, newValue -> observedValueChanged() }
    override fun setupChangeListener() {
        view!!.valueProperty().addListener(contentChangedListener)

    }
}