package org.zsd.ts2.omdbapi.interpreter

import monix.execution.Scheduler.Implicits.global
import ord.zsd.ts2.omdbapi._
import ord.zsd.ts2.omdbapi.interpreter.HttpOMDbApiInterpreter
import org.scalatest.FunSuite

import scala.concurrent.Await
import scala.concurrent.duration._

class HttpOMDbApiInterpreterTest extends FunSuite {

  import HttpOMDbApiInterpreter.interpret

  test("Find by title should work") {
    val findMedia = FindMedia(findType = FindByTitle("pulp fiction"))
    val task = interpret(findMedia)

    val findResponse: FindResponse = Await.result(task.runAsync, 10.seconds)

    val expected = FindResult(
      title = "Pulp Fiction",
      imdbId = "tt0110912",
      mediaType = MovieType,
      year = "1994",
      rated = Some("R"),
      released = Some("14 Oct 1994"),
      runtime = Some("154 min"),
      genre = Vector("Crime", "Drama"),
      director = Vector("Quentin Tarantino"),
      writer = Vector("Quentin Tarantino (story)", "Roger Avary (story)", "Quentin Tarantino"),
      actors = Vector("Tim Roth", "Amanda Plummer", "Laura Lovelace", "John Travolta"),
      plot = Some("The lives of two mob hit men, a boxer, a gangster's wife, and a pair of diner bandits intertwine in four tales of violence and redemption."),
      language = Some("English, Spanish, French"),
      country = Some("USA"),
      awards = Some("Won 1 Oscar. Another 60 wins & 65 nominations."),
      poster = Some("https://images-na.ssl-images-amazon.com/images/M/MV5BMTkxMTA5OTAzMl5BMl5BanBnXkFtZTgwNjA5MDc3NjE@._V1_SX300.jpg"),
      metascore = Some("94"),
      imdbRating = Some("8.9"),
      imdbVotes = Some("1,376,761"),
      MovieSpecifics
    )

    assert(findResponse == expected)
  }
}
