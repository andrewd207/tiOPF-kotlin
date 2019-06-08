package tiOPF

const val CuStandardTask_ReadPK   = "StandardTask_ReadPK"
const val CuStandardTask_ReadThis = "StandardTask_ReadThis"
const val CuStandardTask_Read     = "StandardTask_Read"
const val CuStandardTask_Save     = "StandardTask_Save"

const val CLineEnding = '\n'
const val CTIErrorInternal = "tiOPF Internal Error: "
const val CTIErrorInvalidObject = CTIErrorInternal + " BaseObject.TestValid failed"
const val CErrorCallToTIPerMgrWhileShuttingDown = "Call to tiPerMgr while shutting down"
const val CErrorAttemptToRegisterDuplicateVisitor = "Attempt to register duplicate visitor %s";
const val CErrorInvalidVisitorGroup = "Attempt to execute visitors for an unknown visitor group \"%s\"";

const val CErrorInvalidQueryFieldKind    = "Invalid QueryFieldKind"
const val CErrorInvalidQueryFieldKindStr = "Invalid QueryFieldKind <%s>"
const val CErrorInvalidTtiQueryFieldKind = CErrorInvalidQueryFieldKind

const val CDefaultMinPoolSize = 1
const val CDefaultMaxPoolSizeMultiUser = 9999
const val CDefaultMaxPoolSizeSingleUser = 1

const val CPasswordMasked = "Password masked from view"
const val CErrorInconsistentTableNames = "Inconsistent table names found in DBColMap"
const val CErrorQueryReturnedMoreThanOneRow = "Query returned \"%d\" rows when 1 was expected"

val CBoolTrueArray = arrayOf("true", "t", "y", "1")
val CBoolFalseArray = arrayOf("false", "f", "n", "0")