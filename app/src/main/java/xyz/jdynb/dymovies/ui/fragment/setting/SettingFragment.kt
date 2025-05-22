package xyz.jdynb.dymovies.ui.fragment.setting

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.recyclerview.widget.RecyclerView
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.config.AppConfig.DEV_HOME_URL
import xyz.jdynb.dymovies.config.AppConfig.SPLIT_LANZOU_URL
import xyz.jdynb.dymovies.config.SPConfig
import xyz.jdynb.dymovies.model.user.User
import xyz.jdynb.dymovies.ui.activity.DownloadActivity
import xyz.jdynb.dymovies.ui.activity.LoginActivity
import xyz.jdynb.dymovies.utils.SpUtils.get
import xyz.jdynb.dymovies.utils.SpUtils.remove
import xyz.jdynb.dymovies.utils.ThemeUtils
import xyz.jdynb.dymovies.utils.fitNavigationBar
import xyz.jdynb.dymovies.utils.startActivity

class SettingFragment : PreferenceFragmentCompat() {

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
    val recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState)
    recyclerView.fitNavigationBar()
    return recyclerView
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

    findPreference<Preference>("clear_cache")?.setOnPreferenceClickListener {
      MaterialAlertDialogBuilder(requireContext()).setTitle("提示")
        .setMessage("清理视频播放存在的缓存，建议定期进行清理。正在下载文件时请勿执行清理")
        .setPositiveButton("清理") { _, _ ->
          requireContext().externalCacheDir?.deleteRecursively()
        }
        .setNegativeButton("取消", null)
        .show()
      true
    }

    val items = arrayOf(
      "BRV(com.github.liangjingkanji)",
      "Net(com.github.liangjingkanji)",
      "danmakuFlameMaster(com.github.ctiao)",
      "okhttp(com.squareup.okhttp3)",
      "Glide(com.github.bumptech.glide)",
      "Litepal(org.litepal.guolindev)",
      "uaoan-dlna(com.github.uaoan)",
      "AndroidVideoCache",
    )

    findPreference<Preference>("usage_dep")?.setOnPreferenceClickListener {
      MaterialAlertDialogBuilder(requireContext())
        .setTitle("第三方依赖")
        .setItems(items, null)
        .setPositiveButton("关闭", null)
        .show()
      true
    }

    findPreference<Preference>("statement")?.setOnPreferenceClickListener {
      MaterialAlertDialogBuilder(requireContext())
        .setTitle("免责声明")
        .setMessage("App内所有资源均来自于互联网采集而来，本App和服务端不存储任何资源，请勿相信视频内出现的如何广告")
        .setPositiveButton("关闭", null)
        .show()
      true
    }

    findPreference<Preference>("dev")?.setOnPreferenceClickListener {
      startActivity(Intent(Intent.ACTION_VIEW, DEV_HOME_URL.toUri()))
      true
    }

    findPreference<Preference>("splitlanzou")?.setOnPreferenceClickListener {
      startActivity(Intent(Intent.ACTION_VIEW, SPLIT_LANZOU_URL.toUri()))
      true
    }

    findPreference<SwitchPreferenceCompat>(SPConfig.DARK_THEME)?.setOnPreferenceChangeListener { _, newValue ->
      val isDark = newValue as Boolean
      ThemeUtils.setTheme(requireActivity(), if (isDark) ThemeUtils.THEME_DARK else ThemeUtils.THEME_AUTO)
      ThemeUtils.notifyThemeChanged()
      true
    }

    findPreference<Preference>("download_manager")?.setOnPreferenceClickListener {
      startActivity<DownloadActivity>()
      true
    }

    findPreference<EditTextPreference>(SPConfig.DOWNLOAD_PATH)?.setDefaultValue(
      Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS
      )!!.path + "/DongYuMovies"
    )
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
    if (!isLogin) {
      userInfoPreference.title = "当前未登录"
    }
    loginPreference.title = if (token == null) "去登录" else "退出登录"
    loginPreference.summary = if (token == null) "点击前往登录" else null
    getUser()
  }
}