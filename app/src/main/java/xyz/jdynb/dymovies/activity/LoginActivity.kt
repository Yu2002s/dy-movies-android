package xyz.jdynb.dymovies.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import kotlinx.coroutines.delay
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.base.BaseActivity
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.config.SPConfig
import xyz.jdynb.dymovies.databinding.ActivityLoginBinding
import xyz.jdynb.dymovies.model.user.UserAuth
import xyz.jdynb.dymovies.utils.SpUtils.put
import xyz.jdynb.dymovies.utils.showToast

class LoginActivity : BaseActivity() {

  private lateinit var binding: ActivityLoginBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    binding.btnGetCode.setOnClickListener {
      binding.btnGetCode.isEnabled = false

      scopeNetLife {
        Post<String>(Api.USERS_GET_CODE) {
          addQuery("email", binding.editEmail.text.toString())
        }.await()

        "验证码发送成功".showToast()

        var count = 60

        while (count-- > 0) {
          delay(1000)
          binding.btnGetCode.text = "重新发送(${count}秒)"
        }
        binding.btnGetCode.isEnabled = true
        binding.btnGetCode.text = "获取验证码"
      }
    }

    binding.btnLogin.setOnClickListener {
      scopeNetLife {
        val email = binding.editEmail.text?.trim().toString()
        val userAuth = Post<UserAuth>(Api.USERS) {
          addQuery("email", email)
          addQuery("code", binding.editCode.text.toString())
        }.await()

        SPConfig.USER_TOKEN put userAuth.token
        SPConfig.USER_EMAIL put email

        "登录成功".showToast()

        finish()
        setResult(RESULT_OK)
      }
    }
  }
}