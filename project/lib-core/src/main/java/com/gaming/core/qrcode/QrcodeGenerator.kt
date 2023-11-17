package com.gaming.core.qrcode

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import com.gaming.core.utils.LogUtils
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.jvm.Throws

internal class QrcodeGenerator {

    private lateinit var invocation: b

    /**
     * 通过data生成指定位置的url
     * @return 是否生成成功
     * @param data 二维码中的数据
     * @param size 二维码的大小
     * @param x X轴坐标
     * @param y Y轴坐标
     */
    suspend fun create(abstractQrcodeCall: b): Boolean = withContext(Dispatchers.IO) {
        this@QrcodeGenerator.invocation = abstractQrcodeCall
        /**之前生成的促销图是通过渠道品牌来命名的.*/
        try {
            //1. 生成指定二维码的数据
            val bitMatrix = MultiFormatWriter().encode(
                invocation.c(),
                BarcodeFormat.QR_CODE,
                invocation.d(),
                invocation.d()
            )

            //2. 将qrcode数据转换成bitmap
            val qrcodeBitmap = bitMatrix.asBitmap()

            //3. 将原始图片和二维码合成一张图片
            val sourceBmp = BitmapFactory.decodeFile(invocation.b().absolutePath)
            val newBitmap = combineBitmapWithQrcode(
                sourceBmp,
                qrcodeBitmap,
                invocation.e().first,
                invocation.e().second
            )

            //4. 将新的bitmap写入到相册中.
            ContentResolverUtils.syncAlbum(invocation.a(), invocation.b().name, newBitmap)
        } catch (error: Exception) {
            error.printStackTrace()
            LogUtils.d("qrtag","failed,请重试~ ${error.message}")
            false
        }
    }


    @Throws(IllegalStateException::class)
    private fun BitMatrix.asBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        for (i in 0 until bitmap.width) {
            for (j in 0 until bitmap.height) {
                bitmap.setPixel(i, j, if (this.get(i, j)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    //generate bitmap with qrcode
    private fun combineBitmapWithQrcode(
        background: Bitmap,
        overlay: Bitmap,
        posX: Int,
        posY: Int
    ): Bitmap {
        val combinedBitmap =
            Bitmap.createBitmap(background.width, background.height, background.config)
        val canvas = Canvas(combinedBitmap)
        canvas.drawBitmap(background, 0f, 0f, null)
        canvas.drawBitmap(overlay, posX.toFloat(), posY.toFloat(), null)
        return combinedBitmap
    }
}