package tiOPF

class VisAutoCollectionRead: VisAutoAbs() {
    private var classDBCollection: ClassDBCollection? = null
    private var criteria: Criteria? = null
    private val classesWithParent = List<Any>()
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
            val item = it
            assert(visitedClassType != null, {"visitedClassType == null"})
            GTIOPFManager().classDBMappingManager.attrColMaps.findAllMappingsByMapToClass(
                classDBCollection!!.perObjectAbsClass, whereAttrColMaps)
            whereAttrColMaps.forEach{
                if (ClassDBMapRelationshipType.Foreign !in it.dbColMap.pkInfo){
                    addToParams(where, whereAttrColMaps, item)
                }

            }

            val tableName = whereAttrColMaps.tableName()
            assert(tableName.isNotEmpty(), { "Unable to find table name. whereAttrColMaps.Count = " +
                    "${whereAttrColMaps.size} + '. Suspect a missing [ClassDBMapRelationshipType.Foreign] value " +
                    "in the child classes RegisterMapping calls." })
            query!!.selectRow(tableName, where)
            var count = 0
            while (!query!!.eof){
                mapRowToObject(true)
                query!!.next()
                count++
            }
            query!!.close()
            if (count > 1)
                throw Exception(CErrorQueryReturnedMoreThanOneRow.format(count))
        }
    }

    private fun setupCriteria(){
        criteria = null

        if (visited is IFiltered){
            val filtered = visited as IFiltered
            if (filtered.hasCriteria() || filtered.hasOrderBy()) {
                criteria = filtered.criteria
                criteria.map
            }

        }



    }

    protected fun mapRowToObject(checkForDuplicates: Boolean){

    }

}