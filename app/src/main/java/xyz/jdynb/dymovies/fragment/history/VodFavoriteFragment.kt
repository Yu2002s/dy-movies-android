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
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.drake.net.utils.scope
import com.drake.net.utils.scopeLife
import com.drake.net.utils.withDefault
import org.litepal.LitePal
import org.litepal.extension.findAll
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.activity.VideoPlayActivity
import xyz.jdynb.dymovies.model.vod.VodDetail
import xyz.jdynb.dymovies.model.vod.VodFavorite

class VodFavoriteFragment: Fragment() {
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return RecyclerView(requireContext()).apply {
      layoutParams = ViewGroup.LayoutParams(-1, -1)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val rv = view as RecyclerView
    rv.grid(3).divider {
      setDivider(10, true)
      orientation = DividerOrientation.GRID
      includeVisible = true
    }.setup {
      addType<VodFavorite>(R.layout.item_grid_favorite)

      R.id.item.onClick {
        VideoPlayActivity.play(getModel<VodFavorite>().detailId)
      }

      R.id.item.onLongClick {
        scope {
          withDefault {
            getModel<VodFavorite>().delete()
          }
          mutable.removeAt(modelPosition)
          notifyItemRemoved(modelPosition)
        }
      }
    }

    scopeLife {
      rv.models = withDefault {
        LitePal.findAll<VodFavorite>()
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
  }
}