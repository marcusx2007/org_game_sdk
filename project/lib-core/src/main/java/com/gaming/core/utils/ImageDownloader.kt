package com.gaming.core.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.File
import java.io.FileOutputStream

/**
 * 图片下载管理器 ~
 */
internal object ImageDownloader {

    /**
     * 下载图片~
     */
    suspend fun downloadImage(
        url: String?,
        path: String,
        downloading: ((Status) -> Unit)? = null
    ): Status =
        withContext(Dispatchers.IO) {
            val resp = OkHttpClient().newCall(Request.Builder().url(url ?: "").build()).execute()
            if (!resp.isSuccessful) {
                LogUtils.d(
                    "ImageDownloader.downloadImage", "code=${resp.code()},msg=${resp.message()}"
                )
                Fail
            }
            try {
                val stream = resp.body()?.byteStream() ?: return@withContext Fail
                val total = resp.body()?.contentLength() ?: 0L
                if (total == 0L) {
                    LogUtils.d("ImageDownloader.downloadImage", "invalid content length~")
                    return@withContext Fail
                }
                LogUtils.d("ImageDownloader.downloadImage", "total=${total}")

                val dest = File(path, System.currentTimeMillis().toString() + ".jpg")
                val fos = FileOutputStream(dest)

                var sum = 0
                var len = 0
                val buffer = ByteArray(2048)
                while (stream.read(buffer).also { len = it } != -1) {
                    fos.write(buffer, 0, len)
                    sum += len
                    val progress = (sum * 1.0f / total * 100).toInt()

                    //将进度回调给主协程 ~
                    downloading?.let { callback ->
                        withContext(Dispatchers.Main) {
                            callback.invoke(Progress.get(progress))
                        }
                    }
                    LogUtils.d("ImageDownloader.downloadImage", "progress=$progress")
                }
                fos.flush()
                stream.close()
                fos.close()
                withContext(Dispatchers.Main) {
                    Success.create(dest)
                }
            } catch (error: Exception) {
                LogUtils.d("ImageDownloader.downloadImage", "error=${error.message}")
                Fail
            }
        }

    sealed class Status {
        /**
         * 调用状态,成功=true/失败=false
         */
        open val success: Boolean = false

        /**
         * 下载进度
         */
        open val progress: Int? = null

        /**
         * 下载成功后的文件
         */
        open val file: File? = null
    }


    object Fail : Status() {
        override val success: Boolean
            get() = false
    }

    /**
     * 下载成功监听
     */
    class Success private constructor(private val dest: File) : Status() {
        override val success: Boolean
            get() = true
        override val file: File
            get() = dest

        companion object {
            fun create(file: File): Success {
                return Success(file)
            }
        }
    }


    class Progress private constructor(private val pro: Int) : Status() {
        override val success: Boolean
            get() = true
        override val progress: Int
            get() = pro

        companion object {
            @JvmStatic
            fun get(progress: Int): Progress {
                return Progress(progress)
            }
        }
    }
}