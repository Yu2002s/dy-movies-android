package xyz.jdynb.dymovies.ui.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.base.BaseActivity
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.ActivityFeedbackBinding
import xyz.jdynb.dymovies.model.FeedbackModel
import xyz.jdynb.dymovies.model.result.SimpleResult
import xyz.jdynb.dymovies.utils.json
import xyz.jdynb.dymovies.utils.showToast
import xyz.jdynb.dymovies.utils.throttleClick

/**
 * App 反馈
 */
class FeedbackActivity : BaseActivity() {

  private val feedbackModel = FeedbackModel()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val binding: ActivityFeedbackBinding =
      DataBindingUtil.setContentView(this, R.layout.activity_feedback)
    setSupportActionBar(binding.header.toolBar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    binding.m = feedbackModel

    binding.btnSubmit.throttleClick {
      if (!feedbackModel.isValid()) {
        "请输入反馈/建议内容".showToast()
        return@throttleClick
      }
      scopeNetLife {
        val result = Post<SimpleResult>(Api.FEEDBACK) {
          json(feedbackModel)
        }.await()
        result.msg.showToast()
        finish()
      }
    }
  }

}