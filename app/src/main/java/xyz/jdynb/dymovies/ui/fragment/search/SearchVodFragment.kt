package xyz.jdynb.dymovies.ui.fragment.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.setup
import com.drake.net.Post
import com.drake.net.utils.scope
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.FragmentSearchBinding
import xyz.jdynb.dymovies.dialog.VodFilterDialog
import xyz.jdynb.dymovies.event.OnVodFilterChangListener
import xyz.jdynb.dymovies.model.page.Page
import xyz.jdynb.dymovies.model.vod.VodDetail
import xyz.jdynb.dymovies.model.vod.VodFilterParams
import xyz.jdynb.dymovies.model.vod.VodProvider
import xyz.jdynb.dymovies.model.vod.VodType
import xyz.jdynb.dymovies.ui.activity.VideoPlayActivity
import xyz.jdynb.dymovies.utils.getSerializableForKey
import xyz.jdynb.dymovies.utils.json
import xyz.jdynb.dymovies.utils.putSerializable

class SearchVodFragment : SearchFragment(), OnVodFilterChangListener {

  private var _binding: FragmentSearchBinding? = null
  private val binding get() = _binding!!

  private lateinit var vodFilterDialog: VodFilterDialog

  companion object {

    private val TAG = SearchVodFragment::class.java.simpleName

    private const val PARAM_TYPE = "type"
    private const val PARAM_PROVIDERS = "vodProviders"

    fun newInstance(vodType: VodType, vodProviders: List<VodProvider>): SearchVodFragment {
      val args = Bundle()
      args.putSerializable(PARAM_TYPE, vodType)
      args.putSerializable(PARAM_PROVIDERS, vodProviders)
      val fragment = SearchVodFragment()
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

    val vodType = requireArguments().getSerializableForKey<VodType>(PARAM_TYPE) ?: VodType()
    val vodProviders = requireArguments()
      .getSerializableForKey<List<VodProvider>>(PARAM_PROVIDERS) ?: listOf()
    // keyword = requireArguments().getString("keyword") ?: ""
    vodQueryParams.pid = if (vodType.id == 0) null else vodType.id

    ViewCompat.setOnApplyWindowInsetsListener(binding.searchRv) { v, insets ->
      v as RecyclerView
      v.clipToPadding = false
      val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
      v.updatePadding(bottom = bottom)
      (binding.filterFab.layoutParams as MarginLayoutParams).bottomMargin = bottom + 50
      ViewCompat.setOnApplyWindowInsetsListener(v, null)
      insets
    }
    // Log.d(TAG, "onViewCreated: $keyword")

    binding.searchRv.addOnScrollListener(onScrollListener)
    binding.searchRv.divider {
      setDivider(10, true)
      orientation = DividerOrientation.GRID
      includeVisible = true
    }.setup {
      setAnimation(AnimationType.SCALE)
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
        vodQueryParams.flag = model.value
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
      Log.d(TAG, "onRefresh: $vodQueryParams")
      scope {
        val result = Post<Page<VodDetail>>(Api.VOD_SEARCH) {
          vodQueryParams.page = index
          json(vodQueryParams)
        }.await()
        addData(result.data) {
          index < result.lastPage
        }
      }
    }

    binding.filterFab.setOnClickListener {
      if (!::vodFilterDialog.isInitialized) {
        vodFilterDialog = VodFilterDialog(vodType.children)
        vodFilterDialog.onVodFilterChangListener = this
      }
      vodFilterDialog.show(childFragmentManager, null)
    }

    if (keyword.isNotBlank()) {
      search()
    }
  }

  override fun search() {
    super.search()
    Log.d(TAG, "refresh: $keyword, binding: $_binding")
    _binding?.refresh?.showLoading()
  }

  override fun onChanged(vodFilterParams: VodFilterParams) {
    vodQueryParams.sort = vodFilterParams.sort
    vodQueryParams.year = vodFilterParams.year
    vodQueryParams.tid = vodFilterParams.type
    vodQueryParams.area = vodFilterParams.area
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

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}