package tiOPF.Mediator

import tiOPF.*
import tiOPF.Log.LOG
import tiOPF.Log.LogSeverity
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties



class MediatorManager {
    data class Def(val mediatorClass: KClass<MediatorView<*>>, val minSubjectClass: KClass<*>, val propertyTypes: TypeKinds, val propertyName: String){
        val mediatorCompanion = mediatorClass.companionObjectInstance as MediatorViewCompanion
        fun handles(subject: Any, gui: Any, prop: KProperty1<*, *>?): Boolean {
            return handles(subject::class, gui, prop)
        }
        fun handles(subjectClass: KClass<*>, gui: Any, prop: KProperty1<*, *>?): Boolean{
            val mediatorViewCompanion = (mediatorClass.companionObjectInstance as MediatorViewCompanion)
            val name: String
            var result = false
            if (prop == null) {
                result = mediatorViewCompanion.compositeMediator
                name = ""
            }
            else {
                name = prop.name
                result = true
            }
            if (!result)
                return false

            result = gui::class.isSubclassOf(mediatorViewCompanion.componentClass) && subjectClass.isSubclassOf(minSubjectClass)
            if (result && !mediatorViewCompanion.compositeMediator){
                if (propertyName != "")
                    result =  name.equals(propertyName, true)
            }
            return result
        }
        fun betterMatchThan(def: Def?): Boolean{
            if (def == null)
                return true

            var result = mediatorCompanion.compositeMediator == def.mediatorCompanion.compositeMediator
            if (result) {
                result = propertyName != "" && def.propertyName == ""
                if (!result) {
                    result = !(def.propertyName != "" && propertyName == "")
                    if (result) {
                        result = !mediatorCompanion.componentClass.isSubclassOf(def.mediatorCompanion.componentClass)
                        if (!result) {
                            result = !def.mediatorCompanion.componentClass.isSubclassOf(mediatorCompanion.componentClass)
                            if (result) {
                                result = minSubjectClass.isSubclassOf(def.minSubjectClass)
                                if (!result) {
                                    result = !def.minSubjectClass.isSubclassOf(minSubjectClass)
                                }
                            }
                        }
                    }
                }
            }
            return result
        }
    }
    companion object {
        val defs = mutableListOf<Def>()
        private fun registerMediator(
            mediatorClass: KClass<*>, // KClass<MediatorView<*>>
            minSubjectClass: KClass<*>,
            propertyTypes: TypeKinds,
            propertyName: String
        ): Def {
            if (!mediatorClass.isSubclassOf(MediatorView::class))
                mediatorError(this, "attempted to register invalid Mediator class: ${mediatorClass.simpleName}")
            val classCompanion = mediatorClass.companionObject as MediatorViewCompanion
            val s = (if (classCompanion.compositeMediator) "composite " else "")
            LOG(
                "Registering %smediator %s with subject %s".format(
                    s,
                    mediatorClass.simpleName,
                    minSubjectClass.simpleName
                ), LogSeverity.Debug
            )

            if (!minSubjectClass.isSubclassOf(ObjectList::class) && classCompanion.compositeMediator)
                mediatorError(this, SErrCompositeNeedsList.format(mediatorClass.simpleName, minSubjectClass.simpleName))

            val item = Def(mediatorClass as KClass<MediatorView<*>>, minSubjectClass, propertyTypes, propertyName)
            defs.add(item)
            return item
        }

        fun registerMediator(
            mediatorClass: KClass<*>, // KClass<MediatorView<*>>
            minSubjectClass: KClass<*>,
            propertyName: String
        ): Def {
            return registerMediator(mediatorClass, minSubjectClass, emptySet(), propertyName)
        }

        fun registerMediator(
            mediatorClass: KClass<*>, // KClass<MediatorView<*>>
            minSubjectClass: KClass<*>,
            propertyTypes: TypeKinds
        ): Def {
            return registerMediator(mediatorClass, minSubjectClass, propertyTypes, "")
        }
        /*fun registerMediator(mediatorClass: KClass<MediatorView<*>>, minSubjectClass: KClass<*>): Def {
        return registerMediator(mediatorClass, minSubjectClass, setOf(/*typekinds??*/), "propertyName")
    }*/

        fun findDefFor(subject: Object, gui: Any): Def? {
            return findDefFor(subject::class, gui, null)
        }

        fun findDefFor(subject: Any, gui: Any, propName: String): Def? {
            val props = propName.split('.')

            var realSubject: Any? = subject
            var prop: KProperty1<*, *>? = getPropFromPath(subject::class, propName, ValueReference((realSubject)))

            return if (realSubject != null)
                findDefFor(realSubject::class, gui, prop)
            else
                findDefFor(getClassFromPath(subject::class, propName)!!, gui, prop)
        }

        fun findDefFor(subjectClass: KClass<*>, gui: Any, prop: KProperty1<*, *>?): Def? {
            return defs.find { it.handles(subjectClass, gui, prop) }
        }
    }
}