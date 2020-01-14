package javaFXMediators

import javafx.scene.control.ComboBox
import kotlin.reflect.KClass

open class ComboBoxMediatorViewCompanion: ComboBoxBaseMediatorViewCompanion(){
    override val componentClass: KClass<*>
        get() = ComboBox::class
}

class ComboBoxMediatorView<T: Any>: ComboBoxBaseMediatorView<T>() {
    companion object: ComboBoxMediatorViewCompanion()
}