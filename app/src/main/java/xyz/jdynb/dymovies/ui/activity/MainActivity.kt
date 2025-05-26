package xyz.jdynb.dymovies.ui.activity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.CacheControl
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.adapter.CommonViewPagerAdapter
import xyz.jdynb.dymovies.base.BaseActivity
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.ActivityMainBinding
import xyz.jdynb.dymovies.download.DownloadService
import xyz.jdynb.dymovies.model.app.Update
import xyz.jdynb.dymovies.ui.fragment.home.HomeFragment
import xyz.jdynb.dymovies.ui.fragment.live.LiveFragment
import xyz.jdynb.dymovies.ui.fragment.mine.MineFragment
import xyz.jdynb.dymovies.utils.isDarkMode
import xyz.jdynb.dymovies.utils.showToast
import xyz.jdynb.dymovies.utils.startActivity
import java.util.Timer
import java.util.TimerTask
import kotlin.system.exitProcess

class MainActivity : BaseActivity() {

  private lateinit var binding: ActivityMainBinding

  private lateinit var insetController: WindowInsetsControllerCompat

  private val downloadManager by lazy {
    getSystemService(DOWNLOAD_SERVICE) as DownloadManager
  }

  private var downloadedUri: Uri? = null

  companion object {
    private const val TAG = "MainActivity"
  }

  private val mOnPageChangeCallback = object : OnPageChangeCallback() {
    override fun onPageSelected(position: Int) {
      val menuItem = binding.bottomNav.menu.getItem(position)
      menuItem.isChecked = true
      insetController.isAppearanceLightStatusBars = !isDarkMode && menuItem.itemId != R.id.nav_mine
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // 启动下载服务
    startService(Intent(this, DownloadService::class.java))

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    init()
    initViewPager()
    initBottomNav()
    checkPermissions()
  }

  @SuppressLint("UnspecifiedRegisterReceiverFlag")
  private fun init() {
    insetController = WindowCompat.getInsetsController(window, binding.root)

    val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
    intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED)

    ContextCompat.registerReceiver(
      this, downloadBroadcastReceiver, intentFilter,
      ContextCompat.RECEIVER_EXPORTED
    )
  }

  /**
   * 初始化底部导航栏
   */
  private fun initBottomNav() {
    binding.bottomNav.setOnItemSelectedListener { item ->
      binding.vp.setCurrentItem(
        when (item.itemId) {
          R.id.nav_home -> 0
          R.id.nav_live -> 1
          R.id.nav_mine -> 2
          else -> 0
        }, false
      )
      true
    }
  }

  /**
   * 初始化 ViewPager2
   */
  private fun initViewPager() {
    val vp = binding.vp

    val fragments = listOf(HomeFragment::class, LiveFragment::class, MineFragment::class)

    vp.apply {
      isUserInputEnabled = false
      adapter = CommonViewPagerAdapter(supportFragmentManager, lifecycle, fragments)
      registerOnPageChangeCallback(mOnPageChangeCallback)
    }
  }

  /**
   * 检查更新
   */
  private fun checkUpdate() {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
    scopeNetLife {
      Get<Update?>(Api.APP_UPDATES + "/${versionCode}") {
        setCacheControl(CacheControl.FORCE_NETWORK)
      }.await()?.let { update ->
        val uri = update.url.toUri()

        // 强制更新的情况下，点击对话框按钮时将强行退出 App
        fun exitApp() {
          if (update.isForce == 1) {
            exitProcess(0)
          }
        }

        // 打开浏览器更新
        fun openBrowser() {
          startActivity(Intent(Intent.ACTION_VIEW, uri))
          exitApp()
        }

        MaterialAlertDialogBuilder(this@MainActivity)
          .setTitle("发现新版本: ${update.versionName}")
          .setMessage(update.content)
          .setCancelable(update.isForce == 0)
          .setPositiveButton("下载更新") { _, _ ->

            try {
              val title = getString(R.string.app_name) + update.versionName + ".apk"

              val request = DownloadManager.Request(uri)
                .setTitle(title)
                .setDescription("冬雨影视更新")
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
              val id = downloadManager.enqueue(request)
              getDownloadStatus(id)
            } catch (ignored: Exception) {
              openBrowser()
            }
          }
          .setNeutralButton("外部更新") { _, _ ->
            openBrowser()
          }
          .also {
            if (update.isForce == 0) {
              it.setNegativeButton("取消", null)
            }
          }.show()
      }
    }.catch {
      MaterialAlertDialogBuilder(this@MainActivity)
        .setTitle("提示")
        .setMessage("检查更新失败了，可能系统正在维护中...请耐心等待修复")
        .setPositiveButton("关闭", null)
        .show()
    }
  }

  /**
   * 通过 URI 安装 apk
   */
  private fun installApk() {
    downloadedUri ?: return
    // install apk
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(downloadedUri, "application/vnd.android.package-archive")
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val canInstall = packageManager.canRequestPackageInstalls()
      if (!canInstall) {
        register.launch(
          Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            .setData("package:$packageName".toUri())
        )
        return
      }
    }
    startActivity(intent)
  }

  private fun getDownloadStatus(id: Long) {
    val timer = Timer()
    var i = 0
    val query = DownloadManager.Query().setFilterById(id)
    timer.schedule(object : TimerTask() {
      override fun run() {
        val cursor = downloadManager.query(query)
        cursor.moveToFirst()
        try {
          val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
          if (status == DownloadManager.STATUS_FAILED) {
            Log.d(TAG, "下载出错了")
            timer.cancel()
            MaterialAlertDialogBuilder(this@MainActivity)
              .setTitle("下载出错")
              .setMessage("下载地址异常，请反馈开发者修复，请耐心等待开发者修复此问题")
              .setPositiveButton("关闭", null)
              .setNegativeButton("反馈") { _, _ ->
                startActivity<FeedbackActivity>()
              }.show()
          } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
            Log.d(TAG, "下载完成")
            timer.cancel()
          }
        } catch (e: IllegalArgumentException) {
          timer.cancel()
        }
        if (++i > 100) {
          timer.cancel()
        }
      }
    }, 0, 1000)
  }

  private val register =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == RESULT_OK) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && packageManager.canRequestPackageInstalls()) {
          installApk()
        }
      }
    }

  private val downloadBroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
      when (intent.action) {
        DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
          val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
          downloadedUri = downloadManager.getUriForDownloadedFile(downloadId)
          installApk()
          "冬雨影视更新安装包下载完成，正在前往安装...".showToast()
        }

        DownloadManager.ACTION_NOTIFICATION_CLICKED -> {
          Log.d(TAG, "用户点击了通知")
        }
      }
    }
  }

  override fun onStart() {
    super.onStart()
    if (downloadedUri != null) {
      return
    }
    checkUpdate()
  }

  override fun onDestroy() {
    super.onDestroy()
    register.unregister()
    unregisterReceiver(downloadBroadcastReceiver)
    // App 销毁时取消注册指定的回调，这里已经是MainActivity，通常不需要此操作
    binding.vp.unregisterOnPageChangeCallback(mOnPageChangeCallback)
  }
}