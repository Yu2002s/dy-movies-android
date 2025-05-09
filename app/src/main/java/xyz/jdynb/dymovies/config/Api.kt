package xyz.jdynb.dymovies.config

/**
 * App 全局 Api 配置
 */
object Api {
  // http://192.168.31.82
  val BASE_URL = "http://dyys.jdynb.xyz"
  // val BASE_URL = "http://192.168.31.82"
  // val BASE_URL = "http://192.168.110.48"
  /**
   * 影片相关接口
   */
  const val VOD = "/vods"

  /**
   * 影片类型接口
   */
  const val VOD_TYPE = "/vodTypes"

  /**
   * 影片全部类型
   */
  const val VOD_TYPE_ALL = "${VOD_TYPE}/all"

  /**
   * 根据影片类型查找影片
   */
  const val VOD_LIST_BY_TYPE = "${VOD}/type"

  /**
   * 主页接口
   */
  const val VOD_HOME = "/homes"

  /**
   * 获取最新的影片
   */
  const val VOD_LATEST = "${VOD}/latest"

  /**
   * tv 直播接口
   */
  const val TV_LIVE = "/tvLives"

  /**
   * 影片详情
   */
  const val VOD_DETAIL = "${VOD}/"

  /**
   * 影片视频
   */
  const val VOD_VIDEO = "/vodVideos"

  /**
   * 影片视频源
   */
  const val VOD_VIDEO_SOURCE = "${VOD_VIDEO}/source"

  /**
   * 影片视频列表
   */
  const val VOD_VIDEO_LIST = "$VOD_VIDEO/video"

  /**
   * 搜索影片
   */
  const val VOD_SEARCH = "/vodSearchs"

  /**
   * 搜索建议
   */
  const val VOD_SEARCH_INDEX = "${VOD_SEARCH}/suggest"

  /**
   * 影片供应商
   */
  const val VOD_PROVIDER = "/vodProviders"

  /**
   * 用户相关
   */
  const val USERS = "/users"

  /**
   * 获取验证码
   */
  const val USERS_GET_CODE = "${USERS}/code"

  /**
   * 影片评论
   */
  const val VOD_COMMENTS = "/vodComments"

  /**
   * 影片解析
   */
  const val VOD_PARSE = "/vodParses"

  /**
   * App 更新
   */
  const val APP_UPDATES = "/updates"

  /**
   * 反馈
   */
  const val FEEDBACK = "/feedbacks"

  /**
   * 爱奇艺搜索接口地址
   */
  const val IQIYI_SEARCH = "https://mesh.if.iqiyi.com/portal/lw/search/homePageV3"
  const val IQIYI_VIDEO_INFO = "https://mesh.if.iqiyi.com/player/pcw/video/playervideoinfo"

  /**
   * 豆瓣搜索接口
   */
  const val DOUBAN_SEARCH = "https://m.douban.com/rexxar/api/v2/search"

  /**
   * 豆瓣影片详情
   */
  const val DOUBAN_VIDEO_DETAIL = "https://movie.douban.com/subject"
}