package tiOPF

import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

// complete

class VisitorMappingGroup(val groupName: String, val visitorControllerClass: KClass<VisitorController>): BaseObject() {
    fun createControllerClassInstance(visitorManager: VisitorManager, config: VisitorControllerConfig): VisitorController{
            return visitorControllerClass.primaryConstructor!!.call(visitorManager, config)
    }
    private val mappings = List<KClass<Visitor>>()
    fun add(klass: KClass<Visitor>){
        if (mappings.contains(klass))
            throw Exception(CErrorAttemptToRegisterDuplicateVisitor.format(klass.qualifiedName))
        mappings.add(klass)
    }
    fun assignVisitorInstances(visitorList: List<Visitor>){
        mappings.forEach { visitorList.add( it.primaryConstructor!!.call()) }
    }

}