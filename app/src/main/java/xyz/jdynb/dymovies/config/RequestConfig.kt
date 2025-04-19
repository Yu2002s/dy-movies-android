package xyz.jdynb.dymovies.config

/**
 * 网络请求方面的配置
 */
object RequestConfig {

  const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0"

  const val ACCEPT = "*/*"

  const val ACCEPT_LANGUAGE = "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6"

  val VIDEO_HEADERS = mapOf(
    "User-Agent" to RequestConfig.USER_AGENT,
    "Accept" to RequestConfig.ACCEPT,
    "Accept-Language" to RequestConfig.ACCEPT_LANGUAGE
  )
}