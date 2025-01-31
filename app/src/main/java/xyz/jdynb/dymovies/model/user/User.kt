package xyz.jdynb.dymovies.model.user

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class User(
  @SerialName("id")
  val id: Int = 0,
  @SerialName("email")
  val email: String = "",
  @SerialName("username")
  val username: String = "",
  @SerialName("avatar")
  val avatar: String = ""
)
