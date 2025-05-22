package xyz.jdynb.dymovies.ui.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.WindowCompat
import xyz.jdynb.dymovies.base.BaseActivity
import xyz.jdynb.dymovies.databinding.ActivitySettingBinding

class SettingActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val binding = ActivitySettingBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.header.toolBar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }

  override fun onSupportNavigateUp(): Boolean {
    if (supportFragmentManager.backStackEntryCount == 0) {
      finish()
      return true
    }
    supportFragmentManager.popBackStack()
    return super.onSupportNavigateUp()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return false
  }

}