package k.common.apidoc

import play.routing.Router
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Created by kk on 16/9/12.
 */
@Singleton
class DefinedApis
@Inject
private constructor(private val routerProvider: Provider<Router>) {


    fun ApiRoutes(): List<RouteInfo> {
        return this.routerProvider.get().documentation().map {
            RouteInfo(httpMethod = it.httpMethod,
                    url = it.pathPattern,
                    controllerPath = it.controllerMethodInvocation)
        }.filter { it.JsonApiAnnotation() != null }
    }

    fun JsonApis(): ApiDefinition {
        val apis = ApiDefinition()
        ApiRoutes().map { it.BuildApiInfo() }.forEach {
            apis.AddApiInfo(it)
        }

        return apis
    }
}