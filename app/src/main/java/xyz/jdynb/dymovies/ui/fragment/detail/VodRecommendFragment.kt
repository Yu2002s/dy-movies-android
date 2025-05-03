package xyz.jdynb.dymovies.ui.fragment.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.grid
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.ui.activity.VideoPlayActivity
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.FragmentRecommendBinding
import xyz.jdynb.dymovies.model.page.Page
import xyz.jdynb.dymovies.model.vod.Vod
import xyz.jdynb.dymovies.model.vod.VodVideo
import xyz.jdynb.dymovies.utils.fitNavigationBar

class VodRecommendFragment : Fragment() {

  private var _binding: FragmentRecommendBinding? = null
  private val binding get() = _binding!!

  private var isFirst = true
  private val vodTypeId get() = (requireActivity() as VideoPlayActivity).vodTypeId

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentRecommendBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.recommendRv.grid(3).divider {
      orientation = DividerOrientation.GRID
      setDivider(8, true)
      includeVisible = true
    }.also {
      it.setHasFixedSize(true)
      it.fitNavigationBar()
    }.setup {
      setAnimation(AnimationType.SCALE)
      addType<Vod>(R.layout.item_grid_vod)
      R.id.vod_img.onClick {
        VideoPlayActivity.play(getModel<Vod>().id)
      }
    }

    binding.refresh.onRefresh {
      scope {
        val result = Get<Page<Vod>>(Api.VOD_LIST_BY_TYPE + "/${vodTypeId}") {
          param("page", index)
          param("pid", 0)
        }.await()
        addData(result.data) {
          index < result.lastPage
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    if (vodTypeId == 0) {
      binding.refresh.showError()
      return
    }
    if (isFirst) {
      isFirst = false
      binding.refresh.refreshing()
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}