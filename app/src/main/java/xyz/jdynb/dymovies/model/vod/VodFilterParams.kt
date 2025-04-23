package xyz.jdynb.dymovies.model.vod

data class VodFilterParams(
  /* val keyword: String = "",
   val tid: Int? = null,
   val pid: Int? = null,*/
  var year: String = "",
  var sort: String = "latest"
)
