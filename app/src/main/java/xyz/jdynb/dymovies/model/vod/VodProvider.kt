package xyz.jdynb.dymovies.model.vod

import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import kotlinx.serialization.Serializable
import xyz.jdynb.dymovies.BR

@Keep
@Serializable
data class VodProvider(
  val id: Int = 0,
  val name: String = "",
  val remark: String = ""
): BaseObservable() {

  var value = name

  @get:Bindable
  var isChecked = false
    set(value) {
      field = value
      notifyPropertyChanged(BR.checked)
    }
}