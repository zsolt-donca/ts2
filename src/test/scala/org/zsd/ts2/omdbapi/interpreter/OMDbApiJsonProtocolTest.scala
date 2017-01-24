package org.zsd.ts2.omdbapi.interpreter

import ord.zsd.ts2.omdbapi._
import ord.zsd.ts2.omdbapi.interpreter.OMDbApiJsonProtocol
import org.scalatest.FunSuite
import spray.json.{jsonReader, pimpString}

class OMDbApiJsonProtocolTest extends FunSuite {

  import OMDbApiJsonProtocol._

  test("Deserialize a partly unknown movie") {
    val json =
      """{
        |  "Title": "Terminator",
        |  "Year": "2001",
        |  "Rated": "N/A",
        |  "Released": "01 Apr 2001",
        |  "Runtime": "N/A",
        |  "Genre": "Short, Action",
        |  "Director": "Ryan McDonald",
        |  "Writer": "Ryan McDonald",
        |  "Actors": "Joshua Miller",
        |  "Plot": "N/A",
        |  "Language": "English",
        |  "Country": "USA",
        |  "Awards": "N/A",
        |  "Poster": "N/A",
        |  "Metascore": "N/A",
        |  "imdbRating": "4.8",
        |  "imdbVotes": "16",
        |  "imdbID": "tt1994570",
        |  "Type": "movie",
        |  "Response": "True"
        |}
        |""".stripMargin.parseJson

    val response = jsonReader[FindResponse].read(json)
    val expected = FindResult(
      title = "Terminator",
      year = "2001",
      rated = None,
      released = Some("01 Apr 2001"),
      runtime = None,
      genre = Vector("Short", "Action"),
      director = Vector("Ryan McDonald"),
      writer = Vector("Ryan McDonald"),
      actors = Vector("Joshua Miller"),
      plot = None,
      language = Some("English"),
      country = Some("USA"),
      awards = None,
      poster = None,
      metascore = None,
      imdbRating = Some("4.8"),
      imdbVotes = Some("16"),
      imdbId = "tt1994570",
      mediaType = MovieType,
      typeSpecifics = MovieSpecifics
    )

    assert(response == expected)
  }

  test("Deserialize a completely known movie") {
    val json =
      """{
        |  "Title": "The Shawshank Redemption",
        |  "Year": "1994",
        |  "Rated": "R",
        |  "Released": "14 Oct 1994",
        |  "Runtime": "142 min",
        |  "Genre": "Crime, Drama",
        |  "Director": "Frank Darabont",
        |  "Writer": "Stephen King (short story \"Rita Hayworth and Shawshank Redemption\"), Frank Darabont (screenplay)",
        |  "Actors": "Tim Robbins, Morgan Freeman, Bob Gunton, William Sadler",
        |  "Plot": "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.",
        |  "Language": "English",
        |  "Country": "USA",
        |  "Awards": "Nominated for 7 Oscars. Another 19 wins & 30 nominations.",
        |  "Poster": "https://images-na.ssl-images-amazon.com/images/M/MV5BODU4MjU4NjIwNl5BMl5BanBnXkFtZTgwMDU2MjEyMDE@._V1_SX300.jpg",
        |  "Metascore": "80",
        |  "imdbRating": "9.3",
        |  "imdbVotes": "1,754,270",
        |  "imdbID": "tt0111161",
        |  "Type": "movie",
        |  "Response": "True"
        |}
        |""".stripMargin.parseJson

    val response = jsonReader[FindResponse].read(json)
    val expected = FindResult(
      title = "The Shawshank Redemption",
      year = "1994",
      rated = Some("R"),
      released = Some("14 Oct 1994"),
      runtime = Some("142 min"),
      genre = Vector("Crime", "Drama"),
      director = Vector("Frank Darabont"),
      writer = Vector("Stephen King (short story \"Rita Hayworth and Shawshank Redemption\")", "Frank Darabont (screenplay)"),
      actors = Vector("Tim Robbins", "Morgan Freeman", "Bob Gunton", "William Sadler"),
      plot = Some("Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency."),
      language = Some("English"),
      country = Some("USA"),
      awards = Some("Nominated for 7 Oscars. Another 19 wins & 30 nominations."),
      poster = Some("https://images-na.ssl-images-amazon.com/images/M/MV5BODU4MjU4NjIwNl5BMl5BanBnXkFtZTgwMDU2MjEyMDE@._V1_SX300.jpg"),
      metascore = Some("80"),
      imdbRating = Some("9.3"),
      imdbVotes = Some("1,754,270"),
      imdbId = "tt0111161",
      mediaType = MovieType,
      typeSpecifics = MovieSpecifics
    )

    assert(response == expected)
  }

