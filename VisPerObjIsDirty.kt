package tiOPF
// complete

class VisPerObjIsDirty(): Visitor() {
    var dirty: Boolean = false
    override fun acceptVisitor(): Boolean {
        return (visited is Object || visited is ObjectList<*>) && !dirty
    }

    override fun execute(visited: Visited?) {
        super.execute(visited)
        if (!acceptVisitor())
            return

        if (visited is Object)
            dirty = visited.objectState in arrayOf(
                    Object.PerObjectState.Create,
                    Object.PerObjectState.Update,
                    Object.PerObjectState.Delete
                    )
        else
            throw Exception("Invalid visited type")
    }
}