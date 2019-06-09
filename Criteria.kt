package tiOPF

import kotlin.reflect.KClass

enum class CriteriaType{
    AND,
    OR,
    NONE
}

class CriteriaList: ObjectList<Criteria>(){
    val ownerAsCriteria: Criteria get() = owner as Criteria
}

class Criteria(name: String = ""): Object() {
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
    fun mapFieldNames(kClass: KClass<Object>){
        val maps = criteriaAttrColMaps
        GTIOPFManager().classDBMappingManager.attrColMaps.findAllMappingsByMapToClass(kClass, maps)
        val visProAttributeToFieldName = VisProAttributeToFieldName(maps, kClass)
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
        assert(testValid(source as SelectionCriteriaAbs, SelectionCriteriaAbs::class), { CTIErrorInvalidObject })
        super.assignPublicProps(source)
        fieldName = source.fieldName
        attribute = source.attribute
        isNegative = source.isNegative
        value = source.value
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