package models

import com.avaje.ebean.config.ServerConfig
import com.avaje.ebean.event.ServerConfigStartup
import k.common.AnsiColor
import k.common.Helper
import k.ebean.config.MysqlEncryptKeyManager

/**
 * Created by kk on 14/11/28.
 */
class MyServerConfigStartup : ServerConfigStartup {
    override fun onStart(serverConfig: ServerConfig) {
        Helper.DLog(ansiColor=AnsiColor.GREEN, message = "He, MyServerConfigStartup is called...")
        serverConfig.encryptKeyManager = MysqlEncryptKeyManager()
    }
}
