package tiOPF.Mapper

import tiOPF.Object

class SelectParam(var paramName: String, var paramType: MapPropType, var sqlParamName: String, var value: Any?) : Object() {
    var paramTypeName = ""
    var typeName = ""
    var passBy = ""
}