package tiOPF.Mediator

import tiOPF.Object
import tiOPF.ValueReference
import tiOPF.className

enum class ObjectUpdateMoment {
    None,
    Default,
    OnChange,
    OnExit,
    Custom
}

internal const val SErrInvalidFieldName      = "No fieldname specified for column %d"
internal const val SErrInvalidAlignmentChar  = "Invalid alignment character \"%s\" specified for column %d"
internal const val SErrInvalidWidthSpecifier = "Invalid with \"%s\" specified for column %d"
internal const val SErrNotListObject         = "%s is not a TtiObjectList"
internal const val SErrCompositeNeedsList    = "%s needs a TtiObjectList class but is registered with %s"
internal const val SErrActive                = "Operation not allowed while the mediator is active"
internal const val SErrNoGUIFieldName        = "no gui fieldname set"
internal const val SErrNoSubjectFieldName    = "no subject fieldname set"
internal const val SErrInvalidPropertyName   = "<%s> is not a property of <%s>"
internal const val DefFieldWidth = 75

internal const val CMediatorFieldSeparator = '|'
internal const val CFieldDelimiter = ';'

class EtiMediator(override val message: String): Exception(message)

typealias ObjectToGuiEvent = (sender: MediatorView, source: Object, dest: Any, handled: ValueReference<Boolean>) -> Unit
typealias BeforeGuiToObjectEvent = (sender: MediatorView, source: Any, dest: Object, handled: ValueReference<Boolean>) -> Unit
typealias AfterGuiToObjectEvent = (sender: MediatorView, source: Any, dest: Object) -> Unit

typealias OnBeforeSetupField = (obj: Object, filedName: String, value: ValueReference<String>) -> Unit

internal fun mediatorError(sender: Any?, msg: String){
    val err: String
    var cn = ""
    var sn = ""
    when (sender) {
        null -> err = msg
        is MediatorView -> {
            val m = sender
            val v = m.view
            val s = m.subject
            cn = if (v != null) { v::class.simpleName!! + " instance" } else "null"
            sn = if (s != null) { s::class.simpleName!!} else "null"

            err = "Mediator %s (%s,%s,%s) : %s".format(m.className(), sn, cn, m.fieldName, msg)
        }
        else -> err = "%s : %S".format(sender::class.simpleName!!, msg)
    }
    throw EtiMediator(err)
}

internal fun mediatorError(sender: Any?, msg: String, args: Array<Any>){
    mediatorError(sender, msg.format(args))
}

internal fun String.tiSubString(first: Char, last: Char): String{
    var result = substringAfter(first, "")
    result = result.substringBefore(last, "")
    return result
}

internal fun String.tiFieldName(): String{
    return substringBefore('(')
}

internal fun String.tiFieldCaption(): String{
    var s = tiSubString('(', ')')
    return if (s.isBlank())
        tiFieldName()
    else {
        tiSubString('"', '"')
    }
}

internal fun String.tiFieldWidth(): Int{
    var s = tiSubString('(', ')')
    if (s.isBlank())
        return DefFieldWidth
    else
        return  s.substringBefore(',').toInt()
}
internal fun String.tiFieldAlignment(): MediatorFieldInfo.Alignment{
    var s = tiSubString('(', ')').trim()
    return when (s) {
        "" -> MediatorFieldInfo.Alignment.Left
        else -> {
            s = s.substringAfter(',', "").substringAfter(',', "")
            if (s.isBlank())
                MediatorFieldInfo.Alignment.Left
            else
                MediatorFieldInfo.Alignment.fromChar(s[0])
        }
    }
}

class BaseMediator {
}