package xyz.jdynb.dymovies.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.WindowManager
import xyz.jdynb.dymovies.DyMoviesApplication

/**
 * dp转像素
 */
fun Int.dp2px(context: Context = DyMoviesApplication.context): Int {
    return (context.resources.displayMetrics.density * this + 0.5).toInt()
}

@JvmName("getWindowWidthWithCtx")
fun Context.getWindowWidth(): Int {
    if (this is Activity) {
        return getWindowWidth()
    }
    return getWindowWidth(this)
}

fun Dialog.getWindowWidth(): Int {
    val wm = this.window!!.windowManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        return wm.currentWindowMetrics.bounds.width()
    }
    return wm.defaultDisplay.width
}

fun Dialog.getWindowHeight(): Int {
    val wm = this.window!!.windowManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        return wm.currentWindowMetrics.bounds.height()
    }
    return wm.defaultDisplay.height
}

fun Activity.getWindowWidth(): Int {
   return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this.windowManager.currentWindowMetrics.bounds.width()
    } else {
        this.windowManager.defaultDisplay.width
    }
}

fun Activity.getWindowHeight(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this.windowManager.currentWindowMetrics.bounds.height()
    } else {
        this.windowManager.defaultDisplay.height
    }
}

fun getWindowWidth() = getWindowWidth(null)

fun getWindowWidth(ctx: Context? = null): Int {
    val context = ctx ?: DyMoviesApplication.context
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        windowManager.currentWindowMetrics.bounds.width()
    } else {
        windowManager.defaultDisplay.width
    }
}

fun getWindowHeight(): Int {
    val context = DyMoviesApplication.context
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        windowManager.currentWindowMetrics.bounds.height()
    } else {
        windowManager.defaultDisplay.height
    }
}

/**
 * 是否为深色模式
 */
val isDarkMode: Boolean get() {
    val context = DyMoviesApplication.context
    val theme = ThemeUtils.currentTheme
    if (theme != ThemeUtils.THEME_AUTO) {
        return theme == ThemeUtils.THEME_DARK
    }
    val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
}
