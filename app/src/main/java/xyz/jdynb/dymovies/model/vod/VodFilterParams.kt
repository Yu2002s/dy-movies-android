package xyz.jdynb.dymovies.model.vod

data class VodFilterParams(
  var type: Int? = null,
  var year: String? = null,
  var sort: Int = 1,
  var area: String? = null,
  var page: Int = 1,
)
