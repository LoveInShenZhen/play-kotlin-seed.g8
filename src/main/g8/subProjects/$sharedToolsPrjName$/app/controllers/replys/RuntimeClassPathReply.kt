package controllers.replys

import jodd.util.ClassLoaderUtil
import k.aop.annotations.Comment
import k.common.Hub
import k.reply.ReplyBase

/**
 * Created by kk on 16/12/16.
 */
class RuntimeClassPathReply : ReplyBase() {

    @Comment("运行时的ClassPath")
    var class_path = listOf<String>()

    fun Load() {
        this.class_path = ClassLoaderUtil.getDefaultClasspath(Hub.application().classloader()).map { it.absolutePath }
    }
}