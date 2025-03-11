package xyz.jdynb.dymovies.fragment.detail

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.danikula.videocache.parser.Playlist
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.reflect.copyType
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.dividerSpace
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.scope.NetCoroutineScope
import com.drake.net.utils.scope
import com.drake.net.utils.withDefault
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.uaoanlao.tv.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.litepal.LitePal
import org.litepal.extension.deleteAll
import org.litepal.extension.findFirst
import xyz.jdynb.dymovies.DyMoviesApplication
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.activity.DownloadActivity
import xyz.jdynb.dymovies.activity.VideoPlayActivity
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.config.RequestConfig
import xyz.jdynb.dymovies.config.SPConfig
import xyz.jdynb.dymovies.databinding.DialogDownloadBinding
import xyz.jdynb.dymovies.databinding.FragmentVodDetailBinding
import xyz.jdynb.dymovies.databinding.ItemListSelectionBinding
import xyz.jdynb.dymovies.dialog.SelectionDialog
import xyz.jdynb.dymovies.event.OnVideoChangeListener
import xyz.jdynb.dymovies.model.ui.Action
import xyz.jdynb.dymovies.model.vod.VideoProxy
import xyz.jdynb.dymovies.model.vod.VodActor
import xyz.jdynb.dymovies.model.vod.VodDetail
import xyz.jdynb.dymovies.model.vod.VodFavorite
import xyz.jdynb.dymovies.model.vod.VodParseUrl
import xyz.jdynb.dymovies.model.vod.VodSource
import xyz.jdynb.dymovies.model.vod.VodVideo
import xyz.jdynb.dymovies.utils.DanmakuUtils
import xyz.jdynb.dymovies.utils.SpUtils.getRequired
import xyz.jdynb.dymovies.utils.fitNavigationBar
import xyz.jdynb.dymovies.utils.startActivity
import xyz.jdynb.dymovies.view.player.DongYuPlayer
import xyz.jdynb.dymovies.view.player.base.BasePlayer
import xyz.jdynb.dymovies.view.player.base.PlayerStateListener

/**
 * 影片详情 fragment
 */
class VodDetailFragment : Fragment(), PlayerStateListener, OnVideoChangeListener {

  private var _binding: FragmentVodDetailBinding? = null
  private val binding get() = _binding!!

  /**
   * 详情信息
   */
  private lateinit var vodDetail: VodDetail

  /**
   * 数据源列表
   */
  private val sourceList = mutableListOf<VodSource>()

  /**
   * 播放器实例
   */
  private val player get() = (requireActivity() as VideoPlayActivity).player

  /**
   * 绑定的下载服务实例
   */
  private val downloadService get() = (requireActivity() as VideoPlayActivity).downloadService

  private var favorite: VodFavorite? = null

  private var job: NetCoroutineScope? = null

  companion object {
    private val TAG = VodDetailFragment::class.simpleName
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_vod_detail, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.detailContent.fitNavigationBar()

    val id = requireArguments().getInt("id")

    // 初始化 VideoPlayer 的一些事件
    initPlayerEvents()
    // 自适应底部导航栏
    // binding.detailRv.fitNavigationBar()

    initViews()
    initViewEvents()

    binding.state.onRefresh {
      job = scope {
        vodDetail =
          withDefault { LitePal.where("detailId = ?", id.toString()).findFirst<VodDetail>() }
            ?: Get<VodDetail>(Api.VOD_DETAIL + id).await().also {
              Log.d(TAG, "detail: $it")
              it.title = it.name
              it.save()
            }

        Log.d(TAG, "returnDetail: $vodDetail")

        val danmakuUrlsResult = async {
          DanmakuUtils.getDanmakuUrls(vodDetail.name, vodDetail.year)
        }

        /**
         * 影视源列表
         */
        val sourcesResult = async {
          Get<List<VodSource>>(Api.VOD_VIDEO) {
            param("name", vodDetail.name)
          }.await()
        }

        /**
         * 收藏信息
         */
        val favoriteResult = async(Dispatchers.Default) {
          LitePal.where("detailId = ?", id.toString()).findFirst<VodFavorite>()
        }

        // 弹幕地址集合列表
        val danmakuUrls = danmakuUrlsResult.await()
        // 影视源列表
        val sources = sourcesResult.await()
        // 收藏信息
        favorite = favoriteResult.await()
        sourceList.addAll(sources)
        // 设置弹幕
        player.setDanmakus(danmakuUrls)
        // 演员列表
        setVideoActors()
        binding.detail = vodDetail
        setSelectionsList()
        setVideoActions()
      }
    }.apply {
      loadingLayout = R.layout.layout_vod_loading
      showLoading()
    }
  }

