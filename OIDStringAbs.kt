package tiOPF

abstract class OIDStringAbs: OID() {
    override fun isNull():Boolean { return  asString === nullOIDAsString() }
    override fun setToNull() { asString = "" }
    override fun nullOIDAsString(): String { return "" }
    override var asString: String = nullOIDAsString()
    override fun equals(compareWith: OID): Boolean {
        return asString === compareWith.asString
    }
    override fun compare(compareWith: OID): Int {
        if (asString < compareWith.asString)
            return -1
        else if (asString > compareWith.asString)
            return 1
        return 0
    }
}