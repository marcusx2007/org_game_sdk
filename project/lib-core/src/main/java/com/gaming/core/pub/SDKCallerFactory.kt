package com.gaming.core.pub

internal class SDKCallerFactory private constructor() {

    internal object H {
        @JvmStatic
        val instance = SDKCallerFactory()
    }

    companion object {
        @JvmStatic
        fun get(): SDKCallerFactory {
            return H.instance
        }
    }

    fun create(): SDKCaller {
        return SDKCallerProxy()
    }
}