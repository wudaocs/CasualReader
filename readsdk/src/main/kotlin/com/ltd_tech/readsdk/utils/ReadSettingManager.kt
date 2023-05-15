package com.ltd_tech.readsdk.utils

import com.ltd_tech.core.utils.storage.SdkKV

/**
 * 阅读设置属性管理
 * author: Kaos
 * created on 2023/5/12
 */
object ReadSettingManager {

    /**
     * 亮度是否随着系统变化
     */
    fun isBrightnessAuto() = SdkKV.isBrightnessAuto()

    fun getBrightness() = SdkKV.getBrightness()

    /**
     * 获取当前全屏状态
     */
    fun isFullScreen() = SdkKV.isFullScreen()

    fun isVolumeTurnPage() = SdkKV.isVolumeTurnPage()

    fun isNightMode() = SdkKV.isNightMode()
}