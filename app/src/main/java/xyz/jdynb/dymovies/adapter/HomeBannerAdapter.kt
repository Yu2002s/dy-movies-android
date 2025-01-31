package xyz.jdynb.dymovies.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.youth.banner.adapter.BannerAdapter
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.databinding.ItemListBannerBinding
import xyz.jdynb.dymovies.model.vod.HomeVod

class HomeBannerAdapter(data: MutableList<HomeVod.Banner>? = null) :
  BannerAdapter<HomeVod.Banner, HomeBannerAdapter.BannerViewHolder>(data) {

  override fun onCreateHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
    val itemBinding = DataBindingUtil.inflate<ItemListBannerBinding>(
      LayoutInflater.from(parent.context),
      R.layout.item_list_banner,
      parent,
      false
    )
    return BannerViewHolder(itemBinding)
  }

  override fun onBindView(
    holder: BannerViewHolder,
    data: HomeVod.Banner?,
    position: Int,
    size: Int
  ) {
    holder.itemBinding.m = data
  }

  class BannerViewHolder : RecyclerView.ViewHolder {

    lateinit var itemBinding: ItemListBannerBinding

    constructor(itemView: View) : super(itemView)
    constructor(itemBinding: ItemListBannerBinding) : super(itemBinding.root) {
      this.itemBinding = itemBinding
    }
  }

}