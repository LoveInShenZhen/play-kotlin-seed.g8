package k.controllers

import com.fasterxml.jackson.databind.JsonNode
import jodd.util.StringUtil
import k.reply.ReplyBase
import org.apache.commons.codec.EncoderException
import org.apache.commons.codec.net.URLCodec
import org.apache.commons.lang3.StringUtils
import play.libs.Jsonp
import play.mvc.Controller
import play.mvc.Http
import play.mvc.Result
import play.mvc.Results

/**
 * Created by kk on 14-4-25.
 */
open class JsonpController : Controller() {
    companion object {


        fun ok(content: JsonNode): Result {

            ctx().args.put("api_reply", content)

            val callback = Http.Context.current().request().getQueryString("callback")
            if (StringUtils.isBlank(callback)) {
                return Controller.ok(content)
            } else {
                return Results.ok(Jsonp(callback, content))
            }
        }

        fun ok(reply: ReplyBase): Result {
            return Controller.ok(reply.toJsonNode())
        }

        //    Nginx 配置:
        //
        //    proxy_http_version 1.1;
        //    proxy_set_header   Host             $http_host;
        //    proxy_set_header   X-Real-IP        $remote_addr;
        //    proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;

        fun ClientIp(): String {
            val real_ip = Controller.request().getHeader("X-Real-IP")
            if (StringUtil.isBlank(real_ip)) {
                return Controller.request().remoteAddress()
            }

            return real_ip
        }

        fun ProxyHttps(): Boolean {
            val use_ssl = Controller.request().getHeader("X-Use-SSL")

            if (StringUtil.isBlank(use_ssl)) {
                return Controller.request().secure()
            }

            if (use_ssl.equals("true", ignoreCase = true) || use_ssl.equals("yes", ignoreCase = true)) {
                return true
            } else {
                return false
            }
        }

        fun Protocol(): String {
            if (ProxyHttps()) {
                return "https"
            } else {
                return "http"
            }
        }

        @Throws(EncoderException::class)
        fun Download(fileName: String, content: ByteArray): Result {
            val response = Controller.response()
            response.setHeader("Content-Type", "application/octet-stream")
            response.setHeader("Content-Disposition", ContentDisposition(fileName))

            return Results.ok(content)
        }

        @Throws(EncoderException::class)
        private fun ContentDisposition(file_name: String): String {
            val urlCodec = URLCodec("UTF-8")
            val file_name_url_ecode = urlCodec.encode(file_name)
            val content_disposition: String
            if (Controller.request().getHeader(Controller.USER_AGENT).indexOf("IE") > 0) {
                content_disposition = String.format("attachment; filename=%s", file_name_url_ecode)
            } else {
                content_disposition = String.format("attachment; filename*=UTF-8''%s", file_name_url_ecode)
            }
            return content_disposition
        }
    }
}
