package com.ltd_tech.core.utils.storage

/**
 * sdk 保存的kv存储
 * author: Kaos
 * created on 2023/5/12
 */
object SdkKV {

    /**
     * 设置是否跟随系统亮度
     */
    fun setAutoBrightness(isAuto: Boolean) {
        TPS.set(shared_read_is_brightness_auto, isAuto)
    }

    /**
     * 获取设置亮度状态 默认 false
     */
    fun isBrightnessAuto() = TPS.get(shared_read_is_brightness_auto, false)

    /**
     * 设置亮度
     */
    fun setBrightness(progress : Int) {
        TPS.set(shared_read_brightness, progress)
    }

    /**
     * 获取当前亮度
     */
    fun getBrightness() = TPS.get(shared_read_brightness, 40)

    /**
     * 获取当前是否全屏状态
     */
    fun isFullScreen() = TPS.get(shared_read_is_full_screen, true)
    
    /**
     * 设置当前全屏状态
     */
    fun setFullScreen(isFullScreen : Boolean){
        TPS.set(shared_read_is_full_screen, isFullScreen)
    }

    /**
     * 阅读过程中是否使用音量键控制翻页
     */
    fun isVolumeTurnPage()= TPS.get(shared_read_is_volume_turn_page, true)

    /**
     * 设置阅读过程中是否使用音量键控制翻页
     */
    fun setVolumeTurnPage(isTurn : Boolean){
        TPS.set(shared_read_is_volume_turn_page, isTurn)
    }

    /**
     * 获取亮度模式
     */
    fun isNightMode() = TPS.get(shared_read_night_mode, true)

    /**
     * 设置亮度模式
     */
    fun setNightMode(isNight : Boolean){
        TPS.set(shared_read_night_mode, isNight)
    }

}

// 是否跟随系统亮度key
internal const val shared_read_is_brightness_auto = "shared_read_is_brightness_auto"
internal const val shared_read_is_full_screen = "shared_read_is_full_screen"
internal const val shared_read_is_volume_turn_page = "shared_read_is_volume_turn_page"
internal const val shared_read_night_mode = "shared_night_mode"
internal const val shared_read_brightness = "shared_read_brightness"