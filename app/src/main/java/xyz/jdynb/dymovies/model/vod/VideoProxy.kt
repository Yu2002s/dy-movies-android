package xyz.jdynb.dymovies.model.vod

import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport

/**
 * 视频代理 （一些视频是多码率的，所以需要配置映射到实际的播放地址，提高加载效率）
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
