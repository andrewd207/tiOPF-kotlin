package tiOPF

class PooledDatabase(owner: Pool): PooledItem(owner) {
    companion object: IPooledItemClass{
        override fun createInstance(owner: Pool): PooledItem {
            return PooledDatabase(owner)
        }
    }
    override fun mustRemoveItemFromPool(listCount: Int): Boolean {
        assert(testValid(this, Database::class), { CTIErrorInvalidObject })
        return super.mustRemoveItemFromPool(listCount) || (data as Database).errorInLastCall
    }

}