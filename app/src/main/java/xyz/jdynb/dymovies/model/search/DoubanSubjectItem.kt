package xyz.jdynb.dymovies.model.search


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DoubanSubjectItem(
  @SerialName("layout")
  var layout: String = "",
  @SerialName("type_name")
  var typeName: String = "",
  @SerialName("target_id")
  var targetId: String = "",
  @SerialName("target")
  var target: Target = Target(),
  @SerialName("target_type")
  var targetType: String = ""
) {
  @Serializable
  data class Target(
    @SerialName("rating")
    var rating: Rating = Rating(),
    @SerialName("controversy_reason")
    var controversyReason: String = "",
    @SerialName("title")
    var title: String = "",
    @SerialName("abstract")
    var `abstract`: String = "",
    @SerialName("has_linewatch")
    var hasLinewatch: Boolean = false,
    @SerialName("uri")
    var uri: String = "",
    @SerialName("cover_url")
    var coverUrl: String = "",
    @SerialName("year")
    var year: String = "",
    @SerialName("card_subtitle")
    var cardSubtitle: String = "",
    @SerialName("id")
    var id: String = "",
    @SerialName("null_rating_reason")
    var nullRatingReason: String = ""
  ) {
    @Serializable
    data class Rating(
      @SerialName("count")
      var count: Int = 0,
      @SerialName("max")
      var max: Int = 0,
      @SerialName("star_count")
      var starCount: Double = 0.0,
      @SerialName("value")
      var value: Double = 0.0
    )
  }
}