package tiOPF

import java.util.concurrent.locks.ReentrantLock

const val INFINITE = Long.MAX_VALUE

class Semaphore(val maximumCount: Int) {
    private val crit = ReentrantLock()
    private var privCount = 0
    val count: Int get() = privCount
    var timeout: Long = 0
    init{
        privCount = maximumCount
    }

    fun acquire():Boolean{
        var result = false
        if (privCount > 0) {
            crit.lock()
            if (privCount > 0)
                privCount--
            crit.unlock()
            result = true
        }
        else {
            if (timeout > 0 && (timeout < INFINITE)){
                var dt = now()
                while (!result || now() < dt+timeout) {
                    crit.lock()
                    if (privCount > 0) {
                        privCount--
                        result = true
                    }
                    crit.unlock()
                }
            }
            else if (timeout == INFINITE){
                while (!result) {
                    crit.lock()
                    if (privCount > 0) {
                        privCount--
                        result = true
                    }
                    crit.unlock()
                }
            }
        }


        return result
    }
    fun release(){
        crit.lock()
        if (privCount < maximumCount)
            privCount++
        crit.unlock()
    }
}