package tiOPF

open class Visited: BaseObject{
    constructor() :super()
    @Published
    open val caption: String = this::class.toString()

    fun iterateAssignTouched(visitor: Visitor, touchedByVisitorList: TouchedByVisitorList){
        // TODO

    }


}