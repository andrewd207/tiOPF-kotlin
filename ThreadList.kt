package tiOPF

import java.util.concurrent.locks.ReentrantLock

class ThreadList<T> {
    private val criticalSection = ReentrantLock()
    private val privList = List<T>()
    fun add(item: T){
        lockList()
        try {
            if (duplicates == Duplicate.Ignore || privList.indexOf(item) == -1)
                privList.add(item)
            else if (duplicates == Duplicate.Error)
                throw Exception("Duplicate item added to list")

        }
        finally {
            unlockList()
        }
    }
    fun clear(){
        lockList()
        try {
            privList.clear()
        }
        finally {
            unlockList()
        }

    }

    fun lockList(): List<T>{
        criticalSection.lock()
        return privList
    }

    fun remove(item: T){
        lockList()
        try {
            privList.remove(item)
        }
        finally {
            unlockList()
        }

    }

    fun unlockList(){
        criticalSection.unlock()

    }

    var duplicates = Duplicate.Ignore
}