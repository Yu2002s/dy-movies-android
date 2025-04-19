package xyz.jdynb.dymovies.model.vod

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class VodProvider(
  val id: Int = 0,
  val name: String = "",
  val remark: String = ""
)
