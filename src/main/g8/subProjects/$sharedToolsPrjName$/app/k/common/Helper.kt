package k.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.common.hash.Hashing
import org.apache.commons.lang3.StringUtils
import play.Logger
import play.libs.Json
import java.io.File
import java.math.RoundingMode
import java.nio.charset.Charset
import java.text.DecimalFormat

/**
 * Created by kk on 16/7/2.
 */
object Helper {

    fun LoadClass(class_name: String): Class<*>? {
        try {
            return Hub.application().classloader().loadClass(class_name)
        } catch (ex: Exception) {
            throw BizLogicException("Can not load class: %s", class_name)
        }

    }

    fun getFile(path: String): File {
        return Hub.application().getFile(path)
    }

    //<editor-fold desc="Log Helper Functions">
    fun DLog(message: String) {
//        val msg = "${AnsiColor.BLUE.code}$message${AnsiColor.RESET.code}"
//        Logger.debug(msg)
        DLog(AnsiColor.BLUE, message)
    }

    fun DLog(ansiColor: AnsiColor, message: String) {
        val msg = "${ansiColor.code}$message${AnsiColor.RESET.code}"
        Logger.debug(msg)
    }

    fun DLog(ansiColor: AnsiColor, bgColor: AnsiColor,  message: String) {
        val msg = "${ansiColor.code}${bgColor.code}$message${AnsiColor.RESET.code}"
        Logger.debug(msg)
    }
    //</editor-fold>

    //<editor-fold desc="Json Helper Functions">
    fun ToJsonString(obj: Any): String {
        return Json.toJson(obj).toString()
    }

    fun ToJsonStringPretty(obj: Any): String {
        val jsonNode = Json.toJson(obj)
        val mapper = ObjectMapper().registerKotlinModule()

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode)
    }

    fun FormatJson(jsonStr: String): String {
        val jsonNode = Json.parse(jsonStr)
        val mapper = ObjectMapper().registerKotlinModule()
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode)
    }

    fun <A> FromJsonString(jsonStr: String, clazz: Class<A>): A {
        val node = Json.parse(jsonStr)
        return Json.fromJson(node, clazz)
    }

    //</editor-fold>

    //<editor-fold desc="String utils methods">
    fun Md5OfString(s: String): String {
        return Hashing.md5().hashString(s, Charset.forName("UTF-8")).toString()
    }

    fun SHA1OfString(s: String): String {
        return Hashing.sha1().hashString(s, Charset.forName("UTF-8")).toString()
    }


    // 将年利率格式化为百分号的形式, 保留1位小数
    fun IRRFormat(irr: Double): String {
        val fmt = DecimalFormat("0.0%")
        fmt.roundingMode = RoundingMode.HALF_UP
        return fmt.format(irr)
    }

    fun BorrowerRate(amount: Double?): String {
        if (amount == null) return "0.00"
        val fmt = DecimalFormat("#0.00")
        fmt.roundingMode = RoundingMode.DOWN
        return fmt.format(amount)
    }

    // 格式化成百分比的形式, 保留2位小数, 100% 除外
    fun PercentageFormat(amount: Double): String {
        if (amount == 0.0) {
            return "0%"
        } else if (amount >= 1.0) {
            return "100%"
        }
        val style = "0.00%"
        val fmt = DecimalFormat(style)
        fmt.roundingMode = RoundingMode.DOWN
        return fmt.format(amount)
    }


    // 对 amount 四舍五入, 保留 count 位小数
    fun Round(amount: Double, count: Int): Double {
        if (count == 0) {
            return Math.round(amount).toDouble()
        }
        return Math.round(amount * Math.pow(10.0, count.toDouble())) / Math.pow(10.0, count.toDouble())
    }

    //  向正无穷方向舍入, 保留 count 位小数
    fun RoundCeiling(amount: Double, count: Int): Double {
        if (count == 0) {
            return Math.ceil(amount)
        }
        return Math.ceil(amount * Math.pow(10.0, count.toDouble())) / Math.pow(10.0, count.toDouble())
    }

    // 转换金额到万元为单位, 保留1位小数, 带单位: 万
    fun Div10Thousands(amount: Double?): String {
        if (amount == null) return ""
        /*double tmp = amount / 10000.0;
        DecimalFormat fmt = new DecimalFormat("#.00 万");
        fmt.setRoundingMode(RoundingMode.HALF_UP);
        return fmt.format(tmp);*/
        val temp = amount / 10000.00
        if (temp < 10) {
            val fmt = DecimalFormat("#.00")
            fmt.roundingMode = RoundingMode.HALF_UP
            return fmt.format(amount)
        } else {
            val fmt = DecimalFormat("#.00万")
            fmt.roundingMode = RoundingMode.HALF_UP
            return fmt.format(temp)
        }

    }

    // 格式化成逗号分隔的形式, 保留2位小数
    fun CommaSeparated(amount: Double?): String {
        if (amount == null) return ""
        val fmt = DecimalFormat("#,##0.00")
        fmt.roundingMode = RoundingMode.HALF_UP
        return fmt.format(amount)
    }

    fun EscapeMarkdown(str: String): String {
        if (StringUtils.isBlank(str)) {
            return ""
        }
        var md = StringUtils.replace(str, "_", "\\_")
        md = StringUtils.replace(md, "*", "\\*")
        return md
    }

    //</editor-fold>

}
