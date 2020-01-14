package tiOPF

data class DBConnectionParams(var databaseName: String,
                              var userName: String,
                              var password: String,
                              var params: String) {
}