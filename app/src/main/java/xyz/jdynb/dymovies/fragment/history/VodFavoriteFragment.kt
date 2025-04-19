package xyz.jdynb.dymovies.fragment.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.drake.brv.BindingAdapter
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.utils.scope
import com.drake.net.utils.withDefault
import org.litepal.LitePal
import org.litepal.extension.findAll
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.activity.VideoPlayActivity
import xyz.jdynb.dymovies.activity.VodHistoryActivity
import xyz.jdynb.dymovies.databinding.FragmentFavoriteBinding
import xyz.jdynb.dymovies.event.Checkable
import xyz.jdynb.dymovies.model.vod.VodFavorite

class VodFavoriteFragment : Fragment(), Checkable {

  private var _binding: FragmentFavoriteBinding? = null
  private val binding get() = _binding!!

  private val parentActivity get() = (requireActivity() as VodHistoryActivity)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favorite, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.favoriteRv.grid(3).divider {
      setDivider(10, true)
      orientation = DividerOrientation.GRID
      includeVisible = true
    }.setup {
      addType<VodFavorite>(R.layout.item_grid_favorite)

      onFastClick(R.id.item, R.id.cb) {
        if (!toggleMode && it == R.id.item) {
          VideoPlayActivity.play(getModel<VodFavorite>().detailId)
          return@onFastClick
        }
        val checked = getModel<VodFavorite>().isChecked.get()
        /*if (it == R.id.item) checked = !checked*/
        setChecked(layoutPosition, !checked)
      }

      R.id.item.onLongClick {
        if (!toggleMode) {
          toggle()
          setChecked(layoutPosition, true)
        }
      }

      onChecked { position, checked, _ ->
        val model = getModel<VodFavorite>(position)
        model.isChecked.set(checked)
      }

      onToggle { position, toggleMode, _ ->
        // 刷新列表显示选择按钮
        val model = getModel<VodFavorite>(position)
        model.isVisibleCheck.set(toggleMode)
        changeListEditable(this)
      }
    }

    binding.refresh.onRefresh {
      parentActivity.toggleMode = false
      scope {
        binding.favoriteRv.models = withDefault {
          LitePal.findAll<VodFavorite>()
        }.also {
          if (it.isEmpty()) {
            showEmpty()
          }
        }
      }
    }.also {
      it.setEnableLoadMore(false)
      it.showLoading()
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  /** 改变编辑状态 */
  private fun changeListEditable(adapter: BindingAdapter) {
    val toggleMode = adapter.toggleMode
    // val checkedCount = adapter.checkedCount

    if (toggleMode) {
      parentActivity.totalCount = adapter.modelCount
    }

    parentActivity.toggleMode = toggleMode

    // 如果取消管理模式则取消全部已选择
    if (!toggleMode) adapter.checkedAll(false)
  }

  override fun toggle(toggleMode: Boolean) {
    binding.favoriteRv.bindingAdapter.toggle(toggleMode)
  }

  override fun refresh() {
    binding.refresh.refreshing()
  }

  @Suppress("UNCHECKED_CAST")
  override fun delete() {
    val bindingAdapter = binding.favoriteRv.bindingAdapter
    val checkedItems = bindingAdapter.checkedPosition
    checkedItems.sortedDescending().forEach { index ->
      val vodFavorite = (bindingAdapter.models as List<VodFavorite>)[index]
      // 暂时在主线程删除
      vodFavorite.delete()
      bindingAdapter.mutable.removeAt(index)
      bindingAdapter.notifyItemRemoved(index)
    }
    parentActivity.toggleMode = false
    toggle(false)
    refresh()
  }

  override fun checkAll(isChecked: Boolean) {
    val bindingAdapter = binding.favoriteRv.bindingAdapter
    if (!bindingAdapter.toggleMode) {
      bindingAdapter.toggle()
    }
    bindingAdapter.checkedAll(isChecked)
  }

  override fun reverseCheck() {
    binding.favoriteRv.bindingAdapter.checkedReverse()
  }
}