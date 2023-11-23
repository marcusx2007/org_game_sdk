package com.gaming.core.pub.impl

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.gaming.core.applications.CommonLifeCycle
import com.gaming.core.pri.GamingGlobal
import com.gaming.core.pub.SDKInitial
import com.gaming.core.utils.LogUtils
import com.gaming.core.analysi.AdjustManager
import com.gaming.core.analysi.BugsnagOperator
import com.gaming.core.analysi.ThinkingDataManager
import com.gaming.core.pri.ConstPool

internal class SDKInitialImpl : SDKInitial {

    private val tag = "origi-sdk-logger"

    private var commonLifeCycle: CommonLifeCycle? = null

    override fun init(application: Application, debug: Boolean, data: ByteArray) {
        try {
            //init application
            GamingGlobal.get().init(application, data)

            //init sdk
            ThinkingDataManager.get().init(
                application, GamingGlobal.get().tdId(),
                GamingGlobal.get().tdUrl()
            )
            AdjustManager.get()
                .init(application, GamingGlobal.get().debug(), GamingGlobal.get().adjustId())

            //init lifecycle
            commonLifeCycle = object : CommonLifeCycle() {
                override fun onActivityPostStarted(activity: Activity) {
                    super.onActivityPostStarted(activity)
                }
                override fun onActivityResumed(activity: Activity) {
                    super.onActivityResumed(activity)
                    AdjustManager.get().onResume()
                }

                override fun onActivityPaused(activity: Activity) {
                    super.onActivityPaused(activity)
                    AdjustManager.get().onPause()
                }
            }
            application.registerActivityLifecycleCallbacks(commonLifeCycle)
            Thread.UncaughtExceptionHandler { t, e ->
                LogUtils.e(tag, "${t.name}: ${e.message}")
                val stackInfo = StringBuilder()
                e.stackTrace.forEach {
                    stackInfo.append(it).append("\n")
                }
                //BugsnagOperator.notify(e)
                ThinkingDataManager.get().trackEvent(
                    "app_crash_event",
                    "app_crash_stack" to "$stackInfo",
                    "app_crash_message" to "${e.message}",
                    "app_crash_cause" to "${e.cause?.message}",
                )
                AdjustManager.get().trackEvent(
                    "app_crash_event", hashMapOf(
                        "app_crash_stack" to "$stackInfo",
                        "app_crash_message" to "${e.message}",
                        "app_crash_cause" to "${e.cause?.message}",
                    )
                )
            }
            //init bugsnag
            //BugsnagOperator.init(application)

            if (debug) {
                Log.d(tag, "sdk init successful~")
            }
        } catch (error: Exception) {
            error.printStackTrace()
            Log.d(tag, "sdk init failed: ${error.message}")
        }
    }
}