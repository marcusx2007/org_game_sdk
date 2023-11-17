package com.gaming.core.utils

import android.content.Context
import android.graphics.Bitmap
import com.gaming.core.pri.GamingGlobal
import java.io.*
import java.nio.charset.StandardCharsets

internal object FileUtil {
    val TAG:String = FileUtil::class.java.simpleName
    private const val READ_CACHE_LENGTH = 8192
    val selfApkFile: File
        get() {
            val context: Context = GamingGlobal.get().application()
            return File(context.packageResourcePath)
        }

    fun deleteFileIfExists(file: File?) {
        if (file != null && file.exists()) {
            file.delete()
        }
    }

    fun ensureFile(file: File?) {
        if (file != null && !file.exists()) {
            ensureDirectory(file.parentFile)
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun ensureDirectory(directory: File?) {
        if (directory != null && !directory.exists()) {
            directory.mkdirs()
        }
    }

    @Throws(IOException::class)
    fun copyFile(src: File?, dst: File?) {
        copyFile(FileInputStream(src), dst)
    }

    @Throws(IOException::class)
    fun copyFile(src: InputStream, dst: String?) {
        copyFile(src, File(dst?:""))
    }

    @Throws(IOException::class)
    fun copyFile(src: InputStream, dst: File?) {
        var ou: BufferedOutputStream? = null
        try {
            ou = BufferedOutputStream(FileOutputStream(dst))
            val buffer = ByteArray(READ_CACHE_LENGTH)
            var read = 0
            while (src.read(buffer).also { read = it } != -1) {
                ou.write(buffer, 0, read)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            src.close()
            ou?.close()
        }
    }

    @Throws(IOException::class)
    fun copyFile(src: String?, dst: String?) {
        var `in`: BufferedInputStream? = null
        var ou: BufferedOutputStream? = null
        try {
            `in` = BufferedInputStream(FileInputStream(src))
            ou = BufferedOutputStream(FileOutputStream(dst))
            val buffer = ByteArray(READ_CACHE_LENGTH)
            var read = 0
            while (`in`.read(buffer).also { read = it } != -1) {
                ou.write(buffer, 0, read)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            `in`?.close()
            ou?.close()
        }
    }

    fun saveBitmap(path: String?, bitmap: Bitmap) {
        val file = File(path?:"")
        saveBitmap(file, bitmap)
    }

    fun saveBitmap(file: File?, bitmap: Bitmap) {
        var out: FileOutputStream? = null
        try {
            ensureFile(file)
            out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            out?.close()
        }
    }

    fun readFile(path: String?): String? {
        val file = File(path?:"")
        return readFile(file)
    }

    fun readFile(file: File?): String? {
        var inputStream: InputStream? = null
        var streamReader: InputStreamReader? = null
        var bufferedReader: BufferedReader? = null
        try {
            inputStream = FileInputStream(file)
            streamReader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
            bufferedReader = BufferedReader(streamReader)
            val builder = StringBuilder()
            var line: String? = null
            while (bufferedReader.readLine().also { line = it } != null) {
                builder.append(line)
            }
            return builder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            streamReader?.close()
            bufferedReader?.close()
        }
        return null
    }

    fun readFileWithBytes(file: File?): ByteArray {
        var `in`: BufferedInputStream? = null
        var out: ByteArrayOutputStream? = null
        try {
            `in` = BufferedInputStream(FileInputStream(file))
            out = ByteArrayOutputStream()
            val buffer = ByteArray(READ_CACHE_LENGTH)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            return out.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            `in`?.close()
            out?.close()
        }
        return byteArrayOf()
    }

    fun writeFile(path: String?, content: String?): Boolean {
        val file = File(path?:"")
        return writeFile(file, content)
    }

    fun writeFile(file: File?, content: String?): Boolean {
        var outputStream: OutputStream? = null
        var streamWriter: OutputStreamWriter? = null
        var bufferedWriter: BufferedWriter? = null
        var success = false
        try {
            ensureFile(file)
            outputStream = FileOutputStream(file)
            streamWriter = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
            bufferedWriter = BufferedWriter(streamWriter)
            bufferedWriter.write(content)
            bufferedWriter.flush()
            success = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream?.close()
            streamWriter?.close()
            bufferedWriter?.close()
        }
        return success
    }

    fun writeFileWithBytes(file: File?, bytes: ByteArray?) {
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            out.write(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            out?.close()
        }
    }

    private fun deleteFile(file: File): Boolean {
        val files = file.listFiles()
        if (files != null && files.isNotEmpty()) {
            for (deleteFile in files) {
                if (deleteFile.isDirectory) {
                    if (!deleteFile(deleteFile)) {
                        return false
                    }
                } else {
                    if (!deleteFile.delete()) {
                        return false
                    }
                }
            }
        }
        return file.delete()
    }
}