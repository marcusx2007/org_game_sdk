package com.gaming.core.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

internal object ShareUtil {
    val TAG:String = ShareUtil::class.java.simpleName
    fun sendText(context: Context, text: String?) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        sendIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        sendIntent.type = "text/plain"
        sendIntent.putExtra(Intent.EXTRA_TEXT, text)
        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    fun sendImage(context: Context, file: File?) {
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file!!)
        } else {
            Uri.fromFile(file)
        }
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        sendIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        sendIntent.type = "image/*"
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri)
        val shareIntent = Intent.createChooser(sendIntent, "Share File")
        val resInfoList = context.packageManager.queryIntentActivities(
            shareIntent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(
                packageName,
                uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        context.startActivity(shareIntent)
    }

    fun shareToWhatsApp(context: Context, text: String?, file: File?) {
        var uri: Uri? = null
        if (file != null && file.exists()) {
            uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        }
        val intent = Intent()
        intent.action = "android.intent.action.SEND"
        intent.putExtra("android.intent.extra.SUBJECT", "WhatsApp")
        intent.putExtra("android.intent.extra.TEXT", text)
        if (uri != null) {
            intent.type = "image/*"
            intent.putExtra("android.intent.extra.STREAM", uri)
        } else {
            intent.type = "text/plain"
        }
        intent.setPackage("com.whatsapp")
        intent.flags = Intent.FLAG_RECEIVER_FOREGROUND
        try {
            context.startActivity(intent)
        } catch (unused: Exception) {
            unused.printStackTrace()
            openMarket(context, "com.whatsapp")
        }
    }

    fun openMarket(context: Context, packageName: String): Boolean {
        var success = openGooglePlay(context, packageName)
        if (!success) {
            success = openBuildInMarket(context, packageName)
        }
        return success
    }

    private fun openBuildInMarket(context: Context, packageName: String): Boolean {
        var success = false
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data =
                Uri.parse("market://details?id=$packageName") //跳转到应用市场，非Google Play市场一般情况也实现了这个接口
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            success = true
        } catch (e: Exception) {
            LogUtils.d(TAG, "Open BuildIn Market fail: ${e.message}")
        }
        return success
    }

    private fun openGooglePlay(context: Context, packageName: String): Boolean {
        var success = false
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=$packageName")
            intent.setPackage("com.android.vending")
            if (intent.resolveActivity(context.packageManager) != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                success = true
            } else { //没有应用市场，通过浏览器跳转到Google Play
                val intent2 = Intent(Intent.ACTION_VIEW)
                intent2.data =
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                if (intent2.resolveActivity(context.packageManager) != null) {
                    intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent2)
                    success = true
                } else {
                    LogUtils.d(TAG, "Can not find any component to open GooglePlay")
                }
            }
        } catch (e: Exception) {
            LogUtils.d(TAG, "Open GooglePlay fail: ${e.message}")
        }
        return success
    }
}