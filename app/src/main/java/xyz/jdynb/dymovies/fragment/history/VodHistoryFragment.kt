package xyz.jdynb.dymovies.fragment.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.grid
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.utils.scope
import com.drake.net.utils.scopeLife
import com.drake.net.utils.withDefault
import org.litepal.LitePal
import org.litepal.extension.find
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.activity.VideoPlayActivity
import xyz.jdynb.dymovies.model.vod.VodDetail

class VodHistoryFragment : Fragment() {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return RecyclerView(requireContext()).also {
      it.layoutParams = ViewGroup.LayoutParams(-1, -1)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val rv = view as RecyclerView
    rv.grid().divider {
      setDivider(10, true)
      orientation = DividerOrientation.GRID
      includeVisible = true
    }.setup {
      addType<VodDetail>(R.layout.item_list_history)

      R.id.item.onClick {
        VideoPlayActivity.play(getModel<VodDetail>().detailId)
      }

      R.id.item.onLongClick {
        scope {
          withDefault {
            getModel<VodDetail>().delete()
          }
          mutable.removeAt(modelPosition)
          notifyItemRemoved(modelPosition)
        }
      }
    }

    scopeLife {
      rv.models = withDefault {
        LitePal.order("updatedAt desc").find<VodDetail>()
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
  }
}