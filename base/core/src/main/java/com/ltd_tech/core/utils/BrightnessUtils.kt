package com.ltd_tech.core.utils

import android.app.Activity
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.view.WindowManager

/**
 * 亮度控制工具类
 * author: Kaos
 * created on 2023/5/12
 */
object BrightnessUtils {

    /**
     * 判断是否开启了自动亮度调节
     */
    fun isAutoBrightness(): Boolean = try {
        Settings.System.getInt(
            application?.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE
        ) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
    } catch (e: SettingNotFoundException) {
        e.printStackTrace()
        false
    }

    /**
     * 获取手动模式下的屏幕亮度
     * * @return value:0~255
     */
    fun getManualScreenBrightness(): Int = try {
        Settings.System.getInt(application?.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }

    /**
     * 获取自动模式下的屏幕亮度
     * @return value:0~255
     */
    fun getAutoScreenBrightness(): Int = try {
        //获取自动调节下的亮度范围在 0~1 之间
        //转换范围为 (0~255)
        (Settings.System.getFloat(
            application?.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS
        ) * 225.0f).toInt()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        0
    }

    /**
     * 获取屏幕的亮度
     * 系统亮度模式中，自动模式与手动模式获取到的系统亮度的值不同
     */
    fun getScreenBrightness(): Int = if (isAutoBrightness()) {
        getAutoScreenBrightness()
    } else {
        getManualScreenBrightness()
    }

    /**
     * 设置亮度:通过设置 Windows 的 screenBrightness 来修改当前 Windows 的亮度
     * lp.screenBrightness:参数范围为 0~1
     */
    fun setBrightness(activity: Activity, brightness: Int) {
        try {
            val lp = activity.window.attributes
            //将 0~255 范围内的数据，转换为 0~1
            lp.screenBrightness = java.lang.Float.valueOf(brightness.toFloat()) * (1f / 255f)
            activity.window.attributes = lp
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * 获取当前系统的亮度
     */
    fun setDefaultBrightness(activity: Activity?) {
        try {
            activity?.run {
                val lp = window.attributes
                lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                window.attributes = lp
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

}
