package k.common

import jodd.io.FastByteArrayOutputStream
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteWatchdog
import org.apache.commons.exec.PumpStreamHandler
import java.io.File

/**
 * Created by kk on 16/10/25.
 */

data class ScriptResult(var ret: Int = 0, var msg: String = "")

class RunCmd(val cmdLine: String, timeOutMs: Long = 60000) {
    private val cmd: CommandLine
    private val executor: DefaultExecutor
    private var exitValue = Int.MIN_VALUE
    private val outAndErr: FastByteArrayOutputStream

    init {
        cmd = CommandLine.parse(cmdLine)

        executor = DefaultExecutor()

        executor.watchdog = ExecuteWatchdog(timeOutMs)

        outAndErr = FastByteArrayOutputStream()
        executor.streamHandler = PumpStreamHandler(outAndErr)

        executor.setExitValue(0)

    }

    fun setWorkingDirectory(dir: File): RunCmd {
        executor.workingDirectory = dir
        return this
    }

    fun AddArgument(argument: String): RunCmd {
        cmd.addArgument(argument)
        return this
    }

    fun Run(): RunCmd {
        Helper.DLog("RunCmd: ${this.CommandLineString}")
        try {
            exitValue = executor.execute(cmd)
        } catch (ex: Exception) {
            this.exitValue = -999
            Helper.DLog(ex.message!!)
        }

        return this
    }

    val ExitCode: Int
        get() = this.exitValue

    val Output: String
        get() = outAndErr.toString("UTF-8")

    val Result: ScriptResult
        get() {
            return ScriptResult(ExitCode, Output)
        }

    val CommandLineString: String
        get() {
            return cmd.executable + " " + cmd.arguments.joinToString(" ")
//            return cmd.toString()
        }
}