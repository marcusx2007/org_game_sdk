package com.gaming.core.qrcode

import android.content.Context
import android.os.Environment
import com.gaming.core.pri.ConstPool
import com.gaming.core.pri.GamingGlobal
import java.io.File


//接口实现类,通过该类获取实际传递的参数~
internal open class AbstractQrcodeCallImpl(
    private val config: AbstractQrConfig,
) : b {

    override fun a(): Context {
        return GamingGlobal.get().application()
    }

    override fun b(): File {
        return GamingGlobal.get().application()
            .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).let {
                val fileName = String.format(
                    ConstPool.PROMOTION_MATERIAL_FILENAME,
                    GamingGlobal.get().application().packageName, GamingGlobal.get().chn()
                )
                File(it, fileName)
            }
    }

    override fun c(): String {
        return config.data()
    }

    override fun d(): Int {
        return config.size()
    }

    override fun e(): Pair<Int, Int> {
        return Pair(config.x(), config.y())
    }

    override fun f(boolean: Boolean) {

    }
}

internal class QrConfig constructor(
    private val data: String,
    private val x: Int,
    private val y: Int,
    private val size: Int
) : AbstractQrConfig {
    override fun x(): Int {
        return x
    }

    override fun y(): Int {
        return y
    }

    override fun size(): Int {
        return size
    }

    override fun data(): String {
        return data
    }

    override fun toString(): String {
        return "[data=$data,x=$x,y=$y,size=$size]"
    }
}

interface AbstractQrConfig {
    fun x(): Int
    fun y(): Int
    fun size(): Int
    fun data(): String
}

internal object QrConfigFactory {
    @JvmStatic
    fun create(x: Int, y: Int, size: Int, data: String): AbstractQrConfig {
        return QrConfig(data, x, y, size)
    }
}