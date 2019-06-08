package tiOPF


import kotlin.Exception

open class EtiOPFException(message: String): Exception(message)

class EtiOPFProgrammerException(message: String): EtiOPFException(message)