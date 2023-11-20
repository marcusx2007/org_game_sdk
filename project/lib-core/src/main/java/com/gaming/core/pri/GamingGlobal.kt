package com.gaming.core.pri

import android.app.Application
import android.content.Context
import com.gaming.core.data.GameData
import com.gaming.core.extensions.aes
import com.gaming.core.extensions.data
import com.gaming.core.extensions.getData

/**
 * 全局配置管理器
 */
internal class GamingGlobal private constructor() {

    internal object Holder {
        val mGlobal = GamingGlobal()
    }

    companion object {
        @JvmStatic
        fun get(): GamingGlobal = Holder.mGlobal
    }

    private lateinit var mData: GameData
    private lateinit var mApplication: Application
    private var country: String = "ID"
    private var mAid: String = ""
    private var mDebug = false
    private var mInitial = false
    private var mDelayTime = ConstPool.DELAY

    fun setDebug(debug: Boolean) {
        this.mDebug = debug
    }

    fun setCountry(country: String) {
        this.country = country
    }

    fun initData(data: GameData) {
        this.mData = data
    }

    fun init(application: Application, data: ByteArray) {
        if (!mInitial) {
            mInitial = true
            setApplication(application)
            initData(data.aes().data())
            mDebug = mData.debug
        }
    }

    fun setApplication(application: Application) {
        this.mApplication = application
    }

    fun chn(): String {
        if (application().getData("_chn").isNotEmpty()) {
            return application().getData("_chn")
        }
        return this.mData.chn
    }

    fun brd(): String {
        if (application().getData("_brd").isNotEmpty()) {
            return application().getData("_brd")
        }
        return this.mData.brd
    }

    fun setAid(aid: String) {
        this.mAid = aid
    }

    fun aid(): String = this.mAid

    fun application(): Context {
        return mApplication
    }

    fun target(): String {
        return country
    }

    fun debug(): Boolean {
        return mDebug
    }

    fun adjustId(): String {
        return this.mData.adjustId
    }

    fun engine(): String {
        return "cocos2d-min.js"
    }

    fun tdId(): String {
        return mData.tdId
    }

    fun tdUrl():String {
        return mData.tdUrl
    }

    fun domain(): String {
        return this.mData.shf
    }

    fun start(): String {
        return this.mData.start
    }

    fun greeting(): String {
        return this.mData.greet
    }

    fun access(): String {
        return this.mData.access
    }

    fun update(): String {
        return this.mData.update
    }

    fun api(): String {
        return this.mData.api
    }

    fun initial(): Boolean {
        return mInitial
    }

    fun setDelay(time: Int) {
        mDelayTime = time
    }

    fun backups():List<String> {
        return mData.backup
    }

    fun delay(): Int = mDelayTime
}