package org.zsd.ts2.omdbapi.interpreter

import monix.execution.Scheduler.Implicits.global
import ord.zsd.ts2.omdbapi._
import ord.zsd.ts2.omdbapi.interpreter.HttpOMDbApiInterpreter
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class HttpOMDbApiInterpreterTest extends FunSuite with Matchers {

  import HttpOMDbApiInterpreter.interpret

  test("Find by title should work") {
    val findMedia = FindMedia(findType = FindByTitle("pulp fiction"))
    val task = interpret(findMedia)

    val findResponse: FindResponse = Await.result(task.runAsync, 10.seconds)

    findResponse should matchPattern {
      case FindResult("Pulp Fiction",
      "tt0110912",
      MovieType,
      "1994",
      Some("R"),
      Some("14 Oct 1994"),
      Some("154 min"),
      Vector("Crime", "Drama"),
      Vector("Quentin Tarantino"),
      _, _, _, _, _, _, _, _, _, _, _) =>
    }
  }
}
