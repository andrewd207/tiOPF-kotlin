package tiOPF

abstract class OIDStringAbs: OID() {
    override fun isNull():Boolean { return  asString === nullOIDAsString() }
    override fun setToNull() { asString = "" }
    override fun nullOIDAsString(): String { return "" }
    override var asString: String = nullOIDAsString()
    override fun equals(compareWith: OID): Boolean {
        return asString === compareWith.asString
    }
    override fun compare(compareWith: OID): Int {
        if (asString < compareWith.asString)
            return -1
        else if (asString > compareWith.asString)
            return 1
        return 0
    }

    override fun assignToQueryParam(fieldName: String, params: QueryParams) {
        assert(params is QueryParams, { "query not QueryParams"})
        params.setValueAsString(fieldName, asString)
    }

    override fun assignToQuery(fieldName: String, query: Query) {
        if (isNull())
            query.setParamIsNull(fieldName, true)
        else
            query.setParamAsString(fieldName, asString)
    }

    override fun assignFromQuery(fieldName: String, query: Query) {
        asString = query.getFieldAsString(fieldName)
    }

    override fun equalsQueryField(fieldName: String, query: Query): Boolean {
        return asString.equals(query.getFieldAsString(fieldName))
    }

    override fun assign(source: OID) {
        asString = source.asString
    }
}