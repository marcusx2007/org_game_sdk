package com.gaming.core.analysi

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustEvent
import com.adjust.sdk.LogLevel
import com.gaming.core.pri.ConstPool
import com.gaming.core.pri.GamingGlobal
import com.gaming.core.utils.LogUtils
import com.gaming.core.extensions.androidId

/**
 * @author marcus.x
 * 管理adjust相关功能
 */
@SuppressLint("StaticFieldLeak")
internal class AdjustManager private constructor() {

    private val tag = "AdjustManager"

    private object Holder {
        val instant = AdjustManager()
    }

    companion object {

        private const val AD_ID = "adid"

        private const val APP_ID = "appid"

        private const val GOOGLE_AD_ID = "gadid"

        @JvmStatic
        fun get(): AdjustManager {
            return Holder.instant
        }
    }

    /**
     * 是否初始化.
     */
    private var initial = false

    //保存粘性事件,如果没有初始化,会加入粘性队列中,进行发送.
    private val stickyEventArray = mutableListOf<AdEvent>()

    private var store: SharedPreferences? = null

    /**
     * 初始化.
     * @param appId adjustId
     * @param userId 主游戏ID
     * @param deviceId 设备id
     */
    fun init(context: Context, debug: Boolean, appId: String) {
        try {
            LogUtils.d(tag, " > adjust start init...$context ,$appId")
            if (store == null) {
                store = context.getSharedPreferences(ConstPool.APP_DATA_SP, Context.MODE_PRIVATE)
                LogUtils.d(tag, " > cache init success...")
            }
            if (!initial) {
                initial = true
                if (appId.isEmpty()) {
                    LogUtils.d(tag, " >  adjust appId is empty.")
                    return
                }
                val env =
                    if (debug) AdjustConfig.ENVIRONMENT_SANDBOX else AdjustConfig.ENVIRONMENT_PRODUCTION
                LogUtils.d(tag, " > configAdjust adjust env=$env")
                val config = AdjustConfig(context.applicationContext, appId, env).apply {
                    setSendInBackground(true)
                    setLogLevel(if (debug) LogLevel.VERBOSE else LogLevel.INFO)
                }
                Adjust.onCreate(config)
                LogUtils.d(tag, " > adjust init success...")
                Adjust.getGoogleAdId(context) { id ->
                    store?.edit()?.putString(GOOGLE_AD_ID, id)?.apply()
                }

                store?.edit()?.putString(APP_ID, appId)?.apply()
                Adjust.addSessionCallbackParameter("aid", context.androidId())
                Adjust.addSessionCallbackParameter("app_chn", GamingGlobal.get().chn())
                Adjust.addSessionCallbackParameter("app_brd", GamingGlobal.get().brd())
                Adjust.addSessionCallbackParameter("ta_distinct_id", ThinkingDataManager.get().distinctId())
            } else {
                LogUtils.d(tag, " > adjust already init.")
            }
        } catch (error: Exception) {
            initial = false
            error.printStackTrace()
            LogUtils.d(tag, " > init ${error.message}")
        }
    }

    /**
     * 更新adjust
     * @param appId adjustId
     * @param userId 主游戏ID
     * @param deviceId 设备id
     */
    fun update(context: Context, debug: Boolean, appId: String) {
        try {
            LogUtils.d(tag, " > adjust start update...$context ,$appId")
            if (store == null) {
                store = context.getSharedPreferences(ConstPool.APP_DATA_SP, Context.MODE_PRIVATE)
                LogUtils.d(tag, " > cache get success...")
            }
            if (appId.isEmpty()) {
                LogUtils.d(tag, " >  adjust appId is empty.")
                return
            }
            val env =
                if (debug) AdjustConfig.ENVIRONMENT_SANDBOX else AdjustConfig.ENVIRONMENT_PRODUCTION
            LogUtils.d(tag, " > configAdjust adjust env=$env")
            val config = AdjustConfig(context.applicationContext, appId, env).apply {
                setSendInBackground(true)
                setLogLevel(if (debug) LogLevel.VERBOSE else LogLevel.INFO)
            }
            Adjust.onCreate(config)
            LogUtils.d(tag, " > adjust update success...")
            //将appId缓存到本地
            store?.edit()?.putString(APP_ID, appId)?.apply()
            Adjust.addSessionCallbackParameter("aid", context.androidId())
            Adjust.addSessionCallbackParameter("app_chn", GamingGlobal.get().chn())
            Adjust.addSessionCallbackParameter("app_brd", GamingGlobal.get().brd())
            Adjust.addSessionCallbackParameter("ta_distinct_id", ThinkingDataManager.get().distinctId())
        } catch (error: Exception) {
            initial = false
            error.printStackTrace()
            LogUtils.d(tag, " > init ${error.message}")
        }
    }

