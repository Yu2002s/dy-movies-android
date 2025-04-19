package xyz.jdynb.dymovies.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.adapter.CommonViewPagerAdapter
import xyz.jdynb.dymovies.base.BaseActivity
import xyz.jdynb.dymovies.databinding.ActivityHistoryBinding
import xyz.jdynb.dymovies.event.Checkable
import xyz.jdynb.dymovies.fragment.history.VodFavoriteFragment
import xyz.jdynb.dymovies.fragment.history.VodHistoryFragment

class VodHistoryActivity : BaseActivity(), MenuProvider {

  private lateinit var binding: ActivityHistoryBinding

  var toggleMode = false
    set(value) {
      field = value
      invalidateMenu()
    }

  var totalCount = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityHistoryBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    addMenuProvider(this, this)

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

    tab.addOnTabSelectedListener(object : OnTabSelectedListener {
      override fun onTabSelected(tab: TabLayout.Tab?) {
        toggleMode = false
        getCurrentChildFragment {
          it.toggle(toggleMode)
        }
      }

      override fun onTabUnselected(tab: TabLayout.Tab?) {

      }

      override fun onTabReselected(tab: TabLayout.Tab?) {

      }

    })
  }

  private fun getCurrentChildFragment(block: (Checkable) -> Unit) {
    val fragments = supportFragmentManager.fragments
    if (fragments.isEmpty()) {
      return
    }
    val currentFragment = fragments[binding.vp.currentItem]
    if (currentFragment is Checkable) {
      block(currentFragment)
    }
  }

  override fun onResume() {
    super.onResume()
    getCurrentChildFragment {
      it.refresh()
    }
  }

  override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
    getCurrentChildFragment {
      when (menuItem.itemId) {
        R.id.edit -> it.toggle(!toggleMode)

        R.id.delete -> it.delete()

        R.id.check_all -> {
          menuItem.isChecked = !menuItem.isChecked
          it.checkAll(menuItem.isChecked)
        }

        R.id.reverse_check -> it.reverseCheck()
      }
    }
    return true
  }

  override fun onPrepareMenu(menu: Menu) {
    super.onPrepareMenu(menu)
    val editItem = menu.findItem(R.id.edit)
    editItem.title = if (toggleMode) getString(R.string.cancel) else getString(R.string.manage)
    editItem.icon = ContextCompat.getDrawable(
      this,
      if (toggleMode) R.drawable.baseline_edit_off_24 else R.drawable.baseline_mode_edit_24
    )
  }

  override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
    menuInflater.inflate(R.menu.menu_check, menu)
  }

}