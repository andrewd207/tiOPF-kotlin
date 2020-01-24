package tiOPF.Log

class LogToConsole: LogTo() {
    override fun log(dateTime: String, threadId: String, message: String, severity: LogSeverity) {
        if (!acceptEvent(dateTime, threadId, severity))
            return

        var logMessage = LogEvent(dateTime, message, "", severity, threadId).asStringStripCrlf()
        logMessage = logMessage.substring(9, logMessage.length)
        println(logMessage)
    }

    override fun acceptEvent(dateTime: String, message: String, severity: LogSeverity): Boolean {
        return (severity == LogSeverity.UserInfo || sevToLog.contains(severity))
    }

    override fun writeToOutput() { /* nothing. all is written in log() */ }
}