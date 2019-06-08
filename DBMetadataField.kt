package tiOPF
// complete
open class DBMetadataField: Object() {
    var name = ""
    var kind = Query.QueryFieldKind.String
    var kindAsString: String
        get() { return queryFieldKindToString(kind)}
        set(value) {
            Query.QueryFieldKind.values().forEach {
                if (it.toString() === value) {
                    kind = it
                    return
                }
            }
        }
    var width = 0

    protected fun getRPadName(): String{
        return name.padEnd((owner as DBMetadataTable).maxFieldNameWidth)
    }
    override fun clone(): DBMetadataField{
        return super.clone() as DBMetadataField

    }
}