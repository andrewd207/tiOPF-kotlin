package tiOPF.Mapper

import tiOPF.ObjectList
import tiOPF.Object
import tiOPF.Published

class Project: Object() {
    enum class EnumType{
        Int,
        String
    }
    class Include: Object(){
        @Published var fileName = ""
    }
    class Unit : Object(){
        class Reference: Object(){
            @Published var name= "" ; set(value) {beginUpdate(); field = value; endUpdate()}
        }
        class Enum :Object(){
            class EnumItem: Object(){
                @Published var name = ""; set(value) {beginUpdate(); field = value; endUpdate()}
                @Published var value: Any? = null // string or int value
            }
            @Published var name = ""; set(value) {beginUpdate(); field = value; endUpdate()}
            @Published var set: String? = null
            @ItemClass(EnumItem::class)
            @Published @Item("item") val values = ObjectList<EnumItem>()
        }
        class ClassItem :Object(){
            class Prop: Object(){
                @Published var name = ""; set(value) {beginUpdate(); field = value; endUpdate()}
                @Published var type = ""
                @Published var virtual: Boolean? = null
            }
            class Validator :Object(){
                @Published var prop = ""; set(value) {beginUpdate(); field = value; endUpdate()}
                @Published var type = ""
                @Published var value: String? = null
            }

            class Mapping:Object(){
                class PropMap:Object(){
                    @Published var prop = ""; set(value) {beginUpdate(); field = value; endUpdate()}
                    @Published var field = ""
                    @Published var type = ""
                }
                @Published var table = ""
                @Published var pk = "OID"
                @Published var pkField = "OID"
                @Published var oidType = "string"
                @ItemClass(PropMap::class)
                @Published @NoParent@Item("prop-map") val mappings = ObjectList<PropMap>()

            }
            abstract class Selection:Object(){
                enum class SelectionType{
                    Func
                }
                @Published var name = ""; set(value) {beginUpdate(); field = value; endUpdate()}
                @Published abstract val type: SelectionType
            }
            class SelectionFunction: Selection() {
                class Param: Object(){
                    @Published var name = ""; set(value) {beginUpdate(); field = value; endUpdate()}
                    @Published var type = ""
                    @Published var typeName = ""
                    @Published var passBy = ""
                    @Published var sqlParam = ""
                }
                @Published override val type = SelectionType.Func

                @ItemClass(Param::class)
                @Published val params = ObjectList<Param>()
                @Published @CDATA var sql: String? = null
            }
            @Published var baseClass = ""; set(value) {beginUpdate(); field = value; endUpdate()}
            @Published var baseClassParent = "Object"
            @Published var autoMap = false
            @Published var autoCreateList = true
            @Published var oidType = "String"
            @Published var notifyObservers = true
            @ItemClass(Prop::class)
            @Published @Item("prop")var classProps = ObjectList<Prop>()
            @Published var validators = ObjectList<Validator>()
            @Comment("Mapping into the tiOPF framework")
            @Published val mapping = Mapping()
            @ItemClass(Selection::class)
            @Published @Item("select")val selections = ObjectList<Selection>()
        }
        @Published var name = "" ; set(value) { beginUpdate(); field = value; endUpdate()}
        @ItemClass(Reference::class)
        @Published @Item("reference") var references =  ObjectList<Reference>()
        @Comment("Enumerations defined here")
        @ItemClass(Enum::class)
        @Published @Item("enum") var enums =  ObjectList<Enum>()
        @Comment("Classes defined here")
        @ItemClass(ClassItem::class)
        @Published @Item("class") var classes = ObjectList<ClassItem>()
    }
    @Published var tabSpaces = 2
    @Published var beginEndTabs = 1
    @Published var projectName = ""; set(value) {beginUpdate(); field = value; endUpdate()}
    @Published var enumType = EnumType.Int
    @Published var visibilityTabs = 0
    @Published var outputdir = "../bom/"
    @Comment("Includes are added to this schema before build-time.")
    @ItemClass(Include::class)
    @Published var includes = ObjectList<Include>()
    @Comment("Units are files that will be created along with defined types, classes, etc.")
    @ItemClass(Unit::class)
    @Published@Item("unit")var projectUnits = ObjectList<Unit>()
}