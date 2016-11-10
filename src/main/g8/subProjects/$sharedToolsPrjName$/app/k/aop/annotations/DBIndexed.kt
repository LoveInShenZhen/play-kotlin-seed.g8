package k.aop.annotations

/**
 * Created by kk on 14/11/14.
 */

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class DBIndexed
