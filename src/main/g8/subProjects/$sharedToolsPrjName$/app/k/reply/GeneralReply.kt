package k.reply

import com.fasterxml.jackson.databind.JsonNode
import k.aop.annotations.Comment

/**
 * Created by kk on 17/3/20.
 */
class GeneralReply : ReplyBase() {

    @Comment("返回的结果(json object)")
    var result: JsonNode? = null
}