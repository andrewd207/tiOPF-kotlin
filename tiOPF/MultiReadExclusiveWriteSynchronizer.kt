package tiOPF

import java.util.concurrent.locks.ReentrantLock

open class MultiReadExclusiveWriteSynchronizer: BaseObject() {
    val criticalSection = ReentrantLock()
    private var writing = false
    private var readCount = 0
    protected open fun canLockForWrite(): Boolean{ return readCount == 0 && !writing }
    protected open fun canLockForRead(): Boolean{ return !writing }
    private fun lockForWrite(): Boolean{
        criticalSection.lock()
        var result = false
        try {
            result = canLockForWrite()
            if (result)
                writing = true
        }
        finally { criticalSection.unlock() }
        return result
    }
    private fun lockForRead(): Boolean{
        criticalSection.lock()
        var result = false
        try {
            result = canLockForRead()
            if (result)
                readCount++
        }
        finally { criticalSection.unlock() }
        return result
    }
    fun beginRead(){
        while (!lockForRead()) {
            Thread.sleep(100)

        }
    }
    fun endRead(){
        criticalSection.lock()
        try { readCount-- }
        finally { criticalSection.unlock() }
    }
    fun beginWrite(){
        while (!lockForWrite())
            Thread.sleep(100)
    }
    fun endWrite(){
        criticalSection.lock()
        try { writing = false}
        finally { criticalSection.unlock() }
    }
}