package tiOPF
// complete
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf

abstract class VisAutoAbs: ObjectVisitor() {
    protected val whereAttrColMaps = AttrColMaps()
    protected val where = QueryParams()
    protected val attrColMaps = AttrColMaps()
    protected val params = QueryParams()

    protected var visitedClassType: KClass<*>? = null
    override fun execute(visited: Visited?) {
        super.execute(visited)
        visitedClassType = visited!!::class as KClass<Object>

    }

    protected fun addToParams(params: QueryParams, attrColMaps: AttrColMaps, data: Object){
        fun _setOIDParam(colname: String, propName: String){
            if (propName.contains("owner", true)){
                if (this is VisAutoUpdateAbs) {
                    assert(data.owner != null, {"Attempting to read a collection but the collections's Visited.Owner is not assigned"})
                    data.owner!!.oid.assignToQueryParam(colname, params)
                }
                else {
                    if (!data.oid.isNull())
                        data.oid.assignToQueryParam(colname, params)
                }
            }
        }
        assert(visitedClassType != null, {"visitedClassType == null"})
        params.clear()
        attrColMaps.forEach {
            val colName = it.dbColMap.colName
            val propName = it.attrMap.attrName
            if ((propName.equals("oid", true) || propName.equals("owner.oid", true))
                && propName.contains("_oid", true))
                _setOIDParam(colName, propName)
            else if (getPropertyInheritsFrom(data, propName, OID::class)){
                val oid: OID? = getObjectProperty(data, propName)
                if (oid != null){
                    oid.assignToQueryParam(colName, params)

                }
            }
            else
                params.setValueFromProp(data, propName, colName)
        }
    }
    protected abstract fun getWhereAttrColMaps()
    protected abstract fun getAttrColMaps()
    override fun setupParams() {
        getWhereAttrColMaps()
        getAttrColMaps()
    }
    protected fun paramsToString(params: QueryParams): String{
        var result: String = ""
        params.forEach {
            result += it.name + " = " + it.valueAsString+", "
        }

        return result
    }
    protected fun queryResultToObject(target: Object, attrColMaps: AttrColMaps){
        fun _setPropValue(attrColMap: AttrColMap){
            val colName = attrColMap.dbColMap.colName
            val propName = attrColMap.attrMap.attrName

            if (propName.equals("OID", true)){
                target.oid.assignFromQuery(colName, query!!)
            }

            if (getPropertyInheritsFrom(target, propName, OID::class)){
                val lOID = getObjectProperty<OID>(target, propName)
                if (lOID != null){
                    lOID.assignFromQuery(colName, query!!)
                    return
                }
            }

            val fieldKind = query!!.fieldKind(query!!.fieldIndex(colName))
            if (target.isReadWriteProp(propName) || fieldKind == Query.QueryFieldKind.Binary){
                when (fieldKind) {
                    Query.QueryFieldKind.String,
                    Query.QueryFieldKind.LongString -> {
                        val string = query!!.getFieldAsString(colName)
                        /*if (string.toUpperCase() in arrayOf("T", "F") && getPropertyType(target, propName) == TypeKind.BOOLEAN)
                            setObjectProperty(target, propName, string.toUpperCase() === "T")*/
                        setObjectProperty(target, propName, string)
                    }
                    Query.QueryFieldKind.Integer -> {
                        when (getPropertyType(target, propName)) {
                            TypeKind.LONG -> setObjectProperty(target, propName, query!!.getFieldAsInteger(propName))
                            TypeKind.INT -> setObjectProperty(target, propName, query!!.getFieldAsInteger(propName).toInt())
                        }
                    }
                    Query.QueryFieldKind.Float ->  setObjectProperty(target, propName, query!!.getFieldAsFloat(propName))
                    Query.QueryFieldKind.DateTime ->  setObjectProperty(target, propName, query!!.getFieldAsDate(propName))
                    Query.QueryFieldKind.Logical ->  setObjectProperty(target, propName, query!!.getFieldAsBoolean(propName))
                    Query.QueryFieldKind.Binary -> {
                        val value = ValueOut<ByteArray>()
                        query!!.assignFieldAsByteArray(propName, value)
                        setObjectProperty(target, propName, value.value)
                    }
                    else -> throw Exception(CErrorInvalidQueryFieldKind)

                }
            }

        }

        attrColMaps.forEach { _setPropValue((it)) }
    }
}

