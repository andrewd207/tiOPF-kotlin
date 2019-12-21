package tiOPF

abstract class VisOwnedQrySelectAbs: ObjectVisitor(){
    protected open fun beforeRow(){}
    protected open fun mapRowToObject(){
        throw EtiOPFException("mapRow is not overridden for ${this.className()}")
    }
    protected open fun afterRow(){}
    protected abstract fun openQuery()
}


open class VisitorSelect: VisOwnedQrySelectAbs(){
    override fun openQuery() {
        if (GTIOPFManager().terminated)
            return
        query!!.open()
    }

}

open class VisitorUpdate: ObjectVisitor() {
    protected open fun afterExecSql(rowsAffected: Int) {
        // this gets called only if query.supportsRowsAffected
        // implement in concrete visitor
        // You can do something like:
        // if (rowsAffected==0) throw ERecordAlreadyChanged("Another user changed row.")
    }

    override fun execute(visited: Visited?) {
        if (GTIOPFManager().terminated)
            return
        super.execute(visited)
        if (!acceptVisitor())
            return
        init()
        try {
            val start = tiGetTickCount()
            setupParams()
            val rowsAffected = query!!.execSQL()
            if (query!!.supportsRowsAffected)
                afterExecSql(rowsAffected.toInt())
            logQueryTiming(className(), tiGetTickCount() - start, 0)
        }
        finally {
            unInit()
        }
    }

}
