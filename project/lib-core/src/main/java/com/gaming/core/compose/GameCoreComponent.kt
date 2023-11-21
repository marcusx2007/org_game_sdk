package com.gaming.core.compose

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.MimeTypeMap
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.gaming.core.web.CommonGameCallback
import com.gaming.core.pri.ConstPool
import com.gaming.core.utils.FileUtil
import com.gaming.core.web.GamingBridge
import com.gaming.core.pri.GamingGlobal
import com.gaming.core.ui.GamingMenu
import com.gaming.core.utils.ImageDownloader
import com.gaming.core.utils.LogUtils
import com.gaming.core.utils.ShareUtil
import com.gaming.core.extensions.getData
import com.gaming.core.extensions.isConnect
import com.gaming.core.extensions.isSame
import com.gaming.core.extensions.isWebViewCompatible
import com.gaming.core.qrcode.QrcodeGenerator
import com.gaming.core.extensions.setData
import com.gaming.core.extensions.stringResources
import com.gaming.core.extensions.styleResources
import com.gaming.core.extensions.toast
import com.gaming.core.extensions.triggerScanning
import com.gaming.core.qrcode.AbstractQrConfig
import com.gaming.core.qrcode.AbstractQrcodeCallImpl
import com.gaming.core.qrcode.QrConfigFactory
import com.gaming.core.ui.SupportActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.Locale


