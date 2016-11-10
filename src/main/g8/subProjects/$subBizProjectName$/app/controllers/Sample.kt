package controllers


import k.aop.annotations.Comment
import k.aop.annotations.JsonApi
import k.common.Helper
import k.common.StringChecker
import k.common.apidoc.ApiInfo
import k.common.apidoc.DefinedApis
import k.controllers.JsonpController
import k.reply.ReplyBase
import k.reply.StringReply
import k.reply.sample.SampleReply
import k.task.SampleTask
import play.mvc.Result
import javax.inject.Inject

/**
 * Created by kk on 16/8/23.
 */

@Comment("测试组方法")
class Sample
@Inject
constructor(var definedApis: DefinedApis) : JsonpController() {


    @Comment("测试方法1")
    @JsonApi(ReplyClass = SampleReply::class)
    fun kktest() : Result {
        val reply = SampleReply()
        Helper.DLog("\n" + ApiInfo.SampleJsonData(StringReply::class))
        return ok(reply)
    }

    @Comment("验证身份证号码")
    @JsonApi(ReplyClass = StringReply::class)
    fun CheckIdCard(idcard: String) : Result {
        val reply = StringReply()
        val msg = StringChecker.IDCardValidate(idcard)
        reply.result = if (msg.isBlank()) "有效的身份证号码" else msg
        return ok(reply)
    }

    @Comment("向 PlanTask 里增加一个测试 sample 任务")
    @JsonApi
    fun AddSampleTask():Result {
        SampleTask.addTask()
        return ok(ReplyBase())
    }

}