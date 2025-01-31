package xyz.jdynb.dymovies.model.vod

import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport

/**
 * 视频代理
 */
data class VideoProxy(
  /**
   * 绑定的详情id
   */
  @Column(nullable = false, unique = false)
  val detailId: Int = 0,
  /**
   * 后端返回的视频播放地址
   */
  @Column(nullable = false, unique = true)
  val url: String = "",
  /**
   * 真实的播放地址
   */
  @Column(nullable = true)
  val realUrl: String? = null,
  @Column(nullable = false)
  val createAt: Long = System.currentTimeMillis()
): LitePalSupport() {
  /**
   * 主键id
   */
  val id = 0L

  fun update() {
    update(id)
  }
}
