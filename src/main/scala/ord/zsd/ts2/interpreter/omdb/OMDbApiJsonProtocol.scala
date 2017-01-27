package ord.zsd.ts2.interpreter.omdb

import ord.zsd.ts2.omdbapi.OMDbOp.FindResponse
import ord.zsd.ts2.omdbapi._
import ord.zsd.ts2.utils.UnapplyUtils.SomeInt
import spray.json.{DefaultJsonProtocol, JsNull, JsNumber, JsObject, JsString, JsValue, RootJsonReader, deserializationError}

object OMDbApiJsonProtocol extends DefaultJsonProtocol {

  implicit val findResponseReader: RootJsonReader[FindResponse] = new RootJsonReader[FindResponse] {
    override def read(json: JsValue): FindResponse = {
      json match {
        case jsObject: JsObject =>
          jsObject.fields.get("Response") match {
            case Some(JsString("True")) => Right(deserializeSuccessResponse(jsObject))
            case Some(JsString("False")) => Left(deserializeErrorResponse(jsObject))
            case _ => deserializationError("Object does not have a valid 'Response' field: " + jsObject)
          }
        case _ => deserializationError("Expected an object, but got: " + json)
      }
    }
  }

  private def deserializeSuccessResponse(jsObject: JsObject): MediaDetails = {
    val fields = jsObject.fields

    def mandatoryStringField(field: String): String = fields.get(field) match {
      case Some(JsString(value)) => value
      case _ => deserializationError(s"Object does not have a '$field' field with string type: $jsObject")
    }

    def mandatoryIntField(field: String): Int = fields.get(field) match {
      case Some(JsNumber(value)) => value.toInt
      case Some(JsString(SomeInt(int))) => int
      case _ => deserializationError(s"Object does not have a '$field' field with number type: $jsObject")
    }

    def mandatoryEnumField[T](field: String, enum: Map[String, T]): T = fields.get(field) match {
      case Some(JsString(value)) if enum.contains(value) => enum(value)
      case _ => deserializationError(s"Object does not have a '$field' field with enum type (${enum.keySet}): $jsObject")
    }

    def optionalStringField(field: String): Option[String] = fields.get(field) match {
      case Some(JsString("N/A")) | Some(JsNull) => None
      case Some(JsString(value)) => Some(value)
      case Some(_) => deserializationError(s"Object should have a field $field with string type, but the type is something else: $jsObject")
      case _ => None
    }

    def optionalIntField(field: String): Option[Int] = fields.get(field) match {
      case Some(JsString("N/A")) | Some(JsNull) | None => None
      case Some(JsNumber(value)) => Some(value.toInt)
      case Some(JsString(SomeInt(int))) => Some(int)
      case _ => deserializationError(s"Object does not have a '$field' field with number type: $jsObject")
    }

    def optionalSeqField(field: String): Seq[String] = optionalStringField(field)
      .map(_.split(',').map(_.trim).filter(_.nonEmpty).toVector)
      .getOrElse(Vector())

    val mediaType = mandatoryEnumField("Type", Map("movie" -> MovieType, "series" -> SeriesType, "episode" -> EpisodeType))
    MediaDetails(
      title = mandatoryStringField("Title"),
      imdbId = mandatoryStringField("imdbID"),
      mediaType = mediaType,
      year = mandatoryStringField("Year"),
      rated = optionalStringField("Rated"),
      released = optionalStringField("Released"),
      runtime = optionalStringField("Runtime"),
      genre = optionalSeqField("Genre"),
      director = optionalSeqField("Director"),
      writer = optionalSeqField("Writer"),
      actors = optionalSeqField("Actors"),
      plot = optionalStringField("Plot"),
      language = optionalStringField("Language"),
      country = optionalStringField("Country"),
      awards = optionalStringField("Awards"),
      poster = optionalStringField("Poster"),
      metascore = optionalStringField("Metascore"),
      imdbRating = optionalStringField("imdbRating"),
      imdbVotes = optionalStringField("imdbVotes"),
      typeSpecifics = mediaType match {
        case MovieType => MovieSpecifics
        case SeriesType => SeriesSpecifics(mandatoryIntField("totalSeasons"))
        case EpisodeType => EpisodeSpecifics(mandatoryIntField("Season"),
          mandatoryIntField("Episode"),
          mandatoryStringField("seriesID"))
      }
    )
  }

  private def deserializeErrorResponse(jsObject: JsObject): String = jsObject.fields.get("Error") match {
    case Some(JsString(message)) => message
    case _ => deserializationError(s"Object does not have an 'Error' field with string type: $jsObject")
  }

}
