package com.gaming.core.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import android.webkit.ValueCallback
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.gaming.core.web.CommonGameCallback
import com.gaming.core.pri.ConstPool
import com.gaming.core.web.GamingBridge
import com.gaming.core.utils.LogUtils
import com.gaming.core.compose.CoreGameClient
import com.gaming.core.compose.GameChromeClient
import com.gaming.core.compose.GameContainerComponent
import com.gaming.core.compose.HoverMenuComponent
import com.gaming.core.compose.NavBarComponent
import com.gaming.core.compose.QueryUriComponent
import java.lang.String


@Composable
internal fun SupportActivity.SupportComponent(boxScope: BoxScope, intent: Intent) = boxScope.run {


    val data by rememberUpdatedState(newValue = intent)

    val url by remember { mutableStateOf(data.getStringExtra("u") ?: "") }
    LogUtils.d("~", "url=$url")

    /**
     * 管理图片选择与结果~
     */
    var fileChooseUri by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    //图片选择结果
    val requestResult =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(),
            onResult = { result ->
                LogUtils.d("~", "activity for result: ${result.resultCode}")
                if (result.resultCode == Activity.RESULT_OK) {
                    val dataString = result.data?.dataString ?: ""
                    if (dataString.isNotEmpty()) {
                        val uriArr = arrayOf(Uri.parse(dataString))
                        fileChooseUri?.onReceiveValue(uriArr)
                        return@rememberLauncherForActivityResult
                    }
                    val clipData = result.data?.clipData ?: return@rememberLauncherForActivityResult
                    val uriList = arrayListOf<Uri>()
                    for (i in 0 until clipData.itemCount) {
                        uriList.add(clipData.getItemAt(i).uri)
                    }
                    fileChooseUri?.onReceiveValue(uriList.toTypedArray())
                } else {
                    fileChooseUri?.onReceiveValue(null)
                }
            })

    val client = remember {
        CoreGameClient().apply {
            setOnPageFinishCallback {}
        }
    }

    val chromeClient = remember {
        GameChromeClient().apply {
            setOnFileChooseCallback { valueCallback, fileChooserParams ->
                fileChooseUri = valueCallback
                requestResult.launch(Intent.createChooser(Intent(Intent.ACTION_PICK).apply {
                    type = fileChooserParams?.acceptTypes?.let { arr ->
                        String.join(",", *arr)
                    } ?: "*/*"
                }, "File Choose"))
            }
        }
    }

    var core by remember {
        mutableStateOf<WebView?>(null)
    }

    //控制返回
    var canBack by remember {
        mutableStateOf(false)
    }

    if (canBack) {
        canBack = false
        if (core?.canGoBack() == true) {
            core?.goBack()
        } else {
            this@SupportComponent.finish()
        }
    }

    //控制刷新
    var refresh by remember {
        mutableStateOf(false)
    }

    if (refresh) {
        refresh = false
        core?.reload()
    }

    val bridge = remember { GamingBridge() }.apply {
        setCallback(object : CommonGameCallback() {
            override fun back() {
                canBack = true
            }
            override fun close() {
                this@SupportComponent.finish()
            }
            override fun refresh() {
                refresh = true
            }
        })
    }

    //管理导航栏状态.
    var nav by remember {
        mutableStateOf(false)
    }
    LogUtils.d("~", "showNav=$nav")

    //save count
    var savecout by remember {
        mutableStateOf(false)
    }
    LogUtils.d("~", "mSafeCount=$savecout")

    //屏幕方向状态.
    var orientation by remember {
        mutableStateOf(ConstPool.LANDSCAPE)
    }

    //管理悬浮菜单.通过Url中的地址获取悬浮菜单的配置
    var hover by remember {
        mutableStateOf(false)
    }
    LogUtils.d("~", "showHover=$hover")

    SideEffect {
        LogUtils.d("~", "re-component success")
        if (savecout && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val window = window
            val layoutParams: WindowManager.LayoutParams = window.attributes
            layoutParams.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
            window.attributes = layoutParams
        }
        requestedOrientation = when (orientation) {
            ConstPool.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            ConstPool.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    if (url.isNotEmpty()) {

        //解析配置~
        QueryUriComponent(url) { h, j, k, l ->
            hover = h
            nav = j
            savecout = k
            orientation = l
        }

        if (hover) {
            HoverMenuComponent(init = {}, onClose = {
                this@SupportComponent.finish()
            }, onRefresh = {
                refresh = true
            })
        }

        if (nav) {
            NavBarComponent(this@SupportComponent.window,nav)
        }

        GameContainerComponent(init = {
            core = this
            core?.webChromeClient = chromeClient
            core?.webViewClient = client
            addJavascriptInterface(bridge, data.getStringExtra("s") ?: "")
            loadUrl(url)
        })
    }
}