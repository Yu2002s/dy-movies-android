package xyz.jdynb.dymovies.config

/**
 * App 全局 Api 配置
 */
object Api {
  // http://192.168.31.82
  val BASE_URL = "http://dyys.jdynb.xyz"
  // val BASE_URL = "http://192.168.31.82"
  // val BASE_URL = "http://192.168.110.48"
  private const val VOD = "/vods"
  const val VOD_CATE = "/vodCates"
  const val VOD_TYPE = "/vodTypes"
  const val VOD_TYPE_ALL = "${VOD_TYPE}/all"
  const val VOD_TYPE_PARENT = "${VOD_TYPE}/parent"
  const val VOD_LIST_BY_TYPE = "${VOD}/type"
  const val VOD_HOME = "/homes"
  const val VOD_LATEST = "${VOD}/latest"
  const val TV_LIVE = "/tvLives"
  const val VOD_DETAIL = "${VOD}/"
  const val VOD_VIDEO = "/vodVideos"
  const val VOD_VIDEO_SOURCE = "${VOD_VIDEO}/source"

  const val VOD_SEARCH = "/vodSearchs"

  const val USERS = "/users"
  const val USERS_GET_CODE = "${USERS}/code"

  const val VOD_COMMENTS = "/vodComments"

  const val VOD_PARSE = "/vodParses"

  const val APP_UPDATES = "/updates"

  /**
   * 爱奇艺搜索接口地址
   */
  const val IQIYI_SEARCH = "https://mesh.if.iqiyi.com/portal/lw/search/homePageV3"
}