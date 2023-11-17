package com.gaming.core.pub

import android.app.Activity

interface SDKCaller {

    /**
     * 回调方法
     */
    fun start(activity: Activity,invocation: () -> Unit)
}