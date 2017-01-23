package k.task

import k.common.Helper
import k.common.Hub
import k.common.OnKBaseStartStop
import scala.concurrent.ExecutionContext
import scala.concurrent.`ExecutionContext$`
import scala.concurrent.duration.Duration
import java.util.concurrent.Executors

/**
 * Created by kk on 17/1/23.
 */
object AsyncTask {

    private val exec = Executors.newWorkStealingPool()

    init {
        val kb = Hub.application().injector().instanceOf(OnKBaseStartStop::class.java)
        kb.RegStopTask(Runnable {
            if (exec != null) {
                Helper.DLog("Stop AsyncTask worker executor ...")
                exec.shutdown()
            }
        })
    }

    private fun execContext(): ExecutionContext {
        return `ExecutionContext$`.`MODULE$`.fromExecutor(exec)
    }

    fun submit(task: Runnable) {
        var actorSystem = Hub.ActorSystem()
        var exec = execContext()

        actorSystem.scheduler()
                .scheduleOnce(Duration.Zero(),
                        task,
                        exec)
    }

    fun submit(task: () -> Unit) {
        var actorSystem = Hub.ActorSystem()
        var exec = execContext()

        actorSystem.scheduler()
                .scheduleOnce(Duration.Zero(),
                        task,
                        exec)
    }
}