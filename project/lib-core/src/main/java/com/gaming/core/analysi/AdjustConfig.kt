package com.gaming.core.analysi


/**
 * adjust事件封装
 * @param name 事件
 * @param sticky 是否是粘性事件
 */
sealed class AdEvent(val name: String, val param: Map<String, String>, val sticky: Boolean) {

    override fun toString(): String {
        return "[event:$name,param={${params()}}sticky:$sticky]"
    }

    private fun params(): String = StringBuilder().apply {
        this@AdEvent.param.forEach { (k, v) ->
            append(k).append(":").append(v).append(",")
        }
    }.toString()
}

internal class StickyEvent constructor(name: String, param: Map<String, String>) :
    AdEvent(name, param, true)

internal class NormalEvent constructor(name: String, param: Map<String, String>) :
    AdEvent(name, param, false)

internal class AdjustEventFactory private constructor() {

    object Holder {
        val factory = AdjustEventFactory()
    }

    companion object {
        @JvmStatic
        fun get(): AdjustEventFactory {
            return Holder.factory
        }
    }

    /**
     * 创建粘性事件
     */
    fun createStickyEvent(event: String, param: Map<String, String> = hashMapOf()): AdEvent {
        return StickyEvent(event, param)
    }

    /**
     * 创建正常事件
     */
    fun createNormalEvent(event: String, param: Map<String, String> = hashMapOf()): AdEvent {
        return NormalEvent(event, param)
    }
}