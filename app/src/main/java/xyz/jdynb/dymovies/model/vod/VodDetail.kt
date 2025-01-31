package xyz.jdynb.dymovies.model.vod

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport

@Keep
@Serializable
data class VodDetail(
  @Column(unique = true, nullable = false)
  @SerialName("id")
  var detailId: Int = 0,
  @SerialName("vid")
  @Column(nullable = false, index = true)
  var vid: Int = 0,
  @SerialName("name")
  var name: String = "",
  @SerialName("pic")
  var pic: String = "",
  @SerialName("lang")
  var lang: String = "",
  @SerialName("area")
  var area: String = "",
  @SerialName("year")
  var year: String = "",
  @SerialName("actor")
  var actor: String = "",
  @SerialName("director")
  var director: String = "",
  @SerialName("flag")
  var flag: String = "",
  @SerialName("des")
  var des: String = "",
  @SerialName("title")
  /**
   * 标题
   */
  @Column(nullable = true)
  var title: String? = "",

  /**
   * 视频id
   */
  @SerialName("videoId")
  var videoId: Int = 0,

  /**
   * 视频地址
   */
  @SerialName("videoUrl")
  var videoUrl: String = "",
  /**
   * 当前播放进度
   */
  @Column(nullable = false, defaultValue = "0")
  var currentProgress: Long = 0,

  /**
   * 影片时长
   */
  @Column(nullable = false, defaultValue = "0")
  var duration: Long = 0,

  /**
   * 创建时间
   */
  var createdAt: Long = System.currentTimeMillis(),

  /**
   * 更新时间
   */
  var updatedAt: Long = createdAt,

  /**
   * 跳过片头开始位置
   */
  @Column(nullable = false, defaultValue = "0")
  var skipStart: Long = 0,

  /**
   * 跳过片头结束位置
   */
  @Column(nullable = false, defaultValue = "0")
  var skipEnd: Long = 0,

  // @Column(nullable = false, defaultValue = "false")
  // var isFavorite: Boolean = false,
): LitePalSupport() {

  @SerialName("detailId")
  val id: Long = 0

  fun update() {
    update(id)
  }

  data class Actor(
    val name: String
  )
}