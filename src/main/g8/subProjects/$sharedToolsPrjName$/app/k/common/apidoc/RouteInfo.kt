package k.common.apidoc


import jodd.util.ReflectUtil
import k.aop.annotations.Comment
import k.aop.annotations.JsonApi
import k.common.Helper

import java.lang.reflect.Method

/**
 * Created by kk on 15/7/20.
 */
class RouteInfo(

        @Comment("API http method: GET or POST")
        val httpMethod: String,

        @Comment("API url")
        val url: String,

        @Comment("控制器方法全路径")
        val controllerPath: String) {

    fun ControllerClass(): Class<*> {
        return RouteInfo::class.java.classLoader.loadClass(this.ControllerClassName())
    }

    fun ControllerClassName(): String {
        val l = controllerPath.lastIndexOf('.')
        return controllerPath.substring(0, l)
    }

    fun ControllerMethodName(): String {
        val l = controllerPath.lastIndexOf('.')
        val r = controllerPath.indexOf('(')
        if (r == -1) {
            // 说明没有 "(",
            return controllerPath.split(".").last()
        }
        return controllerPath.substring(l + 1, r)
    }

    fun ControllerMethod(): Method {
        val methods = ReflectUtil.getAccessibleMethods(ControllerClass())
        val methodName = ControllerMethodName()
        return methods.find { it.name == methodName }!!
    }

    fun JsonApiAnnotation(): JsonApi? {
        val method = ControllerMethod()
        val apiAnno = method.getAnnotation(JsonApi::class.java)
        return apiAnno
    }

    fun ControllerComment(): String {
        val controller_cls = ControllerClass()
        if (controller_cls.getAnnotation(Comment::class.java) != null) {
            return controller_cls.getAnnotation(Comment::class.java).value
        } else {
            return ControllerClassName()
        }
    }

    fun BuildApiInfo() : ApiInfo {
        return ApiInfo(url = this.url,
                httpMethod = this.JsonApiAnnotation()!!.ApiMethodType,
                controllerClass = this.ControllerClassName(),
                methodName = this.ControllerMethodName(),
                replyKClass = this.JsonApiAnnotation()!!.ReplyClass,
                postDataKClass = this.JsonApiAnnotation()!!.PostDataClass)

    }

}
