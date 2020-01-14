package tiOPF

const val CStreamStartSize = 2000000;
const val CStreamGrowBy    =  500000;

abstract class VisStream: Visitor() {
    abstract val stream: PreSizedStream
    protected open fun write(value: String){
        stream.write(value)
    }
    protected open fun writeln(value: String){
        stream.writeln(value)
    }
}

open class VisStringStream: VisStream(){
    override val stream: PreSizedStream = PreSizedStream(CStreamStartSize, CStreamGrowBy)
    open val text: String get() {return stream.asString}
}