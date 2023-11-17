package com.gaming.core.network

import com.gaming.core.pri.GamingGlobal
import com.gaming.core.extensions.data

internal abstract class AbstractNetworkCallImpl : a {
    override fun a(): Boolean {
        return GamingGlobal.get().debug()
    }
    override fun b(): ByteArray {
        return GamingGlobal.get().application().data()
    }
    override fun c(json: String) {
    }
}