@SuppressLint("JavascriptInterface")
@Composable
fun GameCoreComponent(activity: Activity, box: BoxScope, suffix: String, url: String) = box.run {

    //如果reload不为null.那么隐藏大厅.
    var reload by remember {
        mutableStateOf("")
    }

    //加载地址~
    val data by remember {
        mutableStateOf(url)
    }

    //只执行一次的操作.
    var init by remember {
        mutableStateOf(false)
    }

    var webUpdate by remember { mutableStateOf(false) }

    //只执行一次
    if (!init) {
        init = true
        CheckGameComponent(url)
    }

    //只响应一次
    if (webUpdate) {
        WebSystemComponent()
        webUpdate = false
    }

    //记录web,使用web执行相关方法通知h5
    var core by remember {
        mutableStateOf<WebView?>(null)
    }

    val supportRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {}
    )

    if (reload.isNotEmpty()) {
        supportRequest.launch(Intent(activity, SupportActivity::class.java).apply {
            putExtra("u", reload)
            putExtra("s", suffix)
        })
        reload = ""
    }

    //判断网络状态,如果网络错误,显示错误信息页面.
    var isError by remember { mutableStateOf(!activity.isConnect()) }
    LogUtils.d(ConstPool.TAG, "isError=$isError")

    //web client & chrome client
    val client = remember {
        CoreGameClient().apply {
            setOnPageFinishCallback {
                isError = !it
            }
        }
    }
    val chromeClient = remember { GameChromeClient() }

    var backState by remember {
        mutableStateOf(false)
    }

    //两次退出APP
    var clickBackPressTime by remember {
        mutableStateOf(0L)
    }
    if (backState) {
        if (System.currentTimeMillis().minus(clickBackPressTime) > 2000) {
            activity.toast(activity.getString(activity.stringResources("one_more_click_go_back")))
            clickBackPressTime = System.currentTimeMillis()
        } else {
            activity.finish()
        }
        backState = false
    }

    //记录返回建
    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backState = true
            }
        }
    }
    LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher?.addCallback(backCallback)

    //管理saveImage调用状态
    var saveImgDone by remember {
        mutableStateOf(Pair(false, false))
    }
    if (saveImgDone.first) {
        val view = core ?: return
        SaveImageDoneComponent(activity, gameView = view, success = saveImgDone.second)
        saveImgDone = saveImgDone.copy(first = false)
    }

    //管理savePromotionMaterialDone调用状态
    var saveMaterialImg by remember {
        mutableStateOf(Pair(false, false))
    }
    if (saveMaterialImg.first) {
        val view = core ?: return
        SaveMaterialImageDoneComponent(activity, view, saveMaterialImg.second)
        saveMaterialImg = saveMaterialImg.copy(first = false)
    }

    //管理preloadPromotionImageDone调用状态
    var preloadImgDone by remember {
        mutableStateOf(Pair(false, false))
    }
    if (preloadImgDone.first) {
        val view = core ?: return
        PreloadImageDoneComponent(activity, view, preloadImgDone.second)
        preloadImgDone = preloadImgDone.copy(first = false)
    }

    //管理synthesizePromotionImageDone调用状态
    var syncPromotionImg by remember {
        mutableStateOf(Pair(false, false))
    }
    if (syncPromotionImg.first) {
        val view = core ?: return
        SyncPromotionImageComponent(activity, view, syncPromotionImg.second)
        syncPromotionImg = syncPromotionImg.copy(first = false)
    }

    //管理下载图片调用状态.会请权限.
    var imgDownloadState by remember {
        mutableStateOf(Pair(false, ""))
    }
    //请求权限
    val permission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            if (it) {
                //下载图片~
                imgDownloadState = imgDownloadState.copy(first = true)
            } else {
                saveImgDone = saveImgDone.copy(first = true, second = false)
            }
        }
    )
    if (imgDownloadState.first) {
        ImageDownloadComponent(activity, imgDownloadState.second, permission) {
            saveImgDone = saveImgDone.copy(first = true, second = it)
        }
        imgDownloadState = imgDownloadState.copy(first = false)
    }

    //管理二维码调用状态.用于在指定位置生成二维码.
    var qrStatus by remember {
        mutableStateOf(Pair<Boolean, AbstractQrConfig?>(false, null))
    }
    if (qrStatus.first) {
        val config = qrStatus.second ?: return
        QrcodeImageWorkerComponent(true, config = config) {
            syncPromotionImg = syncPromotionImg.copy(true, it)
        }
        qrStatus = qrStatus.copy(first = false)
    }

    //桥接方式.
    val jsBridge = remember {
        GamingBridge().apply {
            setCallback(object : CommonGameCallback() {
                override fun open(url: String) {
                    isError = !activity.isConnect()
                    reload = url.convertUrls("jsb" to "$suffix.post")
                }

                override fun back() {
                    backState = true
                }

                override fun close() {
                    activity.finish()
                }

                override fun refresh() {
                    core?.reload()
                }

                override fun clear() {
                    core?.clearCache(true)
                }

                override fun saveImage(url: String?) {
                    if (url.isNullOrBlank()) {
                        saveImgDone = saveImgDone.copy(first = true, second = false)
                        return
                    }
                    imgDownloadState = imgDownloadState.copy(true, url)
                }

                override fun saveImageDone(succeed: Boolean) {
                    saveImgDone = saveImgDone.copy(true, succeed)
                }

                override fun savePromotionMaterialDone(succeed: Boolean) {
                    saveMaterialImg = saveMaterialImg.copy(true, succeed)
                }

                override fun synthesizePromotionImageDone(succeed: Boolean) {
                    syncPromotionImg = syncPromotionImg.copy(true, succeed)
                }

                override fun preloadPromotionImageDone(succeed: Boolean) {
                    preloadImgDone = preloadImgDone.copy(true, succeed)
                }

                override fun synthesizePromotionImage(
                    qrCodeUrl: String?,
                    size: Int,
                    x: Int,
                    y: Int
                ) {
                    if (qrCodeUrl.isNullOrBlank()) return
                    qrStatus = qrStatus.copy(true, QrConfigFactory.create(x, y, size, qrCodeUrl))
                }
            })
        }
    }
    LogUtils.d(ConstPool.TAG, "jsBridge=$jsBridge")

    var firstRegister by remember {
        mutableStateOf(false)
    }
    //网络广播
    val networkReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (ConnectivityManager.CONNECTIVITY_ACTION == intent?.action) {
                    if (firstRegister) {
                        isError = !activity.isConnect()
                        LogUtils.d(ConstPool.TAG, "internet status:$isError")
                    }
                    firstRegister = true
                }
            }
        }
    }

    //生命周期观察者
    LifecycleComponent(doOnCreate = {
        LogUtils.d(ConstPool.TAG, "do on create ->")
        activity.registerReceiver(
            networkReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }, doOnDestroy = {
        LogUtils.d(ConstPool.TAG, "do on destroy ->")
        activity.unregisterReceiver(networkReceiver)
    }, doOnResume = {
        LogUtils.d(ConstPool.TAG, "do on resume ->")
    })

    if (isError) {
        //显示错误视图~
        LogUtils.d(ConstPool.TAG, "do on error ->")
        Column(
            modifier = Modifier
                .background(Color.White)
        ) {
            ComposeText(
                text = stringResource(id = activity.stringResources("refresh")),
                Modifier.clickable {
                    isError = !activity.isConnect()
                })
        }
    }

    if (data.isNotEmpty()) {
        //记录webView
        GameContainerComponent(init = {
            core = this
            LogUtils.d(ConstPool.TAG, "init game container.")
            core?.addJavascriptInterface(jsBridge, suffix)
            webViewClient = client
            webChromeClient = chromeClient
            //执行web版本的检测,是否弹出WebView的系统更新弹框
            core?.evaluateJavascript(ConstPool.WGB_SCRIPT) {
                if ("false" == it || activity.isWebViewCompatible) {
                    LogUtils.d(ConstPool.TAG, "need update web kernel.")
                    webUpdate = true
                }
            }
            core?.loadUrl(data)
        }, update = {
            LogUtils.d(ConstPool.TAG, "update game container.")
            if (it.url != data) {
                it.loadUrl(data)
            }
        })
    }
}


