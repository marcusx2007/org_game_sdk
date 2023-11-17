package com.gaming.core.network

import android.text.TextUtils

internal object Emu {

    @JvmStatic
    val isEmu: Boolean get() = may01() || mayAbi()

    @JvmStatic
    private fun may01(): Boolean {
        val qemu = pro("ro.kernel.qemu")
        return "1" == qemu
    }

    @JvmStatic
    private fun mayAbi(): Boolean {
        val abi = pro("ro.product.cpu.abi") ?: return false
        return !TextUtils.isEmpty(abi) && abi.contains("x86")
    }

    @JvmStatic
    private fun pro(property: String): String? {
        return try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val method = systemProperties.getMethod("get", String::class.java)
            val params = arrayOfNulls<Any>(1)
            params[0] = property
            method.invoke(systemProperties, *params) as String
        } catch (e: Exception) {
            null
        }
    }
}