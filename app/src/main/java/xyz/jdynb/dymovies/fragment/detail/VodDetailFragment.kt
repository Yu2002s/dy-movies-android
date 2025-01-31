package xyz.jdynb.dymovies.fragment.detail

import android.app.Activity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.danikula.videocache.parser.Playlist
import com.drake.brv.BindingAdapter
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.reflect.copyType
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.dividerSpace
import com.drake.brv.utils.grid
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
import xyz.jdynb.dymovies.databinding.ItemActionBinding
import xyz.jdynb.dymovies.databinding.ItemActorBinding
import xyz.jdynb.dymovies.databinding.ItemListSelectionBinding
import xyz.jdynb.dymovies.databinding.ItemVodSelectionsBinding
import xyz.jdynb.dymovies.event.OnVideoChangeListener
import xyz.jdynb.dymovies.model.ui.Action
import xyz.jdynb.dymovies.model.ui.loadActionList
import xyz.jdynb.dymovies.model.vod.VideoProxy
import xyz.jdynb.dymovies.model.vod.VodActor
import xyz.jdynb.dymovies.model.vod.VodDetail
import xyz.jdynb.dymovies.model.vod.VodFavorite
import xyz.jdynb.dymovies.model.vod.VodParseUrl
import xyz.jdynb.dymovies.model.vod.VodSource
import xyz.jdynb.dymovies.model.vod.VodVideo
import xyz.jdynb.dymovies.utils.DanmakuUtils
import xyz.jdynb.dymovies.utils.SpUtils.getRequired
import xyz.jdynb.dymovies.utils.startActivity
import xyz.jdynb.dymovies.view.player.DongYuPlayer
import xyz.jdynb.dymovies.view.player.base.BasePlayer
import xyz.jdynb.dymovies.view.player.base.PlayerStateListener

class VodDetailFragment : Fragment() {

  private var _binding: FragmentVodDetailBinding? = null
  private val binding get() = _binding!!

  private lateinit var vodDetail: VodDetail
  private val modelList = mutableListOf<Any>()
  private val sourceList = mutableListOf<VodSource>()
  private var currentSelectionPosition = -1

  private val player get() = (requireActivity() as VideoPlayActivity).player

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

    val id = requireArguments().getInt("id")

    // 监听播放状态
    player.setPlayerStateListener(object : PlayerStateListener {
      // 视频已准备播放时
      override fun onVideoPrepared(player: BasePlayer?) {
        skipVideoStart()
        super.onVideoPrepared(player)
        Log.d(TAG, "onVideoPrepared: $vodDetail")
        if (vodDetail.currentProgress > 0L) {
          player?.seekTo(vodDetail.currentProgress)
        }
        vodDetail.duration = player?.endProgress ?: 0
        scope {
          vodDetail.update()
        }

        val autoFullscreen = SPConfig.PLAYER_AUTO_FULLSCREEN.getRequired(false)

        Log.d(TAG, "autoFullscreen: $autoFullscreen")

        if (autoFullscreen) {
          player?.isFullScreen = true
        }
      }

      // 视频进度更改时
      override fun onProgressChanged(currentProgress: Long) {
        skipVideoEnd()
        super.onProgressChanged(currentProgress)
        vodDetail.currentProgress = currentProgress
        scope {
          vodDetail.update()
        }
      }

      override fun onPlayStateChanged(newState: Int) {
        when (newState) {
          DongYuPlayer.STATE_COMPLETED -> {
            player.setAutoNextSelection(SPConfig.PLAYER_AUTO_NEXT.getRequired(true))
          }
        }
        super.onPlayStateChanged(newState)
      }
    })

    player.setVideoChangeListener(object : OnVideoChangeListener {
      override fun onChanged(vodVideo: VodVideo, position: Int) {
        Log.d(TAG, "videoChangeListener: $position")
        if (handleVideoChange(vodVideo, position, true)) {
          vodDetail.currentProgress = 0
        }
      }
    })

    player.headerBinding.videoDownload.setOnClickListener {
      showDownloadDialog()
    }

    player.headerBinding.videoProjection.setOnClickListener {
      Screen().setStaerActivity(requireActivity() as Activity)
        .setName(player.title.toString())
        .setUrl(vodDetail.videoUrl)
        .setImageUrl(vodDetail.pic)
        .show()
    }

