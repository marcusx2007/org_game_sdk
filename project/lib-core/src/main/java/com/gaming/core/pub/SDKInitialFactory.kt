package com.gaming.core.pub

internal class SDKInitialFactory private constructor() {

    internal object H {
        val instance = SDKInitialFactory()
    }

    companion object {
        @JvmStatic
        fun get(): SDKInitialFactory {
            return H.instance
        }
    }

    fun create(): SDKInitial {
        return SDKInitialProxy()
    }
}