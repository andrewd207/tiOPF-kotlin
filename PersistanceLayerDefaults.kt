package tiOPF
// complete
class PersistanceLayerDefaults: BaseObject() {
    var persistanceLayerName: String =""
    var databaseName: String = ""
    var isDatabaseNameFilePath: Boolean = false
    var userName: String = ""
    var password: String = ""
    var params: String = ""
    var canCreateDatabase: Boolean = false
    var canDropDatabase: Boolean = false
    var canSupportMultiUser: Boolean = false
    var canSupportSQL: Boolean = false
}