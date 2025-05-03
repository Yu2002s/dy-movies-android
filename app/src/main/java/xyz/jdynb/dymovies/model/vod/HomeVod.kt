package xyz.jdynb.dymovies.model.vod

import androidx.annotation.Keep
import com.drake.brv.reflect.copyType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.model.ui.Action

@Serializable
@Keep
data class HomeVod(
  val banners: List<Banner> = listOf(),
  val actions: List<Action> = listOf(
    Action("download", "下载列表", R.drawable.baseline_arrow_circle_down_24),
    Action("history", "历史收藏", R.drawable.baseline_history_24),
    Action("feedback", "反馈建议", R.drawable.baseline_feedback_24)
  ),
  val feeds: List<VodFeed> = listOf()
) {

  fun getData(): MutableList<Any> {
    val data = mutableListOf<Any>()
    if (banners.isNotEmpty()) {
      data.add(banners.copyType())
    }
    data.add(actions.copyType())
    if (feeds.isNotEmpty()) {
      data.addAll(feeds)
    }
    return data
  }

  @Serializable
  @Keep
  data class Banner(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("pic")
    val pic: String = "",
    @SerialName("note")
    val note: String,
    // @SerialName("des")
    // val des: String,
  )

  @Serializable
  @Keep
  data class VodFeed(
    @SerialName("title")
    var title: String = "",
    @SerialName("cateId")
    var cateId: Int = 0,
    @SerialName("vodList")
    var vodList: List<Vod> = listOf()
  )
}