@Composable
fun QrcodeImageWorkerComponent(
    needQrWork: Boolean,
    config: AbstractQrConfig,
    result: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    LogUtils.d(ConstPool.TAG, "QrcodeImageWorkerComponent: $config, $needQrWork")
    SideEffect {
        if (needQrWork) {
            LogUtils.d(ConstPool.TAG, "QrcodeImageWorkerComponent start launch")
            scope.launch(Dispatchers.Main + SupervisorJob()) {
                val qrcodeGenerator = QrcodeGenerator()
                val success = qrcodeGenerator.create(object : AbstractQrcodeCallImpl(config){})
                result.invoke(success)
            }
        }
    }
}

@Composable
fun ImageDownloadComponent(
    activity: Activity,
    url: String,
    launcher: ManagedActivityResultLauncher<String, Boolean>,
    invocation: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    DisposableEffect(key1 = 1, effect = {
        val download = suspend {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            FileUtil.ensureDirectory(dir)
            val status = ImageDownloader.downloadImage(url, dir.absolutePath)
            if (status.success) {
                val succeed = (status.file?.length() ?: 0) > 0
                if (succeed) {
                    status.file?.triggerScanning()
                }
                invocation.invoke(succeed)
            } else {
                invocation.invoke(false)
            }
        }
        scope.launch(Dispatchers.Main) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                download()
            } else {
                val permission = ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    download()
                } else {
                    launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }
        onDispose {
            scope.cancel()
        }
    })
}

