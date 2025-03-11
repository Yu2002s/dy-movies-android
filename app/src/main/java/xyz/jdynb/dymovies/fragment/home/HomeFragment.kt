package xyz.jdynb.dymovies.fragment.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.drake.net.Get
import com.drake.net.utils.scope
import com.google.android.material.tabs.TabLayoutMediator
import org.litepal.LitePal
import org.litepal.extension.deleteAll
import xyz.jdynb.dymovies.activity.SearchActivity
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.FragmentHomeBinding
import xyz.jdynb.dymovies.model.vod.VideoProxy
import xyz.jdynb.dymovies.model.vod.VodType
import xyz.jdynb.dymovies.utils.startActivity

class HomeFragment : Fragment() {

  companion object {
    private const val TAG = "HomeFragment"
  }

  private var _binding: FragmentHomeBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentHomeBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val tab = binding.homeTab
    val vp = binding.homeVp

    val vodTypes = mutableListOf(VodType(name = "首页"))
    val adapter = HomeViewPagerAdapter(childFragmentManager, lifecycle, vodTypes)
    vp.adapter = adapter

    binding.state.onRefresh {
      scope {
        val time = 100 * 60 * 60 * 24 * 30L
        LitePal.deleteAll<VideoProxy>(
          "createAt < ?",
          (System.currentTimeMillis() - time).toString()
        )
        vodTypes.addAll(Get<List<VodType>>(Api.VOD_TYPE_ALL).await())
        adapter.notifyItemRangeInserted(1, vodTypes.size)
      }
    }.showLoading()

    TabLayoutMediator(
      tab, vp
    ) { t, position -> t.text = vodTypes[position].name }.attach()

    binding.toolbar.setOnClickListener {
      startActivity<SearchActivity>()
      requireActivity().overridePendingTransition(0, 0)
    }
  }

  class HomeViewPagerAdapter(
    fm: FragmentManager,
    lifecycle: Lifecycle,
    private val vodTypes: List<VodType>
  ) :
    FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount() = vodTypes.size

    override fun createFragment(position: Int): Fragment {
      val vodCate = vodTypes[position]
      if (vodCate.id == 0) {
        return HomeVodFragment()
      }
      return HomeVodTypeFragment.newInstance(vodCate)
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}