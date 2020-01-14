package tiOPF

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.superclasses

val CTypeKindSimple = setOf(TypeKind.STRING, TypeKind.INT, TypeKind.FLOAT)
typealias TypeKinds = Set<TypeKind>
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
    ENUM,
    LIST,
    UNKNOWN;
    fun toSimpleTypeKind(): SimpleTypeKind{
        return when (this) {
            STRING, CHAR, LIST-> SimpleTypeKind.String
            INT, SHORT, BYTE, LONG -> SimpleTypeKind.Int
            DATE -> SimpleTypeKind.DateTime
            BOOLEAN -> SimpleTypeKind.Boolean
            FLOAT, DOUBLE -> SimpleTypeKind.Float
            BYTE_ARRAY -> SimpleTypeKind.Binary
            OBJECT -> SimpleTypeKind.Object
            else -> throw EtiOPFInternalException("Unhandled TypeKind to SimpleTypeKind %s".format(this))
        }

    }
}

enum class SimpleTypeKind{
    Int, Float, String, DateTime, Boolean, Object, Binary;

}

fun classToTypeKind(kClass: KClass<*>): TypeKind{
    fun isEnum( type: KClass<out Any> ): Boolean
    {
        return type.supertypes.any { t ->
            (t.classifier as KClass<out Any>).qualifiedName == "kotlin.Enum" }
    }
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
            if (isEnum(kClass))
                TypeKind.ENUM
            else if (kClass.superclasses.contains(BaseObject::class))
                TypeKind.OBJECT
            else if (kClass.isSubclassOf(Collection::class))
               TypeKind.LIST
            else
                TypeKind.UNKNOWN
        }
    }
}