    /**
     * 配置Adjust
     */
    fun setup(chn: String, brd: String, targetCountry: String, userId: String, deviceId: String) {
        try {
            LogUtils.d(
                tag,
                " > config chn=$chn,brd=$brd,userId=$userId,targetCountry=$targetCountry" + "deviceId=$deviceId"
            )
            if (!initial) {
                LogUtils.d(tag, " > config failed. sdk not init.")
                return
            }
            Adjust.addSessionCallbackParameter("ta_account_id", "$targetCountry-$userId")
            LogUtils.d(tag, "adjust config success...")
            consumeStickyEvent()
        } catch (error: Exception) {
            error.printStackTrace()
            LogUtils.d(tag, " > config failed. ${error.message}")
        }
    }

    fun appId(): String {
        val appId = store?.getString(APP_ID, "") ?: ""
        LogUtils.d(tag, " > appId=$appId")
        return appId
    }


    fun adId(): String {
        var adId = store?.getString(AD_ID, "") ?: ""
        if (!TextUtils.isEmpty(adId)) {
            return adId
        }
        Adjust.getAdid()?.apply {
            adId = this
            LogUtils.d(tag, " > adId=$adId")
            if (!TextUtils.isEmpty(adId)) {
                store?.edit()?.putString(AD_ID, adId)?.apply()
            }
        }

        return adId ?: ""
    }

    /**
     * 消费粘性事件.
     */
    private fun consumeStickyEvent() {
        LogUtils.d(tag, " > consumeStickyEvent ${stickyEventArray.size}")
        stickyEventArray.forEach {
            if (!get(it.name)) {
                trackEvent(it)
                set(it.name)
            }

        }
        stickyEventArray.clear()
        LogUtils.d(tag, "track sticky event...${stickyEventArray.size}")
    }

    var googleAdid: String = ""
        get() {
            return store?.getString(GOOGLE_AD_ID, "") ?: ""
        }

    private fun get(event: String): Boolean {
        return store?.getBoolean(event, false) ?: false
    }

    private fun set(event: String) {
        store?.edit()?.putBoolean(event, true)?.apply()
    }

    /**
     * 追踪start事件
     */
    fun trackEventStart(event: String, params: Map<String, String> = hashMapOf()) {
        if (!initial) {
            LogUtils.d(tag, " > trackEventStart sdk not init...")
            return
        }
        val justOnce = get(event)
        LogUtils.d(tag, "trackEventStart event=$event,${params.size},justOnce=$justOnce")
        if (!justOnce) {
            trackEvent(AdjustEventFactory.get().createNormalEvent(event, params))
            set(event)
        } else {
            LogUtils.d(tag, " > trackEventStart greeting already upload...")
        }
    }

    /**
     * 追踪greeting事件.
     */
    fun trackEventGreeting(event: String, params: Map<String, String> = hashMapOf()) {
        if (!initial) {
            LogUtils.d(tag, " > trackEventStart sdk not init...")
            return
        }
        val justOnce = get(event)
        LogUtils.d(tag, "trackEventGreeting event=$event,${params.size},justOnce=$justOnce")
        if (!justOnce) {
            trackEvent(AdjustEventFactory.get().createNormalEvent(event, params))
            set(event)
        } else {
            LogUtils.d(tag, " > trackEventGreeting greeting already upload...")
        }
    }

