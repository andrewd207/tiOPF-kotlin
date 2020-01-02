package tiOPF

import tiOPF.Log.LOG
import tiOPF.Log.LogSeverity

// complete

const val CMaxPoolSize =  9999; // Maximum number of items allowed in the pool
const val CMinPoolSize =     1; // Minimum number of items to remain in the pool
const val CTimeOut     =     1; // Time (minutes) before items are purged from the pool
const val CWaitTime    =    60; // Time to wait for a pool item (in seconds)
const val CErrorPoolUnlockByData_BadData    = "Attempt to unlock item by passing object that is not owned by a PooledItem"
const val CErrorTimedOutWaitingForSemaphore = "Timed out waiting for %s.lock MinPoolSize=\"%d\", MaxPoolSize=\"%d\", LockedItemCount=\"%d\""
const val CErrorFailedToUnlockPooledItem = "Attempting to unlock PooledData which can not be found in the pool."
const val CErrorSemaphoreAvailableButNoItemsInPool = "Semaphore was available but no items " +
        "available in the pool. MaxPoolSize: %d, Current pool size: %d";

abstract class Pool(val minPoolSize: Int, val maxPoolSize: Int): BaseObject(){
    open val timeout: Int = CTimeOut
    open val waitTime: Int = CWaitTime
    private val pool = ThreadList<PooledItem>()
    val count: Int get() {
            var result = 0
            val list = pool.lockList()
            try {
                result = list.size
            }
            finally {
                pool.unlockList()
            }
            return result
        }
    val countLocked: Int get() {
            var result = 0
            val list = pool.lockList()
            try {
                list.forEach { if (it.locked) result ++ }
            }
            finally {
                pool.unlockList()
            }
            return result
        }
    fun forEach(action: (PooledItem) -> Unit){
        val list = pool.lockList()
        try {
            list.forEach(action)
        }
        finally {
            pool.unlockList()
        }
    }
    private val semaphore = Semaphore(maxPoolSize)
    private fun unlockPoolSemaphore() {
        semaphore.release()
    }
    private fun lockPoolSemaphore(): Boolean{
        return semaphore.acquire()
    }
    open fun lock(): BaseObject?{
        if (!lockPoolSemaphore())
            throw Exception(CErrorTimedOutWaitingForSemaphore.format(
                this::class.simpleName, minPoolSize, maxPoolSize, countLocked))
        val list = pool.lockList()
        try {
            var item: PooledItem? = findAvailableItemInPool(list)

            if (item == null && list.size < maxPoolSize ){
                item = addItem()
                item!!.locked = true
                LOG(
                    "A new PooledItem has been added to the pool.",
                    LogSeverity.ConnectionPool
                )
                LOG(
                    "PooledItem #${list.size} locked.",
                    LogSeverity.ConnectionPool
                )
            }

            if (item == null) {
                throw EtiOPFProgrammerException(CErrorSemaphoreAvailableButNoItemsInPool.format(maxPoolSize, list.size))
            }

            return item!!.data


        }
        finally {
            pool.unlockList()
        }
    }
    open fun unlock(pooledItemData: BaseObject){
        forEach {
            if ( it.data == pooledItemData) {
                val item = it
                item.locked = false
                LOG(
                    "PooledItem  unlocked.",
                    LogSeverity.ConnectionPool
                )
                unlockPoolSemaphore()
                return@forEach
            }
        }
        //sweepForTimeouts()

    }
    protected open fun sweepForTimeouts(){
        val count = count
        if (count < minPoolSize)
            return

        val list = pool.lockList()
        try {
            for (i in count-1 downTo 0) {
                val item = list[i]
                if (item.mustRemoveItemFromPool(count)){
                    LOG(
                        "'Pooled item (${this::class.simpleName}) #${item.index}) being removed from the pool.",
                        LogSeverity.ConnectionPool
                    )
                    list.removeAt(i)
                    LOG(
                        "There are ${list.size} items left in the pool.",
                        LogSeverity.ConnectionPool
                    )
                }
            }
        }
        finally {
            pool.lockList()
        }
    }
    protected fun remove(item: PooledItem){
        if (item.locked)
            unlock(item)

        val list = pool.lockList()
        try {
            list.remove(item)
        }
        finally {
            pool.unlockList()
        }

    }
    protected abstract fun pooledItemInterface(): IPooledItemClass
    protected abstract fun afterAddPooledItem(item: PooledItem)
    protected fun addItem(): PooledItem{
        val result = pooledItemInterface().createInstance(this)
        LOG(
            "Attempting to add pooled item #$count",
            LogSeverity.ConnectionPool
        )
        val list = pool.lockList()
        try {
            afterAddPooledItem(result)
            list.add(result)
            result.index = list.size-1
        }
        finally {
            pool.unlockList()
        }
        return result
    }
    private fun findAvailableItemInPool(list: List<PooledItem>): PooledItem?{
        var result: PooledItem? = null
        forEach {
            if (!it.locked && !it.mustRemoveItemFromPool(list.size)){
                it.index = list.indexOf(it) // pointless?
                it.locked = true
                LOG(
                    "Found an available item (#${it.index}) in pool and locked it.",
                    LogSeverity.ConnectionPool
                )
                result = it
                return@forEach
            }
        }
        return result
    }
    protected fun clear(){
        val list = pool.lockList()
        try {
            for (n in list.size-1 downTo 0) {
                val item = list[n]
                assert(item is PooledItem, { CTIErrorInvalidObject })
                list.remove(item)
            }
        }
        finally {
            pool.unlockList()
        }
    }
}