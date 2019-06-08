package tiOPF

var ULog: Log? = null
fun GLog(): Log?{
    if (ULog == null)
        ULog = Log()

    if (ULog != null)
      return ULog as Log

    return null
}

fun LOG(message: String, severity: LogSeverity = LogSeverity.lsNormal) {
    GLog()?.log(message, severity)
}

fun LOG(array: Any, severity: LogSeverity = LogSeverity.lsNormal) {
    //GLog().log(message, severity)
}

enum class LogSeverity {
    lsNormal
    ,lsUserInfo
    ,lsObjCreation
    ,lsVisitor
    ,lsConnectionPool
    ,lsAcceptVisitor
    ,lsQueryTiming
    ,lsDebug
    ,lsWarning
    ,lsError
    ,lsSQL
}

open class Log: BaseObject() {

    fun log(message: String, severity: LogSeverity = LogSeverity.lsNormal){

    }

    fun log(message: String, args: Any?, severity: LogSeverity = LogSeverity.lsNormal){
        log(String.format(message, args), severity)
    }



}