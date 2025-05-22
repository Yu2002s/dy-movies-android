package xyz.jdynb.dymovies.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.model.SystemNotify

class NotifyFlipperAdapter : BaseAdapter() {

  private val mData = mutableListOf<SystemNotify>()

  fun submitData(data: List<SystemNotify>) {
    mData.clear()
    mData.addAll(data)
    notifyDataSetChanged()
  }

  override fun getCount() = mData.size

  override fun getItem(position: Int) = mData[position]

  override fun getItemId(position: Int) = position.toLong()

  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val view: TextView = if (convertView == null) {
      LayoutInflater.from(parent.context)
        .inflate(R.layout.item_list_notify, parent, false) as TextView
    } else {
      convertView as TextView
    }
    view.text = mData[position].content
    return view
  }
}