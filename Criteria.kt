package tiOPF
// complete
import kotlin.reflect.KClass

enum class CriteriaType{
    AND,
    OR,
    NONE
}

fun tiCriteriaAsSQL(visited: Object, withComments: Boolean = false): String{
    return tiCriteriaAsSQL(visited, null, withComments)
}
fun tiCriteriaAsSQL(visited: Object, params: QueryParams?, withComments: Boolean = false): String{
    assert(visited is Criteria, {"Criteria subtype required"})

    if (!(visited as Criteria).hasCriteria)
        return ""
    var result = ""

    val visitor = VisObjectToSQL(withComments)
    visitor.params = params
    visited.iterate(visitor)

    result = visitor.text

    if (visited.isEmbraced && result.isNotEmpty())
        result = "($result)\n"
    else
        result += "\n"

    return result
}

fun tiCriteriaOrderByAsSQL(visited: Object): String {
    assert(visited is Criteria,{"Criteria subtype required"})

    if (! (visited as Criteria).hasOrderBy)
        return ""
    val cVisited = visited as Criteria
    val orderByList = cVisited.orderByList

    fun orderText(col: Int): String{
        return if (orderByList[col].ascending)
            orderByList[col].fieldName
        else
            orderByList[col].fieldName + " DESC"
    }

    var result = " ORDER BY ${orderText(0)}"
    for (i in 1 until orderByList.count()){
        result += ", ${orderText(i)}"
    }
    result += "\n"

    return result
}

class CriteriaList: ObjectList<Criteria>(){
    val ownerAsCriteria: Criteria get() = owner as Criteria
}

open class Criteria(name: String = ""): Object() {
    @Published val criteriaList = CriteriaList()
    @Published val selectionCriteriaList = SelectionCriteriaList()
    @Published val name: String get() = privName
    private var privName = name
    val groupByList = Columns()
    val orderByList = Columns()
    private val criteriaAttrColMaps= AttrColMaps()
    var criteriaType = CriteriaType.NONE
    private var privIsEmbraced = false
    val isEmbraced: Boolean
        get() = privIsEmbraced
    val hasCriteria: Boolean get () { return criteriaList.size > 0 || selectionCriteriaList.size > 0}
    val hasOrderBy: Boolean get () { return orderByList.isNotEmpty()}

    override fun assignClassProps(source: Object) {
        assert(source is Criteria, { CTIErrorInvalidObject })

        groupByList.assign((source as Criteria).groupByList)
        orderByList.assign(source.orderByList)
        criteriaAttrColMaps.assign(source.criteriaAttrColMaps)
        criteriaList.assign(source.criteriaList)
        selectionCriteriaList.assign(source.selectionCriteriaList)
    }

    override fun assignPublicProps(source: Object) {
        assert(source is Criteria, { CTIErrorInvalidObject })
        super.assignPublicProps(source)
        criteriaType = (source as Criteria).criteriaType
        privIsEmbraced = source.isEmbraced
        privName = source.name
    }
    fun mapFieldNames(kClass: KClass<*>){
        val maps = criteriaAttrColMaps
        GTIOPFManager().classDBMappingManager.attrColMaps.findAllMappingsByMapToClass(kClass, maps)
        val visProAttributeToFieldName = VisProAttributeToFieldName(maps, kClass)
        iterate(visProAttributeToFieldName)

        orderByList.forEach {
            val map = maps.findByClassAttrMap(kClass, it.name)
            if (map != null)
                it.fieldName = map.dbColMap.colName
        }
    }
    fun addAndCriteria(criteria: Criteria){
        privIsEmbraced = true
        criteria.criteriaType = CriteriaType.AND
        criteriaList.add(criteria)
    }
    fun addBetween(attribute: String, value1: Any, value2: Any){
        val criteria = BetweenCriteria(attribute, value1, value2)
        selectionCriteriaList.add(criteria)
    }
    fun addEqualTo(attribute: String, value: Any){
        val data = EqualToCriteria(attribute, value)
        selectionCriteriaList.add(data)
    }
    fun addExists(subQuery: String){
        val data = ExistsCriteria(subQuery)
        selectionCriteriaList.add(data)
    }
    fun addGreaterOrEqualThan(attribute: String, value: Any){
        val data = LessThanCriteria(attribute, value, true)
        selectionCriteriaList.add(data)
    }
    fun addGreaterThan(attribute: String, value: Any){
        val data = GreaterThanCriteria(attribute, value)
        selectionCriteriaList.add(data)
    }
    fun addGroupBy(field: String){
        assert(field.isNotEmpty(), { "field is blank!"})
        val data = Column()
        data.name = field
        groupByList.add(data)
    }
    fun addGroupBy(fields: Array<String>){
        fields.forEach {
            addGroupBy(it)
        }
    }
    fun addIn(attribute: String, subQuery: String){
        val data = InCriteria(attribute, subQuery)
        selectionCriteriaList.add(data)
    }
    fun addIn(attribute: String, valueArray: Array<Any>){
        val data = InCriteria(attribute, "")
        data.valueArray = valueArray.clone()
        selectionCriteriaList.add(data)
    }
    fun addLessOrEqualThan(attribute: String, value: Any){
        val data = GreaterThanCriteria(attribute, value, true)
        selectionCriteriaList.add(data)
    }
    fun addLessThan(attribute: String, value: Any){
        val data = LessThanCriteria(attribute, value)
        selectionCriteriaList.add(data)
    }
    fun addLike(attribute: String, value: String){
        val data = LikeCriteria(attribute, value)
        selectionCriteriaList.add(data)
    }
    fun addNotEqualTo(attribute: String, value: Any){
        val data = EqualToCriteria(attribute, value, true)
        selectionCriteriaList.add(data)
    }
    fun addNotExists(subQuery: String){
        val data = ExistsCriteria(subQuery, true)
        selectionCriteriaList.add(data)
    }

