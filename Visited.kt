package tiOPF
// complete
import kotlin.reflect.KClass

const val CErrorInvalidIterationStyle = "Invalid IterationStyle"

typealias VisitedTouchMethod = (candidates: Visited, visitor: Visitor, list: TouchedByVisitorList, iterationDepth: Int) -> Unit

open class Visited: BaseObject(){
    @Published open val caption: String = this::class.toString()
    fun iterate(visitor: Visitor){
        val touchedByVisitorList = TouchedByVisitorList()
        iterateAssignTouched(visitor, touchedByVisitorList)
    }
    fun findAllByClassType(kClass: KClass<*>, list: List<Visited>){
        list.clear()
        val vis = VisFindAllByClass(kClass, list)
        iterate(vis)
    }
    protected fun iterateTopDownRecurse(visitor: Visitor, touchedByVisitorList: TouchedByVisitorList){
        val touchedObjectList = TouchedByVisitorList()
        try {
            iterateRecurse(visitor, null, touchedObjectList, this::touchMethodExecuteVisitor, 0)
        }
        finally {
            touchedByVisitorList.appendTopDown(touchedObjectList)
        }
    }
    protected fun iterateTopDownSinglePass(visitor: Visitor, touchedByVisitorList: TouchedByVisitorList){
        val touchedObjectList = TouchedByVisitorList()
        try {
            iterateRecurse(visitor, null, touchedObjectList, this::touchMethodAddToList, 0)
            for (i in 0 until touchedObjectList.size()) {
                if (!continueVisiting(visitor))
                    break
                executeVisitor(visitor, touchedObjectList.get(i))
            }
        }
        finally {
            touchedByVisitorList.appendTopDown(touchedObjectList)
        }
    }
    protected fun iterateBottomUpSinglePass(visitor: Visitor, touchedByVisitorList: TouchedByVisitorList){
        val touchedObjectList = TouchedByVisitorList()
        try {
            iterateRecurse(visitor, null, touchedObjectList, this::touchMethodAddToList, 0)
            for (i in touchedObjectList.size()-1 downTo 0){
                if (!continueVisiting(visitor))
                    break
                executeVisitor(visitor, touchedObjectList.get(i))
            }
        }
        finally {
            touchedByVisitorList.appendBottomUp(touchedObjectList)
        }
    }
    internal fun intIterateAssignTouched(visitor: Visitor, touchedByVisitorList: TouchedByVisitorList){
        iterateAssignTouched(visitor, touchedByVisitorList)
    }
    protected open fun iterateAssignTouched(visitor: Visitor, touchedByVisitorList: TouchedByVisitorList){
        when (visitor.iterationStyle){
            Visitor.IterationStyle.isTopDownRecurse -> iterateTopDownRecurse(visitor, touchedByVisitorList)
            Visitor.IterationStyle.isTopDownSinglePass -> iterateTopDownSinglePass(visitor, touchedByVisitorList)
            Visitor.IterationStyle.isBottomUpSinglePass -> iterateBottomUpSinglePass(visitor, touchedByVisitorList)
            else -> throw EtiOPFProgrammerException(CErrorInvalidIterationStyle)
        }



    }
    protected open fun iterateRecurse(visitor: Visitor, derivedParent: Visited?, touchedByVisitorList: TouchedByVisitorList,
                                 touchMethod: VisitedTouchMethod, iterationDepth: Int){
        if (visitor.intVisitBranch(derivedParent, this) && checkContinueVisitingIfTopDownRecurse(visitor)){
            val theIterationDepth = iterationDepth + 1
            if (visitor.intAcceptVisitor(this))
                touchMethod.invoke(this, visitor, touchedByVisitorList, theIterationDepth)
            val classPropNames = List<String>()
            getPropertyNames(this, classPropNames, setOf(TypeKind.OBJECT))
            classPropNames.forEach {
                val candidate = getObjectProperty<Any>(this, it)
                if (candidate is Visited)
                    candidate.iterateRecurse(visitor, this, touchedByVisitorList, touchMethod, theIterationDepth)
                else if (candidate is List<*>)
                    iterateOverList(visitor, candidate, this, touchedByVisitorList, touchMethod, theIterationDepth)
            }
        }
    }
    protected fun iterateOverList(visitor: Visitor, candidates: List<*>, derivedParent: Visited,
                                  touchedByVisitorList: TouchedByVisitorList, touchMethod: VisitedTouchMethod,
                                  iterationDepth: Int){
        candidates.forEach {
            if (it is Visited)
                it.iterateRecurse(visitor, derivedParent, touchedByVisitorList, touchMethod, iterationDepth)
        }

    }
    protected fun touchMethodAddToList(candidates: Visited, visitor: Visitor, list: TouchedByVisitorList, iterationDepth: Int){
        val visitedCandidate = TouchedByVisitor(visitor, candidates, iterationDepth)
        list.add(visitedCandidate)
    }
    protected fun touchMethodExecuteVisitor(candidates: Visited, visitor: Visitor, list: TouchedByVisitorList, iterationDepth: Int){
        val visitedCandidate = TouchedByVisitor(visitor, candidates, iterationDepth)
        list.add(visitedCandidate)
        executeVisitor(visitor, visitedCandidate)
    }
    protected fun executeVisitor(visitor: Visitor, visitedCandidate: TouchedByVisitor){
        visitor.intSetVisisited(visitedCandidate.visited)
        visitor.setDepth(visitedCandidate.iterationDepth)
        visitor.execute(visitedCandidate.visited)
    }
    private val terminated: Boolean get() = GTIOPFManager().terminated
    protected fun continueVisiting(visitor: Visitor): Boolean{
        return visitor.continueVisiting && !terminated

    }
    protected open fun checkContinueVisitingIfTopDownRecurse(visitor: Visitor): Boolean{
        if (visitor.iterationStyle != Visitor.IterationStyle.isTopDownRecurse)
            return true

        return continueVisiting(visitor)
    }




}