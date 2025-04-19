package xyz.jdynb.dymovies.fragment.home;

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.activity.DownloadActivity
import xyz.jdynb.dymovies.activity.VideoPlayActivity
import xyz.jdynb.dymovies.activity.VodHistoryActivity
import xyz.jdynb.dymovies.activity.VodLatestActivity
import xyz.jdynb.dymovies.adapter.HomeBannerAdapter2
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.FragmentHomeVodBinding
import xyz.jdynb.dymovies.databinding.ItemActionBinding
import xyz.jdynb.dymovies.databinding.ItemGridVodBinding
import xyz.jdynb.dymovies.databinding.ItemHomeBannerBinding
import xyz.jdynb.dymovies.databinding.ItemListFeedBinding
import xyz.jdynb.dymovies.model.ui.Action
import xyz.jdynb.dymovies.model.ui.loadActionList
import xyz.jdynb.dymovies.model.vod.HomeVod
import xyz.jdynb.dymovies.model.vod.Vod
import xyz.jdynb.dymovies.utils.dp2px
import xyz.jdynb.dymovies.utils.startActivity
import xyz.jdynb.dymovies.view.pager.ScaleTransformer

class HomeVodFragment : Fragment(), OnClickListener {

  private var _binding: FragmentHomeVodBinding? = null
  private val binding get() = _binding!!

  private val pagerHandler = Handler(Looper.getMainLooper())

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentHomeVodBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.homeRv.divider {
      setDivider(10, true)
      orientation = DividerOrientation.VERTICAL
      includeVisible = true
    }.setup {
      addType<List<HomeVod.Banner>>(R.layout.item_home_banner)
      addType<List<Action>>(R.layout.item_action)
      addType<HomeVod.VodFeed>(R.layout.item_list_feed)
      onCreate {
        when (itemViewType) {
          R.layout.item_home_banner -> {
            getBinding<ItemHomeBannerBinding>().banner.apply {
              offscreenPageLimit = 4
              adapter = HomeBannerAdapter2()
              setCurrentItem(5000)
              pageMargin = -150
              setPageTransformer(false, ScaleTransformer())

              val runnable = object : Runnable {
                override fun run() {
                  setCurrentItem(currentItem + 1)
                  pagerHandler.postDelayed(this, 5000)
                }
              }
              pagerHandler.postDelayed(runnable, 5000)
            }
          }

          R.layout.item_action -> loadActionList {
            when (it.id) {
              "download" -> startActivity<DownloadActivity>()
              "history" -> startActivity<VodHistoryActivity>()
            }
          }

          R.layout.item_list_feed -> {
            getBinding<ItemListFeedBinding>().vodsRv.divider {
              orientation = DividerOrientation.GRID
              setDivider(10, true)
            }.also { it.setHasFixedSize(true) }.setup {
              addType<Vod>(R.layout.item_grid_vod)
              onCreate {
                getBinding<ItemGridVodBinding>().vodImg.layoutParams.height = 165.dp2px()
              }
              R.id.vod_img.onClick {
                VideoPlayActivity.play(getModel<Vod>().id)
              }
            }
          }
        }
      }

      onBind {
        when (itemViewType) {
          R.layout.item_home_banner -> {
            (getBinding<ItemHomeBannerBinding>().banner.adapter as HomeBannerAdapter2).submitData(getModel())
          }

          R.layout.item_action -> {
            getBinding<ItemActionBinding>().actionRv.models = getModel<List<Action>>()
          }

          R.layout.item_list_feed -> {
            getBinding<ItemListFeedBinding>().vodsRv.models = getModel<HomeVod.VodFeed>().vodList
          }
        }
      }

      R.id.tv_vod_latest.onClick {
        startActivity<VodLatestActivity>()
      }
    }

    binding.homeState.onRefresh {
      scope {
        val result = Get<HomeVod>(Api.VOD_HOME).await()
        binding.homeRv.models = result.getData()
      }
    }.showLoading()

  }

  override fun onClick(v: View) {

  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun onDestroy() {
    super.onDestroy()
    pagerHandler.removeCallbacksAndMessages(null)
  }
}
