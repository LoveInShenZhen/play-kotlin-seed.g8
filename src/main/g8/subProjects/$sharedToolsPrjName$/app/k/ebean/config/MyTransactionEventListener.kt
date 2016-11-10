package models


import com.avaje.ebean.Transaction
import com.avaje.ebean.event.TransactionEventListener
import k.common.Helper
import k.task.PlanTaskService
import models.task.PlanTask

/**
 * Created by kk on 15/2/2.
 */
class MyTransactionEventListener : TransactionEventListener {

    init {
        Helper.DLog("Ebean server load MyTransactionEventListener")
    }

    override fun postTransactionCommit(tx: Transaction) {
        if (tx.getUserObject(PlanTask::class.java.name) != null) {
//            val notifyService = NotifyService.getService()
//            notifyService?.NotifyNewPlanTask()

            PlanTaskService.NotifyNewTask()
        }
//        val obj = tx.getUserObject(MyBeanPersistController::class.java.name)
//        if (obj != null) {
//            val sb = obj as StringBuilder
//            Logger.of("PersistLog").info(sb.toString())
//        }
    }

    override fun postTransactionRollback(tx: Transaction, cause: Throwable) {

    }
}
