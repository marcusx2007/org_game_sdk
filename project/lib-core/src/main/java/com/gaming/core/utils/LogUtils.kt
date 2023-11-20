package com.gaming.core.utils

import android.util.Log
import com.gaming.core.analysi.ThinkingDataManager
import com.gaming.core.pri.GamingGlobal
import java.text.SimpleDateFormat

object LogUtils {

    private const val TAG = "core-sdk-impl-logger"

    @JvmStatic
    fun d(tag: String, msg: Any?) {
        val message = buildMessage(tag, msg)
        if (GamingGlobal.get().debug()) {
            Log.d(TAG, message)
        }
    }

    @JvmStatic
    fun e(tag: String, msg: Any?) {
        val message = buildMessage(tag, msg)
        if (GamingGlobal.get().debug()) {
            Log.e(TAG, message)
        }
    }

    //构建信息
    private fun buildMessage(tag: String, msg: Any?): String {
        val message = StringBuilder()
        try {
            val thread = Thread.currentThread().name
            val time =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(System.currentTimeMillis())
            message.append("[$time---$thread---$tag] -> $msg")
        } catch (err: Exception) {
            err.printStackTrace()
        }
        return message.toString()
    }
}