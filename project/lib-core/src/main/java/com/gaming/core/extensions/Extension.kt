package com.gaming.core.extensions

import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import com.gaming.core.data.GameData
import com.gaming.core.pri.GamingGlobal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.File
import java.net.InetAddress
import java.net.URL
import java.text.DecimalFormat


//将数组转为字符串
fun IntArray.string(): String {
    val s = StringBuilder()
    this.forEach {
        s.append(it.toChar())
    }
    return s.toString()
}


fun String?.int(): Int = try {
    this?.toInt() ?: 0
} catch (err: Exception) {
    err.printStackTrace()
    0
}


fun String?.long(): Long {
    return try {
        this?.toLong() ?: 0L
    } catch (error: Exception) {
        error.printStackTrace()
        0L
    }
}

//获取APP的基础配置信息加密文件
fun String.config(): String {
    return this.split(".").let {
        it.first().toString().plus(it.last())
    }
}

//判断是否是有效的加密文件
fun String.isValidFile(): Boolean {
    val index = this.indexOf(".")
    val pointEq = index != -1   //判断是否存在分割号
    val indexEq = index != this.lastIndexOf(".")    //判断是否有两个
    return pointEq && indexEq
}

//获取sdk配置的加密随机数
fun String.encRandom(): Int {
    val arr = this.split(".")
    if (arr.size != 3) return -1
    return arr[1].toInt(16)
}

//是否是adjust的配置
fun String.isAdjust(): Boolean {
    return this.startsWith(0x61.toChar())
}

//是否是oneSignal配置
fun String.isOneSignal(): Boolean {
    return this.startsWith(0x6F.toChar())
}

//是否是td配置
fun String.isThingData(): Boolean {
    return this.startsWith(0x74.toChar())
}

//是否是httpDns配置
fun String.isHttpDns(): Boolean {
    return this.startsWith(0x68.toChar())
}


//是否是COCOS配置文件信息
fun String.isCocosJs(): Boolean {
    return this.startsWith(0x63.toChar())
}


fun String.inetAddress(): String {
    var ipAddress: String = ""
    try {
        val inetAddress = InetAddress.getByName(this)
        ipAddress = inetAddress.hostAddress ?: ""
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ipAddress
}


fun Long.formatSize(): String {
    val size = this
    val KB: Long = 1024
    val MB = (1024 * 1024).toLong()
    val GB = (1024 * 1024 * 1024).toLong()
    val df = DecimalFormat("0.00") //格式化小数
    var formattedSize = ""
    formattedSize = if (size > GB) {
        df.format((size / GB.toFloat()).toDouble()) + "GB"
    } else if (size > MB) {
        df.format((size / MB.toFloat()).toDouble()) + "MB"
    } else if (size > KB) {
        df.format((size / KB.toFloat()).toDouble()) + "KB"
    } else {
        size.toString() + "B"
    }
    return formattedSize
}

fun String.parseHost(): String {
    val url = this
    if (TextUtils.isEmpty(url)) {
        return url
    }
    val host: String = try {
        URL(url).host
    } catch (e: Exception) {
        val startIndex = url.indexOf("://")
        var urlTemp: String = url
        if (startIndex != -1) {
            urlTemp = url.substring(startIndex + 3)
        }
        var endIndex = urlTemp.indexOf(":")
        if (endIndex == -1) {
            endIndex = urlTemp.indexOf("/")
        }
        if (endIndex == -1) {
            endIndex = urlTemp.length
        }
        urlTemp.substring(0, endIndex)
    }
    return host
}


fun String.json(): JSONObject = JSONObject(this)

//解密raw目录下的资源
fun String.decryptByRandom(index: Int): String {
    return try {
        val split = String.format("%02X", index).lowercase()
        var result = ""
        this.split(split).filter { it.isNotEmpty() }.forEach {
            val unicode = it.toInt(16).minus(index)
            result += unicode.toChar()
        }
        result
    } catch (err: Exception) {
        err.printStackTrace()
        ""
    }
}

fun Flow<Int>.min(): Int = runBlocking {
    var min = Int.MAX_VALUE
    this@min.collect {
        min = if (min > it) it else min
    }
    min
}

fun File.triggerScanning() {
    val uri: Uri = try {
        Uri.fromFile(this)
    } catch (e: Exception) {
        return
    }
    GamingGlobal.get().application().sendBroadcast(
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)
    )
}


fun String.isSame(other: String): Boolean {
    if (TextUtils.isEmpty(this) || TextUtils.isEmpty(other)) {
        return false
    }
    var baseUrl1 = getBaseUrl(this)
    var baseUrl2 = getBaseUrl(other)
    if (!baseUrl1.endsWith("/")) {
        baseUrl1 = "$baseUrl1/"
    }
    if (!baseUrl2.endsWith("/")) {
        baseUrl2 = "$baseUrl2/"
    }
    return baseUrl1 == baseUrl2
}

private fun getBaseUrl(url: String): String {
    if (TextUtils.isEmpty(url)) {
        return url
    }
    var baseUrl = url
    val markIndex = url.indexOf("?")
    if (markIndex > 0) {
        baseUrl = url.substring(0, markIndex)
    }
    return baseUrl
}

internal fun String.JSON(): JSONObject {
    return JSONObject(this)
}

internal fun String.data(): GameData {
    return JSONObject(this).run {
        val adjust = getJSONObject("adjust")
        GameData(
            number = getString("id"),
            debug = getBoolean("debug"),
            brd = getString("brd"),
            chn = getString("chn"),
            shf = getString("domain"),
            api = getString("api"),
            backup = getJSONArray("backup").run {
                val list = ArrayList<String>()
                for (i in 0 until length()) {
                    list.add(get(i).toString())
                }
                list
            },
            adjustId = adjust.getString("id"),
            start = adjust.getString("start"),
            greet = adjust.getString("greet"),
            access = adjust.getString("access"),
            update = adjust.getString("update"),
            tdId = getString("tdid"),
            tdUrl = getString("tdurl")
        )
    }
}