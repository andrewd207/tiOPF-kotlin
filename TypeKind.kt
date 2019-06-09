package tiOPF

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

val CTypeKindSimple = setOf(TypeKind.STRING, TypeKind.INT, TypeKind.FLOAT)

enum class TypeKind {

    STRING,
    CHAR,
    INT,
    SHORT,
    BYTE,
    LONG,
    DATE,
    BOOLEAN,
    FLOAT,
    DOUBLE,
    BYTE_ARRAY,
    OBJECT,
    UNKNOWN

}

fun classToTypeKind(kClass: KClass<*>): TypeKind{
    return when (kClass){
        String::class -> TypeKind.STRING
        Char::class -> TypeKind.CHAR
        Int::class -> TypeKind.INT
        Short::class -> TypeKind.SHORT
        Byte::class -> TypeKind.BYTE
        Long::class -> TypeKind.LONG
        Date::class -> TypeKind.DATE
        Boolean::class -> TypeKind.BOOLEAN
        Float::class -> TypeKind.FLOAT
        Double::class -> TypeKind.DOUBLE
        ByteArray::class -> TypeKind.BYTE_ARRAY
        BaseObject::class -> TypeKind.OBJECT

        else -> {
            if (kClass.superclasses.contains(BaseObject::class))
                TypeKind.OBJECT
            else
                TypeKind.UNKNOWN
        }
    }
}