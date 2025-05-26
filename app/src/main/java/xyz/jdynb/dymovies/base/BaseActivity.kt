package xyz.jdynb.dymovies.base

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import xyz.jdynb.dymovies.event.ThemeObserver
import xyz.jdynb.dymovies.utils.ThemeUtils

/**
 * Activity 基类
 */
open class BaseActivity : AppCompatActivity(), ThemeObserver {

  companion object {
    /**
     * 是否是小白条导航栏
     */
    private var isSmallNavigationBar = true
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    ThemeUtils.setTheme(this)
    ThemeUtils.addObserver(this)
    initWindow()
    super.onCreate(savedInstanceState)
  }

  override fun onDestroy() {
    super.onDestroy()
    ThemeUtils.removeObserver(this)
  }

  private fun initWindow() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
      ViewCompat.setOnApplyWindowInsetsListener(v, null)
      val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
      val navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
      isSmallNavigationBar = navigationBarHeight <= 120
      if (onInsetChanged(statusBarHeight, navigationBarHeight)) {
        val rootView: ViewGroup = v.findViewById<ViewGroup>(android.R.id.content)
          .getChildAt(0) as ViewGroup
        (getRecyclerView() ?: getRecyclerView(rootView))?.apply {
          clipToPadding = false
          updatePadding(bottom = navigationBarHeight)
        }
      }
      insets
    }
  }

  /**
   * @return 返回true将自动对 recyclerview 进行底栏处理
   */
  protected open fun onInsetChanged(statusBarHeight: Int, navigationBarHeight: Int): Boolean {
    return false
  }

  /**
   * 返回需要处理底栏的RecyclerView
   */
  protected open fun getRecyclerView(): RecyclerView? {
    return null
  }

  /**
   * 获取当前页面的第一个RecyclerView
   */
  private fun getRecyclerView(view: ViewGroup): RecyclerView? {
    for (i in 0 until view.childCount) {
      val child = view.getChildAt(i)
      if (child is RecyclerView) {
        return child
      }
    }
    return null
  }

  /**
   * 主题改变时触发
   */
  override fun onThemeChanged(theme: String) {
    // 设置主题
    ThemeUtils.setTheme(this)
    // 重启 activity
    recreate()
  }

  /**
   * 对默认的返回事件进行处理
   */
  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      finish()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  /**
   * 检查 App 所需的权限是否已授权
   * 仅进行请求权限，而不对其他任何处理
   */
  fun checkPermissions() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
      val flag = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
      if (flag != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(
          this,
          arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
          0
        )
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
        .setPositiveButton("去授权") { _, _ ->
          val intent = Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            "package:$packageName".toUri()
          )
          startActivity(intent)
        }
        .setNegativeButton("取消", null)
        .setCancelable(false)
        .show()
    }
  }
}