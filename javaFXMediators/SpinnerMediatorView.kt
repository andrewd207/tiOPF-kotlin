package javaFXMediators

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import tiOPF.Mediator.ObjectUpdateMoment
import kotlin.reflect.KClass

open class SpinnerMediatorViewCompanion: ControlMediatorViewCompanion(){
    override val componentClass: KClass<*>
        get() = Spinner::class
}

class SpinnerMediatorViewInt: SpinnerMediatorView<Int>(){
    override fun setupGUIandObject() {
        view!!.valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(0, Int.MAX_VALUE, subject!!.getPropValue(fieldName)!!)
    }
}


open class SpinnerMediatorView<T>: ControlMediatorView<Spinner<T>>() {
    companion object: SpinnerMediatorViewCompanion()

    override fun doObjectToGUI() {
        view!!.valueFactory.value = subject!!.getPropValue<T>(fieldName)
    }

    override fun doGUIToObject() {
        subject!!.setPropValue(fieldName, view!!.value)
    }


    private var contentChangedListener: ChangeListener<T> = ChangeListener { observable, oldValue, newValue -> observedValueChanged() }
    override fun setupChangeListener() {
        view!!.valueFactory.valueProperty().addListener(contentChangedListener)

    }
}