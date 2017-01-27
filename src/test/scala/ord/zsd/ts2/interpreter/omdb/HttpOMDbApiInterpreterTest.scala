package ord.zsd.ts2.interpreter.omdb

import org.scalatest.{FunSuite, Matchers}

class HttpOMDbApiInterpreterTest extends FunSuite with Matchers {

//  test("Find movie") {
//    val findMedia = FindDetails(findType = MovieByTitle("pulp fiction"))
//    val task = interpret(findMedia)
//
//    val findResponse = Await.result(task.runAsync, 10.seconds)
//
//    findResponse should matchPattern {
//      case Right(MediaDetails("Pulp Fiction",
//      "tt0110912",
//      MovieType,
//      "1994",
//      Some("R"),
//      Some("14 Oct 1994"),
//      Some("154 min"),
//      Vector("Crime", "Drama"),
//      Vector("Quentin Tarantino"),
//      _, _, _, _, _, _, _, _, _, _, _)) =>
//    }
//  }
//
//  test("Find episode") {
//    val findMedia = FindDetails(findType = EpisodeByTitle("game of thrones", season = 6, episode = 9))
//    val task = interpret(findMedia)
//
//    val findResponse = Await.result(task.runAsync, 10.seconds)
//
//    findResponse should matchPattern {
//      case Right(MediaDetails("Battle of the Bastards",
//      "tt4283088",
//      EpisodeType,
//      "2016",
//      Some("TV-MA"),
//      Some("19 Jun 2016"),
//      Some("60 min"),
//      Vector("Adventure", "Drama", "Fantasy"),
//      Vector("Miguel Sapochnik"),
//      _, _, _, _, _, _, _, _, _, _,
//      EpisodeSpecifics(6, 9, "tt0944947"))) =>
//    }
//  }
//
//  test("Find series") {
//    val findMedia = FindDetails(findType = SeriesByTitle("Westworld"))
//    val task = interpret(findMedia)
//
//    val findResponse = Await.result(task.runAsync, 10.seconds)
//
//    findResponse should matchPattern {
//      case Right(MediaDetails("Westworld",
//      "tt0475784",
//      SeriesType,
//      _,
//      Some("TV-MA"),
//      Some("02 Oct 2016"),
//      Some("60 min"),
//      Vector("Drama", "Mystery", "Sci-Fi"),
//      _, _, _, _, _, _, _, _, _, _, _, _)) =>
//    }
//  }
}
