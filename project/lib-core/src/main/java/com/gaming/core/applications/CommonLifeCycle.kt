package com.gaming.core.applications

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.Stack

open class CommonLifeCycle : Application.ActivityLifecycleCallbacks {

    val stack = Stack<Activity>()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        stack.push(activity)
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        stack.pop()
    }
}