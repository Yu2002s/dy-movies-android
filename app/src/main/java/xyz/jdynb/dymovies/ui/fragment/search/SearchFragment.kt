package xyz.jdynb.dymovies.ui.fragment.search

import xyz.jdynb.dymovies.base.BaseFragment
import xyz.jdynb.dymovies.model.vod.VodQueryParams

abstract class SearchFragment : BaseFragment() {

  val vodQueryParams = VodQueryParams()

  private var beforeKeyword = ""

  var keyword
    get() = vodQueryParams.keyword
    set(value) {
      vodQueryParams.keyword = value
    }

  open fun search() {
    beforeKeyword = keyword
  }

  override fun onResume() {
    super.onResume()
    if (keyword.isNotBlank() && keyword != beforeKeyword) {
      search()
    }
  }

}