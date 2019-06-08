package tiOPF

class ObjectErrorList: ObjectList<ObjectError>() {
    fun addError(errorProperty: String, errorMessage: String, errorCode: Int = 0){
        val error = ObjectError()
        error.errorProperty = errorProperty
        error.errorMessage = errorMessage
        error.errorCode = errorCode
        error.owner = this
        add(error)
    }

    fun addError(errorMessage: String){
        addError("", errorMessage, 0)
    }
    fun findByMessage(message: String): ObjectError?{
        items.forEach {
            if (it.errorMessage.equals(message) )
                return it
        }
        return null
    }

    fun findByErrorCode(errorCode: Int): ObjectError?{
        items.forEach {
            if (it.errorCode == errorCode )
                return it
        }
        return null
    }

    fun findByErrorProperty(property: String): ObjectError?{
        items.forEach {
            if (it.errorProperty.equals(property) )
                return it
        }
        return null
    }

    open val asString: String
        get() {
            var result = ""
            for (item in items) {
                if (result.isNotEmpty())
                    result+= tiLineEnd()
                result+= item.errorMessage
            }
            return result
        }
}