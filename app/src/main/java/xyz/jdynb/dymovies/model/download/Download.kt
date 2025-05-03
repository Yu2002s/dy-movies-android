package xyz.jdynb.dymovies.model.download

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.databinding.library.baseAdapters.BR
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport
import xyz.jdynb.dymovies.DyMoviesApplication
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.download.DownloadListener
import xyz.jdynb.dymovies.utils.formatBytes

data class Download(
  /**
   * 下载名称
   */
  var name: String = "",
  /**
   * 分组名，用于创建下载分组
   */
  var groupName: String = "",

  /**
   * 封面地址
   */
  @Column(defaultValue = "")
  var cover: String = "",

  /**
   * 下载地址
   */
  var url: String = "",
  /**
   * 文件保存路径
   */
  var downloadPath: String = "",
  /**
   * 需要下载的字节总数
   */
  var byteCount: Long = 0L,
  /**
   * 创建时间
   */
  var createAt: Long = System.currentTimeMillis(),
  /**
   * 更新时间
   */
  var updateAt: Long = createAt,
) : LitePalSupport(), Observable {

  val id: Long = 0

  var listener: DownloadListener? = null

  /**
   * 当前已下载的字节数
   */
  var currentByte: Long = 0L

  /**
   * 进度
   */
  @get:Bindable
  var progress: Int = 0
    set(value) {
      field = value
      registry.notifyChange(this, BR.progress)
    }

  @get:Bindable
  var status: Int = DownloadStatus.UNSTART
    set(value) {
      field = value
      registry.notifyChange(this, BR.status)
    }

  val statusStr: String
    get() {
      return when (status) {
        DownloadStatus.UNSTART -> "未下载"
        DownloadStatus.PREPARE -> "准备中"
        DownloadStatus.DOWNLOADING -> "下载中"
        DownloadStatus.PAUSE -> "已暂停"
        DownloadStatus.ERROR -> "下载失败"
        DownloadStatus.MERGE -> "合并中"
        DownloadStatus.COMPLETED -> "已完成"
        else -> "未下载"
      }
    }

  val isDownloading get() = status == DownloadStatus.PREPARE || status == DownloadStatus.DOWNLOADING

  @get:Bindable("status")
  val isCompleted get() = status == DownloadStatus.COMPLETED

  @get:Bindable("progress", "status")
  val fullStatusStr: String
    get() = DyMoviesApplication.context.getString(R.string.download_status).format(
      statusStr,
      formatBytes(currentByte),
      progress
    )

  @Column(ignore = true)
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

  @get:Bindable("status")
  val downloadIcon
    get() = if (isDownloading) R.drawable.baseline_pause_circle_outline_24
    else R.drawable.baseline_arrow_circle_down_24

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Download

    return url == other.url
  }

  override fun hashCode(): Int {
    return url.hashCode()
  }

  fun update() {
    update(id)
    listener?.onDownload(this)
  }

  fun stop() {
    status = DownloadStatus.UNSTART
    update()
  }

  fun pause() {
    status = DownloadStatus.PAUSE
    update()
  }

  fun start() {
    status = DownloadStatus.DOWNLOADING
    update()
  }

  fun prepare() {
    status = DownloadStatus.PREPARE
    update()
  }

  fun error() {
    status = DownloadStatus.ERROR
    update()
  }

  fun merge() {
    status = DownloadStatus.MERGE
    update()
  }

  fun completed() {
    status = DownloadStatus.COMPLETED
    update()
  }

  override fun toString(): String {
    return "Download(name='$name', url='$url', downloadPath='$downloadPath', currentByte=$currentByte, byteCount=$byteCount, createAt=$createAt, updateAt=$updateAt, id=$id, listener=$listener, progress=$progress, status=$status, statusStr='$statusStr')"
  }

  @Column(ignore = true)
  private val registry = PropertyChangeRegistry()

  override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
    registry.add(callback)
  }

  override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
    registry.remove(callback)
  }
}

/**
 * 下载状态
 */
class DownloadStatus {
  companion object {
    /**
     * 未开始
     */
    const val UNSTART = 0

    /**
     * 准备中。。。
     */
    const val PREPARE = 1

    /**
     * 下载中...
     */
    const val DOWNLOADING = 2

    /**
     * 已暂停
     */
    const val PAUSE = 3

    /**
     * 下载出错
     */
    const val ERROR = 4

    /**
     * 合并中
     */
    const val MERGE = 5

    /**
     * 下载完成
     */
    const val COMPLETED = 6
  }
}