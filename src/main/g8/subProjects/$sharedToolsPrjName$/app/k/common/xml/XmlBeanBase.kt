package k.common.xml

import org.simpleframework.xml.core.Persister

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Created by kk on 15/6/10.
 */
open class XmlBeanBase {

    fun ToXmlStr(): String {
        try {
            val bos = ByteArrayOutputStream()
            val persister = Persister()
            persister.write(this, bos, "UTF-8")
            return bos.toString("UTF-8")
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }

    }

    fun Validate(xml_str: String): Boolean {
        try {
            val bis = ByteArrayInputStream(xml_str.toByteArray(charset("UTF-8")))
            val persister = Persister()
            return persister.validate(this.javaClass, bis, false)
        } catch (ex: Exception) {
            return false
        }

    }

    open fun ParseFromStr(xml_str: String) {
        try {
            val bis = ByteArrayInputStream(xml_str.toByteArray(charset("UTF-8")))
            val persister = Persister()
            persister.read(this, bis, false)
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }

    }

}
