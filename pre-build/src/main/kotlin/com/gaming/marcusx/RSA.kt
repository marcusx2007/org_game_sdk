package com.gaming.marcusx

import java.nio.charset.Charset
import java.security.KeyPairGenerator
import java.security.Signature
import java.util.Base64
import javax.crypto.Cipher

object RSA {
    @JvmStatic
    fun a() {
        val data = "{hello:123,world:456}"
        //1.初始化RSA秘钥
        val rsa = KeyPairGenerator.getInstance("RSA")
        rsa.initialize(4096)
        val key = rsa.genKeyPair()


        //2.公钥加密
        val pubKey = key.public
        val priKey = key.private

        println("公钥: ${Base64.getEncoder().encodeToString(pubKey.encoded)}")
        println("私钥: ${Base64.getEncoder().encodeToString(priKey.encoded)}")

        val pubCipher = Cipher.getInstance("RSA")
        pubCipher.init(Cipher.ENCRYPT_MODE,pubKey)
        val eData = pubCipher.doFinal(data.toByteArray(Charset.defaultCharset()))
        val base64EData= Base64.getEncoder().encodeToString(eData)
        println("rsa-encrypted: $base64EData")

        //3.私钥解密
        val priCipher = Cipher.getInstance("RSA")
        priCipher.init(Cipher.DECRYPT_MODE, priKey)
        val dData = priCipher.doFinal(Base64.getDecoder().decode(base64EData))
        println("rsa-decrypted: ${String(dData)}")

        //4.使用私钥对原始数据签名
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(priKey)
        signature.update(data.toByteArray())
        val signedBytes = signature.sign()
        val signBase64 = Base64.getEncoder().encode(signedBytes)
        println("signed: ${String(signBase64)}")

        //5.使用公钥对签名验证
        val signatureToVerify = Signature.getInstance("SHA256withRSA")
        signatureToVerify.initVerify(pubKey)
        signatureToVerify.update(dData)
        val unSignBase64 = Base64.getDecoder().decode(signBase64)
        println("签名验证情况: ${signatureToVerify.verify(unSignBase64)}")

    }
}