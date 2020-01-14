package tiOPF

open class FilteredObjectList<T: BaseObject>: IFiltered, ObjectList<T> (){
    private var privCriteria: Criteria? = null
    override fun hasCriteria(): Boolean {
        return (privCriteria != null && privCriteria!!.hasCriteria)
    }

    override fun hasOrderBy(): Boolean {
        return (privCriteria != null && privCriteria!!.hasOrderBy)
    }

    override val criteria: Criteria?
        get() {
            if (privCriteria == null)
                privCriteria = Criteria()
            return privCriteria
        }


}