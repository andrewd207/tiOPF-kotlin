package tiOPF

class OIDStringAbs: OID() {
    override fun isNull():Boolean { return  asString === nullOIDAsString() }
    override fun setToNull() { asString = "" }
    override fun nullOIDAsString(): String { return "" }
    override var asString: String = nullOIDAsString()
    override fun clone(): OID {
        val result = OIDStringAbs()
        result.asString = this.asString
        return result

    }
    override fun equals(compareWith: OID): Boolean {
        return asString === compareWith.asString
    }
}