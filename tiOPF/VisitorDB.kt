package tiOPF

abstract class VisOwnedQrySelectAbs: ObjectVisitor(){
    protected open fun beforeRow(){}
    protected open fun mapRowToObject(){
        throw EtiOPFException("mapRow is not overridden for ${this.className()}")
    }
    protected open fun afterRow(){}
    protected abstract fun openQuery()
    override fun execute(visited: Visited?) {
        fun scanQuery(){
            query!!.continueScan = true
            while (!query!!.eof && query!!.continueScan &&  !GTIOPFManager().terminated){
                beforeRow()
                mapRowToObject()
                afterRow()
                query!!.next()
            }
        }
        if (GTIOPFManager().terminated)
            return
        super.execute(visited)

        if (!acceptVisitor())
            return
        assert(database != null, {"Database connection not set"})

        if (visited != null)
            this.visited = visited as Object
        else
            this.visited = null

        init()
        setupParams()
        var start = tiGetTickCount()
        openQuery()
        try {
            val queryTime = tiGetTickCount() - start
            start = tiGetTickCount()
            scanQuery()
            logQueryTiming(className(), queryTime, tiGetTickCount()-start)
        }
        finally {
            query!!.close()
        }
    }
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
            logQueryTiming(className(), tiGetTickCount() - start, 0.toULong())
        }
        finally {
            unInit()
        }
    }

}
