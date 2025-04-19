package xyz.jdynb.dymovies.model.vod

import androidx.annotation.Keep
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.databinding.library.baseAdapters.BR
import com.drake.brv.binding.ObservableIml
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport

/**
 * 影片详情
 */
@Keep
@Serializable
data class VodDetail(
  /**
   * 详情 id
   */
  @Column(unique = true, nullable = false)
  @SerialName("id")
  var detailId: Int = 0,
  /**
   * 影片 id
   */
  @SerialName("vid")
  @Column(nullable = false, index = true)
  var vid: Int = 0,
  /**
   * 影片名称
   */
  @SerialName("name")
  var name: String = "",
  /**
   * 影片封面
   */
  @SerialName("pic")
  var pic: String = "",
  /**
   * 影片语言
   */
  @SerialName("lang")
  var lang: String = "",
  /**
   * 影片地区
   */
  @SerialName("area")
  var area: String = "",
  /**
   * 影片年份
   */
  @SerialName("year")
  var year: String = "",
  /**
   * 影片演员
   */
  @SerialName("actor")
  var actor: String = "",
  /**
   * 影片导演
   */
  @SerialName("director")
  var director: String = "",
  /**
   * 影片标识 (采集来源)
   */
  @SerialName("flag")
  var flag: String = "",
  /**
   * 影片详情信息
   */
  @SerialName("des")
  var des: String = "",
  /**
   * 影片标题
   */
  @SerialName("title")
  @Column(nullable = true)
  var title: String? = "",
  /**
   * 影片类型 id
   */
  @Column(nullable = true)
  @SerialName("tid")
  var tid: Int = 0,
  /**
   * 影片类型名
   */
  @Column(nullable = true)
  @SerialName("type")
  var type: String = "",
  /**
   * 影片热度
   */
  @Column(nullable = true, defaultValue = "0")
  @SerialName("hits")
  var hits: Int = 0,
  /**
   * 影片评分
   */
  @Column(nullable = true, defaultValue = "0")
  @SerialName("score")
  var score: Float = 0f,
  /**
   * 视频 id
   */
  @SerialName("videoId")
  var videoId: Int = 0,

  /**
   * 视频地址
   */
  @SerialName("videoUrl")
  var videoUrl: String? = null,
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
  var skipStart: Int = 0,

  /**
   * 跳过片头结束位置
   */
  @Column(nullable = false, defaultValue = "0")
  var skipEnd: Int = 0,
) : LitePalSupport(), Observable {

  /**
   * 数据库中的 id
   */
  @SerialName("detailId")
  val id: Long = 0

  /**
   * 视频数量
   */
  @Column(ignore = true)
  @Transient
  @get:Bindable
  var videoCount = 0
    set(value) {
      field = value
      registry.notifyChange(this, BR.videoCount)
    }


  @Column(ignore = true)
  @Transient
  @get:Bindable
  var loadingVideos = false
    set(value) {
      field = value
      registry.notifyChange(this, BR.loadingVideos)
    }

  @Column(ignore = true)
  @Transient
  @get:Bindable
  var isChecked = false
    set(value) {
      field = value
      registry.notifyChange(this, BR.checked)
    }

  @Column(ignore = true)
  @get:Bindable
  var isVisibleCheck = false
    set(value) {
      field = value
      registry.notifyChange(this, BR.visibleCheck)
    }

  /**
   * 更新影片详情
   */
  fun update() {
    update(id)
  }

  override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
    registry.add(callback)
  }

  override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
    registry.remove(callback)
  }

  @Transient
  private val registry = PropertyChangeRegistry()
}