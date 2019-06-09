package tiOPF

interface IFiltered {
    fun hasCriteria(): Boolean
    fun hasOrderBy(): Boolean
    val criteria: Criteria?
}