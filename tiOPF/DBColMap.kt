package tiOPF
// complete
enum class ClassDBMapRelationshipType {
    Invalid,
    Primary,
    Foreign,
    Readable
}



typealias PKInfo = Set<ClassDBMapRelationshipType> // no sure how to do sets

class DBColMap(var colName: String = "", var pkInfo: PKInfo = setOf()): Object() {
    override val caption: String
        get() = colName
    val ownerAsDBTableMap: DBTableMap get() = owner as DBTableMap

}