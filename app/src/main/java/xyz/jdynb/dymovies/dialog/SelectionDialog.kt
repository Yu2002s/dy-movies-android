package xyz.jdynb.dymovies.dialog

import android.content.Context
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.utilities.DynamicScheme
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.event.OnVideoChangeListener
import xyz.jdynb.dymovies.model.vod.VodVideo
import xyz.jdynb.dymovies.utils.fitNavigationBar

/**
 * 视频选集对话框
 */
class SelectionDialog(
  context: Context,
  // 当前视频列表
  private val videoList: List<VodVideo>,
  // 当前播放的视频地址
  private val currentVideo: String? = null
) :
  AdaptiveDialog(context) {

  /**
   * 监听选择视频改变时触发
   */
  var videoChangeListener: OnVideoChangeListener? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val recyclerView = RecyclerView(context)
    setContentView(recyclerView)
    recyclerView.fitNavigationBar(window!!.decorView)

    val bindingAdapter = recyclerView
      .grid(3)
      .divider {
        setDivider(10, true)
        orientation = DividerOrientation.GRID
        includeVisible = true
      }
      .setup {
        singleMode = true
        addType<VodVideo>(R.layout.item_list_selection)
        onChecked { position, checked, _ ->
          val model = getModel<VodVideo>(position)
          model.isChecked = checked
          model.notifyChange()
        }
        R.id.item.onFastClick {
          val model = getModel<VodVideo>()
          if (model.isChecked) return@onFastClick
          setChecked(layoutPosition, !model.isChecked)
          videoChangeListener?.onVideoChanged(model, layoutPosition)
        }
      }
    bindingAdapter.models = videoList
    if (currentVideo.isNullOrEmpty()) {
      return
    }
    // 获取当前视频的位置
    val position = videoList.indexOfFirst { it.url == currentVideo }
    if (position < 0) {
      return
    }
    bindingAdapter.setChecked(position, true)
  }
}