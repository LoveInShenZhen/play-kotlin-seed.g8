package k.reply.sample

import jodd.datetime.JDateTime
import k.aop.annotations.Comment
import k.aop.annotations.ElementType
import k.reply.ReplyBase
import k.reply.StringReply
import java.util.*

/**
 * Created by kk on 16/9/5.
 */
class SampleReply : ReplyBase() {

    @Comment("用户名称")
    var userName: String? = null

    @Comment("手机号码")
    var mobile: String? = null

    @Comment("可用余额")
    var moneyAmount: Double? = null

    @Comment("持有股票列表")
    var stocks: MutableMap<String, Int> = mutableMapOf()

    @Comment("朋友列表")
    @ElementType(String::class)
    var friends: MutableList<StringReply> = mutableListOf()

    @Comment("标签列表")
    @ElementType(String::class)
    var tags: MutableSet<String> = mutableSetOf()

    @Comment("注册时间")
    var regTime : JDateTime? = null

    @Comment("书籍名称列表")
    var books : Array<StringReply> = arrayOf()

    @Comment("汽车名称列表")
    var cars : ArrayList<String> = ArrayList()
}