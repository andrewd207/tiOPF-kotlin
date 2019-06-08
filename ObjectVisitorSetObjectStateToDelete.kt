package tiOPF

class ObjectVisitorSetObjectStateToDelete: Visitor() {
    override fun acceptVisitor(visited: Visited): Boolean {
        assert(visited.testValid(Object::class), { CTIErrorInvalidObject})
        val lVisited = visited as Object
        val result = lVisited.objectState != Object.PerObjectState.Deleted

        LOG(arrayOf(this::class.simpleName, visited::class.simpleName, result), Log.LogSeverity.lsAcceptVisitor)
        return result
    }

    override fun visitBranch(derivedParent: Visited?, visited: Visited): Boolean {
        assert(testValid(derivedParent, Object::class, true), { CTIErrorInvalidObject})
        assert(visited.testValid(Object::class), {CTIErrorInvalidObject})
        val lVisited = visited as Object
        return derivedParent == null || derivedParent == lVisited.owner
    }
}