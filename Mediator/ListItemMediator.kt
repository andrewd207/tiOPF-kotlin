package tiOPF.Mediator

import tiOPF.Object
import tiOPF.Published

open class ListItemMediator: Object() {
    @Published open var model: Object? = null
    @Published var active = false; set(value) {
        if (field == value)
            return
        field = value
        if (value)
            model?.attachObserver(this)
        else
            model?.detachObserver(this)
    }
    var listMediator: CustomListMediatorView? = null
    var onBeforeSetupField: OnBeforeSetupField? = null
    val displayNames: String get() = fieldsInfo!!.asString
    internal var fieldsInfoPrivate: MediatorFieldInfo? = null
    var fieldsInfo: MediatorFieldInfo? = fieldsInfoPrivate;  get() = fieldsInfoPrivate
    open fun close(){
        active = false
        model = null
    }

    override fun update(subject: Object) {
        assert(model == subject)
        super.update(subject)
        if (subject.deleted && listMediator != null)
            listMediator!!.itemDeleted(subject)
    }

    override fun stopObserving(subject: Object) {
        model = null
    }
}