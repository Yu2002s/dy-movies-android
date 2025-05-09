package xyz.jdynb.dymovies.ui.fragment.search

import android.os.Bundle
import android.view.View
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

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
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