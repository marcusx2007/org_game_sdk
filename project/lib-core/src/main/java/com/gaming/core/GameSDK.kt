package com.gaming.core

import android.app.Activity
import android.app.Application
import com.gaming.core.pub.SDKCallerFactory
import com.gaming.core.pub.SDKInitialFactory

/**
 * 非Compose版本调用
 */
object GameSDK {

    @JvmStatic
    fun init(application: Application, debug: Boolean, data: ByteArray) {
        preInit(application, debug, data)
    }

    @JvmStatic
    fun start(activity: Activity, invocation: () -> Unit) {
        preStart(activity, invocation)
    }

    @JvmStatic
    private fun preInit(application: Application, debug: Boolean, data: ByteArray) {
        try {
            val sdkInit = SDKInitialFactory.get().create()
            sdkInit.init(application, debug, data)
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    @JvmStatic
    private fun preStart(activity: Activity,invocation: () -> Unit) {
        try {
            val sdkCaller = SDKCallerFactory.get().create()
            sdkCaller.start(activity, invocation)
        } catch (err: Exception) {
            err.printStackTrace()
        }
    }
}