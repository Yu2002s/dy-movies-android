package xyz.jdynb.dymovies.utils

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.bumptech.glide.Glide
import java.util.concurrent.TimeUnit

/**
 * RecyclerView 懒加载图片
 */
fun RecyclerView.lazyLoadImg(): RecyclerView {
  this.addOnScrollListener(object : OnScrollListener() {
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
      when (newState) {
        RecyclerView.SCROLL_STATE_SETTLING -> {
          Glide.with(this@lazyLoadImg.context).pauseRequests()
        }

        RecyclerView.SCROLL_STATE_IDLE -> {
          Glide.with(this@lazyLoadImg.context).resumeRequests()
        }
      }
    }
  })
  return this
}

/**
 * 让 View 自适应底部的导航栏
 */
fun View.fitNavigationBar(target: View? = null, oneSet: Boolean = false, defaultBottom: Int = 0) {
  val targetView = target ?: this
  ViewCompat.setOnApplyWindowInsetsListener(targetView) { v, insets ->
    if (oneSet) {
      ViewCompat.setOnApplyWindowInsetsListener(targetView, null)
    }
    if (v is ViewGroup) {
      v.clipToPadding = false
    }
    val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom + defaultBottom
    v.updatePadding(bottom = bottom)
    insets
  }
}

fun View.throttleClick(
  interval: Long = 500,
  unit: TimeUnit = TimeUnit.MILLISECONDS,
  block: View.() -> Unit
) {
  setOnClickListener(ThrottleClickListener(interval, unit, block))
}

class ThrottleClickListener(
  private val interval: Long = 500,
  private val unit: TimeUnit = TimeUnit.MILLISECONDS,
  private var block: View.() -> Unit
) : View.OnClickListener {
  private var lastTime: Long = 0

  override fun onClick(v: View) {
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastTime > unit.toMillis(interval)) {
      lastTime = currentTime
      block(v)
    }
  }
}