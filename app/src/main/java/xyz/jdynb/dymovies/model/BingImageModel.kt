package xyz.jdynb.dymovies.model

import kotlinx.serialization.Serializable

@Serializable
data class BingImageResponse(
  val images: List<Image>
)

@Serializable
data class Image(val url: String)