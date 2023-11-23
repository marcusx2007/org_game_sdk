package com.origi.test.app.ui.login

import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
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
import com.org.marcus.x.R

import com.org.marcus.x.databinding.ActivityLoginBinding
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private val array = arrayOf("https://game.noradc.com", "https://game.ir02sg.com")

    override fun onCreate(savedInstanceState: Bundle?) {
        hideVirtualButton(window)
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val login = binding.login
        val loading = binding.loading
        val domain: EditText = binding.domain!!

        binding.rgEnv?.setOnCheckedChangeListener { group, int ->
            Log.d("LoginActivity", "onCreate: $int")
            when (int) {
                R.id.rb_br_env -> {
                    domain.setText(array[0])
                }

                R.id.rb_id_env -> {
                    domain.setText(array[1])
                }
            }
        }

        val data = assets.open("sdk.data.enc").readBytes()
        GameSDK.init(application, true, data)

        loginViewModel =
            ViewModelProvider(this, LoginViewModelFactory())[LoginViewModel::class.java]

        loginViewModel.status.observe(this@LoginActivity) {
            if (it == Invalid) {
                loading.isVisible = false
                toast("开关没打开,请检查后台配置~")
            }
        }

        val json = JSONObject(loginViewModel.aes(data))
        Log.d("core-sdk-impl-logger","aes data: $json")
        binding.username.setText(json.optString("chn"))
        binding.password.setText(json.optString("brd"))

        login.setOnClickListener {
            loading.visibility = View.VISIBLE
            loginViewModel.login(
                this@LoginActivity,
                binding.cbShf?.isChecked ?: true,
                binding.username.text.toString().trim(),
                binding.password.text.toString().trim(),
                domain.text?.toString()?.trim() ?: ""
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
