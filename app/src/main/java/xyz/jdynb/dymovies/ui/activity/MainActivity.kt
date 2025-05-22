package xyz.jdynb.dymovies.ui.activity

import android.Manifest
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
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
import kotlin.system.exitProcess

class MainActivity : BaseActivity() {

  private lateinit var binding: ActivityMainBinding

  private lateinit var insetController: WindowInsetsControllerCompat

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

  private fun init() {
    insetController = WindowCompat.getInsetsController(window, binding.root)
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
              val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
              downloadManager.enqueue(request)
            } catch (ignored: Exception) {
              openBrowser()
            }
          }
          .setNeutralButton("外部更新",) {_, _ ->
            openBrowser()
          }
            .also {
            if (update.isForce == 0) {
              it.setNegativeButton("取消", null)
            }
          }.show()
      }
    }
  }

  /**
   * 检查 App 所需的权限是否已授权
   * 仅进行请求权限，而不对其他任何处理
   */
  private fun checkPermissions() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
      val flag = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
      if (flag != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
      }
    } else {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // 大于 Android 34 Sdk 需要请求通知权限，否则将不会显示通知
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
      }
      if (Environment.isExternalStorageManager()) {
        return
      }
      MaterialAlertDialogBuilder(this)
        .setTitle("需要权限")
        .setMessage("下载视频文件需要访问手机储存权限")
        .setPositiveButton("去授权") {_, _ ->
          val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, "package:$packageName".toUri())
          startActivity(intent)
        }
        .setNegativeButton("取消", null)
        .setCancelable(false)
        .show()
    }
  }

  override fun onStart() {
    super.onStart()
    checkUpdate()
  }

  override fun onDestroy() {
    super.onDestroy()
    // App 销毁时取消注册指定的回调，这里已经是MainActivity，通常不需要此操作
    binding.vp.unregisterOnPageChangeCallback(mOnPageChangeCallback)
  }
}