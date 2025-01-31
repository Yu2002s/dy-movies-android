package xyz.jdynb.dymovies.model.vod

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class Job(
  val key: String = "",
  val name: String = ""
)
