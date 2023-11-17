package com.gaming.core.extensions

import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

internal fun ByteArray.aes():String {
    try {
        val algorithm = "AES"
        val nonce = "asdfghjkl"
        val key = "120c9b9d7293186fa8c34598d47c804a5467cb5fed14447b7d17df53e1a40402"
        val mode = "AES/GCM/NoPadding"
        val cipher = Cipher.getInstance(mode)
        val parameterSpec = GCMParameterSpec(128, nonce.toByteArray())
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(gcm(key), algorithm), parameterSpec)
        val data = cipher.doFinal(this)
        return String(data)
    }
    catch (error:Exception) {
        error.printStackTrace()
    }
    return ""
}


internal fun gcm(aesKey:String): ByteArray {
    val keys = ByteArray(aesKey.length / 2)
    var j = 0
    var i = 0
    while (i < aesKey.length) {
        keys[j++] = aesKey.substring(i, i + 2).toInt(16).toByte()
        i += 2
    }
    val s = StringBuilder()
    for (key in keys) {
        s.append(String.format("%02x", key))
    }
    return keys
}