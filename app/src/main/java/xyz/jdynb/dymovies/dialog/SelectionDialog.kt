package xyz.jdynb.dymovies.dialog

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.dividerSpace
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.event.OnVideoChangeListener
import xyz.jdynb.dymovies.model.vod.VodVideo

class SelectionDialog(
  context: Context,
  private val videoList: List<VodVideo>,
  private val currentVideo: String
) :
  AdaptiveDialog(context) {

  var videoChangeListener: OnVideoChangeListener? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val recyclerView = RecyclerView(context)
    setContentView(recyclerView)
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
          if (checked) {
            videoChangeListener?.onChanged(model, position)
          }
        }
        R.id.item.onFastClick {
          val model = getModel<VodVideo>()
          if (model.isChecked) return@onFastClick
          setChecked(layoutPosition, !model.isChecked)
        }
      }
    bindingAdapter.models = videoList
    // bindingAdapter.checkedAll(false)
    val position = videoList.indexOfFirst {
      if (it.url == currentVideo) {
        it.isChecked = false
        true
      } else false
    }
    if (position == -1) {
      return
    }
    bindingAdapter.setChecked(position, true)
  }
}