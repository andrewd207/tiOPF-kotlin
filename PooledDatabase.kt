package tiOPF

class PooledDatabase(owner: Pool): PooledItem(owner) {
    override fun mustRemoveItemFromPool(listCount: Int): Boolean {
        assert(testValid(this, Database::class), { CTIErrorInvalidObject })
        return super.mustRemoveItemFromPool(listCount) || (data as Database).errorInLastCall
    }

}