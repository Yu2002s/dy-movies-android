package xyz.jdynb.dymovies.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.ui.activity.VideoPlayActivity
import xyz.jdynb.dymovies.databinding.ItemListBannerBinding
import xyz.jdynb.dymovies.model.vod.HomeVod

class HomeBannerAdapter2: PagerAdapter() {

  private val data = mutableListOf<HomeVod.Banner>()

  fun submitData(list: List<HomeVod.Banner>) {
    data.clear()
    data.addAll(list)
    notifyDataSetChanged()
  }

  override fun getCount() = 9999

  override fun isViewFromObject(view: View, `object`: Any): Boolean {
    return view == `object`
  }

  override fun instantiateItem(container: ViewGroup, position: Int): Any {
    val binding = DataBindingUtil.inflate<ItemListBannerBinding>(
      LayoutInflater.from(container.context),
      R.layout.item_list_banner,
      container,
      false
    )
    val item = data[position % data.size]
    binding.m = item
    binding.root.setOnClickListener {
      VideoPlayActivity.play(item.id)
    }
    container.addView(binding.root)
    return binding.root
  }

  override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
    container.removeView(`object` as View)
  }
}