  /**
   * 初始化视图
   */
  private fun initViews() {
    // 选集 rv
    binding.sectionRv.dividerSpace(10).setup {
      singleMode = true
      addType<VodVideo>(R.layout.item_list_selection)
      onCreate {
        getBinding<ItemListSelectionBinding>().item.layoutParams.width =
          FrameLayout.LayoutParams.WRAP_CONTENT
      }

      onChecked { position, checked, allChecked ->
        val model = getModel<VodVideo>(position)
        model.isChecked = checked
        model.notifyChange()
        if (checked) {
          onVideoChanged(model, position)
        }
      }

      R.id.item.onFastClick {
        val model = getModel<VodVideo>()
        if (model.isChecked) return@onFastClick
        setChecked(layoutPosition, !model.isChecked)
      }
    }

    // 操作 rv
    binding.actionRv.setup {
      addType<Action>(R.layout.item_grid_action)

      R.id.item_action.onClick {
        val model = getModel<Action>()
        when (model.id) {
          "checkout" -> showCheckoutSourceDialog()
          "download" -> showDownloadDialog()
          "screencast" -> player.headerBinding.videoProjection.callOnClick()
          "setting" -> player.headerBinding.videoSetting.callOnClick()
          "favorite" -> handleFavorite(model)
        }
      }
    }

    binding.actorRv.setup {
      addType<VodActor>(R.layout.item_list_actor)
    }
  }

  /**
   * 初始化视图的一些事件监听
   */
  @Suppress("UNCHECKED_CAST")
  private fun initViewEvents() {
    binding.tvAllSelection.setOnClickListener {
      SelectionDialog(
        requireContext(),
        binding.sectionRv.models as List<VodVideo>,
        vodDetail.videoUrl
      ).also {
        it.videoChangeListener = object : OnVideoChangeListener {
          override fun onVideoChanged(vodVideo: VodVideo, position: Int) {
            setSelectionPosition(position)
          }
        }
        it.show()
      }
    }
  }

  /**
   * 设置选集
   */
  private fun setSelectionPosition(position: Int) {
    if (position < 0) {
      return
    }
    binding.sectionRv.bindingAdapter.setChecked(position, true)
  }

  /**
   * 设置选集列表
   */
  private fun setSelectionsList() {
    val currentSource = sourceList.find { it.name == vodDetail.flag } ?: VodSource(
      "默认", 1, listOf(
        VodVideo(url = vodDetail.videoUrl)
      )
    )
    vodDetail.videoCount = currentSource.videos.size
    player.setVideoList(currentSource.videos, vodDetail.videoUrl)
    binding.sectionRv.models = currentSource.videos
    val selectionPosition = currentSource.videos.indexOfFirst { it.url == vodDetail.videoUrl }
    setSelectionPosition(selectionPosition)
  }

  /**
   * 设置视频操作列表
   */
  private fun setVideoActions() {
    binding.actionRv.models = listOf(
      Action("checkout", "换线路", R.drawable.baseline_route_24),
      Action("download", "下载", R.drawable.baseline_arrow_circle_down_24),
      Action("screencast", "投屏", R.drawable.baseline_live_tv_24),
      Action("setting", "设置", R.drawable.baseline_settings_24),
      Action(
        "favorite",
        if (favorite == null) "收藏" else "取消",
        if (favorite == null) R.drawable.baseline_favorite_border_24 else R.drawable.baseline_favorite_24
      )
    )
  }

  /**
   * 设置影片明星列表
   */
  private fun setVideoActors() {
    binding.actorRv.models = vodDetail.actor.split("[,/、，]".toRegex()).map { VodActor(it) }
  }