    fun addNotIn(attribute: String, subQuery: String){
        val data = InCriteria(attribute, subQuery, true)
        selectionCriteriaList.add(data)
    }
    fun addNotIn(attribute: String, valueArray: Array<Any>){
        val data = InCriteria(attribute, "", true)
        data.valueArray = valueArray.clone()
        selectionCriteriaList.add(data)
    }
    fun addNotIn(attribute: String, objectList: ObjectList<*>, fieldName: String){
        if (objectList.size == 0)
            return

        val list: MutableList<Any> = mutableListOf()

        @Suppress("UNCHECKED_CAST")
        (objectList as ObjectList<Object>).forEach {
            if (it.objectState != PerObjectState.Delete){
                list.add(it.getPropValue<Any>(fieldName)!!)
            }
        }
        val data = InCriteria(attribute, "", true)
        data.valueArray = list.toTypedArray()

        selectionCriteriaList.add(data)
    }

    fun addNotLike(attribute: String, value: String){
        val data = LikeCriteria(attribute, value, true)
        selectionCriteriaList.add(data)
    }
    fun addNotNull(attribute: String){
        val data = NullCriteria(attribute, true)
        selectionCriteriaList.add(data)
    }
    fun addNull(attribute: String){
        val data = NullCriteria(attribute)
        selectionCriteriaList.add(data)
    }
    fun addOrCriteria(criteria: Criteria){
        privIsEmbraced = true
        criteria.criteriaType = CriteriaType.OR
        criteriaList.add(criteria)
    }
    fun addOrderBy(field: String, sortOrderAscending: Boolean = true){
        assert(field.isNotEmpty(), {"field is blank!"})
        val data = Column()
        data.ascending = sortOrderAscending
        data.name = field
        orderByList.add(data)
    }
    fun addOrderBy(fields: Array<String>, sortOrderAscending: Boolean = true){
        fields.forEach {
            addOrderBy(it, sortOrderAscending)
        }
    }
    fun addOrderByAscending(field: String){
        addOrderBy(field, true)
    }
    fun addOrderByAscending(fields: Array<String>) {
        addOrderBy(fields, true)
    }
    fun addOrderByDescending(field: String){
        addOrderBy(field, false)
    }
    fun addOrderByDescending(fields: Array<String>){
        addOrderBy(fields, false)
    }
    fun addSQL(sqlStatement: String){
        val data = SQLCriteria(sqlStatement)
        selectionCriteriaList.add(data)
    }

    fun clearAll(){
        criteriaList.clear()
        selectionCriteriaList.clear()
        orderByList.clear()
        groupByList.clear()
    }

