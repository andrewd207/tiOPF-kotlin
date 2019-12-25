package tiOPF
// complete
interface IVisitorControllerClass{
    fun createInstance(visitorManager: VisitorManager, config: VisitorControllerConfig): VisitorController{
        return VisitorController(visitorManager, config)
    }
}

open class VisitorController(protected val visitorManager: VisitorManager, protected val config: VisitorControllerConfig): BaseObject() {
    companion object: IVisitorControllerClass
    val touchedByVisitorList = TouchedByVisitorList()
    open fun beforeExecuteVisitorGroup(){
        //do nothing
    }
    open fun beforeExecuteVisitor(visitor: Visitor){
        assert(visitor != null, { CTIErrorInvalidObject})
        //do nothing
    }
    open fun afterExecuteVisitor(visitor: Visitor){
        assert(visitor != null, { CTIErrorInvalidObject})
        //do nothing
    }
    open fun afterExecuteVisitorGroup(touchedByVisitorList: TouchedByVisitorList){
        assert(touchedByVisitorList != null, { CTIErrorInvalidObject})
        //do nothing
    }
    open fun afterExecuteVisitorGroupError(){
        // do nothing
    }

}