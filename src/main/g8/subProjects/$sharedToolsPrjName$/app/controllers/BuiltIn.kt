package controllers

import k.aop.annotations.Comment
import k.aop.annotations.JsonApi
import k.common.apidoc.ApiDefinition
import k.common.apidoc.DefinedApis
import k.common.template.ResourceTemplateHelper
import k.controllers.JsonpController
import k.ebean.DbIndex
import play.mvc.Result
import javax.inject.Inject

/**
 * Created by kk on 16/9/18.
 */

@Comment("内置 API 方法")
class BuiltIn
@Inject
constructor(var definedApis: DefinedApis,
            var environment: play.api.Environment,
            var configuration: play.api.Configuration) : JsonpController() {

    @Comment("列出所有的 API")
    @JsonApi(ReplyClass = ApiDefinition::class)
    fun Apis(): Result {
        val reply = definedApis.JsonApis()
        return ok(reply.toJsonStr()).`as`("application/json; charset=UTF-8")
    }

    fun ApiIndex(): Result {
        val apiDef = definedApis.JsonApis()
        val html = ResourceTemplateHelper.Process(DefinedApis::class.java, "/ApiDocTemplates/ApiTest.html", apiDef)

        return ok(html).`as`("text/html; charset=UTF-8")
    }

    fun ApiTest(apiUrl: String): Result {
        val apiInfo = definedApis.ApiRoutes().find { it.url == apiUrl }!!.BuildApiInfo()
        val html = ResourceTemplateHelper.Process(DefinedApis::class.java, "/ApiDocTemplates/ApiSample.html", apiInfo)
        return ok(html).`as`("text/html; charset=UTF-8")
    }

    fun ApiMarkdown(): Result {
        val apiDef = definedApis.JsonApis()
        val md = ResourceTemplateHelper.Process(DefinedApis::class.java, "/ApiDocTemplates/ApiDoc", apiDef)

        return ok(md).`as`("text/plain; charset=UTF-8")
    }

    fun CreateIndexSql(): Result {
        return ok(DbIndex.GetCreateIndexSql()).`as`("text/plain; charset=UTF-8")
    }
}