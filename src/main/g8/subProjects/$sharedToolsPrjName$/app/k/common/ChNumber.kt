package k.common

import java.math.BigDecimal

/**
 * Created by kk on 17/2/24.
 */

// 中文数字 -> 阿拉伯数字

object ChNumberToArabic {

    // 基础数字
    val nums: Map<String, String> = mapOf("零" to "0", "一" to "1", "二" to "2", "两" to "2",  "三" to "3", "四" to "4", "五" to "5", "六" to "6", "七" to "7", "八" to "8", "九" to "9", "点" to ".")

    // 权
    val powers = mapOf("十" to 10, "百" to 100, "千" to 1000)

    // 节
    val sections = mapOf("万" to 10000L, "亿" to 100000000L, "万亿" to 1000000000000L)

    val words: Set<String>

    init {
        val tmpwords = mutableSetOf<String>()
        tmpwords.addAll(nums.keys)
        tmpwords.addAll(powers.keys)
        tmpwords.addAll(sections.keys)

        words = tmpwords
    }

    fun isChNumbers(str: String): Boolean {
        // 暂时只做简答的判断
        val invalidCount = str.filter { words.notContains(it.toString()) }.count()
        return invalidCount == 0
    }

    fun ParseSections(numStr: String): List<NumSection> {
        val sections = mutableListOf<NumSection>()

        val idx_3 = numStr.indexOf("万亿")

        val idx_2 = if (idx_3 > 0) numStr.indexOf("亿", idx_3 + 2) else numStr.indexOf("亿")

        val idx_1 = if (idx_3 > 0) numStr.indexOf("万", idx_3 + 2) else numStr.indexOf("万")

        // 万亿
        var startIndex = 0
        if (idx_3 > 0) {
            val s = numStr.substring(startIndex, idx_3)

            if (s.notNullOrBlank()) {
                val sec = NumSection()
                sec.section = "万亿"
                sec.Parse(s)
                sections.add(sec)
            }
            startIndex = idx_3 + 2
        }

        // 亿
        if (idx_2 > 0) {
            val s = numStr.substring(startIndex, idx_2)

            if (s.notNullOrBlank()) {
                val sec = NumSection()
                sec.section = "亿"
                sec.Parse(s)
                sections.add(sec)
            }
            startIndex = idx_2 + 1
        }

        // 万
        if (idx_1 > 0) {
            val s = numStr.substring(startIndex, idx_1)

            if (s.notNullOrBlank()) {
                val sec = NumSection()
                sec.section = "万"
                sec.Parse(s)
                sections.add(sec)
            }
            startIndex = idx_1 + 1
        }

        if (idx_1 < numStr.length) {
            val s = numStr.substring(startIndex)
            if (s.notNullOrBlank()) {
                val sec = NumSection()
                sec.section = ""
                sec.Parse(s)
                sections.add(sec)
            }
        }

        return sections
    }
}

class NumSection {
    var s_1 = ""             // 个位数
    var s_10 = ""            // 十位数
    var s_100 = ""           // 百位数
    var s_1000 = ""          // 千位数

    var section = ""         // 节

    private var numstr = ""
    var orignNumStr = ""

    fun Parse(str: String) {
        this.orignNumStr = str
        this.numstr = str.replace("零", "")
        val idx_1000 = this.numstr.indexOf("千")
        val idx_100 = this.numstr.indexOf("百")
        val idx_10 = this.numstr.indexOf("十")

        var startIndex = 0
        if (idx_1000 > 0) {
            val s = this.numstr.substring(startIndex, idx_1000)
            if (s.notNullOrBlank()) {
                this.s_1000 = s
            }
            startIndex = idx_1000 + 1
        }

        if (idx_100 > 0) {
            val s = this.numstr.substring(startIndex, idx_100)
            if (s.notNullOrBlank()) {
                this.s_100 = s
            }
            startIndex = idx_100 + 1
        }

        if (idx_10 > 0) {
            val s = this.numstr.substring(startIndex, idx_10)
            if (s.notNullOrBlank()) {
                this.s_10 = s
            }
            startIndex = idx_10 + 1
        }
        if (startIndex < this.numstr.length) {
            this.s_1 = this.numstr.substring(startIndex)
        }
    }

    private fun numValue(nums: String): BigDecimal {
        if (nums.isNullOrBlank()) {
            return BigDecimal.valueOf(0)
        }
        val arabicStr = nums.map { ChNumberToArabic.nums.getOrElse(it.toString()) { "" } }.joinToString("")
        return BigDecimal(arabicStr)
    }

    fun DecimailValue(): BigDecimal {
        val baseNum = ChNumberToArabic.sections.getOrElse(this.section) { 1 }
        return BigDecimal.valueOf(baseNum) * (numValue(this.s_1)
                + numValue(this.s_10) * BigDecimal.valueOf(10)
                + numValue(this.s_100) * BigDecimal.valueOf(100)
                + numValue(this.s_1000) * BigDecimal.valueOf(1000))
    }

    override fun toString(): String {
        return Helper.ToJsonStringPretty(this)
    }
}

object ChPercent {

    fun isChPercent(str: String): Boolean {
        // 必须是"百分之"开头
        if (!str.startsWith("百分之")) {
            return false
        }
        // 剩余部分, 必须是 ChNumberToArabic 中文数字
        val leftStr = str.substring(3)
        if (!ChNumberToArabic.isChNumbers(leftStr)) {
            return false
        }

        return true
    }

    fun Parse(str: String): BigDecimal {
        val x = NumSection()
        x.Parse(str.replace("百分之", ""))

        return x.DecimailValue() * BigDecimal.valueOf(0.01)
    }
}

class ArabicWithUnit {
    var num = ""
    var unit = ""
}

