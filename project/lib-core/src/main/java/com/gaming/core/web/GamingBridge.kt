package com.gaming.core.web

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.app.ActivityManagerCompat
import com.gaming.core.applications.CoreApplication
import com.gaming.core.pri.GamingGlobal
import com.gaming.core.pri.ConstPool.DIR_IMAGES
import com.gaming.core.pri.ConstPool.PROMOTION_MATERIAL_FILENAME
import com.gaming.core.pri.ConstPool.PROMOTION_SHARE_FILENAME
import com.gaming.core.extensions.all
import com.gaming.core.extensions.appName
import com.gaming.core.extensions.cvc
import com.gaming.core.extensions.getData
import com.gaming.core.extensions.gsfAndroidId
import com.gaming.core.extensions.macAddress
import com.gaming.core.extensions.setData
import com.gaming.core.utils.ExtraInfoReader
import com.gaming.core.utils.FileUtil
import com.gaming.core.utils.ImageDownloader
import com.gaming.core.utils.LogUtils
import com.gaming.core.utils.ShareUtil
import com.gaming.core.analysi.AdjustManager
import com.gaming.core.analysi.ThinkingDataManager
import com.gaming.core.extensions.aid
import com.gaming.core.pri.ConstPool
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.File

internal class GamingBridge {

    private var mCallback: GameCallback? = null

    private val mBridgeVersion: Int = 7
    private var userId = ""
    private var initialSDK = false

    fun setCallback(mJsBridgeCb: GameCallback) {
        this.mCallback = mJsBridgeCb
    }

    private fun setCocosData(key: String, value: String) {
        if (TextUtils.isEmpty(key)) {
            return
        }
        GamingGlobal.get().application().setData(key, value)

        if (ConstPool.GAMING_USER_ID == key) {
            userId = value
        }

        if (userId.isEmpty()) {
            userId = GamingGlobal.get().application().getData(ConstPool.GAMING_USER_ID)
        }

        //获取到用户ID, 对SDK进行初始化.
        if (userId.isNotEmpty()) {
            if (!initialSDK) {
                initialSDK = true
                AdjustManager.get().setup(
                    GamingGlobal.get().chn(),
                    GamingGlobal.get().brd(),
                    GamingGlobal.get().target(),
                    userId,
                    GamingGlobal.get().aid()
                )
                //关联用户, 上报 start, end , install事件.
                ThinkingDataManager.get()
                    .setup(userId, GamingGlobal.get().target(), ConstPool.BUILD_VERSION)
            }
        }
    }

    private fun savePromotionMaterial(materialUrl: String?) = MainScope().launch {
        val context: Context = GamingGlobal.get().application()
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (null != dir && !dir.exists()) {
            dir.mkdirs()
        }
        val status = ImageDownloader.downloadImage(materialUrl, dir?.absolutePath!!)
        if (status.success) {
            val fileName = String.format(
                PROMOTION_MATERIAL_FILENAME,
                GamingGlobal.get().application().packageName, GamingGlobal.get().chn()
            )
            val destFile = File(dir, fileName)
            val succeed = status.file?.renameTo(destFile)
            LogUtils.d(TAG, " download succeed = $succeed")
            mCallback?.savePromotionMaterialDone(succeed ?: false)
        } else {
            LogUtils.d(TAG, "download failed")
            if (mCallback != null) {
                mCallback!!.savePromotionMaterialDone(false)
            }
        }
    }

    private fun preloadPromotionImage(imageUrl: String?) = MainScope().launch {
        val context: Context = GamingGlobal.get().application()
        val imagePath = File(context.filesDir, DIR_IMAGES)
        FileUtil.ensureDirectory(imagePath)
        val status = ImageDownloader.downloadImage(imageUrl, imagePath.absolutePath)
        if (status.success) {
            val destFile = File(imagePath, PROMOTION_SHARE_FILENAME)
            val succeed = status.file?.renameTo(destFile)
            LogUtils.d(TAG, "download succeed = $succeed")
            if (mCallback != null) {
                mCallback!!.preloadPromotionImageDone(succeed ?: false)
            }
        } else {
            LogUtils.d(TAG, "download failed")
            if (mCallback != null) {
                mCallback!!.preloadPromotionImageDone(false)
            }
        }
    }

    /**
     * 新版本桥接方法定义.
     * request={"methodName":"setCocosData","parameters":["_int_2195730_promotion_guild_new","1"]}
     */
    @JavascriptInterface
    fun post(request: String): String {
        try {

            val json = JSONObject(request)
            val args = json.optJSONArray("parameters")
            LogUtils.d("post", "request=$request")

            when (json.optString("methodName")) {
                "getBrand" -> {
                    val brd = GamingGlobal.get().brd()
                    LogUtils.d("", "getBrand: $brd")
                    return brd
                }

                "getChannel" -> {
                    val chn = GamingGlobal.get().chn()
                    LogUtils.d("", "getChannel: $chn")
                    return chn
                }

                "getAppName" -> {
                    val appName = GamingGlobal.get().application().appName()
                    LogUtils.d("", "getAppName: $appName")
                    return appName
                }

                "getBridgeVersion" -> {
                    LogUtils.d("", "getBridgeVersion: $mBridgeVersion")
                    return "$mBridgeVersion"
                }

                "getDeviceID" -> {
                    val deviceId = GamingGlobal.get().application().aid()
                    LogUtils.d("", "getDeviceID: $deviceId")
                    return deviceId
                }

                "getPackageName" -> {
                    val pkg = GamingGlobal.get().application().packageName
                    LogUtils.d("", "getPackageName: $pkg")
                    return pkg
                }

                "getSystemVersionCode" -> {
                    val svc = Build.VERSION.SDK_INT
                    LogUtils.d("", "getSystemVersionCode: $svc")
                    return "$svc"
                }

                "getClientVersionCode" -> {
                    val cvc = GamingGlobal.get().application().cvc()
                    LogUtils.d("", "getClientVersionCode: $cvc")
                    return "$cvc"
                }

                "getAccountInfo" -> {
                    val info = GamingGlobal.get().application().getData("us-dta-0")
                    LogUtils.d("", "getAccountInfo: $info")
                    return info
                }

                "getGoogleADID" -> {
                    LogUtils.d("", "getGoogleADID: ${AdjustManager.get().googleAdid}")
                    return AdjustManager.get().googleAdid
                }

                "getAdjustDeviceID" -> {
                    val adjustDeviceId = AdjustManager.get().adId()
                    LogUtils.d("", "getAdjustDeviceID: $adjustDeviceId")
                    return adjustDeviceId
                }

                "getReferID" -> {
                    val referId = ExtraInfoReader[FileUtil.selfApkFile]?.referID ?: ""
                    LogUtils.d("", "getReferID: $referId")
                    return referId
                }

                "getAgentId" -> {
                    val agentId = ExtraInfoReader[FileUtil.selfApkFile]?.agentID ?: ""
                    LogUtils.d("", "getAgentId: $agentId")
                    return agentId
                }

                "getTDTargetCountry" -> {
                    val tdTarget = GamingGlobal.get().target()
                    LogUtils.d("", "getTDTargetCountry: $tdTarget")
                    return tdTarget
                }

                "getCocosAllData" -> {
                    val map: Map<String, *> = GamingGlobal.get().application().all()
                    val obj = JSONObject(map)
                    LogUtils.d("", "getCocosAllData: $obj")
                    return obj.toString()
                }

                "getBuildVersion" -> {
                    val buildVer = ConstPool.BUILD_VERSION
                    LogUtils.d("", "getBuildVersion: $buildVer")
                    return buildVer
                }

                "getLighterHost" -> {
                    return ""
                }

                "getCopiedText" -> {
                    val manager = GamingGlobal.get().application()
                        .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    if (manager.hasPrimaryClip() && manager.primaryClip!!.itemCount > 0) {
                        val clipData: ClipData.Item = manager.primaryClip!!.getItemAt(0)
                        val text = if (TextUtils.isEmpty(clipData.text)) "" else clipData.text
                            .toString()
                        LogUtils.d("","getCopiedText: $text")
                        return text
                    }
                }

                "showNativeToast" -> {
                    val data = args?.get(0)
                    Toast.makeText(GamingGlobal.get().application(), "$data", Toast.LENGTH_SHORT)
                        .show()
                }

                "initAdjustID" -> {
                    val adjustAppID = args?.get(0)
                    if (TextUtils.isEmpty("$adjustAppID")) {
                        return ""
                    }
                    val adjustAppIDCache: String = GamingGlobal.get().adjustId()
                    if (TextUtils.equals("$adjustAppID", adjustAppIDCache)) {
                        return ""
                    }
                    AdjustManager.get()
                        .update(
                            GamingGlobal.get().application(),
                            GamingGlobal.get().debug(),
                            "$adjustAppID"
                        )
                }

                "trackAdjustEvent" -> {
                    val eventToken = "${args?.get(0)}"
                    val jsonData = "${args?.get(1)}"
                    var jsonObj: JSONObject? = null
                    try {
                        jsonObj = JSONObject(jsonData)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    val s2sParams = HashMap<String, String>()
                    if (jsonObj != null) {
                        val keys = jsonObj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val value = jsonObj.optString(key)
                            s2sParams[key] = value
                        }
                    }
                    AdjustManager.get().trackEvent(eventToken, s2sParams)
                }

                "trackAdjustEventStart" -> {
                    val event = args?.get(0)?.toString()?.replace("undefined", "") ?: ""
                    AdjustManager.get()
                        .trackEventStart(event.ifEmpty { GamingGlobal.get().start() })
                }

                "trackAdjustEventGreeting" -> {
                    val event = args?.get(0)?.toString()?.replace("undefined", "") ?: ""
                    AdjustManager.get()
                        .trackEventGreeting(event.ifEmpty { GamingGlobal.get().greeting() })
                }

                "trackAdjustEventAccess" -> {
                    val event = args?.get(0)?.toString()?.replace("undefined", "") ?: ""
                    AdjustManager.get()
                        .trackEventAccess(event.ifEmpty { GamingGlobal.get().access() })
                }

                "trackAdjustEventUpdated" -> {
                    val event = args?.get(0)?.toString()?.replace("undefined", "") ?: ""
                    AdjustManager.get()
                        .trackEventUpdate(event.ifEmpty { GamingGlobal.get().update() })
                }

                "saveGameUrl" -> {
                    val gameUrl = "${args?.get(0)}"
                    if (!URLUtil.isValidUrl(gameUrl)) {
                        return ""
                    }
                    GamingGlobal.get().application().setData(ConstPool.USER_DOMAIN, gameUrl)
                }

                "saveAccountInfo" -> {
                    val plainText = "${args?.get(0)}"
                    if (TextUtils.isEmpty(plainText)) {
                        return ""
                    }
                    GamingGlobal.get().application().setData("us-dta-0", plainText)
                }

                "getCocosData" -> {
                    val key = "${args?.get(0)}"
                    val value = if (key.isEmpty()) "" else GamingGlobal.get().application().getData(key)
                    LogUtils.d("","getCocosData: $value")
                    return value
                }

                "openUrlByBrowser" -> {
                    try {
                        val url = "${args?.get(0)}"
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        LogUtils.d("","openUrlByBrowser: $url")
                        GamingGlobal.get().application().startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                "openUrlByWebView" -> {
                    val url = "${args?.get(0)}"
                    if (url.isEmpty() || url.isBlank()) return ""
                    mCallback?.open(url)
                }

                "openApp" -> {
                    val target = "${args?.get(0)}"
                    val fallbackUrl = "${args?.get(1)}"
                    try {
                        GamingGlobal.get().application()
                            .startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target)))
                    } catch (error: Exception) {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(fallbackUrl)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        GamingGlobal.get().application().startActivity(intent)
                    }
                }

                "loadUrl" -> {
                    val url = "${args?.get(0)}"
                    LogUtils.d("", "loadUrl:$url")
                    if (url.isEmpty() || url.isBlank()) return ""
                    mCallback?.open(url)
                }

                "copyText" -> {
                    val value = args?.get(0)
                    if (TextUtils.isEmpty("$value")) {
                        return ""
                    }
                    val manager = GamingGlobal.get().application()
                        .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    manager.setPrimaryClip(ClipData.newPlainText(null, "$value"))
                }

                "goBack" -> {
                    mCallback?.back()
                }

                "close" -> {
                    mCallback?.close()
                }

                "refresh" -> {
                    mCallback?.refresh()
                }

                "clearCache" -> {
                    mCallback?.clear()
                }

                "nativeLog" -> {
                    var tag = "${args?.get(0)}"
                    if (TextUtils.isEmpty(tag)) {
                        tag = TAG
                    }
                    Log.d(tag, "${args?.get(1)}")
                }

                "commonData" -> {
                    val data = JSONObject()
                    data.put("mac", GamingGlobal.get().application().macAddress())
                    //这个值之前可能存在,随着框架的迭代可能会移除了,传递获取到的值就行了.
                    data.put("gsf_id", GamingGlobal.get().application().gsfAndroidId())
                    LogUtils.d("", "commonData: $data")
                    return data.toString()
                }

                "memoryInfo" -> {
                    val context: Context = GamingGlobal.get().application()
                    val result =
                        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    val mi = ActivityManager.MemoryInfo()
                    result.getMemoryInfo(mi)
                    val data = JSONObject()
                    data.put("DeviceTotalMem", mi.totalMem) //设备实际最大内存
                    data.put("DeviceAvailMem", mi.availMem) //设备可用内存
                    data.put(
                        "isLowMemDevice",
                        ActivityManagerCompat.isLowRamDevice(result)
                    ) //是否低内存设备
                    val r = Runtime.getRuntime()
                    data.put("AppTotalMemory", r.totalMemory()) //App最大可用内存
                    data.put("AppMaxMemory", r.maxMemory()) //App当前可用内存
                    data.put("AppFreeMemory", r.freeMemory()) //App当前空闲内存
                    LogUtils.d("", "getMemInfo: $data")
                    return data.toString()
                }

                "shareToWhatsApp" -> {
                    val value = args?.get(0)
                    val context: Context = GamingGlobal.get().application()
                    val imagePath = File(context.filesDir, DIR_IMAGES)
                    val destFile = File(imagePath, PROMOTION_SHARE_FILENAME)
                    LogUtils.d(
                        "",
                        "shareToWhatsApp: $value , destFile=$destFile,file-length=${destFile.length()}"
                    )
                    GamingGlobal.get().application().let {
                        ShareUtil.shareToWhatsApp(it, "$value", destFile)
                    }
                }

                "shareUrl" -> {
                    val value = args?.get(0)
                    LogUtils.d("", "shareUrl: $value")
                    ShareUtil.sendText(GamingGlobal.get().application(), "$value")
                }

                "isFacebookEnable", "isHttpDnsEnable" -> {
                    return "false"
                }

                "saveImage" -> {
                    val value = args?.get(0)
                    mCallback?.saveImage("$value")
                    return ""
                }

                "savePromotionMaterial" -> {
                    val value = args?.get(0)
                    savePromotionMaterial("$value")
                    return ""
                }

                "preloadPromotionImage" -> {
                    val value = args?.get(0)
                    preloadPromotionImage("$value")
                    return ""
                }

                "synthesizePromotionImage" -> {
                    val qrUrl = args?.get(0)?.toString()
                    val size = args?.get(1)?.toString()?.toInt() ?: 0
                    val x = args?.get(2)?.toString()?.toInt() ?: 0
                    val y = args?.get(3)?.toString()?.toInt() ?: 0
                    mCallback?.synthesizePromotionImage(qrUrl, size, x, y)
                    return ""
                }

                "setCocosData" -> {
                    val (key, value) = Pair(args?.get(0), args?.get(1))
                    LogUtils.d("", "setCocosData: $key,$value")
                    setCocosData("$key", "$value")
                    return ""
                }
            }
        } catch (error: Exception) {
            error.printStackTrace()
            LogUtils.d("post", "error:${error.message}")
        }
        return ""
    }

    companion object {
        val TAG: String = GamingBridge::class.java.simpleName
    }
}