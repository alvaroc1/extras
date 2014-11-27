package com.gravitydev.extras

object Enumerable {
  /**
   * Find all abstract data types (object enums) from a module matching a given type
   */
  private def adts [T] (o: AnyRef): List[T] = synchronized { // reflection api is apparently not thread-safe
    import scala.reflect.runtime.universe._
    import scala.reflect.runtime.currentMirror
    
    // use reflection to find all the adts
    currentMirror.reflectClass(currentMirror.classSymbol(o.getClass)).symbol.typeSignature.members.filter(_.isModule).map(x =>
      currentMirror.reflectModule(x.asModule).instance.asInstanceOf[T]
    ).toList.reverse // for some reason, they come in reverse order
  }
  
}

/**
 * Case-object-based replacement for Enums
 * the built in library should really have something like this
 */
trait Enumerable[T] {
  lazy val list = Enumerable.adts[T](this)
}
abstract class KeyedEnumerable [K,T] (keyFn: T=>K) extends Enumerable [T] {
  def forKey (key: K) = list.find(x => keyFn(x) == key)
  def keyFor (obj: T): K = keyFn(obj)
}