open class VisAutoReadThis: VisAutoAbs(){
    protected var setObjectState: Boolean = false
    override fun getAttrColMaps() {
        assert(visitedClassType != null, {"visitedClassType == null!"})
        GTIOPFManager().classDBMappingManager.attrColMaps.findAllMappingsByMapToClass(visitedClassType!!, attrColMaps)
        val list = List<AttrColMap>()
        attrColMaps.forEach {
            if (ClassDBMapRelationshipType.Foreign in it.dbColMap.pkInfo)
                list.add(it)
        }
        attrColMaps.removeAll(list)
    }

    override fun getWhereAttrColMaps() {
        assert(visitedClassType != null, {"visitedClassType == null!"})
        GTIOPFManager().classDBMappingManager.attrColMaps.findAllMappingsByMapToClass(visitedClassType!!, whereAttrColMaps)
        addToParams(where, whereAttrColMaps, visited as Object)
    }

    override fun acceptVisitor(): Boolean {
        if (visited != null) {
            val kClass: KClass<*> = visited!!::class
            return visited!!.objectState in setOf(Object.PerObjectState.Empty, Object.PerObjectState.PK)
                && GTIOPFManager().classDBMappingManager.classMaps.isClassRegistered(kClass as KClass<Object>)
        }
        return false
    }

    override fun final(visited: Object) {
        if (setObjectState) {
            assert(visited.objectState in arrayOf(Object.PerObjectState.Empty, Object.PerObjectState.PK),
                {"Object state on " + visited.className() + " not Empty or PK it's "+visited.objectState.toString()})
            if (GTIOPFManager().classDBMappingManager.collections.isCollection(visited::class))
                visited.objectState = Object.PerObjectState.PK
            else
                visited.objectState = Object.PerObjectState.Empty
        }
    }

