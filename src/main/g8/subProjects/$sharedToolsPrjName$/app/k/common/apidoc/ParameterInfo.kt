package k.common.apidoc

import k.aop.annotations.Comment
import k.common.Helper

/**
 * Created by kk on 16/9/18.
 */
data class ParameterInfo(@Comment("方法参数名称")
                         var name: String = "",

                         @Comment("参数描述")
                         var desc: String = "",

                         @Comment("参数的数据类型")
                         var type: String = "") {

    fun ToMarkdownStr(str: String): String {
        return Helper.EscapeMarkdown(str)
    }
}