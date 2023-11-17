package com.gaming.core.analysi

import android.app.Application
//import com.bugsnag.android.Bugsnag
//import com.bugsnag.android.performance.BugsnagPerformance

internal class BugsnagImpl : BugsnagProxy {

    override fun init(application: Application) {
//        try {
//            Bugsnag.start(application, "b866f33abd5c6cba4cc42ab1d9183492")
//            BugsnagPerformance.start(application)
//        } catch (error: Exception) {
//            LogUtils.d("", "")
//        }

    }

    override fun notify(exception: Throwable) {
        //Bugsnag.notify(exception)
    }
}

internal interface BugsnagProxy {
    fun init(application: Application)
    fun notify(exception: Throwable)
}

internal class BugsnagProxyImpl : BugsnagProxy by BugsnagImpl()

internal object BugsnagOperator {

    private var proxy: BugsnagProxy? = null

    @JvmStatic
    fun init(application: Application) {
        proxy = BugsnagProxyImpl()
        proxy?.init(application)
    }

    @JvmStatic
    fun notify(exception: Throwable) {
        proxy?.notify(exception)
    }
}