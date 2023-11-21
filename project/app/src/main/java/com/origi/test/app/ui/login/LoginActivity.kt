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
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import com.gaming.core.GameSDK
import com.gaming.core.extensions.toast
import com.gaming.core.utils.LogUtils
import com.org.marcus.x.R

import com.org.marcus.x.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private val array = arrayOf("https://game.noradc.com","https://game.ir02sg.com")

    override fun onCreate(savedInstanceState: Bundle?) {
        hideVirtualButton(window)
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //val timing = intent.extras?.getInt("timing")
        //val logger = intent.extras?.getInt("logger")
        //Log.d("LoginActivity", "onCreate: timing=$timing,logger=$logger")

//        val username = binding.username
//        val password = binding.password
//        val login = binding.login
//        val loading = binding.loading
//        val domain: EditText = binding.domain!!
//
//        binding.rgEnv?.setOnCheckedChangeListener { group, int ->
//            Log.d("LoginActivity", "onCreate: $int")
//            when(int) {
//                R.id.rb_br_env -> {
//                    domain.setText(array[0])
//                }
//                R.id.rb_id_env -> {
//                    domain.setText(array[1])
//                }
//            }
//        }

        GameSDK.init(application, true, assets.open("sdk.data.enc").readBytes())

        loginViewModel =
            ViewModelProvider(this, LoginViewModelFactory())[LoginViewModel::class.java]
//
//        loginViewModel.status.observe(this@LoginActivity) {
//            if (it == Invalid) {
//                loading.isVisible = false
//                toast("开关没打开,请检查后台配置~")
//            }
//        }
//
//        login.setOnClickListener {
//            loading.visibility = View.VISIBLE
//            loginViewModel.login(
//                this@LoginActivity,
//                username.text.toString().trim(),
//                password.text.toString().trim(),
//                domain.text?.toString()?.trim()?: ""
//            )
//        }


//        val html = "<!DOCTYPE html>\n" +
//                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
//                "\n" +
//                "<head>\n" +
//                "    <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.6.4/jquery.min.js\"></script>\n" +
//                "    <script>\$(document).ready(function () {\n" +
//                "        console.log(\"加载~\");\n" +
//                "            \$(\"button\").click(function () {\n" +
//                "                console.log(\"点了按钮\");\n" +
//                "                // \$.post(\"https://<运营商后端接口>/api\", { param1: \"value1\", param2: \"value2\" }, function (data) {\n" +
//                "                    var newPage = window.open('', '_blank'); newPage.document.open(); newPage.document.write(\"测试点击结果\");\n" +
//                "                // });\n" +
//                "            });\n" +
//                "        });</script>\n" +
//                "</head>\n" +
//                "\n" +
//                "<body><button>运行游戏</button></body>\n" +
//                "\n" +
//                "</html>"
//        binding.web?.apply {
//            settings.javaScriptEnabled = true
//            settings.domStorageEnabled = true
//            settings.allowFileAccess = true
//            settings.allowUniversalAccessFromFileURLs = true
//            settings.databaseEnabled = true
//            settings.allowContentAccess = true
//            loadDataWithBaseURL(null,html,"text/html","UFT-8",null)
//        }
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
