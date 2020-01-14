package javaFXMediators

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.CheckBox
import tiOPF.Mediator.ObjectUpdateMoment
import kotlin.reflect.KClass

open class CheckBoxMediatorViewCompanion: ControlMediatorViewCompanion(){
    override val componentClass: KClass<*>
        get() = CheckBox::class
}
class CheckBoxMediatorView: ControlMediatorView<CheckBox>() {
    companion object: CheckBoxMediatorViewCompanion()
    override fun doObjectToGUI() {
        view!!.isSelected = subject!!.getPropValue(fieldName) ?: false
    }

    override fun doGUIToObject() {
        subject!!.setPropValue(fieldName, view!!.isSelected)
    }
    private var contentChangedListener: ChangeListener<Boolean> = ChangeListener { observable, oldValue, newValue -> observedValueChanged() }
    override fun setupChangeListener() {
        view!!.selectedProperty().addListener(contentChangedListener)

    }
}