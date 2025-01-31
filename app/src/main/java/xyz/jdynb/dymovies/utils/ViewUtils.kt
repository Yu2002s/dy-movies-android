package xyz.jdynb.dymovies.utils

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener

fun RecyclerView.loadImg(): RecyclerView {
    this.addOnScrollListener(object : OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            when (newState) {
                RecyclerView.SCROLL_STATE_SETTLING -> {
                    // Glide.with(this@loadImg.context).pauseRequests()
                }
                RecyclerView.SCROLL_STATE_IDLE -> {
                    // Glide.with(this@loadImg.context).resumeRequests()
                }
            }
        }
    })
    return this
}