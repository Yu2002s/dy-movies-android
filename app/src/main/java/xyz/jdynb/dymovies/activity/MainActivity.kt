package xyz.jdynb.dymovies.activity

import android.content.Intent
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.adapter.CommonViewPagerAdapter
import xyz.jdynb.dymovies.base.BaseActivity
import xyz.jdynb.dymovies.databinding.ActivityMainBinding
import xyz.jdynb.dymovies.download.DownloadService
import xyz.jdynb.dymovies.fragment.home.HomeFragment
import xyz.jdynb.dymovies.fragment.live.LiveFragment
import xyz.jdynb.dymovies.fragment.setting.SettingFragment

class MainActivity : BaseActivity() {

  private lateinit var binding: ActivityMainBinding

  private val mOnPageChangeCallback = object : OnPageChangeCallback() {
    override fun onPageSelected(position: Int) {
      val menuItem = binding.bottomNav.menu.getItem(position)
      menuItem.isChecked = true
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    startService(Intent(this, DownloadService::class.java))

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    init()
    initViewPager()
    initBottomNav()
  }

  private fun init() {
  }

  private fun initBottomNav() {
    binding.bottomNav.setOnItemSelectedListener { item ->
      binding.vp.setCurrentItem(when (item.itemId) {
        R.id.nav_home -> 0
        R.id.nav_live -> 1
        R.id.nav_setting -> 2
        else -> 0
      }, false)
      true
    }
  }

  private fun initViewPager() {
    val vp = binding.vp

    /*val bottomNav = binding.bottomNav
    bottomNav.post {
      vp.updatePadding(bottom = bottomNav.measuredHeight)
    }*/

    val fragments = listOf(HomeFragment::class, LiveFragment::class, SettingFragment::class)

    vp.apply {
      isUserInputEnabled = false
      adapter = CommonViewPagerAdapter(supportFragmentManager, lifecycle, fragments)
      registerOnPageChangeCallback(mOnPageChangeCallback)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    binding.vp.unregisterOnPageChangeCallback(mOnPageChangeCallback)
  }
}