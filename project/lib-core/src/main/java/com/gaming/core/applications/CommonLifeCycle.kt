package com.gaming.core.applications

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.gaming.core.utils.LogUtils
import java.util.Stack

open class CommonLifeCycle : Application.ActivityLifecycleCallbacks {

    val stack = Stack<Activity>()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        LogUtils.d("","activity: $activity -> onActivityCreated")
        stack.push(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        LogUtils.d("","activity: $activity -> onActivityStarted")
    }

    override fun onActivityResumed(activity: Activity) {
        LogUtils.d("","activity: $activity -> onActivityResumed")
    }

    override fun onActivityPaused(activity: Activity) {
        LogUtils.d("","activity: $activity -> onActivityPaused")
    }

    override fun onActivityStopped(activity: Activity) {
        LogUtils.d("","activity: $activity -> onActivityStopped")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        LogUtils.d("","activity: $activity -> onActivitySaveInstanceState")
    }

    override fun onActivityDestroyed(activity: Activity) {
        LogUtils.d("","activity: $activity -> onActivityDestroyed")
        if (stack.isNotEmpty()) {
            stack.pop()
        }
    }
}