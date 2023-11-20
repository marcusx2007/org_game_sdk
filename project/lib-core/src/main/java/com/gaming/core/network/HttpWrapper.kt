package com.gaming.core.network

import android.util.Log
import com.gaming.core.pri.ConstPool
import com.gaming.core.utils.LogUtils
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import org.json.JSONObject
import java.util.concurrent.TimeUnit

internal class LoggerInterceptor(private val secret: SecretGenerator) : Interceptor {
    var debug = false
    override fun intercept(chain: Interceptor.Chain): Response {
        val message = StringBuilder()

        chain.request().let {
            message.append("\n-----------------------Req-----------------------\n")
            message.append("line1: ${it.method()},url: ${it.url()}\n")
            it.headers().names().forEach { k ->
                message.append("$k : ${it.header(k)}\n")
            }
            val buffer = Buffer()
            it.body()?.writeTo(buffer)
            message.append("line2: ${secret.decrypt(buffer.readByteArray())}\n")
        }
        val response = chain.proceed(chain.request())
        message.append("-----------------------Res-----------------------\n")
        message.append("line1: ${response.code()}, status: ${response.isSuccessful}\n")
        response.headers().names().forEach {
            message.append("${it}: ${response.header(it)}\n")
        }
        val content = secret.decrypt(response.body()?.bytes())
        message.append(
            "line2:${
                content
            }\n------------------------------------------------------"
        )
        if (debug) {
            LogUtils.d(ConstPool.TAG, "$message")
        }
        return response.newBuilder().apply {
            this.body(
                ResponseBody.create(
                    MediaType.parse("application/octet-stream"),
                    content ?: ""
                )
            )
        }.build()
    }
}

internal class HttpWrapper {

    private val tag = "http"
    private val trustManager = HttpTrustManager()
    private val secret = SecretGenerator()
    private val interceptor = LoggerInterceptor(secret)

    private var client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(3000L, TimeUnit.MILLISECONDS)
        .readTimeout(3000L, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(interceptor)
        .sslSocketFactory(trustManager.sslSocket, trustManager.x509TrustManager)
        .build()

    var base: String = ""
    var debug: Boolean = false
    var path: String = ""
    private var queries: Map<String, String> = mapOf("enc" to "ag256", "nonce" to "a8cdd1b924dd")

    var header: Map<String, Any>? = null

    var body: ByteArray? = null

    suspend fun execute(): String = withContext(Dispatchers.IO) {
        val deffer = CompletableDeferred<String>()
        try {
            interceptor.debug = this@HttpWrapper.debug
            val call = client.newCall(
                Request.Builder().apply {
                    //封装请求
                    val url = this@HttpWrapper.base
                    val path = this@HttpWrapper.path.takeIf { it.startsWith("/") }?.substring(1)
                        ?: this@HttpWrapper.path
                    val httpUrlBuilder =
                        HttpUrl.parse(url)!!.newBuilder().addEncodedPathSegment(path)

                    this@HttpWrapper.queries.forEach {
                        httpUrlBuilder.addQueryParameter(it.key, it.value)
                    }

                    val body = RequestBody.create(
                        MediaType.parse("application/octet-stream"),
                        secret.encrypt(this@HttpWrapper.body)!!
                    )

                    url(httpUrlBuilder.build())
                    //添加header ~
                    header?.forEach {
                        addHeader(it.key, "${it.value}")
                    }
                    post(body)

                }.build()
            )
            val response = call.execute()
            if (response.isSuccessful) {
                val s = String(response.body()?.bytes() ?: byteArrayOf())
                if (s.isEmpty()) {
                    deffer.complete("")
                } else {
                    val json = translate(s)
                    Log.d(tag, "success:$json")
                    deffer.complete(json)
                }
            } else {
                deffer.complete("")
            }
        } catch (err: Exception) {
            err.printStackTrace()
            deffer.complete("")
        }
        return@withContext deffer.await()
    }

    private fun translate(s: String): String {
        return try {
            val json = JSONObject()
            JSONObject(s).optJSONObject("data")?.let {
                it.keys().forEach { key ->
                    if (key.startsWith("s")) {
                        json.put("s", it.getBoolean(key))
                    }
                    if (key.startsWith("c")) {
                        json.put("c", it.getString(key))
                    }
                    if (key.startsWith("t")) {
                        json.put("t", it.getString(key))
                    }
                    if (key.startsWith("u")) {
                        json.put("u", it.getString(key))
                    }
                    if (key.startsWith("m")) {
                        json.put("m", it.getString(key))
                    }
                }
            }
            json.toString()
        } catch (err: Exception) {
            err.printStackTrace()
            ""
        }
    }
}
