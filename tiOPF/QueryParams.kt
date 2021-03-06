package tiOPF

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.primaryConstructor

open class QueryParams: ObjectList<QueryParamAbs>() {
    private var paramIsNull = false
    val asString: String get() {
        var result = ""
        forEach {
            if (result.isNotEmpty())
                result += ", "
            result += it.name+"="+it.valueAsString
        }
        return result
    }
    open fun <T: QueryParamAbs> findParamByName(name: String, returnType: KClass<T> = QueryParamAbs::class as KClass<T>): T?{
        val result: QueryParamAbs? = find { it.name.equals(name, true) }
        return result as T?
    }
    protected open fun <T: QueryParamAbs>findCreateParamByName(name: String, type: KClass<T>): T{
        var result = findParamByName<T>(name)
        if (result == null) {
            result = type.primaryConstructor!!.call() as T
            result.name = name
            add(result)
        }
        return result
    }

    fun getParamIsNull(name: String): Boolean{
        val param: QueryParamAbs? = findParamByName(name)
        return param == null || param.isNull
    }
    fun setParamIsNull(name: String, value: Boolean){
        val param: QueryParamAbs? = findParamByName(name)
        param?.isNull = value
    }

    fun getParamAsString(name: String): String{
        val param =  findParamByName(name, QueryParamString::class)
        assert(param is QueryParamAbs, { CTIErrorInvalidObject })
        return param!!.valueAsString
    }
    fun setParamAsString(name: String, value: String){
        val param = findCreateParamByName(name, QueryParamString::class)
        param.valueAsString = value
    }

    // String
    fun setValueAsString(name: String, value: String){
        val param = findCreateParamByName(name, QueryParamString::class)
        param.valueAsString = value
    }

    fun getValueAsString(name: String): String{
        val param = findParamByName(name, QueryParamAbs::class)
        assert(param is QueryParamAbs, { CTIErrorInvalidObject })
        return param!!.valueAsString
    }

    fun assignFromFieldString(field: FieldString, name: String){
        setValueAsString(name, field.asString)
    }

    // Int
    fun setValueAsInteger(name: String, value: Long){
        val param = findCreateParamByName(name, QueryParamInteger::class)
        param.valueAsInteger = value
    }

    fun getValueAsInteger(name: String): Long{
        val param = findParamByName(name, QueryParamInteger::class)
        assert(param is QueryParamInteger, { CTIErrorInvalidObject })
        return param!!.valueAsInteger
    }

    fun assignFromFieldInteger(field: FieldInteger, name: String){
        setValueAsInteger(name, field.asInteger)
    }

    // Float
    fun setValueAsFloat(name: String, value: Double){
        val param = findCreateParamByName(name, QueryParamFloat::class)
        param.valueAsFloat = value
    }

    fun getValueAsFloat(name: String): Double{
        val param = findParamByName(name, QueryParamFloat::class)
        assert(param is QueryParamFloat, { CTIErrorInvalidObject })
        return param!!.valueAsFloat
    }

    fun assignFromFieldFloat(field: FieldFloat, name: String){
        setValueAsFloat(name, field.asFloat)
    }

    // Boolean
    fun setValueAsBoolean(name: String, value: Boolean){
        val param = findCreateParamByName(name, QueryParamBoolean::class)
        param.valueAsBoolean = value
    }

    fun getValueAsBoolean(name: String): Boolean{
        val param = findParamByName(name, QueryParamBoolean::class)
        assert(param is QueryParamBoolean, { CTIErrorInvalidObject })
        return param!!.valueAsBoolean
    }

    fun assignFromFieldBoolean(field: FieldBoolean, name: String){
        setValueAsBoolean(name, field.asBoolean)
    }

    // Date
    fun setValueAsDateTime(name: String, value: Date){
        val param = findCreateParamByName(name, QueryParamDateTime::class)
        param.valueAsDateTime = value
    }

    fun getValueAsDateTime(name: String): Date{
        val param = findParamByName(name, QueryParamDateTime::class)
        assert(param is QueryParamDateTime, { CTIErrorInvalidObject })
        return param!!.valueAsDateTime
    }

    fun assignFromFieldDateTime(field: FieldDate, name: String){
        setValueAsDateTime(name, field.asDate)
    }

    // Stream/ByteArray
    fun setValueAsByteArray(name: String, value: ByteArray){
        val param = findCreateParamByName(name, QueryParamBlob::class)
        param.valueAsByteArray = value
    }

    fun getValueAsByteArray(name: String): ByteArray{
        val param = findParamByName(name, QueryParamBlob::class)
        assert(param is QueryParamBlob, { CTIErrorInvalidObject })
        return param!!.valueAsByteArray
    }

    fun assignValueToByteArray(name: String, data: ValueReference<ByteArray>){
        val queryData = getValueAsByteArray(name)
        data.value = queryData.copyOf()
    }

    /*fun setValueFromProp(fieldMetadata: Object, propName: String, paramName: String){
        val property = getClassProperty(fieldMetadata::class, propName)
        setValueFromProp(fieldMetadata, property!!, paramName)
    }*/

    fun setValueFromProp(fieldMetadata: Object, property: KProperty<*>, paramName: String){
        assert(fieldMetadata is Object, { CTIErrorInvalidObject })
        assert(paramName.isNotEmpty(), { "paramName is not assigned"})
        assert(isPublishedProp(property), { "$property is not a published property on ${fieldMetadata.className()}"})
        try {
            when (classToTypeKind(getPropertyClass(property))){
                TypeKind.STRING -> {
                    val value = getObjectPropertyProp<String>(fieldMetadata, property)
                    setValueAsString(paramName, value!!)
                }
                TypeKind.INT -> {
                    val value = getObjectPropertyProp<Number>(fieldMetadata, property)
                    setValueAsInteger(paramName, value!!.toLong())
                }
                TypeKind.FLOAT -> {
                    val value = getObjectPropertyProp<Number>(fieldMetadata, property)
                    setValueAsFloat(paramName, value!!.toDouble())
                }
                TypeKind.DATE -> {
                    val value = getObjectPropertyProp<Date>(fieldMetadata, property)
                    setValueAsDateTime(paramName, value!!)
                }
                TypeKind.BOOLEAN -> {
                    val value = getObjectPropertyProp<Boolean>(fieldMetadata, property)
                    setValueAsBoolean(paramName, value!!)
                }
                TypeKind.BYTE_ARRAY -> {
                    val value = getObjectPropertyProp<ByteArray>(fieldMetadata, property)
                    setValueAsByteArray(paramName, value!!)
                }
                else ->
                    if (property.name != "oid")
                        throw EtiOPFProgrammerException(CErrorInvalidTypeKind)
            }
        }
        catch (e: EtiOPFProgrammerException){
            throw EtiOPFProgrammerException(CErrorSettingPropValue.format(property.name, fieldMetadata.className(), e.message))

        }

    }

}