package xyz.jdynb.dymovies.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import xyz.jdynb.dymovies.base.BaseActivity
import xyz.jdynb.dymovies.config.AppConfig
import xyz.jdynb.dymovies.databinding.ActivityCrashBinding
import xyz.jdynb.dymovies.utils.startActivity

/**
 * App全局闪退处理
 */
class CrashActivity: BaseActivity() {

  companion object {

    private const val PARAM_LOG = "log"

    fun actionStart(log: String) {
      startActivity<CrashActivity>(PARAM_LOG to log)
    }
  }

  private val binding by lazy {
    ActivityCrashBinding.inflate(layoutInflater)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)
    setSupportActionBar(binding.header.toolBar)
    supportActionBar?.title = "抱歉，软件闪退了！"

    intent?.getStringExtra(PARAM_LOG)?.let { log ->
      binding.tvCrashContent.text = log
    }

    binding.restartApp.setOnClickListener {
      finish()
    }

    binding.feedback.setOnClickListener {
      startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.FEEDBACK_URL)))
    }
  }
}