package tiOPF

import java.util.Date
import java.time.LocalDate


interface IQueryClass{
    fun createInstance(): Query
}

abstract class Query: BaseObject() {
    companion object: IQueryClass{
        override fun createInstance(): Query {
            throw Exception("abstract class \"Query\" cannot be used directly")
        }
    }
    enum class QueryFieldKind {
        String,
        Integer,
        Float,
        DateTime,
        Logical,
        Binary,
        Macro,
        LongString
    }
    enum class QueryType {
        Select,
        Insert,
        Update,
        Delete,
        DDL
    }

    open fun doChangeOptions(sender: List<String>){
        // do nothing. a subclass can override this
    }

    open val supportsRowsAffected: Boolean = false
    abstract var sql: List<String>
    open var sqlText: String = ""
    abstract var active: Boolean
    abstract val eof: Boolean
    open var continueScan: Boolean = false
    open val options = List<String>()
        get() {
            if (options.onChange == null)
                options.onChange = ::doChangeOptions
            return field
        }

    open fun assignParams(params: QueryParams, where: QueryParams? = null){
        if (params != null)
            for (i in 0..params.count()-1)
                params.items[i].assignToTIQuery(this)
        if (where != null)
            for (i in 0..where.count()-1)
                where.items[i].assignToTIQuery(this)
    }

    // Params
    abstract fun getParamAsString(name: String): String
    abstract fun setParamAsString(name: String, value: String)
    abstract fun getParamAsInteger(name: String): Long
    abstract fun setParamAsInteger(name: String, value: Long)
    abstract fun getParamAsFloat(name: String): Float
    abstract fun setParamAsFloat(name: String, value: Double)
    abstract fun getParamAsBoolean(name: String): Boolean
    abstract fun setParamAsBoolean(name: String, value: Boolean)
    abstract fun getParamAsDate(name: String): Date
    abstract fun setParamAsDate(name: String, value: Date)
    abstract fun getParamAsTextBLOB(name: String): String
    abstract fun setParamAsTextBLOB(name: String, value: String)
    abstract fun getParamAsMacro(name: String): ByteArray
    abstract fun setParamAsMacro(name: String, value: ByteArray)
    abstract fun getParamIsNull(name: String): Boolean
    abstract fun setParamIsNull(name: String, value: Boolean)

    // Fields by String
    abstract fun getFieldAsString(name: String): String
    abstract fun setFieldAsString(name: String, value: String)
    abstract fun getFieldAsFloat(name: String): Double
    abstract fun setFieldAsFloat(name: String, value: Double)
    abstract fun getFieldAsBoolean(name: String): String
    abstract fun setFieldAsBoolean(name: String, value: Boolean)
    abstract fun getFieldAsInteger(name: String): String
    abstract fun setFieldAsInteger(name: String, value: Long)
    abstract fun getFieldAsDate(name: String): LocalDate
    abstract fun setFieldAsDate(name: String, value: LocalDate)
    abstract fun getFieldIsNull(name: String): Boolean
    // Fields by Integer
    abstract fun getFieldAsString(index: Long): String
    abstract fun setFieldAsString(index: Long, value: String)
    abstract fun getFieldAsFloat(index: Long): Double
    abstract fun setFieldAsFloat(index: Long, value: Double)
    abstract fun getFieldAsBoolean(index: Long): String
    abstract fun setFieldAsBoolean(index: Long, value: Boolean)
    abstract fun getFieldAsInteger(index: Long): String
    abstract fun setFieldAsInteger(index: Long, value: Long)
    abstract fun getFieldAsDate(index: Long): LocalDate
    abstract fun setFieldAsDate(index: Long, value: LocalDate)
    abstract fun getFieldIsNull(index: Long): Boolean

    abstract fun open()
    abstract fun close()
    abstract fun execSQL(): Long

    abstract fun selectRow(tableName: String, where: QueryParams, criteria: Criteria? = null)
    abstract fun insertRow(tableName: String, params: QueryParams)
    abstract fun deleteRow(tableName: String, params: QueryParams)
    abstract fun updateRow(tableName: String, params: QueryParams, where: QueryParams)

    abstract fun next()

    abstract fun paramCount(): Long
    abstract fun paramName(index: Long): String

    abstract fun fieldCount(): Long
    abstract fun fieldName(index: Long): String
    abstract fun fieldIndex(name: String): Long
    abstract fun fieldKind(index: Long): QueryFieldKind
    abstract fun fieldSize(index: Long): Long
    abstract fun hasNativeLogicalType(): Boolean

    //abstract fun assignParamFromStream(name: String, stream: Stream<Char>)
    //abstract fun assignParamToStream(name: String, stream: Stream<Char>)

    //abstract fun assignFieldAsStream(name: String, stream: Stream<Char>)
    //abstract fun assignFieldAsStream(index: Long, stream: Stream<Char>)

    open fun attachDatabase(database: Database){
        this.database = database
    }
    open fun detachDatabase(){
        if (active)
            active = false
        database = null
    }

    open var database: Database? = null
        set(value) {
            if (active)
                active = false
            field = value
        }

    abstract fun reset()
    open fun paramsAsString(): String{
        var result = ""
        try {

            for (i in  0..paramCount()-1){
                result = tiAddTrailingValue(result, tiLineEnd(), true)
                result += paramName(i) + " = "
                if (getParamIsNull(paramName(i)))
                    result += "Null"
                else
                    result += tiAddEllipsis(getParamAsString(paramName(i)), 120)
                }
        }
        catch (e: Throwable){
            result = "Unknown"
        }
        return result
    }

    abstract val queryType: QueryType


}
fun queryFieldKindToString(kind: Query.QueryFieldKind): String{
    return kind.name
}