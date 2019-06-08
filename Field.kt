package tiOPF

import kotlin.math.absoluteValue
import kotlin.math.sign

enum class NullValidation {
    AllowNull,
    NotNull
}

abstract class FieldAbs(private var owner: Object,
               var nullValidation: NullValidation = NullValidation.AllowNull )
                : BaseObject() {
    var privIsNull: Boolean = false
    var isNull: Boolean
        get() = privIsNull
        set(value) {
            if (value)
                clear()
            else
                privIsNull = false
        }
    protected open fun clear(){
        privIsNull = true
    }
    abstract var asString: String
    private var privFieldName = ""
    open var fieldName: String
        get() {
            if (privFieldName.isEmpty()){
                assert(testValid(owner, Object::class), { CTIErrorInvalidObject})
                val list = List<String>()
                getPropertyNames(owner, list, arrayOf(TypeKind.OBJECT))
                list.forEach {
                    if (getObjectProperty<Any>(this, it) == this){
                        privFieldName = it
                        return@forEach
                    }
                }
                if (privFieldName.isEmpty())
                    throw Exception(CErrorUnableToDetermineFieldName.format(owner::class.qualifiedName))

            }
            return privFieldName
        }
        set(value) { privFieldName = value }

    open fun isValidValue(errors: ObjectErrorList? = null): Boolean{
        assert(testValid(owner), { CTIErrorInvalidObject})
        val result = nullValidation == NullValidation.AllowNull || !isNull
        errors?.addError(fieldName, CErrorFieldNotAssigned.format(owner.className()+"."+fieldName, owner.oid))
        return result
    }
    abstract fun equals(compareWith: FieldAbs): Boolean
    abstract fun assign(assignFrom: FieldAbs)
    protected open fun setValue(){
        privIsNull = false
    }
}

typealias StringFieldMethodReadEvent = (FieldStringMethod, value: ValueReference<String>) -> Unit

open class FieldString(owner: Object, nullValidation: NullValidation, val maxLength: Int = 0): FieldAbs(owner, nullValidation){
    private var privAsString = ""
    override var asString: String
        get() = privAsString
        set(value) {
            privAsString = value
            setValue()
        }

    override fun isValidValue(errors: ObjectErrorList?): Boolean {
        var result = super.isValidValue(errors)
        if (result) {
            result = maxLength == 0 || asString.length <= maxLength
            if (errors != null && !result)
                errors.addError(fieldName, CErrorFieldTooLong.format(this.className()+"."+fieldName, maxLength, asString.length))
        }
        return result
    }

    override fun equals(compareWith: FieldAbs): Boolean {
        assert(testValid(FieldString::class), { CTIErrorInvalidObject })
        return isNull == compareWith.isNull && asString === compareWith.asString
    }

    override fun assign(assignFrom: FieldAbs) {
        assert(testValid(FieldString::class), { CTIErrorInvalidObject })
        if (assignFrom.isNull)
            isNull = true
        else
            asString = assignFrom.asString
    }

    override fun clear() {
        super.clear()
        privAsString = ""

    }
}

open class FieldStringMethod(owner: Object, val readMethod: StringFieldMethodReadEvent): FieldAbs(owner){
    override var asString: String
        get() {
            assert(readMethod != null, { "readMethod not assigned"})
            val ls = ValueReference("")
            readMethod(this, ls)
            return ls.value
        }
        set(value) {
            assert(false, { "not implemented" })
        }

    override fun equals(compareWith: FieldAbs): Boolean {
        assert(compareWith.testValid(FieldString::class), { CTIErrorInvalidObject })
        return asString.equals(compareWith.asString)
    }

    override fun assign(assignFrom: FieldAbs) {
        assert(false, { "not implemented" })
    }

    override fun clear() {
        // do nothing
    }

    override fun isValidValue(errors: ObjectErrorList?): Boolean {
        assert(false, { "not implemented" })
        return false
    }

}

open class FieldInteger(owner: Object, nullValidation: NullValidation, val maxDigits: Int = 0): FieldAbs(owner, nullValidation){
    private var privAsInteger: Long = 0
    var asInteger: Long
        get() = privAsInteger
        set(value) {
        privAsInteger = value
        setValue()
    }

    override var asString: String
        get() {
            return if (isNull) ""
                   else privAsInteger.toString()

        }
        set(value) {
            privAsInteger =
                if (value.isNotEmpty())
                    value.toLong()
                else
                    0
            setValue()
        }

    override fun isValidValue(errors: ObjectErrorList?): Boolean {
        var result = super.isValidValue(errors)
        if (result)
            result = maxDigits == 0 || (asInteger > 0 && asString.length <= maxDigits) || (asInteger < 0 && asString.length <= maxDigits+1)
        if (errors != null && !result)
            errors.addError(fieldName, CErrorFieldTooLong.format(className()+"."+fieldName, maxDigits, asString.length))
        return result
    }

    override fun equals(compareWith: FieldAbs): Boolean {
        assert(testValid(compareWith, FieldInteger::class), { CTIErrorInvalidObject })
        return isNull == compareWith.isNull && asInteger == (compareWith as FieldInteger).asInteger
    }

    override fun assign(assignFrom: FieldAbs) {
        assert(testValid(assignFrom, FieldInteger::class), { CTIErrorInvalidObject })
        if (assignFrom.isNull)
            isNull = true
        else
            asInteger = (assignFrom as FieldInteger).asInteger
    }

    override fun clear() {
        super.clear()
        privAsInteger = 0
    }


}

open class FieldCurrency(owner: Object, nullValidation: NullValidation = NullValidation.AllowNull): FieldAbs(owner, nullValidation){
    private var privValue: Long = 0
    var asInteger: Long
        get() = privValue
        set(value) {
            privValue = value
            setValue()
        }

    override var asString: String
        get() {
            if (isNull)
                return ""
            else {
                val absoluteValue = privValue.absoluteValue
                val whole: Long = absoluteValue / 100
                val frac = absoluteValue - (whole * 100)
                var result = whole.toString()+"."+frac.toString().padStart(2,'0')
                if (privValue.sign == -1)
                    result = "-"+result
                return result
            }

        }
        set(value) {
            privValue =
                if (value.isNotEmpty())
                    value.toLong()
                else
                    0
            setValue()
        }
    var asCurrencyString: String
        get() {
            if (!isNull){
                val float: Double = privValue.toDouble() / 100
                return float

            }

        }

    override fun isValidValue(errors: ObjectErrorList?): Boolean {
        var result = super.isValidValue(errors)
        if (result)
            result = maxDigits == 0 || (asInteger > 0 && asString.length <= maxDigits) || (asInteger < 0 && asString.length <= maxDigits+1)
        if (errors != null && !result)
            errors.addError(fieldName, CErrorFieldTooLong.format(className()+"."+fieldName, maxDigits, asString.length))
        return result
    }

    override fun equals(compareWith: FieldAbs): Boolean {
        assert(testValid(compareWith, FieldInteger::class), { CTIErrorInvalidObject })
        return isNull == compareWith.isNull && asInteger == (compareWith as FieldInteger).asInteger
    }

    override fun assign(assignFrom: FieldAbs) {
        assert(testValid(assignFrom, FieldInteger::class), { CTIErrorInvalidObject })
        if (assignFrom.isNull)
            isNull = true
        else
            asInteger = (assignFrom as FieldInteger).asInteger
    }

    override fun clear() {
        super.clear()
        privAsInteger = 0
    }


}