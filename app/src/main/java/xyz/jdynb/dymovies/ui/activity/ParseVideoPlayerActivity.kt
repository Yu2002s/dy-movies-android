package xyz.jdynb.dymovies.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.Post
import com.drake.net.utils.scope
import com.drake.net.utils.scopeNetLife
import org.json.JSONObject
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.base.BaseActivity
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.ActivityParseVideoPlayerBinding
import xyz.jdynb.dymovies.event.OnVideoChangeListener
import xyz.jdynb.dymovies.model.vod.ParseVodDetail
import xyz.jdynb.dymovies.model.vod.VodVideo
import xyz.jdynb.dymovies.utils.AESUtils
import xyz.jdynb.dymovies.utils.IpUtil
import xyz.jdynb.dymovies.utils.Md5Utils
import xyz.jdynb.dymovies.utils.gridDivider
import xyz.jdynb.dymovies.utils.json
import xyz.jdynb.dymovies.utils.startActivity
import xyz.jdynb.dymovies.view.player.base.BasePlayer
import xyz.jdynb.dymovies.view.player.base.PlayerStateListener
import java.net.URLDecoder
import java.nio.charset.Charset

/**
 * 影片解析播放
 */
class ParseVideoPlayerActivity : BaseActivity(), PlayerStateListener, OnVideoChangeListener {

  private lateinit var binding: ActivityParseVideoPlayerBinding

  private var _parseVodDetail: ParseVodDetail? = null
  private val parseVodDetail get() = _parseVodDetail!!

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

    val id = intent.getStringExtra(PARAM_ID)
    Log.d(TAG, "id: ${intent.getStringExtra(PARAM_ID)}")

    binding.state.onRefresh {
      scope {
        // vodDetail = LitePal.where("")

        val result = Get<String>(Api.DOUBAN_VIDEO_DETAIL + "/${id}/").await()

        // _parseVodDetail = LitePal.where("id = ?", id).findFirst<ParseVodDetail>()

        val detailPattern = Regex(
          "<script\\s+type=\"application/ld\\+json\"[^>]*>(.*?)</script>",
          RegexOption.DOT_MATCHES_ALL
        )

        detailPattern.find(result)?.let {
          val jsonBody = it.destructured.component1()
          _parseVodDetail = json.decodeFromString<ParseVodDetail>(jsonBody)
          // parseVodDetail.saveOrUpdate("url = ?", "/subject/${id}/")
          Log.d(TAG, "saveDetail: $parseVodDetail")
        }

        binding.m = parseVodDetail

        val pattern =
          "\\{play_link: \"https://www\\.douban\\.com/link2/\\?url=(.+)%3F.+\", ep: \"(\\d)+\"\\}".toRegex()
        val matchResultSequence = pattern.findAll(result)
        val videos = matchResultSequence.map {
          val playUrl = URLDecoder.decode(it.destructured.component1(), "UTF-8")
          val ep = it.destructured.component2()
          VodVideo(name = "第${ep}集", url = playUrl)
        }.toList()
        binding.videoRv.models = videos
        if (videos.isNotEmpty()) {
          binding.videoRv.bindingAdapter.setChecked(0, true)
          binding.player.setVideoList(videos, videos[0].url)
          binding.player.setDanmakus(videos.map { "https://dmku.hls.one/?ac=dm&url=$it" })
        }
      }.catch {
        Log.e(TAG, "error: ${it.message}")
      }
    }.showLoading()

    binding.videoRv.gridDivider(10).setup {
      singleMode = true
      addType<VodVideo>(R.layout.item_list_selection)

      onChecked { position, checked, _ ->
        val model = getModel<VodVideo>(position)
        model.isChecked = checked
        model.notifyChange()
        onVideoChanged(model, position)
      }

      R.id.item.onClick {
        val model = getModel<VodVideo>()
        if (model.isChecked) {
          return@onClick
        }
        setChecked(layoutPosition, true)
      }
    }

    binding.player.setPlayerStateListener(this)
    binding.player.setVideoChangeListener(this)
  }

  override fun onVideoPrepared(player: BasePlayer?) {
    super.onVideoPrepared(player)
  }

  override fun onVideoChanged(vodVideo: VodVideo, position: Int) {
    if (vodVideo.url == binding.player.currentVideoUrl) {
      return
    }

    binding.player.startDanmaku(position)

    // 请求获取播放的真实地址
    scopeNetLife {
      binding.player.showLoading()
      val time = (System.currentTimeMillis() / 1000).toString()
      Log.d(TAG, "time: $time")

      val mdHash = Md5Utils.md5Hex(time + vodVideo.url)
      val iv =
        String("https://t.me/xmflv666".toByteArray().sliceArray(0..15), Charset.defaultCharset())
      Log.d(TAG, "iv: $iv")
      val key = AESUtils.encryptCBCNoPadding(mdHash, "UTF-8", Md5Utils.md5Hex(mdHash), iv)

      Log.d(TAG, "key: $key")
      val ip = IpUtil.getRandomChinaIP()
      val result = Post<String>("https://59.153.166.174:4433/xmflv.js") {
        param("wap", 0)
        param("url", vodVideo.url)
        param("time", time)
        param("area", "CT|ZheJiang_HangZhou-${ip}")
        param("key", key)
        addHeader(
          "User-Agent",
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36 Edg/136.0.0.0"
        )
        addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
        addHeader("Origin", "https://jx.xmflv.com")
        addHeader("X-Forwarded-For", ip)
        addHeader("HTTP_X_FORWARDED_FOR", ip)
        addHeader("REMOTE_ADDR", ip)
        addHeader("X-Remote-IP", ip)
      }.await()

      Log.d(TAG, "result: $result")
      val jsonObject = JSONObject(result)
      val url = jsonObject.getString("url")
      val aesKey = jsonObject.getString("aes_key")
      val aesIv = jsonObject.getString("aes_iv")
      val playUrl = AESUtils.decrypt("AES/CBC/Pkcs5Padding", aesKey, aesIv, url)
      Log.d(TAG, "playUrl: $playUrl")
      binding.player.play(playUrl)
    }.finally {
      binding.player.hideLoading()
    }.catch {
      binding.player.playError()
      binding.player.showMaskView()
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