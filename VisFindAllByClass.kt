package tiOPF

import kotlin.reflect.KClass

class VisFindAllByClass(var classTypeToFind: KClass<*>, var list: List<Visited>): Visitor() {
    override fun execute(visited: Visited?) {
        super.execute(visited)
        if (!acceptVisitor())
            return
        list.add(visited!!)
    }

    override fun acceptVisitor(): Boolean {
        return classTypeToFind.isInstance(visited)
    }
}