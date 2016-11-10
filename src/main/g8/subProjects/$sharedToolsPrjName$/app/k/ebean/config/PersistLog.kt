package models.K.EbeanConfig

import com.fasterxml.jackson.databind.JsonNode
import jodd.datetime.JDateTime

/**
 * Created by kk on 14/11/28.
 */
class PersistLog {

    var action_time: JDateTime
    var action_type: String? = null
    var bean_class: String? = null
    var bean_data: JsonNode? = null

    init {
        action_time = JDateTime()
    }
}
