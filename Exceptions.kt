package tiOPF


import kotlin.Exception

const val CTIOPFExcMsgCanNotConnectToDatabase = "Can not connect to database"
const val CTIOPFExcMsgCanNotFindDatabase      = "Can not find database"
const val CTIOPFExcMsgCanNotConnectToDatabaseAfterRetry = "Can not connect to database after %d attempts at %d sec intervals"

const val CTIOPFExcMsgInvalidUserNamePassword = "Invalid username and password combination"
const val CTIOPFExcMsgCanNotCreateDatabase    = "Can not create database"
const val CTIOPFExcMsgDatabaseAlreadyExists   = "Database already exists"
const val CTIOPFExcCanNotDetermineSQLType     = "Can not determine the type of the SQL (SELECT, UPDATE, ALTER, etc)\n\n%s"
const val CTIOPFExcMsgTIQueryType             = "Invalid TtiQueryType"
const val CTIOPFExcMsgInvalidExecutionPath    = "Invalid execution path. You should not be in this part of the program. Code is being executed that has not been implemented."
const val CTIOPFExcMsgFileNotFound            = "File not found: \"%s\""
const val CTIOPFUnsupportedFieldType          = "Unsupported field type: %s"

open class EtiOPFException(message: String): Exception(message)

class EtiOPFProgrammerException(message: String): EtiOPFException(message)
open class EtiOPFDBException(persistenceLayerName: String, dbName: String, user: String, password: String, message: String = ""):
    EtiOPFException("Database name:     $dbName\n" +
                    "User name:         $user\n" +
                    "Password:          $password\n" +
                    "Persistence layer: $persistenceLayerName"+
                    { if (message.isNotEmpty()) "\nMessage:\n$message"})

open class EtiOPFDBExceptionCanNotConnect(persistenceLayerName: String, dbName: String, user: String, password: String, message: String = ""):
    EtiOPFDBException(persistenceLayerName, dbName, user, password, CTIOPFExcMsgCanNotConnectToDatabase+"\n"+message)

class EtiOPFDBExceptionUserNamePassword(persistenceLayerName: String, dbName: String, user: String, password: String, message: String = ""):
    EtiOPFDBExceptionCanNotConnect(persistenceLayerName, dbName, user, password, CTIOPFExcMsgInvalidUserNamePassword+"\n"+message)