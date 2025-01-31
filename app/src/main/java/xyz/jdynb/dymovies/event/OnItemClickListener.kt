package xyz.jdynb.dymovies.event

import android.view.View

interface OnItemClickListener {

  fun onItemClick(view: View, position: Int)
}