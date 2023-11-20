package com.origi.test.app.ui.login

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import com.gaming.core.GameSDK
import com.gaming.core.extensions.toast

import com.org.marcus.x.databinding.ActivityLoginBinding
import kotlin.math.log

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        val domain: EditText = binding.domain!!

        GameSDK.init(application, true, assets.open("sdk.data.enc.1.0.0").readBytes())

        loginViewModel =
            ViewModelProvider(this, LoginViewModelFactory())[LoginViewModel::class.java]

        loginViewModel.status.observe(this@LoginActivity) {
            if (it == Invalid) {
                toast("开关没打开,请检查后台配置~")
            }
        }

        login.setOnClickListener {
            loading.visibility = View.VISIBLE
            loginViewModel.login(
                this@LoginActivity,
                username.text.toString(),
                password.text.toString(),
                domain.text?.toString() ?: ""
            )
        }
    }
}
