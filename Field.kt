package tiOPF
// complete
import java.text.DateFormat
import java.text.NumberFormat
import java.util.*
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
            val absoluteValue = privValue.absoluteValue
            val whole: Long = absoluteValue / 100
            val frac = absoluteValue - (whole * 100)
            var result = whole.toString()+"."+frac.toString().padStart(2,'0')
            if (privValue.sign == -1)
                result = "-"+result
            return result
        }
        set(value) {
            asCurrencyString = value
            setValue()
        }
    var asCurrencyString: String
        get() {
            if (!isNull){
                // TODO other currencies than the default one?
                val format: NumberFormat = NumberFormat.getCurrencyInstance()
                return format.format(privValue)
            }
            return ""
        }
        set(value) {
            if (value.isEmpty()){
                isNull = true
                return
            }
            isNull = false

            val numberFormat = NumberFormat.getCurrencyInstance()
            privValue = numberFormat.parse(value).toLong()

        }
    var asFloat: Double
        get() = asInteger.toDouble() / 100.0
        set(value) { privValue = (value * 100).toLong() }

    override fun equals(compareWith: FieldAbs): Boolean {
        assert(testValid(compareWith, FieldCurrency::class), { CTIErrorInvalidObject })
        return isNull == compareWith.isNull && asInteger == (compareWith as FieldInteger).asInteger
    }

    override fun assign(assignFrom: FieldAbs) {
        assert(testValid(assignFrom, FieldCurrency::class), { CTIErrorInvalidObject })
        if (assignFrom.isNull)
            isNull = true
        else
            asInteger = (assignFrom as FieldCurrency).asInteger
    }

    override fun clear() {
        super.clear()
        privValue = 0
    }

    fun inc(field: FieldCurrency){
        asInteger+= field.asInteger
    }

    fun inc(i: Int){
        asInteger+= i
    }



}

open class FieldBoolean(owner: Object, nullValidation: NullValidation = NullValidation.AllowNull): FieldAbs(owner, nullValidation){
    private var privValue: Boolean = false
    override var asString: String
        get() {
            return if (isNull) ""
            else privValue.toString()

        }
        set(value) {
            when {
                value.isEmpty() -> clear()
                value.toLowerCase() in CBoolTrueArray -> {
                    privValue = true
                    setValue()
                }
                value.toLowerCase() in CBoolFalseArray -> {
                    privValue = false
                    setValue()
                }
                else -> clear()
            }

        }
    var asBoolean: Boolean
        get() = privValue
        set(value) {
            privValue = value
            setValue()
        }

    override fun equals(compareWith: FieldAbs): Boolean {
        assert(testValid(compareWith, FieldBoolean::class), { CTIErrorInvalidObject })
        return isNull == compareWith.isNull && asBoolean == (compareWith as FieldBoolean).asBoolean
    }

    override fun assign(assignFrom: FieldAbs) {
        assert(testValid(assignFrom, FieldBoolean::class), { CTIErrorInvalidObject })
        if (assignFrom.isNull)
            isNull = true
        else
            asBoolean = (assignFrom as FieldBoolean).asBoolean
    }

    override fun clear() {
        super.clear()
        privValue = false
    }
}

open class FieldDate(owner: Object, nullValidation: NullValidation = NullValidation.AllowNull): FieldAbs(owner, nullValidation){
    private var privValue: Date = Date()
    override var asString: String
        get() {
            return if (isNull) ""
            else DateFormat.getInstance().format(privValue)

        }
        set(value) {
            when {
                value.isEmpty() -> clear()
                else -> privValue = DateFormat.getDateInstance().parse(value)
            }

        }

    var asDate: Date
        get() = privValue
        set(value) {
            privValue = value
            setValue()
        }
    val day: Int
        get() {
            if (isNull)
                return 0
            val cal = Calendar.getInstance()
            cal.time = privValue
            return cal.get(Calendar.DAY_OF_MONTH)
        }
    val month: Int
        get() {
            if (isNull)
                return 0
            val cal = Calendar.getInstance()
            cal.time = privValue
            return cal.get(Calendar.MONTH)
        }
    val year: Int
        get() {
            if (isNull)
                return 0
            val cal = Calendar.getInstance()
            cal.time = privValue
            return cal.get(Calendar.YEAR)
        }


    override fun equals(compareWith: FieldAbs): Boolean {
        assert(testValid(compareWith, FieldDate::class), { CTIErrorInvalidObject })
        return isNull == compareWith.isNull && asDate == (compareWith as FieldDate).asDate
    }

    override fun assign(assignFrom: FieldAbs) {
        assert(testValid(assignFrom, FieldDate::class), { CTIErrorInvalidObject })
        if (assignFrom.isNull)
            isNull = true
        else
            asDate = (assignFrom as FieldDate).asDate
    }

    override fun clear() {
        super.clear()
        privValue = Date()
    }
}