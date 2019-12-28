package tiOPF.Log

import java.util.concurrent.locks.ReentrantLock

abstract class LogToCache: LogTo() {
    val crit = ReentrantLock()
}