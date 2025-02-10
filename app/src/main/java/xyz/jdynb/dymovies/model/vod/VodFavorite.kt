package xyz.jdynb.dymovies.model.vod

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport

@Serializable
@Keep
data class VodFavorite(
  @SerialName("detailId")
  @Column(nullable = false)
  val detailId: Int = 0,
  @SerialName("title")
  @Column(nullable = false, defaultValue = "")
  val title: String? = "",
  @SerialName("pic")
  @Column(nullable = false, defaultValue = "")
  val pic: String = "",
  /*@SerialName("current")
  @Column(nullable = false, defaultValue = "0")
  val current: Long = 0,*/
  @Column(nullable = false, defaultValue = "0")
  @SerialName("duration")
  val duration: Long = 0,
  @Column(nullable = false)
  @SerialName("updateAt")
  val updateAt: Long = System.currentTimeMillis()
) : LitePalSupport() {

  constructor(vodDetail: VodDetail) : this(
    vodDetail.detailId,
    vodDetail.title,
    vodDetail.pic,
    vodDetail.duration
  )

  val id: Long = 0

  fun update() {
    update(id)
  }
}
