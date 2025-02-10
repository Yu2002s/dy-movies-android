package xyz.jdynb.dymovies.fragment.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.activity.LoginActivity
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.config.SPConfig
import xyz.jdynb.dymovies.model.user.User
import xyz.jdynb.dymovies.utils.SpUtils.get
import xyz.jdynb.dymovies.utils.SpUtils.remove
import xyz.jdynb.dymovies.utils.startActivity

class SettingFragment: PreferenceFragmentCompat() {

  private var isLogin = false

  private lateinit var userInfoPreference: Preference
  private lateinit var loginPreference: Preference

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.preference_setting, rootKey)
  }

  override fun onCreateRecyclerView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    savedInstanceState: Bundle?
  ): RecyclerView {
    return super.onCreateRecyclerView(inflater, parent, savedInstanceState).also {
      ViewCompat.setOnApplyWindowInsetsListener(it) {v, insets ->
        v as RecyclerView
        v.clipToPadding = false
        v.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top)
        insets
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    userInfoPreference = findPreference("userinfo")!!
    loginPreference = findPreference("login")!!
    loginPreference.setOnPreferenceClickListener {
      if (isLogin) {
        SPConfig.USER_TOKEN.remove()
        isLogin = false
      }
      startActivity<LoginActivity>()
      true
    }
    getUser()
  }

  private fun getUser() {
    if (!isLogin) {
      return
    }
    scopeNetLife {
      Get<User?>(Api.USERS).await()?.let {
        userInfoPreference.title = it.username
      }
    }
  }

  override fun onResume() {
    super.onResume()
    val token = SPConfig.USER_TOKEN.get<String?>()
    isLogin = token != null
    loginPreference.title = if (token == null) "去登录" else "退出登录"
    loginPreference.summary = if (token == null) "点击前往登录" else null
    getUser()
  }
}