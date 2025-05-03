package xyz.jdynb.dymovies.model.vod

import androidx.annotation.DrawableRes
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import xyz.jdynb.dymovies.BR
import xyz.jdynb.dymovies.R

data class VodFilter(
  val name: String,
  val title: String,
  val filters: List<Item>
) : BaseObservable() {

  @get:Bindable
  var itemExpand = true
    set(value) {
      field = value
      notifyPropertyChanged(BR.itemExpand)
    }

  @get:DrawableRes
  @get:Bindable("itemExpand")
  val expandIcon: Int
    get() = if (itemExpand) R.drawable.baseline_keyboard_arrow_up_24
    else R.drawable.baseline_keyboard_arrow_down_24

  data class Item(
    val label: String,
    val name: String = "",
    // range 2020-2025
    val value: Any? = null,
  ) : BaseObservable() {

    @get:Bindable
    var isChecked = false
      set(value) {
        field = value
        notifyPropertyChanged(BR.checked)
      }
  }

}