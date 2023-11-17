package com.gaming.core.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.gaming.core.utils.WindowConfig

open class BaseComposeActivity:ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowConfig.hideVirtualButton(window)
        super.onCreate(savedInstanceState)
        setContent {
            FullScreen(rootModifier()) {
                this.Content()
            }
        }
    }
    /**
     * 根视图的布局参数.
     */
    open fun rootModifier(): Modifier {
        return Modifier
            .fillMaxSize()
            .background(Color.White)
    }

    /**
     * 子View
     */
    @Composable
    open fun BoxScope.Content() {

    }

    override fun onDestroy() {
        super.onDestroy()
    }
}