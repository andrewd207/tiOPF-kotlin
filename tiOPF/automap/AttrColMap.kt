package tiOPF.automap

import tiOPF.Object
import tiOPF.tiOPF.automap.DBColMap

// complete
class AttrColMap(var attrMap: AttrMap, var dbColMap: DBColMap): Object() {
    override val caption: String
        get() = attrMap.caption + "/" + dbColMap.caption
    val ownerAsClassDBMappingManager: ClassDBMappingManager get() = super.owner as ClassDBMappingManager

}