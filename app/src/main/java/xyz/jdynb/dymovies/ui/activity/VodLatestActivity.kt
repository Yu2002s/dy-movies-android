package xyz.jdynb.dymovies.ui.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.base.BaseActivity
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.ActivityVodLastestBinding
import xyz.jdynb.dymovies.model.vod.Vod
import xyz.jdynb.dymovies.utils.fitNavigationBar

class VodLatestActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val binding =
      DataBindingUtil.setContentView<ActivityVodLastestBinding>(this, R.layout.activity_vod_lastest)
    setSupportActionBar(binding.header.toolBar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    binding.latestRv.grid(3).divider {
      orientation = DividerOrientation.GRID
      setDivider(8, true)
      includeVisible = true
    }.also {
      it.setHasFixedSize(true)
      it.fitNavigationBar()
    }.setup {
      setAnimation(AnimationType.SCALE)
      addType<Vod>(R.layout.item_grid_vod)
      R.id.vod_img.onClick {
        VideoPlayActivity.play(getModel<Vod>().id)
      }
    }

    binding.refresh.onRefresh {
      scope {
        addData(Get<List<Vod>>(Api.VOD_LATEST) {
          addQuery("page", index)
        }.await())
      }
    }.showLoading()
  }

}