    /**
     * 清除本地记录的状态
     */
    fun clearEvent(event: String) {
        store?.edit()?.putBoolean(event, false)?.apply()
        LogUtils.d(tag, " > clear event. $event")
    }

    /**
     * 清除本地所有记录的事件信息
     */
    fun clear(context: Context) {
        context.applicationContext.getSharedPreferences(ConstPool.APP_DATA_SP, Context.MODE_PRIVATE)
            .edit().clear()
            .apply()
        LogUtils.d(tag, " > clear all adjust event.")
    }

    /**
     * 追踪access事件.
     */
    fun trackEventAccess(event: String, params: Map<String, String> = hashMapOf()) {
        if (!initial) {
            LogUtils.d(tag, " > trackEventStart sdk not init...")
            return
        }
        val justOnce = get(event)
        LogUtils.d(tag, "trackEventAccess event=$event,${params.size},justOnce=$justOnce")
        if (!justOnce) {
            trackEvent(AdjustEventFactory.get().createNormalEvent(event, params))
            set(event)
        } else {
            LogUtils.d(tag, " > trackEventAccess greeting already upload...")
        }
    }

    /**
     * 追踪事件. 不作唯一性校验.
     */
    fun trackEvent(event: String, params: Map<String, String> = hashMapOf()) {
        if (!initial) {
            LogUtils.d(tag, " > trackEvent sdk not init...")
            return
        }
        trackEvent(AdjustEventFactory.get().createNormalEvent(event, params))
    }

    /**
     * 追踪update事件
     */
    fun trackEventUpdate(event: String, params: Map<String, String> = hashMapOf()) {
        if (!initial) {
            LogUtils.d(tag, " > trackEventStart sdk not init...")
            return
        }
        val justOnce = get(event)
        LogUtils.d(tag, "trackEventUpdate event=$event,${params.size},justOnce=$justOnce")
        if (!justOnce) {
            trackEvent(AdjustEventFactory.get().createNormalEvent(event, params))
            set(event)
        } else {
            LogUtils.d(tag, " > trackEventUpdate greeting already upload...")
        }
    }

    /**
     * 发送粘性事件.添加到缓存队列.初始化后直接发送.
     * 如果已经初始化,则直接发送.
     */
    fun trackStickyEvent(event: String, params: Map<String, String> = hashMapOf()) {
        if (!initial) {
            val e = AdjustEventFactory.get().createStickyEvent(event, params)
            LogUtils.d(tag, " > trackStickyEvent $event,initial=$initial")
            stickyEventArray.add(e)
        } else {
            LogUtils.d(tag, " > trackStickyEvent $event,initial=$initial")
            val cache = store?.getBoolean(event, false) ?: false
            if (!cache) {
                this.trackEvent(AdjustEventFactory.get().createNormalEvent(event, params))
                store?.edit()?.putBoolean(event, true)?.apply()
            }
        }
    }

    fun onResume() {
        if (initial) {
            LogUtils.d(tag, " > onResume adjust resume")
            Adjust.onResume()
        } else {
            LogUtils.d(tag, " > adjust resume failed initial=$initial")
        }
    }

    fun onPause() {
        if (initial) {
            LogUtils.d(tag, " > onResume adjust pause")
            Adjust.onPause()
        } else {
            LogUtils.d(tag, " > adjust pause failed initial=$initial")
        }
    }

    /**
     * 发送事件
     */
    private fun trackEvent(event: AdEvent) {
        try {
            LogUtils.d(tag, " > trackEvent event=$event")
            val adEvent = AdjustEvent(event.name)
            event.param.entries.forEach { adEvent.addCallbackParameter(it.key, it.value) }
            Adjust.trackEvent(adEvent)
            LogUtils.d(tag, " > adjust track event success...${adEvent.eventToken}")
        } catch (error: Exception) {
            error.printStackTrace()
            LogUtils.d(tag, " > trackEvent ${error.message}")
        }
    }
}