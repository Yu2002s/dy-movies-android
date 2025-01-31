package xyz.jdynb.dymovies.model.vod

import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class VodVideo(
  @SerialName("id")
  val videoId: Int = 0,
  @SerialName("vid")
  var vid: Int = 0,
  @SerialName("name")
  var name: String = "",
  @SerialName("url")
  var url: String = "",
  @SerialName("flag")
  var flag: String = "",
): BaseObservable() {

  var isChecked = false

  override fun equals(other: Any?): Boolean {
    if (other == null) return false
    if (other !is VodVideo) return false
    if (other.vid != vid || other.flag != flag) return false
    return true
  }

  override fun hashCode(): Int {
    return super.hashCode()
  }
}