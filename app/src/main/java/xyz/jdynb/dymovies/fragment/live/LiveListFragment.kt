package xyz.jdynb.dymovies.fragment.live

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.drake.brv.layoutmanager.HoverGridLayoutManager
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.activity.SimpleVideoActivity
import xyz.jdynb.dymovies.databinding.FragmentLiveListBinding
import xyz.jdynb.dymovies.model.live.TvGroup
import xyz.jdynb.dymovies.utils.converter.M3UConverter

class LiveListFragment : Fragment() {

  companion object {
    fun newInstance(url: String): LiveListFragment {
      val args = Bundle()
      args.putString("url", url)
      val fragment = LiveListFragment()
      fragment.arguments = args
      return fragment
    }
  }

  private var _binding: FragmentLiveListBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentLiveListBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val gridLayoutManager = HoverGridLayoutManager(requireContext(), 3)
    gridLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        if (position < 0) {
          return 1
        }
        return when (binding.liveRv.bindingAdapter.getItemViewType(position)) {
          R.layout.item_grid_tv -> 1
          else -> 3
        }
      }
    }

    binding.liveRv.layoutManager = gridLayoutManager
    binding.liveRv.setup {
        addType<TvGroup>(R.layout.item_tv_group)
        addType<TvGroup.TvItem>(R.layout.item_grid_tv)
        R.id.item.onFastClick {
          expandOrCollapse()
        }
        R.id.tv.onClick {
          val tvItem = getModel<TvGroup.TvItem>()
          SimpleVideoActivity.actionStart(tvItem.url, tvItem.name)
        }
      }

    binding.liveState.onRefresh {
      scope {
        val result = Get<List<TvGroup>>(requireArguments().getString("url")!!) {
          converter = M3UConverter()
        }.await()
        binding.liveRv.models = result
      }
    }.showLoading()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}