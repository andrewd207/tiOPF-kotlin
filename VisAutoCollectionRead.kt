package tiOPF

import kotlin.reflect.KClass

open class VisAutoCollectionRead: VisAutoAbs() {
    private var hasParent = false
    private var classToCreate: KClass<Object>? = null
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

    override fun getWhereAttrColMaps() {
        if (visitedClassType == null)
            throw Exception("visitedClassType == null")

        GTIOPFManager().classDBMappingManager.attrColMaps.findAllMappingsByMapToClass(classDBCollection!!.perObjectAbsClass, whereAttrColMaps)
        val list: MutableList<AttrColMap> = mutableListOf()
        whereAttrColMaps.forEach {
            if (ClassDBMapRelationshipType.Foreign !in it.dbColMap.pkInfo)
                list.add(it)
        }
        whereAttrColMaps.removeAll(list)
        addToParams(where, whereAttrColMaps, visited!!)
    }

    override fun getAttrColMaps() {
        if (visitedClassType == null)
            throw Exception("visitedClassType == null")
        GTIOPFManager().classDBMappingManager.attrColMaps.findAllMappingsByMapToClass(classDBCollection!!.perObjectAbsClass, attrColMaps)
        classToCreate = attrColMaps.get(0).attrMap.ownerAsClassMap.perObjAbsClass
        hasParent = GTIOPFManager().classDBMappingManager.classMaps.hasParent(classToCreate!!)
        val list: MutableList<AttrColMap> = mutableListOf()
        if (!hasParent){
            attrColMaps.forEach {
                if (ClassDBMapRelationshipType.Foreign in it.dbColMap.pkInfo)
                    list.add(it)
            }
            attrColMaps.removeAll(list)
        }
    }

    private fun setupCriteria(){
        criteria = null
        if (classDBCollection == null)
            throw Exception("VisAutoCollectionRead.setupCriteria called before classDBCollection is assigned!")

        if (visited is IFiltered){
            val filtered = visited as IFiltered
            if (filtered.hasCriteria() || filtered.hasOrderBy()) {
                criteria = filtered.criteria
                if (criteria == null)
                    throw Exception("IFiltered.hasCriteria == true but returned null Criteria!")
                criteria!!.mapFieldNames(classDBCollection!!.perObjectAbsClass)
            }

        }



    }

    protected fun mapRowToObject(checkForDuplicates: Boolean){
        var index = 0
        fun doesOwnObjects(data: Object): Boolean{
            var result = data.getPropCount(setOf(TypeKind.OBJECT)) > 0
            if (!result)
                return result

            val list = List<Object>()
            data.findAllByClassType(Object::class, list as List<Visited>)
            return list.find { (it as Object).owner == data}  != null
        }
        fun duplicateObject(): Boolean{
            var pkColName = ""
            val colMap = attrColMaps.find { ClassDBMapRelationshipType.Primary in it.dbColMap.pkInfo }

            pkColName = colMap?.dbColMap!!.colName
            assert(pkColName.isNotEmpty(), {"Can not determine primary key column. attrColMaps.size != 1"})
            val oid = GTIOPFManager().run { defaultOIDGenerator.createOIDInstance() }
            val data = (visited as ObjectList<BaseObject>).find(oid)
            var result = data != null
            index = if (result)
                (visited as ObjectList<BaseObject>).indexOf(data)
            else
                -1

            return result
        }

        if (attrColMaps.size == 0)
            return
        val list = visited as ObjectList<Object>
        var dataNew: Object
        if (checkForDuplicates && duplicateObject(/*assigns index*/)){
            val dataOld = list[index]
            dataNew = classToCreate!!.createInstance()
            dataNew.assign(dataOld)
            index = list.indexOf(dataOld)
            list.add(index, dataNew)
            list.remove(dataOld)
        }
        else {
            dataNew = classToCreate!!.createInstance()
            list.add(dataNew)
        }

        queryResultToObject(dataNew, attrColMaps)
        if (GTIOPFManager().classDBMappingManager.collections.isCollection(classToCreate!!) || doesOwnObjects(dataNew))
            dataNew.objectState = Object.PerObjectState.PK
        else
            dataNew.objectState = Object.PerObjectState.Clean

        if (hasParent)
            classesWithParent.add(dataNew)
    }
    protected open fun setContinueVisiting(){
        //do nothing
    }

}