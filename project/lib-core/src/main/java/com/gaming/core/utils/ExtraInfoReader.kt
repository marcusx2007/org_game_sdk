package com.gaming.core.utils

import org.json.JSONException
import org.json.JSONObject
import java.io.File

internal object ExtraInfoReader {
    /**
     * easy api for get extra info.<br></br>
     *
     * @param apkFile apk file
     * @return null if not found
     */
    operator fun get(apkFile: File?): ExtraInfo? {
        val result = getMap(apkFile) ?: return null
        return ExtraInfo(result)
    }

    /**
     * get extra info by map
     *
     * @param apkFile apk file
     * @return null if not found
     */
    fun getMap(apkFile: File?): Map<String, String>? {
        try {
            val rawString = getRaw(apkFile) ?: return null
            val jsonObject = JSONObject(rawString)
            val keys: Iterator<*> = jsonObject.keys()
            val result: MutableMap<String, String> = HashMap()
            while (keys.hasNext()) {
                val key = keys.next().toString()
                result[key] = jsonObject.getString(key)
            }
            return result
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * get raw string from block
     *
     * @param apkFile apk file
     * @return null if not found
     */
    fun getRaw(apkFile: File?): String? {
        return PayloadReader.getString(apkFile, ApkUtil.EXTRA_INFO_BLOCK_ID)
    }


    /**
     * 写入signing block的额外信息
     */
    class ExtraInfo(private val extraInfo: Map<String, String>?) {
        val referID: String?
            get() = if (extraInfo == null || !extraInfo.containsKey(KEY_REFER_ID)) {
                ""
            } else extraInfo[KEY_REFER_ID]
        val agentID: String?
            get() = if (extraInfo == null || !extraInfo.containsKey(KEY_AGENT_ID)) {
                ""
            } else extraInfo[KEY_AGENT_ID]

        companion object {
            const val KEY_REFER_ID = "refer_id" // 线上推广员
            const val KEY_AGENT_ID = "agent_id" // 线下代理
        }
    }
}