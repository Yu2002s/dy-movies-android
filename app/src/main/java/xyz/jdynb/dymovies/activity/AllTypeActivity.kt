package xyz.jdynb.dymovies.activity

import android.os.Bundle
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.base.BaseActivity
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.ActivityAllTypeBinding
import xyz.jdynb.dymovies.model.vod.VodType

class AllTypeActivity: BaseActivity() {

  private lateinit var binding: ActivityAllTypeBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityAllTypeBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    binding.typeRv.grid(3).divider {
      orientation = DividerOrientation.GRID
      setDivider(8, true)
      includeVisible = true
    }.setup {
      addType<VodType>(R.layout.item_grid_vod_type)
      R.id.item.onClick {
        HomeVodTypeActivity.actionStart(this@AllTypeActivity, getModel())
      }
    }

    binding.state.onRefresh {
      scope {
        val result = Get<List<VodType>>(Api.VOD_TYPE_ALL).await()
        binding.typeRv.models = result
      }
    }.showLoading()
  }
}