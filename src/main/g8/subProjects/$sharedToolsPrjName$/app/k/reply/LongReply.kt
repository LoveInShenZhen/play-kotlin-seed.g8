package k.reply

import k.aop.annotations.Comment

/**
 * Created by frank.zhang on 2017/2/14.
 */
class LongReply : ReplyBase() {

    @Comment("Long ID")
    var result: Long? = null

    override fun SampleData() {
        super.SampleData()
        this.result = 0L
    }
}