package tiOPF
// complete
abstract class OID: BaseObject{

    constructor(): super(){
        setToNull()
    }
    abstract fun isNull(): Boolean
    abstract fun nullOIDAsString(): String
    abstract fun setToNull()
    abstract fun clone(): OID
    abstract fun compare(compareWith: OID): Int
    abstract fun equals(compareWith: OID): Boolean
    abstract var asString: String


    open fun assign(source: OID){ assert(false, {this::class.simpleName+ ".assign not implemented"})}
    abstract fun assignToQueryParam(fieldName: String, params: QueryParams)
    abstract fun assignToQuery(fieldName: String, query: Query)
    abstract fun assignToQuery(query: Query)
    abstract fun assignFromQuery(fieldName: String, Query: Query)
    abstract fun assignFromQuery(query: Query)





}

