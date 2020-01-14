package javaFXMediators

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.ChoiceBox
import kotlin.reflect.KClass

open class ChoiceBoxMediatorViewCompanion: ControlMediatorViewCompanion(){
    override val componentClass: KClass<*>
        get() = ChoiceBoxMediatorView::class
}

class ChoiceBoxMediatorView<T>: ControlMediatorView<ChoiceBox<T>>() {
    override fun doObjectToGUI() {
        view!!.value = subject!!.getPropValue(fieldName)
    }

    override fun doGUIToObject() {
        subject!!.setPropValue(fieldName, view!!.value)
    }

    override fun setupGUIandObject() {
        super.setupGUIandObject()
        view!!.value = subject!!.getPropValue(fieldName)
    }

    private var contentChangedListener: ChangeListener<T> = ChangeListener { observable, oldValue, newValue -> observedValueChanged() }
    override fun setupChangeListener() {
        view!!.valueProperty().addListener(contentChangedListener)
    }
}