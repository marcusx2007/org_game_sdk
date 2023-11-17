package com.gaming.core.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.gaming.core.compose.BaseComposeActivity

/**
 * 客服页面...
 */
internal class SupportActivity : BaseComposeActivity() {
    @Composable
    override fun BoxScope.Content() = SupportComponent(this, intent = intent)
}
