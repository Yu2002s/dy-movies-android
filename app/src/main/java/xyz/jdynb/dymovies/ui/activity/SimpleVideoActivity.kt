package xyz.jdynb.dymovies.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import com.drake.net.utils.scope
import com.uaoanlao.tv.Screen
import xyz.jdynb.dymovies.base.BaseActivity
import xyz.jdynb.dymovies.databinding.ActivityLiveVideoBinding
import xyz.jdynb.dymovies.utils.DanmakuUtils
import xyz.jdynb.dymovies.utils.startActivity

class SimpleVideoActivity : BaseActivity() {

  companion object {

    private const val PARAM_URL = "url"

    private const val PARAM_TITLE = "title"

    private const val PARAM_LIVE = "is_live"

    fun actionStart(url: String, title: String = "", isLive: Boolean = false) {
      startActivity<SimpleVideoActivity>(
        PARAM_URL to url,
        PARAM_TITLE to title,
        PARAM_LIVE to isLive
      )
    }
  }

  private lateinit var binding: ActivityLiveVideoBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val url = intent.getStringExtra(PARAM_URL)
    val title = intent.getStringExtra(PARAM_TITLE)
    val isLive = intent.getBooleanExtra(PARAM_LIVE, false)

    binding = ActivityLiveVideoBinding.inflate(layoutInflater)
    setContentView(binding.root)

    url?.let {
      binding.player.apply {
        setTitle(title)
        play(it)
        post {
          isPortrait = true
        }
        bottomBinding.apply {
          portrait.isVisible = false
          playNext.isVisible = false
        }
        headerBinding.videoDownload.isVisible = false
        headerBinding.videoProjection.setOnClickListener {
          Screen().setStaerActivity(this@SimpleVideoActivity)
            .setName(title)
            .setUrl(url)
            .show()
        }

        if (isLive) {
          return@apply
        }
        title?.let {
          scope {
            val result = DanmakuUtils.getDanmakuUrls(it)
            setDanmakus(result)
              .startDanmaku()
          }
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    binding.player.resume()
  }

  override fun onStop() {
    super.onStop()
    binding.player.pause()
  }

  override fun onDestroy() {
    super.onDestroy()
    binding.player.stop()
  }
}