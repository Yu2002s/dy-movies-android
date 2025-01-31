package xyz.jdynb.dymovies.model.vod

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.jdynb.dymovies.model.user.User

@Keep
@Serializable
data class VodReply(
  @SerialName("id")
  var id: Int = 0,
  @SerialName("fromUser")
  var fromUser: User = User(),
  @SerialName("toUser")
  var toUser: User = User(),
  @SerialName("content")
  var content: String = "",
  @SerialName("createAt")
  var createAt: String = ""
)