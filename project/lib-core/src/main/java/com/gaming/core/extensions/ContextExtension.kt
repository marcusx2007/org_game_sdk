@file:Suppress("DEPRECATION")

package com.gaming.core.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.provider.Settings.Secure
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.gaming.core.pri.ConstPool
import com.gaming.core.pri.GamingGlobal
import com.gaming.core.utils.LogUtils
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.NetworkInterface
import java.nio.charset.Charset
import java.util.*


fun Context.appName(): String {
    return try {
        val info = packageManager.getPackageInfo(packageName, 0)
        info.applicationInfo.loadLabel(packageManager).toString()
    } catch (error: Exception) {
        error.printStackTrace()
        ""
    }
}



fun Context.devMode(): Int {
    return Settings.Global.getInt(contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
}

fun Context.simCardStatus(): Int {
    val tm = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    return tm?.simState ?: -1
}

fun Context.data():ByteArray {
    return JSONObject().apply {
        put("chn", GamingGlobal.get().chn())
        put("brd",GamingGlobal.get().brd())
        put("aid", "${aid()}_${packageName.replace(".","_")}")
        put("pkg", packageName)
        put("referrer", referrer())
        put("svc", Build.VERSION.SDK_INT)
        put("svn", Build.VERSION.RELEASE)
        put("platform", "android")
        put("cvc", cvc())
        put("cvn", cvn())
        put("lang",lan())
        put("cgi", cgi())
        put("mcc", mcc())
        put("type", "h5")
        put("rkey", 1)
    }.toString().toByteArray(Charset.defaultCharset())
}

fun Context.cvc(): Int {
    return try {
        val p = this.packageManager.getPackageInfo(packageName, 0)
        p.versionCode
    } catch (error: Exception) {
        error.printStackTrace()
        0
    }
}

fun Context.cvn(): String {
    return try {
        val info = this.packageManager.getPackageInfo(packageName, 0)
        info.versionName
    } catch (error: Exception) {
        error.printStackTrace()
        ""
    }
}

fun Context.lan(): String {
    val defaultLan = ""
    return try {
        val locale: Locale? = this.resources.configuration.locales[0]
        var language = ""
        if (locale != null) {
            language = locale.language
        }
        if (TextUtils.isEmpty(language)) {
            language = Locale.getDefault().language
        }
        if (TextUtils.isEmpty(language)) defaultLan else language.lowercase(Locale.getDefault())
    } catch (error: Exception) {
        error.printStackTrace()
        defaultLan
    }
}

fun Context.cgi(): String {
    return try {
        val telManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val networkCountryCode = telManager.networkCountryIso
        if (TextUtils.isEmpty(networkCountryCode)) "" else networkCountryCode
    } catch (error: Exception) {
        error.printStackTrace()
        ""
    }
}

fun Context.mcc(): String {
    return try {
        val mcc = StringBuilder()
        val subManager =
            this.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val slotCount = subManager.activeSubscriptionInfoCountMax
        for (i in 0 until slotCount) {
            val subscriptionId = this.slotId(i)
            if (subscriptionId != -1) {
                var simCountry = this.simCountryBySlotId(subscriptionId)
                if (simCountry.isEmpty()) {
                    simCountry = this.simCountryByPhoneId(i)
                }
                if (simCountry.isNotEmpty()) {
                    mcc.append(simCountry).append(",")
                }
            }
        }
        mcc.toString()
    } catch (error: Exception) {
        error.printStackTrace()
        ""
    }
}

fun Context.simCountryByPhoneId(index: Int): String {
    val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    return try {
        val clazz = tm::class.java
        val m = clazz.getDeclaredMethod("getSimCountryIsoForPhone", Int::class.java)
        m.isAccessible = true
        m.invoke(tm, index) as? String ?: ""
    } catch (error: Exception) {
        error.printStackTrace()
        ""
    }
}

fun Context.simCountryBySlotId(slotId: Int): String {
    val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    return try {
        val clz = tm::class.java
        val m = clz.getDeclaredMethod("getSimCountryIso", Int::class.java)
        m.isAccessible = true
        val simCode = m.invoke(tm, slotId) as? String
        simCode ?: ""
    } catch (err: Exception) {
        err.printStackTrace()
        ""
    }
}

fun Context.slotId(index: Int): Int {
    return try {
        val manager =
            this.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val clz = manager::class.java
        val m = clz.getDeclaredMethod("getSubId", Int::class.java)
        m.isAccessible = true
        val slotId = m.invoke(manager, index) as? Int
        slotId ?: -1
    } catch (err: Exception) {
        err.printStackTrace()
        -1
    }
}

fun Context.rawResource(name: String): String {
    return try {
        val id = resources.getIdentifier(name, "raw", this.packageName)
        val data = String(resources.openRawResource(id).use {
            it.readBytes()
        }, Charset.defaultCharset())
        return data
    } catch (err: Exception) {
        err.printStackTrace()
        ""
    }
}

fun Context.rawId(name: String): Int {
    return try {
        resources.getIdentifier(name, "raw", this.packageName)
    } catch (error: Exception) {
        error.printStackTrace()
        LogUtils.d("Extension::class", "raw failed: ${error.message}")
        -1
    }
}

fun Context.stringResources(name: String): Int {
    return try {
        resources.getIdentifier(name, "string", this.packageName)
    } catch (error: Exception) {
        error.printStackTrace()
        LogUtils.d("Extension::class", "string failed: ${error.message}")
        -1
    }
}

fun Context.identifier(type: String, name: String): Int {
    return try {
        resources.getIdentifier(name, type, packageName)
    } catch (error: Exception) {
        error.printStackTrace()
        LogUtils.d("Extension::class", "identifier failed: ${error.message}")
        -1
    }
}

fun Context.styleResources(name: String): Int {
    return try {
        resources.getIdentifier(name, "style", this.packageName)
    } catch (error: Exception) {
        error.printStackTrace()
        LogUtils.d("Extension::class", "string failed: ${error.message}")
        -1
    }
}

@DrawableRes
fun Context.mipmapResource(name: String): Int {
    return try {
        return resources.getIdentifier(name, "mipmap", this.packageName)
    } catch (err: Exception) {
        err.printStackTrace()
        -1
    }
}

fun Context.isConnected(): Boolean {
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetworkInfo
    return (activeNetwork != null
            && activeNetwork.isConnected)
}

fun Context.setData(key: String, value: String) {
    this.sharedPreference().edit().putString(key, value).apply()
}

/**
 * sp进行升级.使用sp加密进行储存
 */
fun Context.sharedPreference(): SharedPreferences {
    val alias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    return EncryptedSharedPreferences.create(
        ConstPool.EncryptFileName,
        alias,
        this,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

fun Context.getData(key: String): String {
    return this.sharedPreference().getString(key, "") ?: ""
}


fun Context.getInt(key: String): Int {
    return this.sharedPreference().getInt(key, 0)
}

fun Context.setInt(key: String, value: Int) {
    this.sharedPreference().edit().putInt(key, value).apply()
}

fun Context.all(): Map<String, *> {
    return this.sharedPreference().all
}

fun Context.androidId(): String {
    return Secure.getString(contentResolver, Secure.ANDROID_ID).ifBlank { "" } ?: ""
}

/**
 * 获取Android mac地址
 */
fun Context.macAddress(): String {

    //Android6.0以下获取MAC地址可以直接通过WiFiManager获取.
    val wm = getSystemService(Context.WIFI_SERVICE) as? WifiManager
    val defaultMac = wm?.connectionInfo?.macAddress ?: ""

    //在 Android 6.0（API 级别 23）到 Android 9（API 级别 28）中，
    // 本地设备 MAC 地址（如 WLAN 和蓝牙）无法通过第三方 API 使用。WifiInfo.getMacAddress() 方法
    // 和 BluetoothAdapter.getDefaultAdapter().getAddress() 方法都会返回 02:00:00:00:00:00。

    //https://developer.android.com/training/articles/user-data-ids?hl=zh-cn#mac-11-plus
    if (defaultMac != "02:00:00:00:00:00") {
        return defaultMac
    }

    try {
        var mac = ""
        for (net in NetworkInterface.getNetworkInterfaces()) {
            if (!net.name.equals("wlan0", true)) continue
            val hardBuffer = net.hardwareAddress
            net.inetAddresses.toList().forEach {
                LogUtils.d("Context.macAddress", "inet address: $it")
            }
            if (null != hardBuffer) {
                LogUtils.d("Context.macAddress", "mac address: ${hardBuffer.toHex()}")
                mac = hardBuffer.toHex()
                break
            }
        }

        //如果没有通过网络接口获取到MAC地址, 通过查询系统文件进行获取MAC地址.
        if (mac.isEmpty()) {
            val wlan0 = runtime("cat /sys/class/net/wlan0/address")
            LogUtils.d("Context.macAddress", "wlan0=$wlan0")
            mac = wlan0
        }

        //如果都没有获取到MAC地址, 返回默认MAC地址.
        return mac.ifEmpty { defaultMac }
    } catch (err: Exception) {
        LogUtils.d("mac address", "get failed: ${err.message}")
        return defaultMac
    }
}


fun runtime(cmd: String): String {
    return try {
        val pro = Runtime.getRuntime().exec(cmd)
        val reader = BufferedReader(InputStreamReader(pro.inputStream, "utf-8"))
        var line = ""
        val result = StringBuilder()
        while (reader.readLine()?.also { line = it } != null) {
            result.append(line)
        }
        result.toString()
    } catch (err: Exception) {
        err.printStackTrace()
        LogUtils.d("runtime", "execute failed: ${err.message}")
        err.message ?: ""
    }
}

fun ByteArray.toHex(): String {
    val s = StringBuilder()
    this.forEach {
        s.append(String.format("%02X:", it))
    }
    return s.substring(0, s.lastIndex)
}

fun Context.toast(msg: String) {
    return Toast.makeText(this.applicationContext, msg, Toast.LENGTH_LONG).show()
}


fun Context.isConnect(): Boolean {
    //获取系统的连接服务
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    val info = cm?.activeNetworkInfo
    if (info != null && info.isConnected) {
        // 当前网络是连接的
        if (info.state == NetworkInfo.State.CONNECTED) {
            // 当前所连接的网络可用
            return true
        }
    }
    return false
}


/**
 * 游戏需要的Android System WebView最小版本是：64.0.3282.29 - 328202950
 * 小于该版本，内核会报以下错误：
 * Uncaught SyntaxError: Invalid regular expression: /(?<bundle>.+)UiLanguage/:
 *
 * @return
</bundle> */
val Context.isWebViewCompatible: Boolean
    get() {
        val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
        var compatible = false
        if (packageInfo.versionCode >= 328202950) {
            compatible = true
        }
        return compatible
    }

/**
 * get Google Service Framework id
 *
 * @return gsf id
 */
fun Context.gsfAndroidId(): String? {
    return try {
        val URI = Uri.parse("content://com.google.android.gsf.gservices")
        val ID_KEY = "android_id"
        val params = arrayOf(ID_KEY)
        val c: Cursor = contentResolver?.query(URI, null, null, params, null) ?: return ""
        if (!c.moveToFirst() || c.columnCount < 2) return null
        val id = c.getString(1)
        if (TextUtils.isEmpty(id) || "null" == id) null else java.lang.Long.toHexString(id.toLong())
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        ""
    }
}


/**
 * 在缓存目录下新键子目录
 */
fun Context.getCacheChildDir(child: String?): File {
    val name = if (TextUtils.isEmpty(child)) {
        "app"
    } else {
        child
    }
    val file = File(getExternalOrCacheDir(), name)
    file.mkdirs()
    return file
}

/**
 * getFilesDir和getCacheDir是在手机自带的一块存储区域(internal storage)，通常比较小，SD卡取出也不会影响到，App的sqlite数据库和SharedPreferences都存储在这里。所以这里应该存放特别私密重要的东西。
 *
 * getExternalFilesDir和getExternalCacheDir是在SD卡下(external storage)，在sdcard/Android/data/包名/files和sdcard/Android/data/包名/cache下，会跟随App卸载被删除。
 */
fun Context.getExternalOrCacheDir(): File {
    // 如果获取为空则改为getCacheDir
    val dir = externalCacheDir ?: cacheDir
    if (!dir.exists()) {
        dir.mkdirs()
    }
    return dir
}


fun Activity.onWindowConfiguration() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    requestWindowFeature(Window.FEATURE_NO_TITLE)
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


//get install referrer by build connection
suspend fun Context.installReferrer(): String = withContext(Dispatchers.IO) {
    val cache = this@installReferrer.referrer()
    if (cache != ConstPool.INSTALL_REFERRER_UNKNOWN) {
        return@withContext cache
    }
    val referrerClient = InstallReferrerClient.newBuilder(this@installReferrer).build()
    val deferred = CompletableDeferred<String>()
    referrerClient.startConnection(object : InstallReferrerStateListener {
        override fun onInstallReferrerSetupFinished(respoonCode: Int) {
            if (respoonCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                val response: ReferrerDetails = referrerClient.installReferrer
                val referrer: String =
                    response.installReferrer.ifEmpty { ConstPool.INSTALL_REFERRER_UNKNOWN }
                this@installReferrer.setReferrer(referrer)
                deferred.complete(referrer)
                referrerClient.endConnection()
            } else {
                deferred.complete(ConstPool.INSTALL_REFERRER_UNKNOWN)
            }
        }

        override fun onInstallReferrerServiceDisconnected() {
            deferred.complete(ConstPool.INSTALL_REFERRER_UNKNOWN)
        }
    })
    deferred.await()
}


fun Context.referrer(): String {
    return getSharedPreferences(ConstPool.APP_DATA_SP, Context.MODE_PRIVATE)
        .getString(ConstPool.INSTALL_R_KEY, ConstPool.INSTALL_REFERRER_UNKNOWN)!!
}

fun Context.setReferrer(referrer: String) {
    getSharedPreferences(ConstPool.APP_DATA_SP, Context.MODE_PRIVATE)
        .edit().putString(ConstPool.INSTALL_R_KEY, referrer).apply()
}

// get device id
@SuppressLint("HardwareIds")
suspend fun Context.getMobileId(): String = withContext(Dispatchers.IO) {
    val cache = this@getMobileId.aid()
    if (cache.isNotEmpty()) {
        GamingGlobal.get().setAid(cache)
        return@withContext cache
    }

    val androidId = this@getMobileId.androidId()

    if (androidId.isNotEmpty()) {
        this@getMobileId.setAid(androidId)
        GamingGlobal.get().setAid(androidId)
        return@withContext androidId
    }

    val deferred = CompletableDeferred<String>()
    try {
        val code =
            GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(this@getMobileId)
        if (code == ConnectionResult.SUCCESS) {
            val info = AdvertisingIdClient.getAdvertisingIdInfo(this@getMobileId)
            val aid = info.id?.ifEmpty { UUID.randomUUID().toString() }
            this@getMobileId.setAid(aid!!)
            GamingGlobal.get().setAid(aid)
            deferred.complete(aid)
        }
    } catch (error: Exception) {
        error.printStackTrace()
        deferred.complete(UUID.randomUUID().toString())
    }
    deferred.await()
}


fun Context.aid(): String {
    return getSharedPreferences(ConstPool.APP_DATA_SP, Context.MODE_PRIVATE)
        .getString(ConstPool.DEVICE_ID_KEY, "")!!
}

fun Context.setAid(aid: String) {
    getSharedPreferences(ConstPool.APP_DATA_SP, Context.MODE_PRIVATE)
        .edit().putString(ConstPool.DEVICE_ID_KEY, aid).apply()
}