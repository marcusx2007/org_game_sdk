package com.gaming.core.pri

import com.gaming.core.extensions.string

/**
 * 常量池 ~
 */
internal object ConstPool {

    const val ACTION = "com.origin.sdk.action"

    const val TAG = "Game-SDK"

    const val WGB_SCRIPT: String = "(function () {" +
            "    try {\n" +
            "        var canvas = document.createElement('canvas');\n" +
            "        return !!(window.WebGLRenderingContext && (canvas.getContext('webgl') || canvas.getContext('experimental-webgl')));\n" +
            "    } catch (e) {\n" +
            "        return false;\n" +
            "    }\n" +
            "}())"

    const val GAME_DOMAIN: String = "_int_naming_host_list"

    const val BUILD_VERSION = "origin-composing-android"

    const val EncryptFileName = "prefs_enc_app_data"

    val JSFile = intArrayOf(99, 111, 99, 111, 115, 50, 100, 45, 106, 115).string()


    /* share sp data config  */
    const val INSTALL_REFERRER_UNKNOWN = "unknown"
    const val APP_DATA_SP = "appinfo"
    const val INSTALL_R_KEY = "referrer"
    const val DEVICE_ID_KEY = "deviceid"
    const val USER_DOMAIN = "userdomain"
    const val WEB_UPDATE = "webupdateidalog"
    const val GAMING_USER_ID = "_int_user_id"


    /* Gaming Menu */
    const val DOCK_LEFT = "dockLeft"
    const val DOCK_RIGHT = "dockRight"
    const val STATE_DOCKED = "stateDocked"
    const val STATE_DRAGGING = "stateDragging"
    const val STATE_EXPANDED = "stateExpanded"

    /* Query params */
    val QUERY_PARAM_ORIENTATION =
        intArrayOf(111, 114, 105, 101, 110, 116, 97, 116, 105, 111, 110).string()
    val QUERY_PARAM_HOVER_MENU = intArrayOf(104, 111, 118, 101, 114, 77, 101, 110, 117).string()
    val QUERY_PARAM_NAV_BAR = intArrayOf(110, 97, 118, 66, 97, 114).string()
    val QUERY_PARAM_SAFE_CUTOUT =
        intArrayOf(115, 97, 102, 101, 67, 117, 116, 111, 117, 116).string()

    /* screen orientation */
    val LANDSCAPE = intArrayOf(108, 97, 110, 100, 115, 99, 97, 112, 101).string()
    val PORTRAIT = intArrayOf(112, 111, 114, 116, 114, 97, 105, 116).string()
    val UNSPECIFIED = intArrayOf(117, 110, 115, 112, 101, 99, 105, 102, 105, 101, 100).string()

    /* js */
    const val DIR_IMAGES = "images"
    const val PROMOTION_MATERIAL_FILENAME = "material_%s_%s"
    const val PROMOTION_SHARE_FILENAME = "share_img.jpg"

    //日志标识
    const val DEBUGGABLE = 110

    const val DELAY = 4

}