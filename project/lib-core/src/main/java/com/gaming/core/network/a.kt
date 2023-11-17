package com.gaming.core.network


internal interface a {

    /**
     * 调试模式
     */
    fun a():Boolean

    /**
     * 原始数据
     */
    fun b(): ByteArray

    /**
     * 结果返回
     */
    fun c(json: String)


    /**
     * 静默时间
     */
    fun d():Long
}