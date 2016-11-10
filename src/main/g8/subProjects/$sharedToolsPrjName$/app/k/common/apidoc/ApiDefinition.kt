package k.common.apidoc


import k.aop.annotations.Comment
import k.common.Helper
import k.reply.ReplyBase

/**
 * Created by kk on 15/1/12.
 */
class ApiDefinition : ReplyBase() {

    @Comment("api 分组列表")
    var groups: MutableList<ApiGroup>

    init {
        groups = mutableListOf<ApiGroup>()

    }

    fun GetApiGroupByName(groupName: String): ApiGroup {

        var g = groups.find { it.groupName == groupName }

        if (g == null) {
            g = ApiGroup(groupName)
            groups.add(g)
        }

        return g
    }

    fun groupNames(): Set<String> {
        val names : MutableSet<String> = mutableSetOf()
        for (group in groups) {
            names.add(group.groupName)
        }
        return names
    }

    fun AddApiInfo(apiInfo: ApiInfo) {
        val group = this.GetApiGroupByName(apiInfo.groupName())
        group.apiInfoList.add(apiInfo)
    }

    fun ToMarkdownStr(str: String): String {
        return Helper.EscapeMarkdown(str)
    }
}
