package com.gaming.core.pub.impl

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.gaming.core.analysi.AdjustManager
import com.gaming.core.applications.CommonLifeCycle
import com.gaming.core.extensions.JSON
import com.gaming.core.extensions.getMobileId
import com.gaming.core.extensions.installReferrer
import com.gaming.core.extensions.setData
import com.gaming.core.network.AbstractNetworkCallImpl
import com.gaming.core.network.CoreImpl
import com.gaming.core.pri.ConstPool
import com.gaming.core.pri.GamingGlobal
import com.gaming.core.pub.SDKCaller
import com.gaming.core.ui.GameCoreActivity
import com.gaming.core.utils.LogUtils
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

internal class SDKCallerImpl : SDKCaller {

    override fun start(activity: Activity, invocation: () -> Unit) {
        MainScope().launch(Dispatchers.Main) {
            if (!GamingGlobal.get().initial()) {
                throw IllegalStateException("sdk not initialed. please called GameSDK\$init first.")
            }
            activity.intent.extras?.getInt("logger", -1)?.let {
                LogUtils.d("", "启动logger模式: $it")
                if (it != -1)
                    GamingGlobal.get().setDebug(it == ConstPool.DEBUGGABLE)
            }
            activity.intent.extras?.getInt("timing", ConstPool.DELAY)?.let {
                LogUtils.d("", "启动timing模式: $it")
                if (it != ConstPool.DELAY)
                    GamingGlobal.get().setDelay(it)
            }
            //1.初始化,归因/aid
            val deviceId = GamingGlobal.get().application().getMobileId()
            LogUtils.d(ConstPool.TAG, "deviceId=$deviceId")

            val installer = GamingGlobal.get().application().installReferrer()
            LogUtils.d(ConstPool.TAG, "referrer=$installer")

            val deffer = CompletableDeferred<String>()
            //2.请求接口
            CoreImpl().interview(GamingGlobal.get().application(),
                object : AbstractNetworkCallImpl() {
                    override fun c(json: String) {
                        deffer.complete(json)
                    }

                    override fun d(): Long {
                        return GamingGlobal.get().delay().toLong()
                    }
                })
            val data = deffer.await()
            if (data.isEmpty()) {
                invocation.invoke()
            } else {
                val json = data.JSON()
                if (json.optBoolean("rst")) {
                    GamingGlobal.get().application().setData(ConstPool.GAME_DOMAIN, "")
                }
                GamingGlobal.get().setCountry(json.optString("cty"))
                val domain = json.getString("usr")
                if (domain.isNotEmpty()) {
                    LogUtils.d(ConstPool.TAG, "track adjust event~")
                    AdjustManager.get().trackEventStart(GamingGlobal.get().start())
                    AdjustManager.get().trackEventAccess(GamingGlobal.get().access())
                    AdjustManager.get().trackEventGreeting(GamingGlobal.get().greeting())
                }
                activity.startActivity(Intent(activity, GameCoreActivity::class.java).apply {
                    putExtra("url", domain)
                })
                activity.finish()
            }
        }
    }
}