@Composable
fun NavBarComponent(window: Window, show: Boolean) {
    LogUtils.d(ConstPool.TAG, "nav-component:$show")
    //显示na
    if (show) {
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    } else {
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
}

@Composable
fun SaveImageDoneComponent(activity: Activity, gameView: WebView, success: Boolean) {
    LogUtils.d(ConstPool.TAG, "SaveImageDoneComponent,$gameView,succ=$success")
    activity.runOnUiThread {
        val script =
            if (success) "Listener.send('SAVE_IMAGE_SUCCEED');" else "Listener.send('SAVE_IMAGE_FAILED');"
        gameView.evaluateJavascript(script, null)
    }
}

@Composable
fun SaveMaterialImageDoneComponent(activity: Activity, gameView: WebView, success: Boolean) {
    LogUtils.d(ConstPool.TAG, "SaveMaterialImageDoneComponent,$gameView,succ=$success")
    activity.runOnUiThread {
        val script =
            if (success) "Listener.send('SAVE_PROMOTION_MATERIAL_SUCCEED');" else "Listener.send('SAVE_PROMOTION_MATERIAL_FAILED');"
        gameView.evaluateJavascript(script, null)
    }
}

@Composable
fun PreloadImageDoneComponent(activity: Activity, gameView: WebView, success: Boolean) {
    LogUtils.d(ConstPool.TAG, "PreloadImageDoneComponent,$gameView,succ=$success")
    activity.runOnUiThread {
        val script =
            if (success) "Listener.send('PRELOAD_PROMOTION_IMAGE_SUCCEED');" else "Listener.send('PRELOAD_PROMOTION_IMAGE_FAILED');"
        gameView.evaluateJavascript(script, null)
    }
}


@Composable
fun SyncPromotionImageComponent(activity: Activity, gameView: WebView, success: Boolean) {
    activity.runOnUiThread {
        LogUtils.d(ConstPool.TAG, "SyncPromotionImageComponent")
        val script =
            if (success) "Listener.send('SYNTHESIZE_PROMOTION_IMAGE_SUCCEED');" else "Listener.send('SYNTHESIZE_PROMOTION_IMAGE_FAILED');"
        gameView.evaluateJavascript(script, null)
    }
}


/**
 * 链接带参数转换
 */
fun String?.convertUrls(vararg params: Pair<String, String>): String {
    this ?: return ""
    var uri: Uri = Uri.parse(this)
    var buildUpon = uri.buildUpon()
    params.forEach { (key, value) ->
        if (uri.getQueryParameters(key).size == 0 && value.isNotEmpty()) {
            buildUpon.appendQueryParameter(key, value)
            uri = Uri.parse(buildUpon.build().toString())
            buildUpon = uri.buildUpon()
        }
    }
    return buildUpon.build().toString()
}


@Composable
fun CheckGameComponent(gameUrl: String) {
    val chn = GamingGlobal.get().chn()
    val brd = GamingGlobal.get().brd()
    if (TextUtils.isEmpty(chn) && TextUtils.isEmpty(brd)) { //特征值为空，不符合游戏特征(判定为不是我们自己的游戏)
        //当前加载的url跟缓存的url一样，并且不符合游戏特征，则清除掉缓存url
        val cacheGameUrl = GamingGlobal.get().application().getData(ConstPool.USER_DOMAIN)
        if (gameUrl.isSame(cacheGameUrl)) {
            GamingGlobal.get().application().setData(ConstPool.USER_DOMAIN, "")
        } else {
            LogUtils.d(ConstPool.TAG, "check game feature: abort clearing cached url: $cacheGameUrl")
        }
    } else {
        LogUtils.d(ConstPool.TAG, "check game feature: is validate game page: $gameUrl")
    }
}

@Composable
fun QueryUriComponent(
    url: String,
    invocation: (Boolean, Boolean, Boolean, String) -> Unit
) {
    val uri = Uri.parse(url)
    val hover = uri.getBooleanQueryParameter(ConstPool.QUERY_PARAM_HOVER_MENU, false)
    val navbar = uri.getBooleanQueryParameter(ConstPool.QUERY_PARAM_NAV_BAR, false)
    val saveCount = uri.getBooleanQueryParameter(ConstPool.QUERY_PARAM_SAFE_CUTOUT, false)
    val orientation = uri.getQueryParameter(ConstPool.QUERY_PARAM_ORIENTATION)
    invocation.invoke(hover, navbar, saveCount, orientation ?: ConstPool.LANDSCAPE)
}


@Composable
fun LifecycleComponent(
    doOnCreate: () -> Unit = {}, doOnDestroy: () -> Unit = {},
    doOnResume: () -> Unit
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(key1 = lifecycle, effect = {
        val observer = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
                doOnCreate.invoke()
            }

            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                doOnResume.invoke()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                doOnDestroy.invoke()
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    })
}


@Composable
fun WebSystemComponent() {
    if (GamingGlobal.get().application().getData(ConstPool.WEB_UPDATE).isNotEmpty()) {
        return
    }
    val ctx = GamingGlobal.get().application()
    val builder = AlertDialog.Builder(LocalContext.current, ctx.styleResources("GamingDialog"))
    builder.setMessage(ctx.stringResources("msg_update_android_system_webview"))
    builder.setPositiveButton(
        ctx.stringResources("btn_confirm")
    ) { d: DialogInterface?, _: Int ->
        d?.dismiss()
        ShareUtil.openMarket(ctx, "com.google.android.webview")
    }
    builder.setNegativeButton(
        ctx.stringResources("btn_cancel")
    ) { d: DialogInterface?, _: Int -> d?.dismiss() }
    builder.setNeutralButton(
        ctx.stringResources("btn_never_ask")
    ) { d: DialogInterface?, _: Int ->
        d?.dismiss()
        GamingGlobal.get().application().setData(ConstPool.WEB_UPDATE, "1")
    }
    builder.create().show()
}


