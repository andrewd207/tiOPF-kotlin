package tiOPF.Log

data class LogEvent(var dateTime: String, var message: String, var shortMessage: String, var severity: LogSeverity, var threadId: String){
    fun getFormattedMessageTimeStamp(): String{
        return "$dateTime $threadId ${severity.toString().padEnd(5)} "
    }
    fun asString(): String{
        return (getFormattedMessageTimeStamp() + message).replace(10.toChar(), ' ')
    }
    fun asStringStripCrlf(): String{
        return asString().replace(13.toChar(), ' ')
    }
}