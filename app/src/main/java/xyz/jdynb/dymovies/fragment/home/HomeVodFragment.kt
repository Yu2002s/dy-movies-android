package xyz.jdynb.dymovies.fragment.home;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import com.youth.banner.indicator.CircleIndicator
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.activity.AllTypeActivity
import xyz.jdynb.dymovies.activity.DownloadActivity
import xyz.jdynb.dymovies.activity.VideoPlayActivity
import xyz.jdynb.dymovies.activity.VodHistoryActivity
import xyz.jdynb.dymovies.adapter.HomeBannerAdapter
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.FragmentHomeVodBinding
import xyz.jdynb.dymovies.databinding.ItemActionBinding
import xyz.jdynb.dymovies.databinding.ItemHomeBannerBinding
import xyz.jdynb.dymovies.model.ui.Action
import xyz.jdynb.dymovies.model.ui.loadActionList
import xyz.jdynb.dymovies.model.vod.HomeVod
import xyz.jdynb.dymovies.utils.startActivity

class HomeVodFragment : Fragment() {

  private var _binding: FragmentHomeVodBinding? = null
  private val binding get() = _binding!!

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
      onCreate {
        when (itemViewType) {
          R.layout.item_home_banner -> {
            getBinding<ItemHomeBannerBinding>().banner.setAdapter(HomeBannerAdapter())
              .setIndicator(CircleIndicator(requireContext()))
              .setOnBannerListener { data, _ ->
                data as HomeVod.Banner
                VideoPlayActivity.play(data.id)
              }
          }

          R.layout.item_action -> loadActionList {
            when (it.id) {
              // "allCate" -> startActivity<AllTypeActivity>()
              "download" -> startActivity<DownloadActivity>()
              "history" -> startActivity<VodHistoryActivity>()
            }
          }
        }
      }
      onBind {
        when (itemViewType) {
          R.layout.item_home_banner -> {
            getBinding<ItemHomeBannerBinding>().banner.setDatas(getModel<List<HomeVod.Banner>>())
          }

          R.layout.item_action -> {
            getBinding<ItemActionBinding>().actionRv.models = getModel<List<Action>>()
          }
        }
      }
    }

    binding.homeState.onRefresh {
      scope {
        val result = Get<HomeVod>(Api.VOD_HOME).await()
        binding.homeRv.models = result.getData()
      }
    }.showLoading()

  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
