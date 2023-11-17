package com.gaming.core.pub

import com.gaming.core.pub.impl.SDKInitialImpl

internal class SDKInitialProxy() : SDKInitial by SDKInitialImpl() {
}