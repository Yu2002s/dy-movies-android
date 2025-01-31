package xyz.jdynb.dymovies.model.live

import kotlinx.serialization.Serializable

@Serializable
data class TvLive(
  val id: Int,
  val name: String,
  val url: String
)
