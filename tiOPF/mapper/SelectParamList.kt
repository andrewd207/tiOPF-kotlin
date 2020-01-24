package tiOPF.Mapper

import tiOPF.ObjectList

class SelectParamList: ObjectList<SelectParam>() {
    fun findByName(name: String): SelectParam?{
        forEach {
            if (it.paramName.equals(name, true))
                return it
        }
        return null
    }
}