package xyz.jdynb.dymovies.ui.fragment.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.drake.net.Post
import com.drake.net.utils.scope
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.ui.activity.VideoPlayActivity
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.FragmentVodTypeBinding
import xyz.jdynb.dymovies.dialog.VodFilterDialog
import xyz.jdynb.dymovies.event.OnVodFilterChangListener
import xyz.jdynb.dymovies.model.page.Page
import xyz.jdynb.dymovies.model.vod.Vod
import xyz.jdynb.dymovies.model.vod.VodFilterParams
import xyz.jdynb.dymovies.model.vod.VodQueryParams
import xyz.jdynb.dymovies.model.vod.VodType
import xyz.jdynb.dymovies.utils.getSerializableArguments
import xyz.jdynb.dymovies.utils.json
import xyz.jdynb.dymovies.utils.setSerializableArguments

class HomeVodTypeFragment : Fragment(), OnVodFilterChangListener {

  private var _binding: FragmentVodTypeBinding? = null

  private val binding get() = _binding!!

  private val vodQueryParams = VodQueryParams()

  private lateinit var vodFilterDialog: VodFilterDialog

  companion object {

    fun newInstance(vodType: VodType): HomeVodTypeFragment {
      val fragment = HomeVodTypeFragment()
      fragment.setSerializableArguments("vodType", vodType)
      return fragment
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentVodTypeBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val vodType = getSerializableArguments<VodType>("vodType")
      ?: throw IllegalArgumentException()
    vodQueryParams.pid = vodType.id

    /*binding.filterRv.setup {
      singleMode = true
      addType<VodType>(R.layout.item_type_filter)
      onChecked { position, checked, _ ->
        val model = getModel<VodType>(position)
        model.isChecked = checked
      }
      R.id.chip.onClick {
        val model = getModel<VodType>()
        if (model.isChecked) {
          return@onClick
        }
        setChecked(layoutPosition, true)
        vodQueryParams.tid = model.id
        binding.refresh.autoRefresh()
      }
    }.also {
      it.models = vodType.children
      if (!it.models.isNullOrEmpty()) {
        it.setChecked(0, true)
      }
    }
*/
    binding.rv.grid(3).divider {
      orientation = DividerOrientation.GRID
      setDivider(8, true)
      includeVisible = true
    }.also {
      it.setHasFixedSize(true)
      it.addOnScrollListener(onScrollListener)
    }.setup {
      setAnimation(AnimationType.SCALE)
      addType<Vod>(R.layout.item_grid_vod)
      R.id.vod_img.onClick {
        VideoPlayActivity.play(getModel<Vod>().id)
      }
    }

    binding.refresh.onRefresh {
      scope {
        val result = Post<Page<Vod>>(Api.VOD) {
          vodQueryParams.page = index
          json(vodQueryParams)
        }.await()
        addData(result.data) {
          index < result.lastPage
        }
      }
    }.showLoading()

    binding.filterFab.setOnClickListener {
      if (!::vodFilterDialog.isInitialized) {
        vodFilterDialog = VodFilterDialog(vodType.children)
        vodFilterDialog.onVodFilterChangListener = this
      }
      vodFilterDialog.show(childFragmentManager, null)
    }
  }

  private val onScrollListener = object : OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
      if (dy > 0) {
        binding.filterFab.shrink()
      } else {
        binding.filterFab.extend()
      }
    }
  }

  override fun onChanged(vodFilterParams: VodFilterParams) {
    vodQueryParams.sort = vodFilterParams.sort
    vodQueryParams.tid = vodFilterParams.type
    vodQueryParams.year = vodFilterParams.year
    vodQueryParams.area = vodFilterParams.area
    binding.refresh.autoRefresh()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

}
