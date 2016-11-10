package k.ebean.config

import com.avaje.ebean.config.EncryptKey
import com.avaje.ebean.config.EncryptKeyManager
import k.common.AnsiColor
import k.common.Helper

class MysqlEncryptKeyManager : EncryptKeyManager {

    init {
        Helper.DLog(ansiColor= AnsiColor.GREEN, message = "Ebean Server load MysqlEncryptKeyManager")
    }

    override fun getEncryptKey(tableName: String, columnName: String): EncryptKey {

        return MysqlEncrptKey(tableName, columnName)
    }

    override fun initialise() {

    }

    internal inner class MysqlEncrptKey( val tableName: String,  val columnName: String) : EncryptKey {

        override fun getStringValue(): String {
            return "$tableName@$columnName"
        }

    }
}
