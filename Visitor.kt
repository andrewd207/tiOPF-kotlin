package tiOPF

interface IVisitorClass{
    fun createInstance(): Visitor
    fun visitorControllerClass(): IVisitorControllerClass
    val name: String
}

open class Visitor: BaseObject() {
    companion object : IVisitorClass{

        override fun createInstance(): Visitor {
            return Visitor()
        }

        override fun visitorControllerClass(): IVisitorControllerClass {
            return VisitorController
        }

        override val name: String
            get() = "Visitor"
    }
    enum class IterationStyle {isTopDownRecurse, isTopDownSinglePass, isBottomUpSinglePass}
    protected open var privVisited: Visited? = null
    open val visited: Visited?
        get() = privVisited


    var continueVisiting: Boolean = false
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

    open fun execute(visited: Visited){
        privVisited = visited
    }


}