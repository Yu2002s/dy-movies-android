package xyz.jdynb.dymovies.model.iqiyi

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class IqiyiSearch(
  @SerialName("templates")
  val templates: List<Template> = emptyList()
) {

  @Serializable
  @Keep
  data class Template(
    @SerialName("template")
    val template: Int = 0,
    @SerialName("albumInfo")
    val albumInfo: AlbumInfo? = null,
  )

  @Serializable
  @Keep
  data class AlbumInfo(
    @SerialName("title")
    val title: String = "",
    @SerialName("year")
    val year: Year = Year(),
    @SerialName("videos")
    val videos: List<Video> = emptyList(),
    val qipuId: Long = 0,
  )

  @Serializable
  @Keep
  data class Year(
    @SerialName("value")
    val value: String = ""
  )

  @Serializable
  @Keep
  data class Video(
    @SerialName("pageUrl")
    val pageUrl: String = ""
  )
}