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

    protected open fun setVisited(value: Visited){
        assert(value.testValid(Visited::class, true), {CTIErrorInvalidObject})
        this.privVisited = value
    }
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

    open fun execute(visited: Visited){
        privVisited = visited
    }


}