    override fun execute(visited: Visited?) {
        super.execute(visited)
        if (!acceptVisitor())
            return

        setObjectState = false
        val classMaps = ClassMaps()
        classMaps.ownsObjects = false
        GTIOPFManager().classDBMappingManager.classMaps.findAllParents(visitedClassType!!, classMaps)
        classMaps.forEach {
            visitedClassType = it.perObjAbsClass
            doExecute()
        }
    }
    protected fun doExecute(){
        setupParams()
        val tableName = whereAttrColMaps.tableName()
        query!!.selectRow(tableName, where)
        try {
            var count = 0
            while (!query!!.eof){
                if (count > 0)
                    throw Exception("Query returned more than one row")
                mapRowToObject()
                query!!.next()
                count++
            }

        }
        finally {
            query!!.close()
        }
    }
    protected fun mapRowToObject(){
        assert(visitedClassType != null, {"visitedClassType == null!"})
        queryResultToObject(visited!!, attrColMaps)
        setObjectState = true
    }
}
open class VisAutoCollectionRead: VisAutoAbs() {
    private var hasParent = false
    private var classToCreate: KClass<*>? = null
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
        if (!classToCreate!!.isSubclassOf(Object::class))
            throw Exception("attempt to createInstance of unsupported type ")
        if (checkForDuplicates && duplicateObject(/*assigns index*/)){
            val dataOld = list[index]
            dataNew = classToCreate!!.createInstance() as Object
            dataNew.assign(dataOld)
            index = list.indexOf(dataOld)
            list.add(index, dataNew)
            list.remove(dataOld)
        }
        else {
            dataNew = classToCreate!!.createInstance() as Object
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
class VisAutoCollectionPKRead: VisAutoCollectionRead(){
    override fun getAttrColMaps() {
        super.getAttrColMaps()
        val list = List<AttrColMap>()
        attrColMaps.forEach {
            if (ClassDBMapRelationshipType.Primary !in it.dbColMap.pkInfo
              && ClassDBMapRelationshipType.Readable !in it.dbColMap.pkInfo)
                list.add(it)
        }
        attrColMaps.retainAll(list)
    }

    override fun setContinueVisiting() {
        continueVisiting = false
    }
}
abstract class VisAutoUpdateAbs: VisAutoAbs(){
    override fun getWhereAttrColMaps() {
        assert(visitedClassType != null, { "visitedClassType == null"})
        GTIOPFManager().classDBMappingManager.attrColMaps.findAllMappingsByMapToClass(visitedClassType!!, whereAttrColMaps)
        val list = List<AttrColMap>()
        whereAttrColMaps.forEach {
            if (ClassDBMapRelationshipType.Primary !in it.dbColMap.pkInfo)
                list.add(it)
        }
        whereAttrColMaps.removeAll(list)
        addToParams(where, whereAttrColMaps, visited!!)
    }
    override fun getAttrColMaps() {
        assert(visitedClassType != null, { "visitedClassType == null"})
        GTIOPFManager().classDBMappingManager.attrColMaps.findAllMappingsByMapToClass(visitedClassType!!, attrColMaps)
        val list = List<AttrColMap>()
        attrColMaps.forEach {
            if (ClassDBMapRelationshipType.Primary in it.dbColMap.pkInfo || ClassDBMapRelationshipType.Foreign in it.dbColMap.pkInfo)
                list.add(it)
        }
        attrColMaps.removeAll(list)
        addToParams(params, attrColMaps, visited!!)
    }
    abstract fun doExecuteQuery()
    override fun execute(visited: Visited?) {
        super.execute(visited)
        if (!acceptVisitor())
            return

        val classMaps = ClassMaps()
        classMaps.ownsObjects = true
        GTIOPFManager().classDBMappingManager.classMaps.findAllParents(visitedClassType!!, classMaps)
        if (iterationStyle == IterationStyle.isTopDownRecurse)
            classMaps.forEach {
                visitedClassType = it.perObjAbsClass
                setupParams()
                doExecuteQuery()
            }
        else
            classMaps.asReversed().forEach {
                visitedClassType = it.perObjAbsClass
                setupParams()
                doExecuteQuery()
            }
    }
}

class VisAutoDelete: VisAutoUpdateAbs(){
    override fun getAttrColMaps() {
        // delete has no attrColMaps
    }
    override fun acceptVisitor(): Boolean {
        val kClass: KClass<*> = visited!!::class
        return visited!!.objectState == Object.PerObjectState.Delete
                && GTIOPFManager().classDBMappingManager.classMaps.isClassRegistered(kClass as KClass<Object>)
    }
    override fun doExecuteQuery() {
        query!!.deleteRow(whereAttrColMaps.tableName(), where)
    }
    init { iterationStyle = IterationStyle.isBottomUpSinglePass }

}

class VisAutoUpdate: VisAutoUpdateAbs(){
    override fun acceptVisitor(): Boolean {
        val kClass: KClass<*> = visited!!::class
        return visited!!.objectState == Object.PerObjectState.Update
                && GTIOPFManager().classDBMappingManager.classMaps.isClassRegistered(kClass as KClass<Object>)
    }
    override fun doExecuteQuery() {
        query!!.updateRow(whereAttrColMaps.tableName(), params, where)
    }
    init { iterationStyle = IterationStyle.isBottomUpSinglePass }
}

class VisAutoCreate: VisAutoUpdateAbs(){
    override fun getWhereAttrColMaps() {
        // do nothing
    }

    override fun getAttrColMaps() {
        assert(visitedClassType != null, { "visitedClassType == null"})
        GTIOPFManager().classDBMappingManager.attrColMaps.findAllMappingsByMapToClass(visitedClassType!!, attrColMaps)
        addToParams(params, attrColMaps, visited!!)

    }
    override fun acceptVisitor(): Boolean {
        val kClass: KClass<*> = visited!!::class
        return visited!!.objectState == Object.PerObjectState.Create
                && GTIOPFManager().classDBMappingManager.classMaps.isClassRegistered(kClass as KClass<Object>)
    }
    override fun doExecuteQuery() {
        query!!.insertRow(attrColMaps.tableName(), params)
    }

}

