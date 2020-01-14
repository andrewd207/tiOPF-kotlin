package tiOPF

import java.util.Date
import java.time.LocalDate


interface IQueryCompanion{
    fun createInstance(): Query
}

abstract class Query: BaseObject() {
    companion object: IQueryCompanion{
        override fun createInstance(): Query {
            throw Exception("abstract class \"Query\" cannot be used directly")
        }
    }
    enum class QueryFieldKind {
        String,
        Integer,
        Int64,
        Float,
        Money,
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
    abstract val sql: List<String>
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

    open fun assignParams(params: QueryParams?, where: QueryParams? = null){
        if (params != null)
            for (i in 0..params.count()-1)
                params[i].assignToTIQuery(this)
        if (where != null)
            for (i in 0..where.count()-1)
                where[i].assignToTIQuery(this)
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
    abstract fun getFieldAsBoolean(name: String): Boolean
    abstract fun setFieldAsBoolean(name: String, value: Boolean)
    abstract fun getFieldAsInteger(name: String): Long
    abstract fun setFieldAsInteger(name: String, value: Long)
    abstract fun getFieldAsDate(name: String): LocalDate
    abstract fun setFieldAsDate(name: String, value: LocalDate)
    abstract fun getFieldIsNull(name: String): Boolean
    // Fields by Integer
    abstract fun getFieldAsString(index: Int): String
    abstract fun setFieldAsString(index: Int, value: String)
    abstract fun getFieldAsFloat(index: Int): Double
    abstract fun setFieldAsFloat(index: Int, value: Double)
    abstract fun getFieldAsBoolean(index: Int): Boolean
    abstract fun setFieldAsBoolean(index: Int, value: Boolean)
    abstract fun getFieldAsInteger(index: Int): Long
    abstract fun setFieldAsInteger(index: Int, value: Long)
    abstract fun getFieldAsDate(index: Int): LocalDate
    abstract fun setFieldAsDate(index: Int, value: LocalDate)
    abstract fun getFieldIsNull(index: Int): Boolean

    abstract fun open()
    abstract fun close()
    abstract fun execSQL(): Long

    abstract fun selectRow(tableName: String, where: QueryParams, criteria: Criteria? = null)
    abstract fun insertRow(tableName: String, params: QueryParams)
    abstract fun deleteRow(tableName: String, where: QueryParams)
    abstract fun updateRow(tableName: String, params: QueryParams, where: QueryParams)

    abstract fun next()

    abstract fun paramCount(): Long
    abstract fun paramName(index: Long): String

    abstract fun fieldCount(): Int
    abstract fun fieldName(index: Long): String
    abstract fun fieldIndex(name: String): Int
    abstract fun fieldKind(index: Int): QueryFieldKind
    abstract fun fieldSize(index: Int): Long
    abstract fun hasNativeLogicalType(): Boolean

    //abstract fun assignParamFromStream(name: String, stream: Stream<Char>)
    //abstract fun assignParamToStream(name: String, stream: Stream<Char>)

    abstract fun assignFieldAsByteArray(name: String, data: ValueOut<ByteArray>)
    abstract fun assignFieldAsByteArray(index: Int, data: ValueOut<ByteArray>)

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

    open val queryType: QueryType
        get() {
            var sql = sqlText.toLowerCase().trim()
            sql = sql.replace('\n', ' ')
            val p = sql.indexOf(' ')
            sql = sql.substring(0, p)
            return when (sql){
                "select" -> QueryType.Select
                "show" -> QueryType.Select // only mysql/mariadb has this. 'show tables'
                "insert" -> QueryType.Insert
                "update" -> QueryType.Update
                "delete" -> QueryType.Delete
                "create" -> QueryType.DDL
                "alter" -> QueryType.DDL
                "drop" -> QueryType.DDL
                else -> throw Exception(CTIOPFExcCanNotDetermineSQLType.format(sql))
            }
        }
}
fun queryFieldKindToString(kind: Query.QueryFieldKind): String{
    return kind.name
}