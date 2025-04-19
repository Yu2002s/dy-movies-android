package xyz.jdynb.dymovies.utils.converter

import com.drake.net.convert.NetConverter
import com.drake.net.exception.ConvertException
import okhttp3.Response
import xyz.jdynb.dymovies.model.live.TvGroup
import java.lang.reflect.Type

/**
 * 自定义请求响应转换器
 * 将 M3U 文件转换为 TvGroup 集合
 */
@Suppress("UNCHECKED_CAST")
class M3UConverter: NetConverter {

  companion object {
    private val GROUP_REGEX = "group-title=\"(.+)\"".toRegex()
  }

  override fun <R> onConvert(succeed: Type, response: Response): R {
    val body = response.body ?: throw ConvertException(response, "获取数据异常")
    val bodyContent = body.string()
    val lines = bodyContent.split("\n")
    var name: String? = null
    var beforeGroup: String? = null
    val groups = mutableListOf<TvGroup>()
    var subList = mutableListOf<TvGroup.TvItem>()
    lines.forEach { line ->
      if (line.startsWith("#EXTINF")) {
        val arr = line.split(",")
        val info = arr[0]
        name = arr[1]
        val group = GROUP_REGEX.find(info)?.destructured?.component1() ?: "全部频道"
        if (beforeGroup != group) {
          beforeGroup = group
          subList = mutableListOf()
          groups.add(TvGroup(group, subList))
        }
      } else if (name != null) {
        subList.add(TvGroup.TvItem(name!!, line))
      }
    }
    return groups as R
  }

}