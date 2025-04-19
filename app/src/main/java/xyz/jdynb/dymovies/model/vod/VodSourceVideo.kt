package xyz.jdynb.dymovies.model.vod

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class VodSourceVideo(
  val sources: List<VodProvider> = listOf(),
  val videos: List<VodVideo> = listOf()
)