  /**
   * 视频播放已准备就绪时
   */
  override fun onVideoPrepared(player: BasePlayer?) {
    Log.d(TAG, "onVideoPrepared: $vodDetail")
    // 判断是否可以跳过开头页面
    skipVideoStart()
    super.onVideoPrepared(player)
    if (vodDetail.currentProgress > 0L) {
      player?.seekTo(vodDetail.currentProgress)
    }
    vodDetail.duration = player?.endProgress ?: 0
    scope {
      vodDetail.update()
    }

    val autoFullscreen = SPConfig.PLAYER_AUTO_FULLSCREEN.getRequired(false)

    Log.d(TAG, "autoFullscreen: $autoFullscreen")

    // 自动全屏功能
    if (autoFullscreen) {
      player?.isFullScreen = true
    }
  }

  /**
   * 监听视频的播放进度
   */
  override fun onProgressChanged(currentProgress: Long) {
    // 跳过结束时机
    // skipVideoEnd()
    super.onProgressChanged(currentProgress)
    vodDetail.currentProgress = currentProgress
    scope {
      vodDetail.update()
    }
  }

  /**
   * 视频播放状态改变时
   */
  override fun onPlayStateChanged(newState: Int) {
    when (newState) {
      DongYuPlayer.STATE_COMPLETED -> {
        player.setAutoNextSelection(SPConfig.PLAYER_AUTO_NEXT.getRequired(true))
      }
    }
    super.onPlayStateChanged(newState)
  }

  /**
   * 当播放的视频改变时回调
   * 此回调统一通过 <code> binding.sectionRv.bindingAdapter.setChecked(position, true) 触发 </code>
   * 其他地方回调不直接使用此实现，否则会出现重复调用的情况
   */
  override fun onVideoChanged(vodVideo: VodVideo, position: Int) {
    Log.d(TAG, "videoChangeListener: $position")
    // binding.sectionRv.bindingAdapter.setChecked(position, true)

    // 开始播放视频...
    vodDetail.title = vodDetail.name + " " + vodVideo.name
    vodDetail.updatedAt = System.currentTimeMillis()
    player.apply {
      startDanmaku(position)
      setCurrentVideoUrl(vodVideo.url)
      title = vodDetail.title
      hideMaskView()
      showLoading()
      stop()
    }

    scope {
      val videoUrl = if (!vodVideo.url.endsWith(".m3u8")) {
        Get<VodParseUrl>(Api.VOD_PARSE + "/${vodVideo.videoId}").await().url.also {
          vodVideo.url = it
        }
      } else vodVideo.url
      vodDetail.videoUrl = videoUrl
      vodDetail.update()
      val proxyUrl = getProxyUrl(videoUrl)
      Log.d(TAG, "proxyUrl: $proxyUrl")
      val headers = mapOf(
        "User-Agent" to RequestConfig.USER_AGENT,
        "Accept" to RequestConfig.ACCEPT,
        "Accept-Language" to RequestConfig.ACCEPT_LANGUAGE
      )
      player.setHeaders(headers).play(proxyUrl)
    }.catch {
      // 这里处理播放错误
      player.playError()
      player.showMaskView()
    }
  }

  /**
   * 对 DongYuPlayer 一些必要的事件进行监听
   */
  private fun initPlayerEvents() {
    // 监听播放状态
    player.setPlayerStateListener(this)
    // 监听播放视频改变
    player.setVideoChangeListener(object : OnVideoChangeListener {
      override fun onVideoChanged(vodVideo: VodVideo, position: Int) {
        setSelectionPosition(position)
      }
    })
    // 监听播放器的下载按钮事件
    player.headerBinding.videoDownload.setOnClickListener {
      showDownloadDialog()
    }
    // 监听播放器的投屏按钮事件
    player.headerBinding.videoProjection.setOnClickListener {
      Screen().setStaerActivity(requireActivity() as Activity)
        .setName(player.title.toString())
        .setUrl(vodDetail.videoUrl)
        .setImageUrl(vodDetail.pic)
        .show()
    }
  }

