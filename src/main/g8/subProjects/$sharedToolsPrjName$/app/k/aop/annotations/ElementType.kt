package k.aop.annotations

import kotlin.reflect.KClass

/**
 * Created by kk on 16/8/29.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ElementType(val kclazz : KClass<*>)