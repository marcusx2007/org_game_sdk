package com.gaming.core.pub

import com.gaming.core.pub.impl.SDKCallerImpl

internal class SDKCallerProxy :SDKCaller by SDKCallerImpl(){
}