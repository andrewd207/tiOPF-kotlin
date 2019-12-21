package tiOPF

abstract class QuerySQL: Query() {
    protected fun whereClause(where: QueryParams?): String{
        var result = ""
        if (where == null || where.isEmpty())
            return ""
        where.forEach {
            result = tiAddTrailingValue(result,"\n")
            result+= it.name+" =:"+ it.name
        }
        if (result.isNotEmpty())
            result = "\nwhere\n$result"
        return result
    }
    protected fun sqlAndParamsAsString(): String{
        var result = "SQL:"
        for (s in sql) {
            result+="\n    $s"
        }
        var params = ""
        for (i in 0 until paramCount()){
            params +="\n    ${paramName(i)}:= "
            if (getParamIsNull(paramName(i)))
                params+="Null"
            else
                params+= tiAddEllipsis(getParamAsString(paramName(i)), 120)
        }
        if (params.isNotEmpty())
            result += "\n\nParams:$params"
        return result
    }

    fun paramsAsStringList(): List<String>{
        val result = List<String>()
        try {
            for (i in 0 until paramCount()) {
                val s = if (getParamIsNull(paramName(i)))
                    "null"
                else
                    getParamAsString(paramName(i))

                result.add(paramName(i) + '=' + s)
            }
        }
        catch (e: Exception){
            LOGERROR(e, true)
        }
        return result
    }

    internal fun logParams(){
        val line = "%s: [Param %d] %s"
        val list: List<String> = paramsAsStringList()
        list.forEachIndexed{index, it ->
                LOG(line.format(className(), index+1, it, LogSeverity.lsSQL))
        }
    }

    override fun selectRow(tableName: String, where: QueryParams, criteria: Criteria?) {
        var criteriaWhere = ""
        var lWhere = ""
        var sql = ""

        if ( where != null) {
            where.forEach {
                lWhere = tiAddTrailingValue(lWhere, " and \n")
                lWhere+= it.name + " = :" + it.name
            }
        }

        if (criteria != null && criteria.hasCriteria){
            val criteriaParams = QueryParams()
            criteriaWhere = criteria.asSql(criteriaParams)
            if (criteriaWhere.isNotEmpty()){
                lWhere = tiAddTrailingValue(lWhere, " and \n")
                lWhere+= criteriaWhere
            }
            if (lWhere.isNotEmpty())
                sql = "select * from $tableName\n where \n $lWhere"
            else
                sql = "select * from $tableName"

            if (criteria != null && criteria.hasOrderBy)
                sql+= "\n"+ criteria.orderByAsSQL()

            sqlText = sql
            assignParams(where, criteriaParams)
            open()
        }


    }

    override fun insertRow(tableName: String, params: QueryParams) {
        var sql = "insert into $tableName"
        var fields = ""
        var lParams = ""
        params.forEach {
            fields = tiAddTrailingValue(fields, ",\n")
            lParams = tiAddTrailingValue(lParams, ",\n")
            fields+= it.name
            lParams+= ":"+it.name
        }
        sql+="\n($fields)\n" +
                "values\n" +
                "(\n$lParams\n)"
        sqlText = sql
        assignParams(params)
        execSQL()

    }

    override fun deleteRow(tableName: String, where: QueryParams) {
        sqlText = "delete from $tableName ${whereClause(where)}"
        assignParams(where)
        execSQL()
    }

    override fun updateRow(tableName: String, params: QueryParams, where: QueryParams) {
        var sql = "update $tableName set "
        var fields = ""
        params.forEach {
            fields = tiAddTrailingValue(fields, ",\n")
            fields+= it.name + " =:" + it.name
        }

        sql += "\n"+fields+whereClause(where)
        sqlText = sql
        execSQL()
    }
}