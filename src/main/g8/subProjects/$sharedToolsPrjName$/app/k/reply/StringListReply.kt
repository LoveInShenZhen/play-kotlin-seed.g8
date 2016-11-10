package k.reply

import k.aop.annotations.Comment

/**
 * Created by kk on 16/10/11.
 */
class StringListReply : ReplyBase() {

    @Comment("字符串列表")
    var lines: List<String> = emptyList()
}