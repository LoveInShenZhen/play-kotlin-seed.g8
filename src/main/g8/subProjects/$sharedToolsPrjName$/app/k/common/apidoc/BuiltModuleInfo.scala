package k.common.apidoc

import play.api.inject.Modules
import play.api.{Configuration, Environment}
import play.inject.BuiltInModule

import collection.JavaConverters._
/**
  * Created by kk on 16/10/11.
  */
object BuiltModuleInfo {

  def BuiltInModuleNames(environment: Environment, config: Configuration) = {
    Modules.locate(environment, config).map(it => it.getClass.getName).asJava
  }

  def BuiltInBindings(environment: Environment, config: Configuration) = {
    val x = new BuiltInModule()
    x.bindings(environment, config).map(it => it.toString()).asJava
  }
}
