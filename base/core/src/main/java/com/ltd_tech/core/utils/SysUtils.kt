package com.ltd_tech.core.utils

import android.R
import android.app.Activity
import android.view.View

/**
 * 系统相关操作工具类
 * author: Kaos
 * created on 2023/5/14
 */
object SysUtils {

    private const val EXPAND_STATUS = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

    private const val STABLE_STATUS = View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

    private const val STABLE_NAV = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

    private const val UNSTABLE_STATUS = View.SYSTEM_UI_FLAG_FULLSCREEN
    private const val UNSTABLE_NAV = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

    fun transparentStatusBar(activity: Activity) {
        expandStatusBar(activity)
        activity.window.statusBarColor = activity.resources.getColor(R.color.transparent)
    }

    /**
     * 视图扩充到StatusBar
     */
    fun expandStatusBar(activity: Activity) {
        setFlag(activity, EXPAND_STATUS)
    }

    /**
     * 隐藏系统状态栏
     */
    fun hideStableStatusBar(activity: Activity) {
        //App全屏，隐藏StatusBar
        setFlag(activity, STABLE_STATUS)
    }

    fun hideStableNavBar(activity: Activity) {
        //App全屏，隐藏StatusBar
        setFlag(activity, STABLE_NAV)
    }

    fun showUnStableStatusBar(activity: Activity) {
        clearFlag(activity, UNSTABLE_STATUS)
    }

    fun showUnStableNavBar(activity: Activity) {
        clearFlag(activity, UNSTABLE_NAV)
    }

    fun showStableNavBar(activity: Activity) {
        clearFlag(activity,STABLE_NAV)
    }

    fun setFlag(activity: Activity, flag: Int) {
        val decorView = activity.window.decorView
        val option = decorView.systemUiVisibility or flag
        decorView.systemUiVisibility = option
    }

    //取消flag
    fun clearFlag(activity: Activity, flag: Int) {
        val decorView = activity.window.decorView
        val option = decorView.systemUiVisibility and flag.inv()
        decorView.systemUiVisibility = option
    }
}