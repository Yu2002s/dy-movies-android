package xyz.jdynb.dymovies.model.ui

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import com.drake.brv.BindingAdapter
import com.drake.brv.utils.setup
import kotlinx.serialization.Serializable
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.databinding.ItemActionBinding

fun BindingAdapter.BindingViewHolder.loadActionList(onClick: (model: Action) -> Unit) {
  getBinding<ItemActionBinding>().actionRv.setup {
    addType<Action>(R.layout.item_grid_action)
    R.id.item_action.onClick {
      onClick(getModel())
    }
  }
}

@Serializable
@Keep
data class Action(
  val id: String,
  var name: String,
  @DrawableRes
  var icon: Int
): BaseObservable()