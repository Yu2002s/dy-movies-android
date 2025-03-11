package xyz.jdynb.dymovies.fragment.search

import android.os.Bundle
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
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.Post
import com.drake.net.utils.scope
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.activity.VideoPlayActivity
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.FragmentSearchBinding
import xyz.jdynb.dymovies.fragment.detail.VodDetailFragment
import xyz.jdynb.dymovies.fragment.detail.VodDetailFragment.Companion
import xyz.jdynb.dymovies.model.page.Page
import xyz.jdynb.dymovies.model.vod.VodDetail
import xyz.jdynb.dymovies.utils.fitNavigationBar

class SearchFragment : Fragment() {

  private var _binding: FragmentSearchBinding? = null
  private val binding get() = _binding!!

  private var beforeKeyword = ""

  var keyword = ""
    set(value) {
      if (field != value) {
        arguments?.putString("keyword", value)
        Log.d(TAG, "set keyword: $keyword, $arguments")
        field = value
      }
    }

  companion object {

    private val TAG = SearchFragment::class.java.simpleName

    fun newInstance(type: Int): SearchFragment {
      val args = Bundle()
      args.putInt("type", type)
      val fragment = SearchFragment()
      fragment.arguments = args
      return fragment
    }

  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val type = requireArguments().getInt("type")
    keyword = requireArguments().getString("keyword") ?: ""

    binding.searchRv.fitNavigationBar()

    /*ViewCompat.setOnApplyWindowInsetsListener(binding.searchRv) { v, insets ->
      v as RecyclerView
      v.clipToPadding = false
      v.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom)
      insets
    }*/

    Log.d(TAG, "onViewCreated: $keyword")

    binding.searchRv.divider {
      setDivider(10, true)
      orientation = DividerOrientation.GRID
      includeVisible = true
    }.setup {
      addType<VodDetail>(R.layout.item_list_vod)

      R.id.vod_item.onClick {
        VideoPlayActivity.play(getModel<VodDetail>().detailId)
      }
    }

    binding.refresh.onRefresh {
      Log.d(TAG, "keyword: $keyword")
      scope {
        val result = Post<Page<VodDetail>>(Api.VOD_SEARCH) {
          json("page" to index, "tid" to type, "keyword" to keyword)
        }.await()
        addData(result.data) {
          index < result.lastPage
        }
      }
    }

    if (keyword.isNotEmpty()) {
      binding.refresh.showLoading()
    }
  }

  fun refresh() {
    beforeKeyword = keyword
    Log.d(TAG, "refresh: $keyword, binding: $_binding")
    _binding?.refresh?.showLoading()
  }

  override fun onResume() {
    super.onResume()
    Log.d(TAG, "onResume: $keyword")
    if (keyword.isNotEmpty() && keyword != beforeKeyword) {
      Log.d(TAG, "onRefresh: $keyword")
      refresh()
    }
  }

  override fun onStop() {
    super.onStop()

  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}