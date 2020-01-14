package tiOPF

import java.util.*

open class ValueReference<T>(var value: T)

class ValueReferenceInt(value: Int): ValueReference<Int>(value)
class ValueReferenceDouble(value: Double): ValueReference<Double>(value)
class ValueReferenceDate(value: Date): ValueReference<Date>(value)

class ValueOut<T>{
    var value: T? = null
}