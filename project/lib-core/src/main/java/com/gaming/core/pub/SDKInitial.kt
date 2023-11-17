package com.gaming.core.pub

import android.app.Application

/**
 * 接口隔离.
 */
interface SDKInitial {

    /**
     * @param application
     * @param debug
     * @param data 加密数据.
     */
    fun init(application: Application, debug: Boolean, data: ByteArray)
}