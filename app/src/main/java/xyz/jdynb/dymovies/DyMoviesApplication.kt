package xyz.jdynb.dymovies;

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.widget.TextView
import androidx.core.content.pm.PackageInfoCompat
import com.danikula.videocache.HttpProxyCacheServer
import com.drake.brv.utils.BRV
import com.drake.net.NetConfig
import com.drake.net.exception.HttpResponseException
import com.drake.net.exception.NetConnectException
import com.drake.net.exception.NetSocketTimeoutException
import com.drake.net.exception.NetworkingException
import com.drake.net.interceptor.LogRecordInterceptor
import com.drake.net.interceptor.RequestInterceptor
import com.drake.net.okhttp.setConverter
import com.drake.net.okhttp.setDebug
import com.drake.net.okhttp.setRequestInterceptor
import com.drake.net.request.BaseRequest
import com.drake.statelayout.StateConfig
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import okhttp3.Cache
import org.litepal.LitePal
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.config.SPConfig
import xyz.jdynb.dymovies.exception.LoadingException
import xyz.jdynb.dymovies.utils.AesEncryption
import xyz.jdynb.dymovies.utils.Checker
import xyz.jdynb.dymovies.utils.ContextUtils
import xyz.jdynb.dymovies.utils.EncryptUtils
import xyz.jdynb.dymovies.utils.SpUtils.get
import xyz.jdynb.dymovies.utils.converter.SerializationConverter
import java.io.File
import java.util.concurrent.TimeUnit


class DyMoviesApplication : Application() {

  companion object {
    @SuppressLint("StaticFieldLeak")
    lateinit var context: Context

    /**
     * API-Token 密钥
     */
    private const val API_TOKEN_KEY = "m8wZ0TSYN2"

    val videoCacheDirectory by lazy {
      File(context.externalCacheDir, "video-cache")
    }

    /**
     * 暴露给本地代码的方法，用于获取对象的简单类名
     */
    @JvmStatic
    fun getApplicationName(obj: Any): String? {
      return obj.javaClass.getSimpleName()
    }

    @JvmStatic
    fun getProxy(): HttpProxyCacheServer {
      val application = context as DyMoviesApplication
      if (application.proxy == null) {
        application.proxy = application.newProxy()
      }
      return application.proxy!!
    }
  }

  private var proxy: HttpProxyCacheServer? = null

  init {
    // 这里对App环境进行检测，如果App签名不同，则不允许成功运行
    val ctx = ContextUtils.getContext()
    Checker.verifySignature(ctx)
  }

  override fun onCreate() {
    super.onCreate()
    context = this
    BRV.modelId = BR.m
    initNetConfig()
    initStateConfig()
    initSmartRefreshLayout()
    // 初始化加密程序
    EncryptUtils.getInstance().init()
    LitePal.initialize(this)
  }

  private fun initNetConfig() {
    val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
    // Net 初始化
    NetConfig.initialize(Api.BASE_URL, this) {
      connectTimeout(20, TimeUnit.SECONDS)
      readTimeout(30, TimeUnit.SECONDS)
      writeTimeout(30, TimeUnit.SECONDS)
      setDebug(BuildConfig.DEBUG)
      cache(Cache(cacheDir, 1024 * 1024 * 128))
      // setConverter(GsonConverter())
      // 设置响应转换器，自动进行序列化和反序列化
      setConverter(SerializationConverter())
      addInterceptor(LogRecordInterceptor(BuildConfig.DEBUG))
      // 设置请求拦截器
      setRequestInterceptor(object : RequestInterceptor {
        override fun interceptor(request: BaseRequest) {
          val now = System.currentTimeMillis().toString()
          // 添加版本信息
          request.setHeader("Version", versionCode.toString())
          // 添加客户端标识
          request.setHeader("Platform", "android")
          // 添加请求时间
          request.setHeader("Time", now)
          // 添加Api-Token，校验请求的合法性
          request.setHeader("Api-Token", AesEncryption.encrypt(API_TOKEN_KEY + now))
          // 私钥，目前并无作用
          // request.setHeader("SecretKey", EncryptUtils.getInstance().encode(API_TOKEN_KEY + now))
          SPConfig.USER_TOKEN.get<String>()?.let {
            request.setHeader("Authorization", it)
          }
        }
      })
    }
  }

  private fun initStateConfig() {
    // StateLayout 初始化
    StateConfig.apply {
      loadingLayout = R.layout.layout_loading
      errorLayout = R.layout.layout_error
      emptyLayout = R.layout.layout_empty
      // 设置重试id
      setRetryIds(R.id.error_msg)
      onError { error ->
        startAnimation()
        findViewById<TextView>(R.id.error_msg).text = when (error) {
          is NetworkingException -> "网络错误，请检查网络后点击重试"
          is NetConnectException -> "请检查当前的网络连接后点击重试"
          is NetSocketTimeoutException -> "网络超时，请稍后点击重试"
          is HttpResponseException -> {
            if (error.response.code == 500) {
              "网络请求失败，请点击重试或重新打开App"
            } else {
              getString(R.string.error_tips)
            }
          }
          is LoadingException -> "加载失败了，点击重试。若多次失败请反馈\n${error.message}"
          else -> getString(R.string.error_tips)
        }
      }
      onEmpty {
        startAnimation()
      }
      onContent {
        startAnimation()
      }
      onLoading {
        startAnimation()
      }
    }
  }

  private fun initSmartRefreshLayout() {
    SmartRefreshLayout.setDefaultRefreshHeaderCreator { _, _ -> MaterialHeader(this) }
    SmartRefreshLayout.setDefaultRefreshFooterCreator { _, _ -> ClassicsFooter(this) }
  }

  private fun View.startAnimation() {
    // 先将视图隐藏然后在800毫秒内渐变显示视图
    animate().setDuration(0).alpha(0F).withEndAction {
      animate().setDuration(800).alpha(1F)
    }
  }

  private fun newProxy(): HttpProxyCacheServer {
    return HttpProxyCacheServer.Builder(this)
      .cacheDirectory(videoCacheDirectory)
      .maxCacheSize(2 * 1024 * 1024 * 1024L)
      .build()
  }
}
