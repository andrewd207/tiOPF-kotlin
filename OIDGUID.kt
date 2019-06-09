package tiOPF

class OIDGUID: OIDStringAbs() {
    override fun clone(): OID {
        val result = OIDGUID()
        result.asString = this.asString
        return result
    }

}
