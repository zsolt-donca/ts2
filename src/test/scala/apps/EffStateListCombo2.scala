package apps

import org.atnos.eff._
import all._
import syntax.all._
import cats.data._
import cats.implicits._

import scala.collection.immutable.Seq
import scala.language.higherKinds


object EffStateListCombo2 extends App {

  type Cache = Map[(String, Any), Any]

  def emptyCache: Cache = Map.empty

  type CacheState[T] = State[Cache, T]
  type _cacheState[R] = CacheState |= R

  type Logging[T] = Writer[String, T]
  type _logging[R] = Logging |= R

  def cached[R: _cacheState : _logging, A, B](name: String)(f: A => B)(a: A): Eff[R, B] = {
    for {
      cache <- get
      key = (name, a)
      res <- if (cache.contains(key)) {
        val value = cache(key).asInstanceOf[B]
        for {
          _ <- tell(s"Cache hit: $key -> $value")
        } yield value
      } else {
        val value = f(a)
        for {
          _ <- tell(s"Cache miss for: $key -> $value")
          _ <- put(cache + (key -> value))
        } yield value
      }
    } yield res
  }

  def square[R: _cacheState : _logging]: (Int => Eff[R, Int]) = cached[R, Int, Int]("square")(i => i * i)

  def plusMinus(i: Int): List[Int] = List(i, -i)

  type Stack = Fx.fx3[CacheState, List, Logging]
  val p1: Eff[Stack, Int] = for {
    num1 <- values[Stack, Int](1, 2, 3, 2, 1)
    num2 <- values[Stack, Int](plusMinus(num1): _*)
    squared <- square[Stack].apply(num2)
  } yield squared

  val (res1, log1): (List[(Int, Cache)], List[String]) = p1.runState(emptyCache).runList.runWriter[String].run
  res1.foreach({ case (result, cache) => println(s"result: $result, cache: $cache") })
  log1.foreach(log => println(log))

  println("------------------------------------------------------------------------")

  val ((results2: Seq[Int], cache2: Cache), log2: List[String]) = p1.runList.runState(emptyCache).runWriter[String].run
  println(s"results: $results2, cache: $cache2")
  log2.foreach(log => println(log))
}
