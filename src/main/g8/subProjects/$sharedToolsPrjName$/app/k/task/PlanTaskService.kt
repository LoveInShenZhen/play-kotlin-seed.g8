package k.task

import jodd.datetime.JDateTime
import jodd.exception.ExceptionUtil
import k.common.AnsiColor
import k.common.Helper
import k.common.Hub
import k.ebean.DB
import models.task.PlanTask
import models.task.TaskStatus
import play.Logger
import java.util.concurrent.*

/**
 * Created by kk on 16/9/19.
 */
object PlanTaskService {

    private val parallelWorkerCount = 2 //Runtime.getRuntime().availableProcessors()

    private val seqPlanningWorker: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val parallelPlanningWorker: ScheduledExecutorService = Executors.newScheduledThreadPool(parallelWorkerCount)

    private var seqPlanningTaskLoader: Thread? = null
    private var parallerPlanningTaskLoader: Thread? = null

    private var seqTaskProducer: Thread? = null
    private val seqWorker: ExecutorService = Executors.newSingleThreadExecutor()
    private val seqTaskQueue: BlockingQueue<PlanTask> = LinkedBlockingQueue(2)

    private var parallerTaskProducer: Thread? = null
    private val parallerWorker: ExecutorService = Executors.newFixedThreadPool(parallelWorkerCount)
    private val parallerTaskQueue: BlockingQueue<PlanTask> = LinkedBlockingQueue(parallelWorkerCount + 1)


    private val taskNotifier = Object()
    private val taskLoaderWaitTime = 5
    private var stopNow: Boolean = true

    var isRunning: Boolean = false
        private set

    fun Start() {
        try {
            if (isRunning) return

            stopNow = false

            seqPlanningTaskLoader = BuildPlanningTaskLoader(true, seqPlanningWorker)
            parallerPlanningTaskLoader = BuildPlanningTaskLoader(false, parallelPlanningWorker)

            seqPlanningTaskLoader!!.start()
            parallerPlanningTaskLoader!!.start()

            seqTaskProducer = BuildSeqTaskLoader()
            seqTaskProducer!!.start()
            StartSeqWorker()

            parallerTaskProducer = BuildParallerTaskLoader()
            parallerTaskProducer!!.start()
            StartParallerWorker()

            isRunning = true

            PlanTask.ResetTaskStatus()
            Helper.DLog(AnsiColor.GREEN, "Plan Task Service Started......")

        } catch (ex: Exception) {
            stopNow = true
            isRunning = false

            Helper.DLog(AnsiColor.RED_B, "Start planTask service failed.\n${ExceptionUtil.exceptionChainToString(ex)}")
        }
    }

    fun Stop() {
        Helper.DLog("Try to stop paln task service ...")
        if (!isRunning) return
        stopNow = true
        try {
            Helper.DLog(AnsiColor.GREEN, "Try to stop plan task loader...")
            NotifyNewTask()
            seqPlanningTaskLoader!!.join(120000)
            parallerPlanningTaskLoader!!.join(120000)
            seqTaskProducer!!.join(120000)
            parallerTaskProducer!!.join(120000)

            Helper.DLog(AnsiColor.GREEN, "Try to stop plan task worker...")
            seqPlanningWorker.shutdown()
            parallelPlanningWorker.shutdown()
            seqWorker.shutdown()
            parallerWorker.shutdown()

            seqPlanningWorker.awaitTermination(120, TimeUnit.SECONDS)
            parallelPlanningWorker.awaitTermination(120, TimeUnit.SECONDS)
            seqWorker.awaitTermination(120, TimeUnit.SECONDS)
            parallerWorker.awaitTermination(120, TimeUnit.SECONDS)

            Helper.DLog(AnsiColor.GREEN, "Plan Task Service Stopped......")
        } catch (ex: Exception) {
            Logger.error(ExceptionUtil.exceptionChainToString(ex))
        } finally {
            isRunning = false
        }
    }

    private fun BuildSeqTaskLoader(): Thread {
        return Thread(Runnable {
            while (true) {
                if (stopNow) break

                val tasks = DB.RunInTransaction<List<PlanTask>> {
                    val taskList = PlanTask.where()
                            .eq("require_seq", true)
                            .eq("task_status", TaskStatus.WaitingInDB.code)
                            .isNull("plan_run_time")
                            .setMaxRows(100)
                            .findList()

                    taskList.forEach { it.task_status = TaskStatus.WaitingInQueue.code }

                    DB.Default().saveAll(taskList)

                    return@RunInTransaction taskList
                }

                if (tasks.size == 0) {
                    // 没有任务需要执行, 等候新任务通知
                    try {
                        synchronized(taskNotifier, {
                            taskNotifier.wait(taskLoaderWaitTime * 1000L)
                        })
                    } catch (ex: Exception) {
                        // do nothing
                    }
                } else {
                    var idx = 0
                    while (idx < tasks.size) {
                        if (stopNow) return@Runnable

                        if (this.seqTaskQueue.offer(tasks[idx], 1000, TimeUnit.MILLISECONDS)) {
                            idx++
                        }
                    }
                }
            }
            Helper.DLog("Stop SeqTaskLoader")
        })
    }

    private fun StartSeqWorker() {
        seqWorker.submit {
            while (true) {
                if (stopNow) break

                val task = this.seqTaskQueue.poll(1000, TimeUnit.MILLISECONDS)
                if (task != null) {
                    process_task(task)
                }
            }
            Helper.DLog("Stop Seq Worker")
        }
    }

