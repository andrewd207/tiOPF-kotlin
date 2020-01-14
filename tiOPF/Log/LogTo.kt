package tiOPF.Log

import tiOPF.BaseObject

abstract class LogTo: BaseObject(){
    open fun terminate(){}
    open fun purge(){ writeToOutputSynchronized()}
    protected var privTerminated = false
    val terminated: Boolean get() = privTerminated


    abstract fun log(dateTime: String, threadId: String, message: String, severity: LogSeverity)
    var sevToLog: Set<LogSeverity> = GLog.sevToLog

    protected open fun acceptEvent(dateTime: String, message: String, severity: LogSeverity): Boolean{
        return sevToLog.contains(severity)
    }
    protected open fun writeToOutputSynchronized(){ writeToOutput() }
    abstract fun writeToOutput()


}