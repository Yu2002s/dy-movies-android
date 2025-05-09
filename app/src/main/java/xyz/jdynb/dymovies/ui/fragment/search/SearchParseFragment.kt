package xyz.jdynb.dymovies.ui.fragment.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.FragmentSearchParseBinding
import xyz.jdynb.dymovies.model.search.DoubanSearch
import xyz.jdynb.dymovies.model.search.DoubanSubjectItem
import xyz.jdynb.dymovies.ui.activity.ParseVideoPlayerActivity

class SearchParseFragment : SearchFragment() {

  private var _binding: FragmentSearchParseBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentSearchParseBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.refresh.onRefresh {
      scope {
        val result = Get<DoubanSearch>(Api.DOUBAN_SEARCH) {
          addHeader("Referer", "https://www.douban.com/search")
          addQuery("q", keyword)
          addQuery("start", (index - 1) * 20)
          addQuery("count", 20)
          addQuery("sort", "relevance")
        }.await()
        addData(result.subjects.items.filter { it.target.hasLinewatch })
      }
    }.showLoading()

    binding.searchRv.divider {
      startVisible = true
      endVisible = true
      includeVisible = true
      orientation = DividerOrientation.GRID
      setDivider(10, true)
    }.setup {
      addType<DoubanSubjectItem>(R.layout.item_list_search)

      R.id.search_item.onClick {
        ParseVideoPlayerActivity.play(getModel<DoubanSubjectItem>().targetId)
      }
    }
  }

  override fun search() {
    super.search()
    _binding?.refresh?.showLoading()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

}