    binding.detailRv.divider {
      setDivider(10, true)
      orientation = DividerOrientation.VERTICAL
      includeVisible = true
    }.setup {
      addType<VodDetail>(R.layout.item_vod_info)
      addType<List<Action>>(R.layout.item_action)
      addType<VodSource>(R.layout.item_vod_selections)
      addType<String>(R.layout.item_play_tips)
      addType<List<VodActor>>(R.layout.item_actor)

      onCreate {
        when (itemViewType) {
          R.layout.item_action -> loadActionList {
            when (it.id) {
              "checkout" -> showCheckoutSourceDialog()
              "download" -> showDownloadDialog()
              "screencast" -> player.headerBinding.videoProjection.callOnClick()
              "setting" -> player.headerBinding.videoSetting.callOnClick()
              "favorite" -> {
                scope {
                  withDefault {
                    Log.d(TAG, "favorite: $favorite")
                    if (favorite == null) {
                      it.name = "取消收藏"
                      it.icon = R.drawable.baseline_favorite_24
                      favorite = VodFavorite(
                        id,
                        vodDetail.title,
                        vodDetail.pic,
                        vodDetail.duration
                      )
                      favorite!!.save()
                    } else {
                      it.name = "收藏"
                      it.icon = R.drawable.baseline_favorite_border_24
                      LitePal.deleteAll<VodFavorite>("detailId = ?", id.toString())
                      favorite = null
                    }
                  }
                  it.notifyChange()
                }
              }
            }
          }

          R.layout.item_vod_selections -> {
            getBinding<ItemVodSelectionsBinding>().selectionsRv.dividerSpace(
              20,
              DividerOrientation.GRID
            ).setupSelectionRv {
              onCreate {
                getBinding<ItemListSelectionBinding>().item
                  .layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
              }
            }
          }

          R.layout.item_actor -> {
            getBinding<ItemActorBinding>().rv.setup {
              addType<VodActor>(R.layout.item_list_actor)
            }
          }
        }
      }

      onBind {
        when (itemViewType) {
          R.layout.item_action -> getBinding<ItemActionBinding>().actionRv.models = getModel()
          R.layout.item_vod_selections -> {
            val videos = getModel<VodSource>().videos
            val itemBinding = getBinding<ItemVodSelectionsBinding>()
            itemBinding.selectionsRv.models = videos
            val index = videos.indexOfFirst { it.url == vodDetail.videoUrl }
            if (index != -1) {
              itemBinding.selectionsRv.bindingAdapter
                .setChecked(index, true)
            } else {
              itemBinding.selectionsRv.bindingAdapter
                .checkedAll(false)
            }
          }

          R.layout.item_actor -> {
            val actors = getModel<List<VodActor>>()
            val itemBinding = getBinding<ItemActorBinding>()
            itemBinding.rv.models = actors
          }
        }
      }

      R.id.all_selection.onClick {
        val rv = RecyclerView(requireContext())
        rv.grid(4).divider {
          setDivider(10, true)
          orientation = DividerOrientation.GRID
          includeVisible = true
        }.setupSelectionRv()
        rv.layoutParams = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val videos = sourceList.find { it.name == vodDetail.flag }?.videos
        rv.models = videos
        val position = videos?.indexOfFirst {
          it.url == vodDetail.videoUrl
        }
        if (position == null || position == -1) {
          return@onClick
        }
        rv.bindingAdapter.setChecked(position, true)
        BottomSheetDialog(requireContext()).apply {
          setContentView(rv)
          setOnDismissListener {
            getBinding<ItemVodSelectionsBinding>().selectionsRv.bindingAdapter
              .setChecked(rv.bindingAdapter.checkedPosition[0], true)
          }
        }.show()
      }

      R.id.vod_info.onClick {
        MaterialAlertDialogBuilder(requireContext())
          .setTitle("详情")
          .setMessage(Html.fromHtml(vodDetail.des, Html.FROM_HTML_MODE_COMPACT))
          .setPositiveButton("关闭", null)
          .show()
      }
    }

    binding.state.onRefresh {
      job = scope {
        vodDetail =
          withDefault { LitePal.where("detailId = ?", id.toString()).findFirst<VodDetail>() }
            ?: Get<VodDetail>(Api.VOD_DETAIL + id).await().also {
              Log.d(TAG, "detail: $it")
              it.save()
            }

        Log.d(TAG, "returnDetail: $vodDetail")

        val danmakuUrlsResult = async {
          DanmakuUtils.getDanmakuUrls(vodDetail.name, vodDetail.year)
        }

        val sourcesResult = async {
          Get<List<VodSource>>(Api.VOD_VIDEO) {
            param("name", vodDetail.name)
          }.await()
        }

        val favoriteResult = async(Dispatchers.Default) {
          LitePal.where("detailId = ?", id.toString()).findFirst<VodFavorite>()
        }

        val danmakuUrls = danmakuUrlsResult.await()
        val sources = sourcesResult.await()
        favorite = favoriteResult.await()

        sourceList.addAll(sources)

        val currentSource = sources.find { it.name == vodDetail.flag } ?: VodSource(
          "默认", 1, listOf(
            VodVideo(url = vodDetail.videoUrl)
          )
        )
        player.setDanmakus(danmakuUrls)
        player.setVideoList(currentSource.videos, vodDetail.videoUrl)

        val actors = vodDetail.actor.split("[,/、，]".toRegex()).map { VodActor(it) }.copyType()

        modelList.addAll(
          listOf(
            vodDetail,
            listOf(
              Action("checkout", "切换线路", R.drawable.baseline_route_24),
              Action("download", "下载", R.drawable.baseline_arrow_circle_down_24),
              Action("screencast", "投屏", R.drawable.baseline_live_tv_24),
              Action("setting", "设置", R.drawable.baseline_settings_24),
              Action(
                "favorite",
                if (favorite == null) "收藏" else "取消收藏",
                if (favorite == null) R.drawable.baseline_favorite_border_24 else R.drawable.baseline_favorite_24
              )
            ),
            currentSource,
            actors,
            getString(R.string.play_tips)
          )
        )

        binding.detailRv.bindingAdapter.models = modelList
      }
    }.apply {
      loadingLayout = R.layout.layout_vod_loading
      showLoading()
    }

