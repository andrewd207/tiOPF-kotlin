package tiOPF

import tiOPF.Log.LOG
import tiOPF.Log.LogSeverity

// complete
class VisObjectToSQL(private val withComments: Boolean = false): VisStringStream() {
    private val groupByList = Columns()
    override fun acceptVisitor(): Boolean {
        val result = visited is Criteria && !(visited as Object).deleted
        LOG(
            arrayOf(className(), visited?.className(), result),
            LogSeverity.AcceptVisitor
        )
        return result
    }

    override fun execute(visited: Visited?) {
        super.execute(visited)
        if (!acceptVisitor())
            return
        if ((visited as Criteria).selectionCriteriaList.size == 0)
            return
        if (stream.size == 0)
            write("(")
        else
            when (visited.criteriaType) {
                CriteriaType.AND -> write(" AND \n(")
                CriteriaType.OR -> write(" OR \n(")
                CriteriaType.NONE -> write("(")
            }
        write(asSQLClause(visited.selectionCriteriaList))
        write(")")

        if (withComments)
            write(" /* " + visited.name + " */ ")
        groupByList.copyReferences(visited.groupByList)
    }
    fun groupByClausesAsText(): String{
        if (groupByList.size == 0)
            return ""
        var result = ""
        groupByList.forEach {
            if (result.isEmpty())
                result = " GROUP BY "+ it.name
            else
                result += ", " + it.name
        }
        return result
    }
    private var currentParam = ValueReference(1)
    fun asSQLClause(criteriaList: SelectionCriteriaList): String{
        if (criteriaList.isEmpty())
            return ""
        var result = ""
        var appendBegin = ""
        var appendEnd = ""
        fun include(clause: String){
            result += appendBegin + clause + appendEnd
        }
        include(criteriaList[0].intToSelectClause(params, currentParam))
        if (criteriaList.size == 1)
            return result
        result = "($result)"
        appendBegin = " AND ("
        appendEnd =")"

        for (i in 1 until criteriaList.size){
            include(criteriaList[i].intToSelectClause(params, currentParam))
        }
        return result
    }
    var params: QueryParams? = null
}