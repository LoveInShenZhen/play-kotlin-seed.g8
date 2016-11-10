package k.common

import play.api.Configuration
import play.api.Environment
import play.api.inject.Binding
import play.api.inject.Module
import scala.collection.Seq

/**
 * Created by kk on 16/5/5.
 */
class KBaseModule : Module() {
    override fun bindings(environment: Environment, configuration: Configuration): Seq<Binding<*>> {

        return seq(
                //                bind(DefinedAPIs.class).toSelf().eagerly()
                bind(OnKBaseStartStop::class.java).toSelf().eagerly())
    }
}
