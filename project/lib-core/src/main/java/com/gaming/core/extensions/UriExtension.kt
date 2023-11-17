package com.gaming.core.extensions

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import com.gaming.core.pri.GamingGlobal
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream


/**
 * 将Uri转为File
 */
fun Uri?.toFile(): File? {
    this ?: return null
    Log.i("FileExt", "uriToFile: $this")
    return when (scheme) {
        ContentResolver.SCHEME_FILE -> {
            File(this.path)
        }

        ContentResolver.SCHEME_CONTENT -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getFileFromUriQ()
            } else {
                getFileFromUriN()
            }
        }

        else -> {
            File(toString())
        }
    }
}

/**
 * 根据Uri获取File，AndroidQ及以上可用
 * AndroidQ中只有沙盒中的文件可以直接根据绝对路径获取File，非沙盒环境是无法根据绝对路径访问的
 * 因此先判断Uri是否是沙盒中的文件，如果是直接拼接绝对路径访问，否则使用[saveFileByUri]复制到沙盒中生成File
 */
fun Uri.getFileFromUriQ(): File? {
    var file: File? = getFileFromMedia()
    if (file == null) {
        file = getFileFromDocuments()
    }
    val flag = file?.exists() ?: false
    return if (!flag) {
        this.saveFileByUri()
    } else {
        file
    }
}

/**
 * 根据Uri获取File，AndroidN~AndroidQ可用
 */
fun Uri.getFileFromUriN(): File? {
    var file = getFileFromMedia()
    val uri = this
    Log.i("FileExt", "getFileFromUriN: $uri ${uri.authority} ${uri.path}")
    val authority = uri.authority
    val path = uri.path

    /**
     * fileProvider{@xml/file_paths}授权的Uri
     */
    val packageName = GamingGlobal.get().application().packageName
    if (file == null && authority != null && authority.startsWith(packageName) && path != null) {
        //这里的值来自你的provider_paths.xml，如果不同需要自己进行添加修改
        val externals = mutableListOf(
            "/external",
            "/external_path",
            "/beta_external_files_path",
            "/external_cache_path",
            "/beta_external_path",
            "/external_files",
            "/internal"
        )
        externals.forEach {
            if (path.startsWith(it)) {
                //如果你在provider_paths.xml中修改了path，需要自己进行修改
                val newFile = File(
                    "${Environment.getExternalStorageDirectory().absolutePath}/${
                        path.replace(
                            it,
                            ""
                        )
                    }"
                )
                if (newFile.exists()) {
                    file = newFile
                }
            }
        }
    }
    /**
     * Intent.ACTION_OPEN_DOCUMENT选择的文件Uri
     */
    if (file == null) {
        file = getFileFromDocuments()
    }
    val flag = file?.exists() ?: false
    return if (!flag) {
        //形如content://com.android.providers.downloads.documents/document/582的下载内容中的文件
        //无法根据Uri获取到真实路径的文件，统一使用saveFileByUri()方法获取File
        uri.saveFileByUri()
    } else {
        file
    }
}
/**
 * 根据Uri获取扩展名
 */
fun Uri.getExtensionByUri() =
    GamingGlobal.get().application().contentResolver.getType(this)?.getExtensionByMimeType()

/**
 * 根据MimeType获取拓展名
 */
fun String.getExtensionByMimeType(): String {
    var ext = ""
    runCatching {
        ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(this) ?: ""
    }.onFailure {
        it.printStackTrace()
    }
    return ext
}

/**
 * 根据Uri将文件保存File到沙盒中
 * 此方法能解决部分Uri无法获取到File的问题
 * 但是会造成文件冗余，可以根据实际情况，决定是否需要删除
 */
