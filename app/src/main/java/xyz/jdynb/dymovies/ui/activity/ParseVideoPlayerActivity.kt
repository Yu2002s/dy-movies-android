package xyz.jdynb.dymovies.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.drake.net.Get
import com.drake.net.utils.scope
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.base.BaseActivity
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.ActivityParseVideoPlayerBinding
import xyz.jdynb.dymovies.utils.showToast
import xyz.jdynb.dymovies.utils.startActivity

/**
 * 影片解析播放
 */
class ParseVideoPlayerActivity : BaseActivity() {

  private lateinit var binding: ActivityParseVideoPlayerBinding

  companion object {

    private val TAG = ParseVideoPlayerActivity::class.simpleName

    private const val PARAM_ID = "id"

    fun play(id: String) {
      startActivity<ParseVideoPlayerActivity>(PARAM_ID to id)
    }

  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_parse_video_player)

    "暂时不保存历史记录，正在开发...".showToast()

    val id = intent.getStringExtra(PARAM_ID)
    Log.d(TAG, "id: ${intent.getStringExtra(PARAM_ID)}")

    binding.state.onRefresh {
      scope {
        val result = Get<String>(Api.DOUBAN_VIDEO_DETAIL + "/${id}/").await()
        val pattern =
          "\\{play_link: \"https://www\\.douban\\.com/link2/\\?url=(.+)%3F.+\", ep: \"(\\d)+\"\\}".toRegex()
        val matchResultSequence = pattern.findAll(result)
        matchResultSequence.forEach {
          Log.d(TAG, "link: " + it.destructured.component1())
        }
      }
    }.showLoading()
  }

}