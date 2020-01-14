package javaFXMediators

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.TextInputControl
import javafx.scene.paint.Color
import tiOPF.Mediator.ObjectUpdateMoment
import tiOPF.ValueReferenceInt
import kotlin.reflect.KClass

open class TextInputControlMediatorViewCompanion: ControlMediatorViewCompanion(){
    override val componentClass: KClass<*>
        get() = TextInputControl::class
}

open class TextInputControlMediatorView<T: TextInputControl>: ControlMediatorView<T>(){
    companion object: TextInputControlMediatorViewCompanion();
    private var textChangedListener: ChangeListener<String>? = null
    private var focusChangedListener: ChangeListener<Boolean>? = null
    private var maxLen = -1
    private val maxLengthEnforcer = ChangeListener<String> { observable, oldValue, newValue ->
        if (maxLen > -1) {
            if (newValue.length > maxLen) {
                val s = newValue.substring(0, maxLen)
                view!!.text = s
            }
        }

    }

    var viewReadOnlyColor: Color = Color.LIGHTGRAY; set(value) {
        if (value != field) {
            field = value
            testIfValid() // update view
        }
    }
    init { guiFieldName = "text" }


    override fun doGUIToObject() {
        //super.doGUIToObject()
        checkFieldNames()
        subject?.setPropValue(fieldName, view!!.text)
    }



    override fun setupGUIandObject() {
        val mI = ValueReferenceInt(0)
        val mA = ValueReferenceInt(0)
        if (subject!!.getFieldBounds(fieldName, mI, mA)) {
            maxLen = mA.value
            view!!.textProperty().removeListener(maxLengthEnforcer)
            view!!.textProperty().addListener(maxLengthEnforcer)
        }
        else
            view!!.textProperty().removeListener(maxLengthEnforcer)

        if (view != null && view!!.styleClass.find { it == "tiopf" }.isNullOrEmpty())
            view!!.styleClass.add("tiopf")
    }

    private var contentChangedListener: ChangeListener<String> = ChangeListener { observable, oldValue, newValue -> observedValueChanged() }
    override fun setupChangeListener() {
        view!!.textProperty().addListener(contentChangedListener)
    }

}