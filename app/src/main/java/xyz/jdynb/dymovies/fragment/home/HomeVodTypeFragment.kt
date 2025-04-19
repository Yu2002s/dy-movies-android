package xyz.jdynb.dymovies.fragment.home;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.activity.VideoPlayActivity
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.FragmentVodTypeBinding
import xyz.jdynb.dymovies.model.page.Page
import xyz.jdynb.dymovies.model.vod.Vod
import xyz.jdynb.dymovies.model.vod.VodType
import xyz.jdynb.dymovies.utils.getSerializableArguments
import xyz.jdynb.dymovies.utils.setSerializableArguments

class HomeVodTypeFragment : Fragment() {

  private var _binding: FragmentVodTypeBinding? = null

  private val binding get() = _binding!!

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

    binding.filterRv.setup {
      singleMode = true
      addType<VodType>(R.layout.item_filter)
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
        vodType.id = model.id
        vodType.pid = model.pid
        binding.refresh.autoRefresh()
      }
    }.also {
      it.models = vodType.children
      it.setChecked(0, true)
    }

    binding.rv.grid(3).divider {
      orientation = DividerOrientation.GRID
      setDivider(8, true)
      includeVisible = true
    }.also { it.setHasFixedSize(true) }.setup {
      addType<Vod>(R.layout.item_grid_vod)
      R.id.vod_img.onClick {
        VideoPlayActivity.play(getModel<Vod>().id)
      }
    }

    binding.refresh.onRefresh {
      scope {
        val result = Get<Page<Vod>>(Api.VOD_LIST_BY_TYPE + "/${vodType.id}") {
          param("page", index)
          param("pid", vodType.pid)
        }.await()
        addData(result.data) {
          index < result.lastPage
        }
      }
    }.showLoading()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

}
