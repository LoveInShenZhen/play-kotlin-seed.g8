package k.aop

import kotlin.annotation.Retention
import kotlin.reflect.KClass

/**
 * Created by kk on 14/11/3.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ReplyMeta(val clazz: KClass<*>)
