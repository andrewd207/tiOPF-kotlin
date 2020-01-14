package javaFXMediators

import javafx.scene.control.TextField
import kotlin.reflect.KClass

open class TextFieldMediatorViewCompanion: TextInputControlMediatorViewCompanion(){
    override val componentClass: KClass<*>
        get() = TextField::class
}

class TextFieldMediatorView: TextInputControlMediatorView<TextField>() {
    companion object: TextFieldMediatorViewCompanion()
}