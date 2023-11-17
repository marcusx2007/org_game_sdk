package com.gaming.core.utils

import android.util.Log
import com.gaming.core.analysi.ThinkingDataManager
import com.gaming.core.pri.GamingGlobal
import java.text.SimpleDateFormat

object LogUtils {

    @JvmStatic
    fun d(tag: String, msg: Any?) {
        val message = buildMessage(tag, msg)
        if (GamingGlobal.get().debug()) {
            Log.d(tag, message)
        } else {
            //TODO 将日志写入到本地
        }
    }

    @JvmStatic
    fun e(tag: String, msg: Any?) {
        val message = buildMessage(tag, msg)
        if (GamingGlobal.get().debug()) {
            Log.e(tag, message)
        } else {
            //TODO 将日志写入到本地
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