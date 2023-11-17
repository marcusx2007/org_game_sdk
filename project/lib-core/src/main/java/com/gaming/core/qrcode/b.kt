package com.gaming.core.qrcode

import android.content.Context
import java.io.File

/**
 * 接口代理类
 */
internal interface b {

    //上下文
    fun a(): Context

    //需要生成二维码的源文件
    fun b(): File

    //qrcode的数据
    fun c(): String

    //qrcode的大小
    fun d(): Int

    //qrcode位置
    fun e(): Pair<Int, Int>

    //返回结果到外部
    fun f(boolean: Boolean)
}