  test("Deserialize a series") {
    val json =
      """{
        |  "Title": "Westworld",
        |  "Year": "2016–",
        |  "Rated": "TV-MA",
        |  "Released": "02 Oct 2016",
        |  "Runtime": "60 min",
        |  "Genre": "Drama, Mystery, Sci-Fi",
        |  "Director": "N/A",
        |  "Writer": "Lisa Joy, Jonathan Nolan",
        |  "Actors": "Evan Rachel Wood, Thandie Newton, James Marsden, Ed Harris",
        |  "Plot": "A Western-themed futuristic theme park, populated with artificial intelligence, allows high-paying guests to live out their fantasies with no consequences or retaliation from the android hosts, until now.",
        |  "Language": "English",
        |  "Country": "USA",
        |  "Awards": "Nominated for 3 Golden Globes. Another 5 wins & 12 nominations.",
        |  "Poster": "https://images-na.ssl-images-amazon.com/images/M/MV5BMTEyODk5NTc2MjNeQTJeQWpwZ15BbWU4MDQ5NTgwOTkx._V1_SX300.jpg",
        |  "Metascore": "N/A",
        |  "imdbRating": "9.1",
        |  "imdbVotes": "122,916",
        |  "imdbID": "tt0475784",
        |  "Type": "series",
        |  "totalSeasons": "2",
        |  "Response": "True"
        |}
        |""".stripMargin.parseJson

    val response = jsonReader[FindResponse].read(json)
    val expected = FindResult(
      title = "Westworld",
      year = "2016–",
      rated = Some("TV-MA"),
      released = Some("02 Oct 2016"),
      runtime = Some("60 min"),
      genre = Vector("Drama", "Mystery", "Sci-Fi"),
      director = Vector(),
      writer = Vector("Lisa Joy", "Jonathan Nolan"),
      actors = Vector("Evan Rachel Wood", "Thandie Newton", "James Marsden", "Ed Harris"),
      plot = Some("A Western-themed futuristic theme park, populated with artificial intelligence, allows high-paying guests to live out their fantasies with no consequences or retaliation from the android hosts, until now."),
      language = Some("English"),
      country = Some("USA"),
      awards = Some("Nominated for 3 Golden Globes. Another 5 wins & 12 nominations."),
      poster = Some("https://images-na.ssl-images-amazon.com/images/M/MV5BMTEyODk5NTc2MjNeQTJeQWpwZ15BbWU4MDQ5NTgwOTkx._V1_SX300.jpg"),
      metascore = None,
      imdbRating = Some("9.1"),
      imdbVotes = Some("122,916"),
      imdbId = "tt0475784",
      mediaType = SeriesType,
      typeSpecifics = SeriesSpecifics(2)
    )

    assert(response == expected)
  }

  test("Deserialize an episode") {
    val json =
      """
        |{
        |  "Title": "Battle of the Bastards",
        |  "Year": "2016",
        |  "Rated": "TV-MA",
        |  "Released": "19 Jun 2016",
        |  "Season": "6",
        |  "Episode": "9",
        |  "Runtime": "60 min",
        |  "Genre": "Adventure, Drama, Fantasy",
        |  "Director": "Miguel Sapochnik",
        |  "Writer": "George R.R. Martin (based on \"A Song of Ice and Fire\" by), David Benioff (created by), D.B. Weiss (created by), David Benioff (written for television by), D.B. Weiss (written for television by)",
        |  "Actors": "Peter Dinklage, Kit Harington, Emilia Clarke, Liam Cunningham",
        |  "Plot": "Jon and Sansa face Ramsay Bolton on the fields of Winterfell. Daenerys strikes back at her enemies. Theon and Yara arrive in Meereen.",
        |  "Language": "English",
        |  "Country": "USA",
        |  "Awards": "N/A",
        |  "Poster": "https://images-na.ssl-images-amazon.com/images/M/MV5BMTQ4NDU4Mzg5MF5BMl5BanBnXkFtZTgwNTI3ODgxOTE@._V1_SX300.jpg",
        |  "Metascore": "N/A",
        |  "imdbRating": "9.9",
        |  "imdbVotes": "133962",
        |  "imdbID": "tt4283088",
        |  "seriesID": "tt0944947",
        |  "Type": "episode",
        |  "Response": "True"
        |}
      """.stripMargin.parseJson

    val response = jsonReader[FindResponse].read(json)
    val expected = FindResult(
      title = "Battle of the Bastards",
      year = "2016",
      rated = Some("TV-MA"),
      released = Some("19 Jun 2016"),
      runtime = Some("60 min"),
      genre = Vector("Adventure", "Drama", "Fantasy"),
      director = Vector("Miguel Sapochnik"),
      writer = Vector("George R.R. Martin (based on \"A Song of Ice and Fire\" by)", "David Benioff (created by)", "D.B. Weiss (created by)", "David Benioff (written for television by)", "D.B. Weiss (written for television by)"),
      actors = Vector("Peter Dinklage", "Kit Harington", "Emilia Clarke", "Liam Cunningham"),
      plot = Some("Jon and Sansa face Ramsay Bolton on the fields of Winterfell. Daenerys strikes back at her enemies. Theon and Yara arrive in Meereen."),
      language = Some("English"),
      country = Some("USA"),
      awards = None,
      poster = Some("https://images-na.ssl-images-amazon.com/images/M/MV5BMTQ4NDU4Mzg5MF5BMl5BanBnXkFtZTgwNTI3ODgxOTE@._V1_SX300.jpg"),
      metascore = None,
      imdbRating = Some("9.9"),
      imdbVotes = Some("133962"),
      imdbId = "tt4283088",
      mediaType = EpisodeType,
      typeSpecifics = EpisodeSpecifics(6, 9, "tt0944947")
    )

    assert(response == expected)
  }

  def diff(a: Product, b: Product): Unit = {
    assert(a.productArity == b.productArity)
    for (i <- 0 until a.productArity) {
      println("Testing field #" + i + s": ${a.productElement(i)} vs ${b.productElement(i)}")
      assert(a.productElement(i) == b.productElement(i))
    }
  }
}
