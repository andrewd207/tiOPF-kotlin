package tiOPF.Mapper

import tiOPF.ObjectList
import tiOPF.Object

class Project: Object() {
    enum class EnumType{
        Int,
        String
    }
    class Unit : Object(){
        class Reference: Object(){
            var name= ""
        }
        class Enum :Object(){
            class EnumItem: Object(){
                var name = ""
                var value: Any? = null // string or int value
            }
            var name = ""
            var set: String? = null
            @ItemClass(EnumItem::class)
            @Item("item") val values = ObjectList<EnumItem>()
        }
        class ClassItem :Object(){
            class Prop: Object(){
                var name = ""
                var type = ""
                var virtual: Boolean? = null
            }
            class Validator :Object(){
                var prop = ""
                var type = ""
                var value: String? = null
            }

            class Mapping:Object(){
                class PropMap:Object(){
                    var prop = ""
                    var field = ""
                    var type = ""
                }
                var table = ""
                var pk = "OID"
                var pkField = "OID"
                var oidType = "string"
                @ItemClass(PropMap::class)
                @NoParent@Item("prop-map") val mappings = ObjectList<PropMap>()

            }
            abstract class Selection:Object(){
                enum class SelectionType{
                    Func
                }
                abstract val type: SelectionType
            }
            class SelectionFunction: Selection() {
                class Param: Object(){
                    var name = ""
                    var type = ""
                    var typeName = ""
                    var passBy = ""
                    var sqlParam = ""
                }
                override val type = SelectionType.Func
                var name = ""
                @ItemClass(Param::class)
                val params = ObjectList<Param>()
                @CDATA var sql: String? = null
            }
            var baseClass = ""
            var baseClassParent = "Object"
            var autoMap = false
            var autoCreateList = true
            var oidType = ""
            var notifyObservers = true
            @ItemClass(Prop::class)
            @Item("prop")var classProps = ObjectList<Prop>()
            var validators = ObjectList<Validator>()
            @Comment("Mapping into the tiOPF framework")
            val mapping = Mapping()
            @ItemClass(Selection::class)
            @Item("select")val selections = ObjectList<Selection>()
        }
        var name = ""
        @ItemClass(Reference::class)
        @Item("reference") var references =  ObjectList<Reference>()
        @Comment("Enumerations defined here")
        @ItemClass(Enum::class)
        @Item("enum") var enums =  ObjectList<Enum>()
        @Comment("Classes defined here")
        @ItemClass(ClassItem::class)
        @Item("class") var classes = ObjectList<ClassItem>()
    }
    var tabSpaces = 2
    var beginEndTabs = 1
    var projectName = ""
    var enumType = EnumType.Int
    var visibilityTabs = 0
    var outputdir = "../bom/"
    @Comment("Includes are added to this schema before build-time.")
    @ItemClass(String::class)
    var includes = mutableListOf<String>()
    @Comment("Units are files that will be created along with defined types, classes, etc.")
    @ItemClass(Unit::class)
    @Item("unit")var projectUnits = ObjectList<Unit>()
}