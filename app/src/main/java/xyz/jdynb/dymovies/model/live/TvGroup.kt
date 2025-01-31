package xyz.jdynb.dymovies.model.live

import androidx.databinding.BaseObservable
import com.drake.brv.item.ItemExpand
import com.drake.brv.item.ItemHover
import com.drake.brv.item.ItemPosition
import xyz.jdynb.dymovies.R

data class TvGroup(
  val title: String = "", private var subList: List<TvItem> = listOf(),
) : ItemExpand, ItemHover, ItemPosition, BaseObservable() {

  override var itemExpand: Boolean = false
    set(value) {
      field = value
      notifyChange()
    }

  override var itemGroupPosition: Int = 0

  override var itemPosition: Int = 0

  override var itemHover: Boolean = true

  override fun getItemSublist(): List<Any?> {
    return subList
  }

  val expandIcon get() =  if (itemExpand) R.drawable.baseline_keyboard_arrow_up_24
  else R.drawable.baseline_keyboard_arrow_down_24

  data class TvItem(
    val name: String,
    val url: String
  )
}