fun Uri.saveFileByUri(): File? {
    //文件夹uri，不复制直接return null
    if (isDirectory()) return null
    try {
        val context = GamingGlobal.get().application()
        val inputStream = context.contentResolver.openInputStream(this)
        val fileName = this.getFileName() ?: "${System.currentTimeMillis()}.${getExtensionByUri()}"
        val file = File(context.getCacheChildDir(null), fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        val fos = FileOutputStream(file)
        val bis = BufferedInputStream(inputStream)
        val bos = BufferedOutputStream(fos)
        val byteArray = ByteArray(1024)
        var bytes = bis.read(byteArray)
        while (bytes > 0) {
            bos.write(byteArray, 0, bytes)
            bos.flush()
            bytes = bis.read(byteArray)
        }
        bos.close()
        fos.close()
        return file
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

/**
 * media类型的Uri，相册中选择得到的uri，
 * 形如content://media/external/images/media/11560
 */
fun Uri.getFileFromMedia(): File? {
    var file: File? = null
    val authority = this.authority ?: "'"
    if (authority.startsWith("media")) {
        getDataColumn()?.run {
            file = File(this)
        }
    }
    return if (file?.exists() == true) {
        file
    } else {
        null
    }
}

/**
 * 根据Uri获取文件名
 */
fun Uri.getFileName(): String? {
    val documentFile = DocumentFile.fromSingleUri(GamingGlobal.get().application(), this)
    return documentFile?.name
}

/**
 * Intent.ACTION_OPEN_DOCUMENT选择的文件Uri
 */
fun Uri.getFileFromDocuments(): File? {
    grantPermissions(GamingGlobal.get().application())
    val uriId = when {
        DocumentsContract.isDocumentUri(GamingGlobal.get().application(), this) -> {
            Log.i("FileExt", "getFileFromDocuments: isDocumentUri")
            DocumentsContract.getDocumentId(this)
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && DocumentsContract.isTreeUri(this) -> {
            Log.i("FileExt", "getFileFromDocuments: isTreeUri")
            DocumentsContract.getTreeDocumentId(this)
        }

        else -> null
    }
    Log.i("FileExt", "getFileFromDocuments: $uriId")
    uriId ?: return null
    var file: File? = null
    val split: List<String> = uriId.split(":")
    if (split.size < 2) return null
    when {
        //文件存在沙盒中，可直接拼接全路径访问
        //判断依据目前是Android/data/包名，不够严谨
        split[1].contains("Android/data/${GamingGlobal.get().application().packageName}") -> {
            file = File("${Environment.getExternalStorageDirectory().absolutePath}/${split[1]}")
        }

        isExternalStorageDocument() -> { //内部存储设备中选择
            if (split.size > 1) file =
                File("${Environment.getExternalStorageDirectory().absolutePath}/${split[1]}")
        }

        isDownloadsDocument() -> { //下载内容中选择
            if (uriId.startsWith("raw:")) {
                file = File(split[1])
            } else {
                //MediaStore.Downloads.EXTERNAL_CONTENT_URI
            }
            //content://com.android.providers.downloads.documents/document/582
        }

        isMediaDocument() -> { //多媒体中选择
            var contentUri: Uri? = null
            when (split[0]) {
                "image" -> {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                "video" -> {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }

                "audio" -> {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
            }
            Log.i("FileExt", "isDocumentUri contentUri: $contentUri")
            contentUri?.run {
                val uri = ContentUris.withAppendedId(this, split[1].toLong())
                Log.i("FileExt", "isDocumentUri media: $uri")
                uri.getDataColumn()?.run {
                    file = File(this)
                }

            }
        }
    }
    return if (file?.exists() == true) {
        file
    } else {
        null
    }
}


/**
 * Uri授权，解决Android12和部分手机Uri无法读取访问问题
 */
fun Uri?.grantPermissions(context: Context, intent: Intent = Intent()) {
    this ?: return
    val resInfoList =
        context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    for (resolveInfo in resInfoList) {
        val packageName = resolveInfo.activityInfo.packageName
        context.grantUriPermission(
            packageName,
            this,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}


/**
 * Uri是否在内部存储设备中
 */
fun Uri.isExternalStorageDocument() = "com.android.externalstorage.documents" == this.authority

/**
 * Uri是否在下载内容中
 */
fun Uri.isDownloadsDocument() = "com.android.providers.downloads.documents" == this.authority

/**
 * Uri是否在多媒体中
 */
fun Uri.isMediaDocument() = "com.android.providers.media.documents" == this.authority

/**
 * 判断uri是否是文件夹
 */
fun Uri.isDirectory(): Boolean {
    val paths: List<String> = pathSegments
    return paths.size >= 2 && "tree" == paths[0]

}

/**
 * 根据Uri查询文件路径
 * Android4.4之前都可用，Android4.4之后只有从多媒体中选择的文件可用
 */
fun Uri?.getDataColumn(): String? {
    if (this == null) return null
    var str: String? = null
    var cursor: Cursor? = null
    try {
        cursor = GamingGlobal.get().application().contentResolver.query(
            this,
            arrayOf(MediaStore.MediaColumns.DATA),
            null,
            null,
            null
        )
        cursor?.run {
            if (this.moveToFirst()) {
                val index = this.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                if (index != -1) str = this.getString(index)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        cursor?.close()
    }
    Log.i("FileExt", "getDataColumn: $str")
    return str
}