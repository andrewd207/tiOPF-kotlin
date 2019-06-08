package tiOPF
// complete

class VisitorMappingGroup(val groupName: String, val visitorControllerClass: IVisitorControllerClass): BaseObject() {
    fun createControllerClassInstance(visitorManager: VisitorManager, config: VisitorControllerConfig): VisitorController{
            return visitorControllerClass.createInstance(visitorManager, config)
    }
    private val mappings = List<IVisitorClass>()
    fun add(klass: IVisitorClass){
        if (mappings.contains(klass))
            throw Exception(CErrorAttemptToRegisterDuplicateVisitor.format(klass.name))
        mappings.add(klass)
    }
    fun assignVisitorInstances(visitorList: List<Visitor>){
        mappings.forEach { visitorList.add( it.createInstance()) }
    }

}