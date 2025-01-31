package xyz.jdynb.dymovies.fragment.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.activity.LoginActivity
import xyz.jdynb.dymovies.config.SPConfig
import xyz.jdynb.dymovies.databinding.FragmentSettingBinding
import xyz.jdynb.dymovies.utils.SpUtils.get
import xyz.jdynb.dymovies.utils.SpUtils.remove
import xyz.jdynb.dymovies.utils.startActivity

class SettingFragment: Fragment() {

  private var _binding: FragmentSettingBinding? = null
  private val binding get() = _binding!!

  private var isLogin = false

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.btnLogin.setOnClickListener {
      if (isLogin) {
        SPConfig.USER_TOKEN.remove()
        isLogin = false
      }
      startActivity<LoginActivity>()
    }
  }

  override fun onResume() {
    super.onResume()
    val token = SPConfig.USER_TOKEN.get<String?>()
    isLogin = token != null
    binding.btnLogin.text = if (token == null) "去登录" else "退出登录"
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}