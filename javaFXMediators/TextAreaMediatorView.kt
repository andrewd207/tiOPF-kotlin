package javaFXMediators

import javafx.scene.control.TextArea
import tiOPF.ValueReference
import tiOPF.getPropFromPath
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

open class TextAreaMediatorViewCompanion: TextInputControlMediatorViewCompanion(){
    override val componentClass: KClass<*>
        get() = TextArea::class
}

class TextAreaMediatorView: TextInputControlMediatorView<TextArea>() {
    companion object: TextAreaMediatorViewCompanion()

    override fun doObjectToGUI() {
        val value = subject!!.getPropValue<Any>(fieldName)
        when (value) {
            is String -> view!!.text = value
            is Collection<*> -> {
                var newValue = ""
                value.forEachIndexed { index, it ->
                    if (index == 0)
                        newValue = it.toString()
                    else
                        newValue += "\n${it.toString()}"
                }
                view!!.text = newValue

            }
            else -> view!!.text = value.toString()
        }
    }

    override fun doGUIToObject() {
        checkFieldNames()
        val prop = getPropFromPath(subject!!::class, fieldName, ValueReference(subject!!))
        val propClass = prop!!.getter.returnType.classifier as KClass<*>
        when (propClass) {
            String::class -> subject!!.setPropValue(fieldName, view!!.text)
            Int::class -> subject!!.setPropValue(fieldName, view!!.text.toInt())
            Long::class -> subject!!.setPropValue(fieldName, view!!.text.toLong())
            Float::class -> subject!!.setPropValue(fieldName, view!!.text.toFloat())
            Double::class -> subject!!.setPropValue(fieldName, view!!.text.toDouble())
            Boolean::class -> subject!!.setPropValue(fieldName, view!!.text.toBoolean())
            else ->
                if (propClass.isSubclassOf(MutableList::class)) {
                    // we are hoping for mutable Collection<String> else this could go badly
                    val values = view!!.text.split("\n")
                    val list = subject!!.getPropValue<MutableList<String>>(fieldName)
                    list!!.clear()
                    list.addAll(values)
                }

        }
    }
}