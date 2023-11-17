package com.gaming.core.compose

import android.app.Activity
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.gaming.core.pri.ConstPool
import com.gaming.core.pri.GamingGlobal
import com.gaming.core.utils.LogUtils
import com.gaming.core.network.CoreImpl
import com.gaming.core.extensions.setData
import com.gaming.core.analysi.AdjustManager
import com.gaming.core.extensions.getDeviceId
import com.gaming.core.extensions.installReferrer
import com.gaming.core.network.AbstractNetworkCallImpl
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


/**
 * 主页面UI
 */
@Composable
fun ComposeCoreContent(activity: Activity, box: BoxScope) = box.run {
    //track
    var track by remember { mutableStateOf(false) }
    //url
    var component by remember { mutableStateOf("") }
    //targetCountry
    var target by remember { mutableStateOf("") }

    var init by remember {
        mutableStateOf(false)
    }
    if (!init) {
        //设备ID
        GameIdComponent()
    }
    //归因
    if (!init) {
        ReferComponent()
    }
    //追踪
    if (track) {
        LogUtils.d("~", "track:$component")
        TrackComponent()
    }
    //targetCountry
    if (target.isNotEmpty()) {
        LogUtils.d("~", "target:$target")
        TargetComponent(target = target)
    }
    val invocation = remember {
        { it: Triple<String, String, Boolean> ->
            //component = "https://game.dominocash66.com/"
            component = "https://csh5.idn-zenam.com/customerServicePage/?token=0a22a442967c4b25b5070f866feec214&user_id=100233648&orientation=portrait&hoverMenu=false&safeCutout=true&code=id&aid=57d322f9f2d2df70&level=0&brand=int&chn=website&game_version=1.10.1&platform=a&orientation=portrait&hoverMenu=true"
//            component = it.first
            target = it.second
            track = it.third
        }
    }
    //使用重组调用api~
    if (init) {
        CallComponent(invocationHandler = invocation)
    }
    if (!init) {
        InitAtomizerComponent {
            init = true
        }
    }

    if (init) {
        if (component.isNotEmpty()) {
            //GameCoreComponent(this, url = "https://game.idn-zenam.com/cocos-module-idn/all/646/")
            val suffix = (97..122).let {
                val jsb = StringBuilder()
                val count = (6..12).random()
                for (i in 0 until count) jsb.append(it.random().toChar())
                jsb.toString()
            }
            LogUtils.d("~", "suffix=$suffix")
            GameCoreComponent(activity,this, suffix, component.convertUrls("jsb" to "$suffix.post"))
        } else {
            //TODO 跳转A面
        }
    }
}

@Composable
internal fun CallComponent(
    invocationHandler: (Triple<String, String, Boolean>) -> Unit
) {
    val deffer = remember {
        CompletableDeferred<Triple<String, String, Boolean>>()
    }
    LaunchedEffect(key1 = false, block = {
        val triple = withContext(Dispatchers.IO) {
            call(deffer)
        }
        withContext(Dispatchers.Main) {
            invocationHandler.invoke(triple)
        }
    })
}

//执行api请求~
internal suspend fun call(deferred: CompletableDeferred<Triple<String, String, Boolean>>): Triple<String, String, Boolean> {
    LogUtils.d("~", "deferred=$deferred")
    CoreImpl().interview(GamingGlobal.get().application(),object : AbstractNetworkCallImpl() {
        override fun d(): Long {
            return 4
        }
        override fun c(json: String) {
            LogUtils.d("~", "[result-data]:$json")
            if (json.isEmpty()) {
                deferred.complete(Triple("", "", false))
            } else {
                val data = JSONObject(json)
                if (data.optBoolean("rst")) {
                    GamingGlobal.get().application().setData(ConstPool.GAME_DOMAIN, "")
                }
                val triple = Triple(
                    data.optString("usr"),
                    data.optString("cty"),
                    data.optString("usr").isNotEmpty()
                )
                LogUtils.d("~", "[result-data]:triple=$triple,deferred=$deferred")
                deferred.complete(triple)
            }
        }
    })
    return deferred.await()
}

@Composable
internal fun TargetComponent(target: String) {
    GamingGlobal.get().setCountry(target)
}

/**
 * 归因组件~10:45:23.981
 */
@Composable
internal fun ReferComponent() {
    LaunchedEffect(key1 = Unit, block = {
        LogUtils.d("~", "referrer call.")
        val installer = GamingGlobal.get().application().installReferrer()
        LogUtils.d("~", "auto get refer:$installer")
    })
}


@Composable
internal fun InitAtomizerComponent(onComplete: () -> Unit) {
    val scope = rememberCoroutineScope()
    DisposableEffect(key1 = false, effect = {
        val job = scope.launch(Dispatchers.Main) {
            delay(50)
            onComplete.invoke()
        }
        onDispose {
            job.cancel()
        }
    })
}

@Composable
internal fun TrackComponent() {
    LogUtils.d("~", "auto track adjust~")
    AdjustManager.get().trackEventStart(GamingGlobal.get().start())
    AdjustManager.get().trackEventAccess(GamingGlobal.get().access())
    AdjustManager.get().trackEventGreeting(GamingGlobal.get().greeting())
}

@Composable
internal fun GameIdComponent() {
    LaunchedEffect(key1 = Unit) {
        LogUtils.d("~", "android id call.")
        val deviceId = GamingGlobal.get().application().getDeviceId()
        LogUtils.d("~", "auto get game-id:$deviceId")
    }
}