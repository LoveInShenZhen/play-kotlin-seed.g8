package k.reply


import com.fasterxml.jackson.databind.JsonNode
import k.aop.annotations.Comment
import k.common.Helper
import play.libs.Json

/**
 * Created with IntelliJ IDEA.
 * User: kk
 * Date: 13-11-15
 * Time: 下午5:03
 * To change this template use File | Settings | File Templates.
 */

open class ReplyBase {

    @Comment("返回的错误码, 0: 成功, 非0: 错误")
    var ret: Int

    @Comment("ret=0时, 返回OK, 非0时, 返回错误描述信息")
    var errmsg: String?

    @Comment("ret 非0时, 附加的错误信息, Json 格式")
    var errors: JsonNode?

    init {
        ret = 0
        errmsg = "OK"
        errors = null
    }

    fun toJsonStr(): String {
        return Helper.ToJsonStringPretty(this)
    }

    override fun toString(): String {
        return toJsonStr()
    }

    fun toJsonNode(): JsonNode {
        return Json.toJson(this)
    }

    open fun SampleData() {
        ret = 0
        errmsg = "OK"
        errors = null
    }
}
