package tiOPF

import kotlin.reflect.full.createInstance

// complete

const val CDefaultOIDFieldName = "OID"
abstract class OID(): BaseObject(){

    abstract fun isNull(): Boolean
    abstract fun nullOIDAsString(): String
    abstract fun setToNull()
    abstract fun compare(compareWith: OID): Int
    abstract fun equals(compareWith: OID): Boolean
    abstract var asString: String

    open fun assign(source: OID){ assert(false, {this::class.simpleName+ ".assign not implemented"})}
    abstract fun assignToQueryParam(fieldName: String, params: QueryParams)
    abstract fun assignToQuery(fieldName: String, query: Query)
    fun assignToQuery(query: Query){
        assignToQuery(CDefaultOIDFieldName, query)
    }
    abstract fun assignFromQuery(fieldName: String, query: Query)
    fun assignFromQuery(query: Query){
        assignToQuery(CDefaultOIDFieldName, query)
    }
    abstract fun equalsQueryField(fieldName: String, query: Query): Boolean
    fun clone(): OID {
        val result = this::class.createInstance()
        result.assign(this)
        return result
    }





}

