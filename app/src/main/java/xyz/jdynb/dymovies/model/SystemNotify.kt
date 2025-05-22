package xyz.jdynb.dymovies.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SystemNotify(
  @SerialName("id")
  var id: Int = 0,
  @SerialName("content")
  var content: String = "",
  @SerialName("createAt")
  var createAt: String = ""
)