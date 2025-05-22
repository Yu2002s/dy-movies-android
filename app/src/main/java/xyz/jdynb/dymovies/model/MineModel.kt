package xyz.jdynb.dymovies.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import xyz.jdynb.dymovies.BR
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.model.ui.Action

data class MineModel(
  var actions: List<Action> = listOf(
    Action("download", "下载列表", R.drawable.baseline_arrow_circle_down_24),
    Action("history", "历史收藏", R.drawable.baseline_history_24),
    Action("feedback", "反馈建议", R.drawable.baseline_feedback_24),
    Action("setting", "设置", R.drawable.baseline_settings_24)
  )
) : BaseObservable() {
  @get:Bindable
  var cover: String? = ""
    set(value) {
      field = value
      notifyPropertyChanged(BR.cover)
    }
}
