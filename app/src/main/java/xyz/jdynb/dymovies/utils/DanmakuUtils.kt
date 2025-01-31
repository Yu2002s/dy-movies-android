package xyz.jdynb.dymovies.utils

import android.util.Log
import com.drake.net.Get
import com.drake.net.utils.scope
import com.drake.net.utils.withDefault
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.model.search.IqiyiSearch
import xyz.jdynb.dymovies.utils.converter.SerializationConverter

/**
 * 弹幕相关的的工具方法
 */
object DanmakuUtils {

  private const val DAN_MA_KU_API = "https://dmku.hls.one/?ac=dm&url="

  /**
   * 获取弹幕库地址
   */
  suspend fun getDanmakuUrls(name: String, year: String? = null) = withDefault {
    val result = Get<IqiyiSearch>(Api.IQIYI_SEARCH) {
      converter = SerializationConverter("0")
      param("key", name, true)
      param("current_page", 1)
      param("pageNum", 1)
      param("pageSize", 10)
    }.await()
    Log.d("jdy", "searchName: $name, $year; response: $result")
    for (template in result.templates) {
      val matchYear = template.albumInfo?.year?.value == year
      if (!year.isNullOrEmpty() && !matchYear) {
        continue
      }
      if (template.template == 101 || template.template == 102) {
        return@withDefault template.albumInfo!!.videos.map { DAN_MA_KU_API + it.pageUrl }
      } else if (template.template == 103) {
        // todo
      }
    }

    return@withDefault emptyList()
  }

}