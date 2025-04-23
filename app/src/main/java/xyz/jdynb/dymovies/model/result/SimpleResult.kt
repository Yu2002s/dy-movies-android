package xyz.jdynb.dymovies.model.result

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class SimpleResult(
  val code: Int,
  val msg: String,
)