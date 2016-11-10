package models.task

import com.avaje.ebean.Model
import jodd.datetime.JDateTime
import k.common.Helper
import k.ebean.DB
import models.BaseModel
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * Created by kk on 16/9/19.
 */

@Entity
@Table(name = "plan_task")
class PlanTask : BaseModel() {

    @Column(columnDefinition = "TINYINT(1) COMMENT '是否要求顺序执行'")
    var require_seq: Boolean = false

    @Column(columnDefinition = "VARCHAR(64) COMMENT '顺序执行的类别'", nullable = false)
    var seq_type: String? = null

    @Column(columnDefinition = "DATETIME COMMENT '任务计划执行时间'")
    var plan_run_time: JDateTime? = null

    @Column(columnDefinition = "INTEGER DEFAULT 0 COMMENT '任务状态: 0:WaitingInDB, 7:WaitingInQueue, 8:Error'", nullable = false)
    var task_status: Int = 0

    @Column(columnDefinition = "VARCHAR(1024) COMMENT 'Runnable task class name'", nullable = false)
    var class_name: String? = null

    @Column(columnDefinition = "TEXT COMMENT 'Runnable task class json data'", nullable = false)
    var json_data: String? = null

    @Column(columnDefinition = "TEXT COMMENT '标签,用于保存任务相关的额外数据'")
    var tag: String? = null

    @Column(columnDefinition = "TEXT COMMENT '发生异常情况的时候, 用于记录额外信息'")
    var remarks: String? = null

    companion object : Model.Find<Long, PlanTask>() {

        fun addTask(task: Runnable, requireSeq: Boolean = false, seqType: String = "", planRunTime: JDateTime? = null, tag: String = "") {
            val planTask = PlanTask()
            planTask.require_seq = requireSeq
            planTask.seq_type = seqType
            planTask.plan_run_time = planRunTime
            planTask.task_status = TaskStatus.WaitingInDB.code
            planTask.class_name = task.javaClass.name
            planTask.json_data = Helper.ToJsonStringPretty(task)
            planTask.tag = tag

            planTask.save()
        }

        fun addSingletonTask(task: Runnable, requireSeq: Boolean = false, seqType: String = "", planRunTime: JDateTime? = null, tag: String = "") {
            val className = task.javaClass.name
            val oldTask = PlanTask.where()
                    .eq("class_name", className)
                    .eq("task_status", TaskStatus.WaitingInDB.code)
                    .findUnique()
            if (oldTask == null) {
                addTask(task, requireSeq, seqType, planRunTime, tag)
            } else {
                oldTask.plan_run_time = planRunTime
                oldTask.save()
            }
        }

        fun ResetTaskStatus() {
            DB.RunInTransaction {
                val sql = "update `plan_task` set `task_status`=:init_status where `task_status`=:old_status"
                DB.Default().createSqlUpdate(sql)
                        .setParameter("init_status", TaskStatus.WaitingInDB.code)
                        .setParameter("old_status", TaskStatus.WaitingInQueue.code)
                        .execute()
            }
        }

    }
}

enum class TaskStatus(val desc: String, val code: Int) {
    WaitingInDB("WaitingInDB", 0),
    WaitingInQueue("WaitingInQueue", 7),
    Error("Error", 8)
}