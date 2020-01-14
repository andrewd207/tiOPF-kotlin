package tiOPF

class ObjectError: Object() {
    var errorProperty: String =""
    var errorMessage: String =""
    var errorCode: Int = 0

    override var owner: Object?
        get() {return super.owner }
        set(value) {
            if (value != null)
                assert(ObjectErrorList::class.isInstance(value), { "Owner must be ${ObjectErrorList::class.simpleName}"})
            super.owner = value
        }
}