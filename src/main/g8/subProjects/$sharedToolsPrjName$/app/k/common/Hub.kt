package k.common

import play.Application
import play.Configuration
import play.cache.CacheApi
import play.data.FormFactory

/**
 * Created by kk on 16/5/6.
 */
object Hub {

    private var application: Application? = null

    fun setApplication(application: Application) {
        Hub.application = application
    }

    fun application(): Application {
        if (Hub.application == null) {
            throw BizLogicException("OnKBaseStartStop 模块没有完成初始化")
        }
        return Hub.application as Application
    }


    private var configuration: Configuration? = null

    fun setConfiguration(configuration: Configuration) {
        Hub.configuration = configuration
    }

    fun configuration(): Configuration {
        if (Hub.configuration == null) {
            throw BizLogicException("OnKBaseStartStop 模块没有完成初始化")
        }
        return Hub.configuration as Configuration
    }

    private var cacheApi: CacheApi? = null

    fun setCacheApi(cacheApi: CacheApi) {
        Hub.cacheApi = cacheApi
    }

    fun cacheApi(): CacheApi {
        if (Hub.cacheApi == null) {
            throw BizLogicException("OnKBaseStartStop 模块没有完成初始化")
        }
        return Hub.cacheApi as CacheApi
    }

    private var formFactory: FormFactory? = null

    fun setFormFactory(formFactory: FormFactory) {
        Hub.formFactory = formFactory
    }

    fun formFactory(): FormFactory {
        if (Hub.formFactory == null) {
            throw BizLogicException("OnKBaseStartStop 模块没有完成初始化")
        }
        return Hub.formFactory as FormFactory
    }
}
