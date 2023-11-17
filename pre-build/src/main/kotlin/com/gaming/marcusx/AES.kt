package com.gaming.marcusx

import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object AES {

    private val algorithm = "AES"
    private val nonce = "asdfghjkl"
    private val key = "120c9b9d7293186fa8c34598d47c804a5467cb5fed14447b7d17df53e1a40402"
    private val mode = "AES/GCM/NoPadding"

    @JvmStatic
    fun encrypt(data: ByteArray?): ByteArray? {
        return try {
            val cipher = Cipher.getInstance(mode)
            val parameterSpec = GCMParameterSpec(128, nonce.toByteArray())
            cipher.init(Cipher.ENCRYPT_MODE,SecretKeySpec(gcm(), algorithm) , parameterSpec)
            cipher.doFinal(data)
        } catch (err: Exception) {
            err.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun decrypt(data: ByteArray?): String? {
        return try {
            val parameterSpec = GCMParameterSpec(128, nonce.toByteArray())
            val cipher = Cipher.getInstance(mode)
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(gcm(), algorithm), parameterSpec)
            String(cipher.doFinal(data))
        } catch (err: Exception) {
            err.printStackTrace()
            null
        }
    }

    @JvmStatic
    private fun gcm(): ByteArray {
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