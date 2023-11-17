package com.gaming.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * 全屏
 */
@Composable
fun FullScreen(modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
    val controller = rememberSystemUiController()
    //隐藏导航栏和状态栏
    DisposableEffect(controller) {
        controller.isSystemBarsVisible = false
        controller.setSystemBarsColor(Color.Transparent)
        controller.setNavigationBarColor(Color.Transparent)
        onDispose {
            controller.isSystemBarsVisible = false
            controller.setNavigationBarColor(Color.Transparent)
        }
    }
    Box(
        modifier = modifier
    ) {
        content.invoke(this)
    }
}

@Composable
fun ComposeText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, modifier = modifier, fontSize = 28.sp)
}