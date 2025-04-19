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
import org.litepal.extension.find
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.activity.VideoPlayActivity
import xyz.jdynb.dymovies.activity.VodHistoryActivity
import xyz.jdynb.dymovies.databinding.FragmentHistoryBinding
import xyz.jdynb.dymovies.event.Checkable
import xyz.jdynb.dymovies.model.vod.VodDetail

class VodHistoryFragment : Fragment(), Checkable {

  private var _binding: FragmentHistoryBinding? = null
  private val binding get() = _binding!!

  private val parentActivity get() = (requireActivity() as VodHistoryActivity)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.historyRv.grid().divider {
      setDivider(10, true)
      orientation = DividerOrientation.GRID
      includeVisible = true
    }.setup {
      addType<VodDetail>(R.layout.item_list_history)

      onFastClick(R.id.item, R.id.cb) {
        if (!toggleMode && it == R.id.item) {
          VideoPlayActivity.play(getModel<VodDetail>().detailId)
          return@onFastClick
        }
        val checked = getModel<VodDetail>().isChecked
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
        val model = getModel<VodDetail>(position)
        model.isChecked = checked
      }

      onToggle { position, toggleMode, _ ->
        // 刷新列表显示选择按钮
        val model = getModel<VodDetail>(position)
        model.isVisibleCheck = toggleMode
        changeListEditable(this)
      }
    }

    binding.refresh.setEnableLoadMore(false)
    binding.refresh.onRefresh {
      parentActivity.toggleMode = false
      scope {
        binding.historyRv.models = withDefault {
          LitePal.order("updatedAt desc").find<VodDetail>()
        }.also {
          if (it.isEmpty()) {
            showEmpty()
          }
        }
      }
    }.showLoading()
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
    binding.historyRv.bindingAdapter.toggle(toggleMode)
  }

  override fun refresh() {
    if (_binding == null) {
      return
    }
    binding.refresh.refreshing()
  }

  @Suppress("UNCHECKED_CAST")
  override fun delete() {
    val bindingAdapter = binding.historyRv.bindingAdapter
    val checkedItems = bindingAdapter.checkedPosition
    checkedItems.sortedDescending().forEach { index ->
      val vodDetail = (bindingAdapter.models as List<VodDetail>)[index]
      // 暂时在主线程删除
      vodDetail.delete()
      bindingAdapter.mutable.removeAt(index)
      bindingAdapter.notifyItemRemoved(index)
    }
    parentActivity.toggleMode = false
    toggle(false)
    refresh()
  }

  override fun checkAll(isChecked: Boolean) {
    val bindingAdapter = binding.historyRv.bindingAdapter
    if (!bindingAdapter.toggleMode) {
      bindingAdapter.toggle()
    }
    bindingAdapter.checkedAll(isChecked)
  }

  override fun reverseCheck() {
    binding.historyRv.bindingAdapter.checkedReverse()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}