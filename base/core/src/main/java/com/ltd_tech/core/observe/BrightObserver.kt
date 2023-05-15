package com.ltd_tech.core.observe

import android.app.Activity
import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.Settings
import com.ltd_tech.core.utils.BrightnessUtils
import com.ltd_tech.core.utils.L
import com.ltd_tech.core.utils.getCurrentActivity
import com.ltd_tech.core.utils.storage.SdkKV

/**
 * 由于亮度调节没有 Broadcast 而是直接修改 ContentProvider 的。
 * 所以需要创建一个 Observer 来监听 ContentProvider 的变化情况。
 * author: Kaos
 * created on 2023/5/12
 */
class BrightObserver(private val handler : Handler) : ContentObserver(handler) {

    // 注册 Brightness 的 uri
    private val BRIGHTNESS_MODE_URI =
        Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE)

    private val BRIGHTNESS_URI = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS)

    private val BRIGHTNESS_ADJ_URI = Settings.System.getUriFor("screen_auto_brightness_adj")

    private val tag = BrightObserver::class.java.simpleName

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        onChange(selfChange, null)
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        // 判断当前是否跟随屏幕亮度，如果不是则返回
        if (selfChange || !SdkKV.isBrightnessAuto()){
            return
        }
        if (BRIGHTNESS_MODE_URI.equals(uri)){
            L.il(tag,"亮度模式改变")
        } else if (BRIGHTNESS_URI.equals(uri) && !BrightnessUtils.isAutoBrightness()){
            L.il(tag,"亮度模式为手动模式 值改变")
        } else if(BRIGHTNESS_ADJ_URI.equals(uri) && BrightnessUtils.isAutoBrightness()){
            L.il(tag,"亮度模式为自动模式 值改变")
            BrightnessUtils.setDefaultBrightness(getCurrentActivity())
        } else {
            L.il(tag,"亮度调整 其他")
        }
    }

    /**
     * 注册屏幕亮度广播
     */
    fun registerBrightObserver(activity: Activity){
        val cr: ContentResolver = activity.contentResolver
        cr.unregisterContentObserver(this)
        cr.registerContentObserver(BRIGHTNESS_MODE_URI, false, this)
        cr.registerContentObserver(BRIGHTNESS_URI, false, this)
        cr.registerContentObserver(BRIGHTNESS_ADJ_URI, false, this)
    }

    /**
     * 解注册屏幕亮度广播
     */
    fun unregisterBrightObserver(activity: Activity){
        if (!activity.isFinishing){
            activity.contentResolver.unregisterContentObserver(this)
        }
    }

}


