package models


import com.avaje.ebean.event.BeanPersistController
import com.avaje.ebean.event.BeanPersistRequest
import k.common.AnsiColor
import k.common.Helper
import models.task.PlanTask

/**
 * Created by kk on 15/1/20.
 */
class NewTaskPersistController : BeanPersistController {

    init {
        Helper.DLog(AnsiColor.GREEN, "Ebean server load NewTaskPersistController")
    }

    override fun getExecutionOrder(): Int {
        return 11
    }

    override fun isRegisterFor(cls: Class<*>): Boolean {
        val beanClsName = cls.name
        if (beanClsName == PlanTask::class.java.name) {
            return true
        }
        return false
    }

    override fun preInsert(request: BeanPersistRequest<*>): Boolean {
        return true
    }

    override fun preUpdate(request: BeanPersistRequest<*>): Boolean {
        return true
    }

    override fun preDelete(request: BeanPersistRequest<*>): Boolean {
        return true
    }

    override fun postInsert(request: BeanPersistRequest<*>) {
        val beanClsName = request.bean.javaClass.name
        if (beanClsName == PlanTask::class.java.name) {
            request.transaction.putUserObject(PlanTask::class.java.name, "")
        }
    }

    override fun postUpdate(request: BeanPersistRequest<*>) {
        // do nothing
    }

    override fun postDelete(request: BeanPersistRequest<*>) {
        // do nothing
    }

}
