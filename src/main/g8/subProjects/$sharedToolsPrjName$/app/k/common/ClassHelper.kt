package k.common

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Created by kk on 16/7/3.
 */
object ClassHelper {

    fun ElementClass(collectionClass: Class<*>) : Type {
        return (collectionClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
    }

}
