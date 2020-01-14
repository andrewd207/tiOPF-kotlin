package tiOPF

class PreSizedStream(private val initialSize: Int, private val growBy: Int): BaseObject() {
    private var data: ByteArray = ByteArray(initialSize)
    private var privSize: Int = initialSize
    private var privPosition: Int = 0
    fun clear(){
        privSize = 0
        data = ByteArray(initialSize)
        privPosition = 0
    }
    fun write(value: String){
        val valueBytes = value.toByteArray()
        val neededSize = privSize + valueBytes.size
        if (neededSize > data.size){
            var newSize = data.size
            while(newSize < neededSize)
                newSize+=growBy
            val newData = ByteArray(newSize)
            data.copyInto(newData)
            data = newData
        }
        valueBytes.copyInto(data, privPosition)
        privPosition+=valueBytes.size
    }
    fun writeln(value: String = ""){
        write(value+"\n")
    }
    val asString: String get() {
        return data.toString()
    }
    fun saveToFile(fileName: String){

    }
    val size: Int get() = privSize
    val position: Int get() = privPosition
}
