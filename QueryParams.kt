package tiOPF

import kotlin.reflect.KClass
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
    open fun findParamByName(name: String): QueryParamAbs?{
        return find { it.name.equals(name, true) }
    }
    protected open fun findCreateParamByName(name: String, klass: KClass<QueryParamAbs>): QueryParamAbs{
        var result = findParamByName(name)
        if (result == null) {
            result = klass::primaryConstructor.call() as QueryParamAbs
            result!!.name = name
            add(result)
        }
        return result
    }

    fun getParamIsNull(name: String): Boolean{
        val param = findParamByName(name)
        return param == null || param.isNull
    }
    fun setParamIsNull(name: String, value: Boolean){
        val param = findParamByName(name)
        param?.isNull = value
    }

    fun getParamAsString(name: String): String{
        val param = findParamByName(name)
        assert(testValid(param, QueryParamAbs::class, { CTIErrorInvalidObject }))
        return param!!.valueAsString
    }
    fun setParamAsString(name: String, value: String){
        val param = findCreateParamByName(name, QueryParamString::class as KClass<QueryParamAbs>)
        param.valueAsString = value
    }
    fun assignFromFieldString(field: FieldString)


}