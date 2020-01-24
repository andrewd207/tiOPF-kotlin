package tiOPF.Log

class LogToFile: LogToCache() {
    private var privOerwriteOldFile = false
    private  var privFileName: String = ""
    val filename: String get() = privFileName
    val overwriteOldFile: Boolean get() = privOerwriteOldFile
    var fileCreateAttempts: Int = 20
    var fileCreateAttemptsInterval: Int = 250 // ms
    override fun log(dateTime: String, threadId: String, message: String, severity: LogSeverity) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun writeToOutput() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}