@Composable
fun HoverMenuComponent(
    init: (GamingMenu) -> Unit,
    onRefresh: () -> Unit,
    onClose: () -> Unit
) {
    GamingMenu(LocalContext.current).apply {
        setMenuListener(object : GamingMenu.OnMenuClickListener {
            override fun onRefresh() {
                onRefresh.invoke()
            }

            override fun onClose() {
                onClose.invoke()
            }
        })
        init(this)
    }.show()
}

@Composable
fun GameContainerComponent(
    init: WebView.() -> Unit,
    update: (WebView) -> Unit = {},
) {
    AndroidView(
        factory = { context ->
            val coreView = WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.defaultTextEncodingName = "utf-8"
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowContentAccess = true
                settings.allowFileAccess = true
                settings.allowFileAccessFromFileURLs = true
                settings.allowUniversalAccessFromFileURLs = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.databaseEnabled = true
                settings.useWideViewPort = true //支持自动适配
                settings.loadWithOverviewMode = true
                settings.javaScriptCanOpenWindowsAutomatically = true //支持通过js打开新窗口
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    settings.safeBrowsingEnabled = false
                }
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            coreView.init()
            return@AndroidView coreView
        }, modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.Transparent),
        update = {
            LogUtils.d(ConstPool.TAG, "game container update called.")
            update(it)
        }
    )
}


open class CoreGameClient : WebViewClient() {

    //是否出现加载错误~
    private var mSuccess = false

    private var mCallback: ((Boolean) -> Unit)? = null

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        LogUtils.d(ConstPool.TAG, "shouldInterceptRequest:${request?.url}")
        val url = request?.url ?: return super.shouldInterceptRequest(view, request)
        if (isCocosEngineUrl(url)) {
            val fileName = GamingGlobal.get().engine()
            LogUtils.d("shouldInterceptRequest", "url=$url,fileName=$fileName")
            return url.toWebResponse(GamingGlobal.get().application().assets.open(fileName))
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
        val uri = Uri.parse(url)
        LogUtils.d(ConstPool.TAG, "shouldInterceptRequest:${url}")
        if (isCocosEngineUrl(uri)) {
            val filaName = GamingGlobal.get().engine()
            LogUtils.d(ConstPool.TAG, "shouldInterceptRequest:${filaName}")
            return uri.toWebResponse(GamingGlobal.get().application().assets.open(filaName))
        }
        return super.shouldInterceptRequest(view, url)
    }

    private fun Uri.toWebResponse(`in`: InputStream): WebResourceResponse {
        return WebResourceResponse(
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(this.path),
            "utf-8", `in`
        )
    }

    private fun isCocosEngineUrl(uri: Uri): Boolean {
        val path = uri.path
        return !TextUtils.isEmpty(path) && path!!.contains(ConstPool.JSFile)
    }


    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        mSuccess = false
        LogUtils.d(
            ConstPool.TAG,
            "onReceivedError:${request?.url},code=${error?.errorCode},msg=${error?.description}"
        )
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        LogUtils.d(ConstPool.TAG, "shouldOverrideUrlLoading:${request?.url}")
        return super.shouldOverrideUrlLoading(view, request)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        mSuccess = true
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        mSuccess = !(view.title?.ifBlank { "" }!!.isError())
        mCallback?.invoke(mSuccess)
        LogUtils.d(ConstPool.TAG, "onPageFinished:$url , mSuccess=$mSuccess")
    }

    fun setOnPageFinishCallback(callback: (Boolean) -> Unit) {
        this.mCallback = callback
    }

}


open class GameChromeClient : WebChromeClient() {

    private var callback: ((ValueCallback<Array<Uri>>?, FileChooserParams?) -> Unit)? = null

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        super.onReceivedIcon(view, icon)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        callback?.invoke(filePathCallback, fileChooserParams)
        return true
    }

    fun setOnFileChooseCallback(callback: (ValueCallback<Array<Uri>>?, FileChooserParams?) -> Unit) {
        this.callback = callback
    }
}


fun String.isError(): Boolean {
    if (!TextUtils.isEmpty(this) && (this.contains("404") || this.contains("500") || this.lowercase(
            Locale.getDefault()
        ).contains("error"))
    ) {
        return true
    }
    return false
}