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

    private var apiRoutes:List<RouteInfo>? = null

    fun ApiRoutes(): List<RouteInfo> {
        if (apiRoutes == null) {
            apiRoutes = this.routerProvider.get().documentation().map {
                RouteInfo(httpMethod = it.httpMethod,
                        url = it.pathPattern,
                        controllerPath = it.controllerMethodInvocation)
            }.filter { it.JsonApiAnnotation() != null }
        }
        return apiRoutes!!
    }

    private var jsonApis : ApiDefinition? = null
    fun JsonApis(): ApiDefinition {
        if (jsonApis == null) {
            jsonApis = ApiDefinition()
            ApiRoutes().map { it.BuildApiInfo() }.forEach {
                jsonApis!!.AddApiInfo(it)
            }
        }

        return jsonApis!!
    }

    fun IsJsonApi(urlPath: String): Boolean {
        return ApiRoutes().find { it.url == urlPath } != null
    }
}