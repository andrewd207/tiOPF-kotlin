package tiOPF

import java.io.InputStream
import java.util.stream.Stream

class Value<T: Any>(private var privValue: T) {

    var asString: String
        get() {
            return privValue.toString()
        }
        set(value) {
            privValue = when (privValue) {
                is String -> value as T
                is Int -> value.toInt() as T
                is Long -> value.toLong() as T
                is Float -> value.toFloat() as T
                is Double -> value.toDouble() as T
                is Boolean -> value.toBoolean() as T
                else -> throw Exception("Unsupported type for Value<${privValue::class.simpleName}>")
            }
        }
    var asInt: Int
        get() {
            return when (privValue){
                is Int -> privValue as Int
                is Long -> (privValue as Long).toInt()
                is Float -> (privValue as Float).toInt()
                is Double -> (privValue as Double).toInt()
                is String -> (privValue as String).toInt()
                is Boolean -> {
                    when (privValue){
                        false -> 0
                        else -> 1
                    }
                }
                else -> throw Exception("Unsupported type for Value<${privValue::class.simpleName}>")
            }
        }
        set(value) {
            privValue = when (privValue) {
                is String -> value.toString() as T
                is Int -> value as T
                is Long -> value.toLong() as T
                is Float -> value.toFloat() as T
                is Double -> value.toDouble() as T
                else -> throw Exception("Unsupported type for Value<${privValue::class.simpleName}>")
            }
        }
    var asBlob: ByteArray
        get () {
            return when (privValue){
                is ByteArray -> privValue as ByteArray
                else -> throw Exception("Unsupported type for Value<${privValue::class.simpleName}>")
            }
        }
        set(value) {
            privValue = when (privValue) {
                is ByteArray -> value as T
                else -> throw Exception("Unsupported type for Value<${privValue::class.simpleName}>")
            }

        }
    var asBoolean: Boolean
        get() {
            return when (privValue){
                is Boolean -> privValue as Boolean
                is Number -> privValue != 0
                is String -> (privValue as String).equals("true", true)
                else -> throw Exception("Unsupported type for Value<${privValue::class.simpleName}>")
            }
        }
        set(value) {
            privValue = when (privValue){
                is Boolean -> value as T
                is Number -> when (value){
                    false -> 0 as T
                    else -> 1 as T
                }
                is String -> when (value){
                    false -> "false" as T
                    else -> "true" as T
                }
                else -> throw Exception("Unsupported type for Value<${privValue::class.simpleName}>")
            }

        }

}