package xyz.jdynb.dymovies.utils

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.bumptech.glide.Glide

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
fun View.fitNavigationBar() {
  ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
    if (v is ViewGroup) {
      v.clipToPadding = false
    }
    val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
    v.updatePadding(bottom = bottom)
    insets
  }
}