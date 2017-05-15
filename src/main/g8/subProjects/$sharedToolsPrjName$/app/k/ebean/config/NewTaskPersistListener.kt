package models


import com.avaje.ebean.event.BeanPersistListener
import k.common.AnsiColor
import k.common.Helper
import k.task.PlanTaskService
import models.task.PlanTask

/**
 * Created by kk on 15/1/20.
 */
class NewTaskPersistListener : BeanPersistListener {

    init {
        Helper.DLog(AnsiColor.GREEN, "Ebean server load NewTaskBeanPersistListener")
    }

    override fun isRegisterFor(cls: Class<*>): Boolean {
        val beanClsName = cls.name
        if (beanClsName == PlanTask::class.java.name) {
            return true
        }
        return false
    }

    override fun inserted(bean: Any?) {
        if (bean is PlanTask) {
            PlanTaskService.NotifyNewTask()
            Helper.DLog("new task added...")
        }
    }

    override fun deleted(bean: Any?) {

    }

    override fun updated(bean: Any?, updatedProperties: MutableSet<String>?) {

    }
}
