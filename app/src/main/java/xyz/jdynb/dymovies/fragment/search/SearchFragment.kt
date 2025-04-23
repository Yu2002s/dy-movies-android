package xyz.jdynb.dymovies.fragment.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.setup
import com.drake.net.Post
import com.drake.net.utils.scope
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.activity.VideoPlayActivity
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.FragmentSearchBinding
import xyz.jdynb.dymovies.dialog.VodFilterDialog
import xyz.jdynb.dymovies.event.OnVodFilterChangListener
import xyz.jdynb.dymovies.model.page.Page
import xyz.jdynb.dymovies.model.vod.VodDetail
import xyz.jdynb.dymovies.model.vod.VodFilterParams
import xyz.jdynb.dymovies.model.vod.VodProvider
import xyz.jdynb.dymovies.utils.fitNavigationBar
import xyz.jdynb.dymovies.utils.getSerializableForKey
import xyz.jdynb.dymovies.utils.putSerializable

class SearchFragment : Fragment(), OnVodFilterChangListener {

  private var _binding: FragmentSearchBinding? = null
  private val binding get() = _binding!!

  private lateinit var vodFilterDialog: VodFilterDialog

  private var beforeKeyword = ""

  var keyword = ""
    set(value) {
      if (field != value) {
        arguments?.putString("keyword", value)
        Log.d(TAG, "set keyword: $keyword, $arguments")
        field = value
      }
    }

  private var currentProvider = ""

  private var currentYear = ""

  private var currentSort = ""

  companion object {

    private val TAG = SearchFragment::class.java.simpleName

    private const val PARAM_TYPE_ID = "tid"
    private const val PARAM_PROVIDERS = "vodProviders"

    fun newInstance(tid: Int, vodProviders: List<VodProvider>): SearchFragment {
      val args = Bundle()
      args.putInt(PARAM_TYPE_ID, tid)
      args.putSerializable(PARAM_PROVIDERS, vodProviders)
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

    val tid = requireArguments().getInt(PARAM_TYPE_ID)
    val vodProviders = requireArguments()
      .getSerializableForKey<List<VodProvider>>(PARAM_PROVIDERS) ?: listOf()
    // keyword = requireArguments().getString("keyword") ?: ""

    binding.searchRv.fitNavigationBar()

    // Log.d(TAG, "onViewCreated: $keyword")

    binding.searchRv.addOnScrollListener(onScrollListener)
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

    binding.sourceRv.setup {
      singleMode = true
      addType<VodProvider>(R.layout.item_list_provider)
      onChecked { position, checked, _ ->
        val model = getModel<VodProvider>(position)
        model.isChecked = checked
        currentProvider = model.value
      }
      R.id.tag.onClick {
        val model = getModel<VodProvider>()
        if (model.isChecked) {
          return@onClick
        }
        setChecked(layoutPosition, true)
        binding.refresh.autoRefresh()
      }
    }.also {
      it.models = vodProviders
      val position = if (vodProviders.size > 1) 1 else 0
      it.setChecked(position, true)
    }

    binding.refresh.onRefresh {
      Log.d(TAG, "keyword: $keyword")
      scope {
        val result = Post<Page<VodDetail>>(Api.VOD_SEARCH) {
          json(
            "page" to index,
            "tid" to tid,
            // "pid" to type.pid,
            "keyword" to keyword,
            "flag" to currentProvider,
            "year" to currentYear,
            "sort" to currentSort
          )
        }.await()
        addData(result.data) {
          index < result.lastPage
        }
      }
    }

    Log.d(TAG, "onViewCreated: $keyword")
    if (keyword.isNotEmpty()) {
      binding.refresh.showLoading()
    }

    binding.filterFab.setOnClickListener {
      if (!::vodFilterDialog.isInitialized) {
        vodFilterDialog = VodFilterDialog()
        vodFilterDialog.onVodFilterChangListener = this
      }
      vodFilterDialog.show(childFragmentManager, null)
    }
  }

  override fun onChanged(vodFilterParams: VodFilterParams) {
    currentSort = vodFilterParams.sort
    currentYear = vodFilterParams.year
    binding.refresh.autoRefresh()
  }

  private val onScrollListener = object : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
      if (dy > 0) {
        binding.filterFab.shrink()
      } else {
        binding.filterFab.extend()
      }
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

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}