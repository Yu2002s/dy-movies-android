package xyz.jdynb.dymovies.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.WindowCompat
import com.google.android.material.color.DynamicColors
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.utils.getWindowHeight
import xyz.jdynb.dymovies.utils.getWindowWidth

/**
 * 自适应横屏竖屏的对话框
 */
open class AdaptiveDialog(context: Context) :
  Dialog(context, /*R.style.BaseDialogStyle*/) {

  private val activity = (context as Activity)

  @SuppressLint("PrivateResource")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setCanceledOnTouchOutside(true)

    window?.let {
      it.navigationBarColor = Color.TRANSPARENT
      WindowCompat.setDecorFitsSystemWindows(it, false)
      it.setBackgroundDrawableResource(R.drawable.bg_corner)
    }
  }

  @SuppressLint("PrivateResource")
  override fun onStart() {
    super.onStart()
    val windowWidth = getWindowWidth()
    val windowHeight = getWindowHeight()
    window?.let {
      val attributes = it.attributes
      if (isPortrait()) {
        attributes.width = windowWidth
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
        attributes.gravity = Gravity.BOTTOM
        it.setWindowAnimations(com.google.android.material.R.style.Animation_Material3_BottomSheetDialog)
      } else {
        attributes.width = windowWidth / 3
        attributes.height = windowHeight
        attributes.gravity = Gravity.END
        it.setWindowAnimations(com.google.android.material.R.style.Animation_Material3_SideSheetDialog)
      }
    }
  }

  /**
   * 是否是竖屏状态
   */
  private fun isPortrait() = activity.requestedOrientation.run {
    this == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        || this == ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
        || this == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
  }

}