package xyz.jdynb.dymovies.model.user

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class UserAuth(
  @SerialName("token")
  val token: String = ""
)