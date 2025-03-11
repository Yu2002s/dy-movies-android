package xyz.jdynb.dymovies.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import xyz.jdynb.dymovies.base.BaseActivity
import xyz.jdynb.dymovies.databinding.ActivityVodTypeBinding
import xyz.jdynb.dymovies.model.vod.VodType
import xyz.jdynb.dymovies.utils.getSerializableArguments
import xyz.jdynb.dymovies.utils.putSerializable

class HomeVodTypeActivity: BaseActivity() {

  companion object {
    fun actionStart(context: Context, vodType: VodType) {
      val intent = Intent(context, HomeVodTypeActivity::class.java)
      intent.putSerializable("vodType", vodType)
      context.startActivity(intent)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val binding = ActivityVodTypeBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)

    val vodType = getSerializableArguments<VodType>("vodType") ?: return

    supportActionBar?.title = vodType.name
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    /*supportFragmentManager.beginTransaction()
      .replace(R.id.fragment, HomeVodTypeFragment.newInstance(vodType))
      .commit()*/
  }
}