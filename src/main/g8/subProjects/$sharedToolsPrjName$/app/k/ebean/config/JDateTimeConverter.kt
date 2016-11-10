package models

import com.avaje.ebean.config.ScalarTypeConverter
import jodd.datetime.JDateTime

import java.util.Date

/**
 * Created by kk on 16/2/21.
 */
class JDateTimeConverter : ScalarTypeConverter<JDateTime, Date> {
    override fun getNullValue(): JDateTime? {
        return null
    }

    override fun wrapValue(scalarType: Date): JDateTime {
        return JDateTime(scalarType)
    }

    override fun unwrapValue(beanType: JDateTime): Date {
        return beanType.convertToDate()
    }
}
