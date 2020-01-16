package javaFXMediators

import tiOPF.Mediator.MediatorManager
import tiOPF.TypeKind

fun registerFallbackMediators(){
    MediatorManager.registerMediator(TextFieldMediatorView::class, Object::class, setOf(TypeKind.STRING, TypeKind.INT, TypeKind.FLOAT))
    MediatorManager.registerMediator(TextAreaMediatorView::class, Object::class, setOf(TypeKind.STRING))
    MediatorManager.registerMediator(ComboBoxMediatorView::class, Object::class, setOf(TypeKind.STRING))
    MediatorManager.registerMediator(CheckBoxMediatorView::class, Object::class, setOf(TypeKind.BOOLEAN))
    MediatorManager.registerMediator(SpinnerMediatorViewInt::class, Object::class, setOf(TypeKind.INT))
}