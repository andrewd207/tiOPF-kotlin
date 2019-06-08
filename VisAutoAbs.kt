package tiOPF

import java.lang.Exception
import kotlin.reflect.KClass

abstract class VisAutoAbs: ObjectVisitor() {
    protected val whereAttrColMaps = AttrColMaps()
    protected val where = QueryParams()
    protected val attrColMaps = AttrColMaps()
    protected val params = QueryParams()

    protected var visitedClassType: KClass<Object>? = null
    override fun execute(visited: Visited) {
        super.execute(visited)
        visitedClassType = visited::class as KClass<Object>

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