    ViewCompat.setOnApplyWindowInsetsListener(
      binding.detailRv
    ) { v, insets ->
      v as RecyclerView
      v.clipToPadding = false
      v.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom)
      insets
    }
  }

  private fun BindingAdapter.showCheckoutSourceDialog() {
    MaterialAlertDialogBuilder(requireContext()).apply {
      setTitle("选择线路")
      setSingleChoiceItems(
        sourceList.map { it.name }.toTypedArray(),
        sourceList.indexOfFirst { it.name == vodDetail.flag }) { dialog, which ->
        val modelIndex = modelList.indexOfFirst { it is VodSource }
        if (modelIndex == -1) {
          return@setSingleChoiceItems
        }
        var beforeIndex = getModel<VodSource>(modelIndex).videos
          .indexOfFirst {
            return@indexOfFirst if (it.url == vodDetail.videoUrl) {
              it.isChecked = false
              true
            } else false
          }
        if (beforeIndex == -1) {
          beforeIndex = 0
        }
        val source = sourceList[which]
        vodDetail.flag = source.name
        if (source.videos.isNotEmpty()) {
          if (beforeIndex > source.videos.size - 1) {
            beforeIndex = source.videos.size - 1
          }
          vodDetail.videoUrl = source.videos[beforeIndex].url
        }
        currentSelectionPosition = -1
        modelList[modelIndex] = source
        player.setVideoList(source.videos, vodDetail.videoUrl)
        notifyItemChanged(modelIndex)
        dialog.dismiss()
        player.showToast("正在切换线路:[${source.name}]")
      }.show()
    }
  }

  private fun RecyclerView.setupSelectionRv(block: (BindingAdapter.() -> Unit)? = null): BindingAdapter {
    return setup {
      singleMode = true
      addType<VodVideo>(R.layout.item_list_selection)
      onChecked { position, checked, _ ->
        val model = getModel<VodVideo>(position)
        model.isChecked = checked
        model.notifyChange()
        if (checked) {
          Log.d(TAG, "onChecked: $position")
          handleVideoChange(model, position)
        }
      }
      block?.invoke(this)
      R.id.item.onFastClick {
        val model = getModel<VodVideo>()
        if (model.isChecked) return@onFastClick
        setChecked(layoutPosition, !model.isChecked)
        vodDetail.currentProgress = 0
      }
    }
  }

  private fun handleVideoChange(
    vodVideo: VodVideo,
    position: Int,
    refresh: Boolean = false
  ): Boolean {
    if (currentSelectionPosition == position) {
      return false
    }
    player.startDanmaku(position)
    currentSelectionPosition = position
    vodDetail.title = vodDetail.name + " " + vodVideo.name
    vodDetail.updatedAt = System.currentTimeMillis()
    player.setCurrentVideoUrl(vodVideo.url)
    player.title = vodDetail.title
    player.hideMaskView()
    player.showLoading()

    if (refresh) {
      val index = modelList.indexOfFirst { it is VodSource }
      if (index != -1) {
        binding.detailRv.bindingAdapter.notifyItemChanged(index)
      }
    }
    player.stop()

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
      player.playError()
      player.showMaskView()
    }.finally {
      // player.hideLoading()
    }
    return true
  }

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

  private fun showDownloadDialog() {
    val binding = DialogDownloadBinding.inflate(layoutInflater, null, false)
    BottomSheetDialog(requireContext()).apply {
      setContentView(binding.root)
      show()
    }
    val rv = binding.rv
    binding.btnDownloadList.setOnClickListener {
      startActivity<DownloadActivity>()
    }
    rv.dividerSpace(20, DividerOrientation.GRID).setup {
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

  private fun skipVideoStart() {
    // 跳过片头
    val skipStart = SPConfig.PLAYER_SKIP_START.getRequired(false)
    if (!skipStart) {
      return
    }
    player.skipVideoStart = SPConfig.PLAYER_SKIP_START_TIME.getRequired(0L)
  }

  private fun skipVideoEnd() {
    // 跳过片尾
    val skipEnd = SPConfig.PLAYER_SKIP_END.getRequired(false)
    if (!skipEnd) {
      return
    }
    player.skipVideoEnd = SPConfig.PLAYER_SKIP_END_TIME.getRequired(0L)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    job?.cancel()
    _binding = null
  }

}