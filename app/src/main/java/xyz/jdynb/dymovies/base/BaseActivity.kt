package xyz.jdynb.dymovies.base

import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
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

  override fun onThemeChanged(theme: String) {
    ThemeUtils.setTheme(this)
    recreate()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      finish()
      return true
    }
    return super.onOptionsItemSelected(item)
  }
}