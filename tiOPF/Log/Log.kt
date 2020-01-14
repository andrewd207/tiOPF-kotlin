package tiOPF.Log

import tiOPF.BaseObject
import tiOPF.now
import java.util.concurrent.locks.ReentrantLock
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


var GLog: Log = Log()
    set(value) {
        field = if (value == null || value !is Log)//should never happen
            Log()
        else
            value
    }



fun LOGERROR(exception: Exception, throwException: Boolean = true) {
    GLog.log(exception.message!!, LogSeverity.Error)
    if (throwException)
        throw exception
}

fun LOG(array: Any, severity: LogSeverity = LogSeverity.Normal) {
    GLog.log(array.toString(), severity)
}

enum class LogSeverity {
    Normal
    ,UserInfo
    ,ObjCreation
    ,Visitor
    ,ConnectionPool
    ,AcceptVisitor
    ,QueryTiming
    ,Debug
    ,Warning
    ,Error
    ,SQL
}

val sevToLogMinimal = setOf(LogSeverity.UserInfo, LogSeverity.Warning, LogSeverity.Error)
val sevToLogMedium = sevToLogMinimal + setOf(LogSeverity.Normal)
val sevToLogVerbose = sevToLogMedium + setOf( LogSeverity.ObjCreation,
                                                            LogSeverity.Visitor,
                                                            LogSeverity.ConnectionPool,
                                                            LogSeverity.AcceptVisitor,
                                                            LogSeverity.QueryTiming,
                                                            LogSeverity.Debug,
                                                            LogSeverity.SQL)

enum class LogLevel {
    Minimal,
    Medium,
    Verbose,
    Custom
}
class Log: BaseObject() {
    private val crit = ReentrantLock()
    private val logToList = mutableListOf<LogTo>()

    fun log(message: String, severity: LogSeverity = LogSeverity.Normal) {
        val now = now().toString()
        val threadId = Thread.currentThread().id.toString()
        crit.lock()
        try {
            logToList.forEach {
                it.log(now, threadId, message, severity)
            }
        }
        finally {
            crit.unlock()
        }
    }

    fun log(message: String, args: Any?, severity: LogSeverity = LogSeverity.Normal) {
        log(String.format(message, args), severity)
    }
    fun findByLogClass(kClass: KClass<*>): LogTo?{
        crit.lock()
        try {
            logToList.forEach {
                if (it::class.isSubclassOf(kClass))
                    return it
            }
            return null
        }
        finally {
            crit.unlock()
        }
    }
    fun isRegistered(kClass: KClass<*>): Boolean {
        return findByLogClass(kClass) != null
    }
    fun registerLog(log: LogTo){
        crit.lock()
        try {
            logToList.add(log)
        }
        finally {
            crit.unlock()
        }
    }

    private var settingLevel = false

    var sevToLog: Set<LogSeverity> = setOf() ; set(value) {
        crit.lock()
        try {
            if (!settingLevel) {
                settingLevel = true
                when (value) {
                    sevToLogMinimal -> logLevel = LogLevel.Minimal
                    sevToLogMedium -> logLevel = LogLevel.Medium
                    sevToLogVerbose -> logLevel = LogLevel.Verbose
                    else -> logLevel = LogLevel.Custom
                }
            }

            field = value
            logToList.forEach {
                it.sevToLog = value
            }
        }
        finally {
            settingLevel = false
            crit.unlock()
        }
    }
    var logLevel: LogLevel = LogLevel.Minimal ; set(value) {
        if (!settingLevel) {
            settingLevel = true
            when (value) {
                LogLevel.Minimal -> sevToLog = sevToLogMinimal
                LogLevel.Medium -> sevToLog = sevToLogMedium
                LogLevel.Verbose -> sevToLog = sevToLogVerbose
                LogLevel.Custom -> {
                }
                else -> throw Exception("Unknown LogLevel")
            }
        }
        field = value
    }
    val logToFileName: String get() {
        val logTo = findByLogClass(LogToFile::class) as LogToFile
        if (logTo != null)
            return logTo.filename
        else
            return ""
    }
    fun purge(){
        crit.lock()
        try {
            logToList.forEach {
                it.purge()
            }
        }
        finally {
            crit.unlock()
        }

    }
}