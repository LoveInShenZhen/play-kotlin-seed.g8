package k.controllers.apidoc.sampleform


import k.aop.annotations.Comment
import k.common.Hub

/**
 * Created by kk on 15/7/17.
 */
class PostFormSample {

    @Comment("表单字段1")
    var form_field_1: String? = null

    @Comment("表单字段2")
    var form_field_2: String? = null

    @Comment("表单字段3")
    var form_field_3: String? = null

    companion object {

        var theForm = Hub.formFactory().form<PostFormSample>(PostFormSample::class.java)
    }

}
