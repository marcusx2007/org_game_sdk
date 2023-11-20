package com.origi.test.app.ui.login

import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import com.gaming.core.GameSDK
import com.gaming.core.extensions.toast

import com.org.marcus.x.databinding.ActivityLoginBinding
import java.net.URL
import java.net.URLDecoder

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        hideVirtualButton(window)
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        val domain: EditText = binding.domain!!

        GameSDK.init(application, true, assets.open("sdk.data.enc").readBytes())

        loginViewModel =
            ViewModelProvider(this, LoginViewModelFactory())[LoginViewModel::class.java]

        loginViewModel.status.observe(this@LoginActivity) {
            if (it == Invalid) {
                loading.isVisible = false
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

    private fun hideVirtualButton(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        //隐藏导航栏
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN)
        window.decorView.setOnSystemUiVisibilityChangeListener {
            if (it and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                // 导航栏可见时，隐藏导航栏
                window.decorView.systemUiVisibility = uiOptions
            }
        }
        //适配凹面屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = android.graphics.Color.TRANSPARENT
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        //透明状态栏 透明导航栏
        val compat = WindowInsetsControllerCompat(window, window.decorView)
        compat.hide(WindowInsetsCompat.Type.statusBars())
        compat.hide(WindowInsetsCompat.Type.navigationBars())
        compat.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
