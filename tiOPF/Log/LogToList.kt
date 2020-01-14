package tiOPF.Log

import java.util.concurrent.locks.ReentrantLock

class LogToList: LogTo(){
    protected val crit = ReentrantLock()
    override fun log(dateTime: String, threadId: String, message: String, severity: LogSeverity) {
        if (!acceptEvent(dateTime, message, severity))
            return

        crit.lock()
        try {
            val event = LogEvent(dateTime, message, "", severity, threadId)
            items.add(event)
        }
        finally {
            crit.unlock()
        }
    }
    fun clear(){
        crit.lock()
        try {
            items.clear()
        }
        finally {
            crit.unlock()
        }
    }

    override fun purge() {
        super.purge()
        clear()
    }

    override fun writeToOutput() {/* do nothing */ }

    private val items: MutableList<LogEvent> = mutableListOf()
    val count: Int get (){return items.count()}
    val asString: String get() {
        var result = ""
        crit.lock()
        try {
            items.forEach{
                result += "[]%s %s".format(it.severity.toString(), it.message)
            }
        }
        finally {
            crit.unlock()
        }
        return result
    }
}
