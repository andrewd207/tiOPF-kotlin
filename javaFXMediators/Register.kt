package javaFXMediators

import tiOPF.Mediator.MediatorManager
import tiOPF.TypeKind

fun registerFallbackMediators(){
    MediatorManager.registerMediator(TextFieldMediatorView::class, Object::class, setOf(TypeKind.STRING, TypeKind.INT, TypeKind.FLOAT))
}