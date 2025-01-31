package xyz.jdynb.dymovies.model.vod

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class VodSource(
  @SerialName("name")
  var name: String = "",
  @SerialName("count")
  var count: Int = 0,
  @SerialName("videos")
  var videos: List<VodVideo> = emptyList()
)
