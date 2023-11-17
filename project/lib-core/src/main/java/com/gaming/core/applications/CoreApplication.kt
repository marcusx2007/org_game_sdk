package com.gaming.core.applications

import android.app.Activity
import androidx.multidex.MultiDexApplication
import com.gaming.core.analysi.AdjustManager
import com.gaming.core.analysi.ThinkingDataManager
import com.gaming.core.applications.CommonLifeCycle
import com.gaming.core.pri.GamingGlobal

open class CoreApplication : MultiDexApplication() {

    private var commonLifeCycle: CommonLifeCycle? = null

    override fun onCreate() {
        super.onCreate()
        GamingGlobal.get().setApplication(this)

        //先初始化TD,在初始化Adjust,这样可以先获取TD的相关数据
        ThinkingDataManager.get().init(
            GamingGlobal.get().application(),
            GamingGlobal.get().tdId()
        )

        //在初始化adjust
        AdjustManager.get().init(
            GamingGlobal.get().application(),
            GamingGlobal.get().debug(),
            GamingGlobal.get().adjustId()
        )

        //register
        commonLifeCycle = object : CommonLifeCycle() {
            override fun onActivityPaused(activity: Activity) {
                super.onActivityPaused(activity)
                if (stack.size == 1) {
                    AdjustManager.get().onPause()
                }
            }
            override fun onActivityResumed(activity: Activity) {
                super.onActivityResumed(activity)
                if (stack.size == 1) {
                    AdjustManager.get().onResume()
                }
            }
        }
        registerActivityLifecycleCallbacks(commonLifeCycle)

        //将异常信息记录到数据库中
        Thread.UncaughtExceptionHandler { t, e ->
            //TODO 将异常信息进行上报
        }
    }

    /**
     * get top stack activity~
     */
    fun topActivity():Activity? {
        return commonLifeCycle?.stack?.peek()
    }
}