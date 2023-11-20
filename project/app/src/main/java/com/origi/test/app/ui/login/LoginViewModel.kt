package com.origi.test.app.ui.login


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gaming.core.GameSDK
import com.gaming.core.extensions.setData
import com.origi.test.app.data.LoginRepository
import java.net.URL
import java.net.URLDecoder

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    val status = MutableLiveData<Status>(None)
    private val _status by this::status

    fun login(context: LoginActivity, chn: String, brd: String, domain: String) {
        val map = parseQueryParameters(domain)
        context.setData("_chn", chn)
        context.setData("_brd", map["brd"]?.takeIf { it.isNotEmpty() } ?: brd)
        context.setData("_domain", domain)
        GameSDK.start(context) {
            _status.value = Invalid
        }
    }


    /**
     * 解析url中的查询参数.
     */
    private fun parseQueryParameters(urlString: String): Map<String, String> {
        val queryParameters = mutableMapOf<String, String>()
        try {
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

}


sealed class Status
object Invalid : Status()
object None : Status()
