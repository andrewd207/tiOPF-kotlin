package tiOPF

import java.time.LocalDate
import java.util.*

abstract class QueryParamAbs: Object() {
    var name: String = ""
    val ownerAsQueryParams: QueryParams get() { return owner as QueryParams}
    open val kind: Query.QueryFieldKind = Query.QueryFieldKind.String
    val kindAsString: String get() { return queryFieldKindToString(kind) }
    var isNull: Boolean = true
    abstract var valueAsString: String
    abstract fun assignToTIQuery(query: Query)

}

open class QueryParamString: QueryParamAbs(){
    override val kind: Query.QueryFieldKind get() { return Query.QueryFieldKind.String }
    override var valueAsString: String =""
        set(value) {
            field = value
            isNull = false
        }

    override fun assignToTIQuery(query: Query) {
        assert(tiOPF.testValid(query, Query::class), { CTIErrorInvalidObject})
        query.setParamAsString(name, valueAsString)
    }
}

open class QueryParamInteger: QueryParamAbs() {
    private var valueInt: Long = 0
    override val kind: Query.QueryFieldKind = Query.QueryFieldKind.Integer
    override var valueAsString: String
        get() = valueInt.toString()
        set(value) { valueInt = value.toLong() ; isNull = false}
    var valueAsInteger: Long
        get() = valueInt
        set(value) {
            valueInt = value
            isNull = false
        }

    override fun assignToTIQuery(query: Query) {
        assert(tiOPF.testValid(query, Query::class), { CTIErrorInvalidObject})
        query.setParamAsInteger(name, valueInt)
    }
}

open class QueryParamFloat: QueryParamAbs() {
    private var valueDouble: Double = 0.0
    override val kind: Query.QueryFieldKind = Query.QueryFieldKind.Float
    override var valueAsString: String
        get() = valueDouble.toString()
        set(value) { valueDouble = value.toDouble() ; isNull = false }
    var valueAsFloat: Double
        get() = valueDouble
        set(value) {
            valueDouble = value
            isNull = false
        }

    override fun assignToTIQuery(query: Query) {
        assert(tiOPF.testValid(query, Query::class), { CTIErrorInvalidObject})
        query.setParamAsFloat(name, valueAsFloat)
    }
}

open class QueryParamDateTime: QueryParamAbs() {
    private var valueDateTime: LocalDate = LocalDate.now()
    override val kind: Query.QueryFieldKind = Query.QueryFieldKind.DateTime
    override var valueAsString: String
        get() = valueDateTime.toString()
        set(value) { valueDateTime = LocalDate.parse(value) ; isNull = false}
    var valueAsDateTime: LocalDate
        get() = valueDateTime
        set(value) {
            valueDateTime = value
            isNull = false
        }

    override fun assignToTIQuery(query: Query) {
        assert(tiOPF.testValid(query, Query::class), { CTIErrorInvalidObject})
        query.setParamAsDate(name, valueAsDateTime)
    }
}

open class QueryParamBoolean: QueryParamAbs() {
    private var valueBoolean: Boolean = false
    override val kind: Query.QueryFieldKind = Query.QueryFieldKind.Binary
    override var valueAsString: String
        get() = valueBoolean.toString()
        set(value) {
            valueBoolean = (value.equals("true", true))
            isNull = false
        }
    var valueAsBoolean: Boolean
        get() = valueBoolean
        set(value) {
            valueBoolean = value
            isNull = false
        }


    override fun assignToTIQuery(query: Query) {
        assert(tiOPF.testValid(query, Query::class), { CTIErrorInvalidObject})
        query.setParamAsBoolean(name, valueAsBoolean)
    }
}

open class QueryParamBlob: QueryParamAbs() {
    private var valueByteArray: ByteArray = ByteArray(0)
    override val kind: Query.QueryFieldKind = Query.QueryFieldKind.Macro
    override var valueAsString: String
        get() = Base64.getMimeEncoder().encodeToString(valueByteArray)
        set(value) {
            valueByteArray = Base64.getMimeDecoder().decode(value)
            isNull = false
        }
    var valueAsByteArray: ByteArray
        get() = valueByteArray
        set(value) {
            valueByteArray = value
            isNull = false
        }
    override fun assignToTIQuery(query: Query) {
        assert(tiOPF.testValid(query, Query::class), { CTIErrorInvalidObject})
        query.setParamAsMacro(name, valueByteArray)
    }
}
