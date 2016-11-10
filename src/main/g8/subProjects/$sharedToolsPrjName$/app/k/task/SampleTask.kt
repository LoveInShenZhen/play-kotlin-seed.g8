package k.task

import jodd.datetime.JDateTime
import k.common.AnsiColor
import k.common.Helper
import models.task.PlanTask

/**
 * Created by kk on 16/9/20.
 */

class SampleTask : Runnable {
    override fun run() {
        val now = JDateTime()
        Helper.DLog(AnsiColor.CYAN_B, now.toString())
    }

    companion object {
        fun addTask() {
            val task = SampleTask()
            PlanTask.addTask(task)
        }
    }
}