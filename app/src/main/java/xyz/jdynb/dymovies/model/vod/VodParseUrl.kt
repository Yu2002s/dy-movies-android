package xyz.jdynb.dymovies.model.vod

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 对于一些播放地址不是 m3u8 文件，则需要进行额外的解析操作
 */
@Serializable
@Keep
data class VodParseUrl(
  @SerialName("url")
  val url: String = ""
)