    internal fun asSql(params: QueryParams, withComments: Boolean = false): String{
        var result: String
        if (!hasCriteria)
            return ""
        val visitor = VisObjectToSQL(withComments)
        visitor.params = params
        iterate(visitor)
        result = visitor.text
        if (isEmbraced && result.isNotEmpty())
            result = "($result)"
        else
            result += "\n"
        return  result
    }
    internal fun orderByAsSQL(): String{
        fun orderText(columnId: Int): String{
            if (orderByList[columnId].ascending)
                return orderByList[columnId].fieldName
            return orderByList[columnId].fieldName+ " DESC"

        }
        var result = ""
        if (!hasOrderBy)
            return result
        result = " ORDER BY "+ orderText(0)
        for (i in 1 until orderByList.size)
            result += ", "+ orderText(i)
        result+= "\n"
        return result
    }

    init {
        criteriaList.owner = this
        criteriaList.itemOwner = this
        criteriaList.autosetItemOwner = false

        selectionCriteriaList.owner = this
        selectionCriteriaList.ownsObjects = true

        groupByList.owner = this
        groupByList.ownsObjects = true

        orderByList.owner = this
        orderByList.ownsObjects = true

        criteriaAttrColMaps.owner = this
        criteriaAttrColMaps.ownsObjects = true
        criteriaAttrColMaps.autosetItemOwner = false
    }


}

class SelectionCriteriaList: ObjectList<SelectionCriteriaAbs>(){
    val ownerAsCriteria: Criteria get() = owner as Criteria
}

abstract class SelectionCriteriaAbs(@Published var attribute: String, @Published  var value: Any, var isNegative: Boolean = false, fieldName: String = ""): Object(){
    abstract fun getClause(): String
    val ownerAsCriteria: Criteria get() = owner as Criteria
    var fieldName: String = fieldName ; get() {
            if (field.isEmpty())
                return attribute
            return field
        }
    override fun assignPublicProps(source: Object){
        assert(source is SelectionCriteriaAbs, { CTIErrorInvalidObject })
        source as SelectionCriteriaAbs
        super.assignPublicProps(source)
        fieldName = source.fieldName
        attribute = source.attribute
        isNegative = source.isNegative
        value = source.value
    }

    protected open fun toSelectClause(params: QueryParams? = null, paramNo: ValueReference<Int>? = null): String{
        // do nothing. not all classes will implement this
        var result: String
        if (params != null && paramNo != null){
            result = fieldName + getClause() + getParamName(paramNo.value, true)
            params.setParamAsString(getParamName(paramNo.value, false), value.toString())
            paramNo.value++
        }
        else
            result = fieldName+getClause()+ getSQLValue(value)
        return result
    }
    internal fun intToSelectClause(params: QueryParams? = null, paramNo: ValueReference<Int>? = null): String{
        return toSelectClause(params, paramNo)
    }
}

class Column: Object(){
    var ascending = false
    var name = ""
    var fieldName: String = ""
        get() {
            if (field.isEmpty())
                return name
            return field
        }
}

class Columns: ObjectList<Column>(){
    val ownerAsCriteria: Criteria get() = owner as Criteria
    fun copyReferences(source: Columns){
        addAll(source)
    }
}

abstract class ValueCriteriaAbs(attribute: String, value: Any, isNegative: Boolean = false, fieldName: String = ""): SelectionCriteriaAbs(attribute, value, isNegative, fieldName)
abstract class FieldCriteriaAbs(attribute: String, value: Any, isNegative: Boolean = false, fieldName: String = ""): SelectionCriteriaAbs(attribute, value, isNegative, fieldName){
    override fun toSelectClause(params: QueryParams?, paramNo: ValueReference<Int>?): String {
        return fieldName + getClause() + getSQLValue(value)
    }
}

class EqualToCriteria(attribute: String, value: Any, isNegative: Boolean = false, fieldName: String = ""): ValueCriteriaAbs(attribute, value, isNegative, fieldName){
    override fun getClause(): String {
        if (isNegative)
            return " <> "
        return " = "
    }
}
class EqualToFieldCriteria(attribute: String, value: Any, isNegative: Boolean, fieldName: String = ""): FieldCriteriaAbs(attribute, value, isNegative, fieldName){
    override fun getClause(): String {
        if (isNegative)
            return " <> "
        return " = "
    }
}
class ExistsCriteria(subQuery: String, isNegative: Boolean = false, fieldName: String = ""): ValueCriteriaAbs("", subQuery, isNegative, fieldName){
    override fun getClause(): String {
        if (isNegative)
            return " NOT EXISTS "
        return " EXISTS "

    }

