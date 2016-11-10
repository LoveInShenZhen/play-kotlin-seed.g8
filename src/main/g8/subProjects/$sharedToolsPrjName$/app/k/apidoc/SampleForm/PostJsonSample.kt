package k.controllers.apidoc.sampleform



import k.aop.annotations.Comment
import java.util.ArrayList

/**
 * Created by kk on 15/7/17.
 */
class PostJsonSample {

    @Comment("用户名")
    var name: String? = null

    @Comment("好友列表")
    var friends: List<String>

    init {
        friends = ArrayList<String>()
    }
}
