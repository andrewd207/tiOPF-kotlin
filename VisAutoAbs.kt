package tiOPF

import kotlin.reflect.KClass

open class VisAutoAbs: Visitor() {
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
                    data.owner.oid.assignToQueryParam(colname, params)
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


}