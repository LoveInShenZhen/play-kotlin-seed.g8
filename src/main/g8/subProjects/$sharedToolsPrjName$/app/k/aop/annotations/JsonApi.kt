package k.aop.annotations

import k.controllers.apidoc.sampleform.PostFormSample
import k.reply.ReplyBase
import play.mvc.With
import kotlin.reflect.KClass

/**
 * Created by kk on 14-6-10.
 */
@With(k.aop.actions.JsonApiAction::class)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonApi(val Transactional: Boolean = true,
                         val UseEtag: Boolean = false,
                         val ReplyClass: KClass<*> = ReplyBase::class,
                         val ApiMethodType: String = "GET",
                         val PostDataClass: KClass<*> = PostFormSample::class)
