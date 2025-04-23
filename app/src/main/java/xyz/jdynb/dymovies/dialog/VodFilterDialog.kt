package xyz.jdynb.dymovies.dialog

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
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
import xyz.jdynb.dymovies.event.OnVodFilterChangListener
import xyz.jdynb.dymovies.model.vod.VodFilter
import xyz.jdynb.dymovies.model.vod.VodFilterParams

class VodFilterDialog : BottomSheetDialogFragment() {

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
            if (model.name == "year") {
              vodFilterParams.year = model.value
            } else if (model.name == "sort") {
              vodFilterParams.sort = model.value
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
            it.value == if (model.name == "year") {
              vodFilterParams.year
            } else {
              vodFilterParams.sort
            }
          }
          this.bindingAdapter.setChecked(if (index == -1) 0 else index, true)
        }

      }
    }.models = listOf(
      getYearFilter(),
      VodFilter(
        "sort",
        "排序",
        listOf(
          VodFilter.Item("倒序", "sort", "latest"),
          VodFilter.Item("升序", "sort", "oldest")
        )
      )
    )

    binding.btnClose.setOnClickListener {
      dismiss()
    }

    binding.btnFilter.setOnClickListener {
      dismiss()
      onVodFilterChangListener?.onChanged(vodFilterParams)
    }
  }

  private fun getYearFilter(): VodFilter {
    val childFilters = mutableListOf(VodFilter.Item("全部", "year"))
    val vodFilter = VodFilter("year", "年份", childFilters)
    val nowYear = Calendar.getInstance().get(Calendar.YEAR)
    for (year in nowYear downTo 2020) {
      val childFilter = VodFilter.Item(year.toString(), "year", year.toString())
      childFilters.add(childFilter)
    }
    var year = 2020
    while (year >= 1950) {
      val beforeYear = year
      val beforeYearStr = year.toString().substring(year.toString().length - 2)
      year -= 10
      val yearStr = year.toString().substring(year.toString().length - 2)
      childFilters.add(VodFilter.Item("${yearStr}-${beforeYearStr}年", "year", "$year-$beforeYear"))
    }
    return vodFilter
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

}