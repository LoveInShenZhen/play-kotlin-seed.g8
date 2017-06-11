package k.aop.actions


import com.avaje.ebean.Ebean
import com.avaje.ebean.TxIsolation
import com.avaje.ebean.TxScope
import jodd.datetime.JDateTime
import k.aop.annotations.JsonApi
import k.common.Hub
import k.controllers.JsonpController
import k.reply.ReplyBase
import play.mvc.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

/**
 * Created by kk on 14-6-10.
 */
class JsonApiAction : Action<JsonApi>() {

    private var before_call: JDateTime? = null

    override fun call(ctx: Http.Context): CompletionStage<Result> {
        before_call = JDateTime()

        if (this.configuration.UseEtag && this.configuration.Transactional) {
            // Etag 只能用于查询, 不能用于交易类(只对数据库有更新操作)的 api 上
            // 所以, UseEtag 为 True 的时候, Transactional 必须是设置成 False, 否则抛出异常
            val reply = ReplyBase()
            reply.ret = -1
            reply.errmsg = "UseEtag 为 True 的时候, Transactional 必须是设置成 False"
            val result = JsonpController.ok(reply)
            return CompletableFuture.completedFuture(result)
        }

        if (this.configuration.Transactional && this.dbConfiged) {
            return call_api_with_tran(ctx)
        } else {
            return call_api(ctx)
        }
    }

    private val dbConfiged: Boolean
        get() {
            return Hub.configuration().getString("db.default.url", "").isNotBlank()
        }

    private fun call_api(ctx: Http.Context): CompletionStage<Result> {
        if (this.configuration.UseEtag) {
            ctx.args.put("USE_ETAG", true)
        }
        return delegate.call(ctx).thenApply {
            if (this.configuration.UseEtag && ctx.args.containsKey("api_reply_etag")) {
                // Api 使用 Etag 方式, 来标记Api的查询结果是否和上次一致(木有发生变化)
                val apiReplyEtag = ctx.args["api_reply_etag"] as String

                // 保存本次查询结果的 Etag
                ctx.response().setHeader(Controller.ETAG, apiReplyEtag)

                if (ctx.request().hasHeader(Controller.IF_NONE_MATCH)) {
                    // 需要检查, 本次查询结果和上一次的是否一致, 如果一致, 返回 304
                    val lastEtag = ctx.request().getHeader(Controller.IF_NONE_MATCH)
                    if (apiReplyEtag == lastEtag) {
                        // 和上次的查询结果一致
                        return@thenApply Results.status(Controller.NOT_MODIFIED)
                    } else {
                        return@thenApply it
                    }
                } else {
                    // 没有 If-None-Match header
                    return@thenApply it
                }
            } else {
                // 不使用 Etag
                return@thenApply it
            }
        }

    }

    private fun call_api_with_tran(ctx: Http.Context): CompletionStage<Result> {
        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED)
        return Ebean.execute<CompletionStage<Result>>(txScope) {
            delegate.call(ctx)
        }
    }
}
