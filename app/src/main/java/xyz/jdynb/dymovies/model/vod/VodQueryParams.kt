package xyz.jdynb.dymovies.model.vod

import kotlinx.serialization.Serializable

@Serializable
data class VodQueryParams(
  var keyword: String = "",
  var pid: Int? = null,
  var tid: Int? = null,
  var year: String? = null,
  var sort: Int = 1,
  var flag: String? = null,
  var area: String? = null,
  var page: Int = 1,
)
