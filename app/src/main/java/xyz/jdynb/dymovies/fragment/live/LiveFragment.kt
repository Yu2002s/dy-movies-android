package xyz.jdynb.dymovies.fragment.live

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
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.FragmentLiveBinding
import xyz.jdynb.dymovies.model.live.TvLive

class LiveFragment : Fragment() {

  private var _binding: FragmentLiveBinding? = null
  private val binding get() = _binding!!

  /*override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
    menuInflater.inflate(R.menu.menu_search, menu)
    val searchView = menu.getItem(0).actionView as SearchView
    searchView.setOnQueryTextListener(object : OnQueryTextListener {
      override fun onQueryTextChange(newText: String?): Boolean {

        return false
      }

      override fun onQueryTextSubmit(query: String?): Boolean {
        return false
      }
    })
  }

  override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
    return true
  }*/

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentLiveBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // binding.toolbar.addMenuProvider(this, viewLifecycleOwner)

    val tvLives = mutableListOf<TvLive>()

    val vp = binding.liveVp
    val tab = binding.liveTab
    val adapter = LiveViewPagerAdapter(childFragmentManager, lifecycle, tvLives)
    vp.adapter = adapter

    TabLayoutMediator(tab, vp) { t, position ->
      t.text = tvLives[position].name
    }.attach()

    binding.state.onRefresh {
      scope {
        val result = Get<List<TvLive>>(Api.TV_LIVE).await()
        tvLives.addAll(result)
        adapter.notifyItemRangeChanged(0, tvLives.size)
      }
    }.showLoading()
  }

  class LiveViewPagerAdapter(fm: FragmentManager, lifecycle: Lifecycle, private val tvLives: MutableList<TvLive>) :
    FragmentStateAdapter(fm, lifecycle) {
    override fun getItemCount() = tvLives.size

    override fun createFragment(position: Int): Fragment {
      return LiveListFragment.newInstance(tvLives[position].url)
    }

  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}