    override fun toSelectClause(params: QueryParams?, paramNo: ValueReference<Int>?): String {
        return getClause() + "("+value+")"
    }
}
class GreaterThanCriteria(attribute: String, value: Any, isNegative: Boolean = false, fieldName: String = ""): ValueCriteriaAbs(attribute, value, isNegative, fieldName){
    override fun getClause(): String {
        if (isNegative)
            return " <= "
        return " > "
    }
}
class GreaterThanFieldCriteria(attribute: String, value: Any, isNegative: Boolean, fieldName: String = ""): FieldCriteriaAbs(attribute, value, isNegative, fieldName){
    override fun getClause(): String {
        if (isNegative)
            return " <= "
        return " > "
    }
}
class InCriteria(attribute: String, value: Any, isNegative: Boolean = false, fieldName: String = ""): ValueCriteriaAbs(attribute, value, isNegative, fieldName){
    override fun getClause(): String {
        if (isNegative)
            return " NOT IN "
        return " IN "
    }
    var valueArray: Array<Any> = arrayOf()
    override fun toSelectClause(params: QueryParams?, paramNo: ValueReference<Int>?): String {
        var result: String
        var sep = ""
        if (valueArray.isNotEmpty() ){
            result = fieldName + getClause() + "("
            valueArray.forEach {
                if (params != null){
                    result += sep + getParamName(paramNo!!.value, true)
                    params.setParamAsString(getParamName(paramNo.value, false), it.toString())
                    paramNo.value++
                }
                else {
                    result += sep + getSQLValue(it)
                }
                sep = ", "
            }
            result += ")"
        }
        else {
            result = fieldName + getClause() + "(" + value + ")"
        }
        return result
    }

}
class LessThanCriteria(attribute: String, value: Any, isNegative: Boolean = false, fieldName: String = ""): ValueCriteriaAbs(attribute, value, isNegative, fieldName){
    override fun getClause(): String {
        if (isNegative)
            return " >= "
        return " < "
    }
}
class LessThanFieldCriteria(attribute: String, value: Any, isNegative: Boolean = false, fieldName: String = ""): FieldCriteriaAbs(attribute, value, isNegative, fieldName){
    override fun getClause(): String {
        if (isNegative)
            return " >= "
        return " < "
    }
}
class LikeCriteria(attribute: String, value: Any, isNegative: Boolean = false, fieldName: String = ""): ValueCriteriaAbs(attribute, value, isNegative, fieldName){
    override fun getClause(): String {
        if (isNegative)
            return " NOT LIKE "
        return " LIKE "
    }
}
class NullCriteria(attribute: String, isNegative: Boolean = false, fieldName: String = ""): ValueCriteriaAbs(attribute, Any(), isNegative, fieldName){
    override fun getClause(): String {
        if (isNegative)
            return " IS NOT NULL "
        return " IS NULL "
    }

    override fun toSelectClause(params: QueryParams?, paramNo: ValueReference<Int>?): String {
        return fieldName + getClause()
    }
}
class BetweenCriteria(attribute: String, value1: Any, val value2: Any, isNegative: Boolean = false, fieldName: String = ""):
    ValueCriteriaAbs(attribute, value1, isNegative, fieldName){
    override fun getClause(): String {
        if (isNegative)
            return " NOT BETWEEN "
        return " BETWEEN "
    }

    override fun toSelectClause(params: QueryParams?, paramNo: ValueReference<Int>?): String {
        var result: String
        if (params != null){
            result = fieldName+getClause()+ getParamName(paramNo!!.value, true)
                    " AND " + getParamName(paramNo.value+1, true )
            params.setValueAsString(getParamName(paramNo.value, false), value.toString())
            params.setValueAsString(getParamName(paramNo.value+1, false), value2.toString())
            paramNo.value+=2
        }
        else {
            result = fieldName + getClause() + getSQLValue(value) + " AND " + getSQLValue(value2)
        }
        return result
    }

}
class SQLCriteria(sqlStatement: String): SelectionCriteriaAbs(sqlStatement, "", false, ""){
    override fun getClause(): String {
        return attribute
    }

    override fun toSelectClause(params: QueryParams?, paramNo: ValueReference<Int>?): String {
        return getClause()
    }
}