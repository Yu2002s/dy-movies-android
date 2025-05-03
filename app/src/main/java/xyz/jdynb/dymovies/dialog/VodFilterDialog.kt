package xyz.jdynb.dymovies.dialog

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.databinding.DialogVodFilterBinding
import xyz.jdynb.dymovies.databinding.ItemFilterBinding
import xyz.jdynb.dymovies.constants.VodFilterName
import xyz.jdynb.dymovies.event.OnVodFilterChangListener
import xyz.jdynb.dymovies.model.vod.VodFilter
import xyz.jdynb.dymovies.model.vod.VodFilterParams
import xyz.jdynb.dymovies.model.vod.VodType

/**
 * 影片过滤对话框
 */
class VodFilterDialog(private val vodTypes: List<VodType> = emptyList()) :
  BottomSheetDialogFragment() {

  private var _binding: DialogVodFilterBinding? = null
  private val binding get() = _binding!!

  private val vodFilterParams = VodFilterParams()

  var onVodFilterChangListener: OnVodFilterChangListener? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = DataBindingUtil.inflate(inflater, R.layout.dialog_vod_filter, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.filterRv.setup {
      addType<VodFilter>(R.layout.item_filter)

      R.id.expand_icon.onClick {
        val model = getModel<VodFilter>()
        model.itemExpand = !model.itemExpand
      }

      onCreate {
        getBinding<ItemFilterBinding>().rv.divider {
          setDivider(10, true)
          orientation = DividerOrientation.GRID
        }.setup {
          singleMode = true
          addType<VodFilter.Item>(R.layout.item_list_filter)

          onChecked { position, checked, _ ->
            val model = getModel<VodFilter.Item>(position)
            model.isChecked = checked
          }

          R.id.chip.onClick {
            val model = getModel<VodFilter.Item>()
            if (model.isChecked) {
              return@onClick
            }
            model.value.let {
              when (model.name) {
                VodFilterName.TYPE -> vodFilterParams.type = it as? Int
                VodFilterName.YEAR -> vodFilterParams.year = it as? String
                VodFilterName.SORT -> vodFilterParams.sort = it as Int
                VodFilterName.AREA -> vodFilterParams.area = it as? String
              }
            }
            setChecked(layoutPosition, !model.isChecked)
          }
        }
      }

      onBind {
        getBinding<ItemFilterBinding>().rv.apply {
          val model = getModel<VodFilter>()
          models = model.filters
          val index = model.filters.indexOfFirst {
            it.value == when (model.name) {
              VodFilterName.TYPE -> vodFilterParams.type
              VodFilterName.YEAR -> vodFilterParams.year
              VodFilterName.SORT -> vodFilterParams.sort
              VodFilterName.AREA -> vodFilterParams.area
              else -> null
            }
          }
          this.bindingAdapter.setChecked(if (index == -1) 0 else index, true)
        }
      }
    }.models = listOf(
      getTypeFilter(),
      getYearFilter(),
      getAreaFilter(),
      getSortFilter(),
    )

    binding.btnClose.setOnClickListener {
      dismiss()
    }

    binding.btnFilter.setOnClickListener {
      dismiss()
      onVodFilterChangListener?.onChanged(vodFilterParams)
    }
  }

  private fun getTypeFilter(): VodFilter {
    return VodFilter(VodFilterName.TYPE, "类型", vodTypes.toMutableList().also {
      it.add(0, VodType(name = "全部"))
    }.map {
      VodFilter.Item(it.name, VodFilterName.TYPE, it.id)
    })
  }

  private fun getYearFilter(): VodFilter {
    val childFilters = mutableListOf(VodFilter.Item("全部", VodFilterName.YEAR))
    val vodFilter = VodFilter(VodFilterName.YEAR, "年份", childFilters)
    val nowYear = Calendar.getInstance().get(Calendar.YEAR)
    for (year in nowYear downTo 2020) {
      val childFilter = VodFilter.Item(year.toString(), VodFilterName.YEAR, year.toString())
      childFilters.add(childFilter)
    }
    var year = 2020
    while (year >= 1950) {
      val beforeYear = year
      val beforeYearStr = year.toString().substring(year.toString().length - 2)
      year -= 10
      val yearStr = year.toString().substring(year.toString().length - 2)
      childFilters.add(
        VodFilter.Item(
          "${yearStr}-${beforeYearStr}年",
          VodFilterName.YEAR,
          "$year-$beforeYear"
        )
      )
    }
    return vodFilter
  }

  private fun getSortFilter(): VodFilter {
    val childFilters = listOf(
      VodFilter.Item("降序", VodFilterName.SORT, 1),
      VodFilter.Item("升序", VodFilterName.SORT, 2),
    )
    return VodFilter(VodFilterName.SORT, "排序", childFilters)
  }

  /*private fun getLanguageFilter(): VodFilter {
    val childFilters = arrayOf("中文", "粤语", "英语", "日语", "韩语")
    return VodFilter(VodFilterName.LANG, "语言", )
  }*/

  private fun getAreaFilter(): VodFilter {
    val childFilters = arrayOf(null, "中国", "大陆", "香港", "台湾", "日本", "韩国", "泰国", "美国", "英国", "法国").map {
      VodFilter.Item(it ?: "全部", VodFilterName.AREA, it)
    }
    return VodFilter(VodFilterName.AREA, "地区", childFilters)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

}