  /**
   * 显示切换线路对话框
   */
  private fun showCheckoutSourceDialog() {
    MaterialAlertDialogBuilder(requireContext()).apply {
      setTitle("选择线路")
      setSingleChoiceItems(
        sourceList.map { it.name }.toTypedArray(),
        sourceList.indexOfFirst { it.name == vodDetail.flag }) { dialog, which ->
        val source = sourceList[which]
        if (source.videos.isEmpty()) {
          player.showToast("当前源下视频为空")
          return@setSingleChoiceItems
        }
        vodDetail.flag = source.name
        val videoCount = source.videos.size
        vodDetail.videoCount = videoCount
        vodDetail.videoCount = source.videos.size
        binding.tvSelections.text = "选集(${source.name})"
        binding.tvAllSelection.text = "${videoCount}集全"
        var checkedPosition = binding.sectionRv.bindingAdapter.checkedPosition[0]
        binding.sectionRv.models = source.videos
        if (checkedPosition > videoCount - 1) {
          checkedPosition = videoCount - 1
        }
        setSelectionPosition(checkedPosition)
        player.setVideoList(source.videos, vodDetail.videoUrl)
        dialog.dismiss()
        player.showToast("正在切换线路:[${source.name}]")
      }.show()
    }
  }

  /**
   * 获取视频的代理地址，用于缓存播放的视频
   */
  private suspend fun getProxyUrl(url: String) = withDefault {
    val detailId = vodDetail.detailId
    val videoUrl = vodDetail.videoUrl
    val videoProxy = LitePal.where("detailId = ? and url = ?", detailId.toString(), videoUrl)
      .findFirst<VideoProxy>()
    DyMoviesApplication.getProxy().getProxyUrl(if (videoProxy == null) {
      val result = Get<String>(url).await()
      val playlist = Playlist.parse(result)
      playlist.elements.firstOrNull()?.let {
        val uri = it.uri.toString()
        if (uri.endsWith(".m3u8")) {
          val baseUrl = url.substring(0, url.lastIndexOf('/') + 1)
          val playUrl = baseUrl + uri
          VideoProxy(detailId, url, playUrl).save()
          return@let playUrl
        }
        null
      } ?: url
    } else {
      if (!videoProxy.realUrl.isNullOrEmpty()) {
        videoProxy.realUrl
      } else {
        videoProxy.url
      }
    })
  }

  /**
   * 显示下载对话框
   */
  private fun showDownloadDialog() {
    val binding = DialogDownloadBinding.inflate(layoutInflater, null, false)
    BottomSheetDialog(requireContext()).apply {
      setContentView(binding.root)
      show()
    }
    binding.btnDownloadList.setOnClickListener {
      startActivity<DownloadActivity>()
    }
    binding.rv.dividerSpace(20, DividerOrientation.GRID).setup {
      addType<VodVideo>(R.layout.item_list_selection)

      R.id.item.onClick {
        val vodVideo = getModel<VodVideo>()
        val name = vodDetail.name + " " + vodVideo.name
        MaterialAlertDialogBuilder(requireContext())
          .setTitle("下载")
          .setMessage("是否下载 $name")
          .setPositiveButton("下载") { _, _ ->
            downloadService.addDownload(vodVideo.url, name, vodDetail.name)
          }
          .setNegativeButton("取消", null)
          .show()
      }
    }.models = sourceList.find { it.name == vodDetail.flag }?.videos
  }

  /**
   * 收藏影片
   */
  private fun handleFavorite(action: Action) {
    scope {
      withDefault {
        Log.d(TAG, "favorite: $favorite")
        if (favorite == null) {
          action.name = "取消"
          action.icon = R.drawable.baseline_favorite_24
          favorite = VodFavorite(vodDetail)
          favorite!!.save()
        } else {
          action.name = "收藏"
          action.icon = R.drawable.baseline_favorite_border_24
          LitePal.deleteAll<VodFavorite>("detailId = ?", vodDetail.detailId.toString())
          favorite = null
        }
      }
      action.notifyChange()
    }
  }

  private fun skipVideoStart() {
    // 跳过片头
    val skipStart = SPConfig.PLAYER_SKIP_START.getRequired(false)
    if (!skipStart) {
      return
    }
    // player.skipVideoStart = SPConfig.PLAYER_SKIP_START_TIME.getRequired<Long>(0L)
  }

  private fun skipVideoEnd() {
    // 跳过片尾
    val skipEnd = SPConfig.PLAYER_SKIP_END.getRequired(false)
    if (!skipEnd) {
      return
    }
    // player.skipVideoEnd = SPConfig.PLAYER_SKIP_END_TIME.getRequired<Long>(0L)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    job?.cancel()
    _binding = null
  }

}