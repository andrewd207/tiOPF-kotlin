package tiOPF

import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.superclasses

interface IVisitor{
    fun visitorControllerClass(): KClass<VisitorController> {
        return VisitorController::class
    }

}
open class Visitor: BaseObject() {
    companion object: IVisitor

    enum class IterationStyle {isTopDownRecurse, isTopDownSinglePass, isBottomUpSinglePass}
    protected open var privVisited: Visited? = null
    open val visited: Visited?
        get() = privVisited


    var continueVisiting: Boolean = true
    private var privDepth = 0
    val depth: Int
        get() = privDepth
    var iterationStyle: IterationStyle = IterationStyle.isTopDownRecurse
    var visitedsOwner: Visited? = null

    internal fun setDepth(value: Int){
        privDepth = value
    }

    internal fun intSetVisisited(value: Visited){
        setVisited(value)
    }
    protected open fun setVisited(value: Visited){
        assert(value.testValid(Visited::class, true), {CTIErrorInvalidObject})
        this.privVisited = value
    }

    internal fun intAcceptVisitor(): Boolean{return acceptVisitor()}
    internal fun intAcceptVisitor(visited: Visited): Boolean{return acceptVisitor(visited)}

    protected open fun acceptVisitor(): Boolean{
        return true
    }
    protected open fun acceptVisitor(visited: Visited): Boolean{
        assert(visited.testValid(), {CTIErrorInvalidObject})
        setVisited(visited)
        return acceptVisitor()
    }
    protected open fun visitBranch(derivedParent: Visited?, visited: Visited): Boolean{
        assert(testValid(derivedParent, null, true), {CTIErrorInvalidObject})
        assert(visited.testValid(), {CTIErrorInvalidObject})
        return true
    }
    internal fun intVisitBranch(derivedParent: Visited?, visited: Visited): Boolean{
        return visitBranch(derivedParent, visited)
    }

    open fun execute(visited: Visited?){
        privVisited = visited
    }


}


fun getControllerClass(kClass: KClass<Visitor>): KClass<VisitorController>{
    if (!kClass.isSubclassOf(Visitor::class))
        throw EtiOPFException("%s is not a subclass of Visitor".format(kClass.qualifiedName))
    var theClass = kClass
    if (kClass.companionObjectInstance != null && kClass.companionObjectInstance is IVisitor)
        return (kClass.companionObjectInstance as IVisitor).visitorControllerClass()
    while (theClass != Visitor::class){
      theClass.superclasses.forEach {
          if (it.companionObjectInstance != null && it.companionObjectInstance is IVisitor)
              return (it.companionObjectInstance as IVisitor).visitorControllerClass()
          if (it.isSubclassOf(Visitor::class))
              theClass = it as KClass<Visitor>
      }

    }

    // at least return something
    return VisitorController::class
}