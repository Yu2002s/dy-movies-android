package xyz.jdynb.dymovies.utils

import android.util.Log
import com.drake.net.Get
import kotlinx.coroutines.coroutineScope
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.model.iqiyi.IQiYiVideoInfo
import xyz.jdynb.dymovies.model.iqiyi.IqiyiSearch
import xyz.jdynb.dymovies.utils.converter.SerializationConverter

/**
 * 弹幕相关的的工具方法
 */
object DanmakuUtils {

  private val TAG = DanmakuUtils::class.java.simpleName

  private const val DAN_MA_KU_API = "https://dmku.hls.one/?ac=dm&url="

  /**
   * 获取弹幕库地址
   */
  suspend fun getDanmakuUrls(name: String, year: String? = null) = coroutineScope {
    val result = Get<IqiyiSearch>(Api.IQIYI_SEARCH) {
      converter = SerializationConverter("0")
      param("key", name, true)
      param("current_page", 1)
      param("pageNum", 1)
      param("pageSize", 10)
    }.await()
    Log.d(TAG, "searchName: $name, $year")
    for (template in result.templates) {
      val albumInfo = template.albumInfo
      val matchYear = albumInfo?.year?.value == year
      if (!year.isNullOrEmpty() && !matchYear) {
        continue
      }
      val templateCode = template.template
      return@coroutineScope when (templateCode) {
        101, 102 -> albumInfo!!.videos.map { DAN_MA_KU_API + it.pageUrl }
        103 -> {
          listOf(DAN_MA_KU_API + Get<IQiYiVideoInfo>(Api.IQIYI_VIDEO_INFO) {
            converter = SerializationConverter("A00000")
            setQuery("id", albumInfo!!.qipuId)
            setQuery("locale", "cn_s")
          }.await().vu)
        }
        else -> emptyList()
      }
    }

    return@coroutineScope emptyList()
  }

}