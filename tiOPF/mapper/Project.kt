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
        @Published var fileName = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
    }
    class Unit : Object(){
        class Reference: Object(){
            @Published var name= "" ; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
        }
        class Enum :Object(){
            class EnumItem: Object(){
                @Published var name = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
                @Published var value: Any? = null; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}} // string or int value
            }
            @Published var name = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
            @Published var set: String? = null; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
            @ItemClass(EnumItem::class)
            @Published @Item("item") val values = ObjectList<EnumItem>()
        }
        class ClassItem :Object(){
            class Prop: Object(){
                @Published var name = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
                @Published var type = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
                @Published var virtual: Boolean? = null; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
            }
            class Validator :Object(){
                enum class Type {
                    Required,
                    Greater,
                    GreaterEqual,
                    Less,
                    LessEqual,
                    NotEqual,
                    RegExp;
                    override fun toString(): String {
                        return when(this){
                            GreaterEqual -> "Greater-Equal"
                            LessEqual    -> "Less-Equal"
                            NotEqual     -> "Not-Equal"
                            RegExp       -> "Reg-Exp"
                            else         -> this.name
                        }
                    }
                    companion object {
                        fun fromString(s: String): Type{
                            return when (s.toLowerCase()){
                                "required"     -> Required
                                "greater"       -> Greater
                                "greater-equal" -> GreaterEqual
                                "less"          -> Less
                                "less-equal"    -> LessEqual
                                "not-equal"     -> NotEqual
                                "reg-exp"       -> RegExp
                                else -> throw Exception("unhandled Validator type: $s")
                            }
                        }
                    }
                }
                @Published var prop = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
                @Published var type = Type.Required; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
                @Published var value: String? = null; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
            }

            class Mapping:Object(){
                class PropMap:Object(){
                    @Published var prop = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
                    @Published var field = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
                    @Published var type = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
                }
                @Published var table = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
                @Published var pk = "OID"; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
                @Published var pkField = "OID"; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
                @Published var oidType = "String"; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
                @ItemClass(PropMap::class)
                @Published @NoParent@Item("prop-map") val mappings = ObjectList<PropMap>()

            }
            abstract class Selection:Object(){
                enum class SelectionType{
                    Func
                }
                @Published var name = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
                @Published abstract val type: SelectionType
            }
            class SelectionFunction: Selection() {
                class Param: Object(){
                    @Published var name = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
                    @Published var type = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
                    @Published var typeName = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
                    @Published var passBy = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
                    @Published var sqlParam = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
                }
                @Published override val type = SelectionType.Func

                @ItemClass(Param::class)
                @Published val params = ObjectList<Param>()
                @Published @CDATA var sql: String? = null; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
            }
            @Published var baseClass = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
            @Published var baseClassParent = "Object"; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
            @Published var autoMap = false; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
            @Published var autoCreateList = true; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
            @Published var oidType = "String"; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
            @Published var notifyObservers = true; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
            @ItemClass(Prop::class)
            @Published @Item("prop")var classProps = ObjectList<Prop>()
            @ItemClass(Validator::class)
            @Published var validators = ObjectList<Validator>()
            @Comment("Mapping into the tiOPF framework")
            @Published val mapping = Mapping()
            @ItemClass(Selection::class)
            @Published @Item("select")val selections = ObjectList<Selection>()
        }
        @Published var name = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
        @ItemClass(Reference::class)
        @Published @Item("reference") var references =  ObjectList<Reference>()
        @Comment("Enumerations defined here")
        @ItemClass(Enum::class)
        @Published @Item("enum") var enums =  ObjectList<Enum>()
        @Comment("Classes defined here")
        @ItemClass(ClassItem::class)
        @Published @Item("class") var classes = ObjectList<ClassItem>()
    }
    @Published var tabSpaces = 2; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
    @Published var beginEndTabs = 1; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
    @Published var projectName = ""; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
    @Published var enumType = EnumType.Int; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
    @Published var visibilityTabs = 0; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
    @Published var outputdir = "../bom/"; set(value) {if (value != field) { beginUpdate(); field = value; endUpdate()}}
    @Comment("Includes are added to this schema before build-time.")
    @ItemClass(Include::class)
    @Published var includes = ObjectList<Include>()
    @Comment("Units are files that will be created along with defined types, classes, etc.")
    @ItemClass(Unit::class)
    @Published@Item("unit")var projectUnits = ObjectList<Unit>()
}