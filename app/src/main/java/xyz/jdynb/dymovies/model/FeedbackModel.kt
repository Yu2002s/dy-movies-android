package xyz.jdynb.dymovies.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import kotlinx.serialization.Serializable
import xyz.jdynb.dymovies.BR

/**
 * App 反馈
 */
@Serializable
class FeedbackModel : BaseObservable() {

  /**
   * 反馈内容
   */
  @get:Bindable
  var content: String = ""
    set(value) {
      field = value
      notifyPropertyChanged(BR.content)
    }

  /**
   * 联系方式
   */
  @get:Bindable
  var contact: String = ""
    set(value) {
      field = value
      notifyPropertyChanged(BR.contact)
    }

  /**
   * 是否通过提交验证
   */
  @Bindable("content")
  fun isValid(): Boolean {
    return content.isNotEmpty()
  }
}
