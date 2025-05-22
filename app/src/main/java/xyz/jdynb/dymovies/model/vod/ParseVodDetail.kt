package xyz.jdynb.dymovies.model.vod


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litepal.crud.LitePalSupport

@Serializable
data class ParseVodDetail(
  @SerialName("@context")
  var context: String = "",
  @SerialName("name")
  var name: String = "",
  @SerialName("url")
  var url: String = "",
  @SerialName("image")
  var image: String = "",
  @SerialName("director")
  var director: List<Director> = listOf(),
  @SerialName("author")
  var author: List<Author> = listOf(),
  @SerialName("actor")
  var actor: List<Actor> = listOf(),
  @SerialName("datePublished")
  var datePublished: String = "",
  @SerialName("genre")
  var genre: List<String> = listOf(),
  @SerialName("duration")
  var duration: String = "",
  @SerialName("description")
  var description: String = "",
  @SerialName("@type")
  var type: String = "",
  @SerialName("aggregateRating")
  var aggregateRating: AggregateRating = AggregateRating()
) : LitePalSupport() {

  val id: Int = 0

  @Serializable
  data class Director(
    @SerialName("@type")
    var type: String = "",
    @SerialName("url")
    var url: String = "",
    @SerialName("name")
    var name: String = ""
  )

  @Serializable
  data class Author(
    @SerialName("@type")
    var type: String = "",
    @SerialName("url")
    var url: String = "",
    @SerialName("name")
    var name: String = ""
  )

  @Serializable
  data class Actor(
    @SerialName("@type")
    var type: String = "",
    @SerialName("url")
    var url: String = "",
    @SerialName("name")
    var name: String = ""
  )

  @Serializable
  data class AggregateRating(
    @SerialName("@type")
    var type: String = "",
    @SerialName("ratingCount")
    var ratingCount: String = "",
    @SerialName("bestRating")
    var bestRating: String = "",
    @SerialName("worstRating")
    var worstRating: String = "",
    @SerialName("ratingValue")
    var ratingValue: String = ""
  )
}