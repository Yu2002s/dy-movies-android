package xyz.jdynb.dymovies.ui.fragment.detail

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.danikula.videocache.parser.Playlist
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.dividerSpace
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.brv.utils.staggered
import com.drake.net.Get
import com.drake.net.scope.NetCoroutineScope
import com.drake.net.utils.scope
import com.drake.net.utils.scopeNetLife
import com.drake.net.utils.withDefault
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.uaoanlao.tv.Screen
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.litepal.LitePal
import org.litepal.extension.deleteAll
import org.litepal.extension.findFirst
import xyz.jdynb.dymovies.DyMoviesApplication
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.config.RequestConfig
import xyz.jdynb.dymovies.config.SPConfig
import xyz.jdynb.dymovies.databinding.DialogDownloadBinding
import xyz.jdynb.dymovies.databinding.FragmentVodDetailBinding
import xyz.jdynb.dymovies.databinding.ItemListSelectionBinding
import xyz.jdynb.dymovies.dialog.SelectionDialog
import xyz.jdynb.dymovies.event.OnVideoChangeListener
import xyz.jdynb.dymovies.event.OnVideoSkipChangeListener
import xyz.jdynb.dymovies.model.ui.Action
import xyz.jdynb.dymovies.model.vod.VideoProxy
import xyz.jdynb.dymovies.model.vod.VodActor
import xyz.jdynb.dymovies.model.vod.VodDetail
import xyz.jdynb.dymovies.model.vod.VodFavorite
import xyz.jdynb.dymovies.model.vod.VodParseUrl
import xyz.jdynb.dymovies.model.vod.VodProvider
import xyz.jdynb.dymovies.model.vod.VodSourceVideo
import xyz.jdynb.dymovies.model.vod.VodVideo
import xyz.jdynb.dymovies.ui.activity.DownloadActivity
import xyz.jdynb.dymovies.ui.activity.SettingActivity
import xyz.jdynb.dymovies.ui.activity.VideoPlayActivity
import xyz.jdynb.dymovies.utils.DanmakuUtils
import xyz.jdynb.dymovies.utils.SpUtils.getRequired
import xyz.jdynb.dymovies.utils.fitNavigationBar
import xyz.jdynb.dymovies.utils.showToast
import xyz.jdynb.dymovies.utils.startActivity
import xyz.jdynb.dymovies.view.player.DongYuPlayer
import xyz.jdynb.dymovies.view.player.base.BasePlayer
import xyz.jdynb.dymovies.view.player.base.PlayerStateListener
import java.net.URL

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
   * 源列表
   */
  private val vodSources = mutableListOf<VodProvider>()

  /**
   * 影片视频列表
   */
  private val vodVideos = mutableListOf<VodVideo>()

  private var currentSelectedFlag = ""

  /**
   * 已保存的视频坐标（用于换源时切换视频）
   */
  private var savedPosition = -1

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
    private const val IDM_PACKAGE_NAME = "idm.internet.download.manager.plus"
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

    init()
    // 初始化 VideoPlayer 的一些事件
    initPlayerEvents()

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

        setVideoActions()
        setSkipTimes()
        // 演员列表
        setVideoActors()
        binding.detail = vodDetail
        // 将当前的影片类型 id 传递给父 activity
        (requireActivity() as VideoPlayActivity).vodTypeId = vodDetail.tid

        Log.d(TAG, "returnDetail: $vodDetail")

        /**
         * 影视源列表
         */
        val sourceVideoResult = async {
          Get<VodSourceVideo>(Api.VOD_VIDEO_SOURCE) {
            addQuery("vid", vodDetail.vid)
            addQuery("flag", vodDetail.flag)
          }.await()
        }

        /**
         * 收藏信息
         */
        val favoriteResult = async {
          LitePal.where("detailId = ?", id.toString()).findFirst<VodFavorite>()
        }

        val danmakuUrls = async {
          try {
            DanmakuUtils.getDanmakuUrls(vodDetail.name, vodDetail.year)
          } catch (ignored: Exception) {
            emptyList()
          }
        }

        // 影视源列表
        val sourceVideo = sourceVideoResult.await()
        // 收藏信息
        favorite = favoriteResult.await()
        player.setDanmakus(danmakuUrls.await())
        // 设置采集源和视频数据
        setSourcesAndVideos(sourceVideo)
        setSelectionsList()
      }
    }.apply {
      loadingLayout = R.layout.layout_vod_loading
      showLoading()
    }

    binding.tipsCard.setOnClickListener {
      it.isVisible = false
    }
  }

  private fun init() {
    /*val filterAd = SPConfig.AD_FILTER.getRequired<Boolean>(true)
    M3U8ProxyCache.adFilter = filterAd
    if (filterAd) {
      player.showToast("广告过滤已开启，无法播放请设置中关闭")
    }*/
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

      onChecked { position, checked, _ ->
        val model = getModel<VodVideo>(position)
        model.isChecked = checked
        model.notifyChange()
        if (checked) {
          if (vodDetail.flag != model.flag) {
            player.setVideoList(vodVideos, vodDetail.videoUrl)
          }
          vodDetail.flag = model.flag
        }
      }

      R.id.item.onFastClick {
        val model = getModel<VodVideo>()
        if (model.isChecked) return@onFastClick
        onVideoChanged(model, layoutPosition)
      }
    }

    // 操作 rv
    binding.actionRv.setup {
      addType<Action>(R.layout.item_grid_action)

      R.id.item_action.onClick {
        val model = getModel<Action>()
        when (model.id) {
          // "checkout" -> showCheckoutSourceDialog()
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

    // 全部选集区域
    binding.tvAllSelection.setOnClickListener {
      val videos = binding.sectionRv.models as List<VodVideo>
      if (videos.isEmpty()) {
        "当前视频列表为空，请切换".showToast()
        return@setOnClickListener
      }
      SelectionDialog(requireContext(), videos, vodDetail.videoUrl).also {
        // 全部选集对话框选中事件
        it.videoChangeListener = this
        it.show()
      }
    }

    binding.cardInfo.setOnClickListener {
      MaterialAlertDialogBuilder(requireContext())
        .setTitle(vodDetail.name)
        .setMessage(HtmlCompat.fromHtml(vodDetail.des, HtmlCompat.FROM_HTML_MODE_LEGACY))
        .setPositiveButton("关闭", null)
        .show()
    }

    binding.videoTab.addOnTabSelectedListener(object : OnTabSelectedListener {
      @SuppressLint("NotifyDataSetChanged")
      override fun onTabSelected(tab: TabLayout.Tab) {
        val currentSource = vodSources[tab.position].name
        if (currentSelectedFlag == currentSource) {
          return
        }
        currentSelectedFlag = currentSource
        val isCurrentSource = currentSource == vodDetail.flag
        // 选中事件
        Log.i(TAG, "onTabSelected: ${tab.position}")
        vodDetail.loadingVideos = true
        // 加载视频信息
        scopeNetLife {
          val result = Get<List<VodVideo>>(Api.VOD_VIDEO_LIST) {
            addQuery("name", vodDetail.name)
            addQuery("flag", currentSource)
          }.await()
          vodVideos.clear()
          vodVideos.addAll(result)
          vodDetail.videoCount = vodVideos.size
          val bindingAdapter = binding.sectionRv.bindingAdapter
          binding.sectionRv.models = result
          if (isCurrentSource && vodSources.isNotEmpty()) {
            val index = vodVideos.indexOfFirst { it.url == vodDetail.videoUrl }
            if (index != -1) {
              bindingAdapter.setChecked(index, true)
            } else if (savedPosition in result.indices) {
              bindingAdapter.setChecked(savedPosition, true)
              val currentVideo = vodVideos[savedPosition]
              vodDetail.videoUrl = currentVideo.url
              onVideoChanged(currentVideo, savedPosition)
            }
            savedPosition = -1
          }
        }.finally {
          vodDetail.loadingVideos = false
        }
      }

      override fun onTabUnselected(tab: TabLayout.Tab?) {
      }

      override fun onTabReselected(tab: TabLayout.Tab?) {
      }
    })
  }

  /**
   * 设置选集列表
   */
  private fun setSelectionsList() {
    if (vodVideos.isEmpty()) {
      Log.d(TAG, "vodVideos is empty.")
      return
    }
    vodDetail.videoCount = vodVideos.size
    val selectionPosition = if (vodDetail.videoUrl.isNullOrEmpty()) {
      vodDetail.videoUrl = vodVideos[0].url
      0
    } else {
      vodVideos.indexOfFirst { it.url == vodDetail.videoUrl }
    }
    binding.sectionRv.staggered(if (vodVideos.size > 3) 2 else 1, RecyclerView.HORIZONTAL)
    binding.sectionRv.models = vodVideos
    player.setVideoList(vodVideos, vodDetail.videoUrl)
    Log.d(TAG, "selectionPosition: $selectionPosition")
    if (selectionPosition != -1) {
      onVideoChanged(vodVideos[selectionPosition], selectionPosition)
    }
  }

  /**
   * 设置视频操作列表
   */
  private fun setVideoActions() {
    binding.actionRv.models = listOf(
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
   * 设置跳过片头和跳过片尾相关信息
   */
  private fun setSkipTimes() {
    player.skipVideoStart = vodDetail.skipStart
    player.skipVideoEnd = vodDetail.skipEnd
  }

  private fun setSourcesAndVideos(sourceVideo: VodSourceVideo) {
    vodSources.addAll(sourceVideo.sources)
    vodVideos.addAll(sourceVideo.videos)
    val videoTab = binding.videoTab
    videoTab.apply {
      // var currentIndex = 0
      vodSources.forEachIndexed { index, it ->
        val tab = newTab()
        tab.text = it.name
        if (it.name == vodDetail.flag) {
          //currentIndex = index
          currentSelectedFlag = it.name
        }
        addTab(tab, vodDetail.flag == it.name)
      }
      // getTabAt(currentIndex)?.select()
    }
  }

  /**
   * 视频播放已准备就绪时
   */
  override fun onVideoPrepared(player: BasePlayer?) {
    Log.d(TAG, "onVideoPrepared: $vodDetail")
    // 读取已保存的进度信息
    // 为了保证跳过片头有用，需要写在 super 的前面
    if (vodDetail.currentProgress > 0L) {
      // 如果有数据，就跳转到指定位置
      player?.seekTo(vodDetail.currentProgress)
    }
    super.onVideoPrepared(player)

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
    Log.d(TAG, "onVideoChanged: $position")
    // 保持同步切换
    if (currentSelectedFlag == vodVideo.flag) {
      binding.sectionRv.bindingAdapter.setChecked(position, true)
    }
    vodDetail.vid = vodVideo.vid
    // 只有切换选集的时候才进行重置进度为0
    if (vodDetail.videoUrl != vodVideo.url) {
      player.setProgress(0)
      vodDetail.currentProgress = 0
    }
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

    scopeNetLife {
      val videoUrl = if (!vodVideo.url.endsWith(".m3u8")) {
        Get<VodParseUrl>(Api.VOD_PARSE + "/${vodVideo.videoId}") {
          addQuery("flag", vodDetail.flag)
        }.await().url.also {
          vodVideo.url = it
        }
      } else vodVideo.url
      vodDetail.videoUrl = videoUrl
      vodDetail.update()
      val proxyUrl = getProxyUrl(videoUrl)
      Log.d(TAG, "proxyUrl: $proxyUrl")
      player.setHeaders(RequestConfig.VIDEO_HEADERS).play(proxyUrl)
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
    player.setVideoChangeListener(this)
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

    // 播放器跳过片头片尾
    player.setVideoSkipChangeListener(object : OnVideoSkipChangeListener {
      override fun onSkipEndChanged(skipEnd: Int) {
        if (vodDetail.skipEnd != skipEnd) {
          vodDetail.skipEnd = skipEnd
        }
      }

      override fun onSkipStartChanged(skipStart: Int) {
        if (vodDetail.skipStart != skipStart) {
          vodDetail.skipStart = skipStart
        }
      }
    })

    // 点击换源
    player.setSwitchSourceClickListener {
      val tab = binding.videoTab
      if (tab.tabCount == 0) {
        "当前播放源为空".showToast()
        return@setSwitchSourceClickListener
      }
      var selectedPosition = tab.selectedTabPosition + 1
      if (selectedPosition >= tab.tabCount) {
        selectedPosition = 0
      }
      // 保存当前播放视频的位置，用于换源时指定新的视频地址
      savedPosition = binding.sectionRv.bindingAdapter.checkedPosition.getOrElse(0) { -1 }
      vodDetail.flag = vodSources[selectedPosition].name
      tab.getTabAt(selectedPosition)?.select()
      player.showToast("已切换到线路[${currentSelectedFlag}]")
    }
  }

  /**
   * 获取视频的代理地址，用于缓存播放的视频
   */
  private suspend fun getProxyUrl(url: String): String {
    val detailId = vodDetail.detailId
    val videoUrl = vodDetail.videoUrl
    val videoProxy = LitePal.where("detailId = ? and url = ?", detailId.toString(), videoUrl)
      .findFirst<VideoProxy>()

    val playUrl = if (videoProxy == null) {
      getVideoUrl(url, detailId)
    } else {
      if (!videoProxy.realUrl.isNullOrEmpty()) {
        videoProxy.realUrl
      } else {
        videoProxy.url
      }
    }

    Log.d(TAG, "playUrl: $playUrl")

    val enableCache = SPConfig.VIDEO_CACHE.getRequired(true)

    if (enableCache) {
      return DyMoviesApplication.getProxy().getProxyUrl(playUrl)
    }
    return playUrl
  }

  /**
   * 获取视频的实际播放地址
   */
  private suspend fun getVideoUrl(url: String, detailId: Int): String {
    val result = coroutineScope { Get<String>(url).await() }
    val playlist = Playlist.parse(result)
    Log.d(TAG, "m3u8Elements: ${playlist.elements}")
    if (playlist.elements.size > 10) {
      return url
    }
    return playlist.elements.firstOrNull()?.let {
      val uri = it.uri.toString()
      if (uri.endsWith(".m3u8")) {
        Log.d(TAG, "realUri: $uri")
        val baseUrl = if (uri.startsWith("/")) {
          val u = URL(url)
          u.protocol + "://" + u.host
        } else {
          url.substring(0, url.lastIndexOf('/') + 1)
        }
        val playUrl = baseUrl + uri
        Log.d(TAG, "playUrl: $playUrl")
        VideoProxy(detailId, url, playUrl).save()
        return@let playUrl
      }
      null
    } ?: url
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
          .setMessage("是否下载 $name\n\n注意：内置下载可能存在问题，建议使用IDM+加速下载(plus版本)")
          .setPositiveButton("下载") { _, _ ->
            downloadService.addDownload(vodVideo.url, name, vodDetail.name, vodDetail.pic)
          }
          .setNeutralButton("IDM下载") { _, _ ->
            // openIDM
            idmDownload(name, vodVideo.url)
          }
          .setNegativeButton("取消", null)
          .show()
      }
    }.models = vodVideos
  }

  /**
   * 使用 idm 下载
   */
  private fun idmDownload(name: String, url: String) {
    try {
      val intent = Intent(Intent.ACTION_VIEW, url.toUri())
      intent.putExtra("filename", "$name.mp4")
      intent.`package` = SPConfig.IDM_PACKAGE_NAME.getRequired<String>(IDM_PACKAGE_NAME)
      startActivity(intent)
    } catch (e: ActivityNotFoundException) {
      Log.e(TAG, "idm not found!")
      MaterialAlertDialogBuilder(requireContext())
        .setTitle("提示")
        .setMessage("在你的手机上没有找到IDM+ App，请先下载安装并打开IDM进行初始化。如已安装请前往设置确认包名是否正确")
        .setNeutralButton("去设置") { _, _ ->
          startActivity<SettingActivity>()
        }
        .setPositiveButton("去下载") { _, _ ->
          startActivity(
            Intent(
              Intent.ACTION_VIEW,
              "https://cn.bing.com/search?q=IDM+%20android%E4%B8%8B%E8%BD%BD".toUri()
            )
          )
          "注意识别国内奸商的盗版App".showToast()
        }
        .show()
    }
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

  override fun onDestroyView() {
    super.onDestroyView()
    job?.cancel()
    _binding = null
  }

}