    private fun BuildParallerTaskLoader(): Thread {
        return Thread(Runnable {
            while (true) {
                if (stopNow) break

                val tasks = DB.RunInTransaction<List<PlanTask>> {
                    val taskList = PlanTask.where()
                            .eq("require_seq", false)
                            .eq("task_status", TaskStatus.WaitingInDB.code)
                            .isNull("plan_run_time")
                            .setMaxRows(100)
                            .findList()

                    taskList.forEach { it.task_status = TaskStatus.WaitingInQueue.code }

                    DB.Default().saveAll(taskList)

                    return@RunInTransaction taskList
                }

                if (tasks.size == 0) {
                    // 没有任务需要执行, 等候新任务通知
                    try {
                        synchronized(taskNotifier, {
                            taskNotifier.wait(taskLoaderWaitTime * 1000L)
                        })
                    } catch (ex: Exception) {
                        // do nothing
                    }
                } else {
                    var idx = 0
                    while (idx < tasks.size) {
                        if (stopNow) return@Runnable

                        if (this.parallerTaskQueue.offer(tasks[idx], 1000, TimeUnit.MILLISECONDS)) {
                            idx++
                        }
                    }
                }
            }
            Helper.DLog("Stop Paraller TaskLoader")
        })
    }

    private fun StartParallerWorker() {
        for (i in 1..parallelWorkerCount) {
            parallerWorker.submit {
                while (true) {
                    if (stopNow) break

                    val task = this.parallerTaskQueue.poll(1000, TimeUnit.MILLISECONDS)
                    if (task != null) {
                        process_task(task)
                    }
                }
                Helper.DLog("Stop Paraller Worker: $i")
            }
        }
    }


    private fun BuildPlanningTaskLoader(requireSeq: Boolean, worker: ScheduledExecutorService): Thread {
        return Thread(Runnable {
            val loadedTasks = mutableListOf<PlanTask>()
            while (true) {
                if (stopNow) break

                try {
                    DB.RunInTransaction {
                        val endTime = JDateTime().addSecond(taskLoaderWaitTime + 1)
                        val tasks = PlanTask.where()
                                .eq("require_seq", requireSeq)
                                .eq("task_status", TaskStatus.WaitingInDB.code)
                                .le("plan_run_time", endTime)
                                .findList()

                        tasks.forEach {
                            it.task_status = TaskStatus.WaitingInQueue.code
                        }
                        DB.Default().saveAll(tasks)

                        loadedTasks.clear()
                        loadedTasks.addAll(tasks)
                    }   // 事务截至点

                    if (loadedTasks.size > 0) {
                        loadedTasks.forEach {
                            SchedulePlanTask(it, worker)
                        }
                    } else {
                        // 在 endTime 之前没有需要执行的 task, 尝试等待新任务, 释放 cpu
                        try {
                            synchronized(taskNotifier, {
                                taskNotifier.wait(taskLoaderWaitTime * 1000L)
                            })
                        } catch (ex: Exception) {
                            // do nothing
                        }
                    }

                } catch (ex: Exception) {
                    loadedTasks.clear()
                    Logger.error(ExceptionUtil.exceptionChainToString(ex))
                }
            }
            Helper.DLog("Stop PlanningTaskLoader for requireSeq: $requireSeq")
        })
    }

    private fun SchedulePlanTask(task: PlanTask, worker: ScheduledExecutorService) {
        val now = JDateTime()
        if (task.plan_run_time == null) task.plan_run_time = now
        val interval = task.plan_run_time!!.timeInMillis - now.timeInMillis
        val delay = if (interval > 0) interval else 0

        worker.schedule({
            try {
                process_task(task)
            } catch (ex: Exception) {
                Logger.error(ExceptionUtil.exceptionChainToString(ex))
            }
        },
                delay,
                TimeUnit.MILLISECONDS)
    }

    private fun DeserializeJsonData(task: PlanTask): Runnable? {
        try {
            return Helper.FromJsonString(task.json_data!!, Class.forName(task.class_name)) as Runnable
        } catch (ex: Exception) {
            return null
        }
    }

    private fun process_task(task: PlanTask) {
        try {
            val runObj = DeserializeJsonData(task)
            if (runObj != null) {
                try {
                    DB.RunInTransaction({
                        runObj.run()    // 执行任务
                        task.refresh()
                        task.delete()   // 任务执行成功后, 从数据库里删除记录
                    })
                } catch (ex: Exception) {
                    // 任务执行发生错误, 标记任务状态, 记录
                    DB.RunInTransaction {
                        task.refresh()
                        task.task_status = TaskStatus.Error.code
                        task.remarks = ExceptionUtil.exceptionChainToString(ex)
                        task.save()
                    }
                }
            } else {
                DB.RunInTransaction {
                    task.refresh()
                    task.task_status = TaskStatus.Error.code
                    task.remarks = "反序列化任务失败"
                    task.save()
                }
            }
        } catch (ex: Exception) {
            Logger.error(ExceptionUtil.exceptionChainToString(ex))
        }
    }

    fun NotifyNewTask() {
        synchronized(taskNotifier, {
            taskNotifier.notifyAll()
        })
    }

    fun Enabled(): Boolean {
        val enable = Hub.configuration().getBoolean("k.planTaskService", false)
        if (enable) {
            try {
                if (DB.TableExists("plan_task")) {
                    return true
                } else {
                    Helper.DLog(AnsiColor.RED_B, "Database table plan_task does not exists. PlanTask service can not started.")
                    return false
                }
            } catch (ex: Exception) {
                Helper.DLog(AnsiColor.RED_B, "Start plan task service failed.\n${ExceptionUtil.exceptionChainToString(ex)}")
                return false
            }
        } else {
            return false
        }
    }
}