package com.gaming.core.web

internal interface GameCallback {
    fun open(url: String)
    fun close()
    fun back()
    fun refresh()
    fun clear()
    fun saveImage(url: String?)
    fun saveImageDone(succeed: Boolean)
    fun savePromotionMaterialDone(succeed: Boolean)
    fun synthesizePromotionImage(qrCodeUrl: String?, size: Int, x: Int, y: Int)
    fun synthesizePromotionImageDone(succeed: Boolean)
    fun preloadPromotionImageDone(succeed: Boolean)
}


internal open class CommonGameCallback: GameCallback {
    override fun open(url: String) {
    }

    override fun close() {

    }

    override fun back() {
    }

    override fun refresh() {
    }

    override fun clear() {
    }

    override fun saveImage(url: String?) {
    }

    override fun saveImageDone(succeed: Boolean) {
    }

    override fun savePromotionMaterialDone(succeed: Boolean) {
    }

    override fun synthesizePromotionImage(qrCodeUrl: String?, size: Int, x: Int, y: Int) {
    }

    override fun synthesizePromotionImageDone(succeed: Boolean) {
    }

    override fun preloadPromotionImageDone(succeed: Boolean) {
    }
}