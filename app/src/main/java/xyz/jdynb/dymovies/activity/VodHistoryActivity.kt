package xyz.jdynb.dymovies.activity

import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import xyz.jdynb.dymovies.adapter.CommonViewPagerAdapter
import xyz.jdynb.dymovies.base.BaseActivity
import xyz.jdynb.dymovies.databinding.ActivityHistoryBinding
import xyz.jdynb.dymovies.fragment.history.VodFavoriteFragment
import xyz.jdynb.dymovies.fragment.history.VodHistoryFragment

class VodHistoryActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val binding = ActivityHistoryBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    val tab = binding.tab
    val vp = binding.vp

    val fragments = listOf(VodHistoryFragment::class, VodFavoriteFragment::class)

    vp.adapter = CommonViewPagerAdapter(supportFragmentManager, lifecycle, fragments)

    TabLayoutMediator(tab, vp) { t, position ->
      t.text = when (position) {
        0 -> "历史"
        else -> "收藏"
      }
    }.attach()
  }

}