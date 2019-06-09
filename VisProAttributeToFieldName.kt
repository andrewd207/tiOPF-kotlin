package tiOPF
// complete
import kotlin.reflect.KClass

class VisProAttributeToFieldName(private val attrColMaps: AttrColMaps, private val classType: KClass<Object>): Visitor() {
    override fun acceptVisitor(): Boolean {
        return visited is SelectionCriteriaAbs
    }

    override fun execute(visited: Visited) {
        super.execute(visited)
        if (!acceptVisitor())
            return

        val criteria = visited as SelectionCriteriaAbs
        val map = attrColMaps.findByClassAttrMap(classType, criteria.attribute)
        if (map != null)
            criteria.fieldName = map.dbColMap.colName
    }
}