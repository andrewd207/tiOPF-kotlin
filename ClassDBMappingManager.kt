package tiOPF

import kotlin.reflect.KClass

class ClassDBMappingManager: Object() {
    val dbMaps: DBMaps
    fun registerMapping(databaseName: String, kClass: KClass<*>, tableName: String, attrName: String, colName: String, pkInfo: PKInfo? = null){
        val dbMap = db


    }
    fun registerMapping(kClass: KClass<*>, tableName: String, attrName: String, colName: String, pkInfo: PKInfo? = null){
        registerMapping("", kClass, tableName, attrName, colName, pkInfo)


    }
}