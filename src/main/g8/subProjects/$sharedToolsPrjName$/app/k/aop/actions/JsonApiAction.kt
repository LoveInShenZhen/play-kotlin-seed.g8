package k.aop.actions


import com.avaje.ebean.Ebean
import com.avaje.ebean.TxIsolation
import com.avaje.ebean.TxScope
import com.fasterxml.jackson.databind.JsonNode
import jodd.datetime.JDateTime
import jodd.exception.ExceptionUtil
import k.aop.annotations.JsonApi
import k.common.BizLogicException
import k.common.Helper
import k.common.Hub
import k.controllers.JsonpController
import k.reply.ReplyBase
import play.Logger
import play.mvc.*
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*
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

    private val hostName: String
        get() {
            try {
                val IP = InetAddress.getLocalHost()
                return IP.hostName
            } catch (e: UnknownHostException) {
                e.printStackTrace()
                return "Unknown Host Name"
            }

        }

    private fun log(ex_msg: String) {
        val record_api = Hub.configuration().getBoolean("jsonapi.log", false)!!
        if (record_api) {
            val logger = Logger.of("jsonapi")
            val log_guid = UUID.randomUUID()
            val request = Controller.request()
            val now = JDateTime()

            val del_sql = String.format("delete from api_log where guid='%s';", log_guid.toString())
            val ins_sql = String.format("insert api_log (`guid`, `log_time`, `api_method`, `api_path`, `api_url`, "
                    + "`form_data`, `spend_time`, `host_name`, `client_ip`, `exceptions`) "
                    + "values ('%s', '%s', '%s', '%s', '%s', '%s', %s, '%s', '%s', '%s');",
                    log_guid.toString(),
                    now.toString("YYYY-MM-DD hh:mm:ss:mss"),
                    request.method(),
                    request.path(),
                    request.uri(),
                    Helper.ToJsonStringPretty(request.body().asJson()),
                    (now.timeInMillis - this.before_call!!.timeInMillis).toString(),
                    this.hostName,
                    JsonpController.ClientIp(),
                    ex_msg)
            logger.info(del_sql)
            logger.info(ins_sql)
        }
    }

    private fun call_api(ctx: Http.Context): CompletionStage<Result> {
        try {
            try {
                val api_result = delegate.call(ctx)

                if (this.configuration.UseEtag && ctx.args.containsKey("api_reply")) {
                    // Api 使用 Etag 方式, 来标记Api的查询结果是否和上次一致(木有发生变化)
                    val api_reply = ctx.args["api_reply"] as JsonNode
                    val the_etag = Helper.SHA1OfString(api_reply.toString())

                    // 保存本次查询结果的 Etag
                    ctx.response().setHeader(Controller.ETAG, the_etag)

                    if (ctx.request().hasHeader(Controller.IF_NONE_MATCH)) {
                        // 需要检查, 本次查询结果和上一次的是否一致, 如果一致, 返回 304
                        val last_etag = ctx.request().getHeader(Controller.IF_NONE_MATCH)
                        if (the_etag == last_etag) {
                            // 和上次的查询结果一致
                            return CompletableFuture.completedFuture<Result>(Results.status(Controller.NOT_MODIFIED))
                        } else {
                            return api_result
                        }
                    } else {
                        // 没有 If-None-Match header
                        return api_result
                    }
                } else {
                    // 不使用 Etag
                    return api_result
                }

            } catch (e: RuntimeException) {
                throw e
            } catch (t: Throwable) {
                throw RuntimeException(t)
            }

        } catch (ex: BizLogicException) {
            val reply = ReplyBase()
            reply.ret = ex.ErrCode
            reply.errmsg = ex.message
            val result = JsonpController.ok(reply)
            handleApiException(ctx, ex)
            log(ex.message!!)
            return CompletableFuture.completedFuture(result)

        } catch (ex: Exception) {
            val reply = ReplyBase()
            reply.ret = -1
            reply.errmsg = ExceptionUtil.exceptionChainToString(ex)
            val result = JsonpController.ok(reply)
            handleApiException(ctx, ex)
            log(ex.message!!)
            return CompletableFuture.completedFuture(result)
        }

    }

    private fun call_api_with_tran(ctx: Http.Context): CompletionStage<Result> {
        try {
            val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED)
            val txCallable: () -> CompletionStage<Result> = {
                delegate.call(ctx)
            }

            return Ebean.execute<CompletionStage<Result>>(txScope, txCallable)

        } catch (ex: BizLogicException) {
            val reply = ReplyBase()
            reply.ret = ex.ErrCode
            reply.errmsg = ex.message
            val result = JsonpController.ok(reply)
            handleApiException(ctx, ex)
            log(ex.message!!)
            return CompletableFuture.completedFuture(result)

        } catch (ex: Exception) {
            val reply = ReplyBase()
            reply.ret = -1
            reply.errmsg = ExceptionUtil.exceptionChainToString(ex)
            Logger.warn(ExceptionUtil.exceptionChainToString(ex))
            val result = JsonpController.ok(reply)
            handleApiException(ctx, ex)
            log(ex.message!!)
            return CompletableFuture.completedFuture(result)
        }

    }

    private fun handleApiException(ctx: Http.Context, ex: Exception) {
        // todo send email when has json exceptions
        Logger.error("api url: ${ctx.toString()}\n${ExceptionUtil.exceptionChainToString(ex)}")
    }

}
