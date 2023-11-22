package com.origi.test.app.ui.login


import android.content.ComponentName
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gaming.core.GameSDK
import com.gaming.core.extensions.setData
import com.origi.test.app.data.LoginRepository
import java.net.URL
import java.net.URLDecoder
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    val status = MutableLiveData<Status>(None)
    private val _status by this::status

    fun login(context: LoginActivity, reqSHF:Boolean,chn: String, brd: String, domain: String) {
        val map = parseQueryParameters(domain)
        context.setData("_chn", chn)
        context.setData("_brd", map["brd"]?.takeIf { it.isNotEmpty() } ?: brd)
        context.setData("_domain", domain)
        if (reqSHF) {
            GameSDK.start(context) {
                _status.value = Invalid
            }
        }else {
            context.startActivity(Intent().setComponent(
                ComponentName(context.packageName,"com.gaming.core.ui.GameCoreActivity")
            ).putExtra("url",domain))
            context.finish()
        }
    }


    /**
     * 解析url中的查询参数.
     */
    private fun parseQueryParameters(urlString: String): Map<String, String> {
        val queryParameters = mutableMapOf<String, String>()
        try {
            if (!urlString.contains("?")) return mapOf()
            val url = URL(urlString)
            val query = url.query
            val pairs = query.split("&")
            for (pair in pairs) {
                val keyValue = pair.split("=")
                val key = URLDecoder.decode(keyValue[0], "UTF-8")
                val value = URLDecoder.decode(keyValue[1], "UTF-8")
                queryParameters[key] = value
            }
        } catch (e: Exception) {
            // 处理异常，例如URL不合法等
            e.printStackTrace()
        }

        return queryParameters
    }

    fun aes(bytes:ByteArray):String {
        try {
            val algorithm = "AES"
            val nonce = "asdfghjkl"
            val key = "120c9b9d7293186fa8c34598d47c804a5467cb5fed14447b7d17df53e1a40402"
            val mode = "AES/GCM/NoPadding"
            val cipher = Cipher.getInstance(mode)
            val parameterSpec = GCMParameterSpec(128, nonce.toByteArray())
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(gcm(key), algorithm), parameterSpec)
            val data = cipher.doFinal(bytes)
            return String(data)
        }
        catch (error:Exception) {
            error.printStackTrace()
        }
        return ""
    }
    private fun gcm(key:String): ByteArray {
        val keys = ByteArray(key.length / 2)
        var j = 0
        var i = 0
        while (i < key.length) {
            keys[j++] = key.substring(i, i + 2).toInt(16).toByte()
            i += 2
        }
        val s = StringBuilder()
        for (key in keys) {
            s.append(String.format("%02x", key))
        }
        return keys
    }
}


sealed class Status
object Invalid : Status()
object None : Status()
