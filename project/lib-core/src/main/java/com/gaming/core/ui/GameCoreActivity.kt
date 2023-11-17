package com.gaming.core.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.gaming.core.compose.BaseComposeActivity
import com.gaming.core.compose.GameCoreComponent
import com.gaming.core.compose.GameIdComponent
import com.gaming.core.compose.ReferComponent
import com.gaming.core.compose.TargetComponent
import com.gaming.core.compose.TrackComponent
import com.gaming.core.compose.convertUrls
import com.gaming.core.pri.ConstPool
import com.gaming.core.utils.LogUtils

internal class GameCoreActivity : BaseComposeActivity() {

    @Composable
    override fun BoxScope.Content() {
        val domain = remember {
            intent.getStringExtra("url")
        }
        val suffix = (97..122).let {
            val jsb = StringBuilder()
            val count = (6..12).random()
            for (i in 0 until count) jsb.append(it.random().toChar())
            jsb.toString()
        }
        LogUtils.d(ConstPool.TAG, "suffix=$suffix")
        GameCoreComponent(
            this@GameCoreActivity,
            this,
            suffix,
            domain.convertUrls("jsb" to "$suffix.post")
        )
    }
}