package tiOPF

class VisAutoCollectionRead: VisAutoAbs() {
    private var classDBCollection: ClassDBCollection? = null
    private var criteria: Criteria? = null
    private fun readDataForParentClass(collection: ClassDBCollection){
        classDBCollection = collection
        setupParams()
        setupCriteria()

        if (criteria == null)
            query!!.selectRow(attrColMaps.tableName(), where, criteria)
        else
            query!!.selectRow(attrColMaps.tableName(), where)

        while (!query!!.eof){
            mapRowToObject(false)
            query!!.next()
        }

        query!!.close()
    }
    private fun readDataForChildClasses(collection: ClassDBCollection){
        classDBCollection = collection
        getAttrColMaps()
        (visited as ObjectList<Object>).forEach{
            assert(visitedClassType != null, {"visitedClassType == null"})
            GTIOPFManager().classDBMappingManager.attr
        }
    }

    private fun setupCriteria(){

    }

    protected fun mapRowToObject(checkForDuplicates: Boolean){

    }

}