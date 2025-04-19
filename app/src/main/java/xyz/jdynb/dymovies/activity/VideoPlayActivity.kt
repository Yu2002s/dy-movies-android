package xyz.jdynb.dymovies.activity

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.uaoanlao.tv.Screen
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.databinding.ActivityVideoPlayBinding
import xyz.jdynb.dymovies.download.DownloadService
import xyz.jdynb.dymovies.fragment.detail.VodCommentFragment
import xyz.jdynb.dymovies.fragment.detail.VodDetailFragment
import xyz.jdynb.dymovies.fragment.detail.VodRecommendFragment
import xyz.jdynb.dymovies.utils.startActivity

class VideoPlayActivity : AppCompatActivity(), ServiceConnection {

  companion object {

    private const val PARAM_ID = "id"

    private val TAG = VideoPlayActivity::class.java.simpleName

    fun play(id: Int) {
      startActivity<VideoPlayActivity>(PARAM_ID to id)
    }
  }

  private lateinit var binding: ActivityVideoPlayBinding

  val player get() = binding.player

  /**
   * 影片详情的具体类型
   */
  var vodTypeId = 0

  private var _downloadService: DownloadService? = null
  val downloadService get() = _downloadService!!

  override fun onServiceDisconnected(name: ComponentName?) {
    _downloadService = null
  }

  override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
    _downloadService = (service as DownloadService.DownloadBinder).getService()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    fitDanmaku()

    WindowCompat.setDecorFitsSystemWindows(window, false)
    bindService(Intent(this, DownloadService::class.java), this, BIND_AUTO_CREATE)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_video_play)

    val id = intent.getIntExtra(PARAM_ID, 0)

    player.showLoading()

    val vp = binding.detailVp
    val tab = binding.detailTab
    vp.isUserInputEnabled = false
    vp.adapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle) {
      override fun getItemCount() = 3
      override fun createFragment(position: Int): Fragment {
        return when (position) {
          0 -> VodDetailFragment::class
          1 -> VodCommentFragment::class
          2 -> VodRecommendFragment::class
          else -> throw IllegalStateException()
        }.run {
          java.getDeclaredConstructor().newInstance().also {
              it.arguments = bundleOf("id" to id)
          }
        }
      }
    }

    TabLayoutMediator(tab, vp) { t, position ->
      t.text = when (position) {
        0 -> "视频"
        1 -> "评论"
        2 -> "相关推荐"
        else -> null
      }
    }.attach()
  }

  /**
   * 解决弹幕滚动重现重复的问题
   */
  private fun fitDanmaku() {
    // 获取系统window支持的模式
    val modes = window.windowManager.defaultDisplay.supportedModes
    // 对获取的模式，基于刷新率的大小进行排序，从小到大排序
    modes.sortBy {
      it.refreshRate
    }

    window.let {
      val lp = it.attributes
      // 取出最小的那一个刷新率，直接设置给window
      lp.preferredDisplayModeId = modes.first().modeId
      it.attributes = lp
    }
  }

  override fun onResume() {
    super.onResume()
    player.resume()
  }

  override fun onPause() {
    super.onPause()
    player.pause()
  }

  override fun onDestroy() {
    super.onDestroy()
    player.stop()
    unbindService(this)
  }
}