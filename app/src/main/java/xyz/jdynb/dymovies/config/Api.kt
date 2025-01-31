package xyz.jdynb.dymovies.config

import xyz.jdynb.dymovies.BuildConfig

object Api {
  val BASE_URL = /*if (BuildConfig.DEBUG) "http://192.168.245.123" else*/ "http://dyys.jdynb.xyz"
  private const val VOD = "/vods"
  const val VOD_TYPE = "/vodTypes"
  const val VOD_TYPE_ALL = "${VOD_TYPE}/all"
  const val VOD_LIST_BY_TYPE = "${VOD}/type"
  const val VOD_HOME = "/homes"
  const val TV_LIVE = "/tvLives"
  const val VOD_DETAIL = "${VOD}/"
  const val VOD_VIDEO = "/vodVideos"

  const val VOD_SEARCH = "/vodSearchs"

  const val USERS = "/users"
  const val USERS_GET_CODE = "${USERS}/code"

  const val VOD_COMMENTS = "/vodComments"

  const val VOD_PARSE = "/vodParses"

  /**
   * 爱奇艺搜索
   */
  const val IQIYI_SEARCH = "https://mesh.if.iqiyi.com/portal/lw/search/homePageV3"
}