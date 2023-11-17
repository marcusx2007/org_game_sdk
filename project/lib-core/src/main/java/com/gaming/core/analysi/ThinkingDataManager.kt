package com.gaming.core.analysi

import android.content.Context
import cn.thinkingdata.android.ThinkingAnalyticsSDK
import com.gaming.core.utils.LogUtils
import org.json.JSONObject

internal class ThinkingDataManager {

    private val tag = "ThinkingDataManager"
    private lateinit var sdk: ThinkingAnalyticsSDK
    private var accountId = ""
    private var targetCountry = ""

    companion object {

        @JvmStatic
        private var instance: ThinkingDataManager? = null

        @JvmStatic
        fun get(): ThinkingDataManager {
            if (null == instance) {
                instance = ThinkingDataManager()
            }
            return instance!!
        }
    }

    /**
     * 是否初始化.
     */
    private var initial = false

    /**
     * 初始化方法
     */
    fun init(context: Context, appId: String, serverUrl: String = "") {
        LogUtils.d(tag, " > init $appId, serverUrl $serverUrl")
        if (!initial) {
            initial = true
            try {
                ThinkingAnalyticsSDK.calibrateTimeWithNtp("time.apple.com")
                sdk = ThinkingAnalyticsSDK.sharedInstance(context, appId, serverUrl)
                LogUtils.d(tag, " > sdk init success...")
            } catch (error: Exception) {
                error.printStackTrace()
                LogUtils.d(tag, " > init failed ${error.message}")
            }
        } else {
            LogUtils.d(tag, " > thinking data already init...")
        }
    }


    fun distinctId(): String {
        if (!initial) return ""
        val tdDistinctID = sdk.distinctId
        LogUtils.d(tag, "distinctId=$tdDistinctID")
        return tdDistinctID
    }

    fun tdDeviceId(): String {
        if (!initial) return ""
        val tdDeviceId = sdk.deviceId
        LogUtils.d(tag, "tdDeviceId=$tdDeviceId")
        return tdDeviceId
    }

    /**
     * 初始化相关用户信息. 在webView加载主游戏的时候调用.
     */
    fun setup(accountId: String, targetCountry: String, buildVersion: String) {
        this.accountId = accountId
        this.targetCountry = targetCountry.uppercase()
        LogUtils.d(
            tag,
            " > setup accountId=${this.accountId},targetCountry=${this.targetCountry},buildVersion=$buildVersion"
        )
        if (initial && this::sdk.isInitialized) {
            try {
                val properties = JSONObject().apply {
                    put("region", targetCountry.uppercase())
                    put("build_version", buildVersion)
                }
                ThinkingAnalyticsSDK.enableTrackLog(true)
                sdk.enableAutoTrack(
                    arrayListOf(
                        ThinkingAnalyticsSDK.AutoTrackEventType.APP_INSTALL,
                        ThinkingAnalyticsSDK.AutoTrackEventType.APP_START,
                        ThinkingAnalyticsSDK.AutoTrackEventType.APP_END
                    ), properties
                )
                val tdUser = "$targetCountry-$accountId"
                LogUtils.d(tag, " > sdk setup success...TD UserId=$tdUser")
                sdk.login(tdUser)
                LogUtils.d(tag, " > thinking data sdk login success $accountId")
            } catch (error: Exception) {
                error.printStackTrace()
                LogUtils.d(tag, " > setup ${error.message}")
            }
        } else {
            LogUtils.d(tag, " > set failed. $initial, ${this::sdk.isInitialized}")
        }
    }

    /**
     * 获取TargetCountry
     */
    fun getTargetCountry(): String {
        LogUtils.d(tag, " > getTargetCountry target=$targetCountry")
        return this.targetCountry
    }

    /**
     * 上报事件
     */
    fun trackEvent(name: String, vararg param: Pair<String, Any>) {
        if (initial && this::sdk.isInitialized) {
            val json = JSONObject()
            param.forEach { json.put(it.first, it.second) }
            trackEvent(name, json)
        } else {
            LogUtils.d(tag, " > track failed. $initial, ${this::sdk.isInitialized}")
        }
    }

    /**
     * 上报事件
     */
    private fun trackEvent(name: String, json: JSONObject) {
        if (initial && this::sdk.isInitialized) {
            LogUtils.d(tag, " > track params $json")
            sdk.track(name, json)
            LogUtils.d(tag, " > track event success...")
        } else {
            LogUtils.d(tag, " > track failed. $initial, ${this::sdk.isInitialized}")
        }
    }

    fun flush() {
        if (initial && this::sdk.isInitialized) {
            sdk.flush()
        }
    }

    /**
     * 退出登录.
     */
    fun logout() {
        if (initial && this::sdk.isInitialized) {
            LogUtils.d(tag, " > flush called...")
            sdk.logout()
        } else {
            LogUtils.d(tag, " > set failed. $initial, ${this::sdk.